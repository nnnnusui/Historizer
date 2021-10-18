package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.domain.content.Content.Paragraph
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase
import zio.ZIO

trait ContentParagraph { self: UsesDatabase with ContentRoot =>
  import profile.api._

  val contentParagraph = new Repository {
    case class Data(contentId: ID)
    class TableInfo(tag: Tag) extends ContentSubTable[Data](tag, "paragraph") {
      def * = contentId <> (Data.apply, Data.unapply)
    }
    val table                        = TableQuery[TableInfo]
    override def schema: profile.DDL = table.schema
    val joined                       = contentRoot.joined join table on (_._1.id === _.contentId)

    type Self   = Paragraph.type
    type Out[A] = ZIO[Any, Throwable, A]
    def getBy(id: ID): Out[Option[(ID, Self)]] = run { implicit it =>
      joined
        .filter(_._2.contentId === id)
        .result
        .headOption
        .map(_.map { case ((root, closure), data) =>
          (closure.ancestorId, Paragraph)
        })
    }
    def create(parentId: ID, self: Self): Out[ID] = run { implicit it =>
      for {
        id <- contentRoot.createNodeQuery(parentId)
        _  <- table += Data(id)
      } yield id
    }
    def update(args: (ID, Self)): Out[Unit] = run { implicit it => DBIO.seq() }
  }
}
