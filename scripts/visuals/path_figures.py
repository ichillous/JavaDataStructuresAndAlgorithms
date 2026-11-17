"""Graph figures: representation, traversal order, topological order and shortest paths.

Fixtures live in examples-path.properties and are checked against the library by
VisualExamplesPathTest. Every value drawn here is read from the fixture, never recomputed.
"""

from drawing import STATES, ints


def _pairs(fixtures, key):
    """Parse ``a:b,c:d`` (or ``a:b:c`` triples) into lists of ints."""
    value = fixtures[key]
    return [] if not value else [[int(part) for part in item.split(":")] for item in value.split(",")]


def _strings(fixtures, key):
    value = fixtures[key]
    return [] if not value else value.split(",")


def _arrow_text(values):
    return ", ".join(str(value) for value in values)


def _box(c, x, y, width, height, state):
    """A framed box in a state's colours, for content that needs more than one line of text."""
    stroke, fill, _, dashed = STATES[state]
    c.rect(x, y, width, height, fill=fill, stroke=stroke, dashed=dashed)


# ---------------------------------------------------------------------------------------
# graph-representations
# ---------------------------------------------------------------------------------------

REPRESENTATIONS_IDEA = "Edge tests favor a matrix; neighbor scans favor an adjacency list."


def graph_representations(c, fx):
    n = int(fx["path.repr.vertices"])
    cost = lambda layout: (f"edge test {fx[f'path.repr.cost.{layout}.edge']}  ·  "
                           f"neighbors {fx[f'path.repr.cost.{layout}.neighbors']}")

    y = c.step(156, 1, "Adjacency list")
    c.text(66, y + 6, cost("list"), size=18, color="muted")
    y += 24
    for vertex in range(n):
        neighbors = ints(fx, f"path.repr.list.{vertex}")
        c.text(66, y + 22, f"{vertex} →", weight="700")
        if neighbors:
            c.cell_row(124, y, neighbors, cell=40, gap=6, height=30)
        else:
            c.text(124, y + 22, "empty", size=18, color="muted")
        y += 36

    y = c.step(y + 32, 2, "Adjacency matrix")
    c.text(66, y + 6, cost("matrix"), size=18, color="muted")
    matrix = ints(fx, "path.repr.matrix")
    rows = [[str(matrix[row * n + col]) for col in range(n)] for row in range(n)]
    states = {(row, col): "focus" for row in range(n) for col in range(n) if matrix[row * n + col]}
    labels = [str(vertex) for vertex in range(n)]
    gy = y + 46
    bottom = c.grid(124, gy, rows, cell_w=44, cell_h=30, row_labels=labels, col_labels=labels,
                    states=states)
    c.text(322, gy + 36, "row = from", size=18, color="muted")
    c.text(322, gy + 64, "column = to", size=18, color="muted")

    y = c.step(bottom + 32, 3, "Edge list")
    c.text(66, y + 6, cost("edges"), size=18, color="muted")
    x = 66
    for source, target in _pairs(fx, "path.repr.edges"):
        x += c.chip(x, y + 20, f"{source}→{target}", color="blue") + 8

    y = c.legend(y + 78, [("focus", "1 = edge present, 0 = no edge")])
    c.text(28, y + 20, "deg = out-degree · V = vertices · E = edges", size=18, color="muted")
    c.note(y + 36, REPRESENTATIONS_IDEA)


# ---------------------------------------------------------------------------------------
# bfs-vs-dfs-order
# ---------------------------------------------------------------------------------------

ORDER_IDEA = "A queue visits whole layers; a stack follows one branch to its end."
ORDER_POSITIONS = {0: (260, 195), 1: (150, 275), 2: (370, 275), 3: (90, 355), 4: (210, 355),
                   5: (370, 355)}


