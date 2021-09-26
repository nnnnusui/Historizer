package com.github.nnnnusui.historizer

import Types._

import GraphQL._

object Types {
  case class QueryParagraphArgs(id: ID)
  case class MutationEditParagraphArgs(paragraphId: ID, from: Int, to: Int)
  case class Paragraph(id: ID, content: String)

}

object Operations {

  case class Query(
      paragraphs: Service.IO[List[Paragraph]],
      paragraph: QueryParagraphArgs => Service.IO[Option[Paragraph]]
  )

  case class Mutation(
      editParagraph: MutationEditParagraphArgs => Service.IO[Paragraph]
  )

}

