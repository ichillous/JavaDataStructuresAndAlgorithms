package io.github.ichillous.dsa.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.Test;

class RingDequeTest {
    @Test
    void emptyDequeRejectsPeeksAndRemovalsWithoutChangingState() {
        RingDeque<Integer> deque = new RingDeque<>();
        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
        assertThrows(NoSuchElementException.class, deque::peekFirst);
        assertThrows(NoSuchElementException.class, deque::peekLast);
        assertThrows(NoSuchElementException.class, deque::removeFirst);
        assertThrows(NoSuchElementException.class, deque::removeLast);
        assertTrue(deque.isEmpty());
        deque.addLast(1);
        assertEquals(1, deque.removeFirst());
    }

    @Test
    void singletonCanBeAddedAndRemovedThroughEveryEndCombination() {
        RingDeque<Integer> deque = new RingDeque<>(1);
        for (int value = 0; value < 100; value++) {
            deque.addFirst(value);
            assertEquals(value, deque.peekFirst());
            assertEquals(value, deque.peekLast());
            assertEquals(value, deque.removeLast());
            deque.addLast(value);
            assertEquals(value, deque.removeFirst());
            deque.addFirst(value);
            assertEquals(value, deque.removeFirst());
            deque.addLast(value);
            assertEquals(value, deque.removeLast());
            assertTrue(deque.isEmpty());
        }
    }

    @Test
    void wrappedBufferGrowsWithoutChangingLogicalOrder() {
        RingDeque<Integer> deque = new RingDeque<>(3);
        deque.addLast(1);
        deque.addLast(2);
        deque.addLast(3);
        assertEquals(1, deque.removeFirst());
        deque.addLast(4);
        deque.addFirst(0);
        assertEquals(4, deque.removeLast());
        deque.addLast(5);
        assertEquals(0, deque.removeFirst());
        assertEquals(2, deque.removeFirst());
        assertEquals(3, deque.removeFirst());
        assertEquals(5, deque.removeFirst());
        assertTrue(deque.isEmpty());
    }

    @Test
    void frontGrowthAndAlternatingRemovalsPreserveBothEnds() {
        RingDeque<Integer> deque = new RingDeque<>(1);
        ArrayDeque<Integer> expected = new ArrayDeque<>();
        for (int value = 0; value < 2000; value++) {
            deque.addFirst(value);
            expected.addFirst(value);
        }
        while (!expected.isEmpty()) {
            assertEquals(expected.removeFirst(), deque.removeFirst());
            if (!expected.isEmpty()) {
                assertEquals(expected.removeLast(), deque.removeLast());
            }
        }
        assertTrue(deque.isEmpty());
    }

    @Test
    void rejectsNonpositiveCapacityAndNullElements() {
        assertThrows(IllegalArgumentException.class, () -> new RingDeque<>(0));
        assertThrows(IllegalArgumentException.class, () -> new RingDeque<>(-1));
        RingDeque<String> deque = new RingDeque<>(1);
        deque.addLast("kept");
        assertThrows(NullPointerException.class, () -> deque.addFirst(null));
        assertThrows(NullPointerException.class, () -> deque.addLast(null));
        assertEquals(1, deque.size());
        assertEquals("kept", deque.removeLast());
    }

    @Test
    void clearOfWrappedBufferSupportsReuseThroughBothEnds() {
        RingDeque<Integer> deque = new RingDeque<>(3);
        deque.addLast(1);
        deque.addLast(2);
        deque.removeFirst();
        deque.addLast(3);
        deque.addLast(4);
        deque.clear();
        deque.clear();
        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
        deque.addFirst(5);
        deque.addLast(6);
        assertFalse(deque.isEmpty());
        assertEquals(5, deque.removeFirst());
        assertEquals(6, deque.removeLast());
    }

    @Test
    void seededMixedOperationsMatchArrayDequeFromSmallCapacities() {
        Random random = new Random(0xA16DE);
        for (int initialCapacity : new int[] {1, 2, 3, 8}) {
            RingDeque<Integer> deque = new RingDeque<>(initialCapacity);
            ArrayDeque<Integer> expected = new ArrayDeque<>();
            for (int operation = 0; operation < 12_000; operation++) {
                int value = random.nextInt();
                switch (random.nextInt(12)) {
                    case 0, 1, 2 -> {
                        expected.addFirst(value);
                        deque.addFirst(value);
                    }
                    case 3, 4, 5 -> {
                        expected.addLast(value);
                        deque.addLast(value);
                    }
                    case 6 -> {
                        if (expected.isEmpty()) {
                            assertThrows(NoSuchElementException.class, deque::removeFirst);
                        } else {
                            assertEquals(expected.removeFirst(), deque.removeFirst());
                        }
                    }
                    case 7 -> {
                        if (expected.isEmpty()) {
                            assertThrows(NoSuchElementException.class, deque::removeLast);
                        } else {
                            assertEquals(expected.removeLast(), deque.removeLast());
                        }
                    }
                    case 8, 9 -> {
                        if (expected.isEmpty()) {
                            assertThrows(NoSuchElementException.class, deque::peekFirst);
                            assertThrows(NoSuchElementException.class, deque::peekLast);
                        } else {
                            assertEquals(expected.peekFirst(), deque.peekFirst());
                            assertEquals(expected.peekLast(), deque.peekLast());
                        }
                    }
                    default -> {
                        if (random.nextInt(100) == 0) {
                            expected.clear();
                            deque.clear();
                        }
                    }
                }
                assertEquals(expected.size(), deque.size());
                assertEquals(expected.isEmpty(), deque.isEmpty());
            }
            while (!expected.isEmpty()) {
                assertEquals(expected.removeFirst(), deque.removeFirst());
            }
            assertTrue(deque.isEmpty());
        }
    }
}
