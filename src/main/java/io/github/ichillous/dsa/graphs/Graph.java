package io.github.ichillous.dsa.graphs;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Adjacency-list graph with fixed vertices {@code 0..vertexCount-1}.
 * Neighbor iteration follows insertion order. Duplicate edges are ignored;
 * an undirected self-loop counts as one edge and one adjacency entry.
 * This mutable educational graph is not thread-safe.
 */
public final class Graph {
    private final List<Set<Integer>> adjacency;
    private final boolean directed;
    private int edgeCount;

    private Graph(int vertexCount, boolean directed) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("vertexCount must be nonnegative");
        }
        this.directed = directed;
        adjacency = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            adjacency.add(new LinkedHashSet<>());
        }
    }

    /** Creates a directed graph with no edges. */
    public static Graph directed(int vertexCount) {
        return new Graph(vertexCount, true);
    }

    /** Creates an undirected graph with no edges. */
    public static Graph undirected(int vertexCount) {
        return new Graph(vertexCount, false);
    }

    /** Adds an edge in expected O(1) time, returning this graph for chaining. */
    public Graph addEdge(int from, int to) {
        checkVertex(from);
        checkVertex(to);
        if (adjacency.get(from).add(to)) {
            edgeCount++;
            if (!directed) {
                adjacency.get(to).add(from);
            }
        }
        return this;
    }

    /** Returns an immutable snapshot in insertion order, in O(out-degree) time. */
    public List<Integer> neighbors(int vertex) {
        checkVertex(vertex);
        return List.copyOf(adjacency.get(vertex));
    }

    /** Returns the number of vertices, including isolated vertices. */
    public int vertexCount() {
        return adjacency.size();
    }

    /** Returns distinct edges; reverse adjacency entries are not counted twice. */
    public int edgeCount() {
        return edgeCount;
    }

    /** Returns whether edges have a direction. */
    public boolean isDirected() {
        return directed;
    }

    void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount()) {
            throw new IndexOutOfBoundsException("vertex: " + vertex);
        }
    }
}
