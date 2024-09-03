---

# 🧩 Common Divide and Conquer Problems in Java

![Java Divide and Conquer Problems](https://img.shields.io/badge/Java-Divide_and_Conquer_Problems-blue?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Merge Sort](#-merge-sort)
- [Quick Sort](#-quick-sort)
- [Binary Search](#-binary-search)
- [Maximum Subarray Problem (Kadane's Algorithm)](#-maximum-subarray-problem-kadanes-algorithm)
- [Matrix Multiplication (Strassen's Algorithm)](#-matrix-multiplication-strassens-algorithm)
- [Closest Pair of Points](#-closest-pair-of-points)
- [Exponentiation by Squaring](#-exponentiation-by-squaring)
- [Counting Inversions](#-counting-inversions)

## 🔄 Merge Sort

### Problem Statement:
Sort an array of integers in ascending order.

### Divide and Conquer Approach:
Merge Sort divides the array into two halves, recursively sorts each half, and then merges the sorted halves to produce the final sorted array.

#### Implementation:
```java
public class MergeSort {
    public void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }

    private void merge(int[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        int[] leftArray = new int[n1];
        int[] rightArray = new int[n2];

        System.arraycopy(arr, left, leftArray, 0, n1);
        System.arraycopy(arr, mid + 1, rightArray, 0, n2);

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (leftArray[i] <= rightArray[j]) {
                arr[k] = leftArray[i];
                i++;
            } else {
                arr[k] = rightArray[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            arr[k] = leftArray[i];
            i++;
            k++;
        }

        while (j < n2) {
            arr[k] = rightArray[j];
            j++;
            k++;
        }
    }

    public static void main(String[] args) {
        int[] arr = {12, 11, 13, 5, 6, 7};
        MergeSort sorter = new MergeSort();
        sorter.mergeSort(arr, 0, arr.length - 1);
        System.out.println("Sorted array: " + Arrays.toString(arr));
    }
}
```

## ⚡ Quick Sort

### Problem Statement:
Sort an array of integers in ascending order.

### Divide and Conquer Approach:
Quick Sort selects a pivot element, partitions the array around the pivot, and then recursively sorts the partitions.

#### Implementation:
```java
public class QuickSort {
    public void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = (low - 1);

        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }

        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1;
    }

    public static void main(String[] args) {
        int[] arr = {10, 7, 8, 9, 1, 5};
        QuickSort sorter = new QuickSort();
        sorter.quickSort(arr, 0, arr.length - 1);
        System.out.println("Sorted array: " + Arrays.toString(arr));
    }
}
```

## 🔍 Binary Search

### Problem Statement:
Given a sorted array of integers, find the index of a target value.

### Divide and Conquer Approach:
Binary Search repeatedly divides the array in half, narrowing down the search range until the target value is found or the search space is empty.

#### Implementation:
```java
public class BinarySearch {
    public int binarySearch(int[] arr, int target) {
        int low = 0;
        int high = arr.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            if (arr[mid] == target) {
                return mid;
            }

            if (arr[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return -1;
    }

    public static void main(String[] args) {
        BinarySearch searcher = new BinarySearch();
        int[] arr = {2, 3, 4, 10, 40};
        int target = 10;
        int result = searcher.binarySearch(arr, target);
        System.out.println("Element found at index: " + result);
    }
}
```

## 📈 Maximum Subarray Problem (Kadane's Algorithm)

### Problem Statement:
Find the contiguous subarray within a one-dimensional array of numbers that has the largest sum.

### Divide and Conquer Approach:
The problem can be solved by dividing the array into two halves, finding the maximum subarray sum for the left and right halves, and finding the maximum subarray that crosses the midpoint.

#### Implementation:
```java
public class MaximumSubarray {
    public int maxSubArray(int[] nums) {
        return maxSubArray(nums, 0, nums.length - 1);
    }

    private int maxSubArray(int[] nums, int left, int right) {
        if (left == right) return nums[left];

        int mid = left + (right - left) / 2;

        int leftSum = maxSubArray(nums, left, mid);
        int rightSum = maxSubArray(nums, mid + 1, right);
        int crossSum = maxCrossingSum(nums, left, mid, right);

        return Math.max(Math.max(leftSum, rightSum), crossSum);
    }

    private int maxCrossingSum(int[] nums, int left, int mid, int right) {
        int sum = 0;
        int leftMax = Integer.MIN_VALUE;
        for (int i = mid; i >= left; i--) {
            sum += nums[i];
            if (sum > leftMax) leftMax = sum;
        }

        sum = 0;
        int rightMax = Integer.MIN_VALUE;
        for (int i = mid + 1; i <= right; i++) {
            sum += nums[i];
            if (sum > rightMax) rightMax = sum;
        }

        return leftMax + rightMax;
    }

    public static void main(String[] args) {
        MaximumSubarray solver = new MaximumSubarray();
        int[] nums = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println("Maximum subarray sum: " + solver.maxSubArray(nums));
    }
}
```

## 🔢 Matrix Multiplication (Strassen's Algorithm)

### Problem Statement:
Multiply two matrices more efficiently than the standard matrix multiplication method.

### Divide and Conquer Approach:
Strassen's Algorithm divides each matrix into four submatrices and recursively multiplies them, reducing the overall number of multiplications required.

#### Simplified Implementation:
```java
public class StrassensMatrixMultiplication {
    public int[][] multiply(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];

        if (n == 1) {
            C[0][0] = A[0][0] * B[0][0];
        } else {
            int[][] A11 = new int[n / 2][n / 2];
            int[][] A12 = new int[n / 2][n / 2];
            int[][] A21 = new int[n / 2][n / 2];
            int[][] A22 = new int[n / 2][n / 2];

            int[][] B11 = new int[n / 2][n / 2];
            int[][] B12 = new int[n / 2][n / 2];
            int[][] B21 = new int[n / 2][n / 2];
            int[][] B22 = new int[n / 2][n / 2];

            // Dividing the matrices into quadrants
            divide(A, A11, 0, 0);
            divide(A, A12, 0, n / 2);
            divide(A, A21, n / 2, 0);
            divide(A, A22, n / 2, n / 2);

            divide(B, B11, 0, 0);
           

 divide(B, B12, 0, n / 2);
            divide(B, B21, n / 2, 0);
            divide(B, B22, n / 2, n / 2);

            // Strassen's formula
            int[][] M1 = multiply(add(A11, A22), add(B11, B22));
            int[][] M2 = multiply(add(A21, A22), B11);
            int[][] M3 = multiply(A11, subtract(B12, B22));
            int[][] M4 = multiply(A22, subtract(B21, B11));
            int[][] M5 = multiply(add(A11, A12), B22);
            int[][] M6 = multiply(subtract(A21, A11), add(B11, B12));
            int[][] M7 = multiply(subtract(A12, A22), add(B21, B22));

            int[][] C11 = add(subtract(add(M1, M4), M5), M7);
            int[][] C12 = add(M3, M5);
            int[][] C21 = add(M2, M4);
            int[][] C22 = add(subtract(add(M1, M3), M2), M6);

            // Combining the result
            combine(C11, C, 0, 0);
            combine(C12, C, 0, n / 2);
            combine(C21, C, n / 2, 0);
            combine(C22, C, n / 2, n / 2);
        }

        return C;
    }

    private void divide(int[][] P, int[][] C, int iB, int jB) {
        for (int i = 0, iC = iB; i < C.length; i++, iC++) {
            for (int j = 0, jC = jB; j < C.length; j++, jC++) {
                C[i][j] = P[iC][jC];
            }
        }
    }

    private void combine(int[][] C, int[][] P, int iB, int jB) {
        for (int i = 0, iC = iB; i < C.length; i++, iC++) {
            for (int j = 0, jC = jB; j < C.length; j++, jC++) {
                P[iC][jC] = C[i][j];
            }
        }
    }

    private int[][] add(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] + B[i][j];
            }
        }
        return C;
    }

    private int[][] subtract(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] - B[i][j];
            }
        }
        return C;
    }

    public static void main(String[] args) {
        StrassensMatrixMultiplication smm = new StrassensMatrixMultiplication();
        int[][] A = {{1, 2}, {3, 4}};
        int[][] B = {{5, 6}, {7, 8}};
        int[][] C = smm.multiply(A, B);

        System.out.println("Product of matrices:");
        for (int[] row : C) {
            System.out.println(Arrays.toString(row));
        }
    }
}
```

## 🔍 Closest Pair of Points

### Problem Statement:
Given a set of points in a plane, find the pair of points with the smallest distance between them.

### Divide and Conquer Approach:
Divide the set of points into two halves, find the closest pairs in each half, and then find the closest pair that spans both halves.

#### Implementation:
```java
import java.util.Arrays;

public class ClosestPair {
    public double closestPair(Point[] points) {
        Arrays.sort(points, (a, b) -> Double.compare(a.x, b.x));
        return closestPair(points, 0, points.length - 1);
    }

    private double closestPair(Point[] points, int left, int right) {
        if (right - left <= 3) {
            return bruteForce(points, left, right);
        }

        int mid = left + (right - left) / 2;
        double d1 = closestPair(points, left, mid);
        double d2 = closestPair(points, mid + 1, right);
        double d = Math.min(d1, d2);

        Point[] strip = new Point[right - left + 1];
        int j = 0;
        for (int i = left; i <= right; i++) {
            if (Math.abs(points[i].x - points[mid].x) < d) {
                strip[j++] = points[i];
            }
        }

        Arrays.sort(strip, 0, j, (a, b) -> Double.compare(a.y, b.y));

        for (int i = 0; i < j; i++) {
            for (int k = i + 1; k < j && (strip[k].y - strip[i].y) < d; k++) {
                d = Math.min(d, distance(strip[i], strip[k]));
            }
        }

        return d;
    }

    private double bruteForce(Point[] points, int left, int right) {
        double minDist = Double.MAX_VALUE;
        for (int i = left; i < right; i++) {
            for (int j = i + 1; j <= right; j++) {
                minDist = Math.min(minDist, distance(points[i], points[j]));
            }
        }
        return minDist;
    }

    private double distance(Point a, Point b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    public static void main(String[] args) {
        ClosestPair cp = new ClosestPair();
        Point[] points = {new Point(2, 3), new Point(12, 30), new Point(40, 50), new Point(5, 1), new Point(12, 10), new Point(3, 4)};
        System.out.println("The smallest distance is " + cp.closestPair(points));
    }
}

class Point {
    double x, y;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
```

## 📉 Exponentiation by Squaring

### Problem Statement:
Efficiently compute the power of a number.

### Divide and Conquer Approach:
Exponentiation by Squaring is a method to calculate `a^b` in O(log b) time by recursively dividing the exponentiation into smaller subproblems.

#### Implementation:
```java
public class ExponentiationBySquaring {
    public long power(long base, long exponent) {
        if (exponent == 0) return 1;
        if (exponent % 2 == 0) {
            return power(base * base, exponent / 2);
        } else {
            return base * power(base * base, (exponent - 1) / 2);
        }
    }

    public static void main(String[] args) {
        ExponentiationBySquaring exp = new ExponentiationBySquaring();
        long base = 2;
        long exponent = 10;
        System.out.println(base + "^" + exponent + " = " + exp.power(base, exponent));
    }
}
```

## 📊 Counting Inversions

### Problem Statement:
Given an array of integers, count the number of inversions required to sort the array.

### Divide and Conquer Approach:
The problem can be solved by modifying the merge sort algorithm to count inversions while merging two halves of the array.

#### Implementation:
```java
public class CountingInversions {
    public int countInversions(int[] arr) {
        return countInversions(arr, new int[arr.length], 0, arr.length - 1);
    }

    private int countInversions(int[] arr, int[] temp, int left, int right) {
        if (left >= right) return 0;
        
        int mid = (left + right) / 2;
        int invCount = 0;

        invCount += countInversions(arr, temp, left, mid);
        invCount += countInversions(arr, temp, mid + 1, right);
        invCount += mergeAndCount(arr, temp, left, mid, right);

        return invCount;
    }

    private int mergeAndCount(int[] arr, int[] temp, int left, int mid, int right) {
        int i = left, j = mid + 1, k = left;
        int invCount = 0;

        while (i <= mid && j <= right) {
            if (arr[i] <= arr[j]) {
                temp[k++] = arr[i++];
            } else {
                temp[k++] = arr[j++];
                invCount += (mid + 1) - i;
            }
        }

        while (i <= mid) {
            temp[k++] = arr[i++];
        }

        while (j <= right) {
            temp[k++] = arr[j++];
        }

        System.arraycopy

(temp, left, arr, left, right - left + 1);
        
        return invCount;
    }

    public static void main(String[] args) {
        CountingInversions counter = new CountingInversions();
        int[] arr = {2, 4, 1, 3, 5};
        System.out.println("Number of inversions: " + counter.countInversions(arr));
    }
}
```

## 🎓 Conclusion

The problems outlined in this section demonstrate the power and versatility of the Divide and Conquer technique in solving complex computational problems. By mastering these problems, you'll gain a deep understanding of how to effectively break down problems into smaller, more manageable components and solve them efficiently.

Key takeaways:
- **Merge Sort and Quick Sort**: Fundamental sorting algorithms that efficiently sort arrays using Divide and Conquer.
- **Binary Search**: A classic example of applying Divide and Conquer to search problems.
- **Maximum Subarray and Closest Pair of Points**: Demonstrate how Divide and Conquer can be applied to optimization problems.
- **Matrix Multiplication and Exponentiation**: Show how Divide and Conquer can reduce computational complexity in mathematical problems.

By practicing these problems, you'll enhance your problem-solving skills and be well-prepared to tackle complex Divide and Conquer challenges in your Java applications! 💻🚀

---
