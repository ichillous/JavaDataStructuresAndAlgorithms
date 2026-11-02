# Practice, then inspect the reasoning

Work from the [exercise sheet](exercises.md) and [topic lessons](../README.md).
For each task, predict the result, draw the state, then open the solution. Every
Java block below is compiled and executed by the documentation test. These are
small uses of the maintained library, not a second set of implementations.

## 1. Boundaries and duplicate values

Find the number of occurrences of `2` in `[1,2,2,2,5]` without scanning the array.
State what is true before `low` and at/after `high` during lower-bound search.

<details><summary>Reasoning and executable answer</summary>

The lower bound is the first value at least 2; the upper bound is the first value
greater than 2. Their difference counts the equal run. During lower-bound search,
all positions before `low` are too small, and all positions at/after `high` are
large enough. Each step shrinks the uncertain interval, retaining the midpoint as a possible
answer boundary when `high` moves to it, and strictly decreases `high-low`. Two logarithmic searches still take O(log n) time and O(1)
space. An O(n) sortedness check would change that bound; sorted input is required.

```java
import io.github.ichillous.dsa.algorithms.SearchAlgorithms;

int[] sorted = {1, 2, 2, 2, 5};
int from = SearchAlgorithms.lowerBound(sorted, 2);
int to = SearchAlgorithms.upperBound(sorted, 2);
assert from == 1 && to == 4 && to - from == 3;
assert SearchAlgorithms.lowerBound(sorted, 6) == sorted.length;
assert SearchAlgorithms.binarySearch(sorted, 4) == -1;
```

Try a target below every value, above every value, and an empty array. Run
`java -jar target/java-data-structures-and-algorithms-1.0.0-SNAPSHOT.jar learn binary-search 4`
to see each interval.

</details>

## 2. A cache hit changes the eviction candidate

With capacity two: put A, put B, get A, put C. Which key disappears? How would a
FIFO cache differ?

<details><summary>Reasoning and executable answer</summary>

The recency list changes `A,B -> B,A -> A,C`, least recent first. A successful
lookup promotes its node, so B is evicted. FIFO would ignore that hit and evict A.
The map must contain exactly the same keys as the linked nodes; unlinking and
promotion must update both neighbors and both end pointers.

```java
import io.github.ichillous.dsa.collections.LruCache;
import io.github.ichillous.dsa.collections.RingDeque;

var cache = new LruCache<String, Integer>(2);
cache.put("A", 10);
cache.put("B", 20);
assert cache.get("A") == 10;
cache.put("C", 30);
assert cache.get("B") == null;
assert cache.get("A") == 10 && cache.get("C") == 30;
assert cache.size() == 2;
var deque = new RingDeque<Integer>(2);
deque.addLast(1);
deque.addLast(2);
assert deque.removeFirst() == 1;
deque.addLast(3);
deque.addFirst(0); // grow after the head has wrapped
assert deque.removeFirst() == 0;
assert deque.removeLast() == 3 && deque.removeFirst() == 2;
```

Use an access-order `LinkedHashMap` as an independent cache model and `ArrayDeque`
as a deque model. Include replacement, misses, capacity one, clear, and reuse.

</details>

## 3. Delete a two-child root and a shared prefix

Delete 4 from an ordered set containing 1 through 7. Separately, remove `car`
without losing `cart` or `cat`.

<details><summary>Reasoning and executable answer</summary>

For two-child BST deletion, replacing a node with its inorder successor preserves
ordering; the successor's original position then has at most one child to unlink.
An AVL tree must also update heights and rebalance every ancestor on the return
path. For a trie, word membership is a flag independent of whether children exist.
Prune a node only when it has no children and ends no other word.

```java
import io.github.ichillous.dsa.structures.AvlTree;
import io.github.ichillous.dsa.structures.Trie;
import java.util.List;

var tree = new AvlTree<Integer>();
for (int value : new int[]{4, 2, 6, 1, 3, 5, 7}) tree.add(value);
assert tree.remove(4);
assert tree.inOrder().equals(List.of(1, 2, 3, 5, 6, 7));
assert tree.size() == 6;
var trie = new Trie();
for (String word : List.of("", "car", "cart", "cat")) trie.add(word);
assert trie.remove("car") && !trie.contains("car");
assert trie.contains("cart") && trie.contains("cat") && trie.contains("");
assert trie.wordsWithPrefix("ca", 1).equals(List.of("cart"));
```

