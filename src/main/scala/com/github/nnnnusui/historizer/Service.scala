package com.github.nnnnusui.historizer

import com.github.nnnnusui.historizer.Types._
import com.github.nnnnusui.historizer.domain.{Action, Paragraph}
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase
import zio.{Has, UIO, URIO, ZIO, ZLayer}

object Service {
  trait Service {
    def findParagraphs: UIO[List[Paragraph]]
    def findParagraph(args: QueryParagraphArgs): UIO[Option[Paragraph]]
    def addParagraph(args: MutationAddParagraphArgs): UIO[Paragraph]
    def removeText(args: MutationRemoveTextArgs): UIO[Option[Paragraph]]
    def addText(args: MutationAddTextArgs): UIO[Option[Paragraph]]
  }
  type Get   = Has[Service]
  type IO[A] = ZIO[Get, Throwable, A]

  def findParagraphs: IO[List[Paragraph]] = URIO.accessM(_.get.findParagraphs)
  def findParagraph(args: QueryParagraphArgs): IO[Option[Paragraph]] =
    URIO.accessM(_.get.findParagraph(args))
  def addParagraph(args: MutationAddParagraphArgs): IO[Paragraph] =
    URIO.accessM(_.get.addParagraph(args))
  def removeText(args: MutationRemoveTextArgs): IO[Option[Paragraph]] =
    URIO.accessM(_.get.removeText(args))
  def addText(args: MutationAddTextArgs): IO[Option[Paragraph]] =
    URIO.accessM(_.get.addText(args))

  type Repository = repository.Paragraph
  def make: ZLayer[Any, Nothing, Get] =
    ZLayer.fromEffect {
      for {
        repository <- UsesDatabase.setup(new Repository with H2Database)
      } yield new Service {
        import repository._

        def findParagraphs                          = paragraph.getAll.map(_.toList).orDie
        def findParagraph(args: QueryParagraphArgs) = paragraph.getBy(args.id).orDie
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
        def addText(args: MutationAddTextArgs) = {
//          val action = Action.Add(args.text, args.offset)
          paragraph
            .getBy(args.paragraphId)
            .flatMap(
              _.map(it => it.copy(content = it.content.patch(args.offset, args.text, 0)))
                .map(paragraph.update)
                .getOrElse(ZIO.none)
            )
            .orDie
        }

      }
    }

}
