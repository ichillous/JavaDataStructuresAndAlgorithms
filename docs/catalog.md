# Implementation catalog

Source lives in `io.github.ichillous.dsa`. Each row pairs an implementation with
executable contracts. Run one with `./mvnw -Dtest=ClassNameTest test`; run
`./mvnw clean verify` for the full build. See the [complexity reference](complexity.md)
for bounds and assumptions. To start from a problem rather than a class, use the
[solution finder](solution-finder.md), which also covers JDK classes and techniques
not implemented here.

## Collections

All insertions reject null. Empty stack/queue/list-end operations throw
`NoSuchElementException`; invalid indices throw `IndexOutOfBoundsException`.
Growing arrays retain capacity after removals. Capacity doubles when full, capped at
`Integer.MAX_VALUE - 8`; only a requirement beyond that cap throws
`OutOfMemoryError("Required array length too large")`. The cap is hard, unlike `ArrayList`,
which grows by half and treats `Integer.MAX_VALUE - 8` as a soft limit. The cap applies to
the array-backed collections and the `ChainedHashMap` bucket array. `OpenAddressingHashMap` is
stricter: its table never exceeds 2^30 slots, and an insertion that needs more throws
`IllegalStateException`.

| Source | Operations and contract | Tests |
| --- | --- | --- |
| [DynamicArray](../src/main/java/io/github/ichillous/dsa/collections/DynamicArray.java) | Indexed `add/get/set/remove`, `clear`, iteration. Insert permits `index == size`; access does not. | [DynamicArrayTest](../src/test/java/io/github/ichillous/dsa/collections/DynamicArrayTest.java) |
| [DoublyLinkedList](../src/main/java/io/github/ichillous/dsa/collections/DoublyLinkedList.java) | Both-end insertion/removal, indexed `get/set/remove`, iteration. | [DoublyLinkedListTest](../src/test/java/io/github/ichillous/dsa/collections/DoublyLinkedListTest.java) |
| [ArrayStack](../src/main/java/io/github/ichillous/dsa/collections/ArrayStack.java) | `push/pop/peek`; LIFO. | [ArrayStackTest](../src/test/java/io/github/ichillous/dsa/collections/ArrayStackTest.java) |
| [ArrayQueue](../src/main/java/io/github/ichillous/dsa/collections/ArrayQueue.java) | `enqueue/dequeue/peek`; FIFO with circular storage. | [ArrayQueueTest](../src/test/java/io/github/ichillous/dsa/collections/ArrayQueueTest.java) |
| [ChainedHashMap](../src/main/java/io/github/ichillous/dsa/collections/ChainedHashMap.java) | Non-null keys/values; `put/get/remove/containsKey`. `put`/`remove` return the previous value; null means absent. | [ChainedHashMapTest](../src/test/java/io/github/ichillous/dsa/collections/ChainedHashMapTest.java) |
| [OpenAddressingHashMap](../src/main/java/io/github/ichillous/dsa/collections/OpenAddressingHashMap.java) | Same non-null contract as `ChainedHashMap` (`put/get/remove/containsKey`, `size/isEmpty/clear`; null means absent). Linear probing over a power-of-two table (at most 2^30 slots), load at most 2/3, backward-shift deletion with no tombstones. | [OpenAddressingHashMapTest](../src/test/java/io/github/ichillous/dsa/collections/OpenAddressingHashMapTest.java) |
| [RingDeque](../src/main/java/io/github/ichillous/dsa/collections/RingDeque.java) | Both-end add/remove/peek, non-null elements, circular growing buffer; empty operations throw. | [RingDequeTest](../src/test/java/io/github/ichillous/dsa/collections/RingDequeTest.java) |
| [LruCache](../src/main/java/io/github/ichillous/dsa/collections/LruCache.java) | Positive fixed capacity; get and replacement promote to most recent; insertion evicts least recent. Null means absent, null keys/values rejected. | [LruCacheTest](../src/test/java/io/github/ichillous/dsa/collections/LruCacheTest.java) |
| [LfuCache](../src/main/java/io/github/ichillous/dsa/collections/LfuCache.java) | Positive fixed capacity; get and replacement increment a use count; insertion evicts the lowest count, ties to the least recently used. `frequency(key)` is 0 when absent and is not a use. Counts saturate at `Integer.MAX_VALUE`. Null means absent, null keys/values rejected. | [LfuCacheTest](../src/test/java/io/github/ichillous/dsa/collections/LfuCacheTest.java) |