Extend the sequence with repeated deletion and deletion until empty. A sorted
snapshot alone does not prove AVL balance; the maintained AVL tests also inspect
the height/balance invariant after updates.

</details>

## 4. Range sum and minimum have different empty identities

For `[2,-1,4,3]`, add five at index one and query `[1,3)`. What should `[2,2)` mean?

<details><summary>Reasoning and executable answer</summary>

The changed array is `[2,4,4,3]`, giving sum 8 and minimum 4. The empty sum is zero;
an empty minimum has no value. `OptionalLong` avoids treating `Long.MAX_VALUE` as
both a legitimate minimum and a sentinel. A Fenwick prefix query combines disjoint
blocks by clearing the lowest set bit; a segment query combines covered intervals.

```java
import io.github.ichillous.dsa.structures.FenwickTree;
import io.github.ichillous.dsa.structures.SegmentTree;

long[] values = {2, -1, 4, 3};
var sums = new FenwickTree(values);
var minima = new SegmentTree(values);
sums.add(1, 5);
minima.set(1, 4);
assert sums.rangeSum(1, 3) == 8;
assert minima.rangeMin(1, 3).orElseThrow() == 4;
assert sums.rangeSum(2, 2) == 0;
assert minima.rangeMin(2, 2).isEmpty();
assert new SegmentTree(new long[]{Long.MAX_VALUE}).rangeMin(0, 1).orElseThrow() == Long.MAX_VALUE;
```

Compare every small interval with a direct array model after each update. For
Fenwick updates, force overflow and verify that no partial update survives.

</details>

## 5. Negative edges and negative cycles are different

For directed edges `0->1=4`, `0->2=5`, `1->2=-7`, find the shortest route. Then add
`2->1=1`. Explain why a finite answer disappears only in the second graph.

<details><summary>Reasoning and executable answer</summary>

The first graph has route `[0,1,2]` of cost -3 and no cycle. Dijkstra's precondition
fails, but Bellman–Ford works: repeated edge relaxation covers paths with
successively more edges. Any shortest path without a reachable negative cycle can
be made simple and uses at most V-1 edges. A further improvement proves a reachable
negative cycle. After the new edge, the `1->2->1` cycle costs -6 and can be repeated.
An unreachable negative cycle does not change distances from this source.

```java
import io.github.ichillous.dsa.graphs.AdvancedGraphAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import java.util.List;

var graph = WeightedGraph.directed(3)
    .addEdge(0, 1, 4).addEdge(0, 2, 5).addEdge(1, 2, -7);
var paths = AdvancedGraphAlgorithms.bellmanFord(graph, 0);
assert paths.pathTo(2).equals(List.of(0, 1, 2));
assert paths.distanceTo(2).orElseThrow() == -3;
graph.addEdge(2, 1, 1);
boolean rejected = false;
try { AdvancedGraphAlgorithms.bellmanFord(graph, 0); }
catch (IllegalArgumentException expected) { rejected = true; }
assert rejected;
```

The implementation uses exact internal integers so an overflowing intermediate
detour cannot fabricate a cycle or hide a finite answer. Final distances must fit
in `long`. Test negative cycles before interpreting finite-distance overflow.

</details>

## 6. Local connectivity versus global reachability

What are the strongly connected groups in `0<->1`, `1->2`, `2<->3`? Which edge in
an undirected triangle with a tail is a bridge?

<details><summary>Reasoning and executable answer</summary>

SCC membership requires mutual reachability, not just one directed path. Kosaraju
uses reverse finishing order and the transposed graph to separate maximal groups.
For bridges, a DFS tree edge to a child is essential exactly when that child's
subtree cannot reach the parent or an ancestor without using that edge. The lowlink
comparison is strict: `low[child] > discovery[parent]`.

```java
import io.github.ichillous.dsa.graphs.AdvancedGraphAlgorithms;
import io.github.ichillous.dsa.graphs.Graph;
import java.util.List;

var directed = Graph.directed(4).addEdge(0, 1).addEdge(1, 0)
    .addEdge(1, 2).addEdge(2, 3).addEdge(3, 2);
assert AdvancedGraphAlgorithms.stronglyConnectedComponents(directed)
    .equals(List.of(List.of(0, 1), List.of(2, 3)));
var undirected = Graph.undirected(4).addEdge(0, 1).addEdge(1, 2)
    .addEdge(2, 0).addEdge(2, 3);
assert AdvancedGraphAlgorithms.bridges(undirected)
    .equals(List.of(new AdvancedGraphAlgorithms.Bridge(2, 3)));
```

