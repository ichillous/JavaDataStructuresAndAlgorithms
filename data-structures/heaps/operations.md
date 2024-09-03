---

# 🛠️ Heap Operations in Java

![Java Heap Operations](https://img.shields.io/badge/Java-Heap_Operations-blue?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Introduction to Heap Operations](#-introduction-to-heap-operations)
- [Insertion](#-insertion)
  - [Algorithm and Implementation](#-algorithm-and-implementation)
- [Deletion](#-deletion)
  - [Algorithm and Implementation](#-algorithm-and-implementation)
- [Heapify](#-heapify)
  - [Algorithm and Implementation](#-algorithm-and-implementation)
  - [Building a Heap from an Array](#-building-a-heap-from-an-array)
- [Peek](#-peek)
  - [Algorithm and Implementation](#-algorithm-and-implementation)
- [Heap Sort](#-heap-sort)
  - [Algorithm and Implementation](#-algorithm-and-implementation)
- [Common Pitfalls and Best Practices](#-common-pitfalls-and-best-practices)
- [Conclusion](#-conclusion)

## 🌟 Introduction to Heap Operations

Heaps are a crucial data structure in many algorithms, particularly in priority queues and sorting. The primary operations on a heap include **insertion**, **deletion**, **heapify**, **peek**, and **heap sort**. These operations allow the heap to maintain its properties and provide efficient access to the maximum or minimum element.

## ➕ Insertion

### Algorithm and Implementation

Insertion in a heap involves adding the new element at the end of the tree (or array, if using an array representation) to maintain the complete binary tree property. The element is then "bubbled up" to restore the heap property.

#### Steps:
1. Add the new element at the end of the array (or tree).
2. Compare the added element with its parent; if the heap property is violated, swap the elements.
3. Repeat the process until the heap property is restored or the element reaches the root.

#### Example:
```java
import java.util.ArrayList;
import java.util.Collections;

class MinHeap {
    private ArrayList<Integer> heap;

    public MinHeap() {
        heap = new ArrayList<>();
    }

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

    public void printHeap() {
        System.out.println(heap);
    }
}

public class HeapInsertionExample {
    public static void main(String[] args) {
        MinHeap minHeap = new MinHeap();
        minHeap.insert(10);
        minHeap.insert(5);
        minHeap.insert(3);
        minHeap.insert(2);
        minHeap.insert(8);

        minHeap.printHeap(); // Output: [2, 5, 3, 10, 8]
    }
}
```

## ➖ Deletion

### Algorithm and Implementation

Deletion, typically the removal of the root element, involves replacing the root with the last element in the heap and then "bubbling down" this element to restore the heap property.

#### Steps:
1. Replace the root element with the last element in the array.
2. Remove the last element.
3. Compare the new root with its children; if the heap property is violated, swap it with the smaller (or larger, in a max heap) child.
4. Repeat the process until the heap property is restored or the element becomes a leaf.

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

## 🔄 Heapify

### Algorithm and Implementation

Heapify is the process of converting a binary tree into a heap by ensuring that all subtrees satisfy the heap property. This operation is essential when building a heap from an unsorted array.

#### Steps:
1. Start from the last non-leaf node and move towards the root.
2. For each node, compare it with its children and swap if the heap property is violated.
3. Recursively heapify the affected subtree.

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

### Building a Heap from an Array

To build a heap from an unsorted array, start heapifying from the last non-leaf node up to the root.

#### Example:
```java
public void buildHeap(int[] array) {
    for (int i = (array.length / 2) - 1; i >= 0; i--) {
        heapify(array, array.length, i);
    }
}

private void heapify(int[] array, int size, int index) {
    int largest = index;
    int leftChild = 2 * index + 1;
    int rightChild = 2 * index + 2;

    if (leftChild < size && array[leftChild] > array[largest]) {
        largest = leftChild;
    }

    if (rightChild < size && array[rightChild] > array[largest]) {
        largest = rightChild;
    }

    if (largest != index) {
        int swap = array[index];
        array[index] = array[largest];
        array[largest] = swap;

        heapify(array, size, largest);
    }
}
```

## 👀 Peek

### Algorithm and Implementation

The `peek` operation retrieves the root element of the heap without removing it. In a min heap, this is the smallest element, and in a max heap, this is the largest element.

#### Example:
```java
public int peek() {
    if (heap.isEmpty()) {
        throw new NoSuchElementException("Heap is empty");
    }
    return heap.get(0);
}
```

## 🔄 Heap Sort

### Algorithm and Implementation

**Heap Sort** is a comparison-based sorting algorithm that uses a heap data structure. The algorithm involves building a max heap (for ascending order) from the array, then repeatedly removing the largest element from the heap and placing it at the end of the array.

#### Steps:
1. Build a max heap from the unsorted array.
2. Swap the root of the heap with the last element in the array.
3. Reduce the heap size and heapify the root element.
4. Repeat the process until the array is sorted.

#### Example:
```java
public void heapSort(int[] array) {
    int n = array.length;

    // Build max heap
    for (int i = n / 2 - 1; i >= 0; i--) {
        heapify(array, n, i);
    }

    // One by one extract elements
    for (int i = n - 1; i >= 0; i--) {
        // Move current root to end
        int temp = array[0];
        array[0] = array[i];
        array[i] = temp;

        // Call max heapify on the reduced heap
        heapify(array, i, 0);
    }
}

private void heapify(int[] array, int size, int index) {
    int largest = index;
    int leftChild = 2 * index + 1;
    int rightChild = 2 * index + 2;

    if (leftChild < size && array[leftChild] > array[largest]) {
        largest = leftChild;
    }

    if (rightChild < size && array[rightChild] > array[largest]) {
        largest = rightChild;
    }

    if (largest != index) {
        int swap = array[index];
        array[index] = array[largest];
        array[largest] = swap;

        heapify(array, size, largest);
    }
}
```

## ⚠️ Common Pitfalls and Best Practices

1. **Maintain the Heap Property**: Ensure that all operations (insertion, deletion, heapify) maintain

 the heap property.
2. **Handle Edge Cases**: Consider edge cases like empty heaps and single-element heaps.
3. **Choose the Right Heap Type**: Use min heaps for priority queues where the smallest element is prioritized, and max heaps where the largest element is.
4. **Optimize Heapify**: When building a heap, use a bottom-up approach for efficient heapification.

## 🎓 Conclusion

Heap operations are fundamental to many algorithms and data structures, particularly in priority queues and sorting. By mastering heap operations like insertion, deletion, heapify, and heap sort, you'll be well-equipped to implement efficient algorithms in Java.

Key takeaways:
- **Insertion**: Add elements while maintaining the heap property.
- **Deletion**: Remove the root element and restore the heap property.
- **Heapify**: Convert a binary tree into a heap efficiently.
- **Heap Sort**: Sort an array using the heap data structure.

By mastering these operations, you'll enhance your ability to work with heaps in Java, enabling you to tackle a wide range of algorithmic challenges! 💻🚀

---
