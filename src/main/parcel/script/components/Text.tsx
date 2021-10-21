import { useMutation, useQuery } from "@apollo/client";
import React, { useEffect, useState } from "react";
import {
  AddPartialTextDocument,
  AddTextDocument,
  GetTextDocument,
  MutationAddTextArgsInput,
  QueryTextArgsInput,
  Text as TextSchema,
  UpdatedTextDocument,
} from "../graphql/generated";
import { TaggedUnion } from "../types/TaggedUnion";
import { Caret } from "./Caret";
import { Editor } from "./Editor";

type SchemaMap = {
  full: TextSchema;
  query: QueryTextArgsInput;
  mutation: MutationAddTextArgsInput;
};
export const Text: React.FC<{
  schema: TaggedUnion<SchemaMap>;
}> = ({ schema }) => {
  const [addText] = useMutation(AddTextDocument);
  const [addPartialText] = useMutation(AddPartialTextDocument);
  const textQuery = useQuery(
    GetTextDocument,
    schema.kind === "query"
      ? { variables: { args: { id: schema.value.id } } }
      : { skip: true }
  );
  const { id, value, carets } = {
    ...schema.value,
    ...textQuery.data?.text,
  };

  useEffect(() => {
    if (schema.kind !== "mutation") return;
    addText({ variables: { args: schema.value } });
  }, []);
  useEffect(() => {
    if (!id) return;
    textQuery.subscribeToMore({
      document: UpdatedTextDocument,
      variables: { args: { id } },
      updateQuery: (
        { text: current },
        {
          subscriptionData: {
            data: { updatedText: updated },
          },
        }
      ) => ({ text: { ...current, ...updated } }),
    });
  }, [id]);

  const [editing, setEditing] = useState<number | "none">("none");
  useEffect(() => {
    if (value !== "") return;
    setEditing(0);
  }, [id]);

  if (textQuery.loading) return <span>...loading text</span>;
  if (textQuery.error) return <span>{textQuery.error.message}</span>;

  const contents = (() => {
    const { sum, beforeOffset: lastOffset } = carets.reduce(
      ({ sum, beforeOffset }, it) => {
        const { id, offset } = it;
        const text = value.substring(beforeOffset, offset);
        const caret = <Caret key={id} schema={{ kind: "full", value: it }} />;
        return { sum: [...sum, text, caret], beforeOffset: offset };
      },
      { sum: [] as (JSX.Element | string)[], beforeOffset: 0 }
    );
    return [...sum, value.substring(lastOffset, value.length)];
  })();

  const startSelect = (event: React.PointerEvent) => {
    event.currentTarget.setPointerCapture(event.pointerId);
  };
  const endSelect = () => {
    const selection = document.getSelection();
    const range = selection.getRangeAt(0);
    const offset = range.startOffset;
    const length = range.endOffset - offset;
    // addCaret({ variables: { args: { textId: id, offset } } });
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
      {contents}
    </span>
  ) : (
    <span id={id}>
      {value.substring(0, editing)}
      <Editor tagName="span" onApply={onApply} onBlur={endEdit} />
      <Caret schema={{ kind: "mutation", value: { textId: id, offset: 0 } }} />
      {value.substring(editing)}
    </span>
  );
};
