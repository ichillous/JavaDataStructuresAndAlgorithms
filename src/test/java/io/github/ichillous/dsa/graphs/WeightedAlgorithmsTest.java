package io.github.ichillous.dsa.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalLong;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WeightedAlgorithmsTest {
    @Test
    void dijkstraFindsShortestPathsAndDiscardsStaleHeapEntries() {
        WeightedGraph graph = WeightedGraph.directed(6).addEdge(0, 1, 20)
                .addEdge(0, 2, 2).addEdge(2, 1, 3).addEdge(1, 3, 4)
                .addEdge(2, 3, 100).addEdge(3, 4, 0);
        GraphAlgorithms.ShortestPaths paths = GraphAlgorithms.dijkstra(graph, 0);
        assertEquals(0, paths.source());
        assertEquals(OptionalLong.of(0), paths.distanceTo(0));
        assertEquals(OptionalLong.of(5), paths.distanceTo(1));
        assertEquals(OptionalLong.of(9), paths.distanceTo(4));
        assertEquals(OptionalLong.empty(), paths.distanceTo(5));
        assertEquals(List.of(0), paths.pathTo(0));
        assertEquals(List.of(0, 2, 1, 3, 4), paths.pathTo(4));
        assertEquals(List.of(), paths.pathTo(5));
    }

    @Test
    void zeroWeightCyclesLoopsAndParallelEdgesDoNotCorruptPredecessors() {
        WeightedGraph graph = WeightedGraph.undirected(4).addEdge(0, 0, 0)
                .addEdge(0, 1, 7).addEdge(0, 1, 0).addEdge(1, 2, 0)
                .addEdge(2, 0, 0).addEdge(2, 3, 4);
        GraphAlgorithms.ShortestPaths paths = GraphAlgorithms.dijkstra(graph, 0);
        assertEquals(OptionalLong.of(0), paths.distanceTo(2));
        assertEquals(OptionalLong.of(4), paths.distanceTo(3));
        assertEquals(List.of(0, 2, 3), paths.pathTo(3));
        assertEquals(paths.pathTo(3).size(), new HashSet<>(paths.pathTo(3)).size());
    }

    @Test
    void longMaxDistanceIsReachableAndZeroWeightContinuationWorks() {
        WeightedGraph graph = WeightedGraph.directed(4).addEdge(0, 1, Long.MAX_VALUE)
                .addEdge(1, 2, 0).addEdge(2, 0, 1);
        GraphAlgorithms.ShortestPaths paths = GraphAlgorithms.dijkstra(graph, 0);
        assertEquals(OptionalLong.of(Long.MAX_VALUE), paths.distanceTo(1));
        assertEquals(OptionalLong.of(Long.MAX_VALUE), paths.distanceTo(2));
        assertEquals(OptionalLong.empty(), paths.distanceTo(3));
        assertEquals(List.of(0, 1, 2), paths.pathTo(2));
    }

    @Test
    void overflowDetoursAreIgnoredButUnrepresentableShortestDistancesFail() {
        WeightedGraph overflow = WeightedGraph.directed(3).addEdge(0, 1, Long.MAX_VALUE).addEdge(1, 2, 1);
        assertThrows(ArithmeticException.class, () -> GraphAlgorithms.dijkstra(overflow, 0));
        overflow.addEdge(0, 2, 10);
        assertEquals(OptionalLong.of(10), GraphAlgorithms.dijkstra(overflow, 0).distanceTo(2));
    }

    @Test
    void negativeWeightsAreRejectedEvenInUnreachableComponents() {
        WeightedGraph graph = WeightedGraph.directed(4).addEdge(0, 1, 2).addEdge(2, 3, -1);
        assertThrows(IllegalArgumentException.class, () -> GraphAlgorithms.dijkstra(graph, 0));
    }

    @Test
    void shortestPathResultsAreImmutableSnapshots() {
        WeightedGraph graph = WeightedGraph.directed(3).addEdge(0, 1, 8);
        GraphAlgorithms.ShortestPaths paths = GraphAlgorithms.dijkstra(graph, 0);
        graph.addEdge(0, 1, 1).addEdge(1, 2, 1);
        assertEquals(OptionalLong.of(8), paths.distanceTo(1));
        assertEquals(OptionalLong.empty(), paths.distanceTo(2));
        assertThrows(UnsupportedOperationException.class, () -> paths.pathTo(1).clear());
        assertThrows(IndexOutOfBoundsException.class, () -> paths.distanceTo(3));
        assertThrows(IndexOutOfBoundsException.class, () -> paths.pathTo(-1));
    }

    @Test
    void weightedAlgorithmInputsAreValidated() {
        assertThrows(NullPointerException.class, () -> GraphAlgorithms.dijkstra(null, 0));
        assertThrows(NullPointerException.class, () -> GraphAlgorithms.minimumSpanningForest(null));
        assertThrows(IndexOutOfBoundsException.class, () -> GraphAlgorithms.dijkstra(WeightedGraph.directed(0), 0));
        assertThrows(IndexOutOfBoundsException.class, () -> GraphAlgorithms.dijkstra(WeightedGraph.directed(2), -1));
        assertThrows(IllegalArgumentException.class, () -> GraphAlgorithms.minimumSpanningForest(WeightedGraph.directed(1)));
    }

    @Test
    void kruskalSupportsDisconnectedGraphsLoopsParallelEdgesAndNegativeWeights() {
        WeightedGraph graph = WeightedGraph.undirected(7).addEdge(0, 0, -100)
                .addEdge(0, 1, 8).addEdge(0, 1, 2).addEdge(1, 2, -3)
                .addEdge(0, 2, 10).addEdge(3, 4, 4).addEdge(4, 5, 4).addEdge(3, 5, 20);
        GraphAlgorithms.SpanningForest forest = GraphAlgorithms.minimumSpanningForest(graph);
        assertEquals(7, forest.totalWeight());
        assertEquals(3, forest.components());
        assertEquals(List.of(new WeightedGraph.Edge(1, 2, -3), new WeightedGraph.Edge(0, 1, 2),
                new WeightedGraph.Edge(3, 4, 4), new WeightedGraph.Edge(4, 5, 4)), forest.edges());
        assertThrows(UnsupportedOperationException.class, () -> forest.edges().clear());
    }

    @Test
    void emptyForestAndIsolatedVerticesHaveZeroCost() {
        assertEquals(new GraphAlgorithms.SpanningForest(List.of(), 0, 0),
                GraphAlgorithms.minimumSpanningForest(WeightedGraph.undirected(0)));
        assertEquals(new GraphAlgorithms.SpanningForest(List.of(), 0, 3),
                GraphAlgorithms.minimumSpanningForest(WeightedGraph.undirected(3)));
    }

    @Test
    void spanningForestDefensivelyCopiesPublicConstructorInput() {
        List<WeightedGraph.Edge> edges = new ArrayList<>();
        edges.add(new WeightedGraph.Edge(0, 1, 4));
        GraphAlgorithms.SpanningForest result = new GraphAlgorithms.SpanningForest(edges, 4, 1);
        edges.clear();
        assertEquals(1, result.edges().size());
    }

    @Test
    void spanningForestChecksFinalLongOverflowWithoutRejectingCancellation() {
        WeightedGraph graph = WeightedGraph.undirected(4).addEdge(0, 1, Long.MIN_VALUE)
                .addEdge(1, 2, -1).addEdge(2, 3, Long.MAX_VALUE);
        assertEquals(-2, GraphAlgorithms.minimumSpanningForest(graph).totalWeight());
        assertThrows(ArithmeticException.class, () -> GraphAlgorithms.minimumSpanningForest(
                WeightedGraph.undirected(3).addEdge(0, 1, Long.MAX_VALUE).addEdge(1, 2, 1)));
        assertThrows(ArithmeticException.class, () -> GraphAlgorithms.minimumSpanningForest(
                WeightedGraph.undirected(3).addEdge(0, 1, Long.MIN_VALUE).addEdge(1, 2, -1)));
    }

    @Test
    void seededDijkstraMatchesIndependentFloydWarshallIncludingDisconnectedGraphs() {
        Random random = new Random(0xD1A57AL);
        long infinity = 1_000_000;
        for (int trial = 0; trial < 120; trial++) {
            int count = 1 + random.nextInt(8);
            boolean directed = random.nextBoolean();
            WeightedGraph graph = directed ? WeightedGraph.directed(count) : WeightedGraph.undirected(count);
            long[][] expected = new long[count][count];
            for (int vertex = 0; vertex < count; vertex++) {
                Arrays.fill(expected[vertex], infinity);
                expected[vertex][vertex] = 0;
            }
            for (int edge = 0; edge < count * count; edge++) {
                if (random.nextBoolean()) {
                    int from = random.nextInt(count);
                    int to = random.nextInt(count);
                    long weight = random.nextInt(30);
                    graph.addEdge(from, to, weight);
                    expected[from][to] = Math.min(expected[from][to], weight);
                    if (!directed) {
                        expected[to][from] = Math.min(expected[to][from], weight);
                    }
                }
            }
            for (int via = 0; via < count; via++) {
                for (int from = 0; from < count; from++) {
                    for (int to = 0; to < count; to++) {
                        expected[from][to] = Math.min(expected[from][to], expected[from][via] + expected[via][to]);
                    }
                }
            }
            for (int source = 0; source < count; source++) {
                GraphAlgorithms.ShortestPaths paths = GraphAlgorithms.dijkstra(graph, source);
                for (int target = 0; target < count; target++) {
                    if (expected[source][target] == infinity) {
                        assertEquals(OptionalLong.empty(), paths.distanceTo(target), "trial " + trial);
                        assertTrue(paths.pathTo(target).isEmpty());
                    } else {
                        assertEquals(OptionalLong.of(expected[source][target]), paths.distanceTo(target), "trial " + trial);
                        List<Integer> path = paths.pathTo(target);
                        assertEquals(source, path.get(0));
                        assertEquals(target, path.get(path.size() - 1));
                        long sum = 0;
                        for (int step = 1; step < path.size(); step++) {
                            int next = path.get(step);
                            long cheapest = graph.neighbors(path.get(step - 1)).stream()
                                    .filter(edge -> edge.to() == next).mapToLong(WeightedGraph.Edge::weight).min().orElseThrow();
                            sum += cheapest;
                        }
                        assertEquals(expected[source][target], sum);
                    }
                }
            }
        }
    }

    @Test
    void seededKruskalMatchesExhaustiveForestsOnSmallMultigraphs() {
        Random random = new Random(0xF0AE57L);
        for (int trial = 0; trial < 100; trial++) {
            int count = 1 + random.nextInt(5);
            WeightedGraph graph = WeightedGraph.undirected(count);
            int edges = random.nextInt(9);
            for (int edge = 0; edge < edges; edge++) {
                graph.addEdge(random.nextInt(count), random.nextInt(count), random.nextInt(21) - 10);
            }
            GraphAlgorithms.SpanningForest actual = GraphAlgorithms.minimumSpanningForest(graph);
            List<Set<Integer>> originalComponents = components(count, graph.edges());
            assertEquals(originalComponents.size(), actual.components(), "trial " + trial);
            assertEquals(count - actual.components(), actual.edges().size());
            assertEquals(new HashSet<>(originalComponents), new HashSet<>(components(count, actual.edges())));
            long best = Long.MAX_VALUE;
            List<WeightedGraph.Edge> all = graph.edges();
            for (int mask = 0; mask < (1 << all.size()); mask++) {
                if (Integer.bitCount(mask) != count - originalComponents.size()) {
                    continue;
                }
                List<WeightedGraph.Edge> chosen = new ArrayList<>();
                long sum = 0;
                for (int edge = 0; edge < all.size(); edge++) {
                    if ((mask & (1 << edge)) != 0) {
                        chosen.add(all.get(edge));
                        sum += all.get(edge).weight();
                    }
                }
                if (new HashSet<>(components(count, chosen)).equals(new HashSet<>(originalComponents))) {
                    best = Math.min(best, sum);
                }
            }
            assertFalse(best == Long.MAX_VALUE);
            assertEquals(best, actual.totalWeight(), "trial " + trial);
        }
    }

    private static List<Set<Integer>> components(int count, List<WeightedGraph.Edge> edges) {
        // Transitive closure is independent of Kruskal's union-find implementation.
        boolean[][] connected = new boolean[count][count];
        for (int vertex = 0; vertex < count; vertex++) {
            connected[vertex][vertex] = true;
        }
        for (WeightedGraph.Edge edge : edges) {
            connected[edge.from()][edge.to()] = true;
            connected[edge.to()][edge.from()] = true;
        }
        for (int via = 0; via < count; via++) {
            for (int from = 0; from < count; from++) {
                for (int to = 0; to < count; to++) {
                    connected[from][to] |= connected[from][via] && connected[via][to];
                }
            }
        }
        Set<Integer> seen = new HashSet<>();
        List<Set<Integer>> result = new ArrayList<>();
        for (int first = 0; first < count; first++) {
            if (!seen.contains(first)) {
                Set<Integer> group = new HashSet<>();
                for (int next = 0; next < count; next++) {
                    if (connected[first][next]) {
                        group.add(next);
                        seen.add(next);
                    }
                }
                result.add(group);
            }
        }
        return result;
    }
}
