---

# 🧩 Common Dynamic Programming Problems in Java

![Java Dynamic Programming Problems](https://img.shields.io/badge/Java-DP_Problems-teal?style=for-the-badge&logo=java)

## 📋 Table of Contents
- [Fibonacci Sequence](#-fibonacci-sequence)
- [Knapsack Problem](#-knapsack-problem)
- [Longest Common Subsequence (LCS)](#-longest-common-subsequence-lcs)
- [Matrix Chain Multiplication](#-matrix-chain-multiplication)
- [Coin Change Problem](#-coin-change-problem)
- [Edit Distance](#-edit-distance)
- [Rod Cutting Problem](#-rod-cutting-problem)
- [Subset Sum Problem](#-subset-sum-problem)
- [Optimal Binary Search Tree](#-optimal-binary-search-tree)

## 🌀 Fibonacci Sequence

### Problem Statement:
Calculate the nth Fibonacci number, where each number in the sequence is the sum of the two preceding ones. The sequence starts with 0 and 1.

### Dynamic Programming Approach:
This problem has overlapping subproblems, making it a perfect candidate for Dynamic Programming. You can solve it using either memoization or tabulation.

#### Implementation:
```java
public class Fibonacci {
    // Top-Down Approach with Memoization
    public int fibonacciMemo(int n, int[] memo) {
        if (n <= 1) return n;
        if (memo[n] != 0) return memo[n];
        memo[n] = fibonacciMemo(n - 1, memo) + fibonacciMemo(n - 2, memo);
        return memo[n];
    }

    // Bottom-Up Approach with Tabulation
    public int fibonacciTab(int n) {
        if (n <= 1) return n;
        int[] dp = new int[n + 1];
        dp[0] = 0;
        dp[1] = 1;
        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        return dp[n];
    }

    public static void main(String[] args) {
        Fibonacci fib = new Fibonacci();
        int n = 10;
        System.out.println("Fibonacci number using memoization: " + fib.fibonacciMemo(n, new int[n + 1]));
        System.out.println("Fibonacci number using tabulation: " + fib.fibonacciTab(n));
    }
}
```

## 🎒 Knapsack Problem

### Problem Statement:
Given a set of items, each with a weight and a value, determine the maximum value that can be obtained by selecting items such that their total weight does not exceed a given limit.

### Dynamic Programming Approach:
The Knapsack Problem can be solved using a bottom-up approach where you build a table to store the maximum value that can be obtained with a given weight limit.

#### Implementation:
```java
public class Knapsack {
    public int knapsack(int[] weights, int[] values, int capacity) {
        int n = weights.length;
        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            for (int w = 1; w <= capacity; w++) {
                if (weights[i - 1] <= w) {
                    dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][w - weights[i - 1]] + values[i - 1]);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        return dp[n][capacity];
    }

    public static void main(String[] args) {
        Knapsack knapsack = new Knapsack();
        int[] weights = {1, 3, 4, 5};
        int[] values = {1, 4, 5, 7};
        int capacity = 7;
        System.out.println("Maximum value in Knapsack: " + knapsack.knapsack(weights, values, capacity));
    }
}
```

## 🔄 Longest Common Subsequence (LCS)

### Problem Statement:
Given two sequences, find the length of the longest subsequence present in both sequences.

### Dynamic Programming Approach:
The LCS problem can be solved by building a table where each entry represents the LCS of the subsequences up to that point.

#### Implementation:
```java
public class LongestCommonSubsequence {
    public int lcs(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        return dp[m][n];
    }

    public static void main(String[] args) {
        LongestCommonSubsequence lcs = new LongestCommonSubsequence();
        String s1 = "AGGTAB";
        String s2 = "GXTXAYB";
        System.out.println("Length of LCS: " + lcs.lcs(s1, s2));
    }
}
```

## 📏 Matrix Chain Multiplication

### Problem Statement:
Determine the most efficient way to multiply a given sequence of matrices. The problem is to find the minimum number of multiplications required to multiply the sequence of matrices.

### Dynamic Programming Approach:
The Matrix Chain Multiplication problem can be solved using a table that stores the minimum number of scalar multiplications needed to compute the matrix product.

#### Implementation:
```java
public class MatrixChainMultiplication {
    public int matrixChainOrder(int[] p) {
        int n = p.length - 1;
        int[][] dp = new int[n][n];

        for (int length = 2; length <= n; length++) {
            for (int i = 0; i < n - length + 1; i++) {
                int j = i + length - 1;
                dp[i][j] = Integer.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    int cost = dp[i][k] + dp[k + 1][j] + p[i] * p[k + 1] * p[j + 1];
                    if (cost < dp[i][j]) {
                        dp[i][j] = cost;
                    }
                }
            }
        }

        return dp[0][n - 1];
    }

    public static void main(String[] args) {
        MatrixChainMultiplication mcm = new MatrixChainMultiplication();
        int[] p = {1, 2, 3, 4};
        System.out.println("Minimum number of multiplications: " + mcm.matrixChainOrder(p));
    }
}
```

## 🪙 Coin Change Problem

### Problem Statement:
Given an array of coin denominations and a total amount, find the minimum number of coins needed to make up that amount.

### Dynamic Programming Approach:
This problem can be solved using a bottom-up DP approach where you build a table to store the minimum coins needed for each amount up to the target amount.

#### Implementation:
```java
public class CoinChange {
    public int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1);
        dp[0] = 0;

        for (int i = 1; i <= amount; i++) {
            for (int coin : coins) {
                if (i - coin >= 0) {
                    dp[i] = Math.min(dp[i], dp[i - coin] + 1);
                }
            }
        }

        return dp[amount] > amount ? -1 : dp[amount];
    }

    public static void main(String[] args) {
        CoinChange cc = new CoinChange();
        int[] coins = {1, 2, 5};
        int amount = 11;
        System.out.println("Minimum coins needed: " + cc.coinChange(coins, amount));
    }
}
```

## 🖊️ Edit Distance

### Problem Statement:
Given two strings, find the minimum number of operations required to convert one string into the other. The allowed operations are insertion, deletion, and substitution.

### Dynamic Programming Approach:
The Edit Distance problem is solved by constructing a table that keeps track of the minimum operations required for all substrings.

#### Implementation:
```java
public class EditDistance {
    public int minDistance(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp

[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }

        return dp[m][n];
    }

    public static void main(String[] args) {
        EditDistance ed = new EditDistance();
        String word1 = "horse";
        String word2 = "ros";
        System.out.println("Minimum edit distance: " + ed.minDistance(word1, word2));
    }
}
```

## ✂️ Rod Cutting Problem

### Problem Statement:
Given a rod of length `n` and a table of prices for different lengths, determine the maximum revenue obtainable by cutting up the rod and selling the pieces.

### Dynamic Programming Approach:
This problem can be solved by building a table that stores the maximum revenue obtainable for each rod length.

#### Implementation:
```java
public class RodCutting {
    public int cutRod(int[] prices, int n) {
        int[] dp = new int[n + 1];
        
        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < i; j++) {
                dp[i] = Math.max(dp[i], prices[j] + dp[i - j - 1]);
            }
        }
        
        return dp[n];
    }

    public static void main(String[] args) {
        RodCutting rc = new RodCutting();
        int[] prices = {1, 5, 8, 9, 10, 17, 17, 20};
        int n = 8;
        System.out.println("Maximum obtainable value: " + rc.cutRod(prices, n));
    }
}
```

## 🔢 Subset Sum Problem

### Problem Statement:
Given a set of non-negative integers and a sum, determine if there is a subset with a sum equal to the given sum.

### Dynamic Programming Approach:
This problem can be solved by using a table where the entry `dp[i][j]` is `true` if a subset of the first `i` elements has a sum equal to `j`.

#### Implementation:
```java
public class SubsetSum {
    public boolean isSubsetSum(int[] set, int sum) {
        int n = set.length;
        boolean[][] dp = new boolean[n + 1][sum + 1];

        for (int i = 0; i <= n; i++) {
            dp[i][0] = true;
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= sum; j++) {
                if (set[i - 1] <= j) {
                    dp[i][j] = dp[i - 1][j] || dp[i - 1][j - set[i - 1]];
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }

        return dp[n][sum];
    }

    public static void main(String[] args) {
        SubsetSum ss = new SubsetSum();
        int[] set = {3, 34, 4, 12, 5, 2};
        int sum = 9;
        System.out.println("Subset with given sum exists: " + ss.isSubsetSum(set, sum));
    }
}
```

## 🌲 Optimal Binary Search Tree

### Problem Statement:
Given a sorted array of keys and their corresponding search probabilities, construct a Binary Search Tree (BST) that minimizes the expected search cost.

### Dynamic Programming Approach:
This problem can be solved by filling a table that stores the minimum cost of searching an optimal BST for every possible subsequence of keys.

#### Implementation:
```java
public class OptimalBST {
    public int optimalSearchTree(int[] keys, int[] freq, int n) {
        int[][] dp = new int[n][n];

        for (int length = 1; length <= n; length++) {
            for (int i = 0; i <= n - length; i++) {
                int j = i + length - 1;
                dp[i][j] = Integer.MAX_VALUE;
                int sum = 0;
                for (int k = i; k <= j; k++) {
                    sum += freq[k];
                }
                for (int r = i; r <= j; r++) {
                    int cost = sum + (r > i ? dp[i][r - 1] : 0) + (r < j ? dp[r + 1][j] : 0);
                    if (cost < dp[i][j]) {
                        dp[i][j] = cost;
                    }
                }
            }
        }

        return dp[0][n - 1];
    }

    public static void main(String[] args) {
        OptimalBST obst = new OptimalBST();
        int[] keys = {10, 12, 20};
        int[] freq = {34, 8, 50};
        int n = keys.length;
        System.out.println("Cost of Optimal BST: " + obst.optimalSearchTree(keys, freq, n));
    }
}
```

## 🎓 Conclusion

The problems outlined in this section demonstrate the wide applicability and versatility of Dynamic Programming in solving complex optimization problems. By mastering these problems, you'll be well-equipped to tackle even more challenging DP problems in your Java development journey.

Key takeaways:
- **Fibonacci Sequence**: A classic example demonstrating the basics of DP.
- **Knapsack Problem**: Illustrates how DP can be used to solve combinatorial optimization problems.
- **Longest Common Subsequence**: Shows how DP can be applied to sequence alignment problems.
- **Matrix Chain Multiplication**: Demonstrates the efficiency of DP in solving multiplication order optimization.
- **Coin Change and Edit Distance**: Real-world problems that showcase the power of DP in solving complex issues.

By practicing these problems, you'll strengthen your understanding of Dynamic Programming and enhance your problem-solving skills in Java! 💻🚀

---
