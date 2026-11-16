package io.github.ichillous.dsa.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Bottom-up dynamic programs with explicit state definitions and no shared mutable state. */
public final class DynamicProgramming {
    private DynamicProgramming() {}

    /**
     * Returns the fewest coins summing to amount, or {@code -1} if impossible; amount zero needs
     * zero coins. Each strictly positive denomination may be reused. Duplicate denominations
     * are allowed. O(amount * number of coins) time and O(amount) extra space.
     *
     * @throws IllegalArgumentException for nonpositive coins, negative amount, or an amount
     *         whose table (amount + 1 entries) would exceed {@code Integer.MAX_VALUE - 8}.
     *         Valid but large amounts are pseudopolynomial and can still exhaust memory
     *         ({@link OutOfMemoryError}).
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
     *         capacity whose table (capacity + 1 entries) would exceed
     *         {@code Integer.MAX_VALUE - 8}. Valid but large capacities are pseudopolynomial and
     *         can still exhaust memory ({@link OutOfMemoryError}).
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

    /**
     * Returns one longest common subsequence of the two strings, measured in UTF-16 code units
     * (a surrogate pair may be split). The empty string results when either input is empty or
     * nothing is shared.
     *
     * <p>Tie rule (deterministic): build the full table {@code dp[i][j]}, then backtrack from
     * {@code (a.length, b.length)}. Equal final units are taken; otherwise move up
     * ({@code i--}) when {@code dp[i-1][j] >= dp[i][j-1]}, else move left ({@code j--}).
     * Its length equals {@link #longestCommonSubsequenceLength}. Inputs are not modified.
     * O(mn) time and O(mn) auxiliary space (the full table is kept so the path can be
     * walked), which can exhaust memory ({@link OutOfMemoryError}) for very long strings.
     *
     * @throws NullPointerException if either argument is null
     * @throws IllegalArgumentException if a table row of {@code b.length() + 1} entries would
     *         exceed {@code Integer.MAX_VALUE - 8}
     */
    public static String longestCommonSubsequence(String a, String b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        checkArrayStateSize(b.length(), "length of b");
        int[][] table = new int[a.length() + 1][b.length() + 1];
        for (int row = 1; row <= a.length(); row++) {
            for (int column = 1; column <= b.length(); column++) {
                table[row][column] = a.charAt(row - 1) == b.charAt(column - 1)
                        ? table[row - 1][column - 1] + 1
                        : Math.max(table[row - 1][column], table[row][column - 1]);
            }
        }
        StringBuilder reversed = new StringBuilder(table[a.length()][b.length()]);
        int row = a.length();
        int column = b.length();
        while (row > 0 && column > 0) {
            if (a.charAt(row - 1) == b.charAt(column - 1)) {
                reversed.append(a.charAt(row - 1));
                row--;
                column--;
            } else if (table[row - 1][column] >= table[row][column - 1]) {
                row--;
            } else {
                column--;
            }
        }
        return reversed.reverse().toString();
    }

    /** The kind of one step in an {@link #editScript edit script}. */
    public enum EditOperation {
        /** Source and target units are equal; both cursors advance, cost 0. */
        MATCH,
        /** Replace the source unit with the target unit; both cursors advance, cost 1. */
        SUBSTITUTE,
        /** Insert the target unit; only the target cursor advances, cost 1. */
        INSERT,
        /** Delete the source unit; only the source cursor advances, cost 1. */
        DELETE
    }

    /**
     * One step of an edit script. The indices are cursor positions <em>before</em> the step
     * is applied, in the original source and in the target: {@code MATCH} and
     * {@code SUBSTITUTE} consume source unit {@code sourceIndex} and target unit
     * {@code targetIndex}; {@code DELETE} consumes source unit {@code sourceIndex} (the target
     * cursor is {@code targetIndex}); {@code INSERT} emits target unit {@code targetIndex}
     * (the source cursor is {@code sourceIndex}).
     *
     * @param operation the step kind, not null
     * @param sourceIndex the source cursor, at least 0
     * @param targetIndex the target cursor, at least 0
     */
    public record EditStep(EditOperation operation, int sourceIndex, int targetIndex) {
        /** Validates the step. */
        public EditStep {
            Objects.requireNonNull(operation, "operation");
            if (sourceIndex < 0 || targetIndex < 0) {
                throw new IllegalArgumentException("cursor positions must be nonnegative");
            }
        }
    }

