package io.elijahlamoon.dott

import java.time.LocalDate
import java.time.LocalDateTime

import DataGenerator.Samples
import model._

class OrdersSuite extends munit.FunSuite {

  val order1 = Order(
    customerName = "Eve Wilson",
    customerContact = "eve.wilson733@example.com",
    shippingAddress = "368 Elm Street, Springfield, USA",
    createdAt = LocalDateTime.parse("2023-08-17T18:02:57"),
    items = List(
      Item(
        product = Samples.legion15ach6h // 2021
      ),
      Item(
        product = Samples.iphoneX // 2021
      )
    )
  )
  val order2 = Order(
    customerName = "Bob Wilson",
    customerContact = "bob.wilson435@example.com",
    shippingAddress = "404 Maple Avenue, Anytown, USA",
    createdAt = LocalDateTime.parse("2020-08-27T23:56:32"),
    items = List(
      Item(
        product = Samples.googlePixel6 // 2022
      ),
      Item(
        product = Samples.categoryTheoryForProgrammers // 2022
      ),
      Item(
        product = Samples.programmingInScala // 2023
      )
    )
  )
  val order3 = Order(
    customerName = "Bob Davis",
    customerContact = "bob.davis7@example.com",
    shippingAddress = "70 Magnolia Drive, Coastside, USA",
    createdAt = LocalDateTime.parse("2020-12-16T17:16:09"),
    items = List(
      Item(
        product = Samples.implementationOfFunctionalLanguages // 2015
      ),
      Item(
        product = Samples.functionalProgrammingInScala // 2014
      ),
      Item(
        product = Samples.iphone8 // 2019
      )
    )
  )
  val ordersDb = List(order1, order2, order3)
    .map(order => order -> order.items.map(_.product))
    .toMap[Order, List[Product]]

  def filterOrders(intervalStart: LocalDateTime, intervalEnd: LocalDateTime) = {
    ordersDb
      .filter { case (_, products) =>
        products.exists { product =>
          import product.createdAt
          val isProductCreatedAtIntervalStart = createdAt isEqual intervalStart
          val isProductCreatedAtIntervalEnd = createdAt isEqual intervalEnd
          val isProductCreatedBetweenIntervals =
            (createdAt isAfter intervalStart) && (createdAt isBefore intervalEnd)

          isProductCreatedAtIntervalStart || isProductCreatedAtIntervalEnd || isProductCreatedBetweenIntervals
        }
      }
      .keys
      .toList
  }

  val filteredOrders1 =
    filterOrders(2014.beginningOfYear, 2020.endOfYear)
  val filteredOrders2 =
    filterOrders(2021.beginningOfYear, 2021.endOfYear)
  val filteredOrders3 =
    filterOrders(2021.beginningOfYear, 2024.endOfYear)
  val filteredOrders4 =
    filterOrders(2010.beginningOfYear, 2012.endOfYear)
  val filteredOrders5 =
    filterOrders(2012.beginningOfYear, 2024.endOfYear)

  test("Only order 3 contains products added between 2014 and 2020") {
    assertEquals(filteredOrders1.size, 1)
    assert(filteredOrders1.contains(order3))
  }

  test("Only order 1 contains products added in 2021") {
    assert(filteredOrders2.size == 1 && filteredOrders2.contains(order1))
  }
  test("Orders 1 and 2 contain products added between 2021 and 2024") {
    assert(
      filteredOrders3.size == 2 &&
        filteredOrders3.contains(order1) &&
        filteredOrders3.contains(order2)
    )
  }
  test("No products were added in 2010-2012") {
    assert(filteredOrders4.isEmpty)
  }

  test("All orders have products added between 2012 and 2024") {
    assert(filteredOrders5.size == 3)
  }

  implicit class IntYearToLocalDateASd(private val year: Int) {
    def beginningOfYear = LocalDate.parse(s"$year-01-01").atStartOfDay()
    def endOfYear = LocalDate.parse(s"$year-12-31").atStartOfDay()
  }
}
