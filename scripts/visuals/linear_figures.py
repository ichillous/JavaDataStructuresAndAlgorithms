"""Fixture-backed portrait figures for arrays, links, buffers and caches."""

from drawing import ints


ARRAY_IDEA = (
    "Move the suffix right to left before writing the new value. "
    "Otherwise an unread value can be overwritten."
)
STACK_IDEA = (
    "The top is the final live array element. Appending and removing "
    "there gives last-in, first-out order."
)
LINKED_IDEA = (
    "Repair both neighbors, then detach the removed node. "
    "A forward link and its backward link must agree."
)
QUEUE_IDEA = (
    "At logical offset i, use (head + i) % capacity. "
    "Dequeue follows logical order, never raw slot order."
)
HASH_IDEA = (
    "A shared hash only selects a bucket. Equality still distinguishes "
    "the mappings; a collision must preserve both."
)
LRU_IDEA = (
    "A hit changes recency. On overflow, evict the left end; "
    "the map points straight to the node to move."
)


def array_insertion(c, fixtures):
    """Show actual copied contents before the insertion position is overwritten."""
    before = ints(fixtures, "array.before")
    after = ints(fixtures, "array.after")
    index = int(fixtures["array.index"])
    value = int(fixtures["array.value"])
    labels = [str(i) for i in range(len(after))]
    shifted = [*before, "free"]
    for position in range(len(before) - 1, index - 1, -1):
        shifted[position + 1] = before[position]

    c.text(28, 151, f"1  Before: size = {len(before)}", weight="700")
    c.cells(100, 180, [*before, "free"], cell=96, gap=16, labels=labels)
    c.text(28, 286, f"2  Shift right: index {index} → {index + 1}", weight="700")
    start = 100 + index * 112 + 48
    c.arrow(start, 316, start + 112, 316)
    c.cells(100, 342, shifted, cell=96, gap=16, active=(index + 1,), labels=labels)
    target_left = 100 + index * 112
    c.rect(target_left, 342, 96, 48, fill="none", stroke="red", dashed=True)
    c.text(target_left + 48, 448, "old copy: still here", anchor="middle", size=18)
    c.text(28, 486, f"3  Write {value}, then size becomes {len(after)}", weight="700")
    c.cells(100, 514, after, cell=96, gap=16, active=(index,), labels=labels)
    c.note(628, ARRAY_IDEA)


def stack_lifo(c, fixtures):
    """Show the final live element and the same endpoint used by both operations."""
    values = ints(fixtures, "stack.input")
    popped = ints(fixtures, "stack.popped")
    newest = values[-1]
    remaining = values[:-1]
    push_order = " → ".join(map(str, values))

    def vertical_stack(y, live, highlight=False):
        for row, value in enumerate(reversed(live)):
            top = y + row * 62
            active = highlight and row == 0
            c.rect(180, top, 160, 54, fill="soft" if active else "paper",
                   stroke="blue" if active else "line")
            c.text(260, top + 35, value, anchor="middle", weight="700" if active else "400")
            c.text(370, top + 35, f"index {len(live) - row - 1}", color="muted", size=18)
        c.text(102, y + 35, "top", anchor="middle", weight="700", size=18)
        c.arrow(130, y + 28, 170, y + 28)

    c.text(28, 151, f"Push order: {push_order}", weight="700")
    c.text(28, 184, "Top = final live array element", color="muted", size=18)
    c.text(200, 212, f"1  push({newest})", anchor="middle", color="blue")
    c.text(320, 212, f"2  pop → {popped[0]}", anchor="middle", color="red")
    # Both operations cross the same upper edge, in opposite directions.
    c.arrow(220, 226, 220, 272)
    c.arrow(300, 272, 300, 226, color="red")
    vertical_stack(280, values, highlight=True)
    c.text(260, 494, "bottom = first value pushed", anchor="middle", size=18)

    c.text(28, 541, "3  After pop: the top moves down", weight="700")
    vertical_stack(570, remaining, highlight=True)
    c.text(260, 728, f"pop() returned {popped[0]}; top is now {remaining[-1]}", anchor="middle")
    c.text(28, 766, "Full pop order:", weight="700")
    c.text(260, 802, " → ".join(map(str, popped)), anchor="middle", weight="700")
    c.paragraph(28, 850, "Stack: push and pop share one end. A FIFO queue uses opposite ends.",
                width=42)
    c.note(932, STACK_IDEA)


def _bidirectional(c, first, second, y, color="blue"):
    """Parallel arrows show next above and previous below without hiding either."""
    c.arrow(first, y - 10, second, y - 10, color=color)
    c.arrow(second, y + 10, first, y + 10, color=color)


