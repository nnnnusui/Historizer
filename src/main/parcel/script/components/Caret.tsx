import { useMutation, useQuery } from "@apollo/client";
import React, { useEffect } from "react";
import {
  AddCaretDocument,
  Caret as CaretSchema,
  GetCaretDocument,
  MovedCaretDocument,
  MutationAddCaretArgsInput,
  QueryCaretArgsInput,
} from "../graphql/generated";
import { TaggedUnion } from "../types/TaggedUnion";
import { Editor } from "./Editor";

type SchemaMap = {
  full: CaretSchema;
  query: QueryCaretArgsInput;
  mutation: MutationAddCaretArgsInput;
};
export const Caret: React.FC<{
  schema: TaggedUnion<SchemaMap>;
}> = ({ schema }) => {
  // const [removeCursor] = useMutation(RemoveCursorDocument);
  const [addCaret] = useMutation(AddCaretDocument);
  const caretQuery = useQuery(
    GetCaretDocument,
    schema.kind === "query"
      ? { variables: { args: { id: schema.value.id } } }
      : { skip: true }
  );
  const { id, offset } = {
    ...schema.value,
    ...caretQuery.data?.caret,
  };

  useEffect(() => {
    if (schema.kind !== "mutation") return;
    console.log(schema.value);
    addCaret({ variables: { args: schema.value } });
  }, []);
  useEffect(() => {
    if (!id) return;
    caretQuery.subscribeToMore({
      document: MovedCaretDocument,
      variables: { args: { id } },
      updateQuery: (
        { caret: current },
        {
          subscriptionData: {
            data: { movedCaret: moved },
          },
        }
      ) => ({ caret: { ...current, ...moved } }),
    });
  }, [id]);
  if (caretQuery.loading) return <span>...loading text</span>;
  if (caretQuery.error) return <span>{caretQuery.error.message}</span>;

  const color = "#ed3c21";
  return (
    <span
      // key={id}
      style={{
        display: "inline-block",
        position: "absolute",
        opacity: ".8",
      }}
    >
      <span
        style={{
          position: "absolute",
          width: ".4em",
          height: ".4em",
          backgroundColor: color,
        }}
      />
      <span
        style={{
          position: "relative",
          height: "100%",
          borderLeftWidth: "2px",
          borderLeftStyle: "solid",
          borderColor: color,
        }}
      />
    </span>
  );
};
