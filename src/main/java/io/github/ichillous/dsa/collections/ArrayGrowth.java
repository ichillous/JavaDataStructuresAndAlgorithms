package io.github.ichillous.dsa.collections;

/**
 * Capacity-growth policy shared by the array-backed collections, following the JDK's
 * {@code ArrayList} policy: grow to the larger of the required length and double the current
 * capacity, capped at {@link #MAX_ARRAY_LENGTH}. Not instantiable.
 */
final class ArrayGrowth {
    /** The largest array length that is safe to request on common JVMs. */
    static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    private ArrayGrowth() {
    }

    /**
     * Returns the new capacity for a buffer of {@code currentCapacity} that must hold at least
     * {@code minimumCapacity} elements. Runs in O(1) time and space.
     *
     * @throws OutOfMemoryError ("Required array length too large") only when
     *         {@code minimumCapacity} is negative (overflowed) or exceeds {@link #MAX_ARRAY_LENGTH}
     */
    static int grow(int currentCapacity, int minimumCapacity) {
        if (minimumCapacity < 0 || minimumCapacity > MAX_ARRAY_LENGTH) {
            throw new OutOfMemoryError("Required array length too large");
        }
        long doubled = 2L * currentCapacity;
        return (int) Math.min(Math.max(doubled, minimumCapacity), MAX_ARRAY_LENGTH);
    }
}
