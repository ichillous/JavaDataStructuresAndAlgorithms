"""Hashing, cache and sketch figures; fixtures in examples-hashing.properties.

Slot positions inside OpenAddressingHashMap, BloomFilter and CountMinSketch are private, so
these figures draw conceptual tables that follow the hashing rule documented in each class's
Javadoc. Every displayed index and counter is read from the fixture file; VisualExamplesHashingTest
recomputes each one from the documented rule and from the real objects.
"""

from drawing import ints

CATEGORY = "Hashing and sketches"
EMPTY = "."


def _strings(fixtures, key):
    value = fixtures[key].strip()
    return [part.strip() for part in value.split(",")] if value else []


def _slots(fixtures, key):
    """A slot list from the fixture: '.' becomes an empty cell."""
    return ["" if part == EMPTY else part for part in _strings(fixtures, key)]


# ---------------------------------------------------------------- hash-index-spread

SPREAD_IDEA = "Mixing lets high bits pick the slot, so such keys stop colliding."
HEX_X, HEX_CELL, HEX_GAP = 122, 42, 4


def hash_index_spread(c, fixtures):
    """Two keys whose low bits agree: a plain mask collides, spreading and multiplying separate them."""
    capacity = int(fixtures["hashing.spread.capacity"])
    bits = int(fixtures["hashing.spread.slotBits"])
    keys = _strings(fixtures, "hashing.spread.keys")
    hash_hex = _strings(fixtures, "hashing.spread.hashHex")
    masked = ints(fixtures, "hashing.spread.maskedSlots")
    spread_hex = _strings(fixtures, "hashing.spread.spreadHex")
    product_hex = _strings(fixtures, "hashing.spread.productHex")
    slots = ints(fixtures, "hashing.spread.slots")

    def hex_rows(y, digits, focus_index, state):
        """Two rows of eight hex digits, one per key; returns the y below the cells."""
        for row, (key, value) in enumerate(zip(keys, digits)):
            top = y + row * 48
            c.text(30, top + 27, key, size=18, color="muted")
            states = [None] * 8
            states[focus_index] = state
            c.cell_row(HEX_X, top, list(value), cell=HEX_CELL, gap=HEX_GAP, height=40, states=states)
        return y + 88

    y = c.step(156, 1, f"A mask keeps only the low {bits} bits")
    y = hex_rows(y + 4, hash_hex, 7, "bad")
    c.text(30, y + 22, f"hashCode & {capacity - 1} gives slot {masked[0]} and slot {masked[1]}: a collision",
           size=18, color="red", weight="700")

    y = c.step(y + 62, 2, "Spread: h ^ (h >>> 16)")
    y = hex_rows(y + 4, spread_hex, 7, "good")
    c.text(30, y + 22, f"The high half now reaches the low {bits} bits: {spread_hex[0][-1]} and {spread_hex[1][-1]}",
           size=18, color="green", weight="700")

    y = c.step(y + 62, 3, f"Multiply, keep the top {bits} bits")
    y = hex_rows(y + 4, product_hex, 0, "focus")
    c.text(30, y + 22, f"Times 0x9E3779B9: slot {slots[0]} and slot {slots[1]} of {capacity}",
           size=18, color="blue", weight="700")

    y = c.legend(y + 58, [("bad", "mask keeps"), ("good", "after spreading"), ("focus", "top bits: the slot")])
    c.note(y + 14, SPREAD_IDEA)


# ---------------------------------------------------------------- table rows shared below

ROW_X, CELL, GAP = 39, 50, 6


def _x(index):
    return ROW_X + index * (CELL + GAP) + CELL / 2


def _table(c, y, slots, states, index_labels=True):
    labels = list(range(len(slots))) if index_labels else None
    return c.cell_row(ROW_X, y, slots, cell=CELL, gap=GAP, states=states, labels=labels)


# ---------------------------------------------------------------- open-addressing-probe

PROBE_IDEA = "A collision moves to the next slot and wraps past the end; a lookup retraces it to the first empty slot."


