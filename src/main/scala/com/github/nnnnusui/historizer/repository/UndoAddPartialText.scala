package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase

trait UndoAddPartialText { self: UsesDatabase with UndoRoot =>
  import profile.api._

  val undoAddPartialText = new Repository {
    case class Data(undoId: ID, offset: Int, partialText: String)
    class TableInfo(tag: Tag) extends UndoSubTable[Data](tag, "add_partial_text") {
      def offset      = column[Int]("offset")
      def partialText = column[String]("partial_text")
      def *           = (undoId, offset, partialText) <> (Data.tupled, Data.unapply)
    }
    val table                        = TableQuery[TableInfo]
    override def schema: profile.DDL = table.schema
    val joined                       = undoRoot.table join table on (_.id === _.undoId)
  }
}
