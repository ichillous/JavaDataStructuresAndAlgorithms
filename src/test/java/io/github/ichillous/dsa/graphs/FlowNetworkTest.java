package io.github.ichillous.dsa.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class FlowNetworkTest {
    private static final long SEED = 0xF10L;

    private static FlowNetwork random(Random random, int n) {
        FlowNetwork network = new FlowNetwork(n);
        int edges = random.nextInt(20);
        for (int i = 0; i < edges; i++) {
            network.addEdge(random.nextInt(n), random.nextInt(n), random.nextInt(8));
        }
        return network;
    }

    private static long bruteForceMinCut(FlowNetwork network, int s, int t) {
        int n = network.vertexCount();
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << n); mask++) {
            if ((mask >> s & 1) == 0 || (mask >> t & 1) == 1) {
                continue;
            }
            best = Math.min(best, cutCapacity(network, mask));
        }
        return best;
    }

    private static long cutCapacity(FlowNetwork network, int sourceMask) {
        long sum = 0;
        for (FlowNetwork.Edge e : network.edges()) {
            if ((sourceMask >> e.from() & 1) == 1 && (sourceMask >> e.to() & 1) == 0) {
                sum += e.capacity();
            }
        }
        return sum;
    }

    @Test
    void maxFlowEqualsBruteForceMinCut() {
        Random random = new Random(SEED);
        for (int trial = 0; trial < 400; trial++) {
            int n = 2 + random.nextInt(7);
            FlowNetwork network = random(random, n);
            int s = random.nextInt(n);
            int t = (s + 1 + random.nextInt(n - 1)) % n;
            List<FlowNetwork.Edge> before = network.edges();
            FlowNetwork.MaxFlow flow = network.maxFlow(s, t);
            String message = "seed=" + SEED + " trial=" + trial;
            assertEquals(bruteForceMinCut(network, s, t), flow.value(), message);
            assertEquals(before, network.edges(), message);
            assertEquals(flow, network.maxFlow(s, t), message);
        }
    }

    @Test
    void flowIsFeasibleAndCutIsTight() {
        Random random = new Random(SEED + 1);
        for (int trial = 0; trial < 400; trial++) {
            int n = 2 + random.nextInt(7);
            FlowNetwork network = random(random, n);
            int s = random.nextInt(n);
            int t = (s + 1 + random.nextInt(n - 1)) % n;
            FlowNetwork.MaxFlow flow = network.maxFlow(s, t);
            String message = "seed=" + (SEED + 1) + " trial=" + trial;
            List<FlowNetwork.Edge> edges = network.edges();
            assertEquals(edges.size(), flow.edgeFlows().size(), message);
            long[] net = new long[n];
            for (int i = 0; i < edges.size(); i++) {
                long f = flow.edgeFlows().get(i);
                assertTrue(f >= 0 && f <= edges.get(i).capacity(), message);
                net[edges.get(i).from()] -= f;
                net[edges.get(i).to()] += f;
            }
            for (int v = 0; v < n; v++) {
                if (v == s) {
                    assertEquals(-flow.value(), net[v], message);
                } else if (v == t) {
                    assertEquals(flow.value(), net[v], message);
                } else {
                    assertEquals(0, net[v], message);
                }
            }
            int mask = 0;
            for (int v : flow.sourceSide()) {
                mask |= 1 << v;
            }
            assertTrue((mask >> s & 1) == 1 && (mask >> t & 1) == 0, message);
            assertEquals(flow.value(), cutCapacity(network, mask), message);
        }
    }

    @Test
    void unreachableSinkHasZeroFlow() {
        FlowNetwork network = new FlowNetwork(4).addEdge(0, 1, 5).addEdge(2, 3, 5);
        FlowNetwork.MaxFlow flow = network.maxFlow(0, 3);
        assertEquals(0, flow.value());
        assertEquals(List.of(0L, 0L), flow.edgeFlows());
        assertEquals(List.of(0, 1), flow.sourceSide());
    }

    @Test
    void sourceEqualsSinkRejected() {
        FlowNetwork network = new FlowNetwork(2).addEdge(0, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> network.maxFlow(1, 1));
    }

    @Test
    void validationAndEmptyAndSingleton() {
        assertThrows(IllegalArgumentException.class, () -> new FlowNetwork(-1));
        FlowNetwork network = new FlowNetwork(2);
        assertThrows(IndexOutOfBoundsException.class, () -> network.addEdge(0, 2, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> network.addEdge(-1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> network.addEdge(0, 1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> network.maxFlow(0, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> network.maxFlow(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> new FlowNetwork.Edge(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new FlowNetwork.Edge(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new FlowNetwork.Edge(0, 0, -1));
        assertEquals(0, new FlowNetwork(0).vertexCount());
        assertThrows(IndexOutOfBoundsException.class, () -> new FlowNetwork(0).maxFlow(0, 0));
        assertEquals(0, network.maxFlow(0, 1).value());
        assertEquals(List.of(0), network.addEdge(0, 1, 0).maxFlow(0, 1).sourceSide());
        assertEquals(1, network.addEdge(0, 1, 1).maxFlow(0, 1).value());
        assertThrows(UnsupportedOperationException.class,
                () -> network.maxFlow(0, 1).edgeFlows().add(1L));
        assertThrows(UnsupportedOperationException.class, () -> network.edges().clear());
    }

    @Test
    void parallelEdgesAndSelfLoopsAreKept() {
        FlowNetwork network = new FlowNetwork(3).addEdge(0, 1, 2).addEdge(0, 1, 3)
                .addEdge(1, 1, 9).addEdge(1, 2, 4);
        FlowNetwork.MaxFlow flow = network.maxFlow(0, 2);
        assertEquals(4, flow.value());
        assertEquals(0L, flow.edgeFlows().get(2));
        assertEquals(4L, flow.edgeFlows().get(0) + flow.edgeFlows().get(1));
    }

    @Test
    void classicNetwork() {
        FlowNetwork network = new FlowNetwork(6)
                .addEdge(0, 1, 16).addEdge(0, 2, 13).addEdge(1, 2, 10).addEdge(2, 1, 4)
                .addEdge(1, 3, 12).addEdge(3, 2, 9).addEdge(2, 4, 14).addEdge(4, 3, 7)
                .addEdge(3, 5, 20).addEdge(4, 5, 4);
        FlowNetwork.MaxFlow flow = network.maxFlow(0, 5);
        assertEquals(23, flow.value());
        assertEquals(List.of(0, 1, 2, 4), flow.sourceSide());
    }

    @Test
    void largeCapacitiesAreExact() {
        long half = Long.MAX_VALUE / 2;
        FlowNetwork network = new FlowNetwork(4).addEdge(0, 1, half).addEdge(1, 3, half)
                .addEdge(0, 2, half).addEdge(2, 3, half);
        FlowNetwork.MaxFlow flow = network.maxFlow(0, 3);
        assertEquals(2 * half, flow.value());
        assertEquals(List.of(half, half, half, half), flow.edgeFlows());
        FlowNetwork single = new FlowNetwork(2).addEdge(0, 1, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, single.maxFlow(0, 1).value());
    }

    @Test
    void flowBeyondLongThrows() {
        FlowNetwork network = new FlowNetwork(2).addEdge(0, 1, Long.MAX_VALUE)
                .addEdge(0, 1, 1);
        assertThrows(ArithmeticException.class, () -> network.maxFlow(0, 1));
        FlowNetwork phases = new FlowNetwork(3).addEdge(0, 2, Long.MAX_VALUE)
                .addEdge(0, 1, 1).addEdge(1, 2, 1);
        assertThrows(ArithmeticException.class, () -> phases.maxFlow(0, 2));
    }

    @Test
    void deepPathNetwork() {
        int n = 100_000;
        FlowNetwork network = new FlowNetwork(n);
        for (int i = 0; i + 1 < n; i++) {
            network.addEdge(i, i + 1, 5 + i % 3);
        }
        FlowNetwork.MaxFlow flow = network.maxFlow(0, n - 1);
        assertEquals(5, flow.value());
        assertEquals(n - 1, flow.edgeFlows().size());
    }
}
