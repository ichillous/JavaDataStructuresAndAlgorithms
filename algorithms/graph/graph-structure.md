# Graph structure: cut vertices, bipartiteness, cycles and Euler paths

These algorithms answer structural questions about a graph rather than distances:
which vertices hold it together, whether it splits into two sides, where a cycle
is, and whether one walk can use every edge once. All are in the maintained
[GraphStructureAlgorithms](../../src/main/java/io/github/ichillous/dsa/graphs/GraphStructureAlgorithms.java)
and are iterative, so a 100,000-vertex path does not overflow the call stack.

**Preconditions.** Articulation points, bipartition and odd cycles need an
undirected `Graph`; the directed-cycle search needs a directed one; the Euler path
accepts either. A wrong direction throws `IllegalArgumentException`, null throws
`NullPointerException`, and inputs are never modified. `Graph` ignores duplicate
edges and stores an undirected self-loop as one edge with one adjacency entry.
This lesson relies on that: a loop is a cycle of length one, is never part of a
bipartition, and adds 2 to an undirected degree.

## The invariants

- **Articulation points.** In a DFS, `low[v]` is the earliest discovery time
  reachable from `v`'s subtree by tree edges plus at most one back edge. A non-root
  `p` is a cut vertex when some child `c` has `low[c] >= discovery[p]` (the subtree
  cannot get above `p`). A root is a cut vertex only with two or more DFS children.
  This uses `>=`, unlike bridges which use `>`, because removing `p` itself also
  cuts a back edge that ends exactly at `p`.
- **Bipartition.** BFS colors each vertex opposite to its parent. Every edge must
  join different colors; the first edge joining equal colors proves an odd cycle.
  The smallest vertex of each component is left.
- **Odd-cycle witness.** For a same-colored edge `u-v` the tree paths from `u` and
  `v` to their nearest common ancestor have lengths of equal parity, so path plus
  the edge has odd length.
- **Directed cycle.** DFS keeps the current path. An edge into a vertex still on the
  path closes a cycle made of the path suffix; an edge into a finished vertex does
  not.
- **Euler path (Hierholzer).** Walk unused edges until stuck, then splice detours in
  as the stack unwinds. The path exists exactly when the edges are in one component
  and the degrees allow it: undirected, zero or two odd-degree vertices; directed,
  every `out - in` is 0 except at most one `+1` (the start) and one `-1` (the end).
  The result has `E + 1` vertices.

## A worked trace

```text
bowtie: triangles {0,1,2} and {2,3,4} share vertex 2
DFS from 0: disc 0=1, 1=2, 2=3, 3=4, 4=5, low(4)=3 (back edge 4-2)
finish 4: child of 3, low 3 >= disc(3)? low(4)=3 < disc(3)=4 -> 3 is not a cut
finish 3: child of 2, low(3)=3 >= disc(2)=3 -> 2 is a cut vertex
finish 2: child of 1, low(2)=1 < disc(1)=2 -> 1 is not a cut
articulation points: [2]

directed 0->1, 1->2, 2->0, 2->3     out-in: 2 is +1, 3 is -1, others 0
stack walk from 2 (first unused neighbor in insertion order): 2 0 1 2 3
Euler path: [2, 0, 1, 2, 3]
```

## Executable example

