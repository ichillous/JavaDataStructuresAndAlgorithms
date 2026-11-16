package io.github.ichillous.dsa.graphs;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * Structural graph algorithms: articulation points, bipartition with an odd-cycle
 * witness, directed-cycle witness, Eulerian paths and Prim's minimum spanning
 * forest.
 *
 * <p>Every traversal is iterative, so a 100,000-vertex path graph does not
 * overflow the call stack. Inputs are never mutated and results are immutable
 * snapshots. Null arguments throw {@link NullPointerException}; a graph of the
 * wrong direction throws {@link IllegalArgumentException}. Tie-breaking is
 * deterministic and documented per method.
 *
 * <p>{@link Graph} ignores duplicate edges, and an undirected self-loop is one
 * edge with one adjacency entry. This class treats such a loop as one edge that
 * contributes 2 to the vertex's undirected degree (so it never changes degree
 * parity) and as a cycle of length 1.
 */
public final class GraphStructureAlgorithms {
    private GraphStructureAlgorithms() {}

    /** A two-coloring of an undirected graph; both lists are ascending and immutable. */
    public record Bipartition(List<Integer> left, List<Integer> right) {
        /**
         * Defensively snapshots both sides.
         *
         * @throws IllegalArgumentException if a side is not strictly ascending or the
         *     sides share a vertex
         */
        public Bipartition {
            left = List.copyOf(left);
            right = List.copyOf(right);
            requireStrictlyAscending(left, "left");
            requireStrictlyAscending(right, "right");
            java.util.Set<Integer> seen = new java.util.HashSet<>(left);
            for (int vertex : right) {
                if (seen.contains(vertex)) {
                    throw new IllegalArgumentException("vertex on both sides: " + vertex);
                }
            }
        }

        private static void requireStrictlyAscending(List<Integer> side, String name) {
            for (int i = 1; i < side.size(); i++) {
                if (side.get(i - 1) >= side.get(i)) {
                    throw new IllegalArgumentException(name + " must be strictly ascending");
                }
            }
        }
    }

