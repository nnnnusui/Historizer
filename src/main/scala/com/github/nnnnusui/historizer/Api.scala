package com.github.nnnnusui.historizer

import caliban.{GraphQL, RootResolver}
import caliban.GraphQL.graphQL
import caliban.schema.GenericSchema
import zio.clock.Clock
import zio.console.Console

object Api extends GenericSchema[Service.Get] {
  val api: GraphQL[Console with Clock with Service.Get] =
    graphQL(
      RootResolver(
        Operations.Query(
//          args => Service.find(args.id),
          Service.findParagraphs
        )
      )
    )
}