Array/list iterators are fail fast for structural changes and do not support
iterator removal. `set` is not structural. Fail fast is a debugging aid, not a
concurrency guarantee. Map keys must keep stable equality and hashes while stored.

## Ordered, prefix, and indexed structures

| Source | Operations and contract | Tests |
| --- | --- | --- |
| [BinarySearchTree](../src/main/java/io/github/ichillous/dsa/structures/BinarySearchTree.java) | Iterative unbalanced ordered set: `add/remove/contains`, `inOrder`, `height`. | [BinarySearchTreeTest](../src/test/java/io/github/ichillous/dsa/structures/BinarySearchTreeTest.java) |
| [AvlTree](../src/main/java/io/github/ichillous/dsa/structures/AvlTree.java) | Ordered set with rotations restoring balance after insertion/deletion. | [AvlTreeTest](../src/test/java/io/github/ichillous/dsa/structures/AvlTreeTest.java) |
| [RedBlackTreeMap](../src/main/java/io/github/ichillous/dsa/structures/RedBlackTreeMap.java) | Left-leaning red-black ordered map with subtree sizes. `put/get/containsKey/remove` follow `java.util.Map` (null means absent; non-null keys and values; an equivalent key replaces the value and keeps the first key object). `firstKey/lastKey/floorKey/ceilingKey` return `Optional`. `rank(key)` counts keys strictly below a key that need not be present; `select(rank)` throws `IndexOutOfBoundsException` outside `[0,size)`; half-open `countInRange(from,to)` is 0 when `from >= to`. Immutable ascending `keys` snapshot; `height` in edges (`-1` when empty). | [RedBlackTreeMapTest](../src/test/java/io/github/ichillous/dsa/structures/RedBlackTreeMapTest.java) |
| [BinaryMinHeap](../src/main/java/io/github/ichillous/dsa/structures/BinaryMinHeap.java) | Natural-order `add/peek/remove`; duplicates retained; empty operations throw. `add`/`remove` are failure-atomic if `compareTo` throws. Collection constructor uses bottom-up heapify. | [BinaryMinHeapTest](../src/test/java/io/github/ichillous/dsa/structures/BinaryMinHeapTest.java) |
| [Trie](../src/main/java/io/github/ichillous/dsa/structures/Trie.java) | `add/remove/contains/startsWith`, `wordsWithPrefix(prefix, limit)`. Case-sensitive UTF-16; empty word allowed; empty prefix always exists. | [TrieTest](../src/test/java/io/github/ichillous/dsa/structures/TrieTest.java) |
| [UnionFind](../src/main/java/io/github/ichillous/dsa/structures/UnionFind.java) | `find/union/connected/componentSize/components`; compression and union by size. `union` reports whether components merged. | [UnionFindTest](../src/test/java/io/github/ichillous/dsa/structures/UnionFindTest.java) |
| [FenwickTree](../src/main/java/io/github/ichillous/dsa/structures/FenwickTree.java) | Defensive `long[]` copy; zero-based point `add`, half-open `prefixSum(endExclusive)` / `rangeSum(from,to)`. Empty ranges sum to zero. Construction and `add` throw `ArithmeticException` iff some final node sum leaves `long` range (a failed `add` changes nothing); `prefixSum`/`rangeSum` are exact and throw only when the true sum is outside `long`. | [FenwickTreeTest](../src/test/java/io/github/ichillous/dsa/structures/FenwickTreeTest.java) |
| [SegmentTree](../src/main/java/io/github/ichillous/dsa/structures/SegmentTree.java) | Defensive long-array copy, point set and half-open rangeMin; OptionalLong.empty for empty ranges, including on an empty tree. Long.MAX_VALUE is a valid minimum. | [SegmentTreeTest](../src/test/java/io/github/ichillous/dsa/structures/SegmentTreeTest.java) |
| [SparseTable](../src/main/java/io/github/ichillous/dsa/structures/SparseTable.java) | Immutable; defensive `long[]` copy; half-open `rangeMin(from,to)` in O(1) from two overlapping power-of-two blocks (valid because minimum is idempotent). `OptionalLong.empty` for empty ranges, including on an empty table. `Long.MAX_VALUE` is a valid minimum. No updates: build a new table for new data. | [SparseTableTest](../src/test/java/io/github/ichillous/dsa/structures/SparseTableTest.java) |
| [LazySegmentTree](../src/main/java/io/github/ichillous/dsa/structures/LazySegmentTree.java) | Defensive `long[]` copy; half-open `rangeAdd(from,to,delta)` and exact `rangeSum(from,to)` with lazy pending additions. Empty ranges sum to zero; queries never modify the tree. `rangeAdd` throws `ArithmeticException` and changes nothing exactly when an element would leave `long` range; `rangeSum` throws exactly when the true sum does. Sums and pending additions are exact 128-bit values. Construction throws `ArithmeticException` for more than 2^29 elements. | [LazySegmentTreeTest](../src/test/java/io/github/ichillous/dsa/structures/LazySegmentTreeTest.java) |

