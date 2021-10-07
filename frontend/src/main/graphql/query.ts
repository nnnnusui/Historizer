import { gql } from "@apollo/client";

export const GetTexts = gql`
  query getTexts {
    texts { id value }
  }
`;
export const GetText = gql`
  query getText($args: QueryTextArgsInput!) {
    text(args: $args) { value }
  }
`
