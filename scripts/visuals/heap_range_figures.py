"""Heap, trie, range-query and union-find figures; fixtures in examples-heap_range.properties.

Internal arrays and parent pointers of the library classes are private, so each figure shows the
conceptual structure that follows the class's documented algorithm (sift rules, union by size
with path compression, the lazy tag rule), computed here from the fixture. The Java test
VisualExamplesHeapRangeTest recomputes the same structure with an independent model and checks
the observable results against the library.
"""

from drawing import ints

CATEGORY = "Trees and ranges"

# ---------------------------------------------------------------------------------------------
# heap-sift-paths
# ---------------------------------------------------------------------------------------------

SIFT_IDEA = "Each repair follows one path of at most log2 n levels."
HEAP_R = 20
HEAP_X = {0: 260, 1: 160, 2: 360, 3: 110, 4: 210, 5: 310, 6: 410}
HEAP_ROW_X, HEAP_CELL, HEAP_GAP, HEAP_CELL_H = 96, 50, 6, 34


def _heap_point(slot, top, gap):
    return HEAP_X[slot], top + gap * ((slot + 1).bit_length() - 1)


def _heap_tree(c, values, top, gap, states, path):
    """A complete binary tree in array order; edges between consecutive path slots are blue."""
    for slot in range(1, len(values)):
        parent = (slot - 1) // 2
        color = "blue" if slot in path and parent in path else "line"
        c.edge(_heap_point(parent, top, gap), _heap_point(slot, top, gap), radius=HEAP_R,
               directed=False, color=color)
    for slot, value in enumerate(values):
        x, y = _heap_point(slot, top, gap)
        c.node(x, y, value, radius=HEAP_R, state=states.get(slot))


def _heap_row(c, y, caption, values, states, width=7):
    c.text(28, y + HEAP_CELL_H / 2 + 6, caption, size=18, color="muted", weight="700")
    cells = list(values) + [""] * (width - len(values))
    marks = [states.get(slot) for slot in range(len(values))] + ["muted"] * (width - len(values))
    c.cell_row(HEAP_ROW_X, y, cells, cell=HEAP_CELL, gap=HEAP_GAP, height=HEAP_CELL_H, states=marks)


def heap_sift_paths(c, fixtures):
    """add sifts the new value up one path; remove sifts the displaced last value down one."""
    base = ints(fixtures, "heap_range.sift.base")
    value = int(fixtures["heap_range.sift.insert"])
    grown = ints(fixtures, "heap_range.sift.afterInsert")
    up_path = ints(fixtures, "heap_range.sift.insertPath")
    removed = int(fixtures["heap_range.sift.removed"])
    shrunk = ints(fixtures, "heap_range.sift.afterRemove")
    down_path = ints(fixtures, "heap_range.sift.removePath")
    gap = 40

    # 1. Add: the new value starts in the first free slot and climbs while its parent is larger.
    y = c.step(156, 1, f"add({value}): sift up from slot {up_path[0]} to slot {up_path[-1]}")
    c.chip(28, y + 4, f"{value} added", color="blue")
    top = y + 20
    # The value that sat on the path before the climb has moved one level down.
    _heap_tree(c, grown, top, gap, {up_path[-1]: "focus", up_path[0]: "alt"}, set(up_path))
    row = top + 2 * gap + HEAP_R + 12
    _heap_row(c, row, "before", base, {up_path[-1]: "alt"})
    _heap_row(c, row + HEAP_CELL_H + 6, "after", grown, {up_path[-1]: "focus", up_path[0]: "alt"})

    # 2. Remove: the last value replaces the root and descends through the smaller child.
    y = c.step(row + 2 * HEAP_CELL_H + 44, 2,
               "remove(): sift down " + " to ".join(f"slot {slot}" for slot in (down_path[0], down_path[-1])))
    c.chip(28, y + 4, f"{removed} removed", color="red")
    top = y + 20
    pulled = {slot: "alt" for slot in down_path[:-1]}
    final = {down_path[-1]: "focus"}
    _heap_tree(c, shrunk, top, gap, {**pulled, **final}, set(down_path))
    row = top + 2 * gap + HEAP_R + 12
    before_states = {0: "bad", len(grown) - 1: "focus"}
    before_states.update({slot: "alt" for slot in down_path[1:]})
    _heap_row(c, row, "before", grown, before_states)
    _heap_row(c, row + HEAP_CELL_H + 6, "after", shrunk, {**pulled, **final})

    y = c.legend(row + 2 * HEAP_CELL_H + 44,
                 [("focus", "sifted value"), ("alt", "shifted one level"), ("bad", "removed")])
    c.note(y + 14, SIFT_IDEA)


