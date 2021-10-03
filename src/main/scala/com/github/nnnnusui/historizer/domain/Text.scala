package com.github.nnnnusui.historizer.domain

import Text._
case class Text(id: ID, value: Value)
object Text {
  type ID    = Int
  type Value = String
  def tupled = (apply _).tupled

  case class Partial()
}
