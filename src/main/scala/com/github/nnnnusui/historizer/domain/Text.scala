package com.github.nnnnusui.historizer.domain

import Text._
case class Text(id: ID, value: Value) {
  type Self = Text
  def actedOn(action: Action[Self]): ActionResult[Self] = action match {
    case Action.RemovePartialText(offset, length) =>
      ActionResult(
        copy(value = value.patch(offset, "", length)),
        Action.AddPartialText(offset, value.substring(offset, offset + length))
      )
    case Action.AddPartialText(offset, text) =>
      ActionResult(
        copy(value = value.patch(offset, text, 0)),
        Action.RemovePartialText(offset, text.length)
      )
  }
}
object Text {
  type ID    = Int
  type Value = String
  def tupled = (apply _).tupled

  sealed trait Decoration
  object Decoration {
    case class Before(offset: Int, value: String)
    case class After(offset: Int, value: String)
  }
}
