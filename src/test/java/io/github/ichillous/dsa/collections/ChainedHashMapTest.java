package io.github.ichillous.dsa.collections;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ChainedHashMapTest {
    @Test
    void distinguishesAbsentMappingsAndReplacesEqualKeys() {
        ChainedHashMap<String, Integer> map = new ChainedHashMap<>();
        assertNull(map.get("missing"));
        assertFalse(map.containsKey("missing"));
        assertNull(map.remove("missing"));
        assertNull(map.put("key", 1));
        assertEquals(1, map.put(new String("key"), 2));
        assertEquals(1, map.size());
        assertTrue(map.containsKey("key"));
        assertEquals(2, map.get("key"));
        assertEquals(2, map.remove("key"));
        assertNull(map.remove("key"));
        assertTrue(map.isEmpty());
    }

    @Test
    void handlesCollidingBucketHeadMiddleAndTailRemoval() {
        ChainedHashMap<Key, Integer> map = new ChainedHashMap<>(16);
        Key tail = new Key(1, 0);
        Key middle = new Key(2, 0);
        Key head = new Key(3, 0);
        map.put(tail, 10);
        map.put(middle, 20);
        map.put(head, 30);
        assertEquals(10, map.remove(tail));
        assertEquals(20, map.get(middle));
        assertEquals(30, map.get(head));
        map.put(tail, 10); // Reinsert at the bucket head: tail -> head -> middle.
        assertEquals(30, map.remove(head));
        assertEquals(10, map.remove(tail));
        assertEquals(20, map.remove(middle));
        assertEquals(0, map.size());
    }

    @Test
    void supportsNegativeAndMinimumIntegerHashCodes() {
        ChainedHashMap<Key, String> map = new ChainedHashMap<>(3);
        Key minimum = new Key(1, Integer.MIN_VALUE);
        Key negative = new Key(2, -7);
        Key maximum = new Key(3, Integer.MAX_VALUE);
        map.put(minimum, "minimum");
        map.put(negative, "negative");
        map.put(maximum, "maximum");
        assertEquals("minimum", map.get(new Key(1, Integer.MIN_VALUE)));
        assertEquals("negative", map.remove(negative));
        assertEquals("maximum", map.get(maximum));
    }

    @Test
    void preservesAllMappingsAcrossRepeatedResizingWithCollisions() {
        ChainedHashMap<Key, Integer> map = new ChainedHashMap<>(1);
        for (int i = 0; i < 300; i++) {
            assertNull(map.put(new Key(i, i % 3), i * 2));
        }
        assertEquals(300, map.size());
        for (int i = 0; i < 300; i++) {
            assertEquals(i * 2, map.get(new Key(i, i % 3)));
        }
        for (int i = 299; i >= 0; i--) {
            assertEquals(i * 2, map.remove(new Key(i, i % 3)));
        }
        assertTrue(map.isEmpty());
    }

    @Test
    void rejectsNullKeysAndValuesWithoutChangingExistingMappings() {
        ChainedHashMap<String, Integer> map = new ChainedHashMap<>();
        map.put("present", 7);
        assertThrows(NullPointerException.class, () -> map.put(null, 1));
        assertThrows(NullPointerException.class, () -> map.put("new", null));
        assertThrows(NullPointerException.class, () -> map.put("present", null));
        assertThrows(NullPointerException.class, () -> map.get(null));
        assertThrows(NullPointerException.class, () -> map.containsKey(null));
        assertThrows(NullPointerException.class, () -> map.remove(null));
        assertEquals(1, map.size());
        assertEquals(7, map.get("present"));
    }

    @Test
    void validatesCapacityAndSupportsClearAndReuse() {
        assertThrows(IllegalArgumentException.class, () -> new ChainedHashMap<>(0));
        assertThrows(IllegalArgumentException.class, () -> new ChainedHashMap<>(-1));
        ChainedHashMap<String, Integer> map = new ChainedHashMap<>(1);
        map.put("a", 1);
        map.put("b", 2);
        map.clear();
        map.clear();
        assertEquals(0, map.size());
        assertFalse(map.containsKey("a"));
        assertNull(map.get("b"));
        assertNull(map.put("a", 3));
        assertEquals(3, map.get("a"));
    }

    @Test
    void randomizedOperationsAgreeWithHashMapUnderHeavyCollisions() {
        Random random = new Random(0x4A5A);
        ChainedHashMap<Key, Integer> actual = new ChainedHashMap<>(1);
        Map<Key, Integer> expected = new HashMap<>();
        for (int step = 0; step < 6_000; step++) {
            Key key = keyFor(random.nextInt(80));
            int operation = random.nextInt(6);
            if (operation < 3) {
                int value = random.nextInt();
                assertEquals(expected.put(key, value), actual.put(key, value));
            } else if (operation == 3) {
                assertEquals(expected.remove(key), actual.remove(key));
            } else if (operation == 4) {
                assertEquals(expected.get(key), actual.get(key));
            } else if (random.nextInt(30) == 0) {
                actual.clear();
                expected.clear();
            }
            assertEquals(expected.size(), actual.size(), "size at step " + step);
            assertEquals(expected.isEmpty(), actual.isEmpty());
            for (int i = 0; i < 80; i++) {
                Key candidate = keyFor(i);
                assertEquals(expected.get(candidate), actual.get(candidate));
                assertEquals(expected.containsKey(candidate), actual.containsKey(candidate));
            }
        }
    }

    private static Key keyFor(int value) {
        return new Key(value, value == 0 ? Integer.MIN_VALUE : value % 5 - 2);
    }

    private record Key(int value, int hash) {
        @Override
        public int hashCode() {
            return hash;
        }
    }
}
