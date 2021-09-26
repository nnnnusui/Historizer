import React from "react";

const Editor = (target: HTMLElement) => {
  const button = (name: string, action: () => void) => {
    const element = document.createElement("button");
    element.textContent = name;
    element.classList.add(name);
    element.addEventListener("click", action);
    return element;
  };
  const before = (() => {
    const element = document.createElement("div");
    element.classList.add("before");
    element.innerHTML = target.innerHTML;
    return element;
  })();
  const textArea = (() => {
    const container = document.createElement("div");
    container.classList.add("after");
    const dummy = document.createElement("div");
    dummy.classList.add("dummy");
    const textArea = document.createElement("textarea");

    textArea.value = target.innerHTML;
    dummy.innerHTML = textArea.value;
    textArea.addEventListener("input", (event) => {
      const target = event.target as HTMLTextAreaElement;
      dummy.innerHTML = target.value + "\u200b";
    });
    container.append(dummy, textArea);
    return { element: container, text: () => textArea.value };
  })();
  return (() => {
    const element = document.createElement("div");
    element.classList.add("editor");
    element.append(
      before,
      textArea.element,
      button("apply", () => (target.innerHTML = textArea.text())),
      button("cancel", () => (target.innerHTML = before.innerHTML))
    );
    return element;
  })();
};

function parents(target: HTMLElement) {
  var parents: HTMLElement[] = [];
  var element = target;
  while (element.parentElement) {
    element = element.parentElement;
    parents.push(element);
  }
  return parents;
}

export const Editable: React.FC = (props) => {
  const onClick = (event: React.MouseEvent<HTMLElement>) => {
    const target = event.target as HTMLElement;
    console.log(document.getSelection());
    // if (
    //   parents(target).find((it) => it.classList.contains("editor")) ||
    //   target.children.length != 0 ||
    //   target.childNodes.length != 1
    // ) {
    //   console.log("canceled.");
    //   return;
    // }
    // const editor = Editor(target);
    // target.firstChild.replaceWith(editor);
  };
  return <div onClick={onClick}>{props.children}</div>;
};
