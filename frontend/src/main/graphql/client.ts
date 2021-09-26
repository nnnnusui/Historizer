import { ApolloClient, HttpLink, InMemoryCache } from "apollo-boost";

export const client = new ApolloClient({
  cache: new InMemoryCache(),
  link: new HttpLink({
    uri: 'http://localhost:8088/graphql'
  })
})
