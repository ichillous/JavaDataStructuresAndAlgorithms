"""Sorting figures; fixtures in examples-sorting.properties.

Intermediate states of the in-place int[] sorts are not observable through the API, so each
figure shows the states the documented algorithm produces; VisualExamplesSortingTest re-derives
every one of them from the documented invariant and checks the final result against the library.
"""

from drawing import ints

CATEGORY = "Sorting and searching"
ROW_X, CELL, GAP = 39, 56, 8


def edge(index, side):
    return ROW_X + index * (CELL + GAP) + (0 if side == "left" else CELL)


def row(c, y, values, states, labels=None):
    return c.cell_row(ROW_X, y, values, cell=CELL, gap=GAP, states=states, labels=labels)


# ---------------------------------------------------------------- insertion-sort-shift
INSERTION_IDEA = ("Insertion sort shifts only the larger values one slot right, then drops the saved "
                  "key into the gap, so the sorted prefix grows by one.")


def insertion_sort_shift(c, fixtures):
    values = ints(fixtures, "sorting.insertion.values")
    index = int(fixtures["sorting.insertion.index"])
    key = int(fixtures["sorting.insertion.key"])
    shifted = ints(fixtures, "sorting.insertion.shifted")
    dest = int(fixtures["sorting.insertion.destination"])
    after = ints(fixtures, "sorting.insertion.after")
    n = len(values)
    tail = ["warn"] * (n - index - 1)

    y = c.step(156, 1, f"Prefix is sorted; save key {key}")
    y = row(c, y + 4, values, ["good"] * index + ["focus"] + tail, list(range(n)))

    y = c.step(y + 26, 2, f"Shift {len(shifted)} larger values right")
    states = ["good"] * dest + ["muted"] + ["alt"] * len(shifted) + tail
    cells = values[:dest] + [""] + shifted + values[index + 1:]
    y = row(c, y + 4, cells, states, list(range(n)))
    c.brace(edge(dest + 1, "left"), edge(index, "right"), y + 4, f"{len(shifted)} shifts")

    y = c.step(y + 64, 3, f"Drop {key} into the gap at index {dest}")
    states = ["good"] * dest + ["focus"] + ["good"] * (index - dest) + tail
    y = row(c, y + 4, after, states, list(range(n)))
    c.brace(edge(0, "left"), edge(index, "right"), y + 4, f"sorted prefix: {index + 1} values",
            color="green")

    y = c.legend(y + 70, [("good", "sorted prefix"), ("focus", "the key"), ("alt", "shifted right"),
                          ("muted", "gap"), ("warn", "not yet inserted")])
    c.note(y + 14, INSERTION_IDEA)


# ------------------------------------------------------------------ heapsort-phases
HEAP_IDEA = ("Heapsort builds a max-heap, then swaps the root to the end and repairs the shorter heap; "
             "the sorted suffix grows by one each time.")


def heapsort_phases(c, fixtures):
    source = ints(fixtures, "sorting.heap.input")
    built = ints(fixtures, "sorting.heap.built")
    one = ints(fixtures, "sorting.heap.afterOne")
    two = ints(fixtures, "sorting.heap.afterTwo")
    n = len(source)

    def heap_states(size):
        return ["focus"] + ["alt"] * (size - 1) + ["good"] * (n - size)

    def suffix_brace(y, size):
        c.brace(edge(size, "left"), edge(n - 1, "right"), y + 4, f"sorted: {n - size}", color="green")

    y = c.step(156, 1, "Build a max-heap in place")
    c.text(ROW_X, y + 12, "input", size=18, color="muted")
    y = row(c, y + 22, source, [None] * n)
    c.text(ROW_X, y + 28, "every parent is at least its children", size=18, color="muted")
    y = row(c, y + 38, built, heap_states(n))

    y = c.step(y + 40, 2, "Swap the root to the end, repair")
    c.text(ROW_X, y + 12, "after extracting 1 maximum", size=18, color="muted")
    y = row(c, y + 22, one, heap_states(n - 1))
    suffix_brace(y, n - 1)
    c.text(ROW_X, y + 66, "after 2 maxima; repeat until sorted", size=18, color="muted")
    y = row(c, y + 76, two, heap_states(n - 2))
    suffix_brace(y, n - 2)

    y = c.legend(y + 60, [("focus", "heap root"), ("alt", "rest of the heap"), ("good", "sorted suffix")])
    c.note(y + 14, HEAP_IDEA)


