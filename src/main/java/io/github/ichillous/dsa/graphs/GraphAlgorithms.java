package io.github.ichillous.dsa.graphs;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.PriorityQueue;
import java.util.Queue;

/** Classic graph algorithms. Results are immutable snapshots, detached from graphs. */
public final class GraphAlgorithms {
    private GraphAlgorithms() {}

    /**
     * Visits vertices reachable from source in breadth-first order.
     * O(V+E) time and O(V+E) auxiliary space including adjacency snapshots.
     */
    public static List<Integer> breadthFirst(Graph graph, int source) {
        Objects.requireNonNull(graph, "graph");
        graph.checkVertex(source);
        boolean[] seen = new boolean[graph.vertexCount()];
        List<Integer> order = new ArrayList<>();
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(source);
        seen[source] = true;
        while (!queue.isEmpty()) {
            int current = queue.remove();
            order.add(current);
            for (int neighbor : graph.neighbors(current)) {
                if (!seen[neighbor]) {
                    seen[neighbor] = true;
                    queue.add(neighbor);
                }
            }
        }
        return List.copyOf(order);
    }

    /**
     * Iterative DFS in the same order as recursive DFS over insertion-ordered
     * neighbors. O(V+E) time and space, with no Java call-stack depth limit.
     */
    public static List<Integer> depthFirst(Graph graph, int source) {
        Objects.requireNonNull(graph, "graph");
        graph.checkVertex(source);
        boolean[] seen = new boolean[graph.vertexCount()];
        List<Integer> order = new ArrayList<>();
        Deque<Iterator<Integer>> stack = new ArrayDeque<>();
        seen[source] = true;
        order.add(source);
        stack.push(graph.neighbors(source).iterator());
        while (!stack.isEmpty()) {
            Iterator<Integer> neighbors = stack.peek();
            if (!neighbors.hasNext()) {
                stack.pop();
                continue;
            }
            int neighbor = neighbors.next();
            if (!seen[neighbor]) {
                seen[neighbor] = true;
                order.add(neighbor);
                stack.push(graph.neighbors(neighbor).iterator());
            }
        }
        return List.copyOf(order);
    }

    /**
     * Returns a minimum-edge path including both endpoints, or an empty list
     * when target is unreachable. Ties follow neighbor insertion order.
     * O(V+E) time and O(V+E) auxiliary space.
     */
    public static List<Integer> shortestPath(Graph graph, int source, int target) {
        Objects.requireNonNull(graph, "graph");
        graph.checkVertex(source);
        graph.checkVertex(target);
        int[] parent = new int[graph.vertexCount()];
        Arrays.fill(parent, -1);
        parent[source] = source;
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(source);
        while (!queue.isEmpty() && parent[target] == -1) {
            int current = queue.remove();
            for (int neighbor : graph.neighbors(current)) {
                if (parent[neighbor] == -1) {
                    parent[neighbor] = current;
                    queue.add(neighbor);
                }
            }
        }
        return buildPath(parent, source, target);
    }

    /**
     * Returns breadth-first components of an undirected graph, starting each
     * component at its smallest vertex. Directed graphs are rejected.
     * O(V+E) time and O(V+E) auxiliary space.
     */
    public static List<List<Integer>> connectedComponents(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        if (graph.isDirected()) {
            throw new IllegalArgumentException("connectedComponents requires an undirected graph");
        }
        boolean[] seen = new boolean[graph.vertexCount()];
        List<List<Integer>> components = new ArrayList<>();
        Queue<Integer> queue = new ArrayDeque<>();
        for (int start = 0; start < graph.vertexCount(); start++) {
            if (seen[start]) {
                continue;
            }
            List<Integer> component = new ArrayList<>();
            seen[start] = true;
            queue.add(start);
            while (!queue.isEmpty()) {
                int current = queue.remove();
                component.add(current);
                for (int neighbor : graph.neighbors(current)) {
                    if (!seen[neighbor]) {
                        seen[neighbor] = true;
                        queue.add(neighbor);
                    }
                }
            }
            components.add(List.copyOf(component));
        }
        return List.copyOf(components);
    }

