package com.github.nnnnusui.historizer.domain

case class Text(value: String)
object Text {
  type ID         = String
  type Identified = (ID, Text)
}
