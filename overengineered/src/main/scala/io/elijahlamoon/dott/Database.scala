package io.elijahlamoon.dott

import cats.effect.kernel.Resource
import cats.{effect => ce}
import com.typesafe.config.ConfigFactory
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.implicits.toConnectionIOOps
import doobie.util.ExecutionContexts
import fly4s.Fly4s
import fly4s.data.Fly4sConfig
import fly4s.data.Location

import scala.annotation.nowarn
import dto.{OrdersItemsDto, OrderDto, ItemDto, ProductDto}

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

  // TODO: get, insert, delete
  import schema._
  import schema.ctx._
  // ------- Products -------
  def getProducts = stream(products)
  def insertProduct(product: ProductDto) = run {
    products.insertValue(lift(product))
  }
  def deleteProduct(name: String) = run {
    products.filter(_.name == lift(name)).delete
  }
  // ------- Items -------
  def getItems = stream(items)
  def insertItem(item: ItemDto) = run {
    items.insertValue(lift(item))
  }
  // ------- Orders -------

  implicit class DbOps[A](private val action: doobie.ConnectionIO[A])
      extends AnyVal {
    def processAction: ce.IO[A] = transactor.use { xa =>
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

  val products = quote(query[ProductDto])
  val items = quote(query[ItemDto])
  val orders = quote(query[OrderDto])
  val ordersItems = quote(querySchema[OrdersItemsDto]("orders_items"))
}
