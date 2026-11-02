# Breadth-first search and choosing a distance model

BFS finds vertices reachable from a source in increasing number of edges. The
maintained [GraphAlgorithms](../../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java)
uses the repository's fixed-vertex [Graph](../../src/main/java/io/github/ichillous/dsa/graphs/Graph.java):
vertices are `0..V-1`, duplicate edges are ignored, neighbor order follows insertion
order, and directed edges are traversed only in their given direction. Invalid
vertices and null graphs are rejected. Do not mutate a graph during a computation.

## Queue invariant: discovered once, processed by layer

Mark a vertex when it is **enqueued**, not when it is dequeued. Its first discovery
then gives a shortest unweighted path, and each vertex enters the queue at most
once. Queue distances never decrease; processing layer `d` discovers layer `d+1`.

```text
edges: 0--1--3
       |     |
       2-----4       vertices 5 and 6 isolated
queue after processing 0: [1, 2]
queue after processing 1: [2, 3]
queue after processing 2: [3, 4]
BFS order: [0, 1, 2, 3, 4]
```

```java
import io.github.ichillous.dsa.graphs.Graph;
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import java.util.List;

var graph = Graph.undirected(7).addEdge(0, 1).addEdge(0, 2)
        .addEdge(1, 3).addEdge(2, 4).addEdge(3, 4);
assert GraphAlgorithms.breadthFirst(graph, 0).equals(List.of(0, 1, 2, 3, 4));
assert GraphAlgorithms.shortestPath(graph, 0, 4).equals(List.of(0, 2, 4));
assert GraphAlgorithms.shortestPath(graph, 0, 6).isEmpty();
assert GraphAlgorithms.shortestPath(graph, 0, 0).equals(List.of(0));
assert GraphAlgorithms.connectedComponents(graph).size() == 3;
```

The maintained BFS, unweighted path and undirected-component methods take
O(V + E) time and O(V + E) auxiliary space including adjacency snapshots and
results. A single-source traversal visits only the reachable portion, but its
state arrays still include every vertex. An empty graph has no valid source.

**Counterexample:** marking only on dequeue can enqueue the same vertex through
several parents. Without additional care that increases work and can repeatedly
overwrite parents. A disconnected vertex must remain unreachable, not be assigned
a default distance of zero.

## Weighted paths: BFS's objective changes

A direct edge of weight 10 uses fewer edges than a two-edge route of total weight
2. BFS optimizes the former count. For nonnegative weights, use Dijkstra:

```java
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import java.util.List;

var weighted = WeightedGraph.directed(4)
        .addEdge(0, 1, 10).addEdge(0, 2, 1).addEdge(2, 1, 1);
var paths = GraphAlgorithms.dijkstra(weighted, 0);
assert paths.distanceTo(1).orElseThrow() == 2L;
assert paths.pathTo(1).equals(List.of(0, 2, 1));
assert paths.distanceTo(3).isEmpty();
```

The maintained lazy-heap Dijkstra takes O(V + E log(E + 1)) time and O(V + E)
space; weighted graphs permit parallel edges, so the heap can contain more than
V entries. It rejects **every** negative edge, even in an unreachable component.
Unreachable distances are empty; a reachable `Long.MAX_VALUE` is valid. Overflowing
detours are ignored, but an unrepresentable final reachable shortest distance throws.

## Signed edges: Bellman–Ford

[AdvancedGraphAlgorithms](../../src/main/java/io/github/ichillous/dsa/graphs/AdvancedGraphAlgorithms.java)
relaxes all directed adjacency edges repeatedly. Each finite label is the cost of
an actual walk. After pass `k`, paths of at most `k` edges have been considered;
in-place updates may also propagate longer paths. Without a reachable negative
cycle, a shortest simple path uses at most `V-1` edges. A further improvement then
proves that a source-reachable negative cycle exists.

```java
import io.github.ichillous.dsa.graphs.AdvancedGraphAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import java.util.List;

var signed = WeightedGraph.directed(4)
        .addEdge(0, 1, 4).addEdge(0, 2, 5).addEdge(1, 2, -3);
var paths = AdvancedGraphAlgorithms.bellmanFord(signed, 0);
assert paths.distanceTo(2).orElseThrow() == 1L;
assert paths.pathTo(2).equals(List.of(0, 1, 2));
assert paths.distanceTo(3).isEmpty();
```

This implementation rejects the entire result for any source-reachable negative
cycle; unreachable cycles are ignored. A reachable undirected negative edge
already creates a negative closed walk. Relaxations use exact `BigInteger`
arithmetic, and only final reachable distances must fit in `long`.
There are O(VE) relaxations plus O(V + E) setup. With maximum intermediate bit
length `B = O(64 + log(VE + 1))`, time is O(V + E + VEB) bit operations and
auxiliary storage is O((V + E)B) bits including snapshots. These arithmetic costs
should not be hidden behind a constant-time `long` assumption.

## Practice

1. Validate returned paths edge by edge and compare their lengths with a distance
   oracle; do not demand one arbitrary tied path from every implementation.
2. Construct a negative edge that breaks Dijkstra's settled-vertex argument.
3. Add a negative cycle first outside and then inside the source's reachable set.
   Explain why Bellman–Ford treats those cases differently.

Use the [solutions](../../docs/solutions.md) and continue with [DFS](dfs.md).
