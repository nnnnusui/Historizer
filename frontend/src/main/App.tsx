import { ApolloProvider } from "@apollo/client";
import React from "react";
import { Article } from "./components/Article";
import { client } from "./graphql/client";

export const App = () => {
  return (
    <ApolloProvider client={client}>
      <h1>Historzer</h1>
      <Article />
    </ApolloProvider>
  );
};
