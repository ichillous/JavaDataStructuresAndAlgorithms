package io.github.ichillous.dsa.algorithms;

import java.util.Objects;

/** In-place ascending sorts for {@code int[]} values. Every method rejects a null array. */
public final class SortAlgorithms {
    private SortAlgorithms() {}

    /** Stable insertion sort: O(n²) worst time, O(n) best time, O(1) extra space. */
    public static void insertionSort(int[] values) {
        Objects.requireNonNull(values, "values");
        for (int index = 1; index < values.length; index++) {
            int value = values[index];
            int position = index;
            while (position > 0 && values[position - 1] > value) {
                values[position] = values[position - 1];
                position--;
            }
            values[position] = value;
        }
    }

    /** Stable bubble sort with early exit: O(n²) worst time, O(n) best time, O(1) space. */
    public static void bubbleSort(int[] values) {
        Objects.requireNonNull(values, "values");
        for (int end = values.length - 1; end > 0; end--) {
            boolean changed = false;
            for (int index = 0; index < end; index++) {
                if (values[index] > values[index + 1]) {
                    swap(values, index, index + 1);
                    changed = true;
                }
            }
            if (!changed) {
                return;
            }
        }
    }

    /** Stable top-down merge sort: O(n log n) worst time and O(n) extra space. */
    public static void mergeSort(int[] values) {
        Objects.requireNonNull(values, "values");
        mergeSort(values, new int[values.length], 0, values.length);
    }

    private static void mergeSort(int[] values, int[] buffer, int from, int to) {
        if (to - from < 2) {
            return;
        }
        int middle = from + (to - from) / 2;
        mergeSort(values, buffer, from, middle);
        mergeSort(values, buffer, middle, to);
        if (values[middle - 1] <= values[middle]) {
            return;
        }
        int left = from;
        int right = middle;
        for (int index = from; index < to; index++) {
            if (left < middle && (right == to || values[left] <= values[right])) {
                buffer[index] = values[left++];
            } else {
                buffer[index] = values[right++];
            }
        }
        System.arraycopy(buffer, from, values, from, to - from);
    }

    /**
     * Three-way quicksort: typically O(n log n), O(n²) worst time, O(log n) stack space.
     * Equal keys form one partition. Recursing only on the smaller partition bounds stack depth
     * even when pivot choices produce quadratic work. This sort is not stable.
     */
    public static void quickSort(int[] values) {
        Objects.requireNonNull(values, "values");
        quickSort(values, 0, values.length - 1);
    }

    private static void quickSort(int[] values, int low, int high) {
        while (low < high) {
            int pivot = values[low + (high - low) / 2];
            int less = low;
            int scan = low;
            int greater = high;
            // [low, less) < pivot; [less, scan) == pivot; (greater, high] > pivot.
            while (scan <= greater) {
                if (values[scan] < pivot) {
                    swap(values, less++, scan++);
                } else if (values[scan] > pivot) {
                    swap(values, scan, greater--);
                } else {
                    scan++;
                }
            }
            if (less - low < high - greater) {
                quickSort(values, low, less - 1);
                low = greater + 1;
            } else {
                quickSort(values, greater + 1, high);
                high = less - 1;
            }
        }
    }

    /** Unstable heapsort: O(n log n) worst time and O(1) extra space. */
    public static void heapSort(int[] values) {
        Objects.requireNonNull(values, "values");
        for (int root = values.length / 2 - 1; root >= 0; root--) {
            siftDown(values, root, values.length);
        }
        for (int end = values.length - 1; end > 0; end--) {
            swap(values, 0, end);
            siftDown(values, 0, end);
        }
    }

    private static void siftDown(int[] values, int root, int size) {
        while (root < size / 2) {
            int child = root * 2 + 1;
            if (child + 1 < size && values[child] < values[child + 1]) {
                child++;
            }
            if (values[root] >= values[child]) {
                return;
            }
            swap(values, root, child);
            root = child;
        }
    }

    private static void swap(int[] values, int first, int second) {
        int temporary = values[first];
        values[first] = values[second];
        values[second] = temporary;
    }
}
