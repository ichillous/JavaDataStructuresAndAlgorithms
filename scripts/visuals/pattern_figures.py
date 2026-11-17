"""Array-pattern and divide-and-conquer figures; fixtures in examples-pattern.properties.

Pointer positions, windows and stacks are private to ``ArrayPatterns``. Every state drawn here
is read from the fixture file; VisualExamplesPatternTest re-derives each one by following the
documented invariant and asserts it equals the fixture, so the pictures cannot drift from the
code.
"""

from drawing import ints

CATEGORY = "Array patterns"


def _strings(fixtures, key):
    value = fixtures[key]
    return [part.strip() for part in value.split(",")] if value else []


# ---------------------------------------------------------------------------------------------
# two-pointers-pair-sum

PAIR_IDEA = "Each move drops an index with no partner: at most n - 1 moves."


def two_pointers_pair_sum(c, fixtures):
    values = ints(fixtures, "pattern.pair.values")
    target = int(fixtures["pattern.pair.target"])
    lefts = ints(fixtures, "pattern.pair.lefts")
    rights = ints(fixtures, "pattern.pair.rights")
    sums = ints(fixtures, "pattern.pair.sums")
    first, second = int(fixtures["pattern.pair.first"]), int(fixtures["pattern.pair.second"])
    n = len(values)
    reasons = {
        "large": "{v} is too large even with the smallest, {s}",
        "small": "{v} is too small even with the largest, {s}",
    }

    y = 156
    for step, (left, right, total) in enumerate(zip(lefts, rights, sums), 1):
        match = total == target
        verdict = "match" if match else ("too large" if total > target else "too small")
        row_y = c.step(y, step, f"{values[left]} + {values[right]} = {total}: {verdict}")
        # Indices dropped by earlier steps are out of play.
        dropped = set(range(left)) | set(range(right + 1, n))
        states = [("good" if match else "focus") if i in (left, right) else ("muted" if i in dropped else None)
                  for i in range(n)]
        labels = [f"left {left}" if i == left else f"right {right}" if i == right else "" for i in range(n)]
        c.cell_row(39, row_y, values, cell=56, gap=8, states=states, labels=labels)
        if match:
            line = f"Indices {first} and {second} sum to the target {target}"
        elif total > target:
            line = reasons["large"].format(v=values[right], s=values[left])
        else:
            line = reasons["small"].format(v=values[left], s=values[right])
        c.text(39, row_y + 100, line, size=18, color="muted")
        y += 168
    y = c.legend(y - 6, [("focus", "left and right"), ("muted", "dropped"), ("good", "match")])
    c.note(y + 14, PAIR_IDEA)


# ---------------------------------------------------------------------------------------------
# sliding-window-distinct

WINDOW_IDEA = "The left edge never moves back, so each unit is added once: linear time."


def sliding_window_distinct(c, fixtures):
    text = fixtures["pattern.window.text"]
    shown = ints(fixtures, "pattern.window.shown")
    previous = ints(fixtures, "pattern.window.previous")
    starts = ints(fixtures, "pattern.window.starts")
    bests = ints(fixtures, "pattern.window.bests")
    span_start, span_end = int(fixtures["pattern.window.spanStart"]), int(fixtures["pattern.window.spanEnd"])
    n = len(text)
    # The heading depends on whether the earlier copy was inside the window at that time.
    y = 156
    previous_start = 0
    for step, (i, seen, start, best) in enumerate(zip(shown, previous, starts, bests), 1):
        unit = text[i]
        if seen == -1:
            grew_best = step > 1 and best > bests[step - 2]
            heading = (f"i = {i}: {unit} is new, best Span({span_start}, {span_end})" if grew_best
                       else f"i = {i}: {unit} is new, window grows")
        elif seen >= previous_start:
            heading = f"i = {i}: {unit} was at {seen}, start jumps to {start}"
        else:
            heading = f"i = {i}: {unit} at {seen} is stale, start stays"
        row_y = c.step(y, step, heading)
        states = []
        for index in range(n):
            if index > i:
                states.append(None)
            elif index == i:
                states.append("alt")
            elif index == seen:
                states.append("bad")
            elif index >= start:
                states.append("focus")
            else:
                states.append("muted")
        c.cell_row(39, row_y, list(text), cell=52, gap=8, states=states, labels=list(range(n)))
        previous_start = start
        y += 136
    y = c.legend(y + 6, [("focus", "window"), ("alt", "read now"), ("bad", "earlier copy"),
                         ("muted", "dropped")])
    c.note(y + 14, WINDOW_IDEA)


