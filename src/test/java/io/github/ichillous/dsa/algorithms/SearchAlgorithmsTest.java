package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Random;
import java.util.function.LongPredicate;
import org.junit.jupiter.api.Test;

class SearchAlgorithmsTest {
    @Test
    void handlesEmptyAndSingletonArrays() {
        assertEquals(-1, SearchAlgorithms.linearSearch(new int[0], 4));
        assertEquals(-1, SearchAlgorithms.binarySearch(new int[0], 4));
        assertEquals(0, SearchAlgorithms.lowerBound(new int[0], 4));
        assertEquals(0, SearchAlgorithms.upperBound(new int[0], 4));
        assertEquals(0, SearchAlgorithms.binarySearch(new int[] {4}, 4));
        assertEquals(-1, SearchAlgorithms.binarySearch(new int[] {4}, 5));
        assertEquals(1, SearchAlgorithms.lowerBound(new int[] {4}, 5));
        assertEquals(1, SearchAlgorithms.upperBound(new int[] {4}, 4));
    }

    @Test
    void findsFirstDuplicateAndExtremeValues() {
        int[] sorted = {Integer.MIN_VALUE, -1, -1, -1, 4, Integer.MAX_VALUE};
        assertEquals(1, SearchAlgorithms.binarySearch(sorted, -1));
        assertEquals(1, SearchAlgorithms.lowerBound(sorted, -1));
        assertEquals(4, SearchAlgorithms.upperBound(sorted, -1));
        assertEquals(0, SearchAlgorithms.binarySearch(sorted, Integer.MIN_VALUE));
        assertEquals(5, SearchAlgorithms.binarySearch(sorted, Integer.MAX_VALUE));
        assertEquals(6, SearchAlgorithms.upperBound(sorted, Integer.MAX_VALUE));
    }

    @Test
    void linearSearchDoesNotRequireSortedInput() {
        assertEquals(1, SearchAlgorithms.linearSearch(new int[] {9, 2, 7, 2}, 2));
        assertEquals(-1, SearchAlgorithms.linearSearch(new int[] {9, 2, 7, 2}, 4));
    }

    @Test
    void searchesMatchIndependentScanAcrossSeededInputs() {
        Random random = new Random(0x5EA2C4);
        for (int trial = 0; trial < 1000; trial++) {
            int[] values = random.ints(random.nextInt(100), -20, 21).toArray();
            Arrays.sort(values);
            int target = random.nextInt(51) - 25;
            int lower = 0;
            while (lower < values.length && values[lower] < target) {
                lower++;
            }
            int upper = lower;
            while (upper < values.length && values[upper] == target) {
                upper++;
            }
            int expected = lower < upper ? lower : -1;
            assertEquals(lower, SearchAlgorithms.lowerBound(values, target), "trial " + trial);
            assertEquals(upper, SearchAlgorithms.upperBound(values, target), "trial " + trial);
            assertEquals(expected, SearchAlgorithms.binarySearch(values, target), "trial " + trial);
            assertEquals(expected, SearchAlgorithms.linearSearch(values, target), "trial " + trial);
        }
    }

    @Test
    void rejectsNullArrays() {
        assertThrows(NullPointerException.class, () -> SearchAlgorithms.linearSearch(null, 0));
        assertThrows(NullPointerException.class, () -> SearchAlgorithms.binarySearch(null, 0));
        assertThrows(NullPointerException.class, () -> SearchAlgorithms.lowerBound(null, 0));
        assertThrows(NullPointerException.class, () -> SearchAlgorithms.upperBound(null, 0));
    }

    /** The documented bound on predicate calls: one per halving of the full {@code long} range. */
    private static final int MAX_CALLS = 64;

    /**
     * A monotone predicate that is true from {@code threshold} on, recording every call. It fails
     * on call 65, so a search that does not converge (for example after a midpoint overflow) fails
     * at once instead of spinning.
     */
    private static final class Probe implements LongPredicate {
        private final long threshold;
        private final long low;
        private final long high;
        private int calls;
        private boolean outside;

        Probe(long threshold, long low, long high) {
            this.threshold = threshold;
            this.low = low;
            this.high = high;
        }

        @Override
        public boolean test(long value) {
            if (++calls > MAX_CALLS) {
                throw new AssertionError("more than " + MAX_CALLS + " predicate calls; last value " + value);
            }
            outside |= value < low || value >= high;
            return value >= threshold;
        }
    }

