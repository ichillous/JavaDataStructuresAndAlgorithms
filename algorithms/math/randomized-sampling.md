# Randomized sampling: reservoir, Fisher-Yates and the alias method

Use these tools when a result must be a fair random choice: a uniform sample of a
stream whose length is unknown, a uniformly random order, or repeated draws from a
fixed weighted distribution. The maintained sources are
[Sampling](../../src/main/java/io/github/ichillous/dsa/algorithms/Sampling.java)
(`reservoirSample`, `shuffle`) and
[AliasTable](../../src/main/java/io/github/ichillous/dsa/algorithms/AliasTable.java).

Preconditions: you supply the `java.util.random.RandomGenerator`. A seeded
`SplittableRandom` makes a run reproducible, which is what tests and lessons use;
production code can pass `ThreadLocalRandom.current()` or a `SecureRandom`.
`reservoirSample` needs non-null elements and `k >= 0`. `AliasTable` needs finite,
non-negative weights with a positive finite sum. `shuffle` mutates its array; the
other two never change their inputs.

## Which tool when

| Need | Tool | Why |
| --- | --- | --- |
| `k` random items from a stream of unknown length, one pass | reservoir sampling (Algorithm R) | O(k) memory, never needs `n` up front |
| A uniformly random order of an array | Fisher-Yates `shuffle` | Each of the n! orders has probability exactly 1/n! |
| The same shuffle for a `List` | `Collections.shuffle(list, random)` | Same algorithm from the JDK; it needs a `Random`, so wrap other generators |
| Many weighted draws from fixed weights | `AliasTable` | O(n) build, O(1) per draw, versus O(log n) for a prefix-sum binary search |
| Weights that change between draws | Fenwick tree or prefix sums | The alias table must be rebuilt after any change |

## The invariants

Reservoir sampling keeps the first `k` items, then treats item number `m + 1` like
this: draw `j` uniformly from `[0, m + 1)` and replace slot `j` when `j < k`. If
every item seen so far is in the reservoir with probability `k / m`, then the new
item is in with probability `k / (m + 1)`, and an old item stays in with probability
`(k / m)(1 - 1 / (m + 1)) = k / (m + 1)`. Induction from `m = k` gives a uniform
sample at every prefix, so the stream can stop anywhere.

Fisher-Yates walks `i` from `n - 1` down to 1 and swaps position `i` with a uniform
position in `[0, i]`. Positions above `i` are final. The number of choice sequences
is `n (n - 1) ... 2 = n!`, and each yields a different permutation, so each order
has probability exactly `1 / n!`.

The alias method cuts the unit interval into `n` equal columns. Each column holds at
most two indices: its own, with probability `cutoff`, and an `alias` otherwise.
Vose's construction scales weights to mean 1, then repeatedly pairs a "small" index
(scaled weight below 1) with a "large" one: the small index fills its column with
its own mass and the large donor tops up the rest, shrinking by the amount it gave.
For every index the mass in the table, `(cutoff + sum of (1 - cutoff) of columns
aliasing it) / n`, equals its normalized weight. Rounding can leave entries in a
worklist when the other is empty; they have mass 1 up to rounding, so they get
cutoff 1.

Trace of the table for weights `[1, 0, 3]` (sum 4, scaled weights `[0.75, 0, 2.25]`):

```text
small = {0, 1}   large = {2}
pair small 1 (0.00) with large 2 (2.25): cutoff[1] = 0.00, alias[1] = 2, large 2 -> 1.25
pair small 0 (0.75) with large 2 (1.25): cutoff[0] = 0.75, alias[0] = 2, large 2 -> 1.00
leftover large 2 (1.00):                 cutoff[2] = 1.00
mass of 0 = 0.75 / 3                       = 0.25
mass of 1 = 0.00 / 3                       = 0
mass of 2 = (1.00 + 0.25 + 1.00) / 3       = 0.75
```