def open_addressing_probe(c, fixtures):
    """put(L) finds its home slot taken, walks to the next slot and wraps; a lookup retraces it."""
    existing = _strings(fixtures, "hashing.probe.existing")
    existing_slots = ints(fixtures, "hashing.probe.existingSlots")
    after = _slots(fixtures, "hashing.probe.after")
    key = fixtures["hashing.probe.key"]
    home = int(fixtures["hashing.probe.home"])
    path = ints(fixtures, "hashing.probe.path")
    absent = fixtures["hashing.probe.absent"]
    absent_path = ints(fixtures, "hashing.probe.absentPath")
    landed = path[-1]

    # 1. put(L): the home slot is taken, so the probe moves on and wraps.
    y = c.step(156, 1, f"put({key}): home slot {home} is taken")
    states = ["focus" if value else "muted" for value in after]
    states[home] = "warn"
    states[landed] = "good"
    row_y = y + 70
    c.curve(_x(home), row_y - 6, _x(landed), row_y - 6, bend=-34, color="blue",
            label=f"{home} taken, wrap to {landed}")
    y = _table(c, row_y, after, states)
    homes = ", ".join(f"{k} {s}" for k, s in zip(existing + [key], existing_slots + [home]))
    c.text(39, y + 20, f"Home slots: {homes}", size=18, color="muted")
    c.text(39, y + 48, f"{key} lands in slot {landed}, the first free slot on its path",
           size=18, color="green", weight="700")

    # 2. get(T) retraces the same path and ends at an empty slot.
    y = c.step(y + 88, 2, f"get({absent}) also has home slot {home}")
    states = ["focus" if value else "muted" for value in after]
    for slot in absent_path[:-1]:
        states[slot] = "warn"
    states[absent_path[-1]] = "bad"
    y = _table(c, y + 6, after, states)
    walk = ", ".join(str(slot) for slot in absent_path)
    c.text(39, y + 20, f"Visits {walk}: slot {absent_path[-1]} is empty, so {absent} is absent",
           size=18, color="red", weight="700")

    y = c.legend(y + 66, [("focus", "stored key"), ("good", f"{key} placed"), ("warn", "taken or visited"),
                          ("bad", "empty slot ends search"), ("muted", "empty slot")])
    c.note(y + 14, PROBE_IDEA)


# ---------------------------------------------------------------- backward-shift-delete

SHIFT_IDEA = "Shifting later keys back keeps every key reachable."


def backward_shift_delete(c, fixtures):
    """Remove H from the middle of a cluster: clearing only its slot loses M; shifting M back keeps it."""
    order = _strings(fixtures, "hashing.shift.order")
    homes = ints(fixtures, "hashing.shift.homes")
    before = _slots(fixtures, "hashing.shift.before")
    after = _slots(fixtures, "hashing.shift.after")
    removed = fixtures["hashing.shift.removed"]
    hole = int(fixtures["hashing.shift.holeSlot"])
    lookup = fixtures["hashing.shift.lookup"]
    lookup_home = int(fixtures["hashing.shift.lookupHome"])
    moved_from = int(fixtures["hashing.shift.movedFrom"])
    moved_to = int(fixtures["hashing.shift.movedTo"])
    stays = fixtures["hashing.shift.stays"]
    stays_slot = int(fixtures["hashing.shift.staysSlot"])

    # 1. The cluster, with the key to remove in its middle.
    y = c.step(156, 1, f"A cluster: {', '.join(order)}")
    states = ["muted" if not value else ("warn" if value == removed else "focus") for value in before]
    y = _table(c, y + 6, before, states)
    shown = ", ".join(f"{k} {h}" for k, h in zip(order, homes))
    c.text(39, y + 20, f"Home slots: {shown}", size=18, color="muted")

    # 2. Clearing only the slot breaks the lookup.
    y = c.step(y + 58, 2, f"Clear only slot {hole} to remove {removed}")
    holed = list(before)
    holed[hole] = ""
    states = ["muted" if not value else "focus" for value in holed]
    states[hole] = "bad"
    states[moved_from] = "warn"
    row_y = y + 52
    c.pointer(_x(lookup_home), row_y, f"get({lookup}) starts here", color="red", below=False)
    y = _table(c, row_y, holed, states, index_labels=False)
    c.text(39, y + 24, f"Slot {hole} is empty, so {lookup} is wrongly absent", size=18, color="red",
           weight="700")

    # 3. Backward shift repairs the hole.
    y = c.step(y + 62, 3, f"Shift {lookup} back instead")
    states = ["muted" if not value else "focus" for value in after]
    states[moved_to] = "good"
    states[stays_slot] = "alt"
    row_y = y + 28
    c.curve(_x(moved_from), row_y - 6, _x(moved_to), row_y - 6, bend=-14, color="green")
    y = _table(c, row_y, after, states, index_labels=False)
    c.text(39, y + 24, f"{lookup} moves from slot {moved_from} to {moved_to}; slot {moved_from} is cleared",
           size=18, color="green", weight="700")
    c.text(39, y + 52, f"{stays} stays: it is already at its home slot {stays_slot}", size=18, color="violet",
           weight="700")

    y = c.legend(y + 82, [("focus", "stored"), ("warn", "to remove"), ("bad", "hole"),
                          ("good", "moved back"), ("alt", "left in place"), ("muted", "empty")])
    c.note(y + 14, SHIFT_IDEA)


