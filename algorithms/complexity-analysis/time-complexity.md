---

# ⏳ Time Complexity in Java

![Java Time Complexity](https://img.shields.io/badge/Java-Time_Complexity-red?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Introduction to Time Complexity](#-introduction-to-time-complexity)
- [Big O Notation](#-big-o-notation)
- [Common Time Complexities](#-common-time-complexities)
  - [Constant Time O(1)](#-constant-time-o1)
  - [Logarithmic Time O(log n)](#-logarithmic-time-olog-n)
  - [Linear Time O(n)](#-linear-time-on)
  - [Linearithmic Time O(n log n)](#-linearithmic-time-on-log-n)
  - [Quadratic Time O(n^2)](#-quadratic-time-on2)
  - [Cubic Time O(n^3)](#-cubic-time-on3)
  - [Exponential Time O(2^n)](#-exponential-time-o2n)
  - [Factorial Time O(n!)](#-factorial-time-on)
- [How to Analyze Time Complexity](#-how-to-analyze-time-complexity)
  - [Counting Operations](#-counting-operations)
  - [Nested Loops](#-nested-loops)
  - [Recursion](#-recursion)
- [Best, Worst, and Average Case Analysis](#-best-worst-and-average-case-analysis)
- [Amortized Time Complexity](#-amortized-time-complexity)
- [Common Pitfalls and Best Practices](#-common-pitfalls-and-best-practices)
- [Conclusion](#-conclusion)

## 🌟 Introduction to Time Complexity

**Time Complexity** is a way of measuring the efficiency of an algorithm in terms of the time it takes to run as a function of the input size. Understanding time complexity allows developers to predict how an algorithm will perform as the size of the input grows. It’s essential for optimizing code and ensuring scalability.

### Why Time Complexity Matters:
- Helps determine if an algorithm is efficient for large input sizes.
- Predicts performance and identifies bottlenecks.
- Guides the selection of the best algorithm for a given problem.

## 🔑 Big O Notation

**Big O Notation** is a mathematical notation used to describe the upper bound of an algorithm’s time complexity. It characterizes the growth rate of the algorithm's running time as the input size increases, focusing on the worst-case scenario.

### Example:
- **O(1)** means constant time: The algorithm's runtime doesn't change as the input size increases.
- **O(n)** means linear time: The runtime grows directly proportional to the input size.

## ⏱️ Common Time Complexities

### Constant Time O(1)

An algorithm has constant time complexity if it performs a fixed number of operations regardless of the input size.

#### Example:
```java
int getFirstElement(int[] arr) {
    return arr[0];  // This operation always takes the same amount of time, no matter the size of the array.
}
```

### Logarithmic Time O(log n)

An algorithm has logarithmic time complexity if it reduces the problem size by a constant factor at each step. This commonly occurs in algorithms like binary search.

#### Example:
```java
int binarySearch(int[] arr, int target) {
    int low = 0, high = arr.length - 1;
    while (low <= high) {
        int mid = low + (high - low) / 2;
        if (arr[mid] == target) {
            return mid;
        } else if (arr[mid] < target) {
            low = mid + 1;
        } else {
            high = mid - 1;
        }
    }
    return -1;
}
```

### Linear Time O(n)

An algorithm has linear time complexity if the number of operations grows directly in proportion to the input size.

#### Example:
```java
int sumArray(int[] arr) {
    int sum = 0;
    for (int num : arr) {
        sum += num;  // The loop runs n times, where n is the size of the array.
    }
    return sum;
}
```

### Linearithmic Time O(n log n)

An algorithm has linearithmic time complexity if it performs a logarithmic number of operations for each element in the input. This is common in efficient sorting algorithms like Merge Sort and Quick Sort.

#### Example (Merge Sort):
```java
public void mergeSort(int[] arr, int left, int right) {
    if (left < right) {
        int mid = (left + right) / 2;
        mergeSort(arr, left, mid);
        mergeSort(arr, mid + 1, right);
        merge(arr, left, mid, right);
    }
}
```

### Quadratic Time O(n^2)

An algorithm has quadratic time complexity if the number of operations grows as the square of the input size. This is common in algorithms that involve nested loops, such as simple sorting algorithms like Bubble Sort.

#### Example:
```java
void bubbleSort(int[] arr) {
    for (int i = 0; i < arr.length - 1; i++) {
        for (int j = 0; j < arr.length - i - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
    }
}
```

### Cubic Time O(n^3)

An algorithm has cubic time complexity if it involves three nested loops, where the number of operations grows as the cube of the input size.

#### Example:
```java
void cubicAlgorithm(int[][] matrix) {
    int sum = 0;
    for (int i = 0; i < matrix.length; i++) {
        for (int j = 0; j < matrix.length; j++) {
            for (int k = 0; k < matrix.length; k++) {
                sum += matrix[i][j] * matrix[j][k];
            }
        }
    }
}
```

### Exponential Time O(2^n)

An algorithm has exponential time complexity if its runtime doubles with each additional input. This is typical for algorithms that solve combinatorial problems, such as generating all subsets of a set or solving the Traveling Salesman Problem (TSP).

#### Example:
```java
void printSubsets(int[] arr, int index, List<Integer> current) {
    if (index == arr.length) {
        System.out.println(current);
        return;
    }
    current.add(arr[index]);
    printSubsets(arr, index + 1, current);
    current.remove(current.size() - 1);
    printSubsets(arr, index + 1, current);
}
```

### Factorial Time O(n!)

An algorithm has factorial time complexity if its runtime grows factorially with the input size. This occurs in algorithms that involve generating all permutations or combinations, such as solving the Traveling Salesman Problem using brute force.

#### Example:
```java
void generatePermutations(int[] arr, int index) {
    if (index == arr.length - 1) {
        System.out.println(Arrays.toString(arr));
        return;
    }
    for (int i = index; i < arr.length; i++) {
        swap(arr, i, index);
        generatePermutations(arr, index + 1);
        swap(arr, i, index);
    }
}

void swap(int[] arr, int i, int j) {
    int temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
}
```

## 🔍 How to Analyze Time Complexity

### Counting Operations

To analyze an algorithm’s time complexity, count the number of basic operations (e.g., comparisons, assignments) the algorithm performs relative to the size of the input.

#### Example:
```java
void exampleAlgorithm(int[] arr) {
    for (int i = 0; i < arr.length; i++) { // Executes n times
        for (int j = 0; j < arr.length; j++) { // Executes n times for each i
            System.out.println(arr[i] + arr[j]); // Executes n^2 times
        }
    }
}
```

### Nested Loops

When loops are nested, multiply their number of iterations to determine the total number of operations. For example, two nested loops each running `n` times result in O(n^2) time complexity.

### Recursion

To analyze recursive algorithms, you often need to set up a recurrence relation and solve it. This is common in divide-and-conquer algorithms like Merge Sort, where each recursion reduces the problem size.

#### Example:
```java
void recursiveAlgorithm(int n) {
    if (n <= 1) return;
    recursiveAlgorithm(n / 2); // Divides the problem in half at each step
}
```

## 🏆 Best, Worst, and Average Case Analysis

- **Best Case**: The time complexity for the best possible input.
- **Worst Case**: The time complexity for the worst possible input (most commonly used in Big O analysis).
- **Average Case**: The expected time complexity for a "typical" input.

### Example (Linear Search):
- **Best Case**: O(1) — The element is found at the beginning.
- **Worst Case**: O(n) — The element is found at the end or not at all.

## ⏳ Amortized Time Complexity



Amortized analysis is used to analyze algorithms that have expensive operations that occur infrequently. The average cost of operations over time is considered.

### Example (Dynamic Arrays):
In a dynamic array, resizing the array is an expensive operation, but it happens infrequently. The amortized time complexity for insertion is still O(1), despite the occasional resizing.

## ⚠️ Common Pitfalls and Best Practices

1. **Ignoring Constant Factors**: Time complexity analysis focuses on the growth rate as input size increases, so constant factors are generally ignored (e.g., O(2n) is still O(n)).
2. **Overlooking Edge Cases**: Ensure that all edge cases (e.g., empty arrays, extremely large inputs) are considered when analyzing time complexity.
3. **Recursive Algorithms**: Be mindful of recursive algorithms, as they can lead to exponential time complexity if not properly optimized.

## 🎓 Conclusion

Understanding and analyzing time complexity is critical for writing efficient algorithms that can scale with increasing input sizes. Time complexity gives a clear indication of how an algorithm performs and provides insights into how to optimize it.

Key takeaways:
- **Big O Notation**: Describes the upper bound of an algorithm’s time complexity.
- **Common Complexities**: Ranges from constant time O(1) to factorial time O(n!).
- **Optimizing Algorithms**: Analyzing time complexity helps identify inefficiencies and guide improvements.

By mastering time complexity analysis, you’ll be able to design and optimize algorithms for maximum performance in your Java applications! 💻🚀

---
