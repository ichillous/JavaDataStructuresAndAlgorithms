package io.github.ichillous.dsa.graphs;

import io.github.ichillous.dsa.graphs.ShortestPathAlgorithms.AllPairs;
import io.github.ichillous.dsa.graphs.ShortestPathAlgorithms.PathTree;
import io.github.ichillous.dsa.graphs.ShortestPathAlgorithms.Route;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShortestPathAlgorithmsTest {
    /** Cheapest parallel edge weight between consecutive vertices, summed exactly. */
    private static long pathWeight(WeightedGraph graph, List<Integer> path) {
        long total = 0;
        for (int index = 1; index < path.size(); index++) {
            int from = path.get(index - 1);
            int to = path.get(index);
            long best = Long.MAX_VALUE;
            boolean found = false;
            for (WeightedGraph.Edge edge : graph.neighbors(from)) {
                if (edge.to() == to && (!found || edge.weight() < best)) {
                    best = edge.weight();
                    found = true;
                }
            }
            assertTrue(found, "missing edge " + from + "->" + to);
            total = Math.addExact(total, best);
        }
        return total;
    }

    private static void assertPathMatches(WeightedGraph graph, int source, int target,
            OptionalLong expected, List<Integer> path, String context) {
        if (expected.isEmpty()) {
            assertTrue(path.isEmpty(), context);
            return;
        }
        assertEquals(source, path.get(0), context);
        assertEquals(target, path.get(path.size() - 1), context);
        assertEquals(expected.getAsLong(), pathWeight(graph, path), context);
        assertEquals(path.size(), path.stream().distinct().count(), context);
        assertThrows(UnsupportedOperationException.class, () -> path.add(0), context);
    }

    private static List<String> snapshot(WeightedGraph graph) {
        List<String> state = new ArrayList<>();
        graph.edges().forEach(edge -> state.add(edge.toString()));
        state.add(graph.vertexCount() + "/" + graph.isDirected());
        return state;
    }

    @Test
    void zeroOneBfsMatchesDijkstra() {
        long seed = 0x01B;
        Random random = new Random(seed);
        for (int trial = 0; trial < 300; trial++) {
            int size = 1 + random.nextInt(12);
            WeightedGraph graph = trial % 3 == 0 ? WeightedGraph.undirected(size) : WeightedGraph.directed(size);
            for (int edge = 0; edge < size * 2; edge++) {
                graph.addEdge(random.nextInt(size), random.nextInt(size), random.nextInt(2));
            }
            List<String> before = snapshot(graph);
            int source = random.nextInt(size);
            PathTree actual = ShortestPathAlgorithms.zeroOneBfs(graph, source);
            GraphAlgorithms.ShortestPaths expected = GraphAlgorithms.dijkstra(graph, source);
            String context = "seed " + seed + ", trial " + trial;
            assertEquals(source, actual.source());
            for (int vertex = 0; vertex < size; vertex++) {
                assertEquals(expected.distanceTo(vertex), actual.distanceTo(vertex), context);
                assertPathMatches(graph, source, vertex, expected.distanceTo(vertex),
                        actual.pathTo(vertex), context);
            }
            assertEquals(before, snapshot(graph), context);
        }
        WeightedGraph bad = WeightedGraph.directed(2).addEdge(0, 1, 2);
        assertThrows(IllegalArgumentException.class, () -> ShortestPathAlgorithms.zeroOneBfs(bad, 0));
        WeightedGraph negative = WeightedGraph.directed(3).addEdge(0, 1, 1).addEdge(2, 2, -1);
        assertThrows(IllegalArgumentException.class, () -> ShortestPathAlgorithms.zeroOneBfs(negative, 0));
    }

    @Test
    void zeroOneBfsUsesZeroEdgesAheadOfLongerHops() {
        WeightedGraph graph = WeightedGraph.directed(5)
                .addEdge(0, 1, 1).addEdge(0, 2, 0).addEdge(2, 3, 0).addEdge(3, 1, 0);
        PathTree tree = ShortestPathAlgorithms.zeroOneBfs(graph, 0);
        assertEquals(OptionalLong.of(0), tree.distanceTo(1));
        assertEquals(List.of(0, 2, 3, 1), tree.pathTo(1));
        assertEquals(OptionalLong.empty(), tree.distanceTo(4));
        assertEquals(List.of(), tree.pathTo(4));
        assertEquals(List.of(0), tree.pathTo(0));
        assertThrows(IndexOutOfBoundsException.class, () -> tree.distanceTo(5));
        assertThrows(IndexOutOfBoundsException.class, () -> tree.pathTo(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> ShortestPathAlgorithms.zeroOneBfs(graph, 9));
        assertThrows(NullPointerException.class, () -> ShortestPathAlgorithms.zeroOneBfs(null, 0));
    }

    @Test
    void dagShortestPathsMatchesBellmanFord() {
        long seed = 0xDA6;
        Random random = new Random(seed);
        for (int trial = 0; trial < 300; trial++) {
            int size = 1 + random.nextInt(12);
            List<Integer> labels = new ArrayList<>();
            for (int v = 0; v < size; v++) {
                labels.add(v);
            }
            Collections.shuffle(labels, random);
            WeightedGraph graph = WeightedGraph.directed(size);
            for (int edge = 0; edge < size * 3; edge++) {
                int a = random.nextInt(size);
                int b = random.nextInt(size);
                if (a != b) {
                    graph.addEdge(labels.get(Math.min(a, b)), labels.get(Math.max(a, b)),
                            random.nextInt(21) - 10);
                }
            }
            List<String> before = snapshot(graph);
            int source = random.nextInt(size);
            PathTree actual = ShortestPathAlgorithms.dagShortestPaths(graph, source);
            AdvancedGraphAlgorithms.ShortestPaths expected = AdvancedGraphAlgorithms.bellmanFord(graph, source);
            String context = "seed " + seed + ", trial " + trial;
            for (int vertex = 0; vertex < size; vertex++) {
                assertEquals(expected.distanceTo(vertex), actual.distanceTo(vertex), context);
                assertPathMatches(graph, source, vertex, expected.distanceTo(vertex),
                        actual.pathTo(vertex), context);
            }
            assertEquals(before, snapshot(graph), context);
        }
    }

    @Test
    void dagShortestPathsRejectsCyclesAndUndirectedGraphs() {
        WeightedGraph cycle = WeightedGraph.directed(4).addEdge(1, 2, 1).addEdge(2, 3, 1).addEdge(3, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> ShortestPathAlgorithms.dagShortestPaths(cycle, 0));
        WeightedGraph loop = WeightedGraph.directed(1).addEdge(0, 0, 5);
        assertThrows(IllegalArgumentException.class, () -> ShortestPathAlgorithms.dagShortestPaths(loop, 0));
        WeightedGraph undirected = WeightedGraph.undirected(2).addEdge(0, 1, 1);
        assertThrows(IllegalArgumentException.class,
                () -> ShortestPathAlgorithms.dagShortestPaths(undirected, 0));
        assertThrows(IndexOutOfBoundsException.class,
                () -> ShortestPathAlgorithms.dagShortestPaths(WeightedGraph.directed(1), 1));
    }

    @Test
    void dagShortestPathsIsExactAtLongBoundaries() {
        WeightedGraph detours = WeightedGraph.directed(5)
                .addEdge(0, 1, Long.MAX_VALUE).addEdge(1, 2, Long.MAX_VALUE)
                .addEdge(0, 2, Long.MIN_VALUE).addEdge(2, 3, Long.MAX_VALUE);
        PathTree tree = ShortestPathAlgorithms.dagShortestPaths(detours, 0);
        assertEquals(OptionalLong.of(Long.MIN_VALUE), tree.distanceTo(2));
        assertEquals(OptionalLong.of(-1), tree.distanceTo(3));
        assertEquals(OptionalLong.empty(), tree.distanceTo(4));
        WeightedGraph overflow = WeightedGraph.directed(3)
                .addEdge(0, 1, Long.MAX_VALUE).addEdge(1, 2, 1);
        assertThrows(ArithmeticException.class, () -> ShortestPathAlgorithms.dagShortestPaths(overflow, 0));
        // The same graph is fine from a source that cannot reach the overflowing vertex.
        assertEquals(OptionalLong.of(1), ShortestPathAlgorithms.dagShortestPaths(overflow, 1).distanceTo(2));
    }

    private static WeightedGraph grid(Random random, int side, double blocked, boolean[] open) {
        WeightedGraph graph = WeightedGraph.directed(side * side);
        for (int cell = 0; cell < open.length; cell++) {
            open[cell] = random.nextDouble() >= blocked;
        }
        int[][] moves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int row = 0; row < side; row++) {
            for (int col = 0; col < side; col++) {
                int cell = row * side + col;
                for (int[] move : moves) {
                    int r = row + move[0];
                    int c = col + move[1];
                    if (r >= 0 && r < side && c >= 0 && c < side && open[cell] && open[r * side + c]) {
                        graph.addEdge(cell, r * side + c, 1);
                    }
                }
            }
        }
        return graph;
    }

    @Test
    void aStarMatchesDijkstraWithManhattanOnGrid() {
        long seed = 0xA57A;
        Random random = new Random(seed);
        int side = 30;
        int found = 0;
        for (int trial = 0; trial < 40; trial++) {
            boolean[] open = new boolean[side * side];
            WeightedGraph graph = grid(random, side, 0.25, open);
            int source = random.nextInt(side * side);
            int target = random.nextInt(side * side);
            int targetRow = target / side;
            int targetCol = target % side;
            List<String> before = snapshot(graph);
            Optional<Route> route = ShortestPathAlgorithms.aStar(graph, source, target,
                    v -> Math.abs(v / side - targetRow) + Math.abs(v % side - targetCol));
            OptionalLong expected = GraphAlgorithms.dijkstra(graph, source).distanceTo(target);
            String context = "seed " + seed + ", trial " + trial;
            assertEquals(expected.isPresent(), route.isPresent(), context);
            if (route.isPresent()) {
                found++;
                assertEquals(expected.getAsLong(), route.get().distance(), context);
                assertPathMatches(graph, source, target, expected, route.get().path(), context);
            }
            assertEquals(before, snapshot(graph), context);
        }
        assertTrue(found > 0, "seed " + seed + " produced no reachable pair");
    }

    @Test
    void aStarHandlesTrivialAndUnreachableTargets() {
        WeightedGraph graph = WeightedGraph.directed(3).addEdge(0, 1, 4);
        Route same = ShortestPathAlgorithms.aStar(graph, 1, 1, v -> 0).orElseThrow();
        assertEquals(List.of(1), same.path());
        assertEquals(0, same.distance());
        assertTrue(ShortestPathAlgorithms.aStar(graph, 0, 2, v -> 0).isEmpty());
        assertEquals(4, ShortestPathAlgorithms.aStar(graph, 0, 1, v -> 0).orElseThrow().distance());
        assertThrows(IndexOutOfBoundsException.class, () -> ShortestPathAlgorithms.aStar(graph, 0, 3, v -> 0));
        assertThrows(NullPointerException.class, () -> ShortestPathAlgorithms.aStar(graph, 0, 1, null));
        WeightedGraph negative = WeightedGraph.directed(2).addEdge(0, 1, -1);
        assertThrows(IllegalArgumentException.class, () -> ShortestPathAlgorithms.aStar(negative, 0, 1, v -> 0));
    }

    @Test
    void aStarRejectsInconsistentHeuristic() {
        WeightedGraph path = WeightedGraph.directed(3).addEdge(0, 1, 1).addEdge(1, 2, 1);
        // h(0) = 5 > w(0,1) + h(1) = 1.
        assertThrows(IllegalArgumentException.class,
                () -> ShortestPathAlgorithms.aStar(path, 0, 2, v -> v == 0 ? 5 : 0));
        // h(1) = 2 > w(1,2) + h(2) = 1, so the edge check fires when vertex 1 is expanded.
        assertThrows(IllegalArgumentException.class,
                () -> ShortestPathAlgorithms.aStar(path, 0, 2, v -> v == 1 ? 2 : 0));
        assertThrows(IllegalArgumentException.class,
                () -> ShortestPathAlgorithms.aStar(path, 0, 2, v -> v == 1 ? -1 : 0));
        assertThrows(IllegalArgumentException.class,
                () -> ShortestPathAlgorithms.aStar(path, 0, 2, v -> 1));
        // A violation behind an overflow-skipped edge is still reported.
        WeightedGraph huge = WeightedGraph.directed(3)
                .addEdge(0, 1, Long.MAX_VALUE).addEdge(1, 2, 1);
        assertThrows(IllegalArgumentException.class,
                () -> ShortestPathAlgorithms.aStar(huge, 0, 2, v -> v == 1 ? 7 : 0));
    }

    @Test
    void aStarFollowsDijkstraOverflowPolicy() {
        WeightedGraph graph = WeightedGraph.directed(4)
                .addEdge(0, 1, Long.MAX_VALUE).addEdge(1, 2, 1)
                .addEdge(0, 3, 5).addEdge(3, 2, Long.MAX_VALUE);
        assertThrows(ArithmeticException.class, () -> ShortestPathAlgorithms.aStar(graph, 0, 2, v -> 0));
        Route exact = ShortestPathAlgorithms.aStar(graph, 0, 1, v -> 0).orElseThrow();
        assertEquals(Long.MAX_VALUE, exact.distance());
        // Equal-cost alternatives near Long.MAX_VALUE keep the first improving relaxation.
        WeightedGraph tie = WeightedGraph.directed(4)
                .addEdge(0, 1, Long.MAX_VALUE - 1).addEdge(0, 2, 1).addEdge(2, 1, Long.MAX_VALUE - 2)
                .addEdge(1, 3, 0);
        Route route = ShortestPathAlgorithms.aStar(tie, 0, 3, v -> 0).orElseThrow();
        assertEquals(Long.MAX_VALUE - 1, route.distance());
        assertEquals(List.of(0, 1, 3), route.path());
    }

    @Test
    void aStarOrdersPrioritiesAboveLongMaxExactly() {
        // f(1) = (MAX - 5) + 10 > Long.MAX_VALUE, f(2) = MAX. Exact unsigned ordering pops the target
        // first and returns without scanning the inconsistent edge 1 -> 2. A saturated f ties the two and
        // the lower vertex number (1) expands first; a signed-wrapped f is negative and also pops 1 first.
        // Either mistake scans 1 -> 2 and throws IllegalArgumentException.
        WeightedGraph graph = WeightedGraph.directed(3)
                .addEdge(0, 1, Long.MAX_VALUE - 5).addEdge(0, 2, Long.MAX_VALUE).addEdge(1, 2, 1);
        Route route = ShortestPathAlgorithms.aStar(graph, 0, 2, v -> v == 1 ? 10 : 0).orElseThrow();
        assertEquals(List.of(0, 2), route.path());
        assertEquals(Long.MAX_VALUE, route.distance());
    }

    /** Edges are nonnegative base weights shifted by vertex potentials, so no negative cycle exists. */
    private static WeightedGraph signedWithoutNegativeCycles(Random random, int size) {
        long[] potential = new long[size];
        for (int v = 0; v < size; v++) {
            potential[v] = random.nextInt(21) - 10;
        }
        WeightedGraph graph = WeightedGraph.directed(size);
        for (int edge = 0; edge < size * 3; edge++) {
            int from = random.nextInt(size);
            int to = random.nextInt(size);
            graph.addEdge(from, to, random.nextInt(8) + potential[to] - potential[from]);
        }
        return graph;
    }

    @Test
    void floydWarshallMatchesBellmanFordFromEverySource() {
        long seed = 0xF10D;
        Random random = new Random(seed);
        for (int trial = 0; trial < 150; trial++) {
            int size = 1 + random.nextInt(9);
            WeightedGraph graph = signedWithoutNegativeCycles(random, size);
            List<String> before = snapshot(graph);
            AllPairs all = ShortestPathAlgorithms.floydWarshall(graph);
            assertEquals(size, all.vertexCount());
            for (int source = 0; source < size; source++) {
                AdvancedGraphAlgorithms.ShortestPaths expected = AdvancedGraphAlgorithms.bellmanFord(graph, source);
                for (int target = 0; target < size; target++) {
                    String context = "seed " + seed + ", trial " + trial + ", " + source + "->" + target;
                    assertEquals(expected.distanceTo(target), all.distance(source, target), context);
                    assertPathMatches(graph, source, target, expected.distanceTo(target),
                            all.path(source, target), context);
                }
            }
            assertEquals(before, snapshot(graph), "seed " + seed + ", trial " + trial);
        }
    }

    @Test
    void floydWarshallOnUndirectedGraphUsesBothDirections() {
        WeightedGraph graph = WeightedGraph.undirected(4).addEdge(0, 1, 2).addEdge(1, 2, 3).addEdge(0, 1, 7);
        AllPairs all = ShortestPathAlgorithms.floydWarshall(graph);
        assertEquals(OptionalLong.of(5), all.distance(2, 0));
        assertEquals(List.of(2, 1, 0), all.path(2, 0));
        assertEquals(OptionalLong.empty(), all.distance(0, 3));
        assertEquals(List.of(), all.path(3, 0));
        assertEquals(List.of(3), all.path(3, 3));
        assertEquals(OptionalLong.of(0), all.distance(3, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> all.distance(0, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> all.path(-1, 0));
    }

    @Test
    void floydWarshallRejectsNegativeCycleAndNegativeUndirectedEdge() {
        WeightedGraph cycle = WeightedGraph.directed(3).addEdge(0, 1, 1).addEdge(1, 2, -3).addEdge(2, 0, 1);
        assertThrows(IllegalArgumentException.class, () -> ShortestPathAlgorithms.floydWarshall(cycle));
        // The cycle is unreachable from vertex 0 but still rejected: all pairs covers every vertex.
        WeightedGraph remote = WeightedGraph.directed(4).addEdge(0, 1, 1).addEdge(2, 3, -1).addEdge(3, 2, 0);
        assertThrows(IllegalArgumentException.class, () -> ShortestPathAlgorithms.floydWarshall(remote));
        WeightedGraph undirected = WeightedGraph.undirected(2).addEdge(0, 1, -1);
        assertThrows(IllegalArgumentException.class, () -> ShortestPathAlgorithms.floydWarshall(undirected));
        WeightedGraph loop = WeightedGraph.directed(1).addEdge(0, 0, -1);
        assertThrows(IllegalArgumentException.class, () -> ShortestPathAlgorithms.floydWarshall(loop));
        WeightedGraph zeroCycle = WeightedGraph.directed(2).addEdge(0, 1, -2).addEdge(1, 0, 2);
        assertEquals(OptionalLong.of(-2), ShortestPathAlgorithms.floydWarshall(zeroCycle).distance(0, 1));
        assertThrows(NullPointerException.class, () -> ShortestPathAlgorithms.floydWarshall(null));
    }

    @Test
    void floydWarshallLongBoundary() {
        WeightedGraph fits = WeightedGraph.directed(3).addEdge(0, 1, Long.MAX_VALUE).addEdge(1, 2, -1);
        AllPairs all = ShortestPathAlgorithms.floydWarshall(fits);
        assertEquals(OptionalLong.of(Long.MAX_VALUE), all.distance(0, 1));
        assertEquals(OptionalLong.of(Long.MAX_VALUE - 1), all.distance(0, 2));
        assertEquals(List.of(0, 1, 2), all.path(0, 2));
        WeightedGraph overflow = WeightedGraph.directed(3).addEdge(0, 1, Long.MAX_VALUE).addEdge(1, 2, 1);
        assertThrows(ArithmeticException.class, () -> ShortestPathAlgorithms.floydWarshall(overflow));
        WeightedGraph underflow = WeightedGraph.directed(3).addEdge(0, 1, Long.MIN_VALUE).addEdge(1, 2, -1);
        assertThrows(ArithmeticException.class, () -> ShortestPathAlgorithms.floydWarshall(underflow));
        // An overflowing detour does not poison a representable shortest distance.
        WeightedGraph detour = WeightedGraph.directed(3)
                .addEdge(0, 1, Long.MAX_VALUE).addEdge(1, 2, Long.MAX_VALUE).addEdge(0, 2, Long.MIN_VALUE);
        assertEquals(OptionalLong.of(Long.MIN_VALUE), ShortestPathAlgorithms.floydWarshall(detour).distance(0, 2));
    }

    @Test
    void emptyAndSingletonGraphs() {
        WeightedGraph empty = WeightedGraph.directed(0);
        assertEquals(0, ShortestPathAlgorithms.floydWarshall(empty).vertexCount());
        assertThrows(IndexOutOfBoundsException.class, () -> ShortestPathAlgorithms.zeroOneBfs(empty, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> ShortestPathAlgorithms.dagShortestPaths(empty, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> ShortestPathAlgorithms.aStar(empty, 0, 0, v -> 0));
        WeightedGraph single = WeightedGraph.directed(1);
        PathTree bfs = ShortestPathAlgorithms.zeroOneBfs(single, 0);
        assertEquals(OptionalLong.of(0), bfs.distanceTo(0));
        assertEquals(List.of(0), bfs.pathTo(0));
        PathTree dag = ShortestPathAlgorithms.dagShortestPaths(single, 0);
        assertEquals(OptionalLong.of(0), dag.distanceTo(0));
        assertEquals(List.of(0), dag.pathTo(0));
        assertEquals(0, ShortestPathAlgorithms.aStar(single, 0, 0, v -> 0).orElseThrow().distance());
        AllPairs all = ShortestPathAlgorithms.floydWarshall(single);
        assertEquals(OptionalLong.of(0), all.distance(0, 0));
        assertEquals(List.of(0), all.path(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Route(List.of(), 0));
        assertThrows(NullPointerException.class, () -> new Route(null, 0));
    }

    @Test
    void longPathsDoNotOverflowTheStack() {
        int size = 100_000;
        WeightedGraph chain = WeightedGraph.directed(size);
        for (int v = 0; v + 1 < size; v++) {
            chain.addEdge(v, v + 1, v % 2);
        }
        PathTree bfs = ShortestPathAlgorithms.zeroOneBfs(chain, 0);
        assertEquals(size, bfs.pathTo(size - 1).size());
        assertEquals(OptionalLong.of((size - 1) / 2), bfs.distanceTo(size - 1));
        PathTree dag = ShortestPathAlgorithms.dagShortestPaths(chain, 0);
        assertEquals(bfs.distanceTo(size - 1), dag.distanceTo(size - 1));
        Route route = ShortestPathAlgorithms.aStar(chain, 0, size - 1, v -> 0).orElseThrow();
        assertEquals(size, route.path().size());
        assertEquals((size - 1) / 2, route.distance());
    }
}
