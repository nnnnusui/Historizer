import { useMutation } from "@apollo/client";
import React, { useState } from "react";
import { AddArticleDocument } from "../graphql/generated";

export const AddArticleForm: React.FC = () => {
  const [formInput, setFormInput] = useState({ title: "" });
  const [addArticle, { loading, error }] = useMutation(AddArticleDocument);
  if (loading) return <p>...loading</p>;
  if (error) return <p>{error.message}</p>;

  const onChange = (event: React.FormEvent<HTMLInputElement>) => {
    const { name, value } = event.currentTarget;
    setFormInput((prev) => ({ ...prev, [name]: value }));
  };

  const onSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    console.log(formInput);
    addArticle({ variables: { args: formInput } });
  };

  return (
    <form onSubmit={onSubmit}>
      <input type="text" name="title" onChange={onChange} />
      <input type="submit" value="Add" />
    </form>
  );
};
