import { useQuery } from "@apollo/client";
import React, { useEffect } from "react";
import {
  AddedArticleDocument,
  GetArticlesDocument,
} from "../graphql/generated";

export const ArticleList: React.FC = () => {
  const { loading, error, data, subscribeToMore } =
    useQuery(GetArticlesDocument);
  useEffect(() => {
    subscribeToMore({
      document: AddedArticleDocument,
      updateQuery: (
        { articles: currents },
        {
          subscriptionData: {
            data: { addedArticle: latest },
          },
        }
      ) => ({
        articles: [...currents, latest],
      }),
    });
  }, []);
  if (loading) return <p>...loading</p>;
  if (error) return <p>{error.message}</p>;

  const { articles } = data;
  return (
    <ul>
      {articles.map(({ id, title }) => (
        <li key={id}>{title}</li>
      ))}
    </ul>
  );
};
