# AVL trees: restore balance locally

[`AvlTree`](../../src/main/java/io/github/ichillous/dsa/structures/AvlTree.java) adds a height invariant to BST order: at every node, the heights of the left and right subtrees differ by at most one. Height is measured in edges, with an empty subtree at -1 and a leaf at 0.

This bounds height by O(log n), giving O(log n) worst-case search, insertion and deletion. The tree remains a set: null keys are rejected, and `compareTo == 0` means duplicate.

| At&nbsp;a&nbsp;glance | |
| --- | --- |
| **Use&nbsp;it&nbsp;when** | Lookups dominate and you want the shallowest guaranteed height, with membership and sorted traversal only. |
| **Avoid&nbsp;it&nbsp;when** | You need a map, nearest-key queries or rank and select; use [`RedBlackTreeMap`](red-black-trees.md) or the JDK's `TreeMap`. |
| **Cost** | O(log n) worst case for `add`, `remove` and `contains`; `height()` is O(1); `inOrder()` is O(n) |
| **In&nbsp;this&nbsp;repo** | [`AvlTree`](../../src/main/java/io/github/ichillous/dsa/structures/AvlTree.java) |
| **In&nbsp;the&nbsp;JDK** | No direct equivalent: `java.util.TreeSet` is red-black based |

<!-- visual:generated avl-rotation -->
<picture><source media="(prefers-color-scheme: dark)" srcset="../../docs/visuals/assets/avl-rotation-dark.svg"><img src="../../docs/visuals/assets/avl-rotation.svg" alt="Inserting 30, 20 and 10 produces a left-heavy chain. A right rotation makes 20 the root with children 10 and 30, preserving in-order traversal and restoring height one." width="520"></picture>

**Figure 07.** Inserting 30, 20 and 10 produces a left-heavy chain. A right rotation makes 20 the root with children 10 and 30, preserving in-order traversal and restoring height one. [Visual index and step-throughs](../../docs/visuals/README.md).
<!-- /visual:generated -->

## A rotation preserves sorted order

Insert `30, 20, 10`. The root becomes left-heavy by two levels. A right rotation promotes 20:

```text
before:       30             after:       20
             /                          /  \
            20                         10  30
           /
          10
```

The inorder sequence remains `10, 20, 30`. More generally, a rotation preserves the relative order of the subtrees transferred between nodes. Recompute the demoted node's height before the promoted node's height, because the latter depends on the former.

```java
import io.github.ichillous.dsa.structures.AvlTree;
import java.util.List;

AvlTree<Integer> keys = new AvlTree<>();
keys.add(30);
keys.add(20);
keys.add(10);
assert keys.height() == 1;
assert keys.inOrder().equals(List.of(10, 20, 30));
assert keys.size() == 3;
```

## Four imbalance shapes

Here “left-heavy child” and “right-heavy child” refer to subtree heights, rather than the most recently inserted key.

| Heavy path from an unbalanced node | Repair |
| --- | --- |
| Left, then left | Right rotation |
| Right, then right | Left rotation |
| Left, then right | Left rotation on child, then right rotation |
| Right, then left | Right rotation on child, then left rotation |

Inserting `30, 10, 20` needs the double rotation: a single right rotation would not place the median key at the local root. The cases are small, but stale heights can make later choices wrong.

```java
import io.github.ichillous.dsa.structures.AvlTree;
import java.util.List;

AvlTree<Integer> keys = new AvlTree<>();
for (int value : new int[] {30, 10, 20}) keys.add(value);
assert keys.height() == 1;
assert keys.inOrder().equals(List.of(10, 20, 30));
boolean removed = keys.remove(20);
assert removed && keys.size() == 2;
assert keys.inOrder().equals(List.of(10, 30));
assert keys.height() == 1;
```

## Deletion must rebalance too

Removing a key can shorten a subtree, making an ancestor unbalanced. Repair may have to continue to the root. A heavy child with equal-height children is a valid deletion case; deciding solely from the removed key misses that case. The implementation checks child balance factors and updates cached heights along the return path.

Search and updates cost O(log n) with O(log n) recursive stack for updates. Cached root height is O(1) to read. Stored nodes cost O(n); inorder traversal costs O(n) time and O(n) returned snapshot space. Rotations are O(1) local work, but an operation may repair multiple ancestors.

## In real Java

The JDK has no AVL class. `java.util.TreeSet` offers what `AvlTree` lacks: according to the Java SE 17 documentation, `floor(e)` returns the greatest element less than or equal to `e` and `ceiling(e)` the least element greater than or equal to it, each `null` when none exists, and `pollFirst()` removes the lowest element. It is the better choice when nearest-key queries are needed.

```java
import io.github.ichillous.dsa.structures.AvlTree;
import java.util.List;
import java.util.TreeSet;

AvlTree<Integer> avl = new AvlTree<>();
TreeSet<Integer> jdk = new TreeSet<>();
for (int key : List.of(10, 20, 30, 40)) {
    avl.add(key);
    jdk.add(key);
}
assert jdk.floor(25) == 20 && jdk.ceiling(25) == 30;
assert jdk.floor(5) == null;
assert jdk.pollFirst() == 10;
assert avl.contains(10) && !jdk.contains(10);
assert avl.inOrder().equals(List.of(10, 20, 30, 40));
```

## Common mistakes

1. **Rebalancing only at the new node's parent.** Inserting 30, 20, 10 leaves 10's parent, 20, balanced, but the root 30 is two levels left-heavy. Every ancestor on the return path must be checked.
2. **Applying a single rotation to a zig-zag.** Inserting 30, 10, 20 and rotating right at 30 leaves 10 on top with 30 on its right and 20 below that: still a tree that is out of balance. The invariant needs the double rotation that promotes the median key, 20.
3. **Updating heights in the wrong order.** In the right rotation of 30, 20, 10, recompute the demoted 30 before the promoted 20. Otherwise 20 caches a stale height and the balance factor above it is wrong, which `height()` and sorted output alone may not reveal.

## Practice

1. Draw the four insertion cases using three distinct keys; verify the same sorted result after each repair.
2. Find a deletion where the heavy child has balance zero. Explain which single rotation preserves balance.
3. Compare random operations with `TreeSet`, then independently validate every node's BST range, cached height and balance. Size and sorted output alone can miss stale-height defects.

## Check yourself

<details>
<summary>What is the height of an AVL tree after inserting 1, 2, 3, 4, 5, 6, 7 in order?</summary>

2. The rotations keep it a perfect tree of seven keys, with 4 at the root, whereas a plain BST would have height 6.

</details>

<details>
<summary>Why does a rotation not change the in-order sequence?</summary>

It only moves a middle subtree from one node to the other, and that subtree stays between the same two keys, so the left, root, right reading is unchanged.

</details>

<details>
<summary>Can deleting a key force rebalancing more than once?</summary>

Yes. The shorter subtree can unbalance an ancestor, and fixing it can shorten that subtree in turn, so repair may continue up to the root.

</details>

Check the [worked solutions](../../docs/solutions.md). The [AVL tests](../../src/test/java/io/github/ichillous/dsa/structures/AvlTreeTest.java) include structural validation as well as observable set behavior. The [solution finder](../../docs/solution-finder.md#2-ordered-keys-and-nearest-key-queries) compares this technique with its alternatives.
