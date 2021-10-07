package com.github.nnnnusui.historizer.domain

import History._
sealed trait History {
  val base: Base
}
object History {
  type ID = Int
  case class Base(id: ID)
  case class AddText(base: Base, textId: Text.ID, offset: Int, text: String)    extends History
  case class RemoveText(base: Base, textId: Text.ID, offset: Int, text: String) extends History
}
