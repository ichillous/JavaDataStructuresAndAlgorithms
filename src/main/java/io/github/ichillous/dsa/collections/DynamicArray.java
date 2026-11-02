package io.github.ichillous.dsa.collections;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A resizable contiguous array that rejects null elements.
 *
 * <p>Indexed access is O(1); appending is amortized O(1), while insertion and removal
 * at an arbitrary index are O(n). Space is O(capacity); capacity grows geometrically
 * and is retained after removal. Iterators visit
 * elements in index order and fail fast after structural changes. This class is not
 * thread-safe.
 *
 * @param <T> element type
 */
public final class DynamicArray<T> implements Iterable<T> {
    private Object[] elements;
    private int size;
    private int modificationCount;

    /** Creates an empty array with an initial capacity of eight. */
    public DynamicArray() {
        this(8);
    }

    /**
     * Creates an empty array with the requested capacity; zero is allowed.
     *
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public DynamicArray(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity must be nonnegative");
        }
        elements = new Object[initialCapacity];
    }

    /** Appends a non-null element in amortized O(1) time. */
    public void add(T element) {
        add(size, element);
    }

    /** Inserts at an index in [0, size], shifting the following elements right. */
    public void add(int index, T element) {
        checkPosition(index);
        Objects.requireNonNull(element, "element");
        ensureCapacity();
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = element;
        size++;
        modificationCount++;
    }

    /** Returns the element at an index in [0, size). */
    public T get(int index) {
        Objects.checkIndex(index, size);
        return elementAt(index);
    }

    /** Replaces an element and returns its previous value; does not invalidate iterators. */
    public T set(int index, T element) {
        Objects.checkIndex(index, size);
        Objects.requireNonNull(element, "element");
        T previous = elementAt(index);
        elements[index] = element;
        return previous;
    }

    /** Removes and returns the indexed element, shifting the following elements left. */
    public T remove(int index) {
        Objects.checkIndex(index, size);
        T removed = elementAt(index);
        int moved = size - index - 1;
        System.arraycopy(elements, index + 1, elements, index, moved);
        elements[--size] = null; // Release the removed reference, including the old tail.
        modificationCount++;
        return removed;
    }

    /** Returns the number of elements. */
    public int size() {
        return size;
    }

    /** Returns whether there are no elements. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Removes all elements in O(n), retaining capacity for reuse. */
    public void clear() {
        if (size != 0) {
            Arrays.fill(elements, 0, size, null);
            size = 0;
            modificationCount++;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private final int expectedModificationCount = modificationCount;
            private int index;

            @Override
            public boolean hasNext() {
                checkForModification();
                return index < size;
            }

            @Override
            public T next() {
                checkForModification();
                if (index == size) {
                    throw new NoSuchElementException();
                }
                return elementAt(index++);
            }

            private void checkForModification() {
                if (expectedModificationCount != modificationCount) {
                    throw new ConcurrentModificationException();
                }
            }
        };
    }

    private void checkPosition(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        }
    }

    private void ensureCapacity() {
        if (size == elements.length) {
            int capacity = elements.length == 0 ? 1 : Math.multiplyExact(elements.length, 2);
            elements = Arrays.copyOf(elements, capacity);
        }
    }

    @SuppressWarnings("unchecked")
    private T elementAt(int index) {
        return (T) elements[index];
    }
}
