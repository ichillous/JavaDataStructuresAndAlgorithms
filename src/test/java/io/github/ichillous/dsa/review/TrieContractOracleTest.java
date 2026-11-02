package io.github.ichillous.dsa.review;

import io.github.ichillous.dsa.structures.Trie;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises UTF-16 ordering, shared prefixes and iterative depth independently. */
class TrieContractOracleTest {
    @Test
    void arbitraryUtf16StringsMatchTreeSetPrefixFiltering() {
        Trie trie = new Trie();
        TreeSet<String> expected = new TreeSet<>();
        Random random = new Random(0x71E0C0DEL);
        char[] alphabet = {'\u0000', 'A', 'a', 'b', '\u00e9', '\u4e2d', '\ud83d', '\ude80'};
        for (int operation = 0; operation < 2_000; operation++) {
            String word = randomWord(random, alphabet);
            if (random.nextBoolean()) {
                assertEquals(expected.add(word), trie.add(word));
            } else {
                assertEquals(expected.remove(word), trie.remove(word));
            }
            assertEquals(expected.contains(word), trie.contains(word));
            assertEquals(expected.size(), trie.size());
            String prefix = word.substring(0, random.nextInt(word.length() + 1));
            int limit = random.nextInt(8);
            List<String> matches = expected.stream().filter(value -> value.startsWith(prefix))
                    .limit(limit).toList();
            assertEquals(matches, trie.wordsWithPrefix(prefix, limit));
            assertEquals(prefix.isEmpty() || expected.stream().anyMatch(value -> value.startsWith(prefix)),
                    trie.startsWith(prefix));
        }
    }

    @Test
    void deepWordsSupportListingAndPruningWithoutCallStackRecursion() {
        String stem = "a".repeat(40_000);
        Trie trie = new Trie();
        assertTrue(trie.add(stem + "z"));
        assertTrue(trie.add(stem));
        assertTrue(trie.add(stem + "b"));
        assertEquals(List.of(stem, stem + "b", stem + "z"), trie.wordsWithPrefix("", 10));
        assertTrue(trie.remove(stem));
        assertFalse(trie.contains(stem));
        assertTrue(trie.startsWith(stem));
        assertTrue(trie.remove(stem + "z"));
        assertTrue(trie.remove(stem + "b"));
        assertTrue(trie.isEmpty());
        assertFalse(trie.startsWith("a"));
        assertTrue(trie.startsWith(""));
    }

    private static String randomWord(Random random, char[] alphabet) {
        char[] value = new char[random.nextInt(8)];
        for (int index = 0; index < value.length; index++) {
            value[index] = alphabet[random.nextInt(alphabet.length)];
        }
        return new String(value);
    }
}
