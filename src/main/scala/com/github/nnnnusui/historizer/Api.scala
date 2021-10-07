package com.github.nnnnusui.historizer

import caliban.{GraphQL, RootResolver}
import caliban.GraphQL.graphQL
import caliban.schema.GenericSchema
import zio.clock.Clock
import zio.console.Console

object Api extends GenericSchema[GetUserService.Get] {
  val api: GraphQL[Console with Clock with GetUserService.Get] = graphQL(Operation.resolver)

  object Operation {
    case class Query()
    case class Mutation()
    case class Subscription()
    def resolver: RootResolver[Query, Mutation, Subscription] =
      RootResolver(
        Query(),
        Mutation(),
        Subscription()
      )
  }
}
