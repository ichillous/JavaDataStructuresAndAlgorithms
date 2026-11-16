# Open addressing: linear probing with backward-shift deletion

Use an open-addressed table when you want a hash map with no per-entry nodes: mappings live directly in two arrays and a collision moves to a nearby slot. The maintained [OpenAddressingHashMap](../../src/main/java/io/github/ichillous/dsa/collections/OpenAddressingHashMap.java) mirrors [ChainedHashMap](../../src/main/java/io/github/ichillous/dsa/collections/ChainedHashMap.java): keys and values are non-null, `put` and `remove` return the previous value, and null means absent. Read [collision resolution](collision-resolution.md) first for the chaining side of the comparison.

## Chaining versus probing

| Question | Separate chaining | Linear probing |
| --- | --- | --- |
| Where does a colliding key go? | Into its bucket's linked list | Into the next free slot, wrapping at the end |
| Extra memory | One node per entry | None per entry; the table itself must stay at most 2/3 full |
| Load factor above 1 | Allowed; chains lengthen | Impossible; the table must grow first |
| Cache behavior | Pointer chasing | Neighboring slots, usually the same cache lines |
| Deletion | Unlink one node | Must repair the cluster (below) |
| Sensitive to | Long chains from bad hashes | Long clusters, which also merge with neighbors |

## The invariant

The table length is a power of two. A key's home slot is the Fibonacci hash of `h ^ (h >>> 16)`: multiply by `0x9E3779B9` and keep the top bits. The first step spreads high bits into low bits, and the multiply scrambles patterns such as sequential integers.

**Every key is stored on its probe path: starting at its home slot and moving forward, there is no empty slot between the home slot and the key.** A lookup walks from the home slot and may stop at the first empty slot, because an empty slot proves the key was never placed beyond it. The class keeps the load at most 2/3, so an empty slot always exists and an absent-key search always terminates.

## Load factor and expected probes

With load `α = n / capacity` and well-distributed hashes, Knuth's classic analysis of linear probing gives these expected probe counts:

| Search | Expected probes | At α = 1/2 | At α = 2/3 |
| --- | --- | --- | --- |
| Successful | about ½(1 + 1/(1−α)) | 1.5 | 2 |
| Unsuccessful | about ½(1 + 1/(1−α)²) | 2.5 | 5 |

The unsuccessful figure grows quadratically as α approaches 1, which is why the table resizes when a `put` would push the load above 2/3 rather than waiting until it is full. Doubling the table re-inserts every mapping, because home slots depend on the table length; copying slots to the same indices would break the invariant.

## Backward-shift deletion trace

Capacity 8. Each entry is written `key(home)`. Suppose `A` and `B` both hash to slot 5, `C` to slot 6, `D` to slot 7 and `E` to slot 1. Insertion order A, B, C, D, E gives a cluster that wraps past the end of the table:

```text
slot:   0     1     2   3   4   5     6     7
        D(7)  E(1)  .   .   .   A(5)  B(5)  C(6)
```

`B` is pushed to slot 6, which `C` wanted, so `C` lands in slot 7; that was `D`'s home, so `D` wraps to slot 0. Now `remove(A)`. The hole starts at slot 5 and the scan walks forward until an empty slot. An entry may fill the hole unless its home slot lies cyclically after the hole and at or before the entry's own slot:

```text
hole=5  look at slot 6: B(5)  home 5 is not after the hole    -> move B to 5, hole=6
hole=6  look at slot 7: C(6)  home 6 is not after the hole    -> move C to 6, hole=7
hole=7  look at slot 0: D(7)  home 7 is not after the hole    -> move D to 7, hole=0
hole=0  look at slot 1: E(1)  home 1 is after the hole        -> stays put
        look at slot 2: empty                                 -> stop; clear slot 0

slot:   0   1     2   3   4   5     6     7
        .   E(1)  .   .   .   B(5)  C(6)  D(7)
```

Every remaining key is again reachable from its home slot with no gap, and `E` was correctly left alone: moving it to slot 0 would put it before its home slot, where lookups would never find it.

## Executable example

```java
import io.github.ichillous.dsa.collections.OpenAddressingHashMap;

record Key(int id) {
    @Override
    public int hashCode() {
        return 42; // every key shares one home slot: one long cluster
    }
}

OpenAddressingHashMap<Key, String> map = new OpenAddressingHashMap<>(8);
for (int i = 0; i < 6; i++) {
    assert map.put(new Key(i), "v" + i) == null;
}
assert map.size() == 6;
assert map.remove(new Key(0)).equals("v0"); // the cluster head
assert map.remove(new Key(3)).equals("v3"); // a key in the middle
assert map.remove(new Key(3)) == null;
for (int i : new int[] {1, 2, 4, 5}) {
    assert map.get(new Key(i)).equals("v" + i);
}
assert !map.containsKey(new Key(0));
assert map.put(new Key(1), "new").equals("v1");
assert map.size() == 4;
```

## Why tombstones degrade

The common alternative marks a removed slot as a tombstone: lookups skip it, inserts may reuse it. This keeps the invariant but pays elsewhere:

- A miss must walk past every tombstone in its cluster until a never-used slot, so repeated insert/remove churn makes unsuccessful searches slower even though `size()` stays small.
- The table can fill with tombstones. The load that bounds probe lengths must count them, so the map needs a periodic rehash that has nothing to do with live entries.
- Removing and re-adding different keys leaves residue that depends on history, which is harder to test and reason about.

Backward shifting pays the repair cost once, inside `remove`, which scans to the end of the cluster: O(cluster length). Afterward the table has no residue, and probe lengths depend only on the live keys.

## Costs

| Operation | Time | Auxiliary space |
| --- | --- | --- |
| `get`, `containsKey`, `remove` | O(1) expected; O(n) worst | O(1) |
| `put` | O(1) expected amortized; O(n) worst | O(1) except a resize, which needs O(capacity) temporary space |
| `clear` | O(capacity) | O(1) |
| Storage | O(capacity), and capacity is at least 1.5n because the load stays at most 2/3 | none per entry |

Worst cases appear when many keys share a home slot or a cluster: a constant `hashCode` makes the whole table one cluster. User `equals` and `hashCode` are assumed O(1).

## Pitfall: clearing the slot only

The tempting `remove` sets the found slot to empty and stops. With the trace above, clearing slot 5 leaves `B` in slot 6 with home 5. A later `get(B)` starts at slot 5, sees it empty and reports absent, although `B` is stored. The executable example would fail on `map.get(new Key(1))` if `remove` did that, because all six keys share one home slot.

## OpenJDK implementation note

`java.util.IdentityHashMap`, which compares keys by reference, is documented in OpenJDK as a simple linear-probe hash table that stores keys and values in one array, alternating key and value. That is an implementation note about OpenJDK's class, not a guarantee of the `Map` contract, and other JDKs are free to differ. `HashMap` and `ChainedHashMap`, by contrast, chain collisions.

## Practice

1. Capacity 8, entries `A(3)`, `B(3)`, `C(4)`, `D(3)` inserted in that order. Write the slots, then trace `remove(B)` and say which entries move.
2. Compute the expected probe count for an unsuccessful search at α = 3/4 and α = 0.9, and explain why the resize threshold is 2/3 rather than 0.9.
3. Write a test that inserts and removes 10,000 different keys while the size never exceeds 5, then checks that every lookup still ends quickly. Explain why a tombstone design would need extra rehash logic for the same test.

See the [catalog](../../docs/catalog.md) for the API and tests, and the [complexity guide](../../docs/complexity.md) for the cost table.
