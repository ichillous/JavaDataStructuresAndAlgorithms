# A practical learning roadmap

Start with the [quick start](../README.md), run the tests, and keep the [implementation catalog](catalog.md) open. The goal is to explain why an implementation works, change it safely, and choose it for the right problem. Move to the next stage when you can complete its checkpoint without copying the solution.

Each session follows the same loop: predict an example by hand, read the implementation, run its tests, write one counterexample, then explain its cost using the [complexity guide](complexity.md). Time estimates depend on your Java background; these stages are checkpoints rather than a deadline.

| Stage | Study and run | Checkpoint |
| --- | --- | --- |
| 1. Java and contracts | [Java prerequisites](../java-fundamentals/core-java-concepts.md), generic types, `equals`/`hashCode`, `Comparable`, exceptions, arrays, JUnit assertions | Explain why a key used in a hash table should not change its equality or hash code while stored. Distinguish an empty result from an invalid argument. |
| 2. Linear storage | `DynamicArray`, `DoublyLinkedList`, `ArrayStack`, `ArrayQueue` in the [collections package](../src/main/java/io/github/ichillous/dsa/collections) | Draw a queue after wraparound and after growth. Explain amortized append versus indexed insertion. Add a test for clearing and reusing a collection. |
| 3. Searching and sorting | `SearchAlgorithms`, `SortAlgorithms` in the [algorithms package](../src/main/java/io/github/ichillous/dsa/algorithms) | Derive a half-open binary-search invariant, then find the entire run of a duplicate value using lower and upper bounds. Explain quicksort's time and stack bounds separately. |
| 4. Lookup and order | `ChainedHashMap`, `BinarySearchTree`, `AvlTree`, `BinaryMinHeap`, `Trie` in the [structures catalog](catalog.md) | Delete a tree node with two children; trace an AVL rotation; remove a trie word without deleting a longer word sharing its prefix. Choose between hash lookup, sorted traversal, priority removal and prefix lookup. |
| 5. Graphs and connectivity | `Graph`, `WeightedGraph`, `GraphAlgorithms`, `UnionFind`, and `FenwickTree` in the [graph catalog](catalog.md) | Reconstruct a BFS shortest path, reject a cyclic topological-order request, and explain why Dijkstra rejects negative weights. Explain why a disconnected graph has a spanning forest rather than one spanning tree. Trace a Fenwick update and a half-open range query. |
| 6. Problem-solving patterns | `DynamicProgramming`, `GreedyAlgorithms`, `Backtracking`, `StringAlgorithms` in the [algorithms package](../src/main/java/io/github/ichillous/dsa/algorithms) | Write the state and recurrence before coding DP. Give a counterexample to a plausible greedy rule. Generate unique permutations of repeated values. Find overlapping string matches. |
| 7. Independent implementation | Choose three [exercises](exercises.md), hide the source, and keep the public contract | Compare against a JDK or brute-force model, explain a failing seed, and submit a change that passes the complete build. |

## How to read the tests

The [test tree](../src/test/java/io/github/ichillous/dsa) mirrors the implementation packages. Start with small examples and exception tests; then inspect seeded randomized tests. A model test runs the same operations against a small independent reference, such as `ArrayList`, `TreeSet`, `PriorityQueue` or brute-force enumeration. The reference should not reuse the implementation's internal algorithm.

When a model test fails, preserve the seed and reduce the operation sequence until the smallest failing example is clear. A passing randomized run is useful evidence, not a proof for all inputs. Do not increase iteration counts to hide a failure or remove a difficult input to make the suite green.

## Choose a capstone

- **Autocomplete index:** store words in `Trie`, add and delete entries, and return bounded suggestions. Test shared prefixes, the empty word, case differences and the documented character model.
- **Dependency planner:** represent prerequisites with a directed `Graph`, return a topological order, and report a cycle. Check disconnected components and isolated tasks.
- **Route explorer:** compare BFS on an unweighted graph with Dijkstra on a weighted graph. Include an unreachable destination and multiple candidate paths.
- **Collection laboratory:** replay a saved operation sequence against a custom collection and a JDK model; show the first mismatch and explain the invariant that broke.

Keep capstones separate from the core APIs until their behavior and tests are clear. Use JDK collections in application code unless implementing a data structure is itself the learning objective.
