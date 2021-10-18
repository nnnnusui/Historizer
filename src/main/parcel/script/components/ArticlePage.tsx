import React, { useEffect } from "react";
import { useQuery } from "@apollo/client";
import { useParams } from "react-router-dom";
import { AddedContentDocument, GetArticleDocument } from "../graphql/generated";
import { AddContentForm } from "./AddContentForm";

export const ArticlePage: React.FC = () => {
  const { articleId: id } = useParams<{ articleId: string }>();
  const { loading, error, data, subscribeToMore } = useQuery(
    GetArticleDocument,
    {
      variables: { args: { id } },
    }
  );
  useEffect(() => {
    // subscribeToMore({
    //   document: AddedContentDocument,
    //   updateQuery: (
    //     { article },
    //     {
    //       subscriptionData: {
    //         data: { addedContent },
    //       },
    //     }
    //   ) => ({
    //     articles: [...currents, latest],
    //   }),
    // });
  }, []);
  if (loading) return <p>...loading</p>;
  if (error) return <p>{error.message}</p>;
  const { article } = data;
  if (!article) return <p>Article{`{id: ${id}}`} not found.</p>;

  const { title } = article;
  return (
    <>
      <h1>{title}</h1>
      <AddContentForm parentId={id} />
    </>
  );
};
