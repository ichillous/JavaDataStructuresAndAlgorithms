package io.github.ichillous.dsa.structures;

import java.util.Objects;

/**
 * A Bloom filter: a fixed-size bit array that answers "possibly present" or
 * "definitely absent" for added elements.
 *
 * <p><b>Invariant.</b> Every bit selected by an added element stays set forever, so
 * {@link #mightContain} never returns {@code false} for an element that was added
 * (no false negatives). It may return {@code true} for an absent element when other
 * elements happened to set all of its bits (a false positive). There is no deletion:
 * clearing a bit could erase evidence for other elements.
 *
 * <p><b>Hashing.</b> The element's {@code hashCode()} is mixed with SplitMix64
 * (constants {@code 0x9E3779B97F4A7C15}, {@code 0xBF58476D1CE4E5B9},
 * {@code 0x94D049BB133111EB}) into a 64-bit value whose low and high 32 bits are
 * {@code h1} and {@code h2} (both signed). The probe step is
 * {@code step = 1 + floorMod(h2, bitCount - 1)} when {@code bitCount > 1} (0 when
 * {@code bitCount == 1}), and the i-th bit index is
 * {@code floorMod(h1 + i * step, bitCount)} (Kirsch-Mitzenmacher double hashing),
 * computed in {@code long} arithmetic so it cannot overflow. Because the step lies in
 * {@code [1, bitCount - 1]} it is never a multiple of {@code bitCount}, so the probes
 * of one element never all collapse onto a single bit when {@code hashCount > 1}
 * and {@code bitCount > 1}. (Using {@code h2} directly would collapse them for about
 * one element in {@code bitCount}.) The mapping is fixed, so behavior is reproducible.
 *
 * <p><b>Limitation.</b> Only {@code hashCode()} is consulted, so distinct elements
 * with equal hash codes (for example {@code "Aa"} and {@code "BB"}) are
 * indistinguishable. The quality of the false-positive rate also depends on
 * {@code hashCode()} spreading elements over its 32 bits.
 *
 * <p><b>Contract.</b> Elements must be non-null ({@link NullPointerException}).
 * Adding an element already present is allowed and still counts in
 * {@link #insertions()}. {@code add} and {@code mightContain} take O(k) time and
 * O(1) extra space; storage is {@code ceil(m / 64)} longs. Not thread safe.
 *
 * @param <T> the element type
 */
public final class BloomFilter<T> {
    private static final double LN2 = Math.log(2);
    /** Largest accepted bit count (2^30 bits = 128 MiB), to fail fast instead of exhausting memory. */
    private static final int MAX_BITS = 1 << 30;

    private final long[] words;
    private final int bitCount;
    private final int hashCount;
    private long insertions;

    /**
     * Creates an empty filter with exactly {@code bitCount} bits and
     * {@code hashCount} indices per element.
     *
     * @param bitCount  number of bits, from 1 to 2^30
     * @param hashCount indices per element, at least 1
     * @throws IllegalArgumentException if either argument is below 1, or if
     *                                  {@code bitCount} exceeds 2^30
     */
    public BloomFilter(int bitCount, int hashCount) {
        if (bitCount < 1) {
            throw new IllegalArgumentException("bitCount must be at least 1: " + bitCount);
        }
        if (hashCount < 1) {
            throw new IllegalArgumentException("hashCount must be at least 1: " + hashCount);
        }
        if (bitCount > MAX_BITS) {
            throw new IllegalArgumentException("bitCount exceeds " + MAX_BITS + ": " + bitCount);
        }
        this.bitCount = bitCount;
        this.hashCount = hashCount;
        this.words = new long[(int) (((long) bitCount + 63) >>> 6)];
    }

