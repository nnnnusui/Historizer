package com.github.nnnnusui.historizer.controller

import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers._

import com.github.nnnnusui.historizer.Service
import com.github.nnnnusui.historizer.controller.Types._
import com.github.nnnnusui.historizer.usecase.CaretService

object Api {
  val schema = new GenericSchema[Service.Get with CaretService.Get] {}
  import schema._
  val api = graphQL(Operation.resolver) |+| CaretApi.api @@ printErrors @@ apolloTracing

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
        addText: Input[MutationAddTextArgs] => Service.IO[Text],
        addPartialText: Input[MutationAddPartialTextArgs] => Service.IO[Text]
    )
    case class Subscription(
        addedText: Service.Stream[Text],
        updatedText: Input[SubscriptionUpdatedTextArgs] => Service.Stream[Text]
    )
  }

}
