package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.algorithms.AhoCorasick;
import io.github.ichillous.dsa.algorithms.AhoCorasick.Match;
import io.github.ichillous.dsa.algorithms.StringAlgorithms;
import io.github.ichillous.dsa.algorithms.StringAlgorithms.Span;
import io.github.ichillous.dsa.algorithms.SuffixArray;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks every value the string figures display against the maintained classes. */
class VisualExamplesStringTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-string.properties");

    // z-box

    @Test
    void zArrayMatchesTheFigureAndItsBoxIsACopyOfThePrefix() {
        String text = DATA.text("string.z.text");
        int[] z = StringAlgorithms.zFunction(text);
        assertArrayEquals(DATA.ints("string.z.array"), z);

        int start = DATA.number("string.z.boxStart");
        int end = DATA.number("string.z.boxEnd");
        assertEquals(start + z[start], end, "the box is [start, start + z[start])");
        assertEquals(text.substring(0, end - start), text.substring(start, end),
                "the box repeats the prefix");
        for (int index = 1; index < start; index++) {
            assertTrue(index + z[index] <= end, "no earlier match reaches further right");
        }
    }

    @Test
    void zValuesInsideTheBoxAreCopiedOrCappedAtTheBoxEdge() {
        int[] z = StringAlgorithms.zFunction(DATA.text("string.z.text"));
        int start = DATA.number("string.z.boxStart");
        int end = DATA.number("string.z.boxEnd");

        int copyIndex = DATA.number("string.z.copyIndex");
        int copyMirror = DATA.number("string.z.copyMirror");
        assertEquals(copyIndex - start, copyMirror, "the mirror is the offset inside the box");
        assertEquals(DATA.number("string.z.copyValue"), z[copyMirror]);
        assertTrue(z[copyMirror] < end - copyIndex, "the mirror value fits inside the box");
        assertEquals(z[copyMirror], z[copyIndex]);

        int capIndex = DATA.number("string.z.capIndex");
        int capMirror = DATA.number("string.z.capMirror");
        assertEquals(capIndex - start, capMirror);
        assertEquals(DATA.number("string.z.capMirrorValue"), z[capMirror]);
        assertTrue(z[capMirror] > end - capIndex, "the mirror value overruns the box");
        assertEquals(DATA.number("string.z.capValue"), end - capIndex);
        assertEquals(DATA.number("string.z.capValue"), z[capIndex]);
    }

    // rabin-karp-rolling

    @Test
    void windowHashesFollowTheDocumentedFormula() {
        String text = DATA.text("string.rk.text");
        String pattern = DATA.text("string.rk.pattern");
        assertEquals(257, DATA.number("string.rk.base"));
        assertEquals(Integer.MAX_VALUE, DATA.number("string.rk.modulus"));

        assertEquals(DATA.longs("string.rk.patternHash")[0], hash(pattern));
        long[] shown = DATA.longs("string.rk.windowHashes");
        assertEquals(text.length() - pattern.length() + 1, shown.length);
        for (int start = 0; start < shown.length; start++) {
            assertEquals(shown[start], hash(text.substring(start, start + pattern.length())),
                    "window " + start);
        }
    }

    @Test
    void theImpostorHitIsRejectedAndTheRealHitIsReported() {
        String text = DATA.text("string.rk.text");
        String pattern = DATA.text("string.rk.pattern");
        int impostor = DATA.number("string.rk.impostorStart");
        int match = DATA.number("string.rk.matchStart");
        long patternHash = hash(pattern);

        List<Integer> hashHits = new ArrayList<>();
        for (int start = 0; start + pattern.length() <= text.length(); start++) {
            if (hash(text.substring(start, start + pattern.length())) == patternHash) {
                hashHits.add(start);
            }
        }
        assertEquals(List.of(impostor, match), hashHits, "exactly two windows share the hash");

        String impostorWindow = text.substring(impostor, impostor + pattern.length());
        assertNotEquals(pattern, impostorWindow, "the impostor has the pattern's hash but other units");
        int firstDifference = 0;
        while (impostorWindow.charAt(firstDifference) == pattern.charAt(firstDifference)) {
            firstDifference++;
        }
        assertEquals(DATA.number("string.rk.mismatchUnit"), firstDifference);
        assertEquals(pattern, text.substring(match, match + pattern.length()));

        assertEquals(List.of(match), StringAlgorithms.rabinKarpFindAll(text, pattern));
        assertEquals(StringAlgorithms.kmpFindAll(text, pattern),
                StringAlgorithms.rabinKarpFindAll(text, pattern));
    }

    // manacher-mirror

    @Test
    void shownRadiiAreTheLongestOddPalindromesAndTheSpanIsTheLongest() {
        String text = DATA.text("string.man.text");
        int[] radii = DATA.ints("string.man.radii");
        assertEquals(text.length(), radii.length);
        for (int center = 0; center < radii.length; center++) {
            assertEquals(directRadius(text, center), radii[center], "center " + center);
        }
        Span longest = StringAlgorithms.longestPalindromicSubstring(text);
        assertEquals(new Span(DATA.number("string.man.longestStart"),
                DATA.number("string.man.longestEnd")), longest);
        int center = DATA.number("string.man.center");
        assertEquals(longest.endExclusive() - longest.startInclusive(), 2 * radii[center] - 1);
        assertEquals(center - radii[center] + 1, DATA.number("string.man.left"));
        assertEquals(center + radii[center] - 1, DATA.number("string.man.right"));
    }

    @Test
    void mirrorRadiiAreCopiedOrCappedAtTheRightEdge() {
        String text = DATA.text("string.man.text");
        int[] radii = DATA.ints("string.man.radii");
        int left = DATA.number("string.man.left");
        int right = DATA.number("string.man.right");

        int fitIndex = DATA.number("string.man.fitIndex");
        int fitMirror = DATA.number("string.man.fitMirror");
        assertEquals(left + right - fitIndex, fitMirror);
        assertTrue(radii[fitMirror] <= right - fitIndex + 1, "the mirror radius fits");
        assertEquals(radii[fitMirror], radii[fitIndex], "a fitting radius is final");

        int capIndex = DATA.number("string.man.capIndex");
        int capMirror = DATA.number("string.man.capMirror");
        assertEquals(left + right - capIndex, capMirror);
        assertEquals(DATA.number("string.man.capMirrorRadius"), radii[capMirror]);
        assertEquals(DATA.number("string.man.capStart"), right - capIndex + 1);
        assertTrue(radii[capMirror] > right - capIndex + 1, "the mirror radius overruns right");
        assertEquals(DATA.number("string.man.capStart"), radii[capIndex],
                "past the edge the next units differ, so the capped value is final");
        assertNotEquals(text.charAt(capIndex - radii[capIndex]), text.charAt(capIndex + radii[capIndex]));
    }

    // aho-corasick-links

    @Test
    void theTrieNodesAreTheDistinctPatternPrefixes() {
        List<String> patterns = DATA.strings("string.ac.patterns");
        List<String> expected = new ArrayList<>();
        for (String pattern : patterns) {
            for (int length = 1; length <= pattern.length(); length++) {
                String prefix = pattern.substring(0, length);
                if (!expected.contains(prefix)) {
                    expected.add(prefix);
                }
            }
        }
        expected.sort(null);
        List<String> shown = new ArrayList<>(DATA.strings("string.ac.nodes"));
        shown.sort(null);
        assertEquals(expected, shown);
    }

    @Test
    void shownFailureLinksFollowTheDefinitionAndShowUpInTheMatches() {
        List<String> patterns = DATA.strings("string.ac.patterns");
        List<String> nodes = DATA.strings("string.ac.nodes");
        AhoCorasick automaton = new AhoCorasick(patterns);

        List<String> shown = DATA.strings("string.ac.failure");
        for (String node : nodes) {
            String target = longestProperSuffixInTrie(node, nodes);
            String link = node + ">" + target;
            assertEquals(!target.isEmpty(), shown.contains(link), "failure link of " + node);
        }
        // Behavior: reading a node's spelling reports every pattern inside it, so links must reach them.
        for (String node : nodes) {
            List<Match> expected = new ArrayList<>();
            for (int start = 0; start < node.length(); start++) {
                for (int end = start + 1; end <= node.length(); end++) {
                    String piece = node.substring(start, end);
                    if (patterns.contains(piece)) {
                        expected.add(new Match(start, piece));
                    }
                }
            }
            assertEquals(expected, automaton.findAll(node), "matches while reading " + node);
        }
    }

    @Test
    void theScanKeepsTheShownPrefixesAndReportsTheShownMatches() {
        List<String> patterns = DATA.strings("string.ac.patterns");
        List<String> nodes = DATA.strings("string.ac.nodes");
        String text = DATA.text("string.ac.text");

        List<String> kept = DATA.strings("string.ac.kept");
        assertEquals(text.length(), kept.size());
        for (int end = 1; end <= text.length(); end++) {
            String longest = "";
            for (int start = 0; start < end; start++) {
                String candidate = text.substring(start, end);
                if (nodes.contains(candidate)) {
                    longest = candidate;
                    break;
                }
            }
            assertEquals(longest.isEmpty() ? "root" : longest, kept.get(end - 1), "after unit " + end);
        }

        List<Match> expected = new ArrayList<>();
        for (String entry : DATA.strings("string.ac.matches")) {
            String[] parts = entry.split(":");
            expected.add(new Match(Integer.parseInt(parts[0]), parts[1]));
        }
        assertEquals(expected, new AhoCorasick(patterns).findAll(text));
    }

    // suffix-array-lcp

    @Test
    void suffixOrderAndLcpMatchTheFigure() {
        String text = DATA.text("string.sa.text");
        SuffixArray array = new SuffixArray(text);
        assertArrayEquals(DATA.ints("string.sa.suffixes"), array.suffixes());
        assertArrayEquals(DATA.ints("string.sa.lcp"), array.lcp());

        int[] suffixes = array.suffixes();
        int[] lcp = array.lcp();
        for (int rank = 1; rank < suffixes.length; rank++) {
            String previous = text.substring(suffixes[rank - 1]);
            String current = text.substring(suffixes[rank]);
            assertTrue(previous.compareTo(current) < 0, "rank " + rank + " is larger");
            int common = 0;
            while (common < previous.length() && common < current.length()
                    && previous.charAt(common) == current.charAt(common)) {
                common++;
            }
            assertEquals(common, lcp[rank], "lcp at rank " + rank);
        }
    }

    @Test
    void thePatternOccupiesTheShownRankRange() {
        String text = DATA.text("string.sa.text");
        String pattern = DATA.text("string.sa.pattern");
        SuffixArray array = new SuffixArray(text);
        int[] suffixes = array.suffixes();
        int low = DATA.number("string.sa.low");
        int high = DATA.number("string.sa.high");

        for (int rank = 0; rank < suffixes.length; rank++) {
            boolean inside = low <= rank && rank < high;
            assertEquals(inside, text.startsWith(pattern, suffixes[rank]), "rank " + rank);
        }
        assertFalse(DATA.list("string.sa.occurrences").isEmpty());
        assertEquals(DATA.list("string.sa.occurrences"), array.occurrences(pattern));
        List<Integer> starts = new ArrayList<>();
        for (int rank = low; rank < high; rank++) {
            starts.add(suffixes[rank]);
        }
        starts.sort(null);
        assertEquals(starts, array.occurrences(pattern));
    }

    // Independent models.

    /** sum(unit * 257^(p-1-j)) mod (2^31 - 1), the formula the lesson documents. */
    private static long hash(String value) {
        long hash = 0;
        for (int index = 0; index < value.length(); index++) {
            hash = (hash * DATA.number("string.rk.base") + value.charAt(index))
                    % DATA.number("string.rk.modulus");
        }
        return hash;
    }

    /** Largest r such that text[center - r + 1 .. center + r - 1] is a palindrome, checked directly. */
    private static int directRadius(String text, int center) {
        int radius = 1;
        while (center - radius >= 0 && center + radius < text.length()
                && text.charAt(center - radius) == text.charAt(center + radius)) {
            radius++;
        }
        return radius;
    }

    /** The longest proper suffix of {@code node} that is a trie node, or "" for the root. */
    private static String longestProperSuffixInTrie(String node, List<String> nodes) {
        for (int start = 1; start < node.length(); start++) {
            String suffix = node.substring(start);
            if (nodes.contains(suffix)) {
                return suffix;
            }
        }
        return "";
    }
}
