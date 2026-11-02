package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.algorithms.Backtracking;
import io.github.ichillous.dsa.algorithms.DynamicProgramming;
import io.github.ichillous.dsa.algorithms.GreedyAlgorithms;
import io.github.ichillous.dsa.algorithms.SearchAlgorithms;
import io.github.ichillous.dsa.algorithms.SortAlgorithms;
import io.github.ichillous.dsa.algorithms.StringAlgorithms;
import io.github.ichillous.dsa.collections.ArrayQueue;
import io.github.ichillous.dsa.collections.ArrayStack;
import io.github.ichillous.dsa.collections.ChainedHashMap;
import io.github.ichillous.dsa.collections.DoublyLinkedList;
import io.github.ichillous.dsa.collections.DynamicArray;
import io.github.ichillous.dsa.collections.LruCache;
import io.github.ichillous.dsa.graphs.AdvancedGraphAlgorithms;
import io.github.ichillous.dsa.graphs.Graph;
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import io.github.ichillous.dsa.structures.AvlTree;
import io.github.ichillous.dsa.structures.BinaryMinHeap;
import io.github.ichillous.dsa.structures.FenwickTree;
import io.github.ichillous.dsa.structures.SegmentTree;
import io.github.ichillous.dsa.structures.Trie;
import io.github.ichillous.dsa.structures.UnionFind;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/** The SVG and browser traces share these fixtures with real Java API checks. */
class VisualExamplesTest {
    private static final Properties DATA = new Properties();

    @BeforeAll
    static void loadCanonicalExamples() throws Exception {
        Path path = Path.of(System.getProperty("basedir", "."), "docs", "visuals", "data", "examples.properties");
        try (InputStream stream = Files.newInputStream(path)) {
            DATA.load(stream);
        }
    }

    private static String text(String key) {
        String value = DATA.getProperty(key);
        assertNotNull(value, "Missing visual fixture: " + key);
        return value;
    }

    private static int number(String key) {
        return Integer.parseInt(text(key));
    }

    private static int[] ints(String key) {
        return Arrays.stream(text(key).split(",")).mapToInt(Integer::parseInt).toArray();
    }

    private static List<Integer> list(String key) {
        return Arrays.stream(ints(key)).boxed().toList();
    }

    private static long[] longs(String key) {
        return Arrays.stream(ints(key)).asLongStream().toArray();
    }

    @Test
    void insertionPreservesTheShiftedSuffix() {
        DynamicArray<Integer> values = new DynamicArray<>();
        for (int value : ints("array.before")) values.add(value);
        values.add(number("array.index"), number("array.value"));
        List<Integer> observed = new ArrayList<>();
        values.forEach(observed::add);
        assertEquals(list("array.after"), observed);
    }

    @Test
    void stackPushAndPopUseTheSameEnd() {
        ArrayStack<Integer> stack = new ArrayStack<>();
        for (int value : ints("stack.input")) stack.push(value);
        List<Integer> popped = new ArrayList<>();
        while (!stack.isEmpty()) popped.add(stack.pop());
        assertEquals(list("stack.popped"), popped);
    }

    @Test
    void unlinkRepairsBothNeighbors() {
        DoublyLinkedList<Integer> values = new DoublyLinkedList<>();
        for (int value : ints("linked.before")) values.addLast(value);
        assertEquals(ints("linked.before")[number("linked.index")], values.remove(number("linked.index")));
        List<Integer> observed = new ArrayList<>();
        values.forEach(observed::add);
        assertEquals(list("linked.after"), observed);
        assertEquals(30, values.removeLast());
        assertEquals(10, values.removeFirst());
        assertTrue(values.isEmpty());
    }

