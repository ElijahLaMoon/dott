package io.elijahlamoon.dott.overengineered
package model

import java.time.LocalDateTime
import java.util.UUID

import Order._

final case class Order private (
    customerName: OrderCustomerName,
    customerContact: OrderCustomerContact,
    shippingAddress: OrderShippingAddress,
    createdAt: OrderCreationDate,
    items: List[Item],
    uuid: OrderUuid,
    grandTotal: OrderGrandTotal
)

object Order {
  def apply(
      customerName: OrderCustomerName,
      customerContact: OrderCustomerContact,
      shippingAddress: OrderShippingAddress,
      createdAt: OrderCreationDate,
      items: List[Item],
      uuid: OrderUuid = OrderUuid(UUID.randomUUID()),
      grandTotal: Option[OrderGrandTotal] = None
  ): Order = Order(
    customerName,
    customerContact,
    shippingAddress,
    createdAt,
    items,
    uuid,
    grandTotal = grandTotal.getOrElse(
      OrderGrandTotal(items.map(_.cost.value).sum.clear)
    )
  )

  case class OrderCustomerName(value: String) extends AnyVal
  case class OrderCustomerContact(value: String) extends AnyVal
  case class OrderShippingAddress(value: String) extends AnyVal
  case class OrderCreationDate(value: LocalDateTime) extends AnyVal
  case class OrderUuid(value: UUID) extends AnyVal
  case class OrderGrandTotal(value: BigDecimal) extends AnyVal {
    override def toString = s"€$value"
  }
}
