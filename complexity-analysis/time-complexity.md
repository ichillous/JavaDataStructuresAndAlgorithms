# Time Complexity

Time complexity is a crucial concept in algorithm analysis that helps us understand how the runtime of an algorithm grows as the input size increases. It provides a way to express the efficiency of algorithms in terms of the number of operations performed.

## Table of Contents
- [Introduction](#introduction)
- [Big O Notation](#big-o-notation)
- [Common Time Complexities](#common-time-complexities)
- [Analyzing Time Complexity](#analyzing-time-complexity)
- [Best, Average, and Worst Case](#best-average-and-worst-case)
- [Examples in Java](#examples-in-java)

## Introduction

Time complexity describes the amount of time an algorithm takes to run as a function of the input size. It focuses on how the algorithm's performance scales with larger inputs, rather than the exact number of operations or time in seconds.

## Big O Notation

Big O notation is used to describe the upper bound of the time complexity. It provides a standardized way to express how the runtime grows in the worst-case scenario.

Common Big O notations:
- O(1): Constant time
- O(log n): Logarithmic time
- O(n): Linear time
- O(n log n): Linearithmic time
- O(n²): Quadratic time
- O(2ⁿ): Exponential time

## Common Time Complexities

1. **Constant Time - O(1)**: The algorithm takes the same amount of time regardless of the input size.
   Example: Accessing an array element by index.

2. **Logarithmic Time - O(log n)**: The runtime grows logarithmically with the input size.
   Example: Binary search on a sorted array.

3. **Linear Time - O(n)**: The runtime grows linearly with the input size.
   Example: Traversing an array or linked list.

4. **Linearithmic Time - O(n log n)**: Common in efficient sorting algorithms.
   Example: Merge Sort, Quick Sort (average case).

5. **Quadratic Time - O(n²)**: The runtime grows quadratically with the input size.
   Example: Nested loops, simple sorting algorithms like Bubble Sort.

6. **Exponential Time - O(2ⁿ)**: The runtime doubles with each addition to the input.
   Example: Recursive calculation of Fibonacci numbers without memoization.

## Analyzing Time Complexity

To analyze time complexity:
1. Identify the basic operations in the algorithm.
2. Count how many times these operations are executed.
3. Express this count as a function of the input size.
4. Simplify the function to its dominant term.
5. Express the result using Big O notation.

## Best, Average, and Worst Case

- **Best Case**: The minimum time required for an algorithm (optimal input).
- **Average Case**: The expected time for typical inputs.
- **Worst Case**: The maximum time required (least optimal input).

We usually focus on the worst-case scenario to ensure our algorithm performs well under all conditions.

## Examples in Java

1. **Constant Time - O(1)**
   ```java
   public int getFirstElement(int[] array) {
       return array[0];
   }
   ```

2. **Linear Time - O(n)**
   ```java
   public boolean linearSearch(int[] array, int target) {
       for (int element : array) {
           if (element == target) {
               return true;
           }
       }
       return false;
   }
   ```

3. **Quadratic Time - O(n²)**
   ```java
   public void bubbleSort(int[] array) {
       int n = array.length;
       for (int i = 0; i < n - 1; i++) {
           for (int j = 0; j < n - i - 1; j++) {
               if (array[j] > array[j + 1]) {
                   // Swap elements
                   int temp = array[j];
                   array[j] = array[j + 1];
                   array[j + 1] = temp;
               }
           }
       }
   }
   ```

Understanding time complexity helps in choosing the right algorithms and data structures for efficient problem-solving, especially when dealing with large datasets.
