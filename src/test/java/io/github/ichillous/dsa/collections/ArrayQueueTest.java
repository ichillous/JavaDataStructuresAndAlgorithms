package io.github.ichillous.dsa.collections;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ArrayQueueTest {
    @Test
    void growsWhileWrappedWithoutReorderingElements() {
        ArrayQueue<Integer> queue = new ArrayQueue<>(3);
        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);
        assertEquals(1, queue.dequeue());
        assertEquals(2, queue.dequeue());
        queue.enqueue(4);
        queue.enqueue(5); // Physical order is [4, 5, 3].
        queue.enqueue(6); // Resize must copy logical order [3, 4, 5].
        for (int value = 3; value <= 6; value++) {
            assertEquals(value, queue.peek());
            assertEquals(value, queue.dequeue());
        }
        assertTrue(queue.isEmpty());
    }

    @Test
    void singletonCapacityWrapsAndGrows() {
        ArrayQueue<Integer> queue = new ArrayQueue<>(1);
        queue.enqueue(1);
        assertEquals(1, queue.dequeue());
        queue.enqueue(2);
        queue.enqueue(3);
        assertEquals(2, queue.dequeue());
        assertEquals(3, queue.dequeue());
    }

    @Test
    void validatesCapacityNullAndEmptyOperations() {
        assertThrows(IllegalArgumentException.class, () -> new ArrayQueue<>(0));
        assertThrows(IllegalArgumentException.class, () -> new ArrayQueue<>(-1));
        ArrayQueue<Integer> queue = new ArrayQueue<>();
        assertThrows(NoSuchElementException.class, queue::dequeue);
        assertThrows(NoSuchElementException.class, queue::peek);
        assertThrows(NullPointerException.class, () -> queue.enqueue(null));
        assertEquals(0, queue.size());
    }

    @Test
    void clearResetsWrappedBufferAndAllowsReuse() {
        ArrayQueue<Integer> queue = new ArrayQueue<>(2);
        queue.enqueue(1);
        queue.enqueue(2);
        queue.dequeue();
        queue.enqueue(3);
        queue.clear();
        queue.clear();
        assertTrue(queue.isEmpty());
        queue.enqueue(4);
        queue.enqueue(5);
        assertEquals(4, queue.dequeue());
        assertEquals(5, queue.dequeue());
    }

    @Test
    void randomizedOperationsAgreeWithArrayDeque() {
        Random random = new Random(0xC1AC13);
        ArrayQueue<Integer> actual = new ArrayQueue<>(1);
        Deque<Integer> expected = new ArrayDeque<>();
        for (int step = 0; step < 10_000; step++) {
            int operation = random.nextInt(6);
            if (operation < 3 || expected.isEmpty()) {
                int value = random.nextInt();
                actual.enqueue(value);
                expected.addLast(value);
            } else if (operation == 3) {
                assertEquals(expected.removeFirst(), actual.dequeue());
            } else if (operation == 4) {
                assertEquals(expected.peekFirst(), actual.peek());
            } else if (random.nextInt(20) == 0) {
                expected.clear();
                actual.clear();
            }
            assertEquals(expected.size(), actual.size(), "size at step " + step);
            assertEquals(expected.isEmpty(), actual.isEmpty());
            if (!expected.isEmpty()) {
                assertEquals(expected.peekFirst(), actual.peek());
            }
        }
        while (!expected.isEmpty()) {
            assertEquals(expected.removeFirst(), actual.dequeue());
        }
        assertTrue(actual.isEmpty());
    }
}