    /**
     * Returns the articulation points (cut vertices) of an undirected graph in
     * ascending order: vertices whose removal increases the number of connected
     * components. Uses iterative lowlink DFS. A DFS root is an articulation point
     * exactly when it has more than one DFS child; any other vertex {@code p} is
     * one when some child {@code c} has {@code low[c] >= discovery[p]}.
     * Self-loops are ignored and disconnected graphs are supported. Because
     * {@code Graph} suppresses duplicate edges, skipping the single parent entry
     * is sound. Isolated vertices and graphs with no vertices give an empty list.
     * Runs in O(V+E) time and O(V+E) auxiliary space including adjacency
     * snapshots.
     *
     * @param undirected the graph; not modified
     * @return ascending immutable list of cut vertices
     * @throws IllegalArgumentException if the graph is directed
     * @throws NullPointerException if the graph is null
     */
    public static List<Integer> articulationPoints(Graph undirected) {
        requireUndirected(undirected, "articulationPoints");
        int n = undirected.vertexCount();
        List<List<Integer>> adjacency = snapshot(undirected);
        int[] discovered = new int[n];
        int[] low = new int[n];
        int[] parent = new int[n];
        int[] next = new int[n];
        int[] children = new int[n];
        boolean[] cut = new boolean[n];
        Arrays.fill(parent, -1);
        int[] stack = new int[n];
        int time = 0;
        for (int start = 0; start < n; start++) {
            if (discovered[start] != 0) {
                continue;
            }
            int top = 0;
            stack[top++] = start;
            discovered[start] = low[start] = ++time;
            while (top > 0) {
                int vertex = stack[top - 1];
                List<Integer> neighbors = adjacency.get(vertex);
                if (next[vertex] == neighbors.size()) {
                    top--;
                    int up = parent[vertex];
                    if (up != -1) {
                        low[up] = Math.min(low[up], low[vertex]);
                        if (parent[up] != -1 && low[vertex] >= discovered[up]) {
                            cut[up] = true;
                        }
                    }
                    continue;
                }
                int neighbor = neighbors.get(next[vertex]++);
                if (neighbor == parent[vertex]) {
                    continue;
                }
                if (discovered[neighbor] == 0) {
                    parent[neighbor] = vertex;
                    children[vertex]++;
                    discovered[neighbor] = low[neighbor] = ++time;
                    stack[top++] = neighbor;
                } else {
                    low[vertex] = Math.min(low[vertex], discovered[neighbor]);
                }
            }
            if (children[start] > 1) {
                cut[start] = true;
            }
        }
        List<Integer> result = new ArrayList<>();
        for (int vertex = 0; vertex < n; vertex++) {
            if (cut[vertex]) {
                result.add(vertex);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Two-colors an undirected graph by BFS from ascending uncolored vertices.
     * The smallest vertex of each component (including isolated vertices) is on
     * the left. Returns empty when the graph has an odd cycle, which includes any
     * self-loop. Both sides are ascending. Runs in O(V+E) time and O(V+E)
     * auxiliary space.
     *
     * @param undirected the graph; not modified
     * @return the partition, or empty if the graph is not bipartite
     * @throws IllegalArgumentException if the graph is directed
     * @throws NullPointerException if the graph is null
     */
    public static Optional<Bipartition> bipartition(Graph undirected) {
        requireUndirected(undirected, "bipartition");
        Coloring coloring = color(undirected);
        if (coloring.conflictFrom >= 0) {
            return Optional.empty();
        }
        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();
        for (int vertex = 0; vertex < coloring.color.length; vertex++) {
            (coloring.color[vertex] == 0 ? left : right).add(vertex);
        }
        return Optional.of(new Bipartition(left, right));
    }

    /**
     * Returns an odd-length cycle as vertices {@code v0..v(k-1)}, k odd, where
     * consecutive vertices and {@code v(k-1)-v0} are edges, or empty when the
     * graph is bipartite. A self-loop at {@code v} gives {@code [v]}. The witness
     * is built from the first same-colored edge met by the BFS used by
     * {@link #bipartition}: the two BFS-tree paths to their nearest common
     * ancestor, joined by that edge. The cycle is simple. Runs in O(V+E) time and
     * O(V+E) auxiliary space.
     *
     * @param undirected the graph; not modified
     * @return an odd cycle, or empty if the graph is bipartite
     * @throws IllegalArgumentException if the graph is directed
     * @throws NullPointerException if the graph is null
     */
    public static Optional<List<Integer>> oddCycle(Graph undirected) {
        requireUndirected(undirected, "oddCycle");
        Coloring c = color(undirected);
        if (c.conflictFrom < 0) {
            return Optional.empty();
        }
        int a = c.conflictFrom;
        int b = c.conflictTo;
        List<Integer> fromA = new ArrayList<>();
        List<Integer> fromB = new ArrayList<>();
        while (a != b) {
            if (c.depth[a] >= c.depth[b]) {
                fromA.add(a);
                a = c.parent[a];
            } else {
                fromB.add(b);
                b = c.parent[b];
            }
        }
        List<Integer> cycle = new ArrayList<>(fromA);
        cycle.add(a);
        Collections.reverse(fromB);
        cycle.addAll(fromB);
        return Optional.of(List.copyOf(cycle));
    }

    /**
     * Finds a directed cycle by iterative DFS from ascending start vertices,
     * visiting neighbors in insertion order. The first edge into a vertex that is
     * still on the DFS path closes the cycle, returned as {@code v0..v(k-1)} with
     * edges {@code vi->v(i+1)} and {@code v(k-1)->v0}, starting at the path vertex
     * that the closing edge reaches. A self-loop gives {@code [v]}. Empty when the
     * graph is acyclic. Runs in O(V+E) time and O(V+E) auxiliary space.
     *
     * @param directed the graph; not modified
     * @return a cycle, or empty if the graph is acyclic
     * @throws IllegalArgumentException if the graph is undirected
     * @throws NullPointerException if the graph is null
     */
    public static Optional<List<Integer>> directedCycle(Graph directed) {
        Objects.requireNonNull(directed, "directed");
        if (!directed.isDirected()) {
            throw new IllegalArgumentException("directedCycle requires a directed graph");
        }
        int n = directed.vertexCount();
        List<List<Integer>> adjacency = snapshot(directed);
        byte[] state = new byte[n]; // 0 new, 1 on path, 2 finished
        int[] position = new int[n];
        int[] next = new int[n];
        int[] path = new int[n];
        for (int start = 0; start < n; start++) {
            if (state[start] != 0) {
                continue;
            }
            int top = 0;
            path[top] = start;
            position[start] = top++;
            state[start] = 1;
            while (top > 0) {
                int vertex = path[top - 1];
                List<Integer> neighbors = adjacency.get(vertex);
                if (next[vertex] == neighbors.size()) {
                    state[vertex] = 2;
                    top--;
                    continue;
                }
                int neighbor = neighbors.get(next[vertex]++);
                if (state[neighbor] == 1) {
                    List<Integer> cycle = new ArrayList<>(top - position[neighbor]);
                    for (int i = position[neighbor]; i < top; i++) {
                        cycle.add(path[i]);
                    }
                    return Optional.of(List.copyOf(cycle));
                }
                if (state[neighbor] == 0) {
                    state[neighbor] = 1;
                    path[top] = neighbor;
                    position[neighbor] = top++;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds an Eulerian path (a trail using every edge exactly once) by
     * Hierholzer's algorithm, iteratively. Works for directed and undirected
     * graphs and returns {@code E+1} vertices, where {@code E} is
     * {@link Graph#edgeCount()}. A graph with no edges gives
     * {@code Optional.of(List.of())}, whatever its vertex count. Isolated vertices
     * are ignored; all edges must lie in one connected component.
     *
     * <p>Start vertex: directed, the vertex with out-in = 1, otherwise the
     * smallest vertex with an edge; undirected, the smallest odd-degree vertex,
     * otherwise the smallest vertex with an edge. At each vertex the first unused
     * neighbor in {@link Graph#neighbors} (insertion) order is followed, so the
     * result is deterministic.
     *
     * <p>Self-loops: an undirected loop is one edge, appearing once in the
     * adjacency list, but adds 2 to its vertex's degree (parity unchanged) and is
     * traversed once, appearing as the vertex twice in a row. A directed loop adds
     * one out-edge and one in-edge.
     *
     * <p>Empty when the degree conditions fail or the edges are disconnected.
     * Runs in O(V+E) expected time (hash lookup pairs the two entries of an
     * undirected edge) and O(V+E) auxiliary space.
     *
     * @param g the graph; not modified
     * @return the vertex sequence, or empty if no Euler path exists
     * @throws NullPointerException if the graph is null
     */
    public static Optional<List<Integer>> eulerianPath(Graph g) {
        Objects.requireNonNull(g, "g");
        int n = g.vertexCount();
        if (g.edgeCount() == 0) {
            return Optional.of(List.of());
        }
        // Per-vertex entries (neighbor, edgeId) in Graph's neighbor insertion order.
        // An undirected edge shares one id between its two entries (a loop has one).
        int[][] to = new int[n][];
        int[][] id = new int[n][];
        int[] degree = new int[n]; // directed: out-degree; undirected: a loop counts 2
        int[] indegree = new int[n];
        Map<Long, Integer> ids = new HashMap<>();
        int edgeId = 0;
        for (int v = 0; v < n; v++) {
            List<Integer> neighbors = g.neighbors(v);
            to[v] = new int[neighbors.size()];
            id[v] = new int[neighbors.size()];
            for (int i = 0; i < to[v].length; i++) {
                int w = neighbors.get(i);
                to[v][i] = w;
                if (g.isDirected()) {
                    degree[v]++;
                    indegree[w]++;
                    id[v][i] = edgeId++;
                } else {
                    degree[v] += w == v ? 2 : 1;
                    long key = (long) Math.min(v, w) * n + Math.max(v, w);
                    Integer known = ids.get(key);
                    if (known == null) {
                        known = edgeId++;
                        ids.put(key, known);
                    }
                    id[v][i] = known;
                }
            }
        }
        int start = chooseEulerStart(g.isDirected(), n, degree, indegree);
        if (start < 0) {
            return Optional.empty();
        }
        boolean[] used = new boolean[edgeId];
        int[] pointer = new int[n];
        int[] stack = new int[edgeId + 1];
        int top = 0;
        stack[top++] = start;
        List<Integer> path = new ArrayList<>(edgeId + 1);
        while (top > 0) {
            int vertex = stack[top - 1];
            while (pointer[vertex] < to[vertex].length && used[id[vertex][pointer[vertex]]]) {
                pointer[vertex]++;
            }
            if (pointer[vertex] == to[vertex].length) {
                path.add(vertex);
                top--;
            } else {
                used[id[vertex][pointer[vertex]]] = true;
                stack[top++] = to[vertex][pointer[vertex]++];
            }
        }
        if (path.size() != edgeId + 1) {
            return Optional.empty();
        }
        Collections.reverse(path);
        return Optional.of(List.copyOf(path));
    }

    /** Returns the Euler start vertex, or -1 when the degree conditions fail. */
    private static int chooseEulerStart(boolean directed, int n, int[] degree, int[] indegree) {
        int smallestWithEdge = -1;
        int special = -1;
        int surplus = 0;
        int deficit = 0;
        int odd = 0;
        for (int v = 0; v < n; v++) {
            if (smallestWithEdge < 0 && (degree[v] > 0 || indegree[v] > 0)) {
                smallestWithEdge = v;
            }
            if (directed) {
                int balance = degree[v] - indegree[v];
                if (balance == 1) {
                    surplus++;
                    special = v;
                } else if (balance == -1) {
                    deficit++;
                } else if (balance != 0) {
                    return -1;
                }
            } else if ((degree[v] & 1) == 1 && odd++ == 0) {
                special = v;
            }
        }
        if (directed ? surplus != deficit || surplus > 1 : odd != 0 && odd != 2) {
            return -1;
        }
        return special >= 0 ? special : smallestWithEdge;
    }

    /**
     * Prim's minimum spanning forest using a lazy binary heap, restarted from the
     * smallest unvisited vertex for each component. Same contract as
     * {@link GraphAlgorithms#minimumSpanningForest}: negative weights are allowed,
     * a disconnected graph gives a forest ({@code V - components} edges; isolated
     * vertices count as components), loops are never selected and parallel edges
     * are fine. The total is accumulated with {@link BigInteger} and must fit in a
     * {@code long}, otherwise {@link ArithmeticException} is thrown; intermediate
     * cancellation does not cause spurious overflow.
     *
     * <p>Selected edges are listed in selection order, oriented from the tree
     * vertex to the newly reached vertex. Among equal weights the heap prefers the
     * smaller target vertex, then the smaller source vertex, so the chosen forest
     * may differ from Kruskal's on ties although the total weight is equal.
     * Runs in O(V + E log(E+1)) time and O(V+E) auxiliary space.
     *
     * @param undirected the graph; not modified
     * @return the forest, its exact total weight and component count
     * @throws IllegalArgumentException if the graph is directed
     * @throws NullPointerException if the graph is null
     * @throws ArithmeticException if the total weight does not fit in a long
     */
    public static GraphAlgorithms.SpanningForest primMinimumSpanningForest(WeightedGraph undirected) {
        Objects.requireNonNull(undirected, "undirected");
        if (undirected.isDirected()) {
            throw new IllegalArgumentException("primMinimumSpanningForest requires an undirected graph");
        }
        int n = undirected.vertexCount();
        boolean[] visited = new boolean[n];
        Comparator<WeightedGraph.Edge> order = Comparator
                .comparingLong(WeightedGraph.Edge::weight)
                .thenComparingInt(WeightedGraph.Edge::to)
                .thenComparingInt(WeightedGraph.Edge::from);
        PriorityQueue<WeightedGraph.Edge> heap = new PriorityQueue<>(order);
        List<WeightedGraph.Edge> selected = new ArrayList<>();
        BigInteger total = BigInteger.ZERO;
        int components = 0;
        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }
            components++;
            visited[start] = true;
            heap.addAll(undirected.neighbors(start));
            while (!heap.isEmpty()) {
                WeightedGraph.Edge edge = heap.poll();
                if (visited[edge.to()]) {
                    continue;
                }
                visited[edge.to()] = true;
                selected.add(edge);
                total = total.add(BigInteger.valueOf(edge.weight()));
                for (WeightedGraph.Edge out : undirected.neighbors(edge.to())) {
                    if (!visited[out.to()]) {
                        heap.add(out);
                    }
                }
            }
        }
        return new GraphAlgorithms.SpanningForest(selected, total.longValueExact(), components);
    }

    private record Coloring(int[] color, int[] parent, int[] depth, int conflictFrom, int conflictTo) {}

    private static Coloring color(Graph graph) {
        int n = graph.vertexCount();
        List<List<Integer>> adjacency = snapshot(graph);
        int[] color = new int[n];
        int[] parent = new int[n];
        int[] depth = new int[n];
        Arrays.fill(color, -1);
        Arrays.fill(parent, -1);
        Deque<Integer> queue = new ArrayDeque<>();
        for (int start = 0; start < n; start++) {
            if (color[start] != -1) {
                continue;
            }
            color[start] = 0;
            queue.add(start);
            while (!queue.isEmpty()) {
                int vertex = queue.remove();
                for (int neighbor : adjacency.get(vertex)) {
                    if (color[neighbor] == -1) {
                        color[neighbor] = 1 - color[vertex];
                        parent[neighbor] = vertex;
                        depth[neighbor] = depth[vertex] + 1;
                        queue.add(neighbor);
                    } else if (color[neighbor] == color[vertex]) {
                        return new Coloring(color, parent, depth, vertex, neighbor);
                    }
                }
            }
        }
        return new Coloring(color, parent, depth, -1, -1);
    }

    private static void requireUndirected(Graph graph, String name) {
        Objects.requireNonNull(graph, "graph");
        if (graph.isDirected()) {
            throw new IllegalArgumentException(name + " requires an undirected graph");
        }
    }

    private static List<List<Integer>> snapshot(Graph graph) {
        List<List<Integer>> adjacency = new ArrayList<>(graph.vertexCount());
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {
            adjacency.add(graph.neighbors(vertex));
        }
        return adjacency;
    }
}
