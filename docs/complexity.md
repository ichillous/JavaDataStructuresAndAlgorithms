# Complexity for the maintained implementations

These bounds describe the code in the [catalog](catalog.md), rather than every historical Markdown snippet. They use a RAM model: primitive arithmetic, array access and references cost O(1); user-supplied `compareTo`, `equals` and `hashCode` are assumed O(1). If a key comparison scans a long string, include that cost as well. Bounds describe growth, not a promise about milliseconds or allocation size on a particular JVM.

`C` is retained array capacity (which may exceed current size after removals), `n` is the number of stored elements or input values, `h` is tree height, `V` is the number of vertices, and `E` is the number of graph edges, counting parallel weighted edges. Space means additional working storage unless the table says stored space or includes output. Logarithmic bounds conventionally mean `log(n + 1)` for empty and singleton inputs.

## Data structures

| Implementation / operation | Time | Space and assumptions |
| --- | --- | --- |
| `DynamicArray` get/set | O(1) worst | O(C) stored capacity; resizing uses temporary O(n) storage |
| `DynamicArray` append | O(1) amortized; O(n) for a resize | Geometric capacity growth spreads copying over a sequence of appends |
| `DynamicArray` indexed insertion/removal | O(n) worst | Shifting dominates; an insertion may also grow capacity |
| `DoublyLinkedList` add/remove at either end | O(1) worst | O(n) stored nodes; this implementation has both head and tail |
| `DoublyLinkedList` get/set/remove by index | O(n) worst | O(1 + min(index, n - 1 - index)) to find the node; local unlinking is O(1) |
| `ArrayStack` push | O(1) amortized; O(n) on growth | O(C) stored array; pop/peek are O(1) |
| `ArrayQueue` enqueue | O(1) amortized; O(n) on growth | O(C) stored circular buffer; dequeue/peek are O(1); no shifting on dequeue |
| `ChainedHashMap` get/contains/remove | O(1) expected; O(n) worst | O(n + capacity) stored space; good hash distribution and bounded load are required for expected O(1) |
| `ChainedHashMap` put | O(1) expected amortized; O(n) worst | Includes occasional rehashing; equal hashes can still produce a linear chain |
| `BinarySearchTree` add/remove/contains | O(h), up to O(n) worst | O(n) stored nodes; sorted insertions can produce a chain; the maintained traversal/update code uses iteration |
| `AvlTree` add/remove/contains | O(log n) worst | O(n) stored nodes; recursive updates use O(log n) stack; rotations restore balance |
| Tree inorder traversal | O(n) | O(n) result snapshot plus traversal workspace |
| `BinaryMinHeap` add/remove minimum | O(log n) amortized; add can be O(n) on array growth | O(C) retained array space; peek O(1), bottom-up construction from a collection O(n) |
| `Trie` add/contains/startsWith/remove | O(L log A) | `L` UTF-16 code units, at most `A` children per node, ordered `TreeMap` children; removal keeps O(L) path workspace |
| `Trie` prefix enumeration | O(P log A + visited nodes + output characters) | Prefix length `P`; result limit bounds returned words, not the length of those words; includes output storage and iterative traversal workspace |
| `UnionFind` find/union/connected/componentSize | O(α(n)) amortized | Path compression plus union by size; O(n) stored arrays; construction O(n); an individual find is not guaranteed O(1) |
| `FenwickTree` point add, prefix/range sum | O(log n) worst | O(n) stored sums; construction O(n), query/update O(1) additional storage |

A linked-list operation is O(1) only after the relevant node is known, or at an end for which the structure keeps a pointer. Finding an element by value or position is a separate cost. Likewise, an unbalanced BST is not automatically logarithmic because it is a tree.

## Searching, sorting and strings

| Algorithm | Time | Auxiliary space / behavior |
| --- | --- | --- |
| Linear search | O(n) worst; O(1) best | O(1); returns the first matching index or `-1` |
| Binary search, lower/upper bound | O(log n) worst | O(1); ascending input is an unchecked precondition |
| Insertion sort | O(n²) worst; O(n) already sorted | O(1); stable |
| Bubble sort with early exit | O(n²) worst; O(n) already sorted | O(1); stable |
| Merge sort | O(n log n) worst | O(n) temporary buffer plus O(log n) call stack; stable |
| Three-way quicksort | O(n log n) on typical balanced partitions; O(n²) worst | O(log n) stack in this implementation, because only the smaller partition is recursive; unstable |
| Heap sort | O(n log n) worst | O(1); unstable; heap construction itself is O(n) |
| KMP first match | O(t + p) | O(p) prefix table for text length `t`, pattern length `p` |
| KMP all matches | O(t + p + k) | O(p + k), including `k` returned indices; overlaps are included |

