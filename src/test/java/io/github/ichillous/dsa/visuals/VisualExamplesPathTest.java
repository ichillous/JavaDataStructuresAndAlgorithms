package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.graphs.AdvancedGraphAlgorithms;
import io.github.ichillous.dsa.graphs.Graph;
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import io.github.ichillous.dsa.graphs.ShortestPathAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.TreeSet;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Checks every value the path-family graph figures display against the maintained classes. */
class VisualExamplesPathTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-path.properties");

    /** Parses {@code a:b,c:d} into integer tuples. */
    private static List<int[]> tuples(String key) {
        return DATA.strings(key).stream()
                .map(item -> Arrays.stream(item.split(":")).mapToInt(Integer::parseInt).toArray())
                .toList();
    }

    private static Graph graph(String prefix, boolean directed) {
        int vertices = DATA.number(prefix + ".vertices");
        Graph graph = directed ? Graph.directed(vertices) : Graph.undirected(vertices);
        for (int[] edge : tuples(prefix + ".edges")) {
            graph.addEdge(edge[0], edge[1]);
        }
        return graph;
    }

    private static WeightedGraph weighted(String prefix) {
        WeightedGraph graph = WeightedGraph.directed(DATA.number(prefix + ".vertices"));
        for (int[] edge : tuples(prefix + ".edges")) {
            graph.addEdge(edge[0], edge[1], edge[2]);
        }
        return graph;
    }

    @Test
    void representationsStoreTheSameGraphThreeWays() {
        Graph graph = graph("path.repr", true);
        int vertices = DATA.number("path.repr.vertices");
        assertEquals(tuples("path.repr.edges").size(), graph.edgeCount());
        int[] matrix = DATA.ints("path.repr.matrix");
        assertEquals(vertices * vertices, matrix.length);
        for (int from = 0; from < vertices; from++) {
            assertEquals(DATA.list("path.repr.list." + from), graph.neighbors(from), "list row " + from);
            for (int to = 0; to < vertices; to++) {
                int expected = graph.neighbors(from).contains(to) ? 1 : 0;
                assertEquals(expected, matrix[from * vertices + to], "matrix " + from + "," + to);
            }
        }
        // The edge list is the insertion order, so replaying it rebuilds the same adjacency.
        Graph replay = Graph.directed(vertices);
        for (int[] edge : tuples("path.repr.edges")) {
            replay.addEdge(edge[0], edge[1]);
        }
        for (int vertex = 0; vertex < vertices; vertex++) {
            assertEquals(graph.neighbors(vertex), replay.neighbors(vertex));
        }
    }

    @Test
    void breadthFirstAndDepthFirstVisitTheSameGraphInDifferentOrders() {
        Graph graph = graph("path.order", false);
        int source = DATA.number("path.order.source");
        List<Integer> bfs = GraphAlgorithms.breadthFirst(graph, source);
        List<Integer> dfs = GraphAlgorithms.depthFirst(graph, source);
        assertEquals(DATA.list("path.order.bfs"), bfs);
        assertEquals(DATA.list("path.order.dfs"), dfs);
        assertFalse(bfs.equals(dfs), "the figure is only useful if the orders differ");

        List<Integer> distances = new ArrayList<>();
        for (int vertex : bfs) {
            distances.add(GraphAlgorithms.shortestPath(graph, source, vertex).size() - 1);
        }
        assertEquals(DATA.list("path.order.bfsDistance"), distances);

        // Independent reference: recursive DFS over insertion-ordered neighbors, recording parents.
        int[] parent = new int[graph.vertexCount()];
        Arrays.fill(parent, -2);
        List<Integer> referenceOrder = new ArrayList<>();
        recursiveDfs(graph, source, -1, parent, referenceOrder);
        assertEquals(dfs, referenceOrder);
        List<Integer> parents = new ArrayList<>();
        for (int vertex : dfs) {
            parents.add(parent[vertex]);
        }
        assertEquals(DATA.list("path.order.dfsParent"), parents);
    }

    private static void recursiveDfs(Graph graph, int vertex, int from, int[] parent, List<Integer> order) {
        parent[vertex] = from;
        order.add(vertex);
        for (int neighbor : graph.neighbors(vertex)) {
            if (parent[neighbor] == -2) {
                recursiveDfs(graph, neighbor, vertex, parent, order);
            }
        }
    }

    @Test
    void kahnsAlgorithmReadyQueueAndInDegreeDropsMatchTheFixture() {
        Graph graph = graph("path.kahn", true);
        int vertices = graph.vertexCount();
        int[] indegree = new int[vertices];
        for (int vertex = 0; vertex < vertices; vertex++) {
            for (int neighbor : graph.neighbors(vertex)) {
                indegree[neighbor]++;
            }
        }
        assertEquals(DATA.list("path.kahn.indegree"), Arrays.stream(indegree).boxed().toList());

        Deque<Integer> ready = new ArrayDeque<>();
        for (int vertex = 0; vertex < vertices; vertex++) {
            if (indegree[vertex] == 0) {
                ready.add(vertex);
            }
        }
        assertEquals(DATA.list("path.kahn.start"), List.copyOf(ready));
        List<Integer> popped = new ArrayList<>();
        while (!ready.isEmpty()) {
            int current = ready.remove();
            popped.add(current);
            List<String> drops = new ArrayList<>();
            for (int neighbor : graph.neighbors(current)) {
                int before = indegree[neighbor]--;
                drops.add(neighbor + ":" + before + ":" + indegree[neighbor]);
                if (indegree[neighbor] == 0) {
                    ready.add(neighbor);
                }
            }
            assertEquals(DATA.strings("path.kahn.drops." + current), drops, "drops after popping " + current);
            assertEquals(DATA.list("path.kahn.ready." + current), List.copyOf(ready),
                    "ready queue after popping " + current);
        }
        assertEquals(DATA.list("path.kahn.pops"), popped);
        assertEquals(DATA.list("path.kahn.order"), GraphAlgorithms.topologicalSort(graph));
        assertTrue(io.github.ichillous.dsa.graphs.GraphStructureAlgorithms.directedCycle(graph).isEmpty());
    }

    @Test
    void everyMethodTheChooserNamesExistsAndAnswersItsQuestion() {
        Graph plain = Graph.directed(2).addEdge(0, 1);
        WeightedGraph one = WeightedGraph.directed(2).addEdge(0, 1, 1);
        int rows = DATA.number("path.chooser.rows");
        assertEquals(7, rows);
        for (int row = 1; row <= rows; row++) {
            String method = DATA.text("path.chooser." + row + ".method");
            String owner = DATA.text("path.chooser." + row + ".owner");
            Supplier<Object> call = switch (owner + "." + method) {
                case "ShortestPathAlgorithms.floydWarshall" ->
                        () -> ShortestPathAlgorithms.floydWarshall(one).distance(0, 1);
                case "GraphAlgorithms.shortestPath" -> () -> GraphAlgorithms.shortestPath(plain, 0, 1);
                case "ShortestPathAlgorithms.zeroOneBfs" ->
                        () -> ShortestPathAlgorithms.zeroOneBfs(one, 0).distanceTo(1);
                case "ShortestPathAlgorithms.dagShortestPaths" ->
                        () -> ShortestPathAlgorithms.dagShortestPaths(one, 0).distanceTo(1);
                case "AdvancedGraphAlgorithms.bellmanFord" ->
                        () -> AdvancedGraphAlgorithms.bellmanFord(one, 0).distanceTo(1);
                case "ShortestPathAlgorithms.aStar" ->
                        () -> ShortestPathAlgorithms.aStar(one, 0, 1, vertex -> 0).orElseThrow().distance();
                case "GraphAlgorithms.dijkstra" -> () -> GraphAlgorithms.dijkstra(one, 0).distanceTo(1);
                default -> fail("the chooser names an unknown method " + owner + "." + method);
            };
            Object answer = call.get();
            Object expected = method.equals("shortestPath") ? List.of(0, 1)
                    : method.equals("aStar") ? (Object) 1L : OptionalLong.of(1);
            assertEquals(expected, answer, owner + "." + method);
        }
    }

    @Test
    void zeroOneBfsPutsZeroWeightEdgesInFrontAndUnitEdgesBehind() {
        WeightedGraph graph = weighted("path.zob");
        int source = DATA.number("path.zob.source");
        // Independent reference of the deque discipline, recording each pop's pushes.
        int n = graph.vertexCount();
        long[] distance = new long[n];
        boolean[] reached = new boolean[n];
        boolean[] done = new boolean[n];
        Deque<Integer> deque = new ArrayDeque<>();
        deque.add(source);
        reached[source] = true;
        assertEquals(DATA.list("path.zob.deque.start"), List.copyOf(deque));
        while (!deque.isEmpty()) {
            int current = deque.removeFirst();
            if (done[current]) {
                continue;
            }
            done[current] = true;
            List<Integer> front = new ArrayList<>();
            List<Integer> back = new ArrayList<>();
            List<String> improved = new ArrayList<>();
            for (WeightedGraph.Edge edge : graph.neighbors(current)) {
                long candidate = distance[current] + edge.weight();
                if (!reached[edge.to()] || candidate < distance[edge.to()]) {
                    if (reached[edge.to()]) {
                        improved.add(edge.to() + ":" + distance[edge.to()] + ":" + candidate);
                    }
                    distance[edge.to()] = candidate;
                    reached[edge.to()] = true;
                    if (edge.weight() == 0) {
                        deque.addFirst(edge.to());
                        front.add(edge.to());
                    } else {
                        deque.addLast(edge.to());
                        back.add(edge.to());
                    }
                }
            }
            String key = ".pop" + current;
            assertEquals(DATA.list("path.zob.front" + key), reverse(front), "front pushes of pop " + current);
            assertEquals(DATA.list("path.zob.back" + key), back, "back pushes of pop " + current);
            assertEquals(DATA.list("path.zob.deque" + key), List.copyOf(deque), "deque after pop " + current);
            if (!improved.isEmpty()) {
                assertEquals(DATA.strings("path.zob.improved" + key), improved);
            }
        }
        var tree = ShortestPathAlgorithms.zeroOneBfs(graph, source);
        List<Integer> distances = new ArrayList<>();
        for (int vertex = 0; vertex < n; vertex++) {
            assertEquals(distance[vertex], tree.distanceTo(vertex).orElseThrow());
            distances.add((int) tree.distanceTo(vertex).orElseThrow());
        }
        assertEquals(DATA.list("path.zob.distance"), distances);
        assertEquals(DATA.list("path.zob.path"), tree.pathTo(1));
    }

    private static List<Integer> reverse(List<Integer> values) {
        List<Integer> copy = new ArrayList<>(values);
        java.util.Collections.reverse(copy);
        return copy;
    }

    @Test
    void aStarWithManhattanDistanceTouchesFewerCellsThanZeroHeuristic() {
        int rows = DATA.number("path.astar.rows");
        int columns = DATA.number("path.astar.columns");
        WeightedGraph grid = WeightedGraph.undirected(rows * columns);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int cell = row * columns + column;
                if (column + 1 < columns) {
                    grid.addEdge(cell, cell + 1, 1);
                }
                if (row + 1 < rows) {
                    grid.addEdge(cell, cell + columns, 1);
                }
            }
        }
        int source = DATA.number("path.astar.source");
        int target = DATA.number("path.astar.target");
        int targetRow = target / columns;
        int targetColumn = target % columns;
        TreeSet<Integer> evaluated = new TreeSet<>();
        var route = ShortestPathAlgorithms.aStar(grid, source, target, cell -> {
            evaluated.add(cell);
            return Math.abs(cell / columns - targetRow) + Math.abs(cell % columns - targetColumn);
        }).orElseThrow();
        assertEquals(DATA.list("path.astar.route"), route.path());
        assertEquals(DATA.number("path.astar.distance"), route.distance());
        assertEquals(DATA.list("path.astar.hSeen"), List.copyOf(evaluated));

        List<Integer> g = new ArrayList<>();
        List<Integer> h = new ArrayList<>();
        List<Integer> f = new ArrayList<>();
        for (int index = 0; index < route.path().size(); index++) {
            int cell = route.path().get(index);
            int estimate = Math.abs(cell / columns - targetRow) + Math.abs(cell % columns - targetColumn);
            g.add(index); // every edge costs 1, so g is the number of steps taken
            h.add(estimate);
            f.add(index + estimate);
        }
        assertEquals(DATA.list("path.astar.g"), g);
        assertEquals(DATA.list("path.astar.h"), h);
        assertEquals(DATA.list("path.astar.f"), f);

        TreeSet<Integer> zeroEvaluated = new TreeSet<>();
        var plain = ShortestPathAlgorithms.aStar(grid, source, target, cell -> {
            zeroEvaluated.add(cell);
            return 0;
        }).orElseThrow();
        assertEquals(route.distance(), plain.distance());
        assertEquals(DATA.number("path.astar.zeroSeen"), zeroEvaluated.size());
        assertTrue(evaluated.size() < zeroEvaluated.size());
    }

    @Test
    void allowingVertexKImprovesExactlyTheShownEntry() {
        WeightedGraph graph = weighted("path.fw");
        int n = graph.vertexCount();
        long inf = Long.MAX_VALUE / 4;
        long[][] d = new long[n][n];
        for (long[] row : d) {
            Arrays.fill(row, inf);
        }
        for (int v = 0; v < n; v++) {
            d[v][v] = 0;
        }
        for (WeightedGraph.Edge edge : graph.edges()) {
            d[edge.from()][edge.to()] = Math.min(d[edge.from()][edge.to()], edge.weight());
        }
        int k = DATA.number("path.fw.k");
        long[][] before = null;
        long[][] after = null;
        for (int via = 0; via < n; via++) {
            if (via == k) {
                before = copy(d);
            }
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    d[i][j] = Math.min(d[i][j], d[i][via] + d[via][j]);
                }
            }
            if (via == k) {
                after = copy(d);
            }
        }
        assertEquals(DATA.strings("path.fw.before"), flatten(before, inf));
        assertEquals(DATA.strings("path.fw.after"), flatten(after, inf));
        assertEquals(DATA.strings("path.fw.final"), flatten(d, inf));

        int from = DATA.number("path.fw.from");
        int to = DATA.number("path.fw.to");
        int changed = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (before[i][j] != after[i][j]) {
                    changed++;
                    assertEquals(from, i);
                    assertEquals(to, j);
                }
            }
        }
        assertEquals(1, changed);
        assertEquals(DATA.number("path.fw.oldValue"), before[from][to]);
        assertEquals(DATA.number("path.fw.newValue"), after[from][to]);
        assertEquals(after[from][to], before[from][k] + before[k][to]);

        var all = ShortestPathAlgorithms.floydWarshall(graph);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                OptionalLong library = all.distance(i, j);
                assertEquals(d[i][j] >= inf ? OptionalLong.empty() : OptionalLong.of(d[i][j]), library);
            }
        }
        Optional<List<Integer>> route = Optional.of(all.path(from, to));
        assertEquals(List.of(0, 1, 2), route.orElseThrow());
    }

    private static long[][] copy(long[][] matrix) {
        return Arrays.stream(matrix).map(long[]::clone).toArray(long[][]::new);
    }

    private static List<String> flatten(long[][] matrix, long inf) {
        List<String> cells = new ArrayList<>();
        for (long[] row : matrix) {
            for (long value : row) {
                cells.add(value >= inf ? "INF" : Long.toString(value));
            }
        }
        return cells;
    }
}
