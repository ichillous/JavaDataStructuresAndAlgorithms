# Data structures

These lessons explain how each structure stores its data, the invariant that keeps it correct, what each operation costs, and which maintained Java class implements it. Start from the question you need answered most often; the map below names the family that answers it, a method to start with and the folder to open.

| At a glance | |
| --- | --- |
| **Use it when** | You must decide how to store data so that the question you ask most (by position, arrival, key, order, priority, prefix, range, connectivity, or approximately) stays cheap. |
| **Avoid it when** | You need a procedure rather than a container: searching, sorting, graph methods and dynamic programming live in [algorithms](../algorithms/README.md). |
| **Cost** | Each lesson states its bounds; the [complexity guide](../docs/complexity.md) collects them for every maintained class. |
| **In this repo** | The [`collections`](../src/main/java/io/github/ichillous/dsa/collections), [`structures`](../src/main/java/io/github/ichillous/dsa/structures) and [`graphs`](../src/main/java/io/github/ichillous/dsa/graphs) packages. |
| **In the JDK** | `java.util` collections such as `ArrayList`, `ArrayDeque`, `HashMap`, `TreeMap` and `PriorityQueue`; each lesson names the closest one. |

<!-- visual:generated data-structures-map | Each card pairs a question with the family that answers it, a method to start with and the folder to open. -->
<picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/data-structures-map-dark.svg"><img src="../docs/visuals/assets/data-structures-map.svg" alt="9 cards sort the structures by the question they answer. Each card names the family, a method to start with and the folder to open: by position, DynamicArray.get, arrays/ and linked-lists/; by arrival, RingDeque.removeFirst, stacks/ and queues/; by key, ChainedHashMap.get, hash-tables/; by order, RedBlackTreeMap.floorKey, trees/; by priority, BinaryMinHeap.remove, heaps/; by prefix, Trie.wordsWithPrefix, tries/; by range, FenwickTree.rangeSum, range-queries/; by connectivity, UnionFind.connected, union-find/ and graphs/; approximately, BloomFilter.mightContain, probabilistic/." width="520"></picture>

**Figure 52.** Each card pairs a question with the family that answers it, a method to start with and the folder to open. [Visual index and step-throughs](../docs/visuals/README.md).
<!-- /visual:generated -->

## Start here

1. [Arrays: position is the address](arrays/overview.md): contiguous storage and amortized growth, which most other structures build on.
2. [Hash tables: separate equality from position](hash-tables/overview.md): the default for lookups by key, and the base of the LRU and LFU caches.
3. [Trees: choose the invariant before the shape](trees/overview.md): how ordered keys, priorities, prefixes and ranges each call for a different tree.
4. [Binary heaps: a partial order is enough](heaps/overview.md): the structure behind priority queues, Dijkstra's algorithm and top-k.
5. [Graphs: model relationships explicitly](graphs/overview.md): vertices, edges and representations, the entry point to the graph algorithms.

## Pick by problem