# ---------------------------------------------------------------- lfu-buckets

LFU_IDEA = "Hits lift keys; the oldest key in the lowest bucket is evicted."
LFU_X, LFU_CELL, LFU_GAP = 110, 56, 8


def lfu_buckets(c, fixtures):
    """LfuCache(3): put A, B, C and hit C; then a hit on B lifts it; then put(D) evicts A."""
    capacity = int(fixtures["hashing.lfu.capacity"])
    puts = _strings(fixtures, "hashing.lfu.puts")
    first_hit = fixtures["hashing.lfu.firstHit"]
    hit = fixtures["hashing.lfu.hit"]
    inserted = fixtures["hashing.lfu.insert"]
    victim = fixtures["hashing.lfu.victim"]

    def bucket(prefix, frequency):
        return _strings(fixtures, f"hashing.lfu.{prefix}.f{frequency}")

    def rows(y, buckets, marks, notes):
        """Buckets 1 and 2 as rows of keys, least recent first; ``notes`` maps a frequency to
        ``(text, color)`` drawn to the right of its row. Returns the y below the rows."""
        for frequency in (1, 2):
            keys = buckets[frequency]
            top = y + (frequency - 1) * 52
            c.text(30, top + 29, f"freq {frequency}", size=18, color="muted", weight="700")
            c.cell_row(LFU_X, top, keys, cell=LFU_CELL, gap=LFU_GAP, height=44,
                       states=[marks.get(key) for key in keys])
            if frequency in notes:
                text, color = notes[frequency]
                c.text(LFU_X + len(keys) * (LFU_CELL + LFU_GAP) + 6, top + 29, text, size=18, color=color,
                       weight="700")
        return y + 96

    y = c.step(156, 1, f"Capacity {capacity}: put {', '.join(puts)}, get {first_hit}")
    y = rows(y + 4, {1: bucket("start", 1), 2: bucket("start", 2)}, {first_hit: "focus"},
             {2: (f"{first_hit} was hit", "blue")})
    c.text(30, y + 24, "Left is least recent, right is most recent", size=18, color="muted")

    y = c.step(y + 62, 2, f"get({hit}) lifts {hit} to freq 2")
    y = rows(y + 4, {1: bucket("afterHit", 1), 2: bucket("afterHit", 2)}, {hit: "focus"},
             {2: (f"{hit} was hit", "blue")})

    y = c.step(y + 40, 3, f"put({inserted}) on a full cache")
    # The victim leaves bucket 1 and the new key takes its place; both are drawn side by side.
    y = rows(y + 4, {1: [victim] + bucket("afterInsert", 1), 2: bucket("afterInsert", 2)},
             {victim: "bad", inserted: "focus"}, {1: (f"{victim} out, {inserted} in", "red")})
    c.text(30, y + 24, f"{victim} is the oldest key of the lowest bucket", size=18, color="red", weight="700")

    y = c.legend(y + 58, [("focus", "just hit or added"), ("bad", "evicted victim")])
    c.note(y + 14, LFU_IDEA)


# ---------------------------------------------------------------- bloom-filter-bits

BLOOM_IDEA = "One clear bit proves absence; all-set bits mean only possibly present."
BIT_X, BIT_CELL, BIT_GAP = 29, 26, 3


