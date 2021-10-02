package com.github.nnnnusui.historizer

import caliban.{GraphQL, RootResolver}
import caliban.GraphQL.graphQL
import caliban.schema.GenericSchema
import caliban.wrappers.Wrappers.printErrors
import zio.clock.Clock
import zio.console.Console

import com.github.nnnnusui.historizer.Types.Operations

object Api extends GenericSchema[Service.Get] {
  val api: GraphQL[Console with Clock with Service.Get] =
    graphQL(
      RootResolver(
        Operations.Query(
          Service.findParagraphs,
          args => Service.findParagraph(args)
        ),
        Operations.Mutation(
          args => Service.addParagraph(args),
          args => Service.removeText(args),
          args => Service.addText(args)
        )
      )
    ) @@ printErrors
}
