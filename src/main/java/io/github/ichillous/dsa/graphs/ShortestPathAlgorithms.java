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
import java.util.Optional;
import java.util.OptionalLong;
import java.util.PriorityQueue;
import java.util.function.IntToLongFunction;

/**
 * Specialised shortest-path algorithms that complement
 * {@link GraphAlgorithms#dijkstra} and {@link AdvancedGraphAlgorithms#bellmanFord}:
 * 0-1 BFS, shortest paths on a DAG, A* with a checked-consistent heuristic and
 * Floyd-Warshall all-pairs paths.
 *
 * <p>Vertices are {@code 0..V-1}. Null arguments throw {@link NullPointerException};
 * invalid vertices throw {@link IndexOutOfBoundsException}; violated preconditions
 * throw {@link IllegalArgumentException}. Input graphs are never mutated and must
 * not be mutated during a call. Results are detached immutable snapshots, and all
 * traversals are iterative. Unreachable vertices have empty distances and paths.
 * Where several shortest paths exist, the one kept is documented per method.
 */
public final class ShortestPathAlgorithms {
    private ShortestPathAlgorithms() {}

    /** A shortest route: the vertices from source to target and its total weight. */
    public record Route(List<Integer> path, long distance) {
        /** Validates and defensively copies the path, which must be nonempty. */
        public Route {
            Objects.requireNonNull(path, "path");
            path = List.copyOf(path);
            if (path.isEmpty()) {
                throw new IllegalArgumentException("path must not be empty");
            }
        }
    }

    /**
     * Immutable single-source result with signed {@code long} distances. It keeps
     * its own reachability flags, so negative distances are valid values.
     */
    public static final class PathTree {
        private final int source;
        private final long[] distance;
        private final boolean[] reachable;
        private final int[] parent;

        private PathTree(int source, long[] distance, boolean[] reachable, int[] parent) {
            this.source = source;
            this.distance = distance;
            this.reachable = reachable;
            this.parent = parent;
        }

        /** Returns the source vertex in O(1). */
        public int source() {
            return source;
        }

        /**
         * Returns the signed shortest distance in O(1), or empty when unreachable.
         * Throws IndexOutOfBoundsException for an invalid vertex.
         */
        public OptionalLong distanceTo(int vertex) {
            checkVertex(vertex);
            return reachable[vertex] ? OptionalLong.of(distance[vertex]) : OptionalLong.empty();
        }

        /**
         * Returns an immutable path including both endpoints, or an empty list when
         * unreachable. O(path length) time and space; no recursion. Throws
         * IndexOutOfBoundsException for an invalid vertex.
         */
        public List<Integer> pathTo(int vertex) {
            checkVertex(vertex);
            if (!reachable[vertex]) {
                return List.of();
            }
            return rebuild(parent, source, vertex);
        }

        private void checkVertex(int vertex) {
            if (vertex < 0 || vertex >= distance.length) {
                throw new IndexOutOfBoundsException("vertex: " + vertex);
            }
        }
    }

    /**
     * 0-1 BFS. Every edge weight in the whole graph must be 0 or 1, otherwise
     * IllegalArgumentException. A weight-0 relaxation pushes to the front of a
     * deque and a weight-1 relaxation to the back, so vertices leave the deque in
     * nondecreasing distance order and the first removal of a vertex is final.
     * Works on directed and undirected graphs. Distances are at most V-1, so no
     * overflow is possible. Equal-length alternatives keep the first strictly
     * improving relaxation. O(V+E) time and O(V+E) auxiliary space.
     */
    public static PathTree zeroOneBfs(WeightedGraph graph, int source) {
        Objects.requireNonNull(graph, "graph");
        graph.checkVertex(source);
        for (WeightedGraph.Edge edge : graph.edges()) {
            if (edge.weight() != 0 && edge.weight() != 1) {
                throw new IllegalArgumentException("0-1 BFS requires weights 0 or 1");
            }
        }
        int n = graph.vertexCount();
        long[] distance = new long[n];
        boolean[] reachable = new boolean[n];
        boolean[] done = new boolean[n];
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        reachable[source] = true;
        parent[source] = source;
        Deque<Integer> deque = new ArrayDeque<>();
        deque.add(source);
        while (!deque.isEmpty()) {
            int current = deque.removeFirst();
            if (done[current]) {
                continue; // A later duplicate of an already finalized vertex.
            }
            done[current] = true;
            for (WeightedGraph.Edge edge : graph.neighbors(current)) {
                long candidate = distance[current] + edge.weight();
                if (!reachable[edge.to()] || candidate < distance[edge.to()]) {
                    distance[edge.to()] = candidate;
                    reachable[edge.to()] = true;
                    parent[edge.to()] = current;
                    if (edge.weight() == 0) {
                        deque.addFirst(edge.to());
                    } else {
                        deque.addLast(edge.to());
                    }
                }
            }
        }
        return new PathTree(source, distance, reachable, parent);
    }

