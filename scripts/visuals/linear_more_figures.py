"""Linear-structure figures added after the original set; fixtures in examples-linear_more.properties.

``dynamic_array_growth`` is the reference example for new figures: numbered steps read top
to bottom, every value comes from the fixture file, every state is named in a label or the
legend, and the figure ends with the KEY IDEA note.
"""

from drawing import ints

GROWTH_IDEA = ("Doubling makes copies rare: n appends copy fewer than 2n values in total, "
               "so one append costs O(1) amortized.")

# One row geometry for every array in the figure, so the old and new arrays line up.
ROW_X, CELL, GAP = 39, 50, 6


def dynamic_array_growth(c, fixtures):
    """Append to a full DynamicArray: allocate double, copy the live prefix, then write."""
    capacity = int(fixtures["linear_more.growth.capacity"])
    before = ints(fixtures, "linear_more.growth.before")
    value = int(fixtures["linear_more.growth.value"])
    grown = int(fixtures["linear_more.growth.grownCapacity"])
    after = ints(fixtures, "linear_more.growth.after")
    copies = int(fixtures["linear_more.growth.copies"])
    next_append = int(fixtures["linear_more.growth.nextGrowthAppend"])
    next_copies = int(fixtures["linear_more.growth.nextCopies"])
    total = int(fixtures["linear_more.growth.totalCopies"])

    def x(index):
        return c.cell_center(ROW_X, index, CELL, GAP)

    def edge(index, side):
        return ROW_X + index * (CELL + GAP) + (0 if side == "left" else CELL)

    # 1. The old array is full, so the new value has nowhere to go.
    y = c.step(156, 1, f"add({value}) finds no free slot")
    c.cell_row(ROW_X, y, before, cell=CELL, gap=GAP)
    waiting = len(before) + 1
    c.cell_row(edge(waiting, "left"), y, [value], cell=CELL, gap=GAP, states=["warn"])
    c.text(x(waiting), y + 74, "waiting", anchor="middle", size=18, color="gold", weight="700")
    c.brace(edge(0, "left"), edge(len(before) - 1, "right"), y + 60,
            f"size {len(before)} = capacity {capacity}")

    # 2. A new array of double the capacity receives copies, then the new value.
    y = c.step(312, 2, f"Allocate {grown} slots, copy {copies}, write {value}")
    c.brace(edge(0, "left"), edge(grown - 1, "right"), y + 40, f"new capacity {grown}",
            color="blue", above=True)
    row_y = y + 52
    states = ["good"] * len(before) + ["focus"] + ["muted"] * (grown - len(after))
    c.cell_row(ROW_X, row_y, after + [""] * (grown - len(after)), cell=CELL, gap=GAP, states=states)
    c.brace(edge(0, "left"), edge(len(before) - 1, "right"), row_y + 60, f"{copies} copies",
            color="green")
    c.pointer(x(len(after)), row_y + 48, f"size = {len(after)}")

    # 3. The running count: copies happen only when the array is full.
    y = c.step(540, 3, "Running copy count")
    left = 66
    left += c.chip(left, y, f"{len(after)} appends", color="blue") + 10
    c.chip(left, y, f"{copies} copies so far", color="green")
    c.text(66, y + 60, f"Append {next_append} copies {next_copies} more: {total} copies in {next_append} appends.",
           size=18, color="muted")

    y = c.legend(y + 106, [("good", "copied"), ("focus", "new"), ("muted", "free slot"),
                           ("warn", "waiting")])
    c.note(y + 14, GROWTH_IDEA)


# ---------------------------------------------------------------------------------------------
# singly-insert-after

INSERT_IDEA = "Link the new node onward first, then redirect the predecessor."

NODE_R = 24
NODE_XS = (62, 167, 272, 377)
NULL_X = 466


