package io.elijahlamoon.dott.overengineered.model

import Category._
import Item._

/** Wrapper with additional info about [[Product]]. `cost` includes profit */
final case class Item(
    product: Product,
    shippingFee: ItemShippingFee,
    taxAmount: ItemTaxAmount,
    cost: ItemCost
)

object Item {
  def apply(product: Product): Item = {
    val shippingFee: ItemShippingFee = product.category match {
      case Books       => ItemShippingFee(10)
      case Electronics => ItemShippingFee(20)
    }
    val taxAmount: ItemTaxAmount = product.category match {
      case Books       => ItemTaxAmount(product.price.value * 0.07)
      case Electronics => ItemTaxAmount(product.price.value * 0.18)
    }
    val cost: ItemCost = {
      val profit: BigDecimal = product.price.value * 0.15
      ItemCost(
        product.price.value + shippingFee.value + taxAmount.value + profit
      )
    }

    Item(product, shippingFee, taxAmount, cost)
  }

  case class ItemShippingFee(value: BigDecimal) extends AnyVal {
    override def toString = s"€$value"
  }
  case class ItemTaxAmount(value: BigDecimal) extends AnyVal {
    override def toString = s"€$value"
  }
  case class ItemCost(value: BigDecimal) extends AnyVal {
    override def toString = s"€$value"
  }

}
