# Java Data Structures and Algorithms

[![Java CI](https://github.com/ichillous/JavaDataStructuresAndAlgorithms/actions/workflows/ci.yml/badge.svg)](https://github.com/ichillous/JavaDataStructuresAndAlgorithms/actions/workflows/ci.yml)
![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)
![Runtime dependencies: none](https://img.shields.io/badge/runtime_dependencies-none-green)

A hands-on Java learning library: readable implementations, executable examples,
reference-model tests, and the reasoning behind each algorithm. Start with an
invariant, trace a small example, then use the tests to challenge your understanding.

This project grew from Isiah's original study notes. Those notes keep their original
URLs; the compiled source and tests are the implementation reference.

## Run it

Install a **JDK 17 or newer** (a JRE alone cannot compile the project), then:

```sh
git clone https://github.com/ichillous/JavaDataStructuresAndAlgorithms.git
cd JavaDataStructuresAndAlgorithms
./mvnw clean verify
java -jar target/java-data-structures-and-algorithms-1.0.0-SNAPSHOT.jar
```

On Windows, use `mvnw.cmd clean verify`. Maven does not need to be installed: the
wrapper downloads Maven 3.9.11 and verifies its pinned SHA-256 checksum. The first
build needs internet access for the build tools and JUnit; examples run offline.

`verify` compiles with Java 17 language/API compatibility and warnings treated as
errors, runs all JUnit tests, validates Javadoc, packages the examples, and generates
a JaCoCo coverage report. CI repeats it on Linux with JDK 17 and 21 and on Windows
with JDK 21. There are no runtime dependencies; JUnit is a test dependency.

## Choose a starting point

| Your goal | Start here |
| --- | --- |
| Learn the fundamentals in order | [Learning roadmap](docs/learning-roadmap.md) |
| Find an implementation and its tests | [Implementation catalog](docs/catalog.md) |
| Compare time, space, and assumptions | [Complexity reference](docs/complexity.md) |
| Practice explaining and extending code | [Exercises](docs/exercises.md) |
| Understand the original notes and changes | [Migration and audit](docs/migration.md) |
| Add an implementation or regression test | [Contributing](CONTRIBUTING.md) |

## What's implemented

- **Collections:** dynamic array, doubly linked list, array stack, circular queue,
  and a resizing hash map with separate chaining.
- **Ordered and indexed structures:** unbalanced BST, AVL tree, min-heap, trie,
  union-find, and Fenwick tree.
- **Search and sorting:** linear/binary search, lower/upper bounds, insertion,
  bubble, merge, three-way quicksort, and heapsort.
- **Graphs:** BFS, iterative DFS, shortest unweighted paths, components,
  topological sorting, Dijkstra, and Kruskal's minimum spanning forest.
- **Problem-solving patterns:** KMP string matching, coin change, 0/1 knapsack,
  longest common/increasing subsequences, edit distance, activity selection,
  unique permutations, and N-queens.

Each public API documents its input contract. Nulls are rejected; collection
elements and map keys/values are non-null. Sorts mutate their input; snapshots
and enumerated results are immutable. String algorithms use Java UTF-16 code
units. Structures are intentionally not thread safe. See the
[catalog](docs/catalog.md) for duplicate, empty-result, overflow, and indexing rules.

## A small example

The coin system `{1, 3, 4}` is a useful counterexample to greedy coin selection:
choosing `4 + 1 + 1` uses three coins, while dynamic programming finds `3 + 3`.

```java
import io.github.ichillous.dsa.algorithms.DynamicProgramming;
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;

int coins = DynamicProgramming.minimumCoins(new int[]{1, 3, 4}, 6); // 2

WeightedGraph roads = WeightedGraph.undirected(4)
        .addEdge(0, 1, 8).addEdge(0, 2, 2)
        .addEdge(2, 1, 1).addEdge(1, 3, 3);
var routes = GraphAlgorithms.dijkstra(roads, 0);
System.out.println(routes.pathTo(3));                   // [0, 2, 1, 3]
System.out.println(routes.distanceTo(3).orElseThrow()); // 6
```

These statements belong inside a method or JShell session. The
[packaged showcase](src/main/java/io/github/ichillous/dsa/examples/AlgorithmShowcase.java)
also demonstrates sorting, bounds, word frequencies, autocomplete, task dependencies,
range sums, and overlapping string matches.

## How correctness is checked

Tests cover ordinary examples and invalid inputs, plus deterministic randomized
comparisons against independent models: JDK collections, `Arrays.sort`, `String.indexOf`,
brute-force small problem solvers, Floyd–Warshall shortest paths, and exhaustive
spanning forests. Dedicated regressions cover hash collisions, duplicate values,
queue wraparound, AVL rotations/deletion, deep trees/tries, and signed-long limits.
Fixed seeds make failures reproducible. Coverage helps locate gaps; it is not a
proof of correctness or a performance benchmark.

```sh
./mvnw test                              # all unit tests
./mvnw -Dtest=AvlTreeTest test            # one test class
python3 scripts/check_docs.py            # local file links; requires Python 3
```

After `verify`, open `target/site/jacoco/index.html` for coverage and
`target/reports/apidocs/index.html` for generated API documentation.

## Repository map

```text
src/main/java/io/github/ichillous/dsa/
  collections/    arrays, lists, stacks, queues, hashing
  structures/     trees, heaps, tries, union-find, range sums
  algorithms/     search, sort, strings, DP, greedy, backtracking
  graphs/         graph models and graph algorithms
  examples/       runnable applications of the library
src/test/java/    edge cases and deterministic reference-model tests
docs/            roadmap, catalog, exercises, complexity, migration
algorithms/      original explanatory notes (not compiled)
data-structures/ original explanatory notes (not compiled)
```

The original [Java fundamentals](java-fundamentals/core-java-concepts.md),
[data-structure notes](data-structures/arrays/overview.md),
[algorithm notes](algorithms/searching/binary-search.md),
[complexity notes](complexity-analysis/time-complexity.md), and
[reading list](resources/books.md) remain available. Their banners distinguish
explanatory fragments from the runnable library. This is an educational project;
for application code, consider the standard Java collections first.
