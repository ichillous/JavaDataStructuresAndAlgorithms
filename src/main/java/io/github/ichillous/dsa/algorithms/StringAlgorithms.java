package io.github.ichillous.dsa.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * String matching (KMP, Z-function, Rabin–Karp) and Manacher's palindromes. All indices are UTF-16
 * code-unit positions; no method normalizes Unicode or folds case. Inputs are never mutated and
 * returned lists are immutable.
 */
public final class StringAlgorithms {
    /** Rabin–Karp polynomial base; each UTF-16 unit (0..65535) is one digit. */
    static final long RABIN_KARP_BASE = 257;

    /** Rabin–Karp modulus, the Mersenne prime 2^31 - 1, so every product stays below 2^48. */
    static final long RABIN_KARP_MODULUS = 2_147_483_647L;

    private StringAlgorithms() {}

    /**
     * A half-open range {@code [startInclusive, endExclusive)} of UTF-16 positions, returned by
     * {@link #longestPalindromicSubstring} and {@link ArrayPatterns#longestSubstringWithoutRepeats}.
     * The compact constructor throws {@link IllegalArgumentException} unless
     * {@code 0 <= startInclusive <= endExclusive}.
     *
     * @param startInclusive first position, at least zero
     * @param endExclusive position after the last unit, at least {@code startInclusive}
     */
    public record Span(int startInclusive, int endExclusive) {
        /** Validates {@code 0 <= startInclusive <= endExclusive}. */
        public Span {
            if (startInclusive < 0 || endExclusive < startInclusive) {
                throw new IllegalArgumentException(
                        "require 0 <= start <= end: " + startInclusive + ", " + endExclusive);
            }
        }
    }

