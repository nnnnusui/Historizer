package com.github.nnnnusui.historizer.controller

import caliban.GraphQL.graphQL
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers._
import caliban.{GraphQL, RootResolver}
import com.github.nnnnusui.historizer.Service
import com.github.nnnnusui.historizer.controller.Types._
import zio.clock.Clock
import zio.console.Console

object Api extends GenericSchema[Service.Get] {
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
          addText = Service.addText
        ),
        Subscription(
          addedText = Service.addedText
        )
      )
    import Output._
    case class Query(
        texts: Service.IO[Seq[Text]],
        text: Input[QueryTextArgs] => Service.IO[Option[Text]]
    )
    case class Mutation(
        addText: Service.IO[Text]
    )
    case class Subscription(
        addedText: Service.Stream[Text]
    )
  }

}
