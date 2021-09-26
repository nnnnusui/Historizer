import React from "react";
import { Schema } from "../Schema";

export const Paragraph: React.FC<Schema.Paragraph> = (props) => {
  const { id, content } = props;
  return <p>{content}</p>;
};
