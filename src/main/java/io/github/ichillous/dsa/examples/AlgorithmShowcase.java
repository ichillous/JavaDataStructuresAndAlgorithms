package io.github.ichillous.dsa.examples;

import io.github.ichillous.dsa.algorithms.DynamicProgramming;
import io.github.ichillous.dsa.algorithms.SearchAlgorithms;
import io.github.ichillous.dsa.algorithms.SortAlgorithms;
import io.github.ichillous.dsa.algorithms.StringAlgorithms;
import io.github.ichillous.dsa.collections.ChainedHashMap;
import io.github.ichillous.dsa.graphs.Graph;
import io.github.ichillous.dsa.graphs.GraphAlgorithms;
import io.github.ichillous.dsa.graphs.WeightedGraph;
import io.github.ichillous.dsa.structures.AvlTree;
import io.github.ichillous.dsa.structures.FenwickTree;
import io.github.ichillous.dsa.structures.Trie;

import java.util.Arrays;
import java.util.List;

/** Small deterministic examples; run the packaged JAR after {@code ./mvnw verify}. */
public final class AlgorithmShowcase {
    private AlgorithmShowcase() {}

    /** Runs independent examples with no input files, network access, or runtime dependencies. */
    public static void main(String[] args) {
        System.out.println("Java Data Structures and Algorithms | runnable examples");

        int[] scores = {42, 7, 19, 7, 88};
        SortAlgorithms.mergeSort(scores);
        System.out.println("Sorted scores: " + Arrays.toString(scores));
        System.out.println("First score >= 19: index " + SearchAlgorithms.lowerBound(scores, 19));

        ChainedHashMap<String, Integer> frequency = new ChainedHashMap<>();
        for (String word : List.of("learn", "test", "learn", "build")) {
            Integer count = frequency.get(word);
            frequency.put(word, count == null ? 1 : count + 1);
        }
        System.out.println("Word frequency: learn=" + frequency.get("learn")
                + ", distinct=" + frequency.size());

        Trie commands = new Trie();
        for (String command : List.of("commit", "compare", "compile", "test")) {
            commands.add(command);
        }
        System.out.println("Autocomplete 'com': " + commands.wordsWithPrefix("com", 3));

        AvlTree<Integer> leaderboard = new AvlTree<>();
        for (int score : scores) {
            leaderboard.add(score);
        }
        System.out.println("Distinct ordered scores: " + leaderboard.inOrder());

        // Vertices: 0=home, 1=cafe, 2=park, 3=library, 4=unreachable station.
        WeightedGraph roads = WeightedGraph.undirected(5)
                .addEdge(0, 1, 8).addEdge(0, 2, 2).addEdge(2, 1, 1)
                .addEdge(1, 3, 3).addEdge(2, 3, 10);
        GraphAlgorithms.ShortestPaths routes = GraphAlgorithms.dijkstra(roads, 0);
        System.out.println("Shortest route home -> library: " + routes.pathTo(3)
                + ", cost=" + routes.distanceTo(3).orElseThrow());
        System.out.println("Station reachable: " + routes.distanceTo(4).isPresent());

        // Dependencies: read(0) -> implement(1) -> test(2) -> explain(3).
        Graph dependencies = Graph.directed(4).addEdge(0, 1).addEdge(1, 2).addEdge(2, 3);
        System.out.println("Study task order: " + GraphAlgorithms.topologicalSort(dependencies));

        FenwickTree dailyMinutes = new FenwickTree(new long[]{20, 35, 15, 40, 30});
        dailyMinutes.add(2, 10);
        System.out.println("Study minutes on days [1,4): " + dailyMinutes.rangeSum(1, 4));
        System.out.println("Minimum coins for 6 using [1,3,4]: "
                + DynamicProgramming.minimumCoins(new int[]{1, 3, 4}, 6));
        System.out.println("Overlapping 'ana' matches in 'banana': "
                + StringAlgorithms.kmpFindAll("banana", "ana"));
    }
}