def linked_unlink(c, fixtures):
    """Depict a known middle node; its discovery cost is outside the local unlink."""
    before = ints(fixtures, "linked.before")
    after = ints(fixtures, "linked.after")
    removed = before[int(fixtures["linked.index"])]

    c.text(28, 151, f"1  Before: remove node {removed}", weight="700")
    c.text(100, 187, "head", anchor="middle", color="muted", size=18)
    c.text(420, 187, "tail", anchor="middle", color="muted", size=18)
    for x, value in zip((100, 260, 420), before):
        c.node(x, 233, value, radius=32, color="red" if value == removed else None)
    _bidirectional(c, 142, 218, 233)
    _bidirectional(c, 302, 378, 233)
    c.text(260, 290, "selected for removal", anchor="middle", color="red", size=18)

    c.text(28, 342, "2  Repair both neighboring links", weight="700")
    c.text(52, 380, f"{after[0]}.next → {after[1]}")
    c.text(52, 414, f"{after[1]}.previous → {after[0]}")
    c.text(28, 466, f"3  After: size = {len(after)}", weight="700")
    c.node(100, 515, after[0], radius=32)
    c.node(420, 515, after[1], radius=32)
    _bidirectional(c, 142, 378, 515)
    c.text(100, 570, "head", anchor="middle", color="muted", size=18)
    c.text(420, 570, "tail", anchor="middle", color="muted", size=18)

    c.node(260, 640, removed, radius=32, color="red")
    c.text(260, 696, "Detached: previous = next = null", anchor="middle", size=18)
    c.note(734, LINKED_IDEA)


def queue_wrap(c, fixtures):
    """Show slot order and dequeue order separately, including the wrap edge."""
    capacity = int(fixtures["queue.capacity"])
    initial = ints(fixtures, "queue.input")
    removed = ints(fixtures, "queue.removed")
    appended = ints(fixtures, "queue.append")
    physical = ints(fixtures, "queue.physical")
    logical = ints(fixtures, "queue.after")
    head = int(fixtures["queue.head"])
    slots = [str(i) for i in range(capacity)]

    c.text(28, 151, f"1  Full buffer: capacity = {capacity}", weight="700")
    c.cells(60, 183, initial, cell=88, gap=16, labels=slots)
    c.text(104, 282, "head = 0", anchor="middle", color="blue", size=18)
    removed_text = ", ".join(map(str, removed))
    appended_text = ", ".join(map(str, appended))
    c.paragraph(28, 329, f"Dequeue {removed_text}; enqueue {appended_text}.", width=42)

    c.text(28, 390, "2  Physical slots after wrap", weight="700")
    c.cells(60, 420, physical, cell=88, gap=16, active=(head,), labels=slots)
    head_x = 60 + head * 104 + 44
    c.arrow(head_x, 531, head_x, 508)
    c.text(head_x, 551, f"head = {head}", anchor="middle", color="blue", size=18)
    # This arrow is specifically the physical last-slot-to-zero transition.
    c.path("M 460 444 H 478 V 584 H 42 V 444 H 54", color="blue", arrow=True)
    order = " → ".join(str((head + offset) % capacity) for offset in range(len(logical)))
    c.text(260, 618, f"Read slots {order}", anchor="middle", size=18)

    c.text(28, 674, "3  FIFO order: next out on the left", weight="700")
    c.cells(60, 704, logical, cell=88, gap=16, active=(0,),
            labels=[f"offset {i}" for i in range(len(logical))])
    c.text(104, 818, "next out", anchor="middle", color="blue", size=18)
    c.note(855, QUEUE_IDEA)


