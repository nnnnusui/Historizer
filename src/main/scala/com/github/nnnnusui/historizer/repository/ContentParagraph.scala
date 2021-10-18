package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.domain.EntityRepository
import com.github.nnnnusui.historizer.domain.content.Content
import com.github.nnnnusui.historizer.domain.content.Content.Paragraph
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase

trait ContentParagraph { self: UsesDatabase with ContentRoot =>
  import profile.api._

  val contentParagraph = new Repository with EntityRepository[Content.Paragraph.type] {
    case class Data(contentId: ID)
    class TableInfo(tag: Tag) extends ContentSubTable[Data](tag, "paragraph") {
      def * = contentId <> (Data.apply, Data.unapply)
    }
    val table                        = TableQuery[TableInfo]
    override def schema: profile.DDL = table.schema
    val joined                       = contentRoot.table join table on (_.id === _.contentId)

    type Self = Paragraph.type
    override def getBy(id: ID): Out[Option[Self]] = run { implicit it =>
      joined.filter(_._1.id === id).result.headOption
    }.map(_.map { case (_, it) => Paragraph })
    override def create(self: Self): Out[ID] = run { implicit it =>
      for {
        id <- contentRoot.createQuery
        _  <- table += Data(id)
      } yield id
    }
    override def update(args: (ID, Self)): Out[Unit] = run { implicit it => DBIO.seq() }
  }
}
