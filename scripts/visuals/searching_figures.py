"""Searching and selection figures; fixtures in examples-searching.properties.

Each figure shows the example stored in the fixture file. VisualExamplesSearchingTest checks
the same values against SearchAlgorithms and Selection. Where a figure shows a state that the
library keeps private (the partitions inside Selection.kthSmallest), this module derives it
from the documented rule and refuses to draw if the result differs from the fixture, which the
Java test re-derives independently.
"""

from drawing import ints

SCAN_IDEA = "A hit stops the scan; only a miss costs n comparisons."
CAPACITY_IDEA = "Once a capacity fits, every larger one fits: each probe halves the range."
QUICK_IDEA = "k lies in one part of each partition: keep it, discard the rest."
MERGE_IDEA = "The smallest head is the next output; ties pop in list order."


def _pairs(fixtures, key):
    """Parses 'a:b,c:d' into [(a, b), (c, d)]."""
    return [tuple(int(part) for part in item.split(":")) for item in fixtures[key].split(",")]


# ---------------------------------------------------------------------------------------------
# linear-scan-first-match
# ---------------------------------------------------------------------------------------------

def linear_scan_first_match(c, fixtures):
    values = ints(fixtures, "searching.scan.values")
    hit_target = int(fixtures["searching.scan.hitTarget"])
    hit_index = int(fixtures["searching.scan.hitIndex"])
    hit_comparisons = int(fixtures["searching.scan.hitComparisons"])
    miss_target = int(fixtures["searching.scan.missTarget"])
    miss_result = int(fixtures["searching.scan.missResult"])
    miss_comparisons = int(fixtures["searching.scan.missComparisons"])
    labels = list(range(len(values)))
    row_x = 28

    # 1. A hit: stop at the first match, ignore the duplicate behind it.
    y = c.step(156, 1, f"Search {hit_target}: stop at the first match")
    states = ["bad" if i < hit_index else "good" if i == hit_index else "muted"
              for i in range(len(values))]
    y = c.cell_row(row_x, y, values, states=states, labels=labels)
    c.text(row_x, y + 22, f"{hit_comparisons} comparisons, returns index {hit_index}.", size=18)
    duplicate = values.index(hit_target, hit_index + 1)
    c.text(row_x, y + 48, f"The {hit_target} at index {duplicate} is never inspected.", size=18,
           color="muted")

    # 2. A miss: every cell is inspected before the search can say no.
    y = c.step(y + 100, 2, f"Search {miss_target}: inspect every cell")
    y = c.cell_row(row_x, y, values, states=["bad"] * len(values), labels=labels)
    c.text(row_x, y + 22, f"{miss_comparisons} comparisons, returns {miss_result}.", size=18)
    c.text(row_x, y + 48, f"{miss_comparisons} comparisons = n; absence cannot be known sooner.", size=18,
           color="muted")

    y = c.legend(y + 100, [("bad", "inspected, not equal"), ("good", "first match, returned"),
                           ("muted", "never inspected")])
    c.note(y + 14, SCAN_IDEA)


# ---------------------------------------------------------------------------------------------
# predicate-boundary
# ---------------------------------------------------------------------------------------------

def predicate_boundary(c, fixtures):
    weights = ints(fixtures, "searching.capacity.weights")
    days = int(fixtures["searching.capacity.days"])
    low0 = int(fixtures["searching.capacity.low"])
    high0 = int(fixtures["searching.capacity.highExclusive"])
    answer = int(fixtures["searching.capacity.answer"])
    needed = ints(fixtures, "searching.capacity.daysNeeded")
    probes = _pairs(fixtures, "searching.capacity.probes")
    capacities = list(range(low0, high0))
    cell, gap, x0 = 32, 4, 28

    # Replay firstTrue on the fixture's days-needed table and refuse to draw a different story.
    low, high, replay, ranges = low0, high0, [], []
    while low < high:
        mid = low + (high - low) // 2
        replay.append((mid, needed[mid - low0]))
        ranges.append((low, high))
        if needed[mid - low0] <= days:
            high = mid
        else:
            low = mid + 1
    if replay != probes or low != answer:
        raise ValueError(f"predicate-boundary fixture disagrees with a firstTrue replay: {replay}, {low}")

    # 1. The monotone row.
    y = c.step(156, 1, f"Which capacity c ships in {days} days?")
    c.text(x0, y + 4, "weights " + ", ".join(str(w) for w in weights) + ", in order", size=18, color="muted")
    y += 18
    fits = [needed[cap - low0] <= days for cap in capacities]
    y = c.cell_row(x0, y, capacities, cell=cell, gap=gap, height=40,
                   states=["good" if ok else "bad" for ok in fits],
                   labels=["no" if i == 0 else "yes" if i == fits.index(True) else "" for i in range(len(fits))])
    c.text(x0, y + 20, f"False, then true: the boundary is capacity {answer}.", size=18, color="muted")

    # 2. firstTrue narrows [low, high).
    y = c.step(y + 66, 2, f"firstTrue({low0}, {high0}, fits) probes the middle")
    for (low, high), (mid, used) in zip(ranges, probes):
        outcome = (f"needs {used} days, fits: high = {mid}" if used <= days
                   else f"needs {used} days, too slow: low = {mid + 1}")
        c.text(x0, y + 8, f"[{low}, {high}) probe {mid}: {outcome}", size=18)
        states = ["warn" if cap == mid else "focus" if low <= cap < high else "muted"
                  for cap in capacities]
        c.cell_row(x0, y + 22, capacities, cell=cell, gap=gap, height=32, states=states)
        y += 72
    c.text(x0, y + 8, f"low = high = {answer}: the smallest capacity that fits.", size=18, weight="700",
           color="blue")

    y = c.legend(y + 54, [("good", "fits"), ("bad", "too slow"), ("focus", "still undecided"),
                          ("warn", "probed middle"), ("muted", "ruled out")])
    c.note(y + 14, CAPACITY_IDEA)