BST/AVL use `compareTo == 0` for equivalence, reject null, and return false on
duplicate insertion or absent deletion. `RedBlackTreeMap` also uses `compareTo == 0`;
its height is at most 2 log2(n + 1) - 1 edges. Height counts edges (`-1` when empty).
Keys must keep stable ordering while stored. `inOrder` returns an immutable snapshot.
Trie results follow `String.compareTo`, perform no normalization, and reject
negative limits. Union-find indices are `[0,size)`. Fenwick construction and
`add` throw iff some final node sum leaves `long` range, and a failed update leaves
the tree unchanged; queries throw only when the true requested sum overflows `long`.
A sparse table checks bounds like the segment tree and performs no arithmetic. The lazy
segment tree validates bounds before its overflow check, and a rejected range addition
leaves every element unchanged. The two range-sum structures differ in their update
overflow contract: a Fenwick `add` can be rejected because an internal node sum would
overflow even when every element fits (on `{Long.MAX_VALUE, 0}`, `add(1, 1)` throws), while
the lazy segment tree rejects an addition only when an element itself would leave `long` range.

## Probabilistic structures

| Source | Operations and contract | Tests |
| --- | --- | --- |
| [BloomFilter](../src/main/java/io/github/ichillous/dsa/structures/BloomFilter.java) | Fixed-size approximate set: `add`, `mightContain`, `insertions`, `estimatedFalsePositiveRate`; `forExpectedInsertions(n, p)` sizes `m = ceil(-n ln p / (ln 2)^2)`, `k = max(1, round(m/n ln 2))`. Bit count at most 2^30 (larger requests throw `IllegalArgumentException`). The k probes use double hashing with step `1 + floorMod(h2, m - 1)`. No false negatives, no deletion; elements with equal `hashCode` are indistinguishable; null rejected. | [BloomFilterTest](../src/test/java/io/github/ichillous/dsa/structures/BloomFilterTest.java) |
| [CountMinSketch](../src/main/java/io/github/ichillous/dsa/structures/CountMinSketch.java) | Approximate frequency table: `add(item[, count])`, `estimate`, `totalCount`; `withErrorBounds(epsilon, delta)` gives `width = ceil(e/epsilon)`, `depth = ceil(ln(1/delta))`. `width * depth` at most 2^25 counters (larger requests throw `IllegalArgumentException`). Estimates never undercount; overflow throws `ArithmeticException` and leaves the sketch unchanged; negative counts rejected. | [CountMinSketchTest](../src/test/java/io/github/ichillous/dsa/structures/CountMinSketchTest.java) |

Both structures have **one-sided error**. A Bloom filter may report an absent element as present
but never an added one as absent; a Count-Min estimate may exceed the true count but is never
below it. Neither error is a bug: size the structure from the target error rate, and verify
behavior with seeded statistical tests, never exact counts. Hashing depends on `hashCode`, so
equal hash codes cannot be told apart. Both reject oversized tables before allocating.

## Algorithm utilities