    @Test
    void physicalQueueSlotsAgreeWithLogicalFifoOrder() {
        ArrayQueue<Integer> queue = new ArrayQueue<>(number("queue.capacity"));
        for (int value : ints("queue.input")) queue.enqueue(value);
        for (int value : ints("queue.removed")) assertEquals(value, queue.dequeue());
        for (int value : ints("queue.append")) queue.enqueue(value);
        List<Integer> observed = new ArrayList<>();
        while (!queue.isEmpty()) observed.add(queue.dequeue());
        assertEquals(list("queue.after"), observed);
        int[] physical = ints("queue.physical");
        int[] logical = ints("queue.after");
        for (int index = 0; index < logical.length; index++) {
            assertEquals(logical[index], physical[(number("queue.head") + index) % physical.length]);
        }
    }

    @Test
    void equalHashesDoNotMakeEqualKeys() {
        String[] keys = text("hash.keys").split(",");
        assertNotEquals(keys[0], keys[1]);
        assertEquals(number("hash.hash"), keys[0].hashCode());
        assertEquals(keys[0].hashCode(), keys[1].hashCode());
        ChainedHashMap<String, Integer> map = new ChainedHashMap<>(4);
        map.put(keys[0], 1);
        map.put(keys[1], 2);
        assertEquals(1, map.remove(keys[0]));
        assertEquals(2, map.get(keys[1]));
        assertEquals(1, map.size());
    }

    @Test
    void aHitPromotesBeforeLruEviction() {
        LruCache<String, Integer> cache = new LruCache<>(number("lru.capacity"));
        List<String> order = new ArrayList<>(List.of(text("lru.before").split(",")));
        cache.put(order.get(0), 10);
        cache.put(order.get(1), 20);
        assertEquals(10, cache.get("A"));
        order.remove("A");
        order.add("A");
        assertEquals(List.of(text("lru.afterHit").split(",")), order);
        cache.put("C", 30);
        order.remove(0);
        order.add("C");
        assertEquals(List.of(text("lru.afterPut").split(",")), order);
        assertNull(cache.get("B"));
        assertEquals(10, cache.get("A"));
        assertEquals(30, cache.get("C"));
    }

    @Test
    void aThreeNodeAvlRotationHasTheShownBalancedShape() {
        AvlTree<Integer> tree = new AvlTree<>();
        for (int value : ints("avl.input")) tree.add(value);
        assertEquals(list("avl.inorder"), tree.inOrder());
        assertEquals(number("avl.height"), tree.height());
        // A three-key BST of height one must put the median at the root.
        assertEquals(20, tree.inOrder().get(1));
    }

    @Test
    void heapifyPreservesValuesAndMinimumRemovalOrder() {
        BinaryMinHeap<Integer> heap = new BinaryMinHeap<>(list("heap.input"));
        List<Integer> observed = new ArrayList<>();
        while (!heap.isEmpty()) observed.add(heap.remove());
        assertEquals(list("heap.sorted"), observed);
    }

    @Test
    void removingCarRetainsTheCartPathAndOtherBranch() {
        Trie trie = new Trie();
        for (String word : text("trie.words").split(",")) trie.add(word);
        assertTrue(trie.remove(text("trie.remove")));
        assertFalse(trie.contains(text("trie.remove")));
        assertTrue(trie.startsWith(text("trie.remove")));
        assertEquals(List.of(text("trie.after").split(",")), trie.wordsWithPrefix("ca", 10));
    }

    @Test
    void unionFindHasTwoShownComponents() {
        UnionFind union = new UnionFind(number("union.size"));
        for (String pair : text("union.pairs").split(",")) {
            String[] ends = pair.split(":");
            union.union(Integer.parseInt(ends[0]), Integer.parseInt(ends[1]));
        }
        assertEquals(number("union.components"), union.components());
        assertTrue(union.connected(0, 2));
        assertTrue(union.connected(3, 4));
        assertFalse(union.connected(0, 3));
    }

    @Test
    void fenwickBlocksGiveTheShownPrefixAndUpdatedRange() {
        FenwickTree tree = new FenwickTree(longs("fenwick.values"));
        assertEquals(number("fenwick.prefix"), tree.prefixSum(number("fenwick.prefixEnd")));
        tree.add(number("fenwick.index"), number("fenwick.delta"));
        int[] range = ints("fenwick.range");
        assertEquals(number("fenwick.rangeSum"), tree.rangeSum(range[0], range[1]));
    }

