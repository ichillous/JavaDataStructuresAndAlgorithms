# Queues: process in arrival order

A FIFO queue adds at the back and removes at the front. Use it when earlier scheduled work should run first: level-order traversal, unweighted shortest paths, buffers or simple task processing.

```text
enqueue A, B, C       dequeue -> A
front [A B C] back    front [B C] back
```

The invariant is that the front is the earliest enqueued value not yet removed. Unlike a stack, a queue does not allow a later arrival to jump ahead.

```java
import io.github.ichillous.dsa.collections.ArrayQueue;

ArrayQueue<String> jobs = new ArrayQueue<>(2);
jobs.enqueue("first");
jobs.enqueue("second");
String next = jobs.dequeue();
jobs.enqueue("third");
assert next.equals("first");
assert jobs.peek().equals("second");
assert jobs.size() == 2;
```

[`ArrayQueue`](../../src/main/java/io/github/ichillous/dsa/collections/ArrayQueue.java) uses a growing circular buffer. Null elements are rejected; empty `dequeue` and `peek` throw `NoSuchElementException`. Initial capacity must be positive. It is an in-memory educational queue, with no blocking or concurrency guarantees.

## Queue, deque or priority queue?

| Need | Discipline | Maintained type |
| --- | --- | --- |
| Earliest arrival first | FIFO | `ArrayQueue` |
| Insert/remove at either end | Deque | [`RingDeque`](../../src/main/java/io/github/ichillous/dsa/collections/RingDeque.java) |
| Most urgent / smallest key first | Priority order | [`BinaryMinHeap`](../../src/main/java/io/github/ichillous/dsa/structures/BinaryMinHeap.java) |
| Most recent arrival first | LIFO | [`ArrayStack`](../../src/main/java/io/github/ichillous/dsa/collections/ArrayStack.java) |

A priority queue is not FIFO when priorities differ. Nor does it promise a stable order among equal priorities unless its comparator includes a tie breaker.

## Costs and an implementation trap

Circular-buffer enqueue is O(1) amortized and O(n) when growing; dequeue and peek are O(1). Storage is O(capacity), retained after draining. `clear()` scans the retained buffer, so it costs O(capacity), not merely O(current size).

Removing index zero from a dynamic array shifts every remaining element. Repeating that operation to drain `n` items costs O(n²) overall. A circular head index avoids those shifts while keeping the same logical FIFO order.

## Practice

1. Trace the queue after enqueueing `1, 2`, dequeuing once, then enqueueing `3`.
2. Explain why wraparound alone does not imply fullness; identify the separate state needed to distinguish full from empty.
3. Compare random enqueue/dequeue sequences with `ArrayDeque`, including growth after the head has moved.

Continue with [circular implementation](implementation.md), [priority queues](priority-queues.md) and the [worked solutions](../../docs/solutions.md).