| Source | Operations and contract | Tests |
| --- | --- | --- |
| [SearchAlgorithms](../src/main/java/io/github/ichillous/dsa/algorithms/SearchAlgorithms.java) | Linear/binary search, lower/upper bounds. Binary/bounds require ascending input, unchecked for efficiency. Search returns `-1` when absent; bounds return `[0,n]` insertion positions. | [SearchAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/algorithms/SearchAlgorithmsTest.java) |
| [SortAlgorithms](../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java) | Ascending in-place `int[]` insertion/bubble/merge/quick/heap sort. Merge/insertion/bubble stable; quick/heap unstable. `countingSort` (range at most `COUNTING_SORT_MAX_RANGE` = 2^20, else `IllegalArgumentException` with the array unchanged), `radixSort` (LSD, 4 passes of 8 bits, full `int` range) and `mergeSort(T[], Comparator)` (in place, stable, null array/comparator/element rejected before any move). | [SortAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/algorithms/SortAlgorithmsTest.java) |
| [DynamicProgramming](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java) | Minimum coins (`-1` impossible, positive reusable denominations); 0/1 knapsack (`long` result, zero weights/negative values allowed); LCS length, strictly increasing LIS length, Levenshtein distance. Coin amounts and knapsack capacities whose table would exceed `Integer.MAX_VALUE - 8` entries throw `IllegalArgumentException`; smaller large values can still exhaust memory. Reconstruction: `longestCommonSubsequence` (string), `editScript` (`EditStep` list of `EditOperation` MATCH/SUBSTITUTE/INSERT/DELETE with cursor indices; non-MATCH count equals `editDistance`), `knapsack01Items` (ascending indices, bitset rows), `minimumCoinsChoice` (`Optional` ascending coin list; empty when impossible, `Optional.of(List.of())` for 0) and `longestIncreasingSubsequenceIndices` (patience sorting with lower bound). Each follows a documented deterministic tie rule; the same table-size guard applies. | [DynamicProgrammingTest](../src/test/java/io/github/ichillous/dsa/algorithms/DynamicProgrammingTest.java) |
| [GreedyAlgorithms](../src/main/java/io/github/ichillous/dsa/algorithms/GreedyAlgorithms.java) | Maximum-cardinality activity selection. Positive-length half-open `Interval(start,end)`; touching endpoints allowed. Huffman code lengths (`long` frequencies, zero frequency gives length 0, a single used symbol gives 1, leaves-first tie rule, checked merge sums) and canonical prefix codes (Kraft-checked, `""` for unused). | [GreedyAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/algorithms/GreedyAlgorithmsTest.java) |
| [Backtracking](../src/main/java/io/github/ichillous/dsa/algorithms/Backtracking.java) | Unique permutations (at most 9 values), N-queens (n in `0..12`, row-to-column solutions). Empty input has one empty solution; results immutable. | [BacktrackingTest](../src/test/java/io/github/ichillous/dsa/algorithms/BacktrackingTest.java) |
| [SequenceAlgorithms](../src/main/java/io/github/ichillous/dsa/algorithms/SequenceAlgorithms.java) | Optional maximum nonempty subarray (long sum, ties earliest start then end); fixed-window maxima (1<=window<=n); long inversion count, equals not inverted. | [SequenceAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/algorithms/SequenceAlgorithmsTest.java) |
| [BacktrackingPuzzles](../src/main/java/io/github/ichillous/dsa/algorithms/BacktrackingPuzzles.java) | 9x9 Sudoku, validated givens and independent rows, true solves in place, false/invalid leaves input unchanged; signed subset sum returns optional immutable indices, at most 30 inputs, smallest inclusion-mask witness. | [BacktrackingPuzzlesTest](../src/test/java/io/github/ichillous/dsa/algorithms/BacktrackingPuzzlesTest.java) |
| [Selection](../src/main/java/io/github/ichillous/dsa/algorithms/Selection.java) | Zero-based k-th smallest `int` by introselect (copy; duplicates count separately; bad `k` throws `IndexOutOfBoundsException`); stable `smallestK` with a bounded heap (`k < 0` throws, `k >= n` returns all sorted); heap-based `mergeSorted` (ties by list index, unsorted list throws `IllegalArgumentException`); Boyer-Moore `majorityElement` (`OptionalInt`, count strictly above n/2). Collection elements must be non-null; results are immutable. | [SelectionTest](../src/test/java/io/github/ichillous/dsa/algorithms/SelectionTest.java) |
| [ArrayPatterns](../src/main/java/io/github/ichillous/dsa/algorithms/ArrayPatterns.java) | Two-pointer pair with sum (ascending input unchecked; smallest first index, then its largest partner); longest repeat-free UTF-16 window as a half-open `StringAlgorithms.Span` (earliest tie; `""` gives `Span(0,0)`); next strictly greater indices; histogram area (`long`, heights at least 0); difference-array range adds (exact `long` results, 128-bit prefix, `ArithmeticException` on a final overflow); interval merge (touching merged) and minimum meeting rooms (touching compatible), reusing `GreedyAlgorithms.Interval`. | [ArrayPatternsTest](../src/test/java/io/github/ichillous/dsa/algorithms/ArrayPatternsTest.java) |
| [DynamicProgrammingClassics](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgrammingClassics.java) | Coin combinations (unordered, duplicates collapse, checked `long`); word break (shortest first word, recursively); longest palindromic subsequence length (UTF-16 units, O(n) space); matrix-chain order (`ChainOrder`, `"((A1A2)A3)"`, smallest-split ties; checked, with overflowing candidates losing, so `ArithmeticException` only when the optimal cost exceeds `long`); Held-Karp travelling salesman (`Tour`, 1 to 16 cities, entries in [-2^47, 2^47]); weighted interval scheduling (`Schedule`, half-open, checked total). Coin combinations apply the same `amount + 1 > Integer.MAX_VALUE - 8` table guard as `DynamicProgramming`. | [DynamicProgrammingClassicsTest](../src/test/java/io/github/ichillous/dsa/algorithms/DynamicProgrammingClassicsTest.java) |