# ---------------------------------------------------------------------------------------------
# bounded-heap-topk
# ---------------------------------------------------------------------------------------------

TOPK_IDEA = "The root is the largest value kept; an arrival must beat it to get in."


def _topk_fates(stream, actions):
    """Fate of each arrival: dropped on arrival, evicted later, or in the final answer."""
    evicted = {}
    kept = []  # indices currently kept
    for index, action in enumerate(actions):
        if action == "fill":
            kept.append(index)
        elif action.startswith("replace:"):
            gone = int(action.split(":")[1])
            victim = max((i for i in kept if stream[i] == gone), key=lambda i: i)
            kept.remove(victim)
            evicted[victim] = True
            kept.append(index)
    fates = []
    for index, action in enumerate(actions):
        if action == "drop":
            fates.append("bad")
        elif index in evicted:
            fates.append("alt")
        else:
            fates.append("good")
    return fates


def bounded_heap_topk(c, fixtures):
    """Selection.smallestK keeps at most k values; an arrival must beat the largest kept."""
    k = int(fixtures["heap_range.topk.k"])
    stream = ints(fixtures, "heap_range.topk.stream")
    actions = fixtures["heap_range.topk.actions"].split(",")
    kept_after = [[int(v) for v in part.split(",")] for part in fixtures["heap_range.topk.keptAfter"].split("|")]
    result = ints(fixtures, "heap_range.topk.result")
    fates = _topk_fates(stream, actions)

    # 1. The stream, coloured by what finally happens to each arrival.
    y = c.step(156, 1, f"Stream of {len(stream)} values, k = {k}")
    row_x = (520 - len(stream) * 56 + 6) // 2
    y = c.cell_row(row_x, y + 2, stream, cell=50, gap=6, height=44, states=fates)

    # 2. One row per arrival: what the heap does and what it holds afterwards, root first.
    y = c.step(y + 34, 2, "Each arrival meets the root")
    c.text(40, y + 14, "arrives", size=18, color="muted", weight="700")
    c.text(116, y + 14, "heap does", size=18, color="muted", weight="700")
    c.text(330, y + 14, "kept, root first", size=18, color="muted", weight="700")
    row_y = y + 26
    for index, value in enumerate(stream):
        top = row_y + index * 36
        c.cell_row(32, top, [value], cell=48, height=32, states=[fates[index]])
        action = actions[index]
        if action == "fill":
            label, color = "fill (heap not full)", "muted"
        elif action == "drop":
            label, color = f"drop, not below {max(kept_after[index])}", "red"
        else:
            label, color = f"evict {action.split(':')[1]}, keep {value}", "ink"
        c.text(96, top + 23, label, size=18, color=color, weight="700" if action != "fill" else "400")
        ordered = sorted(kept_after[index], reverse=True)
        cells = ordered + [""] * (k - len(ordered))
        marks = ["focus" if (slot == 0 and len(ordered) == k) else None for slot in range(len(ordered))]
        marks += ["muted"] * (k - len(ordered))
        c.cell_row(330, top, cells, cell=44, gap=6, height=32, states=marks)
    y = row_y + len(stream) * 36 + 12
    c.chip(40, y, "smallestK returns " + ", ".join(map(str, result)), color="green")
    y = c.legend(y + 62, [("good", "in the answer"), ("alt", "kept, evicted later"),
                          ("bad", "dropped on arrival"), ("focus", "root, largest kept")])
    c.note(y + 14, TOPK_IDEA)


