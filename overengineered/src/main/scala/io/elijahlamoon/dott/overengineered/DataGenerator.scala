package io.elijahlamoon.dott
package overengineered

import cats.data.NonEmptyList
import fs2.Stream

import scala.util.Random

import model._

object DataGenerator {

  def generateOrders[F[_]](
      items: NonEmptyList[Item],
      quantity: Int = 1_500
  ): Stream[F, Order] =
    Stream
      .range(start = 0, stopExclusive = quantity)
      .map(_ => generateOrder(items))

  def generateOrder(items: NonEmptyList[Item]): Order = {
    val firstNames =
      List("John", "Jane", "Bob", "Alice", "Charlie", "Alex", "Eve", "Jackie")
    val lastNames = List(
      "Smith",
      "Johnson",
      "Brown",
      "Davis",
      "Miller",
      "Wilson",
      "Moore",
      "Taylor"
    )
    val customerName: String =
      getRandomElement(firstNames) + " " + getRandomElement(lastNames)
    val customerContact = customerName.toLowerCase
      .split(' ')
      .mkString(
        start = "",
        sep = ".",
        end = s"${Random.nextInt(1000)}@example.com"
      )
    val shippingAddresses = List(
      "Elm Street, Springfield, USA",
      "Maple Avenue, Anytown, USA",
      "Oak Lane, Smallville, USA",
      "Pine Road, Centerville, USA",
      "Birch Street, Lakeview, USA",
      "Cedar Blvd, Rivertown, USA",
      "Willow Way, Pleasantville, USA",
      "Aspen Court, Metrocity, USA",
      "Magnolia Drive, Coastside, USA",
      "Redwood Circle, Capitol City, USA"
    ).map(a => s"${Random.nextInt(999) + 1} $a")

    val localItems: List[Item] = {
      val numberOfItems = Random.nextInt(5) + 1 // adding 1 so it's never 0
      (1 to numberOfItems)
        .map(_ => getRandomElement(items.toList))
        .toList
    }

    // we dont want to generate orders which have products that were added to db more recently than the order was created
    val (oldestProductsDate, mostRecentProductsDate) = {
      val years = localItems.map(_.product.createdAt.value)
      (years.min.toLocalDate, years.max.toLocalDate)
    }

    import Order._
    Order(
      OrderCustomerName(customerName),
      OrderCustomerContact(customerContact),
      OrderShippingAddress(getRandomElement(shippingAddresses)),
      OrderCreationDate(
        randomDateTimeSpecific(oldestProductsDate, mostRecentProductsDate)
      ),
      localItems
    )
  }

  // -------------------------------------------------
  object Samples {
    import Product._
    import java.time.LocalDateTime

    lazy val predefinedProducts: NonEmptyList[Product] = NonEmptyList.of(
      iphone8,
      iphoneX,
      googlePixel6,
      legion15ach6h,
      programmingInScala,
      categoryTheoryForProgrammers,
      implementationOfFunctionalLanguages,
      functionalProgrammingInScala,
      typeDrivenDevelopmentWithIdris,
      theoriesOfProgrammingLanguages,
      engineeringACompiler
    )
    lazy val predefinedItems: NonEmptyList[Item] =
      predefinedProducts.map(Item(_))

    val iphone8 = electronics(
      name = "iPhone 8",
      weight = 0.2,
      price = 399,
      createdAt = randomDateInYear(2019)
    )
    val iphoneX = electronics(
      name = "iPhone X",
      weight = 0.3,
      price = 599,
      createdAt = randomDateInYear(2021)
    )
    val googlePixel6 = electronics(
      name = "Google Pixel 6",
      weight = 0.3,
      price = 499,
      createdAt = randomDateInYear(2022)
    )
    val legion15ach6h = electronics(
      name = "Lenovo Legion 15ACH6H 2022",
      weight = 2.0,
      price = 1499,
      createdAt = randomDateInYear(2021)
    )
    val programmingInScala = book(
      name = "Martin Odersky, Programming in Scala 5th edition",
      weight = 0.512,
      price = (19.99),
      createdAt = randomDateInYear(2023)
    )
    val categoryTheoryForProgrammers = book(
      name = "Bartosz Milewski, Category Theory for Programmers",
      weight = 0.354,
      price = 14.99,
      createdAt = randomDateInYear(2022)
    )
    val implementationOfFunctionalLanguages = book(
      name =
        "Simon L. Peyton Jones, The Implementation of Functional Programming Languages",
      weight = 0.402,
      price = 16.49,
      createdAt = randomDateInYear(2015)
    )
    val functionalProgrammingInScala = book(
      name = "Paul Chiusano, Functional Programming in Scala",
      weight = 0.267,
      price = 13.49,
      createdAt = randomDateInYear(2014)
    )
    val typeDrivenDevelopmentWithIdris = book(
      name = "Edwin Brady, Type-Driven Development with Idris",
      weight = 0.318,
      price = 17.49,
      createdAt = randomDateInYear(2017)
    )
    val theoriesOfProgrammingLanguages = book(
      name = "John C. Reynolds, Theories of Programming Languages",
      weight = 0.402,
      price = 16.49,
      createdAt = randomDateInYear(2009)
    )
    val engineeringACompiler = book(
      name = "Keith Cooper, Engineering a Compiler",
      weight = 0.481,
      price = 23.49,
      createdAt = randomDateInYear(2012)
    )

    private def book(
        name: String,
        weight: Double,
        price: BigDecimal,
        createdAt: LocalDateTime
    ): Product = Product(
      name = ProductName(name),
      category = Category.Books,
      weight = ProductWeight(weight),
      price = ProductPrice(price),
      createdAt = ProductCreationDate(createdAt)
    )
    private def electronics(
        name: String,
        weight: Double,
        price: BigDecimal,
        createdAt: LocalDateTime
    ): Product =
      book(name, weight, price, createdAt).copy(category = Category.Electronics)
  }
}
