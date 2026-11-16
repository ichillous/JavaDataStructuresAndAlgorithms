package io.github.ichillous.dsa.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BinaryMinHeapTest {
    @Test
    void emptyHeapRejectsPeekAndRemoveThenSupportsReuse() {
        BinaryMinHeap<Integer> heap = new BinaryMinHeap<>();
        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
        assertThrows(NoSuchElementException.class, heap::peek);
        assertThrows(NoSuchElementException.class, heap::remove);
        heap.add(7);
        assertEquals(7, heap.peek());
        assertEquals(7, heap.remove());
        assertTrue(heap.isEmpty());
        heap.add(-1);
        assertEquals(-1, heap.remove());
    }

    @Test
    void handlesDuplicatesNegativeAndExtremeValuesWithoutSubtractionOverflow() {
        BinaryMinHeap<Integer> heap = new BinaryMinHeap<>();
        for (int value : new int[] {0, Integer.MAX_VALUE, -7, Integer.MIN_VALUE, 0, 1}) {
            heap.add(value);
        }
        assertEquals(List.of(Integer.MIN_VALUE, -7, 0, 0, 1, Integer.MAX_VALUE), drain(heap));
    }

    @Test
    void bottomUpHeapifyCopiesInputAndHandlesEvenLastChild() {
        List<Integer> input = new ArrayList<>(List.of(8, 7, 6, 5, 4, 3, 2, 1));
        BinaryMinHeap<Integer> heap = new BinaryMinHeap<>(input);
        input.set(7, 100);
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), drain(heap));
        assertEquals(100, input.get(7));
        assertTrue(new BinaryMinHeap<Integer>(List.of()).isEmpty());
    }

    @Test
    void genericNaturalOrderingWorksForStrings() {
        BinaryMinHeap<String> heap = new BinaryMinHeap<>(List.of("pear", "apple", "banana"));
        assertEquals("apple", heap.remove());
        assertEquals("banana", heap.remove());
        assertEquals("pear", heap.remove());
    }

    @Test
    void nullValuesAndNullCollectionsAreRejected() {
        BinaryMinHeap<Integer> heap = new BinaryMinHeap<>();
        assertThrows(NullPointerException.class, () -> heap.add(null));
        assertThrows(NullPointerException.class, () -> new BinaryMinHeap<Integer>(null));
        assertThrows(NullPointerException.class,
                () -> new BinaryMinHeap<>(Arrays.asList(1, null, 2)));
        assertTrue(heap.isEmpty());
    }

    @Test
    void seededMixedOperationsAgreeWithPriorityQueue() {
        Random random = new Random(0xB1A);
        PriorityQueue<Integer> expected = new PriorityQueue<>();
        BinaryMinHeap<Integer> heap = new BinaryMinHeap<>();
        for (int operation = 0; operation < 12_000; operation++) {
            if (expected.isEmpty() || random.nextBoolean()) {
                int value = random.nextInt();
                expected.add(value);
                heap.add(value);
            } else {
                assertEquals(expected.remove(), heap.remove());
            }
            assertEquals(expected.size(), heap.size());
            assertEquals(expected.isEmpty(), heap.isEmpty());
            if (!expected.isEmpty()) {
                assertEquals(expected.peek(), heap.peek());
            }
        }
        while (!expected.isEmpty()) {
            assertEquals(expected.remove(), heap.remove());
        }
    }

    private static List<Integer> drain(BinaryMinHeap<Integer> heap) {
        List<Integer> result = new ArrayList<>();
        while (!heap.isEmpty()) {
            result.add(heap.remove());
        }
        return result;
    }

    private static final class Fragile implements Comparable<Fragile> {
        static boolean failing;
        final int key;

        Fragile(int key) {
            this.key = key;
        }

        @Override
        public int compareTo(Fragile other) {
            if (failing) {
                throw new IllegalStateException("comparison failed");
            }
            return Integer.compare(key, other.key);
        }
    }

    private static List<Integer> drainKeys(BinaryMinHeap<Fragile> heap) {
        List<Integer> keys = new ArrayList<>();
        while (!heap.isEmpty()) {
            keys.add(heap.remove().key);
        }
        return keys;
    }

    @Test
    void addAndRemoveAreFailureAtomicWhenCompareToThrows() {
        for (int size = 1; size <= 12; size++) {
            for (int newKey : new int[] {-5, 3, 100}) {
                Fragile.failing = false;
                BinaryMinHeap<Fragile> heap = new BinaryMinHeap<>();
                List<Integer> expected = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    int key = (i * 7) % 11 + 1;
                    heap.add(new Fragile(key));
                    expected.add(key);
                }
                Collections.sort(expected);
                Fragile candidate = new Fragile(newKey);
                Fragile.failing = true;
                try {
                    // Adding to a nonempty heap always compares with the parent first.
                    assertThrows(IllegalStateException.class, () -> heap.add(candidate));
                    assertEquals(size, heap.size());
                    if (size >= 3) {
                        // With fewer than three elements remove needs no comparison.
                        assertThrows(IllegalStateException.class, heap::remove);
                        assertEquals(size, heap.size());
                    }
                } finally {
                    Fragile.failing = false;
                }
                assertEquals(expected, drainKeys(heap), "size " + size + " key " + newKey);
            }
        }
    }
}
