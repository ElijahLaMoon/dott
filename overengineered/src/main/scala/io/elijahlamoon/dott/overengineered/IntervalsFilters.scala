package io.elijahlamoon.dott.overengineered

import cats.effect.IO
import io.elijahlamoon.dott.overengineered.model.Item

import java.time.LocalDate

import Database.DbOps

object IntervalsFilters {

  def filterItems(
      intervalStart: LocalDate,
      intervalEnd: LocalDate
  ): IO[List[Item]] =
    Database
      .getItemsMoreEfficiently(productPredicate = productDto => {
        import productDto.{createdAt => c}
        val createdAt = c.parseAsLocalDateTime

        val startDate = intervalStart.atStartOfDay()
        val endDate = intervalEnd.atStartOfDay()

        val isProductCreatedAtIntervalStart = createdAt isEqual startDate
        val isProductCreatedAtIntervalEnd = createdAt isEqual endDate
        val isProductCreatedBetweenIntervals =
          (createdAt isAfter startDate) && (createdAt isBefore endDate)

        isProductCreatedAtIntervalStart || isProductCreatedAtIntervalEnd || isProductCreatedBetweenIntervals
      })
      .compile
      .toList
      .performAction

  def countOrdersInIntervalBetween(
      monthsAgoStart: Long = 0,
      monthsAgoEnd: Long
  ): IO[Long] = {
    Database
      .getOrdersMoreEfficiently { orderDto =>
        val createdAt = orderDto.createdAt.parseAsLocalDateTime
        val today = todaysDate().atStartOfDay()

        val startDate = today minusMonths monthsAgoStart
        val endDate = today minusMonths monthsAgoEnd

        val isOrderCreatedAtIntervalStart = createdAt isEqual startDate
        val isOrderCreatedAtIntervalEnd = createdAt isEqual endDate
        val isOrderCreatedBetweenIntervals =
          (createdAt isBefore startDate) && (createdAt isAfter endDate)

        isOrderCreatedAtIntervalStart || isOrderCreatedAtIntervalEnd || isOrderCreatedBetweenIntervals
      }
      .compile
      .count
      .performAction
  }
}
