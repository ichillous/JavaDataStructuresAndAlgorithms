package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

class SamplingTest {
    private static final long SEED = 0x5A3L;
    private static final int TRIALS = 60_000;

    private static String seed() {
        return "seed=0x" + Long.toHexString(SEED);
    }

    @Test
    void reservoirIsUniform() {
        List<Integer> stream = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        SplittableRandom random = new SplittableRandom(SEED);
        int[] hits = new int[10];
        for (int t = 0; t < TRIALS; t++) {
            List<Integer> sample = Sampling.reservoirSample(stream, 3, random);
            assertEquals(3, sample.size());
            assertEquals(3, new HashSet<>(sample).size(), "distinct items, " + seed());
            for (int v : sample) {
                hits[v]++;
            }
        }
        for (int v = 0; v < 10; v++) {
            assertEquals(0.3, hits[v] / (double) TRIALS, 0.01, "item " + v + ", " + seed());
        }
    }

    @Test
    void reservoirSmallStreamsAndValidation() {
        SplittableRandom random = new SplittableRandom(SEED);
        List<String> items = new ArrayList<>(List.of("a", "b", "c"));
        assertEquals(List.of("a", "b", "c"), Sampling.reservoirSample(items, 3, random));
        assertEquals(List.of("a", "b", "c"), Sampling.reservoirSample(items, 10, random));
        assertEquals(List.of(), Sampling.reservoirSample(items, 0, random));
        assertEquals(List.of(), Sampling.reservoirSample(List.<String>of(), 5, random));
        assertEquals(List.of("a", "a"), Sampling.reservoirSample(List.of("a", "a"), 2, random));
        assertEquals(List.of("a", "b", "c"), items);

        // k = 0 does not read the stream at all.
        Iterable<String> exploding = () -> {
            throw new AssertionError("must not iterate");
        };
        assertEquals(List.of(), Sampling.reservoirSample(exploding, 0, random));

        // Any sample is a sub-multiset of the stream (reference check on arbitrary sizes).
        List<Integer> stream = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            stream.add(i % 7);
        }
        for (int k = 0; k <= 60; k += 5) {
            List<Integer> sample = Sampling.reservoirSample(stream, k, random);
            assertEquals(Math.min(k, 50), sample.size(), seed());
            Map<Integer, Integer> have = new HashMap<>();
            Map<Integer, Integer> sampled = new HashMap<>();
            stream.forEach(v -> have.merge(v, 1, Integer::sum));
            sample.forEach(v -> sampled.merge(v, 1, Integer::sum));
            sampled.forEach((v, c) -> assertTrue(c <= have.get(v), "value " + v + ", " + seed()));
        }

