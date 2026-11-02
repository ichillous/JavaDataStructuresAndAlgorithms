package io.github.ichillous.dsa.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Knuth–Morris–Pratt matching with UTF-16 indices, matching {@link String#indexOf(String)}. */
public final class StringAlgorithms {
    private StringAlgorithms() {}

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
