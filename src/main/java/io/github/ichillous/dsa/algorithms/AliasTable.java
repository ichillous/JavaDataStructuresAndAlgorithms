package io.github.ichillous.dsa.algorithms;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Weighted sampling of indices in O(1) per draw, built in O(n) with Vose's alias method.
 *
 * <p>Index {@code i} is returned with probability {@code weights[i] / sum(weights)}. The
 * table has {@code n} columns of equal width {@code 1/n}; column {@code i} holds index
 * {@code i} with probability {@code cutoff[i]} and a second index {@code alias[i]} otherwise.
 * Invariant: for every index {@code j}, {@code (cutoff[j] + sum of (1 - cutoff[i]) over the
 * columns i with alias[i] = j) / n} equals the normalized weight of {@code j}. A draw picks a
 * column uniformly, then a biased coin chooses between the column's own index and its alias.
 *
 * <p>Construction scales the weights to mean 1, splits the indices into those below 1
 * ("small") and the rest ("large"), and repeatedly pairs a small column with a large donor:
 * the small column keeps its own mass and the donor fills the rest of the column, losing that
 * much mass. Floating-point rounding can leave entries in either worklist after the other is
 * empty; their true mass is 1 up to rounding, so they get cutoff 1. A zero-weight index can
 * therefore never be returned.
 *
 * <p>Weights are copied on construction; later changes to the caller's array do not affect
 * the table, which is immutable (draws use only the caller's generator). Construction time
 * and space O(n); {@link #sample} time O(1) with one {@code nextInt} and one
 * {@code nextDouble}.
 */
public final class AliasTable {
    private final double[] normalized;
    private final double[] cutoff;
    private final int[] alias;

    /**
     * Builds the table for the given weights.
     *
     * @param weights one weight per index, each finite and at least 0, with a finite positive
     *     sum; the array is copied and not modified
     * @throws NullPointerException if {@code weights} is null
     * @throws IllegalArgumentException if {@code weights} is empty, contains a NaN, an
     *     infinity or a negative value, or sums to zero or to infinity
     */
    public AliasTable(double[] weights) {
        Objects.requireNonNull(weights, "weights");
        if (weights.length == 0) {
            throw new IllegalArgumentException("weights must not be empty");
        }
        double sum = 0;
        int heaviest = 0;
        for (int i = 0; i < weights.length; i++) {
            double w = weights[i];
            if (!Double.isFinite(w) || w < 0) {
                throw new IllegalArgumentException("weights must be finite and non-negative: " + w);
            }
            sum += w;
            if (w > weights[heaviest]) {
                heaviest = i;
            }
        }
        if (!(sum > 0) || !Double.isFinite(sum)) {
            throw new IllegalArgumentException("weights must have a finite positive sum: " + sum);
        }
        int n = weights.length;
        normalized = new double[n];
        double[] scaled = new double[n];
        Deque<Integer> small = new ArrayDeque<>();
        Deque<Integer> large = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            normalized[i] = weights[i] / sum;
            scaled[i] = normalized[i] * n;
            (scaled[i] < 1.0 ? small : large).push(i);
        }
        cutoff = new double[n];
        alias = new int[n];
        while (!small.isEmpty() && !large.isEmpty()) {
            int s = small.pop();
            int l = large.pop();
            cutoff[s] = scaled[s];
            alias[s] = l;
            scaled[l] = (scaled[l] + scaled[s]) - 1.0;
            (scaled[l] < 1.0 ? small : large).push(l);
        }
        for (int i : large) {
            cutoff[i] = 1.0;
            alias[i] = i;
        }
        for (int i : small) {
            // Rounding leftover: the true mass is 1. A zero weight must still never be drawn.
            boolean zero = normalized[i] == 0;
            cutoff[i] = zero ? 0.0 : 1.0;
            alias[i] = zero ? heaviest : i;
        }
    }

    /**
     * Returns the number of indices.
     *
     * @return the length of the weight array the table was built from, at least 1
     */
    public int size() {
        return normalized.length;
    }

    /**
     * Returns the probability that {@link #sample} returns {@code index}: the weight divided
     * by the sum of weights, computed at construction (not reconstructed from the table).
     *
     * @param index an index in {@code [0, size())}
     * @return a probability in {@code [0, 1]}
     * @throws IndexOutOfBoundsException if {@code index} is outside {@code [0, size())}
     */
    public double probability(int index) {
        Objects.checkIndex(index, normalized.length);
        return normalized[index];
    }

    /**
     * Draws one index with probability {@link #probability}; indices of weight 0 are never
     * returned. Time O(1), auxiliary space O(1).
     *
     * @param random the source of randomness
     * @return an index in {@code [0, size())}
     * @throws NullPointerException if {@code random} is null
     */
    public int sample(RandomGenerator random) {
        Objects.requireNonNull(random, "random");
        int column = random.nextInt(normalized.length);
        return random.nextDouble() < cutoff[column] ? column : alias[column];
    }

    /** Test hook: the probability that a draw returns {@code index}, reconstructed from the table. */
    double tableProbability(int index) {
        double mass = cutoff[index];
        for (int i = 0; i < alias.length; i++) {
            if (alias[i] == index && i != index) {
                mass += 1.0 - cutoff[i];
            }
        }
        return mass / normalized.length;
    }
}
