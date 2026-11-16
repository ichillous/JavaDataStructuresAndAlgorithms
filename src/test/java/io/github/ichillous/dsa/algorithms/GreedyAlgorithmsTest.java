package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.ichillous.dsa.algorithms.GreedyAlgorithms.Interval;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GreedyAlgorithmsTest {
    @Test
    void earliestFinishBeatsChoosingEarliestStartAndAllowsTouching() {
        Interval longInterval = new Interval(0, 10);
        Interval first = new Interval(1, 3);
        Interval second = new Interval(3, 5);
        Interval third = new Interval(5, 6);
        assertEquals(List.of(first, second, third),
                GreedyAlgorithms.selectActivities(List.of(longInterval, third, second, first)));
        assertEquals(List.of(), GreedyAlgorithms.selectActivities(List.of()));
    }

    @Test
    void handlesNegativeAndExtremeEndpointsAndDuplicateActivities() {
        Interval first = new Interval(Integer.MIN_VALUE, -1);
        Interval second = new Interval(-1, Integer.MAX_VALUE);
        assertEquals(List.of(first, second),
                GreedyAlgorithms.selectActivities(List.of(second, first, first)));
    }

    @Test
    void resultIsImmutableAndInputOrderDoesNotChange() {
        List<Interval> intervals = new ArrayList<>(List.of(new Interval(4, 6), new Interval(0, 2)));
        List<Interval> original = List.copyOf(intervals);
        List<Interval> result = GreedyAlgorithms.selectActivities(intervals);
        assertEquals(original, intervals);
        assertThrows(UnsupportedOperationException.class, () -> result.add(new Interval(7, 8)));
    }

    @Test
    void scheduleCardinalityAgreesWithExhaustiveCompatibleSubsets() {
        Random random = new Random(0x622ED);
        for (int trial = 0; trial < 300; trial++) {
            List<Interval> intervals = new ArrayList<>();
            int size = random.nextInt(11);
            for (int index = 0; index < size; index++) {
                int start = random.nextInt(20) - 10;
                intervals.add(new Interval(start, start + random.nextInt(8) + 1));
            }
            int expected = 0;
            for (int mask = 0; mask < (1 << size); mask++) {
                boolean compatible = true;
                for (int first = 0; first < size && compatible; first++) {
                    if ((mask & (1 << first)) == 0) {
                        continue;
                    }
                    for (int second = first + 1; second < size; second++) {
                        if ((mask & (1 << second)) != 0
                                && overlaps(intervals.get(first), intervals.get(second))) {
                            compatible = false;
                            break;
                        }
                    }
                }
                if (compatible) {
                    expected = Math.max(expected, Integer.bitCount(mask));
                }
            }
            List<Interval> selected = GreedyAlgorithms.selectActivities(intervals);
            assertEquals(expected, selected.size(), "trial " + trial);
            for (int index = 0; index < selected.size(); index++) {
                assertTrue(intervals.contains(selected.get(index)));
                if (index > 0) {
                    assertTrue(selected.get(index - 1).end() <= selected.get(index).start());
                }
            }
        }
    }

    @Test
    void rejectsEmptyBackwardsAndNullIntervals() {
        assertThrows(IllegalArgumentException.class, () -> new Interval(1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Interval(4, 1));
        assertThrows(NullPointerException.class, () -> GreedyAlgorithms.selectActivities(null));
        assertThrows(NullPointerException.class,
                () -> GreedyAlgorithms.selectActivities(Arrays.asList(new Interval(0, 1), null)));
    }

    @Test
    void huffmanLengthsAreOptimal() {
        long[] classic = {5, 9, 12, 13, 16, 45};
        int[] lengths = GreedyAlgorithms.huffmanCodeLengths(classic);
        assertArrayEquals(new int[] {4, 4, 3, 3, 3, 1}, lengths);
        assertEquals(224, weightedLength(classic, lengths));
        long seed = 0x4AFF3L;
        Random random = new Random(seed);
        for (int trial = 0; trial < 300; trial++) {
            long[] frequencies = new long[1 + random.nextInt(6)];
            for (int index = 0; index < frequencies.length; index++) {
                frequencies[index] = random.nextInt(4) == 0 ? 0 : random.nextInt(20) + 1;
            }
            long[] copy = frequencies.clone();
            int[] actual = GreedyAlgorithms.huffmanCodeLengths(frequencies);
            String message = "seed " + seed + " trial " + trial + " " + Arrays.toString(copy);
            assertArrayEquals(copy, frequencies, message);
            assertEquals(bruteOptimalLength(frequencies), weightedLength(frequencies, actual), message);
            int used = 0;
            double kraft = 0;
            for (int index = 0; index < frequencies.length; index++) {
                assertEquals(frequencies[index] == 0, actual[index] == 0, message);
                if (actual[index] > 0) {
                    used++;
                    kraft += Math.pow(2, -actual[index]);
                }
            }
            if (used >= 2) {
                assertEquals(1.0, kraft, 0.0, message);
            }
        }
    }

    private static long weightedLength(long[] frequencies, int[] lengths) {
        long total = 0;
        for (int index = 0; index < frequencies.length; index++) {
            total += frequencies[index] * lengths[index];
        }
        return total;
    }

    /** Minimum weighted length over every length vector satisfying the Kraft inequality. */
    private static long bruteOptimalLength(long[] frequencies) {
        List<Long> used = new ArrayList<>();
        for (long frequency : frequencies) {
            if (frequency > 0) {
                used.add(frequency);
            }
        }
        if (used.isEmpty()) {
            return 0;
        }
        if (used.size() == 1) {
            return used.get(0);
        }
        return bestAssignment(used, 0, new int[used.size()]);
    }

    private static long bestAssignment(List<Long> used, int index, int[] lengths) {
        if (index == lengths.length) {
            long kraftNumerator = 0;
            for (int length : lengths) {
                kraftNumerator += 1L << (lengths.length - length);
            }
            if (kraftNumerator > (1L << lengths.length)) {
                return Long.MAX_VALUE;
            }
            long total = 0;
            for (int position = 0; position < lengths.length; position++) {
                total += used.get(position) * lengths[position];
            }
            return total;
        }
        long best = Long.MAX_VALUE;
        for (int length = 1; length < lengths.length; length++) {
            lengths[index] = length;
            best = Math.min(best, bestAssignment(used, index + 1, lengths));
        }
        return best;
    }

    @Test
    void huffmanEdgeCasesTiesAndOverflow() {
        assertArrayEquals(new int[0], GreedyAlgorithms.huffmanCodeLengths(new long[0]));
        assertArrayEquals(new int[] {0, 0, 0}, GreedyAlgorithms.huffmanCodeLengths(new long[3]));
        assertArrayEquals(new int[] {0, 1, 0},
                GreedyAlgorithms.huffmanCodeLengths(new long[] {0, 7, 0}));
        assertArrayEquals(new int[] {1}, GreedyAlgorithms.huffmanCodeLengths(new long[] {Long.MAX_VALUE}));
        assertArrayEquals(new int[] {1, 1}, GreedyAlgorithms.huffmanCodeLengths(new long[] {3, 3}));
        // Four equal weights: leaves merge in index order, giving a balanced tree.
        assertArrayEquals(new int[] {2, 2, 2, 2},
                GreedyAlgorithms.huffmanCodeLengths(new long[] {1, 1, 1, 1}));
        // Tie rule: leaf 2 (weight 2) is taken before the earlier-created internal node (weight 2).
        assertArrayEquals(new int[] {3, 3, 2, 1},
                GreedyAlgorithms.huffmanCodeLengths(new long[] {1, 1, 2, 4}));
        assertArrayEquals(new int[] {2, 2, 2, 2, 0},
                GreedyAlgorithms.huffmanCodeLengths(new long[] {2, 2, 2, 2, 0}));
        assertThrows(IllegalArgumentException.class,
                () -> GreedyAlgorithms.huffmanCodeLengths(new long[] {1, -1}));
        assertThrows(NullPointerException.class, () -> GreedyAlgorithms.huffmanCodeLengths(null));
        assertThrows(ArithmeticException.class,
                () -> GreedyAlgorithms.huffmanCodeLengths(new long[] {Long.MAX_VALUE, 1}));
        assertThrows(ArithmeticException.class, () -> GreedyAlgorithms.huffmanCodeLengths(
                new long[] {Long.MAX_VALUE, Long.MAX_VALUE, 1}));
        // Total exactly Long.MAX_VALUE is fine.
        assertArrayEquals(new int[] {1, 1},
                GreedyAlgorithms.huffmanCodeLengths(new long[] {Long.MAX_VALUE - 1, 1}));
    }

    @Test
    void huffmanSkewedFrequenciesGiveDeepTreesWithoutRecursion() {
        // Fibonacci weights force a maximally deep tree.
        long[] frequencies = new long[60];
        frequencies[0] = 1;
        frequencies[1] = 1;
        for (int index = 2; index < frequencies.length; index++) {
            frequencies[index] = frequencies[index - 1] + frequencies[index - 2];
        }
        int[] lengths = GreedyAlgorithms.huffmanCodeLengths(frequencies);
        assertEquals(59, lengths[0]);
        assertEquals(59, lengths[1]);
        assertEquals(1, lengths[59]);
        assertEquals(60, GreedyAlgorithms.canonicalCodes(lengths).stream().distinct().count());
    }

    @Test
    void canonicalCodesArePrefixFree() {
        assertEquals(List.of("00", "01", "10", "110", "111"),
                GreedyAlgorithms.canonicalCodes(new int[] {2, 2, 2, 3, 3}));
        assertEquals(List.of("110", "111", "10", "0"),
                GreedyAlgorithms.canonicalCodes(new int[] {3, 3, 2, 1}));
        assertEquals(List.of("", "0", ""), GreedyAlgorithms.canonicalCodes(new int[] {0, 1, 0}));
        assertEquals(List.of(), GreedyAlgorithms.canonicalCodes(new int[0]));
        assertEquals(List.of(""), GreedyAlgorithms.canonicalCodes(new int[] {0}));
        // Incomplete codes are allowed.
        assertEquals(List.of("00", "01", "10"), GreedyAlgorithms.canonicalCodes(new int[] {2, 2, 2}));
        assertThrows(IllegalArgumentException.class,
                () -> GreedyAlgorithms.canonicalCodes(new int[] {1, 1, 1}));
        assertThrows(IllegalArgumentException.class,
                () -> GreedyAlgorithms.canonicalCodes(new int[] {1, 2, 2, 2}));
        assertThrows(IllegalArgumentException.class,
                () -> GreedyAlgorithms.canonicalCodes(new int[] {1, -1}));
        assertThrows(IllegalArgumentException.class,
                () -> GreedyAlgorithms.canonicalCodes(new int[] {1, Integer.MAX_VALUE, 1, 1}));
        assertEquals(List.of("0", "1"),
                GreedyAlgorithms.canonicalCodes(new int[] {1, 1}));
        assertEquals(List.of("0", "1" + "0".repeat(39)),
                GreedyAlgorithms.canonicalCodes(new int[] {1, 40}));
        int[] deep = new int[71];
        for (int index = 0; index < 70; index++) {
            deep[index] = index + 1;
        }
        deep[70] = 70;
        List<String> deepCodes = GreedyAlgorithms.canonicalCodes(deep);
        assertEquals("0", deepCodes.get(0));
        assertEquals("1".repeat(69) + "0", deepCodes.get(69));
        assertEquals("1".repeat(70), deepCodes.get(70));
        assertThrows(NullPointerException.class, () -> GreedyAlgorithms.canonicalCodes(null));
        assertThrows(UnsupportedOperationException.class,
                () -> GreedyAlgorithms.canonicalCodes(new int[] {1}).add("x"));

        long seed = 0xCA0EL;
        Random random = new Random(seed);
        for (int trial = 0; trial < 200; trial++) {
            long[] frequencies = new long[random.nextInt(12)];
            for (int index = 0; index < frequencies.length; index++) {
                frequencies[index] = random.nextInt(3) == 0 ? 0 : random.nextInt(50) + 1;
            }
            int[] lengths = GreedyAlgorithms.huffmanCodeLengths(frequencies);
            int[] copy = lengths.clone();
            List<String> codes = GreedyAlgorithms.canonicalCodes(lengths);
            String message = "seed " + seed + " trial " + trial + " " + Arrays.toString(lengths);
            assertArrayEquals(copy, lengths, message);
            assertEquals(lengths.length, codes.size(), message);
            for (int first = 0; first < codes.size(); first++) {
                assertEquals(lengths[first], codes.get(first).length(), message);
                for (int second = 0; second < codes.size(); second++) {
                    if (first != second && lengths[first] > 0 && lengths[second] > 0) {
                        assertTrue(!codes.get(second).startsWith(codes.get(first)), message);
                    }
                }
            }
            // Canonical rule: among equal lengths, codes increase with the symbol index.
            for (int first = 0; first < codes.size(); first++) {
                for (int second = first + 1; second < codes.size(); second++) {
                    if (lengths[first] > 0 && lengths[first] == lengths[second]) {
                        assertTrue(codes.get(first).compareTo(codes.get(second)) < 0, message);
                    }
                }
            }
        }
    }

    private static boolean overlaps(Interval first, Interval second) {
        return first.start() < second.end() && second.start() < first.end();
    }
}
