import { useQuery } from "@apollo/client";
import React from "react";
import { GetTextDocument } from "../graphql/generated";
import { Editor } from "./Editor";

export const Text: React.FC<{ id: string }> = ({ id }) => {
  const { loading, error, data } = useQuery(GetTextDocument, {
    variables: { args: { id } },
  });
  if (loading) return <p>...loading</p>;
  if (error) return <p>{error.message}</p>;
  const {
    text: { value },
  } = data;
  const onApply = () => {};
  return (
    <span id={id}>
      {id}: <Editor tagName="span" onApply={onApply} />
    </span>
  );
};
