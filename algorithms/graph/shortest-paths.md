# Shortest-path toolkit: 0-1 BFS, DAGs, A* and Floyd–Warshall

Purpose: pick the cheapest correct shortest-path algorithm for the structure you
actually have. [ShortestPathAlgorithms](../../src/main/java/io/github/ichillous/dsa/graphs/ShortestPathAlgorithms.java)
adds four specialised tools to the [BFS](bfs.md) lesson's Dijkstra and Bellman–Ford
methods. All use the repository's
[WeightedGraph](../../src/main/java/io/github/ichillous/dsa/graphs/WeightedGraph.java):
vertices `0..V-1`, `long` weights, parallel edges and self-loops allowed. Unreachable
vertices have empty distances and paths. Null graphs are rejected, bad vertices throw
`IndexOutOfBoundsException`, and a violated precondition throws
`IllegalArgumentException`. Inputs are never mutated; results are detached snapshots.
Read [DFS](dfs.md) for the topological-order idea used by the DAG method.

## Decision table

| Graph | Algorithm |
| --- | --- |
| unweighted | BFS |
| 0/1 weights | 0-1 BFS |
| nonnegative weights | Dijkstra |
| heuristic available | A* |
| DAG | topological DP |
| negative weights | Bellman–Ford |
| all pairs | Floyd–Warshall |
| sparse all-pairs | Johnson, not implemented here |

Choose the most specific row that applies; each more specific algorithm exploits a
structural fact that the general one cannot assume.

## The invariants

* **0-1 BFS.** A deque holds vertices in nondecreasing distance order. A weight-0
  edge pushes to the front (same distance), a weight-1 edge to the back (one more).
  The first time a vertex leaves the deque its distance is final. Every edge in the
  whole graph must be 0 or 1.
* **DAG DP.** Process vertices in topological order; when a vertex is processed all
  its predecessors are done, so its label is final. Negative weights are fine because
  no cycle exists. The graph must be directed and acyclic everywhere, including parts
  unreachable from the source.
* **A\*.** With heuristic `h`, expand by `f = g + h`. If `h(target) = 0` and
  `h(u) <= w(u,v) + h(v)` on every edge (consistency), `f` never decreases along a
  path, so a vertex leaves the queue with its optimal `g` and is never reopened. The
  implementation verifies `h >= 0`, `h(target) = 0` and consistency on every edge it
  scans, and throws `IllegalArgumentException` on a violation. It cannot verify edges
  the search never scans, so a heuristic is still your responsibility.
* **Floyd–Warshall.** After round `k`, `d[i][j]` is the shortest walk from `i` to `j`
  using only intermediate vertices below `k`. Any `d[i][i] < 0` is a negative cycle
  and is rejected, anywhere in the graph; a negative undirected edge is such a cycle.

## Worked trace: 0-1 BFS

```text
edges: 0 -1-> 1,  0 -0-> 2,  2 -0-> 3,  3 -0-> 1
start          deque [0]            dist 0:0
pop 0: 0->1 (w=1) back, 0->2 (w=0) front     deque [2, 1]   dist 1:1, 2:0
pop 2: 2->3 (w=0) front                      deque [3, 1]   dist 3:0
pop 3: 3->1 (w=0) improves 1 from 1 to 0    deque [1, 1]   dist 1:0
pop 1: final; the duplicate 1 is skipped
path to 1: 0 -> 2 -> 3 -> 1, distance 0
```

## Executable example: 0-1 BFS and DAG paths

```java
import io.github.ichillous.dsa.graphs.ShortestPathAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import java.util.List;
import java.util.OptionalLong;

var grid = WeightedGraph.directed(5)
        .addEdge(0, 1, 1).addEdge(0, 2, 0).addEdge(2, 3, 0).addEdge(3, 1, 0);
var tree = ShortestPathAlgorithms.zeroOneBfs(grid, 0);
assert tree.distanceTo(1).equals(OptionalLong.of(0));
assert tree.pathTo(1).equals(List.of(0, 2, 3, 1));
assert tree.distanceTo(4).isEmpty();

var dag = WeightedGraph.directed(4)
        .addEdge(0, 1, 4).addEdge(0, 2, 5).addEdge(1, 2, -3).addEdge(2, 3, 2);
var signed = ShortestPathAlgorithms.dagShortestPaths(dag, 0);
assert signed.distanceTo(3).equals(OptionalLong.of(3));
assert signed.pathTo(3).equals(List.of(0, 1, 2, 3));
```

## Longest path in a DAG by negation

Longest simple paths are NP-hard in general, but in a DAG negating every weight turns
a longest path into a shortest path. Signed weights are exactly what the DAG method
accepts.

