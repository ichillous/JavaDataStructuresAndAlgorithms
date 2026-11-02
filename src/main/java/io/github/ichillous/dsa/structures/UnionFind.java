package io.github.ichillous.dsa.structures;

/**
 * Disjoint sets over indices [0, size), using union by size and iterative path compression.
 * A sequence of m operations costs O(m alpha(n)) amortized time after O(n) initialization;
 * alpha is the inverse Ackermann function. Storage is O(n). This class is not thread safe.
 */
public final class UnionFind {
    private final int[] parent;
    private final int[] componentSizes;
    private int components;

    /** Creates one component per index; negative sizes are rejected. */
    public UnionFind(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must be nonnegative");
        }
        parent = new int[size];
        componentSizes = new int[size];
        components = size;
        for (int i = 0; i < size; i++) {
            parent[i] = i;
            componentSizes[i] = 1;
        }
    }

    /** Returns a representative, compressing the path. Invalid indices throw IndexOutOfBoundsException. */
    public int find(int element) {
        checkIndex(element);
        int root = element;
        while (parent[root] != root) {
            root = parent[root];
        }
        while (parent[element] != element) {
            int next = parent[element];
            parent[element] = root;
            element = next;
        }
        return root;
    }

    /** Merges two components, returning false if they were already connected. */
    public boolean union(int first, int second) {
        // Validate both before path compression, so invalid input cannot partially mutate the forest.
        checkIndex(first);
        checkIndex(second);
        int firstRoot = find(first);
        int secondRoot = find(second);
        if (firstRoot == secondRoot) {
            return false;
        }
        if (componentSizes[firstRoot] < componentSizes[secondRoot]) {
            int swap = firstRoot;
            firstRoot = secondRoot;
            secondRoot = swap;
        }
        parent[secondRoot] = firstRoot;
        componentSizes[firstRoot] += componentSizes[secondRoot];
        components--;
        return true;
    }

    /** Returns whether two indices are in the same component. */
    public boolean connected(int first, int second) {
        checkIndex(first);
        checkIndex(second);
        return find(first) == find(second);
    }

    /** Returns the size of the component containing an index. */
    public int componentSize(int element) {
        return componentSizes[find(element)];
    }

    /** Returns the number of current components in O(1) time. */
    public int components() {
        return components;
    }

    /** Returns the number of indices in O(1) time. */
    public int size() {
        return parent.length;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= parent.length) {
            throw new IndexOutOfBoundsException("index " + index + " outside [0, " + parent.length + ")");
        }
    }
}
