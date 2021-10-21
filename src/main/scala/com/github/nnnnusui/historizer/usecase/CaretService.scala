package com.github.nnnnusui.historizer.usecase

import zio.{Has, Hub, Ref, UIO, URIO, ZIO, ZLayer}
import zio.stream.{UStream, ZStream}

import com.github.nnnnusui.historizer.controller.Types._

object CaretService {
  trait Service {
    def findCarets(args: QueryCaretsArgs): UIO[Seq[Output.Caret]]
    def findCaret(args: QueryCaretArgs): UIO[Option[Output.Caret]]
    def addCaret(args: MutationAddCaretArgs): UIO[Output.Caret]
    def addedCaret(args: SubscriptionAddedCaretArgs): UStream[Output.Caret]
    def moveCaret(args: MutationMoveCaretArgs): UIO[Output.Caret]
    def movedCaret(args: SubscriptionMovedCaretArgs): UStream[Output.Caret]
    def deleteCaret(args: MutationDeleteCaretArgs): UIO[Boolean]
    def deletedCaret(args: SubscriptionDeletedCaretArgs): UStream[CaretId]
  }
  type Get       = Has[Service]
  type IO[A]     = ZIO[Get, Throwable, A]
  type Stream[A] = ZStream[Get, Throwable, A]
  def findCarets(input: Input[QueryCaretsArgs]): IO[Seq[Output.Caret]] =
    URIO.accessM(_.get.findCarets(input.args))
  def findCaret(input: Input[QueryCaretArgs]): IO[Option[Output.Caret]] =
    URIO.accessM(_.get.findCaret(input.args))
  def addCaret(input: Input[MutationAddCaretArgs]): IO[Output.Caret] =
    URIO.accessM(_.get.addCaret(input.args))
  def addedCaret(input: Input[SubscriptionAddedCaretArgs]): Stream[Output.Caret] =
    ZStream.accessStream(_.get.addedCaret(input.args))
  def moveCaret(input: Input[MutationMoveCaretArgs]): IO[Output.Caret] =
    URIO.accessM(_.get.moveCaret(input.args))
  def movedCaret(input: Input[SubscriptionMovedCaretArgs]): Stream[Output.Caret] =
    ZStream.accessStream(_.get.movedCaret(input.args))
  def deleteCaret(input: Input[MutationDeleteCaretArgs]): IO[Boolean] =
    URIO.accessM(_.get.deleteCaret(input.args))
  def deletedCaret(input: Input[SubscriptionDeletedCaretArgs]): Stream[CaretId] =
    ZStream.accessStream(_.get.deletedCaret(input.args))

  case class Caret(textId: TextId, output: Output.Caret)
  def make: ZLayer[Any, Nothing, Has[Service]] = ZLayer.fromEffect {
    for {
      caret           <- Ref.make(Seq.empty[Caret])
      addedCaretHub   <- Hub.unbounded[Caret]
      movedCaretHub   <- Hub.unbounded[Caret]
      deletedCaretHub <- Hub.unbounded[CaretId]
    } yield new Service {

      override def findCarets(args: QueryCaretsArgs): UIO[Seq[Output.Caret]] =
        caret.get.map(_.filter(_.textId == args.textId).map(_.output))
      override def findCaret(args: QueryCaretArgs): UIO[Option[Output.Caret]] =
        caret.get.map(_.find(_.output.id == args.id).map(_.output))

      override def addCaret(args: MutationAddCaretArgs): UIO[Output.Caret] =
        caret.get
          .map(it => {
            val size   = it.size
            val output = Output.Caret(size.toString, args.offset)
//            val domain = Caret(args.textId, output)
//            caret.update(_ :+ domain)
//            addedCaretHub.publish(domain)
            output
          })
      override def addedCaret(args: SubscriptionAddedCaretArgs): UStream[Output.Caret] =
        ZStream.unwrapManaged(
          addedCaretHub.subscribe
            .map(_.filterOutput(_.textId == args.textId))
            .map(_.map(_.output))
            .map(ZStream.fromQueue(_))
        )

      override def moveCaret(args: MutationMoveCaretArgs): UIO[Output.Caret] = {
        for {
          it <- caret.get.map(_.find(_.output.id == args.id))
          Caret(textId, _output) = it.get
        } yield {
          val output = _output.copy(offset = args.offset)
          val domain = Caret(textId, output)
          caret.update(_.map(it => if (it.output.id == args.id) domain else it))
          movedCaretHub.publish(domain)
          output
        }
      }
      override def movedCaret(args: SubscriptionMovedCaretArgs): UStream[Output.Caret] =
        ZStream.unwrapManaged(
          movedCaretHub.subscribe
            .map(_.filterOutput(_.output.id == args.id))
            .map(_.map(_.output))
            .map(ZStream.fromQueue(_))
        )

      override def deleteCaret(args: MutationDeleteCaretArgs): UIO[Boolean] =
        caret
          .modify(it => {
            if (it.exists(_.output.id == args.id)) (true, it.filterNot(_.output.id == args.id))
            else (false, it)
          })
          .tap(deleted => UIO.when(deleted)(deletedCaretHub.publish(args.id)))
      override def deletedCaret(args: SubscriptionDeletedCaretArgs): UStream[CaretId] =
        ZStream.unwrapManaged(
          deletedCaretHub.subscribe
            .map(_.filterOutput(_ == args.id))
            .map(ZStream.fromQueue(_))
        )
    }
  }
}
