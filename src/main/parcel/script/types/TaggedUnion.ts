export type TaggedUnion<KeyValueStore> = {
  [Key in keyof KeyValueStore]: { kind: Key; value: KeyValueStore[Key] };
}[keyof KeyValueStore];
