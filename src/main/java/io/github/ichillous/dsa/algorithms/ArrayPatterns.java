package io.github.ichillous.dsa.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Classic linear-scan patterns over arrays, strings and intervals: two pointers, a variable
 * sliding window, monotonic stacks, difference arrays and interval sweeps.
 *
 * <p>All methods reject null arguments and null collection elements with
 * {@link NullPointerException} and never modify their inputs. Returned arrays and lists are
 * fresh copies or immutable. Index ranges are half-open {@code [from, to)}.
 */
public final class ArrayPatterns {
    private ArrayPatterns() {}

    /** Two distinct indices {@code first < second}, both non-negative. */
    public record IndexPair(int first, int second) {
        /** Rejects a negative first index or a second index that is not greater than the first. */
        public IndexPair {
            if (first < 0 || first >= second) {
                throw new IllegalArgumentException("an index pair needs 0 <= first < second");
            }
        }
    }

    /** Adds {@code delta} to every index in {@code [fromInclusive, toExclusive)}; may be empty. */
    public record RangeAdd(int fromInclusive, int toExclusive, long delta) {
        /** Rejects a negative start or an end before the start. */
        public RangeAdd {
            if (fromInclusive < 0 || fromInclusive > toExclusive) {
                throw new IllegalArgumentException("a range add needs 0 <= from <= to");
            }
        }
    }