# ---------------------------------------------------------------------------------------------
# monotonic-stack-next-greater

STACK_IDEA = "Each index is pushed once and popped once: O(n) despite the inner loop."


def monotonic_stack_next_greater(c, fixtures):
    values = ints(fixtures, "pattern.stack.values")
    result = ints(fixtures, "pattern.stack.result")
    n = len(values)
    c.text(39, 156, "Input values and their indices", weight="700")
    c.cell_row(39, 170, values, cell=52, gap=8, labels=list(range(n)))
    c.text(492, 274, "stack, bottom to top", anchor="end", size=18, color="muted", weight="700")
    y = 306
    for i in range(n):
        pops = ints(fixtures, f"pattern.stack.pops{i}")
        after = ints(fixtures, f"pattern.stack.after{i}")
        value = values[i]
        action = f"pop {', '.join(map(str, pops))}, push {i}" if pops else f"push {i}"
        c.step(y, i + 1, f"i = {i}, value {value}: {action}")
        if pops:
            line = ", ".join(f"answer[{p}] = {i}" for p in pops)
            if len(after) > 1:
                line += f"; equal {values[after[-2]]} stays"
        elif after[:-1]:
            line = f"top value {values[after[-2]]} is not less than {value}"
        else:
            line = "stack is empty, nothing to compare"
        c.text(66, y + 26, line, size=18, color="muted")
        states = [None] * (len(after) - 1) + ["focus"]
        c.cell_row(398, y - 20, after, cell=44, gap=6, height=44, states=states)
        y += 72
    answer = ", ".join(str(a) for a in result)
    c.chip(39, y - 22, f"next greater index: {answer}", color="good")
    y = c.legend(y + 38, [("focus", "top of the stack")])
    c.note(y + 14, STACK_IDEA)


# ---------------------------------------------------------------------------------------------
# difference-array-ranges

DIFF_IDEA = "Two marks per update, then one running sum: O(n + u) in total."


def _signed(value):
    return f"+{value}" if value > 0 else str(value)


def difference_array_ranges(c, fixtures):
    length = int(fixtures["pattern.diff.length"])
    first = ints(fixtures, "pattern.diff.first")
    second = ints(fixtures, "pattern.diff.second")
    after_first = ints(fixtures, "pattern.diff.afterFirst")
    after_second = ints(fixtures, "pattern.diff.afterSecond")
    result = ints(fixtures, "pattern.diff.result")
    labels = [str(i) for i in range(length)] + ["spare"]

    def diff_row(y, diff, marks):
        states = [marks.get(i) for i in range(len(diff))]
        return c.cell_row(39, y, [_signed(v) if v else "0" for v in diff], cell=54, gap=8,
                          states=states, labels=labels)

    y = 156
    row_y = c.step(y, 1, f"Add {_signed(first[2])} on [{first[0]}, {first[1]}): two marks")
    diff_row(row_y, after_first, {first[0]: "good", first[1]: "bad"})
    y += 132
    row_y = c.step(y, 2, f"Add {_signed(second[2])} on [{second[0]}, {second[1]}): two more")
    diff_row(row_y, after_second, {second[0]: "good", second[1]: "bad"})
    y += 132
    row_y = c.step(y, 3, "One running sum recovers the values")
    c.cell_row(39, row_y, result, cell=54, gap=8, states=["focus"] * length, labels=[str(i) for i in range(length)])
    running = [str(after_second[0])]
    total = after_second[0]
    for index in range(1, length):
        total += after_second[index]
        running.append(f"{total}")
    c.text(39, row_y + 100, "sums: " + ", ".join(running), size=18, color="muted")
    y = c.legend(row_y + 142, [("good", "range start: add"), ("bad", "range end: undo"),
                               ("focus", "value after the sum")])
    c.note(y + 14, DIFF_IDEA)


# ---------------------------------------------------------------------------------------------
# interval-sweep-rooms

SWEEP_IDEA = "The busiest moment sets the room count; at a tie an end comes first."


