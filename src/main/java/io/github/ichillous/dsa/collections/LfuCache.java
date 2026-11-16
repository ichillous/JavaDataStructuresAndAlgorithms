package io.github.ichillous.dsa.collections;

import java.util.HashMap;
import java.util.Objects;

/**
 * A fixed-capacity least-frequently-used cache with least-recently-used tie-breaking, built from
 * a key map, one doubly linked list per use count (a frequency bucket) and a tracked minimum
 * frequency. Each bucket runs from its least recently used entry to its most recently used entry.
 *
 * <p>A new key starts at frequency 1. Successful get calls and replacing put calls move the entry
 * from bucket f to the most recent end of bucket f + 1. Inserting a new key into a full cache first
 * evicts the least recent entry of the lowest non-empty bucket; the eviction rule is therefore
 * deterministic: lowest frequency, then oldest use. Unsuccessful lookups, {@link #frequency} and
 * removals of other keys do not change any entry's frequency or relative order. An evicted or
 * removed key forgets its history: if reinserted it starts again at frequency 1.
 *
 * <p>Get, put, remove and frequency take expected O(1) time, assuming constant-time key
 * hashing/equality; the minimum frequency is maintained without scanning because it only
 * resets to 1 on insertion and advances by one when the minimum bucket empties on a promotion.
 * Storage is O(capacity), including hash-table capacity retained after removals and clear.
 * Frequencies are capped at {@link Integer#MAX_VALUE} rather than overflowing. Keys and values
 * must be non-null; keys must keep stable equality/hash codes while cached. Null return values
 * unambiguously mean absence. This class is not thread safe; even get changes cache state.
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class LfuCache<K, V> {
    private final int capacity;
    private final HashMap<K, Node<K, V>> entries = new HashMap<>();
    private final HashMap<Integer, Bucket<K, V>> buckets = new HashMap<>();
    private int minFrequency;

    /** Creates an empty cache with a strictly positive maximum number of entries. */
    public LfuCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    /**
     * Returns the value and increments the entry's frequency, or returns null when absent.
     * Runs in expected O(1) time.
     */
    public V get(K key) {
        Node<K, V> node = entries.get(Objects.requireNonNull(key, "key"));
        if (node == null) {
            return null;
        }
        touch(node);
        return node.value;
    }

    /**
     * Inserts or replaces a value. A replacement increments the frequency and returns the previous
     * value; a new key starts at frequency 1 and returns null. Inserting a new key into a full
     * cache evicts the least recently used entry among those with the lowest frequency. Runs in
     * expected amortized O(1) time.
     */
    public V put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Node<K, V> node = entries.get(key);
        if (node != null) {
            V previous = node.value;
            node.value = value;
            touch(node);
            return previous;
        }
        if (entries.size() == capacity) {
            Bucket<K, V> lowest = buckets.get(minFrequency);
            Node<K, V> evicted = lowest.first;
            detach(evicted);
            entries.remove(evicted.key);
        }
        node = new Node<>(key, value);
        entries.put(key, node);
        attach(node);
        minFrequency = 1;
        return null;
    }

    /** Removes and returns a value, or returns null when the key is absent. Expected O(1). */
    public V remove(K key) {
        Node<K, V> node = entries.remove(Objects.requireNonNull(key, "key"));
        if (node == null) {
            return null;
        }
        detach(node);
        return node.value;
    }

    /**
     * Returns the entry's use count, or 0 when the key is absent. This query does not count as a
     * use and does not change eviction order. Expected O(1) time.
     */
    public int frequency(K key) {
        Node<K, V> node = entries.get(Objects.requireNonNull(key, "key"));
        return node == null ? 0 : node.frequency;
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

    /** Removes all entries in O(capacity) time, retaining the key table for reuse. */
    public void clear() {
        entries.clear();
        buckets.clear();
        minFrequency = 0;
    }

    private void touch(Node<K, V> node) {
        if (node.frequency == Integer.MAX_VALUE) {
            Bucket<K, V> bucket = buckets.get(node.frequency);
            if (bucket.last != node) {
                detach(node);
                attach(node);
            }
            return;
        }
        int oldFrequency = node.frequency;
        detach(node);
        node.frequency = oldFrequency + 1;
        attach(node);
        if (minFrequency == oldFrequency && !buckets.containsKey(oldFrequency)) {
            minFrequency = node.frequency;
        }
    }

    /** Appends the node at the most recent end of its frequency bucket. */
    private void attach(Node<K, V> node) {
        Bucket<K, V> bucket = buckets.computeIfAbsent(node.frequency, f -> new Bucket<>());
        node.previous = bucket.last;
        node.next = null;
        if (bucket.last == null) {
            bucket.first = node;
        } else {
            bucket.last.next = node;
        }
        bucket.last = node;
    }

    /** Unlinks the node from its bucket and drops the bucket when it becomes empty. */
    private void detach(Node<K, V> node) {
        Bucket<K, V> bucket = buckets.get(node.frequency);
        if (node.previous == null) {
            bucket.first = node.next;
        } else {
            node.previous.next = node.next;
        }
        if (node.next == null) {
            bucket.last = node.previous;
        } else {
            node.next.previous = node.previous;
        }
        node.previous = null;
        node.next = null;
        if (bucket.first == null) {
            buckets.remove(node.frequency);
        }
    }

    private static final class Bucket<K, V> {
        private Node<K, V> first;
        private Node<K, V> last;
    }

    private static final class Node<K, V> {
        private final K key;
        private V value;
        private int frequency = 1;
        private Node<K, V> previous;
        private Node<K, V> next;

        private Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
