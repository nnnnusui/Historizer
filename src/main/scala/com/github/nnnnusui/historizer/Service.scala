package com.github.nnnnusui.historizer

import com.github.nnnnusui.historizer.Types._
import com.github.nnnnusui.historizer.GraphQL.ID
import zio.{Has, Ref, UIO, URIO, ZLayer}

object Service {

  trait Service {
    def findParagraphs: UIO[List[Paragraph]]
    def findParagraph(id: ID): UIO[Option[Paragraph]]
  }
  type Get = Has[Service]
  type IO[A] = URIO[Get, A]

  def findParagraphs: IO[List[Paragraph]] = URIO.accessM(_.get.findParagraphs)
  def findParagraph(id: ID): IO[Option[Paragraph]] = URIO.accessM(_.get.findParagraph(id))
//  def editParagraphs(args: MutationEditParagraphArgs): IO[Paragraph] = URIO.accessM(_.get.findParagraphs)

  def make(initial: List[Paragraph] = List(Paragraph("1", "Sample paragraph"))): ZLayer[Any, Nothing, Get] = ZLayer.fromEffect {
    for {
      paragraphs <- Ref.make(initial)
    } yield new Service {

      def findParagraphs = paragraphs.get
      def findParagraph(id: ID) = paragraphs.get.map(_.find(_.id == id))

//      def findById(id: Int): UIO[Option[Section]] = users.get.map(_.find(c => c.id == id))
    }
  }

}
