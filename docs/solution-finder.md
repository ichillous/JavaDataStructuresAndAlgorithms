# Solution finder

Start from the problem, not from a structure's name. Each section below names a
problem you might search for and lists the reasonable data-structure and algorithm
options in Java: when each fits, what it costs, where it lives (a class in this
repository, a JDK class or method, or nowhere yet) and what goes wrong in practice.
Search the page with Ctrl-F (Cmd-F on macOS) for a problem word such as "nearest",
"top k", "overlap", "prefix", "shortest" or "cache", or for a class name. Rows marked
`not implemented here` keep a technique visible without code. Exact contracts of the
repository classes are in the [catalog](catalog.md), and their bounds are in the
[complexity guide](complexity.md).

## How to choose

Answer these questions before picking a row:

1. **Do you need order?** Sorted iteration, nearest keys, ranks and range queries
   need an ordered structure. Lookup and membership alone do not.
2. **Is the data static or changing?** Static data can be preprocessed once
   (sorting, prefix sums, sparse tables). Changing data needs a structure that
   supports updates between queries.
3. **Is an approximate answer acceptable?** Filters and sketches trade exactness
   (false positives, overestimates) for far less memory.
4. **Is memory bounded, or is the data a stream?** If you cannot keep every item,
   prefer one-pass techniques with bounded state.
5. **How large is the input, and do you need worst-case guarantees?** Expected and
   amortized bounds are usually fine. Adversarial input, latency limits or
   hash-flooding risk call for worst-case bounds.
6. **Must the input stay unmodified?** Many sorts and partitions work in place.
   Copy first, or choose an API that leaves its input alone.

## Status legend

| Marker | Meaning |
| --- | --- |
| `repo:` | A class in this repository, linked to its source and a lesson. It is tested to the standard in the [quality bar](quality.md). |
| `JDK:` | A Java SE 17 class or method, written as plain text in the form `java.util.TreeMap#floorKey`. |
| `not implemented here` | A known technique with no code in this repository. The row gives the idea, when to use it and its cost. |

- JDK statements follow the Java SE 17 API specification.
- "OpenJDK implementation note" marks behavior of the OpenJDK implementation, not
  a guarantee: it can change between releases and vendors. "Stated in the
  Javadoc" means the Java SE 17 Javadoc gives it as an implementation note rather
  than as part of the contract.
- The Time / space column describes the technique. For a JDK class, only a bound
  quoted from its specification is a guarantee.
- Unless a row says otherwise, n is the number of items, k the number of results,
  V and E the numbers of vertices and edges.
- Repository structures are not thread safe, and they reject null elements, keys
  and values.

## Contents

