# Max flow, min cut and bipartite matching

A flow network is a directed graph whose edges carry a nonnegative `long` capacity.
A **flow** from a source `s` to a sink `t` assigns each edge an amount between 0 and
its capacity so that every vertex other than `s` and `t` receives exactly as much as
it sends. The **value** is the net amount leaving `s`. The maintained
[FlowNetwork](../../src/main/java/io/github/ichillous/dsa/graphs/FlowNetwork.java)
computes a maximum flow and a minimum cut with Dinic's algorithm, and
[BipartiteMatching](../../src/main/java/io/github/ichillous/dsa/graphs/BipartiteMatching.java)
computes a maximum matching with Hopcroft-Karp.

Preconditions: vertices are `0..V-1`; capacities are `>= 0`; parallel edges are kept
and each is a separate edge; `source != sink`. The network is never mutated by a
computation, so the same call can be repeated.

## The invariant: residual graphs

For every edge `u -> v` with capacity `c` and current flow `f`, the **residual graph**
has a forward arc of residual `c - f` and a backward arc `v -> u` of residual `f`.
Pushing flow along a backward arc cancels earlier flow. Two facts drive everything:

- **Augmenting path theorem.** A flow is maximum exactly when the residual graph has
  no `s -> t` path.
- **Max-flow/min-cut.** At that point let `S` be the vertices reachable from `s`.
  Every edge leaving `S` is saturated and every edge entering `S` carries zero flow,
  so the flow value equals the capacity of the edges leaving `S`. No cut can be
  smaller, because every flow is at most every cut's capacity.

Dinic's algorithm repeats two steps. A BFS labels residual vertices by distance from
`s`. A blocking flow then saturates level-increasing arcs until no such path is left.
After each phase the `s -> t` residual distance strictly increases, so there are at
most `V - 1` phases. The depth-first blocking-flow search and the BFS are iterative,
so a 100,000-vertex path does not overflow the call stack.

```text
s=0  t=3   edges (capacity): 0->1 (3), 0->2 (2), 1->2 (1), 1->3 (2), 2->3 (3)
phase 1: levels 0:0 1:1 2:1 3:2
         path 0->1->3 pushes 2, path 0->2->3 pushes 2
         arcs 1->3 and 0->2 are now saturated
phase 2: residual 0->1 has 1; 1->2 has 1; 2->3 has 1
         levels 0:0 1:1 2:2 3:3, path 0->1->2->3 pushes 1
phase 3: 0->1 is saturated and 0->2 too, so 3 is unreachable. Flow = 5
reachable from 0 in the last residual graph: {0}; cut edges 0->1 (3) + 0->2 (2) = 5
```

```java
import io.github.ichillous.dsa.graphs.FlowNetwork;
import io.github.ichillous.dsa.graphs.BipartiteMatching;
import java.util.List;

var network = new FlowNetwork(4)
        .addEdge(0, 1, 3).addEdge(0, 2, 2).addEdge(1, 2, 1)
        .addEdge(1, 3, 2).addEdge(2, 3, 3);
FlowNetwork.MaxFlow flow = network.maxFlow(0, 3);
assert flow.value() == 5;
assert flow.sourceSide().equals(List.of(0));
long cut = 0;
for (FlowNetwork.Edge e : network.edges()) {
    if (flow.sourceSide().contains(e.from()) && !flow.sourceSide().contains(e.to())) {
        cut += e.capacity();
    }
}
assert cut == flow.value();

var pairs = List.of(
        new BipartiteMatching.Pair(0, 0), new BipartiteMatching.Pair(0, 1),
        new BipartiteMatching.Pair(1, 0), new BipartiteMatching.Pair(2, 0));
List<BipartiteMatching.Pair> matching = BipartiteMatching.maximumMatching(3, 2, pairs);
assert matching.size() == 2;
assert matching.get(0).left() < matching.get(1).left();
```

## Matching as flow

A bipartite matching is a max-flow problem. Add a super-source with capacity-1 edges
to every left vertex, a capacity-1 edge from each left to each adjacent right vertex,
and capacity-1 edges from every right vertex to a super-sink. Integral capacities give
an integral maximum flow, whose saturated middle edges are a maximum matching.
Hopcroft-Karp is Dinic's phase structure specialized to this unit network: each phase
runs a BFS that stops at the first layer reaching a free right vertex, then a DFS over
layered edges that augments only along shortest paths until a maximal set of
vertex-disjoint ones is found. The shortest path length grows each phase, giving `O(sqrt(V))` phases. Duplicate input pairs are
ignored, out-of-range vertices throw `IndexOutOfBoundsException`, and the result is
sorted by left vertex. When several maximum matchings exist the one returned is
deterministic but unspecified; test the size and validity, not one particular answer.

## Costs

| Operation | Time | Auxiliary space |
| --- | --- | --- |
| `maxFlow` (Dinic) | O(V^2 E) | O(V + E) |
| `maximumMatching` (Hopcroft-Karp) | O(E sqrt(V)) after an O(E log E) deduplicating sort | O(V + E) |

Residuals never overflow: a backward arc's residual is the flow on its edge, which is
bounded by that edge's capacity. The total value is accumulated with
`Math.addExact`, so an answer above `Long.MAX_VALUE` throws `ArithmeticException`
instead of wrapping. Two parallel paths of capacity `Long.MAX_VALUE / 2` are exact.

**Pitfall:** a greedy choice is not a flow. With edges `0-0`, `0-1` and `1-0`,
taking `0-0` first blocks left vertex 1, yet the maximum has size 2. The augmenting
path starts at free left vertex 1, takes right vertex 0 (undoing the match `0-0`
through a backward arc) and re-matches left vertex 0 to right vertex 1. A residual
graph without backward arcs would stop at the wrong answer. Also, a maximum flow is
not unique; only its value and the minimal source side are determined.

## Not implemented here

Minimum-cost flow (successive shortest paths) and the Hungarian algorithm for
minimum-cost perfect assignment on weighted bipartite graphs are not implemented in
this repository. Reach for them when edges carry costs; plain `BipartiteMatching`
only maximizes the count of pairs.

## Practice

1. Compare `maxFlow` on random networks of at most eight vertices with the minimum over
   all `2^(V-2)` cuts that separate `s` from `t`. Use a fixed seed.
2. Verify a returned flow edge by edge: capacities respected, conservation at every
   inner vertex, and the cut formed by `sourceSide` equal to the value.
3. Model "assign tasks to workers" as a matching, then add one task adjacent to a
   single already-used worker and trace the augmenting path that reassigns it.

Continue with the [catalog](../../docs/catalog.md), the
[complexity guide](../../docs/complexity.md) and [BFS](bfs.md). The [solution finder](../../docs/solution-finder.md#16-spanning-trees-flows-and-matching) compares this technique with its alternatives.
