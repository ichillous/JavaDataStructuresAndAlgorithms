package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Test
    void zFunctionMatchesNaive() {
        assertArrayEquals(new int[] {7, 1, 0, 0, 3, 1, 0}, StringAlgorithms.zFunction("aabxaab"));
        assertArrayEquals(new int[0], StringAlgorithms.zFunction(""));
        assertArrayEquals(new int[] {1}, StringAlgorithms.zFunction("x"));
        assertArrayEquals(new int[] {4, 3, 2, 1}, StringAlgorithms.zFunction("aaaa"));
        Random random = new Random(0x2F);
        char[] alphabet = {'a', 'b', '\uD83D', '\uDE00'};
        for (int trial = 0; trial < 500; trial++) {
            String text = randomString(random, alphabet, random.nextInt(40));
            int[] expected = new int[text.length()];
            for (int start = 0; start < text.length(); start++) {
                int length = 0;
                while (start + length < text.length()
                        && text.charAt(length) == text.charAt(start + length)) {
                    length++;
                }
                expected[start] = length;
            }
            assertArrayEquals(expected, StringAlgorithms.zFunction(text), "seed 0x2F trial " + trial);
        }
    }

    @Test
    void zAndRabinKarpMatchKmp() {
        assertEquals(List.of(0, 1, 2), StringAlgorithms.zFindAll("aaaaa", "aaa"));
        assertEquals(List.of(0, 1, 2), StringAlgorithms.rabinKarpFindAll("aaaaa", "aaa"));
        assertEquals(List.of(0, 1, 2, 3), StringAlgorithms.zFindAll("abc", ""));
        assertEquals(List.of(0, 1, 2, 3), StringAlgorithms.rabinKarpFindAll("abc", ""));
        assertEquals(List.of(0), StringAlgorithms.zFindAll("", ""));
        assertEquals(List.of(0), StringAlgorithms.rabinKarpFindAll("", ""));
        assertEquals(List.of(), StringAlgorithms.zFindAll("ab", "abc"));
        assertEquals(List.of(), StringAlgorithms.rabinKarpFindAll("ab", "abc"));
        assertEquals(List.of(0, 2), StringAlgorithms.zFindAll("\uD83Da\uD83D", "\uD83D"));
        assertEquals(List.of(0, 2), StringAlgorithms.rabinKarpFindAll("\uD83Da\uD83D", "\uD83D"));
        long seed = 0x5A;
        Random random = new Random(seed);
        char[] alphabet = {'a', 'b'};
        for (int trial = 0; trial < 1000; trial++) {
            String text = randomString(random, alphabet, random.nextInt(40));
            String pattern = randomString(random, alphabet, random.nextInt(7));
            List<Integer> expected = StringAlgorithms.kmpFindAll(text, pattern);
            assertEquals(expected, StringAlgorithms.zFindAll(text, pattern),
                    "z seed " + seed + " trial " + trial);
            assertEquals(expected, StringAlgorithms.rabinKarpFindAll(text, pattern),
                    "rk seed " + seed + " trial " + trial);
        }
    }

    @Test
    void rabinKarpVerifiesCollisions() {
        Random random = new Random(0xC0111DE);
        Map<Long, String> seen = new HashMap<>();
        String first = null;
        String second = null;
        while (second == null) {
            String candidate = randomString(random, new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'}, 7);
            long hash = rabinKarpHash(candidate);
            String earlier = seen.putIfAbsent(hash, candidate);
            if (earlier != null && !earlier.equals(candidate)) {
                first = earlier;
                second = candidate;
            }
        }
        assertEquals(rabinKarpHash(first), rabinKarpHash(second));
        assertNotEquals(first, second);
        assertEquals(List.of(), StringAlgorithms.rabinKarpFindAll(first, second));
        assertEquals(List.of(), StringAlgorithms.rabinKarpFindAll("zz" + first + "zz", second));
        assertEquals(List.of(2), StringAlgorithms.rabinKarpFindAll("zz" + first + "zz", first));
        assertEquals(List.of(2),
                StringAlgorithms.rabinKarpFindAll("zz" + first + second + "zz", first));
        assertEquals(List.of(2 + first.length()),
                StringAlgorithms.rabinKarpFindAll("zz" + first + second + "zz", second));
    }

    @Test
    void stringMatchersDoNotMutateOrAcceptNull() {
        assertThrows(NullPointerException.class, () -> StringAlgorithms.zFunction(null));
        assertThrows(NullPointerException.class, () -> StringAlgorithms.zFindAll(null, "x"));
        assertThrows(NullPointerException.class, () -> StringAlgorithms.zFindAll("x", null));
        assertThrows(NullPointerException.class, () -> StringAlgorithms.rabinKarpFindAll(null, "x"));
        assertThrows(NullPointerException.class, () -> StringAlgorithms.rabinKarpFindAll("x", null));
        assertThrows(NullPointerException.class, () -> StringAlgorithms.longestPalindromicSubstring(null));
        assertThrows(UnsupportedOperationException.class,
                () -> StringAlgorithms.zFindAll("abc", "a").add(4));
        assertThrows(UnsupportedOperationException.class,
                () -> StringAlgorithms.rabinKarpFindAll("abc", "a").add(4));
        int[] z = StringAlgorithms.zFunction("abab");
        z[1] = 99;
        assertArrayEquals(new int[] {4, 0, 2, 0}, StringAlgorithms.zFunction("abab"));
    }

    @Test
    void longestPalindromeMatchesBruteForceEarliestTie() {
        assertEquals(new StringAlgorithms.Span(0, 3), StringAlgorithms.longestPalindromicSubstring("babad"));
        assertEquals(new StringAlgorithms.Span(1, 3), StringAlgorithms.longestPalindromicSubstring("cbbd"));
        assertEquals(new StringAlgorithms.Span(0, 1), StringAlgorithms.longestPalindromicSubstring("abc"));
        assertEquals(new StringAlgorithms.Span(0, 0), StringAlgorithms.longestPalindromicSubstring(""));
        assertEquals(new StringAlgorithms.Span(0, 1), StringAlgorithms.longestPalindromicSubstring("x"));
        assertEquals(new StringAlgorithms.Span(0, 5), StringAlgorithms.longestPalindromicSubstring("aaaaa"));
        long seed = 0x1D;
        Random random = new Random(seed);
        char[] alphabet = {'a', 'b', 'c'};
        for (int trial = 0; trial < 1000; trial++) {
            String text = randomString(random, alphabet, random.nextInt(25));
            int bestStart = 0;
            int bestEnd = 0;
            for (int start = 0; start < text.length(); start++) {
                for (int end = start + 1; end <= text.length(); end++) {
                    if (end - start > bestEnd - bestStart
                            && new StringBuilder(text.substring(start, end)).reverse().toString()
                                    .equals(text.substring(start, end))) {
                        bestStart = start;
                        bestEnd = end;
                    }
                }
            }
            assertEquals(new StringAlgorithms.Span(bestStart, bestEnd),
                    StringAlgorithms.longestPalindromicSubstring(text),
                    "seed " + seed + " trial " + trial + " text " + text);
        }
    }

    @Test
    void spanValidatesBounds() {
        assertThrows(IllegalArgumentException.class, () -> new StringAlgorithms.Span(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new StringAlgorithms.Span(2, 1));
        assertEquals(new StringAlgorithms.Span(2, 2), new StringAlgorithms.Span(2, 2));
    }

    private static long rabinKarpHash(String value) {
        long hash = 0;
        for (int index = 0; index < value.length(); index++) {
            hash = (hash * StringAlgorithms.RABIN_KARP_BASE + value.charAt(index))
                    % StringAlgorithms.RABIN_KARP_MODULUS;
        }
        return hash;
    }

    private static String randomString(Random random, char[] alphabet, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            result.append(alphabet[random.nextInt(alphabet.length)]);
        }
        return result.toString();
    }
}
