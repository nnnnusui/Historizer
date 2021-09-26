import React from "react";
import { ApolloProvider } from "react-apollo";
import { Article } from "./components/Article";
import { client } from "./graphql/client";

export const App = () => {
  return (
    <ApolloProvider client={client}>
      <h1>Historzer</h1>
      <Article></Article>
      {/* <Editable>
        <section>
          <h1>Section A</h1>
          <p>content A</p>
        </section>
        <section>
          <h1>Section B</h1>
          <p>content B</p>
        </section>
      </Editable> */}
    </ApolloProvider>
  );
};