All reject null. Sorts (including counting, radix and Comparator merge sort) and `Sampling.shuffle` mutate
arrays, and Sudoku commits only a complete solution. Other algorithm inputs are preserved. DP
arrays scale with amount/capacity: large numeric values may exhaust memory even with a short
input array. Amounts and capacities whose table would exceed `Integer.MAX_VALUE - 8` entries
are rejected with `IllegalArgumentException` in `DynamicProgramming`, `DynamicProgrammingClassics`
and the reconstruction methods; smaller tables can still run out of memory. Backtracking bounds
inputs because it materializes every answer; Held-Karp is limited to 16 cities.

`Selection.kthSmallest` is worst-case linear; `smallestK` and `mergeSorted` are stable with
documented tie rules. Equal-key behavior is part of each contract, not an accident of the algorithm.

## Strings and text indexes

| Source | Operations and contract | Tests |
| --- | --- | --- |
| [StringAlgorithms](../src/main/java/io/github/ichillous/dsa/algorithms/StringAlgorithms.java) | KMP, Z-function and verified Rabin-Karp all-occurrence search (overlaps included; identical results), Z-array (`z[0] = length`) and Manacher longest palindromic substring as a half-open `Span` (earliest start on ties; `""` gives `Span(0,0)`). UTF-16 positions; empty pattern matches at every boundary `0..length`. | [StringAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/algorithms/StringAlgorithmsTest.java) |
| [AhoCorasick](../src/main/java/io/github/ichillous/dsa/algorithms/AhoCorasick.java) | Multi-pattern matching automaton (BFS failure links, output links). Copies patterns; duplicates collapse; an empty pattern collection is allowed (it never matches), but an individual empty pattern is rejected. `findAll` returns overlapping `Match(start, pattern)` ordered by start then pattern length. UTF-16 positions. | [AhoCorasickTest](../src/test/java/io/github/ichillous/dsa/algorithms/AhoCorasickTest.java) |
| [SuffixArray](../src/main/java/io/github/ichillous/dsa/algorithms/SuffixArray.java) | Suffix array by prefix doubling with radix sort, plus Kasai LCP (`lcp[0] = 0`). `occurrences` ascending (empty pattern gives `0..n`), `distinctSubstrings` (long), `longestRepeatedSubstring` (ties: earliest in suffix order, `""` if none). Copies input and returned arrays. | [SuffixArrayTest](../src/test/java/io/github/ichillous/dsa/algorithms/SuffixArrayTest.java) |

All string algorithms index UTF-16 code units and reject null. Rabin-Karp verifies every hash
hit, so its answers are exact; its running time depends on how many hits it must verify:
O(t + p + k·p) for k reported matches when spurious hits are rare, and O(t·p) in the worst case,
which many genuine matches or input crafted against the fixed base 257 and modulus 2^31 - 1 can
force. Nothing is randomized. Aho-Corasick and suffix-array results are immutable or copied.

