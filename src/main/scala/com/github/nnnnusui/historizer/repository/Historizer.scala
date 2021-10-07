package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.interop.slick.akka.UsesExecutionContext
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase
import zio.{IO, ZIO}

trait Historizer { self: UsesDatabase with UsesExecutionContext with Text with UndoAction =>
  import profile.api._
  val historizer = new {
    import com.github.nnnnusui.historizer.domain._
    def add(actionResult: ActionResult[Text]): IO[Throwable, Option[Text]] = actionResult match {
      case ActionResult(state, undo: Action.AddPartialText) =>
        run {
          for {
            updated <- text.updateQuery(state)
            _       <- undoActionAddPartialText.createQuery(state.id, undo.offset, undo.text)
            if updated.isDefined
          } yield updated
        }
      case ActionResult(state, undo: Action.RemovePartialText) =>
        run {
          for {
            updated <- text.updateQuery(state)
            _       <- undoActionRemovePartialText.createQuery(state.id, undo.offset, undo.length)
            if updated.isDefined
          } yield updated
        }
    }
    def undoText(textId: Text.ID): ZIO[Any, Throwable, Option[Action[Text]]] =
      for {
        addActions    <- undoActionAddPartialText.getByTextId(textId)
        removeActions <- undoActionRemovePartialText.getByTextId(textId)
      } yield (addActions ++ removeActions).sortBy(_._1.id).reverse.head._2
  }
}
