# A quality bar that can be checked

The first library upgrade established a build and 154 unit tests. The next audit
found that the original topic pages still held a large parallel collection of
uncompiled implementations. A tested class did not validate those copied snippets,
and an introductory catalog alone did not teach how to reason about the code.

This pass uses these completion criteria:

1. Preserve all original topic paths, but replace duplicate implementations with
   maintained lessons: purpose, preconditions, invariant, worked state changes,
   complexity, a counterexample, and a progressive exercise.
2. Compile and execute **every Java fence** in repository Markdown during tests.
   Examples contain expected-result assertions; a separate failing assertion probe
   proves those assertions run. A lesson edit can fail the build like a source edit.
3. Add useful algorithms with distinct tradeoffs, not synonyms for existing APIs:
   signed shortest paths, SCCs, bridges, range minima, LRU eviction, deque ends,
   sequence/window problems, signed subset witnesses, and validated Sudoku search.
4. Check observable contracts with independent models and adversarial cases:
   extreme arithmetic, negative cycles, long graph paths, empty ranges, aliases,
   unsatisfiable puzzles, retained capacity, recency, duplicate values and ties.
5. Validate warnings, Javadoc, test suite, documentation links, packaged examples
   and terminal lessons. Independently review code and teaching claims. Verify
   Linux/Windows CI for the exact final commit and confirm remote `main`.

The full build enforces 90% production line coverage and 85% branch coverage,
including examples. These are regression floors, not a correctness score. A
randomized model test is only as good as its oracle and sampled inputs; inspect
invariants and counterexamples as well. There are no timing-based correctness
assertions and no claimed benchmark speedup.

## Writing an executable lesson

Use a plain Java fence with optional initial one-line imports, then statements and
assertions about the expected result. No enclosing class or method is needed.
Keep examples bounded, deterministic and free of network/filesystem side effects.
Link to the maintained source instead of pasting its full implementation. Use
ordinary text or a non-Java fence for pseudocode, and clearly label it as such.

The test compiles with `--release 17 -Xlint:all -Werror` and loads each temporary
class with assertions enabled. Failure names contain the Markdown file and line.
It uses only the JDK compiler; there is no documentation framework or runtime
dependency. The full JDK requirement therefore applies to running tests too.

Run `./mvnw -Dtest=DocumentedExamplesTest test` during lesson development, then
`./mvnw clean verify` for the final change. Follow the [roadmap](learning-roadmap.md),
try the [solutions](solutions.md), and consult [validation evidence](validation.md)
for measured results and scope.
