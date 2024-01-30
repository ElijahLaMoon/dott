package io.elijahlamoon.dott.model

import java.time.LocalDateTime
import java.util.UUID

final case class Order(
    customerName: String,
    customerContact: String,
    shippingAddress: String,
    createdAt: LocalDateTime,
    items: List[Item]
) {
  val uuid: UUID = UUID.randomUUID()
  val grandTotal: BigDecimal =
    items.map(_.cost).sum.setScale(2, BigDecimal.RoundingMode.HALF_UP)

}