def interval_sweep_rooms(c, fixtures):
    starts = ints(fixtures, "pattern.sweep.starts")
    ends = ints(fixtures, "pattern.sweep.ends")
    events = _strings(fixtures, "pattern.sweep.events")
    depths = ints(fixtures, "pattern.sweep.depths")
    rooms = int(fixtures["pattern.sweep.rooms"])
    horizon = max(ends)
    x0, unit = 70, 50

    def tx(time):
        return x0 + time * unit

    y = c.step(156, 1, "Intervals on a time axis")
    for index, (start, end) in enumerate(zip(starts, ends)):
        top = y + 4 + index * 36
        c.rect(tx(start), top, (end - start) * unit, 28, fill="soft", stroke="blue", radius=6)
        c.text((tx(start) + tx(end)) / 2, top + 21, f"[{start}, {end})", anchor="middle", size=18,
               color="blue", weight="700")
    axis_y = y + 4 + len(starts) * 36 + 4
    c.line(tx(0), axis_y, tx(horizon), axis_y, color="line")
    for time in range(horizon + 1):
        c.text(tx(time), axis_y + 26, time, anchor="middle", size=18, color="muted")
    touching = [(a, b) for a, e in zip(starts, ends) for b in starts if e == b and a != b]
    if touching:
        meet = ends[starts.index(touching[0][0])]
        c.line(tx(meet), y - 2, tx(meet), axis_y, color="gold", width=2, dashed=True)
        c.text(tx(meet) + 10, y + 20, f"touch at {meet}", size=18, color="gold", weight="700")

    y = c.step(axis_y + 74, 2, "Sort the events and track the depth")
    c.text(39, y + 20, "events, ends first at equal times", size=18, color="muted")
    row = y + 32
    n = len(events)
    cell, gap = 50, 6
    c.cell_row(39, row, events, cell=cell, gap=gap,
               states=["good" if event.startswith("S") else "bad" for event in events])
    c.text(39, row + 82, "rooms in use after each event", size=18, color="muted")
    c.cell_row(39, row + 94, depths, cell=cell, gap=gap,
               states=["focus" if depth == rooms else None for depth in depths])
    tie = [i for i in range(n - 1) if events[i][0] == "E" and events[i + 1][0] == "S"
           and events[i][1:] == events[i + 1][1:]]
    if tie:
        i = tie[0]
        c.brace(39 + i * (cell + gap), 39 + (i + 1) * (cell + gap) + cell, row + 156,
                f"tie at {events[i][1:]}: end first", color="gold")
    y = c.legend(row + 226, [("good", "S: a start"), ("bad", "E: an end"),
                             ("focus", f"deepest: {rooms} rooms")])
    c.note(y + 14, SWEEP_IDEA)


# ---------------------------------------------------------------------------------------------
# inversion-count-merge

INVERSION_IDEA = "A smaller right value jumps over every left value still waiting."


def inversion_count_merge(c, fixtures):
    left = ints(fixtures, "pattern.inversion.left")
    right = ints(fixtures, "pattern.inversion.right")
    emitted = ints(fixtures, "pattern.inversion.emitted")
    adds = ints(fixtures, "pattern.inversion.adds")
    total = int(fixtures["pattern.inversion.total"])
    cell, gap = 48, 6
    left_x = 39
    right_x = left_x + len(left) * (cell + gap) + 46

    def halves(row_y, left_states, right_states):
        c.cell_row(left_x, row_y, left, cell=cell, gap=gap, states=left_states)
        c.cell_row(right_x, row_y, right, cell=cell, gap=gap, states=right_states)

    def edges(x, count):
        return x, x + count * (cell + gap) - gap

    # 1. Two sorted halves; the library has already counted inside each (zero here).
    row_y = c.step(156, 1, "Sorted halves, 0 pairs inside each")
    halves(row_y, None, None)
    c.brace(*edges(left_x, len(left)), row_y + 60, "left half")
    c.brace(*edges(right_x, len(right)), row_y + 60, "right half")

    # 2. The first emitted value comes from the right and jumps over every left value.
    first_adds = adds[0]
    row_y = c.step(304, 2, f"Emit {emitted[0]}: it jumps over {left[0]} and {left[1]}")
    halves(row_y, ["bad"] * len(left), ["focus"] + [None] * (len(right) - 1))
    pairs = " and ".join(f"({a}, {emitted[0]})" for a in left)
    c.text(39, row_y + 76, f"+{first_adds}: pairs {pairs}", size=18, color="muted")

    # 3. After one left value is emitted (adding 0), the next right value jumps over what is left.
    second_right = right[1]
    waiting = left[len(left) - adds[2]:]
    row_y = c.step(442, 3, f"Emit {second_right}: it jumps over {waiting[0]}")
    halves(row_y, ["good"] * (len(left) - len(waiting)) + ["bad"] * len(waiting),
           ["good", "focus"] + [None] * (len(right) - 2))
    c.text(39, row_y + 76, f"+{adds[2]}: pair ({waiting[0]}, {second_right}); emitting {left[0]} added 0",
           size=18, color="muted")

    # 4. The per-emission additions, in merge order.
    row_y = c.step(580, 4, "Added as each value is emitted")
    states = ["focus" if add else None for add in adds]
    c.cell_row(39, row_y, emitted, cell=cell, gap=gap, states=states,
               labels=[f"+{a}" for a in adds])
    c.text(39, row_y + 100, " + ".join(map(str, adds)) + f" = {total} cross inversions", size=18,
           color="muted")
    y = c.legend(row_y + 136, [("focus", "from the right"), ("bad", "jumped over"),
                               ("good", "emitted")])
    c.note(y + 14, INVERSION_IDEA)