    @Test
    void segmentUpdateRepairsIntervalMinima() {
        SegmentTree tree = new SegmentTree(longs("segment.values"));
        int[] range = ints("segment.range");
        assertEquals(number("segment.before"), tree.rangeMin(range[0], range[1]).orElseThrow());
        tree.set(number("segment.index"), number("segment.value"));
        assertEquals(number("segment.after"), tree.rangeMin(range[0], range[1]).orElseThrow());
    }

    @Test
    void binaryTraceMaintainsBothBoundaryInvariants() {
        int[] sorted = ints("binary.values");
        int target = number("binary.target");
        int low = 0;
        int high = sorted.length;
        for (String encoded : text("binary.steps").split(",")) {
            int[] step = Arrays.stream(encoded.split(":")).mapToInt(Integer::parseInt).toArray();
            assertArrayEquals(new int[]{low, high, low + (high - low) / 2}, step);
            for (int i = 0; i < low; i++) assertTrue(sorted[i] < target);
            for (int i = high; i < sorted.length; i++) assertTrue(sorted[i] >= target);
            if (sorted[step[2]] < target) low = step[2] + 1;
            else high = step[2];
        }
        assertEquals(low, high);
        assertEquals(number("binary.result"), low);
        assertEquals(low, SearchAlgorithms.lowerBound(sorted, target));
    }

    @Test
    void sortingTraceEndpointsAgreeWithMaintainedSorts() {
        int[] merge = ints("merge.values");
        SortAlgorithms.mergeSort(merge);
        assertArrayEquals(ints("merge.sorted"), merge);
        int[] quick = ints("quick.values");
        SortAlgorithms.quickSort(quick);
        assertArrayEquals(ints("quick.sorted"), quick);
        int[] bubble = ints("bubble.values");
        int[] afterSwap = bubble.clone();
        int first = afterSwap[0];
        afterSwap[0] = afterSwap[1];
        afterSwap[1] = first;
        assertArrayEquals(ints("bubble.afterSwap"), afterSwap);
        SortAlgorithms.bubbleSort(bubble);
        assertArrayEquals(ints("bubble.sorted"), bubble);
    }

    @Test
    void coinDpEveryCellMatchesAnExecutableSubproblem() {
        int[] expected = ints("coins.table");
        assertEquals(number("coins.amount") + 1, expected.length);
        for (int amount = 0; amount < expected.length; amount++) {
            assertEquals(expected[amount], DynamicProgramming.minimumCoins(ints("coins.values"), amount));
        }
    }

    @Test
    void knapsackRowsNeverReuseAnItem() {
        int[] weights = ints("knapsack.weights");
        int[] values = ints("knapsack.values");
        int[] first = ints("knapsack.first");
        int[] last = ints("knapsack.final");
        for (int capacity = 0; capacity <= number("knapsack.capacity"); capacity++) {
            assertEquals(first[capacity], DynamicProgramming.knapsack01(
                    new int[]{weights[0]}, new int[]{values[0]}, capacity));
            assertEquals(last[capacity], DynamicProgramming.knapsack01(weights, values, capacity));
        }
    }

    @Test
    void kmpKeepsOverlappingMatches() {
        assertEquals(list("kmp.matches"), StringAlgorithms.kmpFindAll(text("kmp.text"), text("kmp.pattern")));
    }

    @Test
    void queensBoardAndChosenBranchAreValid() {
        List<List<Integer>> solutions = Backtracking.nQueens(number("queens.n"));
        assertEquals(number("queens.count"), solutions.size());
        assertEquals(list("queens.first"), solutions.get(0));
        for (int row = 0; row < number("queens.n"); row++) {
            for (int other = 0; other < row; other++) {
                assertNotEquals(solutions.get(0).get(row), solutions.get(0).get(other));
                assertNotEquals(row - other, Math.abs(solutions.get(0).get(row) - solutions.get(0).get(other)));
            }
        }
    }

