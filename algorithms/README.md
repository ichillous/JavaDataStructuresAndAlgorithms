# Algorithms

These lessons teach the procedures that work on data: searching, sorting, selection, graph methods, dynamic programming, greedy choice, backtracking, string matching and number theory. Each one states its precondition and invariant, traces a concrete example and links the maintained Java method, so name the task first and the map below shows where to go.

| At&nbsp;a&nbsp;glance | |
| --- | --- |
| **Use&nbsp;it&nbsp;when** | You know the task (find, order, select, traverse, optimize, match or compute) and must choose the method whose precondition your input meets. |
| **Avoid&nbsp;it&nbsp;when** | You are still choosing how to store the data: start with [data structures](../data-structures/README.md). |
| **Cost** | Each lesson states time and extra space; the [complexity guide](../docs/complexity.md) collects them for every maintained method. |
| **In&nbsp;this&nbsp;repo** | The [`algorithms`](../src/main/java/io/github/ichillous/dsa/algorithms) and [`graphs`](../src/main/java/io/github/ichillous/dsa/graphs) packages, such as `SearchAlgorithms`, `SortAlgorithms` and `GraphAlgorithms`. |
| **In&nbsp;the&nbsp;JDK** | `Arrays.sort`, `Arrays.binarySearch`, `Collections.sort`, `String.indexOf` and `BigInteger.modPow`; no direct equivalent for graph algorithms or dynamic programming. |

<!-- visual:generated algorithms-map | Each card pairs a task with its family, a method to start with and the folders to open. -->
<picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/algorithms-map-dark.svg"><img src="../docs/visuals/assets/algorithms-map.svg" alt="9 cards sort the algorithms by the task they perform. Each card shows a small pictogram, names the family, a method to start with and the folder to open: search, SearchAlgorithms.lowerBound, searching/ and array-patterns/; sort, SortAlgorithms.mergeSort, sorting/ and divide-and-conquer/; select, Selection.kthSmallest, searching/; traverse, GraphAlgorithms.breadthFirst, graph/; shortest path, GraphAlgorithms.dijkstra, graph/; connect, GraphAlgorithms.minimumSpanningForest, graph/; optimize, DynamicProgramming.knapsack01, greedy/ and backtracking/ and dynamic-programming/; match text, StringAlgorithms.kmpFindAll, strings/; compute, NumberTheory.gcd, math/ and bit-manipulation/." width="520"></picture>

**Figure 53.** Each card pairs a task with its family, a method to start with and the folders to open. [Visual index and step-throughs](../docs/visuals/README.md).
<!-- /visual:generated -->

## Start here

1. [Recurrences, amortization and honest cost models](complexity-analysis/time-complexity.md): the cost vocabulary every other lesson uses.
2. [Binary search as a boundary problem](searching/binary-search.md): the invariant-first style behind `lowerBound` and binary search on the answer.
3. [Choosing a sort](sorting/README.md): when to use a repository sort or a JDK sort for a concrete input.
4. [Choosing a graph algorithm](graph/README.md): picks the graph method by question, precondition and cost.
5. [Dynamic programming starts with a state definition](dynamic-programming/overview.md): how to define a state before writing a recurrence.

## Pick by problem

