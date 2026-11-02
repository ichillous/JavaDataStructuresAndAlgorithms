# From notes to an executable course

## Original baseline

Commit `d926b0b50e1cc029640db21a45b59dbb26f6d894` contained 47 Markdown files,
including the README, with no Java source tree, standard build, tests or CI. The
first upgrade (`8414d7b`) added 20 library classes, a showcase, a Maven wrapper,
154 tests, CI and a navigation layer. It preserved the original explanatory
snippets with banners and repaired confirmed defects.

The second audit found a remaining maintenance problem: those large copied
implementations were still outside the build. A test of a similarly named source
class could not certify a Markdown fragment. Learners also needed worked states,
correctness arguments and progressive solutions, not only a list of implementations.

## Current organization

All original topic paths remain, so existing links still work. Their pages are now
maintained lessons with purpose, preconditions, invariants, worked traces, costs,
counterexamples and exercises. Java examples call the library or explicitly named
JDK types; duplicate standalone implementations have been removed. Earlier
versions remain available in normal Git history, without rewriting it.

| Material | Current role |
| --- | --- |
| `data-structures/` | Lessons on representation, mutation, invariants and choosing a structure |
| `algorithms/` | Lessons on search/sort and proof patterns, graph algorithms and problem solving |
| Both `complexity-analysis/` paths | Executable introductions to time/space; [full reference](complexity.md) describes maintained APIs |
| `java-fundamentals/`, `resources/` | Prerequisites and reading guidance |
| `src/main/java/io/github/ichillous/dsa/` | Maintained implementations and runnable terminal lessons |
| `src/test/java/io/github/ichillous/dsa/` | Unit/model/oracle tests plus the Markdown example compiler |
| `docs/` | Roadmap, catalog, exercises, worked solutions, quality criteria and validation evidence |

Every plain fenced Java block is compiled for Java 17 and executed with assertions
enabled by `DocumentedExamplesTest`. These blocks are optional imports followed by
statements, not complete source files. If adapting one into your own program,
run with `java -ea` so its assertion expressions are evaluated.

## Additions and compatibility

The Java 17 namespace and existing compiled APIs remain. The second pass adds
`RingDeque`, `LruCache`, minimum `SegmentTree`, `SequenceAlgorithms`,
`BacktrackingPuzzles` and `AdvancedGraphAlgorithms`. It extends learning coverage
with signed shortest paths, SCCs, bridges, window/sequence problems, signed subset
witnesses and validated Sudoku. Their contracts are in the [catalog](catalog.md).

The no-argument packaged JAR still runs `AlgorithmShowcase`. Arguments now select
`--help` or `learn TOPIC`; invalid commands return exit status 2 with usage guidance.
Previously extra showcase arguments were ignored. The [README](../README.md)
shows all lesson commands, including a parameterized binary-search trace.

Terminology and behavior are now explicit: zero-based half-open ranges, optional
empty minima/distances, UTF-16 string indices, signed-long overflow rules,
immutable result snapshots and size limits for materialized/exponential searches.
Sudoku is the intentional algorithm input-mutation exception: it copies a complete
solution back only on success. It does not establish puzzle uniqueness.

## What is and is not established

The [quality bar](quality.md) and [validation evidence](validation.md) record exact
checks. Passing unit tests, documentation assertions, coverage floors and
independent review provide evidence; they are not a formal proof over all inputs.
There is no claimed performance multiplier or benchmark speedup.

The library deliberately does not implement every historical topic: knight's tour,
optimal BSTs, lazy segment propagation and Sudoku uniqueness remain possible
extensions. Mutable structures remain unsynchronized; APIs do not implement every
Java Collections Framework interface. Prefer the standard JDK types in application
code when implementing a structure is not itself the learning goal.
