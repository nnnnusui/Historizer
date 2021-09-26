import { gql } from "@apollo/client";

export const EditParagraph = gql`
  mutation editParagraph(
    $id: String!,
    $offset: Int!,
    $length: Int!,
    $to: String!,
  ) { 
    editParagraph(id: $id, offset: $offset, length: $length, to: $to) { id content }
  }
`;
