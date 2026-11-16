package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.ichillous.dsa.algorithms.DynamicProgrammingClassics.ChainOrder;
import io.github.ichillous.dsa.algorithms.DynamicProgrammingClassics.Schedule;
import io.github.ichillous.dsa.algorithms.DynamicProgrammingClassics.Tour;
import io.github.ichillous.dsa.algorithms.DynamicProgrammingClassics.WeightedInterval;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class DynamicProgrammingClassicsTest {
    // ---- coin combinations ----

    @Test
    void coinCombinationsKnownValuesAndBoundaries() {
        assertEquals(4, DynamicProgrammingClassics.countCoinCombinations(new int[] {1, 2, 5}, 5));
        assertEquals(0, DynamicProgrammingClassics.countCoinCombinations(new int[] {2}, 3));
        assertEquals(1, DynamicProgrammingClassics.countCoinCombinations(new int[] {2}, 0));
        assertEquals(1, DynamicProgrammingClassics.countCoinCombinations(new int[0], 0));
        assertEquals(0, DynamicProgrammingClassics.countCoinCombinations(new int[0], 4));
        assertEquals(1, DynamicProgrammingClassics.countCoinCombinations(
                new int[] {Integer.MAX_VALUE}, 0));
        assertEquals(0, DynamicProgrammingClassics.countCoinCombinations(
                new int[] {Integer.MAX_VALUE}, 5));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgrammingClassics.countCoinCombinations(new int[] {1, 0}, 3));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgrammingClassics.countCoinCombinations(new int[] {1}, -1));
        assertThrows(NullPointerException.class,
                () -> DynamicProgrammingClassics.countCoinCombinations(null, 1));
    }

    @Test
    void coinCombinationsCollapseDuplicatesAndKeepInputUnchanged() {
        int[] coins = {5, 1, 2, 2, 1, 5};
        int[] copy = coins.clone();
        assertEquals(4, DynamicProgrammingClassics.countCoinCombinations(coins, 5));
        assertArrayEquals(copy, coins);
    }

    @Test
    void coinCombinationsRejectOversizedTables() {
        int[] coins = {1};
        assertThrows(IllegalArgumentException.class, () -> DynamicProgrammingClassics
                .countCoinCombinations(coins, Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> DynamicProgrammingClassics
                .countCoinCombinations(coins, Integer.MAX_VALUE - 1));
        assertThrows(IllegalArgumentException.class, () -> DynamicProgrammingClassics
                .countCoinCombinations(coins, Integer.MAX_VALUE - 9 + 1));
    }

    @Test
    void coinCombinationsOverflowThrowsInsteadOfWrapping() {
        int[] coins = IntStream.rangeClosed(1, 60).toArray();
        assertThrows(ArithmeticException.class,
                () -> DynamicProgrammingClassics.countCoinCombinations(coins, 5000));
    }

    @Test
    void coinCombinationsMatchBruteForceEnumeration() {
        long seed = 0xC01DL;
        Random random = new Random(seed);
        for (int trial = 0; trial < 300; trial++) {
            int[] coins = new int[random.nextInt(5)];
            for (int index = 0; index < coins.length; index++) {
                coins[index] = random.nextInt(7) + 1;
            }
            int amount = random.nextInt(21);
            Set<Integer> distinct = new HashSet<>();
            for (int coin : coins) {
                distinct.add(coin);
            }
            long expected = enumerateCombinations(new ArrayList<>(distinct), 0, amount);
            assertEquals(expected, DynamicProgrammingClassics.countCoinCombinations(coins, amount),
                    "seed " + seed + " trial " + trial + " coins " + Arrays.toString(coins)
                            + " amount " + amount);
        }
    }

    private static long enumerateCombinations(List<Integer> coins, int index, int remaining) {
        if (remaining == 0) {
            return 1;
        }
        if (index == coins.size()) {
            return 0;
        }
        long total = 0;
        for (int used = 0; used * coins.get(index) <= remaining; used++) {
            total += enumerateCombinations(coins, index + 1, remaining - used * coins.get(index));
        }
        return total;
    }

    // ---- word break ----

    @Test
    void wordBreakKnownCases() {
        assertEquals(Optional.of(List.of("apple", "pen", "apple")),
                DynamicProgrammingClassics.wordBreak("applepenapple", List.of("apple", "pen")));
        assertEquals(Optional.empty(), DynamicProgrammingClassics.wordBreak("catsandog",
                List.of("cats", "dog", "sand", "and", "cat")));
        assertEquals(Optional.of(List.of()), DynamicProgrammingClassics.wordBreak("", List.of()));
        assertEquals(Optional.empty(), DynamicProgrammingClassics.wordBreak("a", List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgrammingClassics.wordBreak("a", List.of("a", "")));
        assertThrows(NullPointerException.class,
                () -> DynamicProgrammingClassics.wordBreak(null, List.of("a")));
        assertThrows(NullPointerException.class,
                () -> DynamicProgrammingClassics.wordBreak("a", java.util.Arrays.asList("a", null)));
    }

    @Test
    void wordBreakPrefersShortestFirstWordRecursively() {
        List<String> dictionary = List.of("cat", "cats", "and", "sand", "dog");
        assertEquals(Optional.of(List.of("cats", "and", "dog")),
                DynamicProgrammingClassics.wordBreak("catsanddog", List.of("cats", "and", "dog")));
        // Both [cat, sand, dog] and [cats, and, dog] work; the shorter first word wins.
        assertEquals(Optional.of(List.of("cat", "sand", "dog")),
                DynamicProgrammingClassics.wordBreak("catsanddog", dictionary));
        // The shortest first word "a" leads nowhere, so the next shortest is used.
        assertEquals(Optional.of(List.of("ab", "c")),
                DynamicProgrammingClassics.wordBreak("abc", List.of("a", "ab", "c", "bd")));
        assertEquals(Optional.of(List.of("a", "a", "a")),
                DynamicProgrammingClassics.wordBreak("aaa", List.of("aa", "a", "aaa", "a")));
    }

    @Test
    void wordBreakMatchesBruteForceSegmentation() {
        long seed = 0xBEA7L;
        Random random = new Random(seed);
        for (int trial = 0; trial < 400; trial++) {
            StringBuilder text = new StringBuilder();
            int length = random.nextInt(9);
            for (int index = 0; index < length; index++) {
                text.append((char) ('a' + random.nextInt(2)));
            }
            List<String> dictionary = new ArrayList<>();
            int words = random.nextInt(5);
            for (int index = 0; index < words; index++) {
                StringBuilder word = new StringBuilder();
                int wordLength = random.nextInt(3) + 1;
                for (int position = 0; position < wordLength; position++) {
                    word.append((char) ('a' + random.nextInt(2)));
                }
                dictionary.add(word.toString());
            }
            List<String> best = null;
            for (List<String> candidate : allSegmentations(text.toString(), 0, new HashSet<>(dictionary))) {
                if (best == null || compareLengths(candidate, best) < 0) {
                    best = candidate;
                }
            }
            assertEquals(Optional.ofNullable(best),
                    DynamicProgrammingClassics.wordBreak(text.toString(), dictionary),
                    "seed " + seed + " trial " + trial + " text " + text + " dictionary " + dictionary);
        }
    }

    private static List<List<String>> allSegmentations(String text, int start, Set<String> words) {
        List<List<String>> result = new ArrayList<>();
        if (start == text.length()) {
            result.add(List.of());
            return result;
        }
        for (int end = start + 1; end <= text.length(); end++) {
            String word = text.substring(start, end);
            if (words.contains(word)) {
                for (List<String> rest : allSegmentations(text, end, words)) {
                    List<String> segmentation = new ArrayList<>();
                    segmentation.add(word);
                    segmentation.addAll(rest);
                    result.add(segmentation);
                }
            }
        }
        return result;
    }

    private static int compareLengths(List<String> left, List<String> right) {
        for (int index = 0; index < Math.min(left.size(), right.size()); index++) {
            int difference = Integer.compare(left.get(index).length(), right.get(index).length());
            if (difference != 0) {
                return difference;
            }
        }
        return Integer.compare(left.size(), right.size());
    }

    // ---- longest palindromic subsequence ----

    @Test
    void palindromicSubsequenceKnownValues() {
        assertEquals(4, DynamicProgrammingClassics.longestPalindromicSubsequenceLength("bbbab"));
        assertEquals(2, DynamicProgrammingClassics.longestPalindromicSubsequenceLength("cbbd"));
        assertEquals(0, DynamicProgrammingClassics.longestPalindromicSubsequenceLength(""));
        assertEquals(1, DynamicProgrammingClassics.longestPalindromicSubsequenceLength("x"));
        assertEquals(7, DynamicProgrammingClassics.longestPalindromicSubsequenceLength("racecar"));
        assertEquals(1, DynamicProgrammingClassics.longestPalindromicSubsequenceLength("abcd"));
        assertThrows(NullPointerException.class,
                () -> DynamicProgrammingClassics.longestPalindromicSubsequenceLength(null));
    }

    @Test
    void palindromicSubsequenceMatchesBruteForce() {
        long seed = 0x9A11L;
        Random random = new Random(seed);
        for (int trial = 0; trial < 300; trial++) {
            StringBuilder text = new StringBuilder();
            int length = random.nextInt(13);
            for (int index = 0; index < length; index++) {
                text.append((char) ('a' + random.nextInt(3)));
            }
            int expected = 0;
            for (int mask = 0; mask < (1 << length); mask++) {
                StringBuilder subsequence = new StringBuilder();
                for (int index = 0; index < length; index++) {
                    if ((mask & (1 << index)) != 0) {
                        subsequence.append(text.charAt(index));
                    }
                }
                if (subsequence.toString().contentEquals(new StringBuilder(subsequence).reverse())) {
                    expected = Math.max(expected, subsequence.length());
                }
            }
            assertEquals(expected,
                    DynamicProgrammingClassics.longestPalindromicSubsequenceLength(text.toString()),
                    "seed " + seed + " trial " + trial + " text " + text);
        }
    }

    // ---- matrix chain ----

    @Test
    void matrixChainKnownValuesAndSingletons() {
        assertEquals(new ChainOrder(4500, "((A1A2)A3)"),
                DynamicProgrammingClassics.matrixChainOrder(new int[] {10, 30, 5, 60}));
        assertEquals(new ChainOrder(0, "A1"),
                DynamicProgrammingClassics.matrixChainOrder(new int[] {7, 9}));
        assertEquals(new ChainOrder(10 * 20 * 30, "(A1A2)"),
                DynamicProgrammingClassics.matrixChainOrder(new int[] {10, 20, 30}));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgrammingClassics.matrixChainOrder(new int[] {5}));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgrammingClassics.matrixChainOrder(new int[0]));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgrammingClassics.matrixChainOrder(new int[] {5, 0, 3}));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgrammingClassics.matrixChainOrder(new int[] {5, -1, 3}));
        assertThrows(NullPointerException.class,
                () -> DynamicProgrammingClassics.matrixChainOrder(null));
    }

    @Test
    void matrixChainTiesGoToTheSmallestSplit() {
        // Every order costs 2, so the first split (after A1) must be chosen at each level.
        assertEquals(new ChainOrder(2, "(A1(A2A3))"),
                DynamicProgrammingClassics.matrixChainOrder(new int[] {1, 1, 1, 1}));
        assertEquals(new ChainOrder(3, "(A1(A2(A3A4)))"),
                DynamicProgrammingClassics.matrixChainOrder(new int[] {1, 1, 1, 1, 1}));
    }

    @Test
    void matrixChainExtremeDimensionsAreExactOrOverflow() {
        int max = Integer.MAX_VALUE;
        assertEquals((long) max * max,
                DynamicProgrammingClassics.matrixChainOrder(new int[] {max, 1, max}).cost());
        assertThrows(ArithmeticException.class,
                () -> DynamicProgrammingClassics.matrixChainOrder(new int[] {max, max, max}));
        // Even a single triple product of Integer.MAX_VALUE exceeds a long.
        assertThrows(ArithmeticException.class,
                () -> DynamicProgrammingClassics.matrixChainOrder(new int[] {max, max, max, max}));
    }

    @Test
    void matrixChainMatchesBruteForceAndDoesNotMutateInput() {
        long seed = 0x3A7L;
        Random random = new Random(seed);
        for (int trial = 0; trial < 300; trial++) {
            int[] dimensions = new int[2 + random.nextInt(7)];
            for (int index = 0; index < dimensions.length; index++) {
                dimensions[index] = random.nextInt(9) + 1;
            }
            int[] copy = dimensions.clone();
            ChainOrder order = DynamicProgrammingClassics.matrixChainOrder(dimensions);
            String message = "seed " + seed + " trial " + trial + " dims " + Arrays.toString(copy);
            assertArrayEquals(copy, dimensions, message);
            assertEquals(bruteChain(dimensions, 1, dimensions.length - 1), order.cost(), message);
            assertEquals(order.cost(), evaluate(order.parenthesization(), dimensions), message);
        }
    }

    private static long bruteChain(int[] dimensions, int from, int to) {
        if (from == to) {
            return 0;
        }
        long best = Long.MAX_VALUE;
        for (int split = from; split < to; split++) {
            long cost = bruteChain(dimensions, from, split) + bruteChain(dimensions, split + 1, to)
                    + (long) dimensions[from - 1] * dimensions[split] * dimensions[to];
            best = Math.min(best, cost);
        }
        return best;
    }

    /** Evaluates a parenthesization string such as "((A1A2)A3)" and returns its scalar cost. */
    private static long evaluate(String expression, int[] dimensions) {
        int[] position = {0};
        long[] result = parse(expression, position, dimensions);
        assertEquals(expression.length(), position[0]);
        return result[2];
    }

    /** Returns {rows, columns, cost} for the sub-expression starting at the position. */
    private static long[] parse(String expression, int[] position, int[] dimensions) {
        if (expression.charAt(position[0]) == 'A') {
            position[0]++;
            int start = position[0];
            while (position[0] < expression.length() && Character.isDigit(expression.charAt(position[0]))) {
                position[0]++;
            }
            int index = Integer.parseInt(expression.substring(start, position[0]));
            return new long[] {dimensions[index - 1], dimensions[index], 0};
        }
        assertEquals('(', expression.charAt(position[0]++));
        long[] left = parse(expression, position, dimensions);
        long[] right = parse(expression, position, dimensions);
        assertEquals(')', expression.charAt(position[0]++));
        assertEquals(left[1], right[0]);
        return new long[] {left[0], right[1], left[2] + right[2] + left[0] * left[1] * right[1]};
    }

    // ---- Held-Karp ----

    @Test
    void heldKarpSingleCityTwoCitiesAndTieRule() {
        assertEquals(new Tour(List.of(0), 0),
                DynamicProgrammingClassics.travelingSalesman(new long[][] {{0}}));
        assertEquals(new Tour(List.of(0, 1), 7),
                DynamicProgrammingClassics.travelingSalesman(new long[][] {{0, 3}, {4, 0}}));
        // All tours cost 0: the smallest last city (1) closes the tour, so city 2 precedes it.
        assertEquals(new Tour(List.of(0, 2, 1), 0),
                DynamicProgrammingClassics.travelingSalesman(new long[3][3]));
    }

    @Test
    void heldKarpPredecessorTieGoesToTheSmallestCity() {
        // All tours cost 0. The last city is 1; its predecessor tie among cities 2 and 3 goes to 2
        // (smallest), whose own predecessor is 3.
        assertEquals(new Tour(List.of(0, 3, 2, 1), 0),
                DynamicProgrammingClassics.travelingSalesman(new long[4][4]));
    }

    @Test
    void heldKarpRejectsBadMatrices() {
        long limit = 1L << 47;
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgrammingClassics.travelingSalesman(new long[0][0]));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgrammingClassics.travelingSalesman(new long[17][17]));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgrammingClassics.travelingSalesman(new long[][] {{0, 1}, {1}}));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgrammingClassics.travelingSalesman(
                        new long[][] {{0, limit + 1}, {1, 0}}));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgrammingClassics.travelingSalesman(
                        new long[][] {{0, -limit - 1}, {1, 0}}));
        assertThrows(NullPointerException.class,
                () -> DynamicProgrammingClassics.travelingSalesman(null));
        assertThrows(NullPointerException.class,
                () -> DynamicProgrammingClassics.travelingSalesman(new long[][] {null}));
    }

    @Test
    void heldKarpAtTheLargestSizeAndLargestEntriesDoesNotOverflow() {
        long limit = 1L << 47;
        long[][] distances = new long[16][16];
        for (long[] row : distances) {
            Arrays.fill(row, limit);
        }
        Tour tour = DynamicProgrammingClassics.travelingSalesman(distances);
        assertEquals(16 * limit, tour.cost());
        assertEquals(16, new HashSet<>(tour.order()).size());
        for (long[] row : distances) {
            Arrays.fill(row, -limit);
        }
        assertEquals(-16 * limit, DynamicProgrammingClassics.travelingSalesman(distances).cost());
    }

    @Test
    void heldKarpMatchesPermutationBruteForce() {
        long seed = 0x75FL;
        Random random = new Random(seed);
        for (int trial = 0; trial < 120; trial++) {
            int cities = 1 + random.nextInt(8);
            long[][] distances = new long[cities][cities];
            for (long[] row : distances) {
                for (int column = 0; column < cities; column++) {
                    row[column] = random.nextInt(41) - 10;
                }
            }
            long[][] copy = Arrays.stream(distances).map(long[]::clone).toArray(long[][]::new);
            Tour tour = DynamicProgrammingClassics.travelingSalesman(distances);
            String message = "seed " + seed + " trial " + trial + " cities " + cities;
            assertTrue(Arrays.deepEquals(copy, distances), message);
            assertEquals(bruteTour(distances), tour.cost(), message);
            assertEquals(0, tour.order().get(0), message);
            assertEquals(cities, new HashSet<>(tour.order()).size(), message);
            assertEquals(cities, tour.order().size(), message);
            assertEquals(tourCost(distances, tour.order()), tour.cost(), message);
        }
    }

    private static long tourCost(long[][] distances, List<Integer> order) {
        if (order.size() == 1) {
            return 0; // a one-city tour has no edges; the diagonal is unused
        }
        long total = 0;
        for (int index = 0; index < order.size(); index++) {
            total += distances[order.get(index)][order.get((index + 1) % order.size())];
        }
        return total;
    }

    private static long bruteTour(long[][] distances) {
        int cities = distances.length;
        int[] rest = IntStream.range(1, cities).toArray();
        long[] best = {Long.MAX_VALUE};
        permute(rest, 0, order -> {
            List<Integer> tour = new ArrayList<>();
            tour.add(0);
            for (int city : order) {
                tour.add(city);
            }
            best[0] = Math.min(best[0], tourCost(distances, tour));
        });
        return best[0];
    }

    private static void permute(int[] items, int index, java.util.function.Consumer<int[]> visit) {
        if (index == items.length) {
            visit.accept(items);
            return;
        }
        for (int swap = index; swap < items.length; swap++) {
            int saved = items[index];
            items[index] = items[swap];
            items[swap] = saved;
            permute(items, index + 1, visit);
            items[swap] = items[index];
            items[index] = saved;
        }
    }

    // ---- weighted interval scheduling ----

    @Test
    void weightedIntervalsKnownCasesAndTies() {
        assertEquals(new Schedule(List.of(), 0),
                DynamicProgrammingClassics.weightedIntervalScheduling(List.of()));
        WeightedInterval big = new WeightedInterval(0, 10, 10);
        WeightedInterval first = new WeightedInterval(0, 5, 4);
        WeightedInterval second = new WeightedInterval(5, 10, 7);
        // Two touching intervals (11) beat the single long one (10).
        assertEquals(new Schedule(List.of(first, second), 11),
                DynamicProgrammingClassics.weightedIntervalScheduling(List.of(big, second, first)));
        // Equal totals (11 each): the interval that sorts later (second) is skipped.
        WeightedInterval tied = new WeightedInterval(0, 10, 11);
        assertEquals(new Schedule(List.of(tied), 11),
                DynamicProgrammingClassics.weightedIntervalScheduling(List.of(tied, first, second)));
        // Zero-weight intervals are never chosen.
        assertEquals(new Schedule(List.of(), 0), DynamicProgrammingClassics.weightedIntervalScheduling(
                List.of(new WeightedInterval(1, 2, 0))));
    }

    @Test
    void weightedIntervalsValidateInputsAndHandleExtremes() {
        assertThrows(IllegalArgumentException.class, () -> new WeightedInterval(3, 3, 1));
        assertThrows(IllegalArgumentException.class, () -> new WeightedInterval(0, 1, -1));
        assertThrows(NullPointerException.class,
                () -> DynamicProgrammingClassics.weightedIntervalScheduling(null));
        assertThrows(NullPointerException.class, () -> DynamicProgrammingClassics
                .weightedIntervalScheduling(Arrays.asList(new WeightedInterval(0, 1, 1), null)));
        WeightedInterval low = new WeightedInterval(Integer.MIN_VALUE, 0, Long.MAX_VALUE);
        WeightedInterval high = new WeightedInterval(0, Integer.MAX_VALUE, 1);
        assertThrows(ArithmeticException.class,
                () -> DynamicProgrammingClassics.weightedIntervalScheduling(List.of(low, high)));
        WeightedInterval overlapping = new WeightedInterval(-1, Integer.MAX_VALUE, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, DynamicProgrammingClassics
                .weightedIntervalScheduling(List.of(low, overlapping)).totalWeight());
    }

    @Test
    void weightedIntervalsMatchBruteForce() {
        long seed = 0x1A7E5L;
        Random random = new Random(seed);
        for (int trial = 0; trial < 300; trial++) {
            List<WeightedInterval> intervals = new ArrayList<>();
            int size = random.nextInt(13);
            for (int index = 0; index < size; index++) {
                int start = random.nextInt(20) - 10;
                intervals.add(new WeightedInterval(
                        start, start + 1 + random.nextInt(8), random.nextInt(10)));
            }
            List<WeightedInterval> original = List.copyOf(intervals);
            Schedule schedule = DynamicProgrammingClassics.weightedIntervalScheduling(intervals);
            String message = "seed " + seed + " trial " + trial + " intervals " + intervals;
            assertEquals(original, intervals, message);
            long expected = 0;
            for (int mask = 0; mask < (1 << size); mask++) {
                long total = 0;
                boolean valid = true;
                for (int i = 0; i < size && valid; i++) {
                    if ((mask & (1 << i)) == 0) {
                        continue;
                    }
                    total += intervals.get(i).weight();
                    for (int j = i + 1; j < size && valid; j++) {
                        valid = (mask & (1 << j)) == 0 || !overlaps(intervals.get(i), intervals.get(j));
                    }
                }
                if (valid) {
                    expected = Math.max(expected, total);
                }
            }
            assertEquals(expected, schedule.totalWeight(), message);
            long chosenTotal = 0;
            for (int i = 0; i < schedule.chosen().size(); i++) {
                chosenTotal += schedule.chosen().get(i).weight();
                assertTrue(intervals.contains(schedule.chosen().get(i)), message);
                for (int j = i + 1; j < schedule.chosen().size(); j++) {
                    assertTrue(!overlaps(schedule.chosen().get(i), schedule.chosen().get(j)), message);
                }
                if (i > 0) {
                    assertTrue(schedule.chosen().get(i - 1).end() <= schedule.chosen().get(i).end(), message);
                }
            }
            assertEquals(schedule.totalWeight(), chosenTotal, message);
        }
    }

    private static boolean overlaps(WeightedInterval first, WeightedInterval second) {
        return first.start() < second.end() && second.start() < first.end();
    }

    @Test
    void resultsAreImmutable() {
        Schedule schedule = DynamicProgrammingClassics.weightedIntervalScheduling(
                List.of(new WeightedInterval(0, 1, 1)));
        assertThrows(UnsupportedOperationException.class,
                () -> schedule.chosen().add(new WeightedInterval(5, 6, 1)));
        Tour tour = DynamicProgrammingClassics.travelingSalesman(new long[][] {{0, 1}, {1, 0}});
        assertThrows(UnsupportedOperationException.class, () -> tour.order().add(9));
        assertThrows(UnsupportedOperationException.class,
                () -> DynamicProgrammingClassics.wordBreak("a", List.of("a")).orElseThrow().add("b"));
    }
}