def bfs_vs_dfs_order(c, fx):
    source = int(fx["path.order.source"])
    y = c.step(156, 1, f"One graph, source {source}")
    for first, second in _pairs(fx, "path.order.edges"):
        c.edge(ORDER_POSITIONS[first], ORDER_POSITIONS[second], radius=24, directed=False)
    for vertex, (x, ny) in ORDER_POSITIONS.items():
        c.node(x, ny, vertex, radius=24)

    row_x, cell, gap = 66, 50, 6
    bfs = ints(fx, "path.order.bfs")
    distance = ints(fx, "path.order.bfsDistance")
    layer_state = {0: "focus", 1: "good", 2: "alt"}
    y = c.step(425, 2, "breadthFirst: layer by layer")
    y = c.cell_row(row_x, y, bfs, cell=cell, gap=gap,
                   states=[layer_state[d] for d in distance], labels=[f"d{d}" for d in distance])

    dfs = ints(fx, "path.order.dfs")
    parent = ints(fx, "path.order.dfsParent")
    y = c.step(y + 30, 3, "depthFirst: one branch to its end")
    y = c.cell_row(row_x, y, dfs, cell=cell, gap=gap,
                   states=["focus"] + [None] * (len(dfs) - 1),
                   labels=["start"] + [f"via {p}" for p in parent[1:]])

    y = c.legend(y + 30, [("focus", "source"), ("good", "1 edge away"), ("alt", "2 edges away")])
    c.text(28, y + 22, "d = edges from source · via = reached from",
           size=18, color="muted")
    c.note(y + 40, ORDER_IDEA)


# ---------------------------------------------------------------------------------------
# topological-kahn
# ---------------------------------------------------------------------------------------

KAHN_IDEA = "A vertex is output once its in-degree is 0, so every edge points forward."
KAHN_POSITIONS = {2: (130, 190), 3: (330, 190), 0: (130, 266), 1: (330, 266), 4: (230, 342)}


def topological_kahn(c, fx):
    indegree = ints(fx, "path.kahn.indegree")
    c.step(156, 1, "In-degrees at the start")
    for source, target in _pairs(fx, "path.kahn.edges"):
        c.edge(KAHN_POSITIONS[source], KAHN_POSITIONS[target], radius=24)
    for vertex, (x, y) in KAHN_POSITIONS.items():
        c.node(x, y, vertex, radius=24, state="good" if indegree[vertex] == 0 else None)
        label = f"in {indegree[vertex]}"
        if x < 200:
            c.text(x - 36, y + 7, label, anchor="end", size=18, color="muted")
        else:
            c.text(x + 36, y + 7, label, size=18, color="muted")

    y = c.step(405, 2, "Pop a ready vertex, lower its targets")
    c.text(44, y + 18, "popped", size=18, color="muted")
    c.text(130, y + 18, "in-degree drops", size=18, color="muted")
    c.text(340, y + 18, "ready queue", size=18, color="muted")
    y += 30
    rows = [("start", "in-degree 0", _strings(fx, "path.kahn.start"))]
    for popped in _strings(fx, "path.kahn.pops"):
        drops = _pairs(fx, f"path.kahn.drops.{popped}")
        text = ", ".join(f"{v}: {a}→{b}" for v, a, b in drops) if drops else "none"
        rows.append((f"pop {popped}", text, _strings(fx, f"path.kahn.ready.{popped}")))
    for chip, text, ready in rows:
        c.chip(44, y, chip, color="blue")
        c.text(130, y + 21, text, size=18)
        if ready:
            c.cell_row(340, y, ready, cell=40, gap=6, height=30, states=["good"] * len(ready))
        else:
            c.text(340, y + 21, "empty", size=18, color="muted")
        y += 36

    order = _arrow_text(_strings(fx, "path.kahn.order"))
    c.text(28, y + 22, f"Output order: {order}", weight="700")
    y = c.legend(y + 54, [("good", "in-degree 0, ready")])
    c.note(y + 14, KAHN_IDEA)


# ---------------------------------------------------------------------------------------
# shortest-path-chooser
# ---------------------------------------------------------------------------------------

CHOOSER_IDEA = ("Use the most specific algorithm whose precondition your graph meets; each one "
                "exploits a fact the general one cannot assume.")
OWNER_STATES = {"GraphAlgorithms": "focus", "ShortestPathAlgorithms": "alt",
                "AdvancedGraphAlgorithms": "good"}


