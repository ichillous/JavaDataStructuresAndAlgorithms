# Contributing

This repository teaches data structures and algorithms through readable implementations and tests. Favor a small, correct addition with a clear explanation over another large collection of snippets.

## Build and run

Install a JDK 17 or newer, then run from the repository root:

```sh
./mvnw clean verify
```

On Windows, run `mvnw.cmd clean verify`. The wrapper downloads the pinned Maven distribution and dependencies on first use; no global Maven installation is required. Do not commit `target/`, downloaded distributions, IDE state or credentials.

For a focused test while developing:

```sh
./mvnw -Dtest=DynamicArrayTest test
```

A focused test is a development check. Run the complete `clean verify` command before submitting the final change. Follow the README for runnable examples and generated documentation.

## Before implementing

Read the [catalog](docs/catalog.md) and the nearest implementation and test. For an API, state the following in Javadoc or the topic guide:

- Accepted inputs, null policy, index range and empty-input behavior.
- Return meaning, mutation and ownership of returned collections or arrays.
- Duplicate handling, ordering and numeric overflow behavior where relevant.
- The invariant that makes the algorithm correct, plus time and auxiliary-space costs with assumptions.

Keep source under `src/main/java/io/github/ichillous/dsa` and tests under the matching package in `src/test/java`. Prefer the JDK over extra runtime dependencies. The implementations are educational; they do not need to reproduce the entire Java Collections Framework or promise thread safety.

## Tests that teach

Add a small example and a regression for the failure your change addresses. Include boundary cases appropriate to the API: empty and singleton inputs, duplicates, invalid indices, absent values, negative values, extreme integers, graph cycles or disconnected vertices. For mutable structures, test a sequence of operations rather than only freshly constructed instances.

Use deterministic seeds for randomized tests and an independent model where possible: JDK collections, `Arrays.sort`, a brute-force solver on tiny inputs, or a different algorithm. State the seed in a useful failure message. Avoid assertions that inspect only the same intermediate values the implementation computes; test observable results and essential invariants instead.

Keep enumeration and brute-force test sizes small enough for normal local and CI runs. Do not replace correctness tests with timing thresholds; machine-dependent speed is not a correctness guarantee.

## Documentation and review

Update the [catalog](docs/catalog.md), [complexity guide](docs/complexity.md) and relevant [exercises](docs/exercises.md) when an API or its behavior changes. Link to maintained source instead of copying a second full implementation into Markdown. Existing historical notes retain their paths; their explanatory snippets are outside the build, as described in the [migration guide](docs/migration.md).

A useful change description explains the concrete problem, resulting behavior and exact validation run. Report failed or skipped checks clearly. Do not claim a percentage of coverage, benchmark improvement or support for a Java version without evidence from the corresponding check.

Before submitting, run `python3 scripts/check_docs.py`, inspect `git diff --check`, the final diff and the complete build result. Keep unrelated formatting and generated files out of the change. Preserve existing history and integrate concurrent changes normally.
