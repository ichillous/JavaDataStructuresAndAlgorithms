package io.github.ichillous.dsa.review;

import io.github.ichillous.dsa.graphs.AdvancedGraphAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import java.math.BigInteger;
import java.util.List;
import java.util.OptionalLong;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Independent signed-distance model, including exception precedence and range limits. */
class SignedShortestPathOracleTest {
    @Test
    void signedMultigraphsMatchArbitraryPrecisionFloydWarshall() {
        Random random = new Random(0xBE11F04DL);
        long[] weights = {Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MAX_VALUE,
                Long.MAX_VALUE - 1, -2, -1, 0, 1, 2};
        BigInteger minimum = BigInteger.valueOf(Long.MIN_VALUE);
        BigInteger maximum = BigInteger.valueOf(Long.MAX_VALUE);
        for (int trial = 0; trial < 250; trial++) {
            int size = 1 + random.nextInt(7);
            WeightedGraph graph = WeightedGraph.directed(size);
            BigInteger[][] expected = new BigInteger[size][size];
            for (int vertex = 0; vertex < size; vertex++) {
                expected[vertex][vertex] = BigInteger.ZERO;
            }
            for (int edge = 0; edge < size * 3; edge++) {
                int from = random.nextInt(size);
                int to = random.nextInt(size);
                if (trial % 2 == 0 && from >= to) {
                    continue; // Acyclic trials exercise finite answers and range errors.
                }
                long weight = weights[random.nextInt(weights.length)];
                graph.addEdge(from, to, weight);
                relax(expected, from, to, BigInteger.valueOf(weight));
            }
            for (int via = 0; via < size; via++) {
                for (int from = 0; from < size; from++) {
                    for (int to = 0; to < size; to++) {
                        if (expected[from][via] != null && expected[via][to] != null) {
                            relax(expected, from, to, expected[from][via].add(expected[via][to]));
                        }
                    }
                }
            }
            for (int start = 0; start < size; start++) {
                int source = start;
                boolean negativeCycle = false;
                boolean outsideRange = false;
                for (int target = 0; target < size; target++) {
                    BigInteger distance = expected[source][target];
                    negativeCycle |= distance != null && expected[target][target].signum() < 0;
                    outsideRange |= distance != null
                            && (distance.compareTo(minimum) < 0 || distance.compareTo(maximum) > 0);
                }
                String context = "trial " + trial + ", source " + source;
                if (negativeCycle) {
                    assertThrows(IllegalArgumentException.class,
                            () -> AdvancedGraphAlgorithms.bellmanFord(graph, source), context);
                } else if (outsideRange) {
                    assertThrows(ArithmeticException.class,
                            () -> AdvancedGraphAlgorithms.bellmanFord(graph, source), context);
                } else {
                    AdvancedGraphAlgorithms.ShortestPaths paths =
                            AdvancedGraphAlgorithms.bellmanFord(graph, source);
                    for (int target = 0; target < size; target++) {
                        BigInteger distance = expected[source][target];
                        assertEquals(distance == null ? OptionalLong.empty()
                                        : OptionalLong.of(distance.longValueExact()),
                                paths.distanceTo(target), context);
                        verifyPath(graph, source, target, distance, paths.pathTo(target));
                    }
                }
            }
        }
    }

    @Test
    void negativeCycleDetectionPrecedesFinalNumericRangeChecks() {
        WeightedGraph graph = WeightedGraph.directed(4)
                .addEdge(0, 1, Long.MIN_VALUE).addEdge(1, 2, -1)
                .addEdge(2, 3, 0).addEdge(3, 2, -1);
        assertThrows(IllegalArgumentException.class,
                () -> AdvancedGraphAlgorithms.bellmanFord(graph, 0));
    }

    @Test
    void extremeDetoursAndDisconnectedNegativeCyclesDoNotPoisonFiniteResults() {
        WeightedGraph graph = WeightedGraph.directed(6)
                .addEdge(0, 1, Long.MAX_VALUE).addEdge(1, 2, Long.MAX_VALUE)
                .addEdge(0, 2, Long.MIN_VALUE).addEdge(2, 3, Long.MAX_VALUE)
                .addEdge(4, 5, -1).addEdge(5, 4, 0);
        AdvancedGraphAlgorithms.ShortestPaths paths = AdvancedGraphAlgorithms.bellmanFord(graph, 0);
        assertEquals(OptionalLong.of(Long.MAX_VALUE), paths.distanceTo(1));
        assertEquals(OptionalLong.of(Long.MIN_VALUE), paths.distanceTo(2));
        assertEquals(OptionalLong.of(-1), paths.distanceTo(3));
        assertEquals(List.of(0, 2, 3), paths.pathTo(3));
        assertEquals(OptionalLong.empty(), paths.distanceTo(4));
        assertTrue(paths.pathTo(4).isEmpty());
    }

    private static void relax(BigInteger[][] distance, int from, int to, BigInteger candidate) {
        if (distance[from][to] == null || candidate.compareTo(distance[from][to]) < 0) {
            distance[from][to] = candidate;
        }
    }

    private static void verifyPath(WeightedGraph graph, int source, int target,
            BigInteger expected, List<Integer> path) {
        if (expected == null) {
            assertTrue(path.isEmpty());
            return;
        }
        assertEquals(source, path.get(0));
        assertEquals(target, path.get(path.size() - 1));
        assertEquals(path.size(), path.stream().distinct().count());
        BigInteger weight = BigInteger.ZERO;
        for (int index = 1; index < path.size(); index++) {
            int destination = path.get(index);
            long edgeWeight = graph.neighbors(path.get(index - 1)).stream()
                    .filter(edge -> edge.to() == destination)
                    .mapToLong(WeightedGraph.Edge::weight).min().orElseThrow();
            weight = weight.add(BigInteger.valueOf(edgeWeight));
        }
        assertEquals(expected, weight);
    }
}
