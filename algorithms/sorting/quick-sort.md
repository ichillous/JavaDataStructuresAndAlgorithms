# Three-way quicksort with a bounded call stack

Quicksort partitions around a pivot, then sorts the regions below and above it.
The maintained [implementation](../../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java)
uses a deterministic middle-position pivot and groups equal values together.
It mutates `int[]` in ascending order, rejects null and accepts empty arrays.

## Four regions during partitioning

```text
[ low ... less ) [ less ... scan ) [ scan ... greater ] ( greater ... high ]
     < pivot          == pivot           unknown              > pivot
```

When the scanned value is smaller, swap it into the first region and advance both
`less` and `scan`. When it is larger, swap with `greater` and shrink the unknown
region without advancing `scan`: the incoming value still needs inspection.
An equal value only advances `scan`. Each step shrinks the unknown region.

For `[3, 1, 3, 2, 3]` and pivot `3`, partitioning ends with a below-pivot region
containing `1, 2`, an equal region containing three `3`s, and an empty upper
region. Equal values need no further sorting.

```java
import io.github.ichillous.dsa.algorithms.SortAlgorithms;
import java.util.Arrays;

int[] values = {3, 1, 3, 2, 3, Integer.MIN_VALUE, Integer.MAX_VALUE};
int[] expected = values.clone();
Arrays.sort(expected);
SortAlgorithms.quickSort(values);
assert Arrays.equals(values, expected);
int[] equal = {7, 7, 7, 7};
SortAlgorithms.quickSort(equal);
assert Arrays.equals(equal, new int[] {7, 7, 7, 7});
```

## Time and stack are separate questions

Balanced partitions give O(n log n) time. Bad deterministic pivot choices can give
O(n²) time; the implementation does not randomize and claims no worst-case
O(n log n) time guarantee. All-equal input is handled in O(n) by one partition.

Only the smaller partition is recursive. The larger one is processed by a loop,
so each nested call has at most half as many elements. This bounds auxiliary
stack space to O(log n), even if the total partition work is quadratic. The sort
is unstable because long-distance swaps can reorder equal keys.

**Pitfalls:** advancing `scan` after swapping in an unexamined value can leave an
out-of-place element. Recursing on both partitions instead of looping over the
larger one can require O(n) stack space on an unlucky input. Comparing arbitrary
integers by subtraction can overflow; direct `<` and `>` comparisons are safe.

## Practice

1. Trace all four regions for `[2, 3, 1, 2]`.
2. Explain why the equal region is excluded from both later subproblems.
3. Compare maximum call depth with total comparison count; construct partition
   sizes that show one can remain logarithmic while the other is quadratic.

Use the [solutions](../../docs/solutions.md) and contrast the worst-case guarantee
of [heapsort](heap-sort.md).