    @Test
    void firstTrueMatchesBruteForceOnSeededThresholds() {
        Random random = new Random(0xF1257);
        for (int trial = 0; trial < 2000; trial++) {
            long low = random.nextInt(61) - 30;
            long high = low + random.nextInt(40);
            long threshold = random.nextInt(100) - 50;
            long expected = high;
            for (long value = low; value < high; value++) {
                if (value >= threshold) {
                    expected = value;
                    break;
                }
            }
            Probe probe = new Probe(threshold, low, high);
            assertEquals(expected, SearchAlgorithms.firstTrue(low, high, probe),
                    "trial " + trial + " range [" + low + ", " + high + ") threshold " + threshold);
            assertTrue(!probe.outside, "predicate called outside the range, trial " + trial);
        }
    }

    @Test
    void firstTrueHandlesEmptyAllFalseAndAllTrueRanges() {
        Probe empty = new Probe(0, 5, 5);
        assertEquals(5, SearchAlgorithms.firstTrue(5, 5, empty));
        assertEquals(0, empty.calls, "an empty range never calls the predicate");
        assertEquals(10, SearchAlgorithms.firstTrue(3, 10, constant(false, 3, 10)));
        assertEquals(3, SearchAlgorithms.firstTrue(3, 10, constant(true, 3, 10)));
        assertEquals(-7, SearchAlgorithms.firstTrue(-7, -7, constant(true, -7, -7)));
        assertEquals(-4, SearchAlgorithms.firstTrue(-9, 0, new Probe(-4, -9, 0)));
        assertEquals(6, SearchAlgorithms.firstTrue(5, 7, new Probe(6, 5, 7)));
    }

    @Test
    void firstTrueUsesLogarithmicallyManyCallsInsideTheRange() {
        Probe probe = new Probe(700_000, 0, 1_000_000);
        assertEquals(700_000, SearchAlgorithms.firstTrue(0, 1_000_000, probe));
        assertTrue(probe.calls <= 20, "calls: " + probe.calls);
        assertTrue(!probe.outside);
    }

    /** Probe that is constantly false or true over {@code [low, high)}. */
    private static Probe constant(boolean result, long low, long high) {
        // Values in the range are below Long.MAX_VALUE, so that threshold is never reached.
        return new Probe(result ? Long.MIN_VALUE : Long.MAX_VALUE, low, high);
    }

    // An overflowing midpoint probes outside the range or never converges; Probe fails on call 65.
    @Test
    void firstTrueDoesNotOverflowAtLongExtremes() {
        long min = Long.MIN_VALUE;
        long max = Long.MAX_VALUE;
        // high - low exceeds Long.MAX_VALUE here, so a signed midpoint would overflow.
        long[] thresholds = {min, min + 1, -1, 0, 1, max - 1, max};
        for (long threshold : thresholds) {
            Probe probe = new Probe(threshold, min, max);
            assertEquals(threshold, SearchAlgorithms.firstTrue(min, max, probe), "threshold " + threshold);
            assertTrue(!probe.outside, "predicate called outside the range, threshold " + threshold);
            assertTrue(probe.calls <= 64, "calls: " + probe.calls);
        }
        assertEquals(max, SearchAlgorithms.firstTrue(min, max, constant(false, min, max)));
        assertEquals(min, SearchAlgorithms.firstTrue(min, max, constant(true, min, max)));
        assertEquals(max, SearchAlgorithms.firstTrue(max - 1, max, constant(false, max - 1, max)));
        assertEquals(max - 1, SearchAlgorithms.firstTrue(max - 1, max, constant(true, max - 1, max)));
        assertEquals(min, SearchAlgorithms.firstTrue(min, min + 1, constant(true, min, min + 1)));
        assertEquals(min + 1, SearchAlgorithms.firstTrue(min, min + 1, constant(false, min, min + 1)));
    }

    // Near Long.MAX_VALUE, low + high wraps negative: (low + high) / 2 would leave the range.
    @Test
    void firstTrueDoesNotOverflowNearLongMaxValue() {
        long max = Long.MAX_VALUE;
        long low = max - 1_000_000L;
        for (long threshold : new long[] {low, low + 1, max - 500_000L, max - 1, max}) {
            Probe probe = new Probe(threshold, low, max);
            assertEquals(threshold, SearchAlgorithms.firstTrue(low, max, probe), "threshold " + threshold);
            assertTrue(!probe.outside, "predicate called outside the range, threshold " + threshold);
        }
    }

    @Test
    void firstTrueRejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> SearchAlgorithms.firstTrue(1, 0, value -> true));
        assertThrows(IllegalArgumentException.class,
                () -> SearchAlgorithms.firstTrue(Long.MAX_VALUE, Long.MIN_VALUE, value -> true));
        assertThrows(NullPointerException.class, () -> SearchAlgorithms.firstTrue(0, 1, null));
        assertThrows(NullPointerException.class, () -> SearchAlgorithms.firstTrue(0, 0, null));
    }
}
