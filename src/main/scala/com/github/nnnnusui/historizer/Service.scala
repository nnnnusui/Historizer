package com.github.nnnnusui.historizer

import com.github.nnnnusui.historizer.GraphQL.ID
import com.github.nnnnusui.historizer.Types._
import com.github.nnnnusui.historizer.domain.Paragraph
import zio.{Has, Ref, Runtime, UIO, URIO, ZIO, ZLayer}

object Service {
  trait Service {
    def findParagraphs: UIO[List[Paragraph]]
    def findParagraph(id: ID): UIO[Option[Paragraph]]
    def addParagraph(args: MutationAddParagraphArgs): UIO[Paragraph]
    def removeText(args: MutationRemoveTextArgs): UIO[Option[Paragraph]]
    def addText(args: MutationAddTextArgs): UIO[Option[Paragraph]]
  }
  type Get   = Has[Service]
  type IO[A] = ZIO[Get, Throwable, A]

  def findParagraphs: IO[List[Paragraph]]          = URIO.accessM(_.get.findParagraphs)
  def findParagraph(id: ID): IO[Option[Paragraph]] = URIO.accessM(_.get.findParagraph(id))
  def addParagraph(args: MutationAddParagraphArgs): IO[Paragraph] =
    URIO.accessM(_.get.addParagraph(args))
  def removeText(args: MutationRemoveTextArgs): IO[Option[Paragraph]] =
    URIO.accessM(_.get.removeText(args))
  def addText(args: MutationAddTextArgs): IO[Option[Paragraph]] =
    URIO.accessM(_.get.addText(args))

  def make(): ZLayer[Any, Nothing, Get] =
    ZLayer.fromEffect {
      type Repository = repository.Paragraph
      for {
        _ <- Ref.make(None)
      } yield new Service {
        val repository = new Repository with H2Database
        import repository._
        Runtime.default.unsafeRun(repository.setup)

        def findParagraphs        = paragraph.getAll.map(_.toList).orDie
        def findParagraph(id: ID) = paragraph.getBy(id).orDie
        def addParagraph(args: MutationAddParagraphArgs) =
          paragraph.create(Paragraph(_, args.content)).orDie
        def removeText(args: MutationRemoveTextArgs) =
          paragraph
            .getBy(args.paragraphId)
            .flatMap {
              case None => ZIO.none
              case Some(it) =>
                paragraph.update(it.copy(content = it.content.patch(args.offset, "", args.length)))
            }
            .orDie
        def addText(args: MutationAddTextArgs): UIO[Option[Paragraph]] =
          paragraph
            .getBy(args.paragraphId)
            .flatMap {
              case None => ZIO.none
              case Some(it) =>
                paragraph.update(it.copy(content = it.content.patch(args.offset, args.text, 0)))
            }
            .orDie

      }
    }

}
