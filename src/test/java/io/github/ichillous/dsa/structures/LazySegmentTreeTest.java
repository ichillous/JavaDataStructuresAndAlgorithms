package io.github.ichillous.dsa.structures;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Random;
import org.junit.jupiter.api.Test;

class LazySegmentTreeTest {
    private static final long SEED = 0x1A27;
    private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    @Test
    void lazyTreeMatchesBigIntegerModel() {
        Random random = new Random(SEED);
        int length = 37;
        long[] values = new long[length];
        for (int index = 0; index < length; index++) {
            values[index] = randomElement(random);
        }
        long[] original = values.clone();
        LazySegmentTree tree = new LazySegmentTree(values);
        BigInteger[] model = new BigInteger[length];
        for (int index = 0; index < length; index++) {
            model[index] = BigInteger.valueOf(values[index]);
        }
        int fittingSums = 0;
        int overflowingSums = 0;
        int acceptedAdds = 0;
        int rejectedAdds = 0;
        for (int step = 0; step < 5_000; step++) {
            int a = random.nextInt(length + 1);
            int b = random.nextInt(length + 1);
            int from = Math.min(a, b);
            int to = Math.max(a, b);
            String context = "seed 0x1A27, step " + step + ", range [" + from + ", " + to + ")";
            if (random.nextBoolean()) {
                if (fitsInLong(modelSum(model, from, to))) {
                    fittingSums++;
                } else {
                    overflowingSums++;
                }
                assertSumMatchesModel(model, tree, from, to, context);
                continue;
            }
            long delta = randomDelta(random, model, from, to);
            String update = context + ", delta " + delta;
            if (leavesLongRange(model, from, to, delta)) {
                assertThrows(ArithmeticException.class, () -> tree.rangeAdd(from, to, delta), update);
                rejectedAdds++;
                for (int index = 0; index < length; index++) {
                    assertEquals(model[index].longValueExact(), tree.rangeSum(index, index + 1),
                            update + ", element " + index + " after rejection");
                }
                assertSumMatchesModel(model, tree, 0, length, update + ", whole range after rejection");
            } else {
                assertDoesNotThrow(() -> tree.rangeAdd(from, to, delta), update);
                for (int index = from; index < to; index++) {
                    model[index] = model[index].add(BigInteger.valueOf(delta));
                }
                acceptedAdds++;
            }
        }
        for (int from = 0; from <= length; from++) {
            for (int to = from; to <= length; to++) {
                assertSumMatchesModel(model, tree, from, to,
                        "seed 0x1A27, final range [" + from + ", " + to + ")");
            }
        }
        assertArrayEquals(original, values, "the tree wrote back into its source array");
        String counts = "seed 0x1A27: fitting sums " + fittingSums + ", overflowing sums "
                + overflowingSums + ", accepted adds " + acceptedAdds + ", rejected adds " + rejectedAdds;
        assertTrue(fittingSums >= 500 && overflowingSums >= 200, counts);
        assertTrue(acceptedAdds >= 500 && rejectedAdds >= 200, counts);
    }

    @Test
    void lazyTreeWholeRangeSumOverflowsWhileHalvesFit() {
        LazySegmentTree stored = new LazySegmentTree(new long[] {Long.MAX_VALUE, 0, -1, Long.MAX_VALUE});
        assertThrows(ArithmeticException.class, () -> stored.rangeSum(0, 4));
        assertEquals(Long.MAX_VALUE, stored.rangeSum(0, 2));
        assertEquals(Long.MAX_VALUE - 1, stored.rangeSum(2, 4));

        LazySegmentTree pending = new LazySegmentTree(new long[4]);
        pending.rangeAdd(0, 4, 1L << 61);
        assertThrows(ArithmeticException.class, () -> pending.rangeSum(0, 4));
        assertEquals(1L << 62, pending.rangeSum(0, 2));
        assertEquals(1L << 62, pending.rangeSum(2, 4));
        pending.rangeAdd(0, 4, -(1L << 62));
        assertEquals(Long.MIN_VALUE, pending.rangeSum(0, 4));
        pending.rangeAdd(3, 4, -1);
        assertThrows(ArithmeticException.class, () -> pending.rangeSum(0, 4));
        assertEquals(-(1L << 62), pending.rangeSum(0, 2));
        assertEquals(-(1L << 62) - 1, pending.rangeSum(2, 4));
    }

