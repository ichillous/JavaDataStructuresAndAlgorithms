package io.github.ichillous.dsa.collections;

import java.util.HashMap;
import java.util.Objects;

/**
 * A fixed-capacity least-recently-used cache built from a hash table and a doubly linked list.
 * The list runs from the least recently accessed entry to the most recently accessed entry.
 * Successful get calls and all put calls make their entry most recent; unsuccessful lookups
 * and removals do not change the relative order of other entries.
 *
 * <p>Get and remove take expected O(1) time; put takes expected amortized O(1) time,
 * including hash-table resizing and assuming constant-time key hashing/equality.
 * Storage is O(capacity), including hash-table capacity retained after removals and clear.
 * Keys and values must be non-null; keys must keep stable equality/hash codes while cached.
 * Null return values unambiguously mean absence. This class is not thread safe; even get
 * changes the recency list.
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class LruCache<K, V> {
    private final int capacity;
    private final HashMap<K, Node<K, V>> entries = new HashMap<>();
    private Node<K, V> leastRecent;
    private Node<K, V> mostRecent;

    /** Creates an empty cache with a strictly positive maximum number of entries. */
    public LruCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    /** Returns the value and promotes the entry to most recent, or returns null when absent. */
    public V get(K key) {
        Node<K, V> node = entries.get(Objects.requireNonNull(key, "key"));
        if (node == null) {
            return null;
        }
        promote(node);
        return node.value;
    }

    /**
     * Inserts or replaces a value and makes it most recent. Returns the previous value, or null
     * for a new key. Inserting into a full cache evicts its least recent entry.
     */
    public V put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Node<K, V> node = entries.get(key);
        if (node != null) {
            V previous = node.value;
            node.value = value;
            promote(node);
            return previous;
        }
        node = new Node<>(key, value);
        entries.put(key, node);
        append(node);
        if (entries.size() > capacity) {
            Node<K, V> evicted = leastRecent;
            unlink(evicted);
            entries.remove(evicted.key);
        }
        return null;
    }

    /** Removes and returns a value, or returns null when the key is absent. */
    public V remove(K key) {
        Node<K, V> node = entries.remove(Objects.requireNonNull(key, "key"));
        if (node == null) {
            return null;
        }
        unlink(node);
        return node.value;
    }

    /** Returns the number of cached entries in O(1) time. */
    public int size() {
        return entries.size();
    }

    /** Returns the fixed maximum number of cached entries. */
    public int capacity() {
        return capacity;
    }

    /** Returns whether no entries are cached. */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /** Removes all entries in O(capacity) time, retaining the hash table for reuse. */
    public void clear() {
        entries.clear();
        leastRecent = null;
        mostRecent = null;
    }

    private void promote(Node<K, V> node) {
        if (node != mostRecent) {
            unlink(node);
            append(node);
        }
    }

    private void append(Node<K, V> node) {
        node.previous = mostRecent;
        node.next = null;
        if (mostRecent == null) {
            leastRecent = node;
        } else {
            mostRecent.next = node;
        }
        mostRecent = node;
    }

    private void unlink(Node<K, V> node) {
        if (node.previous == null) {
            leastRecent = node.next;
        } else {
            node.previous.next = node.next;
        }
        if (node.next == null) {
            mostRecent = node.previous;
        } else {
            node.next.previous = node.previous;
        }
        node.previous = null;
        node.next = null;
    }

    private static final class Node<K, V> {
        private final K key;
        private V value;
        private Node<K, V> previous;
        private Node<K, V> next;

        private Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