# ---------------------------------------------------------------------------------------------
# trie-prefix-walk
# ---------------------------------------------------------------------------------------------

TRIE_IDEA = "Walk the prefix once, then visit children in sorted order until the limit."


def _build_trie(words):
    """Nested dicts: children keyed by character, '$' marks a word end."""
    root = {}
    for word in words:
        node = root
        for char in word:
            node = node.setdefault(char, {})
        node["$"] = True
    return root


def trie_prefix_walk(c, fixtures):
    """Walk to the prefix node, then emit marked nodes in sorted order until the limit."""
    words = fixtures["heap_range.trie.words"].split(",")
    prefix = fixtures["heap_range.trie.prefix"]
    limit = int(fixtures["heap_range.trie.limit"])
    result = fixtures["heap_range.trie.result"].split(",")
    everything = fixtures["heap_range.trie.unlimited"].split(",")
    skipped = [word for word in everything if word not in result]
    trie = _build_trie(words)

    # Node layout: one column per leaf in sorted order, a parent above the mean of its children.
    top, gap, radius = 214, 60, 20
    positions = {}
    leaf_x = [60, 135, 210, 285, 360, 435]
    cursor = [0]

    def place(node, path, depth):
        keys = sorted(key for key in node if key != "$")
        if not keys:
            positions[path] = (leaf_x[cursor[0]], top + depth * gap)
            cursor[0] += 1
            return
        for key in keys:
            place(node[key], path + key, depth + 1)
        xs = [positions[path + key][0] for key in keys]
        positions[path] = ((xs[0] + xs[-1]) / 2, top + depth * gap)

    place(trie, "", 0)
    emitted = {word: index + 1 for index, word in enumerate(result)}
    on_path = {prefix[:length] for length in range(len(prefix) + 1)}

    def node_at(path):
        node = trie
        for char in path:
            node = node[char]
        return node

    y = c.step(156, 1, f"wordsWithPrefix(\"{prefix}\", {limit})")
    for path in sorted(positions, key=len):
        if path:
            c.edge(positions[path[:-1]], positions[path], radius=radius, directed=False,
                   color="blue" if path in on_path else "line")
    for path, (x, y_node) in positions.items():
        if path in on_path:
            state = "focus"
        elif path in emitted:
            state = "good"
        elif "$" in node_at(path) and path.startswith(prefix):
            state = "muted"
        elif not path.startswith(prefix):
            state = "muted"
        else:
            state = None
        c.node(x, y_node, path[-1] if path else "root", radius=radius if path else radius + 6, state=state)
    c.text(positions[prefix][0] + 32, positions[prefix][1] - 8, f"prefix {prefix}", size=18,
           color="blue", weight="700")
    # Word labels: emitted words carry their output position.
    for word in everything:
        x, y_node = positions[word]
        if word in emitted:
            label, color, weight = f"{emitted[word]}  {word}", "green", "700"
        else:
            label, color, weight = word, "muted", "400"
        if any(other != word and other.startswith(word) for other in words):
            c.text(x + radius + 8, y_node + 7, label, size=18, color=color, weight=weight)
        else:
            c.text(x, y_node + radius + 24, label, size=18, color=color, weight=weight, anchor="middle")
    for word in words:
        if not word.startswith(prefix):
            x, y_node = positions[word]
            c.text(x + radius + 8, y_node + 7, word, size=18, color="muted")

    bottom = max(y for _, y in positions.values()) + radius + 72
    y = c.step(bottom, 2, f"Stop after {limit} words")
    row_x = 28 + 10
    c.cell_row(row_x, y, result, cell=80, gap=8, height=40, states=["good"] * len(result))
    c.text(row_x, y + 74, "not reached: " + ", ".join(skipped), size=18, color="muted")
    y = c.legend(y + 114, [("focus", "prefix path"), ("good", "emitted"),
                           ("muted", "not visited")])
    c.note(y + 14, TRIE_IDEA)


