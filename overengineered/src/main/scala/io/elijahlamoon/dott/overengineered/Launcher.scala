package io.elijahlamoon.dott.overengineered

import buildinfo.BuildInfo
import cats.effect.ExitCode
import cats.effect.IO
import cats.syntax.all.catsSyntaxApplicativeId
import cats.syntax.all.catsSyntaxTuple3Semigroupal
import cats.syntax.all.toTraverseOps
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import fs2.Stream

import java.time.temporal.ChronoUnit
import scala.{Stream => _}

import CLIArgs._
import Database.DbOps
import IntervalsFilters._

object Launcher
    extends CommandIOApp(
      name = "java -jar dott.jar",
      header =
        "A utility to check whether older products are still being sold in Dott",
      version = BuildInfo.version
    ) {

  override def main: Opts[IO[ExitCode]] =
    (populateDb, intervalOptions, customOrderIntervals)
      .mapN(AppConfig.apply)
      .map { config =>
        for {
          _ <- Database.migrateDb.use_
          _ <- config.populateDatabase.isDefined
            .pure[IO]
            .ifM(
              ifTrue = {
                val quantity = config.populateDatabase.get
                val items = DataGenerator.Samples.predefinedItems
                val generatedOrders =
                  DataGenerator.generateOrders[IO](items, quantity)

                val effectfulStreams = {
                  Stream(items.toList: _*)
                    .evalMap(item =>
                      Database.insertItem(item).performAction
                    ) merge
                    generatedOrders
                      .chunkN(
                        32768, // some anecdotal testing on my laptop shows 2^15 is approximate optimal chunk size
                        allowFewer = true
                      )
                      .evalMap { chunk =>
                        Database.insertOrders(chunk).performAction
                      }
                }

                IO.println("Populating database...") *>
                  effectfulStreams.compile.drain
              },
              ifFalse = IO.unit
            )

          _ <- IO.println("Getting items...")
          items <- (filterItems _).tupled(config.productsAddedBetween)
          today = todaysDate().atStartOfDay()
          oldestItemCreationDate = items
            .map(_.product.createdAt.value)
            .minOption
            .getOrElse(today)
          monthsBetweenTodayAndOldestItem = ChronoUnit.MONTHS.between(
            oldestItemCreationDate,
            today
          )

          resultHeader =
            items.mkString(
              start = s"""
              |----------------------------------------
              |Products with creation date between ${config.productsAddedBetween._1} and ${config.productsAddedBetween._2}:
              |""".stripMargin,
              sep = ";\n",
              end = "\n----------------------------------------"
            )

          _ <- IO.println("Calculating orders...")
          resultBody <- config.customIntervals.nonEmpty
            .pure[IO]
            .ifM(
              ifTrue = {
                (config.customIntervals
                  .map {
                    case n @ CustomInverval.NewerThan(months) =>
                      countOrdersInIntervalBetween(monthsAgoEnd = months)
                        .map(n -> _)

                    case o @ CustomInverval.OlderThan(months) =>
                      countOrdersInIntervalBetween(
                        monthsAgoStart = months,
                        monthsAgoEnd = monthsBetweenTodayAndOldestItem
                      ).map(o -> _)

                    case b @ CustomInverval.Between(left, right) =>
                      countOrdersInIntervalBetween(left, right).map(b -> _)
                  })
                  .sequence
                  .map(_.map { case (timeFrame, count) =>
                    s"Orders placed in custom timeframe ${timeFrame}: $count orders"
                  }.mkString("\n", "\n", "\n"))
              },
              ifFalse = {
                for {
                  interval1_3Months <- countOrdersInIntervalBetween(
                    monthsAgoEnd = 3
                  )
                  interval4_6Months <- countOrdersInIntervalBetween(4, 6)
                  interval7_12Months <- countOrdersInIntervalBetween(7, 12)
                  interval12PlusMonths <- countOrdersInIntervalBetween(
                    monthsAgoStart = 12,
                    monthsAgoEnd = monthsBetweenTodayAndOldestItem
                  )
                } yield {
                  s"""
                  |Orders placed in timeframes:
                  |1-3 months: $interval1_3Months orders
                  |4-6 months: $interval4_6Months orders
                  |7-12 months: $interval7_12Months orders
                  |>12 months: $interval12PlusMonths orders
                  |""".stripMargin
                }
              }
            )

          resultTail = "----------------------------------------"

          _ <- IO.println(resultHeader + resultBody + resultTail)
        } yield ExitCode.Success
      }
}
