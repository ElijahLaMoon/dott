package io.elijahlamoon.dott

import java.time.LocalDateTime
import java.time.LocalDate
import scala.util.Random

package object overengineered {

  def now(): LocalDateTime = LocalDateTime.now()
  def todaysDate(): LocalDate = now().toLocalDate()

  def randomDateInYear(year: Int): LocalDateTime = randomDateTime(year, year)

  def randomDateTime(startYear: Int, endYear: Int): LocalDateTime =
    randomDateTimeSpecific(
      LocalDate.parse(s"$startYear-01-01"),
      LocalDate.parse(s"$endYear-12-31")
    )

  def randomDateTimeSpecific(
      startDate: LocalDate,
      endDate: LocalDate
  ): LocalDateTime =
    LocalDate
      .ofEpochDay(
        Random.between(startDate.toEpochDay, endDate.toEpochDay + 1)
      )
      .atStartOfDay()

  def getRandomElement[A](list: List[A]): A = list(Random.nextInt(list.length))

  implicit class MyBigDecimalOps(private val bd: BigDecimal) extends AnyVal {

    /** Round this BigDecimal to two decimal places */
    def clear: BigDecimal =
      bd.setScale(2, BigDecimal.RoundingMode.HALF_UP)

    def asLong: Long = (this.clear * 100).toLongExact
  }

  implicit class MyLongOps(private val l: Long) extends AnyVal {
    def asBigDecimal: BigDecimal = BigDecimal(l) / 100
  }

  implicit class MyStringOps(private val str: String) extends AnyVal {
    def parseAsCategory: model.Category = model.Category.parse(str)

    // again, because of the size of the project I assume it wont fail
    // and frankly it shouldnt
    def parseAsLocalDateTime: LocalDateTime =
      LocalDateTime.parse(str)
  }
}
