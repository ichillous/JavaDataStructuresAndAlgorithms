package io.github.ichillous.dsa.structures;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A binary indexed tree for point additions and half-open range sums.
 * Indexing is zero based at the API and one based internally. Construction takes
 * O(n) time and space; updates and prefix/range queries take O(log n) time.
 * A stored node sum that overflows a signed long makes construction or an update throw
 * {@link ArithmeticException}, and a failed update leaves the tree unchanged. Queries are exact:
 * {@code prefixSum} and {@code rangeSum} throw {@link ArithmeticException} only when the true
 * requested sum is outside the signed-long range, never because a partial sum overflowed.
 * Not thread safe.
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

    /**
     * Returns the exact sum over [0, endExclusive); an empty prefix sums to zero. Throws
     * {@link ArithmeticException} if and only if the true sum is outside the signed-long range.
     */
    public long prefixSum(int endExclusive) {
        Objects.checkFromToIndex(0, endExclusive, size());
        return exactSum(endExclusive, 0);
    }

    /**
     * Returns the exact sum over [fromInclusive, toExclusive). Throws {@link ArithmeticException}
     * if and only if the true sum is outside the signed-long range, even when a prefix sum used
     * to compute it would overflow.
     */
    public long rangeSum(int fromInclusive, int toExclusive) {
        Objects.checkFromToIndex(fromInclusive, toExclusive, size());
        return exactSum(toExclusive, fromInclusive);
    }

    /**
     * Returns prefix(upper) - prefix(lower) from the O(log n) contributing nodes. The fast path
     * uses checked long arithmetic; if any intermediate step overflows, the nodes are re-added in
     * a BigInteger and the final value is checked once, so only a true overflow escapes.
     */
    private long exactSum(int upper, int lower) {
        try {
            long sum = 0;
            for (int i = upper; i > 0; i -= i & -i) {
                sum = Math.addExact(sum, tree[i]);
            }
            for (int i = lower; i > 0; i -= i & -i) {
                sum = Math.subtractExact(sum, tree[i]);
            }
            return sum;
        } catch (ArithmeticException intermediateOverflow) {
            BigInteger exact = BigInteger.ZERO;
            for (int i = upper; i > 0; i -= i & -i) {
                exact = exact.add(BigInteger.valueOf(tree[i]));
            }
            for (int i = lower; i > 0; i -= i & -i) {
                exact = exact.subtract(BigInteger.valueOf(tree[i]));
            }
            return exact.longValueExact();
        }
    }
}
