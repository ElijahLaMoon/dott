package io.elijahlamoon.dott.dto

final case class ItemDto(
    id: Long,
    productId: Long,
    shippingFee: Long,
    taxAmount: Long,
    cost: Long
)
