package io.elijahlamoon.dott.model

import Category._

/** Wrapper with additional info about [[Product]] */
final case class Item(product: Product) {
  val shippingFee: BigDecimal = product.category match {
    case Books       => 10
    case Electronics => 20
  }
  val taxAmount: BigDecimal = product.category match {
    case Books       => product.price * 0.07
    case Electronics => product.price * 0.18
  }
  val cost: BigDecimal = {
    val profit = product.price * 0.15
    product.price + shippingFee + taxAmount + profit
  }
}
