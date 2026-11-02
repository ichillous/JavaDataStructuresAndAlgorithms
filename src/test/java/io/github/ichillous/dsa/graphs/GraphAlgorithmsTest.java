package io.github.ichillous.dsa.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GraphAlgorithmsTest {
    @Test
    void breadthFirstHandlesCyclesAndUsesInsertionOrder() {
        Graph graph = example();
        assertEquals(List.of(0, 2, 1, 3, 4), GraphAlgorithms.breadthFirst(graph, 0));
        assertEquals(List.of(5), GraphAlgorithms.breadthFirst(graph, 5));
    }

    @Test
    void depthFirstMatchesRecursiveInsertionOrder() {
        Graph graph = example();
        assertEquals(List.of(0, 2, 3, 4, 1), GraphAlgorithms.depthFirst(graph, 0));
        assertEquals(List.of(5), GraphAlgorithms.depthFirst(graph, 5));
    }

    @Test
    void traversalsDoNotUseTheJavaCallStackForLongChains() {
        int vertices = 20_000;
        Graph graph = Graph.directed(vertices);
        for (int vertex = 1; vertex < vertices; vertex++) {
            graph.addEdge(vertex - 1, vertex);
        }
        List<Integer> dfs = GraphAlgorithms.depthFirst(graph, 0);
        assertEquals(vertices, dfs.size());
        assertEquals(vertices - 1, dfs.get(vertices - 1));
        assertEquals(vertices, GraphAlgorithms.breadthFirst(graph, 0).size());
    }

    @Test
    void shortestPathIncludesEndpointsAndUnreachableIsEmpty() {
        Graph graph = example();
        assertEquals(List.of(0, 2, 3, 4), GraphAlgorithms.shortestPath(graph, 0, 4));
        assertEquals(List.of(0), GraphAlgorithms.shortestPath(graph, 0, 0));
        assertEquals(List.of(), GraphAlgorithms.shortestPath(graph, 0, 5));
        assertEquals(List.of(), GraphAlgorithms.shortestPath(graph, 4, 0));
    }

    @Test
    void componentsIncludeIsolatedVerticesAndRejectDirectedGraphs() {
        Graph graph = Graph.undirected(7).addEdge(0, 2).addEdge(2, 1).addEdge(3, 4).addEdge(6, 6);
        assertEquals(List.of(List.of(0, 2, 1), List.of(3, 4), List.of(5), List.of(6)),
                GraphAlgorithms.connectedComponents(graph));
        assertEquals(List.of(), GraphAlgorithms.connectedComponents(Graph.undirected(0)));
        assertThrows(IllegalArgumentException.class, () -> GraphAlgorithms.connectedComponents(Graph.directed(0)));
    }

    @Test
    void topologicalSortOrdersAllEdgesAndAllVertices() {
        Graph graph = Graph.directed(7).addEdge(0, 2).addEdge(1, 2).addEdge(2, 3)
                .addEdge(2, 4).addEdge(4, 5).addEdge(0, 2);
        List<Integer> order = GraphAlgorithms.topologicalSort(graph);
        assertEquals(7, new HashSet<>(order).size());
        for (int from = 0; from < graph.vertexCount(); from++) {
            for (int to : graph.neighbors(from)) {
                assertTrue(order.indexOf(from) < order.indexOf(to));
            }
        }
        assertEquals(List.of(), GraphAlgorithms.topologicalSort(Graph.directed(0)));
    }

    @Test
    void topologicalSortRejectsCyclesAndUndirectedGraphs() {
        assertThrows(IllegalArgumentException.class, () -> GraphAlgorithms.topologicalSort(
                Graph.directed(4).addEdge(1, 2).addEdge(2, 1)));
        assertThrows(IllegalArgumentException.class, () -> GraphAlgorithms.topologicalSort(
                Graph.directed(1).addEdge(0, 0)));
        assertThrows(IllegalArgumentException.class, () -> GraphAlgorithms.topologicalSort(Graph.undirected(0)));
    }

    @Test
    void returnedPathsOrdersAndComponentsAreImmutable() {
        Graph graph = Graph.undirected(2).addEdge(0, 1);
        List<Integer> bfs = GraphAlgorithms.breadthFirst(graph, 0);
        List<Integer> dfs = GraphAlgorithms.depthFirst(graph, 0);
        List<Integer> path = GraphAlgorithms.shortestPath(graph, 0, 1);
        List<List<Integer>> components = GraphAlgorithms.connectedComponents(graph);
        assertThrows(UnsupportedOperationException.class, bfs::clear);
        assertThrows(UnsupportedOperationException.class, dfs::clear);
        assertThrows(UnsupportedOperationException.class, path::clear);
        assertThrows(UnsupportedOperationException.class, components::clear);
        assertThrows(UnsupportedOperationException.class, () -> components.get(0).clear());
        assertThrows(UnsupportedOperationException.class, () -> GraphAlgorithms.topologicalSort(
                Graph.directed(1)).clear());
    }

    @Test
    void invalidVerticesAndNullGraphsFailClearly() {
        Graph graph = Graph.directed(2);
        assertThrows(IndexOutOfBoundsException.class, () -> GraphAlgorithms.breadthFirst(graph, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> GraphAlgorithms.depthFirst(graph, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> GraphAlgorithms.shortestPath(graph, 0, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> GraphAlgorithms.shortestPath(graph, -1, 0));
        assertThrows(NullPointerException.class, () -> GraphAlgorithms.breadthFirst(null, 0));
        assertThrows(NullPointerException.class, () -> GraphAlgorithms.depthFirst(null, 0));
        assertThrows(NullPointerException.class, () -> GraphAlgorithms.shortestPath(null, 0, 0));
        assertThrows(NullPointerException.class, () -> GraphAlgorithms.connectedComponents(null));
        assertThrows(NullPointerException.class, () -> GraphAlgorithms.topologicalSort(null));
    }

    @Test
    void seededTraversalsAndShortestPathsMatchFloydWarshall() {
        Random random = new Random(0xBFE51L);
        int infinity = 100_000;
        for (int trial = 0; trial < 100; trial++) {
            int count = 1 + random.nextInt(9);
            boolean directed = random.nextBoolean();
            Graph graph = directed ? Graph.directed(count) : Graph.undirected(count);
            int[][] distances = new int[count][count];
            for (int[] row : distances) {
                Arrays.fill(row, infinity);
            }
            for (int vertex = 0; vertex < count; vertex++) {
                distances[vertex][vertex] = 0;
            }
            for (int edge = 0; edge < count * count; edge++) {
                int from = random.nextInt(count);
                int to = random.nextInt(count);
                if (random.nextBoolean()) {
                    graph.addEdge(from, to);
                    distances[from][to] = Math.min(distances[from][to], 1);
                    if (!directed) {
                        distances[to][from] = Math.min(distances[to][from], 1);
                    }
                }
            }
            for (int via = 0; via < count; via++) {
                for (int from = 0; from < count; from++) {
                    for (int to = 0; to < count; to++) {
                        distances[from][to] = Math.min(distances[from][to],
                                distances[from][via] + distances[via][to]);
                    }
                }
            }
            for (int from = 0; from < count; from++) {
                Set<Integer> reachable = new HashSet<>();
                for (int to = 0; to < count; to++) {
                    if (distances[from][to] < infinity) {
                        reachable.add(to);
                    }
                    List<Integer> path = GraphAlgorithms.shortestPath(graph, from, to);
                    if (distances[from][to] == infinity) {
                        assertTrue(path.isEmpty(), "trial " + trial);
                    } else {
                        assertEquals(distances[from][to] + 1, path.size(), "trial " + trial);
                        assertEquals(from, path.get(0));
                        assertEquals(to, path.get(path.size() - 1));
                        for (int step = 1; step < path.size(); step++) {
                            assertTrue(graph.neighbors(path.get(step - 1)).contains(path.get(step)));
                        }
                    }
                }
                List<Integer> bfs = GraphAlgorithms.breadthFirst(graph, from);
                List<Integer> dfs = GraphAlgorithms.depthFirst(graph, from);
                assertEquals(reachable, new HashSet<>(bfs));
                assertEquals(reachable.size(), bfs.size());
                assertEquals(reachable, new HashSet<>(dfs));
                assertEquals(reachable.size(), dfs.size());
            }
        }
    }

    @Test
    void seededDagsAlwaysProduceAValidTopologicalOrdering() {
        Random random = new Random(0xDA6L);
        for (int trial = 0; trial < 100; trial++) {
            int count = random.nextInt(20);
            Graph graph = Graph.directed(count);
            List<Integer> permutation = new ArrayList<>();
            for (int vertex = 0; vertex < count; vertex++) {
                permutation.add(vertex);
            }
            java.util.Collections.shuffle(permutation, random);
            for (int first = 0; first < count; first++) {
                for (int second = first + 1; second < count; second++) {
                    if (random.nextInt(5) == 0) {
                        graph.addEdge(permutation.get(first), permutation.get(second));
                    }
                }
            }
            List<Integer> order = GraphAlgorithms.topologicalSort(graph);
            assertEquals(count, new HashSet<>(order).size());
            for (int from = 0; from < count; from++) {
                for (int to : graph.neighbors(from)) {
                    assertTrue(order.indexOf(from) < order.indexOf(to));
                }
            }
        }
    }

    private static Graph example() {
        return Graph.directed(6).addEdge(0, 2).addEdge(0, 1).addEdge(2, 3)
                .addEdge(1, 3).addEdge(3, 4).addEdge(3, 2).addEdge(0, 0);
    }
}