    /**
     * Finds an index pair whose values sum to {@code target} in an ascending array.
     *
     * <p>Invariant: every index below {@code left} has no partner at any index above it, and every
     * index above {@code right} has no partner at any index below it. The result is the smallest
     * first index that has any partner, paired with that index's largest partner. Sums use
     * {@code long} so int values cannot overflow. Ascending order is an unchecked precondition (a
     * scan would cost as much as the search itself); an unsorted array gives an unspecified but
     * non-throwing answer. The input is not modified; an empty or singleton array gives empty.
     * O(n) time, O(1) extra space.
     */
    public static Optional<IndexPair> pairWithSum(int[] sorted, long target) {
        Objects.requireNonNull(sorted, "sorted");
        int left = 0;
        int right = sorted.length - 1;
        while (left < right) {
            long sum = (long) sorted[left] + sorted[right];
            if (sum == target) {
                return Optional.of(new IndexPair(left, right));
            }
            if (sum < target) {
                left++;
            } else {
                right--;
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the longest window of {@code text} with no repeated UTF-16 code unit, as a
     * half-open {@link StringAlgorithms.Span}, the same range type that
     * {@link StringAlgorithms#longestPalindromicSubstring} returns.
     *
     * <p>Invariant: {@code [start, i]} holds distinct units; on a repeat, {@code start} jumps just
     * past the previous occurrence. Ties go to the earliest start. The empty string gives
     * {@code Span(0, 0)}. Supplementary characters count as two units. O(n) expected time and
     * O(min(n, distinct units)) space.
     */
    public static StringAlgorithms.Span longestSubstringWithoutRepeats(String text) {
        Objects.requireNonNull(text, "text");
        Map<Character, Integer> lastSeen = new HashMap<>();
        int start = 0;
        int bestStart = 0;
        int bestLength = 0;
        for (int i = 0; i < text.length(); i++) {
            Integer previous = lastSeen.put(text.charAt(i), i);
            if (previous != null && previous >= start) {
                start = previous + 1;
            }
            if (i - start + 1 > bestLength) {
                bestStart = start;
                bestLength = i - start + 1;
            }
        }
        return new StringAlgorithms.Span(bestStart, bestStart + bestLength);
    }

    /**
     * For each index returns the index of the next strictly greater element to its right, or
     * {@code -1} if none. Equal values are not greater.
     *
     * <p>Invariant: the stack holds indices of values not yet resolved, with non-increasing
     * values from bottom to top. The input is not modified; empty gives an empty array.
     * O(n) time (each index is pushed and popped once), O(n) space.
     */
    public static int[] nextGreaterIndices(int[] values) {
        Objects.requireNonNull(values, "values");
        int[] result = new int[values.length];
        Arrays.fill(result, -1);
        int[] stack = new int[values.length];
        int top = 0;
        for (int i = 0; i < values.length; i++) {
            while (top > 0 && values[stack[top - 1]] < values[i]) {
                result[stack[--top]] = i;
            }
            stack[top++] = i;
        }
        return result;
    }

    /**
     * Returns the area of the largest axis-aligned rectangle in a histogram of unit-width bars.
     *
     * <p>Invariant: the stack holds bar indices with strictly increasing heights; when a bar is
     * popped, the current index is its right boundary and the new stack top its left boundary.
     * Heights must be non-negative, otherwise {@link IllegalArgumentException} is thrown before
     * any work. Area is a {@code long}: it is at most {@code n * Integer.MAX_VALUE}, which fits
     * for every array length. Empty gives 0. The input is not modified. O(n) time, O(n) space.
     */
    public static long largestRectangle(int[] heights) {
        Objects.requireNonNull(heights, "heights");
        for (int height : heights) {
            if (height < 0) {
                throw new IllegalArgumentException("heights must be non-negative");
            }
        }
        int n = heights.length;
        int[] stack = new int[n];
        int top = 0;
        long best = 0;
        for (int i = 0; i <= n; i++) {
            int current = i == n ? 0 : heights[i];
            while (top > 0 && heights[stack[top - 1]] >= current) {
                long height = heights[stack[--top]];
                int left = top == 0 ? 0 : stack[top - 1] + 1;
                best = Math.max(best, height * (i - left));
            }
            if (i < n) {
                stack[top++] = i;
            }
        }
        return best;
    }

    /**
     * Applies range additions with a difference array and returns a new array.
     *
     * <p>Each update adds {@code delta} at {@code from} and subtracts it at {@code to} in a
     * difference array; a running prefix sum then recovers every index. The difference entries
     * and the prefix are held in 128 bits, so the prefix may leave {@code long} range between
     * indices as long as every final value {@code values[i] + prefix} fits; otherwise
     * {@link ArithmeticException} is thrown. An update with {@code to > values.length} throws
     * {@link IndexOutOfBoundsException}, before any result is produced. Empty ranges are ignored.
     * The input array and list are not modified. O(n + u) time for {@code u} updates, O(n) space.
     */
    public static long[] applyRangeAdds(long[] values, List<RangeAdd> updates) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(updates, "updates");
        int n = values.length;
        long[] diffLow = new long[n + 1];
        long[] diffHigh = new long[n + 1];
        for (RangeAdd update : updates) {
            Objects.requireNonNull(update, "update");
            if (update.toExclusive() > n) {
                throw new IndexOutOfBoundsException(
                        "range end " + update.toExclusive() + " exceeds length " + n);
            }
            if (update.fromInclusive() == update.toExclusive()) {
                continue;
            }
            add128(diffLow, diffHigh, update.fromInclusive(), update.delta());
            sub128(diffLow, diffHigh, update.toExclusive(), update.delta());
        }
        long[] result = new long[n];
        long low = 0;
        long high = 0;
        for (int i = 0; i < n; i++) {
            long newLow = low + diffLow[i];
            long carry = Long.compareUnsigned(newLow, low) < 0 ? 1 : 0;
            high += diffHigh[i] + carry;
            low = newLow;
            // Final value = values[i] + (high:low). It fits in a long exactly when the 128-bit
            // sum sign-extends from its low word.
            long valueLow = low + values[i];
            long valueCarry = Long.compareUnsigned(valueLow, low) < 0 ? 1 : 0;
            long valueHigh = high + (values[i] >> 63) + valueCarry;
            if (valueHigh != (valueLow >> 63)) {
                throw new ArithmeticException("value at index " + i + " does not fit in a long");
            }
            result[i] = valueLow;
        }
        return result;
    }

    private static void add128(long[] lows, long[] highs, int index, long delta) {
        long newLow = lows[index] + delta;
        long carry = Long.compareUnsigned(newLow, lows[index]) < 0 ? 1 : 0;
        highs[index] += (delta >> 63) + carry;
        lows[index] = newLow;
    }

    private static void sub128(long[] lows, long[] highs, int index, long delta) {
        long newLow = lows[index] - delta;
        long borrow = Long.compareUnsigned(lows[index], delta) < 0 ? 1 : 0;
        highs[index] -= (delta >> 63) + borrow;
        lows[index] = newLow;
    }

    /**
     * Merges intervals that overlap or touch ({@code [1,3)} and {@code [3,5)} become
     * {@code [1,5)}).
     *
     * <p>Invariant: after sorting by start, the last output interval is the union of every input
     * seen so far that reaches it. Uses {@link GreedyAlgorithms.Interval}, so intervals are
     * non-empty. The result is immutable and sorted by start (disjoint, so also by end); empty
     * input gives an empty list. The input collection is not modified. O(n log n) time, O(n)
     * space.
     */
    public static List<GreedyAlgorithms.Interval> mergeIntervals(
            Collection<GreedyAlgorithms.Interval> intervals) {
        List<GreedyAlgorithms.Interval> ordered = copyNonNull(intervals);
        ordered.sort(Comparator.comparingInt(GreedyAlgorithms.Interval::start)
                .thenComparingInt(GreedyAlgorithms.Interval::end));
        List<GreedyAlgorithms.Interval> merged = new ArrayList<>();
        for (GreedyAlgorithms.Interval interval : ordered) {
            int last = merged.size() - 1;
            if (last >= 0 && interval.start() <= merged.get(last).end()) {
                GreedyAlgorithms.Interval open = merged.get(last);
                if (interval.end() > open.end()) {
                    merged.set(last, new GreedyAlgorithms.Interval(open.start(), interval.end()));
                }
            } else {
                merged.add(interval);
            }
        }
        return List.copyOf(merged);
    }

    /**
     * Returns the minimum number of rooms so no two overlapping intervals share one, which equals
     * the maximum number of intervals covering a single point. Touching intervals
     * ({@code [1,2)} and {@code [2,3)}) do not conflict. Empty input gives 0.
     *
     * <p>Sorts starts and ends separately and sweeps them; at equal times ends are processed
     * first. The input is not modified. O(n log n) time, O(n) space.
     */
    public static int minimumMeetingRooms(Collection<GreedyAlgorithms.Interval> intervals) {
        List<GreedyAlgorithms.Interval> checked = copyNonNull(intervals);
        int n = checked.size();
        int[] starts = new int[n];
        int[] ends = new int[n];
        for (int i = 0; i < n; i++) {
            starts[i] = checked.get(i).start();
            ends[i] = checked.get(i).end();
        }
        Arrays.sort(starts);
        Arrays.sort(ends);
        int rooms = 0;
        int depth = 0;
        int endIndex = 0;
        for (int start : starts) {
            while (ends[endIndex] <= start) {
                endIndex++;
                depth--;
            }
            depth++;
            rooms = Math.max(rooms, depth);
        }
        return rooms;
    }

    private static List<GreedyAlgorithms.Interval> copyNonNull(
            Collection<GreedyAlgorithms.Interval> intervals) {
        Objects.requireNonNull(intervals, "intervals");
        List<GreedyAlgorithms.Interval> copy = new ArrayList<>(intervals.size());
        for (GreedyAlgorithms.Interval interval : intervals) {
            copy.add(Objects.requireNonNull(interval, "interval"));
        }
        return copy;
    }
}