def hash_chaining(c, fixtures):
    """Keep equal hashes visibly distinct from equality-equivalent keys."""
    keys = fixtures["hash.keys"].split(",")
    hash_code = int(fixtures["hash.hash"])
    capacity = len(keys) * 2
    bucket = hash_code % capacity
    # The maintained map prepends a new mapping to the bucket's chain.
    chain = list(reversed(keys))

    c.text(28, 151, "1  Distinct keys, equal hash codes", weight="700")
    for x, key in zip((52, 300), keys):
        c.rect(x, 180, 168, 62, fill="soft", stroke="blue")
        c.text(x + 84, 219, f'"{key}"', anchor="middle", weight="700")
        c.arrow(x + 84, 248, x + 84, 280)
        c.text(x + 84, 315, f"hash = {hash_code}", anchor="middle", size=18)
    c.text(260, 358, f"floorMod({hash_code}, {capacity}) = {bucket}", anchor="middle")

    c.text(28, 403, "2  One bucket, two separate mappings", weight="700")
    for slot in range(capacity):
        top = 434 + slot * 66
        c.rect(52, top, 88, 50, fill="soft" if slot == bucket else "panel",
               stroke="blue" if slot == bucket else "line")
        if slot == bucket:
            c.text(96, top + 32, slot, anchor="middle", weight="700")
        else:
            c.text(96, top + 32, f"{slot} empty", anchor="middle", color="muted", size=18)
    c.arrow(148, 459, 190, 459)
    c.rect(200, 434, 180, 50)
    c.text(290, 467, f'key = "{chain[0]}"', anchor="middle")
    c.text(402, 467, "head", color="muted", size=18)
    c.arrow(290, 491, 290, 546)
    c.rect(200, 556, 180, 50)
    c.text(290, 589, f'key = "{chain[1]}"', anchor="middle")
    c.text(402, 589, "next", color="muted", size=18)
    c.arrow(290, 614, 290, 667)
    c.text(290, 697, "null: end of chain", anchor="middle", size=18)
    c.text(28, 742, "Lookup checks equality along the chain.")
    c.note(780, HASH_IDEA)


def lru_recency(c, fixtures):
    """Trace an access before overflow, so eviction follows recency rather than age."""
    capacity = int(fixtures["lru.capacity"])
    before = fixtures["lru.before"].split(",")
    after_hit = fixtures["lru.afterHit"].split(",")
    after_put = fixtures["lru.afterPut"].split(",")
    hit = after_hit[-1]
    added = after_put[-1]
    evicted = next(key for key in after_hit if key not in after_put)
    endpoint_labels = ["least recent", "most recent"]

    def row(y, values, active=()):
        c.cells(96, y, values, cell=136, gap=56, active=active, labels=endpoint_labels)
        _bidirectional(c, 242, 278, y + 24)

    c.text(28, 151, f"1  Capacity = {capacity}; fill the cache", weight="700")
    c.text(28, 184, "; ".join(f"put({key})" for key in before), color="muted", size=18)
    row(213, before)
    c.text(28, 334, f"2  get({hit}): move the hit to the right", weight="700")
    row(367, after_hit, active=(len(after_hit) - 1,))
    c.text(28, 490, f"3  put({added}): evict {evicted}, append {added}", weight="700")
    row(523, after_put, active=(len(after_put) - 1,))
    c.node(260, 667, evicted, radius=30, color="red")
    c.text(260, 721, "Evicted: least recent before the put", anchor="middle", size=18)
    c.note(760, LRU_IDEA)


FIGURES = [
    {
        "id": "array-insertion",
        "title": "Array insertion: make a gap",
        "category": "Linear structures",
        "description": "Shift the suffix before writing the new value into the live array.",
        "lesson": "data-structures/arrays/operations.md",
        "keyIdea": ARRAY_IDEA,
        "height": 800,
        "draw": array_insertion,
    },
    {
        "id": "linked-unlink",
        "title": "Unlinking a known list node",
        "category": "Linear structures",
        "description": "Bypass the middle node in both directions, then clear its links.",
        "lesson": "data-structures/linked-lists/doubly.md",
        "keyIdea": LINKED_IDEA,
        "height": 904,
        "draw": linked_unlink,
    },
    {
        "id": "stack-lifo",
        "title": "Stack: last in, first out",
        "category": "Linear structures",
        "description": "Push and pop share the top; the newest value leaves first.",
        "lesson": "data-structures/stacks/implementation.md",
        "keyIdea": STACK_IDEA,
        "height": 1110,
        "draw": stack_lifo,
    },
    {
        "id": "queue-wrap",
        "title": "Queue wrap: slots versus order",
        "category": "Linear structures",
        "description": "The head moves around the buffer while FIFO order stays intact.",
        "lesson": "data-structures/queues/implementation.md",
        "keyIdea": QUEUE_IDEA,
        "height": 1025,
        "draw": queue_wrap,
    },
    {
        "id": "hash-chaining",
        "title": "Hash collisions: keep both keys",
        "category": "Linear structures",
        "description": "Equal hash codes select one bucket; key equality still decides a match.",
        "lesson": "data-structures/hash-tables/collision-resolution.md",
        "keyIdea": HASH_IDEA,
        "height": 980,
        "draw": hash_chaining,
    },
    {
        "id": "lru-recency",
        "title": "LRU: a hit changes the order",
        "category": "Linear structures",
        "description": "Promote a cache hit before deciding which entry to evict next.",
        "lesson": "data-structures/hash-tables/overview.md",
        "keyIdea": LRU_IDEA,
        "height": 930,
        "draw": lru_recency,
    },
]