## Number theory and sampling

| Source | Operations and contract | Tests |
| --- | --- | --- |
| [NumberTheory](../src/main/java/io/github/ichillous/dsa/algorithms/NumberTheory.java) | Exact-or-throw `long` arithmetic: `gcd` (nonnegative, `gcd(0,0)=0`, throws when the result is 2^63), checked `lcm`, `extendedGcd` (`Bezout` record, `a*x + b*y = gcd`), `modPow` and `modInverse` (empty when `gcd != 1`) exact for every modulus up to `Long.MAX_VALUE`, `primesUpTo` (sieve, limit at most 50,000,000), deterministic Miller-Rabin `isPrime` correct for every `long`, `fibonacciMod` by 2x2 matrix power. | [NumberTheoryTest](../src/test/java/io/github/ichillous/dsa/algorithms/NumberTheoryTest.java) |
| [Sampling](../src/main/java/io/github/ichillous/dsa/algorithms/Sampling.java) | `reservoirSample(items, k, random)` (Algorithm R, uniform `min(k, n)` items, `k = 0` reads nothing, `n <= k` keeps encounter order, immutable result) and in-place Fisher-Yates `shuffle(int[], random)`. Randomness comes from the caller's `RandomGenerator`; bounded draws use `nextInt(bound)`. | [SamplingTest](../src/test/java/io/github/ichillous/dsa/algorithms/SamplingTest.java) |
| [AliasTable](../src/main/java/io/github/ichillous/dsa/algorithms/AliasTable.java) | Vose's alias method: `new AliasTable(weights)` copies finite non-negative weights with a positive, finite sum, `sample(random)` draws an index in O(1), `probability(i)` is the normalized weight, `size()`. Zero-weight indices are never drawn; rounding leftovers get cutoff 1. | [SamplingTest](../src/test/java/io/github/ichillous/dsa/algorithms/SamplingTest.java) |

Randomized APIs never create their own generator: the caller passes a `java.util.random.RandomGenerator`,
so a seeded generator gives reproducible results and tests can fix seeds. Statistical tests compare
frequencies within a tolerance and never assert exact counts. Number-theory methods are exact for
every `long` or throw `ArithmeticException`; they never wrap.

## Graphs

