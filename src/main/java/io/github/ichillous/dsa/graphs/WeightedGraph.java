package io.github.ichillous.dsa.graphs;

import java.util.ArrayList;
import java.util.List;

/**
 * Weighted adjacency-list multigraph on fixed integer vertices. Parallel edges
 * and self-loops are allowed. Weights are signed {@code long}s: minimum spanning
 * forests allow negative weights, while Dijkstra explicitly rejects them.
 * Edge and neighbor snapshots preserve insertion order. Not thread-safe.
 */
public final class WeightedGraph {
    /** An oriented adjacency entry, or an edge in its originally added orientation. */
    public record Edge(int from, int to, long weight) {}

    private final List<List<Edge>> adjacency;
    private final List<Edge> edges = new ArrayList<>();
    private final boolean directed;

    private WeightedGraph(int vertexCount, boolean directed) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("vertexCount must be nonnegative");
        }
        this.directed = directed;
        adjacency = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            adjacency.add(new ArrayList<>());
        }
    }

    /** Creates a directed graph with no edges. */
    public static WeightedGraph directed(int vertexCount) {
        return new WeightedGraph(vertexCount, true);
    }

    /** Creates an undirected graph with no edges. */
    public static WeightedGraph undirected(int vertexCount) {
        return new WeightedGraph(vertexCount, false);
    }

    /** Adds an edge in amortized O(1) time, returning this graph for chaining. */
    public WeightedGraph addEdge(int from, int to, long weight) {
        checkVertex(from);
        checkVertex(to);
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);
        if (!directed && from != to) {
            adjacency.get(to).add(new Edge(to, from, weight));
        }
        return this;
    }

    /** Returns an immutable snapshot of oriented outgoing edges in O(out-degree). */
    public List<Edge> neighbors(int vertex) {
        checkVertex(vertex);
        return List.copyOf(adjacency.get(vertex));
    }

    /** Returns an immutable O(E) snapshot; each undirected edge appears once. */
    public List<Edge> edges() {
        return List.copyOf(edges);
    }

    /** Returns the number of vertices, including isolated vertices. */
    public int vertexCount() {
        return adjacency.size();
    }

    /** Returns the number of added edges, including parallel edges and loops. */
    public int edgeCount() {
        return edges.size();
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
