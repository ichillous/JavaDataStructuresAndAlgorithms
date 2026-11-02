# Validation evidence

## Baselines and scope

The original commit `d926b0b` contained 47 Markdown files, no Java source tree,
standard build, tests or CI. The first upgrade (`8414d7b`) established 20 library
classes, 154 tests and green Linux/Windows CI. The second pass adds six library
classes and a terminal learning lab, rewrites all 46 original topic pages in place,
and replaces uncompiled parallel snippets with executable lessons.

See the [migration guide](migration.md) for compatibility and intentional changes,
the [catalog](catalog.md) for API scope, and the [quality bar](quality.md) for what
completion requires. Original topic URLs and Git history remain intact.

## Local complete verification (2026-11-02)

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
