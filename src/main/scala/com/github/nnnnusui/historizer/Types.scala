package com.github.nnnnusui.historizer

import com.github.nnnnusui.historizer.GraphQL.ID
import com.github.nnnnusui.historizer.domain.Paragraph

object Types {
  object Operations {
    case class Query(
        paragraphs: Service.IO[List[Paragraph]],
        paragraph: QueryParagraphArgs => Service.IO[Option[Paragraph]]
    )
    case class Mutation(
        addParagraph: MutationAddParagraphArgs => Service.IO[Paragraph],
        removeText: MutationRemoveTextArgs => Service.IO[Option[Paragraph]],
        addText: MutationAddTextArgs => Service.IO[Option[Paragraph]]
    )
  }

  case class QueryParagraphArgs(id: Paragraph.ID)

  case class MutationRemoveTextArgs(paragraphId: ID, offset: Int, length: Int)
  case class MutationAddParagraphArgs(content: String)
  case class MutationAddTextArgs(paragraphId: ID, offset: Int, text: String)
}
