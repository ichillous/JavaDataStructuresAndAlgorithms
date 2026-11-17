"""Dynamic-programming, greedy and backtracking figures; fixtures in examples-optimization.properties.

The library keeps its tables private, so every displayed table is recomputed here from the
fixture inputs by the recurrence the Javadoc documents. ``expect`` stops the build when a
recomputed cell disagrees with the check cells recorded in the fixture file; the Java test
(VisualExamplesOptimizationTest) re-derives the same cells independently and compares the
library's real results (string, script, cost, tour, schedule, lengths, indices) with the
fixture, so the pictures cannot drift from the code.
"""

import heapq

from drawing import ints

CATEGORY = "Optimization"
P = "optimization."


# ---------------------------------------------------------------- helpers

def pairs(fixtures, key):
    """Parse ``a:b=value`` style entries into a dict ``{"a:b": value}``."""
    result = {}
    for entry in fixtures[key].split(","):
        name, value = entry.split("=")
        result[name] = int(value)
    return result


def expect(computed, fixtures, key):
    """Stop the build when a recomputed value differs from the fixture's check value."""
    for name, value in pairs(fixtures, key).items():
        if computed[name] != value:
            raise ValueError(f"{key} {name}: figure computes {computed[name]}, fixture says {value}")


def lcs_walk(a, b):
    """The LCS table and the documented traceback: take equal units, else up when >= left."""
    table = [[0] * (len(b) + 1) for _ in range(len(a) + 1)]
    for i in range(1, len(a) + 1):
        for j in range(1, len(b) + 1):
            table[i][j] = (table[i - 1][j - 1] + 1 if a[i - 1] == b[j - 1]
                           else max(table[i - 1][j], table[i][j - 1]))
    i, j, steps, taken = len(a), len(b), [], []
    while i > 0 and j > 0:
        if a[i - 1] == b[j - 1]:
            steps.append((i, j, "take")); taken.append(a[i - 1]); i -= 1; j -= 1
        elif table[i - 1][j] >= table[i][j - 1]:
            steps.append((i, j, "up")); i -= 1
        else:
            steps.append((i, j, "left")); j -= 1
    return table, steps, "".join(reversed(taken)), (i, j)


def edit_walk(source, target):
    """The edit table and the documented traceback: diagonal if optimal, then delete, then insert."""
    table = [[0] * (len(target) + 1) for _ in range(len(source) + 1)]
    for i in range(len(source) + 1):
        table[i][0] = i
    for j in range(len(target) + 1):
        table[0][j] = j
    for i in range(1, len(source) + 1):
        for j in range(1, len(target) + 1):
            diagonal = table[i - 1][j - 1] + (source[i - 1] != target[j - 1])
            table[i][j] = min(diagonal, min(table[i - 1][j], table[i][j - 1]) + 1)
    i, j, steps = len(source), len(target), []
    while i > 0 or j > 0:
        if i > 0 and j > 0:
            equal = source[i - 1] == target[j - 1]
            if table[i][j] == table[i - 1][j - 1] + (0 if equal else 1):
                steps.append(("MATCH" if equal else "SUBSTITUTE", i - 1, j - 1)); i -= 1; j -= 1
                continue
        if i > 0 and table[i][j] == table[i - 1][j] + 1:
            steps.append(("DELETE", i - 1, j)); i -= 1
        else:
            steps.append(("INSERT", i, j - 1)); j -= 1
    return table, steps[::-1]


# ---------------------------------------------------------------- lcs-traceback

LCS_IDEA = ("The traceback re-derives each choice from the table: take equal letters, otherwise "
            "move toward the larger neighbour, and go up on a tie.")