def _chain(c, y, values, states=None, edge_colors=None, edge_labels=None, head=False):
    """A row of singly linked nodes centred at height ``y``, ending in an arrow to null."""
    states = states or [None] * len(values)
    for x, value, state in zip(NODE_XS, values, states):
        c.node(x, y, value, radius=NODE_R, state=state)
    if head:
        c.text(NODE_XS[0], y - NODE_R - 12, "head", anchor="middle", size=18, color="muted")
    for index in range(len(values) - 1):
        c.edge((NODE_XS[index], y), (NODE_XS[index + 1], y), color=(edge_colors or {}).get(index, "line"),
               radius=NODE_R, label=(edge_labels or {}).get(index))
    _to_null(c, y, len(values) - 1)


def _to_null(c, y, last):
    c.arrow(NODE_XS[last] + NODE_R + 4, y, NULL_X - 26, y, color="muted")
    c.text(NULL_X, y + 6, "null", anchor="middle", size=18, color="muted")


def _write_lines(c, y, color, first, second):
    """Two numbered link writes, each a chip and a bold code text."""
    for offset, (name, code) in enumerate((("write 1", first), ("write 2", second))):
        top = y + offset * 36
        left = 44 + c.chip(44, top, name, color=color) + 12
        c.text(left, top + 21, code, weight="700")


def singly_insert_after(c, fixtures):
    """Two link writes in a fixed order; the wrong order overwrites the only reference to the tail."""
    before = ints(fixtures, "linear_more.insert.before")
    pred = int(fixtures["linear_more.insert.predecessorIndex"])
    value = int(fixtures["linear_more.insert.value"])
    result = ints(fixtures, "linear_more.insert.result")
    reachable = ints(fixtures, "linear_more.insert.wrongReachable")
    lost = ints(fixtures, "linear_more.insert.wrongLost")
    predecessor = before[pred]
    assert result == before[:pred + 1] + [value] + before[pred + 1:]
    assert reachable == [predecessor, value] and lost == before[pred + 1:]

    # 1. The chain and the new, unlinked node.
    y = c.step(156, 1, f"Insert {value} after {predecessor}")
    row = y + 48
    _chain(c, row, before, head=True)
    # The new node sits apart from the chain, with no link in or out.
    c.node(462, 150, value, radius=NODE_R, state="warn")
    c.text(430, 157, "new, not linked", anchor="end", size=18, color="gold", weight="700")

    # 2. The right order.
    y = c.step(row + 56, 2, "Right order: copy the link, then redirect")
    _write_lines(c, y, "green", f"{value}.next = {predecessor}.next", f"{predecessor}.next = {value}")
    row = y + 112
    _chain(c, row, result, states=["good" if v == value else None for v in result],
           edge_colors={pred: "green", pred + 1: "green"}, edge_labels={pred: "2", pred + 1: "1"})

    # 3. The wrong order.
    y = c.step(row + 66, 3, "Wrong order: redirect first")
    _write_lines(c, y, "red", f"{predecessor}.next = {value}", f"{value}.next = {predecessor}.next")
    row = y + 100
    for x, v, state in zip(NODE_XS, reachable + lost, [None, "warn", "bad", "bad"]):
        c.node(x, row, v, radius=NODE_R, state=state)
    c.edge((NODE_XS[0], row), (NODE_XS[1], row), color="red", radius=NODE_R)
    c.edge((NODE_XS[2], row), (NODE_XS[3], row), color="red", radius=NODE_R, dashed=True)
    _to_null(c, row, 3)
    bottom = row + NODE_R
    x = NODE_XS[1]
    c.path(f"M {x + 14} {bottom + 2} C {x + 34} {bottom + 42} {x - 34} {bottom + 42} {x - 14} {bottom + 2}",
           color="red", width=2.5, arrow=True)
    c.text(x, bottom + 72, f"{value}.next = {value}", anchor="middle", size=18, color="red", weight="700")
    c.brace(NODE_XS[2] - NODE_R, NODE_XS[3] + NODE_R, bottom + 14,
            f"{lost[0]} and {lost[1]} unreachable", color="red")

    y = c.legend(bottom + 100, [("warn", "new node"), ("good", "linked"), ("bad", "lost")])
    c.note(y + 14, INSERT_IDEA)


