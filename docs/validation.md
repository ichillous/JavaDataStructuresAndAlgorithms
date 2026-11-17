# Validation evidence

## Lessons and figures expansion verification (2026-11-17)

This pass rewrote or added the 69 lessons under `algorithms/` and `data-structures/`, added
the two folder landing pages, and brought the figures to 98 and the step-throughs to 13.
It added two public methods (`SearchAlgorithms.firstTrue`, `DynamicArray.capacity`) and no
class: the four library packages hold 47 public classes. Every number below was measured
on the final tree with the command named in brackets. Full `./mvnw -q -B clean verify`
passed on OpenJDK 21.0.12, compiling with `maven.compiler.release` 17:

| Check | Result |
| --- | --- |
| Total JUnit invocations | **843 passed**, zero failures, errors or skips, across **68** test classes [`target/surefire-reports/TEST-*.xml`] |
| Executable Markdown | All **231 Java blocks** in 76 Markdown files compiled for Java 17 and executed with assertions enabled; 232 `DocumentedExamplesTest` invocations, the extra one being the assertion probe |
| Production line coverage | **4,495 / 4,547 = 98.86%**, including runnable examples [`target/site/jacoco/jacoco.csv`] |
| Production branch coverage | **2,480 / 2,518 = 98.49%**, including runnable examples [same file] |
| Coverage floors | 90% lines and 85% branches enforced; both passed |
| Compiler and Javadoc | `-Xlint:all -Werror` and Javadoc doclint passed |
| Packaged programs | The default showcase and `learn` with `binary-search 4`, `negative-edges`, `range-queries`, `lru` and `list` exited with status 0 |
| Local documentation | **1,519** file links resolve; 0 broken [`python3 scripts/check_docs.py`] |
| Figures | **98 figures, 196 SVGs**, 0 stale generated files [`python3 scripts/generate_visuals.py --check`] |
| Step-throughs | **13 traces with 118 states** [frames in `docs/visuals/data/visual-data.js`] |
| Lessons | **69 lessons** and 2 landing pages under `algorithms/` and `data-structures/`, each with a figure region and an At a glance table |
| Python visual tests | **66 passed** [`python3 -m unittest discover -s scripts/visuals -p 'test_*.py'`], including new coverage tests for figure placement, lesson figures and the rendered-review report |
| Rendered review | 196 SVG variants and 472 viewer state/layout checks (118 states at 1440px and 390px in both themes), 0 issues [`node scripts/visuals/browser_review.js --all --write --previews`]; details in [visual verification](visuals/verification.md) |
| Working-tree hygiene | `git diff --check` passed |

Integration fixes in this pass: nine figures named a lesson that no longer placed them,
so their `lesson` now points at a page that does (and `dijkstra-path` is now also placed
beside the Dijkstra section of the BFS lesson); the solution finder links the new lessons,
`firstTrue` and the three chooser pages, points union-find, topological sort, insertion sort
and Kruskal at their own lessons, gives open addressing the exact expected and amortized
split, describes `Arrays.parallelSort` only through its Javadoc implementation notes, and
replaces a speed comparison of bubble and insertion sort with a count of element writes.
Passing tests is evidence, not a proof for all inputs or a benchmark result.

## Breadth expansion verification (2026-11-16)

The breadth expansion adds 21 public library classes (47 in `algorithms`, `collections`,
`structures` and `graphs`, up from 26, plus two package-private helpers), 19 new lessons
(65 topic pages in total) and a problem-first [solution finder](solution-finder.md). Full
`./mvnw clean verify` passed on OpenJDK 21.0.12, compiling with `maven.compiler.release` 17:

| Check | Result |
| --- | --- |
| Total JUnit invocations | **632 passed**, zero failures, errors or skips, across 53 test classes |
| Executable Markdown | All **147 Java blocks** across 68 Markdown files compiled for Java 17 and executed with assertions enabled (148 `DocumentedExamplesTest` invocations, the extra one being the assertion probe) |
| Production line coverage | **4,482 / 4,534 = 98.85%**, including runnable examples |
| Production branch coverage | **2,474 / 2,512 = 98.49%**, including runnable examples |
| Coverage floors | 90% lines and 85% branches enforced; both passed |
| Compiler and Javadoc | `-Xlint:all -Werror` and Javadoc doclint `all,-missing` passed |
| Packaged programs | Default showcase and the four terminal lessons (`binary-search`, `negative-edges`, `range-queries`, `lru`) exited with status 0 |
| Local documentation | **895** file links resolve, including this page's own; 0 broken |
| Visual layer | `generate_visuals.py --check` and the 10 Python visual tests passed |
| Working-tree hygiene | `git diff --check` passed |

Review process. An independent correctness review of the existing library came first
and found four contract gaps, all fixed and listed in the [migration guide](migration.md):
`FenwickTree` queries now return exact sums and construction rejects only overflowing
final node sums; array-backed collections grow to `Integer.MAX_VALUE - 8` before failing;
`DynamicProgramming` rejects oversized tables with `IllegalArgumentException`; and
`BinaryMinHeap` updates are failure-atomic. Each of the 18 implementation tasks and the
solution finder then had its own independent review, with fix rounds where findings
required them. Final whole-branch code and documentation reviews followed, and their
headline findings were fixed: the Rabin–Karp cost is stated everywhere as
O(t + p + k·p) when spurious hits are rare and O(t·p) in the worst case, because its
hash is fixed rather than randomized; a duplicate public range record was removed in
favor of `StringAlgorithms.Span`; the documented DP tie rules are pinned by tests;
`matrixChainOrder` throws only when the optimal cost itself overflows; `AhoCorasick`'s
tables are final fields; and overstated or stale claims (Dinic's typical behavior, a
witness for every negative answer, "earlier behavior is unchanged", "no probing map")
were corrected. Passing tests is evidence, not a proof for all inputs or a benchmark
result.

