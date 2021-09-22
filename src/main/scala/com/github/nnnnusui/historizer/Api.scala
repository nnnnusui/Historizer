package com.github.nnnnusui.historizer

import caliban.{GraphQL, RootResolver}
import caliban.GraphQL.graphQL
import caliban.schema.GenericSchema
import zio.clock.Clock
import zio.console.Console

object Api extends GenericSchema[GetUserService.Get] {
  val api: GraphQL[Console with Clock with GetUserService.Get] =
    graphQL(
      RootResolver(
        Operations.Query(
          args => GetUserService.findUser(args.id),
          GetUserService.findUsers
        )
      )
    )
}
