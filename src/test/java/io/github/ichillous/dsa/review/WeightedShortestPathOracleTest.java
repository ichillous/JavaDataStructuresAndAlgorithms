package io.github.ichillous.dsa.review;

import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import java.math.BigInteger;
import java.util.List;
import java.util.OptionalLong;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Independent arbitrary-precision oracle for the public long-distance contract. */
class WeightedShortestPathOracleTest {
    @Test
    void largeWeightsMatchBigIntegerFloydWarshall() {
        Random random = new Random(0x5A17C0DEL);
        long[] weights = {0, 1, 2, Long.MAX_VALUE, Long.MAX_VALUE - 1,
                Long.MAX_VALUE / 2, Long.MAX_VALUE / 2 + 1};
        BigInteger maximum = BigInteger.valueOf(Long.MAX_VALUE);
        for (int trial = 0; trial < 150; trial++) {
            int size = 2 + random.nextInt(6);
            WeightedGraph graph = random.nextBoolean()
                    ? WeightedGraph.directed(size) : WeightedGraph.undirected(size);
            BigInteger[][] expected = new BigInteger[size][size];
            for (int vertex = 0; vertex < size; vertex++) {
                expected[vertex][vertex] = BigInteger.ZERO;
            }
            for (int edge = 0; edge < size * 2; edge++) {
                int from = random.nextInt(size);
                int to = random.nextInt(size);
                long weight = weights[random.nextInt(weights.length)];
                graph.addEdge(from, to, weight);
                relax(expected, from, to, BigInteger.valueOf(weight));
                if (!graph.isDirected()) {
                    relax(expected, to, from, BigInteger.valueOf(weight));
                }
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
                boolean overflowing = false;
                for (BigInteger distance : expected[source]) {
                    overflowing |= distance != null && distance.compareTo(maximum) > 0;
                }
                if (overflowing) {
                    assertThrows(ArithmeticException.class, () -> GraphAlgorithms.dijkstra(graph, source),
                            "trial " + trial + ", source " + source);
                    continue;
                }
                GraphAlgorithms.ShortestPaths actual = GraphAlgorithms.dijkstra(graph, source);
                for (int target = 0; target < size; target++) {
                    BigInteger distance = expected[source][target];
                    assertEquals(distance == null ? OptionalLong.empty()
                                    : OptionalLong.of(distance.longValueExact()),
                            actual.distanceTo(target), "trial " + trial + ", source " + source);
                    verifyPath(graph, source, target, distance, actual.pathTo(target));
                }
            }
        }
    }

    @Test
    void overflowingDetoursDoNotHideRepresentableShortestPaths() {
        WeightedGraph graph = WeightedGraph.directed(4)
                .addEdge(0, 1, Long.MAX_VALUE)
                .addEdge(1, 2, Long.MAX_VALUE)
                .addEdge(0, 2, 7)
                .addEdge(2, 3, 0);
        GraphAlgorithms.ShortestPaths paths = GraphAlgorithms.dijkstra(graph, 0);
        assertEquals(OptionalLong.of(Long.MAX_VALUE), paths.distanceTo(1));
        assertEquals(OptionalLong.of(7), paths.distanceTo(3));
        assertEquals(List.of(0, 2, 3), paths.pathTo(3));
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
