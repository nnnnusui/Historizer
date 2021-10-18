package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase

trait UndoRoot { self: UsesDatabase =>
  import profile.api._

  type ID = Int
  val undoRoot = new Repository {
    case class Root(id: ID)
    class TableInfo(tag: Tag) extends Table[Root](tag, "undo_root") {
      val id = column[ID]("id", O.AutoInc, O.PrimaryKey)
      def *  = (id) <> (Root.apply, Root.unapply)
    }
    val table                        = TableQuery[TableInfo]
    override def schema: profile.DDL = table.schema
    def createQuery                  = table += Root(-1)
  }
  abstract class UndoSubTable[T](tag: Tag, _tableSubName: String)
      extends Table[T](tag, s"undo_${_tableSubName}") {
    val undoId = column[ID]("undo_id", O.PrimaryKey)
    def undo = foreignKey("undo_id", undoId, undoRoot.table)(
      _.id,
      onDelete = ForeignKeyAction.Cascade
    )
  }
}