    /**
     * Returns the first match, or {@code -1}. An empty pattern matches at index zero.
     * O(text length + pattern length) time and O(pattern length) extra space.
     */
    public static int kmpIndexOf(String text, String pattern) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
        if (pattern.isEmpty()) {
            return 0;
        }
        int[] prefix = prefixLengths(pattern);
        int matched = 0;
        for (int index = 0; index < text.length(); index++) {
            while (matched > 0 && text.charAt(index) != pattern.charAt(matched)) {
                matched = prefix[matched - 1];
            }
            if (text.charAt(index) == pattern.charAt(matched)) {
                matched++;
            }
            if (matched == pattern.length()) {
                return index - matched + 1;
            }
        }
        return -1;
    }

    /**
     * Returns an immutable list of all match indices, including overlapping matches.
     * An empty pattern matches each boundary, from zero through {@code text.length()}.
     * O(n + m + k) time and O(m + k) space for text length n, pattern length m, and k results.
     */
    public static List<Integer> kmpFindAll(String text, String pattern) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
        List<Integer> matches = new ArrayList<>();
        if (pattern.isEmpty()) {
            for (int index = 0; index <= text.length(); index++) {
                matches.add(index);
            }
            return List.copyOf(matches);
        }
        int[] prefix = prefixLengths(pattern);
        int matched = 0;
        for (int index = 0; index < text.length(); index++) {
            while (matched > 0 && text.charAt(index) != pattern.charAt(matched)) {
                matched = prefix[matched - 1];
            }
            if (text.charAt(index) == pattern.charAt(matched)) {
                matched++;
            }
            if (matched == pattern.length()) {
                matches.add(index - matched + 1);
                matched = prefix[matched - 1];
            }
        }
        return List.copyOf(matches);
    }

    /**
     * Returns the Z-array: {@code z[i]} is the length of the longest common prefix of
     * {@code text} and {@code text.substring(i)}. By documented convention {@code z[0]} is
     * {@code text.length()}; an empty text gives an empty array. The returned array is fresh.
     * O(n) time (each comparison either extends the rightmost match window or is bounded by an
     * earlier value) and O(n) space.
     */
    public static int[] zFunction(String text) {
        Objects.requireNonNull(text, "text");
        int length = text.length();
        int[] z = new int[length];
        if (length == 0) {
            return z;
        }
        z[0] = length;
        int left = 0;
        int right = 0; // [left, right) is the rightmost prefix match found so far
        for (int index = 1; index < length; index++) {
            int match = 0;
            if (index < right) {
                match = Math.min(right - index, z[index - left]);
            }
            while (index + match < length && text.charAt(match) == text.charAt(index + match)) {
                match++;
            }
            z[index] = match;
            if (index + match > right) {
                left = index;
                right = index + match;
            }
        }
        return z;
    }

    /**
     * Returns all match indices, overlaps included, with the same results and empty-pattern
     * semantics as {@link #kmpFindAll}. Computes the Z-array of {@code pattern + text}: position
     * {@code p + i} has {@code z >= p} exactly when the pattern occurs at text index {@code i}, so
     * no separator character is needed. O(t + p + k) time and O(t + p + k) space (the concatenation
     * and its Z-array); the combined length must fit in an {@code int}.
     */
    public static List<Integer> zFindAll(String text, String pattern) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
        int patternLength = pattern.length();
        if (patternLength == 0) {
            return boundaries(text.length());
        }
        int[] z = zFunction(pattern + text);
        List<Integer> matches = new ArrayList<>();
        for (int index = 0; index + patternLength <= text.length(); index++) {
            if (z[patternLength + index] >= patternLength) {
                matches.add(index);
            }
        }
        return List.copyOf(matches);
    }

    /**
     * Returns all match indices, overlaps included, with the same results and empty-pattern
     * semantics as {@link #kmpFindAll}. A rolling polynomial hash with base 257 modulo the prime
     * 2^31 - 1 selects candidates; every candidate is then compared character by character, so a
     * hash collision never produces a false positive. Hash arithmetic uses {@code long} with
     * operands below 2^31 and units below 2^16, so nothing overflows. O(t + p + k * p) time for
     * k reported matches when spurious hash hits are rare; worst case O(t * p) when many windows
     * genuinely match (for example, all-equal text and pattern) or collide. The base and modulus
     * are fixed, not randomized, so the running time is input-dependent rather than expected, and
     * crafted input can force the worst case. O(1) extra space besides the result.
     */
    public static List<Integer> rabinKarpFindAll(String text, String pattern) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
        int patternLength = pattern.length();
        if (patternLength == 0) {
            return boundaries(text.length());
        }
        if (patternLength > text.length()) {
            return List.of();
        }
        long highPower = 1; // BASE^(p-1) mod MODULUS
        for (int index = 1; index < patternLength; index++) {
            highPower = highPower * RABIN_KARP_BASE % RABIN_KARP_MODULUS;
        }
        long patternHash = 0;
        long windowHash = 0;
        for (int index = 0; index < patternLength; index++) {
            patternHash = (patternHash * RABIN_KARP_BASE + pattern.charAt(index)) % RABIN_KARP_MODULUS;
            windowHash = (windowHash * RABIN_KARP_BASE + text.charAt(index)) % RABIN_KARP_MODULUS;
        }
        List<Integer> matches = new ArrayList<>();
        for (int start = 0; ; start++) {
            if (windowHash == patternHash && text.startsWith(pattern, start)) {
                matches.add(start);
            }
            if (start + patternLength >= text.length()) {
                break;
            }
            long outgoing = text.charAt(start) * highPower % RABIN_KARP_MODULUS;
            windowHash = (windowHash - outgoing + RABIN_KARP_MODULUS) % RABIN_KARP_MODULUS;
            windowHash = (windowHash * RABIN_KARP_BASE + text.charAt(start + patternLength))
                    % RABIN_KARP_MODULUS;
        }
        return List.copyOf(matches);
    }

    /**
     * Returns the longest palindromic substring as a half-open span, using Manacher's algorithm on
     * UTF-16 units (a surrogate pair is two units, so a palindrome is judged unit by unit). Ties
     * go to the earliest start. The empty text gives {@code Span(0, 0)}; any non-empty text gives
     * length at least one. O(n) time and O(n) space.
     */
    public static Span longestPalindromicSubstring(String text) {
        Objects.requireNonNull(text, "text");
        int length = text.length();
        int bestStart = 0;
        int bestLength = 0;
        // odd[i]: palindromes centered on i have radii 1..odd[i]; length 2 * odd[i] - 1
        int[] odd = new int[length];
        for (int index = 0, left = 0, right = -1; index < length; index++) {
            int radius = index > right ? 1 : Math.min(odd[left + right - index], right - index + 1);
            while (index - radius >= 0 && index + radius < length
                    && text.charAt(index - radius) == text.charAt(index + radius)) {
                radius++;
            }
            odd[index] = radius;
            if (index + radius - 1 > right) {
                left = index - radius + 1;
                right = index + radius - 1;
            }
            int start = index - radius + 1;
            int span = 2 * radius - 1;
            if (span > bestLength || span == bestLength && start < bestStart) {
                bestStart = start;
                bestLength = span;
            }
        }
        // even[i]: palindromes centered between i - 1 and i have lengths 2 * 1..even[i]
        int[] even = new int[length];
        for (int index = 0, left = 0, right = -1; index < length; index++) {
            int radius = index > right ? 0 : Math.min(even[left + right - index + 1], right - index + 1);
            while (index - radius - 1 >= 0 && index + radius < length
                    && text.charAt(index - radius - 1) == text.charAt(index + radius)) {
                radius++;
            }
            even[index] = radius;
            if (index + radius - 1 > right) {
                left = index - radius;
                right = index + radius - 1;
            }
            int start = index - radius;
            int span = 2 * radius;
            if (span > bestLength || span == bestLength && span > 0 && start < bestStart) {
                bestStart = start;
                bestLength = span;
            }
        }
        return new Span(bestStart, bestStart + bestLength);
    }

    private static List<Integer> boundaries(int textLength) {
        List<Integer> matches = new ArrayList<>(textLength + 1);
        for (int index = 0; index <= textLength; index++) {
            matches.add(index);
        }
        return List.copyOf(matches);
    }

    private static int[] prefixLengths(String pattern) {
        int[] prefix = new int[pattern.length()];
        for (int index = 1; index < pattern.length(); index++) {
            int length = prefix[index - 1];
            while (length > 0 && pattern.charAt(index) != pattern.charAt(length)) {
                length = prefix[length - 1];
            }
            if (pattern.charAt(index) == pattern.charAt(length)) {
                length++;
            }
            prefix[index] = length;
        }
        return prefix;
    }
}
