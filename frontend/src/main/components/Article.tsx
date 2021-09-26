import React from "react";
import { useGetParagraphsQuery } from "../generated/graphql";
import { Paragraph } from "./Paragraph";

export const Article: React.FC = () => {
  const { loading, error, data } = useGetParagraphsQuery();
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
