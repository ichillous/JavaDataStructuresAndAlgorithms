package io.github.ichillous.dsa.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ArrayGrowthTest {
    private static final int MAX = Integer.MAX_VALUE - 8;

    @Test
    void doublesNormallyAndHonorsLargerMinimum() {
        assertEquals(16, ArrayGrowth.grow(8, 9));
        assertEquals(2, ArrayGrowth.grow(1, 2));
        assertEquals(100, ArrayGrowth.grow(8, 100));
        assertEquals(1 << 30, ArrayGrowth.grow(1 << 29, (1 << 29) + 1));
    }

    @Test
    void capsDoublingAtMaximumSafeArrayLength() {
        assertEquals(MAX, ArrayGrowth.grow(1 << 30, (1 << 30) + 1));
        assertEquals(MAX, ArrayGrowth.grow(MAX - 1, MAX));
        assertEquals(MAX, ArrayGrowth.grow(MAX - 1, MAX - 1));
        assertEquals(MAX, ArrayGrowth.grow(1_500_000_000, 1_500_000_001));
        assertEquals(MAX, ArrayGrowth.grow(Integer.MAX_VALUE, MAX));
    }

    @Test
    void throwsOutOfMemoryErrorOnlyWhenRequiredLengthExceedsCap() {
        OutOfMemoryError error = assertThrows(OutOfMemoryError.class,
                () -> ArrayGrowth.grow(MAX, MAX + 1));
        assertEquals("Required array length too large", error.getMessage());
        assertThrows(OutOfMemoryError.class, () -> ArrayGrowth.grow(1, Integer.MAX_VALUE));
        assertThrows(OutOfMemoryError.class, () -> ArrayGrowth.grow(MAX, Integer.MIN_VALUE));
    }
}
