package com.github.nnnnusui.historizer

import Types._

import GraphQL._

object Types {

  case class Paragraph(id: ID, content: String)

}

object Operations {

  case class Query(
      paragraphs: Service.IO[List[Paragraph]]
  )

}

