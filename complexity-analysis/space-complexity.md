# Space complexity: follow ownership and lifetime

Count the storage that is live at the same time. Distinguish caller-owned input,
auxiliary working memory, retained data-structure capacity and returned output.
The [maintained complexity table](../docs/complexity.md) states which category its
bounds include; “O(1) extra space” does not mean the input itself occupies no memory.

## Aliases are not copies

Assigning an array reference creates an alias, not another array. Cloning creates
a new array, and for a primitive array its elements are independent values.

```java
import java.util.Arrays;

int[] original = {2, 4, 6};
int[] alias = original;
int[] copy = original.clone();
alias[0] = 9;
assert original[0] == 9;
assert Arrays.equals(copy, new int[] {2, 4, 6});
assert alias == original && copy != original;
```

The clone costs O(n) time and O(n) new storage. An object-array clone copies
references, so referenced mutable objects may still be shared. An immutable outer
list likewise does not make every object stored inside it deeply immutable.

## Peak live storage, not total allocated bytes

| Operation | Working storage to account for |
| --- | --- |
| Iterative binary search | A fixed number of indices: O(1) |
| Maintained merge sort | One O(n) buffer plus O(log n) live call frames |
| Maintained quicksort | O(log n) frames because only the smaller partition recurses |
| Naive recursive chain | O(n) frames even if each frame holds only a few values |
| LCS length with two rows | O(min(m,n) + 1), rather than the full O(mn) table |
| Enumerating `k` length-`n` permutations | O(nk) returned data plus search workspace |

The lifetime invariant for a recursive algorithm is: count the current call path
and every buffer still reachable from it. Two sibling calls executed sequentially
do not multiply stack depth by two. Arrays retained by the parent can, however,
remain live while a child runs.

## Output can dominate the working state

KMP uses a prefix table proportional to pattern length, but reporting every match
also allocates a result. Empty patterns match every boundary:

```java
import io.github.ichillous.dsa.algorithms.StringAlgorithms;
import java.util.List;

var matches = StringAlgorithms.kmpFindAll("abcd", "");
assert matches.equals(List.of(0, 1, 2, 3, 4));
assert matches.size() == "abcd".length() + 1;
```

The `k` reported positions require O(k) output storage regardless of how little
state the scanning loop uses. The same distinction matters for path lists, graph
components, autocomplete suggestions and materialized backtracking solutions.

**Counterexample:** an array-backed collection that once held a million elements
may retain that capacity after removing almost everything. Its storage is
O(capacity), not O(current size), unless a shrinking policy is specified. Java GC
can reclaim unreachable objects; it cannot shrink an array that remains in use.

## Practice

1. Draw reference arrows before and after the alias/copy example.
2. Account separately for merge sort's input, reusable buffer and deepest call path.
3. Compare an iterator with a snapshot returned as a list: which needs storage
   proportional to the number of reported values, and how does mutation affect it?

Read [practical memory contracts](../algorithms/complexity-analysis/space-complexity.md)
and the [solutions](../docs/solutions.md) for retained capacity and snapshots.
