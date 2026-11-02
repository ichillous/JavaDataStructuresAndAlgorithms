# Trees: choose the invariant before the shape

A tree is a connected acyclic graph; a rooted tree gives every non-root node one parent. Trees organize hierarchical data, but different tree structures optimize different operations. The word “tree” alone does not imply logarithmic search.

## Compare the maintained structures

| Structure | Defining invariant | Useful operation |
| --- | --- | --- |
| [`BinarySearchTree`](../../src/main/java/io/github/ichillous/dsa/structures/BinarySearchTree.java) | All left descendants compare smaller; all right descendants compare larger | Ordered membership and sorted traversal |
| [`AvlTree`](../../src/main/java/io/github/ichillous/dsa/structures/AvlTree.java) | BST order plus subtree heights differing by at most one | O(log n) worst-case membership/update |
| [`BinaryMinHeap`](../../src/main/java/io/github/ichillous/dsa/structures/BinaryMinHeap.java) | Complete shape; parent no larger than children | Minimum-priority access |
| [`Trie`](../../src/main/java/io/github/ichillous/dsa/structures/Trie.java) | Edge labels spell shared string prefixes | Prefix lookup and suggestions |
| [`SegmentTree`](../../src/main/java/io/github/ichillous/dsa/structures/SegmentTree.java) | Each node stores the minimum of its covered interval | Range minimum with point replacement |

BST and AVL types behave as sets: `compareTo` defines equivalent keys, duplicate add returns false, and nulls are rejected. Heap duplicates are meaningful and retained. Trie words use their own whole-word marker; a word may end at an internal node.

## Height is measured in edges here

```text
       4          root: depth 0
      / \
     2   6        leaves: depth 1
```

A leaf has height 0 and an empty BST/AVL has height -1. Depth measures distance from the root; height measures the longest downward path. Mixing the conventions for empty height 0 versus -1 causes off-by-one mistakes in balancing code.

```java
import io.github.ichillous.dsa.structures.BinarySearchTree;
import java.util.List;

BinarySearchTree<Integer> tree = new BinarySearchTree<>();
assert tree.height() == -1;
tree.add(4);
tree.add(2);
tree.add(6);
assert tree.height() == 1;
assert tree.inOrder().equals(List.of(2, 4, 6));
```

## An interval tree has a different ordering

The maintained range-minimum segment tree pads its leaf layer to a power of two with Long.MAX_VALUE, then divides the padded positions into adjacent intervals. Padding is the identity for minimum, so it cannot introduce a smaller answer. Its parent stores `min(left, right)`, not a key separating smaller and larger values. Replacing a point only changes nodes covering that position.

```java
import io.github.ichillous.dsa.structures.SegmentTree;

SegmentTree minima = new SegmentTree(new long[] {8, 3, 7, 5});
assert minima.rangeMin(1, 4).orElseThrow() == 3;
minima.set(1, 9);
assert minima.rangeMin(1, 4).orElseThrow() == 5;
assert minima.rangeMin(2, 2).isEmpty();
```

Segment-tree build/storage is O(n), and point-set/range-min is O(log n). Half-open empty ranges return `OptionalLong.empty()` so a real minimum of `Long.MAX_VALUE` remains representable. Unlike a Fenwick sum, minimum cannot be recovered by subtracting two prefix minima.

## Practice

1. Choose a structure for sorted unique keys, repeated minimum removal, autocomplete and mutable range minimum. Explain the invariant each choice needs.
2. Draw a five-key BST with height 4, then a balanced one with the same inorder result.
3. Compare random segment-tree updates/queries with a plain array scan, including empty ranges and extreme long values.

Continue with [binary-tree terminology](binary-trees.md), [BST operations](binary-search-trees.md), [AVL balance](avl-trees.md) and the [worked solutions](../../docs/solutions.md).
