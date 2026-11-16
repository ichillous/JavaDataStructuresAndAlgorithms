package io.github.ichillous.dsa.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.ichillous.dsa.algorithms.AhoCorasick.Match;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class AhoCorasickTest {
    @Test
    void ahoCorasickMatchesPerPatternKmp() {
        AhoCorasick automaton = new AhoCorasick(List.of("he", "she", "his", "hers"));
        assertEquals(
                List.of(new Match(1, "she"), new Match(2, "he"), new Match(2, "hers")),
                automaton.findAll("ushers"));

        long seed = 0xACL;
        Random random = new Random(seed);
        for (int trial = 0; trial < 300; trial++) {
            int patternCount = 1 + random.nextInt(5);
            List<String> patterns = new ArrayList<>();
            for (int p = 0; p < patternCount; p++) {
                patterns.add(randomString(random, 1 + random.nextInt(4)));
            }
            String text = randomString(random, random.nextInt(40));
            List<Match> expected = new ArrayList<>();
            for (String pattern : new TreeSet<>(patterns)) {
                for (int start : StringAlgorithms.kmpFindAll(text, pattern)) {
                    expected.add(new Match(start, pattern));
                }
            }
            expected.sort(
                    Comparator.comparingInt(Match::startInclusive)
                            .thenComparingInt(m -> m.pattern().length()));
            assertEquals(
                    expected,
                    new AhoCorasick(patterns).findAll(text),
                    "seed=" + seed + " trial=" + trial + " patterns=" + patterns + " text=" + text);
        }
    }

    @Test
    void invalidArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new AhoCorasick(List.of("a", "")));
        assertThrows(NullPointerException.class, () -> new AhoCorasick(null));
        assertThrows(
                NullPointerException.class,
                () -> new AhoCorasick(java.util.Arrays.asList("a", null)));
        AhoCorasick automaton = new AhoCorasick(List.of("a"));
        assertThrows(NullPointerException.class, () -> automaton.findAll(null));
        assertThrows(IllegalArgumentException.class, () -> new Match(-1, "a"));
        assertThrows(IllegalArgumentException.class, () -> new Match(0, ""));
        assertThrows(NullPointerException.class, () -> new Match(0, null));
    }

    @Test
    void constructorCopiesPatternsAndDuplicatesCollapse() {
        List<String> patterns = new ArrayList<>(List.of("ab", "ab", "b"));
        AhoCorasick automaton = new AhoCorasick(patterns);
        patterns.clear();
        patterns.add("zz");
        assertEquals(
                List.of(new Match(0, "ab"), new Match(1, "b")), automaton.findAll("ab"));
        assertTrue(automaton.findAll("zz").isEmpty());
    }

    @Test
    void emptyTextAndNoMatches() {
        AhoCorasick automaton = new AhoCorasick(Set.of("a", "abc"));
        assertTrue(automaton.findAll("").isEmpty());
        assertTrue(automaton.findAll("xyz").isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> automaton.findAll("a").add(null));
    }

    @Test
    void nestedPatternsReportEveryOverlap() {
        AhoCorasick automaton = new AhoCorasick(List.of("a", "aa", "aaa"));
        assertEquals(
                List.of(
                        new Match(0, "a"),
                        new Match(0, "aa"),
                        new Match(0, "aaa"),
                        new Match(1, "a"),
                        new Match(1, "aa"),
                        new Match(1, "aaa")),
                automaton.findAll("aaaa").subList(0, 6));
        assertEquals(9, automaton.findAll("aaaa").size());
    }

    @Test
    void veryLongPatternDoesNotOverflowTheStack() {
        String pattern = "a".repeat(200_000);
        AhoCorasick automaton = new AhoCorasick(List.of(pattern, "aa"));
        List<Match> matches = automaton.findAll(pattern);
        assertEquals(1 + (pattern.length() - 1), matches.size());
        assertEquals(new Match(0, "aa"), matches.get(0));
    }

    private static String randomString(Random random, int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append((char) ('a' + random.nextInt(3)));
        }
        return builder.toString();
    }
}
