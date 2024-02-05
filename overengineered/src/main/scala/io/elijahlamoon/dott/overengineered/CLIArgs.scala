package io.elijahlamoon.dott.overengineered

import cats.data.Validated.invalidNel
import cats.data.Validated.valid
import cats.syntax.all.catsSyntaxTuple2Semigroupal
import com.monovore.decline.Opts
import com.monovore.decline.time.defaultLocalDate

import java.time.LocalDate

import CLIArgs.CustomInverval._

object CLIArgs {

  val populateDb = Opts
    .option[Int](
      "populate",
      short = "p",
      help =
        """Populate database with randomly generated data by specifying a number of rows to generate. 
          |Please note running this option on an already populated database will most likely fail
          |or lead to incorrect results
          |""".stripMargin
    )
    .orNone

  private val intervalStart = Opts
    .argument[LocalDate]("starting timestamp")
  private val intervalEnd = Opts
    .argument[LocalDate]("closing timestamp")

  val intervalOptions = (intervalStart, intervalEnd).tupled.mapValidated {
    case (start, end) =>
      if (start isBefore end) valid(start -> end)
      else invalidNel(s"$start is more recent than $end")
  }

  // bonus feature
  sealed trait CustomInterval {
    final override def toString: String = this match {
      case NewerThan(months)    => s"<$months"
      case OlderThan(months)    => s">$months"
      case Between(left, right) => s"$left-$right"
    }
  }
  object CustomInverval {
    case class NewerThan(months: Long) extends CustomInterval
    case class OlderThan(months: Long) extends CustomInterval
    case class Between(left: Long, right: Long) extends CustomInterval
  }

  val customOrderIntervals = Opts
    .options[String](
      "interval",
      short = "i",
      help = """
        |Provide a custom interval to filter orders, in months. Can filter by:
        |  1. In-between interval, e.g. '2-7' filters orders placed between 2 and 7 months ago
        |    1.1. Please note than bounds are inclusive and left-hand side has to be lesser than right one
        |  2. Newer than interval, e.g. '<3' filters orders that were places in the last 3 months
        |  3. Older than interval, e.g. '>8' filters all orders that were placed more than 8 months ago
        |To provide multiple intervals just pass them as several options with their own flags, e.g. '-i 4-5 -i <3 -i >15'
        |""".stripMargin,
      metavar = "custom interval"
    )
    .mapValidated { nel =>
      // just a little helper for folding a couple of lines further
      def validList[A](a: A) = cats.data.Validated.Valid(List(a))

      nel
        .map {
          case s"<$less" if less.forall(_.isDigit) =>
            validList(NewerThan(less.toLong))
          case s">$greater" if greater.forall(_.isDigit) =>
            validList(OlderThan(greater.toLong))
          case s"$left-$right"
              if left.forall(_.isDigit) &&
                right.forall(_.isDigit) &&
                left.toInt < right.toInt =>
            validList(Between(left.toLong, right.toLong))
          case other =>
            invalidNel(
              s"""
              |$other is ill-formatted, should be in one of the following formats:
              | 1. '<3' for orders that were placed in the last 3 months
              |""".stripMargin
            )
        }
        .foldLeft[cats.data.ValidatedNel[String, List[CustomInterval]]](
          valid(List.empty)
        )(_ combine _)
    }
    .withDefault(List.empty)
}
