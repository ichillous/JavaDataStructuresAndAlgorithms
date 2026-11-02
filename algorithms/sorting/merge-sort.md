# Merge sort: combine two proven results

Merge sort divides an array into contiguous halves, sorts each half and merges
them. The maintained [implementation](../../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java)
uses half-open ranges, one reusable buffer and ascending `int[]` order. It mutates
input, rejects null and accepts empty input.

## A merge invariant

At each output position, the buffer prefix already contains the smallest consumed
values in sorted order. The unconsumed values in each half remain sorted. Taking
the smaller front value preserves both facts. On a tie, taking the left value
preserves the original relative order of equal keys.

```text
[4, 1, 3, 2]
  /        \
[4, 1]    [3, 2]
  |          |
[1, 4]    [2, 3]
  \        /
 merge fronts: 1, 2, 3, 4
```

```java
import io.github.ichillous.dsa.algorithms.SortAlgorithms;
import java.util.Arrays;

int[] original = {4, 1, 3, 2, 2};
int[] sorted = original.clone();
SortAlgorithms.mergeSort(sorted);
assert Arrays.equals(sorted, new int[] {1, 2, 2, 3, 4});
assert Arrays.equals(original, new int[] {4, 1, 3, 2, 2});
```

The unchanged `original` above comes from cloning before the call; `mergeSort`
itself is in-place only in the API sense that it overwrites the supplied array.
Its temporary buffer means it is not an O(1)-space algorithm.

## Recurrence and optimization

There are two half-size recursive problems and O(n) merging work:
`T(n) = 2T(n/2) + O(n)`, giving O(n log n) worst-case time. The single buffer costs
O(n) auxiliary space, and the recursion stack adds O(log n). The buffer is
allocated even if the array turns out to be sorted.

After sorting both halves, the implementation skips merging when the left maximum
is no greater than the right minimum. Already sorted input therefore takes O(n)
time in this implementation; the O(n log n) statement is a worst-case bound.

**Counterexample:** choosing the right half first on equality still sorts numbers
correctly but loses stability for records with equal keys. Allocating a fresh
buffer for every element would add needless allocation; a shared buffer is enough
because recursive subproblems complete before their parent merge uses it.

## Practice

1. Trace merging `[1, 4, 7]` and `[2, 4, 6]`, labeling the two `4` values by origin.
2. Explain which allocation survives across all recursive calls.
3. Count inversions during merging: when a right value wins, how many unconsumed
   left values form an inversion with it? Continue in
   [divide-and-conquer problems](../divide-and-conquer/problems.md).

Compare your reasoning with the [solutions](../../docs/solutions.md) and
[space analysis](../../complexity-analysis/space-complexity.md).
