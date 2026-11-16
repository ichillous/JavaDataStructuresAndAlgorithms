package io.github.ichillous.dsa.collections;

import java.util.Arrays;
import java.util.Objects;

/**
 * A hash table with linear probing, backward-shift deletion and a maximum load
 * factor of 2/3.
 *
 * <p>Mappings live directly in two parallel arrays whose length is a power of two
 * (at least 2). A key's home slot comes from Fibonacci hashing of
 * {@code h ^ (h >>> 16)}, where {@code h} is its {@code hashCode()}. A collision
 * moves to the next slot, wrapping from the last slot to slot 0.
 *
 * <p>Invariant: every stored key sits on its probe path, which starts at the key's
 * home slot and walks forward with no empty slot in between. A lookup can therefore
 * stop at the first empty slot. Because the load never exceeds 2/3, an empty slot
 * always exists and every probe terminates.
 *
 * <p>Removal shifts later members of the cluster backward to refill the hole, so
 * the invariant holds without tombstones and the table never degrades from
 * deleted-slot residue. A resize re-inserts every mapping into a table twice as
 * large, since home slots depend on the table length.
 *
 * <p>Get, put and remove take expected amortized O(1) time with well-distributed
 * hashes, and about 1/2 (1 + 1/(1 - a)^2) probes for an unsuccessful search at load
 * {@code a}. Adversarial or constant hash codes make an operation O(n). Storage is
 * O(capacity) with no per-entry nodes; a resize needs O(capacity) temporary space.
 * Null keys and values are rejected, so null unambiguously means an absent mapping.
 * Keys must retain stable equals/hashCode behavior while stored. The map stores
 * references and never copies keys or values. This class is not thread-safe.
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class OpenAddressingHashMap<K, V> {
    private static final int MAX_CAPACITY = 1 << 30;
    private static final int FIBONACCI = 0x9E3779B9;

    private Object[] keys;
    private Object[] values;
    private int shift;
    private int size;

    /** Creates an empty map with sixteen slots. */
    public OpenAddressingHashMap() {
        this(16);
    }

    /**
     * Creates an empty map with at least the given positive slot count, rounded up
     * to a power of two (minimum 2). The map grows automatically.
     *
     * @throws IllegalArgumentException if the count is not positive or exceeds 2^30
     */
    public OpenAddressingHashMap(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity must be positive");
        }
        if (initialCapacity > MAX_CAPACITY) {
            throw new IllegalArgumentException("initialCapacity must be at most 2^30");
        }
        allocate(Math.max(2, Integer.highestOneBit(initialCapacity - 1) << 1));
    }

    /** Adds or replaces a mapping, returning its previous value or null if absent. */
    public V put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        int slot = find(key);
        if (keys[slot] != null) {
            @SuppressWarnings("unchecked")
            V previous = (V) values[slot];
            values[slot] = value;
            return previous;
        }
        if ((size + 1L) * 3 > 2L * keys.length) {
            resize();
            slot = emptySlot(key);
        }
        keys[slot] = key;
        values[slot] = value;
        size++;
        return null;
    }

    /** Returns a key's value or null if the non-null key is absent. */
    @SuppressWarnings("unchecked")
    public V get(K key) {
        Objects.requireNonNull(key, "key");
        int slot = find(key);
        return keys[slot] == null ? null : (V) values[slot];
    }

    /** Returns whether the non-null key has a mapping. */
    public boolean containsKey(K key) {
        Objects.requireNonNull(key, "key");
        return keys[find(key)] != null;
    }

    /**
     * Removes a mapping and returns its old value, or null if absent. Later members
     * of the cluster that would otherwise become unreachable are shifted backward.
     */
    @SuppressWarnings("unchecked")
    public V remove(K key) {
        Objects.requireNonNull(key, "key");
        int hole = find(key);
        if (keys[hole] == null) {
            return null;
        }
        V old = (V) values[hole];
        int mask = keys.length - 1;
        int next = (hole + 1) & mask;
        while (keys[next] != null) {
            int home = homeSlot(keys[next]);
            // The entry may fill the hole unless its home lies cyclically in (hole, next].
            if (((next - home) & mask) >= ((next - hole) & mask)) {
                keys[hole] = keys[next];
                values[hole] = values[next];
                hole = next;
            }
            next = (next + 1) & mask;
        }
        keys[hole] = null;
        values[hole] = null;
        size--;
        return old;
    }

    /** Returns the number of mappings. */
    public int size() {
        return size;
    }

    /** Returns whether there are no mappings. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Removes all mappings in O(capacity) time, retaining the arrays for reuse. */
    public void clear() {
        Arrays.fill(keys, null);
        Arrays.fill(values, null);
        size = 0;
    }

    /** Returns the slot holding the key, or the empty slot that ends its probe path. */
    private int find(Object key) {
        int mask = keys.length - 1;
        int slot = homeSlot(key);
        while (keys[slot] != null && !keys[slot].equals(key)) {
            slot = (slot + 1) & mask;
        }
        return slot;
    }

    /** Returns the first empty slot on the key's probe path, without calling equals. */
    private int emptySlot(Object key) {
        int mask = keys.length - 1;
        int slot = homeSlot(key);
        while (keys[slot] != null) {
            slot = (slot + 1) & mask;
        }
        return slot;
    }

    private int homeSlot(Object key) {
        int h = key.hashCode();
        h ^= h >>> 16;
        return (h * FIBONACCI) >>> shift;
    }

    private void resize() {
        if (keys.length >= MAX_CAPACITY) {
            throw new IllegalStateException("map cannot grow beyond 2^30 slots");
        }
        Object[] oldKeys = keys;
        Object[] oldValues = values;
        allocate(oldKeys.length << 1);
        for (int i = 0; i < oldKeys.length; i++) {
            if (oldKeys[i] != null) {
                int slot = emptySlot(oldKeys[i]);
                keys[slot] = oldKeys[i];
                values[slot] = oldValues[i];
            }
        }
    }

    private void allocate(int capacity) {
        keys = new Object[capacity];
        values = new Object[capacity];
        shift = Integer.numberOfLeadingZeros(capacity) + 1;
    }
}
