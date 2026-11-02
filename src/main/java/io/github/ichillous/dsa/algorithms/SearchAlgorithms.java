package io.github.ichillous.dsa.algorithms;

import java.util.Objects;

/** Searches primitive arrays. Sorted-search methods require ascending input and do not modify it. */
public final class SearchAlgorithms {
    private SearchAlgorithms() {}

    /** Returns the first matching index, or {@code -1}. O(n) time and O(1) extra space. */
    public static int linearSearch(int[] values, int target) {
        Objects.requireNonNull(values, "values");
        for (int index = 0; index < values.length; index++) {
            if (values[index] == target) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Returns the first matching index, or {@code -1}, in an ascending array.
     * O(log n) time and O(1) extra space. Sortedness is a precondition, not checked here.
     */
    public static int binarySearch(int[] sorted, int target) {
        int index = lowerBound(sorted, target);
        return index < sorted.length && sorted[index] == target ? index : -1;
    }

    /**
     * Returns the first index whose value is at least {@code target}, or {@code sorted.length}.
     * O(log n) time and O(1) extra space; input must be ascending.
     */
    public static int lowerBound(int[] sorted, int target) {
        Objects.requireNonNull(sorted, "sorted");
        int low = 0;
        int high = sorted.length;
        // Values before low are < target; values at or after high are >= target.
        while (low < high) {
            int middle = low + (high - low) / 2;
            if (sorted[middle] < target) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    /**
     * Returns the first index whose value exceeds {@code target}, or {@code sorted.length}.
     * O(log n) time and O(1) extra space; input must be ascending.
     */
    public static int upperBound(int[] sorted, int target) {
        Objects.requireNonNull(sorted, "sorted");
        int low = 0;
        int high = sorted.length;
        while (low < high) {
            int middle = low + (high - low) / 2;
            if (sorted[middle] <= target) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }
}
