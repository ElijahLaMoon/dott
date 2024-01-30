package io.elijahlamoon.dott.dto

final case class ProductDto(
    id: Long,
    name: String,
    category: String,
    weight: Double,
    price: Long,
    createdAt: String
)