def shortest_path_chooser(c, fx):
    c.text(28, 152, "Ask from the top; the first yes picks the method.", size=18, color="muted")
    y = 172
    for row in range(1, int(fx["path.chooser.rows"]) + 1):
        key = f"path.chooser.{row}."
        state = OWNER_STATES[fx[key + "owner"]]
        stroke, _, ink, _ = STATES[state]
        c.rect(28, y, 232, 50, fill="panel", stroke="line")
        c.text(44, y + 32, fx[key + "question"], weight="700")
        c.arrow(262, y + 25, 290, y + 25, color=stroke)
        _box(c, 292, y, 200, 50, state)
        c.text(304, y + 22, fx[key + "algorithm"], weight="700", color=ink)
        c.text(304, y + 43, fx[key + "method"], size=18, color=ink)
        y += 62
    y = c.legend(y + 24, [(state, owner) for owner, state in OWNER_STATES.items()])
    c.note(y + 14, CHOOSER_IDEA)


# ---------------------------------------------------------------------------------------
# zero-one-bfs-deque
# ---------------------------------------------------------------------------------------

ZERO_ONE_IDEA = "Zero-weight edges go to the deque's front, unit-weight edges to its back."
ZOB_POSITIONS = {0: (90, 205), 1: (430, 205), 2: (170, 290), 3: (350, 290)}


def zero_one_bfs_deque(c, fx):
    c.step(156, 1, "Edge weights 0 and 1")
    for source, target, weight in _pairs(fx, "path.zob.edges"):
        c.edge(ZOB_POSITIONS[source], ZOB_POSITIONS[target], radius=24,
               color="green" if weight == 0 else "violet", label=str(weight))
    for vertex, (x, y) in ZOB_POSITIONS.items():
        c.node(x, y, vertex, radius=24)

    source = fx["path.zob.source"]
    y = c.step(358, 2, "Deque after each pop, front on the left")
    deque = [(int(v), "focus") for v in _strings(fx, "path.zob.deque.start")]
    rows = [("start", f"source {source}", deque)]
    for popped in (0, 2, 3, 1):
        front = [int(v) for v in _strings(fx, f"path.zob.front.pop{popped}")]
        back = [int(v) for v in _strings(fx, f"path.zob.back.pop{popped}")]
        improved = _pairs(fx, f"path.zob.improved.pop{popped}") if f"path.zob.improved.pop{popped}" in fx else []
        deque = ([(v, "good") for v in reversed(front)] + deque[1:] + [(v, "alt") for v in back])
        assert [v for v, _ in deque] == ints(fx, f"path.zob.deque.pop{popped}")
        parts = [f"{v} front" for v in front] + [f"{v} back" for v in back]
        parts += [f"dist {before}→{after}" for _, before, after in improved]
        rows.append((f"pop {popped}", ", ".join(parts) or "no pushes", deque))
    for chip, text, cells in rows:
        width = c.chip(44, y, chip, color="blue")
        c.text(44 + width + 12, y + 21, text, size=18)
        c.cell_row(300, y, [v for v, _ in cells], cell=44, gap=6, height=30,
                   states=[state for _, state in cells])
        y += 38

    distances = ints(fx, "path.zob.distance")
    path = ints(fx, "path.zob.path")
    c.paragraph(28, y + 18, f"The second 1 is skipped: 1 is already final. Route "
                f"{' → '.join(map(str, path))} costs {distances[path[-1]]}, not 1.",
                width=50, size=18, color="muted", line_height=24)
    y = c.legend(y + 82, [("focus", "source"), ("good", "front push, weight 0"),
                          ("alt", "back push, weight 1")])
    c.note(y + 14, ZERO_ONE_IDEA)


# ---------------------------------------------------------------------------------------
# astar-heuristic
# ---------------------------------------------------------------------------------------

ASTAR_IDEA = "A good heuristic keeps the search near the route; far cells never get an h evaluation."


