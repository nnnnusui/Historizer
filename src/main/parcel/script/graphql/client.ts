import { ApolloClient, InMemoryCache, HttpLink, split } from "@apollo/client";
import { WebSocketLink } from "@apollo/client/link/ws";
import { getMainDefinition } from "@apollo/client/utilities";

const webSocketOrigin = (() => {
  const { protocol: _protocol, host } = window.location;
  const protocol = _protocol === "https:" ? "wss:" : "ws:";
  return `${protocol}//${host}`;
})();

const httpLink = new HttpLink({
  uri: "/api/graphql",
});
const webSocketLink = new WebSocketLink({
  uri: `${webSocketOrigin}/ws/graphql`,
  options: {
    reconnect: true,
    connectionParams: {
      credentials: "include",
    },
  },
});
const splitLink = split(
  ({ query }) => {
    const definition = getMainDefinition(query);
    return (
      definition.kind === "OperationDefinition" &&
      definition.operation === "subscription"
    );
  },
  webSocketLink,
  httpLink
);

export const client = new ApolloClient({
  cache: new InMemoryCache(),
  link: splitLink,
});
