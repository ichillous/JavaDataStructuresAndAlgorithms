package io.github.ichillous.dsa.structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A natural-order min-priority queue stored in an array. Each parent is no greater than its children.
 * Add/remove take O(log n) amortized time, peek O(1), and heap construction O(n).
 * Duplicate keys are allowed; nulls are rejected. This class is not thread safe.
 *
 * @param <T> element type with a consistent natural ordering
 */
public final class BinaryMinHeap<T extends Comparable<? super T>> {
    private final ArrayList<T> elements;

    /** Creates an empty heap. */
    public BinaryMinHeap() {
        elements = new ArrayList<>();
    }

    /** Creates a heap from a copy of the collection in O(n) time. */
    public BinaryMinHeap(Collection<? extends T> values) {
        Objects.requireNonNull(values, "values");
        elements = new ArrayList<>(values.size());
        for (T value : values) {
            elements.add(Objects.requireNonNull(value, "value"));
        }
        for (int i = elements.size() / 2 - 1; i >= 0; i--) {
            siftDown(i);
        }
    }

    /** Adds an element in O(log n) amortized time. */
    public void add(T value) {
        Objects.requireNonNull(value, "value");
        elements.add(value);
        int index = elements.size() - 1;
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (elements.get(parent).compareTo(elements.get(index)) <= 0) {
                break;
            }
            swap(parent, index);
            index = parent;
        }
    }

    /** Returns the minimum element; throws NoSuchElementException when empty. */
    public T peek() {
        requireNonEmpty();
        return elements.get(0);
    }

    /** Removes and returns the minimum; throws NoSuchElementException when empty. */
    public T remove() {
        requireNonEmpty();
        T minimum = elements.get(0);
        T last = elements.remove(elements.size() - 1);
        if (!elements.isEmpty()) {
            elements.set(0, last);
            siftDown(0);
        }
        return minimum;
    }

    /** Returns the number of elements. */
    public int size() {
        return elements.size();
    }

    /** Returns whether there are no elements. */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    private void siftDown(int index) {
        // Only internal nodes have children; this also avoids overflowing 2 * index + 1.
        while (index < elements.size() / 2) {
            int child = 2 * index + 1;
            if (child + 1 < elements.size()
                    && elements.get(child + 1).compareTo(elements.get(child)) < 0) {
                child++;
            }
            if (elements.get(index).compareTo(elements.get(child)) <= 0) {
                return;
            }
            swap(index, child);
            index = child;
        }
    }

    private void swap(int first, int second) {
        T value = elements.get(first);
        elements.set(first, elements.get(second));
        elements.set(second, value);
    }

    private void requireNonEmpty() {
        if (elements.isEmpty()) {
            throw new NoSuchElementException("heap is empty");
        }
    }
}
