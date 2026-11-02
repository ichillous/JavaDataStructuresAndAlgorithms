# Implementation catalog

Source lives in `io.github.ichillous.dsa`. Each row pairs an implementation with
executable contracts. Run one with `./mvnw -Dtest=ClassNameTest test`; run
`./mvnw clean verify` for the full build. See the [complexity reference](complexity.md)
for bounds and assumptions.

## Collections

All insertions reject null. Empty stack/queue/list-end operations throw
`NoSuchElementException`; invalid indices throw `IndexOutOfBoundsException`.
Growing arrays retain capacity after removals.

| Source | Operations and contract | Tests |
| --- | --- | --- |
| [DynamicArray](../src/main/java/io/github/ichillous/dsa/collections/DynamicArray.java) | Indexed `add/get/set/remove`, `clear`, iteration. Insert permits `index == size`; access does not. | [DynamicArrayTest](../src/test/java/io/github/ichillous/dsa/collections/DynamicArrayTest.java) |
| [DoublyLinkedList](../src/main/java/io/github/ichillous/dsa/collections/DoublyLinkedList.java) | Both-end insertion/removal, indexed `get/set/remove`, iteration. | [DoublyLinkedListTest](../src/test/java/io/github/ichillous/dsa/collections/DoublyLinkedListTest.java) |
| [ArrayStack](../src/main/java/io/github/ichillous/dsa/collections/ArrayStack.java) | `push/pop/peek`; LIFO. | [ArrayStackTest](../src/test/java/io/github/ichillous/dsa/collections/ArrayStackTest.java) |
| [ArrayQueue](../src/main/java/io/github/ichillous/dsa/collections/ArrayQueue.java) | `enqueue/dequeue/peek`; FIFO with circular storage. | [ArrayQueueTest](../src/test/java/io/github/ichillous/dsa/collections/ArrayQueueTest.java) |
| [ChainedHashMap](../src/main/java/io/github/ichillous/dsa/collections/ChainedHashMap.java) | Non-null keys/values; `put/get/remove/containsKey`. `put`/`remove` return the previous value; null means absent. | [ChainedHashMapTest](../src/test/java/io/github/ichillous/dsa/collections/ChainedHashMapTest.java) |
| [RingDeque](../src/main/java/io/github/ichillous/dsa/collections/RingDeque.java) | Both-end add/remove/peek, non-null elements, circular growing buffer; empty operations throw. | [RingDequeTest](../src/test/java/io/github/ichillous/dsa/collections/RingDequeTest.java) |
| [LruCache](../src/main/java/io/github/ichillous/dsa/collections/LruCache.java) | Positive fixed capacity; get and replacement promote to most recent; insertion evicts least recent. Null means absent, null keys/values rejected. | [LruCacheTest](../src/test/java/io/github/ichillous/dsa/collections/LruCacheTest.java) |

Array/list iterators are fail fast for structural changes and do not support
iterator removal. `set` is not structural. Fail fast is a debugging aid, not a
concurrency guarantee. Map keys must keep stable equality and hashes while stored.

## Ordered, prefix, and indexed structures

| Source | Operations and contract | Tests |
| --- | --- | --- |
| [BinarySearchTree](../src/main/java/io/github/ichillous/dsa/structures/BinarySearchTree.java) | Iterative unbalanced ordered set: `add/remove/contains`, `inOrder`, `height`. | [BinarySearchTreeTest](../src/test/java/io/github/ichillous/dsa/structures/BinarySearchTreeTest.java) |
| [AvlTree](../src/main/java/io/github/ichillous/dsa/structures/AvlTree.java) | Ordered set with rotations restoring balance after insertion/deletion. | [AvlTreeTest](../src/test/java/io/github/ichillous/dsa/structures/AvlTreeTest.java) |
| [BinaryMinHeap](../src/main/java/io/github/ichillous/dsa/structures/BinaryMinHeap.java) | Natural-order `add/peek/remove`; duplicates retained; empty operations throw. Collection constructor uses bottom-up heapify. | [BinaryMinHeapTest](../src/test/java/io/github/ichillous/dsa/structures/BinaryMinHeapTest.java) |
| [Trie](../src/main/java/io/github/ichillous/dsa/structures/Trie.java) | `add/remove/contains/startsWith`, `wordsWithPrefix(prefix, limit)`. Case-sensitive UTF-16; empty word allowed; empty prefix always exists. | [TrieTest](../src/test/java/io/github/ichillous/dsa/structures/TrieTest.java) |
| [UnionFind](../src/main/java/io/github/ichillous/dsa/structures/UnionFind.java) | `find/union/connected/componentSize/components`; compression and union by size. `union` reports whether components merged. | [UnionFindTest](../src/test/java/io/github/ichillous/dsa/structures/UnionFindTest.java) |
| [FenwickTree](../src/main/java/io/github/ichillous/dsa/structures/FenwickTree.java) | Defensive `long[]` copy; zero-based point `add`, half-open `prefixSum(endExclusive)` / `rangeSum(from,to)`. Empty ranges sum to zero. | [FenwickTreeTest](../src/test/java/io/github/ichillous/dsa/structures/FenwickTreeTest.java) |
| [SegmentTree](../src/main/java/io/github/ichillous/dsa/structures/SegmentTree.java) | Defensive long-array copy, point set and half-open rangeMin; OptionalLong.empty for empty ranges, including on an empty tree. Long.MAX_VALUE is a valid minimum. | [SegmentTreeTest](../src/test/java/io/github/ichillous/dsa/structures/SegmentTreeTest.java) |

