package io.elijahlamoon.dott.model

sealed trait Category

object Category {
  case object Books extends Category
  case object Electronics extends Category
}
