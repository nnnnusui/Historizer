import { gql } from '@apollo/client';
import * as Apollo from '@apollo/client';
export type Maybe<T> = T | null;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
const defaultOptions =  {}
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
};

export type Query = {
  __typename?: 'Query';
  paragraph?: Maybe<Paragraph>;
  paragraphs: Array<Paragraph>;
};


export type QueryParagraphArgs = {
  id: Scalars['String'];
};

export type Paragraph = {
  __typename?: 'Paragraph';
  content: Scalars['String'];
  id: Scalars['String'];
};

export type Mutation = {
  __typename?: 'Mutation';
  editParagraph?: Maybe<Paragraph>;
};


export type MutationEditParagraphArgs = {
  id: Scalars['String'];
  length: Scalars['Int'];
  offset: Scalars['Int'];
  to: Scalars['String'];
};

export type EditParagraphMutationVariables = Exact<{
  id: Scalars['String'];
  offset: Scalars['Int'];
  length: Scalars['Int'];
  to: Scalars['String'];
}>;


export type EditParagraphMutation = { __typename?: 'Mutation', editParagraph?: Maybe<{ __typename?: 'Paragraph', id: string, content: string }> };

export type GetParagraphsQueryVariables = Exact<{ [key: string]: never; }>;


export type GetParagraphsQuery = { __typename?: 'Query', paragraphs: Array<{ __typename?: 'Paragraph', id: string, content: string }> };


export const EditParagraphDocument = gql`
    mutation editParagraph($id: String!, $offset: Int!, $length: Int!, $to: String!) {
  editParagraph(id: $id, offset: $offset, length: $length, to: $to) {
    id
    content
  }
}
    `;
export type EditParagraphMutationFn = Apollo.MutationFunction<EditParagraphMutation, EditParagraphMutationVariables>;

/**
 * __useEditParagraphMutation__
 *
 * To run a mutation, you first call `useEditParagraphMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useEditParagraphMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [editParagraphMutation, { data, loading, error }] = useEditParagraphMutation({
 *   variables: {
 *      id: // value for 'id'
 *      offset: // value for 'offset'
 *      length: // value for 'length'
 *      to: // value for 'to'
 *   },
 * });
 */
export function useEditParagraphMutation(baseOptions?: Apollo.MutationHookOptions<EditParagraphMutation, EditParagraphMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<EditParagraphMutation, EditParagraphMutationVariables>(EditParagraphDocument, options);
      }
export type EditParagraphMutationHookResult = ReturnType<typeof useEditParagraphMutation>;
export type EditParagraphMutationResult = Apollo.MutationResult<EditParagraphMutation>;
export type EditParagraphMutationOptions = Apollo.BaseMutationOptions<EditParagraphMutation, EditParagraphMutationVariables>;
export const GetParagraphsDocument = gql`
    query getParagraphs {
  paragraphs {
    id
    content
  }
}
    `;

/**
 * __useGetParagraphsQuery__
 *
 * To run a query within a React component, call `useGetParagraphsQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetParagraphsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetParagraphsQuery({
 *   variables: {
 *   },
 * });
 */
export function useGetParagraphsQuery(baseOptions?: Apollo.QueryHookOptions<GetParagraphsQuery, GetParagraphsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetParagraphsQuery, GetParagraphsQueryVariables>(GetParagraphsDocument, options);
      }
export function useGetParagraphsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetParagraphsQuery, GetParagraphsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetParagraphsQuery, GetParagraphsQueryVariables>(GetParagraphsDocument, options);
        }
export type GetParagraphsQueryHookResult = ReturnType<typeof useGetParagraphsQuery>;
export type GetParagraphsLazyQueryHookResult = ReturnType<typeof useGetParagraphsLazyQuery>;
export type GetParagraphsQueryResult = Apollo.QueryResult<GetParagraphsQuery, GetParagraphsQueryVariables>;