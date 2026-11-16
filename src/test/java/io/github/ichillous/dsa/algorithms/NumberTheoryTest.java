package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.Random;
import org.junit.jupiter.api.Test;

class NumberTheoryTest {
    private static final long SEED = 0x6CDL;
    private static final String SEED_TEXT = "seed=0x6CD ";
    private static final long[] EXTREMES = {
        0, 1, -1, 2, -2, 6, 12, Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE + 1,
        Integer.MAX_VALUE, Integer.MIN_VALUE, 1L << 62, (1L << 62) + 1, 3037000499L
    };

    private static BigInteger big(long v) {
        return BigInteger.valueOf(v);
    }

    private static long randomValue(Random random) {
        return switch (random.nextInt(4)) {
            case 0 -> random.nextInt(50) - 25;
            case 1 -> random.nextInt();
            case 2 -> random.nextLong();
            default -> random.nextLong() >> random.nextInt(64);
        };
    }

    private static List<long[]> pairs(Random random) {
        List<long[]> pairs = new ArrayList<>();
        for (long a : EXTREMES) {
            for (long b : EXTREMES) {
                pairs.add(new long[] {a, b});
            }
        }
        for (int i = 0; i < 5_000; i++) {
            pairs.add(new long[] {randomValue(random), randomValue(random)});
        }
        return pairs;
    }

    @Test
    void gcdLcmMatchBigInteger() {
        for (long[] pair : pairs(new Random(SEED))) {
            BigInteger expectedGcd = big(pair[0]).gcd(big(pair[1]));
            String message = SEED_TEXT + pair[0] + "," + pair[1];
            if (expectedGcd.bitLength() > 63) {
                assertThrows(ArithmeticException.class, () -> NumberTheory.gcd(pair[0], pair[1]), message);
            } else {
                assertEquals(expectedGcd.longValueExact(), NumberTheory.gcd(pair[0], pair[1]), message);
            }
            if (pair[0] == 0 || pair[1] == 0) {
                assertEquals(0, NumberTheory.lcm(pair[0], pair[1]), message);
                continue;
            }
            BigInteger expectedLcm = big(pair[0]).multiply(big(pair[1])).abs().divide(expectedGcd);
            if (expectedLcm.bitLength() > 63) {
                assertThrows(ArithmeticException.class, () -> NumberTheory.lcm(pair[0], pair[1]), message);
            } else {
                assertEquals(expectedLcm.longValueExact(), NumberTheory.lcm(pair[0], pair[1]), message);
            }
        }
    }

    @Test
    void gcdLcmPinnedCases() {
        assertEquals(0, NumberTheory.gcd(0, 0));
        assertEquals(6, NumberTheory.gcd(-12, 18));
        assertEquals(2, NumberTheory.gcd(Long.MIN_VALUE, 2));
        assertEquals(1, NumberTheory.gcd(Long.MIN_VALUE, 3));
        assertEquals(Long.MAX_VALUE, NumberTheory.gcd(Long.MAX_VALUE, 0));
        assertThrows(ArithmeticException.class, () -> NumberTheory.gcd(Long.MIN_VALUE, 0));
        assertThrows(ArithmeticException.class, () -> NumberTheory.gcd(0, Long.MIN_VALUE));
        assertThrows(ArithmeticException.class, () -> NumberTheory.gcd(Long.MIN_VALUE, Long.MIN_VALUE));
        assertEquals(0, NumberTheory.lcm(0, 5));
        assertEquals(0, NumberTheory.lcm(5, 0));
        assertEquals(0, NumberTheory.lcm(0, 0));
        assertEquals(12, NumberTheory.lcm(-4, 6));
        assertThrows(ArithmeticException.class, () -> NumberTheory.lcm(Long.MAX_VALUE, Long.MAX_VALUE - 1));
        assertThrows(ArithmeticException.class, () -> NumberTheory.lcm(Long.MIN_VALUE, 3));
    }

    @Test
    void extendedGcdSatisfiesIdentity() {
        for (long[] pair : pairs(new Random(SEED + 1))) {
            String message = SEED_TEXT + pair[0] + "," + pair[1];
            BigInteger expectedGcd = big(pair[0]).gcd(big(pair[1]));
            NumberTheory.Bezout bezout;
            try {
                bezout = NumberTheory.extendedGcd(pair[0], pair[1]);
            } catch (ArithmeticException allowedOverflow) {
                // Throwing is allowed (gcd 2^63 or an unrepresentable coefficient); wrapping is not.
                continue;
            }
            assertEquals(expectedGcd, big(bezout.gcd()), message);
            BigInteger combination = big(pair[0]).multiply(big(bezout.x()))
                    .add(big(pair[1]).multiply(big(bezout.y())));
            assertEquals(expectedGcd, combination, message);
        }
        // The escape hatch stays narrow: inputs without MIN_VALUE never throw.
        Random random = new Random(SEED + 11);
        for (int i = 0; i < 5_000; i++) {
            long a = randomValue(random);
            long b = randomValue(random);
            if (a == Long.MIN_VALUE || b == Long.MIN_VALUE) {
                continue;
            }
            NumberTheory.Bezout bezout = NumberTheory.extendedGcd(a, b);
            assertEquals(big(a).gcd(big(b)), big(bezout.gcd()), SEED_TEXT + a + "," + b);
            assertEquals(big(bezout.gcd()),
                    big(a).multiply(big(bezout.x())).add(big(b).multiply(big(bezout.y()))), SEED_TEXT + a + "," + b);
        }
    }

