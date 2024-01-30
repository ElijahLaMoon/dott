package io.elijahlamoon.dott

import scala.util.Random
import io.elijahlamoon.dott.randomDateInYear
import model._
import Category._
import io.elijahlamoon.dott.randomDateTimeSpecific

object DataGenerator {
  import Samples._

  def generateOrders(
      items: List[Item],
      quantity: Int = 1_500
  ): LazyList[Order] =
    LazyList.fill(Random.nextInt(quantity) + 1)(generateOrder(items))

  lazy val predefinedProducts: List[Product] = List(
    iphone8,
    iphoneX,
    googlePixel6,
    legion15ach6h,
    programmingInScala,
    categoryTheoryForProgrammers,
    implementationOfFunctionalLanguages,
    functionalProgrammingInScala
  )
  lazy val predefinedItems: List[Item] = predefinedProducts.map(Item(_))

  def generateOrder(items: List[Item]): Order = {
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
        .map(_ => getRandomElement(items))
        .toList
    }

    // we dont want to create orders which have products, that we're added to db more recently than the order was created
    val (oldestProductsDate, mostRecentProductsDate) = {
      val years = localItems.map(_.product.createdAt)
      (years.min.toLocalDate, years.max.toLocalDate)
    }

    Order(
      customerName,
      customerContact,
      shippingAddress = getRandomElement(shippingAddresses),
      createdAt =
        randomDateTimeSpecific(oldestProductsDate, mostRecentProductsDate),
      localItems
    )
  }

  // -------------------------------------------------
  object Samples {
    val iphone8 = Product(
      name = "iPhone 8",
      category = Electronics,
      weight = 0.2,
      price = BigDecimal(399),
      createdAt = randomDateInYear(2019)
    )
    val iphoneX = Product(
      name = "iPhone X",
      category = Electronics,
      weight = 0.3,
      price = BigDecimal(599),
      createdAt = randomDateInYear(2021)
    )
    val googlePixel6 = Product(
      name = "Google Pixel 6",
      category = Electronics,
      weight = 0.3,
      price = BigDecimal(499),
      createdAt = randomDateInYear(2022)
    )
    val legion15ach6h = Product(
      name = "Lenovo Legion 15ACH6H 2022",
      category = Electronics,
      weight = 2.0,
      price = BigDecimal(1499),
      createdAt = randomDateInYear(2021)
    )
    val programmingInScala = Product(
      name = "Martin Odersky, Programming in Scala 5th edition",
      category = Books,
      weight = 0.512,
      price = BigDecimal(19.99),
      createdAt = randomDateInYear(2023)
    )
    val categoryTheoryForProgrammers = Product(
      name = "Bartosz Milewski, Category Theory for Programmers",
      category = Books,
      weight = 0.354,
      price = BigDecimal(14.99),
      createdAt = randomDateInYear(2022)
    )
    val implementationOfFunctionalLanguages = Product(
      name =
        "Simon L. Peyton Jones, The Implementation of Functional Programming Languages",
      category = Books,
      weight = 0.402,
      price = BigDecimal(16.49),
      createdAt = randomDateInYear(2015)
    )
    val functionalProgrammingInScala = Product(
      name = "Paul Chiusano, Functional Programming in Scala",
      category = Books,
      weight = 0.267,
      price = BigDecimal(13.49),
      createdAt = randomDateInYear(2014)
    )
  }
}
