package io.github.ichillous.dsa.structures;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.OptionalLong;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SparseTableTest {
    private static final long SEED = 0x5BA5;

    @Test
    void sparseTableMatchesAllRanges() {
        Random random = new Random(SEED);
        for (int length = 0; length <= 64; length++) {
            for (int trial = 0; trial < 3; trial++) {
                long[] values = randomValues(random, length, trial);
                long[] original = values.clone();
                SparseTable table = new SparseTable(values);
                assertEquals(length, table.size());
                for (int from = 0; from <= length; from++) {
                    for (int to = from; to <= length; to++) {
                        int l = from;
                        int r = to;
                        assertEquals(scanMinimum(values, from, to), table.rangeMin(from, to),
                                () -> "seed 0x5BA5, length " + values.length + ", range ["
                                        + l + ", " + r + ")");
                    }
                }
                assertArrayEquals(original, values, "constructor or queries changed the input");
            }
        }
    }

    @Test
    void sparseTableTreatsLongMaxValueAsARealMinimum() {
        SparseTable table = new SparseTable(new long[] {Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE});
        assertEquals(OptionalLong.of(Long.MAX_VALUE), table.rangeMin(0, 3));
        assertEquals(OptionalLong.of(Long.MAX_VALUE), table.rangeMin(1, 2));
        assertEquals(OptionalLong.empty(), table.rangeMin(1, 1));
        SparseTable mixed = new SparseTable(new long[] {Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE});
        assertEquals(OptionalLong.of(Long.MIN_VALUE), mixed.rangeMin(0, 3));
        assertEquals(OptionalLong.of(Long.MAX_VALUE), mixed.rangeMin(2, 3));
    }

    @Test
    void sparseTableEmptyAndSingleton() {
        SparseTable empty = new SparseTable(new long[0]);
        assertEquals(0, empty.size());
        assertEquals(OptionalLong.empty(), empty.rangeMin(0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> empty.rangeMin(0, 1));

        SparseTable singleton = new SparseTable(new long[] {-7});
        assertEquals(1, singleton.size());
        assertEquals(OptionalLong.of(-7), singleton.rangeMin(0, 1));
        assertEquals(OptionalLong.empty(), singleton.rangeMin(0, 0));
        assertEquals(OptionalLong.empty(), singleton.rangeMin(1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> singleton.rangeMin(0, 2));
    }

    @Test
    void sparseTableCopiesInput() {
        long[] values = {5, 2, 8, 2, 9};
        long[] original = values.clone();
        SparseTable table = new SparseTable(values);
        assertArrayEquals(original, values, "constructor changed the input");
        values[1] = -100;
        values[3] = -100;
        assertEquals(OptionalLong.of(2), table.rangeMin(0, 5));
        assertEquals(OptionalLong.of(2), table.rangeMin(1, 2));
        assertEquals(OptionalLong.of(2), table.rangeMin(3, 5));
        assertEquals(OptionalLong.of(8), table.rangeMin(2, 3));
    }

    @Test
    void sparseTableRejectsBadBounds() {
        assertThrows(NullPointerException.class, () -> new SparseTable(null));
        SparseTable table = new SparseTable(new long[] {4, 6, 2});
        for (int[] bounds : new int[][] {{-1, 0}, {0, 4}, {2, 1}, {4, 4}, {-1, -1},
                {Integer.MIN_VALUE, Integer.MAX_VALUE}, {Integer.MAX_VALUE, Integer.MAX_VALUE}}) {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> table.rangeMin(bounds[0], bounds[1]),
                    () -> "range [" + bounds[0] + ", " + bounds[1] + ")");
        }
        assertEquals(OptionalLong.of(4), table.rangeMin(0, 1));
        assertEquals(OptionalLong.of(6), table.rangeMin(1, 2));
        assertEquals(OptionalLong.of(2), table.rangeMin(2, 3));
        assertEquals(OptionalLong.empty(), table.rangeMin(3, 3));
    }

    /** Draws a mix of extremes, small duplicates and arbitrary longs; trial 2 is all MAX_VALUE. */
    private static long[] randomValues(Random random, int length, int trial) {
        long[] values = new long[length];
        for (int index = 0; index < length; index++) {
            values[index] = trial == 2 ? Long.MAX_VALUE : switch (random.nextInt(5)) {
                case 0 -> Long.MAX_VALUE;
                case 1 -> Long.MIN_VALUE;
                case 2 -> random.nextInt(7) - 3;
                default -> random.nextLong();
            };
        }
        return values;
    }

    private static OptionalLong scanMinimum(long[] values, int from, int to) {
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
