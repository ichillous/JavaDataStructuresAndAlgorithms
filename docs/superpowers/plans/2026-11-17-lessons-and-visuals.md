# Lessons and Visuals Expansion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make every lesson under `algorithms/` and `data-structures/` richer and
better illustrated: on-topic figures placed beside the text they explain, more
content per lesson, new lessons for missing topics, a gallery landing page per
folder, and more interactive step-throughs.

**Architecture:**
- Task 1 upgrades the figure generator so lessons can hold several figures in
  named regions, and so each family of figures lives in its own module, fixture
  file and Java test. That lets the family tasks run in parallel without touching
  shared source files.
- Tasks 2–13 each own a disjoint set of lessons plus one figure module. Task 14
  adds step-throughs. Task 15 adds the folder landing pages. Task 16 integrates
  the docs and records the rendered review. Task 17 is the whole-branch review.
- Generated files (`docs/visuals/data/manifest.json`, `visual-data.js`,
  `docs/visuals/README.md`, figure numbers in lesson regions) are rebuilt by the
  controller after every merge. Never resolve a conflict in them by hand.

**Tech Stack:** Python 3 standard library (generator and its tests), Java 17 with
JUnit Jupiter (fixture checks), Markdown lessons with executable Java fences,
optional Node + Playwright for the rendered review.

**Spec:** the owner's request, verbatim: "improve these folders: algorithms,
data-structures. More content. Better visuals. Take the lead be creative." The
controller's design decisions are this plan; there is no separate spec file.

## Global Constraints

- Everything in `AGENTS.md` applies. In particular:
  - Java 17, no new dependencies, `-Xlint:all -Werror`, Javadoc doclint.
  - JaCoCo floors: lines >= 0.90, branches >= 0.85.
  - Every plain `java` fence in any `.md` file is compiled and run with assertions.
    Contents: optional one-line imports, then statements, with at least one
    `assert`. Use `text` fences for traces and pseudocode. Never put `java` fences
    in plan, brief or report files.
  - No incomplete or duplicate implementations in Markdown. Link to maintained
    source. A lesson example calls the library or a named JDK type.
  - Preserve existing topic paths and public APIs. Additions only.
- **Never hand-edit generated output:** SVGs, `docs/visuals/data/manifest.json`,
  `docs/visuals/data/visual-data.js`, `docs/visuals/README.md`, or the body of a
  `<!-- visual:generated ... -->` region. Edit the source and regenerate.
- **Lesson prose never mentions a figure number.** Numbers are assigned by the
  generator and change when other tasks merge.
