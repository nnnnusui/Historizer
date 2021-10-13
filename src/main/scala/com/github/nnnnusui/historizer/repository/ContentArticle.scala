package com.github.nnnnusui.historizer.repository

import com.github.nnnnusui.historizer.domain.content.Article
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase
import zio.stream.ZStream

trait ContentArticle { self: UsesDatabase with ContentRoot =>
  import profile.api._

  val contentArticle = new Repository with Article.Repository {
    case class Data(contentId: ID, title: String)
    class TableInfo(tag: Tag) extends ContentSubTable[Data](tag, "article") {
      def title = column[String]("title")
      def *     = (contentId, title) <> (Data.tupled, Data.unapply)
    }
    val table                        = TableQuery[TableInfo]
    override def schema: profile.DDL = table.schema
    val joined                       = contentRoot.table join table on (_.id === _.contentId)

    override def getAll: Out[Seq[Identified]] = run { implicit it =>
      table.result
    }.map(_.map(it => (it.contentId, Article(it.title))))
    override def getBy(id: ID): Out[Option[Article]] = run { implicit it =>
      joined.filter(_._1.id === id).result.headOption
    }.map(_.map { case (_, it) => Article(it.title) })
    override def create(self: Article): Out[ID] = run { implicit it =>
      for {
        id <- contentRoot.createQuery
        _  <- table += Data(id, self.title)
      } yield id
    }
    override def update(args: (ID, Article)): Out[Unit] = run { implicit it =>
      for {
        //        _ <- contentRoot.updateQuery()
        _ <- table.update(Data(args._1, args._2.title))
      } yield {}
    }
  }
}
