# Design: problem-first solution finder and breadth expansion

Date: 2026-11-16. Status: draft for approval.

## Intent

The owner uses this repository as a reference at work. When a problem comes up,
they open it to see **every reasonable data-structure or algorithm solution
available in Java**. Their words: "If it's out of sight, it's out of mind, so the
more the merrier." So the main goal is **visibility across a wide range of
techniques**, organized around the problems people actually have.

Because the material is used at work, it must be trustworthy. A wrong claim about
cost, JDK behavior or correctness is worse than a missing entry. The repository
already sets a high bar (see `AGENTS.md`: tested source, reference-model tests,
executable lessons, consistent catalog and complexity docs). New implemented
material must meet that bar. Material that is not implemented must say so plainly.

## Goals

1. **Solution finder.** Add a problem-first index covering at least 100 distinct
   techniques. Each entry gives: when to use it, time/space cost, pitfalls, and
   its home in Java. The home is either a class in this repo, a named JDK class or
   method, or "not implemented here".
2. **Implement the highest-value gaps** to the full repository standard, in
   batches. As each one lands, its finder entry changes from "not implemented
   here" to a link to source and lesson.
3. **Review the existing library and docs for correctness.** Use independent
   reviewers and fix confirmed defects with regression tests.
4. Keep `README.md`, `docs/catalog.md`, `docs/complexity.md`, the roadmap and the
   validation evidence consistent with the new APIs.

## Non-goals

- Untested or partial implementations pasted into Markdown.
- Implementing Java Collections Framework interfaces, or making structures thread
  safe. This is existing policy; the finder points to JDK concurrent types instead.
- Benchmarks or performance claims beyond asymptotic analysis.
- New diagrams in the visual atlas, and new `LearningLab` terminal topics, in this
  pass. Both can follow later.
- Custom concurrent or lock-free structures, distributed algorithms, cryptography
  and floating-point numerical methods. The finder may mention these and point to
  JDK types or name the technique, but they are not implemented.

## Anti-goals (failure even if everything builds)

- A finder entry that misstates a JDK guarantee or a cost. For example, it must not
  call `HashMap` worst-case O(1), or claim `Arrays.sort(int[])` is stable.
- An entry that looks implemented but is not, or a link to a lesson that does not
  cover it.
- Breaking existing topic paths or public APIs, or lowering the coverage floors or
  weakening tests to land more topics.
- Many thin additions that are near-synonyms of existing APIs. Each implemented
  addition must have a distinct trade-off that the finder can explain.

## Constraints (from the repository)

- Java 17 (`maven.compiler.release` 17); no runtime dependencies; JUnit Jupiter
  tests only. `-Xlint:all -Werror`; Javadoc doclint must pass.
- JaCoCo floors: 90% lines and 85% branches across production code.
- Every plain `java` fence in any repository Markdown file is compiled with
  `--release 17 -Xlint:all -Werror` and run with assertions enabled by
  `DocumentedExamplesTest`. Each fence must contain optional one-line imports,
  then statements, and at least one `assert`. Pseudocode uses `text` fences.
- `python3 scripts/check_docs.py` must report zero broken local links.
- Do not hand-edit generated visual regions (`<!-- visual:generated -->`), SVGs or
  visual data. `python3 scripts/generate_visuals.py --check` and the visual
  Python tests must still pass.
- Preserve all existing topic paths. Do not commit `target/`.
- Work happens on branch `ccr-32ae1c43-isy7tl`. Push it and open one draft PR.

## Approaches considered

| Approach | Trade-off | Decision |
| --- | --- | --- |
| **A. Problem-first index plus full-standard implementations of the biggest gaps** | Wide coverage right away. Some entries are honestly marked "not implemented here". Implemented items keep the repository's standard. | **Chosen by the owner.** It gives the coverage they want without lowering trust. |
| B. Full-standard implementations only, with no index of unimplemented techniques | Everything listed is tested. Coverage grows slowly (roughly 25–35 topics this pass), and anything not built stays invisible. | Rejected: it works against "out of sight, out of mind". |
| C. Paste many standalone code snippets into Markdown | Fastest coverage. | Rejected: it breaks `AGENTS.md` and makes the material untrustworthy for professional use. |

## Part 1: The solution finder

**Location:** a single page, `docs/solution-finder.md`, with a table of contents.
One page lets the owner search it in the browser (Ctrl-F) for anything. Builder's
call: split into a folder only if the page passes about 2,000 lines, keeping
`docs/solution-finder.md` as the entry point.

**Linking:** link the finder from the README "Your next step" table (first row
group) and from the catalog intro.

