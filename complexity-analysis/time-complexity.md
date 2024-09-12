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

Here are several examples of Java code with different time complexities, ranging from simple to complex:

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

3. **Logarithmic Time - O(log n)**
   ```java
   public int binarySearch(int[] sortedArray, int target) {
       int left = 0;
       int right = sortedArray.length - 1;
       
       while (left <= right) {
           int mid = left + (right - left) / 2;
           if (sortedArray[mid] == target) {
               return mid;
           }
           if (sortedArray[mid] < target) {
               left = mid + 1;
           } else {
               right = mid - 1;
           }
       }
       return -1;
   }
   ```

4. **Linearithmic Time - O(n log n)**
   ```java
   public void mergeSort(int[] arr, int left, int right) {
       if (left < right) {
           int mid = (left + right) / 2;
           mergeSort(arr, left, mid);
           mergeSort(arr, mid + 1, right);
           merge(arr, left, mid, right);
       }
   }

   private void merge(int[] arr, int left, int mid, int right) {
       // Merge two sorted subarrays
       // Implementation details omitted for brevity
   }
   ```

5. **Quadratic Time - O(n²)**
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

6. **Exponential Time - O(2ⁿ)**
   ```java
   public int fibonacci(int n) {
       if (n <= 1) {
           return n;
       }
       return fibonacci(n - 1) + fibonacci(n - 2);
   }
   ```

7. **Polynomial Time - O(n³)**
   ```java
   public void tripleNestedLoop(int n) {
       for (int i = 0; i < n; i++) {
           for (int j = 0; j < n; j++) {
               for (int k = 0; k < n; k++) {
                   System.out.println("i: " + i + ", j: " + j + ", k: " + k);
               }
           }
       }
   }
   ```

8. **Combination of Time Complexities**
   ```java
   public void combinedComplexity(int[] arr, int target) {
       // O(n log n)
       Arrays.sort(arr);
       
       // O(n)
       for (int i = 0; i < arr.length; i++) {
           // O(log n)
           if (binarySearch(arr, target - arr[i]) != -1) {
               System.out.println("Found pair: " + arr[i] + ", " + (target - arr[i]));
               return;
           }
       }
       System.out.println("No pair found");
   }
   
   // Total time complexity: O(n log n) + O(n * log n) = O(n log n)
   ```

9. **Amortized Time Complexity - ArrayList's add operation**
   ```java
   public class DynamicArray<T> {
       private Object[] array;
       private int size;
       private int capacity;

       public DynamicArray() {
           array = new Object[2];
           size = 0;
           capacity = 2;
       }

       // Amortized O(1) time complexity
       public void add(T element) {
           if (size == capacity) {
               resize();
           }
           array[size++] = element;
       }

       private void resize() {
           capacity *= 2;
           Object[] newArray = new Object[capacity];
           System.arraycopy(array, 0, newArray, 0, size);
           array = newArray;
       }
   }
   ```

Understanding these examples and their time complexities helps in choosing the right algorithms and data structures for efficient problem-solving, especially when dealing with large datasets. It's important to consider not only the worst-case scenario but also the average case and how the algorithm behaves with different input sizes and patterns.