def bloom_filter_bits(c, fixtures):
    """A 16-bit filter with k = 2: two keys set bits, kiwi hits a clear bit, plum hits only set bits."""
    bits = int(fixtures["hashing.bloom.bitCount"])
    added = _strings(fixtures, "hashing.bloom.added")
    added_bits = [ints(fixtures, "hashing.bloom.addedBits0"), ints(fixtures, "hashing.bloom.addedBits1")]
    set_bits = set(ints(fixtures, "hashing.bloom.setBits"))
    miss = fixtures["hashing.bloom.miss"]
    miss_bits = ints(fixtures, "hashing.bloom.missBits")
    positive = fixtures["hashing.bloom.falsePositive"]
    positive_bits = ints(fixtures, "hashing.bloom.falsePositiveBits")

    def row(y, states):
        values = ["1" if i in set_bits else "0" for i in range(bits)]
        return c.cell_row(BIT_X, y, values, cell=BIT_CELL, gap=BIT_GAP, height=40, states=states,
                          labels=list(range(bits)))

    y = c.step(156, 1, f"add({added[0]}) and add({added[1]}) set bits")
    states = ["muted"] * bits
    for index in added_bits[0]:
        states[index] = "focus"
    for index in added_bits[1]:
        states[index] = "alt"
    y = row(y + 6, states)
    left = 39
    left += c.chip(left, y + 8, f"{added[0]}: bits {added_bits[0][0]}, {added_bits[0][1]}", color="blue") + 12
    c.chip(left, y + 8, f"{added[1]}: bits {added_bits[1][0]}, {added_bits[1][1]}", color="violet")

    y = c.step(y + 76, 2, f"mightContain({miss}) reads bits {miss_bits[0]}, {miss_bits[1]}")
    states = [None if i in set_bits else "muted" for i in range(bits)]
    states[miss_bits[0]] = "warn"
    states[miss_bits[1]] = "bad"
    y = row(y + 6, states)
    c.text(39, y + 20, f"Bit {miss_bits[1]} is 0, so {miss} was never added", size=18, color="red", weight="700")

    y = c.step(y + 58, 3, f"mightContain({positive}) reads bits {positive_bits[0]}, {positive_bits[1]}")
    states = [None if i in set_bits else "muted" for i in range(bits)]
    for index in positive_bits:
        states[index] = "warn"
    y = row(y + 6, states)
    c.text(39, y + 20, f"Both are 1, yet {positive} was never added", size=18, color="gold", weight="700")
    c.text(39, y + 48, f"{added[1]} set bit {positive_bits[0]} and {added[0]} set bit {positive_bits[1]}",
           size=18, color="muted")

    y = c.legend(y + 90, [("focus", f"set by {added[0]}"), ("alt", f"set by {added[1]}"),
                          ("warn", "read as 1"), ("bad", "read as 0"), ("muted", "clear bit")])
    c.note(y + 14, BLOOM_IDEA)


# ---------------------------------------------------------------- count-min-rows

CMS_IDEA = "Rows only overcount, so a key's smallest counter is its estimate."
CMS_X, CMS_CELL_W, CMS_CELL_H = 96, 48, 44


def count_min_rows(c, fixtures):
    """A 3 x 8 sketch after five keys: pear's three counters hold 2, 3 and 1; the minimum is the estimate."""
    width = int(fixtures["hashing.cms.width"])
    depth = int(fixtures["hashing.cms.depth"])
    items = _strings(fixtures, "hashing.cms.items")
    counts = ints(fixtures, "hashing.cms.counts")
    table = [ints(fixtures, f"hashing.cms.row{row}") for row in range(depth)]
    query = fixtures["hashing.cms.query"]
    columns = ints(fixtures, "hashing.cms.queryColumns")
    values = ints(fixtures, "hashing.cms.queryCounters")
    estimate = int(fixtures["hashing.cms.estimate"])
    true_count = int(fixtures["hashing.cms.trueCount"])
    minimum_row = values.index(min(values))
    row_labels = [f"row {row}" for row in range(depth)]
    col_labels = list(range(width))

    y = c.step(156, 1, f"Add {len(items)} keys to a {depth} x {width} table")
    c.text(39, y + 6, ", ".join(f"{k} x{n}" for k, n in zip(items, counts)), size=18, color="muted")
    y = c.grid(CMS_X, y + 52, table, cell_w=CMS_CELL_W, cell_h=CMS_CELL_H, row_labels=row_labels,
               col_labels=col_labels)

    y = c.step(y + 40, 2, f"Read {query}'s counter in each row")
    states = {(row, columns[row]): ("good" if row == minimum_row else "warn") for row in range(depth)}
    y = c.grid(CMS_X, y + 34, table, cell_w=CMS_CELL_W, cell_h=CMS_CELL_H, row_labels=row_labels,
               col_labels=col_labels, states=states)
    shown = ", ".join(str(v) for v in values)
    c.text(39, y + 26, f"{query} reads counters {shown}; estimate = min = {estimate}", size=18,
           color="green", weight="700")
    c.text(39, y + 54, f"True count of {query}: {true_count}. An estimate is never lower.", size=18,
           color="muted")

    y = c.legend(y + 90, [("good", "smallest: the estimate"), ("warn", "inflated by other keys")])
    c.note(y + 14, CMS_IDEA)


