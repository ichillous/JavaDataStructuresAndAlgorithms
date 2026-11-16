package io.github.ichillous.dsa.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GraphStructureAlgorithmsTest {
    private static Graph randomGraph(Random random, int n, int edges, boolean directed, boolean loops) {
        Graph graph = directed ? Graph.directed(n) : Graph.undirected(n);
        for (int i = 0; i < edges && n > 0; i++) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);
            if (a != b || loops) {
                graph.addEdge(a, b);
            }
        }
        return graph;
    }

    private static int componentsWithout(Graph graph, int removed) {
        int n = graph.vertexCount();
        boolean[] seen = new boolean[n];
        if (removed >= 0) {
            seen[removed] = true;
        }
        int components = 0;
        for (int s = 0; s < n; s++) {
            if (seen[s]) {
                continue;
            }
            components++;
            List<Integer> stack = new ArrayList<>(List.of(s));
            seen[s] = true;
            while (!stack.isEmpty()) {
                int v = stack.remove(stack.size() - 1);
                for (int w : graph.neighbors(v)) {
                    if (!seen[w]) {
                        seen[w] = true;
                        stack.add(w);
                    }
                }
            }
        }
        return components;
    }

    private static Map<Integer, List<Integer>> snapshot(Graph graph) {
        Map<Integer, List<Integer>> copy = new HashMap<>();
        for (int v = 0; v < graph.vertexCount(); v++) {
            copy.put(v, graph.neighbors(v));
        }
        return copy;
    }

    @Test
    void articulationPointsMatchRemovalOracle() {
        long seed = 0xA77L;
        Random random = new Random(seed);
        int nonEmpty = 0;
        for (int round = 0; round < 400; round++) {
            int n = random.nextInt(11);
            Graph graph = randomGraph(random, n, random.nextInt(14), false, round % 3 == 0);
            Map<Integer, List<Integer>> before = snapshot(graph);
            List<Integer> points = GraphStructureAlgorithms.articulationPoints(graph);
            int base = componentsWithout(graph, -1);
            List<Integer> expected = new ArrayList<>();
            for (int v = 0; v < n; v++) {
                if (componentsWithout(graph, v) > base) {
                    expected.add(v);
                }
            }
            assertEquals(expected, points, "seed=" + seed + " round=" + round);
            assertEquals(before, snapshot(graph), "input mutated, seed=" + seed);
            nonEmpty += points.isEmpty() ? 0 : 1;
        }
        assertTrue(nonEmpty > 20, "oracle should exercise non-empty answers");
    }

    @Test
    void articulationPointsKnownShapes() {
        Graph bowtie = Graph.undirected(5).addEdge(0, 1).addEdge(1, 2).addEdge(2, 0)
                .addEdge(2, 3).addEdge(3, 4).addEdge(4, 2);
        assertEquals(List.of(2), GraphStructureAlgorithms.articulationPoints(bowtie));
        Graph star = Graph.undirected(4).addEdge(0, 1).addEdge(0, 2).addEdge(0, 3);
        assertEquals(List.of(0), GraphStructureAlgorithms.articulationPoints(star));
        Graph cycle = Graph.undirected(4).addEdge(0, 1).addEdge(1, 2).addEdge(2, 3).addEdge(3, 0);
        assertEquals(List.of(), GraphStructureAlgorithms.articulationPoints(cycle));
        Graph loopOnly = Graph.undirected(3).addEdge(1, 1).addEdge(0, 1);
        assertEquals(List.of(), GraphStructureAlgorithms.articulationPoints(loopOnly));
    }

    @Test
    void bipartitionAndOddCycleAgreeWithBruteForceColoring() {
        long seed = 0xB1AL;
        Random random = new Random(seed);
        int bipartiteCount = 0;
        int oddCount = 0;
        for (int round = 0; round < 500; round++) {
            int n = random.nextInt(11);
            Graph graph = randomGraph(random, n, random.nextInt(12), false, round % 10 == 0);
            String context = "seed=" + seed + " round=" + round;
            Map<Integer, List<Integer>> before = snapshot(graph);
            Optional<GraphStructureAlgorithms.Bipartition> partition =
                    GraphStructureAlgorithms.bipartition(graph);
            Optional<List<Integer>> cycle = GraphStructureAlgorithms.oddCycle(graph);
            assertEquals(before, snapshot(graph), context);
            assertEquals(bruteForceBipartite(graph), partition.isPresent(), context);
            assertTrue(partition.isPresent() != cycle.isPresent(), context);
            if (partition.isPresent()) {
                bipartiteCount++;
                assertValidPartition(graph, partition.get(), context);
            } else {
                oddCount++;
                assertValidOddCycle(graph, cycle.get(), context);
            }
        }
        assertTrue(bipartiteCount > 20 && oddCount > 20, "both outcomes must be exercised");
    }

    private static boolean bruteForceBipartite(Graph graph) {
        int n = graph.vertexCount();
        for (int mask = 0; mask < 1 << n; mask++) {
            boolean ok = true;
            for (int v = 0; v < n && ok; v++) {
                for (int w : graph.neighbors(v)) {
                    if (((mask >> v) & 1) == ((mask >> w) & 1)) {
                        ok = false;
                        break;
                    }
                }
            }
            if (ok) {
                return true;
            }
        }
        return false;
    }

    private static void assertValidPartition(Graph graph, GraphStructureAlgorithms.Bipartition p,
            String context) {
        Set<Integer> left = new HashSet<>(p.left());
        Set<Integer> right = new HashSet<>(p.right());
        assertEquals(graph.vertexCount(), left.size() + right.size(), context);
        for (int v = 0; v < graph.vertexCount(); v++) {
            assertTrue(left.contains(v) ^ right.contains(v), context);
            for (int w : graph.neighbors(v)) {
                assertTrue(left.contains(v) != left.contains(w), context);
            }
        }
        assertEquals(p.left().stream().sorted().toList(), p.left(), context);
        assertEquals(p.right().stream().sorted().toList(), p.right(), context);
    }

    private static void assertValidOddCycle(Graph graph, List<Integer> cycle, String context) {
        assertEquals(1, cycle.size() % 2, context);
        assertEquals(cycle.size(), new HashSet<>(cycle).size(), context);
        for (int i = 0; i < cycle.size(); i++) {
            int from = cycle.get(i);
            int to = cycle.get((i + 1) % cycle.size());
            assertTrue(graph.neighbors(from).contains(to), context + " edge " + from + "-" + to);
        }
    }

    @Test
    void bipartitionTieRulesAndWitnessShapes() {
        Graph graph = Graph.undirected(7).addEdge(1, 2).addEdge(2, 3).addEdge(5, 4);
        GraphStructureAlgorithms.Bipartition p = GraphStructureAlgorithms.bipartition(graph).orElseThrow();
        // Smallest vertex of each component is left; isolated vertices 0 and 6 are left too.
        assertEquals(List.of(0, 1, 3, 4, 6), p.left());
        assertEquals(List.of(2, 5), p.right());
        assertEquals(Optional.empty(), GraphStructureAlgorithms.oddCycle(graph));

        Graph loop = Graph.undirected(3).addEdge(0, 1).addEdge(2, 2);
        assertEquals(Optional.empty(), GraphStructureAlgorithms.bipartition(loop));
        assertEquals(Optional.of(List.of(2)), GraphStructureAlgorithms.oddCycle(loop));

        Graph triangle = Graph.undirected(3).addEdge(0, 1).addEdge(1, 2).addEdge(2, 0);
        assertEquals(Optional.of(List.of(1, 0, 2)), GraphStructureAlgorithms.oddCycle(triangle));
    }

    @Test
    void directedCycleWitnessIsValidAndAbsentForDags() {
        long seed = 0xD1CL;
        Random random = new Random(seed);
        int cyclic = 0;
        int acyclic = 0;
        for (int round = 0; round < 500; round++) {
            int n = random.nextInt(10);
            Graph graph = randomGraph(random, n, random.nextInt(14), true, round % 7 == 0);
            String context = "seed=" + seed + " round=" + round;
            Map<Integer, List<Integer>> before = snapshot(graph);
            Optional<List<Integer>> cycle = GraphStructureAlgorithms.directedCycle(graph);
            assertEquals(before, snapshot(graph), context);
            boolean sortFails;
            try {
                GraphAlgorithms.topologicalSort(graph);
                sortFails = false;
            } catch (IllegalArgumentException expected) {
                sortFails = true;
            }
            assertEquals(sortFails, cycle.isPresent(), context);
            if (cycle.isPresent()) {
                cyclic++;
                List<Integer> c = cycle.get();
                assertFalse(c.isEmpty(), context);
                assertEquals(c.size(), new HashSet<>(c).size(), context);
                for (int i = 0; i < c.size(); i++) {
                    assertTrue(graph.neighbors(c.get(i)).contains(c.get((i + 1) % c.size())), context);
                }
            } else {
                acyclic++;
            }
        }
        assertTrue(cyclic > 20 && acyclic > 20, "both outcomes must be exercised");
    }

    @Test
    void directedCycleKnownResults() {
        Graph graph = Graph.directed(5).addEdge(0, 1).addEdge(1, 2).addEdge(2, 3)
                .addEdge(3, 1).addEdge(3, 4);
        assertEquals(Optional.of(List.of(1, 2, 3)), GraphStructureAlgorithms.directedCycle(graph));
        Graph loop = Graph.directed(2).addEdge(0, 1).addEdge(1, 1);
        assertEquals(Optional.of(List.of(1)), GraphStructureAlgorithms.directedCycle(loop));
        // A diamond reaches a finished vertex twice but has no cycle.
        Graph diamond = Graph.directed(4).addEdge(0, 1).addEdge(0, 2).addEdge(1, 3).addEdge(2, 3);
        assertEquals(Optional.empty(), GraphStructureAlgorithms.directedCycle(diamond));
    }

    @Test
    void eulerianPathUsesEveryEdgeOnce() {
        Graph directed = Graph.directed(4).addEdge(0, 1).addEdge(1, 2).addEdge(2, 0).addEdge(2, 3);
        List<Integer> path = GraphStructureAlgorithms.eulerianPath(directed).orElseThrow();
        assertEquals(5, path.size());
        // Vertex 2 has out-in = 1, so it must start; no Euler path can start at 0.
        assertEquals(List.of(2, 0, 1, 2, 3), path);
        assertValidEulerPath(directed, path, "directed example");

        Graph star = Graph.undirected(4).addEdge(0, 1).addEdge(0, 2).addEdge(0, 3);
        assertEquals(Optional.empty(), GraphStructureAlgorithms.eulerianPath(star));

        Graph line = Graph.undirected(3).addEdge(1, 0).addEdge(1, 2);
        assertEquals(Optional.of(List.of(0, 1, 2)), GraphStructureAlgorithms.eulerianPath(line));
        Graph square = Graph.undirected(4).addEdge(1, 2).addEdge(2, 3).addEdge(3, 0).addEdge(0, 1);
        assertEquals(Optional.of(List.of(0, 3, 2, 1, 0)), GraphStructureAlgorithms.eulerianPath(square));
    }

    @Test
    void eulerianPathMatchesDegreeAndConnectivityOracle() {
        long seed = 0xE01L;
        Random random = new Random(seed);
        int found = 0;
        int missing = 0;
        for (int round = 0; round < 600; round++) {
            boolean directed = round % 2 == 0;
            int n = 1 + random.nextInt(8);
            Graph graph = round % 3 == 0
                    ? randomGraph(random, n, random.nextInt(12), directed, round % 5 == 0)
                    : balancedGraph(random, n, directed, round % 4 == 0);
            String context = "seed=" + seed + " round=" + round + " directed=" + directed;
            Map<Integer, List<Integer>> before = snapshot(graph);
            Optional<List<Integer>> path = GraphStructureAlgorithms.eulerianPath(graph);
            assertEquals(before, snapshot(graph), context);
            assertEquals(eulerExists(graph), path.isPresent(), context);
            if (path.isPresent()) {
                found++;
                assertValidEulerPath(graph, path.get(), context);
            } else {
                missing++;
            }
        }
        assertTrue(found > 50 && missing > 50, "found=" + found + " missing=" + missing);
    }

    /** Symmetric-difference of random cycles: every vertex ends with even/balanced degree. */
    private static Graph balancedGraph(Random random, int n, boolean directed, boolean loops) {
        Set<Long> edges = new HashSet<>();
        for (int c = 0; c < 1 + random.nextInt(3); c++) {
            int length = 1 + random.nextInt(Math.min(n, 5));
            List<Integer> vertices = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                vertices.add(random.nextInt(n));
            }
            for (int i = 0; i < length; i++) {
                int a = vertices.get(i);
                int b = vertices.get((i + 1) % length);
                if (a == b && !loops) {
                    continue;
                }
                long key = directed || a <= b ? (long) a * 100 + b : (long) b * 100 + a;
                if (!edges.remove(key)) {
                    edges.add(key);
                }
            }
        }
        Graph graph = directed ? Graph.directed(n) : Graph.undirected(n);
        for (long key : edges) {
            graph.addEdge((int) (key / 100), (int) (key % 100));
        }
        return graph;
    }

    /** Independent criterion: degree conditions plus one component holding all edges. */
    private static boolean eulerExists(Graph g) {
        int n = g.vertexCount();
        int[] out = new int[n];
        int[] in = new int[n];
        int[] parent = new int[n];
        for (int v = 0; v < n; v++) {
            parent[v] = v;
        }
        int edges = 0;
        for (int v = 0; v < n; v++) {
            for (int w : g.neighbors(v)) {
                edges++;
                out[v]++;
                in[w]++;
                parent[find(parent, v)] = find(parent, w);
            }
        }
        if (edges == 0) {
            return true;
        }
        int root = -1;
        for (int v = 0; v < n; v++) {
            if (out[v] + in[v] > 0) {
                if (root == -1) {
                    root = find(parent, v);
                } else if (find(parent, v) != root) {
                    return false;
                }
            }
        }
        int plus = 0;
        int minus = 0;
        int odd = 0;
        for (int v = 0; v < n; v++) {
            if (g.isDirected()) {
                int balance = out[v] - in[v];
                if (balance == 1) {
                    plus++;
                } else if (balance == -1) {
                    minus++;
                } else if (balance != 0) {
                    return false;
                }
            } else {
                // Undirected adjacency lists both directions, except loops once; a
                // loop adds 2 to the degree, i.e. parity of non-loop entries only.
                int loopEntries = g.neighbors(v).contains(v) ? 1 : 0;
                int degree = out[v] - loopEntries + 2 * loopEntries;
                odd += degree % 2;
            }
        }
        return g.isDirected() ? plus <= 1 && plus == minus : odd == 0 || odd == 2;
    }

    private static int find(int[] parent, int v) {
        while (parent[v] != v) {
            parent[v] = parent[parent[v]];
            v = parent[v];
        }
        return v;
    }

    private static void assertValidEulerPath(Graph graph, List<Integer> path, String context) {
        if (graph.edgeCount() == 0) {
            assertTrue(path.isEmpty(), context);
            return;
        }
        assertEquals(graph.edgeCount() + 1, path.size(), context);
        Set<Long> remaining = new HashSet<>();
        for (int v = 0; v < graph.vertexCount(); v++) {
            for (int w : graph.neighbors(v)) {
                remaining.add(edgeKey(graph.isDirected(), v, w));
            }
        }
        assertEquals(graph.edgeCount(), remaining.size(), context);
        for (int i = 0; i + 1 < path.size(); i++) {
            assertTrue(remaining.remove(edgeKey(graph.isDirected(), path.get(i), path.get(i + 1))),
                    context + " reused or missing edge at step " + i);
        }
        assertTrue(remaining.isEmpty(), context);
    }

    private static long edgeKey(boolean directed, int a, int b) {
        return directed || a <= b ? (long) a * 1_000_000 + b : (long) b * 1_000_000 + a;
    }

    @Test
    void undirectedSelfLoopsCountOnceInAdjacencyButTwiceInDegree() {
        // Loop at 0 plus edge 0-1: deg(0)=3, deg(1)=1, so odd vertices 0 and 1.
        Graph pendant = Graph.undirected(2).addEdge(0, 0).addEdge(0, 1);
        assertEquals(Optional.of(List.of(0, 0, 1)), GraphStructureAlgorithms.eulerianPath(pendant));
        // A lone loop is a closed trail of one edge.
        Graph lone = Graph.undirected(3).addEdge(2, 2);
        assertEquals(Optional.of(List.of(2, 2)), GraphStructureAlgorithms.eulerianPath(lone));
        // Triangle with a loop is an Euler circuit starting at the smallest vertex.
        Graph decorated = Graph.undirected(3).addEdge(0, 1).addEdge(1, 2).addEdge(2, 0).addEdge(1, 1);
        List<Integer> circuit = GraphStructureAlgorithms.eulerianPath(decorated).orElseThrow();
        assertEquals(5, circuit.size());
        assertEquals(circuit.get(0), circuit.get(4));
        assertValidEulerPath(decorated, circuit, "decorated triangle");
        // Two loops on different vertices without a connection are disconnected.
        Graph apart = Graph.undirected(2).addEdge(0, 0).addEdge(1, 1);
        assertEquals(Optional.empty(), GraphStructureAlgorithms.eulerianPath(apart));
        // Directed loops add one out and one in, so balance is kept.
        Graph directedLoop = Graph.directed(2).addEdge(0, 0).addEdge(0, 1);
        assertEquals(Optional.of(List.of(0, 0, 1)), GraphStructureAlgorithms.eulerianPath(directedLoop));
    }

    @Test
    void eulerianPathRejectsBadDegreesAndDisconnectedEdges() {
        Graph twoCycles = Graph.directed(6).addEdge(0, 1).addEdge(1, 0).addEdge(3, 4).addEdge(4, 3);
        assertEquals(Optional.empty(), GraphStructureAlgorithms.eulerianPath(twoCycles));
        Graph twoStarts = Graph.directed(4).addEdge(0, 1).addEdge(0, 2).addEdge(0, 3);
        assertEquals(Optional.empty(), GraphStructureAlgorithms.eulerianPath(twoStarts));
        Graph unbalanced = Graph.directed(3).addEdge(0, 1).addEdge(0, 2).addEdge(1, 2).addEdge(2, 0)
                .addEdge(0, 0);
        List<Integer> loopy = GraphStructureAlgorithms.eulerianPath(unbalanced).orElseThrow();
        assertValidEulerPath(unbalanced, loopy, "unbalanced with loop");
        assertEquals(0, loopy.get(0));
        assertEquals(2, loopy.get(loopy.size() - 1));
        Graph k4 = Graph.undirected(4).addEdge(0, 1).addEdge(0, 2).addEdge(0, 3).addEdge(1, 2)
                .addEdge(1, 3).addEdge(2, 3);
        assertEquals(Optional.empty(), GraphStructureAlgorithms.eulerianPath(k4));
        Graph twoComponents = Graph.undirected(4).addEdge(0, 1).addEdge(2, 3);
        assertEquals(Optional.empty(), GraphStructureAlgorithms.eulerianPath(twoComponents));
    }

    @Test
    void primMatchesKruskalTotalAndComponents() {
        long seed = 0x9141L;
        Random random = new Random(seed);
        for (int round = 0; round < 400; round++) {
            int n = random.nextInt(12);
            WeightedGraph graph = WeightedGraph.undirected(n);
            int edges = n == 0 ? 0 : random.nextInt(25);
            for (int i = 0; i < edges; i++) {
                graph.addEdge(random.nextInt(n), random.nextInt(n), random.nextInt(21) - 10);
            }
            String context = "seed=" + seed + " round=" + round;
            GraphAlgorithms.SpanningForest prim = GraphStructureAlgorithms.primMinimumSpanningForest(graph);
            GraphAlgorithms.SpanningForest kruskal = GraphAlgorithms.minimumSpanningForest(graph);
            assertEquals(kruskal.totalWeight(), prim.totalWeight(), context);
            assertEquals(kruskal.components(), prim.components(), context);
            assertEquals(n - prim.components(), prim.edges().size(), context);
            // Valid forest: acyclic, selected edges exist in the graph, same components.
            int[] parent = new int[n];
            for (int v = 0; v < n; v++) {
                parent[v] = v;
            }
            long total = 0;
            for (WeightedGraph.Edge edge : prim.edges()) {
                assertTrue(graph.neighbors(edge.from()).contains(edge), context);
                int a = find(parent, edge.from());
                int b = find(parent, edge.to());
                assertTrue(a != b, "cycle in forest, " + context);
                parent[a] = b;
                total += edge.weight();
            }
            assertEquals(total, prim.totalWeight(), context);
        }
    }

    @Test
    void primTieRuleAndExactTotals() {
        WeightedGraph ties = WeightedGraph.undirected(4).addEdge(0, 3, 1).addEdge(0, 2, 1)
                .addEdge(0, 1, 1).addEdge(1, 2, 1);
        GraphAlgorithms.SpanningForest forest = GraphStructureAlgorithms.primMinimumSpanningForest(ties);
        assertEquals(List.of(new WeightedGraph.Edge(0, 1, 1), new WeightedGraph.Edge(0, 2, 1),
                new WeightedGraph.Edge(0, 3, 1)), forest.edges());
        assertEquals(3, forest.totalWeight());
        assertEquals(1, forest.components());

        WeightedGraph cancel = WeightedGraph.undirected(4).addEdge(0, 1, Long.MIN_VALUE)
                .addEdge(1, 2, Long.MAX_VALUE).addEdge(2, 3, 1).addEdge(3, 3, -5);
        assertEquals(0, GraphStructureAlgorithms.primMinimumSpanningForest(cancel).totalWeight());

        WeightedGraph overflow = WeightedGraph.undirected(3).addEdge(0, 1, Long.MAX_VALUE)
                .addEdge(1, 2, Long.MAX_VALUE);
        assertThrows(ArithmeticException.class,
                () -> GraphStructureAlgorithms.primMinimumSpanningForest(overflow));

        WeightedGraph parallel = WeightedGraph.undirected(2).addEdge(0, 1, 9).addEdge(1, 0, -4)
                .addEdge(0, 1, 5);
        assertEquals(-4, GraphStructureAlgorithms.primMinimumSpanningForest(parallel).totalWeight());
        assertEquals(3, parallel.edgeCount());
    }

    @Test
    void deepPathGraphsDoNotOverflowStack() {
        int n = 100_000;
        Graph path = Graph.undirected(n);
        for (int i = 0; i + 1 < n; i++) {
            path.addEdge(i, i + 1);
        }
        List<Integer> cuts = GraphStructureAlgorithms.articulationPoints(path);
        assertEquals(n - 2, cuts.size());
        assertEquals(1, cuts.get(0));
        assertEquals(n - 2, cuts.get(cuts.size() - 1));
        GraphStructureAlgorithms.Bipartition p = GraphStructureAlgorithms.bipartition(path).orElseThrow();
        assertEquals(n / 2, p.left().size());
        assertEquals(n / 2, p.right().size());
        assertEquals(Optional.empty(), GraphStructureAlgorithms.oddCycle(path));
        List<Integer> euler = GraphStructureAlgorithms.eulerianPath(path).orElseThrow();
        assertEquals(n, euler.size());
        assertEquals(0, euler.get(0));
        assertEquals(n - 1, euler.get(n - 1));

        Graph directed = Graph.directed(n);
        for (int i = 0; i + 1 < n; i++) {
            directed.addEdge(i, i + 1);
        }
        assertEquals(Optional.empty(), GraphStructureAlgorithms.directedCycle(directed));
        directed.addEdge(n - 1, 0);
        assertEquals(n, GraphStructureAlgorithms.directedCycle(directed).orElseThrow().size());
        assertEquals(n + 1, GraphStructureAlgorithms.eulerianPath(directed).orElseThrow().size());

        WeightedGraph weighted = WeightedGraph.undirected(n);
        for (int i = 0; i + 1 < n; i++) {
            weighted.addEdge(i, i + 1, 1);
        }
        assertEquals(n - 1, GraphStructureAlgorithms.primMinimumSpanningForest(weighted).totalWeight());

        Graph oddRing = Graph.undirected(n + 1);
        for (int i = 0; i < n; i++) {
            oddRing.addEdge(i, i + 1);
        }
        oddRing.addEdge(n, 0);
        assertEquals(n + 1, GraphStructureAlgorithms.oddCycle(oddRing).orElseThrow().size());
    }

    @Test
    void emptyAndSingletonGraphs() {
        for (boolean directed : new boolean[] {false, true}) {
            Graph empty = directed ? Graph.directed(0) : Graph.undirected(0);
            Graph single = directed ? Graph.directed(1) : Graph.undirected(1);
            assertEquals(Optional.of(List.of()), GraphStructureAlgorithms.eulerianPath(empty));
            assertEquals(Optional.of(List.of()), GraphStructureAlgorithms.eulerianPath(single));
        }
        Graph empty = Graph.undirected(0);
        Graph single = Graph.undirected(1);
        assertEquals(List.of(), GraphStructureAlgorithms.articulationPoints(empty));
        assertEquals(List.of(), GraphStructureAlgorithms.articulationPoints(single));
        GraphStructureAlgorithms.Bipartition none = GraphStructureAlgorithms.bipartition(empty).orElseThrow();
        assertEquals(List.of(), none.left());
        assertEquals(List.of(), none.right());
        GraphStructureAlgorithms.Bipartition one = GraphStructureAlgorithms.bipartition(single).orElseThrow();
        assertEquals(List.of(0), one.left());
        assertEquals(List.of(), one.right());
        assertEquals(Optional.empty(), GraphStructureAlgorithms.oddCycle(single));
        assertEquals(Optional.empty(), GraphStructureAlgorithms.directedCycle(Graph.directed(0)));
        assertEquals(Optional.empty(), GraphStructureAlgorithms.directedCycle(Graph.directed(1)));
        GraphAlgorithms.SpanningForest forest =
                GraphStructureAlgorithms.primMinimumSpanningForest(WeightedGraph.undirected(0));
        assertEquals(0, forest.components());
        assertEquals(List.of(), forest.edges());
        GraphAlgorithms.SpanningForest isolated =
                GraphStructureAlgorithms.primMinimumSpanningForest(WeightedGraph.undirected(3));
        assertEquals(3, isolated.components());
        assertEquals(0, isolated.totalWeight());
    }

    @Test
    void bipartitionRecordValidatesInvariants() {
        assertThrows(IllegalArgumentException.class,
                () -> new GraphStructureAlgorithms.Bipartition(List.of(2, 1), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new GraphStructureAlgorithms.Bipartition(List.of(), List.of(1, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> new GraphStructureAlgorithms.Bipartition(List.of(0, 1), List.of(1, 2)));
        List<Integer> source = new ArrayList<>(List.of(0, 2));
        GraphStructureAlgorithms.Bipartition ok =
                new GraphStructureAlgorithms.Bipartition(source, List.of(1));
        source.add(9);
        assertEquals(List.of(0, 2), ok.left());
    }

    @Test
    void rejectsWrongDirectionAndNulls() {
        Graph directed = Graph.directed(2).addEdge(0, 1);
        Graph undirected = Graph.undirected(2).addEdge(0, 1);
        assertThrows(IllegalArgumentException.class, () -> GraphStructureAlgorithms.articulationPoints(directed));
        assertThrows(IllegalArgumentException.class, () -> GraphStructureAlgorithms.bipartition(directed));
        assertThrows(IllegalArgumentException.class, () -> GraphStructureAlgorithms.oddCycle(directed));
        assertThrows(IllegalArgumentException.class, () -> GraphStructureAlgorithms.directedCycle(undirected));
        assertThrows(IllegalArgumentException.class,
                () -> GraphStructureAlgorithms.primMinimumSpanningForest(WeightedGraph.directed(2)));
        assertThrows(NullPointerException.class, () -> GraphStructureAlgorithms.articulationPoints(null));
        assertThrows(NullPointerException.class, () -> GraphStructureAlgorithms.bipartition(null));
        assertThrows(NullPointerException.class, () -> GraphStructureAlgorithms.oddCycle(null));
        assertThrows(NullPointerException.class, () -> GraphStructureAlgorithms.directedCycle(null));
        assertThrows(NullPointerException.class, () -> GraphStructureAlgorithms.eulerianPath(null));
        assertThrows(NullPointerException.class,
                () -> GraphStructureAlgorithms.primMinimumSpanningForest(null));
    }
}
