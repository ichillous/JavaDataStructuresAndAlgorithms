# Hash tables: separate equality from position

A hash table maps keys to values by using a hash code to select a bucket, then checking equality within that bucket. Use it for membership, counting, indexing or caching when key order is not required.

[`ChainedHashMap`](../../src/main/java/io/github/ichillous/dsa/collections/ChainedHashMap.java) uses separate chaining and a maximum load factor of 0.75. It rejects null keys and values, so a null lookup result means the key is absent. A replacement returns the old value without changing the mapping count.

```java
import io.github.ichillous.dsa.collections.ChainedHashMap;

ChainedHashMap<String, Integer> counts = new ChainedHashMap<>(2);
counts.put("pear", 1);
Integer previous = counts.put("pear", 2);
counts.put("apple", 5);
assert previous == 1;
assert counts.size() == 2;
assert counts.get("pear") == 2;
assert counts.get("missing") == null;
```

## The invariant and the resize

Every key is stored in the bucket selected by its current hash and current capacity. Every bucket chain contains at most one mapping for an equality-equivalent key.

```text
bucket 0 -> [key A, value] -> [key C, value] -> null
bucket 1 -> null
bucket 2 -> [key B, value] -> null
```

Changing capacity changes bucket indices. Resizing therefore visits every mapping and places it into a new bucket; copying only the old bucket heads would not reestablish the invariant.

## Composition: an LRU cache

A cache also needs a rule for what to discard. [`LruCache`](../../src/main/java/io/github/ichillous/dsa/collections/LruCache.java) combines a hash map with a doubly linked list. A successful get or update promotes the entry to most recent; adding beyond capacity evicts the least recent entry.

```java
import io.github.ichillous.dsa.collections.LruCache;

LruCache<String, Integer> cache = new LruCache<>(2);
cache.put("A", 1);
cache.put("B", 2);
Integer hit = cache.get("A"); // Recency is now B, A.
cache.put("C", 3); // Evicts B.
assert hit == 1;
assert cache.get("B") == null;
assert cache.get("A") == 1 && cache.get("C") == 3;
assert cache.size() == 2;
```

The map gives direct access to a node; the list lets the cache move that known node or evict an endpoint in O(1). LRU is a policy about **recency**, not insertion order or frequency. A fixed capacity must be positive, and the cache rejects nulls.

## Costs and common mistakes

With well-distributed hashes, lookup/removal is O(1) expected and insertion is O(1) expected amortized. Adversarial collisions can make an operation O(n); a resize is O(n). Stored map space is O(n + capacity). LRU operations are expected O(1) under the underlying hash-map assumptions and store O(capacity) entries.

A key must retain stable equality and hash behavior while stored. Mutating a field used by its hash can strand the entry in the wrong bucket. Hash tables do not provide sorted iteration; use an ordered tree when ordering is part of the requirement.

## Practice

1. Explain why a replacement must not increment size.
2. Trace cache capacity 2 with `put A, put B, get A, put C`, then repeat with `put A` as an update.
3. Compare random map operations with `HashMap` under the same null policy; compare LRU behavior with an access-order `LinkedHashMap` model.

Continue with [hash functions](functions.md), [collision resolution](collision-resolution.md) and the [worked solutions](../../docs/solutions.md).
