package com.github.nnnnusui.historizer.controller

import caliban.{GraphQL, RootResolver}
import caliban.GraphQL.graphQL
import caliban.schema.GenericSchema

import com.github.nnnnusui.historizer.controller.Types._
import com.github.nnnnusui.historizer.usecase.CaretService

object CaretApi extends GenericSchema[CaretService.Get] {
  val api: GraphQL[CaretService.Get] =
    graphQL(Operation.resolver)

  object Operation {
    def resolver: RootResolver[Query, Mutation, Subscription] =
      RootResolver(
        Query(
          carets = CaretService.findCarets,
          caret = CaretService.findCaret
        ),
        Mutation(
          addCaret = CaretService.addCaret,
          moveCaret = CaretService.moveCaret,
          deleteCaret = CaretService.deleteCaret
        ),
        Subscription(
          addedCaret = CaretService.addedCaret,
          movedCaret = CaretService.movedCaret,
          deletedCaret = CaretService.deletedCaret
        )
      )
    import Output._
    case class Query(
        carets: Input[QueryCaretsArgs] => CaretService.IO[Seq[Caret]],
        caret: Input[QueryCaretArgs] => CaretService.IO[Option[Caret]]
    )
    case class Mutation(
        addCaret: Input[MutationAddCaretArgs] => CaretService.IO[Caret],
        moveCaret: Input[MutationMoveCaretArgs] => CaretService.IO[Caret],
        deleteCaret: Input[MutationDeleteCaretArgs] => CaretService.IO[Boolean]
    )
    case class Subscription(
        addedCaret: Input[SubscriptionAddedCaretArgs] => CaretService.Stream[Caret],
        movedCaret: Input[SubscriptionMovedCaretArgs] => CaretService.Stream[Caret],
        deletedCaret: Input[SubscriptionDeletedCaretArgs] => CaretService.Stream[CaretId]
    )
  }

}
