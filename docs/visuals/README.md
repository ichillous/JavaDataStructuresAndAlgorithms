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

## 35. Appending to a full array

[Linear structures: read the lesson](../../data-structures/arrays/operations.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/dynamic-array-growth-dark.svg"><img src="assets/dynamic-array-growth.svg" alt="A DynamicArray with capacity 4 holds 10, 20, 30 and 40, so add(50) allocates 8 slots, copies the four values and writes 50 at index 4. That append copies 4 values; the next copy happens at append 9 and moves 8, for 12 copies in 9 appends." width="520"></picture>

**Key idea:** Doubling makes copies rare: n appends copy fewer than 2n values in total, so one append costs O(1) amortized.

## 36. Insert after a known node

[Linear structures: read the lesson](../../data-structures/linked-lists/singly.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/singly-insert-after-dark.svg"><img src="assets/singly-insert-after.svg" alt="In the chain 10, 20, 30, inserting 15 after 10 takes two link writes: 15.next = 10.next, then 10.next = 15, giving 10, 15, 20, 30. Writing 10.next = 15 first makes 15.next point at 15 itself and leaves 20 and 30 unreachable." width="520"></picture>

**Key idea:** Link the new node onward first, then redirect the predecessor.

## 37. Matching brackets with a stack

[Linear structures: read the lesson](../../data-structures/stacks/implementation.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/stack-bracket-matching-dark.svg"><img src="assets/stack-bracket-matching.svg" alt="An ArrayStack traces {[()]}: three pushes and three pops leave it empty. The same stack traced on ([)] holds ( and [ when the closer ) arrives, and the top [ does not match it, so the string is rejected at the third character." width="520"></picture>

**Key idea:** The stack holds exactly the unmatched openers; a closer must match the top.

## 38. A deque at both ends

[Linear structures: read the lesson](../../data-structures/queues/implementation.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/deque-both-ends-dark.svg"><img src="assets/deque-both-ends.svg" alt="A RingDeque with four slots receives addLast(20), addLast(30), addFirst(10) and addLast(40); addFirst moves the head from slot 0 back to slot 3, and the tail ends at slot 2. removeLast() returns 40 and removeFirst() returns 10, leaving 20 and 30." width="520"></picture>

**Key idea:** Both ends are indices on a circular buffer: addFirst steps the head back and wraps.

## 39. Reaching index 5

[Linear structures: read the lesson](../../data-structures/arrays/overview.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/array-vs-list-access-dark.svg"><img src="assets/array-vs-list-access.svg" alt="In a DynamicArray of eight values, get(5) reads slot 5 in one jump. In a DoublyLinkedList of the same values it starts at the tail and follows 2 links, because 5 is in the back half; the hop counts for indices 0 to 7 are 0, 1, 2, 3, 3, 2, 1 and 0." width="520"></picture>

**Key idea:** An array reaches any index in one jump; a list walks links from the nearer end.

## 40. Four ways to walk a tree

[Trees and ranges: read the lesson](../../data-structures/trees/binary-trees.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/tree-traversals-dark.svg"><img src="assets/tree-traversals.svg" alt="A binary search tree built from 8, 3, 10, 1, 6, 14, 4 and 7 has root 8, leaves 1, 4, 7 and 14, and height 3. Pre-order reads 8 3 1 6 4 7 10 14, in-order reads the keys sorted, post-order ends at the root 8, and level order reads one depth at a time." width="520"></picture>

**Key idea:** Pre, in and post order differ only in when the root is read.

## 41. Delete with two children

[Trees and ranges: read the lesson](../../data-structures/trees/binary-search-trees.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/bst-delete-successor-dark.svg"><img src="assets/bst-delete-successor.svg" alt="Deleting key 2 from a BinarySearchTree whose root 2 has children 1 and 4, with 3 below 4, copies the in-order successor 3 into the root and unlinks the old 3. The in-order keys become 1, 3, 4 and the height drops from 2 to 1." width="520"></picture>

**Key idea:** The successor's key replaces the deleted one; its old node is unlinked.

## 42. Red links make 3-nodes

[Trees and ranges: read the lesson](../../data-structures/trees/red-black-trees.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/red-black-23-nodes-dark.svg"><img src="assets/red-black-23-nodes.svg" alt="After put 1 to 5, a RedBlackTreeMap has black root 4 with red left child 2, black children 1 and 3 under 2, and black 5. Read as a 2-3 tree it is the 3-node [2 4] over the 2-nodes [1], [3] and [5]; the height is 2 as a binary tree and 1 as a 2-3 tree." width="520"></picture>

**Key idea:** A red link and its black parent make one 3-node.

## 43. Three local repairs

[Trees and ranges: read the lesson](../../data-structures/trees/red-black-trees.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/red-black-fixups-dark.svg"><img src="assets/red-black-fixups.svg" alt="Rotate left turns black 1 with a red right child 2 into black 2 with a red left child 1. Rotate right turns black 3 over red 2 over red 1 into black 2 with red children 1 and 3. Flip colors turns black 2 with red children into red 2 with black children." width="520"></picture>

**Key idea:** Rotations keep sorted order; a flip splits a 4-node.

## 44. One insertion step

[Sorting and searching: read the lesson](../../algorithms/sorting/insertion-sort.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/insertion-sort-shift-dark.svg"><img src="assets/insertion-sort-shift.svg" alt="In 3, 5, 8, 9, 4, 1 the prefix 3, 5, 8, 9 is sorted. The key 4 is saved, the three larger values 5, 8 and 9 shift one slot right, and 4 drops into index 1." width="520"></picture>

**Key idea:** Insertion sort shifts only the larger values one slot right, then drops the saved key into the gap, so the sorted prefix grows by one.

## 45. Heapsort phases

[Sorting and searching: read the lesson](../../algorithms/sorting/heap-sort.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/heapsort-phases-dark.svg"><img src="assets/heapsort-phases.svg" alt="Heapsort on 3, 1, 6, 5, 2, 4 builds the max-heap 6, 5, 4, 1, 2, 3, then moves 6 and 5 to the end, giving 5, 3, 4, 1, 2, 6 and 4, 3, 2, 1, 5, 6, until 1 to 6 is sorted." width="520"></picture>

**Key idea:** Heapsort builds a max-heap, then swaps the root to the end and repairs the shorter heap; the sorted suffix grows by one each time.

## 46. Counting sort tally

[Sorting and searching: read the lesson](../../algorithms/sorting/non-comparison-and-comparator-sorts.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/counting-sort-counts-dark.svg"><img src="assets/counting-sort-counts.svg" alt="For 5, 3, 6, 3, 4, 6 the range 3 to 6 has four counts: 2, 1, 1 and 2. Writing each value back that many times gives 3, 3, 4, 5, 6, 6." width="520"></picture>

**Key idea:** Counting sort never compares two values: it tallies each value, then writes each value back as many times as it was counted.

## 47. Radix passes by digit

[Sorting and searching: read the lesson](../../algorithms/sorting/non-comparison-and-comparator-sorts.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/radix-lsd-passes-dark.svg"><img src="assets/radix-lsd-passes.svg" alt="Sorting 35, 21, 41, 15, 31, 25 by the ones digit gives 21, 41, 31, 35, 15, 25; a stable pass on the tens digit then gives 15, 21, 25, 31, 35, 41." width="520"></picture>

**Key idea:** Radix sort orders by the least significant digit first; because every pass is stable, ties keep the earlier order and the last pass leaves a full sort.

## 48. Stable and unstable outcomes

[Sorting and searching: read the lesson](../../algorithms/sorting/non-comparison-and-comparator-sorts.md)

<picture><source media="(prefers-color-scheme: dark)" srcset="assets/sort-stability-dark.svg"><img src="assets/sort-stability.svg" alt="Records 3a, 1b, 3c, 2d, 1e, 3f sorted by their number: a stable sort gives 1b, 1e, 2d, 3a, 3c, 3f, keeping tags in order, while a hypothetical unstable outcome 1e, 1b, 2d, 3c, 3a, 3f reorders equal keys." width="520"></picture>

**Key idea:** A stable sort keeps equal keys in their input order; an unstable sort may reorder them, losing any earlier ordering by tag.
