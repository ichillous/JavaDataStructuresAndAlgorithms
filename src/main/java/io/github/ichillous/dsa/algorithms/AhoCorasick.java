package io.github.ichillous.dsa.algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

/**
 * Multi-pattern exact string matching with an Aho-Corasick automaton.
 *
 * <p>Construction copies the patterns into a trie, so later changes to the caller's collection
 * have no effect. Duplicate patterns collapse to one in the trie, and an empty collection yields an
 * automaton with no matches. The empty pattern is rejected, because it
 * would match at every boundary and has no useful start-and-pattern identity.
 * Positions are UTF-16 code-unit indices; no Unicode normalization or case folding occurs.
 * Instances are immutable after construction and safe to share between threads.
 *
 * <p>Invariant: for every trie node {@code v} spelling string {@code s}, its failure link is the
 * node for the longest proper suffix of {@code s} that is also a trie prefix, and its output link
 * is the nearest node along the failure chain that ends a pattern. Failure links are built
 * breadth first with a queue, so shallower nodes are finished first and nothing recurses on
 * pattern length.
 *
 * <p>Costs, for total pattern length {@code P}, text length {@code t} and {@code k} matches:
 * construction is O(P) expected time (child lookup uses a hash map, assumed O(1)) and O(P)
 * space; {@link #findAll} is O(t + k) expected time and O(t + k) auxiliary space, because each
 * text position moves one state forward with amortized-constant failure steps and each match is
 * emitted once.
 */
public final class AhoCorasick {
    /**
     * One occurrence of a pattern in a text.
     *
     * @param startInclusive index of the first matched code unit
     * @param pattern the matched pattern (never null or empty)
     */
    public record Match(int startInclusive, String pattern) {
        /** Rejects a negative start or a null or empty pattern. */
        public Match {
            Objects.requireNonNull(pattern, "pattern");
            if (startInclusive < 0 || pattern.isEmpty()) {
                throw new IllegalArgumentException("a match needs start >= 0 and a nonempty pattern");
            }
        }
    }

    private final List<Map<Character, Integer>> children = new ArrayList<>();
    private final List<String> terminal = new ArrayList<>();
    private int[] failure;
    private int[] output;

    /**
     * Builds the automaton.
     *
     * @param patterns patterns, each nonempty; copied, duplicates collapse, and an empty
     *     collection is allowed (every {@link #findAll} result is then empty)
     * @throws NullPointerException if the collection or any element is null
     * @throws IllegalArgumentException if any pattern is empty
     */
    public AhoCorasick(Collection<String> patterns) {
        Objects.requireNonNull(patterns, "patterns");
        List<String> copy = new ArrayList<>(patterns);
        for (String pattern : copy) {
            Objects.requireNonNull(pattern, "pattern");
            if (pattern.isEmpty()) {
                throw new IllegalArgumentException("patterns must be nonempty");
            }
        }
        newNode();
        for (String pattern : copy) {
            int node = 0;
            for (int i = 0; i < pattern.length(); i++) {
                char c = pattern.charAt(i);
                Integer next = children.get(node).get(c);
                if (next == null) {
                    next = newNode();
                    children.get(node).put(c, next);
                }
                node = next;
            }
            terminal.set(node, pattern);
        }
        buildLinks();
    }

    private int newNode() {
        children.add(new HashMap<>());
        terminal.add(null);
        return children.size() - 1;
    }

    private void buildLinks() {
        int size = children.size();
        failure = new int[size];
        output = new int[size];
        Queue<Integer> queue = new ArrayDeque<>(children.get(0).values());
        while (!queue.isEmpty()) {
            int node = queue.remove();
            for (Map.Entry<Character, Integer> edge : children.get(node).entrySet()) {
                int child = edge.getValue();
                int fallback = failure[node];
                while (fallback != 0 && !children.get(fallback).containsKey(edge.getKey())) {
                    fallback = failure[fallback];
                }
                Integer target = children.get(fallback).get(edge.getKey());
                int link = target == null ? 0 : target;
                failure[child] = link;
                output[child] = terminal.get(link) != null ? link : output[link];
                queue.add(child);
            }
        }
    }

    /**
     * Returns every occurrence of every pattern in {@code text}, overlaps included, as an
     * immutable list ordered by start index and then by pattern length ascending (distinct
     * patterns with equal start have distinct lengths). An empty text has no matches.
     * O(t + k) expected time: the result is bucketed by start with a stable counting pass
     * rather than comparison-sorted.
     *
     * @throws NullPointerException if text is null
     */
    public List<Match> findAll(String text) {
        Objects.requireNonNull(text, "text");
        // Matches are discovered in order of end index, so for a fixed start the shorter
        // pattern is found first; a stable bucket pass by start therefore yields the rule.
        List<String> found = new ArrayList<>();
        List<Integer> starts = new ArrayList<>();
        int[] offsets = new int[text.length() + 1];
        int state = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (state != 0 && !children.get(state).containsKey(c)) {
                state = failure[state];
            }
            Integer next = children.get(state).get(c);
            state = next == null ? 0 : next;
            int node = terminal.get(state) != null ? state : output[state];
            while (node != 0) {
                String pattern = terminal.get(node);
                int start = i + 1 - pattern.length();
                found.add(pattern);
                starts.add(start);
                offsets[start + 1]++;
                node = output[node];
            }
        }
        for (int s = 0; s < text.length(); s++) {
            offsets[s + 1] += offsets[s];
        }
        Match[] ordered = new Match[found.size()];
        for (int m = 0; m < ordered.length; m++) {
            int start = starts.get(m);
            ordered[offsets[start]++] = new Match(start, found.get(m));
        }
        return List.of(ordered);
    }
}
