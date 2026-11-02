package io.github.ichillous.dsa.structures;

import java.util.Objects;

/**
 * A binary indexed tree for point additions and half-open range sums.
 * Indexing is zero based at the API and one based internally. Construction takes
 * O(n) time and space; updates and prefix/range queries take O(log n) time.
 * Any intermediate signed-long overflow throws {@link ArithmeticException};
 * failed updates leave the tree unchanged. Not thread safe.
 */
public final class FenwickTree {
    private final long[] tree;

    /** Creates a tree from a defensive copy of the values. */
    public FenwickTree(long[] values) {
        Objects.requireNonNull(values, "values");
        tree = new long[Math.addExact(values.length, 1)];
        for (int i = 1; i < tree.length; i++) {
            tree[i] = Math.addExact(tree[i], values[i - 1]);
            long parent = (long) i + (i & -i);
            if (parent < tree.length) {
                tree[(int) parent] = Math.addExact(tree[(int) parent], tree[i]);
            }
        }
    }

    /** Returns the number of indexed values. */
    public int size() {
        return tree.length - 1;
    }

    /** Adds delta to one element. Validates every affected sum before mutation. */
    public void add(int index, long delta) {
        Objects.checkIndex(index, size());
        for (long i = (long) index + 1; i < tree.length; i += i & -i) {
            Math.addExact(tree[(int) i], delta);
        }
        for (long i = (long) index + 1; i < tree.length; i += i & -i) {
            tree[(int) i] += delta;
        }
    }

    /** Returns the sum over [0, endExclusive); an empty prefix sums to zero. */
    public long prefixSum(int endExclusive) {
        Objects.checkFromToIndex(0, endExclusive, size());
        long sum = 0;
        for (int i = endExclusive; i > 0; i -= i & -i) {
            sum = Math.addExact(sum, tree[i]);
        }
        return sum;
    }

    /** Returns the sum over [fromInclusive, toExclusive). */
    public long rangeSum(int fromInclusive, int toExclusive) {
        Objects.checkFromToIndex(fromInclusive, toExclusive, size());
        if (fromInclusive == toExclusive) {
            return 0;
        }
        return Math.subtractExact(prefixSum(toExclusive), prefixSum(fromInclusive));
    }
}