| If you need to | Read | Solution finder |
| :--- | :--- | :--- |
| Read or replace the item at an index, and append cheaply | [Arrays](arrays/overview.md) | none |
| Handle items oldest first (queue) or newest first (stack) | [Queues](queues/overview.md), [stacks](stacks/overview.md) | [Stack or queue](../docs/solution-finder.md#last-in-first-out-stack-or-first-in-first-out-queue) |
| Find a value by key when order does not matter | [Hash tables](hash-tables/overview.md) | [Look up by key](../docs/solution-finder.md#find-a-value-by-key-when-order-does-not-matter) |
| Keep the most recently used entries | [Hash tables](hash-tables/overview.md) | [LRU](../docs/solution-finder.md#keep-the-most-recently-used-entries-lru) |
| Evict the least frequently used entry | [LFU cache](hash-tables/lfu-cache.md) | [LFU](../docs/solution-finder.md#evict-the-least-frequently-used-entry) |
| Find the nearest key, or count keys in a range | [Red-black trees](trees/red-black-trees.md) | [Nearest key](../docs/solution-finder.md#find-the-nearest-key-floor-ceiling-or-scan-a-key-range) |
| Always take the smallest item next | [Binary heaps](heaps/overview.md) | [Smallest next](../docs/solution-finder.md#always-take-the-smallest-or-largest-item-next) |
| List every word that starts with a prefix | [Tries](tries/overview.md) | [Prefix search](../docs/solution-finder.md#find-all-keys-that-start-with-a-prefix-autocomplete) |
| Answer range sums while values change | [Range queries](range-queries/overview.md) | [Range queries](../docs/solution-finder.md#update-values-or-ranges-while-answering-range-sums) |
| Ask whether two items are in the same group as groups merge | [Union-find](union-find/overview.md) | [Disjoint sets](../docs/solution-finder.md#merge-groups-and-ask-whether-two-items-are-in-the-same-group) |
| Ask "have I seen this item?" with little memory | [Bloom filter and Count-Min sketch](probabilistic/overview.md) | [Approximate membership](../docs/solution-finder.md#ask-have-i-seen-this-item-with-limited-memory) |
| Choose between an adjacency list and a matrix | [Graph representations](graphs/representation.md) | none |

## Every lesson

Generated from each lesson's first figure, title and "Use it when" row.

<!-- visual:gallery data-structures -->
### Arrays

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="arrays/operations.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/array-insertion-dark.svg"><img src="../docs/visuals/assets/array-insertion.svg" alt="Shift the suffix before writing the new value into the live array." width="160"></picture></a> | [Array operations: move the right elements](arrays/operations.md) | You need ordered, indexed storage that grows, and you can pay O(n) for an insertion or removal away from the end. |
| <a href="arrays/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/array-vs-list-access-dark.svg"><img src="../docs/visuals/assets/array-vs-list-access.svg" alt="In a DynamicArray of eight values, get(5) reads slot 5 in one jump. In a DoublyLinkedList of the same values it starts at the tail and follows 2 links, because 5 is in the back half; the hop counts for indices 0 to 7 are 0, 1, 2, 3, 3, 2, 1 and 0." width="160"></picture></a> | [Arrays: position is the address](arrays/overview.md) | Positions matter and you read or replace by index far more often than you insert or remove in the middle. |

### Graphs

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="graphs/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/shortest-path-chooser-dark.svg"><img src="../docs/visuals/assets/shortest-path-chooser.svg" alt="A column of seven yes-or-no questions from all pairs, unweighted, 0-1 weights, DAG, negative weights and heuristic to nonnegative weights, each paired with the algorithm and the repository method that answers it." width="160"></picture></a> | [Graphs: model relationships explicitly](graphs/overview.md) | Entities and relationships form a network: routes, prerequisites, links between accounts. |
| <a href="graphs/representation.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/graph-representations-dark.svg"><img src="../docs/visuals/assets/graph-representations.svg" alt="One directed graph with 4 vertices and the edges 0 to 1, 0 to 2, 2 to 1 and 2 to 3, shown as an adjacency list, a 4 by 4 adjacency matrix and an edge list, each with the cost of testing an edge and listing neighbors." width="160"></picture></a> | [Graph representations: pay for the operations you need](graphs/representation.md) | You must decide how edges are stored before writing algorithms: a list when you scan neighbors, a matrix when you test arbitrary edges. |
| <a href="graphs/traversal.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/bfs-vs-dfs-order-dark.svg"><img src="../docs/visuals/assets/bfs-vs-dfs-order.svg" alt="A 6-vertex undirected graph from source 0: breadthFirst visits 0, 1, 2, 3, 4, 5 in layers of distance 0, 1 and 2, while depthFirst visits 0, 1, 3, 4, 5, 2 by following one branch to its end." width="160"></picture></a> | [Graph traversal: schedule each vertex deliberately](graphs/traversal.md) | You need what is reachable, the fewest edges to a target, connected components or a dependency order. |

### Hash tables

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="hash-tables/collision-resolution.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/hash-chaining-dark.svg"><img src="../docs/visuals/assets/hash-chaining.svg" alt="Equal hash codes select one bucket; key equality still decides a match." width="160"></picture></a> | [Collision resolution: never confuse a bucket with a key](hash-tables/collision-resolution.md) | Choosing or reviewing how a hash table resolves two keys that select the same bucket or slot. |
| <a href="hash-tables/functions.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/hash-chaining-dark.svg"><img src="../docs/visuals/assets/hash-chaining.svg" alt="Equal hash codes select one bucket; key equality still decides a match." width="160"></picture></a> | [Hash functions and key contracts](hash-tables/functions.md) | You define a key type, or need to understand how a hash code becomes a bucket or slot index. |
| <a href="hash-tables/lfu-cache.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/lfu-buckets-dark.svg"><img src="../docs/visuals/assets/lfu-buckets.svg" alt="A capacity-3 LfuCache after put A, B, C and get C holds A and B in bucket 1 and C in bucket 2. get(B) moves B to the end of bucket 2, and put(D) then evicts A, the oldest key of bucket 1." width="160"></picture></a> | [LFU cache: evict the least used, break ties by recency](hash-tables/lfu-cache.md) | A few keys are used far more often than the rest and a one-time scan over cold keys must not flush them. |
| <a href="hash-tables/open-addressing.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/open-addressing-probe-dark.svg"><img src="../docs/visuals/assets/open-addressing-probe.svg" alt="In an 8-slot table holding K, B and G, put(L) finds home slot 7 taken by G, wraps to slot 0 and stores L there; get(T), also homed at 7, visits slots 7, 0 and 1 and stops at the empty slot 1." width="160"></picture></a> | [Open addressing: linear probing with backward-shift deletion](hash-tables/open-addressing.md) | You want a hash map with no per-entry nodes, many small entries, and neighboring slots that share cache lines. |
| <a href="hash-tables/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/lru-recency-dark.svg"><img src="../docs/visuals/assets/lru-recency.svg" alt="Promote a cache hit before deciding which entry to evict next." width="160"></picture></a> | [Hash tables: separate equality from position](hash-tables/overview.md) | You look entries up by key and need no ordering: membership, counting, indexing, caching. |

### Heaps

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="heaps/operations.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/heap-anatomy-dark.svg"><img src="../docs/visuals/assets/heap-anatomy.svg" alt="Bottom-up heapify turns 9,4,7,1,3,2 into the array 1,3,2,4,9,7. The matching complete binary tree labels every zero-based array slot and keeps every parent no greater than its children." width="160"></picture></a> | [Heap operations: restore order along one path](heaps/operations.md) | You need to understand or review how a binary heap restores order after add, remove and bulk construction. |
| <a href="heaps/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/heap-anatomy-dark.svg"><img src="../docs/visuals/assets/heap-anatomy.svg" alt="Bottom-up heapify turns 9,4,7,1,3,2 into the array 1,3,2,4,9,7. The matching complete binary tree labels every zero-based array slot and keeps every parent no greater than its children." width="160"></picture></a> | [Binary heaps: a partial order is enough](heaps/overview.md) | You repeatedly need the smallest (or largest) of a changing collection: scheduling, event simulation, Dijkstra, the best `k` of a stream. |

### Linked lists

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="linked-lists/doubly.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/linked-unlink-dark.svg"><img src="../docs/visuals/assets/linked-unlink.svg" alt="Bypass the middle node in both directions, then clear its links." width="160"></picture></a> | [Doubly linked lists: repair both neighbors](linked-lists/doubly.md) | You need O(1) changes at both ends or O(1) removal of a node you hold, as in an LRU recency list. |
| <a href="linked-lists/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/linked-unlink-dark.svg"><img src="../docs/visuals/assets/linked-unlink.svg" alt="Bypass the middle node in both directions, then clear its links." width="160"></picture></a> | [Linked lists: preserve connections](linked-lists/overview.md) | You work at the ends or at nodes you already hold, as in queues, deques and recency lists. |
| <a href="linked-lists/singly.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/singly-insert-after-dark.svg"><img src="../docs/visuals/assets/singly-insert-after.svg" alt="In the chain 10, 20, 30, inserting 15 after 10 takes two link writes: 15.next = 10.next, then 10.next = 15, giving 10, 15, 20, 30. Writing 10.next = 15 first makes 15.next point at 15 itself and leaves 20 and 30 unreachable." width="160"></picture></a> | [Singly linked lists: one direction, explicit predecessors](linked-lists/singly.md) | You only walk forward and edit at the head, or you keep a tail for appends and never remove from the back. |

### Probabilistic structures

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="probabilistic/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/bloom-filter-bits-dark.svg"><img src="../docs/visuals/assets/bloom-filter-bits.svg" alt="A 16-bit Bloom filter with two probes per key sets bits 8, 11, 1 and 3 for apple and pear. kiwi reads bits 8 and 7 and bit 7 is clear, a definite miss; plum reads bits 1 and 11, both set, a false positive." width="160"></picture></a> | [Probabilistic membership and frequency: Bloom filter and Count-Min sketch](probabilistic/overview.md) | Exact answers cost too much memory and a small one-sided error is acceptable: skip a disk lookup for most absent keys (Bloom filter), or track approximate counts in a huge key space (Count-Min sketch). |

### Queues

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="queues/implementation.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/queue-wrap-dark.svg"><img src="../docs/visuals/assets/queue-wrap.svg" alt="The head moves around the buffer while FIFO order stays intact." width="160"></picture></a> | [Circular queues and deques](queues/implementation.md) | You need FIFO order, or both-end insertion and removal, without per-element node allocation. |
| <a href="queues/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/queue-wrap-dark.svg"><img src="../docs/visuals/assets/queue-wrap.svg" alt="The head moves around the buffer while FIFO order stays intact." width="160"></picture></a> | [Queues: process in arrival order](queues/overview.md) | Earlier arrivals must be handled first: breadth-first traversal, buffering, simple task processing. |
| <a href="queues/priority-queues.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/heap-anatomy-dark.svg"><img src="../docs/visuals/assets/heap-anatomy.svg" alt="Bottom-up heapify turns 9,4,7,1,3,2 into the array 1,3,2,4,9,7. The matching complete binary tree labels every zero-based array slot and keeps every parent no greater than its children." width="160"></picture></a> | [Priority queues: remove the best candidate](queues/priority-queues.md) | The next item to process is the one with the best priority, not the oldest: scheduling, Dijkstra, event simulation, top-`k`. |

### Range queries

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="range-queries/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/fenwick-blocks-dark.svg"><img src="../docs/visuals/assets/fenwick-blocks.svg" alt="For values 2,-1,4,3, one-based Fenwick slots cover [0,1), [0,2), [2,3) and [0,4). The prefix ending at zero-based boundary 3 combines slots 3 and 2: 4+1=5." width="160"></picture></a> | [Range queries: sparse tables and lazy segment trees](range-queries/overview.md) | Many range queries (sum or minimum) hit the same array, so some build work is worth paying for. |

### Stacks

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="stacks/implementation.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/stack-bracket-matching-dark.svg"><img src="../docs/visuals/assets/stack-bracket-matching.svg" alt="An ArrayStack traces {[()]}: three pushes and three pops leave it empty. The same stack traced on ([)] holds ( and [ when the closer ) arrives, and the top [ does not match it, so the string is rejected at the third character." width="160"></picture></a> | [Implementing a stack by composition](stacks/implementation.md) | You want LIFO access over growable storage and the empty-state, bounds and growth rules to be inspectable. |
| <a href="stacks/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/stack-lifo-dark.svg"><img src="../docs/visuals/assets/stack-lifo.svg" alt="Push and pop share the top; the newest value leaves first." width="160"></picture></a> | [Stacks: the most recent unfinished task](stacks/overview.md) | The most recent unfinished item must be handled first: nested brackets, undo history, explicit DFS or traversal frames. |

### Trees

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="trees/avl-trees.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/avl-rotation-dark.svg"><img src="../docs/visuals/assets/avl-rotation.svg" alt="Inserting 30, 20 and 10 produces a left-heavy chain. A right rotation makes 20 the root with children 10 and 30, preserving in-order traversal and restoring height one." width="160"></picture></a> | [AVL trees: restore balance locally](trees/avl-trees.md) | Lookups dominate and you want the shallowest guaranteed height, with membership and sorted traversal only |
| <a href="trees/binary-search-trees.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/bst-delete-successor-dark.svg"><img src="../docs/visuals/assets/bst-delete-successor.svg" alt="Deleting key 2 from a BinarySearchTree whose root 2 has children 1 and 4, with 3 below 4, copies the in-order successor 3 into the root and unlinks the old 3. The in-order keys become 1, 3, 4 and the height drops from 2 to 1." width="160"></picture></a> | [Binary search trees: an invariant over descendants](trees/binary-search-trees.md) | Keys are inserted and removed over time and you need ordered membership or sorted traversal, and the input order is not adversarial |
| <a href="trees/binary-trees.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/tree-traversals-dark.svg"><img src="../docs/visuals/assets/tree-traversals.svg" alt="A binary search tree built from 8, 3, 10, 1, 6, 14, 4 and 7 has root 8, leaves 1, 4, 7 and 14, and height 3. Pre-order reads 8 3 1 6 4 7 10 14, in-order reads the keys sorted, post-order ends at the root 8, and level order reads one depth at a time." width="160"></picture></a> | [Binary trees: shape and traversal are separate choices](trees/binary-trees.md) | You model a hierarchy with at most two children per node, such as an expression tree, and need to name traversal orders precisely |
| <a href="trees/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/tree-traversals-dark.svg"><img src="../docs/visuals/assets/tree-traversals.svg" alt="A binary search tree built from 8, 3, 10, 1, 6, 14, 4 and 7 has root 8, leaves 1, 4, 7 and 14, and height 3. Pre-order reads 8 3 1 6 4 7 10 14, in-order reads the keys sorted, post-order ends at the root 8, and level order reads one depth at a time." width="160"></picture></a> | [Trees: choose the invariant before the shape](trees/overview.md) | You must pick a tree: ordered keys, repeated minimum, prefixes or interval queries each need a different invariant |
| <a href="trees/red-black-trees.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/red-black-23-nodes-dark.svg"><img src="../docs/visuals/assets/red-black-23-nodes.svg" alt="After put 1 to 5, a RedBlackTreeMap has black root 4 with red left child 2, black children 1 and 3 under 2, and black 5. Read as a 2-3 tree it is the 3-node [2 4] over the 2-nodes [1], [3] and [5]; the height is 2 as a binary tree and 1 as a 2-3 tree." width="160"></picture></a> | [Red-black trees: a 2-3 tree in binary form](trees/red-black-trees.md) | Keys change while you keep asking ordered questions: nearest key, rank, select or range counts |

### Tries

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="tries/implementation.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/trie-pruning-dark.svg"><img src="../docs/visuals/assets/trie-pruning.svg" alt="The words car, cart and cat share a c-a prefix. Deleting car clears the r node&#x27;s word marker but preserves its t child for cart and the sibling t branch for cat." width="160"></picture></a> | [Trie updates: prune paths without deleting other words](tries/implementation.md) | You need to know exactly how a trie inserts, prunes on delete and collects suggestions with a limit. |
| <a href="tries/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/trie-pruning-dark.svg"><img src="../docs/visuals/assets/trie-pruning.svg" alt="The words car, cart and cat share a c-a prefix. Deleting car clears the r node&#x27;s word marker but preserves its t child for cart and the sibling t branch for cat." width="160"></picture></a> | [Tries: share prefixes, mark whole words separately](tries/overview.md) | Prefix queries such as autocomplete are central, over a set of strings that changes. |

### Union-find

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="union-find/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/union-find-dark.svg"><img src="../docs/visuals/assets/union-find.svg" alt="Union operations on five indices produce components {0,1,2} and {3,4}. Parent arrows point toward roots 0 and 3, whose component sizes are three and two. Finding index 2 follows its link to root 0." width="160"></picture></a> | [Union-find: merge groups, ask who is together](union-find/overview.md) | Groups only merge and you ask "are these connected?": Kruskal's algorithm, cycle detection while adding undirected edges, grouping equivalent items. |

<!-- /visual:gallery -->

See also the [solution finder](../docs/solution-finder.md), the [implementation catalog](../docs/catalog.md), the [complexity guide](../docs/complexity.md) and the [visual index](../docs/visuals/README.md).