# ------------------------------------------------------------ counting-sort-counts
COUNTING_IDEA = ("Counting sort never compares two values: it tallies each value, then writes each "
                 "value back as many times as it was counted.")


def counting_sort_counts(c, fixtures):
    values = ints(fixtures, "sorting.counting.input")
    low = int(fixtures["sorting.counting.min"])
    high = int(fixtures["sorting.counting.max"])
    span = int(fixtures["sorting.counting.range"])
    counts = ints(fixtures, "sorting.counting.counts")
    done = ints(fixtures, "sorting.counting.sorted")
    n = len(values)

    y = c.step(156, 1, f"Range {high} - {low} + 1 = {span} values")
    y = row(c, y + 4, values, [None] * n)

    y = c.step(y + 44, 2, f"Tally into counts[value - {low}]")
    y = row(c, y + 4, counts, ["alt"] * span, [str(low + i) for i in range(span)])
    c.text(ROW_X, y + 16, "box = how often the value below occurs", size=18, color="muted")

    y = c.step(y + 70, 3, "Write each value back, count times")
    y = row(c, y + 4, done, ["good"] * n)

    y = c.legend(y + 40, [("alt", "count of a value"), ("good", "written in order")])
    c.note(y + 14, COUNTING_IDEA)


# --------------------------------------------------------------- radix-lsd-passes
RADIX_IDEA = ("Radix sort orders by the least significant digit first; because every pass is stable, "
              "ties keep the earlier order and the last pass leaves a full sort.")


def radix_lsd_passes(c, fixtures):
    source = ints(fixtures, "sorting.radix.input")
    ones = ints(fixtures, "sorting.radix.onesDigits")
    first = ints(fixtures, "sorting.radix.afterOnes")
    tens = ints(fixtures, "sorting.radix.tensDigits")
    second = ints(fixtures, "sorting.radix.afterTens")
    n = len(source)

    y = c.step(156, 1, "Stable pass on the ones digit")
    y = row(c, y + 4, source, ["warn"] * n, [f"d={d}" for d in ones])
    c.text(ROW_X, y + 22, "ties keep input order:", size=18, color="muted")
    y = row(c, y + 32, first, ["alt"] * n)

    y = c.step(y + 44, 2, "Stable pass on the tens digit")
    y = row(c, y + 4, first, ["warn"] * n, [f"d={d}" for d in tens])
    c.text(ROW_X, y + 22, "ties keep the previous order:", size=18, color="muted")
    y = row(c, y + 32, second, ["good"] * n)

    y = c.legend(y + 30, [("warn", "digit being read"), ("alt", "after the ones pass"),
                          ("good", "after the tens pass")])
    c.text(28, y + 20, "Base 10 shown; radixSort uses 8-bit digits.", size=18, color="muted")
    c.note(y + 40, RADIX_IDEA)


# ---------------------------------------------------------------- sort-stability
STABILITY_IDEA = ("A stable sort keeps equal keys in their input order; an unstable sort may reorder "
                  "them, losing any earlier ordering by tag.")


