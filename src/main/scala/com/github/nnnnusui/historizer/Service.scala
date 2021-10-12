package com.github.nnnnusui.historizer

import zio.{Has, Ref, ZIO, ZLayer}

object Service {
  trait Service {
    // ID = Int | ULID
    // Identified[T] = (ID, T)
    // Content(parentId: Content.ID)

    // Article: Content (id == parentId)
    def findArticles: UIO[Seq[Article]] // () => article.findAll
    def findArticle()                   // ID => article.findBy
    def addArticle()                    // Article => article.create
    def updateArticle()                 // Identified[Article] => article.update
    def publishArticle()                // ID => article.update

    // Content (id != parentId)
    def findContent()   // ID => content.findBy
    def addContent()    // Content => content.create => history.create
    def updateContent() // Identified[Content] => content.update => history.create

    // Text(contentId: ID, value: String)
    def addText()           // Text => text.create
    def addPartialText()    // PartialText => text.update => history.create
    def removePartialText() // PartialText => text.update => history.create

    // History(contentId: Content.ID, undoAction: Action, timestamp: Timestamp, commitId: ID)
    def findHistories() // Article.Key => history.findBy
    def findHistory()   // ID => history.findBy

    // Commit(comment: String, timestamp: Timestamp)
    def commit() // () => commit.create (update latest ID)
  }
  type Get   = Has[Service]
  type IO[A] = ZIO[Get, Throwable, A]

  def make: ZLayer[Any, Nothing, Get] = ZLayer.fromEffect {
    for {
      _ <- Ref.make("")
    } yield new Service {}
  }
}