# ---------------------------------------------------------------------------------------------
# quickselect-shrink
# ---------------------------------------------------------------------------------------------

def _derive_rounds(values, k):
    """The documented algorithm: median-of-three pivot, three-way partition, keep k's part."""
    a = list(values)
    low, high = 0, len(a) - 1
    rounds = []
    while low < high:
        sample = (a[low], a[low + (high - low) // 2], a[high])
        pivot = sorted(sample)[1]
        less_end, greater_start, scan = low, high, low
        while scan <= greater_start:
            if a[scan] < pivot:
                a[less_end], a[scan] = a[scan], a[less_end]
                less_end += 1
                scan += 1
            elif a[scan] > pivot:
                a[greater_start], a[scan] = a[scan], a[greater_start]
                greater_start -= 1
            else:
                scan += 1
        rounds.append({"low": low, "high": high, "sample": sample, "pivot": pivot,
                       "less_end": less_end, "greater_start": greater_start, "after": list(a)})
        if k < less_end:
            high = less_end - 1
        elif k > greater_start:
            low = greater_start + 1
        else:
            break
    return rounds


def quickselect_shrink(c, fixtures):
    values = ints(fixtures, "searching.quick.values")
    k = int(fixtures["searching.quick.k"])
    answer = int(fixtures["searching.quick.answer"])
    rounds = _derive_rounds(values, k)
    expected = [tuple(int(p) for p in item.split(":")) for item in fixtures["searching.quick.rounds"].split(",")]
    got = [(r["low"], r["high"], r["pivot"], r["less_end"], r["greater_start"]) for r in rounds]
    if got != expected:
        raise ValueError(f"quickselect-shrink fixture disagrees with the documented rule: {got}")
    for number, r in enumerate(rounds, 1):
        if r["after"] != ints(fixtures, f"searching.quick.after{number}"):
            raise ValueError(f"quickselect-shrink round {number} array disagrees with the fixture")

    row_x = 55
    y = 156
    for number, r in enumerate(rounds, 1):
        low, high, pivot = r["low"], r["high"], r["pivot"]
        less_end, greater_start, after = r["less_end"], r["greater_start"], r["after"]
        sample = ", ".join(str(v) for v in r["sample"])
        y = c.step(y, number, f"Round {number}: pivot {pivot} = median of {sample}")
        if number == 1:
            c.text(row_x, y + 4, "start " + " ".join(str(v) for v in values) + f"; k = {k} is a sorted index",
                   size=18, color="muted")
        in_less, in_equal, in_greater = (low, less_end - 1), (less_end, greater_start), (greater_start + 1, high)
        if k < less_end:
            kept, part = in_less, f"< {pivot}"
        elif k > greater_start:
            kept, part = in_greater, f"> {pivot}"
        else:
            kept, part = in_equal, f"= {pivot}"
        states = []
        for index in range(len(values)):
            if kept[0] <= index <= kept[1]:
                states.append("good" if part.startswith("=") else "focus")
            else:
                states.append("muted")
        y = c.cell_row(row_x, y + (18 if number == 1 else 4), after, states=states, height=44)

        def group(span):
            return "[" + " ".join(str(after[i]) for i in range(span[0], span[1] + 1)) + "]"

        c.text(row_x, y + 20, f"{group(in_less)} < {pivot}  {group(in_equal)} = {pivot}  "
                              f"{group(in_greater)} > {pivot}", size=18)
        if part.startswith("="):
            c.text(row_x, y + 46, f"k = {k} is in the = part: the answer is {answer}.", size=18,
                   weight="700", color="green")
        else:
            c.text(row_x, y + 46, f"k = {k} is in the {part} part: keep [{kept[0]}..{kept[1]}] only.",
                   size=18, weight="700", color="blue")
        y += 90

    y = c.legend(y + 6, [("focus", "part that holds k, kept"), ("muted", "discarded"),
                         ("good", "equal part: the answer")])
    c.note(y + 14, QUICK_IDEA)


# ---------------------------------------------------------------------------------------------
# kway-merge-heap
# ---------------------------------------------------------------------------------------------

def kway_merge_heap(c, fixtures):
    lists = [ints(fixtures, f"searching.merge.list{i}") for i in range(3)]
    names = "ABC"
    output = ints(fixtures, "searching.merge.output")
    sources = ints(fixtures, "searching.merge.sources")
    heads = _pairs(fixtures, "searching.merge.headsAfterThree")
    popped = 3
    cell, gap, x0 = 44, 6, 100

    def tag(value, source):
        return f"{value}·{names[source]}"

    # 1. One head per list.
    y = c.step(156, 1, "The heap holds each list's first value")
    for index, values in enumerate(lists):
        c.text(28, y + 30, names[index], weight="700")
        c.cell_row(x0, y + 4, values, cell=cell, gap=gap, height=40,
                   states=["focus"] + [None] * (len(values) - 1))
        y += 50
    left = x0
    c.text(28, y + 22, "heap", weight="700")
    for index, values in enumerate(lists):
        left += c.chip(left, y + 2, tag(values[0], index)) + 8
    y += 56

    # 2. After three pops the next two heads are equal.
    y = c.step(y + 18, 2, f"After popping {output[0]}, {output[1]}, {output[2]}: a tie")
    c.text(28, y + 22, "output", weight="700")
    c.cell_row(x0, y + 2, [tag(output[i], sources[i]) for i in range(popped)], cell=cell + 10,
               gap=gap, height=40, states=["good"] * popped)
    y += 52
    c.text(28, y + 22, "heap", weight="700")
    left = x0
    for value, source in heads:
        left += c.chip(left, y + 2, tag(value, source), color="gold" if value == heads[0][0] else "blue") + 8
    y += 44
    tied = [source for value, source in heads if value == heads[0][0]]
    c.text(28, y + 14, f"{heads[0][0]}·{names[tied[0]]} and {heads[0][0]}·{names[tied[1]]} compare equal: "
                       f"{names[tied[0]]} has the lower list index,", size=18)
    c.text(28, y + 40, f"so {names[tied[0]]} pops first.", size=18)

    # 3. The finished output keeps equal values in list order.
    y = c.step(y + 86, 3, "Finish: pop until every list is used up")
    ties = {i for i in range(len(output)) if output.count(output[i]) > 1}
    c.cell_row(28, y, [tag(output[i], sources[i]) for i in range(len(output))], cell=58, gap=6, height=40,
               states=["warn" if i in ties else "good" for i in range(len(output))])
    y += 60

    y = c.legend(y + 16, [("focus", "head of a list"), ("good", "output"),
                          ("warn", "equal values, list order")])
    c.note(y + 14, MERGE_IDEA)


FIGURES = [
    {
        "id": "linear-scan-first-match",
        "title": "Linear search: first match",
        "category": "Sorting and searching",
        "description": ("linearSearch for 4 in 9, 4, 4, 2 inspects two cells and returns index 1, never "
                        "reaching the second 4; a search for 7 inspects all four cells and returns -1."),
        "lesson": "algorithms/searching/linear-search.md",
        "keyIdea": SCAN_IDEA,
        "height": 750,
        "draw": linear_scan_first_match,
    },
    {
        "id": "predicate-boundary",
        "title": "Predicate boundary search",
        "category": "Sorting and searching",
        "description": ("For weights 3, 2, 2, 4, 1, 4 shipped in 3 days, capacities 4 and 5 are too slow and "
                        "6 to 16 fit. firstTrue(4, 17, fits) probes 10, 7, 5 and 6 and returns 6."),
        "lesson": "algorithms/searching/binary-search-on-answer.md",
        "keyIdea": CAPACITY_IDEA,
        "height": 884,
        "draw": predicate_boundary,
    },
    {
        "id": "quickselect-shrink",
        "title": "Quickselect keeps one part",
        "category": "Sorting and searching",
        "description": ("kthSmallest of 7, 2, 9, 4, 4, 1 with k = 2: three partitions with pivots 7, 2 and 4 "
                        "narrow the range from [0..5] to [0..3] to [2..3], and the equal part answers 4."),
        "lesson": "algorithms/searching/selection.md",
        "keyIdea": QUICK_IDEA,
        "height": 850,
        "draw": quickselect_shrink,
    },
    {
        "id": "kway-merge-heap",
        "title": "K-way merge with a heap",
        "category": "Sorting and searching",
        "description": ("mergeSorted of 1, 4, 7 and 2, 4 and 3, 6 keeps one head per list in a heap; when "
                        "4 from list A and 4 from list B tie, A pops first, giving 1, 2, 3, 4, 4, 6, 7."),
        "lesson": "algorithms/searching/selection.md",
        "keyIdea": MERGE_IDEA,
        "height": 890,
        "draw": kway_merge_heap,
    },
]
