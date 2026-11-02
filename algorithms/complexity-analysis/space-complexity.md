# Practical memory contracts: capacity, snapshots and search

The [space foundations](../../complexity-analysis/space-complexity.md) explain
input, workspace and output. Here, track which object owns each allocation and
when a caller can safely retain it. These distinctions affect API behavior as
well as the big-O description.

## Capacity is retained state

`DynamicArray`, `ArrayStack` and `ArrayQueue` grow arrays geometrically. Removing
values releases their occupied references but does not shrink those arrays.
Stored space is O(capacity), which can reflect a past maximum or a large initial
reservation rather than the present element count. A resizing append temporarily
needs both the old and new arrays while values are copied.

The queue stores logical order around a circular buffer. Dequeue changes a head
index instead of shifting all elements. Its invariant is that logical element `i`
lives at `(head + i) mod capacity`; growing must copy logical order before resetting
head to zero. See the [queue lesson](../../data-structures/queues/implementation.md).

## Snapshot results are detached from later mutation

The graph API returns immutable neighbor snapshots, not live collections exposing
internal adjacency. Taking a snapshot of degree `d` costs O(d) time and storage.

```java
import io.github.ichillous.dsa.graphs.Graph;
import java.util.List;

var graph = Graph.directed(3).addEdge(0, 1);
var before = graph.neighbors(0);
graph.addEdge(0, 2);
assert before.equals(List.of(1));
assert graph.neighbors(0).equals(List.of(1, 2));
boolean immutable = false;
try {
    before.add(2);
} catch (UnsupportedOperationException expected) {
    immutable = true;
}
assert immutable;
```

BFS/DFS bounds include adjacency snapshots, visited state and results. A traversal
with an O(V) visited array and copied adjacency may use O(V + E), even though an
abstract textbook traversal over directly accessible adjacency needs only O(V)
extra state. Describe the implementation being used.

## Search trees and returned solutions

A depth-`d` recursive search can use O(d) path state while still returning
exponentially many solutions. The maintained permutation API limits input to nine
values and copies each completed solution. The input limit prevents accidental
unbounded enumeration; it does not change the underlying exponential growth.

Sudoku works on a private fixed-size board and copies back only on success.
That extra storage provides a useful mutation guarantee. A failed Fenwick update
similarly needs a validation strategy before changing retained sums. Saving memory
by partially mutating caller state can weaken the API contract.

**Counterexample:** summing allocation sizes across all completed recursive calls
measures allocation traffic, not peak live memory. Conversely, ignoring a returned
list because “the algorithm already finished” hides memory still owned by its caller.
Java does not guarantee tail-call elimination, so tail-recursive source still
needs a stack-depth analysis.

## Practice

1. Contrast the neighbor snapshot with an array/list iterator invalidated by a
   structural mutation. Which behavior is promised by each API?
2. Account for a graph traversal's snapshots, state arrays, explicit frames and output.
3. Propose a streaming permutation API and compare its memory lifetime with the
   current materialized result, including what happens when a consumer keeps results.

Use the [solutions](../../docs/solutions.md) and
[complexity table](../../docs/complexity.md) to check your accounting.
