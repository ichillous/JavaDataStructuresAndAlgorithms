package io.github.ichillous.dsa.visuals;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.UncheckedIOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The shared fixture reader parses the shared files and names a missing key. */
class VisualFixturesTest {
    private static final VisualFixtures SHARED = VisualFixtures.load("examples.properties");

    @Test
    void readsNumbersListsAndStrings() {
        assertEquals("10,30", SHARED.text("array.before"));
        assertEquals(1, SHARED.number("array.index"));
        assertArrayEquals(new int[] {10, 20, 30}, SHARED.ints("array.after"));
        assertArrayEquals(new long[] {10, 20, 30}, SHARED.longs("array.after"));
        assertEquals(List.of(10, 20, 30), SHARED.list("array.after"));
        assertEquals(List.of("Aa", "BB"), SHARED.strings("hash.keys"));
    }

    @Test
    void failuresNameTheKeyAndFile() {
        AssertionFailedError missing = assertThrows(AssertionFailedError.class, () -> SHARED.text("no.such.key"));
        assertTrue(missing.getMessage().contains("no.such.key"));
        assertTrue(missing.getMessage().contains("examples.properties"));
        AssertionFailedError notNumber = assertThrows(AssertionFailedError.class, () -> SHARED.number("hash.keys"));
        assertTrue(notNumber.getMessage().contains("hash.keys"));
        assertThrows(AssertionFailedError.class, () -> SHARED.ints("hash.keys"));
        assertThrows(AssertionFailedError.class, () -> SHARED.longs("hash.keys"));
        assertThrows(UncheckedIOException.class, () -> VisualFixtures.load("examples-missing.properties"));
    }
}
