package io.github.ichillous.dsa.collections;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A doubly linked list with O(1) insertion/removal at either end and no null elements.
 * Indexed access takes O(1 + min(index, size - index)) time. Each node has two links, so
 * total storage is O(n). Iteration follows head to tail and fails fast on structural
 * changes. This class is not thread-safe.
 *
 * @param <T> element type
 */
public final class DoublyLinkedList<T> implements Iterable<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;
    private int modificationCount;

    /** Inserts a non-null element at the front. */
    public void addFirst(T element) {
        Node<T> node = new Node<>(Objects.requireNonNull(element, "element"));
        node.next = head;
        if (head == null) {
            tail = node;
        } else {
            head.previous = node;
        }
        head = node;
        size++;
        modificationCount++;
    }

    /** Inserts a non-null element at the back. */
    public void addLast(T element) {
        Node<T> node = new Node<>(Objects.requireNonNull(element, "element"));
        node.previous = tail;
        if (tail == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
        size++;
        modificationCount++;
    }

    /** Removes and returns the first element, or throws if empty. */
    public T removeFirst() {
        requireNonempty();
        return unlink(head);
    }

    /** Removes and returns the last element, or throws if empty. */
    public T removeLast() {
        requireNonempty();
        return unlink(tail);
    }

    /** Returns the element at an index in [0, size). */
    public T get(int index) {
        return nodeAt(index).element;
    }

    /** Replaces an element, returning its previous value; does not invalidate iterators. */
    public T set(int index, T element) {
        Node<T> node = nodeAt(index);
        Objects.requireNonNull(element, "element");
        T previous = node.element;
        node.element = element;
        return previous;
    }

    /** Removes and returns an indexed element. */
    public T remove(int index) {
        return unlink(nodeAt(index));
    }

    /** Returns the number of elements. */
    public int size() {
        return size;
    }

    /** Returns whether there are no elements. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Removes all nodes in O(n) time. */
    public void clear() {
        if (size != 0) {
            Node<T> node = head;
            while (node != null) {
                Node<T> next = node.next;
                node.previous = null;
                node.next = null;
                node = next;
            }
            head = null;
            tail = null;
            size = 0;
            modificationCount++;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private final int expectedModificationCount = modificationCount;
            private Node<T> next = head;

            @Override
            public boolean hasNext() {
                checkForModification();
                return next != null;
            }

            @Override
            public T next() {
                checkForModification();
                if (next == null) {
                    throw new NoSuchElementException();
                }
                T result = next.element;
                next = next.next;
                return result;
            }

            private void checkForModification() {
                if (expectedModificationCount != modificationCount) {
                    throw new ConcurrentModificationException();
                }
            }
        };
    }

    private Node<T> nodeAt(int index) {
        Objects.checkIndex(index, size);
        if (index < size / 2) {
            Node<T> node = head;
            for (int i = 0; i < index; i++) {
                node = node.next;
            }
            return node;
        }
        Node<T> node = tail;
        for (int i = size - 1; i > index; i--) {
            node = node.previous;
        }
        return node;
    }

    private T unlink(Node<T> node) {
        if (node.previous == null) {
            head = node.next;
        } else {
            node.previous.next = node.next;
        }
        if (node.next == null) {
            tail = node.previous;
        } else {
            node.next.previous = node.previous;
        }
        node.previous = null;
        node.next = null;
        size--;
        modificationCount++;
        return node.element;
    }

    private void requireNonempty() {
        if (head == null) {
            throw new NoSuchElementException("list is empty");
        }
    }

    private static final class Node<T> {
        private T element;
        private Node<T> previous;
        private Node<T> next;

        private Node(T element) {
            this.element = element;
        }
    }
}
