package io.github.ichillous.dsa.algorithms;

import java.util.Arrays;
import java.util.Objects;

/** Bottom-up dynamic programs with explicit state definitions and no shared mutable state. */
public final class DynamicProgramming {
    private DynamicProgramming() {}

    /**
     * Returns the fewest coins summing to amount, or {@code -1} if impossible; amount zero needs
     * zero coins. Each strictly positive denomination may be reused. Duplicate denominations
     * are allowed. O(amount * number of coins) time and O(amount) extra space.
     *
     * @throws IllegalArgumentException for nonpositive coins, negative amount, or an amount
     *         that cannot be represented by an indexed DP array
     */
    public static int minimumCoins(int[] coins, int amount) {
        Objects.requireNonNull(coins, "coins");
        checkArrayStateSize(amount, "amount");
        for (int coin : coins) {
            if (coin <= 0) {
                throw new IllegalArgumentException("coin denominations must be positive");
            }
        }
        // best[a] is the minimum number of coins for exactly a; MAX_VALUE means unreachable.
        int[] best = new int[amount + 1];
        Arrays.fill(best, Integer.MAX_VALUE);
        best[0] = 0;
        for (int current = 1; current <= amount; current++) {
            for (int coin : coins) {
                if (coin <= current && best[current - coin] != Integer.MAX_VALUE) {
                    best[current] = Math.min(best[current], best[current - coin] + 1);
                }
            }
        }
        return best[amount] == Integer.MAX_VALUE ? -1 : best[amount];
    }

    /**
     * Returns the maximum total value of a subset whose total weight is at most capacity.
     * Each item is usable once. Zero weights and negative values are allowed; selecting nothing
     * gives value zero. A {@code long} result avoids overflow when summing int values.
     * O(number of items * capacity) time, O(capacity) extra space (plus O(number of items) validation).
     *
     * @throws IllegalArgumentException for mismatched lengths, negative weights/capacity, or a
     *         capacity that cannot be represented by an indexed DP array
     */
    public static long knapsack01(int[] weights, int[] values, int capacity) {
        Objects.requireNonNull(weights, "weights");
        Objects.requireNonNull(values, "values");
        if (weights.length != values.length) {
            throw new IllegalArgumentException("weights and values must have equal lengths");
        }
        checkArrayStateSize(capacity, "capacity");
        for (int weight : weights) {
            if (weight < 0) {
                throw new IllegalArgumentException("weights must be nonnegative");
            }
        }
        long[] best = new long[capacity + 1];
        for (int item = 0; item < weights.length; item++) {
            // Descend so the current item cannot be consumed more than once.
            for (int remaining = capacity; remaining >= weights[item]; remaining--) {
                best[remaining] = Math.max(
                        best[remaining], best[remaining - weights[item]] + values[item]);
            }
        }
        return best[capacity];
    }

    /**
     * Returns the longest common subsequence length measured in UTF-16 code units.
     * O(mn) time and O(min(m,n)) extra space; empty strings have result zero.
     */
    public static int longestCommonSubsequenceLength(String first, String second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        if (first.length() < second.length()) {
            return longestCommonSubsequenceLength(second, first);
        }
        int[] previous = new int[second.length() + 1];
        int[] current = new int[second.length() + 1];
        for (int row = 1; row <= first.length(); row++) {
            for (int column = 1; column <= second.length(); column++) {
                current[column] = first.charAt(row - 1) == second.charAt(column - 1)
                        ? previous[column - 1] + 1
                        : Math.max(previous[column], current[column - 1]);
            }
            int[] temporary = previous;
            previous = current;
            current = temporary;
        }
        return previous[second.length()];
    }

    /**
     * Returns the longest strictly increasing subsequence length. Equal values do not extend a
     * subsequence. O(n log n) time and O(n) extra space; input is not modified.
     */
    public static int longestIncreasingSubsequenceLength(int[] values) {
        Objects.requireNonNull(values, "values");
        int[] tails = new int[values.length];
        int length = 0;
        for (int value : values) {
            // tails[i] is the smallest final value seen for an increasing subsequence of i+1.
            int low = 0;
            int high = length;
            while (low < high) {
                int middle = low + (high - low) / 2;
                if (tails[middle] < value) {
                    low = middle + 1;
                } else {
                    high = middle;
                }
            }
            tails[low] = value;
            if (low == length) {
                length++;
            }
        }
        return length;
    }

    /**
     * Returns unit-cost Levenshtein distance (insert, delete, replace) in UTF-16 code units.
     * O(mn) time and O(min(m,n)) extra space.
     */
    public static int editDistance(String first, String second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        if (first.length() < second.length()) {
            return editDistance(second, first);
        }
        int[] previous = new int[second.length() + 1];
        int[] current = new int[second.length() + 1];
        for (int column = 0; column <= second.length(); column++) {
            previous[column] = column;
        }
        for (int row = 1; row <= first.length(); row++) {
            current[0] = row;
            for (int column = 1; column <= second.length(); column++) {
                int replacement = previous[column - 1]
                        + (first.charAt(row - 1) == second.charAt(column - 1) ? 0 : 1);
                current[column] = Math.min(
                        replacement, Math.min(previous[column], current[column - 1]) + 1);
            }
            int[] temporary = previous;
            previous = current;
            current = temporary;
        }
        return previous[second.length()];
    }

    private static void checkArrayStateSize(int maximumState, String name) {
        if (maximumState < 0 || maximumState == Integer.MAX_VALUE) {
            throw new IllegalArgumentException(name + " must be between 0 and Integer.MAX_VALUE - 1");
        }
    }
}
