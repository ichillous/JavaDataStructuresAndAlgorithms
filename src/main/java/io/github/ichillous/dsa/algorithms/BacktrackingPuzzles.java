package io.github.ichillous.dsa.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Sudoku constraint search and bounded subset search with explicit validation and ownership. */
public final class BacktrackingPuzzles {
    private static final int SUDOKU_SIZE = 9;
    private static final int ALL_DIGITS = (1 << SUDOKU_SIZE) - 1;
    private static final int MAX_SUBSET_VALUES = 30;

    private BacktrackingPuzzles() {}

    /**
     * Solves a 9-by-9 Sudoku in place, preserving givens, and returns true when a solution exists.
     * Zero means empty; givens must be 1..9 with no duplicates in any row, column, or 3-by-3 box.
     * Returns false without modifying the input if the valid givens have no solution. A solved
     * valid board returns true. This finds one solution and does not test uniqueness.
     *
     * <p>All validation and search use a private copy; only a complete solution is copied back.
     * At each step, search the empty cell with the fewest legal digits (ties by row-major
     * position), and try digits in ascending order. Used-digit bit masks preserve the Sudoku
     * constraints after each placement; backtracking restores them after an unsuccessful choice.
     * For e empty cells the worst-time upper bound is O(e * 9^e), with O(e) recursion space;
     * the copied board and masks have fixed size because this API only accepts 9-by-9 boards.
     *
     * @throws NullPointerException if board itself is null
     * @throws IllegalArgumentException for malformed boards, null or aliased row arrays,
     *         out-of-range cells, or contradictory givens
     */
    public static boolean solveSudoku(int[][] board) {
        Objects.requireNonNull(board, "board");
        if (board.length != SUDOKU_SIZE) {
            throw new IllegalArgumentException("a Sudoku board must have 9 rows");
        }
        int[][] working = new int[SUDOKU_SIZE][];
        int[] rows = new int[SUDOKU_SIZE];
        int[] columns = new int[SUDOKU_SIZE];
        int[] boxes = new int[SUDOKU_SIZE];
        int[] emptyCells = new int[SUDOKU_SIZE * SUDOKU_SIZE];
        int emptyCount = 0;
        for (int row = 0; row < SUDOKU_SIZE; row++) {
            if (board[row] == null || board[row].length != SUDOKU_SIZE) {
                throw new IllegalArgumentException("each Sudoku row must have 9 cells");
            }
            for (int previous = 0; previous < row; previous++) {
                if (board[row] == board[previous]) {
                    throw new IllegalArgumentException("Sudoku rows must be distinct arrays");
                }
            }
            working[row] = board[row].clone();
            for (int column = 0; column < SUDOKU_SIZE; column++) {
                int value = working[row][column];
                if (value < 0 || value > SUDOKU_SIZE) {
                    throw new IllegalArgumentException("Sudoku cells must be between 0 and 9");
                }
                if (value == 0) {
                    emptyCells[emptyCount++] = row * SUDOKU_SIZE + column;
                } else {
                    int box = boxIndex(row, column);
                    int bit = 1 << (value - 1);
                    if (((rows[row] | columns[column] | boxes[box]) & bit) != 0) {
                        throw new IllegalArgumentException("contradictory Sudoku givens");
                    }
                    rows[row] |= bit;
                    columns[column] |= bit;
                    boxes[box] |= bit;
                }
            }
        }
        if (!solve(working, rows, columns, boxes, emptyCells, 0, emptyCount)) {
            return false;
        }
        for (int row = 0; row < SUDOKU_SIZE; row++) {
            System.arraycopy(working[row], 0, board[row], 0, SUDOKU_SIZE);
        }
        return true;
    }

