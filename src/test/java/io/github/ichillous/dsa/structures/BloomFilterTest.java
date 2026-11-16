package io.github.ichillous.dsa.structures;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BloomFilterTest {
    private static final long SEED = 0xB100L;

    @Test
    void bloomSizingFormula() {
        BloomFilter<String> filter = BloomFilter.forExpectedInsertions(1000, 0.01);
        assertEquals(9586, filter.bitCount());
        assertEquals(7, filter.hashCount());
        BloomFilter<String> tiny = BloomFilter.forExpectedInsertions(1, 0.999);
        assertEquals(1, tiny.hashCount());
        assertTrue(tiny.bitCount() >= 1);
    }

    @Test
    void bloomHasNoFalseNegatives() {
        Random random = new Random(SEED);
        BloomFilter<String> filter = BloomFilter.forExpectedInsertions(10_000, 0.01);
        List<String> members = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            String s = Long.toString(random.nextLong(), 36);
            members.add(s);
            filter.add(s);
        }
        for (String s : members) {
            assertTrue(filter.mightContain(s), "seed " + SEED + " lost " + s);
        }
        assertEquals(10_000, filter.insertions());
    }

    @Test
    void bloomFalsePositiveRateNearTarget() {
        BloomFilter<String> filter = BloomFilter.forExpectedInsertions(10_000, 0.01);
        Set<String> reference = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            String s = "in-" + i;
            reference.add(s);
            filter.add(s);
        }
        int falsePositives = 0;
        for (int i = 0; i < 100_000; i++) {
            String probe = "out-" + i;
            assertFalse(reference.contains(probe));
            if (filter.mightContain(probe)) {
                falsePositives++;
            }
        }
        double observed = falsePositives / 100_000.0;
        assertTrue(observed <= 0.02, "observed " + observed + " (deterministic keys, no seed)");
        assertEquals(0.01, filter.estimatedFalsePositiveRate(), 0.002);
    }

    @Test
    void emptyFilterContainsNothingAndEstimatesZero() {
        BloomFilter<Integer> filter = new BloomFilter<>(64, 3);
        assertEquals(0, filter.insertions());
        assertEquals(0.0, filter.estimatedFalsePositiveRate());
        for (int i = 0; i < 100; i++) {
            assertFalse(filter.mightContain(i));
        }
    }

    @Test
    void duplicatesCountAsInsertionsAndExtremeHashesWork() {
        BloomFilter<Object> filter = new BloomFilter<>(1, 1);
        filter.add("x");
        filter.add("x");
        assertEquals(2, filter.insertions());
        assertTrue(filter.mightContain("x"));
        assertTrue(filter.mightContain("anything"), "a one-bit filter saturates");
        BloomFilter<Integer> ints = new BloomFilter<>(1000, 5);
        int[] values = {Integer.MIN_VALUE, Integer.MAX_VALUE, -1, 0, 1};
        for (int v : values) {
            ints.add(v);
        }
        for (int v : values) {
            assertTrue(ints.mightContain(v));
        }
    }

    @Test
    void equalHashCodesAreIndistinguishable() {
        BloomFilter<String> filter = new BloomFilter<>(4096, 4);
        filter.add("Aa");
        assertEquals("Aa".hashCode(), "BB".hashCode());
        assertTrue(filter.mightContain("BB"));
    }

    @Test
    void bloomRejectsBadParameters() {
        assertThrows(IllegalArgumentException.class, () -> new BloomFilter<String>(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new BloomFilter<String>(1, 0));
        assertThrows(IllegalArgumentException.class, () -> BloomFilter.forExpectedInsertions(0, 0.1));
        assertThrows(IllegalArgumentException.class, () -> BloomFilter.forExpectedInsertions(10, 0.0));
        assertThrows(IllegalArgumentException.class, () -> BloomFilter.forExpectedInsertions(10, 1.0));
        assertThrows(IllegalArgumentException.class, () -> BloomFilter.forExpectedInsertions(10, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> BloomFilter.forExpectedInsertions(Integer.MAX_VALUE, 1e-10));
        BloomFilter<String> filter = new BloomFilter<>(8, 2);
        assertThrows(NullPointerException.class, () -> filter.add(null));
        assertThrows(NullPointerException.class, () -> filter.mightContain(null));
    }
}