FIGURES = [
    {
        "id": "hash-index-spread",
        "title": "From hash code to slot",
        "category": CATEGORY,
        "description": ("Integer keys 65536 and 131072 have hash codes 0x00010000 and 0x00020000, whose low "
                        "four bits are both 0, so a plain mask sends both to slot 0. Spreading with h ^ (h >>> 16) "
                        "and multiplying by 0x9E3779B9 gives slots 1 and 2 of 16."),
        "lesson": "data-structures/hash-tables/functions.md",
        "keyIdea": SPREAD_IDEA,
        "height": 870,
        "draw": hash_index_spread,
    },
    {
        "id": "open-addressing-probe",
        "title": "Probing past a taken slot",
        "category": CATEGORY,
        "description": ("In an 8-slot table holding K, B and G, put(L) finds home slot 7 taken by G, wraps to slot 0 "
                        "and stores L there; get(T), also homed at 7, visits slots 7, 0 and 1 and stops at the "
                        "empty slot 1."),
        "lesson": "data-structures/hash-tables/open-addressing.md",
        "keyIdea": PROBE_IDEA,
        "height": 810,
        "draw": open_addressing_probe,
    },
    {
        "id": "backward-shift-delete",
        "title": "Backward-shift deletion",
        "category": CATEGORY,
        "description": ("In the cluster C, H, M, B at slots 3 to 6, clearing only H's slot 4 would hide M, which is "
                        "stored at slot 5 but starts its search at slot 4. Backward-shift deletion moves M to slot 4, "
                        "clears slot 5 and leaves B at its home slot 6."),
        "lesson": "data-structures/hash-tables/open-addressing.md",
        "keyIdea": SHIFT_IDEA,
        "height": 875,
        "draw": backward_shift_delete,
    },
    {
        "id": "lfu-buckets",
        "title": "LFU frequency buckets",
        "category": CATEGORY,
        "description": ("A capacity-3 LfuCache after put A, B, C and get C holds A and B in bucket 1 and C in bucket 2. "
                        "get(B) moves B to the end of bucket 2, and put(D) then evicts A, the oldest key of bucket 1."),
        "lesson": "data-structures/hash-tables/lfu-cache.md",
        "keyIdea": LFU_IDEA,
        "height": 840,
        "draw": lfu_buckets,
    },
    {
        "id": "bloom-filter-bits",
        "title": "Bloom filter bits",
        "category": CATEGORY,
        "description": ("A 16-bit Bloom filter with two probes per key sets bits 8, 11, 1 and 3 for apple and pear. "
                        "kiwi reads bits 8 and 7 and bit 7 is clear, a definite miss; plum reads bits 1 and 11, both "
                        "set, a false positive."),
        "lesson": "data-structures/probabilistic/overview.md",
        "keyIdea": BLOOM_IDEA,
        "height": 870,
        "draw": bloom_filter_bits,
    },
    {
        "id": "count-min-rows",
        "title": "Count-Min rows",
        "category": CATEGORY,
        "description": ("A Count-Min sketch of 3 rows by 8 columns after adding apple 3, pear 1, fig 2, plum 4 and "
                        "kiwi 1. pear reads counters 2, 3 and 1 in its three rows, and the minimum, 1, is the estimate."),
        "lesson": "data-structures/probabilistic/overview.md",
        "keyIdea": CMS_IDEA,
        "height": 870,
        "draw": count_min_rows,
    },
]