**Top of page:**

- A short "how to choose" checklist, covering:
  - whether order is needed
  - whether the data is static or changing
  - whether an approximate answer is acceptable
  - whether memory is bounded or the data is a stream
  - the input size and whether worst-case guarantees are needed
  - whether the input must not be modified
- A status legend.

**Entry format.** Group entries under the problem a person would search for. Each
problem is one table:

```text
### Keep the k largest items from a stream
| Option | Use when | Time / space | Java home | Watch out for |
| Bounded min-heap of size k | streaming; k much less than n | O(n log k) / O(k) | JDK PriorityQueue; repo BinaryMinHeap | ... |
| Quickselect | all in memory; order not needed | O(n) expected | repo Selection (new) | O(n^2) worst case without a fallback |
| Sort then slice | small n; sorted output wanted | O(n log n) | JDK Arrays.sort | ... |
| Count-Min sketch + heap | approximate heavy hitters | O(1) per item | not implemented here | overestimates only |
```

**Java home values:**

- `repo: Class.method`, linked to the source and lesson
- `JDK: java.util.TreeMap#floorKey`, as plain text
- `not implemented here`

An entry may list both a repo class and a JDK class.

**Accuracy rules for JDK claims:**

- State only what the Java SE 17 API specification guarantees.
- Label OpenJDK implementation details as such ("OpenJDK implementation note"). An
  example is `HashMap` turning long bins into trees.
- Where a claim can be shown with a small executable example (`TreeMap` floor and
  ceiling, a bounded `PriorityQueue`, `BitSet`, `Collections.binarySearch`,
  access-order `LinkedHashMap`), add a `java` fence with assertions.
- Keep these to roughly 15–25 fences, chosen for value rather than to cover every
  entry.

**"Not implemented here" entries** get one or two sentences: the idea, when to use
it and its cost. They get no code and no external links. External URLs are not
link-checked and can go stale.

**Problem families and minimum coverage.** Each family lists representative
entries. Bold marks items this design implements (Part 2); other items are JDK or
index-only.

1. **Look up by key:**
   - JDK `HashMap`, `LinkedHashMap`, `IdentityHashMap`, `EnumMap`, `WeakHashMap`
   - repo `ChainedHashMap`; **open-addressing (linear probing) map**
   - cuckoo hashing; perfect hashing; Robin Hood hashing
2. **Ordered keys and nearest-key queries:**
   - JDK `TreeMap`/`TreeSet` (floor, ceiling, subMap); repo BST and AVL
   - **red-black ordered map with rank/select**; skip list (JDK `ConcurrentSkipListMap`)
   - B-tree / B+ tree; order-statistic tree (via rank/select)
3. **Prefix and text keys:**
   - repo `Trie`
   - radix / Patricia trie; ternary search tree
   - **suffix array + LCP**; suffix automaton
4. **Approximate membership and counting:**
   - **Bloom filter**; counting Bloom filter; cuckoo filter
   - **Count-Min sketch**; HyperLogLog
5. **Range queries over arrays:**
   - prefix sums; **difference arrays**; 2D prefix sums
   - repo `FenwickTree` and `SegmentTree`
   - **lazy segment tree (range add, range sum)**; **sparse table**; sqrt decomposition
6. **Intervals:**
   - **merge intervals**; **minimum meeting rooms (interval partitioning)**
   - repo activity selection; **weighted interval scheduling**
   - interval tree; sweep line
7. **Stacks, queues, deques and priority:**
   - repo stack, queue, ring deque, min-heap; JDK `ArrayDeque`, `PriorityQueue`
   - **monotonic stack** (next greater element, largest rectangle)
   - repo sliding-window maximum; **k-way merge**
   - d-ary heap; indexed priority queue; pairing heap
8. **Caching and eviction:**
   - repo LRU; JDK access-order `LinkedHashMap`; **LFU cache**
   - time-to-live (TTL) caches; CLOCK
9. **Searching:**
   - repo linear and binary search, lower and upper bound
   - binary search on the answer; exponential search; interpolation search; ternary search
10. **Selection and top-k:**
    - **quickselect (k-th smallest)**; bounded heap; median of medians
    - **Boyer–Moore majority vote**
11. **Sorting:**
    - repo comparison sorts; JDK `Arrays.sort`, `List.sort`, `Arrays.parallelSort`
    - **counting sort**; **LSD radix sort**; **stable Comparator merge sort**
    - external merge sort; bucket sort; shell sort; Timsort (what JDK object sorts use)
