package io.github.ichillous.dsa.algorithms;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.OptionalLong;

/**
 * Number-theory utilities on {@code long} with an exact-or-throw overflow policy: every result
 * is mathematically correct or the method throws {@link ArithmeticException}; nothing wraps.
 * Modular arithmetic is exact for every modulus up to {@link Long#MAX_VALUE}. All methods are
 * pure and thread-safe; returned lists are immutable.
 */
public final class NumberTheory {
    /** Largest accepted sieve limit; the sieve needs one bit per candidate. */
    public static final int MAX_SIEVE_LIMIT = 50_000_000;

    private static final long[] MILLER_RABIN_BASES = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37};

    private NumberTheory() {}

    /**
     * Bezout coefficients: {@code a * x + b * y == gcd} for the inputs of
     * {@link #extendedGcd}. {@code gcd} is nonnegative.
     *
     * @param gcd the greatest common divisor, at least 0
     * @param x coefficient of the first input
     * @param y coefficient of the second input
     */
    public record Bezout(long gcd, long x, long y) {
        /** Validates that the gcd is nonnegative. */
        public Bezout {
            if (gcd < 0) {
                throw new IllegalArgumentException("gcd must be nonnegative: " + gcd);
            }
        }
    }

    /**
     * Greatest common divisor by the Euclidean algorithm; the result is nonnegative, the signs of
     * the inputs are ignored and {@code gcd(0, 0) = 0}.
     *
     * <p>Invariant: {@code gcd(a, b) = gcd(b, a mod b)}, so the common divisors never change while
     * the second argument strictly shrinks in magnitude. Remainders use Java's signed
     * {@code %}, which never overflows, so {@code Long.MIN_VALUE} is handled without negating it.
     * Time O(log(min(|a|, |b|))), auxiliary space O(1).
     *
     * @throws ArithmeticException if the result is 2^63, which only happens for
     *     {@code gcd(MIN_VALUE, 0)}, {@code gcd(0, MIN_VALUE)} and {@code gcd(MIN_VALUE, MIN_VALUE)}
     */
    public static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return Math.absExact(a);
    }

    /**
     * Least common multiple, always nonnegative; {@code lcm(0, x) = 0}. Computed as
     * {@code |a / gcd * b|} so the intermediate value is as small as possible.
     * Time O(log(min(|a|, |b|))), auxiliary space O(1).
     *
     * @throws ArithmeticException if the result does not fit in a {@code long}
     */
    public static long lcm(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        long g = gcd(a, b);
        return Math.absExact(Math.multiplyExact(a / g, b));
    }

    /**
     * Extended Euclid: returns {@code (g, x, y)} with {@code a * x + b * y = g = gcd(a, b)} and
     * {@code g >= 0}. For {@code (0, 0)} the result is {@code (0, 1, 0)}. The coefficients are the
     * ones the standard iteration produces; they are small (bounded by the other input divided by
     * the gcd, up to sign) but not otherwise canonical.
     *
     * <p>Invariant: after every step each of the two running remainders is an integer combination
     * of {@code a} and {@code b} with the coefficients tracked alongside it. All arithmetic is
     * checked. Time O(log(min(|a|, |b|))), auxiliary space O(1).
     *
     * @throws ArithmeticException if the gcd is 2^63 or a coefficient does not fit in a
     *     {@code long}
     */
    public static Bezout extendedGcd(long a, long b) {
        long oldR = a;
        long r = b;
        long oldS = 1;
        long s = 0;
        long oldT = 0;
        long t = 1;
        while (r != 0) {
            if (r == -1) {
                // gcd is 1 and r itself is the answer; MIN_VALUE / -1 would overflow.
                oldR = r;
                oldS = s;
                oldT = t;
                break;
            }
            long q = oldR / r;
            long nextR = oldR - Math.multiplyExact(q, r);
            long nextS = Math.subtractExact(oldS, Math.multiplyExact(q, s));
            long nextT = Math.subtractExact(oldT, Math.multiplyExact(q, t));
            oldR = r;
            r = nextR;
            oldS = s;
            s = nextS;
            oldT = t;
            t = nextT;
        }
        if (oldR < 0) {
            return new Bezout(Math.negateExact(oldR), Math.negateExact(oldS), Math.negateExact(oldT));
        }
        return new Bezout(oldR, oldS, oldT);
    }

    /**
     * Computes {@code base^exponent mod modulus} by square-and-multiply, exact for every positive
     * {@code long} modulus. A negative {@code base} is reduced into {@code [0, modulus)} first.
     *
     * <p>Invariant: {@code result * base^remaining} is congruent to the answer after every step.
     * Each multiplication goes through an exact modular multiply that never overflows. Time
     * O(log exponent) modular multiplications, each O(1) when the product fits in 63 bits and
     * O(log modulus) otherwise; auxiliary space O(1).
     *
     * @param base any value
     * @param exponent at least 0; {@code x^0 = 1 mod modulus}, so the result is 0 when
     *     {@code modulus = 1}
     * @param modulus at least 1
     * @return a value in {@code [0, modulus)}
     * @throws IllegalArgumentException if {@code modulus < 1} or {@code exponent < 0}
     */
    public static long modPow(long base, long exponent, long modulus) {
        if (modulus < 1) {
            throw new IllegalArgumentException("modulus must be at least 1: " + modulus);
        }
        if (exponent < 0) {
            throw new IllegalArgumentException("exponent must be nonnegative: " + exponent);
        }
        long result = 1 % modulus;
        long b = Math.floorMod(base, modulus);
        long e = exponent;
        while (e > 0) {
            if ((e & 1) == 1) {
                result = mulMod(result, b, modulus);
            }
            b = mulMod(b, b, modulus);
            e >>= 1;
        }
        return result;
    }

    /**
     * Modular inverse: the {@code x} in {@code [0, modulus)} with {@code a * x == 1 (mod modulus)},
     * found with {@link #extendedGcd}. The result is empty when {@code gcd(a, modulus) != 1}.
     * Modulo 1 every number is congruent to 0 and the inverse is 0. Negative {@code a} is reduced
     * first. Time O(log modulus), auxiliary space O(1).
     *
     * @param modulus at least 1
     * @throws IllegalArgumentException if {@code modulus < 1}
     */
    public static OptionalLong modInverse(long a, long modulus) {
        if (modulus < 1) {
            throw new IllegalArgumentException("modulus must be at least 1: " + modulus);
        }
        Bezout bezout = extendedGcd(Math.floorMod(a, modulus), modulus);
        if (bezout.gcd() != 1) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(Math.floorMod(bezout.x(), modulus));
    }

    /**
     * Returns every prime {@code <= limit} in ascending order using the sieve of Eratosthenes.
     *
     * <p>Invariant: when the outer loop reaches {@code p} still unmarked, no smaller prime divides
     * it, so it is prime; marking starts at {@code p * p} because smaller multiples have a
     * smaller prime factor and are already marked. Time O(n log log n), auxiliary space O(n) bits
     * plus the result list.
     *
     * @param limit in {@code [0, MAX_SIEVE_LIMIT]}; limits 0 and 1 give an empty list
     * @return an immutable list
     * @throws IllegalArgumentException if {@code limit} is outside the range
     */
    public static List<Integer> primesUpTo(int limit) {
        if (limit < 0 || limit > MAX_SIEVE_LIMIT) {
            throw new IllegalArgumentException("limit must be in [0, " + MAX_SIEVE_LIMIT + "]: " + limit);
        }
        BitSet composite = new BitSet(limit + 1);
        for (long p = 2; p * p <= limit; p++) {
            if (!composite.get((int) p)) {
                for (long m = p * p; m <= limit; m += p) {
                    composite.set((int) m);
                }
            }
        }
        List<Integer> primes = new ArrayList<>();
        for (int n = 2; n <= limit; n++) {
            if (!composite.get(n)) {
                primes.add(n);
            }
        }
        return List.copyOf(primes);
    }

    /**
     * Deterministic Miller-Rabin primality test, correct for every {@code long}: negative numbers,
     * 0 and 1 are not prime. The twelve bases 2 through 37 are known to give no false positive
     * for any {@code n < 3.3 * 10^24}, which covers all 63-bit values. Small primes are checked
     * by direct division first, so every base is smaller than the candidate.
     *
     * <p>Invariant: writing {@code n - 1 = d * 2^s} with {@code d} odd, an odd prime {@code n}
     * satisfies, for every base {@code a} not divisible by {@code n}, either {@code a^d = 1} or
     * {@code a^(d * 2^r) = -1 (mod n)} for some {@code r < s}; a base violating both proves
     * {@code n} composite. Time O(log^2 n) bit operations per base at worst, auxiliary space O(1).
     */
    public static boolean isPrime(long n) {
        if (n < 2) {
            return false;
        }
        for (long p : MILLER_RABIN_BASES) {
            if (n == p) {
                return true;
            }
            if (n % p == 0) {
                return false;
            }
        }
        long d = n - 1;
        int s = Long.numberOfTrailingZeros(d);
        d >>= s;
        for (long a : MILLER_RABIN_BASES) {
            long x = modPow(a, d, n);
            if (x == 1 || x == n - 1) {
                continue;
            }
            boolean witnessesComposite = true;
            for (int r = 1; r < s; r++) {
                x = mulMod(x, x, n);
                if (x == n - 1) {
                    witnessesComposite = false;
                    break;
                }
            }
            if (witnessesComposite) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code F(n) mod modulus} for the Fibonacci numbers {@code F(0) = 0, F(1) = 1},
     * by raising the matrix {@code [[1, 1], [1, 0]]} to the {@code n}-th power: that power is
     * {@code [[F(n+1), F(n)], [F(n), F(n-1)]]}. Every product and sum is reduced modulo
     * {@code modulus}, exactly, so any positive {@code long} modulus works. Time O(log n) matrix
     * multiplications (eight modular multiplications each), auxiliary space O(1).
     *
     * @param n at least 0
     * @param modulus at least 1; the result is 0 when it is 1
     * @return a value in {@code [0, modulus)}
     * @throws IllegalArgumentException if {@code n < 0} or {@code modulus < 1}
     */
    public static long fibonacciMod(long n, long modulus) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be nonnegative: " + n);
        }
        if (modulus < 1) {
            throw new IllegalArgumentException("modulus must be at least 1: " + modulus);
        }
        long one = 1 % modulus;
        long[] result = {one, 0, 0, one};
        long[] base = {one, one, one, 0};
        long e = n;
        while (e > 0) {
            if ((e & 1) == 1) {
                result = matrixMul(result, base, modulus);
            }
            base = matrixMul(base, base, modulus);
            e >>= 1;
        }
        return result[1];
    }

    private static long[] matrixMul(long[] p, long[] q, long m) {
        return new long[] {
            addMod(mulMod(p[0], q[0], m), mulMod(p[1], q[2], m), m),
            addMod(mulMod(p[0], q[1], m), mulMod(p[1], q[3], m), m),
            addMod(mulMod(p[2], q[0], m), mulMod(p[3], q[2], m), m),
            addMod(mulMod(p[2], q[1], m), mulMod(p[3], q[3], m), m)
        };
    }

    /** {@code (a + b) mod m} for {@code a, b} in {@code [0, m)}, safe when the sum exceeds 2^63. */
    private static long addMod(long a, long b, long m) {
        long sum = a + b; // true sum < 2m < 2^64; a negative wrapped value still means "at least 2^63"
        return (sum < 0 || sum >= m) ? sum - m : sum;
    }

    /**
     * {@code (a * b) mod m} for {@code a, b} in {@code [0, m)} and {@code 1 <= m <= Long.MAX_VALUE}.
     * When the exact product fits in 63 bits (checked with {@link Math#multiplyHigh}) it is a
     * single remainder; otherwise a double-and-add loop on {@code b} keeps every intermediate below
     * {@code m}, so nothing overflows. At most 63 iterations.
     */
    private static long mulMod(long a, long b, long m) {
        long low = a * b;
        if (Math.multiplyHigh(a, b) == 0 && low >= 0) {
            return low % m;
        }
        long result = 0;
        long addend = a;
        long bits = b;
        while (bits > 0) {
            if ((bits & 1) == 1) {
                result = addMod(result, addend, m);
            }
            addend = addMod(addend, addend, m);
            bits >>= 1;
        }
        return result;
    }
}
