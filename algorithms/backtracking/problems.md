---

# 🧩 Common Backtracking Problems in Java

![Java Backtracking Problems](https://img.shields.io/badge/Java-Backtracking_Problems-purple?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [N-Queens Problem](#-n-queens-problem)
- [Sudoku Solver](#-sudoku-solver)
- [Subset Sum Problem](#-subset-sum-problem)
- [Permutations](#-permutations)
- [Knight's Tour Problem](#-knights-tour-problem)
- [Rat in a Maze](#-rat-in-a-maze)
- [Word Search](#-word-search)
- [Hamiltonian Path](#-hamiltonian-path)

## 👑 N-Queens Problem

### Problem Statement:
Place `N` queens on an `N x N` chessboard such that no two queens threaten each other. This means that no two queens can be in the same row, column, or diagonal.

### Backtracking Approach:
Use backtracking to place queens one by one in different columns. If placing a queen in a column leads to a conflict, backtrack and try the next possible position.

#### Implementation:
```java
public class NQueens {
    private void solveNQueens(int[][] board, int row) {
        int N = board.length;
        if (row == N) {
            printBoard(board);
            return;
        }
        for (int col = 0; col < N; col++) {
            if (isSafe(board, row, col)) {
                board[row][col] = 1;
                solveNQueens(board, row + 1);
                board[row][col] = 0;
            }
        }
    }

    private boolean isSafe(int[][] board, int row, int col) {
        int N = board.length;
        for (int i = 0; i < row; i++) {
            if (board[i][col] == 1 || (col - row + i >= 0 && board[i][col - row + i] == 1) || 
                (col + row - i < N && board[i][col + row - i] == 1)) {
                return false;
            }
        }
        return true;
    }

    private void printBoard(int[][] board) {
        for (int[] row : board) {
            for (int cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int N = 4; // Change this to solve for different sizes
        int[][] board = new int[N][N];
        NQueens solver = new NQueens();
        solver.solveNQueens(board, 0);
    }
}
```

## 🧩 Sudoku Solver

### Problem Statement:
Given a partially filled `9 x 9` Sudoku grid, the goal is to complete the grid so that each row, column, and `3 x 3` subgrid contains the digits `1` to `9` without repetition.

### Backtracking Approach:
Fill empty cells one by one, checking whether placing a digit in a particular cell is valid. If placing a digit leads to a conflict, backtrack and try the next digit.

#### Implementation:
```java
public class SudokuSolver {
    public boolean solveSudoku(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isValid(board, row, col, num)) {
                            board[row][col] = num;
                            if (solveSudoku(board)) {
                                return true;
                            }
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == num || board[i][col] == num || 
                board[row - row % 3 + i / 3][col - col % 3 + i % 3] == num) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        int[][] board = {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };
        SudokuSolver solver = new SudokuSolver();
        if (solver.solveSudoku(board)) {
            for (int[] row : board) {
                for (int num : row) {
                    System.out.print(num + " ");
                }
                System.out.println();
            }
        } else {
            System.out.println("No solution exists.");
        }
    }
}
```

## 🛠️ Subset Sum Problem

### Problem Statement:
Given a set of integers and a target sum, determine if there is a subset whose sum is equal to the target.

### Backtracking Approach:
Explore all subsets by including or excluding each element. If a subset's sum matches the target, the problem is solved.

#### Implementation:
```java
public class SubsetSum {
    public boolean isSubsetSum(int[] set, int target) {
        return isSubsetSum(set, target, 0);
    }

    private boolean isSubsetSum(int[] set, int target, int index) {
        if (target == 0) return true;
        if (index == set.length || target < 0) return false;
        return isSubsetSum(set, target - set[index], index + 1) || isSubsetSum(set, target, index + 1);
    }

    public static void main(String[] args) {
        SubsetSum subsetSum = new SubsetSum();
        int[] set = {3, 34, 4, 12, 5, 2};
        int target = 9;
        System.out.println("Subset with given sum exists: " + subsetSum.isSubsetSum(set, target));
    }
}
```

## 🔄 Permutations

### Problem Statement:
Generate all permutations of a given set of elements.

### Backtracking Approach:
Recursively generate permutations by swapping elements and exploring all possible configurations.

#### Implementation:
```java
import java.util.ArrayList;
import java.util.List;

public class Permutations {
    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(nums, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int[] nums, List<Integer> current, List<List<Integer>> result) {
        if (current.size() == nums.length) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int num : nums) {
            if (!current.contains(num)) {
                current.add(num);
                backtrack(nums, current, result);
                current.remove(current.size() - 1);
            }
        }
    }

    public static void main(String[] args) {
        Permutations permutations = new Permutations();
        int[] nums = {1, 2, 3};
        List<List<Integer>> result = permutations.permute(nums);
        for (List<Integer> perm : result) {
            System.out.println(perm);
        }
    }
}
```

## 🏇 Knight's Tour Problem

### Problem Statement:
Find a sequence of moves for a knight on a chessboard such that the knight visits every square exactly once.

### Backtracking Approach:
Place the knight on the board and attempt to move to each possible square. If a move leads to a dead end, backtrack and try a different move.

#### Implementation:
```java
public class KnightsTour {
    private static final int[] xMove = {-2, -1, 1, 2, 2, 1, -1, -2};
    private static final int[] yMove = {1, 2, 2, 1, -1, -2, -2, -1};
    private static final int N = 8;

    public boolean solveKnightsTour() {
        int[][] board = new int[N][N];
        for (int[] row : board) {
            Arrays.fill(row, -1);
        }
        board[0][

0] = 0;
        if (!solveKnightsTourUtil(0, 0, 1, board)) {
            System.out.println("Solution does not exist");
            return false;
        }
        printBoard(board);
        return true;
    }

    private boolean solveKnightsTourUtil(int x, int y, int moveCount, int[][] board) {
        if (moveCount == N * N) return true;
        for (int k = 0; k < 8; k++) {
            int nextX = x + xMove[k];
            int nextY = y + yMove[k];
            if (isSafe(nextX, nextY, board)) {
                board[nextX][nextY] = moveCount;
                if (solveKnightsTourUtil(nextX, nextY, moveCount + 1, board)) {
                    return true;
                }
                board[nextX][nextY] = -1;
            }
        }
        return false;
    }

    private boolean isSafe(int x, int y, int[][] board) {
        return x >= 0 && y >= 0 && x < N && y < N && board[x][y] == -1;
    }

    private void printBoard(int[][] board) {
        for (int[] row : board) {
            for (int cell : row) {
                System.out.print(cell + "\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        KnightsTour knight = new KnightsTour();
        knight.solveKnightsTour();
    }
}
```

## 🐭 Rat in a Maze

### Problem Statement:
Given a maze represented by a grid, find a path from the top-left corner to the bottom-right corner, where the rat can only move in certain directions.

### Backtracking Approach:
Move the rat step by step in the allowed directions. If a move leads to a dead end, backtrack and try a different direction.

#### Implementation:
```java
public class RatInMaze {
    private static final int N = 4;

    public boolean solveMaze(int[][] maze) {
        int[][] solution = new int[N][N];
        if (!solveMazeUtil(maze, 0, 0, solution)) {
            System.out.println("Solution doesn't exist");
            return false;
        }
        printSolution(solution);
        return true;
    }

    private boolean solveMazeUtil(int[][] maze, int x, int y, int[][] solution) {
        if (x == N - 1 && y == N - 1 && maze[x][y] == 1) {
            solution[x][y] = 1;
            return true;
        }
        if (isSafe(maze, x, y)) {
            solution[x][y] = 1;
            if (solveMazeUtil(maze, x + 1, y, solution)) {
                return true;
            }
            if (solveMazeUtil(maze, x, y + 1, solution)) {
                return true;
            }
            solution[x][y] = 0;
            return false;
        }
        return false;
    }

    private boolean isSafe(int[][] maze, int x, int y) {
        return x >= 0 && y >= 0 && x < N && y < N && maze[x][y] == 1;
    }

    private void printSolution(int[][] solution) {
        for (int[] row : solution) {
            for (int cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        RatInMaze rat = new RatInMaze();
        int[][] maze = {
            {1, 0, 0, 0},
            {1, 1, 0, 1},
            {0, 1, 0, 0},
            {1, 1, 1, 1}
        };
        rat.solveMaze(maze);
    }
}
```

## 🔍 Word Search

### Problem Statement:
Given a grid of letters and a word, determine if the word exists in the grid. The word can be constructed from letters of sequentially adjacent cells, where "adjacent" cells are those horizontally or vertically neighboring.

### Backtracking Approach:
Starting from each cell, explore all possible directions to see if the word can be formed. If a path doesn't work, backtrack and try a different path.

#### Implementation:
```java
public class WordSearch {
    private static final int[] xMove = {-1, 1, 0, 0};
    private static final int[] yMove = {0, 0, -1, 1};

    public boolean exist(char[][] board, String word) {
        int m = board.length, n = board[0].length;
        boolean[][] visited = new boolean[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] == word.charAt(0) && search(board, word, i, j, 0, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean search(char[][] board, String word, int x, int y, int index, boolean[][] visited) {
        if (index == word.length()) return true;
        if (x < 0 || y < 0 || x >= board.length || y >= board[0].length || visited[x][y] || board[x][y] != word.charAt(index)) {
            return false;
        }
        visited[x][y] = true;
        for (int k = 0; k < 4; k++) {
            if (search(board, word, x + xMove[k], y + yMove[k], index + 1, visited)) {
                return true;
            }
        }
        visited[x][y] = false;
        return false;
    }

    public static void main(String[] args) {
        WordSearch wordSearch = new WordSearch();
        char[][] board = {
            {'A', 'B', 'C', 'E'},
            {'S', 'F', 'C', 'S'},
            {'A', 'D', 'E', 'E'}
        };
        String word = "ABCCED";
        System.out.println("Word exists in grid: " + wordSearch.exist(board, word));
    }
}
```

## 🚶 Hamiltonian Path

### Problem Statement:
Given a graph, determine whether there is a path that visits every vertex exactly once.

### Backtracking Approach:
Start from each vertex and try to find a path that visits every vertex exactly once. If a path doesn't work, backtrack and try another.

#### Implementation:
```java
public class HamiltonianPath {
    private static final int V = 5;

    public boolean hamiltonianPath(int[][] graph) {
        int[] path = new int[V];
        Arrays.fill(path, -1);
        path[0] = 0;
        if (!hamiltonianPathUtil(graph, path, 1)) {
            System.out.println("Solution does not exist");
            return false;
        }
        printSolution(path);
        return true;
    }

    private boolean hamiltonianPathUtil(int[][] graph, int[] path, int pos) {
        if (pos == V) return true;
        for (int v = 1; v < V; v++) {
            if (isSafe(v, graph, path, pos)) {
                path[pos] = v;
                if (hamiltonianPathUtil(graph, path, pos + 1)) {
                    return true;
                }
                path[pos] = -1;
            }
        }
        return false;
    }

    private boolean isSafe(int v, int[][] graph, int[] path, int pos) {
        if (graph[path[pos - 1]][v] == 0) return false;
        for (int i = 0; i < pos; i++) {
            if (path[i] == v) return false;
        }
        return true;
    }

    private void printSolution(int[] path) {
        System.out.println("Hamiltonian Path exists: ");
        for (int v : path) {
            System.out.print(v + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        HamiltonianPath hp = new HamiltonianPath();
        int[][] graph = {
            {0, 1, 0, 1, 0},
            {1, 0, 1, 1, 1},
            {0, 1, 0, 0, 1},
            {1, 1, 0, 0, 1},
            {0, 1, 1, 1, 0}
        };
        hp.hamiltonianPath(graph);
    }
}
```

## 🎓 Conclusion

The problems outlined in this section demonstrate the versatility and power of Backtracking in solving complex combinatorial and optimization problems. By mastering these problems, you'll gain a deep understanding of how to apply backtracking techniques in various scenarios.

Key takeaways:
- **N-Queens Problem**: A classic example of using backtracking to explore and prune the search space.
- **Sudoku Solver**: Demonstrates how backtracking can solve constraint satisfaction problems.
- **Subset Sum and Permutations**: Shows how backtracking can be used to explore subsets and permutations of elements.
- **Knight's Tour and Hamiltonian Path**: Illustrates the application of backtracking to problems in graph theory and combinatorial search.

By practicing these problems, you'll enhance your problem-solving skills and be well

-prepared to tackle complex backtracking challenges in your Java applications! 💻🚀

---
