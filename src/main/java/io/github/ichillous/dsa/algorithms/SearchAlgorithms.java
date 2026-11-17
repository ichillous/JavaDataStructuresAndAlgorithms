package io.github.ichillous.dsa.algorithms;

import java.util.Objects;
import java.util.function.LongPredicate;

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

    /**
     * Returns the smallest value in {@code [lowInclusive, highExclusive)} for which
     * {@code monotone} is true, or {@code highExclusive} when it is false everywhere (including
     * for an empty range).
     *
     * <p>Precondition, not checked: over the range the predicate is false for a prefix and then
     * true for the rest, so it changes at most once. Invariant: every value below {@code low} is
     * false and every value at or above {@code high} is true or is the original upper bound; only
     * {@code [low, high)} is undecided. If the predicate is not monotone the returned boundary is
     * unspecified, though it is still a value in {@code [lowInclusive, highExclusive]}.
     *
     * <p>The predicate is called O(log(highExclusive - lowInclusive)) times, once per halving
     * of the range (at most 64 calls for the full {@code long} range), and never with a value
     * outside the range. The midpoint is {@code low + ((high - low) >>> 1)}: the difference can
     * exceed {@link Long#MAX_VALUE} (for example for {@code [Long.MIN_VALUE, Long.MAX_VALUE)}),
     * but read as an unsigned 64-bit number it is exact, so the midpoint never overflows for any
     * bounds. Space O(1).
     *
     * @param lowInclusive the smallest candidate
     * @param highExclusive one past the largest candidate; also the result when none qualifies
     * @param monotone predicate that is false then true; must not be null
     * @return the first value for which the predicate holds, or {@code highExclusive}
     * @throws NullPointerException if {@code monotone} is null
     * @throws IllegalArgumentException if {@code lowInclusive > highExclusive}
     */
    public static long firstTrue(long lowInclusive, long highExclusive, LongPredicate monotone) {
        Objects.requireNonNull(monotone, "monotone");
        if (lowInclusive > highExclusive) {
            throw new IllegalArgumentException("lowInclusive must not exceed highExclusive");
        }
        long low = lowInclusive;
        long high = highExclusive;
        while (low < high) {
            long middle = low + ((high - low) >>> 1);
            if (monotone.test(middle)) {
                high = middle;
            } else {
                low = middle + 1;
            }
        }
        return low;
    }
}
