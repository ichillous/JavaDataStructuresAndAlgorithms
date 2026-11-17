package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.algorithms.AliasTable;
import io.github.ichillous.dsa.algorithms.NumberTheory;
import io.github.ichillous.dsa.algorithms.Sampling;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks every value the math and bit-manipulation figures display against the library and the JDK. */
class VisualExamplesMathTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-math.properties");

    // euclid-steps

    @Test
    void euclidRemaindersAndQuotientsMatchTheDivisionSteps() {
        long a = DATA.number("math.euclid.a");
        long b = DATA.number("math.euclid.b");
        List<Long> remainders = new ArrayList<>(List.of(a, b));
        List<Long> quotients = new ArrayList<>();
        long x = a;
        long y = b;
        while (y != 0) {
            quotients.add(x / y);
            long next = x % y;
            remainders.add(next);
            x = y;
            y = next;
        }
        assertEquals(DATA.list("math.euclid.remainders").stream().map(Integer::longValue).toList(), remainders);
        assertEquals(DATA.list("math.euclid.quotients").stream().map(Integer::longValue).toList(), quotients);
        assertEquals(DATA.number("math.euclid.gcd"), NumberTheory.gcd(a, b));
    }

    @Test
    void euclidBackSubstitutionEndsAtTheCoefficientsOfExtendedGcd() {
        int[] left = DATA.ints("math.euclid.back.left");
        int[] right = DATA.ints("math.euclid.back.right");
        int[] leftCoef = DATA.ints("math.euclid.back.leftCoef");
        int[] rightCoef = DATA.ints("math.euclid.back.rightCoef");
        int gcd = DATA.number("math.euclid.gcd");
        for (int row = 0; row < left.length; row++) {
            assertEquals(gcd, leftCoef[row] * left[row] + rightCoef[row] * right[row], "row " + row);
        }
        int last = left.length - 1;
        assertEquals(DATA.number("math.euclid.a"), left[last]);
        assertEquals(DATA.number("math.euclid.b"), right[last]);
        NumberTheory.Bezout bezout =
                NumberTheory.extendedGcd(DATA.number("math.euclid.a"), DATA.number("math.euclid.b"));
        assertEquals(gcd, bezout.gcd());
        assertEquals(DATA.number("math.euclid.x"), bezout.x());
        assertEquals(DATA.number("math.euclid.y"), bezout.y());
        assertEquals(bezout.x(), leftCoef[last]);
        assertEquals(bezout.y(), rightCoef[last]);
    }

    // modpow-squares

    @Test
    void modPowFollowsTheBitsOfTheExponent() {
        long base = DATA.number("math.modpow.base");
        long exponent = DATA.number("math.modpow.exponent");
        long modulus = DATA.number("math.modpow.modulus");
        List<Integer> bits = new ArrayList<>();
        List<Integer> squares = new ArrayList<>();
        List<Integer> results = new ArrayList<>();
        long result = 1;
        long square = base % modulus;
        for (long e = exponent; e > 0; e >>= 1) {
            bits.add((int) (e & 1));
            squares.add((int) square);
            if ((e & 1) == 1) {
                result = result * square % modulus;
            }
            results.add((int) result);
            square = square * square % modulus;
        }
        assertEquals(DATA.list("math.modpow.bits"), bits);
        assertEquals(DATA.list("math.modpow.squares"), squares);
        assertEquals(DATA.list("math.modpow.results"), results);
        assertEquals(DATA.number("math.modpow.answer"), NumberTheory.modPow(base, exponent, modulus));
        assertEquals(DATA.number("math.modpow.answer"), results.get(results.size() - 1));
        assertEquals("1101", Long.toBinaryString(exponent));
        long product = 1;
        for (int i = 0; i < bits.size(); i++) {
            if (bits.get(i) == 1) {
                product *= squares.get(i);
            }
        }
        assertEquals(DATA.number("math.modpow.product"), product);
        assertEquals(DATA.number("math.modpow.answer"), product % modulus);
    }

    // sieve-grid

    @Test
    void sieveGridShowsWhoCrossesOutEachNumberAndWhereEachPrimeStarts() {
        int limit = DATA.number("math.sieve.limit");
        List<Integer> primes = NumberTheory.primesUpTo(limit);
        assertEquals(DATA.list("math.sieve.primes"), primes);

        int[] crossedBy = DATA.ints("math.sieve.crossedBy");
        assertEquals(limit, crossedBy.length);
        for (int n = 1; n <= limit; n++) {
            int smallestFactor = 0;
            for (int d = 2; d < n && smallestFactor == 0; d++) {
                if (n % d == 0) {
                    smallestFactor = d;
                }
            }
            assertEquals(smallestFactor, crossedBy[n - 1], "number " + n);
            assertEquals(smallestFactor == 0 && n > 1, primes.contains(n), "prime " + n);
        }

        int[] starts = DATA.ints("math.sieve.starts");
        int[] newMarks = DATA.ints("math.sieve.newMarks");
        int[] sievingPrimes = {2, 3, 5};
        for (int i = 0; i < sievingPrimes.length; i++) {
            int p = sievingPrimes[i];
            assertEquals(p * p, starts[i]);
            int fresh = 0;
            for (int n = 1; n <= limit; n++) {
                if (crossedBy[n - 1] == p) {
                    fresh++;
                }
            }
            assertEquals(newMarks[i], fresh, "new marks of " + p);
        }
        assertTrue(7 * 7 > limit, "7 squared is past the limit, so 7 never sieves");

        // Multiples below p squared were already crossed out by a smaller prime.
        for (int p : new int[] {3, 5}) {
            List<Integer> expected = new ArrayList<>();
            for (int m = 2 * p; m < p * p; m += p) {
                expected.add(m);
                assertTrue(crossedBy[m - 1] != 0 && crossedBy[m - 1] < p, p + " skips " + m);
            }
            assertEquals(DATA.list("math.sieve.skipped." + p), expected);
        }
    }

    // reservoir-sampling

    @Test
    void reservoirRunMatchesTheLibraryForTheSeed() {
        List<String> items = DATA.strings("math.reservoir.items");
        int k = DATA.number("math.reservoir.k");
        long seed = DATA.number("math.reservoir.seed");
        int[] draws = DATA.ints("math.reservoir.draws");

        SplittableRandom random = new SplittableRandom(seed);
        List<String> slots = new ArrayList<>(items.subList(0, k));
        assertEquals(DATA.strings("math.reservoir.slots." + k), slots);
        for (int arrival = k + 1; arrival <= items.size(); arrival++) {
            int j = random.nextInt(arrival);
            assertEquals(draws[arrival - k - 1], j, "draw at arrival " + arrival);
            if (j < k) {
                slots.set(j, items.get(arrival - 1));
            }
            assertEquals(DATA.strings("math.reservoir.slots." + arrival), slots, "after arrival " + arrival);
        }
        assertEquals(slots, Sampling.reservoirSample(items, k, new SplittableRandom(seed)));
    }

    // alias-table

    @Test
    void aliasColumnsSumBackToTheNormalizedWeights() {
        double[] weights = DATA.strings("math.alias.weights").stream().mapToDouble(Double::parseDouble).toArray();
        double[] probability = fractions("math.alias.probability");
        double[] cutoff = fractions("math.alias.cutoff");
        int[] alias = DATA.ints("math.alias.alias");
        int n = weights.length;
        double sum = 0;
        for (double weight : weights) {
            sum += weight;
        }

        AliasTable table = new AliasTable(weights);
        assertEquals(n, table.size());
        for (int j = 0; j < n; j++) {
            assertEquals(weights[j] / sum, probability[j], 1e-12, "fixture probability " + j);
            assertEquals(probability[j], table.probability(j), 1e-12, "library probability " + j);
            // Column mass of index j: its own share plus what other columns give it as their alias.
            double mass = cutoff[j];
            for (int i = 0; i < n; i++) {
                if (i != j && alias[i] == j) {
                    mass += 1 - cutoff[i];
                }
            }
            assertEquals(probability[j], mass / n, 1e-12, "column mass " + j);
        }
        for (int i = 0; i < n; i++) {
            assertTrue(cutoff[i] > 0 && cutoff[i] <= 1, "cutoff " + i);
            assertTrue(cutoff[i] == 1 ? alias[i] == i : alias[i] != i, "alias of column " + i);
        }
    }

    private static double[] fractions(String key) {
        return DATA.strings(key).stream().mapToDouble(text -> {
            String[] parts = text.split("/");
            return parts.length == 1
                    ? Double.parseDouble(parts[0])
                    : Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
        }).toArray();
    }

    // bitmask-subsets

    @Test
    void maskOperationsAndTheSubsetWalkMatchTheJdk() {
        int width = DATA.number("math.bits.width");
        int mask = DATA.number("math.bits.mask");
        assertEquals("1010", Integer.toBinaryString(mask));
        assertEquals(DATA.number("math.bits.testResult"), (mask >> DATA.number("math.bits.testBit")) & 1);
        assertEquals(DATA.number("math.bits.setResult"), mask | (1 << DATA.number("math.bits.setBit")));
        assertEquals(DATA.number("math.bits.clearResult"), mask & ~(1 << DATA.number("math.bits.clearBit")));
        assertTrue(mask < (1 << width));

        List<Integer> subsets = new ArrayList<>();
        List<Integer> minusOne = new ArrayList<>();
        for (int sub = mask; ; sub = (sub - 1) & mask) {
            subsets.add(sub);
            if (sub == 0) {
                break;
            }
            minusOne.add(sub - 1);
        }
        assertEquals(DATA.list("math.bits.subsets"), subsets);
        assertEquals(DATA.list("math.bits.minusOne"), minusOne);
        assertEquals(1 << Integer.bitCount(mask), subsets.size());
        for (int sub : subsets) {
            assertEquals(sub, sub & mask, "every walked value is a subset of the mask");
        }
    }
}
