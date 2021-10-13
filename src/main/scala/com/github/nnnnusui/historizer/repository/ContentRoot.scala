package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase

trait ContentRoot { self: UsesDatabase =>
  import profile.api._

  type ID = Int
  val contentRoot = new Repository {
    case class Root(id: ID)
    class TableInfo(tag: Tag) extends Table[Root](tag, "content_root") {
      val id = column[ID]("id", O.AutoInc, O.PrimaryKey)
      def *  = (id) <> (Root.apply, Root.unapply)
    }
    val table                        = TableQuery[TableInfo]
    override def schema: profile.DDL = table.schema
    private def autoInc              = table returning table.map(_.id)
    def createQuery                  = autoInc += Root(-1)
//    def updateQuery(args: (ID, Self))      = table.update(???)
  }
  abstract class ContentSubTable[T](tag: Tag, _tableSubName: String)
      extends Table[T](tag, s"content_${_tableSubName}") {
    val contentId = column[ID]("content_id", O.PrimaryKey)
    def content = foreignKey("content_id", contentId, contentRoot.table)(
      _.id,
      onDelete = ForeignKeyAction.Cascade
    )
  }
}
