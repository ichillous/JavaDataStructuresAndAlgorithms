---

# ⚡ Quick Sort in Java

![Java Quick Sort](https://img.shields.io/badge/Java-Quick_Sort-red?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Introduction to Quick Sort](#-introduction-to-quick-sort)
- [How Quick Sort Works](#-how-quick-sort-works)
- [Algorithm](#-algorithm)
- [Implementation](#-implementation)
- [Time and Space Complexity](#-time-and-space-complexity)
- [Advantages and Disadvantages](#-advantages-and-disadvantages)
- [Applications of Quick Sort](#-applications-of-quick-sort)
- [Common Pitfalls and Best Practices](#-common-pitfalls-and-best-practices)
- [Conclusion](#-conclusion)

## 🌟 Introduction to Quick Sort

**Quick Sort** is a highly efficient and widely used sorting algorithm that employs the divide-and-conquer strategy. It works by selecting a "pivot" element from the array and partitioning the other elements into two subarrays, according to whether they are less than or greater than the pivot. The subarrays are then recursively sorted. Quick Sort is often faster in practice than other O(n log n) algorithms like Merge Sort due to its in-place sorting and reduced overhead.

### Key Characteristics:
- **Divide and Conquer**: Quick Sort recursively divides the array around a pivot element.
- **In-Place Sorting**: Requires only a small, constant amount of additional storage.
- **Unstable**: Does not preserve the relative order of equal elements.

## 🔍 How Quick Sort Works

Quick Sort works by selecting a "pivot" element from the array and partitioning the other elements into two subarrays based on whether they are less than or greater than the pivot. The pivot is then placed in its correct position, and the process is recursively applied to the subarrays.

### Visualization:
Consider the following unsorted array: `[10, 7, 8, 9, 1, 5]`.

1. **Select Pivot**:
   - Choose the last element as the pivot: `5`.
   
2. **Partition**:
   - Rearrange the array so that elements less than `5` are on the left, and elements greater than `5` are on the right: `[1, 5, 8, 9, 10, 7]`.
   - `5` is now in its correct position.

3. **Recursion**:
   - Apply the same process to the subarrays `[1]` (left of `5`) and `[8, 9, 10, 7]` (right of `5`).

4. **Final Sorted Array**:
   - After recursively applying the steps, the array becomes `[1, 5, 7, 8, 9, 10]`.

## 📝 Algorithm

1. **Choose a Pivot**: Select an element as the pivot (commonly the last element).
2. **Partition**: Rearrange the array so that elements less than the pivot are on its left, and elements greater than the pivot are on its right.
3. **Recursion**: Recursively apply Quick Sort to the subarrays formed by dividing around the pivot.
4. **Base Case**: The recursion ends when the subarray has zero or one element, as it is already sorted.

### Partitioning Process:
1. Select a pivot element.
2. Move elements less than the pivot to its left.
3. Move elements greater than the pivot to its right.
4. Swap the pivot element to its correct position.

## 💻 Implementation

### Basic Implementation:
```java
public class QuickSort {
    public void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);

            // Recursively sort elements before partition and after partition
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = (low - 1); // Index of smaller element

        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                // Swap arr[i] and arr[j]
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }

        // Swap arr[i + 1] and arr[high] (or pivot)
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1;
    }

    public static void main(String[] args) {
        QuickSort sorter = new QuickSort();
        int[] arr = {10, 7, 8, 9, 1, 5};
        sorter.quickSort(arr, 0, arr.length - 1);
        System.out.println("Sorted array: " + Arrays.toString(arr));
    }
}
```

### Explanation:
- **quickSort**: Recursively sorts the array by dividing it around the pivot.
- **partition**: Rearranges the array around the pivot so that elements less than the pivot come before it, and elements greater than the pivot come after it.

## ⏱️ Time and Space Complexity

- **Time Complexity**:
  - **Best-Case**: O(n log n) – Occurs when the pivot divides the array into two nearly equal subarrays.
  - **Average-Case**: O(n log n) – Typically occurs with random pivot selections.
  - **Worst-Case**: O(n²) – Occurs when the smallest or largest element is always chosen as the pivot, resulting in highly unbalanced partitions.
  
- **Space Complexity**: O(log n) – In-place sorting algorithm, with the extra space mainly used for recursion stack.

## ⚖️ Advantages and Disadvantages

### Advantages:
- **Efficient**: Often faster than Merge Sort and Heap Sort in practice due to in-place sorting and reduced memory overhead.
- **In-Place Sorting**: Requires only a small amount of additional memory.
- **Average O(n log n)**: Provides good performance on average, making it suitable for most datasets.

### Disadvantages:
- **Unstable**: Does not maintain the relative order of equal elements.
- **Worst-Case O(n²)**: Can degrade to O(n²) time complexity if poor pivot choices are made.
- **Recursive Algorithm**: Requires careful handling of stack depth for large arrays.

## 💡 Applications of Quick Sort

Quick Sort is widely used in various applications due to its efficiency:
- **General-Purpose Sorting**: Suitable for most datasets, especially when average-case performance is acceptable.
- **Database Systems**: Often used in database query processing for sorting records.
- **Efficient Sorting**: Preferred in systems with limited memory, as it is an in-place sorting algorithm.

## ⚠️ Common Pitfalls and Best Practices

1. **Pivot Selection**: Use strategies like choosing the median of three elements or randomizing the pivot to avoid worst-case scenarios.
2. **Avoid Deep Recursion**: For large arrays, ensure that the recursion depth is managed, possibly using an iterative version of Quick Sort or a hybrid approach with Insertion Sort.
3. **Handle Edge Cases**: Test with edge cases like arrays with repeated elements, already sorted arrays, and arrays with all elements the same.

## 🎓 Conclusion

Quick Sort is a powerful and versatile sorting algorithm that offers excellent average-case performance and is widely used in practice. By understanding how Quick Sort works and how to optimize it, you can implement an efficient sorting solution for a wide range of applications in Java.

Key takeaways:
- **Divide and Conquer**: Quick Sort divides the array around a pivot element, making it efficient for most datasets.
- **In-Place and Efficient**: Requires minimal additional memory and is often faster than other O(n log n) algorithms.
- **Optimize for Worst-Case**: By carefully selecting the pivot and managing recursion, Quick Sort can be optimized to avoid its worst-case behavior.

By mastering Quick Sort, you'll be equipped to handle a wide range of sorting tasks in your Java applications! 💻🚀

---
