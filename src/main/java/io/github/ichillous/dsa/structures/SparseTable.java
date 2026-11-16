package io.github.ichillous.dsa.structures;

import java.util.Objects;
import java.util.OptionalLong;

/**
 * An immutable sparse table answering range-minimum queries over static {@code long} values in
 * O(1) time.
 *
 * <p>Invariant: row {@code k} stores, for every start {@code i} with {@code i + 2^k <= size},
 * the minimum of the block {@code [i, i + 2^k)}. Row 0 is a copy of the input, and row
 * {@code k} combines two adjacent blocks of row {@code k - 1}. A query of length {@code len}
 * uses {@code k = floor(log2(len))} and takes the minimum of the two blocks
 * {@code [from, from + 2^k)} and {@code [to - 2^k, to)}. They overlap when {@code len} is not
 * a power of two, which is harmless because minimum is idempotent: {@code min(x, x) == x}.
 * Sum is not idempotent, so the same trick would count the overlap twice; use
 * {@link FenwickTree} or {@link LazySegmentTree} for sums.
 *
 * <p>Ranges are half-open. An empty range returns {@link OptionalLong#empty()}, so every
 * {@code long}, including {@link Long#MAX_VALUE}, remains a usable minimum. Duplicates need no
 * special handling. No arithmetic is performed on the values, so nothing can overflow.
 *
 * <p>Construction takes O(n log n) time and stores at most {@code n * (floor(log2 n) + 1)}
 * longs; a query takes O(1) time and space. There are no mutators: build a new table when the
 * data changes, or use {@link SegmentTree} for point updates. Instances are immutable and may
 * be shared between threads once constructed.
 */
public final class SparseTable {
    private final int size;
    private final long[][] minima;

    /**
     * Builds a table from a defensive copy of {@code values}, which may be empty. Later changes
     * to the caller's array do not affect the table, and the input is never modified.
     * O(n log n) time and space.
     *
     * @throws NullPointerException if {@code values} is null
     */
    public SparseTable(long[] values) {
        Objects.requireNonNull(values, "values");
        size = values.length;
        int levels = 32 - Integer.numberOfLeadingZeros(size);
        minima = new long[levels][];
        if (levels == 0) {
            return;
        }
        minima[0] = values.clone();
        for (int level = 1; level < levels; level++) {
            int half = 1 << (level - 1);
            long[] previous = minima[level - 1];
            long[] current = new long[size - (1 << level) + 1];
            for (int start = 0; start < current.length; start++) {
                current[start] = Math.min(previous[start], previous[start + half]);
            }
            minima[level] = current;
        }
    }

    /** Returns the number of indexed values. O(1). */
    public int size() {
        return size;
    }

    /**
     * Returns the minimum over {@code [fromInclusive, toExclusive)}, or an empty optional for an
     * empty range. Requires {@code 0 <= fromInclusive <= toExclusive <= size()}; otherwise throws
     * {@link IndexOutOfBoundsException}. O(1) time and space.
     */
    public OptionalLong rangeMin(int fromInclusive, int toExclusive) {
        Objects.checkFromToIndex(fromInclusive, toExclusive, size);
        if (fromInclusive == toExclusive) {
            return OptionalLong.empty();
        }
        int level = 31 - Integer.numberOfLeadingZeros(toExclusive - fromInclusive);
        long[] blocks = minima[level];
        return OptionalLong.of(Math.min(blocks[fromInclusive], blocks[toExclusive - (1 << level)]));
    }
}