BST/AVL use `compareTo == 0` for equivalence, reject null, and return false on
duplicate insertion or absent deletion. Height counts edges (`-1` when empty).
Keys must keep stable ordering while stored. `inOrder` returns an immutable snapshot.
Trie results follow `String.compareTo`, perform no normalization, and reject
negative limits. Union-find indices are `[0,size)`. Fenwick checks intermediate
signed-long overflow; a failed update leaves the tree unchanged.

## Algorithm utilities

| Source | Operations and contract | Tests |
| --- | --- | --- |
| [SearchAlgorithms](../src/main/java/io/github/ichillous/dsa/algorithms/SearchAlgorithms.java) | Linear/binary search, lower/upper bounds. Binary/bounds require ascending input, unchecked for efficiency. Search returns `-1` when absent; bounds return `[0,n]` insertion positions. | [SearchAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/algorithms/SearchAlgorithmsTest.java) |
| [SortAlgorithms](../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java) | Ascending in-place `int[]` insertion/bubble/merge/quick/heap sort. Merge/insertion/bubble stable; quick/heap unstable. | [SortAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/algorithms/SortAlgorithmsTest.java) |
| [StringAlgorithms](../src/main/java/io/github/ichillous/dsa/algorithms/StringAlgorithms.java) | KMP first/all occurrences, overlaps included. UTF-16 positions; empty pattern matches at every boundary `0..length`. | [StringAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/algorithms/StringAlgorithmsTest.java) |
| [DynamicProgramming](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java) | Minimum coins (`-1` impossible, positive reusable denominations); 0/1 knapsack (`long` result, zero weights/negative values allowed); LCS length, strictly increasing LIS length, Levenshtein distance. | [DynamicProgrammingTest](../src/test/java/io/github/ichillous/dsa/algorithms/DynamicProgrammingTest.java) |
| [GreedyAlgorithms](../src/main/java/io/github/ichillous/dsa/algorithms/GreedyAlgorithms.java) | Maximum-cardinality activity selection. Positive-length half-open `Interval(start,end)`; touching endpoints allowed. | [GreedyAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/algorithms/GreedyAlgorithmsTest.java) |
| [Backtracking](../src/main/java/io/github/ichillous/dsa/algorithms/Backtracking.java) | Unique permutations (at most 9 values), N-queens (n in `0..12`, row-to-column solutions). Empty input has one empty solution; results immutable. | [BacktrackingTest](../src/test/java/io/github/ichillous/dsa/algorithms/BacktrackingTest.java) |
| [SequenceAlgorithms](../src/main/java/io/github/ichillous/dsa/algorithms/SequenceAlgorithms.java) | Optional maximum nonempty subarray (long sum, ties earliest start then end); fixed-window maxima (1<=window<=n); long inversion count, equals not inverted. | [SequenceAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/algorithms/SequenceAlgorithmsTest.java) |
| [BacktrackingPuzzles](../src/main/java/io/github/ichillous/dsa/algorithms/BacktrackingPuzzles.java) | 9x9 Sudoku, validated givens and independent rows, true solves in place, false/invalid leaves input unchanged; signed subset sum returns optional immutable indices, at most 30 inputs, smallest inclusion-mask witness. | [BacktrackingPuzzlesTest](../src/test/java/io/github/ichillous/dsa/algorithms/BacktrackingPuzzlesTest.java) |

