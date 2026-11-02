package io.github.ichillous.dsa.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ConnectivityAlgorithmsTest {
    @Test
    void sccReturnsCanonicalComponentsIncludingLoopsAndIsolatedVertices() {
        Graph graph = Graph.directed(9).addEdge(2, 0).addEdge(0, 1).addEdge(1, 2)
                .addEdge(2, 3).addEdge(3, 4).addEdge(4, 3).addEdge(4, 5)
                .addEdge(5, 5).addEdge(7, 8).addEdge(8, 7);
        assertEquals(List.of(List.of(0, 1, 2), List.of(3, 4), List.of(5), List.of(6), List.of(7, 8)),
                AdvancedGraphAlgorithms.stronglyConnectedComponents(graph));
    }

    @Test
    void sccHandlesEmptyAndEdgelessDirectedGraphs() {
        assertEquals(List.of(), AdvancedGraphAlgorithms.stronglyConnectedComponents(Graph.directed(0)));
        assertEquals(List.of(List.of(0), List.of(1), List.of(2)),
                AdvancedGraphAlgorithms.stronglyConnectedComponents(Graph.directed(3)));
    }

    @Test
    void sccCanonicalOrderIsIndependentOfEdgeInsertionOrder() {
        List<int[]> edges = new ArrayList<>(List.of(new int[] {5, 2}, new int[] {2, 5},
                new int[] {1, 0}, new int[] {0, 1}, new int[] {0, 5}, new int[] {3, 4}));
        Graph first = Graph.directed(6);
        for (int[] edge : edges) {
            first.addEdge(edge[0], edge[1]);
        }
        Collections.reverse(edges);
        Graph second = Graph.directed(6);
        for (int[] edge : edges) {
            second.addEdge(edge[0], edge[1]);
        }
        assertEquals(List.of(List.of(0, 1), List.of(2, 5), List.of(3), List.of(4)),
                AdvancedGraphAlgorithms.stronglyConnectedComponents(first));
        assertEquals(AdvancedGraphAlgorithms.stronglyConnectedComponents(first),
                AdvancedGraphAlgorithms.stronglyConnectedComponents(second));
    }

    @Test
    void sccResultsAreDeeplyImmutableDetachedSnapshots() {
        Graph graph = Graph.directed(2).addEdge(0, 1);
        List<List<Integer>> components = AdvancedGraphAlgorithms.stronglyConnectedComponents(graph);
        graph.addEdge(1, 0);
        assertEquals(List.of(List.of(0), List.of(1)), components);
        assertThrows(UnsupportedOperationException.class, components::clear);
        assertThrows(UnsupportedOperationException.class, () -> components.get(0).clear());
    }

    @Test
    void sccRejectsUndirectedAndNullGraphs() {
        assertThrows(IllegalArgumentException.class,
                () -> AdvancedGraphAlgorithms.stronglyConnectedComponents(Graph.undirected(0)));
        assertThrows(NullPointerException.class, () -> AdvancedGraphAlgorithms.stronglyConnectedComponents(null));
    }

    @Test
    void iterativeSccHandlesDeepChainAndLargeCycle() {
        int count = 30_000;
        Graph graph = Graph.directed(count);
        for (int vertex = 1; vertex < count; vertex++) {
            graph.addEdge(vertex - 1, vertex);
        }
        List<List<Integer>> chain = AdvancedGraphAlgorithms.stronglyConnectedComponents(graph);
        assertEquals(count, chain.size());
        assertEquals(List.of(0), chain.get(0));
        assertEquals(List.of(count - 1), chain.get(count - 1));
        graph.addEdge(count - 1, 0);
        List<List<Integer>> cycle = AdvancedGraphAlgorithms.stronglyConnectedComponents(graph);
        assertEquals(1, cycle.size());
        assertEquals(count, cycle.get(0).size());
        assertEquals(0, cycle.get(0).get(0));
        assertEquals(count - 1, cycle.get(0).get(count - 1));
    }

    @Test
    void seededSccMatchesIndependentMutualReachabilityOracle() {
        Random random = new Random(0x5CC0AACL);
        for (int trial = 0; trial < 250; trial++) {
            int count = random.nextInt(12);
            Graph graph = Graph.directed(count);
            boolean[][] reachable = new boolean[count][count];
            for (int vertex = 0; vertex < count; vertex++) {
                reachable[vertex][vertex] = true;
            }
            for (int edge = 0; edge < count * count; edge++) {
                if (random.nextInt(4) == 0) {
                    int from = random.nextInt(count);
                    int to = random.nextInt(count);
                    graph.addEdge(from, to);
                    reachable[from][to] = true;
                }
            }
            closeReachability(reachable);
            boolean[] assigned = new boolean[count];
            List<List<Integer>> expected = new ArrayList<>();
            for (int first = 0; first < count; first++) {
                if (!assigned[first]) {
                    List<Integer> component = new ArrayList<>();
                    for (int next = 0; next < count; next++) {
                        if (reachable[first][next] && reachable[next][first]) {
                            component.add(next);
                            assigned[next] = true;
                        }
                    }
                    expected.add(component);
                }
            }
            assertEquals(expected, AdvancedGraphAlgorithms.stronglyConnectedComponents(graph), "trial " + trial);
        }
    }

    @Test
    void bridgeDetectionSupportsCyclesLoopsAndDisconnectedGraphs() {
        Graph graph = Graph.undirected(10).addEdge(0, 1).addEdge(1, 2).addEdge(2, 0)
                .addEdge(2, 3).addEdge(3, 4).addEdge(4, 5).addEdge(5, 3)
                .addEdge(5, 5).addEdge(5, 6).addEdge(7, 8).addEdge(8, 7);
        assertEquals(List.of(new AdvancedGraphAlgorithms.Bridge(2, 3),
                new AdvancedGraphAlgorithms.Bridge(5, 6), new AdvancedGraphAlgorithms.Bridge(7, 8)),
                AdvancedGraphAlgorithms.bridges(graph));
    }

    @Test
    void noEdgesLoopsAndCompleteCyclesHaveNoBridges() {
        assertEquals(List.of(), AdvancedGraphAlgorithms.bridges(Graph.undirected(0)));
        assertEquals(List.of(), AdvancedGraphAlgorithms.bridges(Graph.undirected(3)));
        assertEquals(List.of(), AdvancedGraphAlgorithms.bridges(Graph.undirected(1).addEdge(0, 0)));
        assertEquals(List.of(), AdvancedGraphAlgorithms.bridges(
                Graph.undirected(3).addEdge(0, 1).addEdge(1, 2).addEdge(2, 0)));
    }

    @Test
    void bridgesAreCanonicalAndDuplicateSimpleEdgesStayOneEdge() {
        Graph graph = Graph.undirected(5).addEdge(4, 2).addEdge(1, 0).addEdge(2, 4).addEdge(0, 3);
        assertEquals(List.of(new AdvancedGraphAlgorithms.Bridge(0, 1),
                new AdvancedGraphAlgorithms.Bridge(0, 3), new AdvancedGraphAlgorithms.Bridge(2, 4)),
                AdvancedGraphAlgorithms.bridges(graph));
        assertEquals(new AdvancedGraphAlgorithms.Bridge(1, 4), new AdvancedGraphAlgorithms.Bridge(4, 1));
        assertThrows(IllegalArgumentException.class, () -> new AdvancedGraphAlgorithms.Bridge(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> new AdvancedGraphAlgorithms.Bridge(2, -1));
        assertThrows(IllegalArgumentException.class, () -> new AdvancedGraphAlgorithms.Bridge(2, 2));
    }

    @Test
    void bridgeResultsAreImmutableAndDetachedAndInputKindIsChecked() {
        Graph graph = Graph.undirected(3).addEdge(0, 1).addEdge(1, 2);
        List<AdvancedGraphAlgorithms.Bridge> bridges = AdvancedGraphAlgorithms.bridges(graph);
        graph.addEdge(2, 0);
        assertEquals(2, bridges.size());
        assertThrows(UnsupportedOperationException.class, bridges::clear);
        assertThrows(IllegalArgumentException.class, () -> AdvancedGraphAlgorithms.bridges(Graph.directed(0)));
        assertThrows(NullPointerException.class, () -> AdvancedGraphAlgorithms.bridges(null));
    }

    @Test
    void iterativeBridgeDetectionHandlesDeepPathsWithoutCallStackOverflow() {
        int count = 30_000;
        Graph graph = Graph.undirected(count);
        for (int vertex = 1; vertex < count; vertex++) {
            graph.addEdge(vertex - 1, vertex);
        }
        List<AdvancedGraphAlgorithms.Bridge> result = AdvancedGraphAlgorithms.bridges(graph);
        assertEquals(count - 1, result.size());
        assertEquals(new AdvancedGraphAlgorithms.Bridge(0, 1), result.get(0));
        assertEquals(new AdvancedGraphAlgorithms.Bridge(count - 2, count - 1), result.get(result.size() - 1));
        graph.addEdge(count - 1, 0);
        assertTrue(AdvancedGraphAlgorithms.bridges(graph).isEmpty());
    }

    @Test
    void seededBridgesMatchIndependentEdgeRemovalConnectivityOracle() {
        Random random = new Random(0xBA1D6EL);
        for (int trial = 0; trial < 250; trial++) {
            int count = random.nextInt(11);
            Graph graph = Graph.undirected(count);
            for (int edge = 0; edge < count * count; edge++) {
                if (random.nextInt(4) == 0) {
                    graph.addEdge(random.nextInt(count), random.nextInt(count));
                }
            }
            int baseline = componentCountWithoutEdge(graph, -1, -1);
            List<AdvancedGraphAlgorithms.Bridge> expected = new ArrayList<>();
            for (int from = 0; from < count; from++) {
                for (int to : graph.neighbors(from)) {
                    if (from < to && componentCountWithoutEdge(graph, from, to) > baseline) {
                        expected.add(new AdvancedGraphAlgorithms.Bridge(from, to));
                    }
                }
            }
            expected.sort(java.util.Comparator.comparingInt(AdvancedGraphAlgorithms.Bridge::first)
                    .thenComparingInt(AdvancedGraphAlgorithms.Bridge::second));
            assertEquals(expected, AdvancedGraphAlgorithms.bridges(graph), "trial " + trial);
        }
    }

    private static int componentCountWithoutEdge(Graph graph, int removedFirst, int removedSecond) {
        int count = graph.vertexCount();
        boolean[][] reachable = new boolean[count][count];
        for (int from = 0; from < count; from++) {
            reachable[from][from] = true;
            for (int to : graph.neighbors(from)) {
                if (!((from == removedFirst && to == removedSecond)
                        || (from == removedSecond && to == removedFirst))) {
                    reachable[from][to] = true;
                }
            }
        }
        closeReachability(reachable);
        int components = 0;
        for (int vertex = 0; vertex < count; vertex++) {
            boolean firstInComponent = true;
            for (int previous = 0; previous < vertex; previous++) {
                firstInComponent &= !reachable[vertex][previous];
            }
            if (firstInComponent) {
                components++;
            }
        }
        return components;
    }

    private static void closeReachability(boolean[][] reachable) {
        for (int via = 0; via < reachable.length; via++) {
            for (int from = 0; from < reachable.length; from++) {
                for (int to = 0; to < reachable.length; to++) {
                    reachable[from][to] |= reachable[from][via] && reachable[via][to];
                }
            }
        }
    }
}
