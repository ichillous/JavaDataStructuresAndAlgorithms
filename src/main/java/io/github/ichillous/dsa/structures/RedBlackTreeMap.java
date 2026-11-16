package io.github.ichillous.dsa.structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An ordered map backed by a left-leaning red-black tree (Sedgewick's variant) whose
 * nodes also store their subtree sizes, so order statistics run in logarithmic time.
 *
 * <p><b>Invariants.</b> Keys are in binary-search-tree order. Every red node is the
 * left child of a black node, so red links lean left and no red node has a red child.
 * Every path from the root to an empty link passes the same number of black nodes,
 * and the root is black. Read as a 2-3 tree (a red node merges with its black parent
 * into a 3-node), the tree is perfectly balanced, so a tree with {@code n} keys has
 * at most {@code 2 log2(n + 1)} nodes on any root-to-leaf path. Each node also stores
 * the number of keys in its subtree.
 *
 * <p><b>Keys and values.</b> Keys and values must be non-null; every method that takes
 * a key rejects {@code null} with {@link NullPointerException}. Two keys are the same
 * key when {@code compareTo} returns zero, as in {@link java.util.TreeMap}. Putting an
 * equivalent key replaces the value and keeps the key object stored first. Keys must
 * not change their ordering while stored. The map stores references to the caller's
 * keys and values and does not copy them.
 *
 * <p><b>Absent results.</b> {@link #get}, {@link #put} and {@link #remove} return
 * {@code null} for "absent", following {@link java.util.Map}. Nearest-key and endpoint
 * queries return {@link Optional#empty()}.
 *
 * <p><b>Costs.</b> With {@code n} stored keys, lookups, updates, floor, ceiling,
 * rank, select and range counts take O(log n) worst-case time. Queries are iterative
 * and use O(1) extra space. {@code put} and {@code remove} recurse along one search
 * path, using O(log n) stack. The map stores O(n) nodes. {@link #keys()} and
 * {@link #height()} take O(n) time.
 *
 * <p>The map holds at most {@link Integer#MAX_VALUE} keys, so subtree sizes never
 * overflow. This class is not thread safe.
 *
 * @param <K> key type with a natural ordering consistent for the lifetime of the map
 * @param <V> value type
 */
public final class RedBlackTreeMap<K extends Comparable<? super K>, V> {
    private Node<K, V> root;

    /** Creates an empty map. */
    public RedBlackTreeMap() {
    }

    /**
     * Associates {@code value} with {@code key}, replacing any previous value.
     * Replacing a value keeps the stored key object and does not restructure the tree.
     * Inserting a new key may rotate and recolor nodes on its search path.
     * Runs in O(log n) time with O(log n) recursion depth.
     *
     * @param key non-null key
     * @param value non-null value
     * @return the previous value, or {@code null} if the key was absent
     * @throws NullPointerException if {@code key} or {@code value} is null; the map is unchanged
     * @throws IllegalStateException if the key is new and the map already holds
     *     {@link Integer#MAX_VALUE} keys
     */
    public V put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Node<K, V> existing = find(key);
        if (existing != null) {
            V previous = existing.value;
            existing.value = value;
            return previous;
        }
        if (size() == Integer.MAX_VALUE) {
            throw new IllegalStateException("map already holds Integer.MAX_VALUE keys");
        }
        root = insert(root, key, value);
        root.red = false;
        return null;
    }

    /**
     * Returns the value stored for {@code key}, in O(log n) time and O(1) space.
     *
     * @param key non-null key
     * @return the stored value, or {@code null} if the key is absent
     * @throws NullPointerException if {@code key} is null
     */
    public V get(K key) {
        Objects.requireNonNull(key, "key");
        Node<K, V> node = find(key);
        return node == null ? null : node.value;
    }

    /**
     * Returns whether an equivalent key is stored, in O(log n) time and O(1) space.
     *
     * @param key non-null key
     * @return {@code true} if the key is present
     * @throws NullPointerException if {@code key} is null
     */
    public boolean containsKey(K key) {
        Objects.requireNonNull(key, "key");
        return find(key) != null;
    }

    /**
     * Removes the mapping for {@code key}. Removing an absent key leaves the map
     * unchanged. Runs in O(log n) time with O(log n) recursion depth.
     *
     * @param key non-null key
     * @return the removed value, or {@code null} if the key was absent
     * @throws NullPointerException if {@code key} is null
     */
    public V remove(K key) {
        Objects.requireNonNull(key, "key");
        Node<K, V> existing = find(key);
        if (existing == null) {
            return null;
        }
        V previous = existing.value;
        // Make the root part of a 3-node so the descent never starts at a 2-node.
        // Red links lean left, so at rest root.right is never red and only root.left decides.
        if (!isRed(root.left)) {
            root.red = true;
        }
        root = delete(root, key);
        if (root != null) {
            root.red = false;
        }
        return previous;
    }

    /**
     * Returns the number of stored keys, in O(1) time.
     *
     * @return the number of keys, from 0 to {@link Integer#MAX_VALUE}
     */
    public int size() {
        return size(root);
    }

    /**
     * Returns whether the map holds no keys, in O(1) time.
     *
     * @return {@code true} if the map is empty
     */
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Returns the smallest key, in O(log n) time and O(1) space.
     *
     * @return the smallest key, or {@link Optional#empty()} if the map is empty
     */
    public Optional<K> firstKey() {
        if (root == null) {
            return Optional.empty();
        }
        Node<K, V> node = root;
        while (node.left != null) {
            node = node.left;
        }
        return Optional.of(node.key);
    }

    /**
     * Returns the largest key, in O(log n) time and O(1) space.
     *
     * @return the largest key, or {@link Optional#empty()} if the map is empty
     */
    public Optional<K> lastKey() {
        if (root == null) {
            return Optional.empty();
        }
        Node<K, V> node = root;
        while (node.right != null) {
            node = node.right;
        }
        return Optional.of(node.key);
    }

    /**
     * Returns the largest stored key less than or equal to {@code key}, which need not
     * be present. Runs in O(log n) time and O(1) space.
     *
     * @param key non-null probe key
     * @return the floor key, or {@link Optional#empty()} if every stored key is greater
     * @throws NullPointerException if {@code key} is null
     */
    public Optional<K> floorKey(K key) {
        Objects.requireNonNull(key, "key");
        Node<K, V> node = root;
        K best = null;
        while (node != null) {
            int comparison = key.compareTo(node.key);
            if (comparison == 0) {
                return Optional.of(node.key);
            }
            if (comparison < 0) {
                node = node.left;
            } else {
                best = node.key;
                node = node.right;
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * Returns the smallest stored key greater than or equal to {@code key}, which need
     * not be present. Runs in O(log n) time and O(1) space.
     *
     * @param key non-null probe key
     * @return the ceiling key, or {@link Optional#empty()} if every stored key is smaller
     * @throws NullPointerException if {@code key} is null
     */
    public Optional<K> ceilingKey(K key) {
        Objects.requireNonNull(key, "key");
        Node<K, V> node = root;
        K best = null;
        while (node != null) {
            int comparison = key.compareTo(node.key);
            if (comparison == 0) {
                return Optional.of(node.key);
            }
            if (comparison > 0) {
                node = node.right;
            } else {
                best = node.key;
                node = node.left;
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * Returns the number of stored keys strictly less than {@code key}. The key need
     * not be present; the result is then its insertion position in {@link #keys()}.
     * When the key is present, {@code select(rank(key))} returns it. Each step down the
     * tree adds the size of a left subtree it skips. Runs in O(log n) time and O(1) space.
     *
     * @param key non-null probe key
     * @return a rank from 0 to {@link #size()}; 0 for an empty map
     * @throws NullPointerException if {@code key} is null
     */
    public int rank(K key) {
        Objects.requireNonNull(key, "key");
        int smaller = 0;
        Node<K, V> node = root;
        while (node != null) {
            int comparison = key.compareTo(node.key);
            if (comparison < 0) {
                node = node.left;
            } else if (comparison > 0) {
                smaller += 1 + size(node.left);
                node = node.right;
            } else {
                return smaller + size(node.left);
            }
        }
        return smaller;
    }

    /**
     * Returns the key with exactly {@code rank} smaller keys, the same key as
     * {@code keys().get(rank)}. Runs in O(log n) time and O(1) space.
     *
     * @param rank zero-based position in ascending key order
     * @return the key at that position
     * @throws IndexOutOfBoundsException if {@code rank} is outside {@code [0, size())},
     *     which includes every rank of an empty map
     */
    public K select(int rank) {
        Objects.checkIndex(rank, size());
        Node<K, V> node = root;
        int remaining = rank;
        while (true) {
            int leftSize = size(node.left);
            if (remaining < leftSize) {
                node = node.left;
            } else if (remaining > leftSize) {
                remaining -= leftSize + 1;
                node = node.right;
            } else {
                return node.key;
            }
        }
    }

    /**
     * Counts stored keys in the half-open range {@code [fromInclusive, toExclusive)},
     * as {@code rank(toExclusive) - rank(fromInclusive)}. Both ranks lie in
     * {@code [0, size()]}, so the subtraction cannot overflow. An empty or reversed
     * range ({@code fromInclusive >= toExclusive}) counts 0 rather than throwing.
     * Runs in O(log n) time and O(1) space.
     *
     * @param fromInclusive non-null lower bound, included
     * @param toExclusive non-null upper bound, excluded
     * @return the number of keys {@code k} with {@code fromInclusive <= k < toExclusive}
     * @throws NullPointerException if either bound is null
     */
    public int countInRange(K fromInclusive, K toExclusive) {
        Objects.requireNonNull(fromInclusive, "fromInclusive");
        Objects.requireNonNull(toExclusive, "toExclusive");
        if (fromInclusive.compareTo(toExclusive) >= 0) {
            return 0;
        }
        return rank(toExclusive) - rank(fromInclusive);
    }

    /**
     * Returns the keys in ascending order as an immutable snapshot. Later changes to
     * the map do not affect the returned list. Runs in O(n) time with O(n) output space
     * and an O(log n) explicit traversal stack.
     *
     * @return an immutable ascending list of the stored keys; empty for an empty map
     */
    public List<K> keys() {
        List<K> keys = new ArrayList<>(size());
        Deque<Node<K, V>> path = new ArrayDeque<>();
        Node<K, V> node = root;
        while (node != null || !path.isEmpty()) {
            while (node != null) {
                path.push(node);
                node = node.left;
            }
            node = path.pop();
            keys.add(node.key);
            node = node.right;
        }
        return List.copyOf(keys);
    }

    /**
     * Returns the tree height counted in edges: 0 for one key and -1 for an empty map.
     * The height is at most {@code 2 log2(n + 1) - 1}. It is not cached, so this takes
     * O(n) time and O(log n) recursion depth.
     *
     * @return the number of edges on the longest root-to-leaf path, or -1 when empty
     */
    public int height() {
        return height(root);
    }

    /**
     * Checks every structural invariant independently of the update code, for tests.
     * Takes O(n) time and recursion as deep as the tree.
     *
     * @throws IllegalStateException on a BST order violation, a red root, a red node
     *     with a red child, a right-leaning red link, unequal black heights or a wrong
     *     stored subtree size
     */
    void checkInvariants() {
        if (isRed(root)) {
            throw new IllegalStateException("the root is red");
        }
        blackHeight(root, null, null);
    }

    /** Exposes the root so invariant tests can corrupt a tree on purpose. */
    Node<K, V> root() {
        return root;
    }

    private Node<K, V> find(K key) {
        Node<K, V> node = root;
        while (node != null) {
            int comparison = key.compareTo(node.key);
            if (comparison == 0) {
                return node;
            }
            node = comparison < 0 ? node.left : node.right;
        }
        return null;
    }

    // The caller has checked that the key is absent, so no comparison returns zero.
    private Node<K, V> insert(Node<K, V> node, K key, V value) {
        if (node == null) {
            return new Node<>(key, value);
        }
        if (key.compareTo(node.key) < 0) {
            node.left = insert(node.left, key, value);
        } else {
            node.right = insert(node.right, key, value);
        }
        return balance(node);
    }

    // The key is present below node. On the way down, node or node.left is red (node is
    // not a 2-node), so the key is finally removed from a 3-node or 4-node at the bottom.
    private Node<K, V> delete(Node<K, V> node, K key) {
        if (key.compareTo(node.key) < 0) {
            if (!isRed(node.left) && !isRed(node.left.left)) {
                node = moveRedLeft(node);
            }
            node.left = delete(node.left, key);
        } else {
            if (isRed(node.left)) {
                node = rotateRight(node);
            }
            if (key.compareTo(node.key) == 0 && node.right == null) {
                return null;
            }
            if (!isRed(node.right) && !isRed(node.right.left)) {
                node = moveRedRight(node);
            }
            if (key.compareTo(node.key) == 0) {
                Node<K, V> successor = node.right;
                while (successor.left != null) {
                    successor = successor.left;
                }
                node.key = successor.key;
                node.value = successor.value;
                node.right = deleteMin(node.right);
            } else {
                node.right = delete(node.right, key);
            }
        }
        return balance(node);
    }

    private Node<K, V> deleteMin(Node<K, V> node) {
        if (node.left == null) {
            return null;
        }
        if (!isRed(node.left) && !isRed(node.left.left)) {
            node = moveRedLeft(node);
        }
        node.left = deleteMin(node.left);
        return balance(node);
    }

    // node is red and node.left, node.left.left are black: borrow from the right sibling
    // or merge with it, so that node.left or one of its children becomes red.
    private Node<K, V> moveRedLeft(Node<K, V> node) {
        flipColors(node);
        if (isRed(node.right.left)) {
            node.right = rotateRight(node.right);
            node = rotateLeft(node);
            flipColors(node);
        }
        return node;
    }

    // node is red and node.right, node.right.left are black: the mirror of moveRedLeft.
    private Node<K, V> moveRedRight(Node<K, V> node) {
        flipColors(node);
        if (isRed(node.left.left)) {
            node = rotateRight(node);
            flipColors(node);
        }
        return node;
    }

    // Restores the left-leaning invariants on the way back up and refreshes the size.
    private Node<K, V> balance(Node<K, V> node) {
        if (isRed(node.right) && !isRed(node.left)) {
            node = rotateLeft(node);
        }
        if (isRed(node.left) && isRed(node.left.left)) {
            node = rotateRight(node);
        }
        if (isRed(node.left) && isRed(node.right)) {
            flipColors(node);
        }
        node.size = 1 + size(node.left) + size(node.right);
        return node;
    }

    // Turns a right-leaning red link into a left-leaning one; in-order sequence is unchanged.
    private Node<K, V> rotateLeft(Node<K, V> node) {
        Node<K, V> pivot = node.right;
        node.right = pivot.left;
        pivot.left = node;
        pivot.red = node.red;
        node.red = true;
        pivot.size = node.size;
        node.size = 1 + size(node.left) + size(node.right);
        return pivot;
    }

    // Turns a left-leaning red link into a right-leaning one; in-order sequence is unchanged.
    private Node<K, V> rotateRight(Node<K, V> node) {
        Node<K, V> pivot = node.left;
        node.left = pivot.right;
        pivot.right = node;
        pivot.red = node.red;
        node.red = true;
        pivot.size = node.size;
        node.size = 1 + size(node.left) + size(node.right);
        return pivot;
    }

    // Splits a temporary 4-node (insertion) or forms one (deletion) by toggling colors.
    private static void flipColors(Node<?, ?> node) {
        node.red = !node.red;
        node.left.red = !node.left.red;
        node.right.red = !node.right.red;
    }

    private static boolean isRed(Node<?, ?> node) {
        return node != null && node.red;
    }

    private static int size(Node<?, ?> node) {
        return node == null ? 0 : node.size;
    }

    private static int height(Node<?, ?> node) {
        return node == null ? -1 : 1 + Math.max(height(node.left), height(node.right));
    }

    // Returns the number of black nodes on every path from node to an empty link.
    private int blackHeight(Node<K, V> node, K lower, K upper) {
        if (node == null) {
            return 0;
        }
        if ((lower != null && node.key.compareTo(lower) <= 0)
                || (upper != null && node.key.compareTo(upper) >= 0)) {
            throw new IllegalStateException("BST order violated at key " + node.key);
        }
        if (node.red && (isRed(node.left) || isRed(node.right))) {
            throw new IllegalStateException("red node " + node.key + " has a red child");
        }
        if (isRed(node.right)) {
            throw new IllegalStateException("right-leaning red link below key " + node.key);
        }
        int left = blackHeight(node.left, lower, node.key);
        int right = blackHeight(node.right, node.key, upper);
        if (left != right) {
            throw new IllegalStateException("unequal black height below key " + node.key);
        }
        if (node.size != 1 + size(node.left) + size(node.right)) {
            throw new IllegalStateException("wrong subtree size at key " + node.key);
        }
        return left + (node.red ? 0 : 1);
    }

    /** A tree node; package-private only so invariant tests can build corrupt trees. */
    static final class Node<K, V> {
        K key;
        V value;
        Node<K, V> left;
        Node<K, V> right;
        boolean red = true;
        int size = 1;

        private Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
