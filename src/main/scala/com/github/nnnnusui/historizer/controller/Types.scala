package com.github.nnnnusui.historizer.controller

import com.github.nnnnusui.historizer.domain

object Types {
  type ID = String
  case class Input[T](args: T)
  type TextId = ID
  type CursorId = ID
  case class CursorPosition(textId: TextId, offset: Int)
  object Output {
    case class Text(id: TextId, value: String)
    case class Cursor(id: CursorId, position: CursorPosition)
  }

  case class QueryTextArgs(id: TextId)
//  case class MutationAddTextArgs()
  case class MutationAddPartialTextArgs(textId: TextId, offset: Int, value: String)
//  case class MutationRemovePartialTextArgs(textId: TextId, offset: Int, length: Int)
  case class SubscriptionUpdatedTextArgs(id: TextId)
//  case class SubscribeAdded
//  case class MutationAddCursorArgs(position: CursorPosition)
//  case class MutationMoveCursorArgs(id: CursorId, position: CursorPosition)

  // implicits
  import domain.Text
  implicit class TextOutputFromDomain(self: Text.Identified) {
    val (id, Text(value))     = self
    def toOutput: Output.Text = Output.Text(id, value)
  }

}
