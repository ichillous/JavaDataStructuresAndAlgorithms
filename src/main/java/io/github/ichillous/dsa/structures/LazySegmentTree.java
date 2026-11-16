package io.github.ichillous.dsa.structures;

import java.util.Arrays;
import java.util.Objects;

/**
 * A segment tree with lazy propagation: add one value to every element of a half-open range,
 * and read exact range sums, each in O(log n) time.
 *
 * <p>Each node covers an interval {@code [low, high)} and stores the sum, minimum and maximum of
 * its elements, plus a <em>pending addition</em> (lazy tag): an amount already applied to the
 * node's own aggregates but not yet to its children. Invariant: the true aggregates of a node are
 * its stored aggregates plus the pending additions of all its strict ancestors (multiplied by the
 * node's length for the sum). An update applies to a fully covered node in O(1) and stops there.
 * A partially covered node first pushes its pending addition down to both children, then
 * recurses and recombines them. Queries never push: they carry the ancestors' pending additions
 * down the search path instead, so a query does not modify the tree.
 *
 * <p>Overflow policy. Element values are always signed {@code long}s.
 * {@link #rangeAdd(int, int, long)} throws {@link ArithmeticException}, and leaves the tree
 * unchanged, exactly when some element of the range would leave {@code long} range. It reads the
 * range maximum (for a positive delta) or minimum (for a negative one) before modifying
 * anything. {@link #rangeSum(int, int)} returns the exact sum, or throws
 * {@link ArithmeticException} exactly when that true sum is outside {@code long} range. Internally,
 * sums and pending additions are exact signed 128-bit integers held in two longs. A node covers
 * at most 2^29 elements, so its sum needs at most 93 bits. A pending addition is the
 * difference between two values an element has held, so it needs at most 65 bits: it can exceed
 * {@code long} range, for example after adding {@link Long#MAX_VALUE} twice to elements that
 * started at {@link Long#MIN_VALUE}. Stored minima and maxima are values that elements actually
 * held, so they are ordinary longs.
 *
 * <p>The constructor copies its input; the tree never reads or writes the caller's array again.
 * An empty tree is allowed. Empty ranges sum to zero and adding to one does nothing. Invalid
 * ranges throw {@link IndexOutOfBoundsException} before any overflow check.
 *
 * <p>Costs: construction O(n) time and O(n) space (node arrays of {@code 2 * p} entries, where
 * {@code p} is the smallest power of two at least n). Each {@code rangeAdd} and {@code rangeSum}
 * takes O(log n) time and O(log n) recursion stack, allocating O(log n) small temporary values.
 * This mutable class is not thread safe.
 */
public final class LazySegmentTree {
    private final int size;
    private final Int128[] sums;
    private final Int128[] pending;
    private final long[] minima;
    private final long[] maxima;

    /**
     * Creates a tree holding a copy of {@code values}, which may be empty. Later changes to the
     * caller's array do not affect the tree, and the input is never modified. O(n) time.
     *
     * @throws NullPointerException if {@code values} is null
     * @throws ArithmeticException if {@code values} has more than 2^29 elements, so the node
     *         arrays of {@code 2 * p} entries could not be indexed by {@code int}
     */
    public LazySegmentTree(long[] values) {
        Objects.requireNonNull(values, "values");
        size = values.length;
        int capacity = 1;
        while (capacity < size) {
            capacity = Math.multiplyExact(capacity, 2);
        }
        int nodes = Math.multiplyExact(capacity, 2);
        sums = new Int128[nodes];
        pending = new Int128[nodes];
        minima = new long[nodes];
        maxima = new long[nodes];
        Arrays.fill(sums, Int128.ZERO);
        Arrays.fill(pending, Int128.ZERO);
        if (size > 0) {
            build(1, 0, size, values);
        }
    }

    /** Returns the number of elements. O(1). */
    public int size() {
        return size;
    }

    /**
     * Adds {@code delta} to every element in {@code [fromInclusive, toExclusive)}. An empty range
     * or a zero delta changes nothing. The range maximum (or minimum, for a negative delta) is
     * checked before anything is modified. O(log n) time and O(log n) recursion stack.
     *
     * @throws IndexOutOfBoundsException unless
     *         {@code 0 <= fromInclusive <= toExclusive <= size()}
     * @throws ArithmeticException if some element in the range would leave {@code long} range;
     *         the tree is then unchanged
     */
    public void rangeAdd(int fromInclusive, int toExclusive, long delta) {
        Objects.checkFromToIndex(fromInclusive, toExclusive, size);
        if (fromInclusive == toExclusive || delta == 0) {
            return;
        }
        boolean increase = delta > 0;
        long extreme = extreme(1, 0, size, fromInclusive, toExclusive, Int128.ZERO, increase);
        if (increase ? extreme > Long.MAX_VALUE - delta : extreme < Long.MIN_VALUE - delta) {
            throw new ArithmeticException("adding " + delta + " to element value " + extreme
                    + " in [" + fromInclusive + ", " + toExclusive + ") leaves long range");
        }
        add(1, 0, size, fromInclusive, toExclusive, Int128.of(delta));
    }

