package io.github.ichillous.dsa.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class LfuCacheTest {
    @Test
    void evictsLeastFrequentThenLeastRecent() {
        LfuCache<Integer, String> cache = new LfuCache<>(2);
        assertNull(cache.put(1, "a"));
        assertNull(cache.put(2, "b"));
        assertEquals("a", cache.get(1));
        assertNull(cache.put(3, "c"));
        assertNull(cache.get(2));
        assertEquals(0, cache.frequency(2));
        assertEquals("c", cache.get(3));
        assertEquals(2, cache.frequency(1));
        assertEquals(2, cache.frequency(3));
        assertNull(cache.put(4, "d"));
        assertNull(cache.get(1));
        assertEquals("c", cache.get(3));
        assertEquals("d", cache.get(4));
        assertEquals(2, cache.size());
    }

    @Test
    void matchesBruteForceModel() {
        Random random = new Random(0x1F0CL);
        for (int capacity = 1; capacity <= 5; capacity++) {
            LfuCache<Integer, Integer> cache = new LfuCache<>(capacity);
            Model model = new Model(capacity);
            for (int op = 0; op < 4_000; op++) {
                int key = random.nextInt(12);
                String context = "seed=0x1F0C capacity=" + capacity + " op=" + op + " key=" + key;
                switch (random.nextInt(8)) {
                    case 0, 1, 2 -> {
                        int value = random.nextInt();
                        assertEquals(model.put(key, value), cache.put(key, value), context);
                    }
                    case 3, 4 -> assertEquals(model.get(key), cache.get(key), context);
                    case 5 -> assertEquals(model.remove(key), cache.remove(key), context);
                    case 6 -> assertEquals(model.frequency(key), cache.frequency(key), context);
                    default -> {
                        if (random.nextInt(20) == 0) {
                            model.clear();
                            cache.clear();
                        }
                    }
                }
                assertEquals(model.entries.size(), cache.size(), context);
                assertEquals(model.entries.isEmpty(), cache.isEmpty(), context);
                for (int k = 0; k < 12; k++) {
                    assertEquals(model.frequency(k), cache.frequency(k), context + " probe=" + k);
                }
            }
        }
    }

    @Test
    void replacementCountsAsUseAndReturnsOld() {
        LfuCache<String, Integer> cache = new LfuCache<>(2);
        assertNull(cache.put("a", 1));
        assertEquals(1, cache.frequency("a"));
        assertEquals(1, cache.put("a", 2));
        assertEquals(2, cache.frequency("a"));
        assertNull(cache.put("b", 3));
        assertNull(cache.put("c", 4));
        assertNull(cache.get("b"));
        assertEquals(2, cache.get("a"));
        assertEquals(3, cache.frequency("a"));
        assertEquals(3, cache.frequency("a"));
        assertEquals(0, cache.frequency("missing"));
    }

    @Test
    void capacityOneAndClear() {
        LfuCache<Integer, Integer> cache = new LfuCache<>(1);
        assertTrue(cache.isEmpty());
        assertEquals(1, cache.capacity());
        assertNull(cache.get(0));
        assertNull(cache.remove(0));
        for (int key = 0; key < 50; key++) {
            assertNull(cache.put(key, key));
            assertEquals(key, cache.put(key, -key));
            assertEquals(-key, cache.get(key));
            assertEquals(3, cache.frequency(key));
            assertEquals(1, cache.size());
        }
        assertEquals(-49, cache.remove(49));
        assertTrue(cache.isEmpty());
        cache.put(7, 7);
        cache.clear();
        cache.clear();
        assertTrue(cache.isEmpty());
        assertFalse(cache.size() > 0);
        assertEquals(0, cache.frequency(7));
        assertNull(cache.put(8, 8));
        assertEquals(1, cache.frequency(8));
    }

    @Test
    void frequencySaturatesAtMaxValueAndStillRefreshesRecency() {
        LfuCache<Integer, Integer> cache = new LfuCache<>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.seedFrequency(1, Integer.MAX_VALUE - 1);
        cache.seedFrequency(2, Integer.MAX_VALUE - 1);
        assertEquals(1, cache.get(1));
        assertEquals(Integer.MAX_VALUE, cache.frequency(1));
        assertEquals(1, cache.get(1));
        assertEquals(Integer.MAX_VALUE, cache.frequency(1));
        assertEquals(1, cache.put(1, 10));
        assertEquals(Integer.MAX_VALUE, cache.frequency(1));
        assertEquals(2, cache.get(2));
        assertEquals(2, cache.get(2));
        assertEquals(Integer.MAX_VALUE, cache.frequency(2));
        // Lower-frequency key 3 is the victim even though 1 and 2 are saturated.
        assertNull(cache.put(4, 4));
        assertNull(cache.get(3));
        // Top bucket order is now 1, 2 (least to most recent); 4 joins it at saturation.
        cache.seedFrequency(4, Integer.MAX_VALUE);
        assertEquals(10, cache.get(1)); // A saturated hit refreshes recency: order 2, 4, 1.
        assertEquals(Integer.MAX_VALUE, cache.frequency(1));
        assertNull(cache.put(5, 5));
        assertEquals(0, cache.frequency(2));
        assertEquals(Integer.MAX_VALUE, cache.frequency(1));
        assertEquals(Integer.MAX_VALUE, cache.frequency(4));
        assertEquals(1, cache.frequency(5));
    }

    @Test
    void removalKeepsMinimumFrequencyCorrect() {
        LfuCache<Integer, Integer> cache = new LfuCache<>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.get(2);
        cache.get(3);
        cache.get(3);
        assertEquals(1, cache.remove(1));
        cache.put(4, 4);
        cache.put(5, 5);
        assertNull(cache.get(4));
        assertEquals(2, cache.get(2));
        assertEquals(3, cache.get(3));
        assertEquals(5, cache.get(5));
    }

    @Test
    void rejectsNullsAndNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new LfuCache<>(0));
        assertThrows(IllegalArgumentException.class, () -> new LfuCache<>(-1));
        LfuCache<String, Integer> cache = new LfuCache<>(2);
        cache.put("a", 1);
        assertThrows(NullPointerException.class, () -> cache.get(null));
        assertThrows(NullPointerException.class, () -> cache.remove(null));
        assertThrows(NullPointerException.class, () -> cache.frequency(null));
        assertThrows(NullPointerException.class, () -> cache.put(null, 1));
        assertThrows(NullPointerException.class, () -> cache.put("b", null));
        assertThrows(NullPointerException.class, () -> cache.put("a", null));
        assertEquals(1, cache.size());
        assertEquals(1, cache.frequency("a"));
    }

    /** Brute-force reference: evict the entry with the smallest (frequency, last-use tick). */
    private static final class Model {
        private final int capacity;
        private final List<long[]> entries = new ArrayList<>(); // key, value, frequency, tick
        private long tick;

        Model(int capacity) {
            this.capacity = capacity;
        }

        private long[] find(int key) {
            for (long[] entry : entries) {
                if (entry[0] == key) {
                    return entry;
                }
            }
            return null;
        }

        Integer get(int key) {
            long[] entry = find(key);
            if (entry == null) {
                return null;
            }
            entry[2]++;
            entry[3] = ++tick;
            return (int) entry[1];
        }

        Integer put(int key, int value) {
            long[] entry = find(key);
            if (entry != null) {
                int previous = (int) entry[1];
                entry[1] = value;
                entry[2]++;
                entry[3] = ++tick;
                return previous;
            }
            if (entries.size() == capacity) {
                long[] victim = entries.get(0);
                for (long[] candidate : entries) {
                    if (candidate[2] < victim[2]
                            || candidate[2] == victim[2] && candidate[3] < victim[3]) {
                        victim = candidate;
                    }
                }
                entries.remove(victim);
            }
            entries.add(new long[] {key, value, 1, ++tick});
            return null;
        }

        Integer remove(int key) {
            long[] entry = find(key);
            if (entry == null) {
                return null;
            }
            entries.remove(entry);
            return (int) entry[1];
        }

        int frequency(int key) {
            long[] entry = find(key);
            return entry == null ? 0 : (int) entry[2];
        }

        void clear() {
            entries.clear();
        }
    }
}
