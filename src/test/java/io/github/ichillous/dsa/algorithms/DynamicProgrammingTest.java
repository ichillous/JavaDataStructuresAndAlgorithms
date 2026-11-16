package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DynamicProgrammingTest {
    @Test
    void minimumCoinsUsesOptimalRatherThanGreedyChoices() {
        assertEquals(2, DynamicProgramming.minimumCoins(new int[] {1, 3, 4}, 6));
        assertEquals(-1, DynamicProgramming.minimumCoins(new int[] {2, 4}, 7));
        assertEquals(0, DynamicProgramming.minimumCoins(new int[0], 0));
        assertEquals(-1, DynamicProgramming.minimumCoins(new int[0], 3));
        assertEquals(2, DynamicProgramming.minimumCoins(new int[] {3, 3, Integer.MAX_VALUE}, 6));
    }

    @Test
    void minimumCoinsAgreesWithBreadthFirstSearch() {
        Random random = new Random(0xC015);
        for (int trial = 0; trial < 400; trial++) {
            int[] coins = random.ints(random.nextInt(6), 1, 11).toArray();
            int amount = random.nextInt(40);
            assertEquals(minimumCoinsByBfs(coins, amount), DynamicProgramming.minimumCoins(coins, amount),
                    "trial " + trial);
        }
    }

    @Test
    void knapsackTreatsItemsAsSingleUseAndHandlesZeroWeightAndLongResults() {
        assertEquals(9, DynamicProgramming.knapsack01(new int[] {2, 3, 4}, new int[] {4, 5, 7}, 5));
        assertEquals(4, DynamicProgramming.knapsack01(new int[] {2}, new int[] {4}, 4));
        assertEquals(7, DynamicProgramming.knapsack01(new int[] {0, 0, 1}, new int[] {3, 4, 100}, 0));
        assertEquals(0, DynamicProgramming.knapsack01(new int[] {0, 1}, new int[] {-3, -4}, 1));
        assertEquals(2L * Integer.MAX_VALUE, DynamicProgramming.knapsack01(
                new int[] {1, 1}, new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE}, 2));
        assertEquals(0, DynamicProgramming.knapsack01(new int[0], new int[0], 0));
    }

    @Test
    void knapsackAgreesWithExhaustiveSubsets() {
        Random random = new Random(0xB4C4);
        for (int trial = 0; trial < 400; trial++) {
            int size = random.nextInt(10);
            int[] weights = random.ints(size, 0, 10).toArray();
            int[] values = random.ints(size, -10, 30).toArray();
            int capacity = random.nextInt(25);
            long expected = 0;
            for (int mask = 0; mask < (1 << size); mask++) {
                int weight = 0;
                long value = 0;
                for (int item = 0; item < size; item++) {
                    if ((mask & (1 << item)) != 0) {
                        weight += weights[item];
                        value += values[item];
                    }
                }
                if (weight <= capacity) {
                    expected = Math.max(expected, value);
                }
            }
            assertEquals(expected, DynamicProgramming.knapsack01(weights, values, capacity),
                    "trial " + trial);
        }
    }

    @Test
    void lcsHandlesEmptyDisjointAndRepeatedStrings() {
        assertEquals(0, DynamicProgramming.longestCommonSubsequenceLength("", "abc"));
        assertEquals(0, DynamicProgramming.longestCommonSubsequenceLength("abc", "xyz"));
        assertEquals(3, DynamicProgramming.longestCommonSubsequenceLength("abcde", "ace"));
        assertEquals(2, DynamicProgramming.longestCommonSubsequenceLength("aaaa", "aa"));
        assertEquals(2, DynamicProgramming.longestCommonSubsequenceLength("😀", "😀"));
    }

    @Test
    void lcsAgreesWithExhaustiveSubsequences() {
        Random random = new Random(0x1C5);
        for (int trial = 0; trial < 200; trial++) {
            String first = smallString(random, random.nextInt(10));
            String second = smallString(random, random.nextInt(10));
            int expected = 0;
            for (int mask = 0; mask < (1 << first.length()); mask++) {
                StringBuilder subsequence = new StringBuilder();
                for (int index = 0; index < first.length(); index++) {
                    if ((mask & (1 << index)) != 0) {
                        subsequence.append(first.charAt(index));
                    }
                }
                if (isSubsequence(subsequence.toString(), second)) {
                    expected = Math.max(expected, subsequence.length());
                }
            }
            assertEquals(expected, DynamicProgramming.longestCommonSubsequenceLength(first, second),
                    "trial " + trial);
            assertEquals(expected, DynamicProgramming.longestCommonSubsequenceLength(second, first));
        }
    }

    @Test
    void lisIsStrictAndDoesNotModifyInput() {
        int[] values = {Integer.MAX_VALUE, Integer.MIN_VALUE, -1, -1, 0, Integer.MAX_VALUE};
        int[] original = values.clone();
        assertEquals(4, DynamicProgramming.longestIncreasingSubsequenceLength(values));
        assertArrayEquals(original, values);
        assertEquals(0, DynamicProgramming.longestIncreasingSubsequenceLength(new int[0]));
        assertEquals(1, DynamicProgramming.longestIncreasingSubsequenceLength(new int[] {3, 3, 3}));
        assertEquals(1, DynamicProgramming.longestIncreasingSubsequenceLength(new int[] {3, 2, 1}));
    }

    @Test
    void lisAgreesWithExhaustiveSubsets() {
        Random random = new Random(0x115);
        for (int trial = 0; trial < 300; trial++) {
            int[] values = random.ints(random.nextInt(12), -6, 7).toArray();
            int expected = 0;
            for (int mask = 0; mask < (1 << values.length); mask++) {
                boolean increasing = true;
                boolean hasPrevious = false;
                int previous = 0;
                int length = 0;
                for (int index = 0; index < values.length; index++) {
                    if ((mask & (1 << index)) != 0) {
                        if (hasPrevious && previous >= values[index]) {
                            increasing = false;
                            break;
                        }
                        previous = values[index];
                        hasPrevious = true;
                        length++;
                    }
                }
                if (increasing) {
                    expected = Math.max(expected, length);
                }
            }
            assertEquals(expected, DynamicProgramming.longestIncreasingSubsequenceLength(values),
                    "trial " + trial);
        }
    }

    @Test
    void editDistanceHasUnitInsertDeleteAndReplaceCosts() {
        assertEquals(0, DynamicProgramming.editDistance("", ""));
        assertEquals(3, DynamicProgramming.editDistance("", "abc"));
        assertEquals(3, DynamicProgramming.editDistance("kitten", "sitting"));
        assertEquals(2, DynamicProgramming.editDistance("ab", "ba"));
        assertEquals(2, DynamicProgramming.editDistance("😀", ""));
    }

    @Test
    void editDistanceAgreesWithIndependentFullTable() {
        Random random = new Random(0xED17);
        for (int trial = 0; trial < 300; trial++) {
            String first = smallString(random, random.nextInt(12));
            String second = smallString(random, random.nextInt(12));
            int[][] distance = new int[first.length() + 1][second.length() + 1];
            for (int row = 0; row <= first.length(); row++) {
                distance[row][0] = row;
            }
            for (int column = 0; column <= second.length(); column++) {
                distance[0][column] = column;
            }
            for (int row = 1; row <= first.length(); row++) {
                for (int column = 1; column <= second.length(); column++) {
                    distance[row][column] = Math.min(
                            distance[row - 1][column] + 1, distance[row][column - 1] + 1);
                    distance[row][column] = Math.min(distance[row][column],
                            distance[row - 1][column - 1]
                                    + (first.charAt(row - 1) == second.charAt(column - 1) ? 0 : 1));
                }
            }
            int expected = distance[first.length()][second.length()];
            assertEquals(expected, DynamicProgramming.editDistance(first, second), "trial " + trial);
            assertEquals(expected, DynamicProgramming.editDistance(second, first));
            assertEquals(0, DynamicProgramming.editDistance(first, first));
        }
    }

    @Test
    void rejectsInvalidStatesAndNullsBeforeAllocatingOrComputing() {
        assertThrows(NullPointerException.class, () -> DynamicProgramming.minimumCoins(null, 1));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.minimumCoins(new int[] {0}, 0));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.minimumCoins(new int[] {-1}, 2));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.minimumCoins(new int[] {1}, -1));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.minimumCoins(new int[] {1}, Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.knapsack01(new int[] {1}, new int[0], 0));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.knapsack01(new int[] {-1}, new int[] {1}, 0));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.knapsack01(new int[0], new int[0], -1));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.knapsack01(new int[0], new int[0], Integer.MAX_VALUE));
        assertThrows(NullPointerException.class,
                () -> DynamicProgramming.knapsack01(null, new int[0], 0));
        assertThrows(NullPointerException.class,
                () -> DynamicProgramming.knapsack01(new int[0], null, 0));
        assertThrows(NullPointerException.class,
                () -> DynamicProgramming.longestCommonSubsequenceLength(null, ""));
        assertThrows(NullPointerException.class,
                () -> DynamicProgramming.longestCommonSubsequenceLength("", null));
        assertThrows(NullPointerException.class,
                () -> DynamicProgramming.longestIncreasingSubsequenceLength(null));
        assertThrows(NullPointerException.class, () -> DynamicProgramming.editDistance(null, ""));
        assertThrows(NullPointerException.class, () -> DynamicProgramming.editDistance("", null));
    }

    private static int minimumCoinsByBfs(int[] coins, int amount) {
        int[] distance = new int[amount + 1];
        Arrays.fill(distance, -1);
        distance[0] = 0;
        Queue<Integer> frontier = new ArrayDeque<>();
        frontier.add(0);
        while (!frontier.isEmpty()) {
            int current = frontier.remove();
            if (current == amount) {
                return distance[current];
            }
            for (int coin : coins) {
                int next = current + coin;
                if (next <= amount && distance[next] == -1) {
                    distance[next] = distance[current] + 1;
                    frontier.add(next);
                }
            }
        }
        return -1;
    }

    private static String smallString(Random random, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            result.append((char) ('a' + random.nextInt(3)));
        }
        return result.toString();
    }

    private static boolean isSubsequence(String candidate, String text) {
        int position = 0;
        for (int index = 0; index < text.length() && position < candidate.length(); index++) {
            if (candidate.charAt(position) == text.charAt(index)) {
                position++;
            }
        }
        return position == candidate.length();
    }

    @Test
    void tableSizeGuardRejectsLengthsAboveSafeArrayLimit() {
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.minimumCoins(new int[] {1}, Integer.MAX_VALUE - 1));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.minimumCoins(new int[] {1}, Integer.MAX_VALUE - 9 + 1));
        assertThrows(IllegalArgumentException.class, () -> DynamicProgramming.knapsack01(
                new int[0], new int[0], Integer.MAX_VALUE - 2));
        assertThrows(IllegalArgumentException.class, () -> DynamicProgramming.knapsack01(
                new int[0], new int[0], Integer.MAX_VALUE - 8));
    }

    private static String randomString(Random random) {
        StringBuilder text = new StringBuilder();
        for (int i = random.nextInt(9); i > 0; i--) {
            text.append((char) ('a' + random.nextInt(3)));
        }
        return text.toString();
    }

    @Test
    void lcsStringFollowsRule() {
        String a = "ABCBDAB";
        String b = "BDCABA";
        String result = DynamicProgramming.longestCommonSubsequence(a, b);
        assertEquals(4, result.length());
        assertEquals("BCBA", result);
        assertEquals("ABCBDAB", a);
        assertEquals("BDCABA", b);
        assertEquals("", DynamicProgramming.longestCommonSubsequence("", "abc"));
        assertEquals("", DynamicProgramming.longestCommonSubsequence("abc", "xyz"));
        assertEquals("aa", DynamicProgramming.longestCommonSubsequence("aaaa", "aa"));
        Random random = new Random(0x1C5);
        for (int trial = 0; trial < 300; trial++) {
            String x = randomString(random);
            String y = randomString(random);
            String lcs = DynamicProgramming.longestCommonSubsequence(x, y);
            assertEquals(DynamicProgramming.longestCommonSubsequenceLength(x, y), lcs.length(),
                    "seed 0x1C5 trial " + trial);
            assertTrue(isSubsequence(lcs, x) && isSubsequence(lcs, y), "seed 0x1C5 trial " + trial);
        }
    }

    private static String replay(String source, String target,
            List<DynamicProgramming.EditStep> script) {
        StringBuilder out = new StringBuilder();
        int sourceCursor = 0;
        int targetCursor = 0;
        for (DynamicProgramming.EditStep step : script) {
            assertEquals(sourceCursor, step.sourceIndex());
            assertEquals(targetCursor, step.targetIndex());
            switch (step.operation()) {
                case MATCH -> {
                    assertEquals(source.charAt(sourceCursor), target.charAt(targetCursor));
                    out.append(source.charAt(sourceCursor++));
                    targetCursor++;
                }
                case SUBSTITUTE -> {
                    sourceCursor++;
                    out.append(target.charAt(targetCursor++));
                }
                case DELETE -> sourceCursor++;
                case INSERT -> out.append(target.charAt(targetCursor++));
            }
        }
        assertEquals(source.length(), sourceCursor);
        assertEquals(target.length(), targetCursor);
        return out.toString();
    }

    private static long nonMatches(List<DynamicProgramming.EditStep> script) {
        return script.stream()
                .filter(step -> step.operation() != DynamicProgramming.EditOperation.MATCH).count();
    }

    @Test
    void editScriptReplaysToTarget() {
        List<DynamicProgramming.EditStep> script = DynamicProgramming.editScript("kitten", "sitting");
        assertEquals(3, nonMatches(script));
        assertEquals("sitting", replay("kitten", "sitting", script));
        assertEquals(List.of(), DynamicProgramming.editScript("", ""));
        assertEquals(List.of(new DynamicProgramming.EditStep(DynamicProgramming.EditOperation.INSERT, 0, 0)),
                DynamicProgramming.editScript("", "a"));
        assertEquals(List.of(new DynamicProgramming.EditStep(DynamicProgramming.EditOperation.DELETE, 0, 0)),
                DynamicProgramming.editScript("a", ""));
        assertThrows(UnsupportedOperationException.class, () -> script.add(script.get(0)));
        assertThrows(IllegalArgumentException.class,
                () -> new DynamicProgramming.EditStep(DynamicProgramming.EditOperation.MATCH, -1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new DynamicProgramming.EditStep(DynamicProgramming.EditOperation.MATCH, 0, -1));
        assertThrows(NullPointerException.class, () -> new DynamicProgramming.EditStep(null, 0, 0));
        Random random = new Random(0xED17);
        for (int trial = 0; trial < 300; trial++) {
            String x = randomString(random);
            String y = randomString(random);
            List<DynamicProgramming.EditStep> steps = DynamicProgramming.editScript(x, y);
            assertEquals(y, replay(x, y, steps), "seed 0xED17 trial " + trial);
            assertEquals(DynamicProgramming.editDistance(x, y), nonMatches(steps),
                    "seed 0xED17 trial " + trial);
        }
    }

    @Test
    void knapsackItemsAchieveOptimum() {
        int[] weights = {2, 3, 4};
        int[] values = {4, 5, 7};
        assertEquals(List.of(0, 1), DynamicProgramming.knapsack01Items(weights, values, 5));
        assertArrayEquals(new int[] {2, 3, 4}, weights);
        assertArrayEquals(new int[] {4, 5, 7}, values);
        assertEquals(List.of(), DynamicProgramming.knapsack01Items(new int[0], new int[0], 0));
        assertEquals(List.of(), DynamicProgramming.knapsack01Items(new int[] {1}, new int[] {-4}, 1));
        assertEquals(List.of(0, 1), DynamicProgramming.knapsack01Items(
                new int[] {0, 2}, new int[] {5, 3}, 2));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.knapsack01Items(new int[1], new int[2], 1));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.knapsack01Items(new int[] {-1}, new int[] {1}, 1));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.knapsack01Items(new int[0], new int[0], -1));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.knapsack01Items(new int[0], new int[0], Integer.MAX_VALUE));
        Random random = new Random(0x5AC4);
        for (int trial = 0; trial < 400; trial++) {
            int size = random.nextInt(13);
            int[] w = random.ints(size, 0, 10).toArray();
            int[] v = random.ints(size, -10, 30).toArray();
            int capacity = random.nextInt(30);
            long expected = 0;
            for (int mask = 0; mask < (1 << size); mask++) {
                long weight = 0;
                long value = 0;
                for (int item = 0; item < size; item++) {
                    if ((mask & (1 << item)) != 0) {
                        weight += w[item];
                        value += v[item];
                    }
                }
                if (weight <= capacity) {
                    expected = Math.max(expected, value);
                }
            }
            int[] wCopy = w.clone();
            int[] vCopy = v.clone();
            List<Integer> items = DynamicProgramming.knapsack01Items(w, v, capacity);
            long weight = 0;
            long value = 0;
            int previous = -1;
            for (int item : items) {
                assertTrue(item > previous, "ascending, seed 0x5AC4 trial " + trial);
                previous = item;
                weight += w[item];
                value += v[item];
            }
            assertTrue(weight <= capacity, "seed 0x5AC4 trial " + trial);
            assertEquals(expected, value, "seed 0x5AC4 trial " + trial);
            assertEquals(DynamicProgramming.knapsack01(w, v, capacity), value);
            assertArrayEquals(wCopy, w);
            assertArrayEquals(vCopy, v);
        }
    }

    @Test
    void coinsChoiceMatchesMinimum() {
        int[] coins = {1, 3, 4};
        assertEquals(Optional.of(List.of(3, 3)), DynamicProgramming.minimumCoinsChoice(coins, 6));
        assertArrayEquals(new int[] {1, 3, 4}, coins);
        assertEquals(Optional.empty(), DynamicProgramming.minimumCoinsChoice(new int[] {2, 4}, 7));
        assertEquals(Optional.of(List.of()), DynamicProgramming.minimumCoinsChoice(new int[0], 0));
        assertEquals(Optional.empty(), DynamicProgramming.minimumCoinsChoice(new int[0], 3));
        assertEquals(Optional.of(List.of(3, 3)),
                DynamicProgramming.minimumCoinsChoice(new int[] {3, 3, Integer.MAX_VALUE}, 6));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.minimumCoinsChoice(new int[] {0}, 1));
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProgramming.minimumCoinsChoice(new int[] {1}, -1));
        Random random = new Random(0xC0C0);
        for (int trial = 0; trial < 400; trial++) {
            int[] c = random.ints(random.nextInt(6), 1, 11).toArray();
            int amount = random.nextInt(40);
            int expected = DynamicProgramming.minimumCoins(c, amount);
            Optional<List<Integer>> choice = DynamicProgramming.minimumCoinsChoice(c, amount);
            if (expected < 0) {
                assertFalse(choice.isPresent(), "seed 0xC0C0 trial " + trial);
                continue;
            }
            List<Integer> picked = choice.orElseThrow();
            assertEquals(expected, picked.size(), "seed 0xC0C0 trial " + trial);
            assertEquals(amount, picked.stream().mapToInt(Integer::intValue).sum());
            List<Integer> sorted = new ArrayList<>(picked);
            sorted.sort(null);
            assertEquals(sorted, picked);
            for (int coin : picked) {
                assertTrue(Arrays.stream(c).anyMatch(owned -> owned == coin));
            }
        }
    }

    @Test
    void lisIndicesValidAndOptimal() {
        int[] values = {10, 9, 2, 5, 3, 7, 101, 18};
        assertEquals(List.of(2, 4, 5, 7), DynamicProgramming.longestIncreasingSubsequenceIndices(values));
        assertArrayEquals(new int[] {10, 9, 2, 5, 3, 7, 101, 18}, values);
        assertEquals(List.of(), DynamicProgramming.longestIncreasingSubsequenceIndices(new int[0]));
        assertEquals(List.of(2), DynamicProgramming.longestIncreasingSubsequenceIndices(new int[] {2, 2, 2}));
        assertEquals(List.of(0, 1, 2),
                DynamicProgramming.longestIncreasingSubsequenceIndices(new int[] {1, 2, 3}));
        Random random = new Random(0x115);
        for (int trial = 0; trial < 400; trial++) {
            int[] v = random.ints(random.nextInt(15), -5, 6).toArray();
            int[] best = new int[v.length];
            int expected = 0;
            for (int i = 0; i < v.length; i++) {
                best[i] = 1;
                for (int j = 0; j < i; j++) {
                    if (v[j] < v[i]) {
                        best[i] = Math.max(best[i], best[j] + 1);
                    }
                }
                expected = Math.max(expected, best[i]);
            }
            int[] copy = v.clone();
            List<Integer> indices = DynamicProgramming.longestIncreasingSubsequenceIndices(v);
            assertEquals(expected, indices.size(), "seed 0x115 trial " + trial);
            for (int k = 1; k < indices.size(); k++) {
                assertTrue(indices.get(k) > indices.get(k - 1) && v[indices.get(k)] > v[indices.get(k - 1)],
                        "seed 0x115 trial " + trial);
            }
            assertArrayEquals(copy, v);
        }
    }

    @Test
    void reconstructionRejectsNulls() {
        assertThrows(NullPointerException.class, () -> DynamicProgramming.longestCommonSubsequence(null, ""));
        assertThrows(NullPointerException.class, () -> DynamicProgramming.longestCommonSubsequence("", null));
        assertThrows(NullPointerException.class, () -> DynamicProgramming.editScript(null, ""));
        assertThrows(NullPointerException.class, () -> DynamicProgramming.editScript("", null));
        assertThrows(NullPointerException.class,
                () -> DynamicProgramming.knapsack01Items(null, new int[0], 0));
        assertThrows(NullPointerException.class,
                () -> DynamicProgramming.knapsack01Items(new int[0], null, 0));
        assertThrows(NullPointerException.class, () -> DynamicProgramming.minimumCoinsChoice(null, 0));
        assertThrows(NullPointerException.class,
                () -> DynamicProgramming.longestIncreasingSubsequenceIndices(null));
    }
}