```java
import io.github.ichillous.dsa.graphs.Graph;
import io.github.ichillous.dsa.graphs.GraphStructureAlgorithms;
import java.util.List;

var bowtie = Graph.undirected(5).addEdge(0, 1).addEdge(1, 2).addEdge(2, 0)
        .addEdge(2, 3).addEdge(3, 4).addEdge(4, 2);
assert GraphStructureAlgorithms.articulationPoints(bowtie).equals(List.of(2));

var path = Graph.undirected(4).addEdge(0, 1).addEdge(1, 2).addEdge(2, 3);
var sides = GraphStructureAlgorithms.bipartition(path).orElseThrow();
assert sides.left().equals(List.of(0, 2));
assert sides.right().equals(List.of(1, 3));
assert GraphStructureAlgorithms.oddCycle(path).isEmpty();

var triangle = Graph.undirected(3).addEdge(0, 1).addEdge(1, 2).addEdge(2, 0);
assert GraphStructureAlgorithms.bipartition(triangle).isEmpty();
assert GraphStructureAlgorithms.oddCycle(triangle).orElseThrow().size() == 3;

var directed = Graph.directed(5).addEdge(0, 1).addEdge(1, 2).addEdge(2, 3)
        .addEdge(3, 1).addEdge(3, 4);
assert GraphStructureAlgorithms.directedCycle(directed).orElseThrow().equals(List.of(1, 2, 3));

var euler = Graph.directed(4).addEdge(0, 1).addEdge(1, 2).addEdge(2, 0).addEdge(2, 3);
assert GraphStructureAlgorithms.eulerianPath(euler).orElseThrow().equals(List.of(2, 0, 1, 2, 3));
var star = Graph.undirected(4).addEdge(0, 1).addEdge(0, 2).addEdge(0, 3);
assert GraphStructureAlgorithms.eulerianPath(star).isEmpty();

var loop = Graph.undirected(2).addEdge(0, 0).addEdge(0, 1);
assert GraphStructureAlgorithms.eulerianPath(loop).orElseThrow().equals(List.of(0, 0, 1));
```

## Costs and tie rules

| Operation | Time | Auxiliary space | Deterministic rule |
| --- | --- | --- | --- |
| `articulationPoints` | O(V + E) | O(V + E) | ascending vertex order |
| `bipartition` | O(V + E) | O(V + E) | smallest vertex of each component is left; lists ascending |
| `oddCycle` | O(V + E) | O(V + E) | first same-colored edge in BFS order |
| `directedCycle` | O(V + E) | O(V + E) | DFS from ascending vertices, neighbors in insertion order; starts at the vertex the closing edge reaches |
| `eulerianPath` | O(V + E) expected | O(V + E) | start vertex by the degree rule; first unused neighbor in `Graph` insertion order |

Space includes adjacency snapshots. The Euler walk is deterministic because `Graph`
keeps neighbors in insertion order; the expected-time bound comes from a hash lookup that
pairs the two adjacency entries of each undirected edge. A graph with no edges has the Euler path `[]`, whatever its
vertex count; isolated vertices are otherwise ignored.

## Pitfalls

- A single odd-degree vertex count is not enough. Two disjoint cycles have all
  degrees even but no Euler path, because the edges are not connected. The
  implementation checks the degree rule first and then that the walk consumed
  `E` edges.
- Treating an undirected loop as degree 1 gives the wrong parity. Here a loop at
  `v` adds 2, so `[0-0, 0-1]` has odd vertices 0 and 1 and the path `[0, 0, 1]`.
- DFS "visited" is not enough for directed cycles: in the diamond
  `0->1, 0->2, 1->3, 2->3` vertex 3 is reached twice but there is no cycle,
  because the second arrival finds 3 already finished, not on the path.
- Using `low[c] > discovery[p]` (the bridge test) would miss cut vertices such as
  the center of a bowtie.

## Practice

1. Remove each vertex of a small graph and recount components; compare with the
   `articulationPoints` answer.
2. Brute-force all `2^V` colorings of a graph on at most 10 vertices and check that
   a valid coloring exists exactly when `oddCycle` is empty.
3. Build a graph by XOR-ing a few random cycles (every degree stays even) and verify
   `eulerianPath` by deleting each consecutive edge from a multiset.

See the [catalog](../../docs/catalog.md) for the source and test links, the
[complexity guide](../../docs/complexity.md) for the cost summary, and
[depth-first search](dfs.md) for bridges and strongly connected components. For
the spanning-forest counterpart see [minimum spanning trees](minimum-spanning-trees.md).
