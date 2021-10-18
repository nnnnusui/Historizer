import React, { useEffect, useRef } from "react";

export const Editor: React.FC<{
  tagName: React.ElementType;
  onApply: (text: string) => void;
}> = ({ tagName: Tag, onApply }) => {
  const ref = useRef<HTMLElement>();
  useEffect(() => ref.current.focus(), []);
  const onKeyPress = (event: React.KeyboardEvent<HTMLElement>) => {
    if (event.key !== "Enter") return;
    event.preventDefault();
    onApply(event.currentTarget.textContent);
  };
  return <Tag contentEditable onKeyPress={onKeyPress} ref={ref}></Tag>;
};
