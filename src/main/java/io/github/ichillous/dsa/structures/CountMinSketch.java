package io.github.ichillous.dsa.structures;

import java.util.Objects;

/**
 * A Count-Min sketch: a {@code depth x width} table of {@code long} counters that
 * estimates how often each item was added, in sublinear space.
 *
 * <p><b>Invariant.</b> Each added count is applied to one cell per row, so every
 * cell is at least the sum of the counts of the items hashed to it. Therefore
 * {@link #estimate} (the minimum over the item's cells) is always at least the true
 * count: errors are one-sided overestimates caused by collisions. With
 * {@code width = ceil(e / epsilon)} and {@code depth = ceil(ln(1 / delta))} the
 * overestimate is at most {@code epsilon * totalCount()} with probability at least
 * {@code 1 - delta} for a fixed item, assuming the row hashes behave independently.
 *
 * <p><b>Hashing.</b> Row {@code i} uses {@code floorMod(splitMix64(hashCode ^ seed[i]),
 * width)}, where the hash code is sign-extended to 64 bits and
 * {@code seed[i] = splitMix64(0x5EED5EED5EED5EEDL + i)}. SplitMix64 uses the constants
 * {@code 0x9E3779B97F4A7C15}, {@code 0xBF58476D1CE4E5B9} and {@code 0x94D049BB133111EB}.
 * Everything is fixed, so results are reproducible. Items with equal
 * {@code hashCode()} share counts.
 *
 * <p><b>Contract.</b> Items must be non-null ({@link NullPointerException}); counts
 * must be non-negative ({@link IllegalArgumentException}); there is no removal. The table is capped at
 * 2^25 counters.
 * Overflow policy: if adding would overflow any counter or the total, an
 * {@link ArithmeticException} is thrown <em>before</em> anything is modified.
 * {@code add} and {@code estimate} take O(depth) time and O(1) extra space; storage is
 * {@code width * depth} longs. Not thread safe.
 *
 * @param <T> the item type
 */
public final class CountMinSketch<T> {
    /** Largest accepted {@code width * depth} (2^25 counters = 256 MiB), to fail fast instead of exhausting memory. */
    private static final long MAX_CELLS = 1L << 25;
    private static final long SEED_BASE = 0x5EED5EED5EED5EEDL;

    private final int width;
    private final int depth;
    private final long[][] table;
    private final long[] rowSeeds;
    private long total;

    /**
     * Creates an empty sketch with the given dimensions.
     *
     * @param width cells per row, at least 1
     * @param depth number of rows, at least 1
     * @throws IllegalArgumentException if either argument is below 1, or if
     *                                  {@code width * depth} exceeds 2^25 counters
     */
    public CountMinSketch(int width, int depth) {
        if (width < 1) {
            throw new IllegalArgumentException("width must be at least 1: " + width);
        }
        if (depth < 1) {
            throw new IllegalArgumentException("depth must be at least 1: " + depth);
        }
        if ((long) width * depth > MAX_CELLS) {
            throw new IllegalArgumentException(
                    "width * depth exceeds " + MAX_CELLS + ": " + width + " x " + depth);
        }
        this.width = width;
        this.depth = depth;
        this.table = new long[depth][width];
        this.rowSeeds = new long[depth];
        for (int i = 0; i < depth; i++) {
            rowSeeds[i] = mix(SEED_BASE + i);
        }
    }

    /**
     * Creates a sketch with {@code width = ceil(e / epsilon)} and
     * {@code depth = max(1, ceil(ln(1 / delta)))}. For example epsilon = 0.01 and
     * delta = 0.01 give width 272 and depth 5.
     *
     * @param epsilon relative error bound, greater than 0
     * @param delta   failure probability, strictly between 0 and 1
     * @param <T>     the item type
     * @return a new empty sketch
     * @throws IllegalArgumentException if a parameter is out of range or the table would exceed
     *                                  2^25 counters
     */
    public static <T> CountMinSketch<T> withErrorBounds(double epsilon, double delta) {
        if (!(epsilon > 0)) {
            throw new IllegalArgumentException("epsilon must be positive: " + epsilon);
        }
        if (!(delta > 0 && delta < 1)) {
            throw new IllegalArgumentException("delta must be in (0, 1): " + delta);
        }
        double w = Math.ceil(Math.E / epsilon);
        double d = Math.max(1, Math.ceil(Math.log(1 / delta)));
        if (w * d > MAX_CELLS) {
            throw new IllegalArgumentException(
                    "table would exceed " + MAX_CELLS + " counters for epsilon " + epsilon + ", delta " + delta);
        }
        return new CountMinSketch<>((int) Math.max(1, w), (int) d);
    }

    /**
     * Adds one occurrence of the item.
     *
     * @param item a non-null item
     * @throws NullPointerException if {@code item} is null
     * @throws ArithmeticException  if a counter or the total would overflow; nothing changes
     */
    public void add(T item) {
        add(item, 1);
    }

    /**
     * Adds {@code count} occurrences of the item; a count of 0 changes nothing.
     *
     * @param item  a non-null item
     * @param count occurrences to add, at least 0
     * @throws NullPointerException     if {@code item} is null
     * @throws IllegalArgumentException if {@code count} is negative
     * @throws ArithmeticException      if a counter or the total would overflow; the sketch
     *                                  is left unchanged
     */
    public void add(T item, long count) {
        Objects.requireNonNull(item, "item");
        if (count < 0) {
            throw new IllegalArgumentException("count must be non-negative: " + count);
        }
        // Every cell is a sum of a subset of the counts that were also added to total, and
        // all counts are non-negative, so each cell <= total. If total + count does not
        // overflow, no cell + count can, so this one check is enough to leave a failed
        // add with no change.
        long newTotal = Math.addExact(total, count);
        for (int row = 0; row < depth; row++) {
            table[row][column(item, row)] += count;
        }
        total = newTotal;
    }

    /**
     * Returns an upper bound on the item's true count: never below it, and with high
     * probability within {@code epsilon * totalCount()} above it. Absent items may
     * still return a positive value.
     *
     * @param item a non-null item
     * @return the minimum counter over the item's cells
     * @throws NullPointerException if {@code item} is null
     */
    public long estimate(T item) {
        Objects.requireNonNull(item, "item");
        long min = Long.MAX_VALUE;
        for (int row = 0; row < depth; row++) {
            min = Math.min(min, table[row][column(item, row)]);
        }
        return min;
    }

    /** Returns the sum of all counts added. */
    public long totalCount() {
        return total;
    }

    /** Returns the number of cells per row. */
    public int width() {
        return width;
    }

    /** Returns the number of rows. */
    public int depth() {
        return depth;
    }

    private int column(T item, int row) {
        return (int) Math.floorMod(mix(item.hashCode() ^ rowSeeds[row]), (long) width);
    }

    /** One SplitMix64 step: increment by the golden gamma, then mix. */
    private static long mix(long x) {
        long z = x + 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
