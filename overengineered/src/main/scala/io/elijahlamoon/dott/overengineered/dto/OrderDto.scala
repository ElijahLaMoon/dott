package io.elijahlamoon.dott.overengineered
package dto

import java.util.UUID
import model.Order._

final case class OrderDto(
    id: Long,
    uuid: UUID,
    customerName: String,
    customerContact: String,
    shippingAddress: String,
    grandTotal: Long,
    createdAt: String
)

object OrderDto {
  def fromModel(order: model.Order): OrderDto = OrderDto(
    id = 0,
    uuid = order.uuid.value,
    customerName = order.customerName.value,
    customerContact = order.customerContact.value,
    shippingAddress = order.shippingAddress.value,
    grandTotal = order.grandTotal.value.asLong,
    createdAt = order.createdAt.value.toString
  )

  def toModel(dto: OrderDto, items: List[model.Item]): model.Order =
    model.Order(
      OrderCustomerName(dto.customerName),
      OrderCustomerContact(dto.customerContact),
      OrderShippingAddress(dto.shippingAddress),
      OrderCreationDate(dto.createdAt.parseAsLocalDateTime),
      items,
      OrderUuid(dto.uuid),
      grandTotal = OrderGrandTotal(BigDecimal(dto.grandTotal) / 100)
    )
}
