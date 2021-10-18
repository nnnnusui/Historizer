package com.github.nnnnusui.historizer.domain.content

import com.github.nnnnusui.historizer.domain.EntityRepository

import Text._
case class Text(value: Value) extends Content
object Text {
  type Value = String
  type Self  = Text
  trait Repository extends EntityRepository[Self]
}
