package io.github.ichillous.dsa.graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Directed flow network on fixed vertices {@code [0, n)} with nonnegative
 * {@code long} capacities. Parallel edges and self-loops are kept; a self-loop
 * never carries flow. Edges are numbered by insertion order. Not thread-safe.
 *
 * <p>{@link #maxFlow(int, int)} runs Dinic's algorithm and does not mutate the
 * network: every call works on private residual arrays and may be repeated.
 */
public final class FlowNetwork {
    /**
     * A directed edge with a nonnegative capacity.
     *
     * @param from tail vertex, nonnegative
     * @param to head vertex, nonnegative
     * @param capacity maximum flow, at least zero
     */
    public record Edge(int from, int to, long capacity) {
        /** Rejects negative endpoints and negative capacity. */
        public Edge {
            if (from < 0 || to < 0) {
                throw new IllegalArgumentException("endpoints must be nonnegative");
            }
            if (capacity < 0) {
                throw new IllegalArgumentException("capacity must be nonnegative");
            }
        }
    }

    /**
     * Result of a maximum-flow computation.
     *
     * @param value total flow leaving the source
     * @param edgeFlows flow on each edge, indexed by insertion order, with
     *     {@code 0 <= flow <= capacity}; immutable
     * @param sourceSide ascending vertices reachable from the source in the final
     *     residual graph; with the complementary vertices it forms a minimum cut
     *     whose capacity equals {@code value}; immutable
     */
    public record MaxFlow(long value, List<Long> edgeFlows, List<Integer> sourceSide) {
        /** Copies the lists so the record is immutable. */
        public MaxFlow {
            edgeFlows = List.copyOf(edgeFlows);
            sourceSide = List.copyOf(sourceSide);
        }
    }

    private final int vertexCount;
    private final List<Edge> edges = new ArrayList<>();

    /**
     * Creates a network with no edges.
     *
     * @param vertexCount number of vertices, at least zero
     * @throws IllegalArgumentException if {@code vertexCount} is negative
     */
    public FlowNetwork(int vertexCount) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("vertexCount must be nonnegative");
        }
        this.vertexCount = vertexCount;
    }

    /**
     * Adds a directed edge in amortized O(1) time. Parallel edges are kept.
     *
     * @return this network, for chaining
     * @throws IndexOutOfBoundsException if an endpoint is outside {@code [0, n)}
     * @throws IllegalArgumentException if {@code capacity} is negative
     */
    public FlowNetwork addEdge(int from, int to, long capacity) {
        checkVertex(from);
        checkVertex(to);
        edges.add(new Edge(from, to, capacity));
        return this;
    }

    /** Returns an immutable O(E) snapshot of the edges in insertion order. */
    public List<Edge> edges() {
        return List.copyOf(edges);
    }

    /** Returns the number of vertices. */
    public int vertexCount() {
        return vertexCount;
    }

    /**
     * Computes a maximum flow and a minimum cut with Dinic's algorithm.
     *
     * <p>Invariant: each phase builds BFS levels in the residual graph and
     * saturates a blocking flow along level-increasing arcs, so the source-sink
     * distance strictly grows; there are at most V - 1 phases. Both the level
     * search and the blocking-flow search are iterative, so path length is not
     * limited by the call stack. Arcs are scanned in edge insertion order, making
     * the returned flow deterministic (a maximum flow is generally not unique).
     * {@code sourceSide} is the set reachable from the source in the final
     * residual graph, the minimal source side among minimum cuts.
     *
     * <p>Overflow: a reverse arc's residual equals the flow on its edge, which is
     * at most that edge's capacity, so residuals never overflow. The total is
     * accumulated with {@link Math#addExact(long, long)}.
     *
     * <p>Time O(V^2 E); auxiliary space O(V + E). The network is not mutated.
     *
     * @param source the source vertex
     * @param sink the sink vertex
     * @return the flow value, per-edge flows and source side of a minimum cut
     * @throws IndexOutOfBoundsException if either vertex is out of range
     * @throws IllegalArgumentException if {@code source == sink}
     * @throws ArithmeticException only if the maximum flow exceeds {@code long}
     */
    public MaxFlow maxFlow(int source, int sink) {
        checkVertex(source);
        checkVertex(sink);
        if (source == sink) {
            throw new IllegalArgumentException("source and sink must differ");
        }
        int m = edges.size();
        int[] head = new int[vertexCount];
        Arrays.fill(head, -1);
        int[] next = new int[2 * m];
        int[] to = new int[2 * m];
        long[] residual = new long[2 * m];
        // Insert in reverse so each adjacency list is traversed in insertion order.
        for (int i = m - 1; i >= 0; i--) {
            Edge e = edges.get(i);
            to[2 * i] = e.to();
            residual[2 * i] = e.capacity();
            next[2 * i] = head[e.from()];
            head[e.from()] = 2 * i;
            to[2 * i + 1] = e.from();
            next[2 * i + 1] = head[e.to()];
            head[e.to()] = 2 * i + 1;
        }

        int[] level = new int[vertexCount];
        int[] current = new int[vertexCount];
        int[] queue = new int[vertexCount];
        int[] path = new int[vertexCount];
        long total = 0;
        while (buildLevels(source, sink, head, next, to, residual, level, queue)) {
            System.arraycopy(head, 0, current, 0, vertexCount);
            total = Math.addExact(total, blockingFlow(source, sink, next, to, residual,
                    level, current, path));
        }

        // The last (failed) level search leaves level[] >= 0 exactly on the reachable set.
        List<Integer> sourceSide = new ArrayList<>();
        for (int v = 0; v < vertexCount; v++) {
            if (level[v] >= 0) {
                sourceSide.add(v);
            }
        }
        List<Long> flows = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            flows.add(residual[2 * i + 1]);
        }
        return new MaxFlow(total, flows, sourceSide);
    }

    private static boolean buildLevels(int source, int sink, int[] head, int[] next, int[] to,
            long[] residual, int[] level, int[] queue) {
        Arrays.fill(level, -1);
        level[source] = 0;
        int front = 0;
        int back = 0;
        queue[back++] = source;
        while (front < back) {
            int u = queue[front++];
            for (int a = head[u]; a != -1; a = next[a]) {
                if (residual[a] > 0 && level[to[a]] < 0) {
                    level[to[a]] = level[u] + 1;
                    queue[back++] = to[a];
                }
            }
        }
        return level[sink] >= 0;
    }

    private static long blockingFlow(int source, int sink, int[] next, int[] to,
            long[] residual, int[] level, int[] current, int[] path) {
        long pushed = 0;
        int depth = 0;
        int u = source;
        while (true) {
            if (u == sink) {
                long bottleneck = Long.MAX_VALUE;
                for (int i = 0; i < depth; i++) {
                    bottleneck = Math.min(bottleneck, residual[path[i]]);
                }
                int firstSaturated = -1;
                for (int i = 0; i < depth; i++) {
                    residual[path[i]] -= bottleneck;
                    residual[path[i] ^ 1] += bottleneck;
                    if (firstSaturated < 0 && residual[path[i]] == 0) {
                        firstSaturated = i;
                    }
                }
                pushed = Math.addExact(pushed, bottleneck);
                depth = firstSaturated;
                u = to[path[depth] ^ 1];
                continue;
            }
            int a = current[u];
            while (a != -1 && !(residual[a] > 0 && level[to[a]] == level[u] + 1)) {
                a = next[a];
            }
            current[u] = a;
            if (a != -1) {
                path[depth++] = a;
                u = to[a];
            } else if (u == source) {
                return pushed;
            } else {
                level[u] = -1; // dead end for the rest of this phase
                u = to[path[--depth] ^ 1];
                current[u] = next[current[u]];
            }
        }
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IndexOutOfBoundsException("vertex: " + vertex);
        }
    }
}
