package io.github.ichillous.dsa.graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Maximum-cardinality matching in an unweighted bipartite graph with the
 * Hopcroft-Karp algorithm. Left vertices are {@code [0, leftSize)} and right
 * vertices are {@code [0, rightSize)}; the two sides are numbered separately.
 */
public final class BipartiteMatching {
    /**
     * An edge between left vertex {@code left} and right vertex {@code right}.
     * Range checking against the side sizes happens in
     * {@link #maximumMatching(int, int, Collection)}.
     *
     * @param left left-side vertex
     * @param right right-side vertex
     */
    public record Pair(int left, int right) {}

    private BipartiteMatching() {}

    /**
     * Returns a maximum matching.
     *
     * <p>Invariant: the matching is valid after every augmentation, and a phase
     * augments along a maximal set of vertex-disjoint shortest augmenting paths,
     * so the shortest augmenting path length strictly grows and there are
     * O(sqrt(V)) phases. The BFS stops at the first layer that reaches a free
     * right vertex, and the DFS follows only layered edges and accepts a free
     * right vertex only at that layer. A matching with no augmenting path is maximum (Berge).
     * The search is iterative; path length is not limited by the call stack.
     *
     * <p>Duplicate edges are ignored. Which maximum matching is returned is
     * deterministic (free left vertices and edges are scanned in ascending order)
     * but not otherwise specified. The input collection is not mutated.
     *
     * <p>Time O(E sqrt(V)) after an O(E log E) sort for deduplication; auxiliary
     * space O(V + E).
     *
     * @param leftSize number of left vertices, at least zero
     * @param rightSize number of right vertices, at least zero
     * @param edges candidate edges; the collection and its elements must be non-null
     * @return matched pairs, sorted by {@code left}; each vertex appears at most once
     * @throws NullPointerException if {@code edges} or an element is null
     * @throws IllegalArgumentException if a size is negative
     * @throws IndexOutOfBoundsException if an edge endpoint is out of range
     */
    public static List<Pair> maximumMatching(int leftSize, int rightSize, Collection<Pair> edges) {
        Objects.requireNonNull(edges, "edges");
        if (leftSize < 0 || rightSize < 0) {
            throw new IllegalArgumentException("sizes must be nonnegative");
        }
        Pair[] sorted = edges.toArray(new Pair[0]);
        for (Pair p : sorted) {
            Objects.requireNonNull(p, "edge");
            if (p.left() < 0 || p.left() >= leftSize) {
                throw new IndexOutOfBoundsException("left vertex: " + p.left());
            }
            if (p.right() < 0 || p.right() >= rightSize) {
                throw new IndexOutOfBoundsException("right vertex: " + p.right());
            }
        }
        Arrays.sort(sorted, Comparator.comparingInt(Pair::left).thenComparingInt(Pair::right));

        // CSR adjacency over deduplicated edges.
        int[] start = new int[leftSize + 1];
        int[] adj = new int[sorted.length];
        int count = 0;
        Pair previous = null;
        for (Pair p : sorted) {
            if (p.equals(previous)) {
                continue;
            }
            previous = p;
            adj[count++] = p.right();
            start[p.left() + 1]++;
        }
        for (int i = 0; i < leftSize; i++) {
            start[i + 1] += start[i];
        }

        int[] matchLeft = new int[leftSize];
        int[] matchRight = new int[rightSize];
        Arrays.fill(matchLeft, -1);
        Arrays.fill(matchRight, -1);
        int[] dist = new int[leftSize];
        int[] queue = new int[leftSize];
        int[] cursor = new int[leftSize];
        int[] stack = new int[leftSize];
        int shortest;
        while ((shortest = layer(leftSize, start, adj, matchLeft, matchRight, dist, queue)) >= 0) {
            System.arraycopy(start, 0, cursor, 0, leftSize);
            for (int root = 0; root < leftSize; root++) {
                if (matchLeft[root] == -1) {
                    augment(root, shortest, start, adj, matchLeft, matchRight, dist, cursor, stack);
                }
            }
        }

        List<Pair> result = new ArrayList<>();
        for (int u = 0; u < leftSize; u++) {
            if (matchLeft[u] != -1) {
                result.add(new Pair(u, matchLeft[u]));
            }
        }
        return List.copyOf(result);
    }

    /**
     * BFS from all free left vertices. Returns the number of left-vertex layers D
     * (0-based layer of the earliest left vertex adjacent to a free right vertex),
     * i.e. the shortest augmenting path has D + 1 matched-edge hops, or -1 if no
     * augmenting path exists. Expansion stops after layer D.
     */
    private static int layer(int leftSize, int[] start, int[] adj, int[] matchLeft,
            int[] matchRight, int[] dist, int[] queue) {
        int unreached = Integer.MAX_VALUE;
        int front = 0;
        int back = 0;
        for (int u = 0; u < leftSize; u++) {
            if (matchLeft[u] == -1) {
                dist[u] = 0;
                queue[back++] = u;
            } else {
                dist[u] = unreached;
            }
        }
        int shortest = unreached;
        while (front < back) {
            int u = queue[front++];
            if (dist[u] > shortest) {
                break; // deeper layers cannot start a shortest augmenting path
            }
            for (int i = start[u]; i < start[u + 1]; i++) {
                int w = matchRight[adj[i]];
                if (w == -1) {
                    shortest = dist[u]; // BFS order: the first hit is the minimum
                } else if (dist[w] == unreached && dist[u] < shortest) {
                    dist[w] = dist[u] + 1;
                    queue[back++] = w;
                }
            }
        }
        return shortest == unreached ? -1 : shortest;
    }

    /**
     * Iterative DFS along layered alternating paths (dist increases by exactly one
     * per step) from a free left root; a free right vertex is accepted only from a
     * left vertex in layer {@code shortest}, so only shortest paths are augmented.
     */
    private static void augment(int root, int shortest, int[] start, int[] adj,
            int[] matchLeft, int[] matchRight, int[] dist, int[] cursor, int[] stack) {
        int depth = 0;
        stack[depth++] = root;
        while (depth > 0) {
            int u = stack[depth - 1];
            if (cursor[u] < start[u + 1]) {
                int v = adj[cursor[u]];
                int w = matchRight[v];
                if (w == -1 && dist[u] == shortest) {
                    // Flip the path: each stacked vertex takes the right vertex at its cursor.
                    for (int i = depth - 1; i >= 0; i--) {
                        int x = stack[i];
                        int r = adj[cursor[x]];
                        matchLeft[x] = r;
                        matchRight[r] = x;
                    }
                    return;
                }
                if (w != -1 && dist[u] < shortest && dist[w] == dist[u] + 1) {
                    stack[depth++] = w;
                } else {
                    cursor[u]++;
                }
            } else {
                dist[u] = Integer.MAX_VALUE; // dead end for this phase
                depth--;
                if (depth > 0) {
                    cursor[stack[depth - 1]]++;
                }
            }
        }
    }
}
