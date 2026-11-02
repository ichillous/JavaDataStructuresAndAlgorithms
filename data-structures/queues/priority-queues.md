# Priority queues: remove the best candidate

A priority queue removes the smallest or largest key according to an ordering. It is useful when the next task depends on priority rather than arrival: Dijkstra's tentative distances, scheduling, event simulation and selecting the best `k` candidates.

A heap guarantees that its root is minimal, not that its entire array is sorted. After every removal the next minimum becomes accessible; arbitrary iteration need not follow priority order.

```java
import io.github.ichillous.dsa.structures.BinaryMinHeap;
import java.util.List;

BinaryMinHeap<Integer> priorities = new BinaryMinHeap<>(List.of(30, 10, 20, 10));
assert priorities.peek() == 10;
assert priorities.size() == 4;
assert priorities.remove() == 10;
assert priorities.remove() == 10;
assert priorities.remove() == 20;
assert priorities.remove() == 30;
```

[`BinaryMinHeap`](../../src/main/java/io/github/ichillous/dsa/structures/BinaryMinHeap.java) uses natural ordering, keeps duplicates and rejects nulls. Empty peek/removal throws `NoSuchElementException`. Build from a collection copies its values and uses O(n) bottom-up heapification.

## Reverse the ordering with the JDK

The maintained heap has a deliberately small natural-order API. When an application needs a comparator, `java.util.PriorityQueue` supplies that feature directly.

```java
import java.util.Comparator;
import java.util.PriorityQueue;

PriorityQueue<Integer> largestFirst = new PriorityQueue<>(Comparator.reverseOrder());
largestFirst.add(Integer.MIN_VALUE);
largestFirst.add(7);
largestFirst.add(Integer.MAX_VALUE);
assert largestFirst.remove() == Integer.MAX_VALUE;
assert largestFirst.remove() == 7;
assert largestFirst.remove() == Integer.MIN_VALUE;
```

Avoid comparators such as `(a, b) -> b - a`: subtraction can overflow and reverse the intended ordering. Use `Integer.compare`, comparator builders or `reverseOrder`. Equal priorities need an explicit secondary key if a deterministic tie policy matters; priority alone does not establish FIFO ties.

## Cost and a bounded-selection pattern

Peek is O(1); remove-min is O(log n); insertion is O(log n) amortized, with occasional O(n) backing-array growth. Stored space is O(n) relative to retained peak capacity. Sorting every time a value arrives would do much more work.

To keep the largest `k` values from a stream, maintain a **min** heap of at most `k` values. If a new value exceeds the minimum, replace that minimum. This costs O(n log k) time and O(k) stored space for positive `k`. A max heap would expose the value you want to keep, rather than the weakest retained candidate.

## Practice

1. Explain why `[1, 4, 2, 9, 7, 3]` is a valid min heap but not a sorted array.
2. Choose an ordering for equal-priority tasks and explain whether stability is required.
3. Implement the largest-`k` pattern with `BinaryMinHeap`, comparing results with sorting a full copy of each small test stream.

Read [heap operations](../heaps/operations.md) and the [worked solutions](../../docs/solutions.md).