    @Test
    void lazyTreePendingAdditionsBeyondLongRangeStayExact() {
        LazySegmentTree tree = new LazySegmentTree(new long[] {
                Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE});
        tree.rangeAdd(0, 5, Long.MAX_VALUE);
        tree.rangeAdd(0, 5, Long.MAX_VALUE);
        // Every element has moved by 2^64 - 2 since construction, more than one long can hold.
        assertElements(tree, Long.MAX_VALUE - 1, Long.MAX_VALUE - 1, Long.MAX_VALUE - 1,
                Long.MAX_VALUE - 1, Long.MAX_VALUE - 1);
        tree.rangeAdd(1, 3, 1);
        assertElements(tree, Long.MAX_VALUE - 1, Long.MAX_VALUE, Long.MAX_VALUE,
                Long.MAX_VALUE - 1, Long.MAX_VALUE - 1);
        assertThrows(ArithmeticException.class, () -> tree.rangeAdd(0, 5, 1));
        assertThrows(ArithmeticException.class, () -> tree.rangeAdd(2, 3, Long.MAX_VALUE));
        tree.rangeAdd(0, 5, Long.MIN_VALUE);
        assertElements(tree, -2, -1, -1, -2, -2);
        assertEquals(-8, tree.rangeSum(0, 5));
        tree.rangeAdd(0, 5, Long.MIN_VALUE + 2);
        assertElements(tree, Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MIN_VALUE + 1,
                Long.MIN_VALUE, Long.MIN_VALUE);
        assertThrows(ArithmeticException.class, () -> tree.rangeAdd(2, 4, -1));
        assertElements(tree, Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MIN_VALUE + 1,
                Long.MIN_VALUE, Long.MIN_VALUE);
        tree.rangeAdd(1, 3, -1);
        assertThrows(ArithmeticException.class, () -> tree.rangeSum(0, 2));
        assertEquals(Long.MIN_VALUE, tree.rangeSum(4, 5));
    }

    @Test
    void lazyTreeShapesMatchArrayModelOnEveryRange() {
        Random random = new Random(SEED);
        for (int length = 0; length <= 33; length++) {
            long[] model = new long[length];
            for (int index = 0; index < length; index++) {
                model[index] = random.nextInt(2_001) - 1_000;
            }
            LazySegmentTree tree = new LazySegmentTree(model);
            assertEquals(length, tree.size());
            for (int step = 0; step < 40; step++) {
                int a = random.nextInt(length + 1);
                int b = random.nextInt(length + 1);
                int from = Math.min(a, b);
                int to = Math.max(a, b);
                long delta = random.nextInt(2_001) - 1_000;
                tree.rangeAdd(from, to, delta);
                for (int index = from; index < to; index++) {
                    model[index] += delta;
                }
                for (int left = 0; left <= length; left++) {
                    for (int right = left; right <= length; right++) {
                        long expected = 0;
                        for (int index = left; index < right; index++) {
                            expected += model[index];
                        }
                        assertEquals(expected, tree.rangeSum(left, right), "seed 0x1A27, length "
                                + length + ", step " + step + ", range [" + left + ", " + right + ")");
                    }
                }
            }
        }
    }

    @Test
    void lazyTreeTraceFromTheLesson() {
        LazySegmentTree tree = new LazySegmentTree(new long[] {1, 2, 3, 4, 5});
        assertEquals(15, tree.rangeSum(0, 5));
        tree.rangeAdd(1, 4, 2);
        assertElements(tree, 1, 4, 5, 6, 5);
        assertEquals(21, tree.rangeSum(0, 5));
        assertEquals(15, tree.rangeSum(1, 4));
        assertEquals(11, tree.rangeSum(2, 4));
        tree.rangeAdd(0, 5, -1);
        assertElements(tree, 0, 3, 4, 5, 4);
        assertEquals(16, tree.rangeSum(0, 5));
    }