# ---------------------------------------------------------------------------------------------
# stack-bracket-matching

BRACKET_IDEA = "The stack holds exactly the unmatched openers; a closer must match the top."
OPENERS, CLOSERS = "([{", ")]}"


def _bracket_trace(text):
    """Simulate the stack: per character a (operation, stack-after) pair, stopping at a mismatch."""
    stack, steps = [], []
    for token in text:
        if token in OPENERS:
            stack.append(token)
            steps.append(("push", "".join(stack) or "-"))
        elif not stack or stack[-1] != OPENERS[CLOSERS.index(token)]:
            steps.append(("reject", "".join(stack) or "-"))
            break
        else:
            stack.pop()
            steps.append(("pop", "".join(stack) or "-"))
    return steps


def _bracket_grid(c, y, text, steps):
    """A read / op / stack table for one string; characters after a rejection stay blank."""
    cell = 66
    reads = list(text)
    ops = [op for op, _ in steps] + [""] * (len(text) - len(steps))
    stacks = [("none" if stack == "-" else stack) for _, stack in steps] + [""] * (len(text) - len(steps))
    state_of = {"push": "focus", "pop": "good", "reject": "bad"}
    states = {}
    for column, op in enumerate(ops):
        if op == "":
            states[(0, column)] = states[(1, column)] = states[(2, column)] = "muted"
            continue
        states[(1, column)] = state_of[op]
        if op == "reject":
            states[(2, column)] = "bad"
    if stacks[len(steps) - 1] == "none":
        states[(2, len(steps) - 1)] = "good"
    return c.grid(90, y, [reads, [("stop" if op == "reject" else op) for op in ops], stacks],
                  cell_w=cell, cell_h=44, row_labels=["read", "op", "stack"], states=states)


def stack_bracket_matching(c, fixtures):
    """An ArrayStack trace of one balanced and one unbalanced bracket string."""
    balanced = fixtures["linear_more.brackets.balanced"]
    unbalanced = fixtures["linear_more.brackets.unbalanced"]
    mismatch_at = int(fixtures["linear_more.brackets.mismatchAt"])
    mismatch_top = fixtures["linear_more.brackets.mismatchTop"]
    good_steps = _bracket_trace(balanced)
    bad_steps = _bracket_trace(unbalanced)
    # The picture is derived from the fixture strings; the Java test asserts the same stacks.
    assert [stack for _, stack in good_steps] == fixtures["linear_more.brackets.balancedStacks"].split(",")
    assert [stack for op, stack in bad_steps if op != "reject"] == \
        fixtures["linear_more.brackets.unbalancedStacks"].split(",")
    assert bad_steps[-1][0] == "reject" and len(bad_steps) - 1 == mismatch_at

    y = c.step(156, 1, f"Balanced {balanced}: the stack ends empty")
    y = _bracket_grid(c, y + 4, balanced, good_steps)

    y = c.step(y + 52, 2, f"Unbalanced {unbalanced}: rejected at character {mismatch_at + 1}")
    y = _bracket_grid(c, y + 4, unbalanced, bad_steps)
    closer = unbalanced[mismatch_at]
    wanted = OPENERS[CLOSERS.index(closer)]
    c.text(28, y + 32, f"{closer} needs {wanted}, but the top is {mismatch_top}.", size=18, color="red",
           weight="700")

    y = c.legend(y + 78, [("focus", "push"), ("good", "pop: match"), ("bad", "mismatch: stop"),
                          ("muted", "not read")])
    c.note(y + 14, BRACKET_IDEA)


# ---------------------------------------------------------------------------------------------
# deque-both-ends

DEQUE_IDEA = "Both ends are indices on a circular buffer: addFirst steps the head back and wraps."
DEQUE_X, DEQUE_CELL, DEQUE_GAP = 36, 72, 8


