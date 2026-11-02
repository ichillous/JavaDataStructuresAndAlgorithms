package io.github.ichillous.dsa.collections;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A first-in, first-out queue using a growing circular array.
 * Enqueue is amortized O(1); dequeue and peek are O(1); storage is O(capacity),
 * retained after elements are removed.
 * Null elements are rejected. This class is not thread-safe.
 *
 * @param <T> element type
 */
public final class ArrayQueue<T> {
    private Object[] elements;
    private int head;
    private int size;

    /** Creates an empty queue with capacity eight. */
    public ArrayQueue() {
        this(8);
    }

    /** Creates an empty queue with a positive initial capacity. */
    public ArrayQueue(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity must be positive");
        }
        elements = new Object[initialCapacity];
    }

    /** Adds a non-null element to the back, growing the buffer when full. */
    public void enqueue(T element) {
        Objects.requireNonNull(element, "element");
        if (size == elements.length) {
            grow();
        }
        elements[physicalIndex(size)] = element;
        size++;
    }

    /** Removes and returns the front element, or throws if empty. */
    public T dequeue() {
        T result = peek();
        elements[head] = null;
        head = (head + 1) % elements.length;
        size--;
        return result;
    }

    /** Returns the front element without removing it, or throws if empty. */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (size == 0) {
            throw new NoSuchElementException("queue is empty");
        }
        return (T) elements[head];
    }

    /** Returns the number of elements. */
    public int size() {
        return size;
    }

    /** Returns whether there are no elements. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Removes all elements in O(capacity) time, retaining the buffer for reuse. */
    public void clear() {
        Arrays.fill(elements, null);
        head = 0;
        size = 0;
    }

    private int physicalIndex(int offset) {
        // Use long addition so even very large valid array indices do not overflow.
        return (int) (((long) head + offset) % elements.length);
    }

    private void grow() {
        Object[] grown = new Object[Math.multiplyExact(elements.length, 2)];
        for (int i = 0; i < size; i++) {
            grown[i] = elements[physicalIndex(i)];
        }
        elements = grown;
        head = 0;
    }
}
