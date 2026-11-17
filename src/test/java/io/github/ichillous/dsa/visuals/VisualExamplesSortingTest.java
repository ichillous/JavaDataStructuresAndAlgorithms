package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.algorithms.SortAlgorithms;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks every value the sorting figures display. The in-place {@code int[]} sorts expose no
 * intermediate states, so each displayed state is re-derived here from the documented invariant;
 * the final result of each figure is checked against the library itself.
 */
class VisualExamplesSortingTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-sorting.properties");

    @Test
    void insertionStepShiftsLargerValuesAndDropsTheKeyIntoTheGap() {
        int[] values = DATA.ints("sorting.insertion.values");
        int index = DATA.number("sorting.insertion.index");
        int key = values[index];
        assertEquals(DATA.number("sorting.insertion.key"), key);

        int[] prefix = Arrays.copyOf(values, index);
        assertTrue(isSorted(prefix), "the prefix before the step is sorted");
        int[] shifted = Arrays.stream(prefix).filter(value -> value > key).toArray();
        assertArrayEquals(DATA.ints("sorting.insertion.shifted"), shifted);
        assertEquals(index - shifted.length, DATA.number("sorting.insertion.destination"));

        // After i + 1 steps the first i + 1 values are the sorted first i + 1 inputs.
        int[] expectedPrefix = Arrays.copyOf(values, index + 1);
        Arrays.sort(expectedPrefix);
        int[] after = DATA.ints("sorting.insertion.after");
        assertArrayEquals(expectedPrefix, Arrays.copyOf(after, index + 1));
        assertArrayEquals(Arrays.copyOfRange(values, index + 1, values.length),
                Arrays.copyOfRange(after, index + 1, after.length));

        // Running the library for index steps' worth of input reproduces the same prefix.
        int[] viaLibrary = Arrays.copyOf(values, index + 1);
        SortAlgorithms.insertionSort(viaLibrary);
        assertArrayEquals(expectedPrefix, viaLibrary);
        int[] whole = values.clone();
        SortAlgorithms.insertionSort(whole);
        int[] sortedWhole = values.clone();
        Arrays.sort(sortedWhole);
        assertArrayEquals(sortedWhole, whole);
    }

    @Test
    void heapsortStatesFollowTheBuildAndExtractionInvariants() {
        int[] input = DATA.ints("sorting.heap.input");
        int n = input.length;
        int[] built = DATA.ints("sorting.heap.built");
        int[] one = DATA.ints("sorting.heap.afterOne");
        int[] two = DATA.ints("sorting.heap.afterTwo");

        // Reference model of the documented algorithm: bottom-up build, then root-to-end swaps.
        int[] model = input.clone();
        for (int root = n / 2 - 1; root >= 0; root--) {
            siftDown(model, root, n);
        }
        assertArrayEquals(built, model);
        assertTrue(isMaxHeap(built, n));
        assertArrayEquals(sortedCopy(input), sortedCopy(built));

        List<int[]> states = new ArrayList<>();
        for (int end = n - 1; end > 0; end--) {
            int temporary = model[0];
            model[0] = model[end];
            model[end] = temporary;
            siftDown(model, 0, end);
            states.add(model.clone());
        }
        assertArrayEquals(one, states.get(0));
        assertArrayEquals(two, states.get(1));

        int[] sortedValues = sortedCopy(input);
        assertTrue(isMaxHeap(one, n - 1) && isMaxHeap(two, n - 2));
        // The suffix after the heap holds the largest values, sorted, in final position.
        assertArrayEquals(Arrays.copyOfRange(sortedValues, n - 1, n), Arrays.copyOfRange(one, n - 1, n));
        assertArrayEquals(Arrays.copyOfRange(sortedValues, n - 2, n), Arrays.copyOfRange(two, n - 2, n));

        int[] viaLibrary = input.clone();
        SortAlgorithms.heapSort(viaLibrary);
        assertArrayEquals(DATA.ints("sorting.heap.sorted"), viaLibrary);
    }

    @Test
    void countingSortTalliesEachValueThenWritesItBackInOrder() {
        int[] input = DATA.ints("sorting.counting.input");
        int min = Arrays.stream(input).min().getAsInt();
        int max = Arrays.stream(input).max().getAsInt();
        assertEquals(DATA.number("sorting.counting.min"), min);
        assertEquals(DATA.number("sorting.counting.max"), max);
        assertEquals(DATA.number("sorting.counting.range"), max - min + 1);

        int[] counts = new int[max - min + 1];
        for (int value : input) {
            counts[value - min]++;
        }
        assertArrayEquals(DATA.ints("sorting.counting.counts"), counts);

        int[] viaLibrary = input.clone();
        SortAlgorithms.countingSort(viaLibrary);
        assertArrayEquals(DATA.ints("sorting.counting.sorted"), viaLibrary);
    }

    @Test
    void radixPassesAreStableSortsByEachDigitFromTheLeastSignificant() {
        int[] input = DATA.ints("sorting.radix.input");
        assertArrayEquals(DATA.ints("sorting.radix.onesDigits"),
                Arrays.stream(input).map(value -> value % 10).toArray());

        List<Integer> pass = new ArrayList<>(Arrays.stream(input).boxed().toList());
        pass.sort(Comparator.comparingInt(value -> value % 10)); // List.sort is stable
        assertEquals(DATA.list("sorting.radix.afterOnes"), pass);
        assertArrayEquals(DATA.ints("sorting.radix.tensDigits"),
                pass.stream().mapToInt(value -> value / 10 % 10).toArray());

        pass.sort(Comparator.comparingInt(value -> value / 10 % 10));
        assertEquals(DATA.list("sorting.radix.afterTens"), pass);
        for (int i = 1; i < pass.size(); i++) { // ordered by the last two digits
            assertTrue(pass.get(i - 1) % 100 <= pass.get(i) % 100);
        }

        int[] viaLibrary = input.clone();
        SortAlgorithms.radixSort(viaLibrary);
        assertArrayEquals(DATA.ints("sorting.radix.afterTens"), viaLibrary);
    }

    private record Tagged(int key, char tag) {
        static Tagged parse(String text) {
            return new Tagged(text.charAt(0) - '0', text.charAt(1));
        }

        @Override
        public String toString() {
            return key + "" + tag;
        }
    }

    @Test
    void mergeSortKeepsTagOrderAndTheHypotheticalOutcomeReordersEqualKeys() {
        List<String> records = DATA.strings("sorting.stability.records");
        Tagged[] input = records.stream().map(Tagged::parse).toArray(Tagged[]::new);

        Tagged[] viaLibrary = input.clone();
        SortAlgorithms.mergeSort(viaLibrary, Comparator.comparingInt(Tagged::key));
        assertEquals(DATA.strings("sorting.stability.stable"),
                Arrays.stream(viaLibrary).map(Tagged::toString).toList());

        List<Tagged> hypothetical = DATA.strings("sorting.stability.hypothetical").stream()
                .map(Tagged::parse).toList();
        assertEquals(records.stream().sorted().toList(),
                hypothetical.stream().map(Tagged::toString).sorted().toList(), "a permutation");
        for (int i = 1; i < hypothetical.size(); i++) {
            assertTrue(hypothetical.get(i - 1).key() <= hypothetical.get(i).key(), "sorted by key");
        }
        assertTrue(reordersEqualKeys(Arrays.asList(input), hypothetical));
        assertFalse(reordersEqualKeys(Arrays.asList(input), Arrays.asList(viaLibrary)));
        assertNotEquals(Arrays.asList(viaLibrary), hypothetical);
    }

    private static boolean reordersEqualKeys(List<Tagged> input, List<Tagged> outcome) {
        for (int a = 0; a < outcome.size(); a++) {
            for (int b = a + 1; b < outcome.size(); b++) {
                Tagged first = outcome.get(a);
                Tagged second = outcome.get(b);
                if (first.key() == second.key() && input.indexOf(first) > input.indexOf(second)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int[] sortedCopy(int[] values) {
        int[] copy = values.clone();
        Arrays.sort(copy);
        return copy;
    }

    private static boolean isSorted(int[] values) {
        for (int i = 1; i < values.length; i++) {
            if (values[i - 1] > values[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMaxHeap(int[] values, int size) {
        for (int child = 1; child < size; child++) {
            if (values[(child - 1) / 2] < values[child]) {
                return false;
            }
        }
        return true;
    }

    private static void siftDown(int[] values, int root, int size) {
        while (2 * root + 1 < size) {
            int child = 2 * root + 1;
            if (child + 1 < size && values[child] < values[child + 1]) {
                child++;
            }
            if (values[root] >= values[child]) {
                return;
            }
            int temporary = values[root];
            values[root] = values[child];
            values[child] = temporary;
            root = child;
        }
    }
}
