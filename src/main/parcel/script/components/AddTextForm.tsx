import { useMutation } from "@apollo/client";
import React from "react";
import { AddTextDocument } from "../graphql/generated";

export const AddTextForm: React.FC = () => {
  const [addText, { loading, error }] = useMutation(AddTextDocument);
  if (loading) return <p>...loading</p>;
  if (error) return <p>{error.message}</p>;

  const onSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    addText({ variables: {} });
  };
  return (
    <form onSubmit={onSubmit}>
      <input type="submit" value="Add" />
    </form>
  );
};