# ---------------------------------------------------------------------------------------------
# sparse-table-overlap
# ---------------------------------------------------------------------------------------------

SPARSE_IDEA = "Minimum ignores the overlap of two blocks; a sum would count it twice."


def sparse_table_overlap(c, fixtures):
    """rangeMin = min of the block at the left end and the block at the right end."""
    values = ints(fixtures, "heap_range.sparse.values")
    start, end = int(fixtures["heap_range.sparse.from"]), int(fixtures["heap_range.sparse.to"])
    level = int(fixtures["heap_range.sparse.level"])
    row = ints(fixtures, "heap_range.sparse.row")
    first = ints(fixtures, "heap_range.sparse.firstBlock")
    second = ints(fixtures, "heap_range.sparse.secondBlock")
    answer = int(fixtures["heap_range.sparse.answer"])
    true_sum = int(fixtures["heap_range.sparse.trueSum"])
    block_sums = ints(fixtures, "heap_range.sparse.blockSums")
    length = end - start
    block = 1 << level
    cell, gap, x0 = 50, 6, 39

    def left(index):
        return x0 + index * (cell + gap)

    def right(index):
        return left(index) + cell

    # 1. The query range, and the block length that fits inside it.
    y = c.step(156, 1, f"rangeMin({start}, {end}): length {length}, so blocks of {block}")
    states = [("focus" if start <= i < end else "muted") for i in range(len(values))]
    y = c.cell_row(x0, y, values, cell=cell, gap=gap, states=states, labels=list(range(len(values))))

    # 2. One block starts at the left end, one ends at the right end; they overlap.
    y = c.step(y + 30, 2, "One block from each end covers it")
    overlap = set(range(first[0], first[1])) & set(range(second[0], second[1]))
    states = []
    for index in range(len(values)):
        in_first = first[0] <= index < first[1]
        in_second = second[0] <= index < second[1]
        states.append("alt" if in_first and in_second else "good" if in_first
                      else "focus" if in_second else "muted")
    row_y = y + 54
    c.brace(left(first[0]), right(first[1] - 1), row_y - 14,
            f"[{first[0]}, {first[1]}) min {row[first[0]]}", color="green", above=True)
    c.cell_row(x0, row_y, values, cell=cell, gap=gap, states=states)
    c.brace(left(second[0]), right(second[1] - 1), row_y + 48 + 14,
            f"[{second[0]}, {second[1]}) min {row[second[0]]}", color="blue")

    # 3. Combine: minimum is idempotent, sum is not.
    y = c.step(row_y + 48 + 76, 3, "Combine the two answers")
    c.chip(40, y, f"min: min({row[first[0]]}, {row[second[0]]}) = {answer}, correct", color="green")
    c.chip(40, y + 42, f"sum: {block_sums[0]} + {block_sums[1]} = {sum(block_sums)}, true sum {true_sum}",
           color="red")
    overlapped = " + ".join(str(values[i]) for i in sorted(overlap))
    c.text(40, y + 100, f"{overlapped} was counted twice.", size=18, color="muted")
    y = c.legend(y + 140, [("good", "first block only"), ("focus", "second block only"),
                           ("alt", "in both blocks"), ("muted", "outside the range")])
    c.note(y + 14, SPARSE_IDEA)


# ---------------------------------------------------------------------------------------------
# lazy-propagation
# ---------------------------------------------------------------------------------------------

LAZY_IDEA = "A range add stops at a covering node and leaves a pending value."
LAZY_R = 24
LAZY_X = {0: 220, 1: 120, 2: 320, 3: 70, 4: 170, 5: 270, 6: 370}