        assertThrows(IllegalArgumentException.class, () -> Sampling.reservoirSample(items, -1, random));
        assertThrows(NullPointerException.class, () -> Sampling.reservoirSample(null, 1, random));
        assertThrows(NullPointerException.class, () -> Sampling.reservoirSample(items, 1, null));
        List<String> withNull = new ArrayList<>();
        withNull.add(null);
        assertThrows(NullPointerException.class, () -> Sampling.reservoirSample(withNull, 1, random));
        List<String> result = Sampling.reservoirSample(items, 2, random);
        assertThrows(UnsupportedOperationException.class, () -> result.add("x"));
    }

    @Test
    void reservoirIsReproducibleForEqualSeeds() {
        List<Integer> stream = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            stream.add(i);
        }
        assertEquals(
                Sampling.reservoirSample(stream, 7, new SplittableRandom(SEED)),
                Sampling.reservoirSample(stream, 7, new SplittableRandom(SEED)));
    }

    @Test
    void shuffleIsPermutationAndUniformOverThree() {
        SplittableRandom random = new SplittableRandom(SEED);
        Map<String, Integer> counts = new HashMap<>();
        for (int t = 0; t < TRIALS; t++) {
            int[] values = {1, 2, 3};
            Sampling.shuffle(values, random);
            counts.merge(Arrays.toString(values), 1, Integer::sum);
        }
        assertEquals(6, counts.size(), seed());
        counts.forEach((order, count) ->
                assertEquals(1.0 / 6, count / (double) TRIALS, 0.01, order + ", " + seed()));

        for (int length = 0; length <= 40; length++) {
            int[] values = new int[length];
            for (int i = 0; i < length; i++) {
                values[i] = (i * 7) % 11; // duplicates on purpose
            }
            int[] expected = values.clone();
            Arrays.sort(expected);
            Sampling.shuffle(values, random);
            int[] actual = values.clone();
            Arrays.sort(actual);
            assertArrayEquals(expected, actual, "length " + length + ", " + seed());
        }
    }

    @Test
    void shuffleEmptyAndSingletonDrawNothingAndNullsAreRejected() {
        SplittableRandom random = new SplittableRandom(SEED);
        int[] empty = {};
        int[] one = {42};
        Sampling.shuffle(empty, random);
        Sampling.shuffle(one, random);
        assertEquals(0, empty.length);
        assertArrayEquals(new int[] {42}, one);
        assertEquals(new SplittableRandom(SEED).nextLong(), random.nextLong(), "no draws consumed");
        assertThrows(NullPointerException.class, () -> Sampling.shuffle(null, random));
        assertThrows(NullPointerException.class, () -> Sampling.shuffle(new int[1], null));
    }

    @Test
    void shuffleReachesEveryOrderOfFour() {
        SplittableRandom random = new SplittableRandom(SEED);
        Set<String> seen = new LinkedHashSet<>();
        for (int t = 0; t < 5_000; t++) {
            int[] values = {1, 2, 3, 4};
            Sampling.shuffle(values, random);
            seen.add(Arrays.toString(values));
        }
        assertEquals(24, seen.size(), seed());
    }

    @Test
    void aliasTableReconstructsProbabilities() {
        double[][] cases = {
            {1, 0, 3},
            {5},
            {1, 1, 1, 1},
            {0, 0, 7, 0},
            {0.1, 0.2, 0.3, 0.4},
            {1e-300, 1, 1e300},
            {3, 1, 4, 1, 5, 9, 2, 6},
        };
        for (double[] weights : cases) {
            AliasTable table = new AliasTable(weights);
            double sum = 0;
            for (double w : weights) {
                sum += w;
            }
            assertEquals(weights.length, table.size());
            for (int i = 0; i < weights.length; i++) {
                double expected = weights[i] / sum;
                assertEquals(expected, table.probability(i), 1e-12, Arrays.toString(weights));
                assertEquals(expected, table.tableProbability(i), 1e-12, Arrays.toString(weights));
            }
        }

        SplittableRandom gen = new SplittableRandom(SEED);
        for (int round = 0; round < 300; round++) {
            int n = 1 + gen.nextInt(40);
            double[] weights = new double[n];
            double sum = 0;
            for (int i = 0; i < n; i++) {
                weights[i] = gen.nextInt(4) == 0 ? 0.0 : gen.nextDouble() * (1 << gen.nextInt(20));
                sum += weights[i];
            }
            if (sum == 0) {
                weights[0] = 1;
                sum = 1;
            }
            AliasTable table = new AliasTable(weights);
            for (int i = 0; i < n; i++) {
                assertEquals(weights[i] / sum, table.tableProbability(i), 1e-12, "round " + round + ", " + seed());
            }
        }

        SplittableRandom random = new SplittableRandom(SEED);
        AliasTable table = new AliasTable(new double[] {1, 0, 3});
        int[] hits = new int[3];
        for (int t = 0; t < TRIALS; t++) {
            hits[table.sample(random)]++;
        }
        assertEquals(0.25, hits[0] / (double) TRIALS, 0.01, seed());
        assertEquals(0, hits[1], seed());
        assertEquals(0.75, hits[2] / (double) TRIALS, 0.01, seed());
    }

    @Test
    void aliasTableSingletonAlwaysReturnsZero() {
        AliasTable table = new AliasTable(new double[] {2.5});
        SplittableRandom random = new SplittableRandom(SEED);
        for (int t = 0; t < 100; t++) {
            assertEquals(0, table.sample(random));
        }
        assertEquals(1.0, table.probability(0), 0.0);
    }

    @Test
    void aliasTableRejectsBadWeightsAndCopiesItsInput() {
        assertThrows(IllegalArgumentException.class, () -> new AliasTable(new double[] {1, Double.NaN}));
        assertThrows(IllegalArgumentException.class, () -> new AliasTable(new double[] {1, -0.5}));
        assertThrows(IllegalArgumentException.class, () -> new AliasTable(new double[] {1, Double.POSITIVE_INFINITY}));
        assertThrows(IllegalArgumentException.class, () -> new AliasTable(new double[] {0, 0, 0}));
        assertThrows(IllegalArgumentException.class, () -> new AliasTable(new double[] {Double.MAX_VALUE, Double.MAX_VALUE}));
        assertThrows(IllegalArgumentException.class, () -> new AliasTable(new double[0]));
        assertThrows(NullPointerException.class, () -> new AliasTable(null));

        double[] weights = {1, 0, 3};
        AliasTable table = new AliasTable(weights);
        weights[1] = 1000;
        weights[2] = 0;
        assertArrayEquals(new double[] {1, 1000, 0}, weights);
        SplittableRandom random = new SplittableRandom(SEED);
        for (int t = 0; t < 2_000; t++) {
            assertTrue(table.sample(random) != 1, "later edits must not leak in, " + seed());
        }
        assertEquals(0.25, table.probability(0), 1e-12);
        assertEquals(0.0, table.probability(1), 0.0);
        assertThrows(IndexOutOfBoundsException.class, () -> table.probability(3));
        assertThrows(IndexOutOfBoundsException.class, () -> table.probability(-1));
        assertThrows(NullPointerException.class, () -> table.sample(null));
    }
}
