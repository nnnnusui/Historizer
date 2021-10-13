import { ApolloProvider } from "@apollo/client";
import React from "react";
import { ArticleList } from "./components/ArticleList";
import { AddArticleForm } from "./components/AddArticleForm";
import { client } from "./graphql/client";

export const App = () => {
  return (
    <ApolloProvider client={client}>
      <h1>Historzer</h1>
      <AddArticleForm />
      <ArticleList />
    </ApolloProvider>
  );
};
