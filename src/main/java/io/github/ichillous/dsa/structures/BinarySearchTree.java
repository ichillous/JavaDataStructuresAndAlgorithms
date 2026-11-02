package io.github.ichillous.dsa.structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An unbalanced ordered set. Keys are distinct according to {@link Comparable#compareTo}.
 * Search, insertion and deletion take O(h) time, where h can grow to n - 1.
 * All operations are iterative so a sorted insertion sequence does not overflow the call stack.
 * Null keys are rejected. This class is not thread safe.
 *
 * @param <T> key type with a consistent natural ordering
 */
public final class BinarySearchTree<T extends Comparable<? super T>> {
    private Node<T> root;
    private int size;

    /** Adds a key, returning false when an equivalent key already exists. */
    public boolean add(T key) {
        Objects.requireNonNull(key, "key");
        if (root == null) {
            root = new Node<>(key);
            size++;
            return true;
        }
        Node<T> node = root;
        while (true) {
            int comparison = key.compareTo(node.key);
            if (comparison == 0) {
                return false;
            }
            if (comparison < 0) {
                if (node.left == null) {
                    node.left = new Node<>(key);
                    break;
                }
                node = node.left;
            } else {
                if (node.right == null) {
                    node.right = new Node<>(key);
                    break;
                }
                node = node.right;
            }
        }
        size++;
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

    /** Removes a key, returning false if it was absent. */
    public boolean remove(T key) {
        Objects.requireNonNull(key, "key");
        Node<T> parent = null;
        Node<T> node = root;
        while (node != null && key.compareTo(node.key) != 0) {
            parent = node;
            node = key.compareTo(node.key) < 0 ? node.left : node.right;
        }
        if (node == null) {
            return false;
        }

        // For two children, copy the successor and unlink that successor instead.
        if (node.left != null && node.right != null) {
            Node<T> successorParent = node;
            Node<T> successor = node.right;
            while (successor.left != null) {
                successorParent = successor;
                successor = successor.left;
            }
            node.key = successor.key;
            parent = successorParent;
            node = successor;
        }
        Node<T> child = node.left != null ? node.left : node.right;
        if (parent == null) {
            root = child;
        } else if (parent.left == node) {
            parent.left = child;
        } else {
            parent.right = child;
        }
        size--;
        return true;
    }

    /** Returns an immutable sorted snapshot in O(n) time and O(n) space. */
    public List<T> inOrder() {
        List<T> keys = new ArrayList<>(size);
        ArrayDeque<Node<T>> path = new ArrayDeque<>();
        Node<T> node = root;
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

    /** Returns the height measured in edges; an empty tree has height -1. Takes O(n) time. */
    public int height() {
        if (root == null) {
            return -1;
        }
        ArrayDeque<Node<T>> level = new ArrayDeque<>();
        level.add(root);
        int height = -1;
        while (!level.isEmpty()) {
            int width = level.size();
            for (int i = 0; i < width; i++) {
                Node<T> node = level.remove();
                if (node.left != null) {
                    level.add(node.left);
                }
                if (node.right != null) {
                    level.add(node.right);
                }
            }
            height++;
        }
        return height;
    }

    /** Returns the number of distinct keys in O(1) time. */
    public int size() {
        return size;
    }

    /** Returns whether the tree contains no keys. */
    public boolean isEmpty() {
        return size == 0;
    }

    private static final class Node<T> {
        private T key;
        private Node<T> left;
        private Node<T> right;

        private Node(T key) {
            this.key = key;
        }
    }
}
