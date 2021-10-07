package com.github.nnnnusui.historizer.domain

sealed trait Action[T]
object Action {
  case class RemovePartialText(offset: Int, length: Int) extends Action[Text]
  case class AddPartialText(offset: Int, text: String)   extends Action[Text]
}
