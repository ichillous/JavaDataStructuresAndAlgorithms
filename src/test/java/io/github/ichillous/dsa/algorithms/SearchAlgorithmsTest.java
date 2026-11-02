package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SearchAlgorithmsTest {
    @Test
    void handlesEmptyAndSingletonArrays() {
        assertEquals(-1, SearchAlgorithms.linearSearch(new int[0], 4));
        assertEquals(-1, SearchAlgorithms.binarySearch(new int[0], 4));
        assertEquals(0, SearchAlgorithms.lowerBound(new int[0], 4));
        assertEquals(0, SearchAlgorithms.upperBound(new int[0], 4));
        assertEquals(0, SearchAlgorithms.binarySearch(new int[] {4}, 4));
        assertEquals(-1, SearchAlgorithms.binarySearch(new int[] {4}, 5));
        assertEquals(1, SearchAlgorithms.lowerBound(new int[] {4}, 5));
        assertEquals(1, SearchAlgorithms.upperBound(new int[] {4}, 4));
    }

    @Test
    void findsFirstDuplicateAndExtremeValues() {
        int[] sorted = {Integer.MIN_VALUE, -1, -1, -1, 4, Integer.MAX_VALUE};
        assertEquals(1, SearchAlgorithms.binarySearch(sorted, -1));
        assertEquals(1, SearchAlgorithms.lowerBound(sorted, -1));
        assertEquals(4, SearchAlgorithms.upperBound(sorted, -1));
        assertEquals(0, SearchAlgorithms.binarySearch(sorted, Integer.MIN_VALUE));
        assertEquals(5, SearchAlgorithms.binarySearch(sorted, Integer.MAX_VALUE));
        assertEquals(6, SearchAlgorithms.upperBound(sorted, Integer.MAX_VALUE));
    }

    @Test
    void linearSearchDoesNotRequireSortedInput() {
        assertEquals(1, SearchAlgorithms.linearSearch(new int[] {9, 2, 7, 2}, 2));
        assertEquals(-1, SearchAlgorithms.linearSearch(new int[] {9, 2, 7, 2}, 4));
    }

    @Test
    void searchesMatchIndependentScanAcrossSeededInputs() {
        Random random = new Random(0x5EA2C4);
        for (int trial = 0; trial < 1000; trial++) {
            int[] values = random.ints(random.nextInt(100), -20, 21).toArray();
            Arrays.sort(values);
            int target = random.nextInt(51) - 25;
            int lower = 0;
            while (lower < values.length && values[lower] < target) {
                lower++;
            }
            int upper = lower;
            while (upper < values.length && values[upper] == target) {
                upper++;
            }
            int expected = lower < upper ? lower : -1;
            assertEquals(lower, SearchAlgorithms.lowerBound(values, target), "trial " + trial);
            assertEquals(upper, SearchAlgorithms.upperBound(values, target), "trial " + trial);
            assertEquals(expected, SearchAlgorithms.binarySearch(values, target), "trial " + trial);
            assertEquals(expected, SearchAlgorithms.linearSearch(values, target), "trial " + trial);
        }
    }

    @Test
    void rejectsNullArrays() {
        assertThrows(NullPointerException.class, () -> SearchAlgorithms.linearSearch(null, 0));
        assertThrows(NullPointerException.class, () -> SearchAlgorithms.binarySearch(null, 0));
        assertThrows(NullPointerException.class, () -> SearchAlgorithms.lowerBound(null, 0));
        assertThrows(NullPointerException.class, () -> SearchAlgorithms.upperBound(null, 0));
    }
}
