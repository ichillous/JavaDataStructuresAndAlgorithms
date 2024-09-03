
---

# 🖼️ Graph Representation in Java

![Java Graph Representation](https://img.shields.io/badge/Java-Graph_Representation-green?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Introduction to Graph Representation](#-introduction-to-graph-representation)
- [Adjacency Matrix](#-adjacency-matrix)
  - [Advantages and Disadvantages](#-advantages-and-disadvantages)
  - [Example Implementation](#-example-implementation)
- [Adjacency List](#-adjacency-list)
  - [Advantages and Disadvantages](#-advantages-and-disadvantages)
  - [Example Implementation](#-example-implementation)
- [Incidence Matrix](#-incidence-matrix)
  - [Advantages and Disadvantages](#-advantages-and-disadvantages)
  - [Example Implementation](#-example-implementation)
- [Choosing the Right Representation](#-choosing-the-right-representation)
- [Conclusion](#-conclusion)

## 🌟 Introduction to Graph Representation

Graph representation is a fundamental concept in graph theory that defines how a graph's vertices and edges are stored in a computer's memory. The choice of representation affects the efficiency of graph algorithms and operations, such as traversal, insertion, deletion, and search. The most common graph representations are **Adjacency Matrix**, **Adjacency List**, and **Incidence Matrix**.

## 🗺️ Adjacency Matrix

An **Adjacency Matrix** is a 2D array (matrix) used to represent a graph. The rows and columns of the matrix represent vertices, and the value at position `(i, j)` indicates whether there is an edge between vertex `i` and vertex `j`. For weighted graphs, the matrix may store the weight of the edge instead of a simple boolean value.

### Advantages and Disadvantages

#### Advantages:
- **Simple Implementation**: Easy to implement and understand.
- **Quick Edge Lookup**: Edge existence checks are performed in O(1) time.
- **Efficient for Dense Graphs**: Ideal for graphs where the number of edges is close to the square of the number of vertices (dense graphs).

#### Disadvantages:
- **Space Inefficiency**: Requires O(V^2) space, where `V` is the number of vertices. This can be inefficient for sparse graphs.
- **Inefficient for Large Sparse Graphs**: Storing a large graph with few edges wastes significant memory.

### Example Implementation

#### Unweighted Graph:
```java
class Graph {
    private boolean[][] adjacencyMatrix;
    private int vertices;

    public Graph(int vertices) {
        this.vertices = vertices;
        adjacencyMatrix = new boolean[vertices][vertices];
    }

    public void addEdge(int source, int destination) {
        adjacencyMatrix[source][destination] = true;
        adjacencyMatrix[destination][source] = true; // For undirected graph
    }

    public void removeEdge(int source, int destination) {
        adjacencyMatrix[source][destination] = false;
        adjacencyMatrix[destination][source] = false; // For undirected graph
    }

    public boolean hasEdge(int source, int destination) {
        return adjacencyMatrix[source][destination];
    }

    public void printGraph() {
        for (int i = 0; i < vertices; i++) {
            for (int j = 0; j < vertices; j++) {
                System.out.print(adjacencyMatrix[i][j] ? "1 " : "0 ");
            }
            System.out.println();
        }
    }
}
```

## 📋 Adjacency List

An **Adjacency List** is a collection of lists or arrays where each list corresponds to a vertex and contains the vertices that are adjacent to it. This representation is efficient in terms of space, especially for sparse graphs, and is commonly used in practice.

### Advantages and Disadvantages

#### Advantages:
- **Space Efficiency**: Requires O(V + E) space, where `V` is the number of vertices and `E` is the number of edges, making it ideal for sparse graphs.
- **Efficient Iteration**: Easier to iterate over all edges of a vertex, which can improve the performance of certain graph algorithms.

#### Disadvantages:
- **Slower Edge Lookup**: Checking for the existence of an edge is slower, requiring O(V) time in the worst case.
- **More Complex Implementation**: Slightly more complex to implement compared to an adjacency matrix.

### Example Implementation

#### Unweighted Graph:
```java
import java.util.LinkedList;

class Graph {
    private LinkedList<Integer>[] adjacencyList;
    private int vertices;

    public Graph(int vertices) {
        this.vertices = vertices;
        adjacencyList = new LinkedList[vertices];
        for (int i = 0; i < vertices; i++) {
            adjacencyList[i] = new LinkedList<>();
        }
    }

    public void addEdge(int source, int destination) {
        adjacencyList[source].add(destination);
        adjacencyList[destination].add(source); // For undirected graph
    }

    public void removeEdge(int source, int destination) {
        adjacencyList[source].remove(Integer.valueOf(destination));
        adjacencyList[destination].remove(Integer.valueOf(source)); // For undirected graph
    }

    public boolean hasEdge(int source, int destination) {
        return adjacencyList[source].contains(destination);
    }

    public void printGraph() {
        for (int i = 0; i < vertices; i++) {
            System.out.print("Vertex " + i + ":");
            for (int adj : adjacencyList[i]) {
                System.out.print(" -> " + adj);
            }
            System.out.println();
        }
    }
}
```

## 📊 Incidence Matrix

An **Incidence Matrix** is a 2D matrix that represents the relationship between vertices and edges. The rows of the matrix represent the vertices, and the columns represent the edges. The value at position `(i, j)` indicates whether vertex `i` is incident to edge `j`.

### Advantages and Disadvantages

#### Advantages:
- **Useful for Specific Algorithms**: Particularly useful in algorithms that require easy access to the incidence relationship between vertices and edges.
- **Handles Multigraphs**: Can represent multigraphs (graphs with multiple edges between the same vertices) more naturally than other representations.

#### Disadvantages:
- **Space Inefficiency**: Requires O(V * E) space, which can be inefficient for large graphs.
- **Complex Implementation**: More complex to implement and understand compared to adjacency lists and matrices.

### Example Implementation

#### Unweighted Graph:
```java
class Graph {
    private int[][] incidenceMatrix;
    private int vertices;
    private int edges;
    private int edgeCount;

    public Graph(int vertices, int edges) {
        this.vertices = vertices;
        this.edges = edges;
        incidenceMatrix = new int[vertices][edges];
        edgeCount = 0;
    }

    public void addEdge(int source, int destination) {
        if (edgeCount < edges) {
            incidenceMatrix[source][edgeCount] = 1;
            incidenceMatrix[destination][edgeCount] = 1;
            edgeCount++;
        } else {
            System.out.println("Max edges reached. Cannot add more edges.");
        }
    }

    public void printGraph() {
        for (int i = 0; i < vertices; i++) {
            for (int j = 0; j < edges; j++) {
                System.out.print(incidenceMatrix[i][j] + " ");
            }
            System.out.println();
        }
    }
}
```

## 🤔 Choosing the Right Representation

The choice of graph representation depends on several factors:
- **Graph Density**: Use adjacency lists for sparse graphs and adjacency matrices for dense graphs.
- **Space Efficiency**: Adjacency lists are more space-efficient for large graphs with fewer edges.
- **Algorithm Requirements**: Certain algorithms may benefit from a specific representation, such as using incidence matrices for edge-centric operations.
- **Ease of Implementation**: Adjacency matrices are simpler to implement, but adjacency lists are more practical for most applications.

### General Guidelines:
- **Sparse Graphs**: Adjacency List
- **Dense Graphs**: Adjacency Matrix
- **Multigraphs or Edge-Centric Operations**: Incidence Matrix

## 🎓 Conclusion

Graph representation is a critical decision in graph theory, influencing the efficiency of various graph algorithms and operations. Understanding the strengths and weaknesses of each representation allows you to choose the most appropriate one for your specific use case.

Key takeaways:
- **Adjacency Matrix**: Simple and efficient for dense graphs but requires more space.
- **Adjacency List**: Space-efficient for sparse graphs and commonly used in practice.
- **Incidence Matrix**: Useful for specific edge-centric algorithms but more complex and space-intensive.

By mastering graph representations, you'll be better equipped to implement and optimize graph-based algorithms in your Java applications! 💻🚀

---
