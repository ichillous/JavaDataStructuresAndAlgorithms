package io.github.ichillous.dsa.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Random;
import org.junit.jupiter.api.Test;

class LruCacheTest {
    @Test
    void constructorRejectsNonpositiveCapacityAndStartsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new LruCache<>(0));
        assertThrows(IllegalArgumentException.class, () -> new LruCache<>(-1));
        LruCache<String, Integer> cache = new LruCache<>(3);
        assertEquals(3, cache.capacity());
        assertEquals(0, cache.size());
        assertTrue(cache.isEmpty());
        assertNull(cache.get("missing"));
        assertNull(cache.remove("missing"));
    }

    @Test
    void successfulLookupProtectsEntryFromNextEviction() {
        LruCache<String, Integer> cache = new LruCache<>(2);
        assertNull(cache.put("a", 1));
        assertNull(cache.put("b", 2));
        assertEquals(1, cache.get("a"));
        cache.put("c", 3);
        assertNull(cache.get("b"));
        assertEquals(1, cache.get("a"));
        assertEquals(3, cache.get("c"));
        assertEquals(2, cache.size());
        assertFalse(cache.isEmpty());
    }

    @Test
    void updatingExistingKeyReturnsOldValueAndRefreshesRecency() {
        LruCache<String, Integer> cache = new LruCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        assertEquals(1, cache.put("a", 10));
        assertEquals(2, cache.size());
        cache.put("c", 3);
        assertNull(cache.get("b"));
        assertEquals(10, cache.get("a"));
        assertEquals(10, cache.put("a", 10));
        assertEquals(2, cache.size());
    }

    @Test
    void repeatedMostRecentAccessAndAbsentOperationsPreserveOrder() {
        LruCache<Integer, String> cache = new LruCache<>(2);
        cache.put(1, "one");
        cache.put(2, "two");
        assertEquals("two", cache.get(2));
        assertEquals("two", cache.get(2));
        assertNull(cache.get(99));
        assertNull(cache.remove(99));
        cache.put(3, "three");
        assertNull(cache.get(1));
        assertEquals("two", cache.remove(2));
        assertEquals("three", cache.remove(3));
        assertTrue(cache.isEmpty());
    }

    @Test
    void removesMiddleLeastAndMostRecentEntriesAndSupportsReuse() {
        LruCache<Integer, Integer> cache = new LruCache<>(4);
        for (int key = 0; key < 4; key++) {
            cache.put(key, 10 + key);
        }
        assertEquals(11, cache.remove(1));
        assertEquals(10, cache.remove(0));
        assertEquals(13, cache.remove(3));
        assertEquals(12, cache.remove(2));
        assertTrue(cache.isEmpty());
        cache.put(5, 50);
        assertEquals(50, cache.get(5));
        cache.clear();
        cache.clear();
        assertTrue(cache.isEmpty());
        assertEquals(4, cache.capacity());
        cache.put(6, 60);
        assertEquals(60, cache.get(6));
        assertNull(cache.get(5));
    }

    @Test
    void capacityOneHandlesUpdatesEvictionAndRemoval() {
        LruCache<Integer, Integer> cache = new LruCache<>(1);
        for (int key = 0; key < 100; key++) {
            assertNull(cache.put(key, key));
            assertEquals(key, cache.put(key, -key));
            assertEquals(-key, cache.get(key));
            if (key > 0) {
                assertNull(cache.get(key - 1));
            }
            assertEquals(1, cache.size());
        }
        assertEquals(-99, cache.remove(99));
        assertTrue(cache.isEmpty());
    }

    @Test
    void nullInputsAreRejectedBeforeChangingValuesOrRecency() {
        LruCache<String, Integer> cache = new LruCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        assertThrows(NullPointerException.class, () -> cache.get(null));
        assertThrows(NullPointerException.class, () -> cache.remove(null));
        assertThrows(NullPointerException.class, () -> cache.put(null, 3));
        assertThrows(NullPointerException.class, () -> cache.put("a", null));
        cache.put("c", 3);
        assertNull(cache.get("a"));
        assertEquals(2, cache.get("b"));
    }

    @Test
    void collisionsAndEqualDistinctKeyObjectsKeepTheirOwnRecencyNodes() {
        LruCache<CollisionKey, Integer> cache = new LruCache<>(2);
        cache.put(new CollisionKey(1), 10);
        cache.put(new CollisionKey(2), 20);
        assertEquals(10, cache.put(new CollisionKey(1), 11));
        cache.put(new CollisionKey(3), 30);
        assertNull(cache.get(new CollisionKey(2)));
        assertEquals(11, cache.remove(new CollisionKey(1)));
        assertEquals(30, cache.get(new CollisionKey(3)));
        assertEquals(1, cache.size());
    }

    @Test
    void seededMixedOperationsMatchAccessOrderedLinkedHashMap() {
        Random random = new Random(0x1A0CA);
        for (int capacity : new int[] {1, 2, 7, 32}) {
            LruCache<Integer, Integer> cache = new LruCache<>(capacity);
            LinkedHashMap<Integer, Integer> expected = new LinkedHashMap<>(16, 0.75f, true);
            for (int operation = 0; operation < 12_000; operation++) {
                int key = random.nextInt(64);
                switch (random.nextInt(6)) {
                    case 0, 1, 2 -> {
                        int value = random.nextInt();
                        Integer previous = expected.put(key, value);
                        if (expected.size() > capacity) {
                            expected.remove(expected.keySet().iterator().next());
                        }
                        assertEquals(previous, cache.put(key, value));
                    }
                    case 3, 4 -> assertEquals(expected.get(key), cache.get(key));
                    default -> {
                        if (random.nextInt(100) == 0) {
                            expected.clear();
                            cache.clear();
                        } else {
                            assertEquals(expected.remove(key), cache.remove(key));
                        }
                    }
                }
                assertEquals(expected.size(), cache.size());
                assertEquals(expected.isEmpty(), cache.isEmpty());
                assertTrue(cache.size() <= capacity);
            }
            for (int key = 0; key < 64; key++) {
                assertEquals(expected.get(key), cache.get(key));
            }
        }
    }

    private record CollisionKey(int id) {
        @Override
        public int hashCode() {
            return Integer.MIN_VALUE;
        }
    }
}
