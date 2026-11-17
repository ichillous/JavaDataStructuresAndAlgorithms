package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.graphs.BipartiteMatching;
import io.github.ichillous.dsa.graphs.FlowNetwork;
import io.github.ichillous.dsa.graphs.Graph;
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import io.github.ichillous.dsa.graphs.GraphStructureAlgorithms;
import io.github.ichillous.dsa.graphs.LowestCommonAncestor;
import io.github.ichillous.dsa.graphs.TreeAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks every value the connectivity figures display against the maintained classes.
 * Values that the library does not expose (a DFS lowlink, a BFS colour, the matching
 * before an augmentation) are checked against small independent reference models here.
 */
class VisualExamplesConnectivityTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-connectivity.properties");

    // ---- fixture parsing -------------------------------------------------------------

    private static int[] pair(String item) {
        String edge = item.contains(":") ? item.substring(0, item.indexOf(':')) : item;
        String[] ends = edge.split("-");
        return new int[] {Integer.parseInt(ends[0]), Integer.parseInt(ends[1])};
    }

    private static long weightOf(String item) {
        return Long.parseLong(item.substring(item.indexOf(':') + 1));
    }

    private static Graph undirected(String prefix) {
        Graph graph = Graph.undirected(DATA.number(prefix + ".vertices"));
        for (String item : DATA.strings(prefix + ".edges")) {
            int[] ends = pair(item);
            graph.addEdge(ends[0], ends[1]);
        }
        return graph;
    }

    private static WeightedGraph weighted(String prefix) {
        WeightedGraph graph = WeightedGraph.undirected(DATA.number(prefix + ".vertices"));
        for (String item : DATA.strings(prefix + ".edges")) {
            int[] ends = pair(item);
            graph.addEdge(ends[0], ends[1], weightOf(item));
        }
        return graph;
    }

    /** Breadth-first distances from {@code start}, skipping {@code removed} (-1 for none). */
    private static int[] distances(Graph graph, int start, int removed) {
        int[] distance = new int[graph.vertexCount()];
        Arrays.fill(distance, -1);
        Deque<Integer> queue = new ArrayDeque<>();
        distance[start] = 0;
        queue.add(start);
        while (!queue.isEmpty()) {
            int vertex = queue.poll();
            for (int next : graph.neighbors(vertex)) {
                if (next != removed && distance[next] < 0) {
                    distance[next] = distance[vertex] + 1;
                    queue.add(next);
                }
            }
        }
        return distance;
    }

    private static int components(Graph graph, int removed) {
        boolean[] seen = new boolean[graph.vertexCount()];
        int count = 0;
        for (int start = 0; start < graph.vertexCount(); start++) {
            if (start == removed || seen[start]) {
                continue;
            }
            count++;
            int[] distance = distances(graph, start, removed);
            for (int vertex = 0; vertex < distance.length; vertex++) {
                seen[vertex] |= distance[vertex] >= 0;
            }
        }
        return count;
    }

    private static boolean adjacent(Graph graph, int a, int b) {
        return graph.neighbors(a).contains(b);
    }

    // ---- articulation-points ---------------------------------------------------------

    @Test
    void theBowtieCentreIsTheOnlyArticulationPoint() {
        Graph bowtie = undirected("connectivity.cut");
        assertEquals(DATA.list("connectivity.cut.points"), GraphStructureAlgorithms.articulationPoints(bowtie));
    }

    @Test
    void removingEachVertexDisconnectsTheBowtieExactlyForTheArticulationPoints() {
        Graph bowtie = undirected("connectivity.cut");
        int before = components(bowtie, -1);
        assertEquals(1, before);
        List<Integer> disconnecting = new ArrayList<>();
        for (int vertex = 0; vertex < bowtie.vertexCount(); vertex++) {
            if (components(bowtie, vertex) > before) {
                disconnecting.add(vertex);
            }
        }
        assertEquals(DATA.list("connectivity.cut.points"), disconnecting);
        int cut = DATA.list("connectivity.cut.points").get(0);
        assertEquals(DATA.number("connectivity.cut.componentsAfterRemoval"), components(bowtie, cut));
    }

    // ---- bipartite-odd-cycle ---------------------------------------------------------

    @Test
    void theSixCycleTwoColorsIntoTheDisplayedSides() {
        Graph even = undirected("connectivity.bip.even");
        var sides = GraphStructureAlgorithms.bipartition(even).orElseThrow();
        assertEquals(DATA.list("connectivity.bip.even.left"), sides.left());
        assertEquals(DATA.list("connectivity.bip.even.right"), sides.right());
        assertTrue(GraphStructureAlgorithms.oddCycle(even).isEmpty());
    }

    @Test
    void theFiveCycleWithATailFailsAndTheWitnessIsTheDisplayedCycle() {
        Graph odd = undirected("connectivity.bip.odd");
        assertTrue(GraphStructureAlgorithms.bipartition(odd).isEmpty());
        List<Integer> cycle = GraphStructureAlgorithms.oddCycle(odd).orElseThrow();
        assertEquals(DATA.list("connectivity.bip.odd.cycle"), cycle);
        assertEquals(1, cycle.size() % 2);
        for (int i = 0; i < cycle.size(); i++) {
            assertTrue(adjacent(odd, cycle.get(i), cycle.get((i + 1) % cycle.size())),
                    "cycle edge " + i);
        }
    }

    @Test
    void breadthFirstParityColoursTheOddGraphAndOneEdgeJoinsEqualColours() {
        Graph odd = undirected("connectivity.bip.odd");
        int[] depth = distances(odd, 0, -1);
        int[] parity = Arrays.stream(depth).map(d -> d % 2).toArray();
        assertArrayEqualsList(DATA.list("connectivity.bip.odd.parity"), parity);
        int[] conflict = pair(DATA.text("connectivity.bip.odd.conflict"));
        assertTrue(adjacent(odd, conflict[0], conflict[1]));
        assertEquals(parity[conflict[0]], parity[conflict[1]]);
        int sameColourEdges = 0;
        for (int vertex = 0; vertex < odd.vertexCount(); vertex++) {
            for (int next : odd.neighbors(vertex)) {
                if (vertex < next && parity[vertex] == parity[next]) {
                    sameColourEdges++;
                }
            }
        }
        assertEquals(1, sameColourEdges, "exactly the displayed edge breaks the colouring");
    }

    private static void assertArrayEqualsList(List<Integer> expected, int[] actual) {
        assertEquals(expected, Arrays.stream(actual).boxed().toList());
    }

    // ---- euler-path ------------------------------------------------------------------

    @Test
    void theEulerPathStartsAtTheSmallestOddVertexAndUsesEveryEdgeOnce() {
        Graph graph = undirected("connectivity.euler");
        int[] degrees = new int[graph.vertexCount()];
        List<Integer> odd = new ArrayList<>();
        for (int vertex = 0; vertex < degrees.length; vertex++) {
            degrees[vertex] = graph.neighbors(vertex).size();
            if (degrees[vertex] % 2 == 1) {
                odd.add(vertex);
            }
        }
        assertArrayEqualsList(DATA.list("connectivity.euler.degrees"), degrees);
        assertEquals(DATA.list("connectivity.euler.odd"), odd);
        assertEquals(DATA.number("connectivity.euler.start"), odd.get(0));

        List<Integer> path = GraphStructureAlgorithms.eulerianPath(graph).orElseThrow();
        assertEquals(DATA.list("connectivity.euler.path"), path);
        assertEquals(graph.edgeCount() + 1, path.size());
        assertEquals(odd.get(0), path.get(0));
        assertEquals(odd.get(1), path.get(path.size() - 1));
        Set<String> used = new HashSet<>();
        for (int i = 0; i + 1 < path.size(); i++) {
            int a = Math.min(path.get(i), path.get(i + 1));
            int b = Math.max(path.get(i), path.get(i + 1));
            assertTrue(adjacent(graph, a, b));
            assertTrue(used.add(a + "-" + b), "edge repeated: " + a + "-" + b);
        }
        assertEquals(graph.edgeCount(), used.size());
    }

    // ---- mst-kruskal-prim ------------------------------------------------------------

    private static List<String> edgeStrings(List<WeightedGraph.Edge> edges) {
        return edges.stream().map(e -> e.from() + "-" + e.to() + ":" + e.weight()).toList();
    }

    @Test
    void kruskalAndPrimSelectTheDisplayedEdgesInTheDisplayedOrder() {
        WeightedGraph graph = weighted("connectivity.mst");
        var kruskal = GraphAlgorithms.minimumSpanningForest(graph);
        var prim = GraphStructureAlgorithms.primMinimumSpanningForest(graph);
        assertEquals(DATA.strings("connectivity.mst.kruskal"), edgeStrings(kruskal.edges()));
        assertEquals(DATA.strings("connectivity.mst.prim"), edgeStrings(prim.edges()));
        assertEquals(DATA.number("connectivity.mst.total"), kruskal.totalWeight());
        assertEquals(DATA.number("connectivity.mst.total"), prim.totalWeight());
        assertEquals(1, kruskal.components());
        assertEquals(1, prim.components());
        assertEquals(DATA.number("connectivity.mst.primStart"), prim.edges().get(0).from());
    }

    @Test
    void theSortedEdgeRowIsTheStableWeightOrderAndTheSelectedEdgesAreTheTree() {
        WeightedGraph graph = weighted("connectivity.mst");
        List<WeightedGraph.Edge> sorted = new ArrayList<>(graph.edges());
        sorted.sort(Comparator.comparingLong(WeightedGraph.Edge::weight));
        List<String> normalised = sorted.stream()
                .map(e -> Math.min(e.from(), e.to()) + "-" + Math.max(e.from(), e.to()) + ":" + e.weight())
                .toList();
        assertEquals(DATA.strings("connectivity.mst.sorted"), normalised);

        Set<String> kruskalTree = new TreeSet<>();
        for (var edge : GraphAlgorithms.minimumSpanningForest(graph).edges()) {
            kruskalTree.add(Math.min(edge.from(), edge.to()) + "-" + Math.max(edge.from(), edge.to()));
        }
        Set<String> primTree = new TreeSet<>();
        for (var edge : GraphStructureAlgorithms.primMinimumSpanningForest(graph).edges()) {
            primTree.add(Math.min(edge.from(), edge.to()) + "-" + Math.max(edge.from(), edge.to()));
        }
        assertEquals(kruskalTree, primTree, "same tree: only the order differs");
        assertEquals(graph.vertexCount() - 1, primTree.size());
    }

    // ---- max-flow-min-cut ------------------------------------------------------------

    private static FlowNetwork network() {
        FlowNetwork network = new FlowNetwork(DATA.number("connectivity.flow.vertices"));
        for (String item : DATA.strings("connectivity.flow.edges")) {
            int[] ends = pair(item);
            network.addEdge(ends[0], ends[1], weightOf(item));
        }
        return network;
    }

    @Test
    void theFlowNetworkReturnsTheDisplayedFlowsAndSourceSide() {
        FlowNetwork network = network();
        FlowNetwork.MaxFlow flow = network.maxFlow(DATA.number("connectivity.flow.source"),
                DATA.number("connectivity.flow.sink"));
        assertEquals(DATA.number("connectivity.flow.value"), flow.value());
        assertEquals(Arrays.stream(DATA.longs("connectivity.flow.flows")).boxed().toList(), flow.edgeFlows());
        assertEquals(DATA.list("connectivity.flow.sourceSide"), flow.sourceSide());
    }

    @Test
    void theDisplayedCutCapacityEqualsTheFlowAndEveryCrossingEdgeIsSaturated() {
        FlowNetwork network = network();
        FlowNetwork.MaxFlow flow = network.maxFlow(DATA.number("connectivity.flow.source"),
                DATA.number("connectivity.flow.sink"));
        List<FlowNetwork.Edge> edges = network.edges();
        long leaving = 0;
        for (int i = 0; i < edges.size(); i++) {
            FlowNetwork.Edge edge = edges.get(i);
            boolean fromSource = flow.sourceSide().contains(edge.from());
            boolean toSource = flow.sourceSide().contains(edge.to());
            if (fromSource && !toSource) {
                leaving += edge.capacity();
                assertEquals(edge.capacity(), flow.edgeFlows().get(i), "cut edge " + i + " is saturated");
            }
            if (!fromSource && toSource) {
                assertEquals(0L, flow.edgeFlows().get(i), "edge entering the source side carries nothing");
            }
        }
        assertEquals(DATA.number("connectivity.flow.cutCapacity"), leaving);
        assertEquals(flow.value(), leaving);
        // The source-side edge 0->1 carries 3 of 5: residual capacity keeps vertex 1 reachable.
        assertTrue(flow.edgeFlows().get(0) < edges.get(0).capacity());
    }

    // ---- matching-augmenting-path ----------------------------------------------------

    private static Set<String> pairs(List<String> items) {
        Set<String> set = new TreeSet<>();
        for (String item : items) {
            int[] ends = pair(item);
            set.add(ends[0] + "-" + ends[1]);
        }
        return set;
    }

    @Test
    void theIntermediateMatchingIsValidAndOneSmallerThanTheMaximum() {
        Set<String> edges = pairs(DATA.strings("connectivity.match.edges"));
        Set<String> partial = pairs(DATA.strings("connectivity.match.partial"));
        assertTrue(edges.containsAll(partial));
        Set<Integer> lefts = new HashSet<>();
        Set<Integer> rights = new HashSet<>();
        for (String item : partial) {
            int[] ends = pair(item);
            assertTrue(lefts.add(ends[0]), "left vertex matched twice");
            assertTrue(rights.add(ends[1]), "right vertex matched twice");
        }
        assertEquals(DATA.number("connectivity.match.size") - 1, partial.size());
        assertFalse(lefts.contains(DATA.number("connectivity.match.freeLeft")));
        assertFalse(rights.contains(DATA.number("connectivity.match.freeRight")));

        List<BipartiteMatching.Pair> all = new ArrayList<>();
        for (String item : edges) {
            int[] ends = pair(item);
            all.add(new BipartiteMatching.Pair(ends[0], ends[1]));
        }
        var maximum = BipartiteMatching.maximumMatching(DATA.number("connectivity.match.left"),
                DATA.number("connectivity.match.right"), all);
        assertEquals(DATA.number("connectivity.match.size"), maximum.size());
        assertEquals(pairs(DATA.strings("connectivity.match.final")),
                maximum.stream().map(p -> p.left() + "-" + p.right()).collect(java.util.stream.Collectors.toCollection(TreeSet::new)));
    }

    @Test
    void theAugmentingPathAlternatesBetweenTwoFreeVerticesAndFlippingItGivesTheFinalMatching() {
        Set<String> edges = pairs(DATA.strings("connectivity.match.edges"));
        Set<String> partial = pairs(DATA.strings("connectivity.match.partial"));
        int[] left = DATA.ints("connectivity.match.pathLeft");
        int[] right = DATA.ints("connectivity.match.pathRight");
        assertEquals(left.length, right.length);
        assertEquals(DATA.number("connectivity.match.freeLeft"), left[0]);
        assertEquals(DATA.number("connectivity.match.freeRight"), right[right.length - 1]);

        // The path is left[0] - right[0] - left[1] - right[1] - ...; edges alternate
        // unmatched, matched, unmatched, ...
        Set<String> pathEdges = new TreeSet<>();
        for (int i = 0; i < left.length; i++) {
            String unmatched = left[i] + "-" + right[i];
            assertTrue(edges.contains(unmatched));
            assertFalse(partial.contains(unmatched), "step " + i + " must be an unmatched edge");
            pathEdges.add(unmatched);
            if (i + 1 < left.length) {
                String matched = left[i + 1] + "-" + right[i];
                assertTrue(partial.contains(matched), "the return edge must be matched");
                pathEdges.add(matched);
            }
        }
        Set<String> flipped = new TreeSet<>(partial);
        for (String edge : pathEdges) {
            if (!flipped.remove(edge)) {
                flipped.add(edge);
            }
        }
        assertEquals(pairs(DATA.strings("connectivity.match.final")), flipped);
        assertEquals(DATA.number("connectivity.match.size"), flipped.size());
    }

    // ---- lca-binary-lifting ----------------------------------------------------------

    @Test
    void theLiftingTableAndTheLiftedVertexMatchLowestCommonAncestor() {
        Graph tree = undirected("connectivity.lca");
        int root = DATA.number("connectivity.lca.root");
        LowestCommonAncestor lca = new LowestCommonAncestor(tree, root);
        int[] depths = DATA.ints("connectivity.lca.depths");
        for (int vertex = 0; vertex < depths.length; vertex++) {
            assertEquals(depths[vertex], lca.depth(vertex), "depth of " + vertex);
        }
        // up[j][v] is the 2^j-th ancestor, or the root when 2^j exceeds the depth.
        String[] keys = {"connectivity.lca.up0", "connectivity.lca.up1", "connectivity.lca.up2"};
        for (int level = 0; level < keys.length; level++) {
            int[] row = DATA.ints(keys[level]);
            for (int vertex = 0; vertex < row.length; vertex++) {
                OptionalInt ancestor = lca.kthAncestor(vertex, 1 << level);
                assertEquals(ancestor.isPresent() ? ancestor.getAsInt() : root, row[vertex],
                        "up[" + level + "][" + vertex + "]");
            }
        }
    }

    @Test
    void theQueryLiftsTheDeeperVertexMeetsBelowTheAnswerAndReturnsTheAnswer() {
        Graph tree = undirected("connectivity.lca");
        LowestCommonAncestor lca = new LowestCommonAncestor(tree, DATA.number("connectivity.lca.root"));
        int a = DATA.number("connectivity.lca.a");
        int b = DATA.number("connectivity.lca.b");
        int gap = lca.depth(a) - lca.depth(b);
        assertEquals(2, gap, "a single jump of 2^1 equalises the depths");
        assertEquals(DATA.number("connectivity.lca.lifted"), lca.kthAncestor(a, gap).orElseThrow());
        int answer = lca.lca(a, b);
        assertEquals(DATA.number("connectivity.lca.answer"), answer);
        assertEquals(DATA.number("connectivity.lca.distance"), lca.distance(a, b));
        // The two vertices one step below the answer are the pair the last jump reaches.
        int[] meet = DATA.ints("connectivity.lca.meet");
        assertEquals(meet[0], lca.kthAncestor(a, lca.depth(a) - lca.depth(answer) - 1).orElseThrow());
        assertEquals(meet[1], lca.kthAncestor(b, lca.depth(b) - lca.depth(answer) - 1).orElseThrow());
        assertEquals(answer, lca.kthAncestor(meet[0], 1).orElseThrow());
        assertEquals(answer, lca.kthAncestor(meet[1], 1).orElseThrow());
    }

    // ---- tree-diameter ---------------------------------------------------------------

    @Test
    void theTwoSweepsFindTheDisplayedEndpointsDistancesAndPath() {
        Graph tree = undirected("connectivity.diameter");
        int start = DATA.number("connectivity.diameter.start");
        int u = DATA.number("connectivity.diameter.u");
        int v = DATA.number("connectivity.diameter.v");

        // Rooting a LowestCommonAncestor at a vertex makes depth(x) the distance from it.
        LowestCommonAncestor fromStart = new LowestCommonAncestor(tree, start);
        LowestCommonAncestor fromU = new LowestCommonAncestor(tree, u);
        int[] dist0 = DATA.ints("connectivity.diameter.dist0");
        int[] distU = DATA.ints("connectivity.diameter.distU");
        for (int vertex = 0; vertex < dist0.length; vertex++) {
            assertEquals(dist0[vertex], fromStart.depth(vertex), "sweep 1 distance of " + vertex);
            assertEquals(distU[vertex], fromU.depth(vertex), "sweep 2 distance of " + vertex);
        }
        assertEquals(u, smallestFarthest(dist0), "sweep 1 picks the smallest farthest vertex");
        assertEquals(v, smallestFarthest(distU), "sweep 2 picks the smallest farthest vertex");

        List<Integer> path = TreeAlgorithms.diameterPath(tree);
        assertEquals(DATA.list("connectivity.diameter.path"), path);
        assertEquals(u, path.get(0));
        assertEquals(v, path.get(path.size() - 1));
        assertEquals(DATA.number("connectivity.diameter.length"), path.size() - 1);
        assertEquals(DATA.number("connectivity.diameter.length"), distU[v]);
    }

    private static int smallestFarthest(int[] dist) {
        int best = 0;
        for (int vertex = 1; vertex < dist.length; vertex++) {
            if (dist[vertex] > dist[best]) {
                best = vertex;
            }
        }
        return best;
    }
}
