package io.github.ichillous.dsa.visuals;

import io.github.ichillous.dsa.collections.LfuCache;
import io.github.ichillous.dsa.collections.OpenAddressingHashMap;
import io.github.ichillous.dsa.structures.BloomFilter;
import io.github.ichillous.dsa.structures.CountMinSketch;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks every value the hashing figures display. Slot positions inside the open-addressing
 * map, the Bloom filter and the Count-Min sketch are private, so each figure draws a conceptual
 * table built from the hashing rule documented in that class's Javadoc. These tests re-derive
 * every displayed index and counter here from the documented rule, assert the fixture against
 * that derivation, and assert the observable consequences through the public API (which lookups
 * succeed, which key is evicted, which absent key is reported possibly present, which estimate
 * is returned). They never read private state.
 */
class VisualExamplesHashingTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-hashing.properties");
    private static final String EMPTY = ".";

    // ---- hash-index-spread ------------------------------------------------------------------

    @Test
    void spreadingSeparatesKeysWhoseLowBitsCollide() {
        int capacity = DATA.number("hashing.spread.capacity");
        int slotBits = DATA.number("hashing.spread.slotBits");
        assertEquals(capacity, 1 << slotBits);
        List<Integer> keys = DATA.list("hashing.spread.keys");
        List<String> hashHex = DATA.strings("hashing.spread.hashHex");
        List<String> spreadHex = DATA.strings("hashing.spread.spreadHex");
        List<String> productHex = DATA.strings("hashing.spread.productHex");
        List<Integer> masked = DATA.list("hashing.spread.maskedSlots");
        List<Integer> slots = DATA.list("hashing.spread.slots");

        for (int i = 0; i < keys.size(); i++) {
            int hash = keys.get(i).hashCode();
            assertEquals(hashHex.get(i), hex(hash));
            assertEquals(masked.get(i), hash & (capacity - 1));
            int spread = hash ^ (hash >>> 16);
            assertEquals(spreadHex.get(i), hex(spread));
            int product = spread * 0x9E3779B9;
            assertEquals(productHex.get(i), hex(product));
            assertEquals(slots.get(i), product >>> (32 - slotBits));
        }
        assertEquals(masked.get(0), masked.get(1), "a plain mask would collide");
        assertNotEquals(slots.get(0), slots.get(1), "mixing separates the keys");

        OpenAddressingHashMap<Integer, String> map = new OpenAddressingHashMap<>(capacity);
        for (int key : keys) {
            assertNull(map.put(key, "v" + key));
        }
        List<String> layout = layout(capacity, keys);
        for (int i = 0; i < keys.size(); i++) {
            assertEquals(String.valueOf(keys.get(i)), layout.get(slots.get(i)), "each key sits in its home slot");
            assertEquals("v" + keys.get(i), map.get(keys.get(i)));
        }
    }

    // ---- open-addressing-probe --------------------------------------------------------------

    @Test
    void aTakenHomeSlotSendsTheProbeForwardAndWrapsPastTheEnd() {
        int capacity = DATA.number("hashing.probe.capacity");
        List<String> existing = DATA.strings("hashing.probe.existing");
        List<Integer> existingSlots = DATA.list("hashing.probe.existingSlots");
        String key = DATA.text("hashing.probe.key");
        int home = DATA.number("hashing.probe.home");
        List<Integer> path = DATA.list("hashing.probe.path");
        String absent = DATA.text("hashing.probe.absent");
        List<Integer> absentPath = DATA.list("hashing.probe.absentPath");

        OpenAddressingHashMap<String, String> map = new OpenAddressingHashMap<>(capacity);
        for (int i = 0; i < existing.size(); i++) {
            assertEquals(existingSlots.get(i), homeSlot(existing.get(i), capacity));
            assertNull(map.put(existing.get(i), "v" + existing.get(i)));
        }
        assertEquals(DATA.strings("hashing.probe.before"), layout(capacity, existing));
        assertTrue((existing.size() + 1) * 3 <= 2 * capacity, "the documented 2/3 load cap: no resize");

        assertEquals(home, homeSlot(key, capacity));
        assertEquals(home, homeSlot(absent, capacity), "the absent key shares the home slot");
        assertNull(map.put(key, "v" + key));
        List<String> withKey = new ArrayList<>(existing);
        withKey.add(key);
        List<String> afterLayout = layout(capacity, withKey);
        assertEquals(DATA.strings("hashing.probe.after"), afterLayout);
        assertEquals(path, probePathOver(afterLayout, key, capacity));
        assertEquals(capacity - 1, path.get(0), "the walk starts at the last slot");
        assertEquals(0, path.get(1), "and wraps to slot 0");

        assertEquals("v" + key, map.get(key));
        assertTrue(map.containsKey(key));
        assertFalse(map.containsKey(absent));
        assertEquals(absentPath, probePathOver(afterLayout, absent, capacity));
        assertEquals(EMPTY, afterLayout.get(absentPath.get(absentPath.size() - 1)),
                "an empty slot ends the absent search");
    }

    // ---- backward-shift-delete --------------------------------------------------------------

    @Test
    void backwardShiftKeepsEveryRemainingKeyReachableWhereClearingTheSlotDoesNot() {
        int capacity = DATA.number("hashing.shift.capacity");
        List<String> order = DATA.strings("hashing.shift.order");
        List<Integer> homes = DATA.list("hashing.shift.homes");
        String removed = DATA.text("hashing.shift.removed");
        int hole = DATA.number("hashing.shift.holeSlot");
        String lookup = DATA.text("hashing.shift.lookup");
        int lookupHome = DATA.number("hashing.shift.lookupHome");
        String stays = DATA.text("hashing.shift.stays");
        int staysSlot = DATA.number("hashing.shift.staysSlot");

        OpenAddressingHashMap<String, String> map = new OpenAddressingHashMap<>(capacity);
        for (int i = 0; i < order.size(); i++) {
            assertEquals(homes.get(i), homeSlot(order.get(i), capacity));
            map.put(order.get(i), "v" + order.get(i));
        }
        assertTrue((order.size() + 1) * 3 <= 2 * capacity, "the documented 2/3 load cap: no resize");
        List<String> before = layout(capacity, order);
        assertEquals(DATA.strings("hashing.shift.before"), before);
        assertEquals(removed, before.get(hole));
        assertEquals(lookupHome, homeSlot(lookup, capacity));
        assertEquals(DATA.number("hashing.shift.movedFrom"), before.indexOf(lookup));

        // Counterfactual: clearing only the removed slot. A lookup starts at its home slot and
        // stops at the first empty slot, so the moved key would be reported absent.
        List<String> holeOnly = new ArrayList<>(before);
        holeOnly.set(hole, EMPTY);
        assertEquals(EMPTY, holeOnly.get(lookupHome), "the lookup would start at an empty slot");
        List<Integer> naivePath = probePathOver(holeOnly, lookup, capacity);
        assertEquals(List.of(lookupHome), naivePath, "the naive search ends at once");
        assertFalse(naivePath.stream().anyMatch(slot -> holeOnly.get(slot).equals(lookup)),
                "so it never reaches the key stored one slot further on");

        // The real map backward-shifts instead. The documented invariant (every key sits on its
        // probe path from its home slot, with no tombstones) fixes the result: it is the layout of
        // the surviving keys inserted in their original order.
        assertEquals("v" + removed, map.remove(removed));
        List<String> survivors = new ArrayList<>(order);
        survivors.remove(removed);
        List<String> after = layout(capacity, survivors);
        assertEquals(DATA.strings("hashing.shift.after"), after);
        assertEquals(lookupHome, DATA.number("hashing.shift.movedTo"));
        assertEquals(lookup, after.get(lookupHome), "the key moved back to its home slot");
        assertEquals(stays, after.get(staysSlot), "a key already at its home slot stays");
        assertEquals(staysSlot, homeSlot(stays, capacity));
        assertEquals(EMPTY, after.get(DATA.number("hashing.shift.movedFrom")), "the vacated slot is cleared");
        for (String key : survivors) {
            assertEquals("v" + key, map.get(key), key + " is still reachable");
            assertEquals(key, after.get(lastOf(probePathOver(after, key, capacity))),
                    key + " lies on its probe path in the drawn layout");
        }
        assertNull(map.get(removed));
        assertEquals(order.size() - 1, map.size());
    }

    // ---- lfu-buckets ------------------------------------------------------------------------

    @Test
    void aHitMovesAKeyUpOneBucketAndTheLowestBucketsOldestKeyIsEvicted() {
        int capacity = DATA.number("hashing.lfu.capacity");
        LfuCache<String, Integer> cache = new LfuCache<>(capacity);
        List<String> puts = DATA.strings("hashing.lfu.puts");
        for (int i = 0; i < puts.size(); i++) {
            assertNull(cache.put(puts.get(i), i));
        }
        String firstHit = DATA.text("hashing.lfu.firstHit");
        assertEquals(puts.indexOf(firstHit), cache.get(firstHit));
        assertBuckets(cache, puts, "hashing.lfu.start");

        String hit = DATA.text("hashing.lfu.hit");
        assertEquals(puts.indexOf(hit), cache.get(hit));
        assertBuckets(cache, puts, "hashing.lfu.afterHit");

        // Recency inside bucket 2 is observable through eviction order: promote the lone
        // frequency-1 key into bucket 2 on a copy of this history, then insert a new key.
        LfuCache<String, Integer> replay = new LfuCache<>(capacity);
        puts.forEach(key -> replay.put(key, 0));
        replay.get(firstHit);
        replay.get(hit);
        List<String> lowest = DATA.strings("hashing.lfu.afterHit.f1");
        replay.get(lowest.get(0));
        replay.put("Z", 0);
        List<String> bucketTwo = DATA.strings("hashing.lfu.afterHit.f2");
        assertNull(replay.get(bucketTwo.get(0)), "the least recent key of the tied bucket goes first");
        assertEquals(0, replay.get(bucketTwo.get(1)));

        String inserted = DATA.text("hashing.lfu.insert");
        String victim = DATA.text("hashing.lfu.victim");
        assertEquals(capacity, cache.size());
        assertEquals(DATA.number("hashing.lfu.victimFrequency"), cache.frequency(victim));
        assertNull(cache.put(inserted, 99));
        assertEquals(0, cache.frequency(victim), "the victim is gone");
        assertNull(cache.get(victim));
        assertEquals(capacity, cache.size());
        assertEquals(1, cache.frequency(inserted));
        List<String> remaining = new ArrayList<>(puts);
        remaining.remove(victim);
        remaining.add(inserted);
        assertBuckets(cache, remaining, "hashing.lfu.afterInsert");
    }

    private static void assertBuckets(LfuCache<String, Integer> cache, List<String> keys, String prefix) {
        // Frequencies are public; each bucket's members must be exactly the keys of that frequency.
        for (int frequency = 1; frequency <= 3; frequency++) {
            String name = prefix + ".f" + frequency;
            List<String> expected = DATA.strings(name);
            List<String> actual = new ArrayList<>();
            for (String key : keys) {
                if (cache.frequency(key) == frequency) {
                    actual.add(key);
                }
            }
            List<String> sortedExpected = new ArrayList<>(expected);
            sortedExpected.sort(null);
            actual.sort(null);
            assertEquals(sortedExpected, actual, name);
        }
    }

    // ---- bloom-filter-bits ------------------------------------------------------------------

    @Test
    void theBloomFigureShowsTheRealProbesADefiniteMissAndARealFalsePositive() throws Exception {
        int bits = DATA.number("hashing.bloom.bitCount");
        int hashes = DATA.number("hashing.bloom.hashCount");
        BloomFilter<String> filter = new BloomFilter<>(bits, hashes);
        List<String> added = DATA.strings("hashing.bloom.added");
        List<List<Integer>> addedBits = List.of(DATA.list("hashing.bloom.addedBits0"),
                DATA.list("hashing.bloom.addedBits1"));
        for (int i = 0; i < added.size(); i++) {
            assertEquals(addedBits.get(i), probes(added.get(i), bits, hashes));
            filter.add(added.get(i));
            assertTrue(filter.mightContain(added.get(i)));
        }
        assertEquals(added.size(), filter.insertions());

        List<Integer> set = new ArrayList<>();
        addedBits.forEach(set::addAll);
        set.sort(null);
        assertEquals(DATA.list("hashing.bloom.setBits"), set);

        String miss = DATA.text("hashing.bloom.miss");
        List<Integer> missBits = DATA.list("hashing.bloom.missBits");
        assertEquals(missBits, probes(miss, bits, hashes));
        assertEquals(List.of(true, false), missBits.stream().map(set::contains).toList(),
                "the first bit is set but the second is clear");
        assertFalse(filter.mightContain(miss), "one clear bit proves the key was never added");

        String falsePositive = DATA.text("hashing.bloom.falsePositive");
        List<Integer> fpBits = DATA.list("hashing.bloom.falsePositiveBits");
        assertEquals(fpBits, probes(falsePositive, bits, hashes));
        assertTrue(set.containsAll(fpBits), "every probe lands on a set bit");
        assertFalse(added.contains(falsePositive));
        assertTrue(filter.mightContain(falsePositive), "a key that was never added reports possibly present");
        assertTrue(addedBits.get(0).contains(fpBits.get(0)) != addedBits.get(1).contains(fpBits.get(0)),
                "its bits come from different added keys");
    }

    // ---- count-min-rows ---------------------------------------------------------------------

    @Test
    void theCountMinFigureShowsRealCountersAndTheMinimumAsTheEstimate() throws Exception {
        int width = DATA.number("hashing.cms.width");
        int depth = DATA.number("hashing.cms.depth");
        List<String> items = DATA.strings("hashing.cms.items");
        long[] counts = DATA.longs("hashing.cms.counts");
        CountMinSketch<String> sketch = new CountMinSketch<>(width, depth);
        for (int i = 0; i < items.size(); i++) {
            sketch.add(items.get(i), counts[i]);
        }
        assertEquals(DATA.number("hashing.cms.total"), sketch.totalCount());

        long[][] table = new long[depth][width];
        for (int i = 0; i < items.size(); i++) {
            for (int row = 0; row < depth; row++) {
                table[row][column(items.get(i), row, width)] += counts[i];
            }
        }
        for (int row = 0; row < depth; row++) {
            assertArrayEqualsLong(DATA.longs("hashing.cms.row" + row), table[row]);
        }

        String query = DATA.text("hashing.cms.query");
        int[] columns = DATA.ints("hashing.cms.queryColumns");
        long[] values = DATA.longs("hashing.cms.queryCounters");
        long minimum = Long.MAX_VALUE;
        for (int row = 0; row < depth; row++) {
            assertEquals(columns[row], column(query, row, width));
            assertEquals(values[row], table[row][columns[row]]);
            minimum = Math.min(minimum, values[row]);
        }
        assertEquals(DATA.number("hashing.cms.estimate"), minimum);
        assertEquals(minimum, sketch.estimate(query));
        long trueCount = counts[items.indexOf(query)];
        assertEquals(DATA.number("hashing.cms.trueCount"), trueCount);
        assertEquals(trueCount, sketch.estimate(query));
        assertTrue(Arrays.stream(values).anyMatch(value -> value > trueCount),
                "some counters are inflated by colliding keys");
        for (int i = 0; i < items.size(); i++) {
            assertTrue(sketch.estimate(items.get(i)) >= counts[i], "estimates never undercount");
        }
    }

    // ---- helpers ----------------------------------------------------------------------------

    private static String hex(int value) {
        return String.format("%08x", value);
    }

    private static int homeSlot(Object key, int capacity) {
        int h = key.hashCode();
        h ^= h >>> 16;
        return (h * 0x9E3779B9) >>> (Integer.numberOfLeadingZeros(capacity) + 1);
    }

    /**
     * The layout the documented rule describes: each key is placed in its home slot, or in the
     * next free slot after it, wrapping from the last slot to slot 0. "." marks an empty slot.
     */
    private static List<String> layout(int capacity, List<?> insertionOrder) {
        String[] slots = new String[capacity];
        Arrays.fill(slots, EMPTY);
        for (Object key : insertionOrder) {
            int slot = homeSlot(key, capacity);
            while (!slots[slot].equals(EMPTY)) {
                slot = (slot + 1) & (capacity - 1);
            }
            slots[slot] = key.toString();
        }
        return List.of(slots);
    }

    private static int lastOf(List<Integer> path) {
        return path.get(path.size() - 1);
    }

    /** The slots a lookup visits: from the home slot to the key's slot, or to the first empty slot. */
    private static List<Integer> probePathOver(List<String> layout, String key, int capacity) {
        List<Integer> path = new ArrayList<>();
        int slot = homeSlot(key, capacity);
        while (true) {
            path.add(slot);
            if (layout.get(slot).equals(EMPTY) || layout.get(slot).equals(key)) {
                return path;
            }
            slot = (slot + 1) & (capacity - 1);
        }
    }

    /** The documented Bloom rule: SplitMix64, then double hashing with a step in [1, m - 1]. */
    private static List<Integer> probes(String element, int bits, int hashes) {
        long mixed = splitMix(element.hashCode());
        long h1 = (int) mixed;
        long step = 1 + Math.floorMod((int) (mixed >>> 32), bits - 1);
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < hashes; i++) {
            result.add((int) Math.floorMod(h1 + i * step, (long) bits));
        }
        return result;
    }

    /** The documented Count-Min rule: row i hashes with floorMod(splitMix64(hashCode ^ seed[i]), width). */
    private static int column(String item, int row, int width) {
        long seed = splitMix(0x5EED5EED5EED5EEDL + row);
        return (int) Math.floorMod(splitMix(item.hashCode() ^ seed), (long) width);
    }

    private static long splitMix(long x) {
        long z = x + 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private static void assertArrayEqualsLong(long[] expected, long[] actual) {
        assertEquals(Arrays.toString(expected), Arrays.toString(actual));
    }
}
