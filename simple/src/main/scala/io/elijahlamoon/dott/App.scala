package io.elijahlamoon.dott

import java.time.LocalDate

import model._

object App {

  def main(args: Array[String]): Unit = {
    require(
      args.length >= 2,
      "Please provide exactly 2 time intervals in the format \"YYYY-MM-DD\", where first one is an older date.\n" +
        "Example: java -jar dott.jar \"2014-01-01\" \"2020-12-31\""
    )
    val startDate = LocalDate.parse(args(0)).atStartOfDay()
    val endDate = LocalDate.parse(args(1)).atStartOfDay()
    require(
      startDate isBefore endDate,
      s"${startDate.toLocalDate()} is more recent than ${endDate.toLocalDate()}"
    )

    val filteredProducts: List[Product] =
      DataGenerator.predefinedProducts.filter { product =>
        import product.createdAt
        val isProductCreatedAtIntervalStart = createdAt isEqual startDate
        val isProductCreatedAtIntervalEnd = createdAt isEqual endDate
        val isProductCreatedBetweenIntervals =
          (createdAt isAfter startDate) && (createdAt isBefore endDate)

        isProductCreatedAtIntervalStart || isProductCreatedAtIntervalEnd || isProductCreatedBetweenIntervals
      }
    val filteredItems: List[Item] = DataGenerator.predefinedItems.filter {
      item =>
        filteredProducts.contains(item.product)
    }

    val numberOfOrders = args.lift(2).flatMap(_.toIntOption).getOrElse(1_500)
    val orders = DataGenerator.generateOrders(filteredItems, numberOfOrders)
    val filteredOrders: LazyList[Order] = {
      filteredItems
        .to(LazyList)
        .flatMap { itemToLookFor =>
          orders.filter { order =>
            order.items.contains(itemToLookFor)
          }
        }
        .distinctBy(_.uuid)
    }

    val today = LocalDate.now().atStartOfDay()
    def filterOrdersByInterval(
        monthsAgoStart: Long = 0,
        monthsAgoEnd: Long
    ) = {
      val intervalStart = today minusMonths monthsAgoStart
      val intervalEnd = today minusMonths monthsAgoEnd

      filteredOrders.filter { order =>
        (order.createdAt isBefore intervalStart) &&
        (order.createdAt isAfter intervalEnd)
      }.size
    }

    val interval1_3Months = filterOrdersByInterval(monthsAgoEnd = 3)
    val interval4_6Months = filterOrdersByInterval(4, 6)
    val interval7_12Months = filterOrdersByInterval(7, 12)
    val interval12PlusMonths = filteredOrders
      .filter(
        _.createdAt isBefore today.minusMonths(12)
      )
      .size

    // format: off
    val result = s"""
      |Products added between ${startDate} and ${endDate}: ${filteredProducts.mkString(", ")}
      |Orders placed in timeframes:
      |1-3 months: $interval1_3Months orders
      |4-6 months: $interval4_6Months orders
      |7-12 months: $interval7_12Months orders
      |>12 months: $interval12PlusMonths orders
      |""".stripMargin
    // format: on

    println(result)
  }
}
