import { GetTextsDocument, AddedTextDocument } from "../graphql/generated";
import React, { useEffect } from "react";
import { useQuery } from "@apollo/client";
import { Text } from "./Text";

export const TextList: React.FC = () => {
  const { loading, error, data, subscribeToMore } = useQuery(GetTextsDocument);
  useEffect(() => {
    subscribeToMore({
      document: AddedTextDocument,
      updateQuery: (
        { texts: currents },
        {
          subscriptionData: {
            data: { addedText: latest },
          },
        }
      ) => ({ texts: [...currents, latest] }),
    });
  }, []);
  if (loading) return <p>...loading</p>;
  if (error) return <p>{error.message}</p>;
  const { texts } = data;
  return (
    <>
      {texts.map(({ id }) => (
        <Text key={id} id={id} />
      ))}
    </>
  );
};
