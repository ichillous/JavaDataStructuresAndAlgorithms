---

# ⛏️ Heaps in Java

![Java Heaps](https://img.shields.io/badge/Java-Heaps-red?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Introduction to Heaps](#-introduction-to-heaps)
- [Heap Terminology](#-heap-terminology)
- [Types of Heaps](#-types-of-heaps)
  - [Min Heap](#-min-heap)
  - [Max Heap](#-max-heap)
- [Heap Properties](#-heap-properties)
- [Basic Operations on Heaps](#-basic-operations-on-heaps)
  - [Insertion](#-insertion)
  - [Deletion](#-deletion)
  - [Heapify](#-heapify)
  - [Peek](#-peek)
- [Heap Applications](#-heap-applications)
- [Advantages and Disadvantages](#-advantages-and-disadvantages)
- [Common Pitfalls and Best Practices](#-common-pitfalls-and-best-practices)
- [Conclusion](#-conclusion)

## 🌟 Introduction to Heaps

A **heap** is a specialized tree-based data structure that satisfies the heap property. In a heap, the parent node is either greater than or equal to (in a max heap) or less than or equal to (in a min heap) its child nodes. Heaps are commonly used in priority queues and are an essential component of various algorithms, including heapsort.

### Key Characteristics:
- **Complete Binary Tree**: Heaps are typically implemented as complete binary trees, where all levels are fully filled except possibly the last level, which is filled from left to right.
- **Heap Property**: Maintains a specific order where the root is the minimum (min heap) or maximum (max heap) element.

## 📚 Heap Terminology

- **Root**: The topmost node in a heap.
- **Parent Node**: A node that has one or more child nodes.
- **Child Node**: A node that descends from a parent node.
- **Leaf Node**: A node with no children.
- **Heapify**: The process of rearranging the elements to maintain the heap property.

## 🌲 Types of Heaps

### 1. Min Heap

In a **Min Heap**, the root node is the smallest element, and every parent node is smaller than or equal to its child nodes. The smallest element is always at the root.

#### Example:
```
       1
     /   \
    3     2
   / \   / \
  7   6 4   5
```

### 2. Max Heap

In a **Max Heap**, the root node is the largest element, and every parent node is larger than or equal to its child nodes. The largest element is always at the root.

#### Example:
```
       9
     /   \
    5     8
   / \   / \
  2   3 7   4
```

## 🔧 Heap Properties

- **Complete Binary Tree**: Heaps are complete binary trees, meaning all levels are fully filled except possibly the last level, which is filled from left to right.
- **Heap Order Property**: For a min heap, the value of each node is greater than or equal to the value of its parent node. For a max heap, the value of each node is less than or equal to the value of its parent node.

## 🛠️ Basic Operations on Heaps

### ➕ Insertion

Insertion in a heap involves adding the new element at the end of the tree (to maintain the complete binary tree property) and then "bubbling up" the element to restore the heap property.

#### Example:
```java
public void insert(int value) {
    heap.add(value); // Add the new element at the end
    int index = heap.size() - 1;
    int parent = (index - 1) / 2;

    // Bubble up the new element to restore the heap property
    while (index > 0 && heap.get(index) < heap.get(parent)) {
        Collections.swap(heap, index, parent);
        index = parent;
        parent = (index - 1) / 2;
    }
}
```

### ➖ Deletion

Deletion (typically removing the root element) involves replacing the root with the last element in the heap and then "bubbling down" this element to restore the heap property.

#### Example:
```java
public int delete() {
    if (heap.isEmpty()) {
        throw new NoSuchElementException("Heap is empty");
    }

    int root = heap.get(0);
    int lastElement = heap.remove(heap.size() - 1);

    if (!heap.isEmpty()) {
        heap.set(0, lastElement);
        heapifyDown(0);
    }

    return root;
}

private void heapifyDown(int index) {
    int leftChild = 2 * index + 1;
    int rightChild = 2 * index + 2;
    int smallest = index;

    if (leftChild < heap.size() && heap.get(leftChild) < heap.get(smallest)) {
        smallest = leftChild;
    }

    if (rightChild < heap.size() && heap.get(rightChild) < heap.get(smallest)) {
        smallest = rightChild;
    }

    if (smallest != index) {
        Collections.swap(heap, index, smallest);
        heapifyDown(smallest);
    }
}
```

### 🔄 Heapify

**Heapify** is the process of converting a binary tree into a heap by ensuring that all subtrees satisfy the heap property. This operation is often used when building a heap from an unsorted array.

#### Example:
```java
public void heapify(int index) {
    int leftChild = 2 * index + 1;
    int rightChild = 2 * index + 2;
    int smallest = index;

    if (leftChild < heap.size() && heap.get(leftChild) < heap.get(smallest)) {
        smallest = leftChild;
    }

    if (rightChild < heap.size() && heap.get(rightChild) < heap.get(smallest)) {
        smallest = rightChild;
    }

    if (smallest != index) {
        Collections.swap(heap, index, smallest);
        heapify(smallest);
    }
}
```

### 👀 Peek

**Peek** returns the root element of the heap without removing it. For a min heap, this is the minimum element, and for a max heap, this is the maximum element.

#### Example:
```java
public int peek() {
    if (heap.isEmpty()) {
        throw new NoSuchElementException("Heap is empty");
    }
    return heap.get(0);
}
```

## 💡 Heap Applications

Heaps are widely used in various applications, including:
- **Priority Queues**: Heaps are the foundation of priority queues, where elements are processed based on their priority.
- **Heapsort**: A comparison-based sorting algorithm that uses a heap to sort elements in O(n log n) time.
- **Graph Algorithms**: Dijkstra's and Prim's algorithms use heaps to efficiently find the shortest path and minimum spanning tree, respectively.
- **Median Finding**: Heaps can be used to maintain the median of a dynamically updating dataset.

## ⚖️ Advantages and Disadvantages

### Advantages:
- **Efficient Operations**: Heaps provide O(log n) time complexity for insertion, deletion, and peek operations.
- **Versatility**: Useful in various algorithms, particularly those involving priority queues and sorting.
- **Memory Efficiency**: Heaps can be implemented using arrays, minimizing memory overhead.

### Disadvantages:
- **Limited Operations**: Heaps are not designed for efficient searching, and finding non-root elements can be costly.
- **Complexity**: Maintaining the heap property can be complex, particularly during insertion and deletion.
- **Not Suitable for All Applications**: Heaps are less efficient for certain applications, such as those requiring frequent random access or updates.

## ⚠️ Common Pitfalls and Best Practices

1. **Maintaining Heap Property**: Ensure that all operations (insertion, deletion, heapify) maintain the heap property to avoid incorrect results.
2. **Choosing the Right Type**: Use min heaps for priority queues where the smallest element is prioritized, and max heaps where the largest is.
3. **Edge Cases**: Handle edge cases such as inserting into or deleting from an empty heap.
4. **Efficient Heapify**: When building a heap from an unsorted array, use a bottom-up approach to efficiently heapify the array.

## 🎓 Conclusion

Heaps are a powerful and efficient data structure in Java, providing the foundation for priority queues and several key algorithms. Understanding heap properties, operations, and their applications will help you implement and optimize heap-based solutions in your Java applications.

Key takeaways:
- **Heap Property**: Maintain the min or max heap property during all operations.
- **Efficient Operations**: Heaps provide O(log n) time complexity for key operations like insertion and deletion.
- **Versatile Applications**: Heaps are essential in priority queues, sorting algorithms, and graph algorithms.

By mastering heaps, you'll be well-equipped to handle a wide range of data structures and algorithms in your Java applications! 💻🚀

---