def _lazy_point(slot, top, gap):
    return LAZY_X[slot], top + gap * ((slot + 1).bit_length() - 1)


def lazy_propagation(c, fixtures):
    """rangeAdd(2, 4, +5) tags node [2,4); rangeSum(3, 4) reads leaf [3,4) through the tag."""
    values = ints(fixtures, "heap_range.lazy.values")
    add_from, add_to = int(fixtures["heap_range.lazy.addFrom"]), int(fixtures["heap_range.lazy.addTo"])
    delta = int(fixtures["heap_range.lazy.delta"])
    after = ints(fixtures, "heap_range.lazy.sumsAfter")
    true_values = ints(fixtures, "heap_range.lazy.trueValues")
    query_from, query_to = int(fixtures["heap_range.lazy.queryFrom"]), int(fixtures["heap_range.lazy.queryTo"])
    stored = int(fixtures["heap_range.lazy.queryStored"])
    answer = int(fixtures["heap_range.lazy.queryAnswer"])
    gap = 62
    intervals = {0: "[0,4)", 1: "[0,2)", 2: "[2,4)"}
    sums = {0: after[0], 1: after[1], 2: after[2]}
    # Stored leaf values: the leaves below the tagged node were not touched.
    for slot in range(3, 7):
        sums[slot] = values[slot - 3]
    tagged = 2

    def draw_tree(top, states, path):
        for slot in range(1, 7):
            parent = (slot - 1) // 2
            color = "blue" if slot in path and parent in path else "line"
            c.edge(_lazy_point(parent, top, gap), _lazy_point(slot, top, gap), radius=LAZY_R,
                   directed=False, color=color)
        for slot in range(7):
            x, y = _lazy_point(slot, top, gap)
            c.node(x, y, sums[slot], radius=LAZY_R, state=states.get(slot))
            if slot in intervals:
                label_x = x + LAZY_R + 8 if slot == 0 else x - LAZY_R - 8
                c.text(label_x, y + 6, intervals[slot], size=18, color="muted",
                       anchor="start" if slot == 0 else "end")

    # 1. The update stops at the node whose interval lies inside the range.
    y = c.step(156, 1, f"rangeAdd({add_from}, {add_to}, +{delta}) stops at [{add_from},{add_to})")
    top = y + 36
    _, tag_y = _lazy_point(tagged, top, gap)
    draw_tree(top, {tagged: "warn", 5: "muted", 6: "muted"}, set())
    c.chip(LAZY_X[tagged] + LAZY_R + 8, tag_y - 15, f"pending +{delta}", color="gold")
    bottom = top + 2 * gap + LAZY_R
    c.text(28, bottom + 30,
           f"Leaves below still say {values[2]} and {values[3]}; true values are "
           f"{true_values[2]} and {true_values[3]}.", size=18, color="muted")

    # 2. A query carries the pending value down instead of changing the tree.
    y = c.step(bottom + 76, 2, f"rangeSum({query_from}, {query_to}) carries +{delta} down")
    top = y + 36
    draw_tree(top, {0: "focus", tagged: "focus", 6: "focus"}, {0, tagged, 6})
    bottom = top + 2 * gap + LAZY_R
    c.text(28, bottom + 30, f"{stored} stored + {delta} pending x {query_to - query_from} element = {answer}",
           size=18, color="blue", weight="700")
    y = c.legend(bottom + 70, [("warn", "tagged node"), ("muted", "stale child"),
                               ("focus", "queried")])
    c.note(y + 14, LAZY_IDEA)


# ---------------------------------------------------------------------------------------------
# union-find-compression
# ---------------------------------------------------------------------------------------------

UF_IDEA = "Union by size keeps trees shallow; find re-points its path at the root."


def _children(parents):
    kids = {}
    for node, parent in enumerate(parents):
        if node != parent:
            kids.setdefault(parent, []).append(node)
    return kids


