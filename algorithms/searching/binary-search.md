# Binary search as a boundary problem

Use binary search when an array is already in ascending order. The maintained
[SearchAlgorithms](../../src/main/java/io/github/ichillous/dsa/algorithms/SearchAlgorithms.java)
does not scan to validate sortedness: that would turn an O(log n) query into O(n).
Methods reject null, accept an empty array and never modify input.

| Method | Meaning when the target is absent |
| --- | --- |
| `lowerBound(a, x)` | First index with value at least `x`, possibly `a.length` |
| `upperBound(a, x)` | First index with value greater than `x`, possibly `a.length` |
| `binarySearch(a, x)` | First matching index, or `-1` |

## The half-open invariant

For lower bound, every position before `low` contains a value below the target;
every position at or after `high` contains a value at least the target. Only
`[low, high)` remains unknown. Initially that interval is the whole array. Each
comparison shrinks it strictly, and `low == high` identifies the boundary.

Target `2` in `[1, 2, 2, 2, 5]`:

```text
low high middle value action
 0    5     2     2    high = 2
 0    2     1     2    high = 1
 0    1     0     1    low = 1
 1    1                answer = 1
```

```java
import io.github.ichillous.dsa.algorithms.SearchAlgorithms;

int[] sorted = {1, 2, 2, 2, 5};
assert SearchAlgorithms.binarySearch(sorted, 2) == 1;
assert SearchAlgorithms.lowerBound(sorted, 2) == 1;
assert SearchAlgorithms.upperBound(sorted, 2) == 4;
assert SearchAlgorithms.upperBound(sorted, 2)
        - SearchAlgorithms.lowerBound(sorted, 2) == 3;
assert SearchAlgorithms.lowerBound(sorted, 0) == 0;
assert SearchAlgorithms.lowerBound(sorted, 6) == sorted.length;
assert SearchAlgorithms.binarySearch(sorted, 4) == -1;
assert SearchAlgorithms.lowerBound(new int[0], 2) == 0;
```

These three search methods take O(log(n + 1)) time and O(1) extra space. The midpoint is
computed as `low + (high - low) / 2` so adding two large indices cannot overflow.
Preparing sorted data costs extra, and inserting into a sorted array can shift
O(n) elements even though finding its insertion position is logarithmic.

**Counterexample:** `[1, 9, 2]` violates the monotonic predicate “value is below
2.” Discarding a half can discard the answer. Binary search is valid because the
predicate changes once, not merely because a problem involves numbers.

## Practice

1. Trace both bounds when every element equals the target.
2. Use two bounds to count values in an inclusive range `[a, b]`.
3. Specify a monotonic feasibility predicate for finding the minimum machine
   capacity that completes a fixed workload; prove it is monotonic before coding.

Check the [solutions](../../docs/solutions.md) and the
[complexity guide](../../docs/complexity.md) for assumptions behind the bounds.
