package io.elijahlamoon.dott.overengineered.model

sealed trait Category

object Category {
  // because of the size of this project I'm going to keep it like this, without using Option
  def parse(str: String): Category = str.toLowerCase match {
    case "books"       => Books
    case "electronics" => Electronics
  }

  case object Books extends Category
  case object Electronics extends Category
}
