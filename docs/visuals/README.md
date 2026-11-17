# Visual learning index

Open [index.html](index.html) locally for the searchable gallery and thirteen step-throughs.
The HTML uses local assets and works offline without a server. On GitHub, browse the figures below.

Each numbered figure has an explicit light/dark version, an accessible description and a linked lesson.
Blue identifies the current focus; plain outlines give context. Dashed strokes mark alternatives or invalid choices where labeled.
Figures use the same canonical examples as the Java visual-contract tests. See [design and provenance](design.md) and [verification](verification.md).

## 01. Array insertion: make a gap

[Linear structures: read the lesson](../../data-structures/arrays/operations.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/array-insertion-dark.svg"><img src="assets/array-insertion.svg" alt="Shift the suffix before writing the new value into the live array." width="520"></picture>

**Key idea:** Move the suffix right to left before writing the new value. Otherwise an unread value can be overwritten.

## 02. Unlinking a known list node

[Linear structures: read the lesson](../../data-structures/linked-lists/doubly.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/linked-unlink-dark.svg"><img src="assets/linked-unlink.svg" alt="Bypass the middle node in both directions, then clear its links." width="520"></picture>

**Key idea:** Repair both neighbors, then detach the removed node. A forward link and its backward link must agree.

## 03. Stack: last in, first out

[Linear structures: read the lesson](../../data-structures/stacks/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/stack-lifo-dark.svg"><img src="assets/stack-lifo.svg" alt="Push and pop share the top; the newest value leaves first." width="520"></picture>

**Key idea:** The top is the final live array element. Appending and removing there gives last-in, first-out order.

## 04. Queue wrap: slots versus order

[Linear structures: read the lesson](../../data-structures/queues/implementation.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/queue-wrap-dark.svg"><img src="assets/queue-wrap.svg" alt="The head moves around the buffer while FIFO order stays intact." width="520"></picture>

**Key idea:** At logical offset i, use (head + i) % capacity. Dequeue follows logical order, never raw slot order.

## 05. Hash collisions: keep both keys

[Linear structures: read the lesson](../../data-structures/hash-tables/collision-resolution.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/hash-chaining-dark.svg"><img src="assets/hash-chaining.svg" alt="Equal hash codes select one bucket; key equality still decides a match." width="520"></picture>

**Key idea:** A shared hash only selects a bucket. Equality still distinguishes the mappings; a collision must preserve both.

## 06. LRU: a hit changes the order

[Linear structures: read the lesson](../../data-structures/hash-tables/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/lru-recency-dark.svg"><img src="assets/lru-recency.svg" alt="Promote a cache hit before deciding which entry to evict next." width="520"></picture>

**Key idea:** A hit changes recency. On overflow, evict the left end; the map points straight to the node to move.

## 07. AVL rotation

[Trees and ranges: read the lesson](../../data-structures/trees/avl-trees.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/avl-rotation-dark.svg"><img src="assets/avl-rotation.svg" alt="Inserting 30, 20 and 10 produces a left-heavy chain. A right rotation makes 20 the root with children 10 and 30, preserving in-order traversal and restoring height one." width="520"></picture>

**Key idea:** Rotate links, preserve sorted order, and restore the height balance.

## 08. Heap: array and tree

[Trees and ranges: read the lesson](../../data-structures/heaps/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/heap-anatomy-dark.svg"><img src="assets/heap-anatomy.svg" alt="Bottom-up heapify turns 9,4,7,1,3,2 into the array 1,3,2,4,9,7. The matching complete binary tree labels every zero-based array slot and keeps every parent no greater than its children." width="520"></picture>

**Key idea:** A heap puts the minimum at its root; the array is not globally sorted.

## 09. Trie: preserve shared prefixes

[Trees and ranges: read the lesson](../../data-structures/tries/implementation.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/trie-pruning-dark.svg"><img src="assets/trie-pruning.svg" alt="The words car, cart and cat share a c-a prefix. Deleting car clears the r node&#x27;s word marker but preserves its t child for cart and the sibling t branch for cat." width="520"></picture>

**Key idea:** Deleting a word clears its end marker. Keep nodes still needed by longer words.

## 10. Union-find components

[Trees and ranges: read the lesson](../../data-structures/union-find/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/union-find-dark.svg"><img src="assets/union-find.svg" alt="Union operations on five indices produce components {0,1,2} and {3,4}. Parent arrows point toward roots 0 and 3, whose component sizes are three and two. Finding index 2 follows its link to root 0." width="520"></picture>

**Key idea:** Connectivity is a partition: five indices form two disjoint components.

## 11. Fenwick prefix blocks

[Trees and ranges: read the lesson](../../data-structures/range-queries/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/fenwick-blocks-dark.svg"><img src="assets/fenwick-blocks.svg" alt="For values 2,-1,4,3, one-based Fenwick slots cover [0,1), [0,2), [2,3) and [0,4). The prefix ending at zero-based boundary 3 combines slots 3 and 2: 4+1=5." width="520"></picture>

**Key idea:** A prefix query combines disjoint blocks. The low bit gives each block's length.

## 12. Segment minimum

[Trees and ranges: read the lesson](../../data-structures/range-queries/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/segment-minimum-dark.svg"><img src="assets/segment-minimum.svg" alt="An interval tree over 8,3,7,5 answers the minimum on [1,4) by combining leaf [1,2) with node [2,4). Replacing index 1 with 9 repairs its ancestors and changes the query answer from 3 to 5. Leaves use power-of-two padding with MAX_VALUE when needed." width="520"></picture>

**Key idea:** Each node covers an adjacent interval. A point set repairs one path to the root.

## 13. Binary search: lower bound

[Algorithm traces: read the lesson](../../algorithms/searching/binary-search.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/binary-bound-dark.svg"><img src="assets/binary-bound.svg" alt="Half-open ranges find the first target among duplicate values." width="520"></picture>

**Key idea:** Keep the first possible index inside [low, high]. A value at least the target moves high left, so duplicates cannot hide the first match.

## 14. Merge sort: split and combine

[Algorithm traces: read the lesson](../../algorithms/sorting/merge-sort.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/merge-recursion-dark.svg"><img src="assets/merge-recursion.svg" alt="Split into singletons, then merge sorted children bottom up." width="520"></picture>

**Key idea:** Each parent merges two sorted children. A level costs O(n) work, and there are O(log n) levels: O(n log n) overall.

## 15. Quicksort: three-way partition

[Algorithm traces: read the lesson](../../algorithms/sorting/quick-sort.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/quick-partition-dark.svg"><img src="assets/quick-partition.svg" alt="Gather duplicate pivot values into one finished equal region." width="520"></picture>

**Key idea:** Only the smaller and greater regions recurse. Equal keys already belong together, so the equal region needs no further sorting.

## 16. Coin change: build an optimum

[Algorithm traces: read the lesson](../../algorithms/dynamic-programming/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/coin-dp-dark.svg"><img src="assets/coin-dp.svg" alt="Compare every final coin; the largest-first greedy choice loses." width="520"></picture>

**Key idea:** best[a] is optimal for exactly amount a. Each candidate adds one coin to a solved, strictly smaller amount.

## 17. 0/1 knapsack: sweep backward

[Algorithm traces: read the lesson](../../algorithms/dynamic-programming/problems.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/knapsack-state-dark.svg"><img src="assets/knapsack-state.svg" alt="Sweep capacities backward so an item cannot reuse itself." width="520"></picture>

**Key idea:** Descending capacity keeps the current item out of its own predecessor states. Each item is used at most once.

## 18. KMP: keep the overlap

[Algorithm traces: read the lesson](../../algorithms/strings/pattern-matching.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/kmp-overlap-dark.svg"><img src="assets/kmp-overlap.svg" alt="Prefix fallback retains the overlap between two pattern matches." width="520"></picture>

**Key idea:** After a match, retain the longest matched suffix that is also a pattern prefix. This preserves overlapping matches without rescanning text.

## 19. N-queens: reject and undo

[Algorithm traces: read the lesson](../../algorithms/backtracking/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/queens-backtracking-dark.svg"><img src="assets/queens-backtracking.svg" alt="Trace failed placements before reaching the first valid board." width="520"></picture>

**Key idea:** Place one queen per row. Reject used columns or diagonals; undo an unsuccessful choice before trying another.

## 20. Greedy: finish first

[Algorithm traces: read the lesson](../../algorithms/greedy/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/activity-selection-dark.svg"><img src="assets/activity-selection.svg" alt="Choose earliest finishing intervals; touching endpoints remain compatible." width="520"></picture>

**Key idea:** The earliest finishing compatible activity leaves at least as much time for everything that follows. This maximizes the count, not total duration.

## 21. Bubble sort: adjacent swaps

[Algorithm traces: read the lesson](../../algorithms/sorting/bubble-sort.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/bubble-swaps-dark.svg"><img src="assets/bubble-swaps.svg" alt="Swap neighboring values; each pass settles a maximum on the right." width="520"></picture>

**Key idea:** Each adjacent comparison moves the larger value right. After a pass, the unsorted range's maximum is fixed at its end; a swap-free pass stops.

## 22. BFS: discovered vs processed

[Graphs: read the lesson](../../algorithms/graph/bfs.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/bfs-frontier-dark.svg"><img src="assets/bfs-frontier.svg" alt="Follow the queue separately from discovery; isolate unreachable vertex 5." width="520"></picture>

**Key idea:** Discovery happens on enqueue, before processing; isolated vertices stay unreachable.

## 23. Dijkstra: distance and path

[Graphs: read the lesson](../../algorithms/graph/bfs.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/dijkstra-path-dark.svg"><img src="assets/dijkstra-path.svg" alt="Compare weighted routes from source 0 and distinguish unreachable vertex 4." width="520"></picture>

**Key idea:** Choose the cheapest weighted path; absence is separate from every numeric distance.

## 24. Negative edge or cycle?

[Graphs: read the lesson](../../algorithms/graph/shortest-paths.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/bellman-negative-dark.svg"><img src="assets/bellman-negative.svg" alt="A negative edge yields -3; an added cycle makes targets 1 and 2 unbounded." width="520"></picture>

**Key idea:** A negative edge can be safe; this API rejects any reachable negative cycle.

## 25. SCCs and bridges differ

[Graphs: read the lesson](../../algorithms/graph/dfs.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/scc-bridges-dark.svg"><img src="assets/scc-bridges.svg" alt="Directed return paths form SCCs; undirected essential edges are bridges." width="520"></picture>

**Key idea:** SCCs test directed mutual reachability; bridges test an undirected edge's necessity.

## 26. Compare growth, not timings

[Complexity: read the lesson](../../complexity-analysis/time-complexity.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/complexity-growth-dark.svg"><img src="assets/complexity-growth.svg" alt="Five illustrative functions share a value of one at input size one. A logarithmic work axis shows logarithmic, linear, n log n, quadratic and exponential growth without implying benchmark timings." width="520"></picture>

**Key idea:** Theoretical normalized work; constants, machines and measured runtime are outside this comparison.

## 27. Articulation points

[Graphs: read the lesson](../../algorithms/graph/graph-structure.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/articulation-points-dark.svg"><img src="assets/articulation-points.svg" alt="A bowtie graph of two triangles sharing vertex 2 is searched depth-first from 0 with discovery and low values shown, and removing vertex 2 leaves the separate components {0, 1} and {3, 4}." width="520"></picture>

**Key idea:** An articulation point has a child whose low is at least its own disc.

## 28. Bipartite or odd cycle

[Graphs: read the lesson](../../algorithms/graph/graph-structure.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/bipartite-odd-cycle-dark.svg"><img src="assets/bipartite-odd-cycle.svg" alt="A 6-cycle splits into left {0, 2, 4} and right {1, 3, 5}, while a 5-cycle with a tail has an edge 2-3 joining two vertices of the same colour, and the witness is the odd cycle 2, 1, 0, 4, 3." width="520"></picture>

**Key idea:** A graph is two-colourable exactly when it has no odd cycle; the first edge joining equal colours exposes one.

## 29. Euler path

[Graphs: read the lesson](../../algorithms/graph/graph-structure.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/euler-path-dark.svg"><img src="assets/euler-path.svg" alt="A square with a diagonal and a pendant edge has odd degree only at vertices 0 and 4, so the Euler path starts at 0 and walks every edge once: 0, 1, 2, 3, 0, 2, 4." width="520"></picture>

**Key idea:** Connected edges with zero or two odd-degree vertices give an Euler path.

## 30. Kruskal and Prim

[Graphs: read the lesson](../../algorithms/graph/minimum-spanning-trees.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/mst-kruskal-prim-dark.svg"><img src="assets/mst-kruskal-prim.svg" alt="One weighted graph on five vertices has a minimum spanning tree of total weight 11. Kruskal keeps edges in the order 1, 2, 3, 5 and skips two that close a cycle, while Prim from vertex 0 picks 1, 2, 5, 3." width="520"></picture>

**Key idea:** Both algorithms add cheapest safe edges, in different orders, to reach one total.

## 31. Maximum flow and minimum cut

[Graphs: read the lesson](../../algorithms/graph/flows-and-matching.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/max-flow-min-cut-dark.svg"><img src="assets/max-flow-min-cut.svg" alt="A four-vertex flow network from source 0 to sink 3 carries a maximum flow of 5. The source side {0, 1} is the set reachable in the residual graph, and the three edges leaving it have capacities 2, 1 and 2, summing to 5." width="520"></picture>

**Key idea:** Edges leaving the final source side are saturated and sum to the flow.

## 32. Augmenting a matching

[Graphs: read the lesson](../../algorithms/graph/flows-and-matching.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/matching-augmenting-path-dark.svg"><img src="assets/matching-augmenting-path.svg" alt="A bipartite matching of size 2 leaves left vertex 1 and right vertex 1 free. Flipping the alternating path L1, R0, L0, R1 gives the maximum matching of size 3." width="520"></picture>

**Key idea:** Flipping one augmenting path grows a matching by exactly one pair.

## 33. Binary lifting for LCA

[Graphs: read the lesson](../../algorithms/graph/tree-queries.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/lca-binary-lifting-dark.svg"><img src="assets/lca-binary-lifting.svg" alt="In a seven-vertex tree rooted at 0, lca(6, 4) lifts vertex 6 by 2 to vertex 3, finds the 2^2 and 2^1 ancestors equal, jumps both vertices by 2^0 to 1 and 2, and returns their parent 0, read from the table of 2^j-th ancestors." width="520"></picture>

**Key idea:** Equalise depths, then jump both by powers of two while ancestors differ.

## 34. Tree diameter by two sweeps

[Graphs: read the lesson](../../algorithms/graph/tree-queries.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/tree-diameter-dark.svg"><img src="assets/tree-diameter.svg" alt="In a seven-vertex tree, a breadth-first sweep from vertex 0 ends farthest at 6, and a second sweep from 6 ends farthest at 5, so the path 6, 3, 1, 0, 2, 5 of 5 edges is a longest path." width="520"></picture>

**Key idea:** A sweep from any vertex ends at a diameter endpoint; sweep again from there.

## 35. From hash code to slot

[Hashing and sketches: read the lesson](../../data-structures/hash-tables/functions.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/hash-index-spread-dark.svg"><img src="assets/hash-index-spread.svg" alt="Integer keys 65536 and 131072 have hash codes 0x00010000 and 0x00020000, whose low four bits are both 0, so a plain mask sends both to slot 0. Spreading with h ^ (h &gt;&gt;&gt; 16) and multiplying by 0x9E3779B9 gives slots 1 and 2 of 16." width="520"></picture>

**Key idea:** Mixing lets high bits pick the slot, so such keys stop colliding.

## 36. Probing past a taken slot

[Hashing and sketches: read the lesson](../../data-structures/hash-tables/open-addressing.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/open-addressing-probe-dark.svg"><img src="assets/open-addressing-probe.svg" alt="In an 8-slot table holding K, B and G, put(L) finds home slot 7 taken by G, wraps to slot 0 and stores L there; get(T), also homed at 7, visits slots 7, 0 and 1 and stops at the empty slot 1." width="520"></picture>

**Key idea:** A collision moves to the next slot and wraps past the end; a lookup retraces it to the first empty slot.

## 37. Backward-shift deletion

[Hashing and sketches: read the lesson](../../data-structures/hash-tables/open-addressing.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/backward-shift-delete-dark.svg"><img src="assets/backward-shift-delete.svg" alt="In the cluster C, H, M, B at slots 3 to 6, clearing only H&#x27;s slot 4 would hide M, which is stored at slot 5 but starts its search at slot 4. Backward-shift deletion moves M to slot 4, clears slot 5 and leaves B at its home slot 6." width="520"></picture>

**Key idea:** Shifting later keys back keeps every key reachable.

## 38. LFU frequency buckets

[Hashing and sketches: read the lesson](../../data-structures/hash-tables/lfu-cache.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/lfu-buckets-dark.svg"><img src="assets/lfu-buckets.svg" alt="A capacity-3 LfuCache after put A, B, C and get C holds A and B in bucket 1 and C in bucket 2. get(B) moves B to the end of bucket 2, and put(D) then evicts A, the oldest key of bucket 1." width="520"></picture>

**Key idea:** Hits lift keys; the oldest key in the lowest bucket is evicted.

## 39. Bloom filter bits

[Hashing and sketches: read the lesson](../../data-structures/probabilistic/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/bloom-filter-bits-dark.svg"><img src="assets/bloom-filter-bits.svg" alt="A 16-bit Bloom filter with two probes per key sets bits 8, 11, 1 and 3 for apple and pear. kiwi reads bits 8 and 7 and bit 7 is clear, a definite miss; plum reads bits 1 and 11, both set, a false positive." width="520"></picture>

**Key idea:** One clear bit proves absence; all-set bits mean only possibly present.

## 40. Count-Min rows

[Hashing and sketches: read the lesson](../../data-structures/probabilistic/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/count-min-rows-dark.svg"><img src="assets/count-min-rows.svg" alt="A Count-Min sketch of 3 rows by 8 columns after adding apple 3, pear 1, fig 2, plum 4 and kiwi 1. pear reads counters 2, 3 and 1 in its three rows, and the minimum, 1, is the estimate." width="520"></picture>

**Key idea:** Rows only overcount, so a key's smallest counter is its estimate.

## 41. Heap sift paths

[Trees and ranges: read the lesson](../../data-structures/heaps/operations.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/heap-sift-paths-dark.svg"><img src="assets/heap-sift-paths.svg" alt="In the min heap 1, 3, 8, 4, 5, 9, add(7) appends at slot 6 and swaps up one level to slot 2, giving 1, 3, 7, 4, 5, 9, 8. remove() then takes out 1, moves 8 to the root and sifts it down two levels to slot 3, giving 3, 4, 7, 8, 5, 9." width="520"></picture>

**Key idea:** Each repair follows one path of at most log2 n levels.

## 42. Top k with a bounded heap

[Trees and ranges: read the lesson](../../data-structures/queues/priority-queues.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/bounded-heap-topk-dark.svg"><img src="assets/bounded-heap-topk.svg" alt="Selection.smallestK over the stream 8, 3, 9, 5, 2, 5, 1 with k = 3 fills the heap with 8, 3, 9, then evicts 9, evicts 8, drops the second 5 and evicts 5 for 1, ending with 1, 2, 3." width="520"></picture>

**Key idea:** The root is the largest value kept; an arrival must beat it to get in.

## 43. Trie prefix walk

[Trees and ranges: read the lesson](../../data-structures/tries/implementation.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/trie-prefix-walk-dark.svg"><img src="assets/trie-prefix-walk.svg" alt="For the words car, card, care, cart, cat and dog, wordsWithPrefix(&quot;ca&quot;, 3) walks the c and a nodes, then emits car, card and care in sorted order and stops before cart and cat; the dog branch is never visited." width="520"></picture>

**Key idea:** Walk the prefix once, then visit children in sorted order until the limit.

## 44. Sparse table overlap

[Trees and ranges: read the lesson](../../data-structures/range-queries/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/sparse-table-overlap-dark.svg"><img src="assets/sparse-table-overlap.svg" alt="For the values 5, 2, 6, 4, 7, 3, 8, 1, rangeMin(1, 7) takes the minimum of the blocks [1, 5) and [3, 7), which overlap on indices 3 and 4, and gets 2. A sum of the same blocks would give 41 instead of 30." width="520"></picture>

**Key idea:** Minimum ignores the overlap of two blocks; a sum would count it twice.

## 45. Lazy range add

[Trees and ranges: read the lesson](../../data-structures/range-queries/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/lazy-propagation-dark.svg"><img src="assets/lazy-propagation.svg" alt="In a LazySegmentTree over 1, 2, 3, 4, rangeAdd(2, 4, 5) stops at the node covering [2, 4), raising its sum to 17 and leaving a pending +5 for its leaves. rangeSum(3, 4) then reads the leaf&#x27;s stored 4, adds the pending 5 and returns 9." width="520"></picture>

**Key idea:** A range add stops at a covering node and leaves a pending value.

## 46. Path compression

[Trees and ranges: read the lesson](../../data-structures/union-find/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/union-find-compression-dark.svg"><img src="assets/union-find-compression.svg" alt="In a UnionFind over 9 indices, union(0, 5) hangs the size-4 root 4 under the size-5 root 0. Before find(7) the path is 7, 6, 4, 0; afterwards 7 and 6 point directly at 0." width="520"></picture>

**Key idea:** Union by size keeps trees shallow; find re-points its path at the root.

## 47. Appending to a full array

[Linear structures: read the lesson](../../data-structures/arrays/operations.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/dynamic-array-growth-dark.svg"><img src="assets/dynamic-array-growth.svg" alt="A DynamicArray with capacity 4 holds 10, 20, 30 and 40, so add(50) allocates 8 slots, copies the four values and writes 50 at index 4. That append copies 4 values; the next copy happens at append 9 and moves 8, for 12 copies in 9 appends." width="520"></picture>

**Key idea:** Doubling makes copies rare: n appends copy fewer than 2n values in total, so one append costs O(1) amortized.

## 48. Insert after a known node

[Linear structures: read the lesson](../../data-structures/linked-lists/singly.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/singly-insert-after-dark.svg"><img src="assets/singly-insert-after.svg" alt="In the chain 10, 20, 30, inserting 15 after 10 takes two link writes: 15.next = 10.next, then 10.next = 15, giving 10, 15, 20, 30. Writing 10.next = 15 first makes 15.next point at 15 itself and leaves 20 and 30 unreachable." width="520"></picture>

**Key idea:** Link the new node onward first, then redirect the predecessor.

## 49. Matching brackets with a stack

[Linear structures: read the lesson](../../data-structures/stacks/implementation.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/stack-bracket-matching-dark.svg"><img src="assets/stack-bracket-matching.svg" alt="An ArrayStack traces {[()]}: three pushes and three pops leave it empty. The same stack traced on ([)] holds ( and [ when the closer ) arrives, and the top [ does not match it, so the string is rejected at the third character." width="520"></picture>

**Key idea:** The stack holds exactly the unmatched openers; a closer must match the top.

## 50. A deque at both ends

[Linear structures: read the lesson](../../data-structures/queues/implementation.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/deque-both-ends-dark.svg"><img src="assets/deque-both-ends.svg" alt="A RingDeque with four slots receives addLast(20), addLast(30), addFirst(10) and addLast(40); addFirst moves the head from slot 0 back to slot 3, and the tail ends at slot 2. removeLast() returns 40 and removeFirst() returns 10, leaving 20 and 30." width="520"></picture>

**Key idea:** Both ends are indices on a circular buffer: addFirst steps the head back and wraps.

## 51. Reaching index 5

[Linear structures: read the lesson](../../data-structures/arrays/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/array-vs-list-access-dark.svg"><img src="assets/array-vs-list-access.svg" alt="In a DynamicArray of eight values, get(5) reads slot 5 in one jump. In a DoublyLinkedList of the same values it starts at the tail and follows 2 links, because 5 is in the back half; the hop counts for indices 0 to 7 are 0, 1, 2, 3, 3, 2, 1 and 0." width="520"></picture>

**Key idea:** An array reaches any index in one jump; a list walks links from the nearer end.

## 52. Data structures by question

[Maps: read the lesson](../../data-structures/README.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/data-structures-map-dark.svg"><img src="assets/data-structures-map.svg" alt="9 cards sort the structures by the question they answer. Each card shows a small pictogram, names the family, a method to start with and the folder to open: by position, DynamicArray.get, arrays/ and linked-lists/; by arrival, RingDeque.removeFirst, stacks/ and queues/; by key, ChainedHashMap.get, hash-tables/; by order, RedBlackTreeMap.floorKey, trees/; by priority, BinaryMinHeap.remove, heaps/; by prefix, Trie.wordsWithPrefix, tries/; by range, FenwickTree.rangeSum, range-queries/; by connectivity, UnionFind.find, union-find/ and graphs/; approximately, BloomFilter.mightContain, probabilistic/." width="520"></picture>

**Key idea:** Pick by the question you must answer.

## 53. Algorithms by task

[Maps: read the lesson](../../algorithms/README.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/algorithms-map-dark.svg"><img src="assets/algorithms-map.svg" alt="9 cards sort the algorithms by the task they perform. Each card shows a small pictogram, names the family, a method to start with and the folder to open: search, SearchAlgorithms.lowerBound, searching/ and array-patterns/; sort, SortAlgorithms.mergeSort, sorting/ and divide-and-conquer/; select, Selection.kthSmallest, searching/; traverse, GraphAlgorithms.breadthFirst, graph/; shortest path, GraphAlgorithms.dijkstra, graph/; connect, FlowNetwork.maxFlow, graph/; optimize, DynamicProgramming.knapsack01, greedy/ and backtracking/ and dynamic-programming/; match text, StringAlgorithms.kmpFindAll, strings/; compute, NumberTheory.gcd, math/ and bit-manipulation/." width="520"></picture>

**Key idea:** Pick by the task you must perform.

## 54. Euclid and back-substitution

[Math and bits: read the lesson](../../algorithms/math/number-theory.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/euclid-steps-dark.svg"><img src="assets/euclid-steps.svg" alt="NumberTheory.gcd(240, 46) divides 240 by 46, 46 by 10, 10 by 6, 6 by 4 and 4 by 2, reaching remainder 0, so the gcd is 2. Substituting the divisions backwards gives 2 = 47 times 46 minus 9 times 240, the coefficients of extendedGcd." width="520"></picture>

**Key idea:** The last nonzero remainder is the gcd, and undoing each division writes it as x×a + y×b.

## 55. modPow by repeated squaring

[Math and bits: read the lesson](../../algorithms/math/number-theory.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/modpow-squares-dark.svg"><img src="assets/modpow-squares.svg" alt="NumberTheory.modPow(3, 13, 1000) reads the bits of 13 = 1101 from the lowest: the squares of 3 are 3, 9, 81 and 561 modulo 1000, and the result 3, 3, 243, 323 changes only on the 1 bits, ending at 323." width="520"></picture>

**Key idea:** Square the base once per exponent bit and multiply it in only for 1 bits: O(log e) steps.

## 56. Sieve of Eratosthenes

[Math and bits: read the lesson](../../algorithms/math/number-theory.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/sieve-grid-dark.svg"><img src="assets/sieve-grid.svg" alt="NumberTheory.primesUpTo(30) on a grid of 1 to 30: 2 crosses out 14 multiples from 4, 3 crosses out 4 more from 9, 5 crosses out 25, and the ten numbers never crossed are the primes 2, 3, 5, 7, 11, 13, 17, 19, 23 and 29." width="520"></picture>

**Key idea:** A prime p starts crossing at p×p, because smaller multiples have a smaller prime factor.

## 57. Reservoir sampling

[Math and bits: read the lesson](../../algorithms/math/randomized-sampling.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/reservoir-sampling-dark.svg"><img src="assets/reservoir-sampling.svg" alt="Sampling.reservoirSample over the items A to H with k = 3 and seed 17 fills the reservoir with A, B, C, then draws j for each later arrival with replacement chance 3/4 down to 3/8; D, F and H replace slots 0, 0 and 1, giving F, H, C." width="520"></picture>

**Key idea:** Arrival i is kept with chance k/i, so every item seen so far ends up in the sample with equal chance.

## 58. Alias table columns

[Math and bits: read the lesson](../../algorithms/math/randomized-sampling.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/alias-table-dark.svg"><img src="assets/alias-table.svg" alt="AliasTable for the weights 1, 1, 2, 4 has four equal columns: A and B each keep half their column and give the other half to D, while C and D fill their columns. Adding the pieces back gives the probabilities 1/8, 1/8, 1/4 and 1/2." width="520"></picture>

**Key idea:** Equal columns split between two items let one uniform column pick and one coin flip draw any weight.

## 59. A set as a bit mask

[Math and bits: read the lesson](../../algorithms/bit-manipulation/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/bitmask-subsets-dark.svg"><img src="assets/bitmask-subsets.svg" alt="The set {1, 3} of four elements is the int mask 1010 = 10. Testing bit 3 gives 1, setting bit 2 gives 1110, clearing bit 3 gives 0010, and (sub - 1) &amp; mask walks the subsets 10, 8, 2 and 0." width="520"></picture>

**Key idea:** A set is an int: test, set and clear bits in one operation, and (sub − 1) & mask visits every subset.

## 60. LCS table and traceback

[Optimization: read the lesson](../../algorithms/dynamic-programming/problems.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/lcs-traceback-dark.svg"><img src="assets/lcs-traceback.svg" alt="The longest-common-subsequence table for GCTA and TGCAT, with the path longestCommonSubsequence walks back from the corner: it goes up on the tie at the corner, takes T, C and G on equal letters, and returns GCT." width="520"></picture>

**Key idea:** The traceback re-derives each choice from the table: take equal letters, otherwise move toward the larger neighbour, and go up on a tie.

## 61. Edit distance path

[Optimization: read the lesson](../../algorithms/dynamic-programming/problems.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/edit-distance-grid-dark.svg"><img src="assets/edit-distance-grid.svg" alt="The edit-distance table for brain and paint with the path editScript returns: delete b, replace r with p, keep a, i and n, then insert t, for distance 3." width="520"></picture>

**Key idea:** Walking back from the corner prefers the diagonal, then delete, then insert; each step that is not a match adds 1.

## 62. Matrix chain splits

[Optimization: read the lesson](../../algorithms/dynamic-programming/classic-problems.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/matrix-chain-splits-dark.svg"><img src="assets/matrix-chain-splits.svg" alt="The cost table for four matrices with dimensions 30, 1, 40, 10, 25, the three candidate splits of the whole chain, and the stored splits that give (A1((A2A3)A4)) at cost 1400." width="520"></picture>

**Key idea:** Each cell keeps the cheapest split k; following the stored splits from the whole chain down gives the parenthesization.

## 63. Held-Karp states

[Optimization: read the lesson](../../algorithms/dynamic-programming/classic-problems.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/held-karp-masks-dark.svg"><img src="assets/held-karp-masks.svg" alt="The Held-Karp table for four cities, one cost per visited set and last city, the closing edge back to city 0, and the optimal tour 0, 2, 3, 1, 0 of cost 80." width="520"></picture>

**Key idea:** Held-Karp keeps one best cost per (visited set, last city), so 2^n n^2 steps replace the (n-1)! tours.

## 64. Weighted interval choices

[Optimization: read the lesson](../../algorithms/dynamic-programming/classic-problems.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/weighted-interval-choice-dark.svg"><img src="assets/weighted-interval-choice.svg" alt="Four weighted intervals sorted by end with their last compatible interval, the take-or-skip comparison for each, and the walk back that chooses B and D for total weight 10." width="520"></picture>

**Key idea:** Take an interval only if its weight plus the best earlier compatible schedule beats skipping it.

## 65. Huffman merges and lengths

[Optimization: read the lesson](../../algorithms/greedy/huffman-coding.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/huffman-tree-dark.svg"><img src="assets/huffman-tree.svg" alt="Five symbols with frequencies 5, 9, 12, 13 and 16 are merged two lightest at a time into a tree, which gives code lengths 3, 3, 2, 2 and 2 and a weighted length of 124." width="520"></picture>

**Key idea:** Merging the two lightest nodes first leaves the rarest symbols deepest, so frequent symbols get the shortest codes.

## 66. Subset sum, two halves

[Optimization: read the lesson](../../algorithms/backtracking/problems.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/subset-sum-halves-dark.svg"><img src="assets/subset-sum-halves.svg" alt="Six values are split into two halves of three; the sums of all eight subsets of each half are listed, and the first right sum whose complement is stored on the left selects indices 0, 1 and 3 for target 6." width="520"></picture>

**Key idea:** Each half has only 2^(n/2) subsets, so storing one half's sums and scanning the other's finds a pair in about 2^(n/2) steps, not 2^n.

## 67. Three ways to store a graph

[Graphs: read the lesson](../../data-structures/graphs/representation.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/graph-representations-dark.svg"><img src="assets/graph-representations.svg" alt="One directed graph with 4 vertices and the edges 0 to 1, 0 to 2, 2 to 1 and 2 to 3, shown as an adjacency list, a 4 by 4 adjacency matrix and an edge list, each with the cost of testing an edge and listing neighbors." width="520"></picture>

**Key idea:** Edge tests favor a matrix; neighbor scans favor an adjacency list.

## 68. BFS and DFS visit order

[Graphs: read the lesson](../../data-structures/graphs/traversal.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/bfs-vs-dfs-order-dark.svg"><img src="assets/bfs-vs-dfs-order.svg" alt="A 6-vertex undirected graph from source 0: breadthFirst visits 0, 1, 2, 3, 4, 5 in layers of distance 0, 1 and 2, while depthFirst visits 0, 1, 3, 4, 5, 2 by following one branch to its end." width="520"></picture>

**Key idea:** A queue visits whole layers; a stack follows one branch to its end.

## 69. Kahn's topological order

[Graphs: read the lesson](../../algorithms/graph/topological-sort.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/topological-kahn-dark.svg"><img src="assets/topological-kahn.svg" alt="Kahn&#x27;s algorithm on a 5-vertex DAG whose in-degrees are 2, 1, 0, 0 and 2: it pops ready vertices 2, 3, 0, 1, 4, lowering in-degrees and refilling the ready queue, and outputs 2, 3, 0, 1, 4." width="520"></picture>

**Key idea:** A vertex is output once its in-degree is 0, so every edge points forward.

## 70. Shortest-path chooser

[Graphs: read the lesson](../../algorithms/graph/shortest-paths.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/shortest-path-chooser-dark.svg"><img src="assets/shortest-path-chooser.svg" alt="A column of seven yes-or-no questions from all pairs, unweighted, 0-1 weights, DAG, negative weights and heuristic to nonnegative weights, each paired with the algorithm and the repository method that answers it." width="520"></picture>

**Key idea:** Use the most specific algorithm whose precondition your graph meets; each one exploits a fact the general one cannot assume.

## 71. 0-1 BFS and its deque

[Graphs: read the lesson](../../algorithms/graph/shortest-paths.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/zero-one-bfs-deque-dark.svg"><img src="assets/zero-one-bfs-deque.svg" alt="0-1 BFS from vertex 0 on a 4-vertex directed graph: weight-0 targets are pushed to the front of the deque and weight-1 targets to the back, so vertex 1 is first queued at distance 1 and then improved to 0 through 0, 2, 3, 1." width="520"></picture>

**Key idea:** Zero-weight edges go to the deque's front, unit-weight edges to its back.

## 72. A* with a good heuristic

[Graphs: read the lesson](../../algorithms/graph/shortest-paths.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/astar-heuristic-dark.svg"><img src="assets/astar-heuristic.svg" alt="A* on a 5 by 5 unit grid from cell 10 to cell 14 with the Manhattan heuristic: the route 10, 11, 12, 13, 14 has f = g + h equal to 4 everywhere, the heuristic was called for 13 of 25 cells, and with h = 0 it was called for 21." width="520"></picture>

**Key idea:** A good heuristic keeps the search near the route; far cells never get an h evaluation.

## 73. One Floyd-Warshall round

[Graphs: read the lesson](../../algorithms/graph/shortest-paths.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/floyd-warshall-via-dark.svg"><img src="assets/floyd-warshall-via.svg" alt="A 4-vertex directed graph: allowing vertex 1 as a stop-over improves the distance matrix entry d[0][2] from 9 to 5 because d[0][1] + d[1][2] = 3 + 2, and no other entry changes." width="520"></picture>

**Key idea:** Round k asks whether going through vertex k beats the best route so far.

## 74. Two pointers, sorted array

[Array patterns: read the lesson](../../algorithms/array-patterns/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/two-pointers-pair-sum-dark.svg"><img src="assets/two-pointers-pair-sum.svg" alt="pairWithSum on 1, 2, 3, 4, 6 with target 6 starts at the two ends: 1 + 6 is too large so the right index drops, 1 + 4 is too small so the left index drops, and 2 + 4 matches at indices 1 and 3." width="520"></picture>

**Key idea:** Each move drops an index with no partner: at most n - 1 moves.

## 75. Window jumping a repeat

[Array patterns: read the lesson](../../algorithms/array-patterns/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/sliding-window-distinct-dark.svg"><img src="assets/sliding-window-distinct.svg" alt="longestSubstringWithoutRepeats on abcbad grows the window to abc, jumps its start to index 2 when b repeats, ignores the stale earlier a at index 0, and ends with the longest window cbad at indices 2 to 5." width="520"></picture>

**Key idea:** The left edge never moves back, so each unit is added once: linear time.

## 76. Next greater by stack

[Array patterns: read the lesson](../../algorithms/array-patterns/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/monotonic-stack-next-greater-dark.svg"><img src="assets/monotonic-stack-next-greater.svg" alt="nextGreaterIndices on 2, 1, 2, 4, 3 keeps unresolved indices on a stack: index 1 is resolved by the second 2, the equal 2 at index 0 stays, and the 4 resolves indices 2 and 0, giving 3, 2, 3, -1, -1." width="520"></picture>

**Key idea:** Each index is pushed once and popped once: O(n) despite the inner loop.

## 77. Range adds by differences

[Array patterns: read the lesson](../../algorithms/array-patterns/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/difference-array-ranges-dark.svg"><img src="assets/difference-array-ranges.svg" alt="applyRangeAdds on five zeros marks +3 at index 1 and -3 at index 4, then +2 at index 0 and -2 at index 2, and one running sum produces 2, 5, 3, 3, 0." width="520"></picture>

**Key idea:** Two marks per update, then one running sum: O(n + u) in total.

## 78. Sweeping events for rooms

[Array patterns: read the lesson](../../algorithms/array-patterns/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/interval-sweep-rooms-dark.svg"><img src="assets/interval-sweep-rooms.svg" alt="minimumMeetingRooms on the intervals [1, 4), [2, 5), [5, 7) and [6, 8) sorts the eight start and end events, processes the end at 5 before the start at 5, and reaches a depth of 2, so two rooms suffice." width="520"></picture>

**Key idea:** The busiest moment sets the room count; at a tie an end comes first.

## 79. Inversions during a merge

[Array patterns: read the lesson](../../algorithms/divide-and-conquer/problems.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/inversion-count-merge-dark.svg"><img src="assets/inversion-count-merge.svg" alt="countInversions on 2, 4, 1, 3, 5 merges the sorted halves 2, 4 and 1, 3, 5: emitting 1 jumps over two left values, emitting 3 jumps over one, and the other values add nothing, for 3 inversions." width="520"></picture>

**Key idea:** A smaller right value jumps over every left value still waiting.

## 80. Four ways to walk a tree

[Trees and ranges: read the lesson](../../data-structures/trees/binary-trees.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/tree-traversals-dark.svg"><img src="assets/tree-traversals.svg" alt="A binary search tree built from 8, 3, 10, 1, 6, 14, 4 and 7 has root 8, leaves 1, 4, 7 and 14, and height 3. Pre-order reads 8 3 1 6 4 7 10 14, in-order reads the keys sorted, post-order ends at the root 8, and level order reads one depth at a time." width="520"></picture>

**Key idea:** Pre, in and post order differ only in when the root is read; level order reads one depth at a time.

## 81. Delete with two children

[Trees and ranges: read the lesson](../../data-structures/trees/binary-search-trees.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/bst-delete-successor-dark.svg"><img src="assets/bst-delete-successor.svg" alt="Deleting key 2 from a BinarySearchTree whose root 2 has children 1 and 4, with 3 below 4, copies the in-order successor 3 into the root and unlinks the old 3. The in-order keys become 1, 3, 4 and the height drops from 2 to 1." width="520"></picture>

**Key idea:** The successor's key replaces the deleted one; its old node is unlinked.

## 82. Red links make 3-nodes

[Trees and ranges: read the lesson](../../data-structures/trees/red-black-trees.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/red-black-23-nodes-dark.svg"><img src="assets/red-black-23-nodes.svg" alt="After put 1 to 5, a RedBlackTreeMap has black root 4 with red left child 2, black children 1 and 3 under 2, and black 5. Read as a 2-3 tree it is the 3-node [2 4] over the 2-nodes [1], [3] and [5]; the height is 2 as a binary tree and 1 as a 2-3 tree." width="520"></picture>

**Key idea:** A red link and its black parent make one 3-node.

## 83. Three local repairs

[Trees and ranges: read the lesson](../../data-structures/trees/red-black-trees.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/red-black-fixups-dark.svg"><img src="assets/red-black-fixups.svg" alt="Rotate left turns black 1 with a red right child 2 into black 2 with a red left child 1. Rotate right turns black 3 over red 2 over red 1 into black 2 with red children 1 and 3. Flip colors turns black 2 with red children into red 2 with black children." width="520"></picture>

**Key idea:** Rotations keep sorted order; a flip splits a 4-node.

## 84. Linear search: first match

[Sorting and searching: read the lesson](../../algorithms/searching/linear-search.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/linear-scan-first-match-dark.svg"><img src="assets/linear-scan-first-match.svg" alt="linearSearch for 4 in 9, 4, 4, 2 inspects two cells and returns index 1, never reaching the second 4; a search for 7 inspects all four cells and returns -1." width="520"></picture>

**Key idea:** A hit stops the scan; only a miss costs n comparisons.

## 85. Predicate boundary search

[Sorting and searching: read the lesson](../../algorithms/searching/binary-search-on-answer.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/predicate-boundary-dark.svg"><img src="assets/predicate-boundary.svg" alt="For weights 3, 2, 2, 4, 1, 4 shipped in 3 days, capacities 4 and 5 are too slow and 6 to 16 fit. firstTrue(4, 17, fits) probes 10, 7, 5 and 6 and returns 6." width="520"></picture>

**Key idea:** Once a capacity fits, every larger one fits: each probe halves the range.

## 86. Quickselect keeps one part

[Sorting and searching: read the lesson](../../algorithms/searching/selection.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/quickselect-shrink-dark.svg"><img src="assets/quickselect-shrink.svg" alt="kthSmallest of 7, 2, 9, 4, 4, 1 with k = 2: three partitions with pivots 7, 2 and 4 narrow the range from [0..5] to [0..3] to [2..3], and the equal part answers 4." width="520"></picture>

**Key idea:** k lies in one part of each partition: keep it, discard the rest.

## 87. K-way merge with a heap

[Sorting and searching: read the lesson](../../algorithms/searching/selection.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/kway-merge-heap-dark.svg"><img src="assets/kway-merge-heap.svg" alt="mergeSorted of 1, 4, 7 and 2, 4 and 3, 6 keeps one head per list in a heap; when 4 from list A and 4 from list B tie, A pops first, giving 1, 2, 3, 4, 4, 6, 7." width="520"></picture>

**Key idea:** The smallest head is the next output; ties pop in list order.

## 88. One insertion step

[Sorting and searching: read the lesson](../../algorithms/sorting/insertion-sort.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/insertion-sort-shift-dark.svg"><img src="assets/insertion-sort-shift.svg" alt="In 3, 5, 8, 9, 4, 1 the prefix 3, 5, 8, 9 is sorted. The key 4 is saved, the three larger values 5, 8 and 9 shift one slot right, and 4 drops into index 1." width="520"></picture>

**Key idea:** Insertion sort shifts only the larger values one slot right, then drops the saved key into the gap, so the sorted prefix grows by one.

## 89. Heapsort phases

[Sorting and searching: read the lesson](../../algorithms/sorting/heap-sort.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/heapsort-phases-dark.svg"><img src="assets/heapsort-phases.svg" alt="Heapsort on 3, 1, 6, 5, 2, 4 builds the max-heap 6, 5, 4, 1, 2, 3, then moves 6 and 5 to the end, giving 5, 3, 4, 1, 2, 6 and 4, 3, 2, 1, 5, 6, until 1 to 6 is sorted." width="520"></picture>

**Key idea:** Heapsort builds a max-heap, then swaps the root to the end and repairs the shorter heap; the sorted suffix grows by one each time.

## 90. Counting sort tally

[Sorting and searching: read the lesson](../../algorithms/sorting/non-comparison-and-comparator-sorts.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/counting-sort-counts-dark.svg"><img src="assets/counting-sort-counts.svg" alt="For 5, 3, 6, 3, 4, 6 the range 3 to 6 has four counts: 2, 1, 1 and 2. Writing each value back that many times gives 3, 3, 4, 5, 6, 6." width="520"></picture>

**Key idea:** Counting sort never compares two values: it tallies each value, then writes each value back as many times as it was counted.

## 91. Radix passes by digit

[Sorting and searching: read the lesson](../../algorithms/sorting/non-comparison-and-comparator-sorts.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/radix-lsd-passes-dark.svg"><img src="assets/radix-lsd-passes.svg" alt="Sorting 35, 21, 41, 15, 31, 25 by the ones digit gives 21, 41, 31, 35, 15, 25; a stable pass on the tens digit then gives 15, 21, 25, 31, 35, 41." width="520"></picture>

**Key idea:** Radix sort orders by the least significant digit first; because every pass is stable, ties keep the earlier order and the last pass leaves a full sort.

## 92. Stable and unstable outcomes

[Sorting and searching: read the lesson](../../algorithms/sorting/non-comparison-and-comparator-sorts.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/sort-stability-dark.svg"><img src="assets/sort-stability.svg" alt="Records 3a, 1b, 3c, 2d, 1e, 3f sorted by their number: a stable sort gives 1b, 1e, 2d, 3a, 3c, 3f, keeping tags in order, while a hypothetical unstable outcome 1e, 1b, 2d, 3c, 3a, 3f reorders equal keys." width="520"></picture>

**Key idea:** A stable sort keeps equal keys in their input order; an unstable sort may reorder them, losing any earlier ordering by tag.

## 93. Z-array and the Z-box

[Strings: read the lesson](../../algorithms/strings/pattern-matching.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/z-box-dark.svg"><img src="assets/z-box.svg" alt="The Z-array of aabxaabxaab is 11, 1, 0, 0, 7, 1, 0, 0, 3, 1, 0; the match at index 4 is a Z-box [4, 11), index 5 copies z[1] = 1 from it, and index 8 starts at 3 because z[4] = 7 overruns the box." width="520"></picture>

**Key idea:** z[i] starts from its mirror's value; only units past the box edge extend it.

## 94. Rabin-Karp: hash, then verify

[Strings: read the lesson](../../algorithms/strings/pattern-matching.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/rabin-karp-rolling-dark.svg"><img src="assets/rabin-karp-rolling.svg" alt="Searching aeaecbagab for bagab slides a five-unit window; windows 0 and 5 have the pattern&#x27;s hash, window 0 (aeaec) is rejected at unit 0 and window 5 is reported." width="520"></picture>

**Key idea:** Hash hits only choose where to compare; verification rejects impostors.

## 95. Manacher: reuse the mirror

[Strings: read the lesson](../../algorithms/strings/pattern-matching.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/manacher-mirror-dark.svg"><img src="assets/manacher-mirror.svg" alt="In cabacabad the palindrome abacaba around index 4 spans [1, 7]; index 5 copies radius 1 from index 3 and index 6 caps radius 3 from index 2 at the 2 units left, giving odd radii 1, 1, 3, 1, 4, 1, 2, 1, 1." width="520"></picture>

**Key idea:** Inside a palindrome a center copies its mirror's radius, capped at the edge.

## 96. Aho-Corasick links

[Strings: read the lesson](../../algorithms/strings/multi-pattern-and-suffix-arrays.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/aho-corasick-links-dark.svg"><img src="assets/aho-corasick-links.svg" alt="The trie of he, she, his and hers, then its failure links listed one per row: sh to h, she to he, his to s and hers to s; every other node links to the root." width="520"></picture>

**Key idea:** A failure link points at the longest suffix of a node that is still in the trie.

## 97. Aho-Corasick scan

[Strings: read the lesson](../../algorithms/strings/multi-pattern-and-suffix-arrays.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/aho-corasick-scan-dark.svg"><img src="assets/aho-corasick-scan.svg" alt="Scanning ushers keeps the states root, s, sh, she, her and hers, and reports she at 1, he at 2 (through the link she to he) and hers at 2." width="520"></picture>

**Key idea:** One state per text unit; links report every pattern that ends at that unit.

## 98. Suffix array and a pattern range

[Strings: read the lesson](../../algorithms/strings/multi-pattern-and-suffix-arrays.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/suffix-array-lcp-dark.svg"><img src="assets/suffix-array-lcp.svg" alt="The sorted suffixes of banana start at 5, 3, 1, 0, 4, 2 with lcp 0, 1, 3, 0, 0, 2; the pattern ana is the rank range [1, 3), whose starts 3 and 1 sort to 1, 3." width="520"></picture>

**Key idea:** Suffixes sharing a prefix sit side by side: a lookup is one range.
