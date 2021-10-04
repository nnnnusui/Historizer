package com.github.nnnnusui.historizer

import com.github.nnnnusui.historizer.domain.Text

object Types {
  object Operations {
    case class Query(
        texts: Service.IO[List[Text]],
        text: Input[QueryTextArgs] => Service.IO[Option[Text]]
    )
    case class Mutation(
        newText: Input[MutationNewTextArgs] => Service.IO[Text],
        removeText: Input[MutationRemoveTextArgs] => Service.IO[Option[Text]],
        addText: Input[MutationAddTextArgs] => Service.IO[Option[Text]]
    )
  }

  case class Input[T](args: T)
  case class QueryTextArgs(id: Text.ID)

  case class MutationNewTextArgs(value: String)
  case class MutationRemoveTextArgs(textId: Text.ID, offset: Int, length: Int)
  case class MutationAddTextArgs(textId: Text.ID, offset: Int, text: String)
}
