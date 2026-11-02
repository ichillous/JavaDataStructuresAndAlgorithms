package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class StringAlgorithmsTest {
    @Test
    void findsOverlappingMatchesAndFallbackPrefixes() {
        assertEquals(List.of(0, 1, 2), StringAlgorithms.kmpFindAll("aaaa", "aa"));
        assertEquals(4, StringAlgorithms.kmpIndexOf("ababababca", "ababca"));
        assertEquals(List.of(4), StringAlgorithms.kmpFindAll("ababababca", "ababca"));
        assertEquals(-1, StringAlgorithms.kmpIndexOf("abc", "abcd"));
        assertEquals(List.of(), StringAlgorithms.kmpFindAll("abc", "d"));
    }

    @Test
    void emptyPatternMatchesEveryBoundary() {
        assertEquals(0, StringAlgorithms.kmpIndexOf("", ""));
        assertEquals(0, StringAlgorithms.kmpIndexOf("abc", ""));
        assertEquals(List.of(0), StringAlgorithms.kmpFindAll("", ""));
        assertEquals(List.of(0, 1, 2, 3), StringAlgorithms.kmpFindAll("abc", ""));
        assertEquals(-1, StringAlgorithms.kmpIndexOf("", "x"));
    }

    @Test
    void indicesUseUtf16CodeUnits() {
        String text = "😀a😀";
        assertEquals(2, StringAlgorithms.kmpIndexOf(text, "a"));
        assertEquals(List.of(0, 3), StringAlgorithms.kmpFindAll(text, "😀"));
        assertEquals(List.of(1, 4), StringAlgorithms.kmpFindAll(text, "\uDE00"));
    }

    @Test
    void agreesWithJdkAndNaiveMatchingOnSeededInputs() {
        Random random = new Random(0x4B4D50);
        char[] alphabet = {'a', 'b', 'c', '\0', 'é', '\uD83D', '\uDE00'};
        for (int trial = 0; trial < 1000; trial++) {
            String text = randomString(random, alphabet, random.nextInt(50));
            String pattern = randomString(random, alphabet, random.nextInt(10));
            if (!text.isEmpty() && trial % 3 == 0) {
                int start = random.nextInt(text.length());
                pattern = text.substring(start, start + random.nextInt(text.length() - start + 1));
            }
            List<Integer> expected = new ArrayList<>();
            for (int index = 0; index <= text.length(); index++) {
                if (text.startsWith(pattern, index)) {
                    expected.add(index);
                }
            }
            assertEquals(text.indexOf(pattern), StringAlgorithms.kmpIndexOf(text, pattern),
                    "trial " + trial);
            assertEquals(expected, StringAlgorithms.kmpFindAll(text, pattern), "trial " + trial);
        }
    }

    @Test
    void returnsImmutableResults() {
        assertThrows(UnsupportedOperationException.class,
                () -> StringAlgorithms.kmpFindAll("abc", "a").add(4));
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> StringAlgorithms.kmpIndexOf(null, "x"));
        assertThrows(NullPointerException.class, () -> StringAlgorithms.kmpIndexOf("x", null));
        assertThrows(NullPointerException.class, () -> StringAlgorithms.kmpFindAll(null, "x"));
        assertThrows(NullPointerException.class, () -> StringAlgorithms.kmpFindAll("x", null));
    }

    private static String randomString(Random random, char[] alphabet, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            result.append(alphabet[random.nextInt(alphabet.length)]);
        }
        return result.toString();
    }
}
