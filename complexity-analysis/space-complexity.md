# Space Complexity

Space complexity is a measure of the amount of memory an algorithm uses relative to the size of its input. It's crucial for understanding how efficiently an algorithm utilizes memory resources, especially when dealing with large datasets or memory-constrained environments.

## Table of Contents
- [Introduction](#introduction)
- [Types of Space Complexity](#types-of-space-complexity)
- [Common Space Complexities](#common-space-complexities)
- [Analyzing Space Complexity](#analyzing-space-complexity)
- [Trade-offs Between Time and Space](#trade-offs-between-time-and-space)
- [Examples in Java](#examples-in-java)

## Introduction

Space complexity analysis helps developers understand how much additional memory an algorithm needs as the input size grows. Like time complexity, it's typically expressed using Big O notation, which describes the upper bound of memory usage.

## Types of Space Complexity

1. **Auxiliary Space**: The extra space used by the algorithm, not including the space taken by the inputs.
2. **Total Space**: The total memory consumed, including both the space used by the inputs and any extra space used.

Generally, when we talk about space complexity, we're referring to the auxiliary space complexity.

## Common Space Complexities

1. **Constant Space - O(1)**: The algorithm uses a fixed amount of memory regardless of input size.
   Example: Simple calculations, swapping two variables.

2. **Linear Space - O(n)**: Memory usage grows linearly with the input size.
   Example: Creating a new array to store n elements.

3. **Logarithmic Space - O(log n)**: Memory usage grows logarithmically with the input size.
   Example: Some efficient sorting algorithms like Heapsort.

4. **Quadratic Space - O(n²)**: Memory usage grows quadratically with the input size.
   Example: Creating a 2D array to store all pairs of elements.

5. **Exponential Space - O(2ⁿ)**: Memory usage doubles with each addition to the input data set.
   Example: Recursive algorithms that solve Fibonacci sequence.

## Analyzing Space Complexity

To analyze space complexity:
1. Identify variables and data structures used in the algorithm.
2. Determine how the space requirements for these grow with input size.
3. Express the total space as a function of the input size.
4. Simplify to the dominant term and express using Big O notation.

## Trade-offs Between Time and Space

Often, there's a trade-off between time and space complexity. Some common scenarios:
- Using more memory to store pre-computed results (memoization) can reduce time complexity.
- In-place algorithms save space but might increase time complexity.
- Some algorithms use extra space to achieve better time complexity (e.g., hash tables).

## Examples in Java

1. **Constant Space - O(1)**
   ```java
   public int sum(int a, int b) {
       return a + b;
   }
   ```

2. **Linear Space - O(n)**
   ```java
   public int[] doubleArray(int[] arr) {
       int[] result = new int[arr.length];
       for (int i = 0; i < arr.length; i++) {
           result[i] = arr[i] * 2;
       }
       return result;
   }
   ```

3. **Quadratic Space - O(n²)**
   ```java
   public int[][] createMatrix(int n) {
       int[][] matrix = new int[n][n];
       for (int i = 0; i < n; i++) {
           for (int j = 0; j < n; j++) {
               matrix[i][j] = i * j;
           }
       }
       return matrix;
   }
   ```

4. **Space-Time Trade-off Example (Memoization)**
   ```java
   public class Fibonacci {
       private Map<Integer, Integer> memo = new HashMap<>();

       public int fib(int n) {
           if (n <= 1) return n;
           if (memo.containsKey(n)) return memo.get(n);
           int result = fib(n - 1) + fib(n - 2);
           memo.put(n, result);
           return result;
       }
   }
   ```
   This example uses extra space (O(n)) to store computed results, reducing time complexity from O(2ⁿ) to O(n).

Understanding space complexity is crucial for writing efficient algorithms, especially in environments with limited memory resources or when dealing with large-scale data processing.
