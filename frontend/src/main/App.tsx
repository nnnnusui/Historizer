import { ApolloProvider } from "@apollo/client";
import React from "react";
import { ArticleList } from "./components/ArticleList";
import { client } from "./graphql/client";
import { BrowserRouter, Route, Switch } from "react-router-dom";
import { ArticlePage } from "./components/ArticlePage";
import { AddArticleForm } from "./components/AddArticleForm";

export const App = () => {
  return (
    <ApolloProvider client={client}>
      <BrowserRouter>
        <Switch>
          <Route path="/:articleId">
            <ArticlePage />
          </Route>
          <Route path="/">
            <h1>Historzer</h1>
            <AddArticleForm />
            <ArticleList />
          </Route>
        </Switch>
      </BrowserRouter>
    </ApolloProvider>
  );
};