def _deque_step(c, y, number, heading, slots, head, tail, order):
    """One state of the buffer: four slots with the head and tail slots named, and the order."""
    y = c.step(y, number, heading)
    states = []
    labels = []
    for index, slot in enumerate(slots):
        if index == head:
            states.append("focus")
            labels.append(f"{index} head")
        elif index == tail:
            states.append("alt")
            labels.append(f"{index} tail")
        else:
            states.append("muted" if slot == "." else None)
            labels.append(str(index))
    c.cell_row(DEQUE_X, y, ["" if slot == "." else slot for slot in slots], cell=DEQUE_CELL, gap=DEQUE_GAP,
               states=states, labels=labels)
    c.text(436, y + 18, "front to back", anchor="middle", size=18, color="muted")
    c.text(436, y + 44, " ".join(map(str, order)), anchor="middle", size=18, weight="700")


def deque_both_ends(c, fixtures):
    """RingDeque adds and removes at both ends of a four-slot circular buffer."""
    steps = []
    for number in (1, 2, 3, 4):
        prefix = f"linear_more.deque.step{number}."
        steps.append((fixtures[prefix + "slots"].split(","), int(fixtures[prefix + "head"]),
                      int(fixtures[prefix + "tail"]), ints(fixtures, prefix + "order")))
    removed_last = fixtures["linear_more.deque.step4.removedLast"]
    removed_first = fixtures["linear_more.deque.step4.removedFirst"]
    headings = ["addLast(20), addLast(30)", "addFirst(10): head wraps to slot 3",
                "addLast(40): the buffer is full",
                f"removeLast() = {removed_last}, removeFirst() = {removed_first}"]
    y = 156
    for number, (heading, (slots, head, tail, order)) in enumerate(zip(headings, steps), start=1):
        _deque_step(c, y, number, heading, slots, head, tail, order)
        y += 128
    c.text(28, y + 6, "Slots follow the circular-buffer rule (conceptual).", size=18, color="muted")
    y = c.legend(y + 40, [("focus", "head: front"), ("alt", "tail: back"), ("muted", "free slot")])
    c.note(y + 14, DEQUE_IDEA)


# ---------------------------------------------------------------------------------------------
# array-vs-list-access


ACCESS_IDEA = "An array reaches any index in one jump; a list walks links from the nearer end."


