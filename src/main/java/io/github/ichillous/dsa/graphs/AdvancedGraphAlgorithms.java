package io.github.ichillous.dsa.graphs;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * Signed shortest paths and iterative connectivity algorithms. Returned values
 * are immutable snapshots. Input graphs must not be mutated during a computation.
 */
public final class AdvancedGraphAlgorithms {
    private AdvancedGraphAlgorithms() {}

    private record ExactEdge(int from, int to, BigInteger weight) {}

    /**
     * Bellman-Ford shortest paths, accepting signed weights and parallel edges.
     * Throws IllegalArgumentException if any negative cycle is reachable from
     * source; negative cycles in other components are ignored. An undirected
     * negative edge forms a negative closed walk and is rejected when reachable.
     *
     * <p>Relaxations use exact BigInteger arithmetic. Only final shortest distances
     * are converted to long, so overflowing detours and intermediate cancellation
     * do not cause spurious failures. ArithmeticException means a final reachable
     * shortest distance is outside the long range. Negative-cycle detection runs
     * before this range check. Unreachable vertices have empty distances and paths.
     *
     * <p>At most O(VE) relaxations plus O(V+E) initialization. With B the maximum
     * intermediate integer bit length, additions/comparisons cost O(B), giving
     * O(V+E+VEB) bit-operation time and O((V+E)B) auxiliary bits including graph
     * snapshots. Here B is O(64 + log(VE+1)); these bounds include in-place updates
     * on a negative cycle before its detection. Path reconstruction is iterative.
     */
    public static ShortestPaths bellmanFord(WeightedGraph graph, int source) {
        Objects.requireNonNull(graph, "graph");
        graph.checkVertex(source);
        List<ExactEdge> edges = new ArrayList<>();
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            for (WeightedGraph.Edge edge : graph.neighbors(vertex)) {
                edges.add(new ExactEdge(edge.from(), edge.to(), BigInteger.valueOf(edge.weight())));
            }
        }
        BigInteger[] distance = new BigInteger[graph.vertexCount()];
        int[] parent = new int[graph.vertexCount()];
        Arrays.fill(parent, -1);
        distance[source] = BigInteger.ZERO;
        parent[source] = source;
        for (int pass = 1; pass < graph.vertexCount(); pass++) {
            boolean changed = false;
            for (ExactEdge edge : edges) {
                if (distance[edge.from()] == null) {
                    continue;
                }
                BigInteger candidate = distance[edge.from()].add(edge.weight());
                if (distance[edge.to()] == null || candidate.compareTo(distance[edge.to()]) < 0) {
                    distance[edge.to()] = candidate;
                    parent[edge.to()] = edge.from();
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }
        for (ExactEdge edge : edges) {
            if (distance[edge.from()] != null && (distance[edge.to()] == null
                    || distance[edge.from()].add(edge.weight()).compareTo(distance[edge.to()]) < 0)) {
                throw new IllegalArgumentException("graph contains a reachable negative cycle");
            }
        }
        long[] checkedDistance = new long[distance.length];
        boolean[] reachable = new boolean[distance.length];
        for (int vertex = 0; vertex < distance.length; vertex++) {
            if (distance[vertex] != null) {
                checkedDistance[vertex] = distance[vertex].longValueExact();
                reachable[vertex] = true;
            }
        }
        return new ShortestPaths(source, checkedDistance, reachable, parent);
    }

    /** Immutable, detached Bellman-Ford result with signed long distances. */
    public static final class ShortestPaths {
        private final int source;
        private final long[] distance;
        private final boolean[] reachable;
        private final int[] parent;

        private ShortestPaths(int source, long[] distance, boolean[] reachable, int[] parent) {
            this.source = source;
            this.distance = distance.clone();
            this.reachable = reachable.clone();
            this.parent = parent.clone();
        }

        /** Returns the source vertex. */
        public int source() {
            return source;
        }

        /** Returns a signed shortest distance in O(1), or empty when unreachable. */
        public OptionalLong distanceTo(int vertex) {
            checkVertex(vertex);
            return reachable[vertex] ? OptionalLong.of(distance[vertex]) : OptionalLong.empty();
        }

        /**
         * Returns an immutable path including both endpoints, or empty when
         * unreachable. Takes O(path length) time and space; no recursion is used.
         * Equal-length alternatives retain the first strictly improving path
         * discovered by the vertex-ordered edge scans.
         */
        public List<Integer> pathTo(int vertex) {
            checkVertex(vertex);
            if (!reachable[vertex]) {
                return List.of();
            }
            List<Integer> path = new ArrayList<>();
            for (int current = vertex; current != source; current = parent[current]) {
                path.add(current);
            }
            path.add(source);
            Collections.reverse(path);
            return List.copyOf(path);
        }

        private void checkVertex(int vertex) {
            if (vertex < 0 || vertex >= distance.length) {
                throw new IndexOutOfBoundsException("vertex: " + vertex);
            }
        }
    }

    private static final class Frame {
        private final int vertex;
        private int nextNeighbor;

        private Frame(int vertex) {
            this.vertex = vertex;
        }
    }

    /**
     * Iterative Kosaraju decomposition of a directed graph in O(V+E) time and
     * space. Every vertex appears once, including isolated vertices. Vertices in
     * each component are ascending; components are ordered by their smallest
     * vertex. Canonicalization is a linear sweep rather than a comparison sort.
     * Rejects undirected graphs. Both returned list levels are immutable.
     */
    public static List<List<Integer>> stronglyConnectedComponents(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("stronglyConnectedComponents requires a directed graph");
        }
        List<List<Integer>> adjacency = snapshot(graph);
        List<List<Integer>> reverse = new ArrayList<>(graph.vertexCount());
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            reverse.add(new ArrayList<>());
        }
        for (int from = 0; from < adjacency.size(); from++) {
            for (int to : adjacency.get(from)) {
                reverse.get(to).add(from);
            }
        }
        boolean[] seen = new boolean[graph.vertexCount()];
        List<Integer> finishOrder = new ArrayList<>(graph.vertexCount());
        Deque<Frame> frames = new ArrayDeque<>();
        for (int start = 0; start < graph.vertexCount(); start++) {
            if (seen[start]) {
                continue;
            }
            seen[start] = true;
            frames.push(new Frame(start));
            while (!frames.isEmpty()) {
                Frame frame = frames.peek();
                List<Integer> neighbors = adjacency.get(frame.vertex);
                if (frame.nextNeighbor == neighbors.size()) {
                    finishOrder.add(frame.vertex);
                    frames.pop();
                } else {
                    int next = neighbors.get(frame.nextNeighbor++);
                    if (!seen[next]) {
                        seen[next] = true;
                        frames.push(new Frame(next));
                    }
                }
            }
        }
        int[] component = new int[graph.vertexCount()];
        Arrays.fill(component, -1);
        int componentCount = 0;
        Deque<Integer> pending = new ArrayDeque<>();
        for (int index = finishOrder.size() - 1; index >= 0; index--) {
            int start = finishOrder.get(index);
            if (component[start] != -1) {
                continue;
            }
            component[start] = componentCount;
            pending.push(start);
            while (!pending.isEmpty()) {
                int current = pending.pop();
                for (int next : reverse.get(current)) {
                    if (component[next] == -1) {
                        component[next] = componentCount;
                        pending.push(next);
                    }
                }
            }
            componentCount++;
        }
        int[] canonicalIndex = new int[componentCount];
        Arrays.fill(canonicalIndex, -1);
        List<List<Integer>> result = new ArrayList<>(componentCount);
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            int label = component[vertex];
            if (canonicalIndex[label] == -1) {
                canonicalIndex[label] = result.size();
                result.add(new ArrayList<>());
            }
            result.get(canonicalIndex[label]).add(vertex);
        }
        for (int index = 0; index < result.size(); index++) {
            result.set(index, List.copyOf(result.get(index)));
        }
        return List.copyOf(result);
    }

    /** An undirected edge with distinct, nonnegative endpoints, normalized first &lt; second. */
    public record Bridge(int first, int second) {
        /** Normalizes endpoint order; rejects loops and negative vertex identifiers. */
        public Bridge {
            if (first < 0 || second < 0 || first == second) {
                throw new IllegalArgumentException("bridge endpoints must be distinct and nonnegative");
            }
            if (first > second) {
                int temporary = first;
                first = second;
                second = temporary;
            }
        }
    }

    /**
     * Returns edges whose removal increases the number of connected components
     * of an undirected graph. Iterative DFS tracks discovery time and the lowest
     * discovery time reachable through a subtree plus at most one back edge.
     * A tree edge parent-child is a bridge exactly when low[child] &gt; time[parent].
     *
     * <p>Disconnected graphs and self-loops are supported; loops are never bridges.
     * Graph already suppresses duplicate undirected edges, so skipping the single
     * parent edge is sound. Rejects directed graphs. Results have normalized
     * endpoints and lexicographic order. O(V+E+K log(K+1)) time for K bridges,
     * including sorting; O(V+E) auxiliary space. No Java call-stack recursion.
     */
    public static List<Bridge> bridges(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        if (graph.isDirected()) {
            throw new IllegalArgumentException("bridges requires an undirected graph");
        }
        List<List<Integer>> adjacency = snapshot(graph);
        int[] discovered = new int[graph.vertexCount()];
        int[] low = new int[graph.vertexCount()];
        int[] parent = new int[graph.vertexCount()];
        Arrays.fill(parent, -1);
        List<Bridge> result = new ArrayList<>();
        Deque<Frame> frames = new ArrayDeque<>();
        int time = 0;
        for (int start = 0; start < graph.vertexCount(); start++) {
            if (discovered[start] != 0) {
                continue;
            }
            discovered[start] = low[start] = ++time;
            frames.push(new Frame(start));
            while (!frames.isEmpty()) {
                Frame frame = frames.peek();
                List<Integer> neighbors = adjacency.get(frame.vertex);
                if (frame.nextNeighbor == neighbors.size()) {
                    frames.pop();
                    int predecessor = parent[frame.vertex];
                    if (predecessor != -1) {
                        if (low[frame.vertex] > discovered[predecessor]) {
                            result.add(new Bridge(predecessor, frame.vertex));
                        }
                        low[predecessor] = Math.min(low[predecessor], low[frame.vertex]);
                    }
                    continue;
                }
                int next = neighbors.get(frame.nextNeighbor++);
                if (next == parent[frame.vertex]) {
                    continue;
                }
                if (discovered[next] == 0) {
                    parent[next] = frame.vertex;
                    discovered[next] = low[next] = ++time;
                    frames.push(new Frame(next));
                } else {
                    low[frame.vertex] = Math.min(low[frame.vertex], discovered[next]);
                }
            }
        }
        result.sort(Comparator.comparingInt(Bridge::first).thenComparingInt(Bridge::second));
        return List.copyOf(result);
    }

    private static List<List<Integer>> snapshot(Graph graph) {
        List<List<Integer>> adjacency = new ArrayList<>(graph.vertexCount());
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            adjacency.add(graph.neighbors(vertex));
        }
        return adjacency;
    }
}
