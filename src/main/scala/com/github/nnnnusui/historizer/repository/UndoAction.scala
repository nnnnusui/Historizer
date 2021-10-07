package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.domain.Action
import com.github.nnnnusui.historizer.interop.slick.akka.UsesExecutionContext
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase
import zio.IO

trait UndoAction { self: UsesDatabase with UsesExecutionContext =>
  import profile.api._

  val undoAction = new Repository {
    case class UndoAction(id: Int)
    class TableInfo(tag: Tag) extends Table[UndoAction](tag, "undo_action") {
      def *  = (id) <> (UndoAction.apply, UndoAction.unapply)
      val id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    }

    val table  = TableQuery[TableInfo]
    def schema = table.schema

    def createQuery = table += UndoAction(-1)
  }

  val undoActionAddPartialText = new Repository {
    case class AddPartialText(undoActionId: Int, textId: Int, offset: Int, text: String)
    class TableInfo(tag: Tag) extends Table[AddPartialText](tag, "undo_action_add_partial_text") {
      def * =
        (undoActionId, textId, offset, text) <> (AddPartialText.tupled, AddPartialText.unapply)
      val undoActionId = column[Int]("undo_action_id", O.PrimaryKey)
      val textId       = column[Int]("text_id")
      val offset       = column[Int]("offset")
      val text         = column[String]("text")
    }
    val table  = TableQuery[TableInfo]
    def schema = table.schema

    def createQuery(textId: Int, offset: Int, text: String) = for {
      id <- undoAction.createQuery
      _  <- table += AddPartialText(id, textId, offset, text)
    } yield id
    def getByTextId(textId: Int) = run {
      (undoAction.table joinLeft table on (_.id === _.undoActionId))
        .filter(_._2.map(_.textId === textId))
        .result
        .map(_.map(it => (it._1, it._2.map(it => Action.AddPartialText(it.offset, it.text))))
    }
  }

  val undoActionRemovePartialText = new Repository {
    case class RemovePartialText(undoActionId: Int, textId: Int, offset: Int, length: Int)
    class TableInfo(tag: Tag)
        extends Table[RemovePartialText](tag, "undo_action_add_partial_text") {
      def * = (
        undoActionId,
        textId,
        offset,
        length
      ) <> (RemovePartialText.tupled, RemovePartialText.unapply)
      val undoActionId = column[Int]("undo_action_id", O.PrimaryKey)
      val textId       = column[Int]("text_id")
      val offset       = column[Int]("offset")
      val length       = column[Int]("length")
    }
    val table  = TableQuery[TableInfo]
    def schema = table.schema

    def createQuery(textId: Int, offset: Int, length: Int) = for {
      id <- undoAction.createQuery
      _  <- table += RemovePartialText(id, textId, offset, length)
    } yield id
    def getByTextId(textId: Int) = run {
      (undoAction.table joinLeft table on (_.id === _.undoActionId))
        .filter(_._2.map(_.textId === textId))
        .result
        .map(_.map(it => (it._1, it._2.map(it => Action.RemovePartialText(it.offset, it.length)))))
    }
  }
}
