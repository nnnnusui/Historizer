package com.github.nnnnusui.historizer.controller

import com.github.nnnnusui.historizer.domain

object Types {
  type ID = String
  case class Input[T](args: T)
  object Output {
    case class Text(id: ID, value: String)
  }

  type TextId = ID
  case class QueryTextArgs(id: TextId)
//  case class MutationAddTextArgs()
//  case class MutationAddPartialTextArgs(textId: TextId, offset: Int, value: String)
//  case class MutationRemovePartialTextArgs(textId: TextId, offset: Int, length: Int)
//  case class SubscribeAdded

  // implicits
  import domain.Text
  implicit class TextOutputFromDomain(self: Text.Identified) {
    val (id, Text(value))     = self
    def toOutput: Output.Text = Output.Text(id, value)
  }

}
