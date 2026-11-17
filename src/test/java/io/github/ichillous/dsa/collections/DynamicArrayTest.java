package io.github.ichillous.dsa.collections;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DynamicArrayTest {
    @Test
    void capacityReportsBackingLengthAndDoublesWhenFull() {
        assertEquals(8, new DynamicArray<String>().capacity());
        assertEquals(0, new DynamicArray<String>(0).capacity());
        DynamicArray<Integer> array = new DynamicArray<>(4);
        for (int value = 1; value <= 4; value++) {
            array.add(value);
        }
        assertEquals(4, array.capacity(), "a full array has not grown yet");
        array.add(5);
        assertEquals(8, array.capacity());
        for (int value = 6; value <= 9; value++) {
            array.add(value);
        }
        assertEquals(16, array.capacity());
        assertTrue(array.capacity() >= array.size());
    }

    @Test
    void capacityNeverShrinksAndGrowsFromZeroThroughOne() {
        DynamicArray<Integer> array = new DynamicArray<>(0);
        array.add(1);
        assertEquals(1, array.capacity());
        array.add(2);
        assertEquals(2, array.capacity());
        array.add(3);
        assertEquals(4, array.capacity());
        array.remove(0);
        array.remove(0);
        array.remove(0);
        assertEquals(4, array.capacity(), "removal retains capacity");
        array.clear();
        assertEquals(4, array.capacity(), "clear retains capacity");
        array.add(0, 9);
        assertEquals(4, array.capacity(), "an insertion at the front with spare room does not grow");
    }

    @Test
    void capacityMatchesAnObservableGrowthModelOverSeededOperations() {
        Random random = new Random(20261117L);
        DynamicArray<Integer> array = new DynamicArray<>(3);
        int expected = 3;
        int size = 0;
        for (int step = 0; step < 500; step++) {
            if (size > 0 && random.nextInt(3) == 0) {
                array.remove(random.nextInt(size));
                size--;
            } else {
                if (size == expected) {
                    expected = Math.max(2 * expected, expected + 1);
                }
                array.add(random.nextInt(size + 1), step);
                size++;
            }
            assertEquals(size, array.size(), "seed 20261117, step " + step);
            assertEquals(expected, array.capacity(), "seed 20261117, step " + step);
        }
    }

    @Test
    void growsFromZeroAndInsertsAtBothBoundaries() {
        DynamicArray<String> array = new DynamicArray<>(0);
        array.add("b");
        array.add(0, "a");
        array.add(2, "d");
        array.add(2, "c");
        assertEquals(List.of("a", "b", "c", "d"), snapshot(array));
        assertEquals("b", array.set(1, "B"));
        assertEquals("a", array.remove(0));
        assertEquals("d", array.remove(array.size() - 1));
        assertEquals(List.of("B", "c"), snapshot(array));
    }

    @Test
    void validatesCapacityIndicesAndNullWithoutChangingState() {
        assertThrows(IllegalArgumentException.class, () -> new DynamicArray<>(-1));
        DynamicArray<Integer> array = new DynamicArray<>();
        assertThrows(IndexOutOfBoundsException.class, () -> array.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> array.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> array.add(-1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> array.add(1, 1));
        assertThrows(NullPointerException.class, () -> array.add(null));
        array.add(7);
        assertThrows(NullPointerException.class, () -> array.set(0, null));
        assertThrows(IndexOutOfBoundsException.class, () -> array.set(1, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> array.get(-1));
        assertEquals(List.of(7), snapshot(array));
    }

    @Test
    void supportsReuseAfterClearAndRemovalOfTheLastElement() {
        DynamicArray<Integer> array = new DynamicArray<>(1);
        array.clear();
        array.add(1);
        assertEquals(1, array.remove(0));
        assertTrue(array.isEmpty());
        array.add(2);
        array.clear();
        assertEquals(0, array.size());
        array.add(3);
        assertEquals(List.of(3), snapshot(array));
    }

    @Test
    void iteratorHasStandardExhaustionAndUnsupportedRemoval() {
        DynamicArray<Integer> array = new DynamicArray<>();
        Iterator<Integer> empty = array.iterator();
        assertFalse(empty.hasNext());
        assertThrows(NoSuchElementException.class, empty::next);
        array.add(1);
        Iterator<Integer> iterator = array.iterator();
        assertEquals(1, iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    void structuralChangesInvalidateIteratorsButReplacementDoesNot() {
        DynamicArray<Integer> array = new DynamicArray<>();
        array.add(1);
        Iterator<Integer> replacement = array.iterator();
        array.set(0, 2);
        assertEquals(2, replacement.next());
        Iterator<Integer> addition = array.iterator();
        array.add(3);
        assertThrows(ConcurrentModificationException.class, addition::hasNext);
        Iterator<Integer> removal = array.iterator();
        array.remove(0);
        assertThrows(ConcurrentModificationException.class, removal::next);
        Iterator<Integer> clearing = array.iterator();
        array.clear();
        assertThrows(ConcurrentModificationException.class, clearing::next);
    }

    @Test
    void randomizedOperationsAgreeWithArrayList() {
        Random random = new Random(0xDA7A);
        DynamicArray<Integer> actual = new DynamicArray<>(0);
        List<Integer> expected = new ArrayList<>();
        for (int step = 0; step < 5_000; step++) {
            int operation = random.nextInt(7);
            int value = random.nextInt();
            if (operation == 0 || expected.isEmpty()) {
                actual.add(value);
                expected.add(value);
            } else if (operation == 1) {
                int index = random.nextInt(expected.size() + 1);
                actual.add(index, value);
                expected.add(index, value);
            } else if (operation == 2) {
                int index = random.nextInt(expected.size());
                assertEquals(expected.set(index, value), actual.set(index, value));
            } else if (operation == 3 || operation == 4) {
                int index = random.nextInt(expected.size());
                assertEquals(expected.remove(index), actual.remove(index));
            } else if (operation == 5 && random.nextInt(10) == 0) {
                actual.clear();
                expected.clear();
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
