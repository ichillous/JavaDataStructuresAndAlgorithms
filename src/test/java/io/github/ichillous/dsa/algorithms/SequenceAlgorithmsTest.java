package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.ichillous.dsa.algorithms.SequenceAlgorithms.Subarray;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SequenceAlgorithmsTest {
    @Test
    void maximumSubarrayHandlesEmptyNegativeAndMixedInputs() {
        assertEquals(Optional.empty(), SequenceAlgorithms.maximumSubarray(new int[0]));
        assertEquals(Optional.of(new Subarray(0, 1, -7)),
                SequenceAlgorithms.maximumSubarray(new int[] {-7}));
        assertEquals(Optional.of(new Subarray(1, 2, -2)),
                SequenceAlgorithms.maximumSubarray(new int[] {-8, -2, -5}));
        assertEquals(Optional.of(new Subarray(3, 7, 6)),
                SequenceAlgorithms.maximumSubarray(new int[] {-2, 1, -3, 4, -1, 2, 1, -5, 4}));
    }

    @Test
    void maximumSubarrayUsesEarliestStartThenEarliestEndForTies() {
        assertEquals(Optional.of(new Subarray(0, 1, 0)),
                SequenceAlgorithms.maximumSubarray(new int[] {0, 0, 0}));
        assertEquals(Optional.of(new Subarray(0, 1, 1)),
                SequenceAlgorithms.maximumSubarray(new int[] {1, -1, 1}));
        assertEquals(Optional.of(new Subarray(1, 3, 1)),
                SequenceAlgorithms.maximumSubarray(new int[] {-1, 0, 1}));
        assertEquals(Optional.of(new Subarray(0, 1, -1)),
                SequenceAlgorithms.maximumSubarray(new int[] {-1, -1}));
    }

    @Test
    void maximumSubarrayAccumulatesAsLong() {
        assertEquals(Optional.of(new Subarray(0, 2, 2L * Integer.MAX_VALUE)),
                SequenceAlgorithms.maximumSubarray(new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE}));
        assertEquals(Optional.of(new Subarray(0, 1, Integer.MIN_VALUE)),
                SequenceAlgorithms.maximumSubarray(new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE}));
    }

    @Test
    void maximumSubarrayMatchesExhaustiveRangesWithoutMutation() {
        long seed = 0x4AD4AE;
        Random random = new Random(seed);
        for (int trial = 0; trial < 700; trial++) {
            int[] values = random.ints(random.nextInt(21), -10, 11).toArray();
            int[] original = values.clone();
            Subarray expected = null;
            for (int start = 0; start < values.length; start++) {
                long sum = 0;
                for (int end = start + 1; end <= values.length; end++) {
                    sum += values[end - 1];
                    // Enumeration already visits tied ranges in preferred start/end order.
                    if (expected == null || sum > expected.sum()) {
                        expected = new Subarray(start, end, sum);
                    }
                }
            }
            assertEquals(Optional.ofNullable(expected), SequenceAlgorithms.maximumSubarray(values),
                    "seed=" + seed + ", trial=" + trial);
            assertArrayEquals(original, values);
        }
    }

    @Test
    void slidingWindowHandlesDuplicatesExtremesAndBoundaryWindows() {
        int[] values = {1, 3, -1, -3, 5, 3, 6, 7};
        assertArrayEquals(new int[] {3, 3, 5, 5, 6, 7},
                SequenceAlgorithms.slidingWindowMaximum(values, 3));
        assertArrayEquals(values, SequenceAlgorithms.slidingWindowMaximum(values, 1));
        assertArrayEquals(new int[] {7}, SequenceAlgorithms.slidingWindowMaximum(values, values.length));
        assertArrayEquals(new int[] {4, 4, 4},
                SequenceAlgorithms.slidingWindowMaximum(new int[] {4, 4, 4, 4}, 2));
        assertArrayEquals(new int[] {Integer.MAX_VALUE}, SequenceAlgorithms.slidingWindowMaximum(
                new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE}, 2));
        assertArrayEquals(new int[] {5, 4, 3},
                SequenceAlgorithms.slidingWindowMaximum(new int[] {5, 4, 3, 2}, 2));
    }

    @Test
    void slidingWindowMatchesIndependentScanAndReturnsFreshStorage() {
        long seed = 0x51D1A6;
        Random random = new Random(seed);
        for (int trial = 0; trial < 700; trial++) {
            int[] values = random.ints(random.nextInt(60) + 1, -20, 21).toArray();
            int[] original = values.clone();
            int window = random.nextInt(values.length) + 1;
            int[] expected = new int[values.length - window + 1];
            for (int start = 0; start < expected.length; start++) {
                int maximum = Integer.MIN_VALUE;
                for (int index = start; index < start + window; index++) {
                    maximum = Math.max(maximum, values[index]);
                }
                expected[start] = maximum;
            }
            int[] actual = SequenceAlgorithms.slidingWindowMaximum(values, window);
            assertArrayEquals(expected, actual, "seed=" + seed + ", trial=" + trial);
            actual[0] = 123_456;
            assertArrayEquals(original, values);
            assertArrayEquals(expected, SequenceAlgorithms.slidingWindowMaximum(values, window));
        }
    }

    @Test
    void inversionCountExcludesDuplicatesAndHandlesExtremeValues() {
        assertEquals(0, SequenceAlgorithms.countInversions(new int[0]));
        assertEquals(0, SequenceAlgorithms.countInversions(new int[] {1}));
        assertEquals(0, SequenceAlgorithms.countInversions(new int[] {1, 1, 1}));
        assertEquals(3, SequenceAlgorithms.countInversions(new int[] {3, 2, 1}));
        assertEquals(2, SequenceAlgorithms.countInversions(new int[] {2, 1, 1}));
        assertEquals(2, SequenceAlgorithms.countInversions(
                new int[] {Integer.MAX_VALUE, Integer.MIN_VALUE, 0, Integer.MAX_VALUE}));
    }

    @Test
    void inversionCountCanExceedIntRange() {
        int size = 100_000;
        int[] descending = new int[size];
        for (int index = 0; index < size; index++) {
            descending[index] = size - index;
        }
        assertEquals((long) size * (size - 1) / 2, SequenceAlgorithms.countInversions(descending));
        assertEquals(size, descending[0]);
        assertEquals(1, descending[size - 1]);
    }

    @Test
    void inversionCountMatchesQuadraticOracleWithoutMutation() {
        long seed = 0x1A4E25;
        Random random = new Random(seed);
        for (int trial = 0; trial < 700; trial++) {
            int[] values = random.ints(random.nextInt(50), -12, 13).toArray();
            int[] original = values.clone();
            long expected = 0;
            for (int first = 0; first < values.length; first++) {
                for (int second = first + 1; second < values.length; second++) {
                    if (values[first] > values[second]) {
                        expected++;
                    }
                }
            }
            assertEquals(expected, SequenceAlgorithms.countInversions(values),
                    "seed=" + seed + ", trial=" + trial);
            assertArrayEquals(original, values);
        }
    }

    @Test
    void rejectsInvalidWindowsRangesAndNulls() {
        assertThrows(NullPointerException.class, () -> SequenceAlgorithms.maximumSubarray(null));
        assertThrows(NullPointerException.class, () -> SequenceAlgorithms.slidingWindowMaximum(null, 1));
        assertThrows(NullPointerException.class, () -> SequenceAlgorithms.countInversions(null));
        assertThrows(IllegalArgumentException.class,
                () -> SequenceAlgorithms.slidingWindowMaximum(new int[0], 0));
        assertThrows(IllegalArgumentException.class,
                () -> SequenceAlgorithms.slidingWindowMaximum(new int[0], 1));
        assertThrows(IllegalArgumentException.class,
                () -> SequenceAlgorithms.slidingWindowMaximum(new int[] {1}, -1));
        assertThrows(IllegalArgumentException.class,
                () -> SequenceAlgorithms.slidingWindowMaximum(new int[] {1}, 0));
        assertThrows(IllegalArgumentException.class,
                () -> SequenceAlgorithms.slidingWindowMaximum(new int[] {1}, 2));
        assertThrows(IllegalArgumentException.class, () -> new Subarray(-1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Subarray(1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Subarray(2, 1, 0));
    }
}
