---

# ⛏️ Heap Sort in Java

![Java Heap Sort](https://img.shields.io/badge/Java-Heap_Sort-brown?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Introduction to Heap Sort](#-introduction-to-heap-sort)
- [How Heap Sort Works](#-how-heap-sort-works)
- [Algorithm](#-algorithm)
- [Implementation](#-implementation)
- [Time and Space Complexity](#-time-and-space-complexity)
- [Advantages and Disadvantages](#-advantages-and-disadvantages)
- [Applications of Heap Sort](#-applications-of-heap-sort)
- [Common Pitfalls and Best Practices](#-common-pitfalls-and-best-practices)
- [Conclusion](#-conclusion)

## 🌟 Introduction to Heap Sort

**Heap Sort** is a comparison-based sorting algorithm that leverages the heap data structure to sort elements efficiently. Heap Sort works by first building a max heap (for ascending order) from the input data and then repeatedly extracting the maximum element from the heap and rebuilding the heap. This process continues until all elements are sorted.

### Key Characteristics:
- **Heap-Based**: Utilizes a heap data structure, specifically a binary heap.
- **In-Place Sorting**: Requires only a constant amount of additional storage space.
- **Not Stable**: Does not maintain the relative order of equal elements.

## 🔍 How Heap Sort Works

Heap Sort works by first converting the input array into a max heap. Once the max heap is built, the largest element (the root of the heap) is swapped with the last element in the array. The heap size is then reduced by one, and the heap is rebuilt. This process is repeated until the entire array is sorted.

### Visualization:
Consider the following unsorted array: `[4, 10, 3, 5, 1]`.

1. **Build a Max Heap**:
   - Rearrange the array to satisfy the max heap property: `[10, 5, 3, 4, 1]`.
   
2. **Extract Maximum**:
   - Swap the root (maximum element) with the last element: `[1, 5, 3, 4, 10]`.
   - Rebuild the heap: `[5, 4, 3, 1, 10]`.
   
3. **Repeat**:
   - Continue extracting the maximum and rebuilding the heap until the array is fully sorted.

4. **Final Sorted Array**:
   - The array becomes `[1, 3, 4, 5, 10]`.

## 📝 Algorithm

1. **Build a Max Heap**: Convert the input array into a max heap.
2. **Extract Maximum**: Swap the root of the heap with the last element in the array and reduce the heap size.
3. **Heapify**: Rebuild the heap to maintain the max heap property.
4. **Repeat**: Continue the process until the entire array is sorted.

### Heapify Process:
1. Start from the last non-leaf node and move towards the root.
2. For each node, compare it with its children and swap if the heap property is violated.
3. Recursively heapify the affected subtree.

## 💻 Implementation

### Basic Implementation:
```java
public class HeapSort {
    public void heapSort(int[] arr) {
        int n = arr.length;

        // Build max heap
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }

        // One by one extract elements from the heap
        for (int i = n - 1; i >= 0; i--) {
            // Move current root to end
            int temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;

            // Call max heapify on the reduced heap
            heapify(arr, i, 0);
        }
    }

    private void heapify(int[] arr, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        // If left child is larger than root
        if (left < n && arr[left] > arr[largest]) {
            largest = left;
        }

        // If right child is larger than largest so far
        if (right < n && arr[right] > arr[largest]) {
            largest = right;
        }

        // If largest is not root
        if (largest != i) {
            int swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;

            // Recursively heapify the affected sub-tree
            heapify(arr, n, largest);
        }
    }

    public static void main(String[] args) {
        HeapSort sorter = new HeapSort();
        int[] arr = {4, 10, 3, 5, 1};
        sorter.heapSort(arr);
        System.out.println("Sorted array: " + Arrays.toString(arr));
    }
}
```

### Explanation:
- **heapSort**: Builds a max heap from the array and then repeatedly extracts the maximum element, placing it at the end of the array.
- **heapify**: Ensures that the subtree rooted at a given index maintains the heap property.

## ⏱️ Time and Space Complexity

- **Time Complexity**:
  - **Best-Case**: O(n log n) – The time complexity remains consistent across all cases.
  - **Average-Case**: O(n log n) – Similar to the best case, as heap operations are logarithmic.
  - **Worst-Case**: O(n log n) – Heap Sort performs consistently, even in the worst-case scenario.
  
- **Space Complexity**: O(1) – Heap Sort is an in-place sorting algorithm, requiring only a small amount of additional memory.

## ⚖️ Advantages and Disadvantages

### Advantages:
- **Consistent O(n log n)**: Provides O(n log n) time complexity across all cases.
- **In-Place Sorting**: Does not require additional memory beyond the input array.
- **Efficient for Large Datasets**: Heap Sort is well-suited for large datasets where consistent performance is essential.

### Disadvantages:
- **Not Stable**: Heap Sort does not preserve the relative order of equal elements.
- **Overhead of Heap Operations**: While Heap Sort is efficient, the overhead of maintaining the heap structure can make it slower in practice than Quick Sort for smaller datasets.

## 💡 Applications of Heap Sort

Heap Sort is used in scenarios where consistent O(n log n) time complexity is required, and where additional memory usage needs to be minimized:
- **System-Level Sorting**: Suitable for systems where memory usage is a concern, and in-place sorting is required.
- **Selection Algorithms**: Heap Sort can be modified to efficiently find the k-th largest (or smallest) element in an array.
- **Priority Queue Implementation**: The heap structure used in Heap Sort is also foundational for implementing efficient priority queues.

## ⚠️ Common Pitfalls and Best Practices

1. **Avoid Assumptions About Stability**: Heap Sort is not stable, so do not use it when the relative order of equal elements must be preserved.
2. **Consider Overhead**: While Heap Sort is efficient, the overhead of heap operations may not make it the best choice for smaller datasets.
3. **Edge Cases**: Test with edge cases like arrays with repeated elements, already sorted arrays, and arrays with all elements the same.

## 🎓 Conclusion

Heap Sort is a powerful and efficient sorting algorithm that provides consistent O(n log n) performance across all cases. By leveraging the heap data structure, it allows for in-place sorting with minimal additional memory usage, making it an excellent choice for large datasets where memory efficiency is critical.

Key takeaways:
- **Heap-Based Sorting**: Heap Sort utilizes the heap data structure to achieve efficient sorting.
- **In-Place and Consistent**: It is an in-place algorithm with consistent O(n log n) time complexity.
- **Consider Use Cases**: Ideal for large datasets where stability is not a concern, and additional memory usage must be minimized.

By mastering Heap Sort, you'll be equipped to handle efficient sorting tasks in your Java applications, especially in memory-constrained environments! 💻🚀

---
