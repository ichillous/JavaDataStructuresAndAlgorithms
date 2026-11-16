# Minimum spanning forests: Kruskal and Prim

A spanning forest connects every vertex that the graph connects, using no cycle and
the least total weight. A connected graph with `V` vertices gives a tree of `V - 1`
edges; a graph with `c` components gives `V - c` edges. Isolated vertices count as
components. Two maintained implementations give the same total weight:

- [GraphAlgorithms](../../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java)
  `minimumSpanningForest` is Kruskal: sort edges, add each one that joins two
  different sets.
- [GraphStructureAlgorithms](../../src/main/java/io/github/ichillous/dsa/graphs/GraphStructureAlgorithms.java)
  `primMinimumSpanningForest` is Prim: grow one tree from a start vertex by always
  taking the cheapest edge that leaves it.

**Preconditions.** An undirected `WeightedGraph`; a directed one throws
`IllegalArgumentException`. Negative weights, parallel edges and loops are allowed
(loops are never chosen). The total is accumulated exactly and must fit in a
`long`, otherwise `ArithmeticException` is thrown; intermediate cancellation, such
as `Long.MIN_VALUE` and `Long.MAX_VALUE` edges in one tree, is not an error.

## The invariant

The cut property: for any partition of the vertices, a cheapest edge crossing it
belongs to some minimum spanning forest. Prim applies it with the partition
"vertices already in the tree" versus the rest. Kruskal applies it with the
component of the cheapest remaining edge. Because both only ever add a crossing edge
that is cheapest, an exchange argument shows the final forest is minimum. Prim
restarts from the smallest unvisited vertex once its heap is empty, which gives one
tree per component.

## A worked trace

```text
edges: 0-1 (4), 0-2 (1), 1-2 (2), 1-3 (5), 2-3 (8)        start at 0
heap {0-1:4, 0-2:1}   take 0-2 (1)   tree {0,2}   push 1-2:2, 2-3:8
heap {0-1:4, 1-2:2, 2-3:8}   take 1-2 (2)   tree {0,1,2}   push 1-3:5
heap {0-1:4, 1-3:5, 2-3:8}   0-1 now internal, skip
take 1-3 (5)   tree {0,1,2,3}   total 1 + 2 + 5 = 8
```

## Executable example

```java
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import io.github.ichillous.dsa.graphs.GraphStructureAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;

var graph = WeightedGraph.undirected(5).addEdge(0, 1, 4).addEdge(0, 2, 1)
        .addEdge(1, 2, 2).addEdge(1, 3, 5).addEdge(2, 3, 8);
var prim = GraphStructureAlgorithms.primMinimumSpanningForest(graph);
var kruskal = GraphAlgorithms.minimumSpanningForest(graph);
assert prim.totalWeight() == 8;
assert prim.totalWeight() == kruskal.totalWeight();
assert prim.components() == 2;          // vertex 4 is isolated
assert prim.edges().size() == graph.vertexCount() - prim.components();

var signed = WeightedGraph.undirected(4).addEdge(0, 1, Long.MIN_VALUE)
        .addEdge(1, 2, Long.MAX_VALUE).addEdge(2, 3, 1).addEdge(3, 3, -5);
assert GraphStructureAlgorithms.primMinimumSpanningForest(signed).totalWeight() == 0;

var ties = WeightedGraph.undirected(4).addEdge(0, 3, 1).addEdge(0, 2, 1).addEdge(0, 1, 1);
var tied = GraphStructureAlgorithms.primMinimumSpanningForest(ties);
assert tied.edges().get(0).to() == 1;   // equal weights: smaller target first
```

## Costs and choices

| Algorithm | Time | Auxiliary space | Tie rule |
| --- | --- | --- | --- |
| Kruskal | O(V + E log(E + 1)) | O(V + E) | equal weights keep input order |
| Prim, lazy binary heap | O(V + E log(E + 1)) | O(V + E) | smaller weight, then smaller target vertex, then smaller source vertex |

The lazy heap may hold up to `2E` entries because stale edges are skipped on
removal rather than decreased in place. With a dense graph and an adjacency
matrix, an array-scan Prim runs in O(V^2) without a heap, but this library uses
adjacency lists. Prim edges are listed in selection order and oriented from the
tree vertex to the newly reached vertex; Kruskal returns the edges in the original
orientation.

## Pitfall: same total, different forest

With equal weights there can be several minimum forests. Prim and Kruskal may pick
different ones, so compare **totals and component counts**, not edge lists. Another
trap is to run Prim once from vertex 0 on a disconnected graph: that spans only one
component. The implementation restarts for every unvisited vertex. Finally, Prim
computes a minimum spanning tree, not shortest paths: the tree path between two
vertices need not be their shortest path (compare Dijkstra).

## Practice

1. Hand-run Prim from each possible start vertex on the worked example; the total
   must always be 8 even though the selection order changes.
2. Enumerate every edge subset of a tiny graph, keep acyclic ones with `V - c`
   edges and confirm the minimum total matches both algorithms.
3. Make a graph with several equal-weight edges and explain why the two edge lists
   differ while the totals agree.

See the [catalog](../../docs/catalog.md) for the source and test links, the
[complexity guide](../../docs/complexity.md) for the cost summary, and
[graph structure](graph-structure.md) for the related structural algorithms.