```java
import io.github.ichillous.dsa.graphs.ShortestPathAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import java.util.List;

// Task durations: the longest chain 0 -> 1 -> 3 takes 3 + 4 = 7.
long[][] edges = {{0, 1, 3}, {0, 2, 2}, {1, 3, 4}, {2, 3, 1}};
var negated = WeightedGraph.directed(4);
for (long[] edge : edges) {
    negated.addEdge((int) edge[0], (int) edge[1], -edge[2]);
}
var tree = ShortestPathAlgorithms.dagShortestPaths(negated, 0);
assert -tree.distanceTo(3).orElseThrow() == 7L;
assert tree.pathTo(3).equals(List.of(0, 1, 3));
```

Negation is only safe where `Long.MIN_VALUE` cannot occur; this method itself stays
exact, but `-Long.MIN_VALUE` in your own code overflows.

## Executable example: A* and Floyd–Warshall

```java
import io.github.ichillous.dsa.graphs.ShortestPathAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import java.util.List;
import java.util.OptionalLong;

// A 3x3 grid with unit edges, cell = row * 3 + col; the target is cell 8 (row 2, col 2).
var grid = WeightedGraph.undirected(9);
for (int row = 0; row < 3; row++) {
    for (int col = 0; col < 3; col++) {
        if (col < 2) grid.addEdge(row * 3 + col, row * 3 + col + 1, 1);
        if (row < 2) grid.addEdge(row * 3 + col, (row + 1) * 3 + col, 1);
    }
}
var route = ShortestPathAlgorithms.aStar(grid, 0, 8,
        v -> Math.abs(v / 3 - 2) + Math.abs(v % 3 - 2)).orElseThrow();
assert route.distance() == 4L;
assert route.path().size() == 5;

var graph = WeightedGraph.directed(3).addEdge(0, 1, 4).addEdge(1, 2, -1).addEdge(0, 2, 5);
var all = ShortestPathAlgorithms.floydWarshall(graph);
assert all.distance(0, 2).equals(OptionalLong.of(3));
assert all.path(0, 2).equals(List.of(0, 1, 2));
assert all.distance(2, 0).isEmpty();
assert all.distance(1, 1).equals(OptionalLong.of(0));
```

## Costs

| Method | Time | Auxiliary space | Notes |
| --- | --- | --- | --- |
| `zeroOneBfs` | O(V + E) | O(V + E) | distances at most `V-1`, no overflow |
| `dagShortestPaths` | O(V + E) arithmetic operations | O(V + E) | exact `BigInteger` labels, final `longValueExact` |
| `aStar` | O(V + E log(E + 1)) worst case, plus heuristic calls | O(V + E) | good `h` expands far fewer vertices than Dijkstra |
| `floydWarshall` | O(V³) | O(V²) | exact arithmetic; stops at the first `d[i][i] < 0` |

The DAG and Floyd–Warshall methods use exact `BigInteger` intermediates like
Bellman–Ford, so an overflowing detour never produces a wrong answer. Only final
reachable distances must fit in `long`; otherwise `ArithmeticException`. A reachable
`Long.MAX_VALUE` is valid. A\* follows Dijkstra's policy for `g`: overflowing detours
are ignored, and `ArithmeticException` is thrown if the target is reachable but every
route exceeds `long`. Its `f = g + h` saturates at `Long.MAX_VALUE`; the code compares
the exact sum as an unsigned value, which orders identically without misordering ties.

## Counterexample: an inconsistent heuristic

On the path `0 -> 1 -> 2` with unit weights, `h(0) = 5` and `h(1) = h(2) = 0` makes
`h(0) > w(0,1) + h(1)`. The heuristic is not even admissible (the true remaining cost
is 2), and A\* could return a suboptimal route on larger graphs. This library throws
`IllegalArgumentException` at the first scanned violating edge instead. Likewise, 0-1
BFS rejects weight 2: splitting it into two weight-1 edges changes the vertex count, and
silently treating it as 1 would return wrong distances.

## Practice

1. Reduce a weight-2 edge graph to 0-1 BFS by inserting intermediate vertices, and
   compare the result with Dijkstra.
2. Build a grid where Manhattan distance is a consistent heuristic, then one where
   blocked cells make Euclidean distance still consistent. Count vertices expanded by
   A\* versus Dijkstra.
3. Add a negative cycle in a component the source cannot reach: show Bellman–Ford from
   that source ignores it while Floyd–Warshall rejects the graph.

Continue with the [catalog](../../docs/catalog.md) and the
[complexity guide](../../docs/complexity.md). The [solutions](../../docs/solutions.md)
describe the oracles used to test these methods.
