# Recurrences, amortization and honest cost models

Start with the [time-complexity foundations](../../complexity-analysis/time-complexity.md).
This lesson derives bounds for actual patterns used in the repository rather than
assigning a complexity to code merely because it contains recursion or a loop.

## Balanced divide and conquer

A merge-sort call guarantees a sorted interval. Two half-size children plus a
linear merge give `T(n) = 2T(n/2) + O(n)`. For a power-of-two size, each merge level
touches at most `n` values and there are `log2(n)` such levels. This simple model
counts full-merge work; the maintained implementation can skip ordered merges.

```java
int n = 16;
long fullMergeVisits = 0L;
int levels = 0;
for (int width = n; width > 1; width /= 2) {
    fullMergeVisits += n;
    levels++;
}
assert levels == 4;
assert fullMergeVisits == 64L;
```

By contrast, a partition sequence of sizes `n-1, n-2, ...` sums to Θ(n²). A
quicksort implementation can bound stack depth without fixing that worst-case
time: [smaller-partition recursion](../sorting/quick-sort.md) does precisely that.
The recurrence describes work; live frames determine stack space separately.

## A sequence can be cheap despite expensive individual steps

A geometrically growing array copies capacities `1, 2, 4, 8, ...`. For `m` appends,
the total copied entries are less than `2m`, in addition to `m` writes. The
accounting invariant is that earlier cheap appends pay for the next capacity copy;
no probabilistic assumption is needed.

```java
import io.github.ichillous.dsa.collections.DynamicArray;

int count = 16;
long modeledCopies = 0L;
for (int capacity = 1; capacity < count; capacity *= 2) {
    modeledCopies += capacity;
}
var values = new DynamicArray<Integer>(0);
for (int value = 0; value < count; value++) {
    values.add(value);
}
assert modeledCopies == 15L;
assert modeledCopies < 2L * count;
assert values.size() == 16 && values.get(15) == 15;
```

This model gives O(1) **amortized** append but O(n) time for an individual growth.
Indexed insertion still shifts O(n) elements and is not covered by that append
bound. Growing by a fixed single slot would copy `1 + 2 + ... + m` entries instead,
so it would make repeated appends quadratic.

## Include the size hidden inside an operation

Hashing and equality may inspect a key; string comparisons are not always O(1).
Bellman–Ford here uses exact integers to avoid false overflow, so its relaxation
cost includes integer bit length. Numeric-capacity DP may allocate a table much
larger than the number of bits used to encode that capacity.

**Pitfall:** calling a nested scan “linear” because each individual iteration is
simple ignores how often it restarts. Conversely, the deque in
[sliding-window maximum](../divide-and-conquer/problems.md) is linear because
each index enters and leaves at most once across the whole run.

## Practice

1. Draw merge-sort work for sizes 8 and 16, distinguishing leaves from merge levels.
2. Repeat the copy sum with capacity multiplication by 3; explain why a constant
   factor greater than one still gives linear total copying.
3. State the probability assumption in expected hash lookup, then contrast it with
   the deterministic accounting proof for dynamic-array growth.

See the [solutions](../../docs/solutions.md) and
[implementation-specific bounds](../../docs/complexity.md).
