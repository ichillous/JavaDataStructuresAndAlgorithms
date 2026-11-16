package io.github.ichillous.dsa.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Suffix array of a string, with the Kasai longest-common-prefix array.
 *
 * <p>{@code suffixes()[r]} is the start index of the suffix of rank {@code r} in ascending
 * order by UTF-16 code unit (a proper prefix sorts before its extensions). The text is
 * immutable, so the instance is immutable; the arrays it returns are copies. Positions are
 * code-unit indices; there is no Unicode normalization.
 *
 * <p>Invariant of the build (prefix doubling): after the round for length {@code k}, the rank of
 * each suffix equals the dense rank of its first {@code 2k} code units, where a suffix shorter
 * than that ranks below any longer one it prefixes. Each round orders suffixes by the pair
 * (rank of first half, rank of second half) with two stable counting passes (a radix sort), so a
 * round is O(n) once the ranks are dense and there are at most ceil(log2 n) rounds.
 *
 * <p>Costs for text length {@code n}: construction O(n log n) time and O(n + 65,536) space
 * (the constant is the initial counting-sort alphabet, one slot per char value); Kasai LCP O(n);
 * {@link #occurrences} O(p log n + k log k) for pattern length {@code p} and {@code k} hits
 * (hits are sorted into ascending text order); {@link #distinctSubstrings} and
 * {@link #longestRepeatedSubstring} take O(n) over the stored LCP array.
 */
public final class SuffixArray {
    private static final int ALPHABET = Character.MAX_VALUE + 1;

    private final String text;
    private final int[] suffixes;
    private final int[] lcp;

    /**
     * Builds the suffix array and LCP array.
     *
     * @param text the text; may be empty
     * @throws NullPointerException if text is null
     */
    public SuffixArray(String text) {
        this.text = Objects.requireNonNull(text, "text");
        int n = text.length();
        suffixes = new int[n];
        lcp = new int[n];
        build();
        kasai();
    }

    private void build() {
        int n = text.length();
        if (n == 0) {
            return;
        }
        int[] rank = new int[n];
        int[] next = new int[n];
        int[] bySecondKey = new int[n];
        int[] count = new int[Math.max(n, ALPHABET) + 1];
        for (int i = 0; i < n; i++) {
            count[text.charAt(i) + 1]++;
        }
        for (int c = 0; c < ALPHABET; c++) {
            count[c + 1] += count[c];
        }
        for (int i = 0; i < n; i++) {
            suffixes[count[text.charAt(i)]++] = i;
        }
        int classes = 1;
        rank[suffixes[0]] = 0;
        for (int r = 1; r < n; r++) {
            if (text.charAt(suffixes[r]) != text.charAt(suffixes[r - 1])) {
                classes++;
            }
            rank[suffixes[r]] = classes - 1;
        }
        for (int k = 1; classes < n; k <<= 1) {
            // Second-key order: suffixes with no second half first, then by current rank order.
            int filled = 0;
            for (int i = n - k; i < n; i++) {
                bySecondKey[filled++] = i;
            }
            for (int r = 0; r < n; r++) {
                if (suffixes[r] >= k) {
                    bySecondKey[filled++] = suffixes[r] - k;
                }
            }
            // Stable counting sort by first key.
            Arrays.fill(count, 0, classes + 1, 0);
            for (int i = 0; i < n; i++) {
                count[rank[i] + 1]++;
            }
            for (int c = 0; c < classes; c++) {
                count[c + 1] += count[c];
            }
            for (int r = 0; r < n; r++) {
                int suffix = bySecondKey[r];
                suffixes[count[rank[suffix]]++] = suffix;
            }
            next[suffixes[0]] = 0;
            int newClasses = 1;
            for (int r = 1; r < n; r++) {
                int a = suffixes[r - 1];
                int b = suffixes[r];
                int secondA = a + k < n ? rank[a + k] : -1;
                int secondB = b + k < n ? rank[b + k] : -1;
                if (rank[a] != rank[b] || secondA != secondB) {
                    newClasses++;
                }
                next[b] = newClasses - 1;
            }
            System.arraycopy(next, 0, rank, 0, n);
            classes = newClasses;
        }
    }

    private void kasai() {
        int n = text.length();
        int[] rank = new int[n];
        for (int r = 0; r < n; r++) {
            rank[suffixes[r]] = r;
        }
        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] == 0) {
                h = 0;
                continue;
            }
            int j = suffixes[rank[i] - 1];
            while (i + h < n && j + h < n && text.charAt(i + h) == text.charAt(j + h)) {
                h++;
            }
            lcp[rank[i]] = h;
            if (h > 0) {
                h--;
            }
        }
    }

    /** Returns a copy of the suffix start indices in sorted-suffix order. O(n). */
    public int[] suffixes() {
        return suffixes.clone();
    }

    /**
     * Returns a copy of the LCP array: {@code lcp[i]} is the longest common prefix length of the
     * suffixes at ranks {@code i - 1} and {@code i}, and {@code lcp[0]} is 0. O(n).
     */
    public int[] lcp() {
        return lcp.clone();
    }

    /**
     * Returns the start indices of every occurrence of {@code pattern}, ascending, overlaps
     * included, as an immutable list. An empty pattern yields {@code 0..n} inclusive, matching
     * {@link StringAlgorithms#kmpFindAll}. O(p log n + k log k) time, O(k) space.
     *
     * @throws NullPointerException if pattern is null
     */
    public List<Integer> occurrences(String pattern) {
        Objects.requireNonNull(pattern, "pattern");
        if (pattern.isEmpty()) {
            List<Integer> all = new ArrayList<>(text.length() + 1);
            for (int i = 0; i <= text.length(); i++) {
                all.add(i);
            }
            return List.copyOf(all);
        }
        int low = boundary(pattern, false);
        int high = boundary(pattern, true);
        List<Integer> hits = new ArrayList<>(high - low);
        for (int r = low; r < high; r++) {
            hits.add(suffixes[r]);
        }
        Collections.sort(hits);
        return List.copyOf(hits);
    }

    /**
     * Binary search over ranks using at most p code units per comparison. Returns the first
     * rank whose p-prefix is at least the pattern, or, if {@code past}, greater than it.
     */
    private int boundary(String pattern, boolean past) {
        int low = 0;
        int high = suffixes.length;
        while (low < high) {
            int mid = low + (high - low) / 2;
            int order = comparePrefix(suffixes[mid], pattern);
            if (order < 0 || (past && order == 0)) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }

    /** Compares the first pattern.length() units of the suffix with the pattern. */
    private int comparePrefix(int start, String pattern) {
        int limit = Math.min(pattern.length(), text.length() - start);
        for (int i = 0; i < limit; i++) {
            int diff = text.charAt(start + i) - pattern.charAt(i);
            if (diff != 0) {
                return diff;
            }
        }
        return limit < pattern.length() ? -1 : 0;
    }

    /**
     * Returns the number of distinct nonempty substrings: {@code n(n+1)/2 - sum(lcp)}, computed
     * in {@code long} (n is an int, so the product cannot overflow). O(n).
     */
    public long distinctSubstrings() {
        long n = text.length();
        long total = n * (n + 1) / 2;
        for (int value : lcp) {
            total -= value;
        }
        return total;
    }

    /**
     * Returns a longest substring that occurs at least twice (occurrences may overlap), or
     * {@code ""} if none exists or the text is empty. Ties go to the earliest suffix in
     * suffix-array order, which is the lexicographically smallest such substring. O(n).
     */
    public String longestRepeatedSubstring() {
        int best = 0;
        int bestRank = -1;
        for (int r = 1; r < lcp.length; r++) {
            if (lcp[r] > best) {
                best = lcp[r];
                bestRank = r;
            }
        }
        return bestRank < 0 ? "" : text.substring(suffixes[bestRank], suffixes[bestRank] + best);
    }
}
