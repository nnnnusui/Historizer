package com.github.nnnnusui.historizer.controller

import com.github.nnnnusui.historizer.domain

object Types {
  type ID = String
  case class Input[T](args: T)
  type TextId  = ID
  type CaretId = ID
  object Output {
    case class Caret(id: CaretId, textId: TextId, offset: Int)
    case class Text(id: TextId, value: String, carets: Seq[Caret])
  }

  case class QueryTextArgs(id: TextId)
//  case class MutationAddTextArgs()
  case class MutationAddPartialTextArgs(textId: TextId, offset: Int, value: String)
//  case class MutationRemovePartialTextArgs(textId: TextId, offset: Int, length: Int)
  case class SubscriptionUpdatedTextArgs(id: TextId)
//  case class SubscribeAdded
  case class QueryCaretsArgs(textId: TextId)
  case class QueryCaretArgs(id: CaretId)
  case class MutationAddCaretArgs(textId: TextId, offset: Int)
  case class SubscriptionAddedCaretArgs(textId: TextId)
  case class MutationMoveCaretArgs(id: CaretId, offset: Int)
  case class SubscriptionMovedCaretArgs(id: CaretId)
  case class MutationDeleteCaretArgs(id: CaretId)
  case class SubscriptionDeletedCaretArgs(id: CaretId)

  // implicits
  import domain.Text
  implicit class TextOutputFromDomain(self: Text.Identified) {
    val (id, Text(value))                                        = self
    def toOutput(carets: Seq[Output.Caret] = Seq()): Output.Text = Output.Text(id, value, carets)
  }

}
