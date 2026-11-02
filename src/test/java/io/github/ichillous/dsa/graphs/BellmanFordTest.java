package io.github.ichillous.dsa.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalLong;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BellmanFordTest {
    @Test
    void signedDistancesAndPathsIncludeBothEndpoints() {
        WeightedGraph graph = WeightedGraph.directed(6).addEdge(0, 1, 6).addEdge(0, 2, 7)
                .addEdge(1, 2, 8).addEdge(1, 3, 5).addEdge(1, 4, -4)
                .addEdge(2, 3, -3).addEdge(2, 4, 9).addEdge(3, 1, -2)
                .addEdge(4, 0, 2).addEdge(4, 3, 7);
        AdvancedGraphAlgorithms.ShortestPaths paths = AdvancedGraphAlgorithms.bellmanFord(graph, 0);
        assertEquals(0, paths.source());
        assertEquals(OptionalLong.of(0), paths.distanceTo(0));
        assertEquals(OptionalLong.of(2), paths.distanceTo(1));
        assertEquals(OptionalLong.of(7), paths.distanceTo(2));
        assertEquals(OptionalLong.of(4), paths.distanceTo(3));
        assertEquals(OptionalLong.of(-2), paths.distanceTo(4));
        assertEquals(OptionalLong.empty(), paths.distanceTo(5));
        assertEquals(List.of(0, 2, 3, 1, 4), paths.pathTo(4));
        assertEquals(List.of(0), paths.pathTo(0));
        assertEquals(List.of(), paths.pathTo(5));
    }

    @Test
    void reverseOrderedChainRequiresSuccessivePasses() {
        WeightedGraph graph = WeightedGraph.directed(7);
        for (int from = 6; from > 0; from--) {
            graph.addEdge(from, from - 1, -1);
        }
        AdvancedGraphAlgorithms.ShortestPaths paths = AdvancedGraphAlgorithms.bellmanFord(graph, 6);
        assertEquals(OptionalLong.of(-6), paths.distanceTo(0));
        assertEquals(List.of(6, 5, 4, 3, 2, 1, 0), paths.pathTo(0));
    }

    @Test
    void reachableNegativeCyclesFailButUnreachableCyclesAreIgnored() {
        WeightedGraph graph = WeightedGraph.directed(6).addEdge(0, 1, -1)
                .addEdge(2, 3, -2).addEdge(3, 2, 1).addEdge(3, 4, 5).addEdge(5, 5, -1);
        AdvancedGraphAlgorithms.ShortestPaths paths = AdvancedGraphAlgorithms.bellmanFord(graph, 0);
        assertEquals(OptionalLong.of(-1), paths.distanceTo(1));
        assertEquals(OptionalLong.empty(), paths.distanceTo(2));
        assertEquals(OptionalLong.empty(), paths.distanceTo(5));
        graph.addEdge(1, 2, 0);
        assertThrows(IllegalArgumentException.class, () -> AdvancedGraphAlgorithms.bellmanFord(graph, 0));
    }

    @Test
    void negativeSelfLoopIsDetectedEvenOnOneVertex() {
        assertThrows(IllegalArgumentException.class, () -> AdvancedGraphAlgorithms.bellmanFord(
                WeightedGraph.directed(1).addEdge(0, 0, -1), 0));
        assertEquals(List.of(0), AdvancedGraphAlgorithms.bellmanFord(
                WeightedGraph.directed(1).addEdge(0, 0, 0).addEdge(0, 0, 1), 0).pathTo(0));
    }

    @Test
    void reachableUndirectedNegativeEdgeIsANegativeClosedWalk() {
        assertThrows(IllegalArgumentException.class, () -> AdvancedGraphAlgorithms.bellmanFord(
                WeightedGraph.undirected(3).addEdge(0, 1, 1).addEdge(1, 2, -1), 0));
        AdvancedGraphAlgorithms.ShortestPaths paths = AdvancedGraphAlgorithms.bellmanFord(
                WeightedGraph.undirected(4).addEdge(0, 1, 3).addEdge(2, 3, -1), 0);
        assertEquals(OptionalLong.of(3), paths.distanceTo(1));
        assertEquals(OptionalLong.empty(), paths.distanceTo(2));
    }

    @Test
    void parallelEdgesAndZeroCyclesDoNotCreatePredecessorLoops() {
        WeightedGraph graph = WeightedGraph.directed(5).addEdge(0, 1, 9).addEdge(0, 1, 2)
                .addEdge(1, 2, -2).addEdge(2, 1, 2).addEdge(2, 3, 0)
                .addEdge(3, 2, 0).addEdge(1, 1, 0).addEdge(3, 4, -1);
        AdvancedGraphAlgorithms.ShortestPaths paths = AdvancedGraphAlgorithms.bellmanFord(graph, 0);
        assertEquals(OptionalLong.of(-1), paths.distanceTo(4));
        assertEquals(List.of(0, 1, 2, 3, 4), paths.pathTo(4));
        assertEquals(paths.pathTo(4).size(), new HashSet<>(paths.pathTo(4)).size());
    }

    @Test
    void allLongDistancesIncludingMinusOneAndBothExtremesRemainDistinctFromAbsence() {
        WeightedGraph graph = WeightedGraph.directed(5).addEdge(0, 1, Long.MIN_VALUE)
                .addEdge(0, 2, Long.MAX_VALUE).addEdge(0, 3, -1);
        AdvancedGraphAlgorithms.ShortestPaths paths = AdvancedGraphAlgorithms.bellmanFord(graph, 0);
        assertEquals(OptionalLong.of(Long.MIN_VALUE), paths.distanceTo(1));
        assertEquals(OptionalLong.of(Long.MAX_VALUE), paths.distanceTo(2));
        assertEquals(OptionalLong.of(-1), paths.distanceTo(3));
        assertEquals(OptionalLong.empty(), paths.distanceTo(4));
    }

    @Test
    void intermediateOverflowingDetoursDoNotRejectRepresentableFinalDistances() {
        WeightedGraph graph = WeightedGraph.directed(5).addEdge(0, 1, Long.MAX_VALUE)
                .addEdge(0, 4, 1).addEdge(1, 2, Long.MAX_VALUE)
                .addEdge(2, 3, -Long.MAX_VALUE).addEdge(4, 2, 0);
        AdvancedGraphAlgorithms.ShortestPaths paths = AdvancedGraphAlgorithms.bellmanFord(graph, 0);
        assertEquals(OptionalLong.of(1), paths.distanceTo(2));
        assertEquals(OptionalLong.of(1 - Long.MAX_VALUE), paths.distanceTo(3));
        assertEquals(List.of(0, 4, 2, 3), paths.pathTo(3));
    }

    @Test
    void finalOutOfRangeShortestDistancesFailInEitherDirection() {
        assertThrows(ArithmeticException.class, () -> AdvancedGraphAlgorithms.bellmanFord(
                WeightedGraph.directed(3).addEdge(0, 1, Long.MAX_VALUE).addEdge(1, 2, 1), 0));
        assertThrows(ArithmeticException.class, () -> AdvancedGraphAlgorithms.bellmanFord(
                WeightedGraph.directed(3).addEdge(0, 1, Long.MIN_VALUE).addEdge(1, 2, -1), 0));
    }

    @Test
    void negativeCycleDetectionPrecedesFinalRangeChecking() {
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> AdvancedGraphAlgorithms.bellmanFord(WeightedGraph.directed(3)
                        .addEdge(0, 1, Long.MIN_VALUE).addEdge(1, 2, -1).addEdge(2, 1, 0), 0));
        assertTrue(failure.getMessage().contains("negative cycle"));
    }

    @Test
    void resultsAreImmutableAndDetachedAndAllVertexArgumentsAreChecked() {
        WeightedGraph graph = WeightedGraph.directed(3).addEdge(0, 1, 4);
        AdvancedGraphAlgorithms.ShortestPaths paths = AdvancedGraphAlgorithms.bellmanFord(graph, 0);
        graph.addEdge(0, 1, -1).addEdge(1, 2, -1);
        assertEquals(OptionalLong.of(4), paths.distanceTo(1));
        assertEquals(OptionalLong.empty(), paths.distanceTo(2));
        assertThrows(UnsupportedOperationException.class, () -> paths.pathTo(1).clear());
        assertThrows(IndexOutOfBoundsException.class, () -> paths.pathTo(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> paths.distanceTo(3));
        assertThrows(IndexOutOfBoundsException.class, () -> AdvancedGraphAlgorithms.bellmanFord(graph, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> AdvancedGraphAlgorithms.bellmanFord(graph, -1));
        assertThrows(IndexOutOfBoundsException.class,
                () -> AdvancedGraphAlgorithms.bellmanFord(WeightedGraph.directed(0), 0));
        assertThrows(NullPointerException.class, () -> AdvancedGraphAlgorithms.bellmanFord(null, 0));
    }

    @Test
    void seededSignedGraphsMatchExactFloydWarshallAndReachableCycleOracle() {
        Random random = new Random(0xBE11FAADL);
        for (int trial = 0; trial < 240; trial++) {
            int count = 1 + random.nextInt(7);
            WeightedGraph graph = random.nextBoolean() ? WeightedGraph.directed(count) : WeightedGraph.undirected(count);
            for (int edge = 0; edge < count * count; edge++) {
                if (random.nextInt(3) == 0) {
                    graph.addEdge(random.nextInt(count), random.nextInt(count), random.nextInt(15) - 5);
                }
            }
            verifyAgainstExactFloydWarshall(graph, "trial " + trial);
        }
    }

    @Test
    void seededPotentialGraphsExerciseNegativeEdgesWithoutNegativeCycles() {
        Random random = new Random(0xA07E471A1L);
        for (int trial = 0; trial < 140; trial++) {
            int count = 1 + random.nextInt(8);
            long[] potential = new long[count];
            for (int vertex = 0; vertex < count; vertex++) {
                potential[vertex] = random.nextInt(101) - 50;
            }
            WeightedGraph graph = WeightedGraph.directed(count);
            for (int edge = 0; edge < count * count; edge++) {
                if (random.nextBoolean()) {
                    int from = random.nextInt(count);
                    int to = random.nextInt(count);
                    // Every closed walk telescopes to a nonnegative sum.
                    graph.addEdge(from, to, random.nextInt(11) + potential[to] - potential[from]);
                }
            }
            verifyAgainstExactFloydWarshall(graph, "potential trial " + trial);
        }
    }

    private static void verifyAgainstExactFloydWarshall(WeightedGraph graph, String context) {
        int count = graph.vertexCount();
        BigInteger[][] expected = new BigInteger[count][count];
        for (int vertex = 0; vertex < count; vertex++) {
            expected[vertex][vertex] = BigInteger.ZERO;
            for (WeightedGraph.Edge edge : graph.neighbors(vertex)) {
                BigInteger weight = BigInteger.valueOf(edge.weight());
                if (expected[vertex][edge.to()] == null || weight.compareTo(expected[vertex][edge.to()]) < 0) {
                    expected[vertex][edge.to()] = weight;
                }
            }
        }
        for (int via = 0; via < count; via++) {
            for (int from = 0; from < count; from++) {
                for (int to = 0; to < count; to++) {
                    if (expected[from][via] != null && expected[via][to] != null) {
                        BigInteger candidate = expected[from][via].add(expected[via][to]);
                        if (expected[from][to] == null || candidate.compareTo(expected[from][to]) < 0) {
                            expected[from][to] = candidate;
                        }
                    }
                }
            }
        }
        for (int source = 0; source < count; source++) {
            boolean reachesNegativeCycle = false;
            for (int vertex = 0; vertex < count; vertex++) {
                reachesNegativeCycle |= expected[source][vertex] != null
                        && expected[vertex][vertex].signum() < 0;
            }
            int start = source;
            if (reachesNegativeCycle) {
                assertThrows(IllegalArgumentException.class,
                        () -> AdvancedGraphAlgorithms.bellmanFord(graph, start), context + ", source " + source);
                continue;
            }
            AdvancedGraphAlgorithms.ShortestPaths actual = AdvancedGraphAlgorithms.bellmanFord(graph, source);
            for (int target = 0; target < count; target++) {
                if (expected[source][target] == null) {
                    assertEquals(OptionalLong.empty(), actual.distanceTo(target), context);
                    assertEquals(List.of(), actual.pathTo(target), context);
                    continue;
                }
                assertEquals(OptionalLong.of(expected[source][target].longValueExact()), actual.distanceTo(target), context);
                List<Integer> path = actual.pathTo(target);
                assertEquals(source, path.get(0), context);
                assertEquals(target, path.get(path.size() - 1), context);
                assertEquals(path.size(), new HashSet<>(path).size(), context);
                BigInteger sum = BigInteger.ZERO;
                for (int step = 1; step < path.size(); step++) {
                    int next = path.get(step);
                    long weight = graph.neighbors(path.get(step - 1)).stream().filter(edge -> edge.to() == next)
                            .mapToLong(WeightedGraph.Edge::weight).min().orElseThrow();
                    sum = sum.add(BigInteger.valueOf(weight));
                }
                assertEquals(expected[source][target], sum, context);
            }
        }
    }
}
