import React, { useRef } from "react";
import { Schema } from "../graphql/Schema";

export const Editable: React.FC<{
  textId: number;
  offset: number;
  autoFocus: boolean;
  onApply: () => void;
}> = ({ textId, offset, autoFocus, onApply }) => {
  const [addText] = Schema.useAddTextMutation({
    refetchQueries: [{ query: Schema.GetTextsDocument }],
  });
  const ref = useRef<HTMLElement>();
  React.useEffect(() => {
    const node = ref.current;
    if (autoFocus && !node) return;
    node.focus();
  }, []);
  return (
    <span
      ref={ref}
      contentEditable
      onPointerDown={(event) => event.stopPropagation()}
      onKeyDown={(event) => {
        if (event.key !== "Enter") return;
        event.preventDefault();
        const target = event.target as HTMLElement;
        addText({
          variables: { args: { textId, offset, text: target.textContent } },
        }).then(onApply);
        // return false;
      }}
    ></span>
  );
};