All reject null. Sorts mutate arrays, and Sudoku commits only a complete solution.
Other algorithm inputs are preserved. DP arrays scale with
amount/capacity: large numeric values may exhaust memory even with a short input
array. Backtracking bounds inputs because it materializes every answer.

## Graphs

| Source | Operations and contract | Tests |
| --- | --- | --- |
| [Graph](../src/main/java/io/github/ichillous/dsa/graphs/Graph.java) | `directed(n)` / `undirected(n)`, vertices `[0,n)`, fluent `addEdge`, immutable neighbor snapshots. Repeated edges ignored; self-loops allowed. | [GraphTest](../src/test/java/io/github/ichillous/dsa/graphs/GraphTest.java) |
| [WeightedGraph](../src/main/java/io/github/ichillous/dsa/graphs/WeightedGraph.java) | Same vertex model, `long` weights, parallel edges retained, negative weights permitted by the model. | [GraphTest](../src/test/java/io/github/ichillous/dsa/graphs/GraphTest.java) |
| [GraphAlgorithms](../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java) | BFS/iterative DFS, shortest unweighted path, components, topological sort, Dijkstra, Kruskal forest. | [GraphAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/graphs/GraphAlgorithmsTest.java), [WeightedAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/graphs/WeightedAlgorithmsTest.java) |
| [AdvancedGraphAlgorithms](../src/main/java/io/github/ichillous/dsa/graphs/AdvancedGraphAlgorithms.java) | Bellman-Ford, canonical directed SCCs and sorted undirected bridges; iterative graph traversal and immutable results. | [BellmanFordTest](../src/test/java/io/github/ichillous/dsa/graphs/BellmanFordTest.java), [ConnectivityAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/graphs/ConnectivityAlgorithmsTest.java) |

Traversal follows neighbor insertion order. An unreachable path is empty; a
source-to-itself path contains the source. Components/forests require undirected
graphs; topological sort requires directed acyclic graphs and rejects cycles.

Dijkstra rejects all negative edges, including disconnected ones. `distanceTo`
returns `OptionalLong`, distinguishing reachable `Long.MAX_VALUE` from unreachable.
Overflowing detours are ignored; if a reachable vertex's true shortest distance
exceeds signed-long range, it throws `ArithmeticException`. Kruskal supports
negative weights and disconnected graphs; it checks the final forest total with
exact integer arithmetic.

Bellman-Ford accepts signed edges and uses exact internal arithmetic. It rejects
reachable negative cycles before checking final long distances, ignores unreachable
cycles, and represents unreachable vertices with empty values/paths. On an
undirected graph, a reachable negative edge already creates a negative closed walk.
SCC components contain ascending vertices and are ordered by their smallest vertex.
Bridges have canonical ascending endpoints and lexicographic order; self-loops
are not bridges, and Graph ignores parallel unweighted insertions.

Additional independent [signed-path oracle tests](../src/test/java/io/github/ichillous/dsa/review/SignedShortestPathOracleTest.java)
and [shortest-path oracle tests](../src/test/java/io/github/ichillous/dsa/review/WeightedShortestPathOracleTest.java)
use `BigInteger` Floyd–Warshall, and [trie contract tests](../src/test/java/io/github/ichillous/dsa/review/TrieContractOracleTest.java)
cover arbitrary UTF-16 and deep words.

## Working example

[AlgorithmShowcase](../src/main/java/io/github/ichillous/dsa/examples/AlgorithmShowcase.java)
combines core APIs in deterministic examples. [LearningLab](../src/main/java/io/github/ichillous/dsa/examples/LearningLab.java)
adds four terminal lessons; [LearningLabTest](../src/test/java/io/github/ichillous/dsa/examples/LearningLabTest.java)
checks commands, expected states and usage errors. [DocumentedExamplesTest](../src/test/java/io/github/ichillous/dsa/documentation/DocumentedExamplesTest.java)
compiles and executes the Java examples in every Markdown lesson. Run the packaged JAR using the
[README instructions](../README.md). Mutable structures are not thread safe;
these educational APIs do not implement Java Collections Framework interfaces.
