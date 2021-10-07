import React, { useState } from "react";
import { Schema } from "../graphql/Schema";
import { Editable } from "./Editable";

export const Text: React.FC<Schema.Text> = ({ id, value }) => {
  const [removeText] = Schema.useRemoveTextMutation({
    refetchQueries: [{ query: Schema.GetTextsDocument }],
  });

  const [editorPoint, setEditorPoint] = useState<number>(null);
  const startSelect = (event: React.PointerEvent) => {
    event.currentTarget.setPointerCapture(event.pointerId);
  };
  const endSelect = () => {
    const selection = document.getSelection();
    const range = selection.getRangeAt(0);
    const offset = range.startOffset;
    const length = range.endOffset - offset;
    removeText({ variables: { args: { textId: id, offset, length } } });
    setEditorPoint(offset);
  };

  if (!editorPoint)
    return (
      <p>
        <span onPointerDown={startSelect} onLostPointerCapture={endSelect}>
          {value}
        </span>
      </p>
    );
  else
    return (
      <p>
        <span onPointerDown={startSelect} onLostPointerCapture={endSelect}>
          {value.substring(0, editorPoint)}
        </span>
        <Editable
          autoFocus
          textId={id}
          offset={editorPoint}
          onApply={() => setEditorPoint(null)}
        />
        <span onPointerDown={startSelect} onLostPointerCapture={endSelect}>
          {value.substring(editorPoint)}
        </span>
      </p>
    );
};