For tiny graphs, compute mutual reachability independently and remove one edge at
a time to test bridges. Use a long path to expose recursive call-stack limits.

</details>

## 7. State and loop order prevent item reuse

One item has weight 2 and value 3; capacity is 4. Why is the answer 3 rather than 6?
Can a zero-weight item be included twice?

<details><summary>Reasoning and executable answer</summary>

In 0/1 knapsack, descending capacity reads the previous item's state. Ascending
capacity could reuse the same positive-weight item. At zero weight the slot reads
and writes itself once per item, so the item is still consumed only once. Empty
selection remains available; negative-value items need not be chosen. This is
pseudopolynomial in capacity, not polynomial in its encoded bit length.

```java
import io.github.ichillous.dsa.algorithms.DynamicProgramming;
import io.github.ichillous.dsa.algorithms.SequenceAlgorithms;

assert DynamicProgramming.knapsack01(new int[]{2}, new int[]{3}, 4) == 3;
assert DynamicProgramming.knapsack01(new int[]{0, 2}, new int[]{5, 3}, 4) == 8;
assert DynamicProgramming.minimumCoins(new int[]{1, 3, 4}, 6) == 2;
var best = SequenceAlgorithms.maximumSubarray(new int[]{-5, 4, -1, 3, -8}).orElseThrow();
assert best.startInclusive() == 1 && best.endExclusive() == 4 && best.sum() == 6;
assert SequenceAlgorithms.countInversions(new int[]{3, 1, 2}) == 2;
```

Use a subset-enumeration oracle for tiny knapsacks. For maximum subarrays, enumerate
all nonempty intervals, including all-negative and tied arrays.

</details>

## 8. Duplicates and signed values defeat naive pruning

Generate permutations of `[1,1,2]`. Find indices summing to -2 in `[3,-5,9]`.

<details><summary>Reasoning and executable answer</summary>

Permutation choices track positions, so equal values can occupy different slots;
skipping equivalent unused choices prevents duplicate answers. Signed subset sum
cannot stop merely because the remaining target is negative. The bounded
meet-in-the-middle implementation enumerates two halves and looks for complementary
sums. It returns indices so repeated values retain identity.

```java
import io.github.ichillous.dsa.algorithms.Backtracking;
import io.github.ichillous.dsa.algorithms.BacktrackingPuzzles;
import java.util.List;

assert Backtracking.uniquePermutations(new int[]{1, 1, 2}).size() == 3;
assert Backtracking.uniquePermutations(new int[0]).equals(List.of(List.of()));
assert BacktrackingPuzzles.subsetSum(new int[]{3, -5, 9}, -2).orElseThrow().equals(List.of(0, 1));
assert BacktrackingPuzzles.subsetSum(new int[]{3, -5, 9}, 0).orElseThrow().isEmpty();
assert BacktrackingPuzzles.subsetSum(new int[]{3, -5, 9}, Long.MIN_VALUE).isEmpty();
assert Backtracking.nQueens(4).size() == 2;
```

For Sudoku, valid row/column/box constraints are necessary before search begins.
The solver searches a private copy and copies back only a complete solution, so
an unsatisfiable puzzle cannot leave half a solution in the caller's board. See
the [puzzle tests](../src/test/java/io/github/ichillous/dsa/algorithms/BacktrackingPuzzlesTest.java)
for invalid givens and failure-atomicity examples.

</details>

## 9. Why bottom-up heapify is linear

Why is building a heap O(n) when one sift can take O(log n)? What happens for an
empty or singleton input?

<details><summary>Reasoning and executable answer</summary>

A node is sifted only after its child subtrees are heaps. A sift can move no farther
than that node's height. In a complete tree, the number of nodes with height at
least h is floor(n/2^h). Summing those height budgets gives
`sum(h>=1, floor(n/2^h)) < n`. Each level uses constant comparisons/moves, hence
O(n) total work. Counting O(log n) for every leaf discards the fact that leaves
need no sift. Separate insertion has a different O(n log n) worst-case bound. Empty and
singleton construction performs no sifts; the strict height-budget inequality
assumes n > 0.

