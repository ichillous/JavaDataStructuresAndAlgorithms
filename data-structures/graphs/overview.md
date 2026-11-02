# Graphs: model relationships explicitly

A graph has vertices and edges. Vertices may be isolated; edges may be directed, weighted, repeated or self-loops. Decide these semantics before choosing an algorithm: a route map and a prerequisite graph use different contracts.

[`Graph`](../../src/main/java/io/github/ichillous/dsa/graphs/Graph.java) uses integer vertices `[0, vertexCount)`, an explicit directed/undirected factory, and insertion-ordered adjacency sets. Repeated unweighted edges are ignored. In an undirected graph, adding the reverse of an existing edge does not add a new logical edge.

```java
import io.github.ichillous.dsa.graphs.Graph;
import java.util.List;

Graph relationships = Graph.undirected(4);
relationships.addEdge(0, 1).addEdge(0, 2).addEdge(1, 0);
assert relationships.vertexCount() == 4; // Vertex 3 is isolated.
assert relationships.edgeCount() == 2;
assert relationships.neighbors(0).equals(List.of(1, 2));
assert relationships.neighbors(3).isEmpty();
```

## Pick the right question

| Question | Algorithm / restriction |
| --- | --- |
| Which vertices are reachable? | BFS or DFS; directed edges are followed in their direction |
| Fewest edges from s to t? | BFS in an unweighted graph |
| Which undirected components exist? | Connected-components traversal |
| What prerequisite order is possible? | Topological sort of a directed acyclic graph |
| Least total nonnegative edge weight? | Dijkstra |
| Cheapest acyclic connections within every component? | Minimum spanning forest of an undirected weighted graph |

“Reachable from s” is not the same as “connected” in a directed graph. Strong and weak connectivity have separate meanings; the maintained `connectedComponents` API accepts only undirected input to avoid silently choosing one.

## Weighted edges have their own representation

[`WeightedGraph`](../../src/main/java/io/github/ichillous/dsa/graphs/WeightedGraph.java) keeps parallel edges, including identical ones, and accepts signed `long` weights. Its `edges()` returns each added undirected edge once; neighbor entries are oriented outward from the queried vertex.

```java
import io.github.ichillous.dsa.graphs.WeightedGraph;

WeightedGraph routes = WeightedGraph.directed(3);
routes.addEdge(0, 1, 5).addEdge(0, 1, 2).addEdge(1, 2, 4);
assert routes.edgeCount() == 3;
assert routes.neighbors(0).size() == 2;
assert routes.neighbors(1).get(0).to() == 2;
assert routes.neighbors(2).isEmpty();
```

A representation may permit weights that an algorithm rejects. Dijkstra requires every edge to be nonnegative, even in an unreachable component; Kruskal's minimum spanning forest permits negative weights. Negative edges are not universally “invalid graph data.”

## Connectivity without storing every path

[`UnionFind`](../../src/main/java/io/github/ichillous/dsa/structures/UnionFind.java) maintains undirected component membership as connections are added. Union by size and path compression give O(α(V)) amortized operations with O(V) storage. It cannot reconstruct a path or delete an edge; those are different requirements.

```java
import io.github.ichillous.dsa.structures.UnionFind;

UnionFind components = new UnionFind(4);
components.union(0, 1);
components.union(1, 2);
boolean mergedAgain = components.union(0, 2);
assert components.connected(0, 2);
assert !components.connected(0, 3);
assert !mergedAgain && components.components() == 2;
assert components.componentSize(1) == 3;
```

## Costs and ownership

Both adjacency representations store O(V + E) data. Adding an unweighted edge is O(1) expected; adding a weighted edge is O(1) amortized. `neighbors` returns an immutable snapshot in O(out-degree) time and space. Algorithms return detached immutable results, so later graph mutation does not update an old answer.

BFS, DFS, topological ordering and undirected components take O(V + E) time and space here, including snapshots. Dijkstra's lazy heap and Kruskal sorting take O(V + E log(E + 1)) time. Count parallel weighted edges in E; do not assume E is bounded by V² for a multigraph.

## Practice

1. Model course prerequisites and decide edge direction before drawing a sample schedule.
2. Give a directed graph where 0 reaches 1 but 1 cannot reach 0.
3. Compare reachability on small random graphs with an independent adjacency-matrix reference, including isolated vertices and self-loops.

Continue with [representations](representation.md), [traversals and paths](traversal.md) and the [worked solutions](../../docs/solutions.md).
