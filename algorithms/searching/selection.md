# Selection: k-th smallest, top-k, k-way merge and majority

Use selection when you need a rank or a few extreme elements, not a full order.
The maintained [Selection](../../src/main/java/io/github/ichillous/dsa/algorithms/Selection.java)
offers four tools. `kthSmallest` takes an unsorted `int[]` and a zero-based rank.
`smallestK` takes any collection of non-null elements and a comparator.
`mergeSorted` takes lists that are each already sorted. `majorityElement` takes an
`int[]`. Every method rejects null, leaves its input unchanged and returns a fresh
value; returned lists are immutable.

## Which tool when

| Need | Tool | Why |
| --- | --- | --- |
| One rank (median, percentile) of an array | quickselect / introselect | Discards a constant fraction each round: O(n) |
| The k best of a stream or collection, in order, stable | bounded heap | O(n log k) time, O(k) space, never sorts everything |
| Many ranks, or the whole order | sort once | O(n log n), then every rank is an index |
| Several sorted runs into one | k-way merge with a heap | O(N log k), no re-sorting |
| An element held by more than half the positions | Boyer-Moore vote | One pass, O(1) space, plus a verifying pass |

## The invariant

Partition the range around a pivot into three parts: values below it, values equal
to it, values above it. The k-th smallest of the whole range lies in the part that
contains index `k`: everything to the left is smaller and everything to the right
is larger, so no rank inside another part can change. If `k` falls in the equal
part, the pivot value is the answer. Otherwise recurse into one part only.

A fixed pivot rule alone is not enough. With a median-of-three pivot, crafted
inputs make each round peel off only a handful of elements, so the work is
n + (n - 1) + (n - 2) + ... = O(n²). `kthSmallest` is therefore introselect with a
shrink-based trigger. After each median-of-three round it compares the segment that
still contains `k` with the segment the round started from. If the new segment is
longer than three quarters of the old one, the cheap pivot has failed, and the rest
of the selection uses median-of-medians pivots (medians of groups of five, then
their median), which discard a constant fraction of the segment every round.

Why that is linear in the worst case: every successful cheap round leaves at most
3/4 of the segment, so successful rounds cost at most n(1 + 3/4 + (3/4)^2 + ...) =
4n in total. At most one cheap round fails, costing at most n more, and the
median-of-medians phase on what remains is O(n). The three-way partition also keeps
all-equal input linear.

Trace of `[7, 2, 9, 4, 4, 1]` with `k = 2` (sorted: 1, 2, 4, 4, 7, 9):

```text
range      pivot  partition result (< | == | >)     where k=2 falls
[0..5]     7      [2 1 4 4] | [7] | [9]              left part, new range [0..3]
[0..3]     2      [1] | [2] | [4 4]                  right part, new range [2..3]
[2..3]     4      [] | [4 4] | []                    equal part: answer 4
```

The pivot is the median of the first, middle and last values of the range: 7, 9, 1
gives 7, then 2, 1, 4 gives 2, then 4, 4, 4 gives 4.

```java
import io.github.ichillous.dsa.algorithms.Selection;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;

int[] values = {7, 2, 9, 4, 4, 1};
assert Selection.kthSmallest(values, 2) == 4;
assert Selection.kthSmallest(values, 0) == 1;
assert Selection.kthSmallest(values, 5) == 9;
assert values[0] == 7 && values[5] == 1;

record Entry(String name, int score) {}
List<Entry> entries = List.of(new Entry("a", 5), new Entry("b", 1),
        new Entry("c", 5), new Entry("d", 1), new Entry("e", 3));
List<Entry> best = Selection.smallestK(entries, 3, Comparator.comparingInt(Entry::score));
assert best.equals(List.of(new Entry("b", 1), new Entry("d", 1), new Entry("e", 3)));

List<List<Integer>> runs = List.of(List.of(1, 4, 7), List.of(2, 4), List.of());
assert Selection.mergeSorted(runs, Comparator.<Integer>naturalOrder())
        .equals(List.of(1, 2, 4, 4, 7));

assert Selection.majorityElement(new int[] {3, 3, 4}).equals(OptionalInt.of(3));
assert Selection.majorityElement(new int[] {1, 2}).isEmpty();
```

## Costs and contracts

| Method | Time | Auxiliary space | Ties and edge cases |
| --- | --- | --- | --- |
| `kthSmallest(values, k)` | O(n) worst case (cheap rounds total at most 4n, then median-of-medians) | O(n) for the copy, O(log n) recursion in the fallback | Duplicates count separately; `k` outside `[0, n)` throws `IndexOutOfBoundsException`, so an empty array always throws |
| `smallestK(values, k, order)` | O(n log(k + 1)) | O(min(k, n)) plus the result | Equal elements keep encounter order (same as `sorted(order).limit(k)`); `k = 0` gives `[]`; `k >= n` gives all, sorted; `k < 0` throws `IllegalArgumentException` |
| `mergeSorted(lists, order)` | O(N log(k + 1)) for N elements in k lists | O(k) plus the result | Equal elements: lower list index first, then earlier position; an unsorted list throws `IllegalArgumentException` when the out-of-order element is reached |
| `majorityElement(values)` | O(n) | O(1) | Needs a count strictly above `n / 2`; empty input and a tie like `[1, 1, 2, 2]` give empty |

## Pitfalls

**A vote is not a proof.** After the first Boyer-Moore pass the candidate for
`[1, 2, 3]` is `3`, although no value is a majority. Without the second, counting
pass the algorithm would report it. The verification pass is what makes the answer
trustworthy.

**Sorting a heap result by hand.** `smallestK` ranks equal elements by arrival
order. A plain `PriorityQueue` loop that replaces the root when an element is
`<=` the root, or that ignores arrival order, can return a different group of
equal elements than `stream().sorted(order).limit(k)`.

**Unsorted merge inputs.** `mergeSorted` assumes each list is sorted and checks
that only as it consumes the list: it cannot silently produce a wrong order, but it
throws rather than repairing the input.

## Practice

1. Trace `kthSmallest` on `[5, 5, 5, 5]` with `k = 3`. How many rounds does the
   three-way partition need, and how many would a two-way partition need?
2. Use `smallestK` with a reversed comparator to find the three most expensive
   items, and say which of two equally priced items is returned when only one fits.
3. Show that Boyer-Moore returns a non-majority candidate for `[1, 2, 3]`, and
   explain why a majority value can never be cancelled completely by pairing
   different values.

See the [catalog](../../docs/catalog.md) for the source and tests and the
[complexity guide](../../docs/complexity.md) for the bounds. Related lessons:
[binary search](binary-search.md) and [heap sort](../sorting/heap-sort.md). The [solution finder](../../docs/solution-finder.md#10-selection-and-top-k) compares this technique with its alternatives.
