package io.github.ichillous.dsa.collections;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A deque whose elements occupy a contiguous logical sequence in a growing circular array.
 * Additions at either end take amortized O(1) time; removals and peeks take O(1) time.
 * Nulls are rejected. Storage is O(capacity), which is retained after removals or clear
 * and may exceed the current number of elements. This class is not thread safe.
 *
 * @param <T> element type
 */
public final class RingDeque<T> {
    private Object[] elements;
    private int head;
    private int size;

    /** Creates an empty deque with initial capacity eight. */
    public RingDeque() {
        this(8);
    }

    /** Creates an empty deque with a strictly positive initial capacity. */
    public RingDeque(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity must be positive");
        }
        elements = new Object[initialCapacity];
    }

    /** Adds a non-null element at the front, growing the buffer if needed. */
    public void addFirst(T element) {
        Objects.requireNonNull(element, "element");
        ensureSpace();
        head = head == 0 ? elements.length - 1 : head - 1;
        elements[head] = element;
        size++;
    }

    /** Adds a non-null element at the back, growing the buffer if needed. */
    public void addLast(T element) {
        Objects.requireNonNull(element, "element");
        ensureSpace();
        elements[physicalIndex(size)] = element;
        size++;
    }

    /** Removes and returns the first element; throws NoSuchElementException when empty. */
    public T removeFirst() {
        T result = peekFirst();
        elements[head] = null;
        head = head == elements.length - 1 ? 0 : head + 1;
        size--;
        resetHeadIfEmpty();
        return result;
    }

    /** Removes and returns the last element; throws NoSuchElementException when empty. */
    public T removeLast() {
        T result = peekLast();
        elements[physicalIndex(size - 1)] = null;
        size--;
        resetHeadIfEmpty();
        return result;
    }

    /** Returns the first element without removing it; throws NoSuchElementException when empty. */
    public T peekFirst() {
        requireNonempty();
        return elementAt(0);
    }

    /** Returns the last element without removing it; throws NoSuchElementException when empty. */
    public T peekLast() {
        requireNonempty();
        return elementAt(size - 1);
    }

    /** Returns the current number of elements. */
    public int size() {
        return size;
    }

    /** Returns whether the deque contains no elements. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Removes all elements in O(capacity) time, retaining the buffer for reuse. */
    public void clear() {
        Arrays.fill(elements, null);
        head = 0;
        size = 0;
    }

    @SuppressWarnings("unchecked")
    private T elementAt(int offset) {
        return (T) elements[physicalIndex(offset)];
    }

    private int physicalIndex(int offset) {
        return (int) (((long) head + offset) % elements.length);
    }

    private void ensureSpace() {
        if (size == elements.length) {
            Object[] grown = new Object[Math.multiplyExact(elements.length, 2)];
            for (int index = 0; index < size; index++) {
                grown[index] = elements[physicalIndex(index)];
            }
            elements = grown;
            head = 0;
        }
    }

    private void resetHeadIfEmpty() {
        if (size == 0) {
            head = 0;
        }
    }

    private void requireNonempty() {
        if (size == 0) {
            throw new NoSuchElementException("deque is empty");
        }
    }
}
