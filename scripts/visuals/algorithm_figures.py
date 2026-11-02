"""Fixture-backed algorithm traces for the light and dark SVG lesson figures."""

from drawing import ints


KEY_IDEAS = {
    "binary-bound": "Keep the first possible index inside [low, high]. A value at least the target moves high left, so duplicates cannot hide the first match.",
    "merge-recursion": "Each parent merges two sorted children. A level costs O(n) work, and there are O(log n) levels: O(n log n) overall.",
    "quick-partition": "Only the smaller and greater regions recurse. Equal keys already belong together, so the equal region needs no further sorting.",
    "coin-dp": "best[a] is optimal for exactly amount a. Each candidate adds one coin to a solved, strictly smaller amount.",
    "knapsack-state": "Descending capacity keeps the current item out of its own predecessor states. Each item is used at most once.",
    "kmp-overlap": "After a match, retain the longest matched suffix that is also a pattern prefix. This preserves overlapping matches without rescanning text.",
    "queens-backtracking": "Place one queen per row. Reject used columns or diagonals; undo an unsuccessful choice before trying another.",
    "activity-selection": "The earliest finishing compatible activity leaves at least as much time for everything that follows. This maximizes the count, not total duration.",
    "bubble-swaps": "Each adjacent comparison moves the larger value right. After a pass, the unsorted range's maximum is fixed at its end; a swap-free pass stops.",
}


def _array_box(c, x, y, width, values, active=False):
    c.rect(x, y, width, 54, fill="soft" if active else "paper",
           stroke="blue" if active else "line")
    c.text(x + width / 2, y + 35,
           "[" + ", ".join(str(value) for value in values) + "]", anchor="middle")


def _cells(c, y, values, cell=70, gap=14, active=(), labels=None):
    width = len(values) * cell + max(0, len(values) - 1) * gap
    x = (c.width - width) / 2
    c.cells(x, y, values, cell=cell, gap=gap, active=active, labels=labels)
    return [x + index * (cell + gap) + cell / 2 for index in range(len(values))]


def _show_binary_bound(c, fixtures):
    values = ints(fixtures, "binary.values")
    target = int(fixtures["binary.target"])
    result = int(fixtures["binary.result"])
    steps = [tuple(int(part) for part in step.split(":"))
             for step in fixtures["binary.steps"].split(",")]
    c.text(28, 155, f"Find the first value at least {target}.")
    _cells(c, 180, values, active=(result,), labels=range(len(values)))
    c.text(28, 285, "Uninspected positions: [low, high).", color="muted")
    for number, (low, high, middle) in enumerate(steps, 1):
        y = 310 + (number - 1) * 108
        c.rect(28, y, 464, 88, fill="panel")
        c.text(44, y + 30, f"{number}  [{low}, {high}), mid = {middle}", weight="700")
        if values[middle] >= target:
            decision = f"a[{middle}] = {values[middle]} ≥ {target}  →  high = {middle}"
        else:
            decision = f"a[{middle}] = {values[middle]} < {target}  →  low = {middle + 1}"
        c.text(44, y + 62, decision)
        if number < len(steps):
            c.arrow(260, y + 94, 260, y + 103)
    c.text(28, 670, f"Stop at [{result}, {result}): return index {result}.", color="blue", weight="700")
    c.note(710, KEY_IDEAS["binary-bound"])


def _show_merge_recursion(c, fixtures):
    values = ints(fixtures, "merge.values")
    sorted_values = ints(fixtures, "merge.sorted")
    middle = len(values) // 2
    left, right = values[:middle], values[middle:]
    c.text(28, 155, "Split until every child has one value.")
    _array_box(c, 160, 180, 200, values)
    c.arrow(220, 241, 140, 273)
    c.arrow(300, 241, 380, 273)
    _array_box(c, 50, 280, 180, left)
    _array_box(c, 290, 280, 180, right)
    for parent, children in ((140, (80, 200)), (380, (320, 440))):
        for child in children:
            c.arrow(parent, 341, child, 373)
    for x, value in zip((50, 170, 290, 410), values):
        _array_box(c, x, 380, 60, [value])
    c.line(28, 463, 492, 463, width=1)
    c.text(28, 500, "Merge the sorted children bottom up.")
    _array_box(c, 50, 530, 180, sorted(left), active=True)
    _array_box(c, 290, 530, 180, sorted(right), active=True)
    c.arrow(140, 591, 220, 633)
    c.arrow(380, 591, 300, 633)
    _array_box(c, 160, 640, 200, sorted_values, active=True)
    c.text(28, 740, "Always take the smaller front value.")
    c.note(780, KEY_IDEAS["merge-recursion"])