A draw picks a column uniformly, then keeps it with probability `cutoff`: index 1 is
never kept, so it is never returned.

```java
import io.github.ichillous.dsa.algorithms.AliasTable;
import io.github.ichillous.dsa.algorithms.Sampling;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

SplittableRandom random = new SplittableRandom(42);

List<Integer> stream = List.of(10, 20, 30, 40, 50, 60);
List<Integer> sample = Sampling.reservoirSample(stream, 3, random);
assert sample.size() == 3 && stream.containsAll(sample);
assert Sampling.reservoirSample(stream, 10, random).equals(stream);

int[] values = {1, 2, 3, 4, 5};
Sampling.shuffle(values, random);
int[] sorted = values.clone();
Arrays.sort(sorted);
assert Arrays.equals(sorted, new int[] {1, 2, 3, 4, 5});

AliasTable table = new AliasTable(new double[] {1, 0, 3});
assert Math.abs(table.probability(2) - 0.75) < 1e-12;
int zeros = 0;
for (int i = 0; i < 1_000; i++) {
    if (table.sample(random) == 1) {
        zeros++;
    }
}
assert zeros == 0;
```

## Costs and contracts

| Method | Time | Auxiliary space | Edge cases |
| --- | --- | --- | --- |
| `reservoirSample(items, k, random)` | O(n), one draw per item after the first `k` | O(min(k, n)) | `k = 0` gives `[]` without reading the stream; `n <= k` returns every item in encounter order; `k < 0` throws `IllegalArgumentException`; null elements throw; result is immutable and its order is not meaningful |
| `shuffle(values, random)` | O(n), `n - 1` draws | O(1) | In place; length 0 or 1 draws nothing; duplicates are distinct positions |
| `new AliasTable(weights)` | O(n) | O(n) | Copies its input; NaN, infinite, negative, empty or all-zero weights throw `IllegalArgumentException` |
| `sample(random)` | O(1) | O(1) | Zero-weight indices are never returned; `probability(i)` is the normalized input weight |

## Pitfalls

**Modulo bias.** `Math.abs(random.nextInt()) % n` is not uniform. A 32-bit draw has
2^32 outcomes, and unless `n` divides that, the first `2^32 mod n` residues occur
one extra time. For `n = 3` the remainders 0 and 1 are slightly more likely than 2.
`Math.abs(Integer.MIN_VALUE)` is also negative. `RandomGenerator.nextInt(bound)`
rejects the uneven tail, which is why the library uses it for every bounded draw.

**The "swap with any position" shuffle.** Swapping position `i` with a uniform
position in `[0, n)` for every `i` has `n^n` equally likely choice sequences, which
cannot map evenly onto `n!` orders unless `n^n` is a multiple of `n!`. For `n = 3`,
27 sequences over 6 orders leave some orders more likely than others. Fisher-Yates
draws from `[0, i]`, not `[0, n)`.

**Small generator state.** A generator with a 32-bit seed can produce at most
2^32 distinct shuffles, far fewer than `n!` for `n > 12`, however correct the
algorithm. Statistical tests must also allow sampling noise: the tests here use
fixed seeds and tolerances of about 0.01 over 60,000 trials, never exact counts.

## Practice

1. Prove that after `m` items a reservoir of size `k` holds a particular early item
   with probability exactly `k / m`, using the replacement step above.
2. Build the alias table by hand for weights `[2, 1, 1]`. List each column's
   `cutoff` and `alias`, then verify that the three masses add back to the weights.
3. Explain why shuffling a `List` with `Collections.shuffle(list, new Random(7))`
   gives the same order on every run, and why that is a feature in a test and a
   defect in a card game.

See the [catalog](../../docs/catalog.md) for the source and tests and the
[complexity guide](../../docs/complexity.md) for the bounds. Related lessons:
[selection](../searching/selection.md) and
[probabilistic structures](../../data-structures/probabilistic/overview.md).