    /**
     * Single-source shortest paths on a directed acyclic graph by dynamic
     * programming over a topological order, allowing signed weights. The graph
     * must be directed and acyclic as a whole (including components unreachable
     * from the source, and self-loops), otherwise IllegalArgumentException.
     *
     * <p>Invariant: when a vertex is processed every possible predecessor has
     * already been processed, so its label is final. Relaxations use exact
     * BigInteger arithmetic as in Bellman-Ford, so overflowing intermediate sums
     * cannot produce a wrong answer; ArithmeticException means a reachable final
     * distance is outside the long range. The topological order is Kahn's, taking
     * ready vertices in FIFO order seeded by ascending vertex number; ties keep the
     * first strictly improving relaxation. O(V+E) arithmetic operations (each
     * costs O(B) for B-bit intermediates) and O(V+E) auxiliary space.
     */
    public static PathTree dagShortestPaths(WeightedGraph graph, int source) {
        Objects.requireNonNull(graph, "graph");
        graph.checkVertex(source);
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("dagShortestPaths requires a directed graph");
        }
        int n = graph.vertexCount();
        int[] inDegree = new int[n];
        for (WeightedGraph.Edge edge : graph.edges()) {
            inDegree[edge.to()]++;
        }
        int[] order = new int[n];
        int count = 0;
        for (int vertex = 0; vertex < n; vertex++) {
            if (inDegree[vertex] == 0) {
                order[count++] = vertex;
            }
        }
        for (int head = 0; head < count; head++) {
            for (WeightedGraph.Edge edge : graph.neighbors(order[head])) {
                if (--inDegree[edge.to()] == 0) {
                    order[count++] = edge.to();
                }
            }
        }
        if (count != n) {
            throw new IllegalArgumentException("graph contains a cycle");
        }
        BigInteger[] distance = new BigInteger[n];
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        distance[source] = BigInteger.ZERO;
        parent[source] = source;
        for (int vertex : order) {
            if (distance[vertex] == null) {
                continue;
            }
            for (WeightedGraph.Edge edge : graph.neighbors(vertex)) {
                BigInteger candidate = distance[vertex].add(BigInteger.valueOf(edge.weight()));
                if (distance[edge.to()] == null || candidate.compareTo(distance[edge.to()]) < 0) {
                    distance[edge.to()] = candidate;
                    parent[edge.to()] = vertex;
                }
            }
        }
        long[] checked = new long[n];
        boolean[] reachable = new boolean[n];
        for (int vertex = 0; vertex < n; vertex++) {
            if (distance[vertex] != null) {
                checked[vertex] = distance[vertex].longValueExact();
                reachable[vertex] = true;
            }
        }
        return new PathTree(source, checked, reachable, parent);
    }

    private record Candidate(int vertex, long distance, long heuristic) {}

    /**
     * A* search from source to target. Every edge in the graph must be
     * nonnegative (IllegalArgumentException otherwise, as in Dijkstra).
     *
     * <p>Heuristic contract: {@code heuristic} must be a pure function with
     * {@code h(v) >= 0} for every vertex the search evaluates and
     * {@code h(target) == 0}; it is evaluated at most once per vertex. Consistency
     * {@code h(u) <= w(u,v) + h(v)} is verified on every edge the search scans
     * (including edges whose candidate distance is discarded); a violation, a
     * negative estimate or a nonzero target estimate throws
     * IllegalArgumentException. Edges the search never scans are not checked, so
     * an inconsistent heuristic may go unnoticed if its violation is never reached.
     *
     * <p>Invariant: with a consistent heuristic, f = g + h never decreases along
     * a path, so a vertex removed from the queue with its current g has its optimal
     * g and is never reopened. The queue orders by f, then by vertex number. f
     * is the exact sum g + h: since 0 &lt;= g, h &lt;= 2^63-1 the sum fits in an
     * unsigned 64-bit value and is compared with Long.compareUnsigned. Capping f
     * at Long.MAX_VALUE would tie entries that differ and let a vertex with a
     * larger f expand before the target; exact ordering avoids that.
     * The g values follow Dijkstra's overflow policy: a detour exceeding long is
     * ignored. Returns empty when the target is unreachable; if the target is
     * reachable only through routes whose cost exceeds long, ArithmeticException is
     * thrown. Equal-cost alternatives keep the first strictly improving
     * relaxation. O(V + E log(E+1)) time and O(V+E) space, plus heuristic cost.
     */
    public static Optional<Route> aStar(WeightedGraph graph, int source, int target,
            IntToLongFunction heuristic) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(heuristic, "heuristic");
        graph.checkVertex(source);
        graph.checkVertex(target);
        for (WeightedGraph.Edge edge : graph.edges()) {
            if (edge.weight() < 0) {
                throw new IllegalArgumentException("A* requires nonnegative weights");
            }
        }
        int n = graph.vertexCount();
        long[] estimate = new long[n];
        boolean[] known = new boolean[n];
        IntToLongFunction checked = vertex -> {
            if (!known[vertex]) {
                long value = heuristic.applyAsLong(vertex);
                if (value < 0) {
                    throw new IllegalArgumentException("heuristic is negative at vertex " + vertex);
                }
                if (vertex == target && value != 0) {
                    throw new IllegalArgumentException("heuristic must be 0 at the target");
                }
                estimate[vertex] = value;
                known[vertex] = true;
            }
            return estimate[vertex];
        };
        checked.applyAsLong(target);
        long[] distance = new long[n];
        Arrays.fill(distance, -1);
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        distance[source] = 0;
        parent[source] = source;
        PriorityQueue<Candidate> queue = new PriorityQueue<>(
                ((Comparator<Candidate>) (a, b) -> Long.compareUnsigned(
                        a.distance() + a.heuristic(), b.distance() + b.heuristic()))
                        .thenComparingInt(Candidate::vertex));
        queue.add(new Candidate(source, 0, checked.applyAsLong(source)));
        while (!queue.isEmpty()) {
            Candidate current = queue.remove();
            if (current.distance() != distance[current.vertex()]) {
                continue; // Superseded by a cheaper entry.
            }
            if (current.vertex() == target) {
                return Optional.of(new Route(rebuild(parent, source, target), current.distance()));
            }
            for (WeightedGraph.Edge edge : graph.neighbors(current.vertex())) {
                long nextHeuristic = checked.applyAsLong(edge.to());
                if (current.heuristic() > saturatingAdd(edge.weight(), nextHeuristic)) {
                    throw new IllegalArgumentException("heuristic is inconsistent on edge "
                            + edge.from() + " -> " + edge.to());
                }
                if (current.distance() > Long.MAX_VALUE - edge.weight()) {
                    continue; // This path cannot beat a representable distance.
                }
                long nextDistance = current.distance() + edge.weight();
                if (distance[edge.to()] == -1 || nextDistance < distance[edge.to()]) {
                    distance[edge.to()] = nextDistance;
                    parent[edge.to()] = current.vertex();
                    queue.add(new Candidate(edge.to(), nextDistance, nextHeuristic));
                }
            }
        }
        if (reaches(graph, source, target)) {
            throw new ArithmeticException("shortest distance to vertex " + target + " exceeds long range");
        }
        return Optional.empty();
    }

    private static long saturatingAdd(long a, long b) {
        return a > Long.MAX_VALUE - b ? Long.MAX_VALUE : a + b;
    }

    private static boolean reaches(WeightedGraph graph, int source, int target) {
        boolean[] seen = new boolean[graph.vertexCount()];
        Deque<Integer> stack = new ArrayDeque<>();
        seen[source] = true;
        stack.push(source);
        while (!stack.isEmpty()) {
            int current = stack.pop();
            if (current == target) {
                return true;
            }
            for (WeightedGraph.Edge edge : graph.neighbors(current)) {
                if (!seen[edge.to()]) {
                    seen[edge.to()] = true;
                    stack.push(edge.to());
                }
            }
        }
        return false;
    }

    private static List<Integer> rebuild(int[] parent, int source, int target) {
        List<Integer> path = new ArrayList<>();
        for (int current = target; current != source; current = parent[current]) {
            path.add(current);
        }
        path.add(source);
        Collections.reverse(path);
        return List.copyOf(path);
    }

    /** Immutable all-pairs shortest distances with path reconstruction. */
    public static final class AllPairs {
        private final long[][] distance;
        private final boolean[][] reachable;
        private final int[][] next;

        private AllPairs(long[][] distance, boolean[][] reachable, int[][] next) {
            this.distance = distance;
            this.reachable = reachable;
            this.next = next;
        }

        /** Returns the number of vertices in O(1). */
        public int vertexCount() {
            return distance.length;
        }

        /**
         * Returns the signed shortest distance, or empty when {@code to} is not
         * reachable from {@code from}; {@code distance(v, v)} is 0 for every vertex
         * (a negative cycle would have been rejected). O(1). Throws
         * IndexOutOfBoundsException for an invalid vertex.
         */
        public OptionalLong distance(int from, int to) {
            check(from);
            check(to);
            return reachable[from][to] ? OptionalLong.of(distance[from][to]) : OptionalLong.empty();
        }

        /**
         * Returns an immutable simple path including both endpoints, or an empty
         * list when unreachable; {@code path(v, v)} is {@code [v]}. O(path length)
         * time and space, iterative. Throws IndexOutOfBoundsException for an
         * invalid vertex.
         */
        public List<Integer> path(int from, int to) {
            check(from);
            check(to);
            if (!reachable[from][to]) {
                return List.of();
            }
            List<Integer> path = new ArrayList<>();
            path.add(from);
            for (int current = from; current != to; ) {
                current = next[current][to];
                path.add(current);
            }
            return List.copyOf(path);
        }

        private void check(int vertex) {
            if (vertex < 0 || vertex >= distance.length) {
                throw new IndexOutOfBoundsException("vertex: " + vertex);
            }
        }
    }

    /**
     * Floyd-Warshall all-pairs shortest paths with signed weights and parallel
     * edges; for an undirected graph each edge is usable in both directions.
     * Throws IllegalArgumentException if the graph has any negative cycle,
     * anywhere (not only reachable from some source), which includes any negative
     * undirected edge (it is a negative closed walk) and any negative self-loop.
     *
     * <p>Invariant: after round k, {@code d[i][j]} is the shortest walk length from
     * i to j using only intermediate vertices below k. Arithmetic is exact
     * (BigInteger), and the algorithm stops as soon as some {@code d[i][i] < 0},
     * which bounds intermediate growth. After negative-cycle rejection, every
     * finite distance is converted with longValueExact, so an unrepresentable
     * shortest distance between any reachable pair throws ArithmeticException; a
     * reachable Long.MAX_VALUE is valid. Equal-length alternatives keep the first
     * strictly improving intermediate vertex in ascending order. O(V^3) arithmetic
     * operations and O(V^2) space (with B-bit intermediates, O(V^3 B) bit time).
     * An empty graph yields an empty result.
     */
    public static AllPairs floydWarshall(WeightedGraph graph) {
        Objects.requireNonNull(graph, "graph");
        int n = graph.vertexCount();
        BigInteger[][] d = new BigInteger[n][n];
        int[][] next = new int[n][n];
        for (int[] row : next) {
            Arrays.fill(row, -1);
        }
        for (int v = 0; v < n; v++) {
            d[v][v] = BigInteger.ZERO;
            next[v][v] = v;
        }
        for (WeightedGraph.Edge edge : graph.edges()) {
            if (edge.weight() < 0 && (!graph.isDirected() || edge.from() == edge.to())) {
                throw new IllegalArgumentException("graph contains a negative cycle");
            }
            relax(d, next, edge.from(), edge.to(), BigInteger.valueOf(edge.weight()), edge.to());
            if (!graph.isDirected()) {
                relax(d, next, edge.to(), edge.from(), BigInteger.valueOf(edge.weight()), edge.from());
            }
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (d[i][k] == null) {
                    continue;
                }
                for (int j = 0; j < n; j++) {
                    if (d[k][j] == null) {
                        continue;
                    }
                    if (relax(d, next, i, j, d[i][k].add(d[k][j]), next[i][k]) && d[i][i].signum() < 0) {
                        throw new IllegalArgumentException("graph contains a negative cycle");
                    }
                }
            }
        }
        long[][] result = new long[n][n];
        boolean[][] reachable = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (d[i][j] != null) {
                    result[i][j] = d[i][j].longValueExact();
                    reachable[i][j] = true;
                }
            }
        }
        return new AllPairs(result, reachable, next);
    }

    private static boolean relax(BigInteger[][] d, int[][] next, int from, int to,
            BigInteger candidate, int firstHop) {
        if (d[from][to] == null || candidate.compareTo(d[from][to]) < 0) {
            d[from][to] = candidate;
            next[from][to] = firstHop;
            return true;
        }
        return false;
    }
}
