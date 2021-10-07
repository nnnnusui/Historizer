package com.github.nnnnusui.historizer

import com.github.nnnnusui.historizer.Types._
import com.github.nnnnusui.historizer.domain.{Action, Text}
import com.github.nnnnusui.historizer.interop.slick.akka.UsesExecutionContext
import com.github.nnnnusui.historizer.interop.slick.zio.UsesDatabase
import zio.{Has, UIO, URIO, ZIO, ZLayer}

import scala.concurrent.ExecutionContext

object Service {
  trait Service {
    def findTexts: UIO[List[Text]]
    def findText(args: QueryTextArgs): UIO[Option[Text]]
    def newText(args: MutationNewTextArgs): UIO[Text]
    def removeText(args: MutationRemoveTextArgs): UIO[Option[Text]]
    def addText(args: MutationAddTextArgs): UIO[Option[Text]]
    def undo(args: MutationUndoArgs): UIO[Option[Text]]
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
  def undo(input: Input[MutationUndoArgs]): IO[Option[Text]] =
    URIO.accessM(_.get.undo(input.args))

  type Repository = repository.Text with repository.UndoAction with repository.Historizer
  def make(implicit executionContext: ExecutionContext): ZLayer[Any, Nothing, Get] =
    ZLayer.fromEffect {
      for {
        repository <- UsesDatabase.setup(
          new UsesExecutionContext
            with H2Database
            with repository.Text
            with repository.UndoAction
            with repository.Historizer
        )
      } yield new Service {
        import repository._

        def findTexts                          = text.getAll.map(_.toList).orDie
        def findText(args: QueryTextArgs)      = text.getBy(args.id).orDie
        def newText(args: MutationNewTextArgs) = text.create(Text(_, args.value)).orDie

        def removeText(args: MutationRemoveTextArgs) =
          text
            .getBy(args.textId)
            .flatMap {
              _.map { text: Text =>
                val action = Action.RemovePartialText(args.offset, args.length)
                text.actedOn(action)
              }
                .map(historizer.add)
                .getOrElse(ZIO.none)
            }
            .orDie
        def addText(args: MutationAddTextArgs) =
          text
            .getBy(args.textId)
            .flatMap {
              _.map { text =>
                val action = Action.AddPartialText(args.offset, args.text)
                text.actedOn(action).state
              }
                .map(text.update)
                .getOrElse(ZIO.none)
            }
            .orDie

        def undo(args: MutationUndoArgs) =
          text
            .getBy(args.textId)
            .map(ZIO.fromOption)
            .flatten
            .map { text =>
              historizer
                .undoText(text.id)
                .map(ZIO.fromOption)
                .flatten
                .map(text.actedOn)
            }
            .flatten
            .map(_.state)
            .orDie
      }
    }
}