def lcs_traceback(c, fixtures):
    """The LCS table of GCTA and TGCAT with the path longestCommonSubsequence walks back."""
    a, b = fixtures[P + "lcs.a"], fixtures[P + "lcs.b"]
    table, steps, result, end = lcs_walk(a, b)
    expect({f"{i}:{j}": table[i][j] for i in range(len(a) + 1) for j in range(len(b) + 1)},
           fixtures, P + "lcs.cells")
    if result != fixtures[P + "lcs.result"] or table[-1][-1] != int(fixtures[P + "lcs.length"]):
        raise ValueError("lcs traceback disagrees with the fixture")
    if [move for _, _, move in steps] != fixtures[P + "lcs.path"].split(","):
        raise ValueError("lcs path disagrees with the fixture")

    y = c.step(156, 1, "Fill the table; ε is the empty prefix")
    gx, gy, cw, ch = 118, y + 34, 52, 36
    states = {(i, j): ("good" if move == "take" else "warn") for i, j, move in steps}
    states[end] = "focus"
    c.grid(gx, gy, table, cell_w=cw, cell_h=ch, row_labels=["ε"] + list(a), col_labels=["ε"] + list(b),
           states=states)
    y = gy + ch * len(table)

    y = c.step(y + 44, 2, f"Walk back from the corner: {result}")
    for i, j, move in steps:
        if move == "take":
            line = f"{a[i - 1]} = {b[j - 1]}: take {a[i - 1]}, go diagonally"
        else:
            up, left = table[i - 1][j], table[i][j - 1]
            word = "≥" if up >= left else "<"
            line = f"{a[i - 1]} ≠ {b[j - 1]}: up {up} {word} left {left}, go {move}"
        c.text(66, y + 6, line, size=18)
        y += 28
    y = c.legend(y + 16, [("good", "letter taken"), ("warn", "differ: move up or left"),
                          ("focus", "stop at row 0")])
    c.note(y + 14, LCS_IDEA)


# ---------------------------------------------------------------- edit-distance-grid

EDIT_IDEA = ("Walking back from the corner prefers the diagonal, then delete, then insert; "
             "each step that is not a match adds 1.")
EDIT_STYLE = {"MATCH": ("good", "keep"), "SUBSTITUTE": ("warn", "replace"),
              "INSERT": ("focus", "insert"), "DELETE": ("bad", "delete")}