    @Test
    void extendedGcdPinnedCases() {
        assertEquals(new NumberTheory.Bezout(0, 1, 0), NumberTheory.extendedGcd(0, 0));
        NumberTheory.Bezout b = NumberTheory.extendedGcd(240, 46);
        assertEquals(2, b.gcd());
        assertEquals(2, 240 * b.x() + 46 * b.y());
        NumberTheory.Bezout minusOne = NumberTheory.extendedGcd(Long.MIN_VALUE, -1);
        assertEquals(1, minusOne.gcd());
        assertEquals(BigInteger.ONE,
                big(Long.MIN_VALUE).multiply(big(minusOne.x())).add(big(-1).multiply(big(minusOne.y()))));
        assertThrows(ArithmeticException.class, () -> NumberTheory.extendedGcd(Long.MIN_VALUE, 0));
        assertThrows(IllegalArgumentException.class, () -> new NumberTheory.Bezout(-1, 0, 0));
    }

    @Test
    void modPowMatchesBigInteger() {
        Random random = new Random(SEED + 2);
        long[] moduli = {1, 2, 3, 97, Integer.MAX_VALUE, 3037000500L, 1L << 62, (1L << 62) + 1,
            Long.MAX_VALUE, Long.MAX_VALUE - 1, (1L << 61) - 1};
        for (int i = 0; i < 3_000; i++) {
            long modulus = i < moduli.length * 100 ? moduli[i % moduli.length]
                    : Math.max(1, Math.abs(randomValue(random) % Long.MAX_VALUE));
            long base = randomValue(random);
            long exponent = random.nextBoolean() ? random.nextInt(1000) : random.nextLong() >>> 1;
            BigInteger expected = big(base).mod(big(modulus)).modPow(big(exponent), big(modulus));
            assertEquals(expected.longValueExact(), NumberTheory.modPow(base, exponent, modulus),
                    SEED_TEXT + base + "^" + exponent + " mod " + modulus);
        }
    }

