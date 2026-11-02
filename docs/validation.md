# Upgrade validation

Baseline: commit `d926b0b50e1cc029640db21a45b59dbb26f6d894` contained 47
Markdown files, no compiled Java source, and no standard build, tests, or CI.
The upgrade adds 20 library source classes and one packaged showcase while
preserving the original note paths and commit history. See the
[migration audit](migration.md) for repaired historical examples and scope.

## Local evidence (2026-11-02)

- Full `./mvnw clean verify` on a complete Eclipse Temurin JDK 17.0.21:
  **154 tests across 22 test classes; zero failures, errors, or skips**.
- Java 17 compilation with `-Xlint:all -Werror`, validated Javadoc, JAR packaging,
  and JaCoCo report generation all passed as part of that build.
- The packaged JAR ran successfully on OpenJDK 21. Its examples produced the
  expected sorted array, word counts, autocomplete, route `[0, 2, 1, 3]` with cost
  `6`, unreachable destination, dependency order, range sum `100`, coin count `2`,
  and overlapping string-match positions `[1, 3]`.
- `python3 scripts/check_docs.py` passed for all local file links. This check
  excludes external URLs and fragment identifiers.
- `git diff --check` passed. Independent source and documentation review found
  no unresolved material defects.

The tests include deterministic JDK collection models, `Arrays.sort` and
`String.indexOf` comparisons, brute-force dynamic-programming/activity/enumeration
oracles, exhaustive small spanning forests, and Floyd–Warshall shortest paths.
Independent regressions use `BigInteger` distances near signed-long limits and
40,000-character trie words. Corrected historical snippets were separately
compiled and smoke-tested; those audit checks are not counted as maintained
JUnit tests.

## Reproduce and inspect

```sh
./mvnw clean verify
java -jar target/java-data-structures-and-algorithms-1.0.0-SNAPSHOT.jar
python3 scripts/check_docs.py
git diff --check
```

Test XML is generated in `target/surefire-reports/`; Javadoc is in
`target/reports/apidocs/`; coverage is in `target/site/jacoco/`.
Generated artifacts are ignored by Git. The
[CI workflow](../.github/workflows/ci.yml) repeats the full build on Linux
JDK 17/21 and Windows JDK 21; consult the exact commit's Actions run for remote
results. This local evidence does not predeclare a future CI result.

Historical Markdown snippets remain outside Maven's source set. Tests and
coverage are evidence rather than formal proofs, and no benchmark speedup is
claimed. Mutable structures are not thread safe, string positions use UTF-16,
and materialized backtracking results have documented input limits.