```java
import io.github.ichillous.dsa.structures.BinaryMinHeap;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

int n = 31;
long heightBudget = 0;
for (int nodes = n / 2; nodes > 0; nodes /= 2) heightBudget += nodes;
assert heightBudget == 26 && heightBudget < n;
var input = new ArrayList<>(List.of(9, 4, 7, 1, 3, 2));
var heap = new BinaryMinHeap<Integer>(input);
input.set(0, -100); // heap owns a copy of the references
var removed = new ArrayList<Integer>();
while (!heap.isEmpty()) removed.add(heap.remove());
assert removed.equals(List.of(1, 2, 3, 4, 7, 9));
boolean rejected = false;
try { heap.peek(); } catch (NoSuchElementException expected) { rejected = true; }
assert rejected;
var singleton = new BinaryMinHeap<Integer>(List.of(8));
int only = singleton.remove();
assert only == 8 && singleton.isEmpty();
```

After removing a root, the replacement follows the smaller child. Following the
larger one can leave the replacement parent larger than its smaller child. Test complete removal
order against `PriorityQueue` or `Arrays.sort`, including duplicates and empty input.

</details>

## 10. Equal bracket counts do not prove proper nesting

Both `([])` and `([)]` contain two opens and two closes. Why does only the first
have a valid parse?

<details><summary>Reasoning and executable answer</summary>

The stack records the types of unmatched openings, in order. Each closer must
match the most recent opening; matching an older one would cross unfinished work.
An extra closer fails immediately, while extra openings leave the stack nonempty.
Non-bracket characters are ignored by this particular checker.

```java
import io.github.ichillous.dsa.collections.ArrayStack;
import java.util.ArrayList;
import java.util.List;

var results = new ArrayList<Boolean>();
for (String expression : List.of("([]){}", "([)]", "", "(()", ")(")) {
    var pending = new ArrayStack<Character>();
    boolean valid = true;
    for (char token : expression.toCharArray()) {
        if (token == '(' || token == '[' || token == '{') {
            pending.push(token);
        } else if (token == ')' || token == ']' || token == '}') {
            if (pending.isEmpty()) { valid = false; break; }
            char open = pending.pop();
            boolean matches = (open == '(' && token == ')')
                || (open == '[' && token == ']') || (open == '{' && token == '}');
            if (!matches) { valid = false; break; }
        }
    }
    results.add(valid && pending.isEmpty());
}
assert results.equals(List.of(true, false, true, false, false));
```

Each token is processed once and each opening is pushed/popped at most once:
O(n) amortized total time, O(n) worst-case stack storage. For an independent oracle,
generate small strings from the grammar `S -> empty | SS | (S) | [S] | {S}`, then
insert/delete/mismatch brackets to construct counterexamples. Do not compare two
copies of the same stack algorithm and call that independent validation.

</details>

## 11. A collision chain and a probe orbit have different stopping rules

How do equal-hash keys stay distinct? Why does capacity eight with step two fail
to visit half the table even when free slots remain?

<details><summary>Reasoning and executable answer</summary>

Separate chaining uses equality within one bucket. Each equality-equivalent key
appears once; replacement must preserve size, and removal reconnects the chain.
An absent search stops at the end of that chain, regardless of other buckets.

```java
import io.github.ichillous.dsa.collections.ChainedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

assert "Aa".hashCode() == "BB".hashCode();
var map = new ChainedHashMap<String, Integer>(2);
map.put("Aa", 1);
map.put("BB", 2);
Integer old = map.put("Aa", 3);
assert old == 1 && map.size() == 2;
Integer removed = map.remove("Aa");
assert removed == 3 && map.get("BB") == 2 && map.size() == 1;
var orbit = new LinkedHashSet<Integer>();
for (int probe = 0; probe < 8; probe++) orbit.add((probe * 2) % 8);
assert List.copyOf(orbit).equals(List.of(0, 2, 4, 6));
orbit.clear();
for (int probe = 0; probe < 8; probe++) orbit.add((probe * 3) % 8);
assert orbit.size() == 8;
```

Open addressing follows a probe sequence instead of a chain. A full-table miss
must stop after a bounded number of probes. Step and capacity must be coprime for
a full orbit; positivity alone is insufficient. Deletion also must preserve the
path a lookup follows:

```text
same home bucket:    0:Aa  1:BB  2:never-used
delete Aa safely:    0:deleted  1:BB  2:never-used
```

Changing slot zero to never-used would let BB's lookup stop too early. Tombstones
or a proven cluster-repair scheme preserve the search path. The maintained map
uses chaining and does not expose a probing API; this orbit is a design exercise,
not another map implementation. Compare maintained map operations with `HashMap`
using many distinct collision keys, replacement, absent removals and resizing.

</details>
