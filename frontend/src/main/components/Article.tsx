import React from "react";
import { Schema } from "../graphql/Schema";
import { Paragraph } from "./Paragraph";

export const Article: React.FC = () => {
  const { loading, error, data } = Schema.useGetParagraphsQuery();
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
