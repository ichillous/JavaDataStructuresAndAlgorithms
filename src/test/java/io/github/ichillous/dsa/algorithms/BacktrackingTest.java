package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BacktrackingTest {
    @Test
    void enumeratesUniquePermutationsInLexicographicOrderWithoutMutation() {
        int[] values = {2, 1, 1};
        assertEquals(List.of(List.of(1, 1, 2), List.of(1, 2, 1), List.of(2, 1, 1)),
                Backtracking.uniquePermutations(values));
        assertArrayEquals(new int[] {2, 1, 1}, values);
        assertEquals(List.of(List.of()), Backtracking.uniquePermutations(new int[0]));
        assertEquals(List.of(List.of(5)), Backtracking.uniquePermutations(new int[] {5}));
        assertEquals(List.of(List.of(1, 1, 1)), Backtracking.uniquePermutations(new int[] {1, 1, 1}));
    }

    @Test
    void uniquePermutationsAgreeWithExhaustivePositionalEnumeration() {
        Random random = new Random(0x9E24);
        for (int trial = 0; trial < 100; trial++) {
            int[] values = random.ints(random.nextInt(8), -2, 3).toArray();
            Set<List<Integer>> expected = new HashSet<>();
            enumeratePositions(values, new boolean[values.length], new ArrayList<>(), expected);
            List<List<Integer>> actual = Backtracking.uniquePermutations(values);
            assertEquals(expected, new HashSet<>(actual), "trial " + trial);
            assertEquals(expected.size(), actual.size(), "no duplicate results, trial " + trial);
            for (int index = 1; index < actual.size(); index++) {
                assertTrue(lexicographicCompare(actual.get(index - 1), actual.get(index)) < 0);
            }
        }
    }

    @Test
    void nQueensMatchesKnownCountsAndEveryBoardHasNoAttacks() {
        int[] counts = {1, 1, 0, 0, 2, 10, 4, 40, 92, 352};
        for (int n = 0; n < counts.length; n++) {
            List<List<Integer>> solutions = Backtracking.nQueens(n);
            assertEquals(counts[n], solutions.size(), "n=" + n);
            assertEquals(solutions.size(), new HashSet<>(solutions).size(), "unique boards");
            for (List<Integer> board : solutions) {
                assertEquals(n, board.size());
                for (int row = 0; row < n; row++) {
                    assertTrue(board.get(row) >= 0 && board.get(row) < n);
                    for (int previous = 0; previous < row; previous++) {
                        assertTrue(!board.get(row).equals(board.get(previous)));
                        assertTrue(Math.abs(board.get(row) - board.get(previous)) != row - previous);
                    }
                }
            }
        }
        assertEquals(List.of(List.of(1, 3, 0, 2), List.of(2, 0, 3, 1)), Backtracking.nQueens(4));
    }

    @Test
    void resultsAndInnerListsAreImmutable() {
        List<List<Integer>> permutations = Backtracking.uniquePermutations(new int[] {1, 2});
        List<List<Integer>> queens = Backtracking.nQueens(4);
        assertThrows(UnsupportedOperationException.class, () -> permutations.add(List.of()));
        assertThrows(UnsupportedOperationException.class, () -> permutations.get(0).add(3));
        assertThrows(UnsupportedOperationException.class, () -> queens.add(List.of()));
        assertThrows(UnsupportedOperationException.class, () -> queens.get(0).add(3));
    }

    @Test
    void rejectsNullAndInputsBeyondDocumentedEnumerationLimits() {
        assertThrows(NullPointerException.class, () -> Backtracking.uniquePermutations(null));
        assertThrows(IllegalArgumentException.class,
                () -> Backtracking.uniquePermutations(new int[10]));
        assertThrows(IllegalArgumentException.class, () -> Backtracking.nQueens(-1));
        assertThrows(IllegalArgumentException.class, () -> Backtracking.nQueens(13));
        assertThrows(IllegalArgumentException.class, () -> Backtracking.nQueens(Integer.MAX_VALUE));
    }

    private static void enumeratePositions(
            int[] values, boolean[] used, List<Integer> current, Set<List<Integer>> results) {
        if (current.size() == values.length) {
            results.add(List.copyOf(current));
            return;
        }
        for (int index = 0; index < values.length; index++) {
            if (!used[index]) {
                used[index] = true;
                current.add(values[index]);
                enumeratePositions(values, used, current, results);
                current.remove(current.size() - 1);
                used[index] = false;
            }
        }
    }

    private static int lexicographicCompare(List<Integer> first, List<Integer> second) {
        for (int index = 0; index < first.size(); index++) {
            int comparison = Integer.compare(first.get(index), second.get(index));
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }
}