12. **Array and sequence patterns:**
    - **two pointers (pair with target sum)**; **variable-size sliding window (longest substring without repeats)**
    - repo Kadane and inversion count
    - Dutch national flag (three-way partitioning); cycle detection (Floyd's tortoise and hare)
13. **String matching and text:**
    - repo KMP; **Z-algorithm**; **Rabin–Karp (verified matches)**; **Aho–Corasick**
    - Boyer–Moore–Horspool; **Manacher (longest palindromic substring)**
    - repo edit distance and LCS; rolling hash pitfalls
14. **Graph traversal and structure:**
    - repo BFS, DFS, components, topological sort, SCC, bridges
    - **articulation points**; **bipartite check**; **directed cycle with witness**
    - **Eulerian path (Hierholzer)**
15. **Shortest paths:**
    - repo unweighted BFS, Dijkstra, Bellman–Ford
    - **0-1 BFS**; **A\***; **Floyd–Warshall (all pairs)**; Johnson's algorithm
    - DAG shortest path in topological order
16. **Spanning trees, flows and matching:**
    - repo Kruskal; **Prim**
    - **max flow (Dinic) + min cut**; **bipartite matching (Hopcroft–Karp)**
    - min-cost flow; Hungarian algorithm
17. **Trees as data:**
    - **lowest common ancestor (binary lifting)**; **tree diameter**
    - Euler tour; heavy-light decomposition
18. **Dynamic programming:**
    - repo coin change, knapsack, LCS, LIS and edit distance; **solution reconstruction for these**
    - **count coin-change ways**; **word break**; **longest palindromic subsequence**
    - **matrix-chain order**; **bitmask TSP (Held–Karp)**; digit DP
19. **Greedy:**
    - repo activity selection; **Huffman code lengths**
    - fractional knapsack; jump game
20. **Backtracking and search:**
    - repo permutations, N-queens, Sudoku and subset sum
    - combinations; power set; branch and bound; constraint propagation
21. **Disjoint sets and bitsets:**
    - repo `UnionFind`; JDK `BitSet`
    - subset enumeration over bitmasks; Gosper's hack
22. **Math and number theory:**
    - **gcd/lcm (overflow-checked)**; **extended Euclid**; **modular exponentiation**; **modular inverse**
    - **sieve of Eratosthenes**; **deterministic Miller–Rabin for `long`**
    - matrix exponentiation (fast Fibonacci)
    - JDK `Math.*Exact`, `BigInteger`
23. **Randomization and streams:**
    - **reservoir sampling**; **alias method (weighted sampling)**
    - JDK `Collections.shuffle` (Fisher–Yates); JDK `SplittableRandom`
24. **Concurrency-aware JDK structures (index only):**
    - `ConcurrentHashMap`, `ConcurrentSkipListMap`, `CopyOnWriteArrayList`
    - `BlockingQueue` variants, `LongAdder`
25. **Encoding and compression (index only beyond Huffman):**
    - run-length encoding; Burrows–Wheeler transform

The builder may add entries but should not drop a family.

## Part 2: Implementations (full repository standard)

Each item gets:

- source under the existing packages
- Javadoc stating the invariant, null/empty/duplicate/bounds behavior, mutation and
  ownership, overflow policy, and time/space
- JUnit tests against an independent reference (JDK, brute force on small inputs,
  or a different algorithm), with fixed seeds in failure messages and checks that
  inputs are not mutated
- a catalog row and a complexity row
- coverage in a lesson with executable assertions

**Batches.** The order reflects value to everyday professional work, and each
batch ends green.

| Batch | Package / class (builder may refine names) | Contents |
| --- | --- | --- |
| 1. Selection, sorting, array patterns | `algorithms.Selection`, `algorithms.SortAlgorithms` (additions), `algorithms.ArrayPatterns` | Quickselect k-th smallest; top-k with a bounded heap (Comparator); k-way merge of sorted lists; counting sort; LSD radix sort for `int` including negatives; stable Comparator merge sort for object arrays; two-pointer pair sum; variable-size window; monotonic-stack next-greater and largest rectangle; difference-array range add; merge intervals; min meeting rooms; Boyer–Moore majority |
| 2. Lookup and range structures | `collections.OpenAddressingHashMap`, `collections.LfuCache`, `structures.RedBlackTreeMap`, `structures.BloomFilter`, `structures.CountMinSketch`, `structures.SparseTable`, `structures.LazySegmentTree` | Linear probing with backward-shift deletion (no tombstones); O(1) LFU with LRU tie-break; red-black ordered map with get/put/remove, floor, ceiling, rank, select, range count; Bloom filter with false-positive estimate; Count-Min sketch (overestimate-only guarantee); O(1)-query range minimum on static data; range add / range sum |
| 3. Graphs | additions to `graphs.GraphAlgorithms` / `graphs.AdvancedGraphAlgorithms`, plus `graphs.FlowNetwork` and `graphs.TreeAlgorithms` if cleaner | Prim; Floyd–Warshall with negative-cycle detection; A* with a caller-supplied admissible heuristic; 0-1 BFS; bipartite check with odd-cycle witness; articulation points; directed cycle witness; Hierholzer Eulerian path; Dinic max flow + min cut; Hopcroft–Karp; LCA by binary lifting; tree diameter |
| 4. Strings | `algorithms.StringAlgorithms` (additions) or `algorithms.TextIndex` | Z-function; Rabin–Karp with match verification (no false positives); Aho–Corasick multi-pattern; Manacher; suffix array (prefix doubling) + LCP (Kasai) |
| 5. Dynamic programming and greedy | `algorithms.DynamicProgramming` (additions), `algorithms.GreedyAlgorithms` (additions) | Reconstruction: an LCS string, an edit script, chosen knapsack items, chosen coins, an LIS sequence. New: coin-change way count (overflow policy stated), word break, longest palindromic subsequence, matrix-chain order, Held–Karp TSP (n capped), weighted interval scheduling, Huffman code lengths, interval partitioning (if not in batch 1) |
| 6. Math, sampling, probabilistic leftovers | `algorithms.NumberTheory`, `algorithms.Sampling` | gcd/lcm/extended gcd; modPow; modInverse; sieve; deterministic Miller–Rabin for `long`; reservoir sampling; alias method (caller-supplied `Random`/`RandomGenerator` for determinism) |

**Conventions to follow (observed in the existing code):**

- Utility classes are `final` with a private constructor.
- Results are records with validating compact constructors.
- Nulls are rejected with `Objects.requireNonNull(x, "name")`.
- Returned collections are immutable snapshots (`List.copyOf`).
- Absent values use `Optional`, `OptionalInt` or `OptionalLong`, not sentinels. One
  exception: `-1` is the existing "not found" for searches.
- Ranges are half-open.
- Sums use `long`, with an explicit overflow policy (checked or documented).
- Exponential algorithms have explicit input-size caps.
- Graph traversals are iterative, so deep graphs do not overflow the stack.
- Tie-breaking is deterministic and documented.
- Inputs are not modified unless the method is documented as in-place.
- Randomized structures take a seed or a `Random`, so tests are deterministic.

**Lessons.** Put each lesson beside the nearest existing lesson. Create a new
directory only for a new family (likely `algorithms/strings/`,
`algorithms/array-patterns/`, `algorithms/math/`, `data-structures/probabilistic/`
and `data-structures/range-queries/`). Each lesson follows the existing shape:

- purpose and preconditions
- the invariant
- a worked trace (`text` fence)
- an executable example with assertions
- costs
- a counterexample or pitfall
- practice items

One lesson may cover several related APIs. Link each lesson from the README topic
map, the finder and the catalog.

## Part 3: Correctness review of existing material

Before batch 1, independent reviewers read the existing source and the claims in
the catalog, complexity guide and lessons. Each confirmed defect gets a failing
regression test first, then the fix. Findings and fixes are recorded in
`docs/validation.md`. Reviewers get the code, not this design's conclusions.

## Delivery and verification

- Commit order:
  1. design (this file)
  2. review fixes
  3. solution finder (all entries; statuses honest at that moment)
  4. one commit (or a few) per implementation batch, each flipping its finder
     entries to links
- After each batch, run and confirm:
  - `./mvnw clean verify` passes
  - the packaged JAR runs, including the four `learn` topics
  - `python3 scripts/check_docs.py` reports zero broken links
  - `python3 scripts/generate_visuals.py --check` passes
  - the visual Python tests pass
  - `git diff --check` is clean
- Update README counts ("26 library classes"), the topic map, the catalog,
  complexity, the roadmap stage lists, the exercises (new items for new families)
  and `docs/validation.md` with measured results.
- Push to `ccr-32ae1c43-isy7tl` after each batch, and keep one draft PR updated.

## Left to the builder

- Exact method names and signatures, within the conventions above.
- Which hash mixer to use for open addressing, Bloom and Count-Min (it must be
  documented and deterministic).
- Which reference model to test against for each item, as long as it is
  independent of the implementation.
- Lesson file names within the placement rule.
- Wording and order of finder entries within a family.
- Whether a borderline batch item moves to the next batch when a batch grows too
  large. Record the move in the PR description.
