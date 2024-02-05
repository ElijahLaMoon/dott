package io.elijahlamoon.dott.overengineered
package dto

import java.util.UUID

import model.Item._

final case class ItemDto(
    id: Long,
    productUuid: UUID,
    shippingFee: Long,
    taxAmount: Long,
    cost: Long
)

object ItemDto {
  def fromModel(item: model.Item): ItemDto = {
    ItemDto(
      id = 0,
      productUuid = item.product.uuid.value,
      shippingFee = item.shippingFee.value.asLong,
      taxAmount = item.taxAmount.value.asLong,
      cost = item.cost.value.asLong
    )
  }

  def toModel(dto: ItemDto, product: model.Product): model.Item =
    model.Item(
      product,
      shippingFee = ItemShippingFee(dto.shippingFee.asBigDecimal),
      taxAmount = ItemTaxAmount(dto.taxAmount.asBigDecimal),
      cost = ItemCost(dto.cost.asBigDecimal)
    )
}
