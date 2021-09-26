import gql from "graphql-tag";

export const EditParagraph = gql`
  mutation editParagraph(
    $id: ID!,
    $offset: Int!,
    $length: Int!,
    $to: String!,
  ) { 
    editParagraph(id: $id, offset: $offset, length: $length, to: $to) { id content }
  }
`;
