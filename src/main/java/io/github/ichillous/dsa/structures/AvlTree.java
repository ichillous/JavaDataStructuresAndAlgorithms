package io.github.ichillous.dsa.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An AVL ordered set: every node's child heights differ by at most one.
 * Rotations preserve sorted order while keeping search, insertion and deletion O(log n).
 * Keys are distinct according to {@link Comparable#compareTo}; null keys are rejected.
 * This class is not thread safe.
 *
 * @param <T> key type with a consistent natural ordering
 */
public final class AvlTree<T extends Comparable<? super T>> {
    private Node<T> root;
    private int size;

    /** Adds a key, returning false for an equivalent existing key. */
    public boolean add(T key) {
        Objects.requireNonNull(key, "key");
        if (contains(key)) {
            return false;
        }
        root = insert(root, key);
        size++;
        return true;
    }

    /** Removes a key, returning false when absent. */
    public boolean remove(T key) {
        Objects.requireNonNull(key, "key");
        if (!contains(key)) {
            return false;
        }
        root = delete(root, key);
        size--;
        return true;
    }

    /** Returns whether an equivalent key is present. */
    public boolean contains(T key) {
        Objects.requireNonNull(key, "key");
        Node<T> node = root;
        while (node != null) {
            int comparison = key.compareTo(node.key);
            if (comparison == 0) {
                return true;
            }
            node = comparison < 0 ? node.left : node.right;
        }
        return false;
    }

    /** Returns an immutable sorted snapshot in O(n) time and O(n) output space. */
    public List<T> inOrder() {
        List<T> keys = new ArrayList<>(size);
        visit(root, keys);
        return List.copyOf(keys);
    }

    /** Returns height in edges in O(1) time; an empty tree has height -1. */
    public int height() {
        return height(root) - 1;
    }

    /** Returns the number of distinct keys. */
    public int size() {
        return size;
    }

    /** Returns whether the tree contains no keys. */
    public boolean isEmpty() {
        return size == 0;
    }

    private Node<T> insert(Node<T> node, T key) {
        if (node == null) {
            return new Node<>(key);
        }
        if (key.compareTo(node.key) < 0) {
            node.left = insert(node.left, key);
        } else {
            node.right = insert(node.right, key);
        }
        return rebalance(node);
    }

    private Node<T> delete(Node<T> node, T key) {
        int comparison = key.compareTo(node.key);
        if (comparison < 0) {
            node.left = delete(node.left, key);
        } else if (comparison > 0) {
            node.right = delete(node.right, key);
        } else {
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            Node<T> successor = node.right;
            while (successor.left != null) {
                successor = successor.left;
            }
            node.key = successor.key;
            node.right = delete(node.right, successor.key);
        }
        return rebalance(node);
    }

    private Node<T> rebalance(Node<T> node) {
        updateHeight(node);
        int balance = height(node.left) - height(node.right);
        if (balance > 1) {
            if (height(node.left.left) < height(node.left.right)) {
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        }
        if (balance < -1) {
            if (height(node.right.right) < height(node.right.left)) {
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }
        return node;
    }

    private Node<T> rotateRight(Node<T> node) {
        Node<T> pivot = node.left;
        node.left = pivot.right;
        pivot.right = node;
        updateHeight(node);
        updateHeight(pivot);
        return pivot;
    }

    private Node<T> rotateLeft(Node<T> node) {
        Node<T> pivot = node.right;
        node.right = pivot.left;
        pivot.left = node;
        updateHeight(node);
        updateHeight(pivot);
        return pivot;
    }

    private static int height(Node<?> node) {
        return node == null ? 0 : node.height;
    }

    private static void updateHeight(Node<?> node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    private void visit(Node<T> node, List<T> keys) {
        if (node != null) {
            visit(node.left, keys);
            keys.add(node.key);
            visit(node.right, keys);
        }
    }

    // Checks ordering, cached heights and balance independently for the test suite.
    boolean validateInvariants() {
        return validate(root, null, null) >= 0;
    }

    private int validate(Node<T> node, T lower, T upper) {
        if (node == null) {
            return 0;
        }
        if ((lower != null && node.key.compareTo(lower) <= 0)
                || (upper != null && node.key.compareTo(upper) >= 0)) {
            return -1;
        }
        int leftHeight = validate(node.left, lower, node.key);
        int rightHeight = validate(node.right, node.key, upper);
        if (leftHeight < 0 || rightHeight < 0 || Math.abs(leftHeight - rightHeight) > 1
                || node.height != 1 + Math.max(leftHeight, rightHeight)) {
            return -1;
        }
        return node.height;
    }

    private static final class Node<T> {
        private T key;
        private Node<T> left;
        private Node<T> right;
        private int height = 1;

        private Node(T key) {
            this.key = key;
        }
    }
}