    /**
     * Creates a filter sized for {@code n} insertions at the target false-positive rate
     * {@code p}: {@code m = ceil(-n ln p / (ln 2)^2)} bits and
     * {@code k = max(1, round(m / n * ln 2))} indices. For example, n = 1000 and
     * p = 0.01 give m = 9586 and k = 7.
     *
     * @param n                 expected insertions, at least 1
     * @param falsePositiveRate target rate, strictly between 0 and 1
     * @param <T>               the element type
     * @return a new empty filter
     * @throws IllegalArgumentException if {@code n < 1}, {@code p} is not in (0, 1), or
     *                                  the required bit count exceeds 2^30
     */
    public static <T> BloomFilter<T> forExpectedInsertions(int n, double falsePositiveRate) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be at least 1: " + n);
        }
        if (!(falsePositiveRate > 0 && falsePositiveRate < 1)) {
            throw new IllegalArgumentException(
                    "falsePositiveRate must be in (0, 1): " + falsePositiveRate);
        }
        double m = Math.ceil(-n * Math.log(falsePositiveRate) / (LN2 * LN2));
        if (m > MAX_BITS) {
            throw new IllegalArgumentException("required bit count exceeds " + MAX_BITS);
        }
        int bits = (int) m;
        int hashes = (int) Math.max(1, Math.round((double) bits / n * LN2));
        return new BloomFilter<>(bits, hashes);
    }

    /**
     * Adds an element; duplicates are allowed and counted by {@link #insertions()}.
     *
     * @param element a non-null element
     * @throws NullPointerException if {@code element} is null
     */
    public void add(T element) {
        Objects.requireNonNull(element, "element");
        long mixed = mix(element.hashCode());
        long h1 = (int) mixed;
        long step = step(mixed);
        for (int i = 0; i < hashCount; i++) {
            int bit = bitAt(h1, step, i);
            words[bit >>> 6] |= 1L << (bit & 63);
        }
        insertions++;
    }

    /**
     * Returns {@code false} only if the element was definitely never added.
     *
     * @param element a non-null element
     * @return {@code false} if absent for certain, {@code true} if possibly present
     * @throws NullPointerException if {@code element} is null
     */
    public boolean mightContain(T element) {
        Objects.requireNonNull(element, "element");
        long mixed = mix(element.hashCode());
        long h1 = (int) mixed;
        long step = step(mixed);
        for (int i = 0; i < hashCount; i++) {
            int bit = bitAt(h1, step, i);
            if ((words[bit >>> 6] & (1L << (bit & 63))) == 0) {
                return false;
            }
        }
        return true;
    }

    /** Returns the number of bits {@code m}. */
    public int bitCount() {
        return bitCount;
    }

    /** Returns the number of indices {@code k} per element. */
    public int hashCount() {
        return hashCount;
    }

    /** Returns the number of {@link #add} calls so far, counting duplicates. */
    public long insertions() {
        return insertions;
    }

    /**
     * Returns the textbook estimate {@code (1 - e^(-k * insertions / m))^k} of the
     * false-positive probability, assuming uniformly random indices and distinct
     * elements. Duplicate adds make it an overestimate; it is 0 for an empty filter.
     *
     * @return a probability in [0, 1]
     */
    public double estimatedFalsePositiveRate() {
        double exponent = -(double) hashCount * insertions / bitCount;
        return Math.pow(1 - Math.exp(exponent), hashCount);
    }

    private int bitAt(long h1, long step, int i) {
        return Math.floorMod(h1 + i * step, bitCount);
    }

    /** Returns the k bit indices of an element; package-private so tests can check probe spread. */
    int[] bitIndices(T element) {
        Objects.requireNonNull(element, "element");
        long mixed = mix(element.hashCode());
        long h1 = (int) mixed;
        long step = step(mixed);
        int[] bits = new int[hashCount];
        for (int i = 0; i < hashCount; i++) {
            bits[i] = bitAt(h1, step, i);
        }
        return bits;
    }

    /** Probe step in [1, bitCount - 1], derived from the high half; 0 only when bitCount is 1. */
    private long step(long mixed) {
        return bitCount == 1 ? 0 : 1 + Math.floorMod((int) (mixed >>> 32), bitCount - 1);
    }

    /** SplitMix64 finalizer applied to a sign-extended hash code. */
    private static long mix(long x) {
        long z = x + 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
