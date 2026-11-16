package io.github.ichillous.dsa.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Uniform random sampling: reservoir sampling of a stream and the Fisher-Yates shuffle.
 *
 * <p>Every method takes its source of randomness from the caller as a
 * {@link RandomGenerator}, so a seeded generator such as {@link java.util.SplittableRandom}
 * makes runs reproducible. Bounded draws use {@link RandomGenerator#nextInt(int)}, which is
 * unbiased; reducing {@code nextInt()} with the remainder operator would favour small values
 * whenever the bound does not divide 2<sup>32</sup>. The results are exactly uniform only if
 * the generator is; this class adds no bias of its own.
 */
public final class Sampling {
    private Sampling() {}

    /**
     * Returns a uniform random sample of {@code k} items from {@code items} without knowing
     * its length in advance (Algorithm R).
     *
     * <p>Invariant: after the first {@code m >= k} items have been read, the reservoir holds
     * each of them with probability {@code k / m}. Item number {@code m + 1} (1-based) is kept
     * with probability {@code k / (m + 1)}, by drawing {@code j} uniformly from
     * {@code [0, m + 1)} and replacing slot {@code j} when {@code j < k}. A previously held
     * item survives with probability {@code (k / m) (1 - 1 / (m + 1)) = k / (m + 1)}, which
     * preserves the invariant. The order of the returned sample is not meaningful.
     *
     * <p>Behaviour: {@code k = 0} returns an empty list without reading {@code items}. If the
     * stream has {@code n <= k} items, all of them are returned in encounter order, and no
     * random numbers are drawn. Duplicates are separate items. Elements must be non-null. The
     * iterable is traversed once and not modified; the returned list is immutable and owned by
     * the caller. Streams longer than {@link Integer#MAX_VALUE} items use
     * {@link RandomGenerator#nextLong(long)} for the later draws.
     *
     * <p>Time O(n) with one random draw per item beyond the first {@code k}; auxiliary space
     * O(min(k, n)).
     *
     * @param items the stream to sample, read exactly once unless {@code k} is 0
     * @param k the sample size, at least 0
     * @param random the source of randomness
     * @param <T> element type
     * @return an immutable list of {@code min(k, n)} items
     * @throws NullPointerException if an argument or an element is null
     * @throws IllegalArgumentException if {@code k} is negative
     */
    public static <T> List<T> reservoirSample(Iterable<? extends T> items, int k, RandomGenerator random) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(random, "random");
        if (k < 0) {
            throw new IllegalArgumentException("k must be non-negative: " + k);
        }
        if (k == 0) {
            return List.of();
        }
        List<T> reservoir = new ArrayList<>(Math.min(k, 16));
        long seen = 0;
        for (T item : items) {
            Objects.requireNonNull(item, "element");
            seen++;
            if (reservoir.size() < k) {
                reservoir.add(item);
                continue;
            }
            long slot = seen <= Integer.MAX_VALUE ? random.nextInt((int) seen) : random.nextLong(seen);
            if (slot < k) {
                reservoir.set((int) slot, item);
            }
        }
        return List.copyOf(reservoir);
    }

    /**
     * Shuffles {@code values} in place so that every one of the {@code n!} orders is equally
     * likely (Fisher-Yates).
     *
     * <p>Invariant: before step {@code i} (counting down from {@code n - 1}), positions
     * {@code (i, n)} hold a uniformly chosen arrangement of the elements already fixed there.
     * Step {@code i} swaps position {@code i} with a position {@code j} drawn uniformly from
     * {@code [0, i]}, so each remaining element is equally likely to land at {@code i}. The
     * product of choices {@code n (n - 1) ... 2} equals {@code n!} and each order arises from
     * exactly one sequence of choices. Duplicate values are treated as distinct positions.
     * Arrays of length 0 or 1 are left alone and draw nothing.
     *
     * <p>This mutates the argument; copy it first to keep the original. Time O(n) with
     * {@code n - 1} random draws, auxiliary space O(1). A generator with fewer than
     * {@code log2(n!)} bits of state cannot reach every order, however correct the algorithm.
     *
     * @param values the array to permute in place
     * @param random the source of randomness
     * @throws NullPointerException if an argument is null
     */
    public static void shuffle(int[] values, RandomGenerator random) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(random, "random");
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
    }
}