def edit_distance_grid(c, fixtures):
    """The edit table of brain and paint with the script editScript returns, step by step."""
    source, target = fixtures[P + "edit.source"], fixtures[P + "edit.target"]
    table, steps = edit_walk(source, target)
    expect({f"{i}:{j}": table[i][j] for i in range(len(source) + 1) for j in range(len(target) + 1)},
           fixtures, P + "edit.cells")
    shown = [f"{op}:{i}:{j}" for op, i, j in steps]
    if shown != fixtures[P + "edit.steps"].split(",") or table[-1][-1] != int(fixtures[P + "edit.distance"]):
        raise ValueError("edit script disagrees with the fixture")

    y = c.step(156, 1, "The path through the table")
    gx, gy, cw, ch = 118, y + 34, 52, 36
    row, column, states = 0, 0, {}
    for op, _, _ in steps:
        row += 0 if op == "INSERT" else 1
        column += 0 if op == "DELETE" else 1
        states[(row, column)] = EDIT_STYLE[op][0]
    c.grid(gx, gy, table, cell_w=cw, cell_h=ch, row_labels=["ε"] + list(source),
           col_labels=["ε"] + list(target), states=states)
    y = gy + ch * len(table)

    y = c.step(y + 44, 2, f"The script, in order ({table[-1][-1]} changes)")
    for index, (op, i, j) in enumerate(steps):
        color, verb = EDIT_STYLE[op]
        detail = {"MATCH": lambda: source[i], "DELETE": lambda: source[i], "INSERT": lambda: target[j],
                  "SUBSTITUTE": lambda: f"{source[i]} with {target[j]}"}[op]()
        left = 28 + (index // 3) * 238
        c.chip(left, y + (index % 3) * 38, f"{index + 1} {verb} {detail}", color=color)
    y += 3 * 38 + 4

    y = c.legend(y + 24, [("good", "match"), ("warn", "substitute"), ("focus", "insert"),
                          ("bad", "delete")])
    c.note(y + 14, EDIT_IDEA)


# ---------------------------------------------------------------- matrix-chain-splits

CHAIN_IDEA = ("Each cell keeps the cheapest split k; following the stored splits from the "
              "whole chain down gives the parenthesization.")


def chain_tables(dimensions):
    n = len(dimensions) - 1
    cost, split = {}, {}
    for i in range(1, n + 1):
        cost[(i, i)] = 0
    for length in range(2, n + 1):
        for i in range(1, n - length + 2):
            j = i + length - 1
            best = None
            for k in range(i, j):
                candidate = cost[(i, k)] + cost[(k + 1, j)] + dimensions[i - 1] * dimensions[k] * dimensions[j]
                if best is None or candidate < best:  # strict: ties keep the smallest k
                    best, split[(i, j)] = candidate, k
            cost[(i, j)] = best
    return cost, split


def matrix_chain_splits(c, fixtures):
    """Cost and split tables for four matrices, the candidates for the whole chain, the result."""
    d = ints(fixtures, P + "chain.dimensions")
    n = len(d) - 1
    cost, split = chain_tables(d)
    expect({f"{i}:{j}": v for (i, j), v in cost.items() if i != j}, fixtures, P + "chain.costs")
    expect({f"{i}:{j}": v for (i, j), v in split.items()}, fixtures, P + "chain.splits")
    candidates = {k: cost[(1, k)] + cost[(k + 1, n)] + d[0] * d[k] * d[n] for k in range(1, n)}
    expect({str(k): v for k, v in candidates.items()}, fixtures, P + "chain.candidates")

    def bracket(i, j):
        return f"A{i}" if i == j else "(" + bracket(i, split[(i, j)]) + bracket(split[(i, j)] + 1, j) + ")"

    text = bracket(1, n)
    if text != fixtures[P + "chain.parenthesization"] or cost[(1, n)] != int(fixtures[P + "chain.cost"]):
        raise ValueError("matrix chain result disagrees with the fixture")
    used, stack = [], [(1, n)]
    while stack:
        i, j = stack.pop(0)
        if i != j:
            used.append((i, j))
            stack += [(i, split[(i, j)]), (split[(i, j)] + 1, j)]

    shape = f"A{1} is {d[0]}x{d[1]}" + "".join(f", A{k} is {d[k - 1]}x{d[k]}" for k in range(2, n + 1))
    y = c.step(156, 1, "cost(i, j) of the chain Ai..Aj")
    rows = [[cost[(i, j)] if j >= i else "" for j in range(1, n + 1)] for i in range(1, n + 1)]
    states = {(i - 1, j - 1): ("focus" if (i, j) == (1, n) else "good") for i, j in used}
    gx, gy = 118, y + 34
    labels = [f"A{k}" for k in range(1, n + 1)]
    c.grid(gx, gy, rows, cell_w=76, cell_h=34, row_labels=labels, col_labels=labels, states=states)
    y = gy + 34 * n

    y = c.step(y + 40, 2, f"Try each k; d = {', '.join(map(str, d))}")
    for k in range(1, n):
        left, right = cost[(1, k)], cost[(k + 1, n)]
        product = d[0] * d[k] * d[n]
        mark = "  (smallest)" if candidates[k] == min(candidates.values()) else ""
        c.text(66, y + 6, f"k={k}: {left} + {right} + {d[0]}*{d[k]}*{d[n]} = {candidates[k]}{mark}",
               size=18, weight="700" if mark else "400", color="green" if mark else "ink")
        y += 28

    y = c.step(y + 34, 3, "Follow the splits")
    c.chip(300, y - 36, text, color="green")
    for i, j in used:
        k = split[(i, j)]
        left = f"A{i}" if i == k else f"A{i}..A{k}"
        right = f"A{j}" if k + 1 == j else f"A{k + 1}..A{j}"
        c.text(66, y + 6, f"split[{i}][{j}] = {k}: {left} | {right}", size=18)
        y += 28
    y = c.legend(y + 20, [("focus", "whole chain"), ("good", "split the answer reuses")])
    c.note(y + 14, CHAIN_IDEA)


# ---------------------------------------------------------------- held-karp-masks

TSP_IDEA = ("Held-Karp keeps one best cost per (visited set, last city), so 2^n n^2 steps "
            "replace the (n-1)! tours.")


def held_karp_masks(c, fixtures):
    """Held-Karp over four cities: the (visited set, last city) table, the closing edge, the tour."""
    d = [[int(v) for v in row.split(",")] for row in fixtures[P + "tsp.distances"].split(";")]
    others = len(d) - 1
    best, parent = {}, {}
    for size in range(1, others + 1):
        for subset in range(1, 1 << others):
            if bin(subset).count("1") != size:
                continue
            for last in range(others):
                if not subset >> last & 1:
                    continue
                rest = subset & ~(1 << last)
                if rest == 0:
                    best[(subset, last)], parent[(subset, last)] = d[0][last + 1], None
                    continue
                candidates = [(best[(rest, previous)] + d[previous + 1][last + 1], previous)
                              for previous in range(others) if rest >> previous & 1]
                best[(subset, last)], parent[(subset, last)] = min(candidates)

    def name(subset):
        return "".join(str(city + 1) for city in range(others) if subset >> city & 1)

    expect({f"{name(s)}@{last + 1}": v for (s, last), v in best.items()}, fixtures, P + "tsp.states")
    full = (1 << others) - 1
    closing = [best[(full, last)] + d[last + 1][0] for last in range(others)]
    if closing != ints(fixtures, P + "tsp.closing"):
        raise ValueError("closing costs disagree with the fixture")
    end = closing.index(min(closing))  # equal costs: the smallest last city
    tour, subset, last = [], full, end
    while last is not None:
        tour.append(last + 1)
        subset, last = subset & ~(1 << last), parent[(subset, last)]
    tour = [0] + tour[::-1]
    if tour != ints(fixtures, P + "tsp.tour") or min(closing) != int(fixtures[P + "tsp.cost"]):
        raise ValueError("tour disagrees with the fixture")

    on_tour, subset = set(), 0
    for city in tour[1:]:
        subset |= 1 << (city - 1)
        on_tour.add((subset, city - 1))

    y = c.step(156, 1, "best(S, j): cheapest path 0 to j through S")
    sets = sorted(range(1, 1 << others), key=lambda s: (bin(s).count("1"), s))
    rows = [[best[(s, j)] if s >> j & 1 else "" for j in range(others)] for s in sets] + [closing]
    states = {}
    for r, s in enumerate(sets):
        for j in range(others):
            if (s, j) in on_tour:
                states[(r, j)] = "good"
            elif not s >> j & 1:
                states[(r, j)] = "muted"
    states[(len(sets), end)] = "good"
    gx, gy, ch = 150, y + 34, 32
    c.grid(gx, gy, rows, cell_w=82, cell_h=ch, row_labels=[name(s) for s in sets] + ["close"],
           col_labels=[f"end {j + 1}" for j in range(others)], states=states)
    y = gy + ch * len(rows)
    c.text(28, y + 26, "close = full set + the edge back to city 0", size=18, color="muted")
    c.text(28, y + 52, f"Ends 1 and 2 tie at {min(closing)}; the smaller city wins.", size=18, color="muted")
    y += 52

    y = c.step(y + 40, 2, f"Follow the predecessors back: cost {min(closing)}")
    xs = [60 + 100 * index for index in range(len(tour) + 1)]
    path = tour + [0]
    for index, city in enumerate(path):
        c.node(xs[index], y + 30, str(city), radius=22, state="focus" if city == 0 else "good")
    for index in range(len(path) - 1):
        c.edge((xs[index], y + 30), (xs[index + 1], y + 30), radius=22, color="green",
               label=str(d[path[index]][path[index + 1]]))
    y += 74

    y = c.legend(y + 26, [("good", "state on the tour"), ("muted", "city not in set")])
    c.note(y + 14, TSP_IDEA)


# ---------------------------------------------------------------- weighted-interval-choice

INTERVAL_IDEA = ("For each interval by end time, compare skipping it with taking it plus the "
                 "best schedule that ends before it starts.")


def weighted_interval_choice(c, fixtures):
    """Take-or-skip for four intervals sorted by end, with p(i), then the walk back."""
    spans = [tuple(int(v) for v in item.split(":")) for item in fixtures[P + "interval.intervals"].split(",")]
    names = fixtures[P + "interval.names"].split(",")
    count = len(spans)
    best, compatible, take, skip = [0], [], [], []
    for index, (start, end, weight) in enumerate(spans, 1):
        p = sum(1 for other in spans[:index - 1] if other[1] <= start)
        compatible.append(p)
        take.append(weight + best[p])
        skip.append(best[index - 1])
        best.append(max(take[-1], skip[-1]))
    if (compatible != ints(fixtures, P + "interval.compatible") or take != ints(fixtures, P + "interval.take")
            or skip != ints(fixtures, P + "interval.skip") or best[1:] != ints(fixtures, P + "interval.best")):
        raise ValueError("interval table disagrees with the fixture")
    chosen, index = [], count
    while index > 0:
        if best[index] == best[index - 1]:
            index -= 1
        else:
            chosen.append(index - 1)
            index = compatible[index - 1]
    chosen.reverse()
    if chosen != ints(fixtures, P + "interval.chosen") or best[count] != int(fixtures[P + "interval.total"]):
        raise ValueError("interval choice disagrees with the fixture")

    y = c.step(156, 1, "Intervals sorted by end, with p(i)")
    x0, unit, bar = 62, 44, 26
    for index, (start, end, weight) in enumerate(spans):
        top = y + 2 + index * 32
        c.text(28, top + 20, names[index], size=20, weight="700")
        c.rect(x0 + unit * start, top, unit * (end - start), bar, fill="soft", stroke="blue", radius=5)
        c.text(x0 + unit * start + unit * (end - start) / 2, top + 20, f"w={weight}", size=18, color="blue",
               anchor="middle", weight="700")
        c.text(446, top + 20, f"p={compatible[index]}", size=18, color="muted", weight="700")
    axis = y + 2 + count * 32 + 2
    for tick in range(0, max(end for _, end, _ in spans) + 1):
        c.text(x0 + unit * tick, axis + 18, str(tick), size=18, color="muted", anchor="middle")
    y = axis + 22

    y = c.step(y + 22, 2, "take = w + best[p] or skip = best[i-1]")
    gx, gy, ch = 118, y + 34, 34
    rows = [[compatible[i], take[i], skip[i], best[i + 1]] for i in range(count)]
    states = {}
    for i in range(count):
        taken_wins = take[i] > skip[i]
        states[(i, 1)] = "good" if taken_wins else "muted"
        states[(i, 2)] = "muted" if taken_wins else "good"
    c.grid(gx, gy, rows, cell_w=64, cell_h=ch, row_labels=names, col_labels=["p", "take", "skip", "best"],
           states=states)
    y = gy + ch * count

    y = c.step(y + 38, 3, "Walk back")
    c.chip(300, y - 36, f"chosen {' + '.join(names[i] for i in chosen)} = {best[count]}", color="green")
    index = count
    while index > 0:
        if best[index] == best[index - 1]:
            c.text(66, y + 6, f"{names[index - 1]}: best {best[index]} = {best[index - 1]}, skip", size=18)
            index -= 1
        else:
            c.text(66, y + 6, f"{names[index - 1]}: best {best[index]} ≠ {best[index - 1]}, take, "
                              f"jump to p = {compatible[index - 1]}", size=18, weight="700", color="green")
            index = compatible[index - 1]
        y += 28

    y = c.legend(y + 16, [("good", "winning option"), ("muted", "losing option")])
    c.note(y + 14, INTERVAL_IDEA)


# ---------------------------------------------------------------- huffman-tree

HUFFMAN_IDEA = ("Merging the two lightest nodes first leaves the rarest symbols deepest, so "
                "frequent symbols get the shortest codes.")


def huffman_tree(c, fixtures):
    """huffmanCodeLengths: merge the two lightest nodes until one remains; depth is the length."""
    symbols = fixtures[P + "huffman.symbols"].split(",")
    weights = ints(fixtures, P + "huffman.frequencies")
    heap = [(w, i) for i, w in enumerate(weights)]
    heapq.heapify(heap)
    nodes = {i: (w, None) for i, w in enumerate(weights)}  # id -> (weight, children)
    merges, queues, nxt = [], [], len(weights)
    while len(heap) > 1:
        (wa, ia), (wb, ib) = heapq.heappop(heap), heapq.heappop(heap)
        nodes[nxt] = (wa + wb, (ia, ib))
        merges.append(f"{wa}+{wb}={wa + wb}")
        heapq.heappush(heap, (wa + wb, nxt))
        queues.append(sorted(w for w, _ in heap))
        nxt += 1
    root = nxt - 1
    depth = {root: 0}
    for node in range(root, -1, -1):
        if nodes[node][1]:
            for child in nodes[node][1]:
                depth[child] = depth[node] + 1
    lengths = [depth[i] for i in range(len(weights))]
    cost = sum(w * l for w, l in zip(weights, lengths))
    if (merges != fixtures[P + "huffman.merges"].split(",") or lengths != ints(fixtures, P + "huffman.lengths")
            or cost != int(fixtures[P + "huffman.cost"])):
        raise ValueError("huffman figure disagrees with the fixture")

    y = c.step(156, 1, "Merge the two lightest nodes")
    c.text(66, y + 6, "queue: " + " ".join(map(str, sorted(weights))), size=18, color="muted")
    y += 28
    for merge, queue in zip(merges, queues):
        c.text(66, y + 6, f"{merge.replace('+', ' + ').replace('=', ' = ')}", size=18, weight="700")
        c.text(210, y + 6, "queue: " + " ".join(map(str, queue)), size=18, color="muted")
        y += 28
    total = " + ".join(str(int(m.split("=")[1])) for m in merges)
    c.text(66, y + 6, f"merge sums {total} = {cost} bits", size=18, color="green", weight="700")
    y += 28

    y = c.step(y + 26, 2, "Depth in the tree is the code length")
    order = []

    def walk(node):
        if nodes[node][1] is None:
            order.append(node)
        else:
            for child in nodes[node][1]:
                walk(child)

    walk(root)
    x = {leaf: 70 + 96 * index for index, leaf in enumerate(order)}
    for node in range(len(weights), root + 1):
        left, right = nodes[node][1]
        x[node] = (x[left] + x[right]) / 2
    top, gap, radius = y + 32, 62, 25
    where = {node: (x[node], top + depth[node] * gap) for node in nodes}
    for node in range(len(weights), root + 1):
        for child in nodes[node][1]:
            c.edge(where[node], where[child], radius=radius, directed=False, color="muted")
    for node, (nx, ny) in where.items():
        if nodes[node][1] is None:
            c.node(nx, ny, f"{symbols[node]} {weights[node]}", radius=radius, state="good")
            c.text(nx, ny + radius + 22, f"len {lengths[node]}", size=18, anchor="middle", weight="700",
                   color="green")
        else:
            c.node(nx, ny, str(nodes[node][0]), radius=radius, state="focus")
    y = top + max(depth.values()) * gap + radius + 30

    y = c.legend(y + 20, [("good", "symbol and frequency"), ("focus", "merged node (sum)")])
    c.note(y + 14, HUFFMAN_IDEA)


# ---------------------------------------------------------------- subset-sum-halves

SUBSET_IDEA = ("Each half has only 2^(n/2) subsets, so storing one half's sums and scanning "
               "the other's finds a pair in about 2^(n/2) steps, not 2^n.")


def subset_sum_halves(c, fixtures):
    """subsetSum splits six values into halves, stores left sums and scans right sums."""
    values = ints(fixtures, P + "subset.values")
    target = int(fixtures[P + "subset.target"])
    middle = len(values) // 2
    halves = (values[:middle], values[middle:])

    def sums(half):
        return [sum(half[bit] for bit in range(len(half)) if mask >> bit & 1) for mask in range(1 << len(half))]

    def label(half, mask):
        picked = [str(half[bit]) for bit in range(len(half)) if mask >> bit & 1]
        return ",".join(picked) if picked else "none"

    left, right = sums(halves[0]), sums(halves[1])
    if left != ints(fixtures, P + "subset.leftSums") or right != ints(fixtures, P + "subset.rightSums"):
        raise ValueError("half sums disagree with the fixture")
    stored = {}
    for mask, total in enumerate(left):
        stored.setdefault(total, mask)  # the smallest left mask keeps each sum
    scanned = []
    for mask, total in enumerate(right):
        scanned.append(mask)
        if target - total in stored:
            match = (stored[target - total], mask)
            break
    if (match != (int(fixtures[P + "subset.leftMask"]), int(fixtures[P + "subset.rightMask"]))
            or left[0] != int(fixtures[P + "subset.duplicateSum"])):
        raise ValueError("subset match disagrees with the fixture")
    witness = [bit for bit in range(len(values)) if (match[0] | (match[1] << middle)) >> bit & 1]
    if witness != ints(fixtures, P + "subset.witness"):
        raise ValueError("subset witness disagrees with the fixture")

    y = c.step(156, 1, f"Every subset sum of each half, target {target}")
    gy, ch, cw = y + 52, 30, 80
    for half, sums_, gx, title in ((halves[0], left, 50, "left half"), (halves[1], right, 290, "right half")):
        c.text(gx, y + 18, f"{title} {half}".replace("[", "[").replace(" ", " "), size=18, weight="700")
        rows = [[label(half, mask), sums_[mask]] for mask in range(len(sums_))]
        states = {}
        if gx == 50:
            for mask, total in enumerate(sums_):
                if stored[total] != mask:
                    states[(mask, 0)] = states[(mask, 1)] = "muted"
            states[(match[0], 0)] = states[(match[0], 1)] = "good"
        else:
            for mask in scanned:
                states[(mask, 0)] = states[(mask, 1)] = "good" if mask == match[1] else "warn"
        c.grid(gx, gy, rows, cell_w=cw, cell_h=ch, row_labels=[str(m) for m in range(len(rows))],
               col_labels=["subset", "sum"], states=states)
    y = gy + ch * len(left)

    y = c.step(y + 40, 2, "Scan right sums in mask order")
    for mask in scanned:
        need = target - right[mask]
        if mask == match[1]:
            line = f"mask {mask}: sum {right[mask]}, need {need}: left mask {match[0]} has it"
            c.text(66, y + 6, line, size=18, weight="700", color="green")
        else:
            c.text(66, y + 6, f"mask {mask}: sum {right[mask]}, need {need}: not stored", size=18)
        y += 28
    picked = " + ".join(str(values[i]) for i in witness)
    c.text(66, y + 6, f"indices {', '.join(map(str, witness))}: {picked} = {target}", size=18, weight="700",
           color="green")
    y += 40

    y = c.legend(y + 16, [("good", "matching pair"), ("warn", "no match"),
                          ("muted", "duplicate sum, not stored")])
    c.note(y + 14, SUBSET_IDEA)


FIGURES = [
    {
        "id": "lcs-traceback", "title": "LCS table and traceback", "category": CATEGORY,
        "description": ("The longest-common-subsequence table for GCTA and TGCAT, with the path "
                        "longestCommonSubsequence walks back from the corner: it goes up on the tie at "
                        "the corner, takes T, C and G on equal letters, and returns GCT."),
        "keyIdea": LCS_IDEA, "lesson": "algorithms/dynamic-programming/problems.md",
        "height": 860, "draw": lcs_traceback,
    },
    {
        "id": "edit-distance-grid", "title": "Edit distance path", "category": CATEGORY,
        "description": ("The edit-distance table for brain and paint with the path editScript returns: "
                        "delete b, replace r with p, keep a, i and n, then insert t, for distance 3."),
        "keyIdea": EDIT_IDEA, "lesson": "algorithms/dynamic-programming/problems.md",
        "height": 820, "draw": edit_distance_grid,
    },
    {
        "id": "matrix-chain-splits", "title": "Matrix chain splits", "category": CATEGORY,
        "description": ("The cost table for four matrices with dimensions 30, 1, 40, 10, 25, the three "
                        "candidate splits of the whole chain, and the stored splits that give "
                        "(A1((A2A3)A4)) at cost 1400."),
        "keyIdea": CHAIN_IDEA, "lesson": "algorithms/dynamic-programming/classic-problems.md",
        "height": 870, "draw": matrix_chain_splits,
    },
    {
        "id": "held-karp-masks", "title": "Held-Karp states", "category": CATEGORY,
        "description": ("The Held-Karp table for four cities, one cost per visited set and last city, "
                        "the closing edge back to city 0, and the optimal tour 0, 2, 3, 1, 0 of cost 80."),
        "keyIdea": TSP_IDEA, "lesson": "algorithms/dynamic-programming/classic-problems.md",
        "height": 870, "draw": held_karp_masks,
    },
    {
        "id": "weighted-interval-choice", "title": "Weighted interval choices", "category": CATEGORY,
        "description": ("Four weighted intervals sorted by end with their last compatible interval, the "
                        "take-or-skip comparison for each, and the walk back that chooses B and D for "
                        "total weight 10."),
        "keyIdea": INTERVAL_IDEA, "lesson": "algorithms/dynamic-programming/classic-problems.md",
        "height": 890, "draw": weighted_interval_choice,
    },
    {
        "id": "huffman-tree", "title": "Huffman merges and lengths", "category": CATEGORY,
        "description": ("Five symbols with frequencies 5, 9, 12, 13 and 16 are merged two lightest at a "
                        "time into a tree, which gives code lengths 3, 3, 2, 2 and 2 and a weighted "
                        "length of 124."),
        "keyIdea": HUFFMAN_IDEA, "lesson": "algorithms/greedy/huffman-coding.md",
        "height": 900, "draw": huffman_tree,
    },
    {
        "id": "subset-sum-halves", "title": "Subset sum, two halves", "category": CATEGORY,
        "description": ("Six values are split into two halves of three; the sums of all eight subsets of "
                        "each half are listed, and the first right sum whose complement is stored on the "
                        "left selects indices 0, 1 and 3 for target 6."),
        "keyIdea": SUBSET_IDEA, "lesson": "algorithms/backtracking/problems.md",
        "height": 890, "draw": subset_sum_halves,
    },
]
