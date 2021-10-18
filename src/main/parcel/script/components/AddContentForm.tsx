import React, { useState } from "react";
import { useMutation } from "@apollo/client";
import { AddContentDocument, ContentUnion } from "../graphql/generated";
import { Editor } from "./Editor";

export const AddContentForm: React.FC<{ parentId: string }> = ({
  parentId,
}) => {
  const [inEdit, setInEdit] = useState<keyof JSX.IntrinsicElements>(null);
  const [addContent, { loading, error }] = useMutation(AddContentDocument);
  if (loading) return <p>...loading</p>;
  if (error) return <p>{error.message}</p>;

  if (inEdit !== null)
    return (
      <Editor
        tagName={inEdit}
        onApply={(text) => {
          setInEdit(null);
          console.log(text);
        }}
      />
    );

  return (
    <form
      onSubmit={(event) => {
        event.preventDefault();
      }}
    >
      <span>add: </span>
      <input
        type="submit"
        value="paragraph"
        onClick={() =>
          addContent({
            variables: { args: { content: ContentUnion.Paragraph, parentId } },
          })
        }
      />
      {/* <input
        type="submit"
        value="section"
        onClick={() =>
          addContent({ variables: { args: ContentUnion.Section } })
        }
      /> */}
    </form>
  );
};
