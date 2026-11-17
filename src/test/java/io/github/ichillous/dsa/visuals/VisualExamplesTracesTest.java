package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.algorithms.ArrayPatterns;
import io.github.ichillous.dsa.algorithms.Selection;
import io.github.ichillous.dsa.algorithms.SortAlgorithms;
import io.github.ichillous.dsa.algorithms.StringAlgorithms;
import io.github.ichillous.dsa.graphs.Graph;
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks that the final state of each interactive step-through equals what the maintained
 * library returns for the same fixture. The traces are conceptual (they do not copy the
 * library's internals), so the result is the contract being compared.
 */
class VisualExamplesTracesTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-traces.properties");

    @Test
    void insertionSortProducesTheFixtureResult() {
        int[] values = DATA.ints("traces.insertion.values");
        SortAlgorithms.insertionSort(values);
        assertArrayEquals(DATA.ints("traces.insertion.sorted"), values);
    }

    @Test
    void quickselectAnswerIsTheKthSmallest() {
        int[] values = DATA.ints("traces.quickselect.values");
        int k = DATA.number("traces.quickselect.k");
        assertEquals(DATA.number("traces.quickselect.answer"), Selection.kthSmallest(values, k));
    }

    @Test
    void radixSortProducesTheFixtureResult() {
        int[] values = DATA.ints("traces.radix.values");
        SortAlgorithms.radixSort(values);
        assertArrayEquals(DATA.ints("traces.radix.sorted"), values);
    }

    @Test
    void twoPointersFindsTheFixturePair() {
        Optional<ArrayPatterns.IndexPair> pair = ArrayPatterns.pairWithSum(
                DATA.ints("traces.two.values"), DATA.number("traces.two.target"));
        String[] expected = DATA.text("traces.two.result").split(":");
        assertEquals(new ArrayPatterns.IndexPair(Integer.parseInt(expected[0]), Integer.parseInt(expected[1])),
                pair.orElseThrow());
    }

    @Test
    void slidingWindowFindsTheFixtureSpan() {
        StringAlgorithms.Span span = ArrayPatterns.longestSubstringWithoutRepeats(DATA.text("traces.window.text"));
        String[] expected = DATA.text("traces.window.result").split(":");
        assertEquals(new StringAlgorithms.Span(Integer.parseInt(expected[0]), Integer.parseInt(expected[1])), span);
    }

    @Test
    void zFunctionProducesTheFixtureArray() {
        assertArrayEquals(DATA.ints("traces.z.result"), StringAlgorithms.zFunction(DATA.text("traces.z.text")));
    }

    @Test
    void depthFirstVisitsTheFixtureOrder() {
        Graph graph = graph(Graph.undirected(DATA.number("traces.dfs.vertices")), "traces.dfs.edges");
        assertEquals(DATA.list("traces.dfs.order"), GraphAlgorithms.depthFirst(graph, 0));
    }

    @Test
    void topologicalSortEmitsTheFixtureOrder() {
        Graph graph = graph(Graph.directed(DATA.number("traces.topo.vertices")), "traces.topo.edges");
        assertEquals(DATA.list("traces.topo.order"), GraphAlgorithms.topologicalSort(graph));
    }

    private static Graph graph(Graph graph, String key) {
        for (String edge : DATA.strings(key)) {
            String[] ends = edge.split(":");
            graph.addEdge(Integer.parseInt(ends[0]), Integer.parseInt(ends[1]));
        }
        return graph;
    }
}
