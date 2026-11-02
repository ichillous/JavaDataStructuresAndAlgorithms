package io.github.ichillous.dsa.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class GraphTest {
    @Test
    void directedEdgesPreserveInsertionOrderAndIgnoreDuplicates() {
        Graph graph = Graph.directed(4).addEdge(0, 2).addEdge(0, 1).addEdge(0, 2).addEdge(0, 0);
        assertTrue(graph.isDirected());
        assertEquals(4, graph.vertexCount());
        assertEquals(3, graph.edgeCount());
        assertEquals(List.of(2, 1, 0), graph.neighbors(0));
        assertEquals(List.of(), graph.neighbors(2));
    }

    @Test
    void undirectedReverseEdgesAndSelfLoopsAreCountedOnce() {
        Graph graph = Graph.undirected(3).addEdge(0, 1).addEdge(1, 0).addEdge(1, 1);
        assertFalse(graph.isDirected());
        assertEquals(2, graph.edgeCount());
        assertEquals(List.of(1), graph.neighbors(0));
        assertEquals(List.of(0, 1), graph.neighbors(1));
    }

    @Test
    void snapshotsAreImmutableAndDetachedFromLaterEdits() {
        Graph graph = Graph.directed(3).addEdge(0, 1);
        List<Integer> neighbors = graph.neighbors(0);
        graph.addEdge(0, 2);
        assertEquals(List.of(1), neighbors);
        assertThrows(UnsupportedOperationException.class, () -> neighbors.add(2));
    }

    @Test
    void invalidEndpointsAreRejectedBeforeMutation() {
        Graph graph = Graph.undirected(3);
        assertThrows(IllegalArgumentException.class, () -> Graph.directed(-1));
        assertThrows(IllegalArgumentException.class, () -> Graph.undirected(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(0, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.neighbors(3));
        assertEquals(0, graph.edgeCount());
        assertEquals(List.of(), graph.neighbors(0));
    }

    @Test
    void emptyGraphHasNoVerticesOrEdges() {
        Graph graph = Graph.directed(0);
        assertEquals(0, graph.vertexCount());
        assertEquals(0, graph.edgeCount());
        assertThrows(IndexOutOfBoundsException.class, () -> graph.neighbors(0));
    }

    @Test
    void weightedGraphRetainsParallelEdgesAndSignedLongWeights() {
        WeightedGraph graph = WeightedGraph.undirected(3)
                .addEdge(0, 1, Long.MIN_VALUE).addEdge(1, 0, 7).addEdge(2, 2, Long.MAX_VALUE);
        assertFalse(graph.isDirected());
        assertEquals(3, graph.vertexCount());
        assertEquals(3, graph.edgeCount());
        assertEquals(List.of(new WeightedGraph.Edge(1, 0, Long.MIN_VALUE),
                new WeightedGraph.Edge(1, 0, 7)), graph.neighbors(1));
        assertEquals(List.of(new WeightedGraph.Edge(2, 2, Long.MAX_VALUE)), graph.neighbors(2));
        assertEquals(new WeightedGraph.Edge(1, 0, 7), graph.edges().get(1));
    }

    @Test
    void weightedDirectedEdgesAndSnapshotsHaveCorrectOrientation() {
        WeightedGraph graph = WeightedGraph.directed(3).addEdge(0, 1, 5);
        List<WeightedGraph.Edge> neighbors = graph.neighbors(0);
        List<WeightedGraph.Edge> edges = graph.edges();
        graph.addEdge(0, 2, 10);
        assertTrue(graph.isDirected());
        assertEquals(List.of(), graph.neighbors(1));
        assertEquals(List.of(new WeightedGraph.Edge(0, 1, 5)), neighbors);
        assertEquals(neighbors, edges);
        assertThrows(UnsupportedOperationException.class, neighbors::clear);
        assertThrows(UnsupportedOperationException.class, edges::clear);
    }

    @Test
    void weightedInvalidEndpointsLeaveGraphUnchanged() {
        WeightedGraph graph = WeightedGraph.directed(1);
        assertThrows(IllegalArgumentException.class, () -> WeightedGraph.directed(-1));
        assertThrows(IllegalArgumentException.class, () -> WeightedGraph.undirected(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(0, 1, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(-1, 0, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.neighbors(-1));
        assertEquals(0, graph.edgeCount());
        assertEquals(List.of(), graph.neighbors(0));
    }
}