    /**
     * Kahn's topological ordering, in O(V+E) time and space. Rejects undirected
     * graphs and directed cycles (including self-loops), rather than returning
     * a misleading partial ordering. An empty directed graph has empty order.
     */
    public static List<Integer> topologicalSort(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("topologicalSort requires a directed graph");
        }
        int[] indegree = new int[graph.vertexCount()];
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            for (int neighbor : graph.neighbors(vertex)) {
                indegree[neighbor]++;
            }
        }
        Queue<Integer> ready = new ArrayDeque<>();
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            if (indegree[vertex] == 0) {
                ready.add(vertex);
            }
        }
        List<Integer> order = new ArrayList<>();
        while (!ready.isEmpty()) {
            int current = ready.remove();
            order.add(current);
            for (int neighbor : graph.neighbors(current)) {
                if (--indegree[neighbor] == 0) {
                    ready.add(neighbor);
                }
            }
        }
        if (order.size() != graph.vertexCount()) {
            throw new IllegalArgumentException("graph contains a directed cycle");
        }
        return List.copyOf(order);
    }

    private record Candidate(int vertex, long distance) {}

    /**
     * Dijkstra with a lazy binary heap. All edges must be nonnegative, even in
     * disconnected components. Distances include {@link Long#MAX_VALUE}; absence
     * is represented by OptionalLong.empty(), not an overloaded numeric sentinel.
     * Throws ArithmeticException only if a reachable vertex's shortest distance
     * exceeds long range; overflowing detours are safely ignored.
     * O(V + E log(E+1)) time and O(V+E) space for this multigraph implementation.
     */
    public static ShortestPaths dijkstra(WeightedGraph graph, int source) {
        Objects.requireNonNull(graph, "graph");
        graph.checkVertex(source);
        for (WeightedGraph.Edge edge : graph.edges()) {
            if (edge.weight() < 0) {
                throw new IllegalArgumentException("Dijkstra requires nonnegative weights");
            }
        }
        long[] distance = new long[graph.vertexCount()];
        Arrays.fill(distance, -1);
        int[] parent = new int[graph.vertexCount()];
        Arrays.fill(parent, -1);
        distance[source] = 0;
        parent[source] = source;
        PriorityQueue<Candidate> heap = new PriorityQueue<>(
                Comparator.comparingLong(Candidate::distance).thenComparingInt(Candidate::vertex));
        heap.add(new Candidate(source, 0));
        while (!heap.isEmpty()) {
            Candidate current = heap.remove();
            if (current.distance() != distance[current.vertex()]) {
                continue; // An improved entry already superseded this heap entry.
            }
            for (WeightedGraph.Edge edge : graph.neighbors(current.vertex())) {
                if (current.distance() > Long.MAX_VALUE - edge.weight()) {
                    continue; // This path cannot beat a representable distance.
                }
                long nextDistance = current.distance() + edge.weight();
                if (distance[edge.to()] == -1 || nextDistance < distance[edge.to()]) {
                    distance[edge.to()] = nextDistance;
                    parent[edge.to()] = current.vertex();
                    heap.add(new Candidate(edge.to(), nextDistance));
                }
            }
        }
        checkDistanceOverflow(graph, source, distance);
        return new ShortestPaths(source, distance, parent);
    }

    private static void checkDistanceOverflow(WeightedGraph graph, int source, long[] distance) {
        boolean[] seen = new boolean[graph.vertexCount()];
        Queue<Integer> queue = new ArrayDeque<>();
        seen[source] = true;
        queue.add(source);
        while (!queue.isEmpty()) {
            int current = queue.remove();
            if (distance[current] == -1) {
                throw new ArithmeticException("shortest distance to vertex " + current + " exceeds long range");
            }
            for (WeightedGraph.Edge edge : graph.neighbors(current)) {
                if (!seen[edge.to()]) {
                    seen[edge.to()] = true;
                    queue.add(edge.to());
                }
            }
        }
    }

    /** Immutable single-source shortest paths. */
    public static final class ShortestPaths {
        private final int source;
        private final long[] distance;
        private final int[] parent;

        private ShortestPaths(int source, long[] distance, int[] parent) {
            this.source = source;
            this.distance = distance.clone();
            this.parent = parent.clone();
        }

        /** Returns the source vertex used for the computation. */
        public int source() {
            return source;
        }

        /** Returns the minimum distance in O(1), or empty for an unreachable vertex. */
        public OptionalLong distanceTo(int vertex) {
            checkVertex(vertex);
            return distance[vertex] == -1 ? OptionalLong.empty() : OptionalLong.of(distance[vertex]);
        }

        /** Returns an immutable O(path length) path, or empty for an unreachable vertex. */
        public List<Integer> pathTo(int vertex) {
            checkVertex(vertex);
            return buildPath(parent, source, vertex);
        }

        private void checkVertex(int vertex) {
            if (vertex < 0 || vertex >= distance.length) {
                throw new IndexOutOfBoundsException("vertex: " + vertex);
            }
        }
    }

    private static List<Integer> buildPath(int[] parent, int source, int target) {
        if (parent[target] == -1) {
            return List.of();
        }
        List<Integer> path = new ArrayList<>();
        for (int current = target; current != source; current = parent[current]) {
            path.add(current);
        }
        path.add(source);
        Collections.reverse(path);
        return List.copyOf(path);
    }

    /** Minimum spanning forest, with one component per isolated vertex as well. */
    public record SpanningForest(List<WeightedGraph.Edge> edges, long totalWeight, int components) {
        /** Defensively snapshots edges. */
        public SpanningForest {
            edges = List.copyOf(edges);
        }
    }

    /**
     * Kruskal's minimum spanning forest for undirected graphs. Negative weights,
     * loops, parallel edges, and disconnected graphs are supported. Ties retain
     * input order. Throws ArithmeticException if the final total exceeds long
     * range. O(V + E log(E+1)) time and O(V+E) space.
     */
    public static SpanningForest minimumSpanningForest(WeightedGraph graph) {
        Objects.requireNonNull(graph, "graph");
        if (graph.isDirected()) {
            throw new IllegalArgumentException("minimumSpanningForest requires an undirected graph");
        }
        List<WeightedGraph.Edge> ordered = new ArrayList<>(graph.edges());
        ordered.sort(Comparator.comparingLong(WeightedGraph.Edge::weight));
        DisjointSets sets = new DisjointSets(graph.vertexCount());
        List<WeightedGraph.Edge> selected = new ArrayList<>();
        BigInteger total = BigInteger.ZERO;
        int components = graph.vertexCount();
        for (WeightedGraph.Edge edge : ordered) {
            if (sets.union(edge.from(), edge.to())) {
                selected.add(edge);
                total = total.add(BigInteger.valueOf(edge.weight()));
                components--;
            }
        }
        return new SpanningForest(selected, total.longValueExact(), components);
    }

    private static final class DisjointSets {
        private final int[] parent;
        private final int[] size;

        private DisjointSets(int count) {
            parent = new int[count];
            size = new int[count];
            for (int i = 0; i < count; i++) {
                parent[i] = i;
                size[i] = 1;
            }
        }

        private int find(int vertex) {
            while (parent[vertex] != vertex) {
                parent[vertex] = parent[parent[vertex]];
                vertex = parent[vertex];
            }
            return vertex;
        }

        private boolean union(int first, int second) {
            int a = find(first);
            int b = find(second);
            if (a == b) {
                return false;
            }
            if (size[a] < size[b]) {
                int temporary = a;
                a = b;
                b = temporary;
            }
            parent[b] = a;
            size[a] += size[b];
            return true;
        }
    }
}
