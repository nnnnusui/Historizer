package com.github.nnnnusui.historizer

import com.github.nnnnusui.historizer.Types._
import zio.{Has, Ref, UIO, URIO, ZLayer}

object Service {

  trait Service {
    def findParagraphs: UIO[List[Paragraph]]
  }
  type Get = Has[Service]
  type IO[A] = URIO[Get, A]

  def findParagraphs: IO[List[Paragraph]] = URIO.accessM(_.get.findParagraphs)

//  def find(id: Int): IO[Option[Section]] = URIO.accessM(_.get.findById(id))

  def make(initial: List[Paragraph] = List(Paragraph("1", "Sample paragraph"))): ZLayer[Any, Nothing, Get] = ZLayer.fromEffect {
    for {
      paragraphs <- Ref.make(initial)
    } yield new Service {

      def findParagraphs: UIO[List[Paragraph]] = paragraphs.get

//      def findById(id: Int): UIO[Option[Section]] = users.get.map(_.find(c => c.id == id))
    }
  }

}
