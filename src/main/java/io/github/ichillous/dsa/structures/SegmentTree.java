package io.github.ichillous.dsa.structures;

import java.util.Arrays;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * A segment tree for point replacement and minimum queries over half-open ranges.
 * Leaves are padded to a power of two with Long.MAX_VALUE, the identity for minimum.
 * Each internal node covers an adjacent interval and stores the minimum of its two children.
 * Construction takes O(n) time and storage; replacement and nonempty range queries take O(log n)
 * time. Empty ranges return {@link OptionalLong#empty()}, so every long value remains usable.
 * This mutable class is not thread safe.
 */
public final class SegmentTree {
    private final int size;
    private final int leafBase;
    private final long[] tree;

    /** Creates a tree from a defensive copy of the values, which may be empty. */
    public SegmentTree(long[] values) {
        Objects.requireNonNull(values, "values");
        size = values.length;
        int capacity = 1;
        while (capacity < size) {
            capacity = Math.multiplyExact(capacity, 2);
        }
        leafBase = capacity;
        tree = new long[Math.multiplyExact(leafBase, 2)];
        Arrays.fill(tree, Long.MAX_VALUE);
        System.arraycopy(values, 0, tree, leafBase, size);
        for (int node = leafBase - 1; node > 0; node--) {
            tree[node] = Math.min(tree[2 * node], tree[2 * node + 1]);
        }
    }

    /** Returns the number of indexed values. */
    public int size() {
        return size;
    }

    /** Replaces the value at an index in [0, size), repairing its ancestors. */
    public void set(int index, long value) {
        Objects.checkIndex(index, size);
        int node = leafBase + index;
        tree[node] = value;
        while (node > 1) {
            node /= 2;
            tree[node] = Math.min(tree[2 * node], tree[2 * node + 1]);
        }
    }

    /**
     * Returns the minimum over [fromInclusive, toExclusive), or an empty optional for an empty range.
     * Requires 0 &lt;= fromInclusive &lt;= toExclusive &lt;= size; otherwise throws
     * {@link IndexOutOfBoundsException}. This query does not modify the tree.
     */
    public OptionalLong rangeMin(int fromInclusive, int toExclusive) {
        Objects.checkFromToIndex(fromInclusive, toExclusive, size);
        if (fromInclusive == toExclusive) {
            return OptionalLong.empty();
        }
        long minimum = Long.MAX_VALUE;
        int left = leafBase + fromInclusive;
        int right = leafBase + toExclusive;
        while (left < right) {
            if ((left & 1) != 0) {
                minimum = Math.min(minimum, tree[left++]);
            }
            if ((right & 1) != 0) {
                minimum = Math.min(minimum, tree[--right]);
            }
            left /= 2;
            right /= 2;
        }
        return OptionalLong.of(minimum);
    }
}
