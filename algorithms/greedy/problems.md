# Greedy choice across different objectives

A rule is correct for a particular feasible set and objective. Changing “number
of activities” to “total value,” or allowing item reuse, can invalidate its proof.
The examples below separate a maintained API from a small executable JDK exercise.

## Coin change: a local rule that fails

For denominations `[1, 3, 4]` and amount 6, largest-first chooses `4 + 1 + 1`.
The optimum is `3 + 3`. Some coin systems admit a greedy rule, but arbitrary
positive denominations do not. Use the maintained
[DP formulation](../dynamic-programming/overview.md) when its numeric table fits.

```java
import io.github.ichillous.dsa.algorithms.DynamicProgramming;

int greedyCoinCount = 3; // 4 + 1 + 1 for this specific counterexample.
int optimalCoinCount = DynamicProgramming.minimumCoins(new int[] {1, 3, 4}, 6);
assert optimalCoinCount == 2;
assert greedyCoinCount > optimalCoinCount;
```

The DP uses O((c + 1)(a + 1)) time and O(a + 1) storage, with positive reusable
coins and nonnegative amount. A greedy counterexample is not a claim that every
DP is faster; it establishes that the local rule can return a wrong optimum.

## Kruskal: cheapest safe edge

For an undirected weighted graph, Kruskal scans edges by increasing weight and
adds an edge only when it joins different components. The invariant is an acyclic
selected forest extendable to a minimum spanning forest. The cut property
justifies the cheapest crossing edge; disjoint sets enforce acyclicity.

```java
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;

var graph = WeightedGraph.undirected(4)
        .addEdge(0, 1, 4).addEdge(1, 2, -2).addEdge(0, 2, 5);
var forest = GraphAlgorithms.minimumSpanningForest(graph);
assert forest.totalWeight() == 2L;
assert forest.edges().size() == 2;
assert forest.components() == 2; // Vertex 3 is isolated.
assert forest.edges().size() == graph.vertexCount() - forest.components();
```

This maintained algorithm accepts signed weights and parallel edges, ignores
self-loops, rejects directed graphs and returns immutable snapshots. Time is
O(V + E log(E + 1)); working storage including output is O(V + E). Total weight is
accumulated exactly and must fit in `long` at completion. A disconnected graph has
a minimum **forest**, not a spanning tree joining vertices with nonexistent edges.

## Huffman merging as a JDK exercise

For positive symbol frequencies, an optimal binary prefix-code tree can place
two least-frequent symbols at deepest sibling leaves. Merging them reduces to a
smaller instance. Each merge increases weighted code length by the combined
frequency, so summing merge weights gives the final weighted length.

```java
import java.util.List;
import java.util.PriorityQueue;

var frequencies = new PriorityQueue<Long>(List.of(5L, 9L, 12L, 13L, 16L, 45L));
long weightedLength = 0L;
while (frequencies.size() > 1) {
    long merged = Math.addExact(frequencies.remove(), frequencies.remove());
    weightedLength = Math.addExact(weightedLength, merged);
    frequencies.add(merged);
}
assert weightedLength == 224L;
assert frequencies.element() == 100L;
```

For `k` supplied frequencies, heap construction plus `k-1` merges costs O(k log k)
time and O(k) storage. This example computes a weighted length, not actual codes;
code construction requires recording tree children. It assumes the displayed
positive frequencies and at least two symbols. Equal-weight choices can produce
different equally good trees. Checked arithmetic exposes overflow instead of
silently changing the objective.

## Practice

1. Explain why the activity-selection exchange argument does not prove coin change.
2. Enumerate tiny spanning forests and compare weight, components and acyclicity.
3. Trace Huffman merges `5+9`, `12+13`, `14+16`, `25+30`, `45+55`; then record
   tree children to derive code lengths and confirm the same weighted total.

The [solutions](../../docs/solutions.md) discuss independent oracles and proof scope.