| If you need to | Read | Solution finder |
| :--- | :--- | :--- |
| Find a value or its insertion point in sorted data | [Binary search](searching/binary-search.md) | [Sorted search](../docs/solution-finder.md#find-a-value-or-its-insertion-point-in-sorted-data) |
| Find the smallest value that makes a condition true | [Binary search on the answer](searching/binary-search-on-answer.md) | [Search the answer](../docs/solution-finder.md#find-the-smallest-value-that-makes-a-condition-true) |
| Find the k-th smallest item, such as a median | [Selection](searching/selection.md) | [k-th smallest](../docs/solution-finder.md#find-the-k-th-smallest-item-median-percentile) |
| Sort objects by a key and keep equal keys in order | [Counting, radix and stable sorts](sorting/non-comparison-and-comparator-sorts.md) | [Stable sort](../docs/solution-finder.md#sort-objects-by-a-key-keeping-equal-keys-in-their-original-order) |
| Find the longest window that satisfies a condition | [Array patterns](array-patterns/overview.md) | [Windows](../docs/solution-finder.md#find-the-longest-or-shortest-window-that-satisfies-a-condition) |
| Visit everything reachable, or count fewest edges | [Breadth-first search](graph/bfs.md) | [Reachability](../docs/solution-finder.md#visit-everything-reachable-from-a-start-vertex) |
| Order tasks that depend on each other | [Topological sort](graph/topological-sort.md) | [Dependencies](../docs/solution-finder.md#order-tasks-that-depend-on-each-other) |
| Find the cheapest path from one source | [Shortest paths](graph/shortest-paths.md) | [One source](../docs/solution-finder.md#from-one-source) |
| Connect every vertex at the lowest total cost | [Minimum spanning forests](graph/minimum-spanning-trees.md) | [Spanning trees](../docs/solution-finder.md#connect-every-vertex-at-the-lowest-total-cost) |
| Send the most flow through a network, or match two sides | [Max flow and matching](graph/flows-and-matching.md) | [Max flow](../docs/solution-finder.md#find-the-maximum-flow-and-the-minimum-cut) |
| Choose items under a capacity | [Dynamic-programming patterns](dynamic-programming/problems.md) | [Knapsack](../docs/solution-finder.md#choose-items-under-a-capacity) |
| Find every occurrence of one pattern in a text | [Pattern matching](strings/pattern-matching.md) | [One pattern](../docs/solution-finder.md#find-one-pattern-in-a-text) |
| Compute a gcd, a modular power or the primes up to n | [Number theory](math/number-theory.md) | [Modular arithmetic](../docs/solution-finder.md#divisibility-modular-arithmetic-and-primes) |

## Every lesson

Generated from each lesson: a figure from the lesson (one not already shown above it when the lesson has one), its title and its "Use it when" row.

<!-- visual:gallery algorithms -->
### Array patterns

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="array-patterns/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/two-pointers-pair-sum-dark.svg"><img src="../docs/visuals/assets/two-pointers-pair-sum.svg" alt="pairWithSum on 1, 2, 3, 4, 6 with target 6 starts at the two ends: 1 + 6 is too large so the right index drops, 1 + 4 is too small so the left index drops, and 2 + 4 matches at indices 1 and 3." width="160"></picture></a> | [Array patterns: pointers, windows, stacks, differences and sweeps](array-patterns/overview.md) | One left-to-right scan can replace nested loops: pair sums in sorted data, longest windows, next-greater queries, many range updates, overlapping intervals. |

### Backtracking

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="backtracking/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/queens-backtracking-dark.svg"><img src="../docs/visuals/assets/queens-backtracking.svg" alt="Trace failed placements before reaching the first valid board." width="160"></picture></a> | [Backtracking: choose, explore, undo](backtracking/overview.md) | You need valid combinations or one witness under constraints that can reject a partial solution early. |
| <a href="backtracking/problems.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/subset-sum-halves-dark.svg"><img src="../docs/visuals/assets/subset-sum-halves.svg" alt="Six values are split into two halves of three; the sums of all eight subsets of each half are listed, and the first right sum whose complement is stored on the left selects indices 0, 1 and 3 for target 6." width="160"></picture></a> | [Constraint puzzles and signed subset sum](backtracking/problems.md) | You need one valid solution to a constraint puzzle (Sudoku) or a subset hitting an exact target. |

### Bit manipulation

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="bit-manipulation/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/bitmask-subsets-dark.svg"><img src="../docs/visuals/assets/bitmask-subsets.svg" alt="The set {1, 3} of four elements is the int mask 1010 = 10. Testing bit 3 gives 1, setting bit 2 gives 1110, clearing bit 3 gives 0010, and (sub - 1) &amp; mask walks the subsets 10, 8, 2 and 0." width="160"></picture></a> | [Bit manipulation: masks, subsets and exact shifts](bit-manipulation/overview.md) | A set has at most 32 or 64 members, or you need parity, lowest-set-bit, or subset enumeration. |

### Complexity analysis

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="complexity-analysis/space-complexity.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/merge-recursion-dark.svg"><img src="../docs/visuals/assets/merge-recursion.svg" alt="Split into singletons, then merge sorted children bottom up." width="160"></picture></a> | [Practical memory contracts: capacity, snapshots and search](complexity-analysis/space-complexity.md) | You must say what memory an operation needs beyond its input: workspace, retained capacity, output and the recursion stack. |
| <a href="complexity-analysis/time-complexity.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/complexity-growth-dark.svg"><img src="../docs/visuals/assets/complexity-growth.svg" alt="Five illustrative functions share a value of one at input size one. A logarithmic work axis shows logarithmic, linear, n log n, quadratic and exponential growth without implying benchmark timings." width="160"></picture></a> | [Recurrences, amortization and honest cost models](complexity-analysis/time-complexity.md) | You need to say how work grows with input size, by solving a recurrence or by amortized accounting. |

### Divide and conquer

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="divide-and-conquer/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/quick-partition-dark.svg"><img src="../docs/visuals/assets/quick-partition.svg" alt="Gather duplicate pivot values into one finished equal region." width="160"></picture></a> | [Divide and conquer: define what comes back](divide-and-conquer/overview.md) | Subproblems are independent and a cheap combine rule exists: sorting, counting cross pairs, searching a sorted range. |
| <a href="divide-and-conquer/problems.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/inversion-count-merge-dark.svg"><img src="../docs/visuals/assets/inversion-count-merge.svg" alt="countInversions on 2, 4, 1, 3, 5 merges the sorted halves 2, 4 and 1, 3, 5: emitting 1 jumps over two left values, emitting 3 jumps over one, and the other values add nothing, for 3 inversions." width="160"></picture></a> | [Array problems: combine, extend or discard](divide-and-conquer/problems.md) | You can state an invariant that lets one pass reuse earlier work: a merge counts cross pairs, a running sum extends a range, a deque keeps useful candidates. |

### Dynamic programming

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="dynamic-programming/README.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/lcs-traceback-dark.svg"><img src="../docs/visuals/assets/lcs-traceback.svg" alt="The longest-common-subsequence table for GCTA and TGCAT, with the path longestCommonSubsequence walks back from the corner: it goes up on the tie at the corner, takes T, C and G on equal letters, and returns GCT." width="160"></picture></a> | [Recognize the DP family](dynamic-programming/README.md) | You face an optimization or counting problem and need to pick a state shape quickly. |
| <a href="dynamic-programming/classic-problems.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/matrix-chain-splits-dark.svg"><img src="../docs/visuals/assets/matrix-chain-splits.svg" alt="The cost table for four matrices with dimensions 30, 1, 40, 10, 25, the three candidate splits of the whole chain, and the stored splits that give (A1((A2A3)A4)) at cost 1400." width="160"></picture></a> | [Classic dynamic programs](dynamic-programming/classic-problems.md) | You need exact answers for counting, segmentation, bracketing, tours or weighted scheduling. |
| <a href="dynamic-programming/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/coin-dp-dark.svg"><img src="../docs/visuals/assets/coin-dp.svg" alt="Compare every final coin; the largest-first greedy choice loses." width="160"></picture></a> | [Dynamic programming starts with a state definition](dynamic-programming/overview.md) | A solution is built from smaller states that recur, and one state fits in a sentence. |
| <a href="dynamic-programming/problems.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/knapsack-state-dark.svg"><img src="../docs/visuals/assets/knapsack-state.svg" alt="Sweep capacities backward so an item cannot reuse itself." width="160"></picture></a> | [Dynamic-programming patterns and their boundaries](dynamic-programming/problems.md) | You need the best value over a sequence or two strings: knapsack, LCS, edit distance, LIS. |

### Graph algorithms

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="graph/README.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/articulation-points-dark.svg"><img src="../docs/visuals/assets/articulation-points.svg" alt="A bowtie graph of two triangles sharing vertex 2 is searched depth-first from 0 with discovery and low values shown, and removing vertex 2 leaves the separate components {0, 1} and {3, 4}." width="160"></picture></a> | [Choosing a graph algorithm](graph/README.md) | You have a graph problem and want the right method, its precondition and its cost before you read a lesson. |
| <a href="graph/bfs.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/bfs-frontier-dark.svg"><img src="../docs/visuals/assets/bfs-frontier.svg" alt="Follow the queue separately from discovery; isolate unreachable vertex 5." width="160"></picture></a> | [Breadth-first search and choosing a distance model](graph/bfs.md) | You need reachability, fewest-edge paths, distance layers or undirected components. |
| <a href="graph/dfs.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/bfs-vs-dfs-order-dark.svg"><img src="../docs/visuals/assets/bfs-vs-dfs-order.svg" alt="A 6-vertex undirected graph from source 0: breadthFirst visits 0, 1, 2, 3, 4, 5 in layers of distance 0, 1 and 2, while depthFirst visits 0, 1, 3, 4, 5, 2 by following one branch to its end." width="160"></picture></a> | [Depth-first search, finishing order and connectivity](graph/dfs.md) | You need to explore one branch fully: reachability, finishing order, SCCs, bridges and other structure. |
| <a href="graph/flows-and-matching.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/max-flow-min-cut-dark.svg"><img src="../docs/visuals/assets/max-flow-min-cut.svg" alt="A four-vertex flow network from source 0 to sink 3 carries a maximum flow of 5. The source side {0, 1} is the set reachable in the residual graph, and the three edges leaving it have capacities 2, 1 and 2, summing to 5." width="160"></picture></a> | [Max flow, min cut and bipartite matching](graph/flows-and-matching.md) | You must route the most of something through capacity-limited links, find the cheapest set of links separating two vertices, or pair items from two sides (tasks to workers). |
| <a href="graph/graph-structure.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/bipartite-odd-cycle-dark.svg"><img src="../docs/visuals/assets/bipartite-odd-cycle.svg" alt="A 6-cycle splits into left {0, 2, 4} and right {1, 3, 5}, while a 5-cycle with a tail has an edge 2-3 joining two vertices of the same colour, and the witness is the odd cycle 2, 1, 0, 4, 3." width="160"></picture></a> | [Graph structure: cut vertices, bipartiteness, cycles and Euler paths](graph/graph-structure.md) | You need to know which vertices hold a graph together, whether it splits into two sides, where a cycle is, or whether one walk can use every edge once. |
| <a href="graph/minimum-spanning-trees.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/mst-kruskal-prim-dark.svg"><img src="../docs/visuals/assets/mst-kruskal-prim.svg" alt="One weighted graph on five vertices has a minimum spanning tree of total weight 11. Kruskal keeps edges in the order 1, 2, 3, 5 and skips two that close a cycle, while Prim from vertex 0 picks 1, 2, 5, 3." width="160"></picture></a> | [Minimum spanning forests: Kruskal and Prim](graph/minimum-spanning-trees.md) | You must connect every vertex as cheaply as possible and the connection need not be a shortest path between any two of them: networks, clustering, cheapest wiring. |
| <a href="graph/shortest-paths.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/shortest-path-chooser-dark.svg"><img src="../docs/visuals/assets/shortest-path-chooser.svg" alt="A column of seven yes-or-no questions from all pairs, unweighted, 0-1 weights, DAG, negative weights and heuristic to nonnegative weights, each paired with the algorithm and the repository method that answers it." width="160"></picture></a> | [Shortest-path toolkit: 0-1 BFS, DAGs, A* and Floyd–Warshall](graph/shortest-paths.md) | You know something about the graph (weights 0 or 1, acyclic, a target with a heuristic, all pairs wanted) that lets a specialised method beat the general one. |
| <a href="graph/topological-sort.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/topological-kahn-dark.svg"><img src="../docs/visuals/assets/topological-kahn.svg" alt="Kahn&#x27;s algorithm on a 5-vertex DAG whose in-degrees are 2, 1, 0, 0 and 2: it pops ready vertices 2, 3, 0, 1, 4, lowering in-degrees and refilling the ready queue, and outputs 2, 3, 0, 1, 4." width="160"></picture></a> | [Topological sort: order work so prerequisites come first](graph/topological-sort.md) | Work has prerequisites: build steps, course plans, spreadsheet cells, any "A before B" rules that must all hold. |
| <a href="graph/tree-queries.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/lca-binary-lifting-dark.svg"><img src="../docs/visuals/assets/lca-binary-lifting.svg" alt="In a seven-vertex tree rooted at 0, lca(6, 4) lifts vertex 6 by 2 to vertex 3, finds the 2^2 and 2^1 ancestors equal, jumps both vertices by 2^0 to 1 and 2, and returns their parent 0, read from the table of 2^j-th ancestors." width="160"></picture></a> | [Tree queries: lowest common ancestor and diameter](graph/tree-queries.md) | You ask many questions about one fixed tree: lowest common ancestor, distance between two vertices, k-th ancestor, or the longest path. |

### Greedy

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="greedy/huffman-coding.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/huffman-tree-dark.svg"><img src="../docs/visuals/assets/huffman-tree.svg" alt="Five symbols with frequencies 5, 9, 12, 13 and 16 are merged two lightest at a time into a tree, which gives code lengths 3, 3, 2, 2 and 2 and a weighted length of 124." width="160"></picture></a> | [Huffman coding](greedy/huffman-coding.md) | You know symbol frequencies and want the shortest prefix code with whole-bit codewords. |
| <a href="greedy/overview.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/activity-selection-dark.svg"><img src="../docs/visuals/assets/activity-selection.svg" alt="Choose earliest finishing intervals; touching endpoints remain compatible." width="160"></picture></a> | [Greedy algorithms need an exchange argument](greedy/overview.md) | A proof (an exchange argument) shows some optimum contains the local choice, as for earliest-finish activity selection. |
| <a href="greedy/problems.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/coin-dp-dark.svg"><img src="../docs/visuals/assets/coin-dp.svg" alt="Compare every final coin; the largest-first greedy choice loses." width="160"></picture></a> | [Greedy choice across different objectives](greedy/problems.md) | A cut-property or exchange argument covers your exact objective, as for Kruskal and Huffman merging. |

### Math and randomness

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="math/number-theory.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/modpow-squares-dark.svg"><img src="../docs/visuals/assets/modpow-squares.svg" alt="NumberTheory.modPow(3, 13, 1000) reads the bits of 13 = 1101 from the lowest: the squares of 3 are 3, 9, 81 and 561 modulo 1000, and the result 3, 3, 243, 323 changes only on the 1 bits, ending at 323." width="160"></picture></a> | [Number theory: gcd, modular powers, primes and Fibonacci mod m](math/number-theory.md) | You need a gcd, a modular inverse, a power modulo `m`, all primes up to a limit, or a primality verdict for a `long`. |
| <a href="math/randomized-sampling.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/reservoir-sampling-dark.svg"><img src="../docs/visuals/assets/reservoir-sampling.svg" alt="Sampling.reservoirSample over the items A to H with k = 3 and seed 17 fills the reservoir with A, B, C, then draws j for each later arrival with replacement chance 3/4 down to 3/8; D, F and H replace slots 0, 0 and 1, giving F, H, C." width="160"></picture></a> | [Randomized sampling: reservoir, Fisher-Yates and the alias method](math/randomized-sampling.md) | You need a uniform sample from a stream, a uniform shuffle, or many weighted draws from fixed weights. |

### Searching

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="searching/binary-search-on-answer.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/predicate-boundary-dark.svg"><img src="../docs/visuals/assets/predicate-boundary.svg" alt="For weights 3, 2, 2, 4, 1, 4 shipped in 3 days, capacities 4 and 5 are too slow and 6 to 16 fit. firstTrue(4, 17, fits) probes 10, 7, 5 and 6 and returns 6." width="160"></picture></a> | [Binary search on the answer](searching/binary-search-on-answer.md) | You want the smallest (or, by negating the test, the largest) value that satisfies a condition that, once true, stays true for larger values. |
| <a href="searching/binary-search.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/binary-bound-dark.svg"><img src="../docs/visuals/assets/binary-bound.svg" alt="Half-open ranges find the first target among duplicate values." width="160"></picture></a> | [Binary search as a boundary problem](searching/binary-search.md) | The data is in ascending order, or a yes/no question flips from false to true exactly once, and you query repeatedly. |
| <a href="searching/linear-search.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/linear-scan-first-match-dark.svg"><img src="../docs/visuals/assets/linear-scan-first-match.svg" alt="linearSearch for 4 in 9, 4, 4, 2 inspects two cells and returns index 1, never reaching the second 4; a search for 7 inspects all four cells and returns -1." width="160"></picture></a> | [Linear search and the first-match contract](searching/linear-search.md) | The data is unsorted or tiny, you query once, or the match test is not an ordering. |
| <a href="searching/selection.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/quickselect-shrink-dark.svg"><img src="../docs/visuals/assets/quickselect-shrink.svg" alt="kthSmallest of 7, 2, 9, 4, 4, 1 with k = 2: three partitions with pivots 7, 2 and 4 narrow the range from [0..5] to [0..3] to [2..3], and the equal part answers 4." width="160"></picture></a> | [Selection: k-th smallest, top-k, k-way merge and majority](searching/selection.md) | You need one rank, the k best, a merge of sorted runs, or a majority value, and not the whole order. |

### Sorting

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="sorting/README.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/sort-stability-dark.svg"><img src="../docs/visuals/assets/sort-stability.svg" alt="Records 3a, 1b, 3c, 2d, 1e, 3f sorted by their number: a stable sort gives 1b, 1e, 2d, 3a, 3c, 3f, keeping tags in order, while a hypothetical unstable outcome 1e, 1b, 2d, 3c, 3a, 3f reorders equal keys." width="160"></picture></a> | [Choosing a sort](sorting/README.md) | You must choose between the repository sorts and the JDK sorts for a concrete input. |
| <a href="sorting/bubble-sort.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/bubble-swaps-dark.svg"><img src="../docs/visuals/assets/bubble-swaps.svg" alt="Swap neighboring values; each pass settles a maximum on the right." width="160"></picture></a> | [Bubble sort, insertion sort and local order](sorting/bubble-sort.md) | You are learning the sorted-suffix invariant, or an array is known to be almost sorted and tiny. |
| <a href="sorting/heap-sort.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/heapsort-phases-dark.svg"><img src="../docs/visuals/assets/heapsort-phases.svg" alt="Heapsort on 3, 1, 6, 5, 2, 4 builds the max-heap 6, 5, 4, 1, 2, 3, then moves 6 and 5 to the end, giving 5, 3, 4, 1, 2, 6 and 4, 3, 2, 1, 5, 6, until 1 to 6 is sorted." width="160"></picture></a> | [Heapsort and the sorted suffix](sorting/heap-sort.md) | You need a worst-case O(n log n) bound and O(1) extra space for an `int[]`. |
| <a href="sorting/insertion-sort.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/insertion-sort-shift-dark.svg"><img src="../docs/visuals/assets/insertion-sort-shift.svg" alt="In 3, 5, 8, 9, 4, 1 the prefix 3, 5, 8, 9 is sorted. The key 4 is saved, the three larger values 5, 8 and 9 shift one slot right, and 4 drops into index 1." width="160"></picture></a> | [Insertion sort: grow a sorted prefix](sorting/insertion-sort.md) | The array is tiny or nearly sorted, or you insert a few values into an already sorted array. |
| <a href="sorting/merge-sort.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/merge-recursion-dark.svg"><img src="../docs/visuals/assets/merge-recursion.svg" alt="Split into singletons, then merge sorted children bottom up." width="160"></picture></a> | [Merge sort: combine two proven results](sorting/merge-sort.md) | You need a guaranteed O(n log n) bound and stability, and can afford an O(n) buffer. |
| <a href="sorting/non-comparison-and-comparator-sorts.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/counting-sort-counts-dark.svg"><img src="../docs/visuals/assets/counting-sort-counts.svg" alt="For 5, 3, 6, 3, 4, 6 the range 3 to 6 has four counts: 2, 1, 1 and 2. Writing each value back that many times gives 3, 3, 4, 5, 6, 6." width="160"></picture></a> | [Counting, radix and stable Comparator sorts](sorting/non-comparison-and-comparator-sorts.md) | Integer keys fall in a small range (`countingSort`), keys span the full `int` range (`radixSort`), or objects need a stable sort by a key (`mergeSort(T[], Comparator)`). |
| <a href="sorting/quick-sort.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/quick-partition-dark.svg"><img src="../docs/visuals/assets/quick-partition.svg" alt="Gather duplicate pivot values into one finished equal region." width="160"></picture></a> | [Three-way quicksort with a bounded call stack](sorting/quick-sort.md) | You sort an `int[]` in place, especially one with many duplicate values. |

### Strings

| Figure | Lesson | Use it when |
| :---: | :--- | :--- |
| <a href="strings/multi-pattern-and-suffix-arrays.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/aho-corasick-links-dark.svg"><img src="../docs/visuals/assets/aho-corasick-links.svg" alt="The trie of he, she, his and hers, then its failure links listed one per row: sh to h, she to he, his to s and hers to s; every other node links to the root." width="160"></picture></a> | [Multi-pattern matching and suffix arrays](strings/multi-pattern-and-suffix-arrays.md) | Many fixed patterns must be found in one pass over a text (Aho–Corasick), or a fixed text will answer many substring questions: occurrences, distinct substrings, the longest repeated substring (suffix array). |
| <a href="strings/pattern-matching.md"><picture><source media="(prefers-color-scheme: dark)" srcset="../docs/visuals/assets/kmp-overlap-dark.svg"><img src="../docs/visuals/assets/kmp-overlap.svg" alt="Prefix fallback retains the overlap between two pattern matches." width="160"></picture></a> | [Pattern matching: Z-function, Rabin–Karp and Manacher](strings/pattern-matching.md) | One pattern is searched in one text and you need every match or a linear-time guarantee (KMP, Z-function), a cheap rolling-hash filter before an exact check (Rabin–Karp), or the longest palindromic substring (Manacher). |

<!-- /visual:gallery -->

See also the [solution finder](../docs/solution-finder.md), the [implementation catalog](../docs/catalog.md), the [complexity guide](../docs/complexity.md) and the [visual index](../docs/visuals/README.md).
