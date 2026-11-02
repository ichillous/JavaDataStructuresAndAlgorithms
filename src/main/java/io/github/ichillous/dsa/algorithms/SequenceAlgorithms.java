package io.github.ichillous.dsa.algorithms;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

/** Linear and divide-and-conquer array problems. All methods leave their input unchanged. */
public final class SequenceAlgorithms {
    private SequenceAlgorithms() {}

    /** A nonempty half-open array range and its sum, accumulated without int overflow. */
    public record Subarray(int startInclusive, int endExclusive, long sum) {
        /** Rejects a negative start or an empty/backwards range. */
        public Subarray {
            if (startInclusive < 0 || endExclusive <= startInclusive) {
                throw new IllegalArgumentException("a subarray must have 0 <= start < end");
            }
        }
    }

    /**
     * Returns the maximum-sum nonempty contiguous range, or empty for an empty array.
     * Ties choose the earliest start, then the earliest end. All-negative inputs therefore
     * return a single element with the greatest value. Any int-array sum fits in a long.
     * O(n) time and O(1) auxiliary space.
     *
     * <p>Kadane's invariant: the current range has the greatest sum among ranges ending at the
     * current index, with the earliest start on a tie. A strictly negative preceding sum can
     * only hurt a range, so discard it; a zero preceding sum preserves the preferred start.
     */
    public static Optional<Subarray> maximumSubarray(int[] values) {
        Objects.requireNonNull(values, "values");
        if (values.length == 0) {
            return Optional.empty();
        }
        long currentSum = 0;
        int currentStart = 0;
        Subarray best = null;
        for (int index = 0; index < values.length; index++) {
            if (currentSum < 0) {
                currentSum = 0;
                currentStart = index;
            }
            currentSum += values[index];
            if (best == null || currentSum > best.sum()
                    || (currentSum == best.sum()
                            && (currentStart < best.startInclusive()
                                    || (currentStart == best.startInclusive()
                                            && index + 1 < best.endExclusive())))) {
                best = new Subarray(currentStart, index + 1, currentSum);
            }
        }
        return Optional.of(best);
    }

    /**
     * Returns a new array containing the maximum of each consecutive full window.
     * A valid window satisfies {@code 1 <= window <= values.length}; empty input consequently
     * has no valid window. O(n) time and O(window) auxiliary space, excluding the result.
     *
     * <p>The deque stores unexpired indices with strictly decreasing values. A later value
     * at least as large dominates an earlier one for every future window containing both.
     * Each index enters and leaves at most once, giving the linear time bound.
     *
     * @throws IllegalArgumentException if window is outside the valid range
     */
    public static int[] slidingWindowMaximum(int[] values, int window) {
        Objects.requireNonNull(values, "values");
        if (window < 1 || window > values.length) {
            throw new IllegalArgumentException("window must be between 1 and the input length");
        }
        int[] maximums = new int[values.length - window + 1];
        Deque<Integer> candidates = new ArrayDeque<>();
        for (int index = 0; index < values.length; index++) {
            while (!candidates.isEmpty() && candidates.peekFirst() <= index - window) {
                candidates.removeFirst();
            }
            while (!candidates.isEmpty() && values[candidates.peekLast()] <= values[index]) {
                candidates.removeLast();
            }
            candidates.addLast(index);
            if (index >= window - 1) {
                maximums[index - window + 1] = values[candidates.peekFirst()];
            }
        }
        return maximums;
    }

    /**
     * Counts pairs {@code i < j} with {@code values[i] > values[j]}; equal values do not count.
     * Returns zero for an empty array. Input is copied before merging, so it is not modified.
     * O(n log n) time and O(n) auxiliary space. The largest possible int-array pair count fits
     * in a long, even when it exceeds the range of int.
     *
     * <p>During a merge, a smaller right element forms an inversion with every unmerged left
     * element. Counting those pairs when that right element is emitted counts each pair once.
     */
    public static long countInversions(int[] values) {
        Objects.requireNonNull(values, "values");
        return countInversions(values.clone(), new int[values.length], 0, values.length);
    }

    private static long countInversions(int[] values, int[] buffer, int from, int to) {
        if (to - from < 2) {
            return 0;
        }
        int middle = from + (to - from) / 2;
        long inversions = countInversions(values, buffer, from, middle)
                + countInversions(values, buffer, middle, to);
        int left = from;
        int right = middle;
        for (int index = from; index < to; index++) {
            if (left < middle && (right == to || values[left] <= values[right])) {
                buffer[index] = values[left++];
            } else {
                buffer[index] = values[right++];
                inversions += middle - left;
            }
        }
        System.arraycopy(buffer, from, values, from, to - from);
        return inversions;
    }
}
