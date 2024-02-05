package io.elijahlamoon.dott.overengineered

import cats.effect.kernel.Resource
import cats.syntax.apply._
import cats.syntax.traverse._
import cats.{effect => ce}
import com.typesafe.config.ConfigFactory
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.implicits.toConnectionIOOps
import doobie.util.ExecutionContexts
import fly4s.Fly4s
import fly4s.data.Fly4sConfig
import fly4s.data.Location

import java.util.UUID
import scala.annotation.nowarn

import dto.{OrderDto, ItemDto, ProductDto}
import dto.OrdersProductsDto

object Database {
  private val cfg = ConfigFactory.defaultApplication()
  private val driver = cfg.getString("driverClassName")
  private val url = cfg.getString("jdbcUrl")

  private val transactor: Resource[ce.IO, Transactor[ce.IO]] = for {
    ec <- ExecutionContexts.fixedThreadPool[ce.IO](1)
    xa <- HikariTransactor.newHikariTransactor[ce.IO](
      driverClassName = driver,
      url = url,
      user = "",
      pass = "",
      ec
    )
  } yield xa

  val migrateDb = Fly4s
    .make[ce.IO](
      url = url,
      user = None,
      password = None,
      config = Fly4sConfig(
        failOnMissingLocations = true,
        locations = List(Location("migrations"))
      )
    )
    .evalMap(_.migrate)

  import schema._
  import schema.ctx._

  // ------- Products -------
  def getProducts(filterPredicate: ProductDto => Boolean = _ => true) =
    stream(products)
      .filter(filterPredicate)
      .map(ProductDto.toModel)

  def insertProduct(product: model.Product) = run {
    products.insertValue(lift(ProductDto.fromModel(product)))
  }

  def deleteProduct(uuid: model.Product.ProductUuid) = run {
    products.filter(_.uuid == lift(uuid.value)).delete
  }
  // ------------------------

  // ------- Items -------
  def getItems(filterPredicate: ItemDto => Boolean = _ => true) =
    stream(items)
      .filter(filterPredicate)
      .flatMap { itemDto =>
        getProducts(_.uuid == itemDto.productUuid)
          .map(product => ItemDto.toModel(itemDto, product))
      }

  def getItemsMoreEfficiently(
      itemPredicate: ItemDto => Boolean = _ => true,
      productPredicate: ProductDto => Boolean = _ => true
  ) =
    stream {
      items join products on (_.productUuid == _.uuid)
    }
      .filter { case (itemDto, productDto) =>
        itemPredicate(itemDto) && productPredicate(productDto)
      }
      .map { case (itemDto, productDto) =>
        ItemDto.toModel(itemDto, ProductDto.toModel(productDto))
      }

  def insertItem(item: model.Item) = {
    val insertProductQuery = insertProduct(item.product)
    val insertItemQuery = run {
      items.insertValue(lift(ItemDto.fromModel(item)))
    }
    insertProductQuery *> insertItemQuery
  }

  def deleteItemByProductUuid(productUuid: model.Product.ProductUuid) = {
    val deleteProductQuery = deleteProduct(productUuid)
    val deleteItemQuery = run {
      items.filter(_.productUuid == lift(productUuid.value)).delete
    }
    deleteProductQuery *> deleteItemQuery
  }
  // ---------------------

  // ------- Orders -------
  def getOrders(filterPredicate: OrderDto => Boolean = _ => true) =
    stream(orders)
      .filter(filterPredicate)
      .flatMap { orderDto =>
        getItemsForOrder(orderDto.uuid).chunkAll
          .map(chunk => chunk.toList)
          .map(items => OrderDto.toModel(orderDto, items))
      }

