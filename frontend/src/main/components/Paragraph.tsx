import React from "react";
import { Schema } from "../graphql/Schema";

export const Paragraph: React.FC<Schema.Paragraph> = (props) => {
  const [editParagraph, { data, loading, error }] =
    Schema.useEditParagraphMutation();
  if (loading) return <p>submitting...</p>;
  if (error) return <p>{error.message}</p>;

  const { id, content } = props;
  const startSelect = (event: React.PointerEvent) => {
    event.currentTarget.setPointerCapture(event.pointerId);
  };
  const endSelect = () => {
    const selection = document.getSelection();
    const range = selection.getRangeAt(0);
    const offset = range.startOffset;
    const length = range.endOffset - offset;
    editParagraph({
      variables: { id, offset, length, to: "'uhe~~~'" },
    });
  };
  return (
    <p onPointerDown={startSelect} onLostPointerCapture={endSelect}>
      {content}
    </p>
  );
};
