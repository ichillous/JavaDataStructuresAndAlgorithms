package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Comparator;
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

    @Test
    void countingAndRadixMatchArraysSort() {
        long seed = 0xC0DAL;
        Random random = new Random(seed);
        for (int trial = 0; trial < 1_000; trial++) {
            int length = random.nextInt(61);
            int[] small = random.ints(length, -3, 4).toArray();
            int[] medium = random.ints(length, 0, 1_001).toArray();
            int[] full = random.ints(length).toArray();
            for (int[] values : new int[][] {small, medium, full}) {
                int[] expected = values.clone();
                Arrays.sort(expected);
                int[] radix = values.clone();
                SortAlgorithms.radixSort(radix);
                assertArrayEquals(expected, radix, "radix seed " + seed + " trial " + trial);
                if (values != full) {
                    int[] counting = values.clone();
                    SortAlgorithms.countingSort(counting);
                    assertArrayEquals(expected, counting, "counting seed " + seed + " trial " + trial);
                }
            }
        }
    }

    @Test
    void radixSortOrdersExtremes() {
        int[] values = {Integer.MAX_VALUE, Integer.MIN_VALUE, 0, -1, 1, Integer.MIN_VALUE};
        SortAlgorithms.radixSort(values);
        assertArrayEquals(
                new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE}, values);
    }

    @Test
    void countingSortRejectsHugeRangeWithoutMutation() {
        int[] huge = {Integer.MIN_VALUE, Integer.MAX_VALUE};
        assertThrows(IllegalArgumentException.class, () -> SortAlgorithms.countingSort(huge));
        assertArrayEquals(new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE}, huge);

        int[] justOver = {0, SortAlgorithms.COUNTING_SORT_MAX_RANGE};
        assertThrows(IllegalArgumentException.class, () -> SortAlgorithms.countingSort(justOver));
        assertArrayEquals(new int[] {0, SortAlgorithms.COUNTING_SORT_MAX_RANGE}, justOver);

        int[] widest = {5 + (1 << 20) - 1, 5, 7};
        SortAlgorithms.countingSort(widest);
        assertArrayEquals(new int[] {5, 7, 5 + (1 << 20) - 1}, widest);

        int[] atMax = {Integer.MAX_VALUE, Integer.MAX_VALUE - 3};
        SortAlgorithms.countingSort(atMax);
        assertArrayEquals(new int[] {Integer.MAX_VALUE - 3, Integer.MAX_VALUE}, atMax);
    }

    @Test
    void countingAndRadixHandleEmptySingletonAndNull() {
        for (Consumer<int[]> sort : java.util.List.<Consumer<int[]>>of(
                SortAlgorithms::countingSort, SortAlgorithms::radixSort)) {
            int[] empty = {};
            sort.accept(empty);
            assertEquals(0, empty.length);
            int[] one = {Integer.MIN_VALUE};
            sort.accept(one);
            assertArrayEquals(new int[] {Integer.MIN_VALUE}, one);
            assertThrows(NullPointerException.class, () -> sort.accept(null));
        }
    }

    private record Item(int key, int seq) {}

    @Test
    void comparatorMergeSortIsStable() {
        long seed = 0x57AB1EL;
        Random random = new Random(seed);
        Comparator<Item> byKey = Comparator.comparingInt(Item::key);
        for (int trial = 0; trial < 300; trial++) {
            Item[] items = new Item[random.nextInt(80)];
            for (int index = 0; index < items.length; index++) {
                items[index] = new Item(random.nextInt(6), index);
            }
            Item[] expected = items.clone();
            Arrays.sort(expected, byKey);
            SortAlgorithms.mergeSort(items, byKey);
            // Item is a record, so assertArrayEquals compares key and seq element by element.
            assertArrayEquals(expected, items, "seed " + seed + " trial " + trial);
        }
    }

    @Test
    void comparatorMergeSortHandlesEmptySingletonAndExtremes() {
        Integer[] empty = {};
        SortAlgorithms.mergeSort(empty, Comparator.naturalOrder());
        assertEquals(0, empty.length);
        Integer[] one = {7};
        SortAlgorithms.mergeSort(one, Comparator.naturalOrder());
        assertArrayEquals(new Integer[] {7}, one);

        // A subtraction comparator would overflow here; Integer.compare does not.
        Integer[] extremes = {Integer.MAX_VALUE, Integer.MIN_VALUE, 0, -1};
        SortAlgorithms.mergeSort(extremes, Integer::compare);
        assertArrayEquals(new Integer[] {Integer.MIN_VALUE, -1, 0, Integer.MAX_VALUE}, extremes);

        String[] words = {"pear", "fig", "apple", "kiwi"};
        SortAlgorithms.mergeSort(words, Comparator.comparingInt(String::length).reversed());
        assertArrayEquals(new String[] {"apple", "pear", "kiwi", "fig"}, words);
    }

    @Test
    void comparatorMergeSortRejectsNullsWithoutMutation() {
        Integer[] values = {3, 1, null, 2};
        assertThrows(NullPointerException.class,
                () -> SortAlgorithms.mergeSort(values, Comparator.naturalOrder()));
        assertArrayEquals(new Integer[] {3, 1, null, 2}, values);
        assertThrows(NullPointerException.class,
                () -> SortAlgorithms.mergeSort((Integer[]) null, Comparator.<Integer>naturalOrder()));
        assertThrows(NullPointerException.class,
                () -> SortAlgorithms.mergeSort(new Integer[] {1}, null));
    }
}