The sorts mutate their input arrays. Stability describes the treatment of equal keys in the algorithm; primitive `int` values do not carry an identity that lets a test observe equal-key reordering. Three-way partitioning handles duplicate-heavy inputs well, but the deterministic pivot strategy does not provide a worst-case time guarantee. The historical quicksort note shows a different, naive recursion strategy and states its separate bounds.

Strings and trie paths use UTF-16 code units. A supplementary Unicode character occupies two units; a visible grapheme may occupy several. The APIs do not normalize Unicode or ignore case. KMP's empty pattern matches at zero for first-match search and at every boundary `0..text.length()` for all-match search.

## Graphs

| Algorithm / representation | Time | Auxiliary or stored space |
| --- | --- | --- |
| Adjacency representation | Add unweighted edge O(1) expected; add weighted edge O(1) amortized | O(V + E) stored; neighbor snapshots cost O(out-degree) time and space |
| BFS, iterative DFS, unweighted shortest path | O(V + E) | O(V + E), including adjacency snapshots; only the reachable part is visited, while state arrays include all vertices |
| Connected components | O(V + E) | O(V + E), including result; undirected graphs only |
| Kahn topological ordering | O(V + E) | O(V + E); directed graphs only; rejects a cycle |
| Dijkstra with a lazy binary heap | O(V + E log(E + 1)) | O(V + E); weighted multigraph can have more heap entries than vertices |
| Kruskal minimum spanning forest | O(V + E log(E + 1)) | O(V + E), including sorted edge copy and returned forest |

Unweighted graphs ignore duplicate edges and preserve neighbor insertion order. Weighted graphs keep parallel edges and accept signed `long` weights; individual algorithms impose extra requirements. BFS minimizes the number of edges, not arbitrary total weight. Dijkstra rejects every negative edge, including one in an unreachable component. A reachable distance of `Long.MAX_VALUE` is valid; unreachable results use `OptionalLong.empty()`. Overflowing detours are ignored, but an unrepresentable reachable shortest distance causes `ArithmeticException`.

Kruskal accepts negative edges, omits loops, and returns a forest for disconnected graphs. The result has `V - components` selected edges. The total is accumulated exactly and must fit in a `long` at the end; intermediate cancellation does not cause spurious overflow. These implementations return immutable result snapshots and use no recursive graph traversal.

## Dynamic programming, greedy choice and enumeration

| Algorithm | Time | Auxiliary space / parameters |
| --- | --- | --- |
| Minimum coins | O((c + 1)(a + 1)) | O(a + 1), for `c` denominations and amount `a`; includes validation and table allocation; unlimited reuse of positive coins |
| 0/1 knapsack | O((n + 1)(W + 1)) | O(W + 1), for `n` items and capacity `W`; each item may be used at most once, including a zero-weight item |
| LCS length | O(mn + m + n) | O(min(m, n) + 1); lengths are UTF-16 units; empty-input handling and setup are included |
| Edit distance | O(mn + m + n) | O(min(m, n) + 1); unit insertion, deletion and substitution costs |
| Strictly increasing subsequence length | O(n log(n + 1)) | O(n); equal values do not extend a strictly increasing subsequence |
| Earliest-finish activity selection | O(n log(n + 1)) | O(n), including sorted copy and output; nonempty half-open intervals, touching allowed |
| Unique permutations | O(n · n!) upper bound | O(n + nk), including `k` returned permutations; input length limited to 9 |
| N-Queens enumeration | O(n · n!) upper bound | O(n + nk), including `k` solutions represented as row-to-column lists; `n` limited to 0..12 |

Amount- and capacity-based DP is **pseudopolynomial**: O(nW) is polynomial in the numeric value of `W`, not in the number of bits needed to encode it. Valid large inputs can still exceed available memory. Enumeration explicitly limits sizes because it materializes all results; the output alone can dominate memory. These methods return one empty solution for an empty permutation or a zero-size queens board.

For DP, write the state meaning and loop-order invariant first. For greedy methods, give an exchange or cut argument; a rule that sounds plausible is not enough. Choosing the largest coin fails for denominations `[1, 3, 4]` and amount `6`, whereas earliest-finish choice is correct for maximizing the number of compatible unweighted activities.

## Reading a bound honestly

- **Worst case** bounds one operation or run for every valid input of that size.
- **Expected** relies on a probabilistic or distribution assumption; bad hash distributions remain possible.
- **Amortized** bounds a sequence of operations without requiring randomness. One dynamic-array append can still copy the entire array.
- **Auxiliary space** differs from total storage and output size. Include recursion frames, buffers, snapshots and returned values when they matter.

Java integer arithmetic normally wraps. The maintained APIs state where they use a wider type or checked arithmetic. For example, Fenwick operations reject intermediate signed-`long` overflow and a failed update leaves the tree unchanged. Do not substitute subtraction for an ordering comparison on arbitrary integer values. Benchmarking would require a separate controlled harness; asymptotic bounds here are analytical claims, not measured speedups.
