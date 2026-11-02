# From study notes to a runnable library

The original repository was a collection of 47 Markdown files. It had Java snippets inside notes, but no `.java` source tree, build descriptor, test suite or CI workflow. The original topic paths remain available; no commit history was rewritten and no note directory was moved.

The maintained implementations now live under [`src/main/java/io/github/ichillous/dsa`](../src/main/java/io/github/ichillous/dsa). Their tests live under the matching [`src/test/java/io/github/ichillous/dsa`](../src/test/java/io/github/ichillous/dsa) packages. The [catalog](catalog.md) is the entry point for executable code, and the [roadmap](learning-roadmap.md) provides a study sequence.

## Where to go

| Existing material | Maintained destination |
| --- | --- |
| `data-structures/arrays`, `linked-lists`, `stacks`, `queues`, `hash-tables` | [Collections package](../src/main/java/io/github/ichillous/dsa/collections) and [catalog](catalog.md) |
| `data-structures/trees`, `heaps`, `tries` | [Structures package](../src/main/java/io/github/ichillous/dsa/structures) |
| `data-structures/graphs`, `algorithms/graph` | [Graphs package](../src/main/java/io/github/ichillous/dsa/graphs) |
| `algorithms/searching`, `sorting`, `dynamic-programming`, `greedy`, `backtracking`, `divide-and-conquer` | [Algorithms package](../src/main/java/io/github/ichillous/dsa/algorithms) |
| Both original `complexity-analysis` directories | [Complexity guide](complexity.md) for the maintained code; original explanations remain available |
| `java-fundamentals`, `resources` | Original notes, plus [roadmap](learning-roadmap.md) prerequisites |

## What changed in the notes

Every original topic note carries a banner explaining that its snippets are explanatory fragments outside the build. Some snippets intentionally omit imports, class context, validation or supporting methods. A Markdown example is not covered by CI merely because a similarly named Java class exists. Use the source and tests when running or adapting an implementation.

The audit also corrected these concrete defects:

- **Trie deletion:** a fixed child array always has length 26, so `children.length == 0` never identifies an empty node. Deletion now checks live children, preserves shared prefixes, and separates successful removal from the helper's pruning result. The trie diagram now shares the `ca` prefix and the leaf definition distinguishes leaves from word endings.
- **Duplicate permutations:** `current.contains(value)` prevented repeated input values from occupying separate positions. The example now tracks used indices and skips equivalent choices to return unique permutations.
- **Subset sum:** pruning a negative remaining target is invalid when input values may be negative. The backtracking example now explores both choices without that prune and uses a `long` remaining target.
- **Hash probing:** a full fixed-size table could loop forever on insertion or an absent lookup. Examples now bound probes, reject nonpositive capacities and report exhausted insertion. Double hashing chooses a step coprime to capacity so all buckets are reachable; bucket calculation uses `floorMod`.
- **Quicksort space:** the naive two-recursive-call version can consume O(n) stack space; O(log n) requires balanced partitions or explicit smaller-partition recursion. The notes now state the distinction.
- **Java recursion:** tail-call elimination is not guaranteed. The notes now recommend an explicit loop or stack when depth is a concern.

These are targeted repairs, not a certification of every historical snippet. Advanced note-only topics such as Sudoku, knight's tour and optimal binary search trees remain outside the compiled API. The catalog describes the actual implementation scope.

## Compatibility and behavior

There was no existing compiled public API to preserve. New APIs use the `io.github.ichillous.dsa` namespace and target Java 17. They favor explicit contracts: non-null collection elements, validated indices, documented empty-state behavior, and stated numeric and character models. Method names in old notes, such as `insert` or `delete`, may differ from maintained methods such as `add` or `remove`.

Run `./mvnw clean verify` after updating or adapting code, followed by `python3 scripts/check_docs.py` to check local documentation links. On Windows use `mvnw.cmd clean verify`. A JDK and network access for the first dependency download are required; later builds can reuse Maven's cache. See [contributing](../CONTRIBUTING.md) for how to extend the library without creating another untested snippet.
