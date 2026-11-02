package io.github.ichillous.dsa.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
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
}
