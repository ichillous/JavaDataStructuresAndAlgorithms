package io.github.ichillous.dsa.graphs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Whole-tree algorithms on undirected trees (connected, exactly {@code V - 1}
 * edges; a self-loop makes a graph a non-tree). All traversals are iterative.
 */
public final class TreeAlgorithms {
    private TreeAlgorithms() {
    }

    /**
     * Returns one longest simple path (by edge count) of an undirected tree.
     *
     * <p>Invariant (double BFS): a farthest vertex from any start is an endpoint of
     * some diameter, and a farthest vertex from that endpoint ends a diameter.
     * Ties are deterministic: the first sweep starts at vertex 0 and picks the
     * smallest farthest vertex {@code u}; the second sweep starts at {@code u} and
     * picks the smallest farthest vertex {@code v}. The list runs from {@code u} to
     * {@code v}; its size is the diameter in edges plus one. A single vertex gives
     * {@code [0]}. The graph is not mutated and the returned list is immutable.
     * Time O(V), auxiliary space O(V), including adjacency snapshots.
     *
     * @param tree an undirected tree
     * @return the path from {@code u} to {@code v}
     * @throws NullPointerException if {@code tree} is null
     * @throws IllegalArgumentException if {@code tree} is not an undirected tree
     *         (including the empty graph)
     */
    public static List<Integer> diameterPath(Graph tree) {
        TreeValidation.requireTree(tree);
        int[] parent = new int[tree.vertexCount()];
        int u = farthest(tree, 0, parent);
        int v = farthest(tree, u, parent);
        List<Integer> path = new ArrayList<>();
        for (int x = v; x != u; x = parent[x]) {
            path.add(x);
        }
        path.add(u);
        Collections.reverse(path);
        return List.copyOf(path);
    }

    /** BFS from {@code start}, filling {@code parent}; returns the smallest farthest vertex. */
    private static int farthest(Graph tree, int start, int[] parent) {
        int[] dist = new int[tree.vertexCount()];
        Arrays.fill(dist, -1);
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(start);
        dist[start] = 0;
        parent[start] = start;
        while (!queue.isEmpty()) {
            int x = queue.poll();
            for (int y : tree.neighbors(x)) {
                if (dist[y] < 0) {
                    dist[y] = dist[x] + 1;
                    parent[y] = x;
                    queue.add(y);
                }
            }
        }
        int best = start;
        for (int x = 0; x < dist.length; x++) {
            if (dist[x] > dist[best]) {
                best = x;
            }
        }
        return best;
    }
}
