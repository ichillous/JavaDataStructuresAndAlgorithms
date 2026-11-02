package io.github.ichillous.dsa.collections;

import java.util.NoSuchElementException;

/**
 * A last-in, first-out stack backed by a {@link DynamicArray}.
 * Push is amortized O(1); pop and peek are O(1); storage is O(capacity), retained
 * after elements are removed.
 * Null elements are rejected. This class is not thread-safe.
 *
 * @param <T> element type
 */
public final class ArrayStack<T> {
    private final DynamicArray<T> elements = new DynamicArray<>();

    /** Pushes a non-null element onto the top. */
    public void push(T element) {
        elements.add(element);
    }

    /** Removes and returns the top element, or throws if empty. */
    public T pop() {
        requireNonempty();
        return elements.remove(elements.size() - 1);
    }

    /** Returns the top element without removing it, or throws if empty. */
    public T peek() {
        requireNonempty();
        return elements.get(elements.size() - 1);
    }

    /** Returns the number of elements. */
    public int size() {
        return elements.size();
    }

    /** Returns whether there are no elements. */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /** Removes all elements in O(n), retaining capacity for reuse. */
    public void clear() {
        elements.clear();
    }

    private void requireNonempty() {
        if (elements.isEmpty()) {
            throw new NoSuchElementException("stack is empty");
        }
    }
}
