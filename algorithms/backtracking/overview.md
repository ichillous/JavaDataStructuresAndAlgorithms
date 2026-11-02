# Backtracking: choose, explore, undo

Backtracking explores a tree of partial solutions while rejecting branches that
already violate a constraint. It is useful when valid combinations matter more
than a single local choice. The maintained
[Backtracking](../../src/main/java/io/github/ichillous/dsa/algorithms/Backtracking.java)
returns immutable results and limits input sizes because it materializes every
solution. Enumeration can be expensive even when each individual test is cheap.

## The state invariant

A recursive call owns a valid partial solution. It selects a currently legal
choice, updates the bookkeeping, explores the smaller remaining problem, then
undoes **every** state change before trying a sibling. Reaching a complete state
requires copying it; retaining the same mutable list would corrupt prior results.

For permutations of `[1, 1, 2]`, equal values occupy distinct input positions, but
choosing equivalent unused `1` positions at the same depth would duplicate a branch:

```text
[]
├─ 1 -> [1, 1, 2], [1, 2, 1]
├─ 1 -> skip: equivalent first choice
└─ 2 -> [2, 1, 1]
```

```java
import io.github.ichillous.dsa.algorithms.Backtracking;
import java.util.List;

int[] input = {1, 1, 2};
var permutations = Backtracking.uniquePermutations(input);
assert permutations.equals(List.of(
        List.of(1, 1, 2), List.of(1, 2, 1), List.of(2, 1, 1)));
assert input[0] == 1 && input[1] == 1 && input[2] == 2;
assert Backtracking.uniquePermutations(new int[0]).equals(List.of(List.of()));
```

The algorithm sorts a copy, tracks used indices and permits an equal value only
when its equivalent predecessor is already used on the current path. It rejects
null and inputs longer than nine. Worst-case time is O(n · n!), and space is
O(n + nk) including `k` returned permutations. Empty input has one empty ordering.

## Pruning with N-Queens

Place exactly one queen per row. Track occupied columns and both diagonal families;
a call at row `r` then contains `r` mutually nonattacking queens. A conflict can
never be fixed by adding more queens, so that branch can safely be pruned.

```java
import io.github.ichillous.dsa.algorithms.Backtracking;
import java.util.List;

assert Backtracking.nQueens(4).equals(List.of(
        List.of(1, 3, 0, 2), List.of(2, 0, 3, 1)));
assert Backtracking.nQueens(2).isEmpty();
assert Backtracking.nQueens(0).equals(List.of(List.of()));
```

Solutions encode the column for each row; sizes are limited to `0..12`. A loose
time bound is O(n · n!), with O(n + nk) storage including output. These are upper
bounds, not a claim that pruning removes the exponential growth.

**Pitfall:** `current.contains(value)` prevents the second `1` from ever being
chosen. Used **positions** and duplicate-branch suppression solve different
problems. Forgetting to undo an occupied column also silently removes valid boards.

## Practice

1. Trace which of the two equal positions is allowed at each permutation depth.
2. Validate queens solutions independently using row, column and diagonal checks.
3. Explain why pruning a negative remaining sum is unsafe with signed inputs;
   continue with [puzzles and subset sum](problems.md).

Check the [solutions](../../docs/solutions.md) after trying a small search tree.
