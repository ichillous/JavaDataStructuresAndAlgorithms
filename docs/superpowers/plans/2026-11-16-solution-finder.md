# Solution Finder and Breadth Expansion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make this repository a problem-first Java reference. Do this by adding a
100+ technique solution finder, about 50 new fully tested implementations with
lessons, and a correctness review of the existing material.

**Architecture:**
- New algorithms go in new focused classes inside the existing packages
  (`algorithms`, `collections`, `structures`, `graphs`). Only a few existing classes
  get additions, and each of those is owned by exactly one task.
- Each implementation task writes:
  - its own source, test and lesson files
  - a plain-text documentation fragment, which an integration task later merges
    into the shared docs (catalog, complexity, README, roadmap, exercises, finder)

  This lets implementation tasks run in parallel without conflicts.

**Tech Stack:** Java 17, Maven wrapper, JUnit Jupiter 5.13.4, JaCoCo, Python 3
(doc and visual checks). No runtime dependencies.

**Spec:** `docs/superpowers/specs/2026-11-16-solution-finder-design.md`

## Global Constraints

- Java 17 (`maven.compiler.release` 17); no new dependencies of any kind.
- `-Xlint:all -Werror`; Javadoc doclint `all,-missing` must pass, so every
  `{@code}`/HTML tag must be well-formed.
- JaCoCo bundle floors: lines >= 0.90 and branches >= 0.85. Every new class should
  itself reach about 95% lines and 90% branches.
- Every plain `java` fence in any `.md` file is compiled
  (`--release 17 -Xlint:all -Werror`) and executed with assertions enabled.
  - Contents: optional one-line imports, then statements, with at least one `assert`.
  - Use `text` fences for traces and pseudocode.
  - Never write `java` fences in plan, spec or fragment files.
- `python3 scripts/check_docs.py` must report 0 broken local links.
- Do not touch `docs/visuals/**`, `scripts/visuals/**`, or any
  `<!-- visual:generated -->` region.
- Preserve all existing topic paths and public APIs. Additions only.
- Repository conventions apply to every new API:
  - Utility classes are `final` with a private constructor.
  - Records validate in compact constructors.
  - Null arguments throw `NullPointerException` via
    `Objects.requireNonNull(x, "name")`. Collection elements, keys and values are
    non-null.
  - Returned lists are immutable (`List.copyOf`).
  - Ranges are half-open `[from, to)`.
  - Invalid indices throw `IndexOutOfBoundsException`. Other invalid arguments
    throw `IllegalArgumentException`.
  - Inputs are not mutated unless the method is documented as in-place.
  - Constructors defensively copy arrays and collections.
  - Graph traversals are iterative, with no recursion proportional to V.
  - Tie-breaking is deterministic and documented in Javadoc.
  - Randomized APIs take a `java.util.random.RandomGenerator` from the caller.
- Absent values:
  - Map and cache `get`, `put` and `remove` return `null` for "absent".
  - New queries return `Optional`, `OptionalInt` or `OptionalLong`.
  - Index arrays use `-1` for "no index", consistent with search.
  - Existing sentinels are unchanged.
- Javadoc for every public type and method states: purpose, invariant (for
  algorithms), null/empty/duplicate/bounds behavior, mutation and ownership,
  overflow policy, and time and auxiliary-space cost.
- Tests:
  - JUnit Jupiter, in the package matching the source.
  - At least one independent reference model per API (JDK collection, brute force
    on tiny inputs, or a different algorithm).
  - Fixed seeds, included in failure messages.
  - Assert that inputs are not mutated.
  - No timing assertions.
- Lesson shape:
  - title
  - purpose and preconditions
  - the invariant
  - a worked trace (`text` fence)
  - an executable example with assertions (`java` fence)
  - a costs table
  - a counterexample or pitfall
  - three "Practice" items
  - links to the catalog, the complexity guide and the solution finder

  Relative links must resolve from the lesson's directory.
- Fragment file per implementation task: `docs/superpowers/fragments/<task-id>.txt`.
  It is plain text, never `.md`. It contains:
  - (a) catalog table rows in the exact column format of `docs/catalog.md`
    (link cells use the form `[Name]` + `(../src/main/java/...)` exactly as existing rows do), plus the
    catalog section they belong to
  - (b) complexity table rows in the format of `docs/complexity.md`, plus the
    target table
  - (c) solution-finder entries this task implements: the problem heading and the
    technique, with the repo link to write
  - (d) one README topic-map addition
  - (e) one or two exercise rows in the `docs/exercises.md` format
