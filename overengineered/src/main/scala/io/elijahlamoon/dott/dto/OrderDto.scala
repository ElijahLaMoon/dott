package io.elijahlamoon.dott.dto

final case class OrderDto(
    id: Long,
    customerName: String,
    customerContact: String,
    shippingAddress: String,
    grandTotal: Long,
    createdAt: String
)
