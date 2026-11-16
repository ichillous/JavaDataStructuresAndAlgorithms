package io.github.ichillous.dsa.structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A natural-order min-priority queue stored in an array. Each parent is no greater than its children.
 * Add/remove take O(log n) amortized time, peek O(1), and heap construction O(n).
 * Duplicate keys are allowed; nulls are rejected. {@code add} and {@code remove} are failure-atomic:
 * if {@code compareTo} throws, size, contents and order are unchanged. This class is not thread safe.
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

    /**
     * Adds an element in O(log n) amortized time. This operation is failure-atomic with respect to
     * {@code compareTo}: the final position is found with read-only comparisons first, so if a
     * comparison throws, the exception propagates and the heap is unchanged.
     */
    public void add(T value) {
        Objects.requireNonNull(value, "value");
        int position = elements.size();
        while (position > 0) {
            int parent = (position - 1) / 2;
            if (elements.get(parent).compareTo(value) <= 0) {
                break;
            }
            position = parent;
        }
        // No comparisons remain: shift the ancestors on the path down one level.
        elements.add(value);
        int index = elements.size() - 1;
        while (index > position) {
            int parent = (index - 1) / 2;
            elements.set(index, elements.get(parent));
            index = parent;
        }
        elements.set(position, value);
    }

    /** Returns the minimum element; throws NoSuchElementException when empty. */
    public T peek() {
        requireNonEmpty();
        return elements.get(0);
    }

    /**
     * Removes and returns the minimum; throws NoSuchElementException when empty. Like
     * {@link #add}, this is failure-atomic with respect to {@code compareTo}: the path of the
     * displaced last element is found with read-only comparisons before any element moves.
     */
    public T remove() {
        requireNonEmpty();
        T minimum = elements.get(0);
        int lastIndex = elements.size() - 1;
        T last = elements.get(lastIndex);
        if (lastIndex > 0) {
            // A heap index has at most 31 levels, so the descent path fits in a small array.
            int[] path = new int[Integer.SIZE];
            int depth = 0;
            int index = 0;
            while (index < lastIndex / 2) {
                int child = 2 * index + 1;
                if (child + 1 < lastIndex
                        && elements.get(child + 1).compareTo(elements.get(child)) < 0) {
                    child++;
                }
                if (last.compareTo(elements.get(child)) <= 0) {
                    break;
                }
                path[depth++] = child;
                index = child;
            }
            // No comparisons remain: pull the chosen children up, then place the last element.
            int hole = 0;
            for (int step = 0; step < depth; step++) {
                elements.set(hole, elements.get(path[step]));
                hole = path[step];
            }
            elements.set(hole, last);
        }
        elements.remove(lastIndex);
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