    private static Graph graph(String prefix, boolean directed) {
        Graph graph = directed ? Graph.directed(number(prefix + ".vertices")) : Graph.undirected(number(prefix + ".vertices"));
        for (String encoded : text(prefix + ".edges").split(",")) {
            String[] ends = encoded.split(":");
            graph.addEdge(Integer.parseInt(ends[0]), Integer.parseInt(ends[1]));
        }
        return graph;
    }

    private static WeightedGraph weighted(String prefix) {
        WeightedGraph graph = WeightedGraph.directed(number(prefix + ".vertices"));
        for (String encoded : text(prefix + ".edges").split(",")) {
            String[] edge = encoded.split(":");
            graph.addEdge(Integer.parseInt(edge[0]), Integer.parseInt(edge[1]), Long.parseLong(edge[2]));
        }
        return graph;
    }

    @Test
    void earliestFinishingActivitiesAllowTouchingEndpoints() {
        List<GreedyAlgorithms.Interval> intervals = Arrays.stream(text("greedy.intervals").split(","))
                .map(encoded -> encoded.split(":"))
                .map(pair -> new GreedyAlgorithms.Interval(Integer.parseInt(pair[0]), Integer.parseInt(pair[1])))
                .toList();
        List<GreedyAlgorithms.Interval> selected = Arrays.stream(text("greedy.selected").split(","))
                .map(encoded -> encoded.split(":"))
                .map(pair -> new GreedyAlgorithms.Interval(Integer.parseInt(pair[0]), Integer.parseInt(pair[1])))
                .toList();
        assertEquals(selected, GreedyAlgorithms.selectActivities(intervals));
    }

    @Test
    void bfsFrontierOrderAndShortestPathMatch() {
        Graph graph = graph("bfs", false);
        assertEquals(list("bfs.order"), GraphAlgorithms.breadthFirst(graph, 0));
        assertEquals(list("bfs.path"), GraphAlgorithms.shortestPath(graph, 0, 4));
        assertTrue(GraphAlgorithms.shortestPath(graph, 0, 5).isEmpty());
    }

    @Test
    void dijkstraPathAndUnreachableVertexMatch() {
        var paths = GraphAlgorithms.dijkstra(weighted("dijkstra"), 0);
        String[] expected = text("dijkstra.distances").split(",");
        for (int vertex = 0; vertex < expected.length; vertex++) {
            if (expected[vertex].equals("unreachable")) assertTrue(paths.distanceTo(vertex).isEmpty());
            else assertEquals(Long.parseLong(expected[vertex]), paths.distanceTo(vertex).orElseThrow());
        }
        assertEquals(list("dijkstra.path"), paths.pathTo(3));
    }

    @Test
    void signedPathAndTheOptionalAddedNegativeCycleMatch() {
        WeightedGraph graph = weighted("bellman");
        var paths = AdvancedGraphAlgorithms.bellmanFord(graph, 0);
        int[] expected = ints("bellman.distances");
        for (int vertex = 0; vertex < expected.length; vertex++) assertEquals(expected[vertex], paths.distanceTo(vertex).orElseThrow());
        assertEquals(list("bellman.path"), paths.pathTo(2));
        String[] edge = text("bellman.cycleEdge").split(":");
        graph.addEdge(Integer.parseInt(edge[0]), Integer.parseInt(edge[1]), Long.parseLong(edge[2]));
        assertThrows(IllegalArgumentException.class, () -> AdvancedGraphAlgorithms.bellmanFord(graph, 0));
    }

    @Test
    void stronglyConnectedGroupsAndBridgeAreCanonical() {
        List<List<Integer>> groups = Arrays.stream(text("scc.groups").split(","))
                .map(group -> Arrays.stream(group.split(":")).map(Integer::parseInt).toList()).toList();
        assertEquals(groups, AdvancedGraphAlgorithms.stronglyConnectedComponents(graph("scc", true)));
        String[] bridge = text("bridges.result").split(":");
        assertEquals(List.of(new AdvancedGraphAlgorithms.Bridge(Integer.parseInt(bridge[0]), Integer.parseInt(bridge[1]))),
                AdvancedGraphAlgorithms.bridges(graph("bridges", false)));
    }
}
