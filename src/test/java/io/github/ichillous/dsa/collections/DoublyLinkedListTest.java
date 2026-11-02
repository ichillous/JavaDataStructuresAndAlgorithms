package io.github.ichillous.dsa.collections;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DoublyLinkedListTest {
    @Test
    void maintainsBothEndsAcrossSingletonTransitions() {
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        list.addFirst("a");
        assertEquals("a", list.removeLast());
        list.addLast("b");
        assertEquals("b", list.removeFirst());
        list.addFirst("c");
        list.addFirst("b");
        list.addLast("d");
        assertEquals(List.of("b", "c", "d"), snapshot(list));
        assertEquals("b", list.removeFirst());
        assertEquals("d", list.removeLast());
        assertEquals("c", list.removeFirst());
        assertTrue(list.isEmpty());
    }

    @Test
    void accessesAndRemovesNodesFromEitherHalf() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
        for (int value = 0; value < 8; value++) {
            list.addLast(value);
        }
        assertEquals(1, list.get(1));
        assertEquals(6, list.get(6));
        assertEquals(2, list.set(2, 20));
        assertEquals(20, list.remove(2));
        assertEquals(6, list.remove(5));
        assertEquals(0, list.remove(0));
        assertEquals(7, list.remove(list.size() - 1));
        assertEquals(List.of(1, 3, 4, 5), snapshot(list));
    }

    @Test
    void validatesEmptyOperationsIndicesAndNulls() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
        assertThrows(NoSuchElementException.class, list::removeFirst);
        assertThrows(NoSuchElementException.class, list::removeLast);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        assertThrows(NullPointerException.class, () -> list.addFirst(null));
        assertThrows(NullPointerException.class, () -> list.addLast(null));
        list.addLast(1);
        assertThrows(NullPointerException.class, () -> list.set(0, null));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(1));
        assertEquals(List.of(1), snapshot(list));
    }

    @Test
    void clearReleasesBothEndsAndAllowsReuse() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
        list.clear();
        list.addFirst(1);
        list.addLast(2);
        list.clear();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        list.addLast(3);
        list.addFirst(2);
        assertEquals(3, list.removeLast());
        assertEquals(2, list.removeLast());
    }

    @Test
    void iteratorExhaustionAndStructuralChangeAreExplicit() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
        assertThrows(NoSuchElementException.class, list.iterator()::next);
        list.addLast(1);
        Iterator<Integer> replacement = list.iterator();
        list.set(0, 2);
        assertEquals(2, replacement.next());
        assertFalse(replacement.hasNext());
        assertThrows(NoSuchElementException.class, replacement::next);
        assertThrows(UnsupportedOperationException.class, replacement::remove);
        Iterator<Integer> addition = list.iterator();
        list.addFirst(0);
        assertThrows(ConcurrentModificationException.class, addition::hasNext);
        Iterator<Integer> removal = list.iterator();
        list.removeLast();
        assertThrows(ConcurrentModificationException.class, removal::next);
        Iterator<Integer> clearing = list.iterator();
        list.clear();
        assertThrows(ConcurrentModificationException.class, clearing::next);
    }

    @Test
    void randomizedOperationsAgreeWithLinkedList() {
        Random random = new Random(0x11A57);
        DoublyLinkedList<Integer> actual = new DoublyLinkedList<>();
        LinkedList<Integer> expected = new LinkedList<>();
        for (int step = 0; step < 5_000; step++) {
            int operation = random.nextInt(9);
            int value = random.nextInt();
            if (operation == 0 || expected.isEmpty()) {
                actual.addFirst(value);
                expected.addFirst(value);
            } else if (operation == 1) {
                actual.addLast(value);
                expected.addLast(value);
            } else if (operation == 2) {
                assertEquals(expected.removeFirst(), actual.removeFirst());
            } else if (operation == 3) {
                assertEquals(expected.removeLast(), actual.removeLast());
            } else if (operation == 4) {
                int index = random.nextInt(expected.size());
                assertEquals(expected.remove(index), actual.remove(index));
            } else if (operation == 5) {
                int index = random.nextInt(expected.size());
                assertEquals(expected.set(index, value), actual.set(index, value));
            } else if (operation == 6 && random.nextInt(10) == 0) {
                expected.clear();
                actual.clear();
            } else {
                int index = random.nextInt(expected.size());
                assertEquals(expected.get(index), actual.get(index));
            }
            assertEquals(expected.size(), actual.size(), "size at step " + step);
            assertEquals(expected.isEmpty(), actual.isEmpty());
            assertEquals(expected, snapshot(actual), "content at step " + step);
        }
    }

    private static <T> List<T> snapshot(Iterable<T> values) {
        List<T> result = new ArrayList<>();
        values.forEach(result::add);
        return result;
    }
}
