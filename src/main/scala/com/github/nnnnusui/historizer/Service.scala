package com.github.nnnnusui.historizer

import zio.{Has, Hub, UIO, URIO, ZIO, ZLayer}
import zio.stream.{UStream, ZStream}

import com.github.nnnnusui.historizer.controller.Types._
import com.github.nnnnusui.historizer.domain.Text
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase

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
    def addText: UIO[Output.Text]
    def addedText: UStream[Output.Text]
    def addPartialText(args: MutationAddPartialTextArgs): UIO[Output.Text]
    def updatedText(args: SubscriptionUpdatedTextArgs): UStream[Output.Text]
//    def startSession: URIO[Session.Get, Session]
//    def addCursor(args: MutationAddCursorArgs): UIO[Output.Cursor]
//    def moveCursor: URIO[Session.Get, Output.Cursor]
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
  def addText: IO[Output.Text]       = URIO.accessM(_.get.addText)
  def addedText: Stream[Output.Text] = ZStream.accessStream(_.get.addedText)
  def addPartialText(input: Input[MutationAddPartialTextArgs]): IO[Output.Text] =
    URIO.accessM(_.get.addPartialText(input.args))
  def updatedText(input: Input[SubscriptionUpdatedTextArgs]): Stream[Output.Text] =
    ZStream.accessStream(_.get.updatedText(input.args))
//  def startSession: URIO[Get with Session.Get, Session] = URIO.accessM(_.get.startSession)
//  def addCursor(input: Input[MutationAddCursorArgs]): IO[Output.Cursor] = URIO.accessM(_.get.addCursor(input.args))
//  def moveCursor: URIO[Get with Session.Get, Output.Cursor] = URIO.accessM(_.get.moveCursor)

  def make: ZLayer[Any, Nothing, Get] = ZLayer.fromEffect {
    for {
      repository <- UsesDatabase.setup(
        new H2Database with repository.Text {}
      )
      addedTextHub   <- Hub.unbounded[Output.Text]
      updatedTextHub <- Hub.unbounded[Output.Text]
//      addedCursorHub <- Hub.unbounded[Output.Cursor]
    } yield new Service {
      import repository._

      override def findTexts: UIO[Seq[Output.Text]] =
        text.getAll.map(_.map(_.toOutput)).orDie
      override def findText(args: QueryTextArgs): UIO[Option[Output.Text]] = {
        val QueryTextArgs(id) = args
        text.getBy(id.toInt).map(_.map(domain => (id, domain).toOutput))
      }.orDie
      override def addText: UIO[Output.Text] = {
        val domain = Text("")
        for {
          id <- text.create(domain)
        } yield (id.toString, domain).toOutput
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
          text.update(identified).map(_ => identified.toOutput)
        }
      }.flatten.tap(updatedTextHub.publish).orDie
      override def updatedText(args: SubscriptionUpdatedTextArgs): UStream[Output.Text] =
        ZStream.unwrapManaged(
          updatedTextHub.subscribe.map(_.filterOutput(_.id == args.id)).map(ZStream.fromQueue(_))
        )
      //      override def addCursor(args: MutationAddCursorArgs): UIO[Output.Cursor] =
//        addedCursorHub.size.map(size => {
//          val cursor = Output.Cursor(size.toString, args.position)
//          addedCursorHub.publish(cursor)
//          cursor
//        })
//      override def moveCursor: URIO[Session.Get, Output.Cursor] = for {
//        session <- URIO.accessM[Session.Get](_.get.get)
//      } yield Output.Cursor(1)

//      override def startSession: URIO[Session.Get, Session] = {
//        val session = Session("it")
//        for {
//          _ <- URIO.accessM[Session.Get](_.get.set(Some(session)))
//        } yield session
//      }

    }
  }
}
