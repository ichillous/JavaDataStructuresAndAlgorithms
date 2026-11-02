package io.github.ichillous.dsa.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Small, deliberately bounded enumeration examples. Results and their inner lists are immutable. */
public final class Backtracking {
    private Backtracking() {}

    /**
     * Returns each distinct permutation once, in lexicographic order, without modifying input.
     * The empty input has one empty permutation. Inputs are limited to nine elements because
     * this method materializes all results. O(n * n!) worst time and O(n + n * k) space for k
     * distinct results (including output).
     *
     * @throws IllegalArgumentException if the input has more than nine elements
     */
    public static List<List<Integer>> uniquePermutations(int[] values) {
        Objects.requireNonNull(values, "values");
        if (values.length > 9) {
            throw new IllegalArgumentException("at most 9 elements may be enumerated");
        }
        int[] sorted = values.clone();
        Arrays.sort(sorted);
        List<List<Integer>> results = new ArrayList<>();
        permute(sorted, new boolean[sorted.length], new ArrayList<>(), results);
        return List.copyOf(results);
    }

    private static void permute(
            int[] values, boolean[] used, List<Integer> current, List<List<Integer>> results) {
        if (current.size() == values.length) {
            results.add(List.copyOf(current));
            return;
        }
        for (int index = 0; index < values.length; index++) {
            if (used[index]
                    || (index > 0 && values[index] == values[index - 1] && !used[index - 1])) {
                continue;
            }
            used[index] = true;
            current.add(values[index]);
            permute(values, used, current, results);
            current.remove(current.size() - 1);
            used[index] = false;
        }
    }

    /**
     * Returns N-queens solutions as row-to-column lists, in lexicographic order. For n=0 there
     * is one empty solution. O(n * n!) upper-bound time and O(n + n * k) space including k
     * solutions. Sizes are limited to 0..12 to bound accidental enumeration.
     *
     * @throws IllegalArgumentException if n is outside 0..12
     */
    public static List<List<Integer>> nQueens(int n) {
        if (n < 0 || n > 12) {
            throw new IllegalArgumentException("board size must be between 0 and 12");
        }
        List<List<Integer>> results = new ArrayList<>();
        placeQueens(n, new boolean[n], new boolean[Math.max(0, 2 * n - 1)],
                new boolean[Math.max(0, 2 * n - 1)], new ArrayList<>(), results);
        return List.copyOf(results);
    }

    private static void placeQueens(
            int n, boolean[] columns, boolean[] down, boolean[] up,
            List<Integer> current, List<List<Integer>> results) {
        int row = current.size();
        if (row == n) {
            results.add(List.copyOf(current));
            return;
        }
        for (int column = 0; column < n; column++) {
            int downIndex = row - column + n - 1;
            int upIndex = row + column;
            if (columns[column] || down[downIndex] || up[upIndex]) {
                continue;
            }
            columns[column] = true;
            down[downIndex] = true;
            up[upIndex] = true;
            current.add(column);
            placeQueens(n, columns, down, up, current, results);
            current.remove(current.size() - 1);
            columns[column] = false;
            down[downIndex] = false;
            up[upIndex] = false;
        }
    }
}
