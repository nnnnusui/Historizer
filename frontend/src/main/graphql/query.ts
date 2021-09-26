import gql from "graphql-tag";

export const GetParagraphs = gql`
  query getParagraphs {
    paragraphs { id content }
  }
`;

// export const GetSections = gql`
//   query getSections {
//     paragraphs {
//       id
//     }
//   }
// `

// export const GetSection = gql`
//   query getSection($id: Int!) {
//     section(id: $id) {
//       id
//     }
//   }
// `;