    /**
     * Returns a minimum-cost script (unit-cost insert, delete, substitute; UTF-16 units) that
     * turns {@code source} into {@code target}, in left-to-right order. The number of non-
     * {@link EditOperation#MATCH} steps equals {@link #editDistance}.
     *
     * <p>Tie rule (deterministic): fill the full table, then backtrack from the ends. At each
     * cell prefer the diagonal step (MATCH if units are equal, otherwise SUBSTITUTE) when it
     * is optimal, then DELETE, then INSERT. Because the walk runs from the end, ties are
     * resolved for the last operations first. Inputs are not modified; the list is immutable.
     * O(mn) time and O(mn) auxiliary space, which can exhaust memory for very long strings.
     *
     * @throws NullPointerException if either argument is null
     * @throws IllegalArgumentException if a table row of {@code target.length() + 1} entries
     *         would exceed {@code Integer.MAX_VALUE - 8}
     */
    public static List<EditStep> editScript(String source, String target) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        checkArrayStateSize(target.length(), "length of target");
        int[][] table = new int[source.length() + 1][target.length() + 1];
        for (int row = 0; row <= source.length(); row++) {
            table[row][0] = row;
        }
        for (int column = 0; column <= target.length(); column++) {
            table[0][column] = column;
        }
        for (int row = 1; row <= source.length(); row++) {
            for (int column = 1; column <= target.length(); column++) {
                int diagonal = table[row - 1][column - 1]
                        + (source.charAt(row - 1) == target.charAt(column - 1) ? 0 : 1);
                table[row][column] = Math.min(diagonal,
                        Math.min(table[row - 1][column], table[row][column - 1]) + 1);
            }
        }
        List<EditStep> reversed = new ArrayList<>();
        int row = source.length();
        int column = target.length();
        while (row > 0 || column > 0) {
            if (row > 0 && column > 0) {
                boolean equal = source.charAt(row - 1) == target.charAt(column - 1);
                if (table[row][column] == table[row - 1][column - 1] + (equal ? 0 : 1)) {
                    reversed.add(new EditStep(
                            equal ? EditOperation.MATCH : EditOperation.SUBSTITUTE, row - 1, column - 1));
                    row--;
                    column--;
                    continue;
                }
            }
            if (row > 0 && table[row][column] == table[row - 1][column] + 1) {
                reversed.add(new EditStep(EditOperation.DELETE, row - 1, column));
                row--;
            } else {
                reversed.add(new EditStep(EditOperation.INSERT, row, column - 1));
                column--;
            }
        }
        Collections.reverse(reversed);
        return List.copyOf(reversed);
    }

    /**
     * Returns the indices (ascending) of one optimal 0/1 knapsack selection: the values at those
     * indices sum to {@link #knapsack01} and the weights sum to at most {@code capacity}.
     * Same preconditions and exceptions as {@link #knapsack01}.
     *
     * <p>Tie rule (deterministic): items are processed in index order with capacities
     * descending, and an item is recorded as "taken" at a capacity only when taking it is
     * strictly better than the best without it. Backtracking walks items from last to first and
     * takes item {@code i} at remaining capacity {@code w} exactly when it was recorded there.
     * Among equal-value optima this prefers solutions built from lower-indexed items, and
     * never adds an item that does not strictly improve the value (so zero- and
     * negative-value items are omitted).
     *
     * <p>O(n * capacity) time. Auxiliary space is O(capacity) {@code long}s plus one bit per
     * (item, capacity) pair, held as one {@link BitSet} row per item, so memory is about
     * n * (capacity + 1) / 8 bytes; large products can exhaust memory
     * ({@link OutOfMemoryError}). Inputs are not modified; the list is immutable.
     */
    public static List<Integer> knapsack01Items(int[] weights, int[] values, int capacity) {
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
        BitSet[] taken = new BitSet[weights.length];
        for (int item = 0; item < weights.length; item++) {
            taken[item] = new BitSet(capacity + 1);
            for (int remaining = capacity; remaining >= weights[item]; remaining--) {
                long candidate = best[remaining - weights[item]] + values[item];
                if (candidate > best[remaining]) {
                    best[remaining] = candidate;
                    taken[item].set(remaining);
                }
            }
        }
        List<Integer> chosen = new ArrayList<>();
        int remaining = capacity;
        for (int item = weights.length - 1; item >= 0; item--) {
            if (taken[item].get(remaining)) {
                chosen.add(item);
                remaining -= weights[item];
            }
        }
        Collections.reverse(chosen);
        return List.copyOf(chosen);
    }

    /**
     * Returns one multiset of coins (values in ascending order) achieving {@link #minimumCoins},
     * or an empty {@link Optional} when the amount is unreachable. Amount zero gives
     * {@code Optional.of(List.of())}. Same preconditions and exceptions as {@link #minimumCoins}.
     *
     * <p>Tie rule (deterministic): for each amount, coins are tried in the order given and the
     * recorded last coin changes only on a strictly smaller count, so the first listed coin
     * among equally good ones wins. The chosen coins are then sorted ascending. O(amount *
     * number of coins) time and O(amount) auxiliary space; inputs are not modified.
     */
    public static Optional<List<Integer>> minimumCoinsChoice(int[] coins, int amount) {
        Objects.requireNonNull(coins, "coins");
        checkArrayStateSize(amount, "amount");
        for (int coin : coins) {
            if (coin <= 0) {
                throw new IllegalArgumentException("coin denominations must be positive");
            }
        }
        int[] best = new int[amount + 1];
        int[] last = new int[amount + 1];
        Arrays.fill(best, Integer.MAX_VALUE);
        best[0] = 0;
        for (int current = 1; current <= amount; current++) {
            for (int coin : coins) {
                if (coin <= current && best[current - coin] != Integer.MAX_VALUE
                        && best[current - coin] + 1 < best[current]) {
                    best[current] = best[current - coin] + 1;
                    last[current] = coin;
                }
            }
        }
        if (best[amount] == Integer.MAX_VALUE) {
            return Optional.empty();
        }
        List<Integer> chosen = new ArrayList<>(best[amount]);
        for (int remaining = amount; remaining > 0; remaining -= last[remaining]) {
            chosen.add(last[remaining]);
        }
        Collections.sort(chosen);
        return Optional.of(List.copyOf(chosen));
    }

    /**
     * Returns the indices (ascending) of one longest strictly increasing subsequence; empty for
     * an empty array. Equal values never extend a subsequence.
     *
     * <p>Tie rule (deterministic): patience sorting with a lower-bound search keeps, for each
     * length, the index of the smallest (latest on ties) tail seen so far, and each element
     * remembers the tail index one length shorter at the moment it was placed. The result is
     * rebuilt from the tail of the longest length through those links, so it is one specific
     * optimum, not necessarily the lexicographically smallest.
     *
     * <p>O(n log n) time and O(n) auxiliary space; the input is not modified and the list is
     * immutable.
     *
     * @throws NullPointerException if values is null
     */
    public static List<Integer> longestIncreasingSubsequenceIndices(int[] values) {
        Objects.requireNonNull(values, "values");
        int[] tailIndex = new int[values.length];
        int[] previous = new int[values.length];
        int length = 0;
        for (int index = 0; index < values.length; index++) {
            int low = 0;
            int high = length;
            while (low < high) {
                int middle = low + (high - low) / 2;
                if (values[tailIndex[middle]] < values[index]) {
                    low = middle + 1;
                } else {
                    high = middle;
                }
            }
            previous[index] = low == 0 ? -1 : tailIndex[low - 1];
            tailIndex[low] = index;
            if (low == length) {
                length++;
            }
        }
        List<Integer> indices = new ArrayList<>(length);
        for (int index = length == 0 ? -1 : tailIndex[length - 1]; index >= 0; index = previous[index]) {
            indices.add(index);
        }
        Collections.reverse(indices);
        return List.copyOf(indices);
    }

    static void checkArrayStateSize(int maximumState, String name) {
        // The table has maximumState + 1 entries, which must not exceed Integer.MAX_VALUE - 8.
        if (maximumState < 0 || maximumState > Integer.MAX_VALUE - 9) {
            throw new IllegalArgumentException(name + " must be between 0 and Integer.MAX_VALUE - 9");
        }
    }
}
