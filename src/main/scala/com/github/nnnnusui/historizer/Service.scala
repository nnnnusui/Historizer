package com.github.nnnnusui.historizer

import com.github.nnnnusui.historizer.controller.Types._
import com.github.nnnnusui.historizer.domain.content.Article
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase
import zio.stream.{UStream, ZStream}
import zio.{Has, Hub, UIO, URIO, ZIO, ZLayer}

object Service {
  trait Service {
    // ID = Int | ULID
    // Identified[T] = (ID, T)
    // Content(parentId: Content.ID)

    // Article: Content (id == parentId)
    def findArticles: UIO[Seq[Output.Article]]
    def findArticle(args: QueryArticleArgs): UIO[Option[Output.Article]]
    def addArticle(args: MutationAddArticleArgs): UIO[Output.Article]
    def addedArticle: UStream[Output.Article]
//    def updateArticle()                 // Identified[Article] => article.update
//    def publishArticle()                // ID => article.update
//
//    // Content (id != parentId)
//    def findContent()   // ID => content.findBy
//    def addContent()    // Content => content.create => history.create
//    def updateContent() // Identified[Content] => content.update => history.create
//
//    // Text(contentId: ID, value: String)
//    def addText()           // Text => text.create
//    def addPartialText()    // PartialText => text.update => history.create
//    def removePartialText() // PartialText => text.update => history.create
//
//    // History(contentId: Content.ID, undoAction: Action, timestamp: Timestamp, commitId: ID)
//    def findHistories() // Article.Key => history.findBy
//    def findHistory()   // ID => history.findBy
//
//    // Commit(comment: String, timestamp: Timestamp)
//    def commit() // () => commit.create (update latest ID)
  }
  type Get       = Has[Service]
  type IO[A]     = ZIO[Get, Throwable, A]
  type Stream[A] = ZStream[Get, Throwable, A]

  def findArticles: IO[Seq[Output.Article]] = URIO.accessM(_.get.findArticles)
  def findArticle(input: Input[QueryArticleArgs]): IO[Option[Output.Article]] =
    URIO.accessM(_.get.findArticle(input.args))
  def addArticle(input: Input[MutationAddArticleArgs]): IO[Output.Article] =
    URIO.accessM(_.get.addArticle(input.args))
  def addedArticle: Stream[Output.Article] =
    ZStream.accessStream(_.get.addedArticle)

  def make: ZLayer[Any, Nothing, Get] = ZLayer.fromEffect {
    for {
      repository <- UsesDatabase.setup(
        new H2Database with repository.ContentRoot with repository.ContentArticle {}
      )
      subscriber <- Hub.unbounded[Output.Article]
    } yield new Service {
      import repository._

      override def findArticles = {
        for {
          all <- contentArticle.getAll
        } yield for {
          (id, article) <- all
        } yield (id, article).toOutput
      }.orDie
      override def findArticle(args: QueryArticleArgs) = {
        val id = args.id.toInt
        for {
          mayBeArticle <- contentArticle.getBy(id)
        } yield for {
          article <- mayBeArticle
        } yield (id, article).toOutput
      }.orDie

      override def addArticle(args: MutationAddArticleArgs) = {
        val article = Article(args.title)
        for {
          id <- contentArticle.create(article)
        } yield (id, article).toOutput
      }.tap(subscriber.publish).orDie

      override def addedArticle =
        ZStream.unwrapManaged(subscriber.subscribe.map(ZStream.fromQueue(_)))
    }
  }
}
