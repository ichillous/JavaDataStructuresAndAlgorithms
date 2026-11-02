# Visual learning index

Open [index.html](index.html) locally for the searchable gallery and five step-throughs.
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

[Linear structures: read the lesson](../../data-structures/stacks/implementation.md)

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

[Trees and ranges: read the lesson](../../data-structures/trees/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/union-find-dark.svg"><img src="assets/union-find.svg" alt="Union operations on five indices produce components {0,1,2} and {3,4}. Parent arrows point toward roots 0 and 3, whose component sizes are three and two. Finding index 2 follows its link to root 0." width="520"></picture>

**Key idea:** Connectivity is a partition: five indices form two disjoint components.

## 11. Fenwick prefix blocks

[Trees and ranges: read the lesson](../../docs/solutions.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/fenwick-blocks-dark.svg"><img src="assets/fenwick-blocks.svg" alt="For values 2,-1,4,3, one-based Fenwick slots cover [0,1), [0,2), [2,3) and [0,4). The prefix ending at zero-based boundary 3 combines slots 3 and 2: 4+1=5." width="520"></picture>

**Key idea:** A prefix query combines disjoint blocks. The low bit gives each block's length.

## 12. Segment minimum

[Trees and ranges: read the lesson](../../data-structures/trees/overview.md)

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

[Algorithm traces: read the lesson](../../algorithms/dynamic-programming/problems.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/coin-dp-dark.svg"><img src="assets/coin-dp.svg" alt="Compare every final coin; the largest-first greedy choice loses." width="520"></picture>

**Key idea:** best[a] is optimal for exactly amount a. Each candidate adds one coin to a solved, strictly smaller amount.

## 17. 0/1 knapsack: sweep backward

[Algorithm traces: read the lesson](../../algorithms/dynamic-programming/problems.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/knapsack-state-dark.svg"><img src="assets/knapsack-state.svg" alt="Sweep capacities backward so an item cannot reuse itself." width="520"></picture>

**Key idea:** Descending capacity keeps the current item out of its own predecessor states. Each item is used at most once.

## 18. KMP: keep the overlap

[Algorithm traces: read the lesson](../../algorithms/searching/linear-search.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/kmp-overlap-dark.svg"><img src="assets/kmp-overlap.svg" alt="Prefix fallback retains the overlap between two pattern matches." width="520"></picture>

**Key idea:** After a match, retain the longest matched suffix that is also a pattern prefix. This preserves overlapping matches without rescanning text.

## 19. N-queens: reject and undo

[Algorithm traces: read the lesson](../../algorithms/backtracking/problems.md)

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

[Graphs: read the lesson](../../algorithms/graph/bfs.md)

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
