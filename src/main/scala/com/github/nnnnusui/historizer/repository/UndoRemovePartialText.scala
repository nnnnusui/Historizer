package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase

trait UndoRemovePartialText { self: UsesDatabase with UndoRoot =>
  import profile.api._

  val undoRemovePartialText = new Repository {
    case class Data(undoId: ID, offset: Int, length: Int)
    class TableInfo(tag: Tag) extends UndoSubTable[Data](tag, "remove_partial_text") {
      def offset = column[Int]("offset")
      def length = column[Int]("length")
      def *      = (undoId, offset, length) <> (Data.tupled, Data.unapply)
    }
    val table                        = TableQuery[TableInfo]
    override def schema: profile.DDL = table.schema
    val joined                       = undoRoot.table join table on (_.id === _.undoId)
  }
}
