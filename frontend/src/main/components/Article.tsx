import React from "react";
import { useQuery } from "react-apollo";
import { GetParagraphs } from "../graphql/query";
import { Schema } from "../Schema";
import { Paragraph } from "./Paragraph";

export const Article: React.FC = () => {
  const { loading, error, data } = useQuery<Schema.Query>(GetParagraphs);
  if (loading) return <p>...loading</p>;
  if (error) return <p>{error.message}</p>;

  return (
    <article>
      {data.paragraphs.map((it) => (
        <Paragraph key={it.id} {...it} />
      ))}
    </article>
  );
};
