package com.github.nnnnusui.historizer.controller

import caliban.{GraphQL, RootResolver}
import caliban.GraphQL.graphQL
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers._
import com.github.nnnnusui.historizer.Service
import com.github.nnnnusui.historizer.controller.Types._
import zio.clock.Clock
import zio.console.Console

object Api extends GenericSchema[Service.Get] {
//  val session = {
//    case class Query(session: URIO[Session.Get, Option[Session]])
//    graphQL(RootResolver(Query(session = URIO.accessM(_.get.get))))
//  }
  val api: GraphQL[Console with Clock with Service.Get] =
    graphQL(Operation.resolver) @@ printErrors @@ apolloTracing

  object Operation {
    def resolver: RootResolver[Query, Mutation, Subscription] =
      RootResolver(
        Query(
          texts = Service.findTexts,
          text = Service.findText
        ),
        Mutation(
          addText = Service.addText,
          addPartialText = Service.addPartialText
//          addCursor = Service.addCursor
//          moveCursor = Service.moveCursor
        ),
        Subscription(
          addedText = Service.addedText,
          updatedText = Service.updatedText
        )
      )
    import Output._
    case class Query(
        texts: Service.IO[Seq[Text]],
        text: Input[QueryTextArgs] => Service.IO[Option[Text]]
    )
    case class Mutation(
        addText: Service.IO[Text],
        addPartialText: Input[MutationAddPartialTextArgs] => Service.IO[Text]
//        addCursor: Input[MutationAddCursorArgs] => Service.IO[Cursor]
    )
    case class Subscription(
        addedText: Service.Stream[Text],
        updatedText: Input[SubscriptionUpdatedTextArgs] => Service.Stream[Text]
    )
  }

}