- **JDK claims:** state only what the Java SE 17 API documentation guarantees
  (https://docs.oracle.com/en/java/javase/17/docs/api/ is reachable with curl), or
  what `docs/solution-finder.md` already states. Label anything else "OpenJDK
  implementation note". No benchmark or speed claims.
- **Figure rules** (from `docs/visuals/design.md`, as extended by Task 1):
  - One idea per figure. Canvas width 520. Aim for a height of 560–860; split into
    two figures instead of exceeding 900.
  - Every label is at least 18px. No label may leave the canvas or collide with
    another label. The layout test and the rendered review both check this.
  - Color never carries meaning alone: every state also has a text label or a
    legend entry. Both themes must read well.
  - Every figure shows a concrete canonical example stored in the task's fixture
    file, and a Java test checks that example against the public API.
  - `description` (the alt text) states concretely what is shown, in one or two
    sentences. `keyIdea` is one sentence. Neither mentions colors alone.
  - Each figure ends with the standard KEY IDEA note.
- **Family task file ownership** (Tasks 2–13). A task may create or modify only:
  - its figure module `scripts/visuals/<stem>_figures.py`
  - its fixture file `docs/visuals/data/examples-<stem>.properties`, whose keys all
    start with `<stem>.`
  - its Java fixture test
    `src/test/java/io/github/ichillous/dsa/visuals/VisualExamples<Stem>Test.java`
  - the lessons its task lists, and the new lessons its task names
  - generated output produced by running the generator
  - its fragment file `docs/superpowers/fragments/task-NN.txt`
  - library source and tests only where its task names an API
  It must not edit `README.md`, anything else under `docs/`, `scripts/generate_visuals.py`,
  `scripts/visuals/drawing.py`, the other modules, or lessons owned by another task.
  If a drawing primitive is missing, build a private helper inside the module and
  say so in the report.
- **Placing a figure:** write an empty named region where the figure belongs, then
  run the generator:

  ```text
  <!-- visual:generated FIGURE_ID -->
  <!-- /visual:generated -->
  ```

  An optional caption follows a pipe: `<!-- visual:generated FIGURE_ID | Caption sentence. -->`.
  A family task places only figures from its own module and the existing 26.
- **Lesson upgrade contract.** For every existing lesson a family task owns:
  1. **At a glance.** Directly under the opening paragraph, a two-column table
     whose header row is `| At a glance | |` with these rows: **Use it when**,
     **Avoid it when**, **Cost**, **In this repo** (linked class or method),
     **In the JDK** (class or method as code, or "no direct equivalent").
  2. **Figures.** At least one figure about the lesson's own topic, placed next to
     the text it illustrates. A borrowed off-topic figure is replaced or moved to
     the lesson where it fits.
  3. **In real Java.** A section saying what to use in production code, with one
     executable example that shows a behavior worth knowing and is not a repeat of
     an earlier example in the lesson.
  4. **Common mistakes.** Three specific mistakes, each with the concrete input
     that exposes it and the invariant it breaks.
  5. **Check yourself.** Three questions, each in a `<details>` block whose
     `<summary>` is the question and whose body is the answer in one to three
     sentences. Leave a blank line after `<summary>...</summary>` and before
     `</details>`.
  6. Keep the existing content and links unless a statement is wrong. Fix wrong
     statements and list them in the report. Keep the existing "Practice" section.
  Lessons added in the previous expansion (they already have invariant, trace,
  costs, pitfalls and practice) need only items 1, 2 and 5, plus item 3 where the
  lesson has no JDK guidance yet.
- **New lessons** follow the existing lesson shape (purpose and preconditions, the
  invariant, a worked `text` trace, an executable example, a costs table, a pitfall
  or counterexample, three Practice items, links to the catalog, the complexity
  guide and the matching solution-finder section) plus items 1, 2 and 5 above.
- **Fragment file** `docs/superpowers/fragments/task-NN.txt` (plain text): the
  README topic-map additions, catalog and complexity rows for any new API, the
  solution-finder rows that should now link a new lesson or API, roadmap and
  exercise additions, and every wrong statement fixed. Task 16 merges these.
- **Verification before reporting** (run in the task's worktree):

  ```text
  python3 scripts/generate_visuals.py                         -> rewrites outputs
  python3 scripts/generate_visuals.py --check                 -> "0 stale"
  python3 -m unittest discover -s scripts/visuals -p 'test_*.py'  -> OK
  ./mvnw -q -B clean verify                                   -> exit 0
  java -jar target/java-data-structures-and-algorithms-1.0.0-SNAPSHOT.jar  -> exit 0
  python3 scripts/check_docs.py                               -> "0 broken"
  git diff --check                                            -> no output
  ```

  Family tasks also render their figures in both themes with the review tool from
  Task 1, look at every sheet, and fix any overlap, clipping or unreadable label.
- **Commits:** commit early and often inside the worktree; never push. End each
  commit message with the session's attribution lines.

## Review Focus

1. **Figures that disagree with the code.** A figure's numbers, order or states
   must match what the library does on the fixture. Each family's Java fixture test
   asserts every value a figure displays.
2. **Unreadable figures.** Text that overlaps, leaves the canvas, or has low
   contrast in the dark theme. `test_layout.py` (Task 1) and the rendered review
   (Tasks 2–13, 16) cover this.
3. **Wrong claims in new prose.** JDK guarantees, costs and "common mistakes" must
   be true. Reviewers check every new JDK statement against the Java 17 docs.
4. **Broken generated regions.** A marker typo, an unknown figure id or a
   hand-edited region. The generator fails on unknown ids; `--check` catches drift.
5. **Examples that pass for the wrong reason.** Each new executable example must
   assert the behavior its paragraph describes, not a tautology.

---

## Execution waves (controller)

- **Wave 1:** Task 1 (foundation). Merge and push before anything else starts.
- **Wave 2:** Tasks 2–14 in parallel, each in its own worktree branched from the
  work branch after Task 1.
- **Wave 3:** Task 15 (landing pages), then Task 16 (integration and rendered review).
- **Wave 4:** Task 17 (whole-branch review), one fix wave, final verification,
  PR, merge.
- After every merge the controller runs the generator, `--check`, the visual unit
  tests and commits the regenerated output.

---

### Task 1: Visual system foundation

**Files:**
- Modify: `scripts/generate_visuals.py`, `scripts/visuals/drawing.py`,
  `scripts/visuals/lesson_links.py` (delete once migrated), `scripts/visuals/test_assets.py`,
  `scripts/visuals/test_review.py`
- Create: `scripts/visuals/test_layout.py`, `scripts/visuals/browser_review.js`,
  `scripts/visuals/linear_more_figures.py`, `docs/visuals/data/examples-linear_more.properties`,
  `src/test/java/io/github/ichillous/dsa/visuals/VisualFixtures.java`,
  `src/test/java/io/github/ichillous/dsa/visuals/VisualExamplesLinearMoreTest.java`
- Modify: every Markdown file that has a `<!-- visual:generated -->` region (marker
  migration only), `docs/visuals/design.md`, the visual paragraphs of `CONTRIBUTING.md`
  and `AGENTS.md`

**Interfaces (produced for every later task):**
- **Module discovery.** The generator loads `linear_figures`, `tree_figures`,
  `algorithm_figures`, `graph_figures`, `complexity_figures` in that order (figures
  01–26 keep their numbers), then every other `scripts/visuals/*_figures.py` sorted
  by file name. Each exports `FIGURES`, a list of dicts with keys `id`, `title`,
  `category`, `description`, `keyIdea`, `lesson`, `height`, `draw`, and optional
  `subtitle`. Duplicate ids fail the build.
- **Fixtures.** `drawing.load_all_fixtures(directory)` reads
  `examples.properties`, then every `examples-*.properties` sorted by name, and
  fails on a duplicate key. `draw(canvas, fixtures)` receives the merged mapping.
- **Named regions.** In any Markdown file outside `docs/superpowers/`, `target/`
  and hidden directories:
  `<!-- visual:generated FIGURE_ID -->` or
  `<!-- visual:generated FIGURE_ID | Caption. -->`, closed by
  `<!-- /visual:generated -->`. The generator rewrites the body to the `<picture>`
  block (dark and light sources, alt text, width 520) followed by
  a bold `Figure NN.` label, the caption, and a link to the visual index.
  The caption defaults to the figure's description. An unknown id, an unclosed
  region or a nested region fails the build with the file and line.
  All 48 existing unnamed regions are migrated to named regions with their current
  figure and caption, and the output for those pages is otherwise unchanged.
- **Palette.** New semantic colors in both themes: `green` and `mint` (its fill),
  `violet` and `lilac` (its fill), `rose` (fill for `red`). Required contrast,
  at least 4.5:1: ink and muted on mint, lilac and rose; green on paper, panel and
  mint; violet on paper, panel and lilac; red on rose. `test_review.py` checks them.
- **Canvas primitives** (all text at least 18px; signatures are binding):
  - `step(y, number, text)`: numbered badge plus bold heading; returns the next y.
  - `cell_row(x, y, values, *, cell=60, gap=10, height=48, states=None, labels=None)`:
    `states` is a list of `None` or one of `"focus"` (blue on soft), `"good"` (green
    on mint), `"alt"` (violet on lilac), `"bad"` (red on rose), `"warn"` (gold on
    note), `"muted"` (dashed outline, muted text).
  - `grid(x, y, rows, *, cell_w=56, cell_h=44, row_labels=None, col_labels=None, states=None)`:
    a table for DP grids and matrices; `states` maps `(row, column)` to a state name.
  - `pointer(x, y, label, *, color="blue", below=True)`: a short labelled arrow
    pointing at a cell edge.
  - `chip(x, y, text, *, color="blue")`: a rounded label; returns its width.
  - `brace(x1, x2, y, label, *, color="muted", above=False)`: a bracket spanning a
    range, with a centred label.
  - `curve(x1, y1, x2, y2, *, bend=40, color="blue", dashed=False, arrow=True, label=None)`.
  - `edge(start, end, *, color="line", radius=27, dashed=False, label=None, directed=True)`:
    a node-to-node connector trimmed to both circles.
  - `legend(y, items)`: `items` is a list of `(state, text)`; returns the next y.
  - `panel(y, height)`: the framed background used by existing figures.
  - `node(...)` gains an optional `state=` argument using the same state names.
  Existing methods keep their behavior.
- **Layout guard.** `Canvas` records an estimated box for every text. New
  `test_layout.py` regenerates every figure in both themes and fails if a text box
  leaves the canvas, if two text boxes overlap, or if anything is drawn below
  `height - 20`. The width estimate must be calibrated so the 26 existing figures
  pass unchanged.
- **Rendered review tool.** `node scripts/visuals/browser_review.js --figures ID[,ID...] --sheets DIR`
  renders the named figures in both themes into contact-sheet PNGs (at most four
  figures per sheet) and reports real text boxes that overlap or leave the canvas,
  using the browser's measurements. With `--all --write` it also drives
  `docs/visuals/index.html` at 1440px and 390px in both themes through every trace
  frame and rewrites `docs/visuals/data/browser-review.json`. It uses the
  pre-installed Playwright and nothing else. It is optional tooling and not part
  of the Maven build.
- **Java helper.** Package-private `VisualFixtures.load(String fileName)` returns a
  small reader with `text(key)`, `number(key)`, `ints(key)`, `longs(key)`,
  `list(key)` and `strings(key)`, failing with the key name when one is missing.
- **Exemplar figure.** `dynamic-array-growth` in `linear_more_figures.py` (category
  "Linear structures"): appending to a full dynamic array copies into a doubled
  array; shows capacity 4 to 8, the copied cells, the new value and the running
  copy count, using `step`, `cell_row`, `pointer`, `brace` and `legend`. Its fixture
  is checked against `DynamicArray`. It is placed in
  `data-structures/arrays/operations.md`.

- [ ] **Step 1: Write failing tests** for named regions (known id, unknown id,
  caption override, unclosed region), module discovery order, fixture merging and
  duplicate keys, palette contrast for the new colors, and the layout guard on a
  deliberately overflowing text.
- [ ] **Step 2: Run them** with `python3 -m unittest discover -s scripts/visuals -p 'test_*.py'`.
  Expected: the new tests fail.
- [ ] **Step 3: Implement** the generator changes, migrate the 48 regions, delete
  `lesson_links.py`, add the palette colors and primitives, and the layout guard.
- [ ] **Step 4: Regenerate and check.** `python3 scripts/generate_visuals.py` then
  `--check`. Confirm with `git diff --stat` that lesson pages changed only in their
  marker lines and that figures 01–26 keep their numbers.
- [ ] **Step 5: Build the exemplar figure,** its fixture, its Java test and the
  `VisualFixtures` helper. Write `browser_review.js`, render the exemplar and four
  existing figures, look at the sheets and fix anything wrong.
- [ ] **Step 6: Document** the authoring workflow in `docs/visuals/design.md`
  (a section "Adding a figure" covering modules, fixtures, regions, primitives,
  states and the review tool) and update the visual paragraphs in `CONTRIBUTING.md`
  and `AGENTS.md` to match.
- [ ] **Step 7: Run the full verification and commit** `Let lessons hold several
  figures and add shared drawing primitives`.

### Task 2: Linear structures (arrays, linked lists, stacks, queues)

**Stem:** `linear_more` (extend the module, fixture file and Java test created by Task 1).
**Category:** "Linear structures".

**Lessons owned:** `data-structures/arrays/overview.md`, `arrays/operations.md`,
`linked-lists/overview.md`, `linked-lists/singly.md`, `linked-lists/doubly.md`,
`stacks/overview.md`, `stacks/implementation.md`, `queues/overview.md`,
`queues/implementation.md`.

**New figures:**
- `singly-insert-after`: inserting after a known node needs two link writes in a
  fixed order; shows the wrong order losing the tail. Checked against the JDK
  `LinkedList` and `DoublyLinkedList` contents.
- `stack-bracket-matching`: an `ArrayStack` trace on one balanced and one
  unbalanced bracket string, with the stack content after each character.
- `deque-both-ends`: `RingDeque` adding and removing at both ends of a circular
  buffer, with head and tail positions.
- `array-vs-list-access`: reaching index i by one jump in an array versus hops in
  `DoublyLinkedList` from the nearer end, with hop counts.

**Existing figures to place:** `array-insertion`, `dynamic-array-growth`,
`linked-unlink`, `stack-lifo`, `queue-wrap`. `fenwick-blocks` leaves
`arrays/operations.md` (Task 5 places it).

- [ ] **Step 1:** Add fixtures and a failing Java fixture test for each figure's
  displayed values. Run `./mvnw -q -B -Dtest=VisualExamplesLinearMoreTest test`;
  expect failures for the new cases.
- [ ] **Step 2:** Implement the figures; make the tests pass.
- [ ] **Step 3:** Render both themes, inspect, fix.
- [ ] **Step 4:** Upgrade the nine lessons per the lesson upgrade contract and
  place figures.
- [ ] **Step 5:** Write the fragment, run the full verification, commit
  `Illustrate and deepen the linear-structure lessons`.

### Task 3: Hashing, caches and sketches

**Stem:** `hashing`. **Category:** "Hashing and sketches".

**Lessons owned:** `data-structures/hash-tables/overview.md`, `functions.md`,
`collision-resolution.md`, `open-addressing.md`, `lfu-cache.md`,
`data-structures/probabilistic/overview.md`.

**New figures:**
- `hash-index-spread`: hash code to table index by mixing and masking; two keys
  whose low bits collide before mixing and separate after. Checked with real
  `hashCode` values.
- `open-addressing-probe`: inserting into `OpenAddressingHashMap` where the home
  slot is taken; the probe walks and wraps past the end.
- `backward-shift-delete`: removing a key from the middle of a cluster and shifting
  later keys back so lookups still succeed; contrasts with leaving a hole.
- `lfu-buckets`: `LfuCache` frequency buckets, a hit moving a key up one bucket, and
  the eviction victim.
- `bloom-filter-bits`: `BloomFilter` bit array with the probes of two added keys, a
  definite miss and a false positive.
- `count-min-rows`: `CountMinSketch` rows, one counter per row for a key, and the
  minimum as the estimate.

**Existing figures to place:** `hash-chaining`, `lru-recency`.

- [ ] **Step 1:** Fixtures and failing Java test `VisualExamplesHashingTest`.
- [ ] **Step 2:** Implement the figures.
- [ ] **Step 3:** Render both themes, inspect, fix.
- [ ] **Step 4:** Upgrade the six lessons and place figures.
- [ ] **Step 5:** Fragment, full verification, commit
  `Illustrate and deepen the hashing, cache and sketch lessons`.

### Task 4: Search trees

**Stem:** `search_tree`. **Category:** "Trees and ranges".

**Lessons owned:** `data-structures/trees/overview.md`, `binary-trees.md`,
`binary-search-trees.md`, `avl-trees.md`, `red-black-trees.md`.

**New figures:**
- `tree-traversals`: one binary tree with its in-order, pre-order, post-order and
  level-order sequences, and labels for root, leaf, depth and height.
- `bst-delete-successor`: deleting a node with two children from
  `BinarySearchTree` by replacing it with its in-order successor.
- `red-black-23-nodes`: how a red link encodes a 3-node; the same keys as a 2-3
  tree and as a `RedBlackTreeMap` shape.
- `red-black-fixups`: the three local repairs (rotate left, rotate right, flip
  colors) on small before and after shapes.

**Existing figures to place:** `avl-rotation`. `segment-minimum` leaves
`trees/overview.md` (Task 5 places it).

- [ ] **Step 1:** Fixtures and failing Java test `VisualExamplesSearchTreeTest`.
- [ ] **Step 2:** Implement the figures.
- [ ] **Step 3:** Render both themes, inspect, fix.
- [ ] **Step 4:** Upgrade the five lessons and place figures.
- [ ] **Step 5:** Fragment, full verification, commit
  `Illustrate and deepen the search-tree lessons`.

### Task 5: Heaps, tries, range queries and union-find

**Stem:** `heap_range`. **Category:** "Trees and ranges".

**Lessons owned:** `data-structures/heaps/overview.md`, `heaps/operations.md`,
`queues/priority-queues.md`, `tries/overview.md`, `tries/implementation.md`,
`range-queries/overview.md`.
**New lesson:** `data-structures/union-find/overview.md` (uses `UnionFind`; covers
union by size, path compression, the near-constant amortized cost, Kruskal as the
main use, and what union-find cannot do: deletions and listing members cheaply).

**New figures:**
- `heap-sift-paths`: `BinaryMinHeap` insert sifting up one path and remove sifting
  down one path, with the array before and after.
- `bounded-heap-topk`: keeping the k smallest of a stream with a size-k heap, as
  `Selection.smallestK` does; which arrivals are kept and which are dropped.
- `trie-prefix-walk`: `Trie.wordsWithPrefix` walking the prefix path and collecting
  words below it in order, with a limit.
- `sparse-table-overlap`: `SparseTable` answering a range minimum with two
  overlapping power-of-two blocks.
- `lazy-propagation`: `LazySegmentTree` range add leaving a pending value on a
  covering node, and a later query reading through it.
- `union-find-compression`: `UnionFind` before and after a find that flattens a
  path, plus union by size.

**Existing figures to place:** `heap-anatomy`, `trie-pruning`, `fenwick-blocks`,
`segment-minimum`, `union-find`.

- [ ] **Step 1:** Fixtures and failing Java test `VisualExamplesHeapRangeTest`.
- [ ] **Step 2:** Implement the figures.
- [ ] **Step 3:** Render both themes, inspect, fix.
- [ ] **Step 4:** Upgrade the six lessons, write the union-find lesson, place figures.
- [ ] **Step 5:** Fragment, full verification, commit
  `Illustrate and deepen the heap, trie, range-query and union-find lessons`.

### Task 6: Graphs I (representation, traversal, ordering, shortest paths)

**Stem:** `path`. **Category:** "Graphs".

**Lessons owned:** `data-structures/graphs/overview.md`, `representation.md`,
`traversal.md`, `algorithms/graph/bfs.md`, `dfs.md`, `shortest-paths.md`.
**New lesson:** `algorithms/graph/topological-sort.md` (Kahn's algorithm via
`GraphAlgorithms.topologicalSort`, the cycle case with
`GraphStructureAlgorithms.directedCycle` as the witness, and uses: build order,
course prerequisites, DAG shortest paths).

**New figures:**
- `graph-representations`: one small graph as an adjacency list, an adjacency
  matrix and an edge list, with the cost of "is there an edge" and "list
  neighbors" for each.
- `bfs-vs-dfs-order`: the same graph visited by `breadthFirst` and by `depthFirst`,
  with both visit orders.
- `topological-kahn`: Kahn's algorithm on a small DAG: in-degrees, the ready queue
  and the output order.
- `shortest-path-chooser`: a decision diagram from graph properties (unweighted,
  0/1 weights, nonnegative, heuristic available, DAG, negative edges, all pairs) to
  the algorithm and its repo method.
- `zero-one-bfs-deque`: 0-1 BFS pushing zero-weight edges to the front of the
  deque and unit edges to the back.
- `astar-heuristic`: A* on a small grid: g, h and f for expanded cells and the cells
  it never expands compared with Dijkstra.
- `floyd-warshall-via`: one step of Floyd–Warshall: allowing paths through vertex
  k improves one entry of the distance matrix.

**Existing figures to place:** `bfs-frontier`, `dijkstra-path`, `bellman-negative`,
`scc-bridges`.

- [ ] **Step 1:** Fixtures and failing Java test `VisualExamplesPathTest`.
- [ ] **Step 2:** Implement the figures.
- [ ] **Step 3:** Render both themes, inspect, fix.
- [ ] **Step 4:** Upgrade the six lessons, write the topological-sort lesson, place figures.
- [ ] **Step 5:** Fragment, full verification, commit
  `Illustrate and deepen the graph traversal and shortest-path lessons`.

### Task 7: Graphs II (structure, spanning trees, flows, tree queries)

**Stem:** `connectivity`. **Category:** "Graphs".

**Lessons owned:** `algorithms/graph/graph-structure.md`,
`minimum-spanning-trees.md`, `flows-and-matching.md`, `tree-queries.md`.
**New lesson:** `algorithms/graph/README.md`, "Choosing a graph algorithm": a
question-first index of every graph lesson and repo method (reachability, order,
shortest path, connectivity, spanning tree, flow, matching, trees), each with the
method, its precondition and its cost. It does not place Task 6's figures; Task 16
adds cross-links.

**New figures:**
- `articulation-points`: a bowtie graph whose middle vertex is an articulation
  point, with discovery and low values.
- `bipartite-odd-cycle`: a two-colored bipartite graph beside a graph whose odd
  cycle makes coloring fail, with the witness cycle.
- `euler-path`: an Euler path using every edge once, with the degree condition and
  the start vertex rule.
- `mst-kruskal-prim`: one weighted graph; Kruskal's edge order and Prim's growing
  tree reach the same total.
- `max-flow-min-cut`: a small `FlowNetwork` with its maximum flow on each edge and
  the minimum cut separating source side from sink side.
- `matching-augmenting-path`: a bipartite matching improved by flipping one
  augmenting path.
- `lca-binary-lifting`: `LowestCommonAncestor` lifting two vertices by powers of
  two to their meeting point.
- `tree-diameter`: the two breadth-first sweeps that find a longest path in a tree.

- [ ] **Step 1:** Fixtures and failing Java test `VisualExamplesConnectivityTest`.
- [ ] **Step 2:** Implement the figures.
- [ ] **Step 3:** Render both themes, inspect, fix.
- [ ] **Step 4:** Upgrade the four lessons, write the graph README, place figures.
- [ ] **Step 5:** Fragment, full verification, commit
  `Illustrate and deepen the graph structure, spanning-tree, flow and tree-query lessons`.

### Task 8: Sorting

**Stem:** `sorting`. **Category:** "Sorting and searching".

**Lessons owned:** `algorithms/sorting/bubble-sort.md`, `heap-sort.md`,
`merge-sort.md`, `quick-sort.md`, `non-comparison-and-comparator-sorts.md`.
**New lessons:**
- `algorithms/sorting/insertion-sort.md` (`SortAlgorithms.insertionSort`: the
  sorted-prefix invariant, why it is fast on nearly sorted input, stability, and
  its role inside hybrid sorts as an OpenJDK implementation note only if verified).
- `algorithms/sorting/README.md`, "Choosing a sort": a comparison table of every
  sort in the repo and the JDK's `Arrays.sort`, `List.sort` and
  `Arrays.parallelSort` (stable, in place, worst case, extra space, when to pick
  it), plus a short question-first chooser.

**New figures:**
- `insertion-sort-shift`: one insertion step shifting larger values right and
  dropping the key into the gap; the sorted prefix grows by one.
- `heapsort-phases`: `heapSort` building a max-heap and then moving each maximum to
  the end; the sorted suffix grows.
- `counting-sort-counts`: `countingSort` counting values, then writing them back
  in order.
- `radix-lsd-passes`: `radixSort`'s idea on small numbers: stable passes from the
  least significant digit.
- `sort-stability`: equal keys with tags sorted by a stable sort and by an unstable
  one, showing the tag order kept or lost.

**Existing figures to place:** `bubble-swaps`, `merge-recursion`, `quick-partition`.
`heap-anatomy` leaves `heap-sort.md`.

- [ ] **Step 1:** Fixtures and failing Java test `VisualExamplesSortingTest`.
- [ ] **Step 2:** Implement the figures.
- [ ] **Step 3:** Render both themes, inspect, fix.
- [ ] **Step 4:** Upgrade the five lessons, write the two new lessons, place figures.
- [ ] **Step 5:** Fragment, full verification, commit
  `Illustrate and deepen the sorting lessons`.

### Task 9: Searching and selection

**Stem:** `searching`. **Category:** "Sorting and searching".

**Lessons owned:** `algorithms/searching/linear-search.md`, `binary-search.md`,
`selection.md`.
**New API:** `SearchAlgorithms.firstTrue(long lowInclusive, long highExclusive, LongPredicate monotone)`
returns the smallest value in `[lowInclusive, highExclusive)` for which the
predicate is true, or `highExclusive` when none is. Precondition (unchecked): the
predicate is false then true, changing at most once. `lowInclusive > highExclusive`
throws `IllegalArgumentException`; a null predicate throws `NullPointerException`.
The midpoint must not overflow for any `long` bounds. O(log(high − low)) predicate
calls. Tests in `SearchAlgorithmsTest`: a brute-force comparison on random
thresholds with a fixed seed, an empty range, all-false, all-true, bounds at
`Long.MIN_VALUE` and `Long.MAX_VALUE`, and the two exceptions.
**New lesson:** `algorithms/searching/binary-search-on-answer.md` (searching a
range of answers with a monotone feasibility test, using `firstTrue`; worked
example: the minimum capacity that ships a fixed list of weights within a number
of days; why monotonicity must be proved; the off-by-one cases).

**New figures:**
- `linear-scan-first-match`: `linearSearch` stopping at the first match among
  duplicates and scanning everything on a miss.
- `predicate-boundary`: a monotone false…true row and `firstTrue` narrowing to the
  boundary, on the lesson's capacity example.
- `quickselect-shrink`: `Selection.kthSmallest` partitioning and keeping only the
  side that holds index k.
- `kway-merge-heap`: `Selection.mergeSorted` taking the smallest head from a heap of
  list heads, with the tie rule.

**Existing figures to place:** `binary-bound`. `kmp-overlap` leaves
`linear-search.md` (Task 11 places it).

- [ ] **Step 1:** Write failing tests for `firstTrue`; implement it; make them pass.
- [ ] **Step 2:** Fixtures and failing Java test `VisualExamplesSearchingTest`;
  implement the figures.
- [ ] **Step 3:** Render both themes, inspect, fix.
- [ ] **Step 4:** Upgrade the three lessons, write the new lesson, place figures.
- [ ] **Step 5:** Fragment (including catalog, complexity and finder rows for
  `firstTrue`), full verification, commit
  `Add binary search on the answer and illustrate the searching lessons`.

### Task 10: Array patterns, divide and conquer, complexity

**Stem:** `pattern`. **Category:** "Array patterns".

**Lessons owned:** `algorithms/array-patterns/overview.md`,
`algorithms/divide-and-conquer/overview.md`, `divide-and-conquer/problems.md`,
`algorithms/complexity-analysis/time-complexity.md`, `space-complexity.md`.

**New figures:**
- `two-pointers-pair-sum`: `ArrayPatterns.pairWithSum` moving the two ends inward
  and why each move is safe.
- `sliding-window-distinct`: `longestSubstringWithoutRepeats` growing the window
  and jumping its left edge past a repeat.
- `monotonic-stack-next-greater`: `nextGreaterIndices` with the stack content and
  the answers resolved at each step.
- `difference-array-ranges`: `applyRangeAdds` marking range starts and ends, then
  one prefix pass.
- `interval-sweep-rooms`: `minimumMeetingRooms` sweeping start and end events and
  tracking the depth; touching intervals do not conflict.
- `inversion-count-merge`: `SequenceAlgorithms.countInversions` counting, during a
  merge, how many left items each right item jumps over.

**Existing figures to place:** `merge-recursion`, `quick-partition`,
`complexity-growth`.

- [ ] **Step 1:** Fixtures and failing Java test `VisualExamplesPatternTest`.
- [ ] **Step 2:** Implement the figures.
- [ ] **Step 3:** Render both themes, inspect, fix.
- [ ] **Step 4:** Upgrade the five lessons and place figures.
- [ ] **Step 5:** Fragment, full verification, commit
  `Illustrate and deepen the array-pattern, divide-and-conquer and complexity lessons`.

### Task 11: Strings

**Stem:** `string`. **Category:** "Strings".

**Lessons owned:** `algorithms/strings/pattern-matching.md`,
`multi-pattern-and-suffix-arrays.md`.

**New figures:**
- `z-box`: the Z-array of a short string and the Z-box reuse that avoids
  re-comparing characters.
- `rabin-karp-rolling`: the rolling hash window sliding by one character, a hash
  hit that verification confirms and one it rejects.
- `manacher-mirror`: `longestPalindromicSubstring` reusing the mirror position
  inside the current rightmost palindrome.
- `aho-corasick-links`: the `AhoCorasick` trie for a small pattern set with its
  failure links and the matches reported on one text.
- `suffix-array-lcp`: the sorted suffixes of a short word with the `SuffixArray`
  order and LCP values, and a pattern lookup as a range.

**Existing figures to place:** `kmp-overlap`.

- [ ] **Step 1:** Fixtures and failing Java test `VisualExamplesStringTest`.
- [ ] **Step 2:** Implement the figures.
- [ ] **Step 3:** Render both themes, inspect, fix.
- [ ] **Step 4:** Upgrade the two lessons and place figures.
- [ ] **Step 5:** Fragment, full verification, commit
  `Illustrate and deepen the string lessons`.

### Task 12: Dynamic programming, greedy and backtracking

**Stem:** `optimization`. **Category:** "Optimization".

**Lessons owned:** `algorithms/dynamic-programming/overview.md`, `problems.md`,
`classic-problems.md`, `algorithms/greedy/overview.md`, `greedy/problems.md`,
`greedy/huffman-coding.md`, `algorithms/backtracking/overview.md`,
`backtracking/problems.md`.
**New lesson:** `algorithms/dynamic-programming/README.md`, "Recognize the DP
family": linear, grid, interval, knapsack, subsequence, bitmask and
scheduling-with-binary-search families; for each, the state shape, a repo method
that is an instance, its cost, and the question that reveals the family.

**New figures:**
- `lcs-traceback`: the LCS table for two short strings with the traceback path that
  `longestCommonSubsequence` follows and its tie rule.
- `edit-distance-grid`: the edit-distance table with the path that `editScript`
  returns, labelled match, substitute, insert and delete.
- `matrix-chain-splits`: the split choices for `matrixChainOrder` on three or four
  matrices and the chosen parenthesization.
- `held-karp-masks`: Held–Karp states as visited-set and last-city pairs on four
  cities, and the optimal tour.
- `weighted-interval-choice`: `weightedIntervalScheduling` taking or skipping each
  interval using the last compatible one.
- `huffman-tree`: `huffmanCodeLengths` merging the two lightest nodes repeatedly,
  with the final code lengths.
- `subset-sum-halves`: `subsetSum` splitting the input in two halves and matching
  sums across them.

**Existing figures to place:** `coin-dp`, `knapsack-state`, `activity-selection`,
`queens-backtracking`.

- [ ] **Step 1:** Fixtures and failing Java test `VisualExamplesOptimizationTest`.
- [ ] **Step 2:** Implement the figures.
- [ ] **Step 3:** Render both themes, inspect, fix.
- [ ] **Step 4:** Upgrade the eight lessons, write the DP README, place figures.
- [ ] **Step 5:** Fragment, full verification, commit
  `Illustrate and deepen the DP, greedy and backtracking lessons`.

### Task 13: Math, randomness and bit manipulation

**Stem:** `math`. **Category:** "Math and bits".

**Lessons owned:** `algorithms/math/number-theory.md`, `randomized-sampling.md`.
**New lesson:** `algorithms/bit-manipulation/overview.md` (JDK-only examples:
`Integer.bitCount`, `lowestOneBit`, `numberOfTrailingZeros`, `Long.rotateLeft`,
`BitSet`; masks as small sets; testing, setting and clearing a bit; iterating the
set bits; iterating the subsets of a mask; XOR to find the unpaired value; signed
shift versus unsigned shift; the overflow of `1 << 31` and why `1L << n` needs a
long). Examples are short statements with assertions, not a utility class.

**New figures:**
- `euclid-steps`: `NumberTheory.gcd` as shrinking remainders, and the
  back-substitution that gives the `extendedGcd` coefficients.
- `modpow-squares`: `modPow` by repeated squaring, following the exponent's bits.
- `sieve-grid`: `primesUpTo` crossing out multiples on a small grid, starting each
  prime at its square.
- `reservoir-sampling`: `reservoirSample` keeping k items while the replacement
  chance falls as k/i.
- `alias-table`: `AliasTable` columns of equal height, each split between one item
  and its alias, built from four weights.
- `bitmask-subsets`: a small set as a bit mask, with test, set, clear, and the walk
  through the subsets of a mask.

- [ ] **Step 1:** Fixtures and failing Java test `VisualExamplesMathTest`.
- [ ] **Step 2:** Implement the figures.
- [ ] **Step 3:** Render both themes, inspect, fix.
- [ ] **Step 4:** Upgrade the two lessons, write the bit-manipulation lesson, place figures.
- [ ] **Step 5:** Fragment, full verification, commit
  `Illustrate the math lessons and add a bit-manipulation lesson`.

### Task 14: More interactive step-throughs

**Files:**
- Modify: `scripts/visuals/trace_data.py`, `scripts/visuals/test_assets.py`
  (trace invariant tests only), `docs/visuals/index.html` and the generator's
  index text where they state the number of step-throughs
- Create: `docs/visuals/data/examples-traces.properties` (keys start with `traces.`),
  `src/test/java/io/github/ichillous/dsa/visuals/VisualExamplesTracesTest.java`
- Modify `docs/visuals/app.js` only if a new trace cannot be expressed with the
  existing `array`, `dp` and `graph` views; keep it working offline and keyboard
  accessible.

**New traces** (ids and what each frame sequence shows):
- `insertion`: insertion sort; invariant: the prefix is sorted and the multiset is
  unchanged.
- `quickselect`: k-th smallest; invariant: the answer index stays inside the active
  range.
- `radix`: LSD radix passes on small numbers; invariant: after pass d the values
  are sorted by their last d digits.
- `two-pointers`: pair with target sum; invariant: no pair outside the pointers
  can be the answer.
- `window`: longest substring without repeats; invariant: the window has no
  repeated character.
- `z-function`: Z-array construction; invariant: computed entries equal the naive
  values.
- `dfs`: depth-first traversal; invariant: the stack is a path from the source.
- `topological`: Kahn's algorithm; invariant: every emitted vertex has all its
  predecessors emitted.

Each trace derives its frames from fixtures, has a final frame equal to the result
the Java API returns for that fixture (asserted in `VisualExamplesTracesTest`), and
has an invariant test in `test_assets.py` that reasons independently of the trace
builder.

- [ ] **Step 1:** Write the failing invariant tests and the Java fixture test.
- [ ] **Step 2:** Implement the eight trace builders.
- [ ] **Step 3:** Open the companion in Chromium through the review tool at 1440px
  and 390px in both themes, step through every new frame, and fix overflow or
  unreadable states.
- [ ] **Step 4:** Update the stated step-through counts; full verification; commit
  `Add eight interactive step-throughs`.

### Task 15: Folder landing pages with galleries

**Files:**
- Modify: `scripts/generate_visuals.py`, `scripts/visuals/test_assets.py`
- Create: `scripts/visuals/map_figures.py`, `docs/visuals/data/examples-map.properties`,
  `algorithms/README.md`, `data-structures/README.md`

**Interfaces:**
- **Gallery region.** `<!-- visual:gallery FOLDER -->` … `<!-- /visual:gallery -->`
  in a Markdown file. The generator fills it with one table per immediate
  subfolder of `FOLDER` (in name order). Each row is a lesson: a thumbnail of the
  lesson's first placed figure (light and dark sources, width 200, linking to the
  lesson), the lesson title from its first `#` heading as a link, and the "Use it
  when" cell of its At a glance table. A lesson with no figure or no At a glance
  table fails the build.
- **Map figures** (category "Maps"): `data-structures-map` (families of structures
  by the question they answer: by position, by key, by order, by priority, by
  prefix, by range, by connectivity, approximately) and `algorithms-map` (families
  of algorithms by task: search, sort, select, traverse, shortest path, connect,
  optimize, match text, compute). Each names the repo's main class per family.
- **Landing pages.** Each README has: a two-sentence purpose, its map figure, a
  "Start here" list of five lessons with one line each, the generated gallery, a
  link to the solution finder, the catalog, the complexity guide and the visual
  index.

- [ ] **Step 1:** Write failing tests for the gallery region (row per lesson,
  thumbnail and link resolve, failure on a lesson without a figure).
- [ ] **Step 2:** Implement the gallery region and the two map figures.
- [ ] **Step 3:** Write the two READMEs; regenerate; render the map figures and view
  the READMEs' HTML rendering through Chromium at 1280px and 390px.
- [ ] **Step 4:** Full verification; commit `Add gallery landing pages for the
  lesson folders`.

### Task 16: Integration, rendered review and evidence

**Files:**
- Modify: `README.md`, `docs/catalog.md`, `docs/complexity.md`,
  `docs/solution-finder.md`, `docs/learning-roadmap.md`, `docs/exercises.md`,
  `docs/migration.md`, `docs/visuals/verification.md`, `docs/visuals/design.md`,
  `docs/validation.md`, `scripts/visuals/test_assets.py`,
  `docs/visuals/data/browser-review.json`, `docs/visuals/previews/*.png`
- Delete: `docs/superpowers/fragments/`

- [ ] **Step 1: Merge the fragments** into the README topic map, catalog,
  complexity guide, solution finder (link new lessons and `firstTrue`; add
  cross-links between Task 6's and Task 7's graph lessons), roadmap and exercises.
- [ ] **Step 2: Add coverage tests** to `test_assets.py`: every figure is placed in
  at least one region; every `.md` under `algorithms/` and `data-structures/` has
  at least one figure region; the review report's SVG count equals the number of
  SVG assets and its issue list is empty.
- [ ] **Step 3: Run the rendered review** with `--all --write`, fix any issue it
  reports in the owning module, refresh the four preview screenshots, and commit
  the report.
- [ ] **Step 4: Update the stated counts** (figures, SVGs, step-throughs, lessons,
  tests) in `README.md`, `docs/visuals/verification.md` and `docs/visuals/design.md`
  from measured values.
- [ ] **Step 5: Full verification.** Then add a dated section to
  `docs/validation.md` with measured numbers only, and commit
  `Integrate the lesson and visual expansion and record its evidence`.

### Task 17: Whole-branch review

- [ ] **Step 1:** Two independent reviewers: one checks figures against code and
  renders (every new figure viewed in both themes), one checks the new prose
  (JDK claims, costs, mistakes, self-check answers) and cross-document consistency.
- [ ] **Step 2:** One fix wave for all confirmed findings, then one scoped re-review.
- [ ] **Step 3:** Final verification from a clean checkout, update the PR, merge.
