# Singly linked lists: one direction, explicit predecessors

A singly linked node stores a value and a reference to its successor. The sequence begins at `head` and ends at the first null successor. Maintaining a `tail` makes append O(1), but there is still no backward link.

## Trace removal

```text
before:  head -> A -> B -> C -> null
                         ^
                    remove B

after:   head -> A ------> C -> null
```

To remove `B`, a predecessor reference to `A` must already be available. Replacing `A.next` with `B.next` unlinks `B`. Finding `A` from the head costs O(n) in the worst case. Removing the head is the special case: assign the head's successor as the new head.

The core invariant is that repeated successor traversal visits each live node exactly once and eventually reaches null. A mistaken pointer assignment can create a cycle or disconnect the remaining suffix. With a tail field, removing the last live node must also reset the tail.

## Execute the sequence with a JDK deque

This example uses `java.util.LinkedList`, which is **doubly** linked internally. It demonstrates the front/back sequence contract without claiming that the repository supplies a separate singly linked implementation.

```java
import java.util.Deque;
import java.util.LinkedList;

Deque<String> sequence = new LinkedList<>();
sequence.addFirst("B");
sequence.addFirst("A");
sequence.addLast("C");
String first = sequence.removeFirst();
assert first.equals("A");
assert sequence.peekFirst().equals("B");
assert sequence.removeLast().equals("C");
```

For source you can inspect and modify, read [`DoublyLinkedList`](../../src/main/java/io/github/ichillous/dsa/collections/DoublyLinkedList.java). Removing its `previous` links is not sufficient to preserve the same endpoint costs: tail removal must then search for the predecessor.

## Costs and a common trap

Head insertion/removal and append with a tail pointer are O(1). Indexed search and tail removal are O(n) for a singly linked representation. Stored nodes occupy O(n) space; a single unlink operation needs O(1) workspace.

The “copy the next node into this node” trick does not implement general deletion: it cannot remove a tail and changes the identity of the retained node. It is inappropriate when callers hold node references or nodes carry additional identity-sensitive data.

## Practice

1. Draw updates for removing the head, an interior node and the tail from a three-node chain.
2. Explain why the one-node case changes both head and tail.
3. Design a singly linked list with a cycle-detection test before adding it to the library. Compare its observable sequence to a JDK model, and document which endpoint operations are linear.

Check the [worked solutions](../../docs/solutions.md), then study [doubly linked nodes](doubly.md).
