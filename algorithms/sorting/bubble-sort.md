# Bubble sort, insertion sort and local order

Adjacent exchanges make bubble sort useful for learning invariants, although
quadratic worst-case work makes it a poor default for large arrays. The maintained
[SortAlgorithms](../../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java)
mutates `int[]` in ascending order, rejects null and accepts empty input.

## Bubble-sort invariant and trace

During a left-to-right pass, the largest value seen so far moves right. At the end
of the pass, the maximum of the active prefix is at its final position. The suffix
after `end` is therefore sorted and never needs another comparison.

```text
input                  [4, 1, 3, 2]
after first pass       [1, 3, 2 | 4]
after second pass      [1, 2 | 3, 4]
third pass: no swaps    [1 | 2, 3, 4] -> stop
```

A pass with no swaps proves the active portion was already sorted. The maintained
implementation exits early in that case.

```java
import io.github.ichillous.dsa.algorithms.SortAlgorithms;
import java.util.Arrays;

int[] values = {4, 1, 3, 2, 2};
SortAlgorithms.bubbleSort(values);
assert Arrays.equals(values, new int[] {1, 2, 2, 3, 4});
int[] empty = {};
SortAlgorithms.bubbleSort(empty);
assert empty.length == 0;
```

## Insertion sort uses a different invariant

Before inserting element `i`, prefix `[0, i)` is sorted. Shift only larger values
right, then place the saved value in the gap. On nearly sorted data, the number of
shifts tracks the number of inversions rather than always taking quadratic work.

```java
import io.github.ichillous.dsa.algorithms.SortAlgorithms;
import java.util.Arrays;

int[] nearlySorted = {1, 2, 4, 3, 5};
SortAlgorithms.insertionSort(nearlySorted);
assert Arrays.equals(nearlySorted, new int[] {1, 2, 3, 4, 5});
```

Both algorithms take O(n²) worst-case time, O(n) time on already sorted input and
O(1) auxiliary space. Both are stable: equal keys are never swapped or shifted
past one another. Stability is conceptual for primitive integers, whose equal
values do not carry distinct identities.

**Pitfall:** swapping on `>=` instead of `>` destroys that stability. Omitting the
no-swap exit also removes bubble sort's linear best-case bound. A reverse-sorted
array remains quadratic with or without early exit.

## Practice

1. Count comparisons and swaps for `[3, 2, 1]`.
2. Explain why insertion sort needs to save the current value before shifting.
3. Compare all sorts with `Arrays.sort` on copies; check duplicate multiplicities,
   not only whether the result is ordered.

See [solutions](../../docs/solutions.md), then study
[merge sort](merge-sort.md) for a different time/space tradeoff.
