# Graph traversal: schedule each vertex deliberately

Traversal explores reachability while avoiding repeated work on cycles. [`GraphAlgorithms`](../../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java) provides BFS, iterative DFS, unweighted shortest paths, components, topological ordering and weighted algorithms. Results are immutable snapshots.

## BFS: discover before enqueueing

BFS uses a FIFO queue. Mark a vertex when it is discovered and enqueued, so multiple neighbors cannot queue it repeatedly. The queue processes distance layers in increasing edge count.

```text
       0              BFS layers: {0}, {1,2}, {3,4}
      / \
     1   2            isolated vertex: 5
     |   |
     3---4
```

```java
import io.github.ichillous.dsa.graphs.Graph;
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import java.util.List;

Graph graph = Graph.undirected(6);
graph.addEdge(0, 1).addEdge(0, 2).addEdge(1, 3).addEdge(2, 4).addEdge(3, 4);
assert GraphAlgorithms.breadthFirst(graph, 0).equals(List.of(0, 1, 2, 3, 4));
assert GraphAlgorithms.shortestPath(graph, 0, 4).equals(List.of(0, 2, 4));
assert GraphAlgorithms.shortestPath(graph, 0, 5).isEmpty();
assert GraphAlgorithms.shortestPath(graph, 2, 2).equals(List.of(2));
```

The predecessor assigned on first discovery records a shortest path. Reversing the chain from target to source reconstructs an endpoint-inclusive result. Unreachable targets return an empty list; source-to-source returns one vertex.

## DFS: preserve unfinished neighbor scans

DFS follows one branch before returning to alternatives. The maintained implementation uses explicit frames with neighbor iterators, reproducing recursive insertion-order traversal without recursive Java calls. Simply pushing every neighbor can produce a different order.

```java
import io.github.ichillous.dsa.graphs.Graph;
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import java.util.List;

Graph graph = Graph.undirected(6);
graph.addEdge(0, 1).addEdge(0, 2).addEdge(1, 3).addEdge(2, 4).addEdge(3, 4);
assert GraphAlgorithms.depthFirst(graph, 0).equals(List.of(0, 1, 3, 4, 2));
assert GraphAlgorithms.connectedComponents(graph).equals(List.of(List.of(0, 1, 2, 3, 4), List.of(5)));
```

BFS and DFS both cost O(V + E) time; they do not optimize the same output. DFS paths need not have minimum edge count. Connected components starts another traversal for each unseen vertex, including isolated vertices; it accepts only undirected graphs.

## Weighted paths change the next-candidate rule

BFS cannot minimize arbitrary weights. Dijkstra uses a priority queue of tentative distances, ignores superseded entries, and requires every weight to be nonnegative.

```java
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import java.util.List;

WeightedGraph graph = WeightedGraph.directed(5);
graph.addEdge(0, 1, 5).addEdge(0, 2, 1).addEdge(2, 1, 1).addEdge(1, 3, 2);
var paths = GraphAlgorithms.dijkstra(graph, 0);
assert paths.distanceTo(3).orElseThrow() == 4;
assert paths.pathTo(3).equals(List.of(0, 2, 1, 3));
assert paths.distanceTo(4).isEmpty();
```

A distance of `Long.MAX_VALUE` is representable; absence uses `OptionalLong.empty()`. Overflowing detours are ignored, but an unrepresentable reachable shortest distance throws. Dijkstra costs O(V + E log(E + 1)) here because the lazy heap can contain O(E) entries. Traversal and weighted-path working space is O(V + E), including adjacency snapshots.

A topological order answers a different question: repeatedly remove directed vertices of indegree zero. Failing to remove all vertices proves a directed cycle. A cycle is acceptable for reachability but prevents a prerequisite order.

## Practice

1. Explain the duplicate work caused by marking BFS vertices only when dequeued.
2. Give a weighted graph where a one-edge path costs more than a two-edge path.
3. Verify paths by checking endpoints, consecutive edges and distance, rather than requiring one arbitrary tie choice. Compare weighted distances to Floyd–Warshall on tiny graphs.

Check the [worked solutions](../../docs/solutions.md) and the focused [BFS](../../algorithms/graph/bfs.md) / [DFS](../../algorithms/graph/dfs.md) lessons.
