package io.elijahlamoon

import java.time.LocalDateTime
import scala.util.Random
import java.time.LocalDate

package object dott {

  def now(): LocalDateTime = LocalDateTime.now()

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
}
