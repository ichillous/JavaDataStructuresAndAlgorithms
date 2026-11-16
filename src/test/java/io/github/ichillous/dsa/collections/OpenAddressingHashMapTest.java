package io.github.ichillous.dsa.collections;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class OpenAddressingHashMapTest {
    private static final long SEED = 0x0A0A;

    @Test
    void matchesHashMapUnderRandomOperations() {
        Random random = new Random(SEED);
        OpenAddressingHashMap<Integer, Integer> actual = new OpenAddressingHashMap<>(1);
        Map<Integer, Integer> expected = new HashMap<>();
        for (int step = 0; step < 20_000; step++) {
            Integer key = random.nextInt(200);
            String where = "seed " + SEED + " step " + step;
            switch (random.nextInt(5)) {
                case 0 -> {
                    int value = random.nextInt();
                    assertEquals(expected.put(key, value), actual.put(key, value), where);
                }
                case 1 -> assertEquals(expected.get(key), actual.get(key), where);
                case 2 -> assertEquals(expected.remove(key), actual.remove(key), where);
                case 3 -> assertEquals(expected.containsKey(key), actual.containsKey(key), where);
                default -> {
                    if (random.nextInt(200) == 0) {
                        actual.clear();
                        expected.clear();
                    }
                }
            }
            assertEquals(expected.size(), actual.size(), where);
            assertEquals(expected.isEmpty(), actual.isEmpty(), where);
        }
        for (int key = 0; key < 200; key++) {
            assertEquals(expected.get(key), actual.get(key), "seed " + SEED + " key " + key);
        }
    }

    @Test
    void collidingKeysSurviveRemovalInsideClusters() {
        int[] hashes = {0, Integer.MIN_VALUE};
        for (int hash : hashes) {
            OpenAddressingHashMap<Key, Integer> map = new OpenAddressingHashMap<>(4);
            Map<Key, Integer> expected = new HashMap<>();
            for (int i = 0; i < 50; i++) {
                assertNull(map.put(new Key(i, hash), i));
                expected.put(new Key(i, hash), i);
            }
            for (int i = 0; i < 50; i += 3) {
                assertEquals(i, map.remove(new Key(i, hash)));
                expected.remove(new Key(i, hash));
            }
            assertEquals(expected.size(), map.size());
            for (int i = 0; i < 50; i++) {
                assertEquals(expected.get(new Key(i, hash)), map.get(new Key(i, hash)),
                        "hash " + hash + " key " + i);
            }
        }
    }

    @Test
    void probesThatWrapPastTheTableEndSurviveRemovingTheClusterHead() {
        // Different constant hashes give different home slots, so some clusters of 50
        // keys in a 128-slot table must start near the end and wrap to slot 0.
        for (int hash = 0; hash < 128; hash++) {
            OpenAddressingHashMap<Key, Integer> map = new OpenAddressingHashMap<>(96);
            for (int i = 0; i < 50; i++) {
                map.put(new Key(i, hash), i);
            }
            for (int i = 0; i < 50; i++) {
                // Always remove the oldest key, which sits at the head of the cluster.
                assertEquals(i, map.remove(new Key(i, hash)), "hash " + hash);
                for (int rest = i + 1; rest < 50; rest++) {
                    assertEquals(rest, map.get(new Key(rest, hash)),
                            "hash " + hash + " key " + rest);
                }
                assertNull(map.get(new Key(i, hash)));
            }
            assertTrue(map.isEmpty());
        }
    }

    @Test
    void interleavedHomesStayReachableAfterRemoval() {
        Random random = new Random(SEED);
        OpenAddressingHashMap<Key, Integer> actual = new OpenAddressingHashMap<>(2);
        Map<Key, Integer> expected = new HashMap<>();
        for (int step = 0; step < 5_000; step++) {
            Key key = new Key(random.nextInt(60), random.nextInt(4) * 31);
            if (random.nextBoolean()) {
                assertEquals(expected.put(key, step), actual.put(key, step), "seed " + SEED);
            } else {
                assertEquals(expected.remove(key), actual.remove(key), "seed " + SEED);
            }
            assertEquals(expected.size(), actual.size());
        }
        for (Map.Entry<Key, Integer> entry : expected.entrySet()) {
            assertEquals(entry.getValue(), actual.get(entry.getKey()));
        }
    }

    @Test
    void distinguishesAbsentMappingsAndReplacesEqualKeys() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        assertNull(map.get("missing"));
        assertFalse(map.containsKey("missing"));
        assertNull(map.remove("missing"));
        assertNull(map.put("key", 1));
        assertEquals(1, map.put(new String("key"), 2));
        assertEquals(1, map.size());
        assertTrue(map.containsKey("key"));
        assertEquals(2, map.remove("key"));
        assertNull(map.remove("key"));
        assertTrue(map.isEmpty());
    }

    @Test
    void rejectsNullsAndBadCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new OpenAddressingHashMap<>(0));
        assertThrows(IllegalArgumentException.class, () -> new OpenAddressingHashMap<>(-1));
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
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
    void rejectsCapacitiesTooLargeForAnArray() {
        assertThrows(IllegalArgumentException.class,
                () -> new OpenAddressingHashMap<>(Integer.MAX_VALUE));
    }

    @Test
    void clearThenReuse() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>(1);
        map.put("a", 1);
        map.put("b", 2);
        map.clear();
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertFalse(map.containsKey("a"));
        assertNull(map.get("b"));
        assertNull(map.put("a", 3));
        assertEquals(3, map.get("a"));
        assertEquals(1, map.size());
    }

    @Test
    void absentLookupTerminatesWhileGrowingThroughTheLoadLimit() {
        OpenAddressingHashMap<Integer, Integer> map = new OpenAddressingHashMap<>(2);
        for (int i = 0; i < 1_000; i++) {
            map.put(i, i);
            assertNull(map.get(-1 - i));
        }
        assertEquals(1_000, map.size());
    }

    private record Key(int id, int hash) {
        @Override
        public int hashCode() {
            return hash;
        }
    }
}