def sort_stability(c, fixtures):
    records = fixtures["sorting.stability.records"].split(",")
    stable = fixtures["sorting.stability.stable"].split(",")
    other = fixtures["sorting.stability.hypothetical"].split(",")
    n = len(records)
    arrival = {record: i for i, record in enumerate(records)}

    def states(outcome):
        """'bad' for a record that has an equal-key record in the opposite order to the input."""
        result = []
        for i, item in enumerate(outcome):
            flipped = any(o != item and o[0] == item[0]
                          and (arrival[o] < arrival[item]) != (outcome.index(o) < i) for o in outcome)
            result.append("bad" if flipped else "good")
        return result

    y = c.step(156, 1, "Input: key, then tag (arrival order)")
    y = row(c, y + 4, records, [None] * n)

    y = c.step(y + 44, 2, "Stable: mergeSort(T[], Comparator)")
    y = row(c, y + 4, stable, states(stable))
    c.text(ROW_X, y + 28, "equal keys keep their tag order", size=18, color="muted")

    y = c.step(y + 74, 3, "One unstable outcome (hypothetical)")
    y = row(c, y + 4, other, states(other))
    c.text(ROW_X, y + 28, "sorted by key, tags reordered", size=18, color="muted")
    c.text(ROW_X, y + 58, "int[] quickSort and heapSort promise", size=18, color="muted")
    c.text(ROW_X, y + 82, "no stability: equal keys may reorder.", size=18, color="muted")

    y = c.legend(y + 124, [("good", "equal keys in input order"), ("bad", "equal keys reordered")])
    c.note(y + 14, STABILITY_IDEA)


FIGURES = [
    {
        "id": "insertion-sort-shift", "title": "One insertion step", "category": CATEGORY,
        "description": ("In 3, 5, 8, 9, 4, 1 the prefix 3, 5, 8, 9 is sorted. The key 4 is saved, the three "
                        "larger values 5, 8 and 9 shift one slot right, and 4 drops into index 1."),
        "lesson": "algorithms/sorting/insertion-sort.md", "keyIdea": INSERTION_IDEA,
        "height": 877, "draw": insertion_sort_shift,
    },
    {
        "id": "heapsort-phases", "title": "Heapsort phases", "category": CATEGORY,
        "description": ("Heapsort on 3, 1, 6, 5, 2, 4 builds the max-heap 6, 5, 4, 1, 2, 3, then moves 6 "
                        "and 5 to the end, giving 5, 3, 4, 1, 2, 6 and 4, 3, 2, 1, 5, 6, until 1 to 6 is sorted."),
        "lesson": "algorithms/sorting/heap-sort.md", "keyIdea": HEAP_IDEA,
        "height": 893, "draw": heapsort_phases,
    },
    {
        "id": "counting-sort-counts", "title": "Counting sort tally", "category": CATEGORY,
        "description": ("For 5, 3, 6, 3, 4, 6 the range 3 to 6 has four counts: 2, 1, 1 and 2. Writing each "
                        "value back that many times gives 3, 3, 4, 5, 6, 6."),
        "lesson": "algorithms/sorting/non-comparison-and-comparator-sorts.md", "keyIdea": COUNTING_IDEA,
        "height": 775, "draw": counting_sort_counts,
    },
    {
        "id": "radix-lsd-passes", "title": "Radix passes by digit", "category": CATEGORY,
        "description": ("Sorting 35, 21, 41, 15, 31, 25 by the ones digit gives 21, 41, 31, 35, 15, 25; a "
                        "stable pass on the tens digit then gives 15, 21, 25, 31, 35, 41."),
        "lesson": "algorithms/sorting/non-comparison-and-comparator-sorts.md", "keyIdea": RADIX_IDEA,
        "height": 898, "draw": radix_lsd_passes,
    },
    {
        "id": "sort-stability", "title": "Stable and unstable outcomes", "category": CATEGORY,
        "description": ("Records 3a, 1b, 3c, 2d, 1e, 3f sorted by their number: a stable sort gives 1b, 1e, 2d, "
                        "3a, 3c, 3f, keeping tags in order, while a hypothetical unstable outcome 1e, 1b, 2d, 3c, "
                        "3a, 3f reorders equal keys."),
        "lesson": "algorithms/sorting/non-comparison-and-comparator-sorts.md", "keyIdea": STABILITY_IDEA,
        "height": 863, "draw": sort_stability,
    },
]
