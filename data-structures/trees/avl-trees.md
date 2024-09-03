---

# ⚖️ AVL Trees in Java

![Java AVL Trees](https://img.shields.io/badge/Java-AVL_Trees-blue?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Introduction to AVL Trees](#-introduction-to-avl-trees)
- [Structure of an AVL Tree](#-structure-of-an-avl-tree)
- [Properties of AVL Trees](#-properties-of-avl-trees)
- [Balancing the Tree](#-balancing-the-tree)
  - [Rotations](#-rotations)
    - [Right Rotation (Single Rotation)](#-right-rotation-single-rotation)
    - [Left Rotation (Single Rotation)](#-left-rotation-single-rotation)
    - [Left-Right Rotation (Double Rotation)](#-left-right-rotation-double-rotation)
    - [Right-Left Rotation (Double Rotation)](#-right-left-rotation-double-rotation)
- [Basic Operations on AVL Trees](#-basic-operations-on-avl-trees)
  - [Insertion](#-insertion)
  - [Deletion](#-deletion)
  - [Search](#-search)
- [Traversal of AVL Trees](#-traversal-of-avl-trees)
- [Applications of AVL Trees](#-applications-of-avl-trees)
- [Advantages and Disadvantages](#-advantages-and-disadvantages)
- [Common Pitfalls and Best Practices](#-common-pitfalls-and-best-practices)
- [Conclusion](#-conclusion)

## 🌟 Introduction to AVL Trees

An **AVL Tree** is a self-balancing binary search tree where the height difference between the left and right subtrees (balance factor) of any node is at most one. Named after its inventors, Adelson-Velsky and Landis, AVL trees maintain their balance through rotations, ensuring O(log n) time complexity for insertion, deletion, and search operations.

### Key Characteristics:
- **Self-Balancing**: Automatically balances itself during insertions and deletions.
- **Efficient Search**: Guarantees O(log n) search time by maintaining balance.
- **Rotations**: Uses rotations to maintain balance after operations that could unbalance the tree.

## 🛠️ Structure of an AVL Tree

An AVL Tree is a type of binary search tree with an additional property that ensures balance. Each node contains:
1. **Data**: The value stored in the node.
2. **Left Child**: A reference to the left child node.
3. **Right Child**: A reference to the right child node.
4. **Height**: The height of the node, used to maintain balance.

### Basic Node Structure:
```java
class AVLTreeNode {
    int value;
    AVLTreeNode leftChild;
    AVLTreeNode rightChild;
    int height;

    AVLTreeNode(int value) {
        this.value = value;
        leftChild = null;
        rightChild = null;
        height = 1; // New node is initially added at leaf
    }
}
```

## 🔑 Properties of AVL Trees

- **Balance Factor**: For any node, the balance factor (height difference between left and right subtrees) must be -1, 0, or 1.
- **Height**: The height of an AVL tree with `n` nodes is O(log n), ensuring efficient operations.
- **Rotations**: AVL trees use rotations (single and double) to maintain balance after insertions and deletions.

## ⚖️ Balancing the Tree

### Rotations

When the balance factor of a node becomes unbalanced (i.e., less than -1 or greater than 1), rotations are performed to restore balance.

#### 1. Right Rotation (Single Rotation)

A **right rotation** is performed when the left subtree is taller than the right subtree by more than one level, and the left subtree itself is balanced or left-heavy.

```java
private AVLTreeNode rightRotate(AVLTreeNode y) {
    AVLTreeNode x = y.leftChild;
    AVLTreeNode T2 = x.rightChild;

    // Perform rotation
    x.rightChild = y;
    y.leftChild = T2;

    // Update heights
    y.height = Math.max(height(y.leftChild), height(y.rightChild)) + 1;
    x.height = Math.max(height(x.leftChild), height(x.rightChild)) + 1;

    // Return new root
    return x;
}
```

#### 2. Left Rotation (Single Rotation)

A **left rotation** is performed when the right subtree is taller than the left subtree by more than one level, and the right subtree itself is balanced or right-heavy.

```java
private AVLTreeNode leftRotate(AVLTreeNode x) {
    AVLTreeNode y = x.rightChild;
    AVLTreeNode T2 = y.leftChild;

    // Perform rotation
    y.leftChild = x;
    x.rightChild = T2;

    // Update heights
    x.height = Math.max(height(x.leftChild), height(x.rightChild)) + 1;
    y.height = Math.max(height(y.leftChild), height(y.rightChild)) + 1;

    // Return new root
    return y;
}
```

#### 3. Left-Right Rotation (Double Rotation)

A **left-right rotation** is needed when the left subtree is taller, but the left child is right-heavy. This rotation is a combination of a left rotation on the left child followed by a right rotation on the root.

```java
private AVLTreeNode leftRightRotate(AVLTreeNode z) {
    z.leftChild = leftRotate(z.leftChild);
    return rightRotate(z);
}
```

#### 4. Right-Left Rotation (Double Rotation)

A **right-left rotation** is needed when the right subtree is taller, but the right child is left-heavy. This rotation is a combination of a right rotation on the right child followed by a left rotation on the root.

```java
private AVLTreeNode rightLeftRotate(AVLTreeNode z) {
    z.rightChild = rightRotate(z.rightChild);
    return leftRotate(z);
}
```

## 🛠️ Basic Operations on AVL Trees

### ➕ Insertion

Insertion in an AVL tree is similar to insertion in a binary search tree, followed by rebalancing the tree if necessary.

#### Example:
```java
public AVLTreeNode insert(AVLTreeNode node, int value) {
    // Perform the normal BST insertion
    if (node == null) {
        return new AVLTreeNode(value);
    }

    if (value < node.value) {
        node.leftChild = insert(node.leftChild, value);
    } else if (value > node.value) {
        node.rightChild = insert(node.rightChild, value);
    } else {
        return node; // Duplicate values not allowed
    }

    // Update the height of the ancestor node
    node.height = 1 + Math.max(height(node.leftChild), height(node.rightChild));

    // Get the balance factor to check whether this node became unbalanced
    int balance = getBalance(node);

    // Left Left Case
    if (balance > 1 && value < node.leftChild.value) {
        return rightRotate(node);
    }

    // Right Right Case
    if (balance < -1 && value > node.rightChild.value) {
        return leftRotate(node);
    }

    // Left Right Case
    if (balance > 1 && value > node.leftChild.value) {
        return leftRightRotate(node);
    }

    // Right Left Case
    if (balance < -1 && value < node.rightChild.value) {
        return rightLeftRotate(node);
    }

    return node; // Return the unchanged node pointer
}
```

### ➖ Deletion

Deletion in an AVL tree involves removing the node and then rebalancing the tree if necessary.

#### Example:
```java
public AVLTreeNode delete(AVLTreeNode root, int value) {
    // Perform the normal BST deletion
    if (root == null) {
        return root;
    }

    if (value < root.value) {
        root.leftChild = delete(root.leftChild, value);
    } else if (value > root.value) {
        root.rightChild = delete(root.rightChild, value);
    } else {
        // Node with only one child or no child
        if ((root.leftChild == null) || (root.rightChild == null)) {
            AVLTreeNode temp = root.leftChild != null ? root.leftChild : root.rightChild;

            if (temp == null) {
                root = null;
            } else {
                root = temp;
            }
        } else {
            AVLTreeNode temp = findMinValueNode(root.rightChild);
            root.value = temp.value;
            root.rightChild = delete(root.rightChild, temp.value);
        }
    }

    if (root == null) {
        return root;
    }

    // Update the height of the current node
    root.height = Math.max(height(root.leftChild), height(root.rightChild)) + 1;

    // Get the balance factor to check whether this node became unbalanced
    int balance = getBalance(root);

    // Left Left Case
    if (balance > 1 && getBalance(root.leftChild) >= 0) {
        return rightRotate(root);
    }

    // Left Right Case
    if (balance > 1 && getBalance(root.leftChild) < 0) {
        return leftRightRotate(root);
    }

    // Right Right Case
    if (balance < -1 && getBalance(root.rightChild) <= 0) {
        return leftRotate(root);
    }

    //

 Right Left Case
    if (balance < -1 && getBalance(root.rightChild) > 0) {
        return rightLeftRotate(root);
    }

    return root;
}

private AVLTreeNode findMinValueNode(AVLTreeNode node) {
    AVLTreeNode current = node;
    while (current.leftChild != null) {
        current = current.leftChild;
    }
    return current;
}
```

### 🔍 Search

Searching in an AVL tree is the same as in a binary search tree, taking advantage of the ordered structure to efficiently find elements.

#### Example:
```java
public boolean search(AVLTreeNode root, int value) {
    if (root == null) {
        return false;
    }

    if (value < root.value) {
        return search(root.leftChild, value);
    } else if (value > root.value) {
        return search(root.rightChild, value);
    } else {
        return true; // Value found
    }
}
```

## 🔄 Traversal of AVL Trees

Traversal in an AVL tree follows the same methods as in a binary search tree, including inorder, preorder, postorder, and level order traversal.

### A. Inorder Traversal
```java
void inorderTraversal(AVLTreeNode node) {
    if (node != null) {
        inorderTraversal(node.leftChild);
        System.out.print(node.value + " ");
        inorderTraversal(node.rightChild);
    }
}
```

### B. Preorder Traversal
```java
void preorderTraversal(AVLTreeNode node) {
    if (node != null) {
        System.out.print(node.value + " ");
        preorderTraversal(node.leftChild);
        preorderTraversal(node.rightChild);
    }
}
```

### C. Postorder Traversal
```java
void postorderTraversal(AVLTreeNode node) {
    if (node != null) {
        postorderTraversal(node.leftChild);
        postorderTraversal(node.rightChild);
        System.out.print(node.value + " ");
    }
}
```

### D. Level Order Traversal
```java
void levelOrderTraversal(AVLTreeNode root) {
    if (root == null) return;

    Queue<AVLTreeNode> queue = new LinkedList<>();
    queue.add(root);

    while (!queue.isEmpty()) {
        AVLTreeNode node = queue.poll();
        System.out.print(node.value + " ");

        if (node.leftChild != null) queue.add(node.leftChild);
        if (node.rightChild != null) queue.add(node.rightChild);
    }
}
```

## 💡 Applications of AVL Trees

AVL trees are used in applications where self-balancing is crucial for maintaining efficient operations, such as:
- **Databases**: Indexing large datasets where quick lookups and updates are needed.
- **File Systems**: Managing file directories for quick access.
- **Memory Management**: Managing memory allocation where balancing ensures efficient space utilization.
- **Networking**: Routing tables in network devices for efficient data transmission.

## ⚖️ Advantages and Disadvantages

### Advantages:
- **Self-Balancing**: Maintains balance automatically, ensuring O(log n) operations.
- **Efficient Operations**: Provides efficient insertion, deletion, and search operations.
- **Guaranteed Performance**: Ensures worst-case performance of O(log n) for all basic operations.

### Disadvantages:
- **Complexity**: Rotations and balancing logic make AVL trees more complex to implement compared to simple BSTs.
- **Higher Insertion/Deletion Cost**: The need for rebalancing increases the cost of insertion and deletion operations compared to unbalanced BSTs.
- **Memory Overhead**: Requires additional memory for storing the height of each node.

## ⚠️ Common Pitfalls and Best Practices

1. **Correct Rotation Handling**: Ensure rotations are correctly implemented to maintain the balance of the tree.
2. **Edge Case Management**: Consider edge cases, such as deletions that require multiple rotations, to maintain tree balance.
3. **Memory Management**: Be aware of the memory overhead associated with storing node heights.
4. **Testing**: Thoroughly test AVL tree implementations with different scenarios to ensure correct balancing and performance.

## 🎓 Conclusion

AVL Trees are powerful self-balancing binary search trees that ensure efficient operations through automatic balancing. Understanding the structure, operations, and balancing techniques of AVL trees is crucial for implementing efficient data structures in applications requiring dynamic data management.

Key takeaways:
- **Self-Balancing**: AVL trees maintain balance automatically through rotations.
- **Efficient Operations**: Provide consistent O(log n) time complexity for search, insertion, and deletion.
- **Rotation Techniques**: Mastering rotation techniques is key to maintaining balance in AVL trees.

By mastering AVL Trees, you'll be equipped to handle complex data structures and ensure efficient performance in your Java applications! 💻🚀

---
