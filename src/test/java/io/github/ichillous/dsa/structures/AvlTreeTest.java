package io.github.ichillous.dsa.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AvlTreeTest {
    @Test
    void emptyAndSingleNodeContracts() {
        AvlTree<Integer> tree = new AvlTree<>();
        assertTrue(tree.isEmpty());
        assertEquals(-1, tree.height());
        assertEquals(List.of(), tree.inOrder());
        assertFalse(tree.contains(1));
        assertFalse(tree.remove(1));
        assertTrue(tree.add(1));
        assertFalse(tree.add(1));
        assertEquals(1, tree.size());
        assertEquals(0, tree.height());
        assertTrue(tree.remove(1));
        assertTrue(tree.isEmpty());
        assertTrue(tree.validateInvariants());
    }

    @ParameterizedTest
    @ValueSource(strings = {"3,2,1", "1,2,3", "3,1,2", "1,3,2"})
    void allFourInsertionRotationShapesKeepOrderAndHeight(String sequence) {
        AvlTree<Integer> tree = new AvlTree<>();
        for (String key : sequence.split(",")) {
            tree.add(Integer.parseInt(key));
            assertTrue(tree.validateInvariants());
        }
        assertEquals(List.of(1, 2, 3), tree.inOrder());
        assertEquals(1, tree.height());
    }

    @Test
    void removalRebalancesAndPreservesSuccessorChild() {
        AvlTree<Integer> tree = new AvlTree<>();
        TreeSet<Integer> expected = new TreeSet<>();
        for (int key : new int[] {20, 10, 30, 5, 15, 25, 40, 22, 27, 23, 50, 60}) {
            tree.add(key);
            expected.add(key);
        }
        for (int key : new int[] {20, 5, 10, 30, 60, 15, 40, 22, 27, 23, 50, 25}) {
            assertTrue(tree.remove(key));
            expected.remove(key);
            assertEquals(new ArrayList<>(expected), tree.inOrder());
            assertEquals(expected.size(), tree.size());
            assertTrue(tree.validateInvariants(), "invalid tree after removing " + key);
        }
        assertEquals(-1, tree.height());
    }

    @Test
    void sortedInsertionsAndDeletionsStayLogarithmicInHeight() {
        AvlTree<Integer> tree = new AvlTree<>();
        for (int key = 0; key < 10_000; key++) {
            assertTrue(tree.add(key));
        }
        assertTrue(tree.height() < 20);
        assertTrue(tree.validateInvariants());
        for (int key = 0; key < 10_000; key++) {
            assertTrue(tree.remove(key));
            if (key % 100 == 0) {
                assertTrue(tree.validateInvariants());
            }
        }
        assertTrue(tree.isEmpty());
        assertTrue(tree.validateInvariants());
    }

    @Test
    void naturalOrderingDefinesDuplicateKeys() {
        AvlTree<BigDecimal> tree = new AvlTree<>();
        assertTrue(tree.add(new BigDecimal("1.0")));
        assertFalse(tree.add(new BigDecimal("1.00")));
        assertTrue(tree.contains(BigDecimal.ONE));
        assertTrue(tree.remove(BigDecimal.ONE));
    }

    @Test
    void nullsAreRejectedAndSnapshotsAreImmutableAndIndependent() {
        AvlTree<Integer> tree = new AvlTree<>();
        assertThrows(NullPointerException.class, () -> tree.add(null));
        assertThrows(NullPointerException.class, () -> tree.contains(null));
        assertThrows(NullPointerException.class, () -> tree.remove(null));
        tree.add(1);
        List<Integer> snapshot = tree.inOrder();
        tree.remove(1);
        assertEquals(List.of(1), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(2));
    }

    @Test
    void seededMixedOperationsMatchTreeSetAndCheckEveryInvariant() {
        Random random = new Random(0xA71);
        AvlTree<Integer> tree = new AvlTree<>();
        TreeSet<Integer> expected = new TreeSet<>();
        for (int operation = 0; operation < 8000; operation++) {
            int key = random.nextInt(401) - 200;
            switch (random.nextInt(3)) {
                case 0 -> assertEquals(expected.add(key), tree.add(key));
                case 1 -> assertEquals(expected.remove(key), tree.remove(key));
                default -> assertEquals(expected.contains(key), tree.contains(key));
            }
            assertEquals(expected.size(), tree.size());
            assertEquals(expected.isEmpty(), tree.isEmpty());
            assertEquals(new ArrayList<>(expected), tree.inOrder());
            assertTrue(tree.validateInvariants(), "invariant violation at operation " + operation);
        }
    }
}
