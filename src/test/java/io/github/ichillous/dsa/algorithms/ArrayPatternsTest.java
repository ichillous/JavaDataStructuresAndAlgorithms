package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.ichillous.dsa.algorithms.ArrayPatterns.IndexPair;
import io.github.ichillous.dsa.algorithms.ArrayPatterns.RangeAdd;
import io.github.ichillous.dsa.algorithms.GreedyAlgorithms.Interval;
import io.github.ichillous.dsa.algorithms.StringAlgorithms.Span;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ArrayPatternsTest {
    private static Interval iv(int start, int end) {
        return new Interval(start, end);
    }

    @Test
    void recordsValidateTheirBounds() {
        assertThrows(IllegalArgumentException.class, () -> new IndexPair(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> new IndexPair(2, 2));
        assertEquals(1, new IndexPair(0, 1).second());
        assertThrows(IllegalArgumentException.class, () -> new RangeAdd(-1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new RangeAdd(2, 1, 1));
        assertEquals(7L, new RangeAdd(1, 1, 7L).delta());
    }

    @Test
    void pairWithSumMatchesBruteForceRule() {
        assertEquals(Optional.of(new IndexPair(0, 3)),
                ArrayPatterns.pairWithSum(new int[] {1, 2, 3, 4}, 5));
        assertEquals(Optional.empty(), ArrayPatterns.pairWithSum(new int[0], 0));
        assertEquals(Optional.empty(), ArrayPatterns.pairWithSum(new int[] {2}, 4));
        assertEquals(Optional.empty(), ArrayPatterns.pairWithSum(new int[] {1, 2, 4}, 100));
        assertEquals(Optional.of(new IndexPair(0, 1)),
                ArrayPatterns.pairWithSum(new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE},
                        2L * Integer.MAX_VALUE));
        assertEquals(Optional.of(new IndexPair(0, 1)),
                ArrayPatterns.pairWithSum(new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE},
                        2L * Integer.MIN_VALUE));
        // Duplicates: smallest first index, then its largest partner.
        assertEquals(Optional.of(new IndexPair(0, 4)),
                ArrayPatterns.pairWithSum(new int[] {1, 1, 1, 1, 1}, 2));

        long seed = 0x2B0;
        Random random = new Random(seed);
        for (int trial = 0; trial < 1500; trial++) {
            int[] sorted = random.ints(random.nextInt(12), -6, 7).sorted().toArray();
            int[] original = sorted.clone();
            long target = random.nextInt(21) - 10;
            Optional<IndexPair> expected = Optional.empty();
            search:
            for (int i = 0; i < sorted.length; i++) {
                for (int j = sorted.length - 1; j > i; j--) {
                    if ((long) sorted[i] + sorted[j] == target) {
                        expected = Optional.of(new IndexPair(i, j));
                        break search;
                    }
                }
            }
            assertEquals(expected, ArrayPatterns.pairWithSum(sorted, target),
                    "seed " + seed + " trial " + trial + " " + Arrays.toString(sorted)
                            + " target " + target);
            assertArrayEquals(original, sorted);
        }
    }

    @Test
    void longestSubstringWithoutRepeatsUsesEarliestTie() {
        // The window is the same half-open Span type that longestPalindromicSubstring returns.
        Span window = ArrayPatterns.longestSubstringWithoutRepeats("abcabcbb");
        assertEquals(new Span(0, 3), window);
        assertEquals(new Span(0, 1), ArrayPatterns.longestSubstringWithoutRepeats("bbbb"));
        assertEquals(new Span(0, 0), ArrayPatterns.longestSubstringWithoutRepeats(""));
        assertEquals(new Span(2, 5), ArrayPatterns.longestSubstringWithoutRepeats("pwwkew"));
        assertEquals(new Span(0, 1), ArrayPatterns.longestSubstringWithoutRepeats("a"));
        // A repeat before the window start must not shrink the window: "abba" -> "ab" (0,2).
        assertEquals(new Span(0, 2), ArrayPatterns.longestSubstringWithoutRepeats("abba"));
        // A supplementary character is two UTF-16 units, so the window is measured in units.
        assertEquals(new Span(0, 3),
                ArrayPatterns.longestSubstringWithoutRepeats("😀x\uD83D"));

        long seed = 0x2B1;
        Random random = new Random(seed);
        for (int trial = 0; trial < 800; trial++) {
            StringBuilder builder = new StringBuilder();
            int length = random.nextInt(14);
            for (int i = 0; i < length; i++) {
                builder.append("abc".charAt(random.nextInt(3)));
            }
            String text = builder.toString();
            Span expected = new Span(0, 0);
            for (int start = 0; start < text.length(); start++) {
                for (int end = start + 1; end <= text.length(); end++) {
                    boolean distinct = text.substring(start, end).chars().distinct().count()
                            == end - start;
                    if (distinct && end - start > expected.endExclusive() - expected.startInclusive()) {
                        expected = new Span(start, end);
                    }
                }
            }
            assertEquals(expected, ArrayPatterns.longestSubstringWithoutRepeats(text),
                    "seed " + seed + " trial " + trial + " text " + text);
        }
    }

    @Test
    void nextGreaterIndicesMatchesScan() {
        assertArrayEquals(new int[] {3, 2, 3, -1, -1},
                ArrayPatterns.nextGreaterIndices(new int[] {2, 1, 2, 4, 3}));
        assertArrayEquals(new int[0], ArrayPatterns.nextGreaterIndices(new int[0]));
        assertArrayEquals(new int[] {-1}, ArrayPatterns.nextGreaterIndices(new int[] {5}));
        assertArrayEquals(new int[] {-1, -1, -1},
                ArrayPatterns.nextGreaterIndices(new int[] {4, 4, 4}));
        assertArrayEquals(new int[] {1, -1},
                ArrayPatterns.nextGreaterIndices(new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE}));

        long seed = 0x2B2;
        Random random = new Random(seed);
        for (int trial = 0; trial < 800; trial++) {
            int[] values = random.ints(random.nextInt(16), -4, 5).toArray();
            int[] original = values.clone();
            int[] expected = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                expected[i] = -1;
                for (int j = i + 1; j < values.length; j++) {
                    if (values[j] > values[i]) {
                        expected[i] = j;
                        break;
                    }
                }
            }
            assertArrayEquals(expected, ArrayPatterns.nextGreaterIndices(values),
                    "seed " + seed + " trial " + trial + " " + Arrays.toString(values));
            assertArrayEquals(original, values);
        }
    }

    @Test
    void largestRectangleMatchesBruteForceAndLongArea() {
        assertEquals(10, ArrayPatterns.largestRectangle(new int[] {2, 1, 5, 6, 2, 3}));
        assertEquals(0, ArrayPatterns.largestRectangle(new int[0]));
        assertEquals(0, ArrayPatterns.largestRectangle(new int[] {0, 0}));
        assertEquals(6, ArrayPatterns.largestRectangle(new int[] {2, 2, 2}));
        assertEquals(2L * Integer.MAX_VALUE,
                ArrayPatterns.largestRectangle(new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE}));
        assertThrows(IllegalArgumentException.class,
                () -> ArrayPatterns.largestRectangle(new int[] {-1}));
        assertThrows(IllegalArgumentException.class,
                () -> ArrayPatterns.largestRectangle(new int[] {3, Integer.MIN_VALUE}));

        long seed = 0x2B3;
        Random random = new Random(seed);
        for (int trial = 0; trial < 800; trial++) {
            int[] heights = random.ints(random.nextInt(14), 0, 7).toArray();
            int[] original = heights.clone();
            long expected = 0;
            for (int start = 0; start < heights.length; start++) {
                int min = Integer.MAX_VALUE;
                for (int end = start; end < heights.length; end++) {
                    min = Math.min(min, heights[end]);
                    expected = Math.max(expected, (long) min * (end - start + 1));
                }
            }
            assertEquals(expected, ArrayPatterns.largestRectangle(heights),
                    "seed " + seed + " trial " + trial + " " + Arrays.toString(heights));
            assertArrayEquals(original, heights);
        }
    }

    @Test
    void applyRangeAddsMatchesNaive() {
        long[] values = {0, 0, 0, 0};
        assertArrayEquals(new long[] {-1, 4, 4, -1},
                ArrayPatterns.applyRangeAdds(values,
                        List.of(new RangeAdd(1, 3, 5), new RangeAdd(0, 4, -1))));
        assertArrayEquals(new long[] {0, 0, 0, 0}, values);
        assertArrayEquals(new long[0], ArrayPatterns.applyRangeAdds(new long[0], List.of()));
        assertArrayEquals(new long[] {1, 2},
                ArrayPatterns.applyRangeAdds(new long[] {1, 2}, List.of(new RangeAdd(1, 1, 9))));
        assertThrows(IndexOutOfBoundsException.class,
                () -> ArrayPatterns.applyRangeAdds(new long[2], List.of(new RangeAdd(0, 3, 1))));

        assertThrows(ArithmeticException.class,
                () -> ArrayPatterns.applyRangeAdds(new long[] {Long.MAX_VALUE},
                        List.of(new RangeAdd(0, 1, 1))));
        assertThrows(ArithmeticException.class,
                () -> ArrayPatterns.applyRangeAdds(new long[] {Long.MIN_VALUE},
                        List.of(new RangeAdd(0, 1, -1))));
        assertArrayEquals(new long[] {Long.MAX_VALUE},
                ArrayPatterns.applyRangeAdds(new long[] {Long.MAX_VALUE}, List.of(new RangeAdd(0, 1, 0))));

        // The prefix reaches 2 * MAX_VALUE, outside long, but each final value fits.
        long[] base = {-Long.MAX_VALUE, 0};
        long[] baseCopy = base.clone();
        assertArrayEquals(new long[] {Long.MAX_VALUE, 0},
                ArrayPatterns.applyRangeAdds(base,
                        List.of(new RangeAdd(0, 2, Long.MAX_VALUE), new RangeAdd(0, 2, Long.MAX_VALUE),
                                new RangeAdd(1, 2, -Long.MAX_VALUE),
                                new RangeAdd(1, 2, -Long.MAX_VALUE))));
        assertArrayEquals(baseCopy, base);
        // Subtracting Long.MIN_VALUE at the range end cannot be negated in a long.
        assertArrayEquals(new long[] {Long.MIN_VALUE, 0},
                ArrayPatterns.applyRangeAdds(new long[] {0, 0}, List.of(new RangeAdd(0, 1, Long.MIN_VALUE))));

        long seed = 0x2B4;
        Random random = new Random(seed);
        long[] specials = {Long.MIN_VALUE, Long.MAX_VALUE, -1, 1, 0, 7, -7};
        BigInteger min = BigInteger.valueOf(Long.MIN_VALUE);
        BigInteger max = BigInteger.valueOf(Long.MAX_VALUE);
        int overflowing = 0;
        for (int trial = 0; trial < 1500; trial++) {
            int n = random.nextInt(7);
            long[] input = new long[n];
            BigInteger[] expected = new BigInteger[n];
            for (int i = 0; i < n; i++) {
                input[i] = random.nextBoolean() ? specials[random.nextInt(specials.length)]
                        : random.nextInt(21) - 10;
                expected[i] = BigInteger.valueOf(input[i]);
            }
            long[] original = input.clone();
            List<RangeAdd> updates = new ArrayList<>();
            for (int u = random.nextInt(6); u > 0; u--) {
                int from = random.nextInt(n + 1);
                int to = from + random.nextInt(n - from + 1);
                long delta = random.nextBoolean() ? specials[random.nextInt(specials.length)]
                        : random.nextInt(21) - 10;
                updates.add(new RangeAdd(from, to, delta));
                for (int i = from; i < to; i++) {
                    expected[i] = expected[i].add(BigInteger.valueOf(delta));
                }
            }
            boolean fits = Arrays.stream(expected).allMatch(v -> v.compareTo(min) >= 0 && v.compareTo(max) <= 0);
            String message = "seed " + seed + " trial " + trial;
            if (fits) {
                long[] actual = ArrayPatterns.applyRangeAdds(input, updates);
                for (int i = 0; i < n; i++) {
                    assertEquals(expected[i], BigInteger.valueOf(actual[i]), message + " index " + i);
                }
            } else {
                overflowing++;
                assertThrows(ArithmeticException.class,
                        () -> ArrayPatterns.applyRangeAdds(input, updates), message);
            }
            assertArrayEquals(original, input, message);
        }
        assertTrue(overflowing > 20, "random cases should include overflow, got " + overflowing);
    }

    @Test
    void mergeIntervalsCoalescesTouching() {
        assertEquals(List.of(iv(1, 6), iv(8, 12)),
                ArrayPatterns.mergeIntervals(List.of(iv(1, 3), iv(2, 6), iv(8, 10), iv(10, 12))));
        assertEquals(List.of(), ArrayPatterns.mergeIntervals(List.of()));
        assertEquals(List.of(iv(1, 10)),
                ArrayPatterns.mergeIntervals(List.of(iv(1, 10), iv(2, 3), iv(9, 10))));
        assertEquals(List.of(iv(Integer.MIN_VALUE, Integer.MAX_VALUE)),
                ArrayPatterns.mergeIntervals(
                        List.of(iv(0, Integer.MAX_VALUE), iv(Integer.MIN_VALUE, 0))));
        List<Interval> input = new ArrayList<>(List.of(iv(5, 6), iv(1, 2)));
        List<Interval> merged = ArrayPatterns.mergeIntervals(input);
        assertEquals(List.of(iv(5, 6), iv(1, 2)), input);
        assertEquals(List.of(iv(1, 2), iv(5, 6)), merged);
        assertThrows(UnsupportedOperationException.class, () -> merged.add(iv(9, 10)));
        assertThrows(NullPointerException.class, () -> ArrayPatterns.mergeIntervals(null));
        assertThrows(NullPointerException.class,
                () -> ArrayPatterns.mergeIntervals(java.util.Arrays.asList(iv(1, 2), null)));

        long seed = 0x2B5;
        Random random = new Random(seed);
        for (int trial = 0; trial < 800; trial++) {
            List<Interval> intervals = randomIntervals(random);
            boolean[] covered = new boolean[41];
            for (Interval interval : intervals) {
                for (int p = interval.start(); p < interval.end(); p++) {
                    covered[p] = true;
                }
            }
            // Reference: maximal runs of covered unit cells, which merge exactly the
            // overlapping or touching intervals.
            List<Interval> expected = new ArrayList<>();
            for (int p = 0; p < 40; p++) {
                if (covered[p] && (p == 0 || !covered[p - 1])) {
                    int end = p;
                    while (covered[end]) {
                        end++;
                    }
                    expected.add(iv(p, end));
                }
            }
            assertEquals(expected, ArrayPatterns.mergeIntervals(intervals),
                    "seed " + seed + " trial " + trial + " " + intervals);
        }
    }

    @Test
    void minimumMeetingRoomsCountsDepth() {
        assertEquals(2, ArrayPatterns.minimumMeetingRooms(List.of(iv(0, 30), iv(5, 10), iv(15, 20))));
        assertEquals(1, ArrayPatterns.minimumMeetingRooms(List.of(iv(1, 2), iv(2, 3))));
        assertEquals(0, ArrayPatterns.minimumMeetingRooms(List.of()));
        assertEquals(1, ArrayPatterns.minimumMeetingRooms(List.of(iv(3, 4))));
        assertEquals(3, ArrayPatterns.minimumMeetingRooms(List.of(iv(1, 5), iv(1, 5), iv(4, 6))));
        assertEquals(2, ArrayPatterns.minimumMeetingRooms(
                List.of(iv(Integer.MIN_VALUE, Integer.MAX_VALUE), iv(0, 1))));
        assertThrows(NullPointerException.class, () -> ArrayPatterns.minimumMeetingRooms(null));

        long seed = 0x2B6;
        Random random = new Random(seed);
        for (int trial = 0; trial < 800; trial++) {
            List<Interval> intervals = randomIntervals(random);
            List<Interval> original = List.copyOf(intervals);
            int expected = 0;
            for (int point = 0; point < 40; point++) {
                int depth = 0;
                for (Interval interval : intervals) {
                    if (interval.start() <= point && point < interval.end()) {
                        depth++;
                    }
                }
                expected = Math.max(expected, depth);
            }
            assertEquals(expected, ArrayPatterns.minimumMeetingRooms(intervals),
                    "seed " + seed + " trial " + trial + " " + intervals);
            assertEquals(original, intervals);
        }
    }

    private static List<Interval> randomIntervals(Random random) {
        List<Interval> intervals = new ArrayList<>();
        for (int count = random.nextInt(9); count > 0; count--) {
            int start = random.nextInt(39);
            intervals.add(iv(start, start + 1 + random.nextInt(40 - start - 1)));
        }
        return intervals;
    }
}
