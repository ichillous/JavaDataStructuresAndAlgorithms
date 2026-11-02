package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BacktrackingPuzzlesTest {
    @Test
    void solvesKnownSudokuAndPreservesGivensAndRowArrays() {
        int[][] board = classicPuzzle();
        int[][] original = copy(board);
        int[][] rowReferences = board.clone();
        assertTrue(BacktrackingPuzzles.solveSudoku(board));
        assertBoardEquals(solvedBoard(), board);
        assertValidSolution(board, original);
        for (int row = 0; row < 9; row++) {
            assertSame(rowReferences[row], board[row]);
        }
    }

    @Test
    void validSolvedBoardIsAcceptedAndInvalidSolvedBoardIsRejectedWithoutMutation() {
        int[][] board = solvedBoard();
        int[][] original = copy(board);
        assertTrue(BacktrackingPuzzles.solveSudoku(board));
        assertBoardEquals(original, board);
        board[0][0] = board[0][1];
        int[][] invalidOriginal = copy(board);
        assertThrows(IllegalArgumentException.class, () -> BacktrackingPuzzles.solveSudoku(board));
        assertBoardEquals(invalidOriginal, board);
    }

    @Test
    void emptySudokuHasAValidDeterministicSolution() {
        int[][] first = new int[9][9];
        int[][] second = new int[9][9];
        assertTrue(BacktrackingPuzzles.solveSudoku(first));
        assertTrue(BacktrackingPuzzles.solveSudoku(second));
        assertValidSolution(first, new int[9][9]);
        assertBoardEquals(first, second);
    }

    @Test
    void unsatisfiableButNoncontradictoryGivensReturnFalseWithoutMutation() {
        int[][] blockedCell = new int[9][9];
        for (int column = 0; column < 8; column++) {
            blockedCell[0][column] = column + 1;
        }
        blockedCell[1][8] = 9;
        int[][] blockedOriginal = copy(blockedCell);
        assertFalse(BacktrackingPuzzles.solveSudoku(blockedCell));
        assertBoardEquals(blockedOriginal, blockedCell);

        int[][] inconsistentCompletion = classicPuzzle();
        inconsistentCompletion[0][2] = 1; // Individually legal, but the unique puzzle needs 4 here.
        int[][] original = copy(inconsistentCompletion);
        assertFalse(BacktrackingPuzzles.solveSudoku(inconsistentCompletion));
        assertBoardEquals(original, inconsistentCompletion);
    }

    @Test
    void rejectsMalformedAndAliasedSudokuBoards() {
        assertThrows(NullPointerException.class, () -> BacktrackingPuzzles.solveSudoku(null));
        assertThrows(IllegalArgumentException.class, () -> BacktrackingPuzzles.solveSudoku(new int[8][9]));
        assertThrows(IllegalArgumentException.class, () -> BacktrackingPuzzles.solveSudoku(new int[10][9]));
        int[][] nullRow = new int[9][9];
        nullRow[4] = null;
        assertThrows(IllegalArgumentException.class, () -> BacktrackingPuzzles.solveSudoku(nullRow));
        int[][] shortRow = new int[9][9];
        shortRow[4] = new int[8];
        assertThrows(IllegalArgumentException.class, () -> BacktrackingPuzzles.solveSudoku(shortRow));
        int[][] longRow = new int[9][9];
        longRow[4] = new int[10];
        assertThrows(IllegalArgumentException.class, () -> BacktrackingPuzzles.solveSudoku(longRow));
        int[][] aliased = new int[9][];
        int[] shared = new int[9];
        Arrays.fill(aliased, shared);
        assertThrows(IllegalArgumentException.class, () -> BacktrackingPuzzles.solveSudoku(aliased));
        for (int[] row : aliased) {
            assertSame(shared, row);
            assertArrayEquals(new int[9], row);
        }
    }

    @Test
    void rejectsOutOfRangeAndContradictoryGivensWithoutMutation() {
        for (int invalid : new int[] {-1, 10, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            int[][] board = new int[9][9];
            board[8][8] = invalid;
            int[][] original = copy(board);
            assertThrows(IllegalArgumentException.class, () -> BacktrackingPuzzles.solveSudoku(board));
            assertBoardEquals(original, board);
        }
        int[][] rowConflict = new int[9][9];
        rowConflict[0][0] = 1;
        rowConflict[0][8] = 1;
        int[][] columnConflict = new int[9][9];
        columnConflict[0][0] = 1;
        columnConflict[8][0] = 1;
        int[][] boxConflict = new int[9][9];
        boxConflict[0][0] = 1;
        boxConflict[1][1] = 1;
        for (int[][] board : new int[][][] {rowConflict, columnConflict, boxConflict}) {
            int[][] original = copy(board);
            assertThrows(IllegalArgumentException.class, () -> BacktrackingPuzzles.solveSudoku(board));
            assertBoardEquals(original, board);
        }
    }

    @Test
    void solvesSeededPuzzlesDerivedFromValidBoards() {
        long seed = 0x5AD04A;
        Random random = new Random(seed);
        for (int trial = 0; trial < 30; trial++) {
            int[][] puzzle = solvedBoard();
            List<Integer> digits = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
            java.util.Collections.shuffle(digits, random);
            for (int row = 0; row < 9; row++) {
                for (int column = 0; column < 9; column++) {
                    puzzle[row][column] = random.nextInt(4) == 0
                            ? digits.get(puzzle[row][column] - 1) : 0;
                }
            }
            int[][] givens = copy(puzzle);
            assertTrue(BacktrackingPuzzles.solveSudoku(puzzle), "seed=" + seed + ", trial=" + trial);
            assertValidSolution(puzzle, givens);
        }
    }

    @Test
    void subsetSumSupportsSignedValuesDuplicatesAndTheEmptySubset() {
        assertEquals(Optional.of(List.of()), BacktrackingPuzzles.subsetSum(new int[0], 0));
        assertEquals(Optional.empty(), BacktrackingPuzzles.subsetSum(new int[0], 1));
        assertEquals(Optional.of(List.of()), BacktrackingPuzzles.subsetSum(new int[] {0, -1, 1}, 0));
        assertEquals(Optional.of(List.of(0, 1, 2)),
                BacktrackingPuzzles.subsetSum(new int[] {-8, 5, 4}, 1));
        assertEquals(Optional.of(List.of(0)), BacktrackingPuzzles.subsetSum(new int[] {5, 5, 0}, 5));
        assertEquals(Optional.empty(), BacktrackingPuzzles.subsetSum(new int[] {2, 4, -8}, 3));
    }

    @Test
    void subsetSumChoosesTheSmallestInclusionMask() {
        assertEquals(Optional.of(List.of(0, 1)), BacktrackingPuzzles.subsetSum(new int[] {2, 3, 5}, 5));
        assertEquals(Optional.of(List.of(0)), BacktrackingPuzzles.subsetSum(new int[] {5, 2, 3}, 5));
        // Mask 2 for index 1 beats mask 5 for indices 0,2; this is not list-lexicographic order.
        assertEquals(Optional.of(List.of(1)), BacktrackingPuzzles.subsetSum(new int[] {2, 5, 3}, 5));
    }

    @Test
    void subsetSumHandlesLongTargetsAndExtremeIntValues() {
        int[] values = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE};
        assertEquals(Optional.of(List.of(0, 1)), BacktrackingPuzzles.subsetSum(values, 2L * Integer.MAX_VALUE));
        assertEquals(Optional.of(List.of(0, 2)), BacktrackingPuzzles.subsetSum(values, -1));
        assertEquals(Optional.empty(), BacktrackingPuzzles.subsetSum(values, Long.MAX_VALUE));
        assertEquals(Optional.empty(), BacktrackingPuzzles.subsetSum(values, Long.MIN_VALUE));
    }

    @Test
    void subsetSumSupportsTheDocumentedThirtyValueBoundary() {
        int[] values = new int[30];
        Arrays.fill(values, Integer.MAX_VALUE);
        List<Integer> expected = new ArrayList<>();
        for (int index = 0; index < values.length; index++) {
            expected.add(index);
        }
        assertEquals(Optional.of(expected), BacktrackingPuzzles.subsetSum(values, 30L * Integer.MAX_VALUE));
    }

    @Test
    void subsetSumMatchesExhaustiveMasksWithoutInputMutation() {
        long seed = 0x5AB5E7;
        Random random = new Random(seed);
        for (int trial = 0; trial < 500; trial++) {
            int[] values = random.ints(random.nextInt(13), -20, 21).toArray();
            int[] original = values.clone();
            long target = random.nextInt(121) - 60;
            Optional<List<Integer>> expected = Optional.empty();
            for (int mask = 0; mask < (1 << values.length); mask++) {
                long sum = 0;
                List<Integer> indices = new ArrayList<>();
                for (int index = 0; index < values.length; index++) {
                    if ((mask & (1 << index)) != 0) {
                        sum += values[index];
                        indices.add(index);
                    }
                }
                if (sum == target) {
                    expected = Optional.of(indices);
                    break;
                }
            }
            assertEquals(expected, BacktrackingPuzzles.subsetSum(values, target),
                    "seed=" + seed + ", trial=" + trial);
            assertArrayEquals(original, values);
        }
    }

    @Test
    void subsetSumRejectsNullAndOversizedInputAndReturnsImmutableIndices() {
        assertThrows(NullPointerException.class, () -> BacktrackingPuzzles.subsetSum(null, 0));
        assertThrows(IllegalArgumentException.class, () -> BacktrackingPuzzles.subsetSum(new int[31], 1));
        assertThrows(IllegalArgumentException.class, () -> BacktrackingPuzzles.subsetSum(new int[31], 0));
        List<Integer> indices = BacktrackingPuzzles.subsetSum(new int[] {1}, 1).orElseThrow();
        assertThrows(UnsupportedOperationException.class, () -> indices.add(0));
    }

    private static int[][] classicPuzzle() {
        return new int[][] {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };
    }

    private static int[][] solvedBoard() {
        return new int[][] {
            {5, 3, 4, 6, 7, 8, 9, 1, 2},
            {6, 7, 2, 1, 9, 5, 3, 4, 8},
            {1, 9, 8, 3, 4, 2, 5, 6, 7},
            {8, 5, 9, 7, 6, 1, 4, 2, 3},
            {4, 2, 6, 8, 5, 3, 7, 9, 1},
            {7, 1, 3, 9, 2, 4, 8, 5, 6},
            {9, 6, 1, 5, 3, 7, 2, 8, 4},
            {2, 8, 7, 4, 1, 9, 6, 3, 5},
            {3, 4, 5, 2, 8, 6, 1, 7, 9}
        };
    }

    private static int[][] copy(int[][] board) {
        return Arrays.stream(board).map(int[]::clone).toArray(int[][]::new);
    }

    private static void assertBoardEquals(int[][] expected, int[][] actual) {
        assertEquals(expected.length, actual.length);
        for (int row = 0; row < expected.length; row++) {
            assertArrayEquals(expected[row], actual[row], "row=" + row);
        }
    }

    private static void assertValidSolution(int[][] solution, int[][] givens) {
        Set<Integer> allDigits = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        for (int index = 0; index < 9; index++) {
            Set<Integer> row = new HashSet<>();
            Set<Integer> column = new HashSet<>();
            Set<Integer> box = new HashSet<>();
            for (int offset = 0; offset < 9; offset++) {
                row.add(solution[index][offset]);
                column.add(solution[offset][index]);
                box.add(solution[(index / 3) * 3 + offset / 3][(index % 3) * 3 + offset % 3]);
                if (givens[index][offset] != 0) {
                    assertEquals(givens[index][offset], solution[index][offset], "givens preserved");
                }
            }
            assertEquals(allDigits, row, "row=" + index);
            assertEquals(allDigits, column, "column=" + index);
            assertEquals(allDigits, box, "box=" + index);
        }
    }
}
