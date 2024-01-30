package io.elijahlamoon.dott.model

import java.time.LocalDateTime

final case class Product(
    name: String,
    category: Category,
    weight: Double,
    price: BigDecimal,
    createdAt: LocalDateTime
)
