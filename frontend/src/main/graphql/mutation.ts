import { gql } from "@apollo/client";

export const NewText = gql`
  mutation newText($args: MutationNewTextArgsInput!) {
    newText(args: $args) { id value }
  }
`

export const RemoveText = gql`
  mutation removeText(
    $args: MutationRemoveTextArgsInput!
  ) {
    removeText(args: $args) { id value }
  }
`
export const AddText = gql`
  mutation addText(
    $args: MutationAddTextArgsInput!
  ) {
    addText(args: $args) { id value }
  }
`
