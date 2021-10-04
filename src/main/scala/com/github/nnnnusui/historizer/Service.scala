package com.github.nnnnusui.historizer

import com.github.nnnnusui.historizer.Types._
import com.github.nnnnusui.historizer.domain.{Action, Paragraph, Text}
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase
import zio.{Has, UIO, URIO, ZIO, ZLayer}

object Service {
  trait Service {
    def findTexts: UIO[List[Text]]
    def findText(args: QueryTextArgs): UIO[Option[Text]]
    def newText(args: MutationNewTextArgs): UIO[Text]
    def removeText(args: MutationRemoveTextArgs): UIO[Option[Text]]
    def addText(args: MutationAddTextArgs): UIO[Option[Text]]
  }
  type Get   = Has[Service]
  type IO[A] = ZIO[Get, Throwable, A]

  def findTexts: IO[List[Text]] = URIO.accessM(_.get.findTexts)
  def findText(input: Input[QueryTextArgs]): IO[Option[Text]] =
    URIO.accessM(_.get.findText(input.args))
  def newText(input: Input[MutationNewTextArgs]): IO[Text] =
    URIO.accessM(_.get.newText(input.args))
  def removeText(input: Input[MutationRemoveTextArgs]): IO[Option[Text]] =
    URIO.accessM(_.get.removeText(input.args))
  def addText(input: Input[MutationAddTextArgs]): IO[Option[Text]] =
    URIO.accessM(_.get.addText(input.args))

  type Repository = repository.Text
  def make: ZLayer[Any, Nothing, Get] =
    ZLayer.fromEffect {
      for {
        repository <- UsesDatabase.setup(new Repository with H2Database)
      } yield new Service {
        import repository._

        def findTexts                          = text.getAll.map(_.toList).orDie
        def findText(args: QueryTextArgs)      = text.getBy(args.id).orDie
        def newText(args: MutationNewTextArgs) = text.create(Text(_, args.value)).orDie
        def removeText(args: MutationRemoveTextArgs) =
          text
            .getBy(args.textId)
            .flatMap {
              _.map(it => it.copy(value = it.value.patch(args.offset, "", args.length)))
                .map(text.update)
                .getOrElse(ZIO.none)
            }
            .orDie
        def addText(args: MutationAddTextArgs) = {
          val action = Action.Add(args.text, args.offset)
          text
            .getBy(args.textId)
            .flatMap(
              _.map(it => it.copy(value = it.value.patch(args.offset, args.text, 0)))
                .map(text.update)
                .getOrElse(ZIO.none)
            )
            .orDie
        }

      }
    }

}