    @Test
    void modPowPinnedCasesAndPreconditions() {
        assertEquals(0, NumberTheory.modPow(5, 0, 1));
        assertEquals(1, NumberTheory.modPow(5, 0, 7));
        assertEquals(1, NumberTheory.modPow(0, 0, 7));
        assertEquals(4, NumberTheory.modPow(-3, 2, 5));
        assertEquals(Long.MAX_VALUE - 1, NumberTheory.modPow(Long.MAX_VALUE - 1, 1, Long.MAX_VALUE));
        assertEquals(1, NumberTheory.modPow(Long.MAX_VALUE - 1, 2, Long.MAX_VALUE));
        assertEquals(big(Long.MAX_VALUE - 1).modPow(big(Long.MAX_VALUE), big(Long.MAX_VALUE)).longValueExact(),
                NumberTheory.modPow(Long.MAX_VALUE - 1, Long.MAX_VALUE, Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> NumberTheory.modPow(2, 3, 0));
        assertThrows(IllegalArgumentException.class, () -> NumberTheory.modPow(2, 3, -5));
        assertThrows(IllegalArgumentException.class, () -> NumberTheory.modPow(2, -1, 5));
    }

    @Test
    void modInverseMatchesBigInteger() {
        Random random = new Random(SEED + 3);
        for (int i = 0; i < 4_000; i++) {
            long modulus = i % 3 == 0 ? random.nextInt(100) + 1
                    : Math.max(1, Math.abs(randomValue(random) % Long.MAX_VALUE));
            long a = randomValue(random);
            String message = SEED_TEXT + a + " mod " + modulus;
            OptionalLong actual = NumberTheory.modInverse(a, modulus);
            boolean invertible = big(a).mod(big(modulus)).gcd(big(modulus)).equals(BigInteger.ONE);
            assertEquals(invertible, actual.isPresent(), message);
            if (invertible) {
                assertEquals(big(a).modInverse(big(modulus)).longValueExact(), actual.getAsLong(), message);
            }
        }
    }

    @Test
    void modInversePinnedCases() {
        assertEquals(OptionalLong.of(5), NumberTheory.modInverse(3, 7));
        assertEquals(OptionalLong.empty(), NumberTheory.modInverse(4, 8));
        assertEquals(OptionalLong.empty(), NumberTheory.modInverse(0, 7));
        assertEquals(OptionalLong.of(0), NumberTheory.modInverse(12345, 1));
        assertEquals(OptionalLong.of(5), NumberTheory.modInverse(-4, 7));
        assertEquals(OptionalLong.of(Long.MAX_VALUE - 1),
                NumberTheory.modInverse(Long.MAX_VALUE - 1, Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> NumberTheory.modInverse(3, 0));
    }

    @Test
    void sieveMatchesTrialDivision() {
        List<Integer> expected = new ArrayList<>();
        for (int n = 2; n <= 10_000; n++) {
            boolean prime = true;
            for (int d = 2; d * d <= n; d++) {
                if (n % d == 0) {
                    prime = false;
                    break;
                }
            }
            if (prime) {
                expected.add(n);
            }
        }
        assertEquals(expected, NumberTheory.primesUpTo(10_000));
        assertEquals(1229, NumberTheory.primesUpTo(10_000).size());
        assertEquals(List.of(), NumberTheory.primesUpTo(0));
        assertEquals(List.of(), NumberTheory.primesUpTo(1));
        assertEquals(List.of(2), NumberTheory.primesUpTo(2));
        assertEquals(List.of(2, 3), NumberTheory.primesUpTo(3));
        assertEquals(List.of(2, 3, 5, 7), NumberTheory.primesUpTo(10));
        assertEquals(List.of(2, 3, 5, 7), NumberTheory.primesUpTo(9));
        assertThrows(UnsupportedOperationException.class, () -> NumberTheory.primesUpTo(10).add(11));
    }

    @Test
    void sieveLimitsAndLargeRun() {
        assertThrows(IllegalArgumentException.class, () -> NumberTheory.primesUpTo(-1));
        assertThrows(IllegalArgumentException.class,
                () -> NumberTheory.primesUpTo(NumberTheory.MAX_SIEVE_LIMIT + 1));
        List<Integer> primes = NumberTheory.primesUpTo(1_000_000);
        assertEquals(78_498, primes.size());
        assertEquals(999_983, primes.get(primes.size() - 1));
    }

    @Test
    void isPrimeMatchesBigInteger() {
        for (long n : new long[] {Long.MIN_VALUE, -7, -1, 0, 1}) {
            assertFalse(NumberTheory.isPrime(n), "n=" + n);
        }
        for (long n : new long[] {2, 3, 5, 37, 41, 43, 1_000_000_007L, (1L << 61) - 1}) {
            assertTrue(NumberTheory.isPrime(n), "n=" + n);
        }
        // Carmichael numbers, strong pseudoprimes to small bases, and large composites.
        for (long n : new long[] {561, 41041, 3_215_031_751L, 25_326_001L, 4_759_123_141L,
            1_122_004_669_633L, 3_825_123_056_546_413_051L, Long.MAX_VALUE, 1L << 62, 37L * 37}) {
            assertFalse(NumberTheory.isPrime(n), "n=" + n);
        }
        for (long n = -5; n <= 20_000; n++) {
            assertEquals(n >= 2 && big(n).isProbablePrime(50), NumberTheory.isPrime(n), "n=" + n);
        }
        Random random = new Random(SEED + 4);
        for (int i = 0; i < 20_000; i++) {
            long n = switch (i % 3) {
                case 0 -> random.nextLong();
                case 1 -> random.nextLong() >>> (1 + random.nextInt(40));
                default -> Long.MAX_VALUE - random.nextInt(100_000);
            };
            assertEquals(n >= 2 && big(n).isProbablePrime(50), NumberTheory.isPrime(n), SEED_TEXT + "n=" + n);
        }
    }

    @Test
    void fibonacciModMatchesIteration() {
        BigInteger a = BigInteger.ZERO;
        BigInteger b = BigInteger.ONE;
        long[] moduli = {1, 2, 10, 1_000_000_007L, (1L << 61) - 1, Long.MAX_VALUE, (1L << 62) + 1};
        for (int n = 0; n <= 300; n++) {
            for (long m : moduli) {
                assertEquals(a.mod(big(m)).longValueExact(), NumberTheory.fibonacciMod(n, m),
                        "n=" + n + " m=" + m);
            }
            BigInteger next = a.add(b);
            a = b;
            b = next;
        }
        BigInteger f90 = BigInteger.ZERO;
        BigInteger g = BigInteger.ONE;
        for (int i = 0; i < 90; i++) {
            BigInteger next = f90.add(g);
            f90 = g;
            g = next;
        }
        assertEquals(2_880_067_194_370_816_120L, f90.longValueExact());
        assertEquals(f90.mod(big((1L << 61) - 1)).longValueExact(), NumberTheory.fibonacciMod(90, (1L << 61) - 1));
    }

    @Test
    void fibonacciModLargeIndexAndPreconditions() {
        // The Pisano period of 10 is 60, so F(n) mod 10 depends only on n mod 60.
        long n = 1_000_000_000_000_000_000L;
        assertEquals(NumberTheory.fibonacciMod(n % 60, 10), NumberTheory.fibonacciMod(n, 10));
        assertEquals(0, NumberTheory.fibonacciMod(Long.MAX_VALUE, 1));
        assertEquals(0, NumberTheory.fibonacciMod(0, 5));
        assertEquals(1, NumberTheory.fibonacciMod(1, 5));
        assertThrows(IllegalArgumentException.class, () -> NumberTheory.fibonacciMod(-1, 5));
        assertThrows(IllegalArgumentException.class, () -> NumberTheory.fibonacciMod(5, 0));
    }
}