    @Test
    void lazyTreeEmptyAndSingleton() {
        LazySegmentTree empty = new LazySegmentTree(new long[0]);
        assertEquals(0, empty.size());
        assertEquals(0, empty.rangeSum(0, 0));
        empty.rangeAdd(0, 0, Long.MAX_VALUE);
        assertEquals(0, empty.rangeSum(0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> empty.rangeAdd(0, 1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> empty.rangeSum(0, 1));

        LazySegmentTree singleton = new LazySegmentTree(new long[] {7});
        assertEquals(1, singleton.size());
        assertEquals(7, singleton.rangeSum(0, 1));
        assertEquals(0, singleton.rangeSum(1, 1));
        singleton.rangeAdd(0, 1, -10);
        assertEquals(-3, singleton.rangeSum(0, 1));
        singleton.rangeAdd(0, 1, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE - 3, singleton.rangeSum(0, 1));
        assertThrows(ArithmeticException.class, () -> singleton.rangeAdd(0, 1, 4));
        assertEquals(Long.MAX_VALUE - 3, singleton.rangeSum(0, 1));
        singleton.rangeAdd(0, 1, 3);
        singleton.rangeAdd(1, 1, Long.MAX_VALUE);
        singleton.rangeAdd(0, 1, 0);
        assertEquals(Long.MAX_VALUE, singleton.rangeSum(0, 1));

        LazySegmentTree minimum = new LazySegmentTree(new long[] {Long.MIN_VALUE});
        assertThrows(ArithmeticException.class, () -> minimum.rangeAdd(0, 1, -1));
        assertThrows(ArithmeticException.class, () -> minimum.rangeAdd(0, 1, Long.MIN_VALUE));
        assertEquals(Long.MIN_VALUE, minimum.rangeSum(0, 1));
        minimum.rangeAdd(0, 1, Long.MAX_VALUE);
        assertEquals(-1, minimum.rangeSum(0, 1));
    }

    @Test
    void lazyTreeCopiesInput() {
        long[] values = {5, 2, 8};
        LazySegmentTree tree = new LazySegmentTree(values);
        assertArrayEquals(new long[] {5, 2, 8}, values, "constructor changed the input");
        values[1] = -100;
        assertEquals(15, tree.rangeSum(0, 3));
        assertEquals(2, tree.rangeSum(1, 2));
        tree.rangeAdd(0, 3, 10);
        assertArrayEquals(new long[] {5, -100, 8}, values, "rangeAdd wrote into the caller's array");
        assertEquals(45, tree.rangeSum(0, 3));
    }

    @Test
    void lazyTreeRejectsBadBoundsBeforeCheckingOverflow() {
        assertThrows(NullPointerException.class, () -> new LazySegmentTree(null));
        LazySegmentTree tree = new LazySegmentTree(new long[] {4, Long.MAX_VALUE, 2});
        for (int[] bounds : new int[][] {{-1, 0}, {0, 4}, {2, 1}, {4, 4}, {-1, -1},
                {Integer.MIN_VALUE, Integer.MAX_VALUE}, {Integer.MAX_VALUE, Integer.MAX_VALUE}}) {
            String range = "range [" + bounds[0] + ", " + bounds[1] + ")";
            assertThrows(IndexOutOfBoundsException.class,
                    () -> tree.rangeAdd(bounds[0], bounds[1], Long.MAX_VALUE), range);
            assertThrows(IndexOutOfBoundsException.class,
                    () -> tree.rangeSum(bounds[0], bounds[1]), range);
        }
        assertElements(tree, 4, Long.MAX_VALUE, 2);
    }

    private static void assertElements(LazySegmentTree tree, long... expected) {
        assertEquals(expected.length, tree.size());
        for (int index = 0; index < expected.length; index++) {
            assertEquals(expected[index], tree.rangeSum(index, index + 1), "element " + index);
        }
    }

    private static void assertSumMatchesModel(
            BigInteger[] model, LazySegmentTree tree, int from, int to, String message) {
        BigInteger expected = modelSum(model, from, to);
        if (fitsInLong(expected)) {
            assertEquals(expected.longValueExact(), tree.rangeSum(from, to), message);
        } else {
            assertThrows(ArithmeticException.class, () -> tree.rangeSum(from, to), message);
        }
    }

    private static BigInteger modelSum(BigInteger[] model, int from, int to) {
        BigInteger sum = BigInteger.ZERO;
        for (int index = from; index < to; index++) {
            sum = sum.add(model[index]);
        }
        return sum;
    }

    private static boolean fitsInLong(BigInteger value) {
        return value.compareTo(LONG_MIN) >= 0 && value.compareTo(LONG_MAX) <= 0;
    }

    private static boolean leavesLongRange(BigInteger[] model, int from, int to, long delta) {
        for (int index = from; index < to; index++) {
            if (!fitsInLong(model[index].add(BigInteger.valueOf(delta)))) {
                return true;
            }
        }
        return false;
    }

    private static long randomElement(Random random) {
        return switch (random.nextInt(4)) {
            case 0 -> Long.MAX_VALUE - random.nextInt(8);
            case 1 -> Long.MIN_VALUE + random.nextInt(8);
            case 2 -> random.nextInt(201) - 100;
            default -> random.nextLong();
        };
    }

    /**
     * Mixes small deltas, deltas near the long limits, arbitrary longs, and deltas that move
     * the range's maximum or minimum to within one of a limit (so both exact fits and
     * one-past-the-limit rejections occur).
     */
    private static long randomDelta(Random random, BigInteger[] model, int from, int to) {
        int choice = random.nextInt(6);
        if (choice >= 4 && from < to) {
            BigInteger extreme = model[from];
            for (int index = from + 1; index < to; index++) {
                extreme = choice == 4 ? extreme.max(model[index]) : extreme.min(model[index]);
            }
            BigInteger limit = choice == 4 ? LONG_MAX : LONG_MIN;
            BigInteger delta = limit.subtract(extreme).add(BigInteger.valueOf(random.nextInt(3) - 1));
            if (fitsInLong(delta)) {
                return delta.longValueExact();
            }
            return choice == 4 ? Long.MAX_VALUE : Long.MIN_VALUE;
        }
        return switch (choice) {
            case 0, 1 -> random.nextInt(201) - 100;
            case 2 -> Long.MAX_VALUE - random.nextInt(4);
            case 3 -> Long.MIN_VALUE + random.nextInt(4);
            default -> random.nextLong();
        };
    }
}
