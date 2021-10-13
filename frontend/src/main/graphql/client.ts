import { ApolloClient, InMemoryCache, HttpLink, split } from "@apollo/client";
import { WebSocketLink } from '@apollo/client/link/ws';
import { getMainDefinition } from "@apollo/client/utilities";

const httpLink = new HttpLink({
  uri: "http://localhost:8088/api/graphql",
})
const webSocketLink = new WebSocketLink({
  uri: 'ws://localhost:8088/ws/graphql',
  options: {
    reconnect: true
  }
});
const splitLink = split(
  ({ query }) => {
    const definition = getMainDefinition(query);
    return (
      definition.kind === 'OperationDefinition' &&
      definition.operation === 'subscription'
    );
  },
  webSocketLink,
  httpLink,
);

export const client = new ApolloClient({
  cache: new InMemoryCache(),
  link: splitLink,
});
