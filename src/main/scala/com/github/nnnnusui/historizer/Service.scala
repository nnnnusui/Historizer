package com.github.nnnnusui.historizer

import zio.{Has, Hub, Ref, UIO, URIO, ZIO, ZLayer}
import zio.stream.{UStream, ZStream}

import com.github.nnnnusui.historizer.controller.Types._
import com.github.nnnnusui.historizer.domain.Text
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase
import com.github.nnnnusui.historizer.usecase.CaretService

object Service {
  trait Service {
    // ID = Int | ULID
    // Identified[T] = (ID, T)
    // Content(parentId: Content.ID)

    // Article: Content (id == parentId)
//    def findArticles: UIO[Seq[Output.Article]]
//    def findArticle(args: QueryArticleArgs): UIO[Option[Output.Article]]
//    def addArticle(args: MutationAddArticleArgs): UIO[Output.Article]
//    def addedArticle: UStream[Output.Article]
//    def updateArticle()                 // Identified[Article] => article.update
//    def publishArticle()                // ID => article.update
//
//    // Content (id != parentId)
//    def findContent(args: QueryContentArgs): UIO[Option[Output.Content]]
//    def addContent(args: MutationAddContentArgs): UIO[Output.Content]
//    def addedContent(args: SubscriptionAddedContentArgs): UStream[Output.Content]
//    def updateContent() // Identified[Content] => content.update => history.create
//
//    // Text(contentId: ID, value: String)
    def findTexts: UIO[Seq[Output.Text]]
    def findText(args: QueryTextArgs): UIO[Option[Output.Text]]
    def addText(args: MutationAddTextArgs): UIO[Output.Text]
    def addedText: UStream[Output.Text]
    def addPartialText(args: MutationAddPartialTextArgs): UIO[Output.Text]
    def updatedText(args: SubscriptionUpdatedTextArgs): UStream[Output.Text]
//    def startSession: URIO[Session.Get, Session]
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

  def findTexts: IO[Seq[Output.Text]] = URIO.accessM(_.get.findTexts)
  def findText(input: Input[QueryTextArgs]): IO[Option[Output.Text]] =
    URIO.accessM(_.get.findText(input.args))
  def addText(input: Input[MutationAddTextArgs]): IO[Output.Text] =
    URIO.accessM(_.get.addText(input.args))
  def addedText: Stream[Output.Text] = ZStream.accessStream(_.get.addedText)
  def addPartialText(input: Input[MutationAddPartialTextArgs]): IO[Output.Text] =
    URIO.accessM(_.get.addPartialText(input.args))
  def updatedText(input: Input[SubscriptionUpdatedTextArgs]): Stream[Output.Text] =
    ZStream.accessStream(_.get.updatedText(input.args))

//  def startSession: URIO[Get with Session.Get, Session] = URIO.accessM(_.get.startSession)

  def make: ZLayer[Any, Nothing, Has[Service] with CaretService.Get] =
    CaretService.make >+> ZLayer.fromEffect {
      for {
        caretService <- ZIO.access[CaretService.Get](_.get)
        repository <- UsesDatabase.setup(
          new H2Database with repository.Text {}
        )
        addedTextHub   <- Hub.unbounded[Output.Text]
        updatedTextHub <- Hub.unbounded[Output.Text]
      } yield new Service {
        import repository._

        override def findTexts: UIO[Seq[Output.Text]] = {
          for {
            texts <- text.getAll
            seq <- ZIO.foreach(texts)(it =>
              caretService.findCarets(QueryCaretsArgs(it._1)).map((it, _))
            )
          } yield for {
            (identified, carets) <- seq
          } yield identified.toOutput(carets)
        }.orDie
        override def findText(args: QueryTextArgs): UIO[Option[Output.Text]] = {
          val QueryTextArgs(id) = args
          for {
            mayBeText <- text.getBy(id.toInt)
            carets    <- caretService.findCarets(QueryCaretsArgs(id))
          } yield for {
            text <- mayBeText
          } yield (id, text).toOutput(carets)
        }.orDie

        override def addText(args: MutationAddTextArgs): UIO[Output.Text] = {
          val domain = Text(args.value)
          for {
            id <- text.create(domain)
          } yield (id.toString, domain).toOutput()
        }.tap(addedTextHub.publish).orDie
        override def addedText: UStream[Output.Text] =
          ZStream.unwrapManaged(addedTextHub.subscribe.map(ZStream.fromQueue(_)))
        override def addPartialText(args: MutationAddPartialTextArgs): UIO[Output.Text] = {
          val MutationAddPartialTextArgs(textId, offset, value) = args
          for {
            mayBeText <- text.getBy(textId.toInt)
          } yield {
            val domain = (for {
              text <- mayBeText
            } yield {
              val after = text.value.patch(offset, value, 0)
              text.copy(value = after)
            }).get
            val identified = (textId, domain)
            text.update(identified).map(_ => identified.toOutput())
          }
        }.flatten.tap(updatedTextHub.publish).orDie
        override def updatedText(args: SubscriptionUpdatedTextArgs): UStream[Output.Text] =
          ZStream.unwrapManaged(
            updatedTextHub.subscribe.map(_.filterOutput(_.id == args.id)).map(ZStream.fromQueue(_))
          )

//      override def startSession: URIO[Session.Get, Session] = {
//        val session = Session("it")
//        for {
//          _ <- URIO.accessM[Session.Get](_.get.set(Some(session)))
//        } yield session
//      }
      }
    }
}