1. [Look up by key](#1-look-up-by-key)
2. [Ordered keys and nearest-key queries](#2-ordered-keys-and-nearest-key-queries)
3. [Prefix and text keys](#3-prefix-and-text-keys)
4. [Approximate membership and counting](#4-approximate-membership-and-counting)
5. [Range queries over arrays](#5-range-queries-over-arrays)
6. [Intervals](#6-intervals)
7. [Stacks, queues, deques and priority](#7-stacks-queues-deques-and-priority)
8. [Caching and eviction](#8-caching-and-eviction)
9. [Searching](#9-searching)
10. [Selection and top-k](#10-selection-and-top-k)
11. [Sorting](#11-sorting)
12. [Array and sequence patterns](#12-array-and-sequence-patterns)
13. [String matching and text](#13-string-matching-and-text)
14. [Graph traversal and structure](#14-graph-traversal-and-structure)
15. [Shortest paths](#15-shortest-paths)
16. [Spanning trees, flows and matching](#16-spanning-trees-flows-and-matching)
17. [Trees as data](#17-trees-as-data)
18. [Dynamic programming](#18-dynamic-programming)
19. [Greedy](#19-greedy)
20. [Backtracking and search](#20-backtracking-and-search)
21. [Disjoint sets and bitsets](#21-disjoint-sets-and-bitsets)
22. [Math and number theory](#22-math-and-number-theory)
23. [Randomization and streams](#23-randomization-and-streams)
24. [Concurrency-aware JDK structures](#24-concurrency-aware-jdk-structures)
25. [Encoding and compression](#25-encoding-and-compression)

## 1. Look up by key

### Find a value by key when order does not matter

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| General-purpose hash map | The default for key-to-value lookup in one thread | Expected O(1) `get`/`put`; the spec states no worst-case bound; O(n) entries, and iteration costs capacity plus size | JDK: `java.util.HashMap` | Constant time is expected, not worst case: the spec assumes "the hash function disperses the elements properly among the buckets". Keys must keep stable `equals` and `hashCode` while stored. Iteration order is unspecified and may change. Permits one null key and null values. OpenJDK implementation note: a bin with many colliding keys may be stored as a tree. |
| Separate chaining | To see buckets, load factor and resizing at work, or to get this repository's non-null contract | Expected O(1) `get`/`remove`, expected amortized O(1) `put`; O(n) worst; O(n + capacity) | repo: [ChainedHashMap](../src/main/java/io/github/ichillous/dsa/collections/ChainedHashMap.java) ([lesson](../data-structures/hash-tables/collision-resolution.md)) | A `null` return means absent. Keys with equal hashes share one chain, so a poor `hashCode` makes lookups linear. |
| Open-addressing map (linear probing, backward-shift deletion) | Many small entries, where one flat array beats a node per entry for memory and cache locality | Expected amortized O(1) `get`/`put`/`remove` with well-spread hashes (load stays at most 2/3); O(n) worst; O(capacity) | repo: [OpenAddressingHashMap](../src/main/java/io/github/ichillous/dsa/collections/OpenAddressingHashMap.java) ([lesson](../data-structures/hash-tables/open-addressing.md)) | Clusters grow quickly as the load approaches 1, so the repository map doubles its table before the load passes 2/3. Backward-shift deletion shifts later cluster members back instead of leaving tombstones, so lookups do not degrade after many removals. A constant or adversarial `hashCode` forms one cluster and makes each operation O(n). Null keys and values are rejected, so a `null` return means absent. OpenJDK implementation note, stated in the Javadoc: `java.util.IdentityHashMap` is a linear-probe table. |
| Cuckoo hashing | Lookups must be worst-case O(1): a key can only be in one of two slots | O(1) worst-case lookup; expected amortized O(1) insert; O(n) | not implemented here | An insert can loop and force a full rehash with new hash functions. Usable load is about 50% with two single-slot tables. |
| Robin Hood hashing | Open addressing with lower variance in probe length | Expected O(1); O(n) worst | not implemented here | On insert, an entry far from its home slot displaces one that is closer to home. Deletion needs backward shifting. Still degrades with a poor hash. |
| Perfect hashing (static key set) | The key set is fixed in advance, such as keywords or a frozen dictionary | O(1) worst-case lookup after an O(n) expected build; O(n) | not implemented here | Any change to the key set means a rebuild. A lookup of a key outside the set must still compare the stored key. |

### Look up with special key semantics or a predictable order

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Insertion-ordered hash map | Results must come back in first-insertion order | Spec: constant-time basic operations under good hash dispersion; iteration proportional to size, not capacity | JDK: `java.util.LinkedHashMap` | Re-inserting an existing key does not change its position. The access-order mode is a different contract (see [caching](#8-caching-and-eviction)). |
| Order-preserving deduplication | Remove duplicates but keep each item's first position | Expected O(1) per add; O(n) | JDK: `java.util.LinkedHashSet` | Re-adding an element does not move it. |
| Identity map | Keys must compare with `==`, as in object-graph copying or serialization node tables | Spec: constant-time `get`/`put` assuming `System.identityHashCode` disperses well | JDK: `java.util.IdentityHashMap` | It intentionally violates the general `Map` contract. Never use it for value-like keys such as strings or boxed numbers. |
| Enum-keyed map | Every key comes from one enum type | Spec: represented internally as an array. OpenJDK implementation note, stated in the Javadoc: constant-time basic operations | JDK: `java.util.EnumMap` | Iterates in enum declaration order. Null keys are rejected; null values are allowed. |
| Enum set (bit vector) | A set of enum constants, replacing `int` bit flags | Spec: represented internally as a bit vector. OpenJDK implementation note, stated in the Javadoc: constant-time basic operations | JDK: `java.util.EnumSet` | Iterates in declaration order; null elements are rejected. |
| Weak-keyed map | Attach data to objects without keeping them alive | Spec: performance similar to `HashMap` | JDK: `java.util.WeakHashMap` | The garbage collector removes entries at any time, so `size` can shrink with no call. OpenJDK implementation note, stated in the Javadoc: values are held strongly, so a value that refers to its own key keeps the entry alive. Recreatable keys such as `String` give confusing results. |

```java
import java.time.DayOfWeek;
import java.util.EnumMap;
import java.util.List;

EnumMap<DayOfWeek, Integer> shifts = new EnumMap<>(DayOfWeek.class);
shifts.put(DayOfWeek.FRIDAY, 3);
shifts.put(DayOfWeek.MONDAY, 5);
shifts.put(DayOfWeek.WEDNESDAY, 4);
// Iteration follows enum declaration order, not insertion order.
assert List.copyOf(shifts.keySet())
        .equals(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
```

### Build a lookup map from a list of records

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Index by a unique key | Each record has one key, such as an ID | Expected O(n); O(n) | JDK: `java.util.stream.Collectors#toMap` | Duplicate keys throw `IllegalStateException`; pass a merge function to choose a winner. The spec makes no guarantee about the returned map's type or mutability; use the four-argument overload with a map factory when that matters. |
| Group into a multimap | Several records share a key | Expected O(n); O(n) | JDK: `java.util.stream.Collectors#groupingBy`; `java.util.Map#computeIfAbsent` | `groupingBy` also makes no guarantee about the map type; pass `TreeMap::new` for sorted keys. |

```java
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

List<String> words = List.of("apple", "avocado", "banana");
boolean duplicateKey = false;
try {
    words.stream().collect(Collectors.toMap(w -> w.charAt(0), w -> w));
} catch (IllegalStateException expected) {
    duplicateKey = true;                      // 'a' is mapped twice
}
assert duplicateKey;
Map<Character, String> firstByLetter = words.stream()
        .collect(Collectors.toMap(w -> w.charAt(0), w -> w, (first, second) -> first));
assert firstByLetter.get('a').equals("apple");
```

## 2. Ordered keys and nearest-key queries

### Find the nearest key (floor, ceiling) or scan a key range

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Red-black tree map or set | Keys change over time and you need sorted iteration, nearest keys or range views | Spec: guaranteed log(n) for `containsKey`, `get`, `put` and `remove` (`TreeSet`: `add`, `remove`, `contains`); O(n) | JDK: `java.util.TreeMap#floorKey`, `#ceilingKey`, `#subMap`; `java.util.TreeSet#floor`, `#ceiling` | The spec calls `TreeMap` "a Red-Black tree based NavigableMap". The comparator must be consistent with `equals`, or the map breaks the `Map` contract: keys that compare as 0 are the same key. `floorKey` and `ceilingKey` return `null` when no key qualifies. `subMap`, `headMap` and `tailMap` are live views, not copies. OpenJDK implementation note: navigation walks one root-to-leaf path, but a range view's `size()` iterates the range, O(k). Not thread safe. |
| Sorted array with binary search | The keys are built once, or rarely change, and are queried often | O(n log n) build; O(log n) query; O(n) insert; O(n) | JDK: `java.util.Arrays#binarySearch`; repo: [SearchAlgorithms.lowerBound](../src/main/java/io/github/ichillous/dsa/algorithms/SearchAlgorithms.java) and `upperBound` ([lesson](../algorithms/searching/binary-search.md)) | Keeping the array sorted on insert shifts O(n) elements. With duplicates, the JDK search may return any match; use lower and upper bounds for the first and last. |
| Skip list | An ordered map shared between threads | Spec: expected average log(n) for `containsKey`, `get`, `put` and `remove`; O(n) expected | JDK: `java.util.concurrent.ConcurrentSkipListMap#ceilingKey` | Balance is probabilistic, so the bounds are expected, not worst case. Null keys and values are rejected. See [concurrency](#24-concurrency-aware-jdk-structures). |
| Red-black ordered map with rank/select | You need floor and ceiling plus "how many keys are smaller" or "the k-th smallest key" | O(log n) worst per operation (`put` and `remove` use O(log n) stack, queries are iterative); O(n) | repo: [RedBlackTreeMap](../src/main/java/io/github/ichillous/dsa/structures/RedBlackTreeMap.java) with `floorKey`, `ceilingKey`, `rank`, `select` and `countInRange` ([lesson](../data-structures/trees/red-black-trees.md)) | `TreeMap` has no rank or select. OpenJDK implementation note: `headMap(key).size()` iterates the range, O(k). The repository map orders keys by `Comparable` natural order only (no comparator), `rank(key)` counts keys strictly smaller, `select` throws `IndexOutOfBoundsException` outside `[0, size())`, and `keys()` and `height()` cost O(n). |
| Unbalanced binary search tree | Teaching, or keys that arrive in random order | O(h) per operation, O(n) worst; O(n) | repo: [BinarySearchTree](../src/main/java/io/github/ichillous/dsa/structures/BinarySearchTree.java) ([lesson](../data-structures/trees/binary-search-trees.md)) | Sorted insertions build a chain, so h = n. Set semantics: a duplicate add returns false. No floor or ceiling API. |
| AVL tree | An ordered set with a worst-case height bound | O(log n) worst `add`/`remove`/`contains`; O(n) | repo: [AvlTree](../src/main/java/io/github/ichillous/dsa/structures/AvlTree.java) ([lesson](../data-structures/trees/avl-trees.md)) | A set only: no values, floor or ceiling. Height stays below about 1.44·log2(n + 2), lower than a red-black tree's 2·log2(n + 1), at the price of more rebalancing on updates. |
| B-tree or B+ tree | Keys live in pages on disk or in a database index, where each node read is expensive | O(log_B n) node reads for node size B; O(n) | not implemented here | Pays off only when a node read is costly (I/O or cache misses). B+ trees keep values in linked leaves for fast range scans. |

```java
import java.util.List;
import java.util.TreeMap;

TreeMap<Integer, String> releases = new TreeMap<>();
releases.put(10, "v1");
releases.put(20, "v2");
releases.put(40, "v3");
assert releases.floorKey(25) == 20;      // greatest key <= 25
assert releases.ceilingKey(25) == 40;    // least key >= 25
assert releases.floorKey(5) == null;     // no such key: null, not an exception
assert releases.ceilingKey(41) == null;
// subMap(from, to) is half-open and is a live view of the map.
var window = releases.subMap(10, 40);
assert List.copyOf(window.keySet()).equals(List.of(10, 20));
releases.put(30, "v2.5");
assert List.copyOf(window.keySet()).equals(List.of(10, 20, 30));
```

### Find the k-th smallest key or the rank of a key

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Order-statistic tree (subtree sizes) | Keys change and you need rank and select | O(log n) per operation in a balanced tree; O(n) | repo: [RedBlackTreeMap.rank](../src/main/java/io/github/ichillous/dsa/structures/RedBlackTreeMap.java) and `select` ([lesson](../data-structures/trees/red-black-trees.md)) | Every node stores its subtree size, and every rotation must update it. The repository's red-black ordered map with rank/select (see "Red-black ordered map" earlier) is one instance. |
| Fenwick tree over compressed keys | Every key that can ever occur is known in advance, and counts change | O(log n) rank and update; O(n) | repo: [FenwickTree](../src/main/java/io/github/ichillous/dsa/structures/FenwickTree.java) ([lesson](../data-structures/arrays/operations.md)) | First map each key to its index in the sorted list of all keys (coordinate compression). The class gives prefix sums, which are ranks; select (find the k-th) is not provided. |
| Sorted array rank | Static keys | O(log n) rank by lower bound; O(1) select by index | repo: [SearchAlgorithms.lowerBound](../src/main/java/io/github/ichillous/dsa/algorithms/SearchAlgorithms.java) ([lesson](../algorithms/searching/binary-search.md)) | Any insertion costs O(n). |

## 3. Prefix and text keys

### Find all keys that start with a prefix (autocomplete)

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Trie | Many prefix queries over a changing set of strings | O(L log A) per add, contains or prefix check for length L and at most A children per node; enumeration adds visited nodes and output | repo: [Trie.wordsWithPrefix](../src/main/java/io/github/ichillous/dsa/structures/Trie.java) ([lesson](../data-structures/tries/overview.md)) | A node per character uses far more memory than the strings themselves. Case-sensitive UTF-16 with no normalization. |
| Sorted set prefix scan | You already keep a sorted set of strings, or prefix queries are rare | O(log n) string comparisons to find the first candidate, then one step per result | JDK: `java.util.TreeSet#ceiling`, `#tailSet` | Start at `ceiling(prefix)` and stop at the first string that does not start with the prefix. `String.compareTo` orders UTF-16 code units, not locale order. |
| Radix (Patricia) trie | Keys with long shared runs, such as URLs or IP prefixes | O(L) per operation; O(n) nodes | not implemented here | Each edge stores a substring; splitting an edge on insert is the tricky part. |
| Ternary search tree | Prefix queries with less memory than a child map per node | O(L + h) per operation, where h depends on insertion order; O(total characters) | not implemented here | Each node has less, equal and greater children. Sorted insertion order unbalances it. |

### Search for many substrings inside one fixed text

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Suffix array + LCP | Many substring queries, the longest repeated substring, or distinct-substring counts over one fixed text | O(n log n) build by prefix doubling with radix-sorted rounds, O(n + 65,536) space; Kasai LCP O(n); O(p log n + k log k) to list the k ascending occurrences of a pattern of length p; O(n) for the distinct-substring count and the longest repeated substring | repo: [SuffixArray](../src/main/java/io/github/ichillous/dsa/algorithms/SuffixArray.java) with `occurrences`, `lcp`, `distinctSubstrings` and `longestRepeatedSubstring` ([lesson](../algorithms/strings/multi-pattern-and-suffix-arrays.md)) | The text is fixed: rebuild when it changes. Suffixes are ordered by UTF-16 code unit. The LCP array (Kasai's algorithm, O(n)) answers repeated-substring questions; the longest repeated substring may overlap itself, and ties go to the lexicographically smallest. An empty pattern matches at every position `0..n`. |
| Suffix automaton | Distinct substrings or the longest common substring of two strings in linear time | O(n log A) build with ordered transitions; at most 2n − 1 states | not implemented here | Harder to get right than a suffix array. |
| Suffix tree | Linear-time substring queries with direct tree navigation | O(n) build (Ukkonen) for a fixed alphabet; O(n) nodes with a large constant | not implemented here | Memory-heavy. A suffix array plus LCP usually answers the same questions. |

## 4. Approximate membership and counting

### Ask "have I seen this item?" with limited memory

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Exact hash set | Memory for every distinct item is affordable | Expected O(1) per operation; O(n) | JDK: `java.util.HashSet` | Spec: constant time "assuming the hash function disperses the elements properly among the buckets". |
| Bitset over integer IDs | Items are dense, nonnegative integers | O(1) test; O(1) set inside the current size, while a bit past it grows the array (OpenJDK implementation note: amortized O(1) for increasing indices); space proportional to the largest index | JDK: `java.util.BitSet` | One huge index allocates a huge array. See [bitsets](#21-disjoint-sets-and-bitsets). |
| Bloom filter | A large set where an occasional false "maybe present" is acceptable, such as skipping disk or network lookups | O(k) per add or query for k hash functions; about 9.6 bits per item for a 1% false-positive rate | repo: [BloomFilter](../src/main/java/io/github/ichillous/dsa/structures/BloomFilter.java) ([lesson](../data-structures/probabilistic/overview.md)) | False positives only, never false negatives. A plain Bloom filter cannot delete. Size it for the expected count (`forExpectedInsertions(1000, 0.01)` gives 9,586 bits and 7 hashes); overfilling raises the false-positive rate. The repository filter consults only `hashCode()`, so distinct elements with equal hash codes (such as "Aa" and "BB") are indistinguishable, and it is capped at 2^30 bits. |
| Counting Bloom filter | Like a Bloom filter, but items must be removable | O(k) per operation; a small counter per position instead of a bit | not implemented here | Counters can overflow. Removing an item that was never added corrupts the filter. |
| Cuckoo filter | Approximate membership with deletion, in less space than a counting Bloom filter at low false-positive rates | O(1) lookup (two buckets); expected O(1) insert | not implemented here | An insert can fail when the table is nearly full. Deleting an item that was never added can remove another item's fingerprint. |

### Count how often items occur, or how many distinct items there are

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Exact frequency map | The distinct items fit in memory | Expected O(1) per item; O(d) for d distinct items | JDK: `java.util.HashMap#merge`; `java.util.stream.Collectors#groupingBy` with `Collectors#counting` | The spec makes no guarantee about the type or mutability of the map `groupingBy` returns; pass a map factory such as `TreeMap::new` when you need sorted keys. |
| Count-Min sketch | Approximate counts for a huge or unbounded key space | O(d) per update or query for d rows; O(w·d) counters | repo: [CountMinSketch](../src/main/java/io/github/ichillous/dsa/structures/CountMinSketch.java) ([lesson](../data-structures/probabilistic/overview.md)) | With nonnegative increments it overestimates only: an estimate is never below the true count. The error grows with the total count (at most `epsilon x total` with probability `1 - delta` for a fixed item), so rare items are estimated poorly. The repository sketch has no removal, caps the table at 2^25 counters, shares counts between items with equal `hashCode()`, and throws `ArithmeticException` before changing anything if a counter or the total would overflow. |
| Misra–Gries frequent items | Find every item that occurs more than n/k times, in one pass with k − 1 counters | Expected amortized O(1) per item; O(k) | not implemented here | It returns candidates, not exact counts; a second pass must confirm them. Boyer–Moore majority vote is the k = 2 case. |
| HyperLogLog | Estimate the number of distinct items in a stream | O(1) per item; m small registers (kilobytes) | not implemented here | An estimate only, with relative standard error about 1.04/√m; small cardinalities need a correction. Sketches with the same parameters merge. |

```java
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

int[] statusCodes = {200, 404, 200, 500, 200, 404};
Map<Integer, Long> counts = IntStream.of(statusCodes).boxed()
        .collect(Collectors.groupingBy(code -> code, TreeMap::new, Collectors.counting()));
assert counts.equals(Map.of(200, 3L, 404, 2L, 500, 1L));
assert List.copyOf(counts.keySet()).equals(List.of(200, 404, 500));  // TreeMap::new sorts the keys
```

## 5. Range queries over arrays

### Sum over many ranges of an array that does not change

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Prefix sums | A static array with many range-sum queries | O(n) build; O(1) per query as `prefix[to] - prefix[from]`; O(n) | JDK: `java.util.Arrays#parallelPrefix` (in-place cumulation), or a plain loop | Accumulate `int` values in `long`, or use `Math.addExact`. `parallelPrefix` overwrites its input. Subtraction makes this work for sums but not for minimum or maximum. |
| 2D prefix sums | Many rectangle-sum queries on a static grid | O(rows·cols) build; O(1) per query by inclusion–exclusion; O(rows·cols) | not implemented here | Off-by-one errors are common; an extra zero row and column keep the formula uniform. |
| Sqrt decomposition | Simple code for mixed updates and queries when O(√n) per operation is fast enough | O(√n) per query and update; O(n) | not implemented here | Slower than tree structures for large n, but easy to adapt to unusual operations. |

### Update values or ranges while answering range sums

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Fenwick tree (binary indexed tree) | Point updates mixed with prefix or range sums | O(log n) update and query; O(n) build and space | repo: [FenwickTree](../src/main/java/io/github/ichillous/dsa/structures/FenwickTree.java) ([lesson](../data-structures/arrays/operations.md)) | Needs an invertible operation: sum works, minimum does not. The class throws `ArithmeticException` on signed-`long` overflow instead of wrapping. |
| Lazy segment tree (range add, range sum) | Add a value to a whole range and query range sums, interleaved | O(log n) per operation; O(n); O(log n) recursion stack | repo: [LazySegmentTree](../src/main/java/io/github/ichillous/dsa/structures/LazySegmentTree.java) ([lesson](../data-structures/range-queries/overview.md)) | Pending tags must be pushed down, or accounted for, before children are read; the repository's queries carry them down instead, so a query does not modify the tree. Elements are `long`: `rangeAdd` throws `ArithmeticException` and leaves the tree unchanged exactly when some element would leave `long` range, and `rangeSum` throws only when the true sum does. Internally sums and pending additions are exact 128-bit values. At most 2^29 elements. |
| Difference array | Many range additions first, then read every value once | O(1) per range add; O(n) to rebuild the values, so O(n + u) for u updates; O(n) | repo: [ArrayPatterns.applyRangeAdds](../src/main/java/io/github/ichillous/dsa/algorithms/ArrayPatterns.java) ([lesson](../algorithms/array-patterns/overview.md)) | Values are correct only after the final prefix pass, so it cannot answer queries between updates. The repository method returns a new array and leaves its input unchanged; it throws `ArithmeticException` if a final value leaves `long` range (intermediate prefixes may exceed it) and `IndexOutOfBoundsException` for an update past the end. |

### Minimum or maximum over ranges

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Segment tree (point set, range minimum) | Values change between range-minimum queries | O(log n) per operation; O(n) build and space | repo: [SegmentTree.rangeMin](../src/main/java/io/github/ichillous/dsa/structures/SegmentTree.java) ([lesson](../data-structures/trees/overview.md)) | Returns an empty `OptionalLong` for an empty range. It supports minimum only; maximum needs a separate tree. |
| Sparse table | A static array with very many range-minimum queries | O(n log n) build; O(1) per query; O(n log n) | repo: [SparseTable](../src/main/java/io/github/ichillous/dsa/structures/SparseTable.java) ([lesson](../data-structures/range-queries/overview.md)) | Only for idempotent operations (minimum, maximum, gcd), where two overlapping blocks are harmless. Sums need another structure. No updates: the repository table is immutable, so new data needs a rebuild. The repository class answers range minimum only (`rangeMin`, an empty range gives an empty `OptionalLong`), and stores up to n(floor(log2 n) + 1) longs. |
| Monotonic deque (sliding window) | The maximum of every window of a fixed width, left to right | O(n) total; O(w) for width w | repo: [SequenceAlgorithms.slidingWindowMaximum](../src/main/java/io/github/ichillous/dsa/algorithms/SequenceAlgorithms.java) ([lesson](../algorithms/divide-and-conquer/problems.md)) | Only for windows that slide in one direction. |

## 6. Intervals

### Merge overlapping intervals

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Merge intervals (sort by start, then sweep) | Normalize ranges such as booked times or covered IP ranges | O(n log n) for the sort, then O(n); O(n) | repo: [ArrayPatterns.mergeIntervals](../src/main/java/io/github/ichillous/dsa/algorithms/ArrayPatterns.java) ([lesson](../algorithms/array-patterns/overview.md)) | Decide whether touching intervals such as [1, 3) and [3, 5) merge. With half-open intervals they share no point, but the repository method does merge them, giving [1, 5). It takes non-empty `GreedyAlgorithms.Interval` values, returns an immutable list sorted by start, and leaves its input unchanged. |

### Find how many intervals overlap at once (rooms, servers, peak load)

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Minimum meeting rooms (interval partitioning) | The fewest resources so that no two overlapping intervals share one | O(n log n); O(n) | repo: [ArrayPatterns.minimumMeetingRooms](../src/main/java/io/github/ichillous/dsa/algorithms/ArrayPatterns.java) ([lesson](../algorithms/array-patterns/overview.md)) | The answer equals the maximum overlap depth. The repository method sorts starts and ends separately and processes an end before a start at the same time, so [1, 2) and [2, 3) share a room. Empty input gives 0. |
| Sweep line over endpoint events | Peak concurrency, total covered length, or any count that changes only at endpoints | O(n log n) to sort 2n events; O(n) | not implemented here (a JDK building block is `java.util.TreeMap#merge`, to sum changes per coordinate) | The order of events at equal coordinates decides whether touching intervals overlap. |

### Choose a set of non-overlapping intervals

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Activity selection (earliest finish first) | The largest number of non-overlapping intervals, all equally valuable | O(n log n); O(n) | repo: [GreedyAlgorithms.selectActivities](../src/main/java/io/github/ichillous/dsa/algorithms/GreedyAlgorithms.java) ([lesson](../algorithms/greedy/overview.md)) | Wrong when intervals carry different values; use weighted interval scheduling. Touching half-open intervals are compatible. |
| Weighted interval scheduling | Non-overlapping intervals with the largest total weight | O(n log n): sort by end, binary search for the last compatible interval, then DP; O(n) | repo: [DynamicProgrammingClassics.weightedIntervalScheduling](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgrammingClassics.java) ([lesson](../algorithms/dynamic-programming/classic-problems.md)) | Greedy by weight or by finish time is wrong here. Keep totals in `long`: the repository method uses checked sums and throws `ArithmeticException` on overflow. Intervals are half-open with nonnegative weights. When totals tie the later-sorted interval is skipped, so zero-weight intervals are never chosen. |

### Find the stored intervals that overlap a query

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Interval tree | A changing set of intervals; report all that overlap a point or range | O(log n) to find one overlap; O(min(n, k log n)) to report all k overlaps; O(log n) per update; O(n) | not implemented here | A balanced BST keyed by start, where each node also stores the largest end in its subtree. A centered interval tree (static) or a priority search tree reports all k overlaps in O(log n + k). This differs from the repository's segment tree, whose intervals are array positions. |
| Disjoint interval map | Intervals never overlap, such as allocations or reservations | O(log n) lookup and insert | JDK: `java.util.TreeMap#floorEntry`, keyed by start | Before inserting, check the floor entry's end and the next entry's start, or overlaps creep in. |

## 7. Stacks, queues, deques and priority

### Last in, first out (stack) or first in, first out (queue)

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Array deque | The default stack or queue in single-threaded code | Spec: most operations run in amortized constant time; O(n) | JDK: `java.util.ArrayDeque` | The spec: "likely to be faster than `Stack` when used as a stack, and faster than `LinkedList` when used as a queue". No capacity restrictions. Null elements are prohibited. `remove(Object)` and `contains` are linear. Use one end consistently: `push`/`pop` for a stack, `offer`/`poll` for a queue. |
| Linked-list deque | You need null elements, or `ListIterator` edits in the middle | O(1) at both ends; O(n) indexed access | JDK: `java.util.LinkedList` | One node per element. Indexed access walks from the nearer end. |
| Legacy stack | Only when an old API demands it | Synchronized `Vector` methods | JDK: `java.util.Stack` | Its Javadoc recommends a `Deque` implementation instead. |
| Array stack, circular queue and ring deque | To see resizing and wraparound in source you can step through | O(1) amortized add; O(1) remove and peek | repo: [ArrayStack](../src/main/java/io/github/ichillous/dsa/collections/ArrayStack.java) ([lesson](../data-structures/stacks/implementation.md)), [ArrayQueue](../src/main/java/io/github/ichillous/dsa/collections/ArrayQueue.java) and [RingDeque](../src/main/java/io/github/ichillous/dsa/collections/RingDeque.java) ([lesson](../data-structures/queues/implementation.md)) | Removing from an empty structure throws. |

```java
import java.util.ArrayDeque;
import java.util.Deque;

Deque<String> stack = new ArrayDeque<>();
stack.push("a");
stack.push("b");
assert stack.pop().equals("b");          // last in, first out

Deque<String> queue = new ArrayDeque<>();
queue.offer("a");
queue.offer("b");
assert queue.poll().equals("a");         // first in, first out
assert queue.poll().equals("b");
assert queue.poll() == null;             // empty: poll returns null, while pop would throw

boolean rejected = false;
try {
    queue.offer(null);
} catch (NullPointerException expected) {
    rejected = true;
}
assert rejected;                         // null elements are prohibited
```

### Always take the smallest (or largest) item next

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Binary heap priority queue | Repeatedly remove the minimum by natural order or a comparator | OpenJDK implementation note, stated in the Javadoc: O(log n) `offer`, `poll`, `remove()` and `add`; linear `remove(Object)` and `contains(Object)`; constant `peek`, `element` and `size`. O(n) | JDK: `java.util.PriorityQueue` | Ties are broken arbitrarily; add a sequence number for first-in-first-out ties. The iterator does not visit elements in priority order. Nulls are not permitted. Never compare with `(a, b) -> a - b`: subtraction overflows. |
| Binary min-heap | A natural-order minimum with sift-up and sift-down code to read | O(log n) add (amortized for growth) and remove; O(1) peek; O(n) bottom-up build | repo: [BinaryMinHeap](../src/main/java/io/github/ichillous/dsa/structures/BinaryMinHeap.java) ([lesson](../data-structures/heaps/overview.md)) | Natural order only, no comparator. Duplicates are kept. |
| Lazy deletion | Priorities change, as in Dijkstra, and the heap has no decrease-key | O(log m) per push or pop for m stored entries | repo: [GraphAlgorithms.dijkstra](../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java) uses it ([lesson](../algorithms/graph/bfs.md)); JDK: `java.util.PriorityQueue` | Push a new entry on every change and skip popped entries that are stale. The heap holds one entry per update, not per item. |
| Indexed priority queue | Many decrease-key operations on items with integer IDs | O(log n) insert, decrease-key and remove-min; O(n) | not implemented here | A position array maps each ID to its heap slot, and every swap must update it. |
| d-ary heap | Many inserts or decrease-keys and fewer remove-min calls, as in Dijkstra on dense graphs | O(log_d n) insert and decrease-key; O(d·log_d n) remove-min | not implemented here | A larger d makes remove-min more expensive. |
| Pairing heap | Fast meld (merging two heaps) and cheap decrease-key in practice | O(log n) amortized remove-min; insert and meld link two roots; decrease-key is o(log n) amortized, but not constant | not implemented here | Pointer-based with amortized bounds only; tight bounds for decrease-key are still a research question. |

### Find the next greater element or the largest rectangle

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Monotonic stack (next greater element) | For each position, the nearest later element that is strictly greater (stock spans, temperatures) | O(n); O(n) | repo: [ArrayPatterns.nextGreaterIndices](../src/main/java/io/github/ichillous/dsa/algorithms/ArrayPatterns.java) ([lesson](../algorithms/array-patterns/overview.md)) | Each index is pushed and popped once. Equal values do not count as greater in the repository method, which returns the index of the next greater element or `-1`. |
| Monotonic stack (largest rectangle in histogram) | The largest rectangle under a bar chart, or the largest all-ones submatrix row by row | O(n); O(n) | repo: [ArrayPatterns.largestRectangle](../src/main/java/io/github/ichillous/dsa/algorithms/ArrayPatterns.java) ([lesson](../algorithms/array-patterns/overview.md)) | Height times width overflows `int`; the repository method computes the area in `long`. Negative heights throw `IllegalArgumentException`; empty input gives 0. |

### Maximum or minimum of every window of a fixed width

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Monotonic deque | Moving maximum or minimum over a stream or array | O(n) total; O(w) | repo: [SequenceAlgorithms.slidingWindowMaximum](../src/main/java/io/github/ichillous/dsa/algorithms/SequenceAlgorithms.java) ([lesson](../algorithms/divide-and-conquer/problems.md)) | The deque holds indices whose values decrease; an older, smaller candidate can never win again. The width must be between 1 and n. |

### Merge k sorted lists

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| K-way merge (heap of list heads) | Combine k sorted sources, such as files, shards or sorted runs | O(N log k) for N items in total; O(k) heap beyond the result | repo: [Selection.mergeSorted](../src/main/java/io/github/ichillous/dsa/algorithms/Selection.java) ([lesson](../algorithms/searching/selection.md)) | Break ties by source index if equal items must stay in source order; the repository method does, so its result equals a stable sort of the concatenation. It checks each list as it consumes it and throws `IllegalArgumentException` for an unsorted list. |
| Concatenate and stable sort | Small inputs, or the simplest code | O(N log N) | JDK: `java.util.List#sort` | OpenJDK implementation note, stated in the Javadoc: the sort "is well-suited to merging two or more sorted arrays: simply concatenate the arrays and sort the resulting array". |

## 8. Caching and eviction

### Keep the most recently used entries (LRU)

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Access-order linked hash map | A simple single-threaded LRU cache | Spec: constant-time basic operations assuming good hash dispersion; O(capacity) | JDK: `java.util.LinkedHashMap` constructor `(int, float, boolean)` with access order `true`, plus `removeEldestEntry` | It iterates from least to most recently accessed. `get`, `put`, `getOrDefault`, `compute`, `merge` and related methods count as accesses; collection-view operations do not. `removeEldestEntry` runs after `put` and `putAll`, not on `get`. Not thread safe: in access order, the spec counts a `get` as a structural modification. |
| LRU cache (hash map + doubly linked list) | To see the mechanism, or a fixed-capacity cache with this repository's non-null contract | Expected O(1) `get` and `remove`; expected amortized O(1) `put`; O(capacity) | repo: [LruCache](../src/main/java/io/github/ichillous/dsa/collections/LruCache.java) ([lesson](../data-structures/hash-tables/overview.md)) | The capacity must be positive. A replacing `put` also counts as a use. |

```java
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

Map<String, Integer> cache = new LinkedHashMap<>(16, 0.75f, true) {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
        return size() > 2;                 // keep at most two entries
    }
};
cache.put("a", 1);
cache.put("b", 2);
assert cache.get("a") == 1;               // "a" becomes the most recently accessed
cache.put("c", 3);                        // evicts "b", the least recently accessed
assert List.copyOf(cache.keySet()).equals(List.of("a", "c"));
```

### Evict the least frequently used entry

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| LFU cache | Long-term popularity matters more than recency, such as hot keys behind a scan-heavy workload | Expected O(1) per operation with frequency buckets and a tracked minimum; O(capacity) | repo: [LfuCache](../src/main/java/io/github/ichillous/dsa/collections/LfuCache.java) ([lesson](../data-structures/hash-tables/lfu-cache.md)) | Items that were popular long ago can stay forever ("stale popularity"); decaying or windowed counts fix that. The repository cache evicts the lowest frequency and, among equal counts, the least recently used entry. An evicted or removed key forgets its history and restarts at frequency 1. Counts saturate at `Integer.MAX_VALUE`. Even `get` changes state, so the cache is not safe to share between threads. |

### Approximate LRU, time-based expiry and memory pressure

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| CLOCK (second chance) | LRU-like eviction where hits must be cheap, as in page caches | O(1) per hit; an eviction sweeps until it finds an unreferenced slot | not implemented here | A hit only sets a bit, so hits are cheaper than moving list nodes. The worst-case sweep visits every slot. |
| Time-to-live (TTL) cache | Entries must expire a fixed time after insertion, regardless of use | Expected O(1) lookup plus the expiry work | not implemented here (a JDK building block is `java.util.concurrent.DelayQueue`, whose head is the element whose delay expired furthest in the past) | Check expiry on read or sweep periodically, or expired entries still hold memory. Measure durations with `System.nanoTime`, which the spec reserves for elapsed time; wall-clock time can jump. |
| Memory-sensitive cache | Values may be dropped when memory runs low | As the underlying map | JDK: `java.lang.ref.SoftReference` | The spec: soft references are "cleared at the discretion of the garbage collector in response to memory demand" and are "most often used to implement memory-sensitive caches". You do not control when they are cleared. |

## 9. Searching

### Find a value or its insertion point in sorted data

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Binary search | A sorted array or random-access list | O(log n); O(1) | repo: [SearchAlgorithms.binarySearch](../src/main/java/io/github/ichillous/dsa/algorithms/SearchAlgorithms.java) ([lesson](../algorithms/searching/binary-search.md)); JDK: `java.util.Arrays#binarySearch`, `java.util.Collections#binarySearch` | When the key is absent, the JDK methods return `-(insertion point) - 1`. With duplicates, the JDK makes no guarantee which match it finds; the repository's `binarySearch` returns the first, or `-1`. Unsorted input gives undefined results. Spec: `Collections.binarySearch` on a large list that is not `RandomAccess` does O(n) link traversals. |
| Lower and upper bound | The first index whose value is at least x, or greater than x; the count of x is the difference | O(log n); O(1) | repo: [SearchAlgorithms.lowerBound](../src/main/java/io/github/ichillous/dsa/algorithms/SearchAlgorithms.java) and `upperBound` ([lesson](../algorithms/searching/binary-search.md)) | Ascending order is assumed, not checked. Results lie in `[0, n]`. |
| Linear search | Unsorted or tiny data, or a single query | O(n); O(1) | repo: [SearchAlgorithms.linearSearch](../src/main/java/io/github/ichillous/dsa/algorithms/SearchAlgorithms.java) ([lesson](../algorithms/searching/linear-search.md)) | Sorting first pays off only for many queries. |
| Exponential (galloping) search | Sorted data of unknown or unbounded length, or a target likely near the front | O(log i) for a target at position i; O(1) | not implemented here | Double the probe index until it passes the target, then binary search the last gap. Doubling an `int` index can overflow. |
| Interpolation search | Sorted numeric keys spread almost uniformly | O(log log n) expected on uniform keys; O(n) worst | not implemented here | Skewed data makes it linear. Compute the probe position in `long` or `double` to avoid overflow. |

```java
import java.util.Collections;
import java.util.List;

List<Integer> sorted = List.of(10, 20, 30, 40);
assert Collections.binarySearch(sorted, 30) == 2;
int result = Collections.binarySearch(sorted, 25);   // absent
assert result == -3;                                 // -(insertion point) - 1
int insertionPoint = -result - 1;
assert insertionPoint == 2;                          // 25 belongs before 30
assert Collections.binarySearch(sorted, 99) == -5;   // insertion point == size()
```

### Find the smallest value that makes a condition true

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Binary search on the answer | A yes/no test is monotone in the answer: the smallest capacity, the largest minimum gap, the earliest day | O(log R) tests over an answer range of size R | not implemented here | Prove the test is monotone first. Compute the midpoint as `lo + (hi - lo) / 2` (or with `>>>` on nonnegative values) so it cannot overflow. |

### Find the peak of a unimodal function

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Ternary search | A function that strictly increases and then strictly decreases | O(log R) evaluations | not implemented here | Flat stretches (equal values) break it. On integers, a binary search on the sign of f(m + 1) - f(m) is simpler. |

## 10. Selection and top-k

### Find the k-th smallest item (median, percentile)

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Quickselect (k-th smallest) | One order statistic of data held in memory | Repository: O(n) worst case (introselect); O(n) extra for a copy plus O(log n) recursion in the fallback. Classic in-place quickselect: O(n) expected, O(n²) worst with a fixed pivot rule, O(1) extra | repo: [Selection.kthSmallest](../src/main/java/io/github/ichillous/dsa/algorithms/Selection.java) ([lesson](../algorithms/searching/selection.md)) | A classic in-place quickselect reorders the array; the repository method copies its input and leaves it unchanged. It is introselect: a median-of-three quickselect with three-way partitioning that switches to median-of-medians pivots when a round fails to shrink the segment enough, which guarantees O(n) worst case. Duplicates count separately; `k` outside `[0, n)` throws `IndexOutOfBoundsException`. |
| Median of medians | You need a guaranteed linear worst case | O(n) worst, with a larger constant | repo: used as the fallback pivot rule inside [Selection.kthSmallest](../src/main/java/io/github/ichillous/dsa/algorithms/Selection.java) ([lesson](../algorithms/searching/selection.md)); there is no standalone method | Usually used only as the fallback inside introselect, which is how the repository uses it. |
| Sort, then index | A few order statistics, or the sorted data is useful anyway | O(n log n) | JDK: `java.util.Arrays#sort` | Sorts in place. |

### Keep the k largest items from a stream

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Bounded min-heap of size k | Streaming input with k much smaller than n | O(n log k); O(k) | JDK: `java.util.PriorityQueue` ([lesson](../data-structures/queues/priority-queues.md)); repo: [Selection.smallestK](../src/main/java/io/github/ichillous/dsa/algorithms/Selection.java) ([lesson](../algorithms/searching/selection.md)) | Keep the k largest in a **min**-heap, so its head is the weakest kept item. `PriorityQueue` breaks ties arbitrarily, and its iterator order is unspecified; sort the result if you need order. The repository method keeps the k smallest under a `Comparator` (pass `Comparator.reverseOrder()` for the k largest), returns them sorted under it with equal elements in encounter order, and takes a collection rather than a one-pass stream. |
| Sort, then slice | Small n, or the result must also be sorted | O(n log n); O(n) | JDK: `java.util.stream.Stream#sorted`, then `#limit` | Holds all n items. The spec: for ordered streams, the sort is stable. |
| Count-Min sketch plus heap | Approximate heavy hitters over an unbounded key space | O(d + log k) per item | repo: [CountMinSketch.estimate](../src/main/java/io/github/ichillous/dsa/structures/CountMinSketch.java) supplies the counts ([lesson](../data-structures/probabilistic/overview.md)); the heap of candidates is not provided | With nonnegative increments, counts are overestimates, so items near the threshold can be wrongly included. |

```java
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

int k = 3;
PriorityQueue<Integer> kept = new PriorityQueue<>();  // min-heap: head is the weakest kept value
for (int latency : new int[] {12, 87, 5, 87, 40, 3, 99}) {
    kept.offer(latency);
    if (kept.size() > k) {
        kept.poll();                                  // drop the smallest
    }
}
List<Integer> largest = new ArrayList<>(kept);
largest.sort(null);                                   // iteration order is unspecified
assert largest.equals(List.of(87, 87, 99));
```

### Find the majority element (more than half of the items)

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Boyer–Moore majority vote | One pass with O(1) memory | O(n); O(1) | repo: [Selection.majorityElement](../src/main/java/io/github/ichillous/dsa/algorithms/Selection.java) ([lesson](../algorithms/searching/selection.md)) | The vote pass alone always returns a candidate, so a second pass must check that the candidate occurs more than n/2 times. The repository method does both passes and returns an empty `OptionalInt` when no value is a strict majority (and for an empty array). |
| Hash counting | You also need the other counts | Expected O(n); O(d) for d distinct values | JDK: `java.util.HashMap#merge` | Memory grows with the number of distinct values. |

## 11. Sorting

### Sort an array of primitives

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| JDK primitive sort | The default for `int[]`, `long[]` and `double[]` | OpenJDK implementation note, stated in the Javadoc: a Dual-Pivot Quicksort that "offers O(n log(n)) performance on all data sets" | JDK: `java.util.Arrays#sort(int[])` | Not stable: the spec promises stability only for object sorts. Equal primitives are indistinguishable, so this matters when you sort records by a primitive key; sort the records (or boxed indices) with a stable object sort instead. Sorts in place. The spec orders `double[]` by `Double.compareTo`: -0.0 before 0.0, and NaN last. |
| Parallel sort | Very large arrays on a multi-core machine | O(n log n) work spread over threads | JDK: `java.util.Arrays#parallelSort` | Small arrays gain nothing. The object overloads are guaranteed stable. OpenJDK implementation note (stated in the Javadoc for the object overloads): work runs in the ForkJoin common pool. |
| Insertion sort | Tiny or nearly sorted arrays | O(n²) worst; O(n) when already sorted; O(1) | repo: [SortAlgorithms.insertionSort](../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java) ([lesson](../algorithms/sorting/bubble-sort.md)) | Stable. Quadratic on reversed input. |
| Bubble sort | Teaching only | O(n²) worst; O(n) when already sorted, with an early exit; O(1) | repo: [SortAlgorithms.bubbleSort](../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java) ([lesson](../algorithms/sorting/bubble-sort.md)) | Stable but slower than insertion sort in practice. |
| Merge sort | Guaranteed O(n log n) with stability | O(n log n) worst; O(n) buffer | repo: [SortAlgorithms.mergeSort](../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java) ([lesson](../algorithms/sorting/merge-sort.md)) | Needs the extra buffer. |
| Three-way quicksort | In-place sorting with many duplicate values | O(n log n) typical; O(n²) worst; O(log n) stack | repo: [SortAlgorithms.quickSort](../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java) ([lesson](../algorithms/sorting/quick-sort.md)) | Not stable. A deterministic pivot rule has quadratic inputs. |
| Heap sort | A worst-case O(n log n) bound with O(1) extra space | O(n log n) worst; O(1) | repo: [SortAlgorithms.heapSort](../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java) ([lesson](../algorithms/sorting/heap-sort.md)) | Not stable; poor cache locality. |
| Counting sort | Integer keys within a small range r | O(n + r); O(r) | repo: [SortAlgorithms.countingSort](../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java) ([lesson](../algorithms/sorting/non-comparison-and-comparator-sorts.md)) | Memory grows with the range, not the count. Compute `max - min + 1` in `long`; in `int` it can overflow. The repository method sorts an `int[]` in place, computes the range in `long`, and throws `IllegalArgumentException` (leaving the array unchanged) when the range exceeds 2^20 values, which caps the count table at 4 MiB. |
| LSD radix sort | Many fixed-width integer keys | O(w·(n + b)) for w digit passes in base b; O(n + b). The repository method uses w = 4 passes in base 256, so O(n) time and an O(n) buffer | repo: [SortAlgorithms.radixSort](../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java) ([lesson](../algorithms/sorting/non-comparison-and-comparator-sorts.md)) | Every pass must be stable. Flip the sign bit so negative `int` values sort first; the repository method does this and accepts the full `int` range, sorting an `int[]` in place. |
| Bucket sort | Keys spread roughly uniformly over a known range | O(n) expected for uniform keys; O(n²) worst with insertion-sorted buckets | not implemented here | Skewed keys pile into one bucket. |
| Shell sort | Short in-place code that beats insertion sort on medium arrays | Depends on the gaps: O(n²) worst with Shell's halving gaps, O(n^(3/2)) with Knuth's 3h + 1 gaps | not implemented here | Not stable. The worst-case bound depends entirely on the gap sequence. |

### Sort objects by a key, keeping equal keys in their original order

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Timsort (the JDK's stable object sort) | The default for object arrays and lists | O(n log n). OpenJDK implementation note, stated in the Javadoc: an adaptive merge sort, "adapted from Tim Peters's list sort", needing about n comparisons on nearly sorted input and up to n/2 temporary references | JDK: `java.util.Arrays#sort(Object[])`, `java.util.Arrays#sort(T[], Comparator)`, `java.util.List#sort`, `java.util.Collections#sort` | Spec: guaranteed stable, so equal elements are not reordered. It may throw `IllegalArgumentException` if a comparator breaks its contract. Build comparators with `Comparator.comparing` and `thenComparing`; never subtract. |
| Stable Comparator merge sort | A stable sort you can read and step through, with this repository's null policy | O(n log n) comparisons; O(n) copy plus O(log n) recursion | repo: [SortAlgorithms.mergeSort(T[], Comparator)](../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java) ([lesson](../algorithms/sorting/non-comparison-and-comparator-sorts.md)) | Stability comes from taking the left element on ties during the merge. The repository method sorts the caller's array in place and rejects a null array, comparator or element (checked before anything moves). Write comparators with `Integer.compare`, never subtraction. |
| Sort by several keys | Order by a secondary key within ties of the primary key | One sort with a composite comparator | JDK: `java.util.Comparator#thenComparing` | Equivalently, sort by the secondary key and then stably by the primary key. |

```java
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

record Order(String id, int priority) {}

Order[] orders = {
    new Order("a", 2), new Order("b", 1), new Order("c", 2), new Order("d", 1)
};
Arrays.sort(orders, Comparator.comparingInt(Order::priority));
// Equal priorities keep their original relative order: b before d, a before c.
assert Arrays.stream(orders).map(Order::id).toList().equals(List.of("b", "d", "a", "c"));
```

### Sort data that does not fit in memory

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| External merge sort | The data is larger than memory and lives on disk | O(n log n) comparisons; about (n/B)·log_(M/B)(n/B) block transfers for block size B and memory M | not implemented here | Sort memory-sized runs, write them out, then k-way merge the runs. Open files and buffer memory limit how many runs merge at once. |

## 12. Array and sequence patterns

### Find a pair with a given sum

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Two pointers (pair with target sum) | The array is sorted | O(n); O(1) | repo: [ArrayPatterns.pairWithSum](../src/main/java/io/github/ichillous/dsa/algorithms/ArrayPatterns.java) ([lesson](../algorithms/array-patterns/overview.md)) | Sum in `long`: two large `int` values overflow (the repository method does). Requires sorted input, which it does not check: an unsorted array gives an unspecified answer. It returns an `Optional` index pair: the smallest first index that has a partner, with that index's largest partner. |
| Hash complement lookup | Unsorted input in one pass | Expected O(n); O(n) | JDK: `java.util.HashMap` | Look up the complement before inserting the current value, or x + x = target matches the element with itself. |

### Find the longest (or shortest) window that satisfies a condition

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Variable-size sliding window (longest substring without repeats) | Shrinking a valid window keeps it valid, as with "no repeated character" or "sum at most S" over nonnegative values | O(n) with an array over a small alphabet; expected O(n) with a hash map | repo: [ArrayPatterns.longestSubstringWithoutRepeats](../src/main/java/io/github/ichillous/dsa/algorithms/ArrayPatterns.java) ([lesson](../algorithms/array-patterns/overview.md)) | With negative values a sum condition is not monotone; use prefix sums with a hash map instead. The repository method works on UTF-16 code units (a supplementary character counts as two), returns the earliest longest window as a half-open `Range`, and gives `Range(0, 0)` for the empty string. |
| Prefix sums with a hash map | Count or find subarrays with a given sum, negative values allowed | Expected O(n); O(n) | JDK: `java.util.HashMap#merge` | Seed the map with prefix sum 0 seen once. Sum in `long`. |

### Find the contiguous subarray with the largest sum

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Kadane's algorithm | One pass over values of any sign | O(n); O(1) | repo: [SequenceAlgorithms.maximumSubarray](../src/main/java/io/github/ichillous/dsa/algorithms/SequenceAlgorithms.java) ([lesson](../algorithms/divide-and-conquer/problems.md)) | Nonempty answers only: an empty array gives an empty `Optional`, and an all-negative array gives its largest element. The sum is a `long`. |

### Count inversions (how far from sorted?)

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Merge-sort inversion counting | Ranking distance, or measuring disorder | O(n log n); O(n) | repo: [SequenceAlgorithms.countInversions](../src/main/java/io/github/ichillous/dsa/algorithms/SequenceAlgorithms.java) ([lesson](../algorithms/divide-and-conquer/problems.md)) | The count reaches n(n − 1)/2, so it is a `long`. Equal values are not inversions. |
| Fenwick tree inversion counting | Values are small integers, or can be compressed to ranks | O(n log n); O(n) | repo: [FenwickTree](../src/main/java/io/github/ichillous/dsa/structures/FenwickTree.java) ([lesson](../data-structures/arrays/operations.md)) | Compress values to ranks first; count, for each value, how many larger values came before it. |

### Partition into three groups around a pivot

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Dutch national flag (three-way partitioning) | Sort values 0, 1 and 2, or split into less, equal and greater than a pivot in one pass | O(n); O(1) | not implemented here as a public method (the same loop is private inside [SortAlgorithms.quickSort](../src/main/java/io/github/ichillous/dsa/algorithms/SortAlgorithms.java)) | Not stable. Keep four regions: less, equal, unscanned and greater. |

### Detect a cycle in a sequence or a linked list

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Floyd's cycle detection (tortoise and hare) | Iterating x → f(x), or following `next` links, with O(1) memory | O(μ + λ) steps for tail length μ and cycle length λ; O(1) | not implemented here | The first phase shows a cycle exists; a second phase finds where it starts. |
| Visited set | Memory is available and the code should be obvious | Expected O(μ + λ); O(μ + λ) | JDK: `java.util.HashSet` | Values need correct `equals` and `hashCode`. For node identity, use a set backed by `IdentityHashMap` (`Collections.newSetFromMap`). |

## 13. String matching and text

### Find one pattern in a text

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Built-in substring search | Occasional searches | The spec defines only the result: the smallest k for which `startsWith(str, k)` holds. OpenJDK implementation note: a direct scan, O(n·m) worst case | JDK: `java.lang.String#indexOf(String)` | Returns `-1` when absent. Positions are UTF-16 code units. Inputs such as "aaaa…ab" make a direct scan quadratic. |
| Knuth–Morris–Pratt (KMP) | Guaranteed linear time, or every overlapping match | O(n + m); O(m) | repo: [StringAlgorithms.kmpIndexOf](../src/main/java/io/github/ichillous/dsa/algorithms/StringAlgorithms.java) and `kmpFindAll` ([lesson](../algorithms/searching/linear-search.md)) | The empty pattern matches at every boundary `0..n` in `kmpFindAll`. |
| Z-algorithm | Linear-time matching, or the longest prefix match at every position | O(n + m); O(n + m) | repo: [StringAlgorithms.zFunction](../src/main/java/io/github/ichillous/dsa/algorithms/StringAlgorithms.java) and `zFindAll` ([lesson](../algorithms/strings/pattern-matching.md)) | `zFindAll` runs it on pattern + text and needs no separator character (a match is a position whose z value reaches the pattern length), so it uses O(n + m) space for the concatenation. `zFunction` defines `z[0]` as the text length. Overlapping matches are reported, and an empty pattern matches every boundary `0..n`, as in `kmpFindAll`. |
| Rabin–Karp (verified matches) | A rolling hash: many patterns of one length, or two-dimensional matching | O(n + m) hashing plus O(m) to verify each hash hit: O(n + m + z·m) for z matches when spurious hits are rare; O(n·m) worst (many true matches, or crafted collisions against a fixed base and modulus); O(1) extra besides the output | repo: [StringAlgorithms.rabinKarpFindAll](../src/main/java/io/github/ichillous/dsa/algorithms/StringAlgorithms.java) ([lesson](../algorithms/strings/pattern-matching.md)) | A hash hit is only a candidate. Compare the characters to confirm it, or you report false matches; the repository method always verifies. A random base chosen at run time is what makes spurious hits unlikely for every input; the repository method uses a fixed base 257 and modulus 2^31 - 1, so crafted input can force the O(n·m) worst case. |
| Boyer–Moore–Horspool | Long patterns over a large alphabet, where skipping ahead pays off | Sublinear on typical text; O(n·m) worst | not implemented here | The skip table is indexed by character; for UTF-16 use a map, or hash characters into a small table. |
| Regular expressions | The pattern has structure: classes, repetition, alternation | Depends on the pattern | JDK: `java.util.regex.Pattern` | OpenJDK implementation note: the matcher backtracks, so some patterns slow down sharply on input that almost matches; `(.*a){6}x` against a long run of `a` takes time growing like a high power of the length. Limit input length when the pattern or the input is untrusted. Compile once and reuse the `Pattern`; use `Pattern.quote` for literal text. |

```java
String log = "GET /a 200; GET /b 404; GET /c 404";
assert log.indexOf("404") == 19;
assert log.indexOf("404", 20) == 31;     // continue after the previous match
assert log.indexOf("500") == -1;
assert "abc".indexOf("") == 0;           // the empty string occurs at index 0
```

### Find many patterns at once

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Aho–Corasick | Many fixed patterns (keyword lists, filters) in one pass over the text | O(total pattern length) expected build (hash-map transitions); O(t + k) expected search for text length t and k matches | repo: [AhoCorasick](../src/main/java/io/github/ichillous/dsa/algorithms/AhoCorasick.java) ([lesson](../algorithms/strings/multi-pattern-and-suffix-arrays.md)) | The output can be large when patterns overlap heavily. The repository automaton reports every occurrence, overlaps included, ordered by start and then pattern length. Duplicate patterns collapse, an empty pattern is rejected, and the automaton is immutable, so it is safe to share between threads. |
| One regular expression with alternatives | A few patterns and the simplest code | Depends on the pattern | JDK: `java.util.regex.Pattern` | OpenJDK implementation note: alternatives are tried one after another at each position, so the cost grows with the number of patterns. Quote literals with `Pattern.quote`. |

### Find the longest palindromic substring

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Manacher (longest palindromic substring) | Linear time | O(n); O(n) | repo: [StringAlgorithms.longestPalindromicSubstring](../src/main/java/io/github/ichillous/dsa/algorithms/StringAlgorithms.java) ([lesson](../algorithms/strings/pattern-matching.md)) | Decide whether you mean UTF-16 code units or code points, and use one consistently. The repository method judges code units, so a surrogate pair is two units. Ties go to the earliest start, and the empty text gives `Span(0, 0)`. |
| Expand around center | Short strings and the simplest code | O(n²); O(1) | not implemented here | Try both odd-length and even-length centers. |

### Compare or align two strings

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Edit distance (Levenshtein) | Typos, fuzzy matching, the cost of a diff | O(mn); O(min(m, n)) | repo: [DynamicProgramming.editDistance](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java) ([lesson](../algorithms/dynamic-programming/problems.md)) | Unit costs only, and the distance only; see [reconstruction](#recover-the-actual-solution-not-just-its-value) for the edit script. |
| Longest common subsequence (LCS) | Similarity of sequences; the basis of line diffs | O(mn); O(min(m, n)) | repo: [DynamicProgramming.longestCommonSubsequenceLength](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java) ([lesson](../algorithms/dynamic-programming/problems.md)) | Gives the length only. A subsequence need not be contiguous. |
| First mismatch (common prefix length) | Where two arrays first differ | O(p) for common prefix length p | JDK: `java.util.Arrays#mismatch` | Returns `-1` for equal arrays, and the shorter length when one is a proper prefix of the other. For strings, compare `toCharArray()` copies or use `String#regionMatches`. |

```java
import java.util.Arrays;

int[] expected = {1, 2, 3, 4};
int[] actual = {1, 2, 9, 4};
assert Arrays.mismatch(expected, actual) == 2;              // first differing index
assert Arrays.mismatch(expected, expected.clone()) == -1;   // equal contents
assert Arrays.mismatch(new int[] {1, 2}, expected) == 2;    // a proper prefix: the shorter length
```

### Hash text to compare substrings quickly

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Polynomial rolling hash (fingerprints) | Compare substrings in O(1) after O(n) preprocessing; Rabin–Karp; duplicate detection | O(n) build; O(1) per substring hash; O(n) | not implemented here as a reusable API (the rolling hash is private inside [StringAlgorithms.rabinKarpFindAll](../src/main/java/io/github/ichillous/dsa/algorithms/StringAlgorithms.java)) | Collisions always exist: confirm equal hashes by comparing, or use two moduli. The spec fixes `String.hashCode` as s[0]·31^(n−1) + … + s[n−1] in `int` arithmetic, so collisions are easy to construct; never treat it as an identity, and use a random base against untrusted input. |

```java
assert "Aa".hashCode() == "BB".hashCode();   // 65·31 + 97 == 66·31 + 66 == 2112
assert !"Aa".equals("BB");                   // equal hashes, different strings
```

## 14. Graph traversal and structure

### Visit everything reachable from a start vertex

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Breadth-first search (BFS) | Level order; fewest edges from the start | O(V + E) | repo: [GraphAlgorithms.breadthFirst](../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java) ([lesson](../algorithms/graph/bfs.md)) | Mark a vertex when you enqueue it, not when you dequeue it, or it is queued many times. |
| Depth-first search (DFS), iterative | Deep exploration; the basis of many structural algorithms | O(V + E) | repo: [GraphAlgorithms.depthFirst](../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java) ([lesson](../algorithms/graph/dfs.md)) | A recursive DFS overflows the Java stack on long paths; this one keeps an explicit stack. |

### Group vertices into connected components

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Traversal components | A static undirected graph | O(V + E) | repo: [GraphAlgorithms.connectedComponents](../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java) ([lesson](../algorithms/graph/bfs.md)) | Undirected graphs only; directed graphs need strongly connected components. |
| Union-find (disjoint sets) | Edges arrive over time and you ask "are these connected?" | O(α(n)) amortized per operation | repo: [UnionFind](../src/main/java/io/github/ichillous/dsa/structures/UnionFind.java) ([lesson](../data-structures/graphs/overview.md)) | Edges cannot be removed. See [disjoint sets](#21-disjoint-sets-and-bitsets). |

### Order tasks that depend on each other

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Topological sort (Kahn) | Build or job ordering in a directed acyclic graph | O(V + E) | repo: [GraphAlgorithms.topologicalSort](../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java) ([lesson](../algorithms/graph/dfs.md)) | It rejects a cycle but does not show it; see directed cycle with witness below. Many valid orders can exist. |

### Find groups of mutually reachable vertices

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Strongly connected components (Kosaraju) | Directed graphs: cyclic dependency groups, or collapsing cycles into a DAG | O(V + E) | repo: [AdvancedGraphAlgorithms.stronglyConnectedComponents](../src/main/java/io/github/ichillous/dsa/graphs/AdvancedGraphAlgorithms.java) ([lesson](../algorithms/graph/dfs.md)) | Components are sorted and ordered by their smallest vertex. |
| Tarjan's SCC algorithm | One DFS pass instead of two | O(V + E) | not implemented here | A recursive version overflows the stack on deep graphs. |

### Find single points of failure

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Bridges (lowlink) | Edges whose removal disconnects an undirected graph | O(V + E) lowlink; the repository method also sorts the b bridges, O(V + E + b log(b + 1)) | repo: [AdvancedGraphAlgorithms.bridges](../src/main/java/io/github/ichillous/dsa/graphs/AdvancedGraphAlgorithms.java) ([lesson](../algorithms/graph/dfs.md)) | `Graph` ignores repeated edges, so a doubled real-world link is still reported as a bridge. |
| Articulation points | Vertices whose removal disconnects the graph | O(V + E) | repo: [GraphStructureAlgorithms.articulationPoints](../src/main/java/io/github/ichillous/dsa/graphs/GraphStructureAlgorithms.java) ([lesson](../algorithms/graph/graph-structure.md)) | The DFS root needs its own rule: it is a cut vertex only if it has two or more DFS children. The repository method uses an iterative lowlink DFS, supports disconnected graphs, ignores self-loops, returns the points in ascending order, and rejects directed graphs. |

### Check whether a graph is bipartite (two-colorable)

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Bipartite check (BFS two-coloring) | A matching precondition, conflict graphs, two-team splits | O(V + E) | repo: [GraphStructureAlgorithms.bipartition](../src/main/java/io/github/ichillous/dsa/graphs/GraphStructureAlgorithms.java) and `oddCycle` ([lesson](../algorithms/graph/graph-structure.md)) | Color every component, not only the first; the repository method does, placing each component's smallest vertex on the left, and returns an empty `Optional` when the graph has an odd cycle (any self-loop counts). `oddCycle` returns the odd cycle as proof. |

### Find a cycle and show it

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Directed cycle with witness | Report the actual dependency loop, not just "a cycle exists" | O(V + E) | repo: [GraphStructureAlgorithms.directedCycle](../src/main/java/io/github/ichillous/dsa/graphs/GraphStructureAlgorithms.java) ([lesson](../algorithms/graph/graph-structure.md)) | Track three states (unvisited, on the DFS stack, finished); an edge to an on-stack vertex closes a cycle. The repository method is iterative, returns the first cycle found (a self-loop gives `[v]`), returns an empty `Optional` for an acyclic graph, and rejects undirected graphs. |
| Union-find cycle check (undirected) | Detect a cycle as undirected edges are added | O(α(n)) amortized per edge | repo: [UnionFind.union](../src/main/java/io/github/ichillous/dsa/structures/UnionFind.java) ([lesson](../data-structures/graphs/overview.md)) | `union` returns false when both ends are already connected, so the new edge closes a cycle. |

### Use every edge exactly once

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Eulerian path (Hierholzer) | A route covering every edge once: inspection routes, de Bruijn sequences | O(V + E) expected (a hash lookup pairs the two entries of an undirected edge) | repo: [GraphStructureAlgorithms.eulerianPath](../src/main/java/io/github/ichillous/dsa/graphs/GraphStructureAlgorithms.java) ([lesson](../algorithms/graph/graph-structure.md)) | Check degrees first: in an undirected graph, zero or two odd-degree vertices; in a directed graph, at most one vertex with one extra outgoing edge and one with one extra incoming edge. All edges must be connected. The repository method checks this itself and returns an empty `Optional` when it fails; a graph with no edges gives an empty path, and it works for directed and undirected graphs. |

## 15. Shortest paths

### From one source

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| BFS shortest path | Unweighted edges | O(V + E) | repo: [GraphAlgorithms.shortestPath](../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java) ([lesson](../algorithms/graph/bfs.md)) | Minimizes the number of edges, not a total weight. |
| 0-1 BFS | Every weight is 0 or 1 | O(V + E) with a deque | repo: [ShortestPathAlgorithms.zeroOneBfs](../src/main/java/io/github/ichillous/dsa/graphs/ShortestPathAlgorithms.java) ([lesson](../algorithms/graph/shortest-paths.md)) | Push a vertex reached by a 0-edge at the front and by a 1-edge at the back. The repository method throws `IllegalArgumentException` if any edge in the graph has another weight, even one the search would not reach. |
| Dijkstra | Nonnegative weights | O(V + E log(E + 1)) with a lazy binary heap | repo: [GraphAlgorithms.dijkstra](../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java) ([lesson](../algorithms/graph/bfs.md)) | A single negative edge breaks it; the repository version rejects all negative edges. |
| Bellman–Ford | Negative weights, or detecting a negative cycle | O(V·E) | repo: [AdvancedGraphAlgorithms.bellmanFord](../src/main/java/io/github/ichillous/dsa/graphs/AdvancedGraphAlgorithms.java) ([lesson](../algorithms/graph/bfs.md)) | Slow on large graphs. It rejects a negative cycle reachable from the source. |
| DAG shortest path in topological order | A directed acyclic graph, any weight signs | O(V + E) arithmetic operations; O(V + E) | repo: [ShortestPathAlgorithms.dagShortestPaths](../src/main/java/io/github/ichillous/dsa/graphs/ShortestPathAlgorithms.java) ([lesson](../algorithms/graph/shortest-paths.md)) | Relax edges in topological order. Negating every weight gives the longest path, which is otherwise hard. The repository method requires a directed graph that is acyclic as a whole (including parts unreachable from the source), otherwise `IllegalArgumentException`; arithmetic is exact, and `ArithmeticException` means a reachable final distance is outside `long`. |
| A* | One source and one target, with a consistent heuristic (h(u) ≤ w(u, v) + h(v) for every edge, and h(target) = 0), such as straight-line distance on a map | With a consistent heuristic, no worse than Dijkstra and usually far fewer vertices explored (repository: O(V + E log(E + 1)) plus the heuristic's cost); with an admissible but inconsistent heuristic, reopening vertices can take exponential time | repo: [ShortestPathAlgorithms.aStar](../src/main/java/io/github/ichillous/dsa/graphs/ShortestPathAlgorithms.java) ([lesson](../algorithms/graph/shortest-paths.md)) | The repository's A* requires a consistent heuristic (h >= 0, h(target) = 0 and h(u) <= w(u, v) + h(v)) and throws `IllegalArgumentException` when it meets a violation on an edge it scans, so it never reopens vertices; violations on edges it never scans go unnoticed. Edge weights must be nonnegative, and an unreachable target gives an empty `Optional`. An overestimating heuristic can return a longer path. A heuristic of 0 is Dijkstra. |

### Between all pairs of vertices

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Floyd–Warshall (all pairs) | Dense graphs with up to a few hundred vertices | O(V³) arithmetic operations; O(V²) | repo: [ShortestPathAlgorithms.floydWarshall](../src/main/java/io/github/ichillous/dsa/graphs/ShortestPathAlgorithms.java) ([lesson](../algorithms/graph/shortest-paths.md)) | A negative cycle exists if some `d[i][i] < 0` afterwards; the repository method rejects a negative cycle anywhere in the graph with `IllegalArgumentException` (any negative undirected edge is one). Its arithmetic is exact, so there is no "infinity plus weight" overflow, and a finite distance outside `long` throws `ArithmeticException`. Unreachable pairs have empty distances. |
| Johnson's algorithm | Sparse graphs with negative edges | O(V·E log V) with binary heaps; O(V²) output | not implemented here | One Bellman–Ford pass computes vertex potentials that make every weight nonnegative; then Dijkstra runs from every vertex. |
| Dijkstra from every vertex | Sparse graphs with nonnegative weights | V runs of Dijkstra; O(V²) output | repo: [GraphAlgorithms.dijkstra](../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java), once per source ([lesson](../algorithms/graph/bfs.md)) | The output alone is quadratic. |

## 16. Spanning trees, flows and matching

### Connect every vertex at the lowest total cost

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Kruskal | Sparse graphs whose edges are easy to sort | O(V + E log(E + 1)) | repo: [GraphAlgorithms.minimumSpanningForest](../src/main/java/io/github/ichillous/dsa/graphs/GraphAlgorithms.java) ([lesson](../algorithms/greedy/problems.md)) | Returns a forest when the graph is disconnected. Negative weights are allowed. |
| Prim | Dense graphs, or growing one tree from a start vertex | O(E log V) with a binary heap; O(V²) with an array scan. Repository: O(V + E log(E + 1)) with a lazy heap | repo: [GraphStructureAlgorithms.primMinimumSpanningForest](../src/main/java/io/github/ichillous/dsa/graphs/GraphStructureAlgorithms.java) ([lesson](../algorithms/graph/minimum-spanning-trees.md)) | Restart from each unvisited vertex to cover a disconnected graph; the repository method does and returns a forest. Among equal weights it prefers the smaller target vertex, then the smaller source, so its forest can differ from Kruskal's although the total is equal. Negative weights are allowed, the total is checked (`ArithmeticException` beyond `long`), and directed graphs are rejected. |

### Find the maximum flow and the minimum cut

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Max flow (Dinic) + min cut | Capacity planning, assignment with capacities, the bottleneck between two sides | O(V²·E) | repo: [FlowNetwork.maxFlow](../src/main/java/io/github/ichillous/dsa/graphs/FlowNetwork.java) ([lesson](../algorithms/graph/flows-and-matching.md)) | The minimum cut is the set of vertices reachable from the source in the final residual graph; the repository result returns it as `sourceSide`, the minimal source side among minimum cuts. Use `long` for capacities and totals: the repository total uses `Math.addExact` and throws `ArithmeticException` only if the maximum flow exceeds `long`. `maxFlow` does not modify the network, parallel edges are kept, and `source == sink` is rejected. |
| Edmonds–Karp | Simpler maximum flow with BFS augmenting paths | O(V·E²) | not implemented here | Slower than Dinic on large graphs. |

### Match pairs in a bipartite graph

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Bipartite matching (Hopcroft–Karp) | The largest matching of workers to jobs, without costs | O(E·√V) after an O(E log E) sort that removes duplicate edges | repo: [BipartiteMatching.maximumMatching](../src/main/java/io/github/ichillous/dsa/graphs/BipartiteMatching.java) ([lesson](../algorithms/graph/flows-and-matching.md)) | Maximizes the number of pairs only; with costs, use the Hungarian algorithm or min-cost flow. The two sides are numbered separately from 0. The repository method ignores duplicate edges and returns one deterministic maximum matching; which one is not otherwise specified. |
| Augmenting paths (Kuhn's algorithm) | Small graphs and the simplest code | O(V·E) | not implemented here | A recursive version overflows the stack on large graphs. |

### Assign items at the lowest total cost

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Hungarian algorithm | A square cost matrix; the cheapest one-to-one assignment | O(n³); O(n²) | not implemented here | Pad a rectangular matrix with dummy rows or columns. |
| Min-cost flow | Assignment or transport with both capacities and costs | Successive shortest paths: at most F augmentations for integer capacities and flow value F, each a Dijkstra run with potentials, so O(F·E log V): pseudopolynomial in F | not implemented here | Residual edges have negative costs; use vertex potentials (or Bellman–Ford) so Dijkstra stays valid. |

## 17. Trees as data

The rows below treat a tree as a graph with V vertices and V − 1 edges, often
rooted, such as an organization chart or a file system. Build every structure
iteratively: real trees can be paths 100,000 vertices deep.

### Answer ancestor queries on a rooted tree

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Lowest common ancestor (binary lifting) | Many lowest-common-ancestor, distance or k-th-ancestor queries on a static tree | O(V log V) build; O(log V) per query; O(V log V) | repo: [LowestCommonAncestor](../src/main/java/io/github/ichillous/dsa/graphs/LowestCommonAncestor.java) with `lca`, `distance` and `kthAncestor` ([lesson](../algorithms/graph/tree-queries.md)) | Store the 2^j-th ancestor of every vertex. Lift the deeper vertex to the same depth first. The repository class requires an undirected tree (connected, V - 1 edges; a self-loop makes it a non-tree), builds from a chosen root with an iterative BFS, and is immutable. `kthAncestor` returns an empty `OptionalInt` when k exceeds the depth. |
| LCA by Euler tour + range minimum | Very many LCA queries, each in O(1) | O(V log V) build with a sparse table; O(1) per query | not implemented here | The LCA is the shallowest vertex between the two first visits in the Euler tour. |

### Find the longest path in a tree

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Tree diameter (two BFS passes) | The longest path between any two vertices of a tree | O(V); O(V) | repo: [TreeAlgorithms.diameterPath](../src/main/java/io/github/ichillous/dsa/graphs/TreeAlgorithms.java) ([lesson](../algorithms/graph/tree-queries.md)) | BFS from any vertex, then BFS again from the farthest vertex found. With weights, the double pass is correct only for nonnegative edge weights; the repository method is unweighted, counting edges, and returns one longest path (ties go to the smallest endpoints). It requires an undirected tree. |

### Aggregate over subtrees or paths

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Euler tour (subtree as a range) | Subtree sums or updates: flatten the tree so each subtree is one contiguous range | O(V) to flatten, then the range structure's cost | not implemented here (pair it with repo [FenwickTree](../src/main/java/io/github/ichillous/dsa/structures/FenwickTree.java) or [SegmentTree](../src/main/java/io/github/ichillous/dsa/structures/SegmentTree.java)) | Record entry and exit times with an explicit stack. |
| Heavy-light decomposition | Queries or updates along paths, such as the maximum edge weight between two vertices | O(log² V) per path query with a segment tree | not implemented here | Complex to write; for static data, binary lifting that stores path maxima is simpler. |
| Tree DP (post-order) | A value per subtree: sizes, best independent set, subtree sums | O(V) | not implemented here | Process vertices in reverse BFS order instead of recursing. |

## 18. Dynamic programming

### Make an amount from coins

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Minimum coins (unbounded) | The fewest coins for an amount, each denomination reusable | O(c·a) for c coins and amount a; O(a) | repo: [DynamicProgramming.minimumCoins](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java) ([lesson](../algorithms/dynamic-programming/overview.md)) | Returns `-1` when the amount cannot be made. Largest-coin-first greedy fails for coins `[1, 3, 4]` and amount 6. Memory grows with the amount (pseudopolynomial). |
| Count coin-change ways | The number of combinations that make an amount (order ignored) | O(c·a); O(a) | repo: [DynamicProgrammingClassics.countCoinCombinations](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgrammingClassics.java) ([lesson](../algorithms/dynamic-programming/classic-problems.md)) | Loop over coins outside the amount loop to count combinations; swapping the loops counts ordered sequences instead. Counts grow fast: the repository method uses checked `long` arithmetic and throws `ArithmeticException` if any table entry up to the amount overflows, even when the requested amount's own count would fit. Duplicate denominations collapse, and amount 0 has one combination. |

### Choose items under a capacity

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| 0/1 knapsack | Each item used at most once, with integer weights | O(n·W) for capacity W; O(W) | repo: [DynamicProgramming.knapsack01](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java) ([lesson](../algorithms/dynamic-programming/problems.md)) | With one table row, iterate the capacity downward so no item is reused. Pseudopolynomial: a capacity of 10^9 needs a 10^9-entry row. |

### Longest increasing subsequence

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Patience sorting LIS | The length of the longest strictly increasing subsequence | O(n log n); O(n) | repo: [DynamicProgramming.longestIncreasingSubsequenceLength](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java) ([lesson](../algorithms/dynamic-programming/problems.md)) | Strictly increasing: equal values do not extend it. For non-decreasing, search with an upper bound instead of a lower bound. |

### Recover the actual solution, not just its value

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| LCS string reconstruction | You need the common subsequence itself | O(mn); O(mn) table | repo: [DynamicProgramming.longestCommonSubsequence](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java) ([lesson](../algorithms/dynamic-programming/problems.md)) | The single-row table that computes the length cannot be walked back. The repository method keeps the full table (O(mn) memory) and backtracks with a fixed tie rule: on a mismatch it moves up when `dp[i-1][j] >= dp[i][j-1]`. Use Hirschberg's algorithm for linear space. |
| Edit script reconstruction | You need the insert, delete and substitute operations (a diff) | O(mn); O(mn) table | repo: [DynamicProgramming.editScript](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java) ([lesson](../algorithms/dynamic-programming/problems.md)) | Several scripts can have the same cost; the repository method fixes the tie rule (diagonal match or substitute, then delete, then insert, walking back from the ends), so output is deterministic. It keeps the full O(mn) table. |
| Knapsack chosen items reconstruction | You need which items to pack | O(n·W); O(n·W) bits | repo: [DynamicProgramming.knapsack01Items](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java) ([lesson](../algorithms/dynamic-programming/problems.md)) | Record, per item and capacity, whether taking the item improved the value. The repository method records it only when strictly better, so zero- and negative-value items are omitted, and it returns ascending indices, preferring lower-indexed items among equal-value optima. |
| Chosen coins reconstruction | You need the coins, not just their count | O(c·a); O(a) | repo: [DynamicProgramming.minimumCoinsChoice](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java) ([lesson](../algorithms/dynamic-programming/problems.md)) | Store the last coin used for each amount and walk back from the target. The repository method returns an empty `Optional` when the amount is unreachable, the coins in ascending order, and on ties the first listed coin wins. |
| LIS sequence reconstruction | You need the subsequence, not just its length | O(n log n); O(n) | repo: [DynamicProgramming.longestIncreasingSubsequenceIndices](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java) ([lesson](../algorithms/dynamic-programming/problems.md)) | Keep a predecessor index for each element. The repository method returns the ascending indices of one strictly increasing optimum, not necessarily the lexicographically smallest. |
| Hirschberg's algorithm | An LCS or alignment in linear space | O(mn); O(min(m, n)) | not implemented here | Divide and conquer around the middle row, roughly doubling the time. |

### Split a string into dictionary words

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Word break | Decide whether, and how, a string splits into dictionary words | O(n·L) substring checks for maximum word length L, each costing O(L); O(n) | repo: [DynamicProgrammingClassics.wordBreak](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgrammingClassics.java) ([lesson](../algorithms/dynamic-programming/classic-problems.md)) | Store the dictionary in a `HashSet` and only try lengths up to the longest word. Return one segmentation. Listing all of them can take exponential time; counting them is the same DP, but the count overflows `long` quickly, so use checked or `BigInteger` arithmetic. The repository method returns an `Optional` list, the segmentation whose first word is shortest, and rejects an empty dictionary word. |

### Palindromes as subsequences

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Longest palindromic subsequence | The longest palindrome you can keep by deleting characters | O(n²); O(n) | repo: [DynamicProgrammingClassics.longestPalindromicSubsequenceLength](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgrammingClassics.java) ([lesson](../algorithms/dynamic-programming/classic-problems.md)) | Not the same as the longest palindromic *substring* (contiguous; see Manacher). It equals the LCS of the string and its reverse. The repository method returns the length only, counted in UTF-16 code units. |

### Order a chain of operations

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Matrix-chain order | The cheapest order to multiply a chain of matrices, or any interval DP over a sequence | O(n³); O(n²) | repo: [DynamicProgrammingClassics.matrixChainOrder](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgrammingClassics.java) ([lesson](../algorithms/dynamic-programming/classic-problems.md)) | Costs overflow `int` quickly; use `long` and checked arithmetic. The repository method returns the cost and a parenthesization, breaks ties at the smallest split, and throws `ArithmeticException` if any candidate cost exceeds `long`, even one that would not have been optimal. |

### Visit every city once (small n)

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Bitmask TSP (Held–Karp) | An exact shortest tour for up to about 16–20 cities | O(n²·2ⁿ); O(n·2ⁿ) | repo: [DynamicProgrammingClassics.travelingSalesman](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgrammingClassics.java) ([lesson](../algorithms/dynamic-programming/classic-problems.md)) | Exponential. For larger n use heuristics (nearest neighbor, 2-opt), which give no optimality guarantee. The repository method accepts 1 to 16 cities with entries in [-2^47, 2^47] (so a tour cannot overflow), starts the tour at city 0, allows asymmetric matrices, and needs about 4 MB of tables at 16 cities. |

### Count numbers whose digits follow a rule

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Digit DP | Count integers in [0, N] whose digits satisfy a property (digit sums, forbidden digits) | O(digits × states × base) | not implemented here | Track a "tight" flag that says whether the prefix still equals N's prefix. Count [A, B] as f(B) − f(A − 1). |

### Techniques that apply to any DP

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Top-down memoization | The recursion is natural and only some states are reached | Number of reached states × work per state | JDK: `java.util.HashMap` (or arrays when the states are dense integers) | Do not memoize a recursive function with `computeIfAbsent`: the spec says the mapping function should not modify the map, and `HashMap` may throw `ConcurrentModificationException`. Use explicit `get` and `put`. Deep recursion overflows the stack, so prefer bottom-up tables for long chains. |
| Bottom-up table with rolling rows | Each row depends only on the previous row | Memory drops from rows × columns to a few rows | repo: the length-only methods of [DynamicProgramming](../src/main/java/io/github/ichillous/dsa/algorithms/DynamicProgramming.java) ([lesson](../algorithms/dynamic-programming/problems.md)) | Rolling rows discard what reconstruction needs. |

## 19. Greedy

A greedy rule is only correct with a proof, usually an exchange argument. A rule
that sounds plausible is not enough. Kruskal and Prim are greedy as well; see
[spanning trees](#16-spanning-trees-flows-and-matching), where the cut property is
the proof.

### Schedule the most non-overlapping activities

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Activity selection (earliest finish first) | The most non-overlapping intervals, all equally valuable | O(n log n); O(n) | repo: [GreedyAlgorithms.selectActivities](../src/main/java/io/github/ichillous/dsa/algorithms/GreedyAlgorithms.java) ([lesson](../algorithms/greedy/overview.md)) | Not for weighted intervals; see [intervals](#6-intervals). |

### Build an optimal prefix code

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Huffman code lengths | Optimal prefix-free codes for known symbol frequencies | O(n log n); O(n) | repo: [GreedyAlgorithms.huffmanCodeLengths](../src/main/java/io/github/ichillous/dsa/algorithms/GreedyAlgorithms.java) ([lesson](../algorithms/greedy/huffman-coding.md)) | Ties give different trees with the same total length; the repository method orders equal weights by node number (leaves by symbol index, then internal nodes in creation order) so lengths are deterministic, and canonical codes make the codes themselves deterministic. A lone used symbol gets 1 bit by convention, and unused symbols get 0. A total frequency beyond `long` throws `ArithmeticException`. |

### Fill a capacity when items can be split

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Fractional knapsack | Items can be split | O(n log n); O(n) | not implemented here | Taking the best value per weight first is optimal only when items can be split. For whole items use 0/1 knapsack DP. Compare ratios by cross-multiplying in `long` instead of dividing. |

### Reach the end of an array of jump lengths

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Jump game (farthest reach) | Whether you can reach the end of an array of jump lengths, or the fewest jumps | O(n); O(1) | not implemented here | The fewest-jumps version advances level by level, like BFS. |

### Make change with the largest coins first

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Coin change, largest coin first | Only for canonical coin systems such as 1, 5, 10, 25 | O(c) per amount | not implemented here | Wrong for coins `[1, 3, 4]` and amount 6 (greedy uses 3 coins, the optimum is 2); use the DP in [dynamic programming](#18-dynamic-programming). |

## 20. Backtracking and search

### List every arrangement or selection

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Unique permutations | Every distinct ordering of a small multiset | O(n·n!) | repo: [Backtracking.uniquePermutations](../src/main/java/io/github/ichillous/dsa/algorithms/Backtracking.java) ([lesson](../algorithms/backtracking/overview.md)) | At most 9 values; the output alone is factorial. |
| Combinations (n choose k) | Every k-element subset | O(k·C(n, k)) | not implemented here | C(n, k) grows fast; stream results instead of storing them when possible. |
| Power set (all subsets) | Every subset of up to about 20–25 items | O(n·2ⁿ) | not implemented here | Counting a bitmask from 0 to 2ⁿ − 1 lists each subset once; see [bitsets](#21-disjoint-sets-and-bitsets). |

### Solve a constraint puzzle

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| N-queens | Place n non-attacking queens, a model for constraint search | O(n·n!) upper bound | repo: [Backtracking.nQueens](../src/main/java/io/github/ichillous/dsa/algorithms/Backtracking.java) ([lesson](../algorithms/backtracking/overview.md)) | n is limited to 0..12 because every solution is returned. |
| Sudoku with fewest-candidates-first | Constraint puzzles where choosing the most constrained cell first prunes the search | Exponential worst case | repo: [BacktrackingPuzzles.solveSudoku](../src/main/java/io/github/ichillous/dsa/algorithms/BacktrackingPuzzles.java) ([lesson](../algorithms/backtracking/problems.md)) | Finds one solution; it does not prove uniqueness. The board changes only after a full solution. |
| Constraint propagation | Puzzles and scheduling where each choice removes options elsewhere (arc consistency) | Varies; often far fewer search nodes | not implemented here | Combine with backtracking: propagation alone rarely finishes the search. |

### Find a subset with an exact sum

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Meet in the middle (subset sum) | Exact subset sum for up to about 40 items, with signed values | O(2^(n/2)) expected; O(2^(n/2)) | repo: [BacktrackingPuzzles.subsetSum](../src/main/java/io/github/ichillous/dsa/algorithms/BacktrackingPuzzles.java) ([lesson](../algorithms/backtracking/problems.md)) | The repository caps input at 30 values. For small positive targets, DP over sums is cheaper. |

### Find the best answer in a huge search space

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Branch and bound | Optimization search with a bound that prunes branches that cannot beat the best answer so far | Exponential worst case | not implemented here | Only as good as its bound; explore promising branches first. |

## 21. Disjoint sets and bitsets

### Merge groups and ask whether two items are in the same group

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Union-find (disjoint sets) | Dynamic connectivity, Kruskal, grouping equivalent items | O(α(n)) amortized per operation with path compression and union by size; O(n) | repo: [UnionFind](../src/main/java/io/github/ichillous/dsa/structures/UnionFind.java) ([lesson](../data-structures/graphs/overview.md)) | Elements are integers `[0, n)`; map other keys to indices first. It cannot split groups. |

### Store a dense set of small integers

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Bit set | Flags or membership for indices 0..N | O(1) `get`; O(1) `set` inside the current size, while a bit past it grows the array (OpenJDK implementation note: the array at least doubles, so increasing indices cost amortized O(1)); space proportional to the largest set index; bulk `and`, `or` and `xor` | JDK: `java.util.BitSet#set`, `#nextSetBit`, `#cardinality` | Not safe for multithreaded use without external synchronization. `set(from, to)` is half-open. Iterate with `nextSetBit(i + 1)` until it returns `-1`. |
| Bit tricks on one word | Up to 64 flags in a single `int` or `long` | O(1) | JDK: `java.lang.Integer#bitCount`, `#numberOfTrailingZeros`, `#lowestOneBit`, and the same methods on `java.lang.Long` | Shift counts are taken modulo the width: `1 << 32` equals 1 for an `int`. Use `1L << k` for a `long` mask. |

```java
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

BitSet seen = new BitSet();
seen.set(3);
seen.set(64);
seen.set(5, 8);                          // half-open: sets 5, 6 and 7
assert seen.cardinality() == 5;
List<Integer> members = new ArrayList<>();
for (int i = seen.nextSetBit(0); i >= 0; i = seen.nextSetBit(i + 1)) {
    members.add(i);
}
assert members.equals(List.of(3, 5, 6, 7, 64));
assert seen.nextSetBit(65) == -1;        // no set bit at or after 65
```

```java
int mask = 0b1011_0000;
assert Integer.bitCount(mask) == 3;                 // population count
assert Integer.numberOfTrailingZeros(mask) == 4;    // index of the lowest set bit
assert Integer.numberOfTrailingZeros(0) == 32;      // no set bit at all
assert Integer.lowestOneBit(mask) == 0b1_0000;
assert Long.bitCount(-1L) == 64;
```

### Enumerate subsets with bitmasks

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Subset enumeration over bitmasks | Visit every subset of a set of n ≤ 30 items, or every submask of a mask | O(2ⁿ) masks; all submasks of all masks together cost O(3ⁿ) | not implemented here | To visit the nonempty submasks of `m`, start at `s = m` and step with `s = (s - 1) & m` until `s` is 0. Use `long` masks from 31 items (up to 62). |
| Gosper's hack | Visit every n-bit mask with exactly k bits set, in increasing order | O(1) per mask | not implemented here | The next mask comes from the lowest set bit and a division by it; stop before the mask exceeds n bits. |

## 22. Math and number theory

### Integer arithmetic that must not silently overflow

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Checked arithmetic | Any sum, product or negation that might leave the `int` or `long` range | O(1) | JDK: `java.lang.Math#addExact`, `#subtractExact`, `#multiplyExact`, `#negateExact`, `#toIntExact`, `#absExact` | Plain `+` and `*` wrap around silently; the `...Exact` methods throw `ArithmeticException` instead. The spec: `Math.abs(Integer.MIN_VALUE)` returns that same negative value, while `absExact` throws. |
| Floor division and modulo | Bucketing or wrapping negative values, such as a negative index modulo n | O(1) | JDK: `java.lang.Math#floorMod`, `#floorDiv` | `%` takes the sign of the dividend, so `-7 % 3` is `-1`; `floorMod(-7, 3)` is `2`. |
| Arbitrary-precision integers | Values beyond `long`, exact intermediate results, cryptographic-size numbers | Cost grows with the number of digits | JDK: `java.math.BigInteger` | Immutable, so every operation allocates. Far slower than primitives in tight loops. |
| 128-bit products | `long × long` without overflow, as in modular multiplication with a large modulus | O(1) | JDK: `java.lang.Math#multiplyHigh` | It returns the high 64 bits only; combine it with the ordinary low product. |

```java
boolean overflowed = false;
try {
    Math.addExact(Integer.MAX_VALUE, 1);
} catch (ArithmeticException expected) {
    overflowed = true;
}
assert overflowed;                                         // checked: throws
assert Integer.MAX_VALUE + 1 == Integer.MIN_VALUE;         // plain +: wraps silently
assert Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE;   // still negative
assert -7 % 3 == -1 && Math.floorMod(-7, 3) == 2;
```

### Divisibility, modular arithmetic and primes

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| gcd/lcm (overflow-checked) | Reduce fractions, align periods | O(log min(a, b)) | repo: [NumberTheory.gcd](../src/main/java/io/github/ichillous/dsa/algorithms/NumberTheory.java) and `lcm` ([lesson](../algorithms/math/number-theory.md)); JDK: `java.math.BigInteger#gcd` | `gcd(Long.MIN_VALUE, 0)` is 2^63, which does not fit in `long`; the repository method throws `ArithmeticException` there (and for `(0, MIN_VALUE)` and `(MIN_VALUE, MIN_VALUE)`). `lcm` is computed as `a / gcd(a, b) * b` with checked multiplication, and also throws `ArithmeticException` when the result does not fit. |
| Extended Euclid | Solve a·x + b·y = gcd(a, b); modular inverses; linear Diophantine equations | O(log min(a, b)) | repo: [NumberTheory.extendedGcd](../src/main/java/io/github/ichillous/dsa/algorithms/NumberTheory.java) ([lesson](../algorithms/math/number-theory.md)) | The coefficients can be negative and are not canonical. The repository method returns `(gcd, x, y)` and throws `ArithmeticException` only when the gcd is 2^63. |
| Modular exponentiation | Compute a^e mod m for large e (hashing, primality tests) | O(log e) multiplications | repo: [NumberTheory.modPow](../src/main/java/io/github/ichillous/dsa/algorithms/NumberTheory.java) ([lesson](../algorithms/math/number-theory.md)); JDK: `java.math.BigInteger#modPow` | With a `long` modulus above about 3.04·10^9, `a * b % m` overflows; use 128-bit products or `BigInteger`. The repository method uses an overflow-free modular multiply, so it is exact for every positive `long` modulus. It reduces a negative base first and throws `IllegalArgumentException` for a modulus below 1 or a negative exponent. |
| Modular inverse | Divide modulo m | O(log m) | repo: [NumberTheory.modInverse](../src/main/java/io/github/ichillous/dsa/algorithms/NumberTheory.java) ([lesson](../algorithms/math/number-theory.md)); JDK: `java.math.BigInteger#modInverse` | An inverse exists only when gcd(a, m) = 1. The repository method returns an empty `OptionalLong` in that case, where `modInverse` throws `ArithmeticException`. Modulo 1 the inverse is 0. |
| Sieve of Eratosthenes | Every prime up to n | O(n log log n); O(n) bits | repo: [NumberTheory.primesUpTo](../src/main/java/io/github/ichillous/dsa/algorithms/NumberTheory.java) ([lesson](../algorithms/math/number-theory.md)) | Start crossing out at p², and compute p² in `long`. Memory is linear in n. The repository method returns an immutable list and rejects limits above `MAX_SIEVE_LIMIT` (50,000,000) or below 0 with `IllegalArgumentException`. |
| Deterministic Miller–Rabin for `long` | Exact primality of any 64-bit value | O(log n) modular multiplications per base, 12 fixed bases | repo: [NumberTheory.isPrime](../src/main/java/io/github/ichillous/dsa/algorithms/NumberTheory.java) ([lesson](../algorithms/math/number-theory.md)) | Needs overflow-free modular multiplication, which the repository method provides. Testing the twelve prime bases 2, 3, 5, …, 37 makes it exact for every `long`; negative numbers, 0 and 1 are not prime. |
| Probabilistic primality | Primality of large numbers where a tiny error probability is acceptable | Proportional to the certainty parameter | JDK: `java.math.BigInteger#isProbablePrime`, `#nextProbablePrime` | `true` means prime with probability above 1 − 2^(−certainty); `false` means definitely composite. A certainty of 0 or less always returns `true`. |
| Trial division | Factor or test a single small number | O(√n) | not implemented here | Loop while `d <= n / d`, not `d * d <= n`, to avoid overflow. |
| Matrix exponentiation (fast Fibonacci) | The n-th term of a linear recurrence for huge n | O(k³ log n) for a k×k matrix | repo: [NumberTheory.fibonacciMod](../src/main/java/io/github/ichillous/dsa/algorithms/NumberTheory.java) ([lesson](../algorithms/math/number-theory.md)) | Reduce modulo m after every multiplication, or the entries overflow. The repository implements only the 2×2 Fibonacci case (exact for any positive `long` modulus, O(log n) matrix multiplications); there is no general matrix class. |

```java
import java.math.BigInteger;

BigInteger mersenne = BigInteger.TWO.pow(61).subtract(BigInteger.ONE);   // 2^61 - 1 is prime
assert mersenne.isProbablePrime(50);         // a prime is never reported "definitely composite"
BigInteger carmichael = BigInteger.valueOf(561);                        // 3 * 11 * 17
assert carmichael.mod(BigInteger.valueOf(3)).signum() == 0;             // exact proof it is composite
assert carmichael.isProbablePrime(0);        // the spec: certainty <= 0 always returns true
assert BigInteger.valueOf(3).modPow(BigInteger.valueOf(200), BigInteger.valueOf(13)).intValueExact() == 9;
assert BigInteger.valueOf(3).modInverse(BigInteger.valueOf(11)).intValueExact() == 4;  // 3 * 4 = 12 = 1 (mod 11)
```

## 23. Randomization and streams

### Shuffle or sample

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Shuffle a list | Random order for a list | Javadoc: linear time | JDK: `java.util.Collections#shuffle(List, Random)` | The spec: "All permutations occur with equal likelihood assuming that the source of randomness is fair." It mutates the list. OpenJDK implementation note, stated in the Javadoc: it walks backwards, swapping each position with a randomly chosen position at or before it (Fisher–Yates). `java.util.Random` has a 48-bit seed, so for 17 or more elements some permutations can never occur (17! > 2^48). |
| Fisher–Yates shuffle of a primitive array | Shuffle an `int[]` without boxing | O(n); O(1) | repo: [Sampling.shuffle](../src/main/java/io/github/ichillous/dsa/algorithms/Sampling.java) ([lesson](../algorithms/math/randomized-sampling.md)) | Draw the swap index from `[0, i]` inclusive; drawing from the whole array biases the result. The repository method shuffles in place with unbiased bounded draws from the caller's `RandomGenerator`; a generator with too little state cannot reach every permutation. |
| Reservoir sampling | k uniform samples from a stream of unknown length, in one pass | O(n); O(k) | repo: [Sampling.reservoirSample](../src/main/java/io/github/ichillous/dsa/algorithms/Sampling.java) ([lesson](../algorithms/math/randomized-sampling.md)) | Keep the first k items, then replace a random slot with probability k/i for the i-th item. The repository method takes an `Iterable` and the caller's `RandomGenerator`, returns an immutable list whose order is not meaningful, and returns every item in encounter order when there are at most k. |
| Alias method (weighted sampling) | Many draws from one fixed discrete distribution | O(n) build; O(1) per draw | repo: [AliasTable](../src/main/java/io/github/ichillous/dsa/algorithms/AliasTable.java) ([lesson](../algorithms/math/randomized-sampling.md)) | Rebuild when the weights change; the repository table copies its weights and is immutable. Floating-point weights need care at the edges: weights must be finite and nonnegative with a positive finite sum, and an index of weight 0 is never returned. Each draw takes the caller's `RandomGenerator`. |
| Cumulative weights + binary search | Weighted draws when the weights change rarely, or simpler code | O(n) build; O(log n) per draw | JDK: `java.util.Arrays#binarySearch` on prefix sums | Use `long` prefix sums of integer weights, and decode the negative insertion point. |

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

List<Integer> deck = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8));
Collections.shuffle(deck, new Random(42));   // a fixed seed makes a test repeatable
List<Integer> sorted = new ArrayList<>(deck);
Collections.sort(sorted);
assert sorted.equals(List.of(1, 2, 3, 4, 5, 6, 7, 8));   // same items, only the order may change
assert deck.size() == 8;
```

### Choose a random generator

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Seeded linear congruential generator | Reproducible tests and simulations | O(1) per value | JDK: `java.util.Random` | The spec fixes its algorithm, so a seed gives the same sequence on every Java implementation. The period is only 2^48. Not cryptographically secure. Shared across threads it is safe but can contend. |
| Splittable generator | Parallel computations that split work into subtasks | O(1) per value; period 2^64 | JDK: `java.util.SplittableRandom#split` | Instances are not thread-safe: split one per task instead of sharing. |
| Thread-local generator | Concurrent code that needs random values but no seed | O(1) per value | JDK: `java.util.concurrent.ThreadLocalRandom#current` | `setSeed` always throws `UnsupportedOperationException`, so runs are not reproducible. |
| Generator interface | APIs that should accept any generator (Java 17+) | Interface only | JDK: `java.util.random.RandomGenerator` | `Random`, `SplittableRandom` and `ThreadLocalRandom` all implement it; take it as a parameter to make randomized code testable. |

## 24. Concurrency-aware JDK structures

This repository's structures are not thread safe. These JDK types are the
starting point when several threads share data. The rows below quote their
specifications; this repository does not implement them.

### Share a map between threads

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Concurrent hash map | Many threads read and update a map | A hash table: expected O(1) with good hashing, though the spec itself states no time bound | JDK: `java.util.concurrent.ConcurrentHashMap` | The spec: "retrieval operations do *not* entail locking", and retrievals "generally do not block". Null keys and values are not allowed. `size` and `isEmpty` are estimates while updates run. `computeIfAbsent` is atomic, so keep its function short. For counters, store `LongAdder` values with `computeIfAbsent(key, k -> new LongAdder()).increment()`. |
| Synchronized wrapper | Low contention and the simplest code | Every access goes through the thread-safe wrapper | JDK: `java.util.Collections#synchronizedMap` | The spec: you must synchronize on the returned map yourself while iterating over any of its views. |
| Concurrent skip-list map | A sorted map shared between threads, with nearest-key queries | Spec: expected average log(n) for `containsKey`, `get`, `put`, `remove` and their variants | JDK: `java.util.concurrent.ConcurrentSkipListMap#ceilingKey` | Iterators are weakly consistent. Bulk operations such as `putAll` and `clear` are not atomic. Null keys and values are rejected. |

```java
import java.util.concurrent.ConcurrentSkipListMap;

ConcurrentSkipListMap<Long, String> schedule = new ConcurrentSkipListMap<>();
schedule.put(1_000L, "backup");
schedule.put(5_000L, "report");
assert schedule.ceilingKey(1_001L) == 5_000L;   // the next job at or after t = 1001
assert schedule.ceilingKey(6_000L) == null;     // nothing scheduled that late
assert schedule.firstKey() == 1_000L;
```

### Share a list that is read far more often than written

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Copy-on-write list | Listener lists and other read-mostly data | Spec: every mutation makes a fresh copy of the underlying array, so a write costs O(n); traversals need no synchronization | JDK: `java.util.concurrent.CopyOnWriteArrayList` | Iterators see a snapshot taken when they were created and never throw `ConcurrentModificationException`. Iterator `remove`, `set` and `add` throw `UnsupportedOperationException`. Frequent writes are expensive. |

```java
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

List<String> listeners = new CopyOnWriteArrayList<>(List.of("a", "b"));
Iterator<String> snapshot = listeners.iterator();
listeners.add("c");                        // copies the array; the iterator keeps the old one
List<String> seen = new ArrayList<>();
snapshot.forEachRemaining(seen::add);
assert seen.equals(List.of("a", "b"));     // no "c" and no ConcurrentModificationException
assert listeners.equals(List.of("a", "b", "c"));
```

### Hand work between threads (producer–consumer)

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Bounded array blocking queue | Back-pressure: producers must wait when consumers fall behind | Fixed array of the chosen capacity | JDK: `java.util.concurrent.ArrayBlockingQueue` | The capacity cannot change after creation. `put` blocks when full and `take` blocks when empty; `offer` and `poll` return at once. Nulls are rejected (as in every `BlockingQueue`). |
| Linked blocking queue | Producer–consumer with an optional bound | One node per element | JDK: `java.util.concurrent.LinkedBlockingQueue` | Without a capacity argument the bound is `Integer.MAX_VALUE`, so memory, not the queue, stops a runaway producer. |
| Priority blocking queue | Consumers should take the most urgent task first | Same ordering rules as `PriorityQueue` | JDK: `java.util.concurrent.PriorityBlockingQueue` | The spec: logically unbounded, but an addition may fail from resource exhaustion (`OutOfMemoryError`). |
| Delay queue | Release items only after their delay expires (retries, timeouts) | Unbounded | JDK: `java.util.concurrent.DelayQueue` | Elements implement `Delayed`. `size` counts expired and unexpired elements. |
| Synchronous queue | Direct handoff: each insert waits for a matching remove | No capacity at all | JDK: `java.util.concurrent.SynchronousQueue` | You cannot peek or iterate; there is nothing stored. |
| Non-blocking concurrent queue | Many threads share a FIFO queue and nobody needs to wait | Unbounded linked nodes | JDK: `java.util.concurrent.ConcurrentLinkedQueue` | The spec: `size` is not a constant-time operation and may be inaccurate during concurrent changes. Nulls are rejected. |

### Count across threads

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Striped counter | Many threads update a sum, such as statistics | The spec: under high contention, expected throughput is "significantly higher" than `AtomicLong`, at the cost of more space | JDK: `java.util.concurrent.atomic.LongAdder` | `sum` is not an atomic snapshot while updates run. The spec: meant for sums such as statistics, "not for fine-grained synchronization control"; use `AtomicLong` for compare-and-set. It does not define `equals` or `hashCode`, so it is useless as a map key. |
| Atomic counter | Low contention, or you need `compareAndSet` | O(1) per update | JDK: `java.util.concurrent.atomic.AtomicLong` | Under high contention, the `LongAdder` spec expects significantly higher throughput from `LongAdder`. |

## 25. Encoding and compression

### Compress data without losing information

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Run-length encoding | Data with long runs of the same value (masks, simple images, sorted flags) | O(n); output depends on the runs | not implemented here | It expands data without runs (each single value becomes a value and a count). Make the encoding of counts unambiguous. |
| Burrows–Wheeler transform | Group similar contexts together before compression (as in bzip2) | O(n log n) with a suffix array; O(n) | not implemented here | It does not compress by itself; follow it with move-to-front and an entropy coder. Inverting it needs the original row index or an end marker. |
| Move-to-front transform | Turn locally repeated symbols into small numbers, usually after Burrows–Wheeler | O(n·A) with a simple list over an alphabet of size A | not implemented here | Helps only when symbols repeat close together. |
| LZ77 (sliding-window dictionary) | Replace repeated substrings with back-references to earlier text | Depends on how matches are found, often with hash chains | not implemented here | DEFLATE (RFC 1951) combines LZ77 with Huffman coding; use the JDK classes in the next row rather than writing your own. |
| DEFLATE (zlib and gzip) | General-purpose lossless compression | Streamed; memory bounded by the compressor's window | JDK: `java.util.zip.Deflater`, `java.util.zip.Inflater`, `java.util.zip.GZIPOutputStream`, `java.util.zip.GZIPInputStream` | Call `end()` on every `Deflater` or `Inflater` you create yourself; the spec says it "should be called when the compressor is no longer being used". Close compression streams. Decompressing untrusted input can produce far more output than input, so cap the output size. |
| Canonical Huffman codes | Store a Huffman code compactly as code lengths only | O(n log n) to sort symbols by length | repo: [GreedyAlgorithms.canonicalCodes](../src/main/java/io/github/ichillous/dsa/algorithms/GreedyAlgorithms.java) ([lesson](../algorithms/greedy/huffman-coding.md)) | The lengths must satisfy the Kraft inequality; the repository method checks it exactly and throws `IllegalArgumentException` otherwise (incomplete codes are accepted). It returns codes as strings of `0` and `1`, so its output size is the sum of the lengths. See Huffman code lengths in [greedy](#19-greedy). |

### Represent bytes as text

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Base64 | Binary data inside text: JSON, URLs, email | O(n); output is about 4/3 of the input | JDK: `java.util.Base64#getEncoder`, `#getUrlEncoder`, `#getMimeEncoder` | Pick the matching decoder: the basic and URL-safe alphabets differ, and only MIME adds line breaks. |
| Hex encoding | Readable byte dumps, checksums and identifiers | O(n); output is twice the input | JDK: `java.util.HexFormat` (Java 17+) | Choose upper or lower case and delimiters explicitly when output must match another system. |

### Detect corrupted data

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Checksums (CRC-32, CRC-32C) | Detect accidental corruption in storage or transfer | O(n); O(1) | JDK: `java.util.zip.CRC32`, `java.util.zip.CRC32C` | They detect accidents, not tampering; use a cryptographic hash or MAC against attackers. |

### Store small integers compactly

| Option | Use when | Time / space | Java home | Watch out for |
| --- | --- | --- | --- | --- |
| Variable-length integers (varint) | Small integers in compact binary formats | O(bytes) per value | not implemented here | Seven payload bits per byte; encode negative values with zigzag encoding or they take the maximum length. |
