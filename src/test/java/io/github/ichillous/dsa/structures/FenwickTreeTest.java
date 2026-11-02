package io.github.ichillous.dsa.structures;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FenwickTreeTest {
    @Test
    void handlesEmptyRangesAndDefensivelyCopiesInput() {
        FenwickTree empty = new FenwickTree(new long[0]);
        assertEquals(0, empty.size());
        assertEquals(0, empty.prefixSum(0));
        assertEquals(0, empty.rangeSum(0, 0));
        long[] values = {2, -4, 6, 3};
        FenwickTree tree = new FenwickTree(values);
        values[0] = 900;
        assertEquals(7, tree.prefixSum(4));
        assertEquals(2, tree.rangeSum(1, 3));
        tree.add(1, 5);
        assertEquals(12, tree.prefixSum(4));
    }

    @Test
    void validatesAllBounds() {
        FenwickTree tree = new FenwickTree(new long[3]);
        assertThrows(NullPointerException.class, () -> new FenwickTree(null));
        assertThrows(IndexOutOfBoundsException.class, () -> tree.add(-1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> tree.add(3, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> tree.prefixSum(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> tree.prefixSum(4));
        assertThrows(IndexOutOfBoundsException.class, () -> tree.rangeSum(2, 1));
    }

    @Test
    void rejectsOverflowWithoutPartiallyApplyingUpdates() {
        FenwickTree tree = new FenwickTree(new long[]{0, Long.MAX_VALUE});
        assertThrows(ArithmeticException.class, () -> tree.add(0, 1));
        assertEquals(0, tree.prefixSum(1));
        assertEquals(Long.MAX_VALUE, tree.prefixSum(2));
        assertThrows(ArithmeticException.class,
                () -> new FenwickTree(new long[]{Long.MAX_VALUE, 1}));
        FenwickTree range = new FenwickTree(new long[]{-1, Long.MAX_VALUE, 1});
        assertThrows(ArithmeticException.class, () -> range.rangeSum(1, 3));
        assertEquals(0, range.rangeSum(2, 2));
    }

    @Test
    void matchesArrayModelAcrossUpdatesAndRanges() {
        Random random = new Random(0xF3A71C);
        for (int length = 0; length < 75; length++) {
            long[] model = new long[length];
            FenwickTree tree = new FenwickTree(model);
            for (int step = 0; step < 300; step++) {
                if (length > 0 && random.nextBoolean()) {
                    int index = random.nextInt(length);
                    long delta = random.nextInt(201) - 100;
                    tree.add(index, delta);
                    model[index] += delta;
                }
                int a = random.nextInt(length + 1);
                int b = random.nextInt(length + 1);
                int from = Math.min(a, b);
                int to = Math.max(a, b);
                long expected = 0;
                for (int i = from; i < to; i++) {
                    expected += model[i];
                }
                assertEquals(expected, tree.rangeSum(from, to));
            }
        }
    }
}
