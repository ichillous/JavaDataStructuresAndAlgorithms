package io.github.ichillous.dsa.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class RedBlackTreeMapTest {
    private static final long SEED = 0x2B7EE;

    @Test
    void matchesTreeMapUnderRandomOperations() {
        Random random = new Random(SEED);
        RedBlackTreeMap<Integer, Integer> map = new RedBlackTreeMap<>();
        TreeMap<Integer, Integer> model = new TreeMap<>();
        for (int operation = 0; operation < 30_000; operation++) {
            String context = "seed 0x2B7EE, operation " + operation;
            int key = random.nextInt(1001) - 500;
            switch (random.nextInt(10)) {
                case 0, 1 -> {
                    assertEquals(model.put(key, operation), map.put(key, operation), context);
                    assertMutationMatches(model, map, context);
                }
                case 2 -> {
                    assertEquals(model.remove(key), map.remove(key), context);
                    assertMutationMatches(model, map, context);
                }
                case 3 -> {
                    assertEquals(model.get(key), map.get(key), context);
                    assertEquals(model.containsKey(key), map.containsKey(key), context);
                }
                case 4 -> assertEquals(Optional.ofNullable(model.floorKey(key)), map.floorKey(key), context);
                case 5 -> assertEquals(Optional.ofNullable(model.ceilingKey(key)), map.ceilingKey(key), context);
                case 6 -> assertEquals(model.headMap(key).size(), map.rank(key), context);
                case 7 -> {
                    int rank = random.nextInt(model.size() + 3) - 1;
                    if (rank >= 0 && rank < model.size()) {
                        assertEquals(new ArrayList<>(model.keySet()).get(rank), map.select(rank), context);
                    } else {
                        assertThrows(IndexOutOfBoundsException.class, () -> map.select(rank), context);
                    }
                }
                case 8 -> {
                    int to = random.nextInt(1001) - 500;
                    int expected = key >= to ? 0 : model.subMap(key, to).size();
                    assertEquals(expected, map.countInRange(key, to), context);
                }
                default -> {
                    assertEquals(model.isEmpty() ? Optional.empty() : Optional.of(model.firstKey()),
                            map.firstKey(), context);
                    assertEquals(model.isEmpty() ? Optional.empty() : Optional.of(model.lastKey()),
                            map.lastKey(), context);
                }
            }
            assertEquals(model.size(), map.size(), context);
            assertEquals(model.isEmpty(), map.isEmpty(), context);
        }
        assertTrue(model.size() > 500, "the operation mix should grow a large tree, seed 0x2B7EE");
    }

    @Test
    void ascendingInsertionsStayBalanced() {
        int n = 100_000;
        RedBlackTreeMap<Integer, Integer> map = new RedBlackTreeMap<>();
        for (int key = 0; key < n; key++) {
            assertNull(map.put(key, -key));
        }
        assertEquals(n, map.size());
        assertTrue(map.height() <= 2 * log2(n + 1), "height " + map.height());
        map.checkInvariants();
        assertEquals(12_345, map.select(12_345));
        assertEquals(54_321, map.rank(54_321));
        for (int key = 0; key < n; key++) {
            assertEquals(-key, map.remove(key));
            if (key % 10_000 == 0) {
                map.checkInvariants();
                assertTrue(map.height() <= 2 * log2(map.size() + 1), "height after removing " + key);
            }
        }
        assertTrue(map.isEmpty());
        assertEquals(-1, map.height());
        assertEquals(List.of(), map.keys());
        map.checkInvariants();
    }

    @Test
    void descendingInsertionsAndRemovalsStayBalanced() {
        int n = 100_000;
        RedBlackTreeMap<Integer, Integer> map = new RedBlackTreeMap<>();
        for (int key = n - 1; key >= 0; key--) {
            assertNull(map.put(key, key));
        }
        assertTrue(map.height() <= 2 * log2(n + 1), "height " + map.height());
        map.checkInvariants();
        for (int key = n - 1; key >= 0; key--) {
            assertEquals(key, map.remove(key));
            if (key % 10_000 == 0) {
                map.checkInvariants();
                assertTrue(map.height() <= 2 * log2(map.size() + 1), "height after removing " + key);
            }
        }
        assertTrue(map.isEmpty());
        map.checkInvariants();
    }

    @Test
    void emptyMapQueries() {
        RedBlackTreeMap<Integer, String> map = new RedBlackTreeMap<>();
        assertEquals(Optional.empty(), map.floorKey(3));
        assertEquals(Optional.empty(), map.ceilingKey(3));
        assertEquals(Optional.empty(), map.firstKey());
        assertEquals(Optional.empty(), map.lastKey());
        assertEquals(0, map.rank(3));
        assertThrows(IndexOutOfBoundsException.class, () -> map.select(0));
        assertEquals(-1, map.height());
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertEquals(List.of(), map.keys());
        assertNull(map.get(3));
        assertFalse(map.containsKey(3));
        assertNull(map.remove(3));
        assertEquals(0, map.countInRange(-10, 10));
        map.checkInvariants();
    }

    @Test
    void emptyAndSingletonTransitions() {
        RedBlackTreeMap<Integer, String> map = new RedBlackTreeMap<>();
        assertNull(map.put(7, "seven"));
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
        assertEquals(0, map.height());
        assertEquals(Optional.of(7), map.firstKey());
        assertEquals(Optional.of(7), map.lastKey());
        assertEquals(Optional.of(7), map.floorKey(7));
        assertEquals(Optional.of(7), map.floorKey(100));
        assertEquals(Optional.empty(), map.floorKey(6));
        assertEquals(Optional.of(7), map.ceilingKey(-100));
        assertEquals(Optional.empty(), map.ceilingKey(8));
        assertEquals(0, map.rank(7));
        assertEquals(1, map.rank(8));
        assertEquals(7, map.select(0));
        assertThrows(IndexOutOfBoundsException.class, () -> map.select(1));
        assertThrows(IndexOutOfBoundsException.class, () -> map.select(-1));
        assertEquals(1, map.countInRange(7, 8));
        assertEquals(0, map.countInRange(7, 7));
        assertEquals(List.of(7), map.keys());
        map.checkInvariants();
        assertEquals("seven", map.remove(7));
        assertNull(map.remove(7));
        assertTrue(map.isEmpty());
        assertEquals(-1, map.height());
        map.checkInvariants();
    }

    @Test
    void knownExampleRankSelectFloorCeilingAndRangeCounts() {
        RedBlackTreeMap<Integer, String> map = new RedBlackTreeMap<>();
        for (int key : new int[] {30, 10, 50, 20, 40}) {
            map.put(key, "v" + key);
        }
        assertEquals(List.of(10, 20, 30, 40, 50), map.keys());
        assertEquals(0, map.rank(5));
        assertEquals(0, map.rank(10));
        assertEquals(2, map.rank(25));
        assertEquals(2, map.rank(30));
        assertEquals(5, map.rank(60));
        assertEquals(30, map.select(2));
        assertEquals(50, map.select(4));
        assertEquals(3, map.countInRange(15, 45));
        assertEquals(1, map.countInRange(20, 30));
        assertEquals(0, map.countInRange(45, 15));
        assertEquals(0, map.countInRange(20, 20));
        assertEquals(5, map.countInRange(Integer.MIN_VALUE, Integer.MAX_VALUE));
        assertEquals(Optional.of(20), map.floorKey(25));
        assertEquals(Optional.of(30), map.ceilingKey(25));
        assertEquals(Optional.of(30), map.floorKey(30));
        assertEquals(Optional.empty(), map.floorKey(5));
        assertEquals(Optional.empty(), map.ceilingKey(55));
        assertEquals("v40", map.get(40));
        for (int rank = 0; rank < map.size(); rank++) {
            assertEquals(rank, map.rank(map.select(rank)));
        }
        map.checkInvariants();
    }

    @Test
    void extremeIntegerKeysCompareWithoutOverflow() {
        RedBlackTreeMap<Integer, String> map = new RedBlackTreeMap<>();
        map.put(Integer.MAX_VALUE, "max");
        map.put(0, "zero");
        map.put(Integer.MIN_VALUE, "min");
        assertEquals(List.of(Integer.MIN_VALUE, 0, Integer.MAX_VALUE), map.keys());
        assertEquals(2, map.rank(Integer.MAX_VALUE));
        assertEquals(0, map.rank(Integer.MIN_VALUE));
        assertEquals(2, map.countInRange(Integer.MIN_VALUE, Integer.MAX_VALUE));
        assertEquals(Optional.of(Integer.MAX_VALUE), map.floorKey(Integer.MAX_VALUE));
        assertEquals(Optional.of(Integer.MIN_VALUE), map.ceilingKey(Integer.MIN_VALUE));
        assertEquals(Optional.of(0), map.floorKey(Integer.MAX_VALUE - 1));
        map.checkInvariants();
    }

    @Test
    void naturalOrderingDefinesDuplicateKeysAndKeepsTheOriginalKey() {
        RedBlackTreeMap<BigDecimal, String> map = new RedBlackTreeMap<>();
        BigDecimal original = new BigDecimal("1.0");
        assertNull(map.put(original, "first"));
        assertEquals("first", map.put(new BigDecimal("1.00"), "second"));
        assertEquals(1, map.size());
        assertSame(original, map.keys().get(0));
        assertSame(original, map.select(0));
        assertEquals("second", map.get(BigDecimal.ONE));
        assertTrue(map.containsKey(new BigDecimal("1.000")));
        assertEquals("second", map.remove(BigDecimal.ONE));
        assertTrue(map.isEmpty());
    }

    @Test
    void rejectsNulls() {
        RedBlackTreeMap<Integer, String> empty = new RedBlackTreeMap<>();
        assertThrows(NullPointerException.class, () -> empty.put(null, "x"));
        assertThrows(NullPointerException.class, () -> empty.put(1, null));
        assertThrows(NullPointerException.class, () -> empty.get(null));
        assertThrows(NullPointerException.class, () -> empty.containsKey(null));
        assertThrows(NullPointerException.class, () -> empty.remove(null));
        assertThrows(NullPointerException.class, () -> empty.floorKey(null));
        assertThrows(NullPointerException.class, () -> empty.ceilingKey(null));
        assertThrows(NullPointerException.class, () -> empty.rank(null));
        assertThrows(NullPointerException.class, () -> empty.countInRange(null, 1));
        assertThrows(NullPointerException.class, () -> empty.countInRange(1, null));
        assertTrue(empty.isEmpty());

        RedBlackTreeMap<Integer, String> map = new RedBlackTreeMap<>();
        map.put(1, "one");
        assertThrows(NullPointerException.class, () -> map.put(null, "x"));
        assertThrows(NullPointerException.class, () -> map.put(1, null));
        assertThrows(NullPointerException.class, () -> map.put(2, null));
        assertThrows(NullPointerException.class, () -> map.get(null));
        assertThrows(NullPointerException.class, () -> map.containsKey(null));
        assertThrows(NullPointerException.class, () -> map.remove(null));
        assertThrows(NullPointerException.class, () -> map.floorKey(null));
        assertThrows(NullPointerException.class, () -> map.ceilingKey(null));
        assertThrows(NullPointerException.class, () -> map.rank(null));
        assertThrows(NullPointerException.class, () -> map.countInRange(null, 1));
        assertThrows(NullPointerException.class, () -> map.countInRange(1, null));
        assertEquals(List.of(1), map.keys());
        assertEquals("one", map.get(1));
        map.checkInvariants();
    }

    @Test
    void keysSnapshotIsImmutableAndIndependent() {
        RedBlackTreeMap<Integer, String> map = new RedBlackTreeMap<>();
        map.put(2, "two");
        map.put(1, "one");
        List<Integer> snapshot = map.keys();
        map.put(3, "three");
        map.remove(1);
        assertEquals(List.of(1, 2), snapshot);
        assertEquals(List.of(2, 3), map.keys());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(4));
        assertThrows(UnsupportedOperationException.class, () -> map.keys().clear());
    }

    @Test
    void everyInsertionOrderOfUpToSevenKeysThenEveryDeletionKeepsInvariants() {
        for (int n = 0; n <= 7; n++) {
            for (List<Integer> order : permutations(n)) {
                String context = "insertion order " + order;
                RedBlackTreeMap<Integer, Integer> map = new RedBlackTreeMap<>();
                for (int key : order) {
                    map.put(key, 10 * key);
                    map.checkInvariants();
                }
                assertEquals(range(0, n), map.keys(), context);
                assertTrue(map.height() <= 2 * log2(n + 1), context);
                for (int victim = 0; victim < n; victim++) {
                    RedBlackTreeMap<Integer, Integer> copy = new RedBlackTreeMap<>();
                    order.forEach(key -> copy.put(key, 10 * key));
                    assertEquals(10 * victim, copy.remove(victim), context);
                    copy.checkInvariants();
                    List<Integer> expected = range(0, n);
                    expected.remove(Integer.valueOf(victim));
                    assertEquals(expected, copy.keys(), context + " without " + victim);
                }
                for (int key : order) {
                    assertEquals(10 * key, map.remove(key), context);
                    map.checkInvariants();
                }
                assertTrue(map.isEmpty(), context);
            }
        }
    }

    @Test
    void checkInvariantsRejectsEachKindOfCorruption() {
        RedBlackTreeMap<Integer, String> tooLargeLeft = mapOf(1, 2, 3);
        tooLargeLeft.root().left.key = 5;
        assertInvariantFailure(tooLargeLeft, "order");

        RedBlackTreeMap<Integer, String> tooSmallRight = mapOf(1, 2, 3);
        tooSmallRight.root().right.key = 2;
        assertInvariantFailure(tooSmallRight, "order");

        RedBlackTreeMap<Integer, String> redRoot = mapOf(1);
        redRoot.root().red = true;
        assertInvariantFailure(redRoot, "root");

        // 1..5 gives 4 (black) over 2 (red, children 1 and 3) and 5; a red 1 sits under red 2.
        RedBlackTreeMap<Integer, String> redRed = mapOf(1, 2, 3, 4, 5);
        assertTrue(redRed.root().left.red);
        redRed.root().left.left.red = true;
        assertInvariantFailure(redRed, "red child");

        RedBlackTreeMap<Integer, String> redRightChild = mapOf(1, 2, 3, 4, 5);
        redRightChild.root().left.right.red = true;
        assertInvariantFailure(redRightChild, "red child");

        RedBlackTreeMap<Integer, String> rightLeaning = mapOf(1, 2, 3);
        rightLeaning.root().left.red = true;
        rightLeaning.root().right.red = true;
        assertInvariantFailure(rightLeaning, "right-leaning");

        RedBlackTreeMap<Integer, String> unbalanced = mapOf(1, 2, 3);
        unbalanced.root().left.red = true;
        assertInvariantFailure(unbalanced, "black height");

        RedBlackTreeMap<Integer, String> wrongRootSize = mapOf(1, 2, 3);
        wrongRootSize.root().size = 4;
        assertInvariantFailure(wrongRootSize, "size");

        RedBlackTreeMap<Integer, String> wrongLeafSize = mapOf(1, 2, 3);
        wrongLeafSize.root().right.size = 0;
        assertInvariantFailure(wrongLeafSize, "size");
    }

    private static void assertMutationMatches(TreeMap<Integer, Integer> model,
            RedBlackTreeMap<Integer, Integer> map, String context) {
        map.checkInvariants();
        assertEquals(new ArrayList<>(model.keySet()), map.keys(), context);
    }

    private static void assertInvariantFailure(RedBlackTreeMap<Integer, String> map, String fragment) {
        IllegalStateException error = assertThrows(IllegalStateException.class, map::checkInvariants);
        assertTrue(error.getMessage().contains(fragment), error.getMessage());
    }

    private static RedBlackTreeMap<Integer, String> mapOf(int... keys) {
        RedBlackTreeMap<Integer, String> map = new RedBlackTreeMap<>();
        for (int key : keys) {
            map.put(key, "v" + key);
        }
        map.checkInvariants();
        return map;
    }

    private static double log2(int value) {
        return Math.log(value) / Math.log(2);
    }

    private static List<Integer> range(int fromInclusive, int toExclusive) {
        List<Integer> values = new ArrayList<>();
        for (int value = fromInclusive; value < toExclusive; value++) {
            values.add(value);
        }
        return values;
    }

    private static List<List<Integer>> permutations(int n) {
        List<List<Integer>> result = new ArrayList<>();
        permute(range(0, n), 0, result);
        return result;
    }

    private static void permute(List<Integer> values, int start, List<List<Integer>> result) {
        if (start == values.size()) {
            result.add(List.copyOf(values));
            return;
        }
        for (int i = start; i < values.size(); i++) {
            Collections.swap(values, start, i);
            permute(values, start + 1, result);
            Collections.swap(values, start, i);
        }
    }
}
