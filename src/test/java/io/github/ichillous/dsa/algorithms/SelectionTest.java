package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SelectionTest {
    private static final long SEED = 0x5E1EC7L;

    private record Keyed(int key, int id) {}

    private record Tagged(int key, String tag) {}

    @Test
    void kthSmallestMatchesSortedCopy() {
        Random random = new Random(SEED);
        for (int round = 0; round < 2_000; round++) {
            int length = random.nextInt(41);
            boolean small = round % 2 == 0;
            int[] values = new int[length];
            for (int i = 0; i < length; i++) {
                values[i] = small ? random.nextInt(11) - 5 : random.nextInt();
            }
            int[] original = values.clone();
            int[] sorted = values.clone();
            Arrays.sort(sorted);
            for (int k = 0; k < length; k++) {
                String message = "seed=" + Long.toHexString(SEED) + " round=" + round + " k=" + k;
                assertEquals(sorted[k], Selection.kthSmallest(values, k), message);
                assertEquals(sorted[k], Selection.kthSmallestWithRoundLimit(values, k, 0), message);
            }
            assertArrayEquals(original, values, "input mutated, round=" + round);
        }
    }

    @Test
    void kthSmallestHandlesExtremeValues() {
        int[] values = {Integer.MAX_VALUE, Integer.MIN_VALUE, 0, Integer.MAX_VALUE, Integer.MIN_VALUE};
        assertEquals(Integer.MIN_VALUE, Selection.kthSmallest(values, 0));
        assertEquals(Integer.MIN_VALUE, Selection.kthSmallest(values, 1));
        assertEquals(0, Selection.kthSmallest(values, 2));
        assertEquals(Integer.MAX_VALUE, Selection.kthSmallest(values, 4));
    }

    @Test
    void kthSmallestHandlesAdversarialShapes() {
        int n = 10_000;
        int[][] shapes = new int[4][n];
        for (int i = 0; i < n; i++) {
            shapes[0][i] = 7;
            shapes[1][i] = i;
            shapes[2][i] = n - i;
            shapes[3][i] = i < n / 2 ? i : n - i;
        }
        for (int[] shape : shapes) {
            int[] original = shape.clone();
            int[] sorted = shape.clone();
            Arrays.sort(sorted);
            assertEquals(sorted[n / 2], Selection.kthSmallest(shape, n / 2));
            assertEquals(sorted[n / 2], Selection.kthSmallestWithRoundLimit(shape, n / 2, 0));
            assertEquals(sorted[0], Selection.kthSmallest(shape, 0));
            assertEquals(sorted[n - 1], Selection.kthSmallest(shape, n - 1));
            assertArrayEquals(original, shape);
        }
    }

    @Test
    void kthSmallestRejectsBadIndex() {
        int[] values = {3, 1, 2};
        assertThrows(IndexOutOfBoundsException.class, () -> Selection.kthSmallest(values, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> Selection.kthSmallest(values, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> Selection.kthSmallest(new int[0], 0));
        assertThrows(NullPointerException.class, () -> Selection.kthSmallest(null, 0));
        assertEquals(42, Selection.kthSmallest(new int[] {42}, 0));
    }

    @Test
    void smallestKIsStableAndBounded() {
        Random random = new Random(SEED);
        Comparator<Keyed> order = Comparator.comparingInt(Keyed::key);
        for (int round = 0; round < 300; round++) {
            int n = random.nextInt(40);
            List<Keyed> values = new ArrayList<>();
            for (int id = 0; id < n; id++) {
                values.add(new Keyed(random.nextInt(6), id));
            }
            List<Keyed> original = new ArrayList<>(values);
            for (int k = 0; k <= n + 2; k++) {
                List<Keyed> expected = values.stream().sorted(order).limit(k).toList();
                assertEquals(expected, Selection.smallestK(values, k, order),
                        "seed=" + Long.toHexString(SEED) + " round=" + round + " k=" + k);
            }
            assertEquals(original, values);
        }
        List<Keyed> sample = List.of(new Keyed(2, 0), new Keyed(1, 1), new Keyed(1, 2));
        assertEquals(List.of(), Selection.smallestK(sample, 0, order));
        assertEquals(List.of(sample.get(1), sample.get(2), sample.get(0)),
                Selection.smallestK(sample, 5, order));
        assertEquals(List.of(sample.get(1)), Selection.smallestK(sample, 1, order));
        assertThrows(IllegalArgumentException.class, () -> Selection.smallestK(sample, -1, order));
        assertThrows(NullPointerException.class, () -> Selection.smallestK(null, 1, order));
        assertThrows(NullPointerException.class, () -> Selection.smallestK(sample, 1, null));
        Collection<Integer> withNull = new ArrayList<>(Arrays.asList(1, null));
        assertThrows(NullPointerException.class,
                () -> Selection.smallestK(withNull, 1, Comparator.<Integer>naturalOrder()));
        assertThrows(UnsupportedOperationException.class,
                () -> Selection.smallestK(sample, 2, order).add(sample.get(0)));
    }

    @Test
    void smallestKAcceptsHugeKWithoutPreallocatingIt() {
        assertEquals(List.of(1, 2, 3),
                Selection.smallestK(List.of(3, 1, 2), Integer.MAX_VALUE, Comparator.<Integer>naturalOrder()));
    }

    @Test
    void mergeSortedMatchesSortedConcatenationAndTieRule() {
        Comparator<Tagged> order = Comparator.comparingInt(Tagged::key);
        List<List<Tagged>> lists = List.of(
                List.of(new Tagged(1, "a0"), new Tagged(3, "a1"), new Tagged(5, "a2")),
                List.of(new Tagged(1, "b0"), new Tagged(2, "b1")),
                List.of(),
                List.of(new Tagged(5, "d0")));
        List<String> tags = Selection.mergeSorted(lists, order).stream().map(Tagged::tag).toList();
        assertEquals(List.of("a0", "b0", "b1", "a1", "a2", "d0"), tags);
        assertEquals(List.of(),
                Selection.mergeSorted(List.<List<Integer>>of(), Comparator.<Integer>naturalOrder()));
        assertEquals(List.of(), Selection.mergeSorted(List.of(List.<Integer>of(), List.<Integer>of()),
                Comparator.<Integer>naturalOrder()));

        Random random = new Random(SEED);
        for (int round = 0; round < 300; round++) {
            int listCount = random.nextInt(6);
            List<List<Tagged>> generated = new ArrayList<>();
            List<Tagged> concatenation = new ArrayList<>();
            for (int l = 0; l < listCount; l++) {
                int size = random.nextInt(8);
                List<Tagged> list = new LinkedList<>();
                int[] keys = random.ints(size, 0, 6).sorted().toArray();
                for (int p = 0; p < size; p++) {
                    list.add(new Tagged(keys[p], l + ":" + p));
                }
                generated.add(list);
                concatenation.addAll(list);
            }
            List<Tagged> expected = concatenation.stream().sorted(order).toList();
            assertEquals(expected, Selection.mergeSorted(generated, order),
                    "seed=" + Long.toHexString(SEED) + " round=" + round);
        }

        Comparator<Integer> natural = Comparator.naturalOrder();
        assertThrows(IllegalArgumentException.class, () -> Selection.mergeSorted(List.of(List.of(2, 1)), natural));
        assertThrows(IllegalArgumentException.class,
                () -> Selection.mergeSorted(List.of(List.of(1, 2), List.of(0, 5, 4)), natural));
        assertThrows(NullPointerException.class, () -> Selection.mergeSorted(null, natural));
        assertThrows(NullPointerException.class, () -> Selection.mergeSorted(List.of(List.of(1)), null));
        List<List<Integer>> nullList = new ArrayList<>();
        nullList.add(null);
        assertThrows(NullPointerException.class, () -> Selection.mergeSorted(nullList, natural));
        assertThrows(NullPointerException.class,
                () -> Selection.mergeSorted(List.of(Arrays.asList(1, null)), natural));
        assertThrows(UnsupportedOperationException.class,
                () -> Selection.mergeSorted(List.of(List.of(1)), natural).add(2));
    }

    @Test
    void majorityElementMatchesCounting() {
        assertEquals(OptionalInt.of(3), Selection.majorityElement(new int[] {3, 3, 4}));
        assertEquals(OptionalInt.empty(), Selection.majorityElement(new int[] {1, 2}));
        assertEquals(OptionalInt.empty(), Selection.majorityElement(new int[0]));
        assertEquals(OptionalInt.of(7), Selection.majorityElement(new int[] {7}));
        assertEquals(OptionalInt.empty(), Selection.majorityElement(new int[] {1, 1, 2, 2}));
        assertEquals(OptionalInt.of(Integer.MIN_VALUE),
                Selection.majorityElement(new int[] {1, Integer.MIN_VALUE, Integer.MIN_VALUE}));
        assertThrows(NullPointerException.class, () -> Selection.majorityElement(null));

        Random random = new Random(SEED);
        for (int round = 0; round < 2_000; round++) {
            int n = random.nextInt(25);
            int range = 1 + random.nextInt(4);
            int[] values = random.ints(n, 0, range).toArray();
            int[] original = values.clone();
            Map<Integer, Integer> counts = new HashMap<>();
            for (int v : values) {
                counts.merge(v, 1, Integer::sum);
            }
            OptionalInt expected = counts.entrySet().stream()
                    .filter(e -> e.getValue() * 2 > n)
                    .mapToInt(Map.Entry::getKey)
                    .findFirst();
            assertEquals(expected, Selection.majorityElement(values),
                    "seed=" + Long.toHexString(SEED) + " round=" + round + " " + Arrays.toString(values));
            assertArrayEquals(original, values);
        }
    }
}
