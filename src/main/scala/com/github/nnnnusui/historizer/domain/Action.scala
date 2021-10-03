package com.github.nnnnusui.historizer.domain

trait Action
object Action {
  case class Remove(value: String, at: Int) extends Action
  case class Add(value: String, to: Int)    extends Action

  trait Result
  object Result {
    case class Before(value: String, at: Int) extends Result
    case class After(value: String, at: Int)  extends Result
  }
}
