"""Fixture-derived tree and range-query diagrams on a narrow, readable canvas."""

import math

from drawing import ints


IDEAS = {
    "avl-rotation": "Rotate links, preserve sorted order, and restore the height balance.",
    "heap-anatomy": "A heap puts the minimum at its root; the array is not globally sorted.",
    "trie-pruning": "Deleting a word clears its end marker. Keep nodes still needed by longer words.",
    "union-find": "Connectivity is a partition: five indices form two disjoint components.",
    "fenwick-blocks": "A prefix query combines disjoint blocks. The low bit gives each block's length.",
    "segment-minimum": "Each node covers an adjacent interval. A point set repairs one path to the root.",
}


def _edge(c, start, end, color="line", radius=27):
    """Keep arrowheads and shafts outside both node circles."""
    dx, dy = end[0] - start[0], end[1] - start[1]
    length = math.hypot(dx, dy)
    ux, uy = dx / length, dy / length
    c.arrow(start[0] + ux * (radius + 4), start[1] + uy * (radius + 4),
            end[0] - ux * (radius + 8), end[1] - uy * (radius + 8), color=color)


def _heading(c, y, text):
    c.text(28, y, text, weight="700")


def draw_avl(c, fixtures):
    before = ints(fixtures, "avl.input")
    ordered = ints(fixtures, "avl.inorder")
    height = ints(fixtures, "avl.height")[0]
    _heading(c, 155, "1  Insert " + ", ".join(map(str, before)))
    c.rect(28, 185, 464, 310, fill="panel")
    positions = [(335, 235), (245, 335), (155, 435)]
    _edge(c, positions[0], positions[1], "blue")
    _edge(c, positions[1], positions[2], "blue")
    for position, value in zip(positions, before):
        c.node(*position, value, active=True)
    c.text(55, 230, "Left-heavy", color="blue", weight="700")
    c.text(55, 270, f"height = {len(before) - 1}")

    _heading(c, 535, f"2  Rotate right at {before[0]}")
    c.rect(28, 555, 464, 250, fill="panel")
    root, left, right = (260, 605), (150, 715), (370, 715)
    _edge(c, root, left, "blue")
    _edge(c, root, right, "blue")
    c.node(*root, ordered[1], active=True)
    c.node(*left, ordered[0])
    c.node(*right, ordered[2])
    c.text(50, 775, "In-order: " + ", ".join(map(str, ordered)))
    c.text(460, 775, f"h = {height}", anchor="end", color="blue")
    c.note(835, IDEAS["avl-rotation"])