def _partition_snapshots(values):
    """Trace the maintained three-way partition; keep the first smaller-key move."""
    current = list(values)
    pivot = current[len(current) // 2]
    less, scan, greater = 0, 0, len(current) - 1
    snapshots = [(list(current), less, scan, greater)]
    while scan <= greater:
        if current[scan] < pivot:
            current[less], current[scan] = current[scan], current[less]
            less += 1
            scan += 1
            if len(snapshots) == 1:
                snapshots.append((list(current), less, scan, greater))
        elif current[scan] > pivot:
            current[scan], current[greater] = current[greater], current[scan]
            greater -= 1
        else:
            scan += 1
    snapshots.append((list(current), less, scan, greater))
    return pivot, snapshots


def _partition_cells(c, y, snapshot, pivot):
    values, less, scan, greater = snapshot
    cell, gap = 70, 14
    x = (c.width - (len(values) * cell + (len(values) - 1) * gap)) / 2
    for index, value in enumerate(values):
        if index < less:
            fill, stroke, label = "note", "gold", f"<{pivot}"
        elif index < scan:
            fill, stroke, label = "soft", "blue", f"={pivot}"
        elif index <= greater:
            fill, stroke, label = "panel", "line", "?"
        else:
            fill, stroke, label = "paper", "line", f">{pivot}"
        left = x + index * (cell + gap)
        c.rect(left, y, cell, 48, fill=fill, stroke=stroke)
        c.text(left + cell / 2, y + 31, value, anchor="middle", weight="700")
        c.text(left + cell / 2, y + 75, label, anchor="middle", size=18,
               color="muted" if stroke == "line" else stroke)
    c.text(28, y + 116, f"less={less}   scan={scan}   greater={greater}")


def _show_quick_partition(c, fixtures):
    values = ints(fixtures, "quick.values")
    pivot, snapshots = _partition_snapshots(values)
    c.text(28, 155, f"Pivot = {pivot}, the middle value.")
    headings = ("1  Start: every value is unknown",
                "2  Move the first smaller value left",
                "3  Finish: the unknown region is empty")
    for number, (heading, snapshot) in enumerate(zip(headings, snapshots)):
        y = 200 + number * 210
        c.text(28, y, heading, weight="700")
        _partition_cells(c, y + 28, snapshot, pivot)
        if number < len(snapshots) - 1:
            c.arrow(260, y + 158, 260, y + 180)
    c.text(28, 805, "? means not scanned yet; labels name regions.", size=18, color="muted")
    greater_count = len(values) - snapshots[-1][3] - 1
    c.text(28, 840, f"The >{pivot} region has {greater_count} values in this input.")
    c.note(885, KEY_IDEAS["quick-partition"])


def _show_coin_dp(c, fixtures):
    coins = ints(fixtures, "coins.values")
    amount = int(fixtures["coins.amount"])
    table = ints(fixtures, "coins.table")
    c.text(28, 155, f"Coins: {', '.join(map(str, coins))}; target = {amount}")
    c.text(28, 200, "Minimum coins for each exact amount:")
    _cells(c, 225, table, cell=48, gap=14, active=(amount,), labels=range(len(table)))
    c.text(28, 330, "Bottom labels are amounts, not coin values.", size=18, color="muted")
    c.rect(28, 365, 464, 230, fill="panel")
    c.text(44, 403, f"To fill best[{amount}], try each coin:", weight="700")
    choices = [(table[amount - coin] + 1, coin) for coin in coins if coin <= amount]
    optimum = min(cost for cost, _ in choices)
    for index, (cost, coin) in enumerate(choices):
        y = 449 + index * 49
        if cost == optimum:
            c.rect(40, y - 29, 440, 40, fill="soft", stroke="blue")
        c.text(50, y, f"coin {coin}: best[{amount - coin}] + 1 = {cost}",
               color="blue" if cost == optimum else "ink")
    costs = ", ".join(str(cost) for cost, _ in choices)
    c.text(28, 644, f"best[{amount}] = min({costs}) = {table[amount]}", weight="700")
    witness = []
    remaining = amount
    while remaining:
        coin = next(coin for coin in coins
                    if coin <= remaining and table[remaining - coin] + 1 == table[remaining])
        witness.append(coin)
        remaining -= coin
    c.text(28, 683, "Optimal witness: " + " + ".join(map(str, witness)), color="blue")
    greedy, remaining = [], amount
    for coin in sorted(coins, reverse=True):
        while coin <= remaining:
            greedy.append(coin)
            remaining -= coin
    c.rect(28, 720, 464, 100, fill="paper", stroke="red", dashed=True)
    c.text(44, 753, "Greedy: " + " + ".join(map(str, greedy)) + f" uses {len(greedy)} coins.", color="red")
    c.text(44, 788, "Feasible, but not optimal.", color="red")
    c.note(860, KEY_IDEAS["coin-dp"])


def _show_knapsack_state(c, fixtures):
    weights = ints(fixtures, "knapsack.weights")
    values = ints(fixtures, "knapsack.values")
    capacity = int(fixtures["knapsack.capacity"])
    first = ints(fixtures, "knapsack.first")
    final = ints(fixtures, "knapsack.final")
    labels = range(capacity + 1)
    c.text(28, 155, "0/1 items: (weight, value)")
    c.text(28, 190, f"A=({weights[0]}, {values[0]})  B=({weights[1]}, {values[1]})  capacity={capacity}")
    c.text(28, 234, "Before any item; bottom labels are capacity.", size=18)
    centers = _cells(c, 258, [0] * (capacity + 1), labels=labels)
    c.text(28, 378, f"After A: sweep capacity {capacity} → {weights[0]}", weight="700")
    _cells(c, 402, first, active=range(weights[0], capacity + 1), labels=labels)
    c.arrow(centers[-1], 505, centers[weights[0]], 505)
    c.text(28, 545, f"Read best[c-{weights[0]}] from previous items.")
    c.text(28, 605, f"After B: sweep capacity {capacity} → {weights[1]}", weight="700")
    _cells(c, 629, final, active=range(weights[1], capacity + 1), labels=labels)
    c.arrow(centers[-1], 732, centers[weights[1]], 732)
    predecessor = capacity - weights[1]
    c.text(28, 774, f"best[{capacity}] = max({first[capacity]}, best[{predecessor}]+{values[1]}) = {final[capacity]}")
    c.rect(28, 815, 464, 108, fill="paper", stroke="red", dashed=True)
    c.text(44, 850, "Ascending A would reuse the same item:", color="red")
    invalid = first[capacity - weights[0]] + values[0]
    c.text(44, 887, f"best[{capacity}] = best[{capacity - weights[0]}] + {values[0]} = {invalid} : invalid", color="red")
    c.note(965, KEY_IDEAS["knapsack-state"])


def _prefix_lengths(pattern):
    prefix = [0] * len(pattern)
    for index in range(1, len(pattern)):
        length = prefix[index - 1]
        while length and pattern[index] != pattern[length]:
            length = prefix[length - 1]
        if pattern[index] == pattern[length]:
            length += 1
        prefix[index] = length
    return prefix


def _show_kmp_overlap(c, fixtures):
    text = fixtures["kmp.text"]
    pattern = fixtures["kmp.pattern"]
    matches = ints(fixtures, "kmp.matches")
    prefix = _prefix_lengths(pattern)
    cell, gap = 56, 14
    left = (c.width - (len(text) * cell + (len(text) - 1) * gap)) / 2
    c.text(28, 155, f"Pattern: {pattern}; prefix = {prefix}")
    c.text(28, 200, f"First match: starts at index {matches[0]}", weight="700")
    _cells(c, 225, list(text), cell=cell, gap=gap, labels=range(len(text)))
    c.cells(left + matches[0] * (cell + gap), 330, list(pattern), cell=cell, gap=gap,
            active=(len(pattern) - 1,))
    retained = prefix[-1]
    suffix = pattern[-retained:] if retained else ""
    c.text(28, 423, f"prefix[{len(pattern) - 1}] = {retained}: retain suffix '{suffix}'.", color="blue")
    next_index = matches[0] + len(pattern)
    c.text(28, 462, f"Next: text[{next_index}] versus pattern[{retained}].")
    c.arrow(260, 484, 260, 509)
    c.text(28, 550, f"Second match: starts at index {matches[1]}", weight="700")
    _cells(c, 575, list(text), cell=cell, gap=gap, labels=range(len(text)))
    c.cells(left + matches[1] * (cell + gap), 680, list(pattern), cell=cell, gap=gap,
            active=range(retained))
    c.text(28, 775, f"Result: {matches}; matches may overlap.", weight="700")
    c.note(820, KEY_IDEAS["kmp-overlap"])


def _queen_dead_ends(n):
    dead_ends = []

    def visit(columns):
        if len(columns) == n:
            return True
        row = len(columns)
        choices = [column for column in range(n)
                   if all(column != previous and abs(column - previous) != row - previous_row
                          for previous_row, previous in enumerate(columns))]
        if not choices:
            dead_ends.append(list(columns))
        for column in choices:
            if visit(columns + [column]):
                return True
        return False

    visit([])
    return dead_ends


def _show_queens_backtracking(c, fixtures):
    n = int(fixtures["queens.n"])
    columns = ints(fixtures, "queens.first")
    dead_ends = _queen_dead_ends(n)
    cell = 70
    left = (c.width - n * cell) / 2
    top = 205
    c.text(28, 155, f"First solution on a {n} × {n} board")
    for column in range(n):
        c.text(left + column * cell + cell / 2, top - 16, column, anchor="middle", size=18, color="muted")
    for row in range(n):
        c.text(left - 23, top + row * cell + cell / 2 + 7, row, anchor="middle", size=18, color="muted")
        for column in range(n):
            queen = columns[row] == column
            c.rect(left + column * cell, top + row * cell, cell, cell,
                   fill="soft" if queen else ("paper" if (row + column) % 2 == 0 else "panel"),
                   stroke="blue" if queen else "line", radius=0)
            if queen:
                c.text(left + column * cell + cell / 2, top + row * cell + cell / 2 + 9,
                       "Q", size=28, anchor="middle", weight="700", color="blue")
    c.text(28, 530, f"Columns by row: {columns}", weight="700")
    c.rect(28, 565, 464, 140, fill="paper", stroke="red", dashed=True)
    c.text(44, 601, f"Dead end after {dead_ends[0]}:", color="red", weight="700")
    used = sorted(dead_ends[0])
    blocked_row = len(dead_ends[0])
    diagonal = [column for column in range(n) if column not in used]
    c.text(44, 638, f"row {blocked_row}: columns {','.join(map(str, used))} are used;", color="red")
    c.text(44, 675, f"columns {','.join(map(str, diagonal))} are on diagonals.", color="red")
    c.text(28, 750, f"{dead_ends[1]} also reaches a dead end.")
    c.arrow(260, 774, 260, 796)
    c.rect(28, 820, 464, 135, fill="soft", stroke="blue")
    c.text(44, 854, f"Backtrack to row 0; try column {columns[0]}.")
    c.text(44, 892, f"{columns[:1]} → {columns[:2]} → {columns[:3]}", color="blue")
    c.text(44, 930, f"→ {columns}: first solution", color="blue", weight="700")
    c.text(28, 998, f"Q = queen; {fixtures['queens.count']} solutions in total.", size=18, color="muted")
    c.note(1035, KEY_IDEAS["queens-backtracking"])


def _intervals(fixtures, key):
    return [tuple(int(value) for value in interval.split(":"))
            for interval in fixtures[key].split(",")]


def _interval_label(interval):
    return f"[{interval[0]}, {interval[1]})"


def _show_activity_selection(c, fixtures):
    intervals = _intervals(fixtures, "greedy.intervals")
    expected = _intervals(fixtures, "greedy.selected")
    ordered = sorted(enumerate(intervals), key=lambda item: (item[1][1], item[1][0], item[0]))
    decisions, selected = [], []
    last_end = None
    for index, interval in ordered:
        take = last_end is None or interval[0] >= last_end
        decisions.append((chr(ord("A") + index), interval, take))
        if take:
            selected.append(interval)
            last_end = interval[1]
    if selected != expected:
        raise ValueError("greedy.selected does not match earliest-finish selection")
    tempting, last_end = [], None
    for interval in sorted(intervals):
        if last_end is None or interval[0] >= last_end:
            tempting.append(interval)
            last_end = interval[1]
    first_time = min(start for start, _ in intervals)
    final_time = max(end for _, end in intervals)
    left, right = 70, 460

    def time_x(time):
        return left + (time - first_time) * (right - left) / (final_time - first_time)

    c.text(28, 155, "Sort by finishing time first.")
    c.text(260, 194, "time (arbitrary units)", anchor="middle", color="muted")
    c.arrow(left, 220, right + 15, 220, color="muted")
    for time in range(first_time, final_time + 1):
        x = time_x(time)
        c.line(x, 214, x, 226, color="muted")
        c.text(x, 252, time, size=18, anchor="middle", color="muted")
        c.line(x, 270, x, 710, color="line", width=1, dashed=True)
    for number, (name, interval, take) in enumerate(decisions):
        y = 289 + number * 90
        c.rect(28, y - 25, 464, 34, fill="paper", stroke="paper", radius=0)
        color = "blue" if take else "red"
        status = "TAKE" if take else "SKIP: overlap"
        c.text(28, y, f"{name}  {_interval_label(interval)}  {status}", color=color, weight="700")
        c.rect(time_x(interval[0]), y + 23, time_x(interval[1]) - time_x(interval[0]), 28,
               fill="soft" if take else "paper", stroke=color, radius=0, dashed=not take)
    schedule = " → ".join(_interval_label(interval) for interval in selected)
    c.text(28, 760, schedule, color="blue", weight="700")
    c.text(28, 801, "Touching endpoints are compatible.")
    c.rect(28, 840, 464, 110, fill="paper", stroke="red", dashed=True)
    c.text(44, 878, "Tempting: " + " → ".join(_interval_label(interval) for interval in tempting), color="red")
    c.text(44, 918, f"Only {len(tempting)} activities; chosen schedule has {len(selected)}.", color="red")
    c.note(992, KEY_IDEAS["activity-selection"])


def _show_bubble_swaps(c, fixtures):
    values = ints(fixtures, "bubble.values")
    after_swap = ints(fixtures, "bubble.afterSwap")
    sorted_values = ints(fixtures, "bubble.sorted")
    first_swap = list(values)
    first_swap[0], first_swap[1] = first_swap[1], first_swap[0]
    if first_swap != after_swap or values[0] <= values[1]:
        raise ValueError("bubble.afterSwap must show the first adjacent swap")
    first_pass = list(values)
    for index in range(len(first_pass) - 1):
        if first_pass[index] > first_pass[index + 1]:
            first_pass[index], first_pass[index + 1] = first_pass[index + 1], first_pass[index]
    if first_pass != sorted_values:
        raise ValueError("the bubble fixture must become sorted after its first pass")
    cell, gap = 90, 24
    left = (c.width - (len(values) * cell + (len(values) - 1) * gap)) / 2

    def exchange_arrows(pair_start, y):
        start = left + pair_start * (cell + gap) + cell + 4
        end = start + gap - 8
        c.arrow(start, y + 16, end, y + 16)
        c.arrow(end, y + 35, start, y + 35)

    c.text(28, 155, "Pass 1: compare indices 0 → 1", weight="700")
    _cells(c, 180, values, cell=cell, gap=gap, active=(0, 1), labels=range(len(values)))
    exchange_arrows(0, 180)
    c.text(28, 291, f"{values[0]} > {values[1]}: swap only these neighbors.")
    c.arrow(260, 313, 260, 335)

    c.text(28, 365, "After swap: compare indices 1 → 2", weight="700")
    _cells(c, 390, after_swap, cell=cell, gap=gap, active=(1, 2), labels=range(len(after_swap)))
    exchange_arrows(1, 390)
    c.text(28, 501, f"{after_swap[1]} > {after_swap[2]}: another adjacent swap.")
    c.arrow(260, 523, 260, 545)

    c.text(28, 575, "Pass 2 compares indices 0 → 1", weight="700")
    for index, value in enumerate(sorted_values):
        x = left + index * (cell + gap)
        fixed = index == len(sorted_values) - 1
        c.rect(x, 600, cell, 48, fill="note" if fixed else "soft",
               stroke="gold" if fixed else "blue")
        c.text(x + cell / 2, 631, value, anchor="middle", weight="700")
        c.text(x + cell / 2, 674, f"{index} fixed" if fixed else index,
               anchor="middle", size=18, color="gold" if fixed else "muted")
    c.text(28, 720, f"Maximum {max(values)} is fixed after pass 1.")
    c.text(28, 760, f"{sorted_values[0]} ≤ {sorted_values[1]}: no swap; pass 2 changes nothing.")
    c.text(28, 800, f"Stop early. Result: {sorted_values}.", color="blue", weight="700")
    c.note(840, KEY_IDEAS["bubble-swaps"])


FIGURES = [
    {
        "id": "binary-bound",
        "title": "Binary search: lower bound",
        "category": "Algorithm traces",
        "description": "Half-open ranges find the first target among duplicate values.",
        "lesson": "algorithms/searching/binary-search.md",
        "keyIdea": KEY_IDEAS["binary-bound"],
        "height": 940,
        "draw": _show_binary_bound,
    },
    {
        "id": "merge-recursion",
        "title": "Merge sort: split and combine",
        "category": "Algorithm traces",
        "description": "Split into singletons, then merge sorted children bottom up.",
        "lesson": "algorithms/sorting/merge-sort.md",
        "keyIdea": KEY_IDEAS["merge-recursion"],
        "height": 995,
        "draw": _show_merge_recursion,
    },
    {
        "id": "quick-partition",
        "title": "Quicksort: three-way partition",
        "category": "Algorithm traces",
        "description": "Gather duplicate pivot values into one finished equal region.",
        "lesson": "algorithms/sorting/quick-sort.md",
        "keyIdea": KEY_IDEAS["quick-partition"],
        "height": 1100,
        "draw": _show_quick_partition,
    },
    {
        "id": "coin-dp",
        "title": "Coin change: build an optimum",
        "category": "Algorithm traces",
        "description": "Compare every final coin; the largest-first greedy choice loses.",
        "lesson": "algorithms/dynamic-programming/problems.md",
        "keyIdea": KEY_IDEAS["coin-dp"],
        "height": 1065,
        "draw": _show_coin_dp,
    },
    {
        "id": "knapsack-state",
        "title": "0/1 knapsack: sweep backward",
        "category": "Algorithm traces",
        "description": "Sweep capacities backward so an item cannot reuse itself.",
        "lesson": "algorithms/dynamic-programming/problems.md",
        "keyIdea": KEY_IDEAS["knapsack-state"],
        "height": 1170,
        "draw": _show_knapsack_state,
    },
    {
        "id": "kmp-overlap",
        "title": "KMP: keep the overlap",
        "category": "Algorithm traces",
        "description": "Prefix fallback retains the overlap between two pattern matches.",
        "lesson": "algorithms/searching/linear-search.md",
        "keyIdea": KEY_IDEAS["kmp-overlap"],
        "height": 1040,
        "draw": _show_kmp_overlap,
    },
    {
        "id": "queens-backtracking",
        "title": "N-queens: reject and undo",
        "category": "Algorithm traces",
        "description": "Trace failed placements before reaching the first valid board.",
        "lesson": "algorithms/backtracking/problems.md",
        "keyIdea": KEY_IDEAS["queens-backtracking"],
        "height": 1240,
        "draw": _show_queens_backtracking,
    },
    {
        "id": "activity-selection",
        "title": "Greedy: finish first",
        "category": "Algorithm traces",
        "description": "Choose earliest finishing intervals; touching endpoints remain compatible.",
        "lesson": "algorithms/greedy/overview.md",
        "keyIdea": KEY_IDEAS["activity-selection"],
        "height": 1220,
        "draw": _show_activity_selection,
    },
    {
        "id": "bubble-swaps",
        "title": "Bubble sort: adjacent swaps",
        "category": "Algorithm traces",
        "description": "Swap neighboring values; each pass settles a maximum on the right.",
        "lesson": "algorithms/sorting/bubble-sort.md",
        "keyIdea": KEY_IDEAS["bubble-swaps"],
        "height": 1060,
        "draw": _show_bubble_swaps,
    },
]
