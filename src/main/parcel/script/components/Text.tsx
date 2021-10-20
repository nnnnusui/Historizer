import { useMutation, useQuery } from "@apollo/client";
import React, { useEffect, useState } from "react";
import {
  AddPartialTextDocument,
  GetTextDocument,
  UpdatedTextDocument,
} from "../graphql/generated";
import { Editor } from "./Editor";

export const Text: React.FC<{ id: string }> = ({ id }) => {
  const [editing, setEditing] = useState(true);
  const [addPartialText] = useMutation(AddPartialTextDocument);
  const { loading, error, data, subscribeToMore } = useQuery(GetTextDocument, {
    variables: { args: { id } },
  });
  useEffect(() => {
    subscribeToMore({
      document: UpdatedTextDocument,
      variables: { args: { id } },
      updateQuery: (
        { text: current },
        {
          subscriptionData: {
            data: {
              updatedText: { value: updatedValue },
            },
          },
        }
      ) => ({ text: { ...current, value: updatedValue } }),
    });
  }, []);
  if (loading) return <p>...loading</p>;
  if (error) return <p>{error.message}</p>;
  const {
    text: { value },
  } = data;
  const onApply = (value) => {
    addPartialText({ variables: { args: { textId: id, offset: 0, value } } });
    setEditing(false);
  };
  return (
    <span id={id}>
      {value}
      {editing ? <Editor tagName="span" onApply={onApply} /> : <></>}
    </span>
  );
};
