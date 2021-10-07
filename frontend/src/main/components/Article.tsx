import React from "react";
import { Schema } from "../graphql/Schema";
import { Text } from "./Text";

const AddParagraphButton: React.FC = () => {
  const [newText, { loading, error, data }] = Schema.useNewTextMutation({
    refetchQueries: [{ query: Schema.GetTextsDocument }],
  });
  if (loading) return <p>...loading</p>;
  if (error) return <p>{error.message}</p>;

  const onClick = () =>
    newText({
      variables: { args: { value: "てすとてきすと" } },
    });

  return <button onClick={onClick}>addText</button>;
};
export const Article: React.FC = () => {
  const { loading, error, data } = Schema.useGetTextsQuery();
  if (loading) return <p>...loading</p>;
  if (error) return <p>{error.message}</p>;

  return (
    <article>
      {data.texts.map((it) => (
        <Text key={it.id} {...it} />
      ))}
      <AddParagraphButton />
    </article>
  );
};
