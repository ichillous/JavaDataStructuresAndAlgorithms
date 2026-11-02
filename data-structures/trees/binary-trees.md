# Binary trees: shape and traversal are separate choices

A binary tree allows at most two children per node, conventionally called left and right. This describes branching, not key ordering, completeness or balance. A binary search tree adds an ordering rule; a heap adds a different rule.

```text
       A
      / \
     B   C
      \
       D

preorder:   A B D C     root, left, right
inorder:    B D A C     left, root, right
postorder:  D B C A     left, right, root
level order:A B C D     breadth first
```

Preorder can serialize shape when absent children are represented. Postorder processes children before parents, useful for deleting subtrees or evaluating expression trees. Level order uses a FIFO queue. Inorder is sorted **only if the BST ordering invariant holds**.

## Name the shapes precisely

| Term | Meaning |
| --- | --- |
| Full / proper | Every node has zero or two children |
| Complete | Every level except possibly the last is filled; the last fills left to right |
| Perfect | Every internal node has two children and all leaves have equal depth |
| Height balanced | A specified local height condition holds, such as AVL's difference of at most one |

A complete heap is not necessarily a BST. A full tree is not necessarily balanced: one branch can keep splitting while the other ends at a leaf.

The library exposes ordered trees rather than a general mutable binary-node API. This runnable example uses that stronger contract explicitly:

```java
import io.github.ichillous.dsa.structures.BinarySearchTree;
import java.util.List;

BinarySearchTree<Integer> ordered = new BinarySearchTree<>();
for (int value : new int[] {4, 2, 6, 3}) ordered.add(value);
assert ordered.inOrder().equals(List.of(2, 3, 4, 6));
assert ordered.height() == 2;
```

## Costs and a counterexample

A full traversal visits n nodes and costs O(n), even when height is O(log n). Recursive depth-first traversal uses O(h) call frames; a chain can have h = n - 1. The maintained [BST traversal](../../src/main/java/io/github/ichillous/dsa/structures/BinarySearchTree.java) is iterative, so a degenerate tree does not consume one JVM call frame per node. Its returned snapshot itself requires O(n) space.

A binary tree with root 4 and left child 9 is valid as a binary tree but invalid as a BST. Applying “go left for smaller values” to an arbitrary binary tree can miss a value that is present.

## Practice

1. Write all four traversals for the diagram, then move D to C's left and repeat.
2. Find the smallest tree that is full but not complete.
3. Explain what extra markers a preorder serialization needs to distinguish a left-only child from a right-only child.

Read [BST ordering](binary-search-trees.md) and the [worked solutions](../../docs/solutions.md).
