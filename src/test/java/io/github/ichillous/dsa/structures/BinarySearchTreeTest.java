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

class BinarySearchTreeTest {
    @Test
    void emptyTreeHasNoKeysAndHeightMinusOne() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
        assertEquals(List.of(), tree.inOrder());
        assertFalse(tree.contains(4));
        assertFalse(tree.remove(4));
    }

    @Test
    void duplicateInsertionLeavesSizeAndOrderUnchanged() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        assertTrue(tree.add(4));
        assertFalse(tree.add(4));
        assertEquals(1, tree.size());
        assertEquals(0, tree.height());
        assertEquals(List.of(4), tree.inOrder());
    }

    @Test
    void removesLeafRootAndRootWithOneChild() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        tree.add(2);
        tree.add(1);
        tree.add(3);
        assertTrue(tree.remove(1));
        assertTrue(tree.remove(2));
        assertEquals(List.of(3), tree.inOrder());
        assertTrue(tree.remove(3));
        assertTrue(tree.isEmpty());
        assertEquals(-1, tree.height());
    }

    @Test
    void removesTwoChildRootWithDeepSuccessorThatHasRightChild() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        for (int key : new int[] {8, 4, 12, 2, 6, 10, 14, 11}) {
            tree.add(key);
        }
        assertTrue(tree.remove(8));
        assertEquals(List.of(2, 4, 6, 10, 11, 12, 14), tree.inOrder());
        assertEquals(7, tree.size());
        assertFalse(tree.contains(8));
        assertTrue(tree.contains(11));
    }

    @Test
    void removesTwoChildNodeWhoseSuccessorIsImmediateRightChild() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        for (int key : new int[] {5, 3, 7, 2, 4, 8}) {
            tree.add(key);
        }
        assertTrue(tree.remove(3));
        assertTrue(tree.remove(5));
        assertEquals(List.of(2, 4, 7, 8), tree.inOrder());
    }

    @Test
    void removesRootWithOnlyLeftChildAndSplicesNonRootChildren() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        for (int key : new int[] {10, 5, 3, 7, 6}) {
            tree.add(key);
        }
        assertTrue(tree.remove(10));
        assertTrue(tree.remove(7));
        assertTrue(tree.remove(3));
        assertEquals(List.of(5, 6), tree.inOrder());
        assertEquals(1, tree.height());
    }

    @Test
    void orderingDefinesEqualityInsteadOfObjectEquals() {
        BinarySearchTree<BigDecimal> tree = new BinarySearchTree<>();
        assertTrue(tree.add(new BigDecimal("1.0")));
        assertFalse(tree.add(new BigDecimal("1.00")));
        assertTrue(tree.contains(BigDecimal.ONE));
        assertTrue(tree.remove(BigDecimal.ONE));
    }

    @Test
    void nullsAreRejectedAndSnapshotsAreImmutable() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        assertThrows(NullPointerException.class, () -> tree.add(null));
        assertThrows(NullPointerException.class, () -> tree.contains(null));
        assertThrows(NullPointerException.class, () -> tree.remove(null));
        tree.add(1);
        List<Integer> snapshot = tree.inOrder();
        tree.add(2);
        assertEquals(List.of(1), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(3));
    }

    @Test
    void degenerateTreeOperationsDoNotDependOnRecursiveCallStack() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        for (int i = 0; i < 4000; i++) {
            tree.add(i);
        }
        assertEquals(3999, tree.height());
        assertEquals(4000, tree.inOrder().size());
        assertTrue(tree.contains(3999));
        assertTrue(tree.remove(3999));
        assertTrue(tree.remove(0));
        assertEquals(3997, tree.height());
    }

    @Test
    void seededMixedOperationsAgreeWithTreeSet() {
        Random random = new Random(0xB57);
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        TreeSet<Integer> expected = new TreeSet<>();
        for (int operation = 0; operation < 6000; operation++) {
            int key = random.nextInt(257) - 128;
            switch (random.nextInt(3)) {
                case 0 -> assertEquals(expected.add(key), tree.add(key));
                case 1 -> assertEquals(expected.remove(key), tree.remove(key));
                default -> assertEquals(expected.contains(key), tree.contains(key));
            }
            assertEquals(expected.size(), tree.size());
            assertEquals(expected.isEmpty(), tree.isEmpty());
            assertEquals(new ArrayList<>(expected), tree.inOrder());
        }
    }
}