def _forest_layout(parents, x_min, x_max, top, gap):
    """Leaves get equal columns left to right; a parent sits above the mean of its children."""
    kids = _children(parents)
    root = parents.index(next(node for node, parent in enumerate(parents) if node == parent))
    order = []

    def leaves(node):
        if node not in kids:
            order.append(node)
        for child in kids.get(node, []):
            leaves(child)

    leaves(root)
    step = (x_max - x_min) / (len(order) - 1)
    positions = {}

    def place(node, depth):
        for child in kids.get(node, []):
            place(child, depth + 1)
        if node in kids:
            xs = [positions[child][0] for child in kids[node]]
            positions[node] = ((xs[0] + xs[-1]) / 2, top + depth * gap)
        else:
            positions[node] = (x_min + order.index(node) * step, top + depth * gap)

    place(root, 0)
    return positions


def union_find_compression(c, fixtures):
    """union by size attaches the smaller root; find(7) flattens its path."""
    first, second = int(fixtures["heap_range.uf.mergeFirst"]), int(fixtures["heap_range.uf.mergeSecond"])
    sizes = ints(fixtures, "heap_range.uf.rootSizes")
    element = int(fixtures["heap_range.uf.findElement"])
    path = ints(fixtures, "heap_range.uf.pathBefore")
    before = ints(fixtures, "heap_range.uf.parentsBefore")
    after = ints(fixtures, "heap_range.uf.parentsAfter")
    total = int(fixtures["heap_range.uf.componentSize"])
    small_root = path[-2]
    big_root = path[-1]

    # 1. Union by size: the smaller tree's root points at the larger tree's root.
    y = c.step(156, 1, f"union({first}, {second}): smaller root goes under larger")
    left_x, right_x, row_y = 110, 290, y + 42
    c.node(left_x, row_y, big_root, radius=24, state="focus")
    c.node(right_x, row_y, small_root, radius=24, state="alt")
    c.edge((right_x, row_y), (left_x, row_y), radius=24, color="blue", label="points to")
    c.text(left_x, row_y + 52, f"size {sizes[0]}", size=18, color="blue", weight="700", anchor="middle")
    c.text(right_x, row_y + 52, f"size {sizes[1]}", size=18, color="violet", weight="700", anchor="middle")
    c.text(350, row_y + 7, f"one tree, size {total}", size=18, color="green", weight="700")
    c.text(28, row_y + 96, f"{sizes[1]} < {sizes[0]}: root {small_root} gets parent {big_root}.", size=18,
           color="muted")

    # 2 and 3. The same forest before and after find(7), side by side.
    heading = row_y + 154
    top, gap = heading + 66, 46

    def side_step(x, number, text):
        c.parts.append(f'<circle cx="{x + 14}" cy="{heading - 7}" r="14" fill="{c.color("blue")}" '
                       f'stroke="{c.color("blue")}" stroke-width="2"/>')
        c.shape_bottoms.append((heading + 7, f"step badge {number}"))
        c.text(x + 14, heading - 1, number, size=18, color="paper", anchor="middle", weight="700")
        c.text(x + 36, heading, text, weight="700")

    side_step(28, 2, f"Before find({element})")
    side_step(274, 3, f"After find({element})")
    on_path = set(path)
    flattened = {node for node in path[:-1] if after[node] != before[node]}
    for parents, x_min, x_max, state_of in [
            (before, 52, 236, lambda node: "focus" if node in on_path else None),
            (after, 288, 480, lambda node: "good" if node in flattened else
             ("focus" if node == path[-1] else None))]:
        positions = _forest_layout(parents, x_min, x_max, top, gap)
        for node, parent in enumerate(parents):
            if node != parent:
                color = "blue" if (node in on_path and parent in on_path) else "line"
                c.edge(positions[node], positions[parent], radius=14,
                       color=color, directed=False)
        for node, (x, y_node) in positions.items():
            c.node(x, y_node, node, radius=14, state=state_of(node))
    bottom = top + 3 * gap + 14
    c.text(28, bottom + 32, " → ".join(map(str, path)), size=18, color="blue", weight="700")
    c.text(274, bottom + 32, ", ".join(f"{node} → {after[node]}" for node in sorted(flattened)),
           size=18, color="green", weight="700")
    y = c.legend(bottom + 76, [("focus", "on the find path / root"), ("good", "re-pointed at the root")])
    c.note(y + 14, UF_IDEA)