    /**
     * Returns the exact sum of the elements in {@code [fromInclusive, toExclusive)}; an empty
     * range sums to zero. Does not modify the tree. O(log n) time and O(log n) recursion stack.
     *
     * @throws IndexOutOfBoundsException unless
     *         {@code 0 <= fromInclusive <= toExclusive <= size()}
     * @throws ArithmeticException if the true sum is outside {@code long} range
     */
    public long rangeSum(int fromInclusive, int toExclusive) {
        Objects.checkFromToIndex(fromInclusive, toExclusive, size);
        if (fromInclusive == toExclusive) {
            return 0;
        }
        return sum(1, 0, size, fromInclusive, toExclusive, Int128.ZERO).toLongExact();
    }

    private void build(int node, int low, int high, long[] values) {
        if (high - low == 1) {
            sums[node] = Int128.of(values[low]);
            minima[node] = values[low];
            maxima[node] = values[low];
            return;
        }
        int middle = (low + high) >>> 1;
        build(2 * node, low, middle, values);
        build(2 * node + 1, middle, high, values);
        pull(node);
    }

    /** Adds delta to [from, to) below node; the caller has already checked every result fits. */
    private void add(int node, int low, int high, int from, int to, Int128 delta) {
        if (from <= low && high <= to) {
            apply(node, high - low, delta);
            return;
        }
        push(node, low, high);
        int middle = (low + high) >>> 1;
        if (from < middle) {
            add(2 * node, low, middle, from, to, delta);
        }
        if (to > middle) {
            add(2 * node + 1, middle, high, from, to, delta);
        }
        pull(node);
    }

    /**
     * Adds delta to every element under node. Callers reach node with every ancestor's pending
     * addition already pushed, so the new minimum and maximum are true element values and the
     * exact conversions below cannot fail.
     */
    private void apply(int node, int length, Int128 delta) {
        sums[node] = sums[node].plus(delta.times(length));
        minima[node] = Int128.of(minima[node]).plus(delta).toLongExact();
        maxima[node] = Int128.of(maxima[node]).plus(delta).toLongExact();
        if (length > 1) {
            pending[node] = pending[node].plus(delta);
        }
    }

    private void push(int node, int low, int high) {
        Int128 addition = pending[node];
        if (addition.isZero()) {
            return;
        }
        int middle = (low + high) >>> 1;
        apply(2 * node, middle - low, addition);
        apply(2 * node + 1, high - middle, addition);
        pending[node] = Int128.ZERO;
    }

    /** Recomputes node from its children; node's own pending addition is zero here. */
    private void pull(int node) {
        sums[node] = sums[2 * node].plus(sums[2 * node + 1]);
        minima[node] = Math.min(minima[2 * node], minima[2 * node + 1]);
        maxima[node] = Math.max(maxima[2 * node], maxima[2 * node + 1]);
    }

    /** Returns the true sum over [from, to) below node; above holds the ancestors' additions. */
    private Int128 sum(int node, int low, int high, int from, int to, Int128 above) {
        if (from <= low && high <= to) {
            return sums[node].plus(above.times(high - low));
        }
        Int128 carried = above.plus(pending[node]);
        int middle = (low + high) >>> 1;
        Int128 total = Int128.ZERO;
        if (from < middle) {
            total = total.plus(sum(2 * node, low, middle, from, to, carried));
        }
        if (to > middle) {
            total = total.plus(sum(2 * node + 1, middle, high, from, to, carried));
        }
        return total;
    }

    /** Returns the true maximum (or minimum) element value over the nonempty [from, to). */
    private long extreme(int node, int low, int high, int from, int to, Int128 above,
            boolean maximum) {
        if (from <= low && high <= to) {
            return Int128.of(maximum ? maxima[node] : minima[node]).plus(above).toLongExact();
        }
        Int128 carried = above.plus(pending[node]);
        int middle = (low + high) >>> 1;
        if (to <= middle) {
            return extreme(2 * node, low, middle, from, to, carried, maximum);
        }
        if (from >= middle) {
            return extreme(2 * node + 1, middle, high, from, to, carried, maximum);
        }
        long left = extreme(2 * node, low, middle, from, to, carried, maximum);
        long right = extreme(2 * node + 1, middle, high, from, to, carried, maximum);
        return maximum ? Math.max(left, right) : Math.min(left, right);
    }

    /**
     * An exact signed 128-bit two's-complement integer held as two longs. Node sums need at most
     * 93 bits and pending additions at most 65; a pending addition times a node length stays
     * below 2^93 in magnitude. These operations therefore never overflow here.
     */
    private record Int128(long high, long low) {
        static final Int128 ZERO = new Int128(0, 0);

        static Int128 of(long value) {
            return new Int128(value >> 63, value);
        }

        Int128 plus(Int128 other) {
            long sumLow = low + other.low;
            long carry = Long.compareUnsigned(sumLow, low) < 0 ? 1 : 0;
            return new Int128(high + other.high + carry, sumLow);
        }

        /** Multiplies by a nonnegative factor; the carry is the unsigned high word of low * factor. */
        Int128 times(int factor) {
            long carry = Math.multiplyHigh(low, factor) + ((low >> 63) & factor);
            return new Int128(high * factor + carry, low * factor);
        }

        boolean isZero() {
            return high == 0 && low == 0;
        }

        long toLongExact() {
            if (high != (low >> 63)) {
                throw new ArithmeticException("value outside signed long range");
            }
            return low;
        }
    }
}
