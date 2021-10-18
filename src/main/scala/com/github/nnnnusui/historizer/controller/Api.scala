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
          Service.findArticles,
          Service.findArticle,
          Service.findContent
        ),
        Mutation(
          Service.addArticle,
          Service.addContent
        ),
        Subscription(
          Service.addedArticle,
          Service.addedContent
        )
      )
    import Output._
    case class Query(
        articles: Service.IO[Seq[Article]],
        article: Input[QueryArticleArgs] => Service.IO[Option[Article]],
        content: Input[QueryContentArgs] => Service.IO[Option[Content]]
    )
    case class Mutation(
        addArticle: Input[MutationAddArticleArgs] => Service.IO[Article],
        addContent: Input[MutationAddContentArgs] => Service.IO[Content]
    )
    case class Subscription(
        addedArticle: Service.Stream[Article],
        addedContent: Input[SubscriptionAddedContentArgs] => Service.Stream[Content]
    )
  }

}
