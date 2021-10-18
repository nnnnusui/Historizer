package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase

import scala.concurrent.ExecutionContext

trait ContentRoot { self: UsesDatabase =>
  import profile.api._

  type ID = Int
  val contentRoot = new Repository {
    case class Root(id: ID)
    class TableInfo(tag: Tag) extends Table[Root](tag, "content_root") {
      val id = column[ID]("id", O.AutoInc, O.PrimaryKey)
      def *  = (id) <> (Root.apply, Root.unapply)
    }
    val table = TableQuery[TableInfo]

    case class Closure(ancestorId: ID, descendantId: ID)
    class ClosureTable(tag: Tag) extends Table[Closure](tag, "content_closure") {
      val ancestorId   = column[ID]("ancestor_id")
      val descendantId = column[ID]("descendant_id")
      def *            = (ancestorId, descendantId) <> (Closure.tupled, Closure.unapply)
      def pk           = primaryKey("pk_content_closure", (ancestorId, descendantId))
      def ancestorIdFk = foreignKey("ancestor_id_fk", descendantId, table)(
        _.id,
        onDelete = ForeignKeyAction.Cascade
      )
      def descendantIdFk = foreignKey("descendant_id_fk", descendantId, table)(
        _.id,
        onDelete = ForeignKeyAction.Cascade
      )
    }
    val closure = TableQuery[ClosureTable]
    val joined = for {
      (root, closure) <- table join closure on (_.id === _.descendantId)
    } yield (root, closure)

    override def schema: profile.DDL = table.schema ++ closure.schema
    private def autoInc              = table returning table.map(_.id)
    def createRootQuery(implicit executionContext: ExecutionContext) = for {
      id <- autoInc += Root(-1)
      _  <- closure += Closure(ancestorId = id, descendantId = id)
    } yield id
    def createNodeQuery(parentId: ID)(implicit executionContext: ExecutionContext) = for {
      id <- createRootQuery
      closures <- for {
        ancestorIds <- closure.filter(_.descendantId === parentId).map(_.ancestorId).result
      } yield ancestorIds.map(it => Closure(ancestorId = it, descendantId = id))
      _ <- closure ++= closures
    } yield id
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