def astar_heuristic(c, fx):
    rows, columns = int(fx["path.astar.rows"]), int(fx["path.astar.columns"])
    source, target = int(fx["path.astar.source"]), int(fx["path.astar.target"])
    route = ints(fx, "path.astar.route")
    seen = set(ints(fx, "path.astar.hSeen"))
    g, h, f = (ints(fx, f"path.astar.{key}") for key in "ghf")
    h_by_cell = dict(zip(route, h))

    y = c.step(156, 1, "Grid cells show h, the Manhattan distance")
    cells, states = [], {}
    for row in range(rows):
        line = []
        for column in range(columns):
            cell = row * columns + column
            line.append(str(abs(row - target // columns) + abs(column - target % columns))
                        if cell in seen else "")
            states[(row, column)] = ("good" if cell in route else "focus" if cell in seen else "muted")
        cells.append(line)
    labels = [""] * rows
    labels[source // columns] = "start"
    gx = 96
    bottom = c.grid(gx, y + 4, cells, cell_w=56, cell_h=34, row_labels=labels, states=states)
    c.text(gx + columns * 56 + 10, y + 4 + (target // columns) * 34 + 28, "target", size=18, color="muted")
    assert all(h_by_cell[cell] == abs(cell // columns - target // columns) + abs(cell % columns - target % columns)
               for cell in route)

    y = c.step(bottom + 36, 2, f"Along the route, f = g + h stays {f[0]}")
    gy = y + 30
    values = [[str(v) for v in series] for series in (g, h, f)]
    bottom = c.grid(gx, gy, values, cell_w=56, cell_h=34, row_labels=["g", "h", "f"],
                    col_labels=[str(cell) for cell in route], states={(2, col): "focus" for col in range(len(route))})
    c.text(gx - 12, gy - 12, "cell", anchor="end", size=18, color="muted")

    zero = int(fx["path.astar.zeroSeen"])
    y = c.step(bottom + 36, 3, "Cells the heuristic was called for")
    width = c.chip(44, y, f"Manhattan h: {len(seen)} of {rows * columns}", color="green")
    c.chip(44 + width + 10, y, f"h = 0: {zero} of {rows * columns}", color="gold")
    y = c.legend(y + 56, [("good", "route"), ("focus", "h evaluated"), ("muted", "h never called")])
    c.note(y + 14, ASTAR_IDEA)


# ---------------------------------------------------------------------------------------
# floyd-warshall-via
# ---------------------------------------------------------------------------------------

FLOYD_IDEA = "Round k asks whether going through vertex k beats the best route so far."


def _matrix(fixtures, key, n):
    cells = [("∞" if value == "INF" else value) for value in _strings(fixtures, key)]
    return [cells[row * n:(row + 1) * n] for row in range(n)]


def floyd_warshall_via(c, fx):
    n, k = int(fx["path.fw.vertices"]), int(fx["path.fw.k"])
    i, j = int(fx["path.fw.from"]), int(fx["path.fw.to"])
    old, new = fx["path.fw.oldValue"], fx["path.fw.newValue"]
    before, after = _matrix(fx, "path.fw.before", n), _matrix(fx, "path.fw.after", n)
    labels = [str(v) for v in range(n)]
    first = ", ".join(str(v) for v in range(k))
    both = ", ".join(str(v) for v in range(k + 1))
    gx, cell_h = 96, 30

    y = c.step(156, 1, f"Before: stop-overs {{{first}}} allowed")
    states = {(k, col): "alt" for col in range(n)}
    states.update({(row, k): "alt" for row in range(n)})
    states[(i, j)] = "warn"
    bottom = c.grid(gx, y + 28, before, cell_w=56, cell_h=cell_h, row_labels=labels, col_labels=labels,
                    states=states)

    a, b = before[i][k], before[k][j]
    y = c.step(bottom + 34, 2, f"Try vertex {k} as the stop-over")
    c.text(66, y + 6, f"d[{i}][{k}] + d[{k}][{j}] = {a} + {b} = {new}")
    c.text(66, y + 36, f"{new} < {old}, so d[{i}][{j}] becomes {new}")

    y = c.step(y + 76, 3, f"After: stop-overs {{{both}}} allowed")
    states = {(i, j): "good"}
    bottom = c.grid(gx, y + 28, after, cell_w=56, cell_h=cell_h, row_labels=labels, col_labels=labels,
                    states=states)

    y = c.legend(bottom + 36, [("alt", f"row {k} and column {k}"), ("warn", "entry that can improve"),
                               ("good", "improved")])
    c.note(y + 14, FLOYD_IDEA)


FIGURES = [
    {
        "id": "graph-representations", "title": "Three ways to store a graph", "category": "Graphs",
        "description": ("One directed graph with 4 vertices and the edges 0 to 1, 0 to 2, 2 to 1 and "
                        "2 to 3, shown as an adjacency list, a 4 by 4 adjacency matrix and an edge "
                        "list, each with the cost of testing an edge and listing neighbors."),
        "keyIdea": REPRESENTATIONS_IDEA, "lesson": "data-structures/graphs/representation.md",
        "height": 876, "draw": graph_representations,
    },
    {
        "id": "bfs-vs-dfs-order", "title": "BFS and DFS visit order", "category": "Graphs",
        "description": ("A 6-vertex undirected graph from source 0: breadthFirst visits 0, 1, 2, 3, 4, 5 "
                        "in layers of distance 0, 1 and 2, while depthFirst visits 0, 1, 3, 4, 5, 2 by "
                        "following one branch to its end."),
        "keyIdea": ORDER_IDEA, "lesson": "data-structures/graphs/traversal.md",
        "height": 872, "draw": bfs_vs_dfs_order,
    },
    {
        "id": "topological-kahn", "title": "Kahn's topological order", "category": "Graphs",
        "description": ("Kahn's algorithm on a 5-vertex DAG whose in-degrees are 2, 1, 0, 0 and 2: it pops "
                        "ready vertices 2, 3, 0, 1, 4, lowering in-degrees and refilling the ready queue, "
                        "and outputs 2, 3, 0, 1, 4."),
        "keyIdea": KAHN_IDEA, "lesson": "algorithms/graph/topological-sort.md",
        "height": 884, "draw": topological_kahn,
    },
    {
        "id": "shortest-path-chooser", "title": "Shortest-path chooser", "category": "Graphs",
        "description": ("A column of seven yes-or-no questions from all pairs, unweighted, 0-1 weights, "
                        "DAG, negative weights and heuristic to nonnegative weights, each paired with the "
                        "algorithm and the repository method that answers it."),
        "keyIdea": CHOOSER_IDEA, "lesson": "algorithms/graph/shortest-paths.md",
        "height": 876, "draw": shortest_path_chooser,
    },
    {
        "id": "zero-one-bfs-deque", "title": "0-1 BFS and its deque", "category": "Graphs",
        "description": ("0-1 BFS from vertex 0 on a 4-vertex directed graph: weight-0 targets are pushed to "
                        "the front of the deque and weight-1 targets to the back, so vertex 1 is first "
                        "queued at distance 1 and then improved to 0 through 0, 2, 3, 1."),
        "keyIdea": ZERO_ONE_IDEA, "lesson": "algorithms/graph/shortest-paths.md",
        "height": 840, "draw": zero_one_bfs_deque,
    },
    {
        "id": "astar-heuristic", "title": "A* with a good heuristic", "category": "Graphs",
        "description": ("A* on a 5 by 5 unit grid from cell 10 to cell 14 with the Manhattan heuristic: the "
                        "route 10, 11, 12, 13, 14 has f = g + h equal to 4 everywhere, the heuristic was "
                        "called for 13 of 25 cells, and with h = 0 it was called for 21."),
        "keyIdea": ASTAR_IDEA, "lesson": "algorithms/graph/shortest-paths.md",
        "height": 840, "draw": astar_heuristic,
    },
    {
        "id": "floyd-warshall-via", "title": "One Floyd-Warshall round", "category": "Graphs",
        "description": ("A 4-vertex directed graph: allowing vertex 1 as a stop-over improves the distance "
                        "matrix entry d[0][2] from 9 to 5 because d[0][1] + d[1][2] = 3 + 2, and no other "
                        "entry changes."),
        "keyIdea": FLOYD_IDEA, "lesson": "algorithms/graph/shortest-paths.md",
        "height": 852, "draw": floyd_warshall_via,
    },
]
