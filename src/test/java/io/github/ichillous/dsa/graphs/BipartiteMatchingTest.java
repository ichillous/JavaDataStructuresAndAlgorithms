package io.github.ichillous.dsa.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.ichillous.dsa.graphs.BipartiteMatching.Pair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BipartiteMatchingTest {
    private static final long SEED = 0xB1AL;

    private static int bruteForce(int left, int[] adjacencyMask, int u, int usedRight) {
        if (u == left) {
            return 0;
        }
        int best = bruteForce(left, adjacencyMask, u + 1, usedRight);
        for (int v = 0; v < 32; v++) {
            if ((adjacencyMask[u] >> v & 1) == 1 && (usedRight >> v & 1) == 0) {
                best = Math.max(best, 1 + bruteForce(left, adjacencyMask, u + 1, usedRight | 1 << v));
            }
        }
        return best;
    }

    @Test
    void matchingSizeEqualsBruteForce() {
        Random random = new Random(SEED);
        for (int trial = 0; trial < 500; trial++) {
            int left = random.nextInt(8);
            int right = random.nextInt(8);
            List<Pair> edges = new ArrayList<>();
            int[] mask = new int[left];
            if (left > 0 && right > 0) {
                int count = random.nextInt(25);
                for (int i = 0; i < count; i++) {
                    int u = random.nextInt(left);
                    int v = random.nextInt(right);
                    edges.add(new Pair(u, v)); // duplicates are intentional
                    mask[u] |= 1 << v;
                }
            }
            List<Pair> snapshot = List.copyOf(edges);
            List<Pair> matching = BipartiteMatching.maximumMatching(left, right, edges);
            String message = "seed=" + SEED + " trial=" + trial;
            assertEquals(snapshot, edges, message);
            assertEquals(bruteForce(left, mask, 0, 0), matching.size(), message);
            Set<Integer> usedLeft = new HashSet<>();
            Set<Integer> usedRight = new HashSet<>();
            int previousLeft = -1;
            for (Pair p : matching) {
                assertTrue(snapshot.contains(p), message);
                assertTrue(usedLeft.add(p.left()) && usedRight.add(p.right()), message);
                assertTrue(p.left() > previousLeft, message);
                previousLeft = p.left();
            }
        }
    }

    @Test
    void matchingEmptyAndSingleton() {
        assertEquals(List.of(), BipartiteMatching.maximumMatching(0, 0, List.of()));
        assertEquals(List.of(), BipartiteMatching.maximumMatching(3, 3, List.of()));
        assertEquals(List.of(new Pair(0, 0)),
                BipartiteMatching.maximumMatching(1, 1, List.of(new Pair(0, 0), new Pair(0, 0))));
        assertThrows(UnsupportedOperationException.class,
                () -> BipartiteMatching.maximumMatching(1, 1, List.of(new Pair(0, 0)))
                        .add(new Pair(0, 0)));
    }

    @Test
    void augmentingPathReassignsEarlierChoices() {
        // Greedy would take 0-0 and block vertex 1; the maximum matching has size 2.
        List<Pair> matching = BipartiteMatching.maximumMatching(2, 2,
                List.of(new Pair(0, 0), new Pair(0, 1), new Pair(1, 0)));
        assertEquals(List.of(new Pair(0, 1), new Pair(1, 0)), matching);
    }

    @Test
    void invalidArgumentsRejected() {
        assertThrows(NullPointerException.class,
                () -> BipartiteMatching.maximumMatching(1, 1, null));
        List<Pair> withNull = new ArrayList<>();
        withNull.add(null);
        assertThrows(NullPointerException.class,
                () -> BipartiteMatching.maximumMatching(1, 1, withNull));
        assertThrows(IllegalArgumentException.class,
                () -> BipartiteMatching.maximumMatching(-1, 1, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> BipartiteMatching.maximumMatching(1, -1, List.of()));
        assertThrows(IndexOutOfBoundsException.class,
                () -> BipartiteMatching.maximumMatching(1, 1, List.of(new Pair(1, 0))));
        assertThrows(IndexOutOfBoundsException.class,
                () -> BipartiteMatching.maximumMatching(1, 1, List.of(new Pair(-1, 0))));
        assertThrows(IndexOutOfBoundsException.class,
                () -> BipartiteMatching.maximumMatching(1, 1, List.of(new Pair(0, 1))));
        assertThrows(IndexOutOfBoundsException.class,
                () -> BipartiteMatching.maximumMatching(1, 1, List.of(new Pair(0, -1))));
    }

    @Test
    void longAugmentingPathIsFoundIteratively() {
        // The first phase matches i-i greedily; the last left vertex then needs one
        // augmenting path of length n that shifts every earlier vertex.
        int n = 100_000;
        List<Pair> edges = new ArrayList<>();
        for (int i = 0; i + 1 < n; i++) {
            edges.add(new Pair(i, i));
            edges.add(new Pair(i, i + 1));
        }
        edges.add(new Pair(n - 1, 0));
        assertEquals(n, BipartiteMatching.maximumMatching(n, n, edges).size());
    }
}
