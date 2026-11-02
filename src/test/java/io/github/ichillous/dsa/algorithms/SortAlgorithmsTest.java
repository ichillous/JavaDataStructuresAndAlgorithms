package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SortAlgorithmsTest {
    static Stream<Arguments> sorts() {
        return Stream.of(
                Arguments.of("insertion", (Consumer<int[]>) SortAlgorithms::insertionSort),
                Arguments.of("bubble", (Consumer<int[]>) SortAlgorithms::bubbleSort),
                Arguments.of("merge", (Consumer<int[]>) SortAlgorithms::mergeSort),
                Arguments.of("quick", (Consumer<int[]>) SortAlgorithms::quickSort),
                Arguments.of("heap", (Consumer<int[]>) SortAlgorithms::heapSort));
    }

    @ParameterizedTest(name = "{0} sorts edge cases")
    @MethodSource("sorts")
    void sortsEdgeCases(String name, Consumer<int[]> sort) {
        int[][] cases = {
            {}, {1}, {2, 1}, {1, 2, 3}, {5, 4, 3, 2, 1}, {7, 7, 7, 7},
            {Integer.MAX_VALUE, 0, Integer.MIN_VALUE, -1, Integer.MAX_VALUE},
            {5, 1, 5, 2, 5, 3, 5, 4}
        };
        for (int[] values : cases) {
            int[] expected = values.clone();
            Arrays.sort(expected);
            sort.accept(values);
            assertArrayEquals(expected, values, name);
        }
    }

    @ParameterizedTest(name = "{0} matches Arrays.sort on seeded inputs")
    @MethodSource("sorts")
    void matchesJdkSort(String name, Consumer<int[]> sort) {
        Random random = new Random(0x5027);
        for (int trial = 0; trial < 500; trial++) {
            int[] values = random.ints(random.nextInt(151)).toArray();
            if (trial % 2 == 0) {
                for (int index = 0; index < values.length; index++) {
                    values[index] %= 5;
                }
            }
            int[] expected = values.clone();
            Arrays.sort(expected);
            sort.accept(values);
            assertArrayEquals(expected, values, name + " trial " + trial);
        }
    }

    @ParameterizedTest(name = "{0} rejects null")
    @MethodSource("sorts")
    void rejectsNull(String name, Consumer<int[]> sort) {
        assertThrows(NullPointerException.class, () -> sort.accept(null), name);
    }

    @Test
    void quickSortHandlesLargeSortedReverseAndEqualArraysWithoutDeepRecursion() {
        int size = 200_000;
        int[] ascending = new int[size];
        int[] descending = new int[size];
        int[] equal = new int[size];
        Arrays.fill(equal, 8);
        for (int index = 0; index < size; index++) {
            ascending[index] = index;
            descending[index] = size - index - 1;
        }
        int[] expected = ascending.clone();
        SortAlgorithms.quickSort(ascending);
        SortAlgorithms.quickSort(descending);
        SortAlgorithms.quickSort(equal);
        assertArrayEquals(expected, ascending);
        assertArrayEquals(expected, descending);
        int[] expectedEqual = new int[size];
        Arrays.fill(expectedEqual, 8);
        assertArrayEquals(expectedEqual, equal);
    }
}