  // I was having too much fun building and optimizing this query,
  // even there wasnt a big need for that
  def getOrdersMoreEfficiently(
      filterPredicate: OrderDto => Boolean = _ => true
  ) = {
    implicit val orderDtoEqByUuid: cats.Eq[OrderDto] = cats.Eq.by(_.uuid)
    stream {
      for {
        orderDto <- orders
        orderProductDto <- ordersProducts join (_.orderUuid == orderDto.uuid)
      } yield (orderDto, orderProductDto)
    }
      .filter { case (orderDto, _) => filterPredicate(orderDto) }
      .flatMap { case (orderDto, orderProductDto) =>
        stream {
          items
            .filter(_.productUuid == lift(orderProductDto.productUuid))
            .join(products)
            .on(_.productUuid == _.uuid)
        }.map { case (itemDto, productDto) =>
          orderDto -> ItemDto.toModel(itemDto, ProductDto.toModel(productDto))
        }
      }
      .groupAdjacentBy(_._1)
      .map { case (key, values) =>
        OrderDto.toModel(key, values.map(_._2).toList)
      }
  }

  def insertOrder(order: model.Order) = {
    val insertOrderProductsQuery = insertOrderProducts(order)
    val insertOrderQuery = run {
      orders.insertValue(lift(OrderDto.fromModel(order)))
    }

    insertOrderProductsQuery *>
      insertOrderQuery
  }

  def insertOrders(chunk: fs2.Chunk[model.Order]) = {
    val insertOrdersProductsQuery = insertOrdersProducts(chunk)
    val list = chunk.toList.map(OrderDto.fromModel)
    val insertOrderQuery = run {
      liftQuery(list)
        .foreach(orders.insertValue(_))
    }

    insertOrdersProductsQuery *>
      insertOrderQuery
  }

  def deleteOrder(uuid: model.Order.OrderUuid) = {
    val deleteOrderProductsQuery = deleteOrderProduct(uuid)
    val deleteOrderQuery = run {
      orders.filter(_.uuid == lift(uuid.value)).delete
    }

    deleteOrderProductsQuery *> deleteOrderQuery
  }
  // ----------------------

  // ------- Orders-Items -------
  private def getItemsForOrder(orderUuid: UUID) =
    stream(ordersProducts.filter(_.orderUuid == lift(orderUuid)))
      .flatMap(orderProduct =>
        getItems(_.productUuid == orderProduct.productUuid)
      )

  private def insertOrderProducts(order: model.Order) = {
    val ordersProductsDtos = order.items.map(item =>
      OrdersProductsDto(order.uuid.value, item.product.uuid.value)
    )
    run {
      liftQuery(ordersProductsDtos).foreach(ordersProducts.insertValue(_))
    }
  }

  // MUCH more efficient than single element version above
  private def insertOrdersProducts(chunk: fs2.Chunk[model.Order]) = {
    val ordersProductsDtos = chunk.toList
      .flatMap(order => order.items.map(order -> _))
      .map { case (order, item) =>
        OrdersProductsDto(order.uuid.value, item.product.uuid.value)
      }

    run {
      liftQuery(ordersProductsDtos).foreach(ordersProducts.insertValue(_))
    }
  }

  private def deleteOrderProduct(orderUuid: model.Order.OrderUuid) = run {
    ordersProducts
      .filter(_.orderUuid == lift(orderUuid.value))
      .delete
  }
  // ----------------------------

  implicit class DbOps[A](private val action: doobie.ConnectionIO[A])
      extends AnyVal {
    def performAction: ce.IO[A] = transactor.use { xa =>
      action.transact(xa)
    }
  }
}

object schema {
  import io.getquill.SnakeCase
  import io.getquill.doobie.DoobieContext

  val ctx = new DoobieContext.SQLite(SnakeCase)
  import ctx._

  @nowarn implicit val insertMetaProduct = insertMeta[ProductDto](_.id)
  @nowarn implicit val insertMetaItem = insertMeta[ItemDto](_.id)
  @nowarn implicit val insertMetaOrder = insertMeta[OrderDto](_.id)

  val products = quote(querySchema[ProductDto]("products"))
  val items = quote(querySchema[ItemDto]("items"))
  val orders = quote(querySchema[OrderDto]("orders"))
  val ordersProducts = quote(querySchema[OrdersProductsDto]("orders_products"))
}
