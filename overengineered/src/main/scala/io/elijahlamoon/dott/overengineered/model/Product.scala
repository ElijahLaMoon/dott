package io.elijahlamoon.dott.overengineered.model

import java.time.LocalDateTime
import java.util.UUID

import Product._

final case class Product(
    name: ProductName,
    category: Category,
    weight: ProductWeight,
    price: ProductPrice,
    createdAt: ProductCreationDate,
    uuid: ProductUuid = ProductUuid(UUID.randomUUID())
)

object Product {
  case class ProductName(value: String) extends AnyVal
  case class ProductWeight(value: Double) extends AnyVal {
    override def toString = s"$value kg"
  }
  case class ProductPrice(value: BigDecimal) extends AnyVal {
    override def toString = s"€$value"
  }
  case class ProductCreationDate(value: LocalDateTime) extends AnyVal
  case class ProductUuid(value: UUID) extends AnyVal
}
