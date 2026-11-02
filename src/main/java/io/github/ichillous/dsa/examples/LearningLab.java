package io.github.ichillous.dsa.examples;

import io.github.ichillous.dsa.algorithms.SearchAlgorithms;
import io.github.ichillous.dsa.collections.LruCache;
import io.github.ichillous.dsa.graphs.AdvancedGraphAlgorithms;
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import io.github.ichillous.dsa.structures.FenwickTree;
import io.github.ichillous.dsa.structures.SegmentTree;

import java.io.PrintStream;
import java.util.Objects;

/** Small terminal lessons with worked states; no network, GUI, or input files needed. */
public final class LearningLab {
    private LearningLab() {}

    /** Runs the command line. Invalid commands exit with status 2. */
    public static void main(String[] args) {
        int result = run(args, System.out);
        if (result != 0) {
            System.exit(result);
        }
    }

    /**
     * Runs one lesson, writing to the caller's stream. Returns 0 for success or 2 for
     * a usage error. Accepts {@code --help}, {@code learn list}, or {@code learn TOPIC}.
     * The binary-search topic also accepts one int target. Does not close the stream.
     */
    public static int run(String[] args, PrintStream out) {
        Objects.requireNonNull(args, "args");
        Objects.requireNonNull(out, "out");
        for (String argument : args) {
            Objects.requireNonNull(argument, "argument");
        }
        if (args.length == 0 || (args.length == 1 && args[0].equals("--help"))
                || (args.length == 2 && args[0].equals("learn") && args[1].equals("list"))) {
            help(out);
            return 0;
        }
        if (args.length < 2 || !args[0].equals("learn")) {
            return usageError(out, "Choose a lesson with 'learn TOPIC'.");
        }
        String topic = args[1];
        if (topic.equals("binary-search")) {
            if (args.length > 3) {
                return usageError(out, "binary-search accepts at most one int target.");
            }
            int target = 2;
            if (args.length == 3) {
                try {
                    target = Integer.parseInt(args[2]);
                } catch (NumberFormatException exception) {
                    return usageError(out, "Target must fit in a signed Java int.");
                }
            }
            binarySearch(target, out);
            return 0;
        }
        if (args.length != 2) {
            return usageError(out, "This lesson does not accept extra arguments.");
        }
        switch (topic) {
            case "negative-edges" -> negativeEdges(out);
            case "range-queries" -> rangeQueries(out);
            case "lru" -> lru(out);
            default -> { return usageError(out, "Unknown topic: " + topic); }
        }
        return 0;
    }

    private static int usageError(PrintStream out, String message) {
        out.println("Usage error: " + message);
        help(out);
        return 2;
    }

    private static void help(PrintStream out) {
        out.println("Learning lab | predict, trace, explain, then change the example");
        out.println("Usage: java -jar target/java-data-structures-and-algorithms-1.0.0-SNAPSHOT.jar learn TOPIC");
        out.println("  binary-search [target]  Trace lower bound in [1, 2, 2, 2, 5]");
        out.println("  negative-edges         Compare Dijkstra's precondition with Bellman-Ford");
        out.println("  range-queries          Compare Fenwick sums with segment-tree minima");
        out.println("  lru                    Trace recency and eviction at capacity two");
        out.println("Read docs/learning-roadmap.md, then docs/solutions.md for independent checks.");
    }

    private static void binarySearch(int target, PrintStream out) {
        int[] sorted = {1, 2, 2, 2, 5};
        out.println("Predict: where should target " + target + " be inserted in [1, 2, 2, 2, 5]?");
        out.println("Invariant: before low values are < target; at/after high values are >= target.");
        int low = 0;
        int high = sorted.length;
        while (low < high) {
            int middle = low + (high - low) / 2;
            out.println("Inspect [" + low + ", " + high + "): mid=" + middle
                    + ", value=" + sorted[middle]);
            if (sorted[middle] < target) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        int expected = SearchAlgorithms.lowerBound(sorted, target);
        if (low != expected) {
            throw new IllegalStateException("Trace disagrees with maintained search");
        }
        out.println("Lower bound: " + expected + "; first match: "
                + SearchAlgorithms.binarySearch(sorted, target));
        out.println("Why it ends: every iteration strictly shrinks high - low. O(log n) time, O(1) space.");
        out.println("Try: target 0, 4, 6, or an int limit. Explain why duplicates return their first index.");
    }

    private static void negativeEdges(PrintStream out) {
        out.println("Predict shortest 0 -> 2: edges 0->1=4, 0->2=5, 1->2=-7.");
        WeightedGraph graph = WeightedGraph.directed(3)
                .addEdge(0, 1, 4).addEdge(0, 2, 5).addEdge(1, 2, -7);
        try {
            GraphAlgorithms.dijkstra(graph, 0);
            throw new IllegalStateException("Dijkstra should reject a negative edge");
        } catch (IllegalArgumentException expected) {
            out.println("Dijkstra rejects this input: its nonnegative-edge precondition is false.");
        }
        var paths = AdvancedGraphAlgorithms.bellmanFord(graph, 0);
        out.println("Bellman-Ford path: " + paths.pathTo(2) + "; cost: "
                + paths.distanceTo(2).orElseThrow());
        out.println("Invariant: after k complete passes, all shortest paths using at most k edges are covered.");
        out.println("Try: add 2->1=1. The reachable negative cycle makes a finite shortest path undefined.");
        out.println("Bellman-Ford uses O(VE) relaxations; exact integer work also depends on bit length.");
    }

    private static void rangeQueries(PrintStream out) {
        long[] values = {2, -1, 4, 3};
        FenwickTree sums = new FenwickTree(values);
        SegmentTree minima = new SegmentTree(values);
        out.println("Predict sum and minimum over [1, 3) of [2, -1, 4, 3].");
        out.println("Before: sum=" + sums.rangeSum(1, 3) + ", min=" + minima.rangeMin(1, 3).orElseThrow());
        sums.add(1, 5);
        minima.set(1, 4);
        out.println("After adding 5 at index 1: sum=" + sums.rangeSum(1, 3)
                + ", min=" + minima.rangeMin(1, 3).orElseThrow());
        out.println("Fenwick combines disjoint prefix blocks; segment nodes store interval minima.");
        out.println("Both: O(n) build/space, O(log n) updates and queries. Sum overflow is checked.");
        out.println("Try: query [2, 2). Sum is zero; an empty minimum has no value.");
    }

    private static void lru(PrintStream out) {
        LruCache<String, Integer> cache = new LruCache<>(2);
        cache.put("A", 10);
        cache.put("B", 20);
        out.println("After put A, put B: least -> most recent = A, B");
        cache.get("A");
        out.println("After get A: B, A. A cache hit changes recency.");
        cache.put("C", 30);
        out.println("After put C: B evicted; B=" + cache.get("B") + ", size=" + cache.size());
        out.println("Invariant: each map entry has one linked node; the ends identify eviction and promotion.");
        out.println("Expected amortized O(1) get/put, O(capacity) bounded storage. Not thread safe.");
        out.println("Try: replace A's value before inserting C. Explain which key is evicted.");
    }
}
