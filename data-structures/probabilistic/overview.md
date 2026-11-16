# Probabilistic membership and frequency: Bloom filter and Count-Min sketch

Use these structures when exact answers cost too much memory and a small,
one-sided, quantifiable error is acceptable. A Bloom filter answers "have I seen
this?" and a Count-Min sketch answers "about how many times have I seen this?",
both in space that does not grow with the size of the elements. Preconditions: the
elements must have a good `hashCode()` and be non-null; you must accept that the
answer is a bound, not a fact.

The maintained sources are
[BloomFilter](../../src/main/java/io/github/ichillous/dsa/structures/BloomFilter.java)
and
[CountMinSketch](../../src/main/java/io/github/ichillous/dsa/structures/CountMinSketch.java),
tested by
[BloomFilterTest](../../src/test/java/io/github/ichillous/dsa/structures/BloomFilterTest.java)
and
[CountMinSketchTest](../../src/test/java/io/github/ichillous/dsa/structures/CountMinSketchTest.java).

## The invariants and the one-sided errors

**Bloom filter.** `add` sets `k` bits chosen by the element; bits are never
cleared. So if an element was added, all of its `k` bits are set and
`mightContain` returns `true`: there are **no false negatives**. If some bit of a
query is still `0`, no added element can have set it, so `false` is certain. The
only error is a **false positive**: an absent element whose `k` bits were all set
by other elements.

**Count-Min sketch.** `add(item, c)` adds `c` to one counter in each of `depth`
rows. A counter holds the true count of its item plus the counts of every other
item that collided into it, so each counter is at least the true count, and so is
their minimum. The only error is an **overestimate**; it never underestimates.
Taking the minimum over independent rows is what makes a large overestimate
unlikely.

Index `i` of a Bloom element comes from two 32-bit halves `h1`, `h2` of a
SplitMix64-mixed `hashCode()`: `floorMod(h1 + i * step, m)` with
`step = 1 + floorMod(h2, m - 1)`. The step is never a multiple of `m`, so the
`k` probes of one element cannot all land on the same bit (using `h2` directly would
collapse them for about one element in `m`). Both structures reject sizes above a
fixed cap (2^30 bits, 2^25 counters) instead of exhausting memory. The Count-Min row hashes are SplitMix64 of
`hashCode ^ rowSeed[i]` with fixed, documented seeds, so every result in this
lesson is reproducible.

## Sizing math with worked numbers

For `n` expected elements and target false-positive rate `p`:

```text
m = ceil(-n * ln p / (ln 2)^2)        bits
k = max(1, round(m / n * ln 2))       indices per element
```

For `n = 1000`, `p = 0.01`: `-1000 * ln 0.01 = 4605.17`, divided by
`(ln 2)^2 = 0.48045` gives `9585.06`, so `m = 9586` bits (about 1.2 KB, 9.6 bits
per element) and `k = round(9.586 * 0.6931) = round(6.64) = 7`.
`estimatedFalsePositiveRate()` evaluates `(1 - e^(-k * insertions / m))^k`, which is
about `0.01` after 1000 distinct insertions.

For a Count-Min sketch with relative error `epsilon` and failure probability
`delta`:

```text
width = ceil(e / epsilon)
depth = max(1, ceil(ln(1 / delta)))
guarantee: estimate(x) <= count(x) + epsilon * total   with probability >= 1 - delta
```

For `epsilon = 0.01`, `delta = 0.01`: `e / 0.01 = 271.83`, so `width = 272`;
`ln 100 = 4.605`, so `depth = 5`. That is `272 * 5 = 1360` counters regardless of
how many distinct items arrive. After 50,000 additions the allowed overestimate is
`0.01 * 50000 = 500`.

## A worked trace

A 16-bit filter with `k = 2`, where the hash gives element `a` bits `{3, 9}`,
element `b` bits `{9, 14}` and the absent element `c` bits `{3, 14}`:

```text
step        bits set            result
add a       3, 9
add b       3, 9, 14
query c     bit 3 = 1, bit 14 = 1   mightContain = true   (false positive: c was never added)
query d     bit 5 = 0               mightContain = false  (definitely absent)
```

## Executable example

```java
import io.github.ichillous.dsa.structures.BloomFilter;
import io.github.ichillous.dsa.structures.CountMinSketch;

BloomFilter<String> filter = BloomFilter.forExpectedInsertions(1000, 0.01);
assert filter.bitCount() == 9586;
assert filter.hashCount() == 7;
filter.add("apple");
filter.add("apple");
assert filter.mightContain("apple");
assert filter.insertions() == 2;
assert filter.estimatedFalsePositiveRate() < 1e-9;

CountMinSketch<String> sketch = CountMinSketch.withErrorBounds(0.01, 0.01);
assert sketch.width() == 272;
assert sketch.depth() == 5;
sketch.add("apple", 3);
sketch.add("pear");
assert sketch.estimate("apple") >= 3;
assert sketch.estimate("pear") >= 1;
assert sketch.totalCount() == 4;
```

## Costs

| Operation | Time | Extra space | Error |
| --- | --- | --- | --- |
| `BloomFilter.add`, `mightContain` | O(k) | O(1); storage `ceil(m / 64)` longs | False positives only, about `p` at design load |
| `CountMinSketch.add`, `estimate` | O(depth) | O(1); storage `width * depth` longs | Overestimates only, at most `epsilon * total` with probability `1 - delta` |
| Exact `HashSet` / `HashMap` | Expected O(1) | O(n) elements | None |

Counters use checked arithmetic: an `add` that would overflow a counter or the
total throws `ArithmeticException` and leaves the sketch unchanged.

## Pitfalls and counterexamples

- **No deletion.** Clearing the bits of a removed element can clear bits shared
  with other elements and create false negatives, destroying the central
  guarantee. A plain Bloom filter only grows; rebuild it to forget.
  Counting variants trade space for deletion and are not implemented here.
- **Equal hash codes are indistinguishable.** Only `hashCode()` is used, and
  `"Aa"` and `"BB"` share one, so adding the first makes `mightContain("BB")`
  `true` with certainty. Overfilling is the other failure: past the design `n` the
  false-positive rate climbs quickly.
- **A sketch is not a counter map.** `estimate` of an item never added can be
  positive, and the bound is relative to the total stream length, so it says little
  about rare items in a short stream.

HyperLogLog (approximate distinct counts) and cuckoo filters (approximate
membership with deletion) are related structures that are **not implemented here**.

## Practice

1. For `n = 5000` and `p = 0.001`, compute `m` and `k` by hand, then check them
   with `forExpectedInsertions`.
2. Add 3 to `"x"` and 5 to `"y"` in a sketch of width 1 and depth 1. What do both
   estimates return, and why does this still satisfy the guarantee?
3. Design a test that compares a Count-Min sketch to a `HashMap` of exact counts
   over a seeded random stream, and state which inequality it must assert for every
   key.

See the [catalog](../../docs/catalog.md) and the
[complexity guide](../../docs/complexity.md) for the surrounding structures. The [solution finder](../../docs/solution-finder.md#4-approximate-membership-and-counting) compares this technique with its alternatives.
