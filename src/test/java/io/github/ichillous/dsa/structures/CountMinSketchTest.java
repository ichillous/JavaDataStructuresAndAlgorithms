package io.github.ichillous.dsa.structures;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CountMinSketchTest {
    private static final long SEED = 0xC0C0L;

    private static String[] zipfStream(int length) {
        int ranks = 2000;
        double[] cumulative = new double[ranks];
        double sum = 0;
        for (int r = 0; r < ranks; r++) {
            sum += 1.0 / (r + 1);
            cumulative[r] = sum;
        }
        Random random = new Random(SEED);
        String[] stream = new String[length];
        for (int i = 0; i < length; i++) {
            int slot = Arrays.binarySearch(cumulative, random.nextDouble() * sum);
            int rank = slot >= 0 ? slot : -slot - 1;
            stream[i] = "item-" + rank;
        }
        return stream;
    }

    @Test
    void sketchSizingFormula() {
        CountMinSketch<String> sketch = CountMinSketch.withErrorBounds(0.01, 0.01);
        assertEquals(272, sketch.width());
        assertEquals(5, sketch.depth());
        CountMinSketch<String> loose = CountMinSketch.withErrorBounds(1e9, 0.999);
        assertEquals(1, loose.width());
        assertEquals(1, loose.depth());
    }

    @Test
    void sketchNeverUnderestimates() {
        CountMinSketch<String> sketch = CountMinSketch.withErrorBounds(0.01, 0.01);
        Map<String, Long> truth = new HashMap<>();
        for (String s : zipfStream(50_000)) {
            sketch.add(s);
            truth.merge(s, 1L, Long::sum);
        }
        assertEquals(50_000, sketch.totalCount());
        for (Map.Entry<String, Long> e : truth.entrySet()) {
            assertTrue(sketch.estimate(e.getKey()) >= e.getValue(),
                    "seed " + SEED + " underestimated " + e.getKey());
        }
    }

    @Test
    void sketchErrorBoundHoldsForMostItems() {
        double epsilon = 0.01;
        CountMinSketch<String> sketch = CountMinSketch.withErrorBounds(epsilon, 0.01);
        Map<String, Long> truth = new HashMap<>();
        for (String s : zipfStream(50_000)) {
            sketch.add(s);
            truth.merge(s, 1L, Long::sum);
        }
        int within = 0;
        for (Map.Entry<String, Long> e : truth.entrySet()) {
            if (sketch.estimate(e.getKey()) - e.getValue() <= epsilon * sketch.totalCount()) {
                within++;
            }
        }
        assertTrue(within >= 0.99 * truth.size(), "seed " + SEED + ": " + within + "/" + truth.size());
    }

    @Test
    void weightedAddsAndAbsentItems() {
        CountMinSketch<String> sketch = new CountMinSketch<>(1024, 4);
        assertEquals(0, sketch.estimate("never"));
        sketch.add("a", 5);
        sketch.add("a", 0);
        sketch.add("b");
        assertEquals(5, sketch.estimate("a"));
        assertEquals(1, sketch.estimate("b"));
        assertEquals(6, sketch.totalCount());
        CountMinSketch<Integer> narrow = new CountMinSketch<>(1, 1);
        narrow.add(1, 2);
        narrow.add(Integer.MIN_VALUE, 3);
        assertEquals(5, narrow.estimate(7), "width 1 merges every item");
    }

    @Test
    void sketchOverflowLeavesStateUnchanged() {
        CountMinSketch<String> sketch = new CountMinSketch<>(16, 3);
        sketch.add("x", Long.MAX_VALUE);
        assertThrows(ArithmeticException.class, () -> sketch.add("x", 1));
        assertEquals(Long.MAX_VALUE, sketch.estimate("x"));
        assertEquals(Long.MAX_VALUE, sketch.totalCount());
        assertThrows(ArithmeticException.class, () -> sketch.add("y", 1));
        assertEquals(0, sketch.estimate("y"));
    }

    @Test
    void sketchRejectsBadParameters() {
        assertThrows(IllegalArgumentException.class, () -> new CountMinSketch<String>(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new CountMinSketch<String>(1, 0));
        assertThrows(IllegalArgumentException.class, () -> CountMinSketch.withErrorBounds(0.0, 0.5));
        assertThrows(IllegalArgumentException.class, () -> CountMinSketch.withErrorBounds(Double.NaN, 0.5));
        assertThrows(IllegalArgumentException.class, () -> CountMinSketch.withErrorBounds(1e-12, 0.5));
        assertThrows(IllegalArgumentException.class, () -> CountMinSketch.withErrorBounds(0.1, 0.0));
        assertThrows(IllegalArgumentException.class, () -> CountMinSketch.withErrorBounds(0.1, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new CountMinSketch<String>(1 << 20, 64));
        assertThrows(IllegalArgumentException.class, () -> CountMinSketch.withErrorBounds(0.1, Double.MIN_VALUE));
        assertThrows(IllegalArgumentException.class, () -> CountMinSketch.withErrorBounds(1e-9, 0.5));
        CountMinSketch<String> sketch = new CountMinSketch<>(4, 2);
        assertThrows(IllegalArgumentException.class, () -> sketch.add("x", -1));
        assertThrows(NullPointerException.class, () -> sketch.add(null));
        assertThrows(NullPointerException.class, () -> sketch.add(null, 1));
        assertThrows(NullPointerException.class, () -> sketch.estimate(null));
        assertEquals(0, sketch.totalCount());
    }
}
