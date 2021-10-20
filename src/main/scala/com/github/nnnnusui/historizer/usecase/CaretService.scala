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

  def make: ZLayer[Any, Nothing, Has[Service]] = ZLayer.fromEffect {
    for {
      caret           <- Ref.make(Seq.empty[Output.Caret])
      addedCaretHub   <- Hub.unbounded[Output.Caret]
      movedCaretHub   <- Hub.unbounded[Output.Caret]
      deletedCaretHub <- Hub.unbounded[CaretId]
    } yield new Service {

      override def findCarets(args: QueryCaretsArgs): UIO[Seq[Output.Caret]] =
        caret.get.map(_.filter(_.textId == args.textId))
      override def findCaret(args: QueryCaretArgs): UIO[Option[Output.Caret]] =
        caret.get.map(_.find(_.id == args.id))

      override def addCaret(args: MutationAddCaretArgs): UIO[Output.Caret] =
        caret.get
          .map(it => {
            val size   = it.size
            val output = Output.Caret(size.toString, args.textId, args.offset)
            caret.update(_ :+ output)
            output
          })
          .tap(addedCaretHub.publish)
      override def addedCaret(args: SubscriptionAddedCaretArgs): UStream[Output.Caret] =
        ZStream.unwrapManaged(
          addedCaretHub.subscribe
            .map(_.filterOutput(_.textId == args.textId))
            .map(ZStream.fromQueue(_))
        )

      override def moveCaret(args: MutationMoveCaretArgs): UIO[Output.Caret] = {
        for {
          it <- caret.get.map(_.find(_.id == args.id))
        } yield {
          val output = it.get.copy(offset = args.offset)
          caret.update(_.map(it => if (it.id == args.id) output else it))
          output
        }
      }.tap(movedCaretHub.publish)
      override def movedCaret(args: SubscriptionMovedCaretArgs): UStream[Output.Caret] =
        ZStream.unwrapManaged(
          movedCaretHub.subscribe
            .map(_.filterOutput(_.id == args.id))
            .map(ZStream.fromQueue(_))
        )

      override def deleteCaret(args: MutationDeleteCaretArgs): UIO[Boolean] =
        caret
          .modify(it => {
            if (it.exists(_.id == args.id)) (true, it.filterNot(_.id == args.id))
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
