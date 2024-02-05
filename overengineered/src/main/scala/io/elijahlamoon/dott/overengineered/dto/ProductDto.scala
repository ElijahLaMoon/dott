package io.elijahlamoon.dott.overengineered
package dto

import model.Product._

import java.util.UUID

final case class ProductDto(
    id: Long,
    uuid: UUID,
    name: String,
    category: String,
    weight: Double,
    price: Long,
    createdAt: String
)

object ProductDto {
  def fromModel(product: model.Product): ProductDto = {
    ProductDto(
      id = 0,
      uuid = product.uuid.value,
      name = product.name.value,
      category = product.category.toString,
      weight = product.weight.value,
      // multiplying by 100 to avoid precision loss
      price = product.price.value.asLong,
      createdAt = product.createdAt.value.toString
    )
  }

  def toModel(dto: ProductDto): model.Product =
    model.Product(
      name = ProductName(dto.name),
      category = dto.category.parseAsCategory,
      weight = ProductWeight(dto.weight),
      price = ProductPrice(BigDecimal(dto.price) / 100),
      createdAt = ProductCreationDate(dto.createdAt.parseAsLocalDateTime),
      uuid = ProductUuid(dto.uuid)
    )
}