## Visual-layer verification (2026-11-02)

The complete Java 17 build now passes **357 tests across 33 classes**, with zero
failures, errors or skips: the prior 334 checks plus 23 public-API contracts for
shared visual fixtures. All 99 executable Markdown blocks still pass. Production
coverage remains 97.25% lines and 97.26% branches, with both enforced floors met.

The visual layer adds 26 numbered figures (52 explicit light/dark SVGs), five
step-throughs with 54 states, four captured desktop/phone previews and an offline
visual index. Ten portable Python checks and deterministic regeneration pass.
Actual Chromium review measured all 52 SVGs and 216 viewer state/layout combinations;
final text bounds/collision and overflow reports contain no issues. Independent
code, algorithm and image review found no remaining material visual findings.

[Visual coverage and verification](visuals/verification.md) now describes the
2026-11-17 state; this paragraph remains as the record of the first visual layer. The
figure/trace values share canonical data with the Java API tests; no measured
runtime or speedup is claimed. Local documentation file links resolve, and
HTML/image/manifest references are checked separately by the visual suite.

## Baselines and scope

The original commit `d926b0b` contained 47 Markdown files, no Java source tree,
standard build, tests or CI. The first upgrade (`8414d7b`) established 20 library
classes, 154 tests and green Linux/Windows CI. The second pass adds six library
classes and a terminal learning lab, rewrites all 46 original topic pages in place,
and replaces uncompiled parallel snippets with executable lessons.

See the [migration guide](migration.md) for compatibility and intentional changes,
the [catalog](catalog.md) for API scope, and the [quality bar](quality.md) for what
completion requires. Original topic URLs and Git history remain intact.

## Second-pass baseline verification (2026-11-02)

Full `./mvnw clean verify` passed on Eclipse Temurin JDK 17.0.21:

| Check | Result |
| --- | --- |
| Total JUnit invocations | **334 passed**, zero failures, errors or skips, across 32 test classes |
| Unit/model/CLI checks | 234 invocations, including 80 additions beyond the first pass |
| Executable Markdown | All **99 Java blocks** across 48 Markdown files compiled for Java 17 and executed |
| Assertion-control check | One deliberately failing assertion probe was caught as expected, proving assertions are enabled |
| Production line coverage | **1,767 / 1,817 = 97.25%**, including runnable examples |
| Production branch coverage | **889 / 914 = 97.26%**, including runnable examples |
| Coverage floors | 90% lines and 85% branches enforced; both passed |
| Compiler and Javadoc | Java 17 API/language compatibility, `-Xlint:all -Werror`, Javadoc validation passed |
| Packaged programs | Default showcase and all four terminal lessons passed on OpenJDK 21 |
| Local documentation | 295 file links resolve; solution reveals checked as 11 balanced siblings |
| Working-tree hygiene | `git diff --check` passed; generated artifacts ignored |

The added checks include access-order cache/deque models, all-small-range segment
oracles, sequence/pair/subset enumeration, independent Sudoku validity/givens
checks, signed exact-integer Floyd–Warshall models, SCC mutual reachability,
edge-removal bridge oracles and 30,000-vertex iterative-depth cases. Prior collision,
AVL, deep-trie, sorting, KMP, knapsack and spanning-forest checks remain.

Independent code and pedagogy reviews checked invariants, overflow, mutation,
deterministic ties, expected/amortized/worst costs, worked traces and navigation.
Review findings were corrected: segment-tree representation now matches its
interval explanation, preprocessing costs distinguish hashing from sorting,
linked-list retention wording matches source, and promised worked solutions exist.
Passing tests is evidence, not a proof for all inputs or a benchmark result.

## Reproduce

```sh
./mvnw clean verify
java -jar target/java-data-structures-and-algorithms-1.0.0-SNAPSHOT.jar
java -jar target/java-data-structures-and-algorithms-1.0.0-SNAPSHOT.jar learn list
java -jar target/java-data-structures-and-algorithms-1.0.0-SNAPSHOT.jar learn binary-search 4
java -jar target/java-data-structures-and-algorithms-1.0.0-SNAPSHOT.jar learn negative-edges
java -jar target/java-data-structures-and-algorithms-1.0.0-SNAPSHOT.jar learn range-queries
java -jar target/java-data-structures-and-algorithms-1.0.0-SNAPSHOT.jar learn lru
python3 scripts/check_docs.py
```

Reports are generated in `target/surefire-reports/`, `target/reports/apidocs/`,
and `target/site/jacoco/`. The [CI workflow](../.github/workflows/ci.yml) repeats
complete verification and packaged lessons on Linux JDK 17/21 and Windows JDK 21;
consult the exact commit's Actions run for remote results. Documentation-link
checks run on Linux. External URLs and fragment targets are outside that check.

## Limits

Mutable structures are not thread safe. String operations use UTF-16 units,
without implicit normalization. Finite shortest-path distances must fit signed
long even when intermediate arithmetic is exact. Materialized enumeration and
subset witnesses have documented input limits; capacity DP is pseudopolynomial.
The fixed 9x9 Sudoku solver finds one solution, not a uniqueness proof. No measured
speedup is claimed. These learning APIs do not reproduce the full Java Collections
Framework or implement every topic from the earlier notes.
