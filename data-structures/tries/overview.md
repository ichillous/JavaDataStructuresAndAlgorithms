# Tries: share prefixes, mark whole words separately

A trie stores strings along labeled edges. Words with the same prefix share a path, making prefix lookup and autocomplete natural. Use a hash set for exact membership alone; consider a trie when prefixes are the central operation.

```text
root
|-- c
|   `-- a
|       |-- r* -- t*
|       `-- t*
`-- d -- o -- g*

* marks a complete word: car, cart, cat, dog
```

A word can end at an internal node: `car` ends before `cart` does. A leaf is a node with no children; it is not the definition of a word ending. The empty word, if stored, marks the root itself.

[`Trie`](../../src/main/java/io/github/ichillous/dsa/structures/Trie.java) is a case-sensitive set of Java strings. It accepts the empty string and arbitrary UTF-16 code units, rejects nulls and does not normalize case or Unicode. Duplicate add returns false.

```java
import io.github.ichillous.dsa.structures.Trie;
import java.util.List;

Trie words = new Trie();
words.add("car");
words.add("cart");
words.add("cat");
assert words.startsWith("ca");
assert !words.contains("ca");
assert words.contains("car");
assert words.wordsWithPrefix("ca", 2).equals(List.of("car", "cart"));
```

## The lookup invariant

After consuming the first i code units of a query, the current node represents exactly that prefix. A missing edge proves the query cannot be present. Reaching the final node proves prefix existence; exact membership additionally requires its word-ending flag.

The empty prefix always has a path: the root. Consequently, `startsWith("")` is true even in an empty trie, while `contains("")` becomes true only after adding the empty word.

```java
import io.github.ichillous.dsa.structures.Trie;

Trie words = new Trie();
assert words.startsWith("") && !words.contains("");
words.add("");
words.add("Car");
words.add("car");
assert words.contains("");
assert words.size() == 3;
assert !words.contains("CAR");
```

## Costs depend on child storage

This implementation uses ordered `TreeMap` children, so lookup, insertion and removal cost O(L log A), where L is query length in UTF-16 code units and A bounds child count. It does not claim O(L) without the child-map assumption. Nodes and edge maps use space proportional to distinct stored prefixes; removal uses O(L) path workspace.

Suggestions follow `String.compareTo` order and return an immutable snapshot with an explicit result limit. Traversal costs prefix lookup plus visited nodes and output characters. A result limit bounds the number of words, not their lengths or the depth of a long shared branch.

## Practice

1. Add `can` to the diagram and identify exactly which nodes are new.
2. Explain why prefix membership and whole-word membership have different answers for `ca`.
3. Compare random add/remove/contains operations with `HashSet`, and suggestions with a sorted filtered set under the same UTF-16/case policy.

Continue with [deletion and suggestions](implementation.md) and the [worked solutions](../../docs/solutions.md).
