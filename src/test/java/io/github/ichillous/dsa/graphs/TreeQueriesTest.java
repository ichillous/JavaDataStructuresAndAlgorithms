package io.github.ichillous.dsa.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TreeQueriesTest {
    private static final long SEED = 0x1CA;

    /** Random labeled tree: attach each vertex to a random earlier one, then relabel. */
    private static Graph randomTree(Random random, int vertices) {
        int[] label = new int[vertices];
        for (int i = 0; i < vertices; i++) {
            label[i] = i;
        }
        for (int i = vertices - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int t = label[i];
            label[i] = label[j];
            label[j] = t;
        }
        Graph tree = Graph.undirected(vertices);
        for (int i = 1; i < vertices; i++) {
            tree.addEdge(label[i], label[random.nextInt(i)]);
        }
        return tree;
    }

    private static int[] bfsDistances(Graph graph, int start) {
        int[] dist = new int[graph.vertexCount()];
        Arrays.fill(dist, -1);
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(start);
        dist[start] = 0;
        while (!queue.isEmpty()) {
            int v = queue.poll();
            for (int w : graph.neighbors(v)) {
                if (dist[w] < 0) {
                    dist[w] = dist[v] + 1;
                    queue.add(w);
                }
            }
        }
        return dist;
    }

    @Test
    void lcaMatchesNaiveParentWalk() {
        Random random = new Random(SEED);
        for (int round = 0; round < 60; round++) {
            int vertices = 1 + random.nextInt(200);
            Graph tree = randomTree(random, vertices);
            int root = random.nextInt(vertices);
            String context = "seed=" + SEED + " round=" + round + " V=" + vertices + " root=" + root;
            int edgesBefore = tree.edgeCount();
            LowestCommonAncestor lca = new LowestCommonAncestor(tree, root);
            assertEquals(edgesBefore, tree.edgeCount(), context);

            int[] depth = bfsDistances(tree, root);
            int[] parent = new int[vertices];
            for (int v = 0; v < vertices; v++) {
                parent[v] = v;
                for (int w : tree.neighbors(v)) {
                    if (depth[w] == depth[v] - 1) {
                        parent[v] = w;
                    }
                }
                assertEquals(depth[v], lca.depth(v), context);
            }
            for (int a = 0; a < vertices; a++) {
                for (int b = 0; b < vertices; b++) {
                    Set<Integer> ancestorsOfA = new HashSet<>();
                    for (int x = a; ; x = parent[x]) {
                        ancestorsOfA.add(x);
                        if (x == root) {
                            break;
                        }
                    }
                    int expected = b;
                    while (!ancestorsOfA.contains(expected)) {
                        expected = parent[expected];
                    }
                    assertEquals(expected, lca.lca(a, b), context + " a=" + a + " b=" + b);
                    assertEquals(bfsDistances(tree, a)[b], lca.distance(a, b),
                            context + " a=" + a + " b=" + b);
                }
                int ancestor = a;
                for (int k = 0; k <= depth[a]; k++) {
                    assertEquals(OptionalInt.of(ancestor), lca.kthAncestor(a, k),
                            context + " v=" + a + " k=" + k);
                    ancestor = parent[ancestor];
                }
                assertEquals(OptionalInt.empty(), lca.kthAncestor(a, depth[a] + 1), context);
                assertEquals(OptionalInt.empty(), lca.kthAncestor(a, Integer.MAX_VALUE), context);
            }
        }
    }

    @Test
    void lcaOnKnownTree() {
        //        0
        //      /   \
        //     1     2
        //    / \     \
        //   3   4     5
        Graph tree = Graph.undirected(6).addEdge(0, 1).addEdge(0, 2).addEdge(1, 3)
                .addEdge(1, 4).addEdge(2, 5);
        LowestCommonAncestor lca = new LowestCommonAncestor(tree, 0);
        assertEquals(1, lca.lca(3, 4));
        assertEquals(0, lca.lca(3, 5));
        assertEquals(1, lca.lca(1, 4));
        assertEquals(3, lca.lca(3, 3));
        assertEquals(4, lca.distance(3, 5));
        assertEquals(2, lca.depth(3));
        assertEquals(OptionalInt.of(0), lca.kthAncestor(3, 2));
        // Re-rooting changes the answers.
        LowestCommonAncestor rerooted = new LowestCommonAncestor(tree, 3);
        assertEquals(1, rerooted.lca(4, 0));
        assertEquals(4, rerooted.depth(5));
    }

    @Test
    void rejectsNonTrees() {
        Graph cycle = Graph.undirected(3).addEdge(0, 1).addEdge(1, 2).addEdge(2, 0);
        Graph disconnected = Graph.undirected(4).addEdge(0, 1).addEdge(2, 3).addEdge(2, 3);
        Graph forest = Graph.undirected(4).addEdge(0, 1).addEdge(1, 0).addEdge(2, 3);
        Graph directed = Graph.directed(3).addEdge(0, 1).addEdge(1, 2);
        Graph selfLoop = Graph.undirected(3).addEdge(0, 1).addEdge(1, 1);
        Graph tooFew = Graph.undirected(3).addEdge(0, 1);
        Graph empty = Graph.undirected(0);
        for (Graph bad : List.of(cycle, disconnected, forest, directed, selfLoop, tooFew, empty)) {
            assertThrows(IllegalArgumentException.class, () -> new LowestCommonAncestor(bad, 0));
            assertThrows(IllegalArgumentException.class, () -> TreeAlgorithms.diameterPath(bad));
        }
        Graph path = Graph.undirected(3).addEdge(0, 1).addEdge(1, 2);
        assertThrows(IndexOutOfBoundsException.class, () -> new LowestCommonAncestor(path, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> new LowestCommonAncestor(path, -1));
        assertThrows(NullPointerException.class, () -> new LowestCommonAncestor(null, 0));
        assertThrows(NullPointerException.class, () -> TreeAlgorithms.diameterPath(null));
    }

    @Test
    void queryArgumentsAreValidated() {
        Graph path = Graph.undirected(3).addEdge(0, 1).addEdge(1, 2);
        LowestCommonAncestor lca = new LowestCommonAncestor(path, 0);
        assertThrows(IllegalArgumentException.class, () -> lca.kthAncestor(2, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> lca.kthAncestor(3, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> lca.lca(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> lca.lca(0, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> lca.depth(3));
        assertThrows(IndexOutOfBoundsException.class, () -> lca.distance(0, 3));
    }

    @Test
    void constructorDoesNotRetainTheGraph() {
        Graph tree = Graph.undirected(3).addEdge(0, 1).addEdge(1, 2);
        LowestCommonAncestor lca = new LowestCommonAncestor(tree, 0);
        tree.addEdge(0, 2);
        assertEquals(2, lca.distance(0, 2));
    }

    @Test
    void diameterMatchesAllPairsBfs() {
        Random random = new Random(SEED);
        for (int round = 0; round < 100; round++) {
            int vertices = 1 + random.nextInt(120);
            Graph tree = randomTree(random, vertices);
            String context = "seed=" + SEED + " round=" + round + " V=" + vertices;
            int best = 0;
            for (int s = 0; s < vertices; s++) {
                for (int d : bfsDistances(tree, s)) {
                    best = Math.max(best, d);
                }
            }
            List<Integer> path = TreeAlgorithms.diameterPath(tree);
            assertEquals(best + 1, path.size(), context);
            assertEquals(path.size(), new HashSet<>(path).size(), context + " simple");
            for (int i = 1; i < path.size(); i++) {
                assertTrue(tree.neighbors(path.get(i - 1)).contains(path.get(i)), context);
            }
            assertThrows(UnsupportedOperationException.class, () -> path.add(0));
        }
    }

    @Test
    void diameterTieRuleIsDeterministic() {
        // From 0 the farthest vertices are 1 and 2 (smallest: 1); from 1 the farthest is 2.
        Graph star = Graph.undirected(3).addEdge(0, 2).addEdge(0, 1);
        assertEquals(List.of(1, 0, 2), TreeAlgorithms.diameterPath(star));
        // Path 3-0-1-2: from 0 the farthest is 2 (distance 2); from 2 the farthest is 3.
        Graph path = Graph.undirected(4).addEdge(3, 0).addEdge(0, 1).addEdge(1, 2);
        assertEquals(List.of(2, 1, 0, 3), TreeAlgorithms.diameterPath(path));
    }

    @Test
    void diameterDoesNotMutateTheGraph() {
        Graph tree = Graph.undirected(4).addEdge(0, 1).addEdge(1, 2).addEdge(1, 3);
        List<List<Integer>> before = new ArrayList<>();
        for (int v = 0; v < 4; v++) {
            before.add(tree.neighbors(v));
        }
        TreeAlgorithms.diameterPath(tree);
        for (int v = 0; v < 4; v++) {
            assertEquals(before.get(v), tree.neighbors(v));
        }
        assertEquals(3, tree.edgeCount());
    }

    @Test
    void deepPathTree() {
        int vertices = 100_000;
        Graph path = Graph.undirected(vertices);
        for (int i = 1; i < vertices; i++) {
            path.addEdge(i - 1, i);
        }
        LowestCommonAncestor lca = new LowestCommonAncestor(path, 0);
        assertEquals(vertices - 1, lca.depth(vertices - 1));
        assertEquals(500, lca.lca(500, 99_999));
        assertEquals(vertices - 1, lca.distance(0, vertices - 1));
        assertEquals(OptionalInt.of(0), lca.kthAncestor(vertices - 1, vertices - 1));
        assertEquals(OptionalInt.empty(), lca.kthAncestor(5, 6));
        List<Integer> diameter = TreeAlgorithms.diameterPath(path);
        assertEquals(vertices, diameter.size());
        assertEquals(vertices - 1, diameter.get(0));
        assertEquals(0, diameter.get(vertices - 1));
    }

    @Test
    void singletonTree() {
        Graph one = Graph.undirected(1);
        LowestCommonAncestor lca = new LowestCommonAncestor(one, 0);
        assertEquals(0, lca.lca(0, 0));
        assertEquals(0, lca.depth(0));
        assertEquals(0, lca.distance(0, 0));
        assertEquals(OptionalInt.of(0), lca.kthAncestor(0, 0));
        assertEquals(OptionalInt.empty(), lca.kthAncestor(0, 1));
        assertEquals(List.of(0), TreeAlgorithms.diameterPath(one));
    }

    @Test
    void emptyGraphIsRejectedAsNotATree() {
        assertThrows(IllegalArgumentException.class,
                () -> TreeAlgorithms.diameterPath(Graph.undirected(0)));
    }
}
