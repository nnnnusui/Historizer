import { useMutation, useQuery } from "@apollo/client";
import React, { useEffect, useState } from "react";
import {
  AddPartialTextDocument,
  GetTextDocument,
  UpdatedTextDocument,
} from "../graphql/generated";
import { Editor } from "./Editor";

export const Text: React.FC<{ id: string }> = ({ id }) => {
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

  const [editing, setEditing] = useState<number | "none">("none");
  useEffect(() => {
    if (!data) return;
    if (data.text.value !== "") return;
    setEditing(0);
  }, [data]);

  if (loading) return <span>...loading</span>;
  if (error) return <span>{error.message}</span>;

  const {
    text: { value },
  } = data;
  const startSelect = (event: React.PointerEvent) => {
    event.currentTarget.setPointerCapture(event.pointerId);
  };
  const endSelect = () => {
    const selection = document.getSelection();
    const range = selection.getRangeAt(0);
    const offset = range.startOffset;
    const length = range.endOffset - offset;
    // removeText({ variables: { args: { textId: id, offset, length } } });
    setEditing(offset);
  };
  const endEdit = () => {
    setEditing("none");
  };
  const onApply = (value) => {
    if (editing !== "none" && value !== "")
      addPartialText({
        variables: { args: { textId: id, offset: editing, value } },
      });
    endEdit();
  };
  return editing === "none" ? (
    <span id={id} onPointerDown={startSelect} onLostPointerCapture={endSelect}>
      {value}
    </span>
  ) : (
    <span id={id}>
      {value.substring(0, editing)}
      <Editor tagName="span" onApply={onApply} onBlur={endEdit} />
      {value.substring(editing)}
    </span>
  );
};
