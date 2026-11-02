package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayDeque;
import java.util.Arrays;
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
}
