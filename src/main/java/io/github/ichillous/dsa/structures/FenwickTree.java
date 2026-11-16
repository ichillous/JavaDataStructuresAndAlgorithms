package io.github.ichillous.dsa.structures;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * A binary indexed tree for point additions and half-open range sums.
 * Indexing is zero based at the API and one based internally. Construction takes
 * O(n) time and space; updates and prefix/range queries take O(log n) time.
 * Construction or an update throws {@link ArithmeticException} if and only if some final node
 * sum would be outside the signed-long range, and a failed update leaves the tree unchanged. Queries are exact:
 * {@code prefixSum} and {@code rangeSum} throw {@link ArithmeticException} only when the true
 * requested sum is outside the signed-long range, never because a partial sum overflowed.
 * Not thread safe.
 */
public final class FenwickTree {
    private final long[] tree;

    /**
     * Creates a tree from a defensive copy of the values in O(n) time.
     *
     * @throws ArithmeticException if and only if some final node sum (the exact sum of the values
     *         in its range [i - lowbit(i) + 1, i], one based) is outside the signed-long range;
     *         partial sums that cancel do not cause a failure
     */
    public FenwickTree(long[] values) {
        Objects.requireNonNull(values, "values");
        tree = new long[Math.addExact(values.length, 1)];
        try {
            for (int i = 1; i < tree.length; i++) {
                tree[i] = Math.addExact(tree[i], values[i - 1]);
                long parent = (long) i + (i & -i);
                if (parent < tree.length) {
                    tree[(int) parent] = Math.addExact(tree[(int) parent], tree[i]);
                }
            }
        } catch (ArithmeticException partialOverflow) {
            // A partial accumulation overflowed; decide exactly using the true node sums.
            BigInteger[] exact = new BigInteger[tree.length];
            Arrays.fill(exact, BigInteger.ZERO);
            for (int i = 1; i < tree.length; i++) {
                exact[i] = exact[i].add(BigInteger.valueOf(values[i - 1]));
                long parent = (long) i + (i & -i);
                if (parent < tree.length) {
                    exact[(int) parent] = exact[(int) parent].add(exact[i]);
                }
            }
            for (int i = 1; i < tree.length; i++) {
                tree[i] = exact[i].longValueExact();
            }
        }
    }

    /** Returns the number of indexed values. */
    public int size() {
        return tree.length - 1;
    }

    /**
     * Adds delta to one element in O(log n) time.
     *
     * @throws IndexOutOfBoundsException if the index is outside [0, size)
     * @throws ArithmeticException if and only if some updated node value would leave the
     *         signed-long range; every node is validated before any is changed, so a failed
     *         update leaves the tree unchanged
     */
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
