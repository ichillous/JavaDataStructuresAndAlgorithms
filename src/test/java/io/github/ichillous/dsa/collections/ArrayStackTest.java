package io.github.ichillous.dsa.collections;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ArrayStackTest {
    @Test
    void handlesEmptySingletonAndGrowthInLifoOrder() {
        ArrayStack<Integer> stack = new ArrayStack<>();
        assertTrue(stack.isEmpty());
        assertThrows(NoSuchElementException.class, stack::pop);
        assertThrows(NoSuchElementException.class, stack::peek);
        for (int value = 0; value < 100; value++) {
            stack.push(value);
            assertEquals(value, stack.peek());
        }
        for (int value = 99; value >= 0; value--) {
            assertEquals(value, stack.pop());
        }
        assertEquals(0, stack.size());
        stack.push(7);
        assertEquals(7, stack.pop());
    }

    @Test
    void rejectsNullAndSupportsClearAndReuse() {
        ArrayStack<String> stack = new ArrayStack<>();
        assertThrows(NullPointerException.class, () -> stack.push(null));
        stack.push("a");
        stack.push("b");
        stack.clear();
        stack.clear();
        assertTrue(stack.isEmpty());
        assertThrows(NoSuchElementException.class, stack::peek);
        stack.push("c");
        assertEquals("c", stack.pop());
    }

    @Test
    void randomizedOperationsAgreeWithArrayDeque() {
        Random random = new Random(0x57AC);
        ArrayStack<Integer> actual = new ArrayStack<>();
        Deque<Integer> expected = new ArrayDeque<>();
        for (int step = 0; step < 5_000; step++) {
            int operation = random.nextInt(5);
            if (operation < 2 || expected.isEmpty()) {
                int value = random.nextInt();
                actual.push(value);
                expected.push(value);
            } else if (operation == 2) {
                assertEquals(expected.pop(), actual.pop());
            } else if (operation == 3) {
                assertEquals(expected.peek(), actual.peek());
            } else if (random.nextInt(10) == 0) {
                actual.clear();
                expected.clear();
            }
            assertEquals(expected.size(), actual.size());
            assertEquals(expected.isEmpty(), actual.isEmpty());
            if (!expected.isEmpty()) {
                assertEquals(expected.peek(), actual.peek());
            }
        }
        while (!expected.isEmpty()) {
            assertEquals(expected.pop(), actual.pop());
        }
    }
}
