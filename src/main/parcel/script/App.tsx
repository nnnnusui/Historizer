import { ApolloProvider } from "@apollo/client";
import React from "react";
import { client } from "./graphql/client";
import { BrowserRouter, Route, Switch } from "react-router-dom";
import { TextList } from "./components/TextList";
import { AddTextForm } from "./components/AddTextForm";

export const App = () => {
  return (
    <ApolloProvider client={client}>
      <BrowserRouter>
        <Switch>
          <Route path="/:articleId"></Route>
          <Route path="/">
            <h1>Historzer</h1>
            <AddTextForm />
            <TextList />
          </Route>
        </Switch>
      </BrowserRouter>
    </ApolloProvider>
  );
};
