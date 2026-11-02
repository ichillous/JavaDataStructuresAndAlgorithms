# Graph representations: pay for the operations you need

An adjacency list stores outgoing neighbors per vertex. An adjacency matrix stores a cell for every ordered vertex pair. Neither is universally better; choose based on graph density, edge checks and traversal workload.

```text
edges: 0 -> 1, 0 -> 2, 2 -> 1

list                 matrix (row = from, column = to)
0: [1, 2]               0 1 2
1: []                0  0 1 1
2: [1]               1  0 0 0
                     2  0 1 0
```

| Operation | Adjacency list/set | Boolean matrix |
| --- | --- | --- |
| Stored space | O(V + E) | O(V²) |
| Enumerate outgoing neighbors | O(out-degree) | O(V), scanning a row |
| Test a particular edge | O(out-degree) for a list; O(1) expected for a set | O(1) |
| Traverse all possible outgoing positions | O(V + E) total | O(V²) total |

[`Graph`](../../src/main/java/io/github/ichillous/dsa/graphs/Graph.java) uses ordered sets internally for deduplication and reproducible neighbor order. Its public neighbor list is a snapshot, not a mutable set view.

```java
import io.github.ichillous.dsa.graphs.Graph;
import java.util.List;

Graph graph = Graph.directed(3).addEdge(0, 1);
List<Integer> snapshot = graph.neighbors(0);
graph.addEdge(0, 2);
assert snapshot.equals(List.of(1));
assert graph.neighbors(0).equals(List.of(1, 2));
```

## Build a reference matrix

A small matrix is a useful independent test model for an adjacency implementation. This example constructs both representations from the same edge list and compares their meaning.

```java
import io.github.ichillous.dsa.graphs.Graph;

int[][] edges = {{0, 1}, {0, 2}, {2, 1}};
boolean[][] matrix = new boolean[3][3];
Graph graph = Graph.directed(3);
for (int[] edge : edges) {
    graph.addEdge(edge[0], edge[1]);
    matrix[edge[0]][edge[1]] = true;
}
for (int from = 0; from < 3; from++) {
    for (int to = 0; to < 3; to++) {
        assert matrix[from][to] == graph.neighbors(from).contains(to);
    }
}
assert !matrix[1][0]; // Directed edges are not automatically symmetric.
```

This tiny comparison intentionally favors clarity over efficient repeated snapshot calls. Production traversal obtains a neighbor snapshot once per visited vertex rather than once per candidate edge.

## Weights, parallel edges and absence

A single numeric matrix cell cannot represent multiple parallel edges without an explicit aggregation policy. Likewise, using zero to mean “no edge” discards valid zero-weight edges. `WeightedGraph` avoids that ambiguity with explicit edge records; it supports zero, negative and maximum-`long` weights.

For undirected storage, reverse adjacency entries describe the same logical edge. The public edge count counts that edge once, including a self-loop once. Mixing adjacency-entry count and edge count causes degree and complexity mistakes.

## Practice

1. Convert the diagram to an undirected graph and update both the list and matrix.
2. Design a weighted matrix that distinguishes absent edges from zero-cost edges.
3. Compare memory growth conceptually for 10,000 vertices with 20,000 edges; state why a dense matrix allocates many unused cells.

Read [traversal](traversal.md) and the [worked solutions](../../docs/solutions.md).
