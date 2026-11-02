package io.github.ichillous.dsa.structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * A case-sensitive set of strings represented by a prefix tree of UTF-16 code units.
 * All Java strings, including the empty string, are supported; no normalization is performed.
 * Ordered child maps make prefix results follow {@link String#compareTo} ordering.
 * Lookup/update take O(L log A) time for L code units and at most A children per node.
 * Nulls are rejected. This class is not thread safe.
 */
public final class Trie {
    private final Node root = new Node();
    private int size;

    /** Adds a word, returning false when already present. */
    public boolean add(String word) {
        Objects.requireNonNull(word, "word");
        Node node = root;
        for (int i = 0; i < word.length(); i++) {
            node = node.children.computeIfAbsent(word.charAt(i), ignored -> new Node());
        }
        if (node.word) {
            return false;
        }
        node.word = true;
        size++;
        return true;
    }

    /** Returns whether the exact word is present. */
    public boolean contains(String word) {
        Node node = find(word);
        return node != null && node.word;
    }

    /** Returns whether a prefix path exists. The empty prefix always exists, including in an empty trie. */
    public boolean startsWith(String prefix) {
        return find(prefix) != null;
    }

    /** Removes a word and prunes unused nodes, preserving words with a shared prefix. */
    public boolean remove(String word) {
        Objects.requireNonNull(word, "word");
        List<Node> path = new ArrayList<>(word.length() + 1);
        Node node = root;
        path.add(node);
        for (int i = 0; i < word.length(); i++) {
            node = node.children.get(word.charAt(i));
            if (node == null) {
                return false;
            }
            path.add(node);
        }
        if (!node.word) {
            return false;
        }
        node.word = false;
        size--;
        for (int i = word.length() - 1; i >= 0; i--) {
            Node child = path.get(i + 1);
            if (child.word || !child.children.isEmpty()) {
                break;
            }
            path.get(i).children.remove(word.charAt(i));
        }
        return true;
    }

    /**
     * Returns at most limit words in lexicographic order as an immutable snapshot.
     * Negative limits are rejected; zero returns an empty list. Uses iterative traversal,
     * O(prefix length + visited nodes + output characters) space and traversal work,
     * plus the O(prefix length * log A) prefix lookup.
     */
    public List<String> wordsWithPrefix(String prefix, int limit) {
        Objects.requireNonNull(prefix, "prefix");
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be nonnegative");
        }
        Node start = find(prefix);
        if (start == null || limit == 0) {
            return List.of();
        }
        List<String> words = new ArrayList<>();
        StringBuilder word = new StringBuilder(prefix);
        if (start.word) {
            words.add(prefix);
        }
        ArrayDeque<Frame> path = new ArrayDeque<>();
        path.push(new Frame(start.children.entrySet().iterator(), word.length()));
        while (!path.isEmpty() && words.size() < limit) {
            Frame frame = path.peek();
            if (!frame.children.hasNext()) {
                path.pop();
                if (!path.isEmpty()) {
                    word.setLength(path.peek().length);
                }
                continue;
            }
            Map.Entry<Character, Node> child = frame.children.next();
            word.append(child.getKey().charValue());
            if (child.getValue().word) {
                words.add(word.toString());
            }
            path.push(new Frame(child.getValue().children.entrySet().iterator(), word.length()));
        }
        return List.copyOf(words);
    }

    /** Returns the number of distinct words. */
    public int size() {
        return size;
    }

    /** Returns whether no words are stored. */
    public boolean isEmpty() {
        return size == 0;
    }

    private Node find(String text) {
        Objects.requireNonNull(text, "text");
        Node node = root;
        for (int i = 0; i < text.length(); i++) {
            node = node.children.get(text.charAt(i));
            if (node == null) {
                return null;
            }
        }
        return node;
    }

    private static final class Node {
        private final TreeMap<Character, Node> children = new TreeMap<>();
        private boolean word;
    }

    private static final class Frame {
        private final Iterator<Map.Entry<Character, Node>> children;
        private final int length;

        private Frame(Iterator<Map.Entry<Character, Node>> children, int length) {
            this.children = children;
            this.length = length;
        }
    }
}
