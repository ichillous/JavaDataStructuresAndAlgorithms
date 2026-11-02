# Arrays: position is the address

An array stores a fixed number of values addressed by integer index. Use an array when positions matter and size is known; use a dynamic array when the sequence must grow. Contiguous storage makes indexed reads cheap, but inserting at the front still moves later elements.

A Java array has indices `0..length-1`. A range `[from, to)` includes `from`, excludes `to`, and has `to - from` elements. This convention makes the empty range `[i, i)` natural and lets adjacent ranges join without overlap.

## Trace the storage

```text
index        0    1    2    3
value       10   20   30   40
range [1,3)      [20   30]
```

```java
import java.util.Arrays;

int[] values = {10, 20, 30, 40};
int[] middle = Arrays.copyOfRange(values, 1, 3);
assert Arrays.equals(middle, new int[] {20, 30});
middle[0] = 99;
assert values[1] == 20; // The copied array owns different storage.
```

Assigning `int[] alias = values` would share the same array instead. For object arrays, cloning copies references, so referenced mutable objects remain shared. An array's fixed length says nothing about whether its elements may change.

## Grow a logical sequence

[`DynamicArray`](../../src/main/java/io/github/ichillous/dsa/collections/DynamicArray.java) separates **size**, the live element count, from **capacity**, the allocated storage. Its invariant is that live elements occupy exactly the prefix `[0, size)`; unused positions contain no retained elements.

```java
import io.github.ichillous.dsa.collections.DynamicArray;

DynamicArray<String> names = new DynamicArray<>(0);
names.add("Ada");
names.add("Grace");
names.add(1, "Edsger");
assert names.size() == 3;
assert names.get(1).equals("Edsger");
assert names.get(2).equals("Grace");
```

Zero initial capacity is valid; negative capacity is rejected. This library rejects null elements. Existing-element operations require `index < size`, while insertion also permits `index == size`.

## Costs and tradeoffs

| Operation | Fixed array | Dynamic array |
| --- | --- | --- |
| Get / replace by valid index | O(1) | O(1) |
| Append with spare capacity | Needs a separate logical size | O(1) |
| Append when full | Allocate and copy manually | O(n) for that append; O(1) amortized over appends |
| Insert / remove at an index | Shift manually | O(n) worst case |
| Stored space | O(length) | O(capacity), with O(n) temporary space during growth |

Geometric growth is essential to the amortized bound. Growing by exactly one slot on every append copies `1 + 2 + ... + n` elements, which is quadratic overall. Amortized O(1) does not mean each individual append is constant time.

For repeated range sums with point additions, study [`FenwickTree`](../../src/main/java/io/github/ichillous/dsa/structures/FenwickTree.java). For range minimum with point replacement, use [`SegmentTree`](../../src/main/java/io/github/ichillous/dsa/structures/SegmentTree.java). Neither makes ordinary array insertion cheap.

## Practice

1. Predict the logical size and physical contents after removing index 1 from `[10, 20, 30]`.
2. Explain why copying references does not deep-copy mutable objects.
3. Compare 1,000 seeded insert/remove operations with `ArrayList`, including insertion at `size`.

Continue with [array operations](operations.md) and check the [worked solutions](../../docs/solutions.md).
