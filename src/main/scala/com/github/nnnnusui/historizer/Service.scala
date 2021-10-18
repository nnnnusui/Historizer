package com.github.nnnnusui.historizer

import com.github.nnnnusui.historizer.controller.Types._
import com.github.nnnnusui.historizer.domain.content.Content
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
    def findContent(args: QueryContentArgs): UIO[Option[Output.Content]]
    def addContent(args: MutationAddContentArgs): UIO[Output.Content]
//    def addedContent(args: SubscriptionAddedContent): UStream[Output.Content]
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

  def addContent(input: Input[MutationAddContentArgs]): IO[Output.Content] =
    URIO.accessM(_.get.addContent(input.args))

  def make: ZLayer[Any, Nothing, Get] = ZLayer.fromEffect {
    for {
      repository <- UsesDatabase.setup(
        new H2Database
          with repository.ContentRoot
          with repository.ContentArticle
          with repository.ContentParagraph {}
      )
      addedArticleHub <- Hub.unbounded[Output.Article]
      addedContentHub <- Hub.unbounded[Output.Content]
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
        val article = args.toDomain
        for {
          id <- contentArticle.create(article)
        } yield (id, article).toOutput
      }.tap(addedArticleHub.publish).orDie

      override def addedArticle =
        ZStream.unwrapManaged(addedArticleHub.subscribe.map(ZStream.fromQueue(_)))

      override def findContent(args: QueryContentArgs) = {
        val id = args.id.toInt
        for {
          mayBeContent <- contentParagraph.getBy(id)
        } yield for {
          content <- mayBeContent
        } yield (id, content).toOutput
      }.orDie

      override def addContent(args: MutationAddContentArgs) = {
        args.toDomain match {
          case it: Content.Paragraph.type =>
            for {
              id <- contentParagraph.create(it)
            } yield (id, it).toOutput
        }
      }.tap(addedContentHub.publish).orDie

    }
  }
}