| Source | Operations and contract | Tests |
| --- | --- | --- |
| [Graph](../src/main/java/io/github/ichillous/dsa/graphs/Graph.java) | `directed(n)` / `undirected(n)`, vertices `[0,n)`, fluent `addEdge`, immutable neighbor snapshots. Repeated edges ignored; self-loops allowed. | [GraphTest](../src/test/java/io/github/ichillous/dsa/graphs/GraphTest.java) |
| [WeightedGraph](../src/main/java/io/github/ichillous/dsa/graphs/WeightedGraph.java) | Same vertex model, `long` weights, parallel edges retained, negative weights permitted by the model. | [GraphTest](../src/test/java/io/github/ichillous/dsa/graphs/GraphTest.java) |
| [GraphAlgorithms](../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java) | BFS/iterative DFS, shortest unweighted path, components, topological sort, Dijkstra, Kruskal forest. | [GraphAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/graphs/GraphAlgorithmsTest.java), [WeightedAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/graphs/WeightedAlgorithmsTest.java) |
| [AdvancedGraphAlgorithms](../src/main/java/io/github/ichillous/dsa/graphs/AdvancedGraphAlgorithms.java) | Bellman-Ford, canonical directed SCCs and sorted undirected bridges; iterative graph traversal and immutable results. | [BellmanFordTest](../src/test/java/io/github/ichillous/dsa/graphs/BellmanFordTest.java), [ConnectivityAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/graphs/ConnectivityAlgorithmsTest.java) |
| [ShortestPathAlgorithms](../src/main/java/io/github/ichillous/dsa/graphs/ShortestPathAlgorithms.java) | 0-1 BFS, DAG shortest paths with signed weights, A* with a heuristic checked for nonnegativity, zero at the target and consistency on scanned edges, and Floyd-Warshall all pairs with path reconstruction. Precondition violations throw `IllegalArgumentException` (weights outside 0/1, cycles, undirected DAG input, negative edges for A*, any negative cycle in Floyd-Warshall); DAG and Floyd-Warshall use exact arithmetic and throw `ArithmeticException` only if a final shortest distance is outside `long`. A* follows Dijkstra's policy: overflowing detours are ignored, and it throws `ArithmeticException` only when the target is reachable but every route exceeds `long`. Empty `OptionalLong`/path for unreachable. | [ShortestPathAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/graphs/ShortestPathAlgorithmsTest.java) |
| [GraphStructureAlgorithms](../src/main/java/io/github/ichillous/dsa/graphs/GraphStructureAlgorithms.java) | Iterative articulation points, BFS bipartition with odd-cycle witness, directed-cycle witness, Hierholzer Eulerian path (directed or undirected; an undirected self-loop is one edge adding 2 to degree), Prim minimum spanning forest returning `GraphAlgorithms.SpanningForest`. | [GraphStructureAlgorithmsTest](../src/test/java/io/github/ichillous/dsa/graphs/GraphStructureAlgorithmsTest.java) |
| [FlowNetwork](../src/main/java/io/github/ichillous/dsa/graphs/FlowNetwork.java) | Directed `long`-capacity network, parallel edges kept; Dinic `maxFlow(s, t)` with an iterative blocking flow returns the value, per-edge flows in insertion order and the minimal source side of a minimum cut. `source == sink` rejected; network not mutated; total uses `Math.addExact`. | [FlowNetworkTest](../src/test/java/io/github/ichillous/dsa/graphs/FlowNetworkTest.java) |
| [BipartiteMatching](../src/main/java/io/github/ichillous/dsa/graphs/BipartiteMatching.java) | Hopcroft-Karp `maximumMatching(leftSize, rightSize, edges)`: duplicate edges ignored, out-of-range vertices throw `IndexOutOfBoundsException`, result sorted by left vertex, iterative augmentation. | [BipartiteMatchingTest](../src/test/java/io/github/ichillous/dsa/graphs/BipartiteMatchingTest.java) |
| [LowestCommonAncestor](../src/main/java/io/github/ichillous/dsa/graphs/LowestCommonAncestor.java) | Binary-lifting `lca`, `depth`, `distance` and `kthAncestor` (empty above the root; negative `k` rejected) on an undirected tree given as a `Graph` and a root. Non-trees (cycle, self-loop, disconnected, directed, empty) throw `IllegalArgumentException`; a bad root throws `IndexOutOfBoundsException`. Iterative build; the graph is not retained. | [TreeQueriesTest](../src/test/java/io/github/ichillous/dsa/graphs/TreeQueriesTest.java) |
| [TreeAlgorithms](../src/main/java/io/github/ichillous/dsa/graphs/TreeAlgorithms.java) | `diameterPath(tree)` by double BFS; ties go to the smallest farthest vertex in each sweep, path runs from first to second endpoint, immutable result. One vertex gives `[0]`; empty or non-tree graphs throw `IllegalArgumentException`. | [TreeQueriesTest](../src/test/java/io/github/ichillous/dsa/graphs/TreeQueriesTest.java) |

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

`ShortestPathAlgorithms` states its preconditions per method: 0-1 BFS accepts only weights 0 and 1, the
DAG method needs a directed acyclic graph, A* needs nonnegative weights and a consistent heuristic
(only edges it scans are checked, so a bad heuristic on unexplored edges is not detected), and
Floyd-Warshall rejects every negative cycle in the graph, including one that the Bellman–Ford
source of interest cannot reach. `GraphStructureAlgorithms` can certify its negative answers where
a short certificate exists: when `bipartition` is empty, `oddCycle` returns an odd cycle, and
`directedCycle` returns a cycle for a cyclic digraph. `eulerianPath` returns an empty `Optional`,
without a witness, when the degree or connectivity conditions fail. `FlowNetwork` keeps
parallel edges and never mutates the network; the returned minimum cut is the minimal source
side. `LowestCommonAncestor` and `TreeAlgorithms` accept only a valid undirected tree and throw `IllegalArgumentException` otherwise.
All of these use iterative traversal, so a path of 100,000 vertices does not overflow the call stack.

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
