import React from "react";
import { useQuery } from "@apollo/client";
import { useParams } from "react-router-dom";
import { GetArticleDocument } from "../graphql/generated";

export const ArticlePage: React.FC = () => {
  const { articleId: id } = useParams<{ articleId: string }>();
  const { loading, error, data } = useQuery(GetArticleDocument, {
    variables: { args: { id } },
  });
  if (loading) return <p>...loading</p>;
  if (error) return <p>{error.message}</p>;
  const { article } = data;
  if (!article) return <p>Article{`{id: ${id}}`} not found.</p>;

  const { title } = article;
  return (
    <>
      <h1>{title}</h1>
    </>
  );
};
