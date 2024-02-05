package io.elijahlamoon.dott.overengineered

import java.time.LocalDate

final case class AppConfig(
    populateDatabase: Option[Int],
    productsAddedBetween: (LocalDate, LocalDate),
    customIntervals: List[CLIArgs.CustomInterval]
)
