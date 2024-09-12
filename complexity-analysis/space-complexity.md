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

Here are several examples of Java code with different space complexities, ranging from simple to complex:

1. **Constant Space - O(1)**
   ```java
   public int sum(int a, int b) {
       return a + b;
   }
   ```
   This function uses a fixed amount of memory regardless of input size.

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
   This function creates a new array of the same size as the input.

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
   This function creates a 2D array with n² elements.

4. **Logarithmic Space - O(log n)**
   ```java
   public int binarySearch(int[] sortedArray, int target) {
       return binarySearchHelper(sortedArray, target, 0, sortedArray.length - 1);
   }

   private int binarySearchHelper(int[] arr, int target, int left, int right) {
       if (left > right) {
           return -1;
       }
       int mid = left + (right - left) / 2;
       if (arr[mid] == target) {
           return mid;
       } else if (arr[mid] < target) {
           return binarySearchHelper(arr, target, mid + 1, right);
       } else {
           return binarySearchHelper(arr, target, left, mid - 1);
       }
   }
   ```
   This recursive binary search uses O(log n) space due to the call stack.

5. **Linear Space with Recursion - O(n)**
   ```java
   public void printArray(int[] arr, int index) {
       if (index == arr.length) {
           return;
       }
       System.out.println(arr[index]);
       printArray(arr, index + 1);
   }
   ```
   This recursive function to print an array uses O(n) space due to the call stack.

6. **Space-Time Trade-off (Memoization) - O(n)**
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
   This memoized Fibonacci calculation uses O(n) space to store computed results, trading space for time efficiency.

7. **Dynamic Programming - O(n)**
   ```java
   public int longestIncreasingSubsequence(int[] nums) {
       int[] dp = new int[nums.length];
       Arrays.fill(dp, 1);
       int maxLen = 1;

       for (int i = 1; i < nums.length; i++) {
           for (int j = 0; j < i; j++) {
               if (nums[i] > nums[j]) {
                   dp[i] = Math.max(dp[i], dp[j] + 1);
               }
           }
           maxLen = Math.max(maxLen, dp[i]);
       }
       return maxLen;
   }
   ```
   This dynamic programming solution for the Longest Increasing Subsequence problem uses O(n) space.

8. **Graph Representation - Adjacency List O(V + E)**
   ```java
   public class Graph {
       private int V;
       private List<List<Integer>> adj;

       public Graph(int v) {
           V = v;
           adj = new ArrayList<>(V);
           for (int i = 0; i < V; i++) {
               adj.add(new ArrayList<>());
           }
       }

       public void addEdge(int v, int w) {
           adj.get(v).add(w);
       }
   }
   ```
   This graph representation using an adjacency list uses O(V + E) space, where V is the number of vertices and E is the number of edges.

9. **Combination of Space Complexities**
   ```java
   public List<List<Integer>> generatePascalTriangle(int numRows) {
       List<List<Integer>> triangle = new ArrayList<>();
       
       for (int i = 0; i < numRows; i++) {
           List<Integer> row = new ArrayList<>();
           for (int j = 0; j <= i; j++) {
               if (j == 0 || j == i) {
                   row.add(1);
               } else {
                   int sum = triangle.get(i - 1).get(j - 1) + triangle.get(i - 1).get(j);
                   row.add(sum);
               }
           }
           triangle.add(row);
       }
       
       return triangle;
   }
   ```
   This function generates Pascal's Triangle, using O(n²) space to store all elements of the triangle.

10. **Space Optimization Example**
    ```java
    public int[] countSort(int[] arr) {
        int max = Arrays.stream(arr).max().getAsInt();
        int min = Arrays.stream(arr).min().getAsInt();
        int range = max - min + 1;
        
        int[] count = new int[range];
        int[] output = new int[arr.length];
        
        for (int i = 0; i < arr.length; i++) {
            count[arr[i] - min]++;
        }
        
        for (int i = 1; i < count.length; i++) {
            count[i] += count[i - 1];
        }
        
        for (int i = arr.length - 1; i >= 0; i--) {
            output[count[arr[i] - min] - 1] = arr[i];
            count[arr[i] - min]--;
        }
        
        return output;
    }
    ```
    This counting sort implementation uses O(n + k) space, where n is the input size and k is the range of input values. It's more space-efficient than some other sorting algorithms for certain types of input.

Understanding these examples and their space complexities helps in choosing the right algorithms and data structures for efficient memory usage, especially when dealing with large datasets or memory-constrained environments. It's important to consider not only the amount of memory used but also how it scales with input size and the trade-offs between time and space efficiency.