def array_vs_list_access(c, fixtures):
    """get(i) in a DynamicArray is one jump; in a DoublyLinkedList it is hops from the nearer end."""
    values = ints(fixtures, "linear_more.access.values")
    index = int(fixtures["linear_more.access.index"])
    value = int(fixtures["linear_more.access.value"])
    hops = ints(fixtures, "linear_more.access.hops")
    count = len(values)
    assert values[index] == value
    assert hops == [i if i < count // 2 else count - 1 - i for i in range(count)]
    from_head = index < count // 2
    assert [i for i in range(count) if i >= count // 2] == ints(fixtures, "linear_more.access.fromTail")
    steps = index if from_head else count - 1 - index
    assert not from_head and steps == hops[index]

    def x(i):
        return c.cell_center(ROW_X, i, CELL, GAP)

    # 1. The array: one jump.
    y = c.step(156, 1, f"Array get({index}): one jump")
    states = ["focus" if i == index else None for i in range(count)]
    y = c.cell_row(ROW_X, y, values, cell=CELL, gap=GAP, states=states, labels=list(range(count)))
    c.text(28, y + 22, f"Index {index} is at start + {index} slots: no earlier slot is read.", size=18,
           color="muted")

    # 2. The list: hops from the nearer end.
    y = c.step(y + 56, 2, f"List get({index}): {steps} hops from the tail")
    top = y + 44
    states = [None] * count
    for i in range(index, count):
        states[i] = "alt"
    states[index] = "focus"
    labels = [str(i) for i in range(count)]
    labels[0] = "0 head"
    labels[-1] = f"{count - 1} tail"
    y = c.cell_row(ROW_X, top, values, cell=CELL, gap=GAP, states=states, labels=labels)
    c.brace(x(index), x(count - 1), top - 10, f"{steps} hops", color="violet", above=True)
    c.text(28, y + 22, f"{index} is not below {count}/2, so the walk starts at the tail.", size=18, color="muted")

    # 3. Hops from the nearer end for every index.
    y = c.step(y + 56, 3, "Links followed from the nearer end")
    states = ["good" if i < count // 2 else "alt" for i in range(count)]
    y = c.cell_row(ROW_X, y, hops, cell=CELL, gap=GAP, states=states, labels=list(range(count)))
    c.text(28, y + 22, f"At most {max(hops)} hops for {count} nodes; an array needs 1 jump.", size=18,
           color="muted")

    y = c.legend(y + 58, [("focus", f"index {index}"), ("alt", "reached from the tail"),
                          ("good", "reached from the head")])
    c.note(y + 14, ACCESS_IDEA)


FIGURES = [
    {
        "id": "dynamic-array-growth",
        "title": "Appending to a full array",
        "category": "Linear structures",
        "description": ("A DynamicArray with capacity 4 holds 10, 20, 30 and 40, so add(50) allocates 8 "
                        "slots, copies the four values and writes 50 at index 4. That append copies 4 "
                        "values; the next copy happens at append 9 and moves 8, for 12 copies in 9 appends."),
        "lesson": "data-structures/arrays/operations.md",
        "keyIdea": GROWTH_IDEA,
        "height": 856,
        "draw": dynamic_array_growth,
    },
    {
        "id": "singly-insert-after",
        "title": "Insert after a known node",
        "category": "Linear structures",
        "description": ("In the chain 10, 20, 30, inserting 15 after 10 takes two link writes: 15.next = 10.next, "
                        "then 10.next = 15, giving 10, 15, 20, 30. Writing 10.next = 15 first makes 15.next "
                        "point at 15 itself and leaves 20 and 30 unreachable."),
        "lesson": "data-structures/linked-lists/singly.md",
        "keyIdea": INSERT_IDEA,
        "height": 884,
        "draw": singly_insert_after,
    },
    {
        "id": "stack-bracket-matching",
        "title": "Matching brackets with a stack",
        "category": "Linear structures",
        "description": ("An ArrayStack traces {[()]}: three pushes and three pops leave it empty. The same "
                        "stack traced on ([)] holds ( and [ when the closer ) arrives, and the top [ does "
                        "not match it, so the string is rejected at the third character."),
        "lesson": "data-structures/stacks/implementation.md",
        "keyIdea": BRACKET_IDEA,
        "height": 790,
        "draw": stack_bracket_matching,
    },
    {
        "id": "deque-both-ends",
        "title": "A deque at both ends",
        "category": "Linear structures",
        "description": ("A RingDeque with four slots receives addLast(20), addLast(30), addFirst(10) and "
                        "addLast(40); addFirst moves the head from slot 0 back to slot 3, and the tail ends "
                        "at slot 2. removeLast() returns 40 and removeFirst() returns 10, leaving 20 and 30."),
        "lesson": "data-structures/queues/implementation.md",
        "keyIdea": DEQUE_IDEA,
        "height": 890,
        "draw": deque_both_ends,
    },
    {
        "id": "array-vs-list-access",
        "title": "Reaching index 5",
        "category": "Linear structures",
        "description": ("In a DynamicArray of eight values, get(5) reads slot 5 in one jump. In a "
                        "DoublyLinkedList of the same values it starts at the tail and follows 2 links, "
                        "because 5 is in the back half; the hop counts for indices 0 to 7 are 0, 1, 2, 3, "
                        "3, 2, 1 and 0."),
        "lesson": "data-structures/arrays/overview.md",
        "keyIdea": ACCESS_IDEA,
        "height": 864,
        "draw": array_vs_list_access,
    },
]
