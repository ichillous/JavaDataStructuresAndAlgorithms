package io.github.ichillous.dsa.algorithms;

import java.util.Comparator;
import java.util.Objects;

/**
 * In-place ascending sorts for {@code int[]} values, plus a stable {@link Comparator} merge sort
 * for object arrays. Every method rejects a null array.
 */
public final class SortAlgorithms {
    /**
     * Largest value range {@code max - min + 1} that {@link #countingSort(int[])} accepts. The
     * count table has one {@code int} slot per value in the range, so this caps it at 4 MiB.
     */
    public static final int COUNTING_SORT_MAX_RANGE = 1 << 20;

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
     * Counting sort, ascending, in place. It tallies how often each value occurs in a table indexed
     * by {@code value - min} and rewrites the array from the tallies, so it never compares two
     * elements. Equal ints are indistinguishable, so stability is not observable.
     *
     * <p>Time is O(n + r) and auxiliary space is O(r), where r is {@code max - min + 1}. The range
     * is computed in {@code long} arithmetic, so extreme values cannot wrap. An empty or one-element
     * array returns immediately.
     *
     * @param values the array to sort in place; must not be null
     * @throws NullPointerException if {@code values} is null
     * @throws IllegalArgumentException if the range exceeds {@link #COUNTING_SORT_MAX_RANGE}; the
     *     array is left unchanged
     */
    public static void countingSort(int[] values) {
        Objects.requireNonNull(values, "values");
        if (values.length < 2) {
            return;
        }
        int min = values[0];
        int max = values[0];
        for (int value : values) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        long range = (long) max - min + 1;
        if (range > COUNTING_SORT_MAX_RANGE) {
            throw new IllegalArgumentException(
                    "value range " + range + " exceeds " + COUNTING_SORT_MAX_RANGE);
        }
        int[] counts = new int[(int) range];
        for (int value : values) {
            counts[value - min]++; // 0 <= value - min < range <= 2^20, so no int overflow
        }
        int index = 0;
        for (int offset = 0; offset < counts.length; offset++) {
            for (int remaining = counts[offset]; remaining > 0; remaining--) {
                values[index++] = min + offset;
            }
        }
    }

    /**
     * Least-significant-digit radix sort, ascending, in place. It makes four stable counting passes
     * over 8-bit digits, from the lowest byte to the highest. Each key is first XORed with
     * {@link Integer#MIN_VALUE}, which flips the sign bit so that negative values order before
     * non-negative ones in unsigned digit order. The flip only affects the digit extracted, never
     * the stored values. Every value in the full {@code int} range is accepted.
     *
     * <p>Time is O(n) (four passes of n + 256 steps) and auxiliary space is O(n) for the buffer.
     * An empty or one-element array returns immediately.
     *
     * @param values the array to sort in place; must not be null
     * @throws NullPointerException if {@code values} is null
     */
    public static void radixSort(int[] values) {
        Objects.requireNonNull(values, "values");
        if (values.length < 2) {
            return;
        }
        int[] source = values;
        int[] target = new int[values.length];
        for (int shift = 0; shift < Integer.SIZE; shift += Byte.SIZE) {
            int[] starts = new int[257];
            for (int value : source) {
                starts[digit(value, shift) + 1]++;
            }
            for (int bucket = 0; bucket < 256; bucket++) {
                starts[bucket + 1] += starts[bucket];
            }
            for (int value : source) { // front to back keeps each pass stable
                target[starts[digit(value, shift)]++] = value;
            }
            int[] swap = source;
            source = target;
            target = swap;
        }
        // Four passes is an even count, so the final result is back in values.
    }

    private static int digit(int value, int shift) {
        return ((value ^ Integer.MIN_VALUE) >>> shift) & 0xFF;
    }

    /**
     * Stable top-down merge sort for objects, in place in the caller's array. Elements that the
     * comparator treats as equal keep their original relative order: on a tie the merge always
     * takes the left element first.
     *
     * <p>All elements are checked for null before any element moves, so a null leaves the array
     * unchanged. If the comparator itself throws, the array may be partially merged but still holds
     * the same elements. The comparator must be consistent (a total preorder); this is not
     * verified. Never write it as {@code (a, b) -> a - b}, which overflows for distant ints; use
     * {@link Integer#compare(int, int)}.
     *
     * <p>Time is O(n log n) comparisons and auxiliary space is O(n) for a copy of the array, plus
     * O(log n) recursion depth.
     *
     * @param <T> the element type
     * @param values the array to sort in place; must not be null or contain nulls
     * @param order the ordering; must not be null
     * @throws NullPointerException if {@code values}, {@code order} or any element is null
     */
    public static <T> void mergeSort(T[] values, Comparator<? super T> order) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(order, "order");
        for (T value : values) {
            Objects.requireNonNull(value, "values must not contain null");
        }
        mergeSort(values, values.clone(), order, 0, values.length);
    }

    private static <T> void mergeSort(
            T[] values, T[] buffer, Comparator<? super T> order, int from, int to) {
        if (to - from < 2) {
            return;
        }
        int middle = from + (to - from) / 2;
        mergeSort(values, buffer, order, from, middle);
        mergeSort(values, buffer, order, middle, to);
        if (order.compare(values[middle - 1], values[middle]) <= 0) {
            return;
        }
        int left = from;
        int right = middle;
        for (int index = from; index < to; index++) {
            if (left < middle && (right == to || order.compare(values[left], values[right]) <= 0)) {
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