FIGURES = [
    {
        "id": "heap-sift-paths",
        "title": "Heap sift paths",
        "category": CATEGORY,
        "description": ("In the min heap 1, 3, 8, 4, 5, 9, add(7) appends at slot 6 and swaps up one level "
                        "to slot 2, giving 1, 3, 7, 4, 5, 9, 8. remove() then takes out 1, moves 8 to the "
                        "root and sifts it down two levels to slot 3, giving 3, 4, 7, 8, 5, 9."),
        "lesson": "data-structures/heaps/operations.md",
        "keyIdea": SIFT_IDEA,
        "height": 880,
        "draw": heap_sift_paths,
    },
    {
        "id": "bounded-heap-topk",
        "title": "Top k with a bounded heap",
        "category": CATEGORY,
        "description": ("Selection.smallestK over the stream 8, 3, 9, 5, 2, 5, 1 with k = 3 fills the heap with "
                        "8, 3, 9, then evicts 9, evicts 8, drops the second 5 and evicts 5 for 1, ending with "
                        "1, 2, 3."),
        "lesson": "data-structures/queues/priority-queues.md",
        "keyIdea": TOPK_IDEA,
        "height": 820,
        "draw": bounded_heap_topk,
    },
    {
        "id": "trie-prefix-walk",
        "title": "Trie prefix walk",
        "category": CATEGORY,
        "description": ("For the words car, card, care, cart, cat and dog, wordsWithPrefix(\"ca\", 3) walks the "
                        "c and a nodes, then emits car, card and care in sorted order and stops before cart "
                        "and cat; the dog branch is never visited."),
        "lesson": "data-structures/tries/implementation.md",
        "keyIdea": TRIE_IDEA,
        "height": 864,
        "draw": trie_prefix_walk,
    },
    {
        "id": "sparse-table-overlap",
        "title": "Sparse table overlap",
        "category": CATEGORY,
        "description": ("For the values 5, 2, 6, 4, 7, 3, 8, 1, rangeMin(1, 7) takes the minimum of the blocks "
                        "[1, 5) and [3, 7), which overlap on indices 3 and 4, and gets 2. A sum of the same "
                        "blocks would give 41 instead of 30."),
        "lesson": "data-structures/range-queries/overview.md",
        "keyIdea": SPARSE_IDEA,
        "height": 840,
        "draw": sparse_table_overlap,
    },
    {
        "id": "lazy-propagation",
        "title": "Lazy range add",
        "category": CATEGORY,
        "description": ("In a LazySegmentTree over 1, 2, 3, 4, rangeAdd(2, 4, 5) stops at the node covering "
                        "[2, 4), raising its sum to 17 and leaving a pending +5 for its leaves. "
                        "rangeSum(3, 4) then reads the leaf's stored 4, adds the pending 5 and returns 9."),
        "lesson": "data-structures/range-queries/overview.md",
        "keyIdea": LAZY_IDEA,
        "height": 870,
        "draw": lazy_propagation,
    },
    {
        "id": "union-find-compression",
        "title": "Path compression",
        "category": CATEGORY,
        "description": ("In a UnionFind over 9 indices, union(0, 5) hangs the size-4 root 4 under the size-5 "
                        "root 0. Before find(7) the path is 7, 6, 4, 0; afterwards 7 and 6 point directly "
                        "at 0."),
        "lesson": "data-structures/union-find/overview.md",
        "keyIdea": UF_IDEA,
        "height": 860,
        "draw": union_find_compression,
    },
]
