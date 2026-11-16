# Counting, radix and stable Comparator sorts

Use these three methods in the maintained
[SortAlgorithms](../../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java)
when the generic `int[]` sorts are not the right fit:

- `countingSort(int[])` sorts ints whose values span a small range.
- `radixSort(int[])` sorts ints of any value, in four linear passes.
- `mergeSort(T[], Comparator)` sorts objects by a caller-chosen order and keeps equal
  elements in their original order.

All three sort in place, reject a null array and accept an empty one. The Comparator
sort also rejects a null comparator and null elements, checking before it moves
anything. Counting sort requires `max - min + 1` to be at most
`COUNTING_SORT_MAX_RANGE` (2^20) and throws `IllegalArgumentException` otherwise,
leaving the array unchanged.

## Why these can beat n log n

A comparison sort learns one bit per comparison. Distinguishing the n! possible input
orders needs at least log2(n!) bits, which is Ω(n log n) comparisons in the worst case.
Counting and radix sort are not comparison sorts: they use each value as an array
index or digit, so a single step reveals far more than one bit. The bound does not
apply to them, but the trade is real. They need integer keys, and they pay in memory
(a count table of size r, or a buffer of size n) or in passes (one per digit).

## The invariants

- **Counting:** after the tally, `counts[v - min]` is exactly the number of times `v`
  occurs. Writing each value back that many times, in increasing order, is the sorted
  array.
- **LSD radix:** after pass `k`, the array is sorted by its lowest `k` digits. Each pass
  is stable, so it keeps the order established by the earlier, less significant
  digits among elements that tie on the current digit. Sorting by the lowest digit
  first and the highest digit last is therefore a full sort.
- **Sign handling:** in two's complement a negative int has its top bit set, so
  unsigned digit order would place it after every non-negative value. The
  implementation XORs each key with `Integer.MIN_VALUE`, flipping that bit, before
  extracting a digit. The stored values are never altered.
- **Comparator merge:** each half is sorted and stable. On a tie the merge takes the
  left element, so equal elements never swap sides.

## A worked trace

An LSD pass on `[170, 45, 75, 90, 2, 24, 802, 66]`, shown in base 10 for readability. The
implementation uses base 256, four passes of 8 bits, but the mechanism is identical.

```text
input             170  45  75  90   2  24 802  66
pass 1 (ones)     bucket 0: 170 90 | 2: 2 802 | 4: 24 | 5: 45 75 | 6: 66
after pass 1      170  90   2 802  24  45  75  66
pass 2 (tens)     bucket 0: 2 802 | 2: 24 | 4: 45 | 6: 66 | 7: 170 75 | 9: 90
after pass 2        2 802  24  45  66 170  75  90
pass 3 (hundreds) bucket 0: 2 24 45 66 75 90 | 1: 170 | 8: 802
after pass 3        2  24  45  66  75  90 170 802
```

In pass 2, `170` and `75` share tens digit 7 and keep the relative order from pass 1 (170
before 75). That is stability doing the work.

## Executable example

```java
import io.github.ichillous.dsa.algorithms.SortAlgorithms;
import java.util.Arrays;
import java.util.Comparator;

int[] small = {3, -2, 3, 0, -2, 1};
SortAlgorithms.countingSort(small);
assert Arrays.equals(small, new int[] {-2, -2, 0, 1, 3, 3});

int[] wide = {Integer.MAX_VALUE, Integer.MIN_VALUE, 0, -1, 1, Integer.MIN_VALUE};
SortAlgorithms.radixSort(wide);
assert Arrays.equals(wide,
        new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE});

int[] tooWide = {Integer.MIN_VALUE, Integer.MAX_VALUE};
try {
    SortAlgorithms.countingSort(tooWide);
    assert false : "range is far above COUNTING_SORT_MAX_RANGE";
} catch (IllegalArgumentException expected) {
    assert tooWide[0] == Integer.MIN_VALUE && tooWide[1] == Integer.MAX_VALUE;
}

String[] words = {"pear", "fig", "kiwi", "apple", "plum", "date"};
SortAlgorithms.mergeSort(words, Comparator.comparingInt(String::length));
assert Arrays.equals(words, new String[] {"fig", "pear", "kiwi", "plum", "date", "apple"});
```

The four-letter words come out as `pear, kiwi, plum, date`, their original relative order.

## Costs

| Method | Time | Auxiliary space | Notes |
| --- | --- | --- | --- |
| `countingSort` | O(n + r), r = max - min + 1 | O(r) | r at most 2^20; range is computed in `long` |
| `radixSort` | O(n): four passes of n + 256 steps | O(n) | Full int range, negatives included |
| `mergeSort(T[], Comparator)` | O(n log n) comparisons | O(n) buffer, O(log n) stack | Stable; null elements rejected |

Counting sort is only a good choice when r is not much larger than n. Sorting two
values that are 2^20 apart still allocates a 4 MiB table.

## Stability and why a Comparator sort needs it

Sorting records by one key is usually one step of several: sort by name, then by city,
and expect people in the same city to stay in name order. That only works if the
second sort is stable. Primitive ints have no identity, so stability is invisible for
them; once elements are objects compared by a key, it is observable and must be a
documented guarantee. `Arrays.sort(T[], Comparator)` makes the same guarantee, which
is what the tests compare against.

## Pitfall: `(a, b) -> a - b`

Subtraction looks like a comparator but overflows. For `a = Integer.MAX_VALUE` and
`b = -1`, `a - b` wraps to a negative number, so the comparator reports that the
larger value is smaller. A sort using it can return a wrong order with no error. Use
`Integer.compare(a, b)` or `Comparator.comparingInt`.

```java
import java.util.Comparator;

Comparator<Integer> broken = (a, b) -> a - b;
Comparator<Integer> correct = Integer::compare;
assert broken.compare(Integer.MAX_VALUE, -1) < 0;
assert correct.compare(Integer.MAX_VALUE, -1) > 0;
```

A comparator must also be consistent (a total preorder). The sort does not verify that.

## Practice

1. Trace `radixSort` on `[-1, 1]` in binary by hand: show the key after the sign-bit
   flip and explain why `-1` ends up first.
2. Choose between `countingSort` and `radixSort` for one million ages in `[0, 150]` and
   for one million arbitrary ints. State the time and space cost of each choice.
3. Sort `(city, name)` pairs by name, then by city with the stable Comparator sort, and
   show what happens to the name order within a city if the second sort were not stable.

See the [catalog](../../docs/catalog.md) and the
[complexity guide](../../docs/complexity.md) for assumptions behind the bounds. The [solution finder](../../docs/solution-finder.md#11-sorting) compares this technique with its alternatives.
