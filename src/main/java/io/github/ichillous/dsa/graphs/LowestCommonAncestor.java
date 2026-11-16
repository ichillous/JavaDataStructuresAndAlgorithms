package io.github.ichillous.dsa.graphs;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.OptionalInt;

/**
 * Lowest-common-ancestor, depth, distance and k-th-ancestor queries on a tree
 * rooted at a chosen vertex, using binary lifting.
 *
 * <p>Invariant: {@code up[j][v]} is the {@code 2^j}-th ancestor of {@code v}, with
 * the root as its own ancestor at every level. Any ancestor offset is a sum of
 * powers of two, so a query combines at most {@code log2(V) + 1} jumps.
 *
 * <p>The constructor requires an undirected tree: connected with exactly
 * {@code V - 1} edges (a self-loop makes the graph a non-tree), otherwise
 * {@link IllegalArgumentException}. The graph is read once and not retained, so
 * later edits do not affect this object. Instances are immutable and thread-safe.
 * Construction uses an iterative BFS (no recursion proportional to V), O(V log V)
 * time and O(V log V) space. Each query is O(log V) time and O(1) extra space.
 * Distances and depths are at most {@code V - 1}, so no arithmetic overflows.
 */
public final class LowestCommonAncestor {
    private final int[][] up;
    private final int[] depth;

    /**
     * Preprocesses {@code tree} rooted at {@code root}.
     *
     * @param tree an undirected tree
     * @param root the root vertex
     * @throws NullPointerException if {@code tree} is null
     * @throws IllegalArgumentException if {@code tree} is not an undirected tree
     *         (including the empty graph)
     * @throws IndexOutOfBoundsException if {@code root} is not a vertex
     */
    public LowestCommonAncestor(Graph tree, int root) {
        TreeValidation.requireTree(tree);
        tree.checkVertex(root);
        int vertices = tree.vertexCount();
        int levels = Math.max(1, 32 - Integer.numberOfLeadingZeros(vertices));
        up = new int[levels][vertices];
        depth = new int[vertices];
        boolean[] seen = new boolean[vertices];
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(root);
        seen[root] = true;
        up[0][root] = root;
        while (!queue.isEmpty()) {
            int v = queue.poll();
            for (int w : tree.neighbors(v)) {
                if (!seen[w]) {
                    seen[w] = true;
                    depth[w] = depth[v] + 1;
                    up[0][w] = v;
                    queue.add(w);
                }
            }
        }
        for (int j = 1; j < levels; j++) {
            for (int v = 0; v < vertices; v++) {
                up[j][v] = up[j - 1][up[j - 1][v]];
            }
        }
    }

    /**
     * Returns the number of edges from the root to {@code v}, in O(1) time.
     *
     * @param v a vertex
     * @return the depth of {@code v}; the root has depth 0
     * @throws IndexOutOfBoundsException if {@code v} is not a vertex
     */
    public int depth(int v) {
        check(v);
        return depth[v];
    }

    /**
     * Returns the deepest vertex that is an ancestor of both {@code a} and
     * {@code b} (a vertex is its own ancestor), in O(log V) time.
     *
     * @param a a vertex
     * @param b a vertex
     * @return the lowest common ancestor
     * @throws IndexOutOfBoundsException if either argument is not a vertex
     */
    public int lca(int a, int b) {
        check(a);
        check(b);
        int x = a;
        int y = b;
        if (depth[x] < depth[y]) {
            x = b;
            y = a;
        }
        x = lift(x, depth[x] - depth[y]);
        if (x == y) {
            return x;
        }
        for (int j = up.length - 1; j >= 0; j--) {
            if (up[j][x] != up[j][y]) {
                x = up[j][x];
                y = up[j][y];
            }
        }
        return up[0][x];
    }

    /**
     * Returns the number of edges on the unique path between {@code a} and
     * {@code b}, in O(log V) time.
     *
     * @param a a vertex
     * @param b a vertex
     * @return the path length in edges
     * @throws IndexOutOfBoundsException if either argument is not a vertex
     */
    public int distance(int a, int b) {
        return depth[a] + depth[b] - 2 * depth[lca(a, b)];
    }

    /**
     * Returns the {@code k}-th ancestor of {@code v} ({@code k = 0} is {@code v}
     * itself), or empty when {@code k > depth(v)}. O(log V) time.
     *
     * @param v a vertex
     * @param k the number of edges to climb
     * @return the ancestor, or empty if it would be above the root
     * @throws IllegalArgumentException if {@code k} is negative
     * @throws IndexOutOfBoundsException if {@code v} is not a vertex
     */
    public OptionalInt kthAncestor(int v, int k) {
        check(v);
        if (k < 0) {
            throw new IllegalArgumentException("k must be nonnegative: " + k);
        }
        if (k > depth[v]) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(lift(v, k));
    }

    /** Jumps {@code steps} (at most the depth of {@code v}) toward the root. */
    private int lift(int v, int steps) {
        int current = v;
        int remaining = steps;
        for (int j = 0; remaining != 0; j++, remaining >>>= 1) {
            if ((remaining & 1) != 0) {
                current = up[j][current];
            }
        }
        return current;
    }

    private void check(int v) {
        if (v < 0 || v >= depth.length) {
            throw new IndexOutOfBoundsException("vertex: " + v);
        }
    }
}
