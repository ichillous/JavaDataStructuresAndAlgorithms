package io.github.ichillous.dsa.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

class UnionFindTest {
    @Test
    void initiallyEveryIndexIsItsOwnComponent() {
        UnionFind sets = new UnionFind(5);
        assertEquals(5, sets.size());
        assertEquals(5, sets.components());
        for (int element = 0; element < 5; element++) {
            assertEquals(element, sets.find(element));
            assertEquals(1, sets.componentSize(element));
            assertTrue(sets.connected(element, element));
        }
        assertFalse(sets.connected(0, 1));
    }

    @Test
    void unionMergesByConnectivityAndDuplicateUnionDoesNotChangeCounts() {
        UnionFind sets = new UnionFind(6);
        assertTrue(sets.union(0, 1));
        assertTrue(sets.union(2, 3));
        assertTrue(sets.union(1, 3));
        assertTrue(sets.connected(0, 2));
        assertEquals(4, sets.componentSize(3));
        assertEquals(3, sets.components());
        assertFalse(sets.union(0, 2));
        assertFalse(sets.union(4, 4));
        assertEquals(3, sets.components());
        assertTrue(sets.union(4, 0));
        assertEquals(5, sets.componentSize(4));
        assertEquals(2, sets.components());
        assertFalse(sets.connected(0, 5));
    }

    @Test
    void emptyUniverseIsValidAndNegativeSizeIsRejected() {
        UnionFind sets = new UnionFind(0);
        assertEquals(0, sets.size());
        assertEquals(0, sets.components());
        assertThrows(IndexOutOfBoundsException.class, () -> sets.find(0));
        assertThrows(IllegalArgumentException.class, () -> new UnionFind(-1));
    }

    @Test
    void rejectsEveryInvalidIndexBeforeChangingConnectivity() {
        UnionFind sets = new UnionFind(3);
        for (int invalid : new int[] {-1, 3, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            assertThrows(IndexOutOfBoundsException.class, () -> sets.find(invalid));
            assertThrows(IndexOutOfBoundsException.class, () -> sets.componentSize(invalid));
            assertThrows(IndexOutOfBoundsException.class, () -> sets.union(invalid, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> sets.union(0, invalid));
            assertThrows(IndexOutOfBoundsException.class, () -> sets.connected(invalid, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> sets.connected(0, invalid));
        }
        assertEquals(3, sets.components());
        assertFalse(sets.connected(0, 1));
    }

    @Test
    void seededOperationsMatchIndependentComponentLabelModel() {
        int count = 40;
        int[] labels = new int[count];
        Arrays.setAll(labels, index -> index);
        UnionFind sets = new UnionFind(count);
        Random random = new Random(0xD15A01);
        for (int operation = 0; operation < 2000; operation++) {
            int first = random.nextInt(count);
            int second = random.nextInt(count);
            boolean different = labels[first] != labels[second];
            assertEquals(different, sets.union(first, second));
            int oldLabel = labels[second];
            int newLabel = labels[first];
            for (int element = 0; element < count; element++) {
                if (labels[element] == oldLabel) {
                    labels[element] = newLabel;
                }
            }
            assertEquals(Arrays.stream(labels).distinct().count(), sets.components());
            for (int element = 0; element < count; element++) {
                int label = labels[element];
                assertEquals(Arrays.stream(labels).filter(value -> value == label).count(),
                        sets.componentSize(element));
                int other = random.nextInt(count);
                assertEquals(labels[element] == labels[other], sets.connected(element, other));
            }
        }
    }
}
