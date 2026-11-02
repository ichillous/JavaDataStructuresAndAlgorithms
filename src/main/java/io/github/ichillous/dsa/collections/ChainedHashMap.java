package io.github.ichillous.dsa.collections;

import java.util.Arrays;
import java.util.Objects;

/**
 * A hash table with separate chaining and a maximum load factor of 0.75.
 *
 * <p>Get, put and remove take expected amortized O(1) time with well-distributed
 * hashes; adversarial collisions can make an operation O(n). Storage is
 * O(n + capacity). Null keys and values are rejected, so null unambiguously means
 * an absent mapping. Keys must retain stable equals/hashCode behavior while stored.
 * This class is not thread-safe.
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class ChainedHashMap<K, V> {
    private Node<K, V>[] buckets;
    private int size;

    /** Creates an empty map with sixteen buckets. */
    public ChainedHashMap() {
        this(16);
    }

    /** Creates an empty map with a positive bucket count. */
    public ChainedHashMap(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity must be positive");
        }
        buckets = newBuckets(initialCapacity);
    }

    /** Adds or replaces a mapping, returning its previous value or null if absent. */
    public V put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        int index = bucketIndex(key, buckets.length);
        for (Node<K, V> node = buckets[index]; node != null; node = node.next) {
            if (node.key.equals(key)) {
                V previous = node.value;
                node.value = value;
                return previous;
            }
        }
        if (size + 1 > buckets.length * 0.75) {
            resize();
            index = bucketIndex(key, buckets.length);
        }
        buckets[index] = new Node<>(key, value, buckets[index]);
        size++;
        return null;
    }

    /** Returns a key's value or null if the non-null key is absent. */
    public V get(K key) {
        Objects.requireNonNull(key, "key");
        for (Node<K, V> node = buckets[bucketIndex(key, buckets.length)];
                node != null; node = node.next) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    /** Returns whether the non-null key has a mapping. */
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /** Removes a mapping and returns its old value, or null if absent. */
    public V remove(K key) {
        Objects.requireNonNull(key, "key");
        int index = bucketIndex(key, buckets.length);
        Node<K, V> previous = null;
        Node<K, V> node = buckets[index];
        while (node != null) {
            if (node.key.equals(key)) {
                if (previous == null) {
                    buckets[index] = node.next;
                } else {
                    previous.next = node.next;
                }
                size--;
                return node.value;
            }
            previous = node;
            node = node.next;
        }
        return null;
    }

    /** Returns the number of mappings. */
    public int size() {
        return size;
    }

    /** Returns whether there are no mappings. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Removes all mappings in O(capacity) time, retaining the buckets for reuse. */
    public void clear() {
        Arrays.fill(buckets, null);
        size = 0;
    }

    private void resize() {
        Node<K, V>[] grown = newBuckets(Math.multiplyExact(buckets.length, 2));
        for (Node<K, V> bucket : buckets) {
            Node<K, V> node = bucket;
            while (node != null) {
                Node<K, V> next = node.next;
                int index = bucketIndex(node.key, grown.length);
                node.next = grown[index];
                grown[index] = node;
                node = next;
            }
        }
        buckets = grown;
    }

    private static int bucketIndex(Object key, int capacity) {
        // abs(MIN_VALUE) is still negative; floorMod works for every hash code.
        return Math.floorMod(key.hashCode(), capacity);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Node<K, V>[] newBuckets(int capacity) {
        return (Node<K, V>[]) new Node<?, ?>[capacity];
    }

    private static final class Node<K, V> {
        private final K key;
        private V value;
        private Node<K, V> next;

        private Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
}
