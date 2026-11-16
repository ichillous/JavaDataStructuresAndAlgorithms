package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SuffixArrayTest {
    @Test
    void suffixArrayMatchesSortedSuffixes() {
        SuffixArray banana = new SuffixArray("banana");
        assertArrayEquals(new int[] {5, 3, 1, 0, 4, 2}, banana.suffixes());
        assertArrayEquals(new int[] {0, 1, 3, 0, 0, 2}, banana.lcp());

        long seed = 0x5AL;
        Random random = new Random(seed);
        for (int trial = 0; trial < 300; trial++) {
            String text = randomString(random, random.nextInt(60), 1 + random.nextInt(4));
            SuffixArray array = new SuffixArray(text);
            List<Integer> starts = new ArrayList<>();
            for (int i = 0; i < text.length(); i++) {
                starts.add(i);
            }
            starts.sort((a, b) -> text.substring(a).compareTo(text.substring(b)));
            int[] expected = starts.stream().mapToInt(Integer::intValue).toArray();
            String message = "seed=" + seed + " trial=" + trial + " text=" + text;
            assertArrayEquals(expected, array.suffixes(), message);
            int[] lcp = array.lcp();
            for (int r = 0; r < expected.length; r++) {
                int expectedLcp = r == 0 ? 0 : commonPrefix(text, expected[r - 1], expected[r]);
                assertEquals(expectedLcp, lcp[r], message + " rank=" + r);
            }
        }
    }

    @Test
    void highAndLowCodeUnitsSortByUtf16Value() {
        String text = "￿\u0000a￿";
        SuffixArray array = new SuffixArray(text);
        assertArrayEquals(new int[] {1, 2, 3, 0}, array.suffixes());
    }

    @Test
    void occurrencesMatchKmp() {
        long seed = 0x0CCL;
        Random random = new Random(seed);
        for (int trial = 0; trial < 300; trial++) {
            String text = randomString(random, random.nextInt(50), 1 + random.nextInt(3));
            String pattern = randomString(random, random.nextInt(5), 3);
            assertEquals(
                    StringAlgorithms.kmpFindAll(text, pattern),
                    new SuffixArray(text).occurrences(pattern),
                    "seed=" + seed + " trial=" + trial + " text=" + text + " pattern=" + pattern);
        }
        assertEquals(List.of(0, 1, 2, 3), new SuffixArray("abc").occurrences(""));
        assertEquals(List.of(0), new SuffixArray("").occurrences(""));
        assertEquals(List.of(), new SuffixArray("").occurrences("a"));
        assertEquals(List.of(1, 3), new SuffixArray("banana").occurrences("an"));
        assertThrows(NullPointerException.class, () -> new SuffixArray("a").occurrences(null));
        assertThrows(UnsupportedOperationException.class,
                () -> new SuffixArray("aa").occurrences("a").add(1));
    }

    @Test
    void distinctSubstringsMatchesHashSet() {
        long seed = 0xD15L;
        Random random = new Random(seed);
        for (int trial = 0; trial < 200; trial++) {
            String text = randomString(random, random.nextInt(31), 1 + random.nextInt(3));
            Set<String> all = new HashSet<>();
            for (int i = 0; i < text.length(); i++) {
                for (int j = i + 1; j <= text.length(); j++) {
                    all.add(text.substring(i, j));
                }
            }
            assertEquals(
                    all.size(),
                    new SuffixArray(text).distinctSubstrings(),
                    "seed=" + seed + " trial=" + trial + " text=" + text);
        }
    }

    @Test
    void longestRepeatedSubstring() {
        assertEquals("ana", new SuffixArray("banana").longestRepeatedSubstring());
        assertEquals("", new SuffixArray("abc").longestRepeatedSubstring());
        // "ab" and "ba" both repeat with length 2; the earlier suffix-array entry "ab" wins.
        assertEquals("ab", new SuffixArray("abbaab").longestRepeatedSubstring());
        assertEquals("aa", new SuffixArray("aaa").longestRepeatedSubstring());
    }

    @Test
    void emptyText() {
        SuffixArray empty = new SuffixArray("");
        assertArrayEquals(new int[0], empty.suffixes());
        assertArrayEquals(new int[0], empty.lcp());
        assertEquals(0L, empty.distinctSubstrings());
        assertEquals("", empty.longestRepeatedSubstring());
        assertThrows(NullPointerException.class, () -> new SuffixArray(null));
        SuffixArray single = new SuffixArray("x");
        assertArrayEquals(new int[] {0}, single.suffixes());
        assertArrayEquals(new int[] {0}, single.lcp());
        assertEquals(1L, single.distinctSubstrings());
    }

    @Test
    void returnedArraysAreCopies() {
        SuffixArray array = new SuffixArray("banana");
        array.suffixes()[0] = 99;
        array.lcp()[1] = 99;
        assertArrayEquals(new int[] {5, 3, 1, 0, 4, 2}, array.suffixes());
        assertArrayEquals(new int[] {0, 1, 3, 0, 0, 2}, array.lcp());
    }

    @Test
    void largeRepetitiveTextBuilds() {
        String text = "a".repeat(100_000);
        SuffixArray array = new SuffixArray(text);
        assertEquals(99_999, array.suffixes()[0]);
        assertEquals(99_999, array.longestRepeatedSubstring().length());
        assertEquals(100_000L, array.distinctSubstrings());
    }

    private static int commonPrefix(String text, int a, int b) {
        int length = 0;
        while (a + length < text.length()
                && b + length < text.length()
                && text.charAt(a + length) == text.charAt(b + length)) {
            length++;
        }
        return length;
    }

    private static String randomString(Random random, int length, int alphabet) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append((char) ('a' + random.nextInt(alphabet)));
        }
        return builder.toString();
    }
}