def _heapify(values):
    """Match the maintained bottom-up, natural-order heap construction."""
    result = list(values)
    for start in range(len(result) // 2 - 1, -1, -1):
        parent = start
        while parent < len(result) // 2:
            child = parent * 2 + 1
            if child + 1 < len(result) and result[child + 1] < result[child]:
                child += 1
            if result[parent] <= result[child]:
                break
            result[parent], result[child] = result[child], result[parent]
            parent = child
    return result


def draw_heap(c, fixtures):
    values = _heapify(ints(fixtures, "heap.input"))
    _heading(c, 155, "1  Heap array: zero-based slots")
    c.text(50, 190, "Heapify " + ", ".join(map(str, ints(fixtures, "heap.input"))))
    c.cells(55, 215, values, cell=58, gap=12, active=(0,), labels=list(range(len(values))))

    _heading(c, 335, "2  The same values form a complete tree")
    positions = [(260, 390), (140, 490), (380, 490), (80, 590), (200, 590), (330, 590)]
    for child in range(1, len(values)):
        _edge(c, positions[(child - 1) // 2], positions[child])
    for slot, (position, value) in enumerate(zip(positions, values)):
        c.node(*position, value, active=slot == 0)
        if slot == 0:
            c.text(position[0] + 42, position[1] + 7, "slot 0", size=18, color="muted")
        else:
            # Internal labels sit above the node, clear of its two outgoing edges.
            label_y = position[1] - 45 if slot < 3 else position[1] + 48
            c.text(position[0], label_y, f"slot {slot}", size=18,
                   anchor="middle", color="muted")
    c.text(40, 695, "Slot i has children 2i+1 and 2i+2.")
    c.paragraph(40, 740, "Removal order: " + ", ".join(map(str, ints(fixtures, "heap.sorted"))),
                width=39)
    c.note(815, IDEAS["heap-anatomy"])


def _trie_panel(c, top, removed, words):
    root, first, shared = (260, top + 45), (260, top + 130), (260, top + 215)
    branch, sibling, continuation = (180, top + 300), (340, top + 300), (180, top + 385)
    for parent, child in [(root, first), (first, shared), (shared, branch),
                          (shared, sibling), (branch, continuation)]:
        _edge(c, parent, child)
    # All fixture words share c-a; cart extends car and cat ends on a sibling branch.
    c.node(*root, "root")
    c.node(*first, words[0][0])
    c.node(*shared, words[0][1])
    c.node(*branch, words[0][2], active=not removed)
    c.node(*sibling, words[2][2], active=True)
    c.node(*continuation, words[1][3], active=True)
    c.text(105 if not removed else 75, branch[1] + 7, words[0] if not removed else "prefix",
           color="blue" if not removed else "muted")
    c.text(385, sibling[1] + 7, words[2], color="blue")
    c.text(225, continuation[1] + 7, words[1], color="blue")


def draw_trie(c, fixtures):
    words = fixtures["trie.words"].split(",")
    removed = fixtures["trie.remove"]
    remaining = fixtures["trie.after"].split(",")
    _heading(c, 155, "1  Blue nodes mark stored words")
    c.rect(28, 180, 464, 425, fill="panel")
    _trie_panel(c, 180, False, words)
    _heading(c, 660, f"2  Remove {removed}; keep " + " and ".join(remaining))
    c.rect(28, 690, 464, 425, fill="panel")
    _trie_panel(c, 690, True, words)
    c.note(1145, IDEAS["trie-pruning"])


def draw_union(c, fixtures):
    size = ints(fixtures, "union.size")[0]
    pairs = [tuple(map(int, pair.split(":"))) for pair in fixtures["union.pairs"].split(",")]
    parents, sizes = list(range(size)), [1] * size

    def find(index):
        while parents[index] != index:
            index = parents[index]
        return index

    for first, second in pairs:
        first, second = find(first), find(second)
        if first != second:
            if sizes[first] < sizes[second]:
                first, second = second, first
            parents[second] = first
            sizes[first] += sizes[second]
    _heading(c, 155, "1  Union " + ", ".join(f"({a},{b})" for a, b in pairs))
    c.rect(28, 185, 464, 300, fill="panel")
    positions = [(150, 270), (85, 385), (225, 385), (380, 270), (380, 385)]
    for index, parent in enumerate(parents):
        if index != parent:
            _edge(c, positions[index], positions[parent], "blue")
    for index, position in enumerate(positions):
        c.node(*position, index, active=parents[index] == index)
        if parents[index] == index:
            c.text(position[0], 222, f"size {sizes[index]}", anchor="middle", color="blue")
    c.text(50, 465, "components = " + fixtures["union.components"])

    element = pairs[1][1]
    representative = find(element)
    _heading(c, 535, f"2  find({element}) follows its parent")
    _edge(c, (130, 620), (350, 620), "blue")
    c.node(130, 620, element)
    c.node(350, 620, representative, active=True)
    c.text(130, 680, "index", anchor="middle", color="muted")
    c.text(350, 680, "root", anchor="middle", color="muted")
    c.paragraph(45, 745, "Union by size attaches the smaller tree. Find compresses paths to the root.",
                width=40)
    c.note(855, IDEAS["union-find"])


def draw_fenwick(c, fixtures):
    values = ints(fixtures, "fenwick.values")
    end = ints(fixtures, "fenwick.prefixEnd")[0]
    visited = []
    cursor = end
    while cursor:
        visited.append(cursor)
        cursor -= cursor & -cursor
    _heading(c, 155, "1  Values use zero-based positions")
    c.cells(116, 185, values, cell=60, gap=16, labels=list(range(len(values))))

    _heading(c, 310, "2  Internal slots store low-bit blocks")
    for position in range(len(values)):
        c.text(190 + 64 * position + 32, 343, position, size=18, anchor="middle", color="muted")
    sums = {}
    for slot in range(1, len(values) + 1):
        length = slot & -slot
        start = slot - length
        total = sum(values[start:slot])
        sums[slot] = total
        top = 365 + (slot - 1) * 87
        active = slot in visited
        c.text(45, top + 31, f"t[{slot}] = {total}", color="blue" if active else "ink")
        c.text(45, top + 65, f"length {length}", size=18, color="muted")
        c.rect(190 + 64 * start, top, 64 * length, 48,
               fill="soft" if active else "paper", stroke="blue" if active else "line")
        c.text(190 + 64 * (start + length / 2), top + 31, f"[{start},{slot})", anchor="middle")

    _heading(c, 755, f"3  prefixSum({end}): " + " → ".join(map(str, visited + [0])))
    formula = " + ".join(f"t[{slot}]" for slot in visited)
    totals = " + ".join(str(sums[slot]) for slot in visited)
    c.text(45, 800, f"{formula} = {totals} = {fixtures['fenwick.prefix']}")
    c.paragraph(45, 850, f"Subtract the lowest set bit to move left. These blocks cover [0,{end}) exactly once.",
                width=39)
    c.note(960, IDEAS["fenwick-blocks"])


def _minimum_panel(c, top, values, active):
    root, left, right = (260, top + 70), (140, top + 180), (380, top + 180)
    leaves = [(80, top + 290), (200, top + 290), (320, top + 290), (440, top + 290)]
    for parent, child in [(root, left), (root, right), (left, leaves[0]),
                          (left, leaves[1]), (right, leaves[2]), (right, leaves[3])]:
        _edge(c, parent, child)
    for name, position, value, interval in [
            ("root", root, min(values), "[0,4)"),
            ("left", left, min(values[:2]), "[0,2)"),
            ("right", right, min(values[2:]), "[2,4)")]:
        c.node(*position, value, active=name in active)
        c.text(position[0], position[1] - 44, interval, size=18, anchor="middle", color="muted")
    for index, (position, value) in enumerate(zip(leaves, values)):
        c.node(*position, value, active=index in active)
        c.text(position[0], position[1] + 48, f"i={index}", size=18, anchor="middle", color="muted")


def draw_segment(c, fixtures):
    values = ints(fixtures, "segment.values")
    start, end = ints(fixtures, "segment.range")
    index = ints(fixtures, "segment.index")[0]
    replacement = ints(fixtures, "segment.value")[0]
    _heading(c, 155, f"1  Query [{start},{end}): choose covered blocks")
    c.rect(28, 175, 464, 350, fill="panel")
    _minimum_panel(c, 175, values, {index, "right"})
    c.text(45, 570, "Blue: chosen blocks → minimum " + fixtures["segment.before"])

    _heading(c, 635, f"2  set({index},{replacement}): repair one root path")
    c.rect(28, 660, 464, 350, fill="panel")
    changed = list(values)
    changed[index] = replacement
    _minimum_panel(c, 660, changed, {index, "left", "root"})
    c.text(45, 1055, "Blue: changed ancestors and leaf")
    c.text(45, 1100, f"min [{start},{end}) = min({changed[index]},{min(changed[2:])}) = "
           + fixtures["segment.after"])
    c.text(45, 1140, "Pad unused leaves with MAX_VALUE.", color="muted")
    c.note(1185, IDEAS["segment-minimum"])


FIGURES = [
    {"id": "avl-rotation", "title": "AVL rotation", "category": "Trees and ranges",
     "description": "Inserting 30, 20 and 10 produces a left-heavy chain. A right rotation makes 20 the root with children 10 and 30, preserving in-order traversal and restoring height one.",
     "lesson": "data-structures/trees/avl-trees.md", "keyIdea": IDEAS["avl-rotation"],
     "height": 1000, "draw": draw_avl},
    {"id": "heap-anatomy", "title": "Heap: array and tree", "category": "Trees and ranges",
     "description": "Bottom-up heapify turns 9,4,7,1,3,2 into the array 1,3,2,4,9,7. The matching complete binary tree labels every zero-based array slot and keeps every parent no greater than its children.",
     "lesson": "data-structures/heaps/overview.md", "keyIdea": IDEAS["heap-anatomy"],
     "height": 980, "draw": draw_heap},
    {"id": "trie-pruning", "title": "Trie: preserve shared prefixes", "category": "Trees and ranges",
     "description": "The words car, cart and cat share a c-a prefix. Deleting car clears the r node's word marker but preserves its t child for cart and the sibling t branch for cat.",
     "lesson": "data-structures/tries/implementation.md", "keyIdea": IDEAS["trie-pruning"],
     "height": 1310, "draw": draw_trie},
    {"id": "union-find", "title": "Union-find components", "category": "Trees and ranges",
     "description": "Union operations on five indices produce components {0,1,2} and {3,4}. Parent arrows point toward roots 0 and 3, whose component sizes are three and two. Finding index 2 follows its link to root 0.",
     "lesson": "data-structures/trees/overview.md", "keyIdea": IDEAS["union-find"],
     "height": 1020, "draw": draw_union},
    {"id": "fenwick-blocks", "title": "Fenwick prefix blocks", "category": "Trees and ranges",
     "description": "For values 2,-1,4,3, one-based Fenwick slots cover [0,1), [0,2), [2,3) and [0,4). The prefix ending at zero-based boundary 3 combines slots 3 and 2: 4+1=5.",
     "lesson": "docs/solutions.md", "keyIdea": IDEAS["fenwick-blocks"],
     "height": 1125, "draw": draw_fenwick},
    {"id": "segment-minimum", "title": "Segment minimum", "category": "Trees and ranges",
     "description": "An interval tree over 8,3,7,5 answers the minimum on [1,4) by combining leaf [1,2) with node [2,4). Replacing index 1 with 9 repairs its ancestors and changes the query answer from 3 to 5. Leaves use power-of-two padding with MAX_VALUE when needed.",
     "lesson": "data-structures/trees/overview.md", "keyIdea": IDEAS["segment-minimum"],
     "height": 1350, "draw": draw_segment},
]