FIGURES = [
    {
        "id": "two-pointers-pair-sum",
        "title": "Two pointers, sorted array",
        "category": CATEGORY,
        "description": ("pairWithSum on 1, 2, 3, 4, 6 with target 6 starts at the two ends: 1 + 6 is too "
                        "large so the right index drops, 1 + 4 is too small so the left index drops, and "
                        "2 + 4 matches at indices 1 and 3."),
        "lesson": "algorithms/array-patterns/overview.md",
        "keyIdea": PAIR_IDEA,
        "height": 810,
        "draw": two_pointers_pair_sum,
    },
    {
        "id": "sliding-window-distinct",
        "title": "Window jumping a repeat",
        "category": CATEGORY,
        "description": ("longestSubstringWithoutRepeats on abcbad grows the window to abc, jumps its start "
                        "to index 2 when b repeats, ignores the stale earlier a at index 0, and ends with "
                        "the longest window cbad at indices 2 to 5."),
        "lesson": "algorithms/array-patterns/overview.md",
        "keyIdea": WINDOW_IDEA,
        "height": 892,
        "draw": sliding_window_distinct,
    },
    {
        "id": "monotonic-stack-next-greater",
        "title": "Next greater by stack",
        "category": CATEGORY,
        "description": ("nextGreaterIndices on 2, 1, 2, 4, 3 keeps unresolved indices on a stack: index 1 "
                        "is resolved by the second 2, the equal 2 at index 0 stays, and the 4 resolves "
                        "indices 2 and 0, giving 3, 2, 3, -1, -1."),
        "lesson": "algorithms/array-patterns/overview.md",
        "keyIdea": STACK_IDEA,
        "height": 860,
        "draw": monotonic_stack_next_greater,
    },
    {
        "id": "difference-array-ranges",
        "title": "Range adds by differences",
        "category": CATEGORY,
        "description": ("applyRangeAdds on five zeros marks +3 at index 1 and -3 at index 4, then +2 at "
                        "index 0 and -2 at index 2, and one running sum produces 2, 5, 3, 3, 0."),
        "lesson": "algorithms/array-patterns/overview.md",
        "keyIdea": DIFF_IDEA,
        "height": 772,
        "draw": difference_array_ranges,
    },
    {
        "id": "interval-sweep-rooms",
        "title": "Sweeping events for rooms",
        "category": CATEGORY,
        "description": ("minimumMeetingRooms on the intervals [1, 4), [2, 5), [5, 7) and [6, 8) sorts the "
                        "eight start and end events, processes the end at 5 before the start at 5, and "
                        "reaches a depth of 2, so two rooms suffice."),
        "lesson": "algorithms/array-patterns/overview.md",
        "keyIdea": SWEEP_IDEA,
        "height": 840,
        "draw": interval_sweep_rooms,
    },
    {
        "id": "inversion-count-merge",
        "title": "Inversions during a merge",
        "category": CATEGORY,
        "description": ("countInversions on 2, 4, 1, 3, 5 merges the sorted halves 2, 4 and 1, 3, 5: "
                        "emitting 1 jumps over two left values, emitting 3 jumps over one, and the other "
                        "values add nothing, for 3 inversions."),
        "lesson": "algorithms/divide-and-conquer/problems.md",
        "keyIdea": INVERSION_IDEA,
        "height": 892,
        "draw": inversion_count_merge,
    },
]
