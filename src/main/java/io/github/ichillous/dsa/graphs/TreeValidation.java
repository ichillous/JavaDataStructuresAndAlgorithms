package io.github.ichillous.dsa.graphs;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/** Shared precondition check for the tree-query classes in this package. */
final class TreeValidation {
    private TreeValidation() {
    }

    /**
     * Requires {@code graph} to be an undirected tree: non-null, undirected, at
     * least one vertex, exactly {@code V - 1} edges (a self-loop counts as an edge
     * and therefore makes the graph a non-tree) and connected.
     *
     * <p>Iterative BFS from vertex 0; O(V + E) time and O(V + E) auxiliary space
     * including adjacency snapshots. Does not mutate the graph.
     *
     * @throws NullPointerException if {@code graph} is null
     * @throws IllegalArgumentException if the graph is not an undirected tree
     */
    static void requireTree(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        if (graph.isDirected()) {
            throw new IllegalArgumentException("tree must be undirected");
        }
        int vertices = graph.vertexCount();
        if (vertices == 0) {
            throw new IllegalArgumentException("tree must have at least one vertex");
        }
        if (graph.edgeCount() != vertices - 1) {
            throw new IllegalArgumentException(
                    "tree must have V-1 edges: V=" + vertices + ", E=" + graph.edgeCount());
        }
        boolean[] seen = new boolean[vertices];
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(0);
        seen[0] = true;
        int reached = 1;
        while (!queue.isEmpty()) {
            int v = queue.poll();
            for (int w : graph.neighbors(v)) {
                if (!seen[w]) {
                    seen[w] = true;
                    reached++;
                    queue.add(w);
                }
            }
        }
        if (reached != vertices) {
            throw new IllegalArgumentException("tree must be connected");
        }
    }
}
