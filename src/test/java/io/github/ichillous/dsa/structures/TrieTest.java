package io.github.ichillous.dsa.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class TrieTest {
    @Test
    void emptyTrieAndEmptyWordHaveDistinctContracts() {
        Trie trie = new Trie();
        assertTrue(trie.isEmpty());
        assertTrue(trie.startsWith(""));
        assertFalse(trie.contains(""));
        assertFalse(trie.remove(""));
        assertFalse(trie.startsWith("x"));
        assertEquals(List.of(), trie.wordsWithPrefix("", 5));
        assertTrue(trie.add(""));
        assertFalse(trie.add(""));
        assertTrue(trie.contains(""));
        assertEquals(1, trie.size());
        assertEquals(List.of(""), trie.wordsWithPrefix("", 5));
        assertTrue(trie.remove(""));
        assertTrue(trie.isEmpty());
    }

    @Test
    void deletionPreservesSharedPrefixesAndPrunesUnusedPaths() {
        Trie trie = new Trie();
        for (String word : List.of("car", "cart", "cat", "dog")) {
            trie.add(word);
        }
        assertFalse(trie.remove("ca"));
        assertTrue(trie.remove("car"));
        assertFalse(trie.contains("car"));
        assertTrue(trie.contains("cart"));
        assertTrue(trie.startsWith("car"));
        assertTrue(trie.remove("cart"));
        assertFalse(trie.startsWith("car"));
        assertTrue(trie.contains("cat"));
        assertTrue(trie.remove("cat"));
        assertFalse(trie.startsWith("c"));
        assertTrue(trie.contains("dog"));
        assertEquals(1, trie.size());
    }

    @Test
    void removingLongerWordPreservesWordAtPrefix() {
        Trie trie = new Trie();
        trie.add("a");
        trie.add("ab");
        assertTrue(trie.remove("ab"));
        assertTrue(trie.contains("a"));
        assertFalse(trie.startsWith("ab"));
    }

    @Test
    void supportsCaseSensitiveUtf16WordsAndLexicographicOrdering() {
        Trie trie = new Trie();
        List<String> input = List.of("éclair", "Cat", "cat", "😀", "😀a", "猫", "a\u0000b");
        input.forEach(trie::add);
        for (String word : input) {
            assertTrue(trie.contains(word));
        }
        assertFalse(trie.contains("CAT"));
        assertEquals(input.stream().sorted().toList(), trie.wordsWithPrefix("", 100));
        assertEquals(List.of("😀", "😀a"), trie.wordsWithPrefix("😀", 5));
    }

    @Test
    void prefixEnumerationUsesLimitsAndReturnsImmutableSnapshots() {
        Trie trie = new Trie();
        List.of("car", "cart", "cat", "cab", "dog").forEach(trie::add);
        assertEquals(List.of("cab", "car"), trie.wordsWithPrefix("ca", 2));
        assertEquals(List.of("car", "cart"), trie.wordsWithPrefix("car", 5));
        assertEquals(List.of(), trie.wordsWithPrefix("ca", 0));
        assertEquals(List.of(), trie.wordsWithPrefix("missing", 10));
        List<String> snapshot = trie.wordsWithPrefix("ca", 10);
        trie.remove("cat");
        assertEquals(List.of("cab", "car", "cart", "cat"), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("cow"));
    }

    @Test
    void rejectsNullsAndNegativeLimits() {
        Trie trie = new Trie();
        assertThrows(NullPointerException.class, () -> trie.add(null));
        assertThrows(NullPointerException.class, () -> trie.remove(null));
        assertThrows(NullPointerException.class, () -> trie.contains(null));
        assertThrows(NullPointerException.class, () -> trie.startsWith(null));
        assertThrows(NullPointerException.class, () -> trie.wordsWithPrefix(null, 1));
        assertThrows(IllegalArgumentException.class, () -> trie.wordsWithPrefix("", -1));
    }

    @Test
    void longWordsDoNotRequireRecursiveTraversalOrDeletion() {
        Trie trie = new Trie();
        String word = "a".repeat(20_000);
        trie.add(word);
        assertTrue(trie.contains(word));
        assertEquals(List.of(word), trie.wordsWithPrefix("a", 1));
        assertTrue(trie.remove(word));
        assertTrue(trie.isEmpty());
        assertFalse(trie.startsWith("a"));
    }

    @Test
    void seededMixedOperationsAndPrefixQueriesMatchTreeSet() {
        Random random = new Random(0x7A1E);
        Trie trie = new Trie();
        TreeSet<String> expected = new TreeSet<>();
        for (int operation = 0; operation < 4000; operation++) {
            String word = randomWord(random);
            switch (random.nextInt(3)) {
                case 0 -> assertEquals(expected.add(word), trie.add(word));
                case 1 -> assertEquals(expected.remove(word), trie.remove(word));
                default -> assertEquals(expected.contains(word), trie.contains(word));
            }
            assertEquals(expected.size(), trie.size());
            assertEquals(expected.isEmpty(), trie.isEmpty());
            String prefix = word.substring(0, Math.min(2, word.length()));
            int limit = random.nextInt(5);
            assertEquals(expected.stream().filter(value -> value.startsWith(prefix)).limit(limit).toList(),
                    trie.wordsWithPrefix(prefix, limit));
        }
        assertEquals(List.copyOf(expected), trie.wordsWithPrefix("", Integer.MAX_VALUE));
    }

    private static String randomWord(Random random) {
        StringBuilder value = new StringBuilder();
        int length = random.nextInt(6);
        for (int i = 0; i < length; i++) {
            value.append((char) ('a' + random.nextInt(4)));
        }
        return value.toString();
    }
}
