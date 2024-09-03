---

# 🧩 Common Greedy Algorithm Problems in Java

![Java Greedy Algorithm Problems](https://img.shields.io/badge/Java-Greedy_Problems-yellow?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Activity Selection Problem](#-activity-selection-problem)
- [Fractional Knapsack Problem](#-fractional-knapsack-problem)
- [Huffman Coding](#-huffman-coding)
- [Prim's Algorithm](#-prims-algorithm)
- [Kruskal's Algorithm](#-kruskals-algorithm)
- [Job Sequencing Problem](#-job-sequencing-problem)
- [Minimum Coins Problem](#-minimum-coins-problem)
- [Dijkstra's Algorithm](#-dijkstras-algorithm)

## 🕒 Activity Selection Problem

### Problem Statement:
Given a set of activities with start and finish times, select the maximum number of activities that can be performed without overlapping.

### Greedy Approach:
Sort the activities by their finish times. Select the activity that finishes first, then choose the next activity that starts after the previous one finishes.

#### Implementation:
```java
import java.util.Arrays;
import java.util.Comparator;

public class ActivitySelection {
    public static void activitySelection(int[] start, int[] finish) {
        int n = finish.length;
        
        Activity[] activities = new Activity[n];
        for (int i = 0; i < n; i++) {
            activities[i] = new Activity(start[i], finish[i]);
        }
        
        Arrays.sort(activities, Comparator.comparingInt(a -> a.finish));
        
        System.out.println("Selected activities:");
        int lastFinishTime = 0;
        for (Activity activity : activities) {
            if (activity.start >= lastFinishTime) {
                System.out.println("Activity with start time: " + activity.start + ", finish time: " + activity.finish);
                lastFinishTime = activity.finish;
            }
        }
    }

    public static void main(String[] args) {
        int[] start = {1, 3, 0, 5, 8, 5};
        int[] finish = {2, 4, 6, 7, 9, 9};
        activitySelection(start, finish);
    }
}

class Activity {
    int start, finish;
    
    Activity(int start, int finish) {
        this.start = start;
        this.finish = finish;
    }
}
```

## 🎒 Fractional Knapsack Problem

### Problem Statement:
Given a set of items, each with a weight and a value, determine the maximum value that can be obtained by filling a knapsack of a given capacity. Items can be divided into fractions.

### Greedy Approach:
Calculate the value-to-weight ratio for each item and sort the items by this ratio in descending order. Add items to the knapsack starting with the highest ratio until the knapsack is full.

#### Implementation:
```java
import java.util.Arrays;
import java.util.Comparator;

public class FractionalKnapsack {
    public double getMaxValue(int[] weights, int[] values, int capacity) {
        Item[] items = new Item[weights.length];
        for (int i = 0; i < weights.length; i++) {
            items[i] = new Item(weights[i], values[i]);
        }

        Arrays.sort(items, Comparator.comparingDouble(Item::valuePerWeight).reversed());

        double totalValue = 0;
        for (Item item : items) {
            if (capacity - item.weight >= 0) {
                capacity -= item.weight;
                totalValue += item.value;
            } else {
                totalValue += item.valuePerWeight() * capacity;
                break;
            }
        }

        return totalValue;
    }

    public static void main(String[] args) {
        FractionalKnapsack knapsack = new FractionalKnapsack();
        int[] weights = {10, 20, 30};
        int[] values = {60, 100, 120};
        int capacity = 50;

        double maxValue = knapsack.getMaxValue(weights, values, capacity);
        System.out.println("Maximum value in knapsack = " + maxValue);
    }
}

class Item {
    int weight;
    int value;

    Item(int weight, int value) {
        this.weight = weight;
        this.value = value;
    }

    double valuePerWeight() {
        return (double) value / weight;
    }
}
```

## 🗜️ Huffman Coding

### Problem Statement:
Given a set of characters and their frequencies, build a binary tree such that the weighted path length is minimized. This results in a prefix code used for data compression.

### Greedy Approach:
Use a priority queue to build the tree. At each step, extract the two nodes with the lowest frequencies and merge them into a new node.

#### Implementation:
```java
import java.util.PriorityQueue;

public class HuffmanCoding {
    public void huffman(char[] charArray, int[] charFreq) {
        int n = charArray.length;
        PriorityQueue<Node> queue = new PriorityQueue<>(n, Comparator.comparingInt(a -> a.frequency));

        for (int i = 0; i < n; i++) {
            queue.add(new Node(charArray[i], charFreq[i]));
        }

        while (queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();
            Node newNode = new Node('-', left.frequency + right.frequency);
            newNode.left = left;
            newNode.right = right;
            queue.add(newNode);
        }

        printCodes(queue.poll(), "");
    }

    private void printCodes(Node root, String code) {
        if (root.left == null && root.right == null) {
            System.out.println(root.character + ": " + code);
            return;
        }

        printCodes(root.left, code + "0");
        printCodes(root.right, code + "1");
    }

    public static void main(String[] args) {
        char[] charArray = {'a', 'b', 'c', 'd', 'e', 'f'};
        int[] charFreq = {5, 9, 12, 13, 16, 45};

        HuffmanCoding huffman = new HuffmanCoding();
        huffman.huffman(charArray, charFreq);
    }
}

class Node {
    char character;
    int frequency;
    Node left, right;

    Node(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }
}
```

## 🌲 Prim's Algorithm

### Problem Statement:
Given a connected, undirected graph, find a minimum spanning tree that connects all the vertices with the minimum total edge weight.

### Greedy Approach:
Start with an arbitrary vertex and grow the minimum spanning tree by adding the smallest edge that connects a vertex in the tree to a vertex outside the tree.

#### Implementation:
```java
import java.util.*;

public class PrimsAlgorithm {
    public void primMST(int[][] graph) {
        int n = graph.length;
        int[] parent = new int[n];
        int[] key = new int[n];
        boolean[] mstSet = new boolean[n];

        Arrays.fill(key, Integer.MAX_VALUE);
        key[0] = 0;
        parent[0] = -1;

        for (int count = 0; count < n - 1; count++) {
            int u = minKey(key, mstSet);
            mstSet[u] = true;

            for (int v = 0; v < n; v++) {
                if (graph[u][v] != 0 && !mstSet[v] && graph[u][v] < key[v]) {
                    parent[v] = u;
                    key[v] = graph[u][v];
                }
            }
        }

        printMST(parent, graph);
    }

    private int minKey(int[] key, boolean[] mstSet) {
        int min = Integer.MAX_VALUE, minIndex = -1;

        for (int v = 0; v < key.length; v++) {
            if (!mstSet[v] && key[v] < min) {
                min = key[v];
                minIndex = v;
            }
        }

        return minIndex;
    }

    private void printMST(int[] parent, int[][] graph) {
        System.out.println("Edge \tWeight");
        for (int i = 1; i < graph.length; i++) {
            System.out.println(parent[i] + " - " + i + "\t" + graph[i][parent[i]]);
        }
    }

    public static void main(String[] args) {
        PrimsAlgorithm prim = new PrimsAlgorithm();
        int[][] graph = {
            {0, 2, 0, 6, 0},
            {2, 0, 3, 8, 5},
            {0, 3, 0, 0, 7},
            {6, 8, 0, 0, 9},
            {0, 5, 7, 9, 0}
        };
        prim.primMST(graph);
    }
}
```

## 🛠️ Kruskal's Algorithm

### Problem Statement:
Given a connected, undirected graph, find a minimum spanning tree that connects all vertices with the minimum total edge weight.

### Greedy Approach:
Sort all the edges by weight, then add the smallest edge to the growing spanning tree, ensuring no cycles are formed.

#### Implementation:
```java
import java.util.Arrays;

public class KruskalsAlgorithm {
    class Edge implements Comparable<Edge> {
       

 int src, dest, weight;

        public int compareTo(Edge compareEdge) {
            return this.weight - compareEdge.weight;
        }
    }

    class Subset {
        int parent, rank;
    }

    int V, E;
    Edge[] edges;

    KruskalsAlgorithm(int v, int e) {
        V = v;
        E = e;
        edges = new Edge[E];
        for (int i = 0; i < e; ++i) {
            edges[i] = new Edge();
        }
    }

    int find(Subset[] subsets, int i) {
        if (subsets[i].parent != i) {
            subsets[i].parent = find(subsets, subsets[i].parent);
        }
        return subsets[i].parent;
    }

    void union(Subset[] subsets, int x, int y) {
        int xroot = find(subsets, x);
        int yroot = find(subsets, y);

        if (subsets[xroot].rank < subsets[yroot].rank) {
            subsets[xroot].parent = yroot;
        } else if (subsets[xroot].rank > subsets[yroot].rank) {
            subsets[yroot].parent = xroot;
        } else {
            subsets[yroot].parent = xroot;
            subsets[xroot].rank++;
        }
    }

    void kruskalMST() {
        Edge[] result = new Edge[V];
        int e = 0;
        int i = 0;
        for (i = 0; i < V; ++i) {
            result[i] = new Edge();
        }

        Arrays.sort(edges);

        Subset[] subsets = new Subset[V];
        for (i = 0; i < V; ++i) {
            subsets[i] = new Subset();
        }

        for (int v = 0; v < V; ++v) {
            subsets[v].parent = v;
            subsets[v].rank = 0;
        }

        i = 0;
        while (e < V - 1) {
            Edge nextEdge = edges[i++];

            int x = find(subsets, nextEdge.src);
            int y = find(subsets, nextEdge.dest);

            if (x != y) {
                result[e++] = nextEdge;
                union(subsets, x, y);
            }
        }

        System.out.println("Following are the edges in the constructed MST");
        for (i = 0; i < e; ++i) {
            System.out.println(result[i].src + " -- " + result[i].dest + " == " + result[i].weight);
        }
    }

    public static void main(String[] args) {
        int V = 4;
        int E = 5;
        KruskalsAlgorithm graph = new KruskalsAlgorithm(V, E);

        graph.edges[0].src = 0;
        graph.edges[0].dest = 1;
        graph.edges[0].weight = 10;

        graph.edges[1].src = 0;
        graph.edges[1].dest = 2;
        graph.edges[1].weight = 6;

        graph.edges[2].src = 0;
        graph.edges[2].dest = 3;
        graph.edges[2].weight = 5;

        graph.edges[3].src = 1;
        graph.edges[3].dest = 3;
        graph.edges[3].weight = 15;

        graph.edges[4].src = 2;
        graph.edges[4].dest = 3;
        graph.edges[4].weight = 4;

        graph.kruskalMST();
    }
}
```

## 📅 Job Sequencing Problem

### Problem Statement:
Given a set of jobs where each job has a deadline and profit, schedule the jobs to maximize total profit while ensuring no jobs overlap their deadlines.

### Greedy Approach:
Sort jobs by profit in descending order and schedule them one by one, starting with the highest profit. Assign each job to the latest available slot before its deadline.

#### Implementation:
```java
import java.util.Arrays;
import java.util.Comparator;

public class JobSequencing {
    static class Job {
        char id;
        int deadline;
        int profit;

        Job(char id, int deadline, int profit) {
            this.id = id;
            this.deadline = deadline;
            this.profit = profit;
        }
    }

    public void jobSequencing(Job[] jobs, int maxDeadline) {
        Arrays.sort(jobs, Comparator.comparingInt(j -> -j.profit));

        char[] schedule = new char[maxDeadline];
        boolean[] slot = new boolean[maxDeadline];

        int totalProfit = 0;

        for (Job job : jobs) {
            for (int j = Math.min(maxDeadline, job.deadline) - 1; j >= 0; j--) {
                if (!slot[j]) {
                    slot[j] = true;
                    schedule[j] = job.id;
                    totalProfit += job.profit;
                    break;
                }
            }
        }

        System.out.println("Scheduled jobs: " + Arrays.toString(schedule));
        System.out.println("Total profit: " + totalProfit);
    }

    public static void main(String[] args) {
        JobSequencing js = new JobSequencing();
        Job[] jobs = {
            new Job('a', 2, 100),
            new Job('b', 1, 19),
            new Job('c', 2, 27),
            new Job('d', 1, 25),
            new Job('e', 3, 15)
        };
        int maxDeadline = 3;
        js.jobSequencing(jobs, maxDeadline);
    }
}
```

## 💰 Minimum Coins Problem

### Problem Statement:
Given a set of coin denominations and a target amount, find the minimum number of coins required to make the target amount.

### Greedy Approach:
Sort the coins in descending order and start picking the largest coin until the target amount is reached or exceeded.

#### Implementation:
```java
import java.util.Arrays;

public class MinimumCoins {
    public int minCoins(int[] coins, int amount) {
        Arrays.sort(coins);
        int count = 0;
        for (int i = coins.length - 1; i >= 0; i--) {
            while (amount >= coins[i]) {
                amount -= coins[i];
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        MinimumCoins mc = new MinimumCoins();
        int[] coins = {1, 2, 5, 10};
        int amount = 27;
        System.out.println("Minimum coins required: " + mc.minCoins(coins, amount));
    }
}
```

## 🛤️ Dijkstra's Algorithm

### Problem Statement:
Given a graph with non-negative weights, find the shortest path from a source vertex to all other vertices in the graph.

### Greedy Approach:
Start with the source vertex, and repeatedly select the unvisited vertex with the smallest known distance, updating the distances to its neighbors.

#### Implementation:
```java
import java.util.*;

public class DijkstrasAlgorithm {
    public void dijkstra(int[][] graph, int src) {
        int n = graph.length;
        int[] dist = new int[n];
        boolean[] visited = new boolean[n];

        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;

        for (int i = 0; i < n - 1; i++) {
            int u = minDistance(dist, visited);
            visited[u] = true;

            for (int v = 0; v < n; v++) {
                if (!visited[v] && graph[u][v] != 0 && dist[u] != Integer.MAX_VALUE && dist[u] + graph[u][v] < dist[v]) {
                    dist[v] = dist[u] + graph[u][v];
                }
            }
        }

        printSolution(dist);
    }

    private int minDistance(int[] dist, boolean[] visited) {
        int min = Integer.MAX_VALUE, minIndex = -1;
        for (int v = 0; v < dist.length; v++) {
            if (!visited[v] && dist[v] < min) {
                min = dist[v];
                minIndex = v;
            }
        }
        return minIndex;
    }

    private void printSolution(int[] dist) {
        System.out.println("Vertex \tDistance from Source");
        for (int i = 0; i < dist.length; i++) {
            System.out.println(i + " \t\t" + dist[i]);
        }
    }

    public static void main(String[] args) {
        DijkstrasAlgorithm dijkstra = new DijkstrasAlgorithm();
        int[][] graph = {
            {0, 10, 0, 0, 0, 0},
            {10, 0, 5, 0, 0, 0},
            {0, 5, 0, 20, 1, 0},
            {0, 0, 20, 0, 2, 5},
            {0, 0, 1, 2, 0, 3},
            {0, 0, 0, 5, 3, 0}
        };
        dijkstra.dijkstra(graph, 0);
    }
}
```

## 🎓 Conclusion

The problems outlined in this section demonstrate the versatility and efficiency of Greedy Algorithms in solving complex optimization problems. By mastering these problems, you'll be well-equipped to apply greedy strategies to a wide range of challenges in your Java development journey.

Key takeaways:
- **Activity Selection Problem**

: Shows how greedy strategies can optimize scheduling tasks.
- **Fractional Knapsack Problem**: Demonstrates the application of greedy algorithms to maximize value.
- **Huffman Coding**: A classic example of greedy algorithms in data compression.
- **Graph Algorithms**: Prim's, Kruskal's, and Dijkstra's algorithms showcase the power of greedy approaches in graph theory.

By practicing these problems, you'll strengthen your understanding of Greedy Algorithms and enhance your problem-solving skills in Java! 💻🚀

---
