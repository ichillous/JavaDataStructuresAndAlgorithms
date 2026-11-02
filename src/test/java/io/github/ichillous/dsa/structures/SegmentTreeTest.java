package io.github.ichillous.dsa.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.OptionalLong;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SegmentTreeTest {
    @Test
    void emptyTreeAllowsOnlyItsEmptyRange() {
        SegmentTree tree = new SegmentTree(new long[0]);
        assertEquals(0, tree.size());
        assertEquals(OptionalLong.empty(), tree.rangeMin(0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> tree.set(0, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> tree.rangeMin(0, 1));
        assertThrows(NullPointerException.class, () -> new SegmentTree(null));
    }

    @Test
    void emptyRangeIsDistinctFromMinimumEqualToLongMaxValue() {
        SegmentTree tree = new SegmentTree(new long[] {Long.MAX_VALUE, Long.MAX_VALUE});
        assertEquals(OptionalLong.of(Long.MAX_VALUE), tree.rangeMin(0, 2));
        assertEquals(OptionalLong.empty(), tree.rangeMin(0, 0));
        assertEquals(OptionalLong.empty(), tree.rangeMin(1, 1));
        assertEquals(OptionalLong.empty(), tree.rangeMin(2, 2));
        tree.set(1, Long.MIN_VALUE);
        assertEquals(OptionalLong.of(Long.MIN_VALUE), tree.rangeMin(0, 2));
        tree.set(1, Long.MAX_VALUE);
        assertEquals(OptionalLong.of(Long.MAX_VALUE), tree.rangeMin(0, 2));
    }

    @Test
    void singletonCanBeReplacedRepeatedly() {
        SegmentTree tree = new SegmentTree(new long[] {7});
        assertEquals(OptionalLong.of(7), tree.rangeMin(0, 1));
        tree.set(0, -4);
        assertEquals(OptionalLong.of(-4), tree.rangeMin(0, 1));
        tree.set(0, 15);
        assertEquals(OptionalLong.of(15), tree.rangeMin(0, 1));
    }

    @Test
    void nonPowerOfTwoLengthQueriesRespectHalfOpenBounds() {
        SegmentTree tree = new SegmentTree(new long[] {8, 3, 7, -2, 9});
        assertEquals(OptionalLong.of(-2), tree.rangeMin(0, 5));
        assertEquals(OptionalLong.of(3), tree.rangeMin(0, 3));
        assertEquals(OptionalLong.of(7), tree.rangeMin(2, 3));
        assertEquals(OptionalLong.of(9), tree.rangeMin(4, 5));
        tree.set(3, 10);
        assertEquals(OptionalLong.of(3), tree.rangeMin(0, 5));
        assertEquals(OptionalLong.of(9), tree.rangeMin(3, 5));
        tree.set(4, -10);
        assertEquals(OptionalLong.of(-10), tree.rangeMin(0, 5));
    }

    @Test
    void paddedLeavesCannotIntroduceArtificialZeroMinima() {
        SegmentTree tree = new SegmentTree(new long[]{9, 8, 7, 6, 5});
        assertEquals(OptionalLong.of(5), tree.rangeMin(0, 5));
        assertEquals(OptionalLong.of(5), tree.rangeMin(4, 5));
        tree.set(4, Long.MAX_VALUE);
        assertEquals(OptionalLong.of(6), tree.rangeMin(0, 5));
        assertEquals(OptionalLong.of(Long.MAX_VALUE), tree.rangeMin(4, 5));
        assertEquals(OptionalLong.empty(), tree.rangeMin(5, 5));
    }

    @Test
    void constructorAndUpdatesDoNotAliasCallerArray() {
        long[] values = {5, 2, 8};
        SegmentTree tree = new SegmentTree(values);
        values[1] = -100;
        assertEquals(OptionalLong.of(2), tree.rangeMin(0, 3));
        tree.set(2, -7);
        assertEquals(8, values[2]);
        assertEquals(OptionalLong.of(-7), tree.rangeMin(0, 3));
    }

    @Test
    void invalidIndicesAndRangesDoNotChangeStoredValues() {
        SegmentTree tree = new SegmentTree(new long[] {4, 6, 2});
        for (int index : new int[] {-1, 3, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            assertThrows(IndexOutOfBoundsException.class, () -> tree.set(index, -100));
        }
        for (int[] bounds : new int[][] {{-1, 0}, {0, 4}, {2, 1}, {4, 4}, {-1, -1},
                {Integer.MIN_VALUE, Integer.MAX_VALUE}}) {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> tree.rangeMin(bounds[0], bounds[1]));
        }
        assertEquals(OptionalLong.of(4), tree.rangeMin(0, 1));
        assertEquals(OptionalLong.of(6), tree.rangeMin(1, 2));
        assertEquals(OptionalLong.of(2), tree.rangeMin(2, 3));
    }

    @Test
    void seededUpdatesAndAllSmallRangesMatchDirectArrayModel() {
        Random random = new Random(0x5E6A3);
        for (int length = 0; length <= 31; length++) {
            long[] values = new long[length];
            for (int index = 0; index < length; index++) {
                values[index] = random.nextLong();
            }
            SegmentTree tree = new SegmentTree(values);
            assertEquals(length, tree.size());
            for (int operation = 0; operation < 40; operation++) {
                if (length != 0) {
                    int index = random.nextInt(length);
                    long value = switch (random.nextInt(4)) {
                        case 0 -> Long.MAX_VALUE;
                        case 1 -> Long.MIN_VALUE;
                        default -> random.nextLong();
                    };
                    values[index] = value;
                    tree.set(index, value);
                }
                for (int from = 0; from <= length; from++) {
                    for (int to = from; to <= length; to++) {
                        assertEquals(directMinimum(values, from, to), tree.rangeMin(from, to));
                    }
                }
            }
        }
    }

    private static OptionalLong directMinimum(long[] values, int from, int to) {
        if (from == to) {
            return OptionalLong.empty();
        }
        long minimum = values[from];
        for (int index = from + 1; index < to; index++) {
            minimum = Math.min(minimum, values[index]);
        }
        return OptionalLong.of(minimum);
    }
}