    private static boolean solve(
            int[][] board, int[] rows, int[] columns, int[] boxes,
            int[] emptyCells, int depth, int emptyCount) {
        if (depth == emptyCount) {
            return true;
        }
        int bestPosition = depth;
        int fewestDigits = SUDOKU_SIZE + 1;
        int bestCell = Integer.MAX_VALUE;
        for (int position = depth; position < emptyCount; position++) {
            int cell = emptyCells[position];
            int row = cell / SUDOKU_SIZE;
            int column = cell % SUDOKU_SIZE;
            int candidates = ALL_DIGITS & ~(rows[row] | columns[column] | boxes[boxIndex(row, column)]);
            int count = Integer.bitCount(candidates);
            if (count == 0) {
                return false;
            }
            if (count < fewestDigits || (count == fewestDigits && cell < bestCell)) {
                fewestDigits = count;
                bestCell = cell;
                bestPosition = position;
            }
        }
        swap(emptyCells, depth, bestPosition);
        int row = bestCell / SUDOKU_SIZE;
        int column = bestCell % SUDOKU_SIZE;
        int box = boxIndex(row, column);
        int candidates = ALL_DIGITS & ~(rows[row] | columns[column] | boxes[box]);
        while (candidates != 0) {
            int bit = candidates & -candidates;
            candidates ^= bit;
            board[row][column] = Integer.numberOfTrailingZeros(bit) + 1;
            rows[row] |= bit;
            columns[column] |= bit;
            boxes[box] |= bit;
            if (solve(board, rows, columns, boxes, emptyCells, depth + 1, emptyCount)) {
                return true;
            }
            rows[row] ^= bit;
            columns[column] ^= bit;
            boxes[box] ^= bit;
            board[row][column] = 0;
        }
        swap(emptyCells, depth, bestPosition);
        return false;
    }

    private static int boxIndex(int row, int column) {
        return (row / 3) * 3 + column / 3;
    }

    private static void swap(int[] cells, int first, int second) {
        int temporary = cells[first];
        cells[first] = cells[second];
        cells[second] = temporary;
    }

    /**
     * Returns indices of a subset summing exactly to target, or empty if no subset exists.
     * Each input position is usable once; signed values and duplicates are supported. The
     * returned index list is immutable and ascending, and the input is not modified. Target
     * zero always selects the empty subset. Other ties choose the smallest inclusion bit mask,
     * where input index zero is the least-significant bit (prefer excluding higher indices).
     *
     * <p>Meet in the middle: every subset uniquely splits into left and right halves. Store
     * the smallest left mask for each sum, then enumerate right masks in ascending order to
     * find complementary sums. This yields the specified smallest combined mask. At most 30
     * values are accepted, so each half has at most 32,768 subsets and all intermediate int
     * sums fit in long. Bounds on all possible sums safely reject extreme long targets.
     * Expected O(2^(n/2) + n) time and O(2^(n/2) + n) space, including output, assuming
     * expected constant-time hash-map operations.
     *
     * @throws NullPointerException if values is null
     * @throws IllegalArgumentException if more than 30 values are supplied
     */
    public static Optional<List<Integer>> subsetSum(int[] values, long target) {
        Objects.requireNonNull(values, "values");
        if (values.length > MAX_SUBSET_VALUES) {
            throw new IllegalArgumentException("at most 30 values are supported");
        }
        if (target == 0) {
            return Optional.of(List.of());
        }
        long minimum = 0;
        long maximum = 0;
        for (int value : values) {
            minimum += Math.min(0, value);
            maximum += Math.max(0, value);
        }
        if (target < minimum || target > maximum) {
            return Optional.empty();
        }
        int middle = values.length / 2;
        long[] leftSums = subsetSums(values, 0, middle);
        Map<Long, Integer> leftMaskBySum = new HashMap<>();
        for (int mask = 0; mask < leftSums.length; mask++) {
            leftMaskBySum.putIfAbsent(leftSums[mask], mask);
        }
        long[] rightSums = subsetSums(values, middle, values.length);
        for (int rightMask = 0; rightMask < rightSums.length; rightMask++) {
            Integer leftMask = leftMaskBySum.get(target - rightSums[rightMask]);
            if (leftMask != null) {
                int combinedMask = leftMask | (rightMask << middle);
                List<Integer> indices = new ArrayList<>();
                for (int index = 0; index < values.length; index++) {
                    if ((combinedMask & (1 << index)) != 0) {
                        indices.add(index);
                    }
                }
                return Optional.of(List.copyOf(indices));
            }
        }
        return Optional.empty();
    }

    private static long[] subsetSums(int[] values, int from, int to) {
        long[] sums = new long[1 << (to - from)];
        for (int mask = 1; mask < sums.length; mask++) {
            int bit = Integer.numberOfTrailingZeros(mask);
            sums[mask] = sums[mask & (mask - 1)] + values[from + bit];
        }
        return sums;
    }
}