- The work branch is `ccr-32ae1c43-isy7tl` (draft PR #1).
  - Implementation tasks run in isolated worktrees and commit there. They never push.
  - The controller merges, then pushes.
  - Commit messages end with the session's attribution lines.

## Review Focus

1. **Extreme values.** `Integer.MIN_VALUE`/`MAX_VALUE` and `Long.MIN_VALUE`/`MAX_VALUE`
   in new arithmetic must give the exact answer or throw `ArithmeticException`,
   never wrap. Owners and their tests:
   - Task 4: radix and counting sort extremes
   - Task 5: histogram area and difference array
   - Task 10: lazy segment tree
   - Task 13: max flow
   - Task 18: matrix chain
   - Task 19: gcd(`Long.MIN_VALUE`, 0) and modPow near `Long.MAX_VALUE`
2. **Empty and singleton inputs.** Zero-length arrays and strings, 0-vertex graphs,
   k = 0, and capacity 1 must have the documented answer, not crash. Every task
   has an `...EmptyAndSingleton...` test.
3. **Duplicates and ties.** Equal keys in selection, top-k, merge and sorting;
   equal weights in MST and matching; equal-cost reconstructions. Each must follow
   the documented tie rule. Tasks 3, 4, 12, 17 and 18 include tie-rule tests with
   expected outputs.
4. **Mutation and aliasing.**
   - Non-in-place methods leave the input unchanged.
   - Constructors copy their input, so later caller edits don't leak in. This
     applies to SparseTable, LazySegmentTree, AliasTable, AhoCorasick and
     SuffixArray.
   - Returned arrays and lists are copies or immutable.

   Every task asserts the input is unchanged, and constructor tasks mutate the
   source array after construction.
5. **Deep inputs.** A path graph or degenerate tree of 100,000 vertices must not
   throw `StackOverflowError`. Tasks 12, 13 and 14 each include a 100,000-vertex
   path test, and Task 8 inserts 100,000 ascending keys.

---

## Execution waves (controller)

- **Wave 1:**
  - Task 1 (review; read-only agents)
  - Task 2 (finder)
  - Tasks 3–5 (batch 1)
- **Wave 2:** Tasks 6–10 (batch 2).
- **Wave 3:** Tasks 11–16 (batches 3–4).
- **Wave 4:** Tasks 17–20 (batches 5–6).
- **After each wave:**
  1. Merge the worktree branches into `ccr-32ae1c43-isy7tl`.
  2. Run Task 21 (integration) for that wave's fragments.
  3. Run the full verification.
  4. Push.
- **Last:** Task 22 (whole-branch review), then a final Task 21 pass for
  validation evidence.

Full verification, which every task runs before reporting and the controller runs
after each merge:

```text
./mvnw -q -B clean verify                        -> exit 0 (tests, javadoc, coverage floors)
java -jar target/java-data-structures-and-algorithms-1.0.0-SNAPSHOT.jar   -> exit 0
for t in binary-search negative-edges range-queries lru; do java -jar target/*.jar learn $t; done -> exit 0
python3 scripts/check_docs.py                    -> "0 broken"
python3 scripts/generate_visuals.py --check      -> exit 0
python3 -m unittest discover -s scripts/visuals -p 'test_*.py'  -> OK
git diff --check                                 -> no output
```

Focused development checks:
- `./mvnw -q -B -Dtest=<ClassName>Test test`
- for lessons, `./mvnw -q -B -Dtest=DocumentedExamplesTest test`

---

### Task 1: Correctness review of existing material

**Files:**
- Read: all of `src/main/java/**`, `docs/catalog.md`, `docs/complexity.md`, the
  lessons, and `README.md`
- Modify: only files with confirmed defects
- Test: regression tests in the matching existing test class

**Interfaces:**
- Consumes: none
- Produces: none (behavior fixes only, with no signature changes)

- [ ] **Step 1: Dispatch three read-only reviewers in parallel.** Each gets the
  code, not this plan's conclusions. Scopes:
  - (a) `collections` + `structures`
  - (b) `algorithms`
  - (c) `graphs`, plus documentation claims in `docs/catalog.md`,
    `docs/complexity.md` and README "Contracts"

  Reviewers probe behavior with `jshell --class-path target/classes` and never run
  Maven. Each reports findings with:
  - file:line
  - a concrete failing input
  - the expected versus the actual result
- [ ] **Step 2: Reproduce each finding.** For each one, the controller writes a
  failing JUnit test in the existing test class, then runs
  `./mvnw -q -B -Dtest=<Class>Test test` and expects FAIL. Discard findings that
  don't reproduce, and record them as "not reproduced".
- [ ] **Step 3: Fix.** Make the minimal fix and rerun the focused test. Expected:
  PASS.
- [ ] **Step 4: Verify.** Run the full verification. Expected: all green.
- [ ] **Step 5: Commit** `Fix defects found in correctness review`. Record
  findings and fixes in the commit body. If there are none, add no commit and note
  "no confirmed defects" for Task 21.

### Task 2: Solution finder page

**Files:**
- Create: `docs/solution-finder.md`
- Modify: `README.md`, where one row goes at the top of the "Your next step" table:
  "Find every way to solve a problem" linking `docs/solution-finder.md`
- Modify: `docs/catalog.md`, where the intro gets one sentence linking the finder

**Interfaces:**
- Consumes: the existing catalog, plus the Java SE 17 API specification for JDK
  claims
- Produces: `docs/solution-finder.md`, with one `###` heading per problem and
  stable wording that Task 21 edits in place. Each table's columns are exactly:
  `| Option | Use when | Time / space | Java home | Watch out for |`.

- [ ] **Step 1: Write the top of the page.**
  - title
  - one-paragraph purpose
  - a "How to choose" checklist (six questions from the spec)
  - a status legend: `repo:` linked class, `JDK:` plain-text fully qualified
    class#method, `not implemented here`
  - a table of contents linking all 25 families
- [ ] **Step 2: Write all 25 families,** in spec order, with at least 100 distinct
  techniques in total. Each family has one or more `###` problem headings, each
  with one table.
  - Existing repo classes are linked to their source under `../src/main/java/`,
    plus the lesson.
  - Items that Tasks 3–20 will implement are written `not implemented here` for now.
  - JDK claims follow the Java SE 17 spec. Anything that is an implementation
    detail is prefixed "OpenJDK implementation note:".
  - Required correct statements:
    - `HashMap` is expected O(1), not worst case.
    - `Arrays.sort(int[])` is not stable; `Arrays.sort(Object[])` and `List.sort`
      are stable.
    - `TreeMap` is "a Red-Black tree based NavigableMap" with guaranteed log(n)
      `get`/`put`/`remove`/`containsKey`.
    - `PriorityQueue` is O(log n) offer/poll, O(n) `remove(Object)` and
      `contains`.
    - `ArrayDeque` is "likely to be faster than Stack" and `LinkedList`, with no
      capacity restrictions and nulls prohibited.
    - `LinkedHashMap(int, float, true)` iterates in access order and has
      `removeEldestEntry`.
    - `ConcurrentHashMap` retrievals don't block.
    - `Collections.shuffle` is described as Fisher–Yates only as an "OpenJDK
      implementation note". The spec says each permutation is equally likely
      given a fair source of randomness.
- [ ] **Step 3: Add 15–25 executable `java` fences** that prove JDK claims used in
  the page. Required set:
  - `TreeMap` `floorKey`/`ceilingKey`/`subMap`
  - bounded `PriorityQueue` top-k
  - access-order `LinkedHashMap` eviction via `removeEldestEntry` (anonymous
    subclass allowed in a statement)
  - `BitSet` set/`nextSetBit`/`cardinality`
  - `Collections.binarySearch` insertion point `-(ip)-1`
  - `ArrayDeque` as a stack and as a queue
  - `EnumMap` iteration order
  - `Arrays.sort` on objects keeping equal keys in order (stability)
  - `Math.addExact` throwing
  - `BigInteger.isProbablePrime`
  - `String.indexOf`
  - `Integer.bitCount`/`numberOfTrailingZeros`
  - `IntStream`/`Collectors.groupingBy` counting
  - `ConcurrentSkipListMap.ceilingKey`
  - `CopyOnWriteArrayList` snapshot iteration
  - `Collections.shuffle` with `new Random(seed)`, only asserting the
    permutation's multiset
- [ ] **Step 4: Verify the lesson examples.** Run
  `./mvnw -q -B -Dtest=DocumentedExamplesTest test`, then
  `python3 scripts/check_docs.py`. Expected: PASS and "0 broken".
- [ ] **Step 5: Run the full verification and commit** `Add a problem-first
  solution finder covering repo, JDK and known techniques`.

### Task 3: Selection, top-k, k-way merge, majority

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/algorithms/Selection.java`
- Create: `src/test/java/io/github/ichillous/dsa/algorithms/SelectionTest.java`
- Create: `algorithms/searching/selection.md`
- Create: `docs/superpowers/fragments/task-03.txt`

**Interfaces (`public final class Selection`):**
- `static int kthSmallest(int[] values, int k)`:
  - `k` is zero-based, checked with `Objects.checkIndex(k, values.length)`.
  - Input is not mutated; it works on a copy.
  - Introselect: quickselect with a median-of-three pivot and three-way
    partition. It falls back to median-of-medians when partition rounds exceed
    `2 * (32 - Integer.numberOfLeadingZeros(n))`.
  - O(n) worst case, O(n) auxiliary space (the copy).
- `static <T> List<T> smallestK(Collection<? extends T> values, int k, Comparator<? super T> order)`:
  - Returns the k smallest in ascending order.
  - Equal elements keep encounter order (stable).
  - `k < 0` throws IAE; `k >= size` returns all, sorted.
  - Bounded max-heap of size k: O(n log(k+1)) time, O(k) space.
- `static <T> List<T> mergeSorted(List<? extends List<? extends T>> lists, Comparator<? super T> order)`:
  - k-way merge with a heap.
  - Ties: lower list index first, then earlier position.
  - Throws IAE as soon as an input list is found out of order, which is checked in
    O(1) per element.
  - O(N log(k+1)) time.
- `static OptionalInt majorityElement(int[] values)`: Boyer–Moore vote plus a
  verification pass. Returns the value with count > n/2, or empty. O(n) time,
  O(1) space.

- [ ] **Step 1: Write the failing tests:**
  - `kthSmallestMatchesSortedCopy`: seed `0x5E1EC7`; 2,000 random arrays of length
    0..40 over [-5, 5] and over the full int range. Every valid k gives
    `sorted[k]`, and the input is unchanged.
  - `kthSmallestHandlesAdversarialShapes`: all-equal, ascending, descending and
    organ-pipe arrays of length 10,000; median `k = n/2` matches the sort.
  - `kthSmallestRejectsBadIndex`: `k = -1` and `k = n` throw
    `IndexOutOfBoundsException`; an empty array with k = 0 throws.
  - `smallestKIsStableAndBounded`:
    - records `(key, id)` with comparator on key; expect
      `stream().sorted(order).limit(k)`, which is stable
    - `k = 0` gives `[]`
    - `k > n` gives all elements
    - `k = -1` throws IAE
  - `mergeSortedMatchesSortedConcatenationAndTieRule`:
    - lists `[[1,3,5],[1,2],[],[5]]` with tagged elements: equal 1s come from list
      0 first, and equal 5s from list 0 before list 3
    - random check against stable sort of the concatenation
    - `[[2,1]]` throws IAE
  - `majorityElementMatchesCounting`:
    - `[3,3,4]` gives 3
    - `[1,2]` gives empty
    - `[]` gives empty
    - `[7]` gives 7
    - random check against a `HashMap` count
- [ ] **Step 2: Confirm the tests fail.** Run
  `./mvnw -q -B -Dtest=SelectionTest test`. Expected: compilation failure (class
  missing).
- [ ] **Step 3: Implement `Selection`.** Use the signatures above. Javadoc states
  the introselect invariant: after a partition, the k-th element lies in the
  segment that contains index k.
- [ ] **Step 4: Confirm the tests pass.** Run
  `./mvnw -q -B -Dtest=SelectionTest test`. Expected: PASS.
- [ ] **Step 5: Write the lesson** `algorithms/searching/selection.md`. Cover:
  - quickselect versus heap versus sort
  - why a deterministic pivot alone is O(n²) worst case
  - a worked partition trace of `[7, 2, 9, 4, 4, 1]`, k = 2, giving 4

  Then write the fragment `task-03.txt`.
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add selection, top-k, k-way merge and majority vote`.

### Task 4: Counting, radix and Comparator merge sort

**Files:**
- Modify: `src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java`
  (additions only)
- Modify: `src/test/java/io/github/ichillous/dsa/algorithms/SortAlgorithmsTest.java`
  (additions only)
- Create: `algorithms/sorting/non-comparison-and-comparator-sorts.md`
- Create: `docs/superpowers/fragments/task-04.txt`

**Interfaces (additions to `SortAlgorithms`):**
- `public static final int COUNTING_SORT_MAX_RANGE = 1 << 20;`
- `static void countingSort(int[] values)`:
  - In place.
  - Throws IAE when `(long) max - min + 1 > COUNTING_SORT_MAX_RANGE`, leaving the
    array unchanged.
  - O(n + r) time, O(r) space.
- `static void radixSort(int[] values)`:
  - In place, LSD, 4 passes of 8 bits.
  - Flips the sign bit to order negatives.
  - O(n) time, O(n) auxiliary space.
- `static <T> void mergeSort(T[] values, Comparator<? super T> order)`:
  - In place and stable.
  - Rejects null elements up front, leaving the array unchanged.
  - O(n log n) time, O(n) auxiliary space.

- [ ] **Step 1: Write the failing tests:**
  - `countingAndRadixMatchArraysSort`: seed `0xC0DA`; 1,000 arrays of length 0..60.
    Ranges: [-3, 3], [0, 1000], and the full int range (radix only). Each array
    equals `Arrays.sort` of a copy.
  - `radixSortOrdersExtremes`:
    `{MAX_VALUE, MIN_VALUE, 0, -1, 1, MIN_VALUE}` gives
    `{MIN, MIN, -1, 0, 1, MAX}`.
  - `countingSortRejectsHugeRangeWithoutMutation`: `{MIN_VALUE, MAX_VALUE}` throws
    IAE and the array is unchanged. `{5, 5 + (1 << 20) - 1}` succeeds.
  - `comparatorMergeSortIsStable`: records `(key, seq)` sorted by key match
    `Arrays.sort(copy, order)`, which the spec guarantees is stable, element for
    element. Empty and singleton arrays are unchanged. An array containing a null
    throws NPE and is unchanged.
- [ ] **Step 2: Confirm the tests fail.** Run
  `./mvnw -q -B -Dtest=SortAlgorithmsTest test`. Expected: compilation failure.
- [ ] **Step 3: Implement the three methods.**
- [ ] **Step 4: Confirm the tests pass.** Run the same command. Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** The lesson covers:
  - the Ω(n log n) comparison lower bound and why counting and radix avoid it
  - the memory/range trade-off
  - stability, and why a Comparator sort needs it
  - a trace of an LSD pass on `[170, 45, 75, 90, 2, 24, 802, 66]` in base 10, for
    readability
  - the pitfall `(a, b) -> a - b` overflowing
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add counting, radix and stable Comparator merge sort`.

### Task 5: Array and interval patterns

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/algorithms/ArrayPatterns.java`
- Create: `src/test/java/io/github/ichillous/dsa/algorithms/ArrayPatternsTest.java`
- Create: `algorithms/array-patterns/overview.md`
- Create: `docs/superpowers/fragments/task-05.txt`

**Interfaces (`public final class ArrayPatterns`):**
- `public record IndexPair(int first, int second)`: validates `0 <= first < second`.
- `public record Range(int startInclusive, int endExclusive)`: validates
  `0 <= start <= end`; may be empty.
- `public record RangeAdd(int fromInclusive, int toExclusive, long delta)`:
  validates `0 <= from <= to`.
- `static Optional<IndexPair> pairWithSum(int[] sorted, long target)`:
  - Two pointers; ascending input is an unchecked precondition.
  - Sums in `long`.
  - Returns the smallest first index that has any partner, paired with that
    index's largest partner.
  - O(n) time.
- `static Range longestSubstringWithoutRepeats(String text)`:
  - Variable sliding window over UTF-16 units.
  - Earliest start on ties.
  - `""` gives `Range(0, 0)`.
  - O(n) expected time.
- `static int[] nextGreaterIndices(int[] values)`: index of the next strictly
  greater element, or `-1`. Monotonic stack, O(n).
- `static long largestRectangle(int[] heights)`: heights must be >= 0, else IAE.
  `long` area. Monotonic stack, O(n).
- `static long[] applyRangeAdds(long[] values, List<RangeAdd> updates)`:
  - Returns a new array; the input is unchanged.
  - Difference array.
  - Exact arithmetic: throws `ArithmeticException` if any result is outside
    `long`. An intermediate prefix may leave range as long as the final value
    fits; use `Math.addExact` only on final values, with a `BigInteger` or
    128-bit check on the prefix.
  - `to > values.length` throws IOOBE.
  - O(n + u).
- `static List<GreedyAlgorithms.Interval> mergeIntervals(Collection<GreedyAlgorithms.Interval> intervals)`:
  - Coalesces intervals that overlap or touch.
  - Sorted by start; immutable result.
  - O(n log n).
- `static int minimumMeetingRooms(Collection<GreedyAlgorithms.Interval> intervals)`:
  - Maximum overlap depth; touching intervals don't conflict.
  - Empty input gives 0.
  - O(n log n).

- [ ] **Step 1: Write the failing tests:**
  - `pairWithSumMatchesBruteForceRule`:
    - `[1,2,3,4]` with target 5 gives `(0,3)`
    - `[]` gives empty
    - `[MAX_VALUE, MAX_VALUE]` with target `2L*MAX_VALUE` gives `(0,1)`
    - random sorted arrays (seed `0x2B0`) against a brute force taking the
      smallest i, then the largest j
  - `longestSubstringWithoutRepeatsUsesEarliestTie`:
    - `"abcabcbb"` gives (0,3)
    - `"bbbb"` gives (0,1)
    - `""` gives (0,0)
    - `"pwwkew"` gives (2,5)
    - random strings over `"abc"` against brute force
  - `nextGreaterIndicesMatchesScan`: `[2,1,2,4,3]` gives `[3,2,3,-1,-1]`;
    duplicates aren't "greater"; random check against an O(n²) scan.
  - `largestRectangleMatchesBruteForceAndLongArea`:
    - `[2,1,5,6,2,3]` gives 10
    - `[]` gives 0
    - `[MAX_VALUE, MAX_VALUE]` gives `2L*MAX_VALUE`
    - `[-1]` throws IAE
  - `applyRangeAddsMatchesNaive`:
    - `[0,0,0,0]` with `(1,3,+5)` and `(0,4,-1)` gives `[-1,4,4,-1]`
    - the result exceeding `Long.MAX_VALUE` throws `ArithmeticException`
    - an intermediate prefix overflow whose final values fit succeeds
    - the input is unchanged
    - random check against naive loops with `BigInteger`
  - `mergeIntervalsCoalescesTouching`:
    - `[1,3),[2,6),[8,10),[10,12)` gives `[1,6),[8,12)`
    - empty input gives empty
  - `minimumMeetingRoomsCountsDepth`:
    - `[0,30),[5,10),[15,20)` gives 2
    - `[1,2),[2,3)` gives 1
    - empty input gives 0
    - random check against a timeline sweep over all points
- [ ] **Step 2: Confirm the tests fail.** Run
  `./mvnw -q -B -Dtest=ArrayPatternsTest test`. Expected: compilation failure.
- [ ] **Step 3: Implement `ArrayPatterns`.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** The lesson covers:
  - two pointers (with the invariant that excluded indices have no partner)
  - variable sliding windows
  - monotonic stacks
  - difference arrays
  - interval sweep

  Include one trace per pattern.
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add two-pointer, window, monotonic-stack, difference and interval patterns`.

### Task 6: Open-addressing hash map

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/collections/OpenAddressingHashMap.java`
- Create: `src/test/java/io/github/ichillous/dsa/collections/OpenAddressingHashMapTest.java`
- Create: `data-structures/hash-tables/open-addressing.md`
- Create: `docs/superpowers/fragments/task-06.txt`

**Interfaces (`public final class OpenAddressingHashMap<K, V>`):**
- Same public surface and null/absent contract as `ChainedHashMap`:
  - `OpenAddressingHashMap()` and `OpenAddressingHashMap(int initialCapacity)`
    (validate exactly as `ChainedHashMap` does)
  - `V put(K, V)`, `V get(K)`, `boolean containsKey(K)`, `V remove(K)`
  - `int size()`, `boolean isEmpty()`, `void clear()`
- Linear probing over a power-of-two table:
  - Index from Fibonacci hashing of `h ^ (h >>> 16)`.
  - Resize when the load would exceed 2/3.
  - Removal uses backward-shift deletion, so there are no tombstones.
- The Javadoc invariant: every key is stored on its probe path from its home slot
  with no empty slot in between.

- [ ] **Step 1: Write the failing tests:**
  - `matchesHashMapUnderRandomOperations`: seed `0x0A0A`; 20,000 ops of
    put/get/remove/containsKey/clear over keys 0..199. Return values and size
    match `HashMap`.
  - `collidingKeysSurviveRemovalInsideClusters`:
    - a key class whose `hashCode` is constant, and one whose `hashCode` is
      `Integer.MIN_VALUE`
    - insert 50 keys, remove every third, and check every remaining lookup
    - check that keys whose probe wraps past the table end are still found after
      removing the cluster head
  - `rejectsNullsAndBadCapacity`: same exceptions as `ChainedHashMap`.
  - `clearThenReuse`.
- [ ] **Step 2: Confirm the tests fail.** Run
  `./mvnw -q -B -Dtest=OpenAddressingHashMapTest test`. Expected: compilation
  failure.
- [ ] **Step 3: Implement the class.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** The lesson covers:
  - chaining versus probing
  - load factor and expected probes, about ½(1 + 1/(1−α)²) for an unsuccessful
    search
  - a backward-shift deletion trace
  - why tombstones degrade
  - `IdentityHashMap` (OpenJDK implementation note: linear probing)
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add a linear-probing hash map with backward-shift deletion`.

### Task 7: LFU cache

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/collections/LfuCache.java`
- Create: `src/test/java/io/github/ichillous/dsa/collections/LfuCacheTest.java`
- Create: `data-structures/hash-tables/lfu-cache.md`
- Create: `docs/superpowers/fragments/task-07.txt`

**Interfaces (`public final class LfuCache<K, V>`):**
- Same surface as `LruCache`:
  - `LfuCache(int capacity)`, where capacity must be > 0, else IAE
  - `V get(K)`, `V put(K, V)`, `V remove(K)`
  - `int size()`, `int capacity()`, `boolean isEmpty()`, `void clear()`
- Plus `int frequency(K key)`, which returns 0 when absent and does not count as
  a use.
- Behavior:
  - `get` hits and replacing `put`s increment the frequency.
  - A new key starts at frequency 1.
  - When inserting a new key at capacity, evict the lowest frequency; ties go to
    the least recently used.
  - Expected O(1) for every operation, using a key map, frequency buckets of
    linked nodes, and a tracked minimum frequency.

- [ ] **Step 1: Write the failing tests:**
  - `evictsLeastFrequentThenLeastRecent`: capacity 2.
    1. `put(1,a)`, `put(2,b)`, `get(1)`, `put(3,c)` evicts 2.
    2. `get(3)`, `put(4,d)` evicts 1: both 1 and 3 have frequency 2, and 1 was
       used least recently.
  - `matchesBruteForceModel`: seed `0x1F0C`; 20,000 random ops over 12 keys,
    capacities 1..5, against a list model ordered by (frequency, last-use tick).
  - `replacementCountsAsUseAndReturnsOld`.
  - `capacityOneAndClear`.
  - `rejectsNullsAndNonPositiveCapacity`.
- [ ] **Step 2: Confirm the tests fail.** Run
  `./mvnw -q -B -Dtest=LfuCacheTest test`. Expected: compilation failure.
- [ ] **Step 3: Implement the class.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** The lesson covers:
  - LRU versus LFU: scan resistance versus stale popularity
  - a bucket trace
  - when to use JDK `LinkedHashMap` instead
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add an O(1) LFU cache with LRU tie-breaking`.

### Task 8: Red-black ordered map with rank and select

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/structures/RedBlackTreeMap.java`
- Create: `src/test/java/io/github/ichillous/dsa/structures/RedBlackTreeMapTest.java`
- Create: `data-structures/trees/red-black-trees.md`
- Create: `docs/superpowers/fragments/task-08.txt`

**Interfaces (`public final class RedBlackTreeMap<K extends Comparable<? super K>, V>`):**
- `V put(K, V)`, `V get(K)`, `boolean containsKey(K)`, `V remove(K)`
- `int size()`, `boolean isEmpty()`
- `Optional<K> firstKey()`, `Optional<K> lastKey()`
- `Optional<K> floorKey(K)`, `Optional<K> ceilingKey(K)`
- `int rank(K key)`: the number of stored keys strictly less than `key`; the key
  need not be present
- `K select(int rank)`: checked with `Objects.checkIndex`
- `int countInRange(K fromInclusive, K toExclusive)`: 0 when `from >= to`
- `List<K> keys()`: an ascending immutable snapshot
- `int height()`: counted in edges; −1 when empty
- Package-private `void checkInvariants()`. It throws `IllegalStateException` on
  any of:
  - BST order
  - a red root
  - a red node with a red child
  - unequal black height
  - a wrong subtree size
- Implementation: a left-leaning red-black tree (Sedgewick) with subtree sizes.
  Recursion depth is at most 2·log2(n+1).

- [ ] **Step 1: Write the failing tests:**
  - `matchesTreeMapUnderRandomOperations`: seed `0x2B7EE`; 30,000 ops of
    put/remove/get/floor/ceiling/rank/select/countInRange/first/last over keys
    −500..500, against `TreeMap`, with `checkInvariants()` after every mutation.
  - `ascendingInsertionsStayBalanced`: 100,000 ascending keys, then
    `height() <= 2 * log2(n + 1)`, `checkInvariants()`, and removing all keys
    leaves an empty map.
  - `emptyMapQueries`:
    - floor, ceiling, first and last are empty
    - `rank(x)` is 0
    - `select(0)` throws IOOBE
    - `height()` is −1
  - `rejectsNulls`.
- [ ] **Step 2: Confirm the tests fail.** Expected: compilation failure.
- [ ] **Step 3: Implement the class.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** The lesson covers:
  - the 2-3 tree correspondence
  - the three local fixes (rotate left, rotate right, flip colors), with a trace
    of inserting 1..5
  - why height ≤ 2 log2(n+1)
  - rank/select via subtree sizes
  - AVL versus red-black
  - `TreeMap` (spec: "Red-Black tree based")
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add a red-black ordered map with rank, select and range counts`.

### Task 9: Bloom filter and Count-Min sketch

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/structures/BloomFilter.java`
- Create: `src/main/java/io/github/ichillous/dsa/structures/CountMinSketch.java`
- Create: `src/test/java/io/github/ichillous/dsa/structures/BloomFilterTest.java`
- Create: `src/test/java/io/github/ichillous/dsa/structures/CountMinSketchTest.java`
- Create: `data-structures/probabilistic/overview.md`
- Create: `docs/superpowers/fragments/task-09.txt`

**Interfaces:**

`public final class BloomFilter<T>`:
- `BloomFilter(int bitCount, int hashCount)`: both must be >= 1.
- `static <T> BloomFilter<T> forExpectedInsertions(int n, double falsePositiveRate)`:
  - Requires `n >= 1` and `0 < p < 1`.
  - Sizing:
    - `m = ceil(-n ln p / (ln 2)^2)`, which must fit in an `int`, else IAE
    - `k = max(1, round(m / n * ln 2))`
- `void add(T)`, `boolean mightContain(T)`
- `int bitCount()`, `int hashCount()`
- `long insertions()`: counts `add` calls, including duplicates.
- `double estimatedFalsePositiveRate()`: `(1 - e^{-k·insertions/m})^k`.
- Hashing:
  - SplitMix64-mix `hashCode()` into two 32-bit halves `h1` and `h2`.
  - The i-th index is `floorMod(h1 + i·h2, m)` (Kirsch–Mitzenmacher).
  - Documented limitation: elements with equal `hashCode` are indistinguishable.

`public final class CountMinSketch<T>`:
- `CountMinSketch(int width, int depth)`: both must be >= 1.
- `static <T> CountMinSketch<T> withErrorBounds(double epsilon, double delta)`:
  - Requires `0 < ε` and `0 < δ < 1`.
  - `width = ceil(e/ε)`, `depth = ceil(ln(1/δ))`.
- `void add(T item)`, which means count 1.
- `void add(T item, long count)`:
  - Requires `count >= 0`, else IAE.
  - Overflow is checked before mutating, so a failed add leaves the sketch
    unchanged.
- `long estimate(T item)`: always >= the true count.
- `long totalCount()`
- `int width()`, `int depth()`
- Row hashes come from SplitMix64 of `hashCode() ^ rowSeed[i]`, using fixed
  documented seeds.

- [ ] **Step 1: Write the failing tests:**
  - `bloomSizingFormula`: `forExpectedInsertions(1000, 0.01)` gives
    `bitCount() == 9586` and `hashCount() == 7`.
  - `bloomHasNoFalseNegatives`: 10,000 random strings (seed `0xB100`), all
    `mightContain`.
  - `bloomFalsePositiveRateNearTarget`: n = 10,000, p = 0.01, 100,000 absent
    probes, observed rate <= 0.02.
  - `bloomRejectsBadParameters` and nulls.
  - `sketchSizingFormula`: `withErrorBounds(0.01, 0.01)` gives width 272, depth 5.
  - `sketchNeverUnderestimates`: a Zipf-like stream of 50,000 items (seed
    `0xC0C0`); for every item, `estimate >= true`.
  - `sketchErrorBoundHoldsForMostItems`: at least 99% of distinct items satisfy
    `estimate - true <= ε·total`.
  - `sketchOverflowLeavesStateUnchanged`: `add(x, Long.MAX_VALUE)` then `add(x, 1)`
    throws `ArithmeticException`, and the estimate and total are unchanged.
- [ ] **Step 2: Confirm the tests fail.** Expected: compilation failure.
- [ ] **Step 3: Implement both classes.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** The lesson covers:
  - one-sided errors (Bloom gives false positives only; Count-Min overestimates
    only)
  - the sizing math with worked numbers
  - no deletion in a plain Bloom filter
  - HyperLogLog and cuckoo filters named as "not implemented here"
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add a Bloom filter and a Count-Min sketch`.

### Task 10: Sparse table and lazy segment tree

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/structures/SparseTable.java`
- Create: `src/main/java/io/github/ichillous/dsa/structures/LazySegmentTree.java`
- Create: `src/test/java/io/github/ichillous/dsa/structures/SparseTableTest.java`
- Create: `src/test/java/io/github/ichillous/dsa/structures/LazySegmentTreeTest.java`
- Create: `data-structures/range-queries/overview.md`
- Create: `docs/superpowers/fragments/task-10.txt`

**Interfaces:**

`public final class SparseTable`:
- `SparseTable(long[] values)`: defensive copy, immutable.
- `int size()`
- `OptionalLong rangeMin(int fromInclusive, int toExclusive)`: empty for an empty
  range; bounds as in `SegmentTree`.
- O(n log n) build, O(1) query.

`public final class LazySegmentTree`:
- `LazySegmentTree(long[] values)`: defensive copy.
- `int size()`
- `void rangeAdd(int fromInclusive, int toExclusive, long delta)`
- `long rangeSum(int fromInclusive, int toExclusive)`: an empty range gives 0.
- Overflow contract:
  - Stored node sums and pending tags are always exact `long` values.
  - An update that would make any of them unrepresentable throws
    `ArithmeticException` and is rolled back with an undo log, so the tree is
    unchanged.
  - Queries never mutate (no push-down). They accumulate exactly, using
    `BigInteger` over O(log n) contributions, and throw `ArithmeticException` only
    when the requested sum is outside `long`.
- O(log n) update and query.

- [ ] **Step 1: Write the failing tests:**
  - `sparseTableMatchesAllRanges`: seed `0x5BA5`; random arrays of length 0..64;
    every `[l, r)` is checked against a scan. `Long.MAX_VALUE` is a valid minimum.
  - `sparseTableCopiesInput`.
  - `sparseTableRejectsBadBounds` (as in `SegmentTreeTest`).
  - `lazyTreeMatchesBigIntegerModel`: seed `0x1A27`; 5,000 random
    `rangeAdd`/`rangeSum` operations against a `BigInteger[]` model. Every result
    that fits in `long` must match. Every sum that doesn't fit must throw, and so
    must every update the model says makes a node sum unrepresentable, leaving all
    sums unchanged afterwards.
  - `lazyTreeEmptyAndSingleton`.
  - `lazyTreeCopiesInput`.
- [ ] **Step 2: Confirm the tests fail.** Expected: compilation failure.
- [ ] **Step 3: Implement both classes.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** The lesson covers:
  - static versus dynamic data
  - why a sparse table needs an idempotent operation (min works, sum doesn't)
  - a lazy-propagation trace of `rangeAdd(1, 4, +2)` on `[1, 2, 3, 4, 5]`
  - a decision table of prefix sums, Fenwick, segment tree, lazy segment tree and
    sparse table
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add a sparse table and a lazy range-add segment tree`.

### Task 11: Shortest-path toolkit

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/graphs/ShortestPathAlgorithms.java`
- Create: `src/test/java/io/github/ichillous/dsa/graphs/ShortestPathAlgorithmsTest.java`
- Create: `algorithms/graph/shortest-paths.md`
- Create: `docs/superpowers/fragments/task-11.txt`

**Interfaces (`public final class ShortestPathAlgorithms`):**
- `public static final class PathTree`: `int source()`,
  `OptionalLong distanceTo(int)`, `List<Integer> pathTo(int)`.
  - It has its own `reachable[]`, so signed distances are valid.
  - Bad vertices throw IOOBE.
- `public record Route(List<Integer> path, long distance)`
- `static PathTree zeroOneBfs(WeightedGraph g, int source)`:
  - Every weight must be 0 or 1, else IAE.
  - Deque-based; O(V+E).
- `static PathTree dagShortestPaths(WeightedGraph g, int source)`:
  - Directed and acyclic, else IAE.
  - Signed weights are allowed.
  - Exact arithmetic, as in Bellman–Ford: `ArithmeticException` if a reachable
    distance is outside `long`.
  - O(V+E).
- `static Optional<Route> aStar(WeightedGraph g, int source, int target, IntToLongFunction heuristic)`:
  - Every edge must be >= 0, else IAE (as in Dijkstra).
  - Requires `h(v) >= 0` and `h(target) == 0`.
  - Consistency `h(u) <= w(u,v) + h(v)` is checked on every relaxed edge; a
    violation throws IAE.
  - `f = g + h` saturates at `Long.MAX_VALUE`.
  - `g` follows Dijkstra's overflow policy.
  - Returns empty when the target is unreachable.
- `public static final class AllPairs`: `OptionalLong distance(int from, int to)`,
  `List<Integer> path(int from, int to)`, `int vertexCount()`.
- `static AllPairs floydWarshall(WeightedGraph g)`:
  - O(V³) time, O(V²) space.
  - Throws IAE if any negative cycle exists, including any negative undirected
    edge. Stops early once some `d[i][i] < 0`.
  - Results are exact; `ArithmeticException` if a finite shortest distance is
    outside `long`.
  - A reachable `Long.MAX_VALUE` is valid.

- [ ] **Step 1: Write the failing tests:**
  - `zeroOneBfsMatchesDijkstra`: seed `0x01B`; random 0/1 graphs, with distances
    equal to `GraphAlgorithms.dijkstra`. A weight of 2 throws IAE.
  - `dagShortestPathsMatchesBellmanFord`:
    - random DAGs with signed weights (seed `0xDA6`)
    - distances equal `AdvancedGraphAlgorithms.bellmanFord`
    - each path is valid and sums to its distance
    - a cycle or an undirected graph throws IAE
  - `aStarMatchesDijkstraWithManhattanOnGrid`: a 30×30 grid with blocked cells
    (seed `0xA57A`). Route distance equals Dijkstra's, and the path is valid.
  - `aStarRejectsInconsistentHeuristic`.
  - `floydWarshallMatchesBellmanFordFromEverySource`: random signed graphs without
    negative cycles.
  - `floydWarshallRejectsNegativeCycleAndNegativeUndirectedEdge`.
  - `floydWarshallLongBoundary`: path `0→1→2` with weights `MAX_VALUE` and `-1`
    gives `MAX_VALUE - 1`. A path summing to `MAX_VALUE + 1` throws
    `ArithmeticException`.
  - `emptyAndSingletonGraphs`.
- [ ] **Step 2: Confirm the tests fail.** Expected: compilation failure.
- [ ] **Step 3: Implement the class.** No changes to existing graph classes.
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** The lesson includes a decision
  table:

  | Graph | Algorithm |
  | --- | --- |
  | unweighted | BFS |
  | 0/1 weights | 0-1 BFS |
  | nonnegative weights | Dijkstra |
  | heuristic available | A* |
  | DAG | topological DP |
  | negative weights | Bellman–Ford |
  | all pairs | Floyd–Warshall |
  | sparse all-pairs | Johnson, "not implemented here" |

  Plus the longest-path-in-a-DAG negation trick, as an executable example.
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add 0-1 BFS, DAG shortest paths, A* and Floyd–Warshall`.

### Task 12: Graph structure and Prim

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/graphs/GraphStructureAlgorithms.java`
- Create: `src/test/java/io/github/ichillous/dsa/graphs/GraphStructureAlgorithmsTest.java`
- Create: `algorithms/graph/graph-structure.md`
- Create: `algorithms/graph/minimum-spanning-trees.md`
- Create: `docs/superpowers/fragments/task-12.txt`

**Interfaces (`public final class GraphStructureAlgorithms`):**
- `static List<Integer> articulationPoints(Graph undirected)`:
  - Ascending order; iterative lowlink.
  - A directed graph throws IAE.
- `public record Bipartition(List<Integer> left, List<Integer> right)`: both
  ascending.
- `static Optional<Bipartition> bipartition(Graph undirected)`:
  - BFS from ascending uncolored vertices; each component's smallest vertex goes
    on the left.
  - Empty when not bipartite.
- `static Optional<List<Integer>> oddCycle(Graph undirected)`:
  - A vertex cycle `v0..v(k-1)` with k odd, where consecutive vertices and
    `v(k-1)–v0` are edges. A self-loop gives `[v]`.
  - Empty when bipartite.
- `static Optional<List<Integer>> directedCycle(Graph directed)`:
  - The same cycle format, with edges `vi→v(i+1)` and `v(k-1)→v0`.
  - The first cycle found by iterative DFS from ascending vertices.
  - An undirected graph throws IAE.
- `static Optional<List<Integer>> eulerianPath(Graph g)`:
  - Directed or undirected; Hierholzer, iterative.
  - Returns `E+1` vertices using every edge exactly once. A graph with no edges
    gives `Optional.of(List.of())`.
  - Start vertex:
    - directed: the vertex with out − in = 1, else the smallest vertex with an edge
    - undirected: the smallest odd-degree vertex, else the smallest vertex with an
      edge
  - Empty when no Euler path exists. Respects `Graph`'s self-loop rule (one
    adjacency entry).
- `static GraphAlgorithms.SpanningForest primMinimumSpanningForest(WeightedGraph undirected)`:
  - Same contract as Kruskal: negative weights allowed, a forest when the graph is
    disconnected, loops omitted, exact total via `BigInteger`.
  - A directed graph throws IAE.

- [ ] **Step 1: Write the failing tests:**
  - `articulationPointsMatchRemovalOracle`: random graphs (seed `0xA77`).
    Removing a reported vertex increases the component count, and removing an
    unreported one doesn't.
  - `bipartitionAndOddCycleAgreeWithBruteForceColoring`: on graphs with V <= 10,
    either the partition is valid or the odd cycle is valid, and exactly one of
    them is present.
  - `directedCycleWitnessIsValidAndAbsentForDags`: compare against
    `GraphAlgorithms.topologicalSort` throwing.
  - `eulerianPathUsesEveryEdgeOnce`:
    - directed `0→1→2→0, 2→3` gives a 5-vertex path starting at 0
    - undirected star `0-1, 0-2, 0-3` gives empty
    - random Euler-friendly graphs are validated by consuming an edge multiset
  - `primMatchesKruskalTotalAndComponents`: random signed graphs give equal
    totals and component counts, and every result is a valid forest.
  - `deepPathGraphsDoNotOverflowStack`: a 100,000-vertex path for articulation
    points, bipartition and Euler.
  - `emptyAndSingletonGraphs`.
- [ ] **Step 2: Confirm the tests fail.** Expected: compilation failure.
- [ ] **Step 3: Implement the class.** No changes to existing classes.
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the two lessons and the fragment.**
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add articulation points, bipartite checks, cycles, Euler paths and Prim`.

### Task 13: Max flow, min cut, bipartite matching

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/graphs/FlowNetwork.java`
- Create: `src/main/java/io/github/ichillous/dsa/graphs/BipartiteMatching.java`
- Create: `src/test/java/io/github/ichillous/dsa/graphs/FlowNetworkTest.java`
- Create: `src/test/java/io/github/ichillous/dsa/graphs/BipartiteMatchingTest.java`
- Create: `algorithms/graph/flows-and-matching.md`
- Create: `docs/superpowers/fragments/task-13.txt`

**Interfaces:**

`public final class FlowNetwork`:
- `FlowNetwork(int vertexCount)`
- `public record Edge(int from, int to, long capacity)`
- `FlowNetwork addEdge(int from, int to, long capacity)`: capacity must be >= 0;
  parallel edges are kept.
- `List<Edge> edges()`, `int vertexCount()`
- `public record MaxFlow(long value, List<Long> edgeFlows, List<Integer> sourceSide)`:
  - `edgeFlows` is indexed by edge insertion order.
  - `sourceSide` is the ascending list of vertices reachable in the final
    residual graph.
- `MaxFlow maxFlow(int source, int sink)`:
  - Dinic, with iterative blocking-flow DFS. O(V²E).
  - `source == sink` throws IAE.
  - The network is not mutated.
  - Flow accumulates with `Math.addExact`; `ArithmeticException` only if the max
    flow exceeds `long`.

`public final class BipartiteMatching`:
- `public record Pair(int left, int right)`
- `static List<Pair> maximumMatching(int leftSize, int rightSize, Collection<Pair> edges)`:
  - Hopcroft–Karp, O(E√V).
  - Duplicate edges are ignored; out-of-range vertices throw IOOBE.
  - The result is sorted by `left`.

- [ ] **Step 1: Write the failing tests:**
  - `maxFlowEqualsBruteForceMinCut`: seed `0xF10`; random networks with V <= 8;
    the value equals the minimum over all s-t cuts.
  - `flowIsFeasibleAndCutIsTight`: conservation and capacity hold, and the cut
    capacity of `sourceSide` equals the value.
  - `unreachableSinkHasZeroFlow`.
  - `sourceEqualsSinkRejected`.
  - `largeCapacitiesAreExact`: two parallel paths of capacity `Long.MAX_VALUE/2`.
  - `deepPathNetwork`: a 100,000-vertex path with no `StackOverflowError`.
  - `matchingSizeEqualsBruteForce`: random bipartite graphs with at most 7 per
    side; the result is valid (each vertex at most once, edges exist).
  - `matchingEmptyAndSingleton`.
- [ ] **Step 2: Confirm the tests fail.** Expected: compilation failure.
- [ ] **Step 3: Implement both classes.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** The lesson covers:
  - residual graphs
  - max-flow/min-cut
  - matching as flow
  - min-cost flow and the Hungarian algorithm, named as "not implemented here"
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add Dinic max flow with min cut and Hopcroft–Karp matching`.

### Task 14: Tree queries

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/graphs/LowestCommonAncestor.java`
- Create: `src/main/java/io/github/ichillous/dsa/graphs/TreeAlgorithms.java`
- Create: `src/test/java/io/github/ichillous/dsa/graphs/TreeQueriesTest.java`
- Create: `algorithms/graph/tree-queries.md`
- Create: `docs/superpowers/fragments/task-14.txt`

**Interfaces:**

`public final class LowestCommonAncestor`:
- `LowestCommonAncestor(Graph tree, int root)`:
  - The graph must be an undirected tree (connected, V−1 edges), else IAE.
  - Binary lifting with an iterative BFS build. O(V log V).
- `int lca(int a, int b)`, `int depth(int v)`, `int distance(int a, int b)`
- `OptionalInt kthAncestor(int v, int k)`: empty when `k > depth(v)`; `k < 0`
  throws IAE.

`public final class TreeAlgorithms`:
- `static List<Integer> diameterPath(Graph tree)`:
  - Double BFS: from vertex 0, then from the farthest vertex. Ties go to the
    smallest vertex.
  - V = 0 throws IAE; V = 1 gives `[0]`.

- [ ] **Step 1: Write the failing tests:**
  - `lcaMatchesNaiveParentWalk`: random trees (seed `0x1CA`) with V <= 200; all
    pairs are checked, along with distances and kth ancestors.
  - `rejectsNonTrees`: a cycle, a disconnected graph, a directed graph and a bad
    root.
  - `diameterMatchesAllPairsBfs`: random trees.
  - `deepPathTree`: 100,000 vertices; `lca`, `distance` and `diameterPath` length.
  - `singletonTree`.
- [ ] **Step 2: Confirm the tests fail.** Expected: compilation failure.
- [ ] **Step 3: Implement both classes.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** The lesson covers binary lifting
  and the diameter proof sketch, and names Euler tour and heavy-light
  decomposition as "not implemented here".
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add binary-lifting LCA and tree diameter`.

### Task 15: Z-function, Rabin–Karp, Manacher

**Files:**
- Modify: `src/main/java/io/github/ichillous/dsa/algorithms/StringAlgorithms.java`
  (additions only)
- Modify: `src/test/java/io/github/ichillous/dsa/algorithms/StringAlgorithmsTest.java`
  (additions only)
- Create: `algorithms/strings/pattern-matching.md`
- Create: `docs/superpowers/fragments/task-15.txt`

**Interfaces (additions to `StringAlgorithms`):**
- `public record Span(int startInclusive, int endExclusive)`: validates
  `0 <= start <= end`.
- `static int[] zFunction(String text)`: `z[0] = text.length()` by documented
  convention. O(n).
- `static List<Integer> zFindAll(String text, String pattern)`:
  - Same results and empty-pattern semantics as `kmpFindAll`.
  - Uses the z-array of `pattern + text` with `z[i] >= p`, so no separator is
    needed.
- `static List<Integer> rabinKarpFindAll(String text, String pattern)`:
  - Same results as `kmpFindAll`; every hash hit is verified, so there are no
    false positives.
  - Fixed base and modulus are documented.
  - Expected O(t + p), worst O(t·p).
- `static Span longestPalindromicSubstring(String text)`:
  - Manacher, O(n). Earliest start on ties.
  - `""` gives `Span(0, 0)`.
  - Works on UTF-16 units.

- [ ] **Step 1: Write the failing tests:**
  - `zFunctionMatchesNaive`: `"aabxaab"` gives `[7,1,0,0,3,1,0]`; random check
    against a naive version (seed `0x2F`).
  - `zAndRabinKarpMatchKmp`: random texts and patterns over `"ab"`; also
    `"aaaaa"`/`"aaa"` gives `[0,1,2]`, and an empty pattern gives `0..t`.
  - `rabinKarpVerifiesCollisions`: an adversarial pair with equal hash values
    under the documented parameters is computed in the test by brute-force
    search over short strings, and is not reported as a match.
  - `longestPalindromeMatchesBruteForceEarliestTie`: `"babad"` gives (0,3);
    `"cbbd"` gives (1,3); `"abc"` gives (0,1); `""` gives (0,0).
- [ ] **Step 2: Confirm the tests fail.** Expected: compilation failure.
- [ ] **Step 3: Implement the additions.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** The lesson covers:
  - a decision table of `String.indexOf`, KMP, Z, Rabin–Karp, Aho–Corasick and
    suffix array
  - Boyer–Moore–Horspool, named as "not implemented here"

  Link to Task 16's lesson by path:
  `multi-pattern-and-suffix-arrays.md`.
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add Z-function, verified Rabin–Karp and Manacher`.

### Task 16: Aho–Corasick and suffix array

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/algorithms/AhoCorasick.java`
- Create: `src/main/java/io/github/ichillous/dsa/algorithms/SuffixArray.java`
- Create: `src/test/java/io/github/ichillous/dsa/algorithms/AhoCorasickTest.java`
- Create: `src/test/java/io/github/ichillous/dsa/algorithms/SuffixArrayTest.java`
- Create: `algorithms/strings/multi-pattern-and-suffix-arrays.md`
- Create: `docs/superpowers/fragments/task-16.txt`

**Interfaces:**

`public final class AhoCorasick`:
- `AhoCorasick(Collection<String> patterns)`:
  - Copies its input; patterns must be non-empty, else IAE.
  - Duplicates collapse.
- `public record Match(int startInclusive, String pattern)`
- `List<Match> findAll(String text)`:
  - Overlaps are included.
  - Ordered by start, then by pattern length ascending.
  - O(t + matches) after building the automaton.

`public final class SuffixArray`:
- `SuffixArray(String text)`: prefix doubling with counting sort, O(n log n).
- `int[] suffixes()`: a copy.
- `int[] lcp()`: Kasai; `lcp[i] = LCP(suffix[i-1], suffix[i])`, with `lcp[0] = 0`.
- `List<Integer> occurrences(String pattern)`:
  - Ascending.
  - An empty pattern gives `0..n`, matching the `kmpFindAll` convention.
  - O(p log n + k).
- `long distinctSubstrings()`: `n(n+1)/2 − Σlcp`.
- `String longestRepeatedSubstring()`: ties go to the earliest in suffix-array
  order; `""` when there is none.

- [ ] **Step 1: Write the failing tests:**
  - `ahoCorasickMatchesPerPatternKmp`:
    - `{"he","she","his","hers"}` on `"ushers"` gives `(1,"she")`, `(2,"he")`,
      `(2,"hers")`
    - random check against the union of `StringAlgorithms.kmpFindAll`, sorted by
      the documented rule (seed `0xAC`)
    - an empty pattern throws IAE
    - the patterns collection is mutated after construction, and the automaton
      ignores the change
  - `suffixArrayMatchesSortedSuffixes`:
    - `"banana"` gives `[5,3,1,0,4,2]` with lcp `[0,1,3,0,0,2]`
    - random check against sorting `substring(i)` (seed `0x5A`)
  - `occurrencesMatchKmp`.
  - `distinctSubstringsMatchesHashSet`: brute force on n <= 30.
  - `longestRepeatedSubstring`: `"banana"` gives `"ana"`; `"abc"` gives `""`.
  - `emptyText`.
- [ ] **Step 2: Confirm the tests fail.** Expected: compilation failure.
- [ ] **Step 3: Implement both classes.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** The lesson names the suffix
  automaton and Burrows–Wheeler as "not implemented here".
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add Aho–Corasick and a suffix array with LCP`.

### Task 17: DP solution reconstruction

**Files:**
- Modify: `src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java`
  (additions only)
- Modify: `src/test/java/io/github/ichillous/dsa/algorithms/DynamicProgrammingTest.java`
  (additions only)
- Modify: `algorithms/dynamic-programming/problems.md`: append a "Recovering the
  solution, not just its value" section
- Create: `docs/superpowers/fragments/task-17.txt`

**Interfaces (additions to `DynamicProgramming`):**
- `static String longestCommonSubsequence(String a, String b)`:
  - Backtracking rule: equal characters are taken; otherwise move up (`i--`) when
    `dp[i-1][j] >= dp[i][j-1]`, else left.
  - O(mn) time and space.
- `public enum EditOperation { MATCH, SUBSTITUTE, INSERT, DELETE }`
- `public record EditStep(EditOperation operation, int sourceIndex, int targetIndex)`:
  the indices are cursor positions; `MATCH`/`SUBSTITUTE` consume both strings.
- `static List<EditStep> editScript(String source, String target)`:
  - The non-`MATCH` count equals `editDistance`.
  - Tie preference: diagonal, then `DELETE`, then `INSERT`.
- `static List<Integer> knapsack01Items(int[] weights, int[] values, int capacity)`:
  - Ascending item indices.
  - Value sum equals `knapsack01`; weight sum <= capacity.
  - Deterministic backtracking rule, documented.
  - O(nW) time, O(nW) bits.
- `static Optional<List<Integer>> minimumCoinsChoice(int[] coins, int amount)`:
  - Coin values in ascending order; size equals `minimumCoins`.
  - Empty when impossible; `Optional.of(List.of())` for amount 0.
- `static List<Integer> longestIncreasingSubsequenceIndices(int[] values)`:
  - Strictly increasing, O(n log n).
  - The documented rule picks, among optimal subsequences, the one patience
    sorting with lower bound reconstructs. The test pins examples.

- [ ] **Step 1: Write the failing tests:**
  - `lcsStringFollowsRule`: `("ABCBDAB", "BDCABA")` gives length 4 and the exact
    string the rule yields, which the test writes as the literal. Random check:
    the result is a common subsequence and its length equals
    `longestCommonSubsequenceLength` (seed `0x1C5`).
  - `editScriptReplaysToTarget`: `("kitten", "sitting")` gives 3 non-match steps;
    replaying the script on the source yields the target; random check.
  - `knapsackItemsAchieveOptimum`: brute force over subsets with n <= 12.
  - `coinsChoiceMatchesMinimum`: `[1,3,4]` with 6 gives `[3,3]`; impossible gives
    empty; 0 gives `[]`.
  - `lisIndicesValidAndOptimal`: `[10,9,2,5,3,7,101,18]` gives a length-4 pinned
    index list; random check against the O(n²) DP length.
  - Each test also asserts that the inputs are unchanged.
- [ ] **Step 2: Confirm the tests fail.** Expected: compilation failure.
- [ ] **Step 3: Implement the additions.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson section and fragment.** Cover parent pointers
  versus re-deriving from the table, and Hirschberg (named as "not implemented
  here") for O(min(m, n)) space.
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Return reconstructed solutions for LCS, edits, knapsack, coins and LIS`.

### Task 18: Classic DP problems and Huffman coding

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgrammingClassics.java`
- Create: `src/test/java/io/github/ichillous/dsa/algorithms/DynamicProgrammingClassicsTest.java`
- Modify: `src/main/java/io/github/ichillous/dsa/algorithms/GreedyAlgorithms.java`
  (additions only)
- Modify: `src/test/java/io/github/ichillous/dsa/algorithms/GreedyAlgorithmsTest.java`
  (additions only)
- Create: `algorithms/dynamic-programming/classic-problems.md`
- Create: `algorithms/greedy/huffman-coding.md`
- Create: `docs/superpowers/fragments/task-18.txt`

**Interfaces:**

`public final class DynamicProgrammingClassics`:
- `static long countCoinCombinations(int[] coins, int amount)`:
  - Counts unordered combinations; coins must be positive, and duplicates collapse.
  - Checked arithmetic, else `ArithmeticException`.
  - Amount 0 gives 1.
- `static Optional<List<String>> wordBreak(String text, Collection<String> dictionary)`:
  - Empty words throw IAE.
  - Picks the segmentation with the shortest possible first word, recursively
    (documented).
  - `""` gives `Optional.of(List.of())`.
- `static int longestPalindromicSubsequenceLength(String text)`: O(n²) time, O(n)
  space.
- `public record ChainOrder(long cost, String parenthesization)`
- `static ChainOrder matrixChainOrder(int[] dimensions)`:
  - Requires length >= 2 and positive dimensions.
  - Matrices are named `A1..An`; the format is `"((A1A2)A3)"`, and a single
    matrix is `"A1"`.
  - Ties go to the smallest split.
  - Checked arithmetic.
- `public record Tour(List<Integer> order, long cost)`
- `static Tour travelingSalesman(long[][] distances)`:
  - Held–Karp; `n` must be between 1 and 16.
  - The matrix must be square, with every entry in `[-2^47, 2^47]`.
  - The order starts at 0 with no repeat, and the cost includes the return edge.
  - `n = 1` gives `([0], 0)`.
- `public record WeightedInterval(int start, int end, long weight)`: `start < end`
  and `weight >= 0`.
- `public record Schedule(List<WeightedInterval> chosen, long totalWeight)`
- `static Schedule weightedIntervalScheduling(Collection<WeightedInterval> intervals)`:
  - Half-open intervals; touching ones are compatible.
  - Checked total.
  - The chosen list is sorted by end.
  - O(n log n).

Additions to `GreedyAlgorithms`:
- `static int[] huffmanCodeLengths(long[] frequencies)`:
  - Frequencies must be >= 0.
  - Zero-frequency symbols get 0.
  - A single used symbol gets 1.
  - Ties: lower symbol index or earlier-created node first.
- `static List<String> canonicalCodes(int[] codeLengths)`:
  - Canonical Huffman codes; `""` for length 0.
  - Lengths must satisfy Kraft ≤ 1, else IAE.

- [ ] **Step 1: Write the failing tests:**
  - `coinCombinations`:
    - `[1,2,5]` with 5 gives 4
    - `[2]` with 3 gives 0
    - amount 0 gives 1
    - overflow throws, using `[1,2,...,60]` with a large amount
    - random check against brute-force enumeration
  - `wordBreak`:
    - `"applepenapple"` with `{apple, pen}` gives `[apple, pen, apple]`
    - `"catsandog"` gives empty
    - a shortest-first-word example pinned
  - `palindromicSubsequence`: `"bbbab"` gives 4; random check against brute force.
  - `matrixChain`: `[10,30,5,60]` gives `(4500, "((A1A2)A3)")`; random check
    against brute force over parenthesizations.
  - `heldKarpMatchesPermutationBruteForce`: n <= 8 (seed `0x75F`); also n = 1 and
    bounds rejection.
  - `weightedIntervalsMatchBruteForce`: n <= 12.
  - `huffmanLengthsAreOptimal`: `{5,9,12,13,16,45}` gives weighted length 224.
    Random check for n <= 6 against brute-force enumeration of length vectors
    satisfying Kraft. Edge cases: a single symbol and all zeros.
  - `canonicalCodesArePrefixFree`.
- [ ] **Step 2: Confirm the tests fail.** Expected: compilation failure.
- [ ] **Step 3: Implement the classes.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the two lessons and the fragment.** The classic-problems
  lesson includes a "state, transition, order, answer" table per problem. The
  Huffman lesson includes an exchange-argument sketch and names run-length
  encoding as "not implemented here".
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add classic DP problems, weighted interval scheduling and Huffman coding`.

### Task 19: Number theory

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/algorithms/NumberTheory.java`
- Create: `src/test/java/io/github/ichillous/dsa/algorithms/NumberTheoryTest.java`
- Create: `algorithms/math/number-theory.md`
- Create: `docs/superpowers/fragments/task-19.txt`

**Interfaces (`public final class NumberTheory`):**
- `static long gcd(long a, long b)`:
  - Nonnegative; `gcd(0, 0) = 0`.
  - `ArithmeticException` when the result is `2^63` (for example
    `gcd(MIN_VALUE, 0)`).
- `static long lcm(long a, long b)`: checked; `lcm(0, x) = 0`.
- `public record Bezout(long gcd, long x, long y)`: `a·x + b·y = gcd`.
- `static Bezout extendedGcd(long a, long b)`: checked.
- `static long modPow(long base, long exponent, long modulus)`:
  - Requires `modulus >= 1` and `exponent >= 0`.
  - The result is in `[0, modulus)`.
  - Exact for every positive `long` modulus, using 128-bit-safe multiplication.
- `static OptionalLong modInverse(long a, long modulus)`: empty when
  `gcd != 1`.
- `static List<Integer> primesUpTo(int limit)`:
  - Sieve of Eratosthenes.
  - Requires `0 <= limit <= 50_000_000`, else IAE.
  - O(n log log n) time, O(n) bits.
- `static boolean isPrime(long n)`: deterministic Miller–Rabin with bases
  2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, correct for every `long`.
- `static long fibonacciMod(long n, long modulus)`: 2×2 matrix exponentiation,
  O(log n).

- [ ] **Step 1: Write the failing tests:**
  - `gcdLcmMatchBigInteger`: random values and extremes (seed `0x6CD`).
    `gcd(MIN_VALUE, 0)` throws; `gcd(MIN_VALUE, 2)` gives 2.
  - `extendedGcdSatisfiesIdentity`.
  - `modPowMatchesBigInteger`: random values plus modulus `Long.MAX_VALUE` and
    `2^62 + 1`.
  - `modInverseMatchesBigInteger`, including the non-invertible case.
  - `sieveMatchesTrialDivision`: up to 10,000; `primesUpTo(0)` and `(1)` give
    `[]`; `(2)` gives `[2]`.
  - `isPrimeMatchesBigInteger`:
    - Carmichael numbers 561 and 41041
    - strong pseudoprime 3215031751
    - `2^61 − 1` is prime
    - `Long.MAX_VALUE` is composite
    - random check against `BigInteger.isProbablePrime(50)`
  - `fibonacciModMatchesIteration`: `F(90) mod (2^61 − 1)` equals the exact
    `BigInteger` value modulo.
- [ ] **Step 2: Confirm the tests fail.** Expected: compilation failure.
- [ ] **Step 3: Implement the class.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** Point to JDK `BigInteger` and
  `Math.*Exact`.
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add number-theory utilities with exact 64-bit arithmetic`.

### Task 20: Randomized sampling

**Files:**
- Create: `src/main/java/io/github/ichillous/dsa/algorithms/Sampling.java`
- Create: `src/main/java/io/github/ichillous/dsa/algorithms/AliasTable.java`
- Create: `src/test/java/io/github/ichillous/dsa/algorithms/SamplingTest.java`
- Create: `algorithms/math/randomized-sampling.md`
- Create: `docs/superpowers/fragments/task-20.txt`

**Interfaces:**

`public final class Sampling`:
- `static <T> List<T> reservoirSample(Iterable<? extends T> items, int k, RandomGenerator random)`:
  - Algorithm R.
  - `k < 0` throws IAE.
  - Returns `min(k, n)` items. When `n <= k`, they come back in encounter order.
- `static void shuffle(int[] values, RandomGenerator random)`: in-place
  Fisher–Yates.

`public final class AliasTable`:
- `AliasTable(double[] weights)`:
  - Copies its input; Vose's method.
  - Weights must be finite and >= 0 with a positive sum, else IAE.
- `int sample(RandomGenerator random)`: O(1).
- `int size()`
- `double probability(int index)`: the normalized weight.

- [ ] **Step 1: Write the failing tests:**
  - `reservoirIsUniform`: n = 10, k = 3, 60,000 trials with
    `new SplittableRandom(0x5A3)`; each item's inclusion frequency is within
    0.3 ± 0.01.
  - `reservoirSmallStreamsAndValidation`.
  - `shuffleIsPermutationAndUniformOverThree`: each of the 6 orders occurs
    1/6 ± 0.01 over 60,000 trials.
  - `aliasTableReconstructsProbabilities`: the exact probability implied by the
    table equals the normalized weights within 1e-12; the empirical rate for
    `[1, 0, 3]` is about `[0.25, 0, 0.75]` ± 0.01; index 1 is never sampled.
  - `aliasTableRejectsBadWeights` (NaN, negative, all zero) and copies its input.
- [ ] **Step 2: Confirm the tests fail.** Expected: compilation failure.
- [ ] **Step 3: Implement the classes.**
- [ ] **Step 4: Confirm the tests pass.** Expected: PASS.
- [ ] **Step 5: Write the lesson and fragment.** The lesson covers:
  - `Collections.shuffle` and `SplittableRandom`
  - why a modulo-biased shuffle is wrong
- [ ] **Step 6: Verify and commit.** Run the full verification, then commit
  `Add reservoir sampling, Fisher–Yates and the alias method`.

### Task 21: Documentation integration (run after each wave)

**Files:**
- Modify:
  - `docs/catalog.md`, `docs/complexity.md`, `docs/solution-finder.md`
  - `README.md`: class count, topic map, "Contracts" if a new rule applies
  - `docs/learning-roadmap.md`: stage lists
  - `docs/exercises.md`
  - `docs/validation.md`: on the final pass only
- Delete: `docs/superpowers/fragments/*.txt` after merging them

**Interfaces:**
- Consumes: the fragment files of the merged tasks
- Produces: none

- [ ] **Step 1: Merge fragments.** For each fragment:
  - insert its catalog rows in the named section
  - insert its complexity rows in the named table
  - change each named finder row's Java home from `not implemented here` to the
    repo link, plus the lesson link
  - add the README topic-map entry
  - add the exercise rows
- [ ] **Step 2: Update the README** "26 library classes" count with
  `ls src/main/java/io/github/ichillous/dsa/{algorithms,collections,structures,graphs}/*.java | wc -l`.
- [ ] **Step 3: Delete the merged fragments.**
- [ ] **Step 4: Run the full verification.** Expected: all green.
- [ ] **Step 5: Commit** `Integrate <wave> into the catalog, complexity guide,
  finder and README`.
- [ ] **Step 6: Final pass only.** Rewrite the top section of
  `docs/validation.md` with measured numbers from this build:
  - test count, from `target/surefire-reports`
  - executable Markdown block count
  - line and branch coverage, from `target/site/jacoco/jacoco.csv`
  - link count
  - review findings

  Then commit `Record validation evidence for the breadth expansion`.

### Task 22: Whole-branch review

**Files:**
- Read: `git diff 9ea8d55..HEAD`

**Interfaces:**
- Consumes: none
- Produces: none

- [ ] **Step 1: Dispatch two independent reviewers.**
  - Reviewer (a) covers code correctness and contracts, using the Review Focus
    list.
  - Reviewer (b) covers teaching and documentation accuracy: JDK claims in the
    finder, cost tables, and lesson traces.

  Neither gets this plan's conclusions.
- [ ] **Step 2: Fix each confirmed finding.** Write a failing test first (or
  correct the text for documentation findings). Then run the full verification
  and commit `Address whole-branch review findings`.
- [ ] **Step 3: Push and update PR #1's description** with the final summary and
  validation.
