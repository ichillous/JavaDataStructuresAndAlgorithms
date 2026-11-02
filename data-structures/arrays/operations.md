# Array operations: move the right elements

Use [`DynamicArray`](../../src/main/java/io/github/ichillous/dsa/collections/DynamicArray.java) to study resizable indexed storage. The implementation owns its backing array, preserves insertion order, rejects null elements and exposes only the live prefix through its iterator.

## Insertion and removal invariants

To insert `X` at index 1 in `[A, B, C]`, first ensure capacity. Shift elements **right to left**, then write `X` and increase size.

```text
before         A B C _
move C         A B C C
move B         A B B C
write X        A X B C
```

Copying left to right would overwrite `C` before it could be moved. Removal goes in the opposite direction: shift the suffix left, reduce size, and clear the vacated last reference so removed objects can be collected.

```java
import io.github.ichillous.dsa.collections.DynamicArray;

DynamicArray<Integer> values = new DynamicArray<>(1);
values.add(10);
values.add(30);
values.add(1, 20);
int removed = values.remove(0);
int previous = values.set(1, 99);
assert removed == 10;
assert previous == 30;
assert values.size() == 2;
assert values.get(0) == 20 && values.get(1) == 99;
```

`set` replaces a value and returns the previous value; it does not change size. `remove(int)` removes by index, unlike the overloaded value-removal APIs on some JDK lists. `clear()` retains capacity for reuse.

## Boundaries are part of the API

```java
import io.github.ichillous.dsa.collections.DynamicArray;

DynamicArray<String> values = new DynamicArray<>();
values.add(0, "first"); // Insertion at size is allowed.
boolean rejected = false;
try {
    values.get(values.size()); // Reading at size is not allowed.
} catch (IndexOutOfBoundsException expected) {
    rejected = true;
}
assert rejected;
assert values.size() == 1;
```

## Aggregates without shifting

A [Fenwick tree](../../src/main/java/io/github/ichillous/dsa/structures/FenwickTree.java) stores sums of selected intervals. At internal one-based index `i`, the low set bit `i & -i` determines the interval length. A prefix query repeatedly removes that low bit; a point addition visits the ancestors covering that position.

```java
import io.github.ichillous.dsa.structures.FenwickTree;

FenwickTree sums = new FenwickTree(new long[] {2, -1, 4, 3});
assert sums.prefixSum(3) == 5;
sums.add(1, 5); // The logical values are now [2, 4, 4, 3].
assert sums.rangeSum(1, 3) == 8;
assert sums.rangeSum(2, 2) == 0;
```

Fenwick construction costs O(n) time/space; updates and queries cost O(log n) time. Checked intermediate overflow throws `ArithmeticException`; an overflowing update leaves the tree unchanged. It supports additions, not an O(1) general-purpose `set`.

Dynamic-array access/replacement is O(1), indexed shifting is O(n), and append is O(1) amortized with O(n) resize events. Iterators fail fast after structural changes; replacing an element is not structural. Fail-fast behavior detects mistakes and does not make concurrent mutation safe.

## Practice

1. Draw every write when inserting at the front of a full array.
2. Derive `[from, to)` sum as the difference of two prefix sums, including an empty range.
3. Add a reference-array model test for random Fenwick additions and all valid ranges; separately test overflow atomicity.

See the [worked solutions](../../docs/solutions.md), then compare [linked storage](../linked-lists/overview.md).
