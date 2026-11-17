"""Graph structure, spanning-tree, flow and tree-query figures; fixtures in examples-connectivity.properties.

Vertex positions are drawing choices and live here; every graph, weight, order and result
comes from the fixture file and is asserted by VisualExamplesConnectivityTest. The one
exception the controller approved is the DFS discovery and low values in
``articulation_points``: they are internal to the library, so they are computed here by the
standard definition with the neighbour order ``Graph`` documents, and the Java test asserts
the observable result (the articulation points and what removing each vertex does).
"""

from math import cos, pi, sin

from drawing import ints

CATEGORY = "Graphs"
R = 22  # node radius used by every figure in this module


# ---- fixture parsing and small helpers ------------------------------------------------

def _pairs(fixtures, key):
    """Parse ``a-b`` (optionally ``a-b:w``) items into ``(a, b)`` tuples."""
    value = fixtures[key]
    if not value:
        return []
    return [tuple(int(p) for p in item.split(":")[0].split("-")) for item in value.split(",")]


def _weighted(fixtures, key):
    return [(*tuple(int(p) for p in item.split(":")[0].split("-")), int(item.split(":")[1]))
            for item in fixtures[key].split(",")]


def _adjacency(count, edges):
    """Adjacency lists in ``Graph.addEdge`` insertion order, as ``Graph.neighbors`` reports."""
    adjacency = [[] for _ in range(count)]
    for a, b in edges:
        if b not in adjacency[a]:
            adjacency[a].append(b)
        if a != b and a not in adjacency[b]:
            adjacency[b].append(a)
    return adjacency


def _node(c, pos, vertex, state=None, label=None):
    x, y = pos[vertex]
    c.node(x, y, str(vertex) if label is None else label, radius=R, state=state)


def _line(c, pos, a, b, *, color="line", dashed=False, label=None, directed=False):
    c.edge(pos[a], pos[b], radius=R, color=color, dashed=dashed, label=label, directed=directed)


def _side_label(c, pos, vertex, text, *, side, color="muted", weight="700"):
    """An 18px label beside a node: ``left``, ``right``, ``above`` or ``below``."""
    x, y = pos[vertex]
    if side == "left":
        c.text(x - R - 6, y + 6, text, anchor="end", size=18, color=color, weight=weight)
    elif side == "right":
        c.text(x + R + 6, y + 6, text, anchor="start", size=18, color=color, weight=weight)
    elif side == "above":
        c.text(x, y - R - 8, text, anchor="middle", size=18, color=color, weight=weight)
    else:
        c.text(x, y + R + 24, text, anchor="middle", size=18, color=color, weight=weight)


def _vertex_set(vertices):
    return "{" + ", ".join(str(v) for v in vertices) + "}"


# ---- articulation-points --------------------------------------------------------------

CUT_IDEA = "An articulation point has a child whose low is at least its own disc."


def _lowlink(adjacency, root=0):
    """Standard DFS discovery and low values, neighbours in adjacency order, parent skipped."""
    disc, low, parent = {}, {}, {root: None}

    def visit(vertex):
        disc[vertex] = low[vertex] = len(disc) + 1
        for neighbor in adjacency[vertex]:
            if neighbor == parent[vertex]:
                continue
            if neighbor not in disc:
                parent[neighbor] = vertex
                visit(neighbor)
                low[vertex] = min(low[vertex], low[neighbor])
            else:
                low[vertex] = min(low[vertex], disc[neighbor])

    visit(root)
    return disc, low, parent


def articulation_points(c, fixtures):
    count = int(fixtures["connectivity.cut.vertices"])
    edges = _pairs(fixtures, "connectivity.cut.edges")
    cut = ints(fixtures, "connectivity.cut.points")[0]
    disc, low, parent = _lowlink(_adjacency(count, edges))
    tree = {frozenset((v, p)) for v, p in parent.items() if p is not None}
    child = next(v for v, p in parent.items() if p == cut and low[v] >= disc[cut])
    half = 44

    def place(cy):
        return {0: (140, cy - half), 1: (140, cy + half), 2: (260, cy), 3: (380, cy - half),
                4: (380, cy + half)}

    sides = {0: "left", 1: "left", 3: "right", 4: "right"}
    y = c.step(156, 1, "DFS from 0: disc (d) and low (l)")
    pos = place(y + 14 + R + half + 14)
    for a, b in edges:
        is_tree = frozenset((a, b)) in tree
        _line(c, pos, a, b, color="blue" if is_tree else "line", dashed=not is_tree)
    for vertex in range(count):
        _node(c, pos, vertex, state="warn" if vertex == cut else None)
        _side_label(c, pos, vertex, f"d{disc[vertex]} l{low[vertex]}", side=sides.get(vertex, "above"),
                    color="gold" if vertex == cut else "muted")
    y = pos[1][1] + R + 28
    c.text(28, y, "solid: DFS tree edge   dashed: back edge", size=18, color="muted")

    y = c.step(y + 40, 2, f"low({child}) = {low[child]} >= disc({cut}) = {disc[cut]}")
    c.text(66, y + 4, f"Child {child} cannot climb above {cut}: {cut} cuts the graph.", size=18,
           weight="700", color="gold")

    y = c.step(y + 44, 3, f"Remove {cut}: two components remain")
    pos = place(y + 14 + R + half + 14 + 8)
    left = [v for v in range(count) if v != cut and pos[v][0] < 200]
    right = [v for v in range(count) if v != cut and pos[v][0] > 300]
    for a, b in edges:
        if cut not in (a, b):
            _line(c, pos, a, b)
    for vertex in range(count):
        _node(c, pos, vertex, state="bad" if vertex == cut else "focus" if vertex in left else "alt")
    _side_label(c, pos, cut, "removed", side="above", color="red")
    below = pos[1][1] + R + 14
    c.brace(pos[left[0]][0] - R, pos[left[0]][0] + R, below, _vertex_set(left), color="blue")
    c.brace(pos[right[0]][0] - R, pos[right[0]][0] + R, below, _vertex_set(right), color="violet")

    y = c.legend(below + 66, [("warn", "articulation point"), ("bad", "removed vertex")])
    articulation_points.end = y + 12 + c.note_height(CUT_IDEA)
    c.note(y + 12, CUT_IDEA)


# ---- bipartite-odd-cycle --------------------------------------------------------------

BIP_IDEA = ("A graph is two-colourable exactly when it has no odd cycle; the first edge joining equal "
            "colours exposes one.")


def _ring(cx, cy, radius, count, first=-90):
    return {v: (round(cx + radius * cos((first + 360 * v / count) * pi / 180)),
                round(cy + radius * sin((first + 360 * v / count) * pi / 180))) for v in range(count)}


def bipartite_odd_cycle(c, fixtures):
    even_edges = _pairs(fixtures, "connectivity.bip.even.edges")
    left = ints(fixtures, "connectivity.bip.even.left")
    right = ints(fixtures, "connectivity.bip.even.right")
    odd_edges = _pairs(fixtures, "connectivity.bip.odd.edges")
    parity = ints(fixtures, "connectivity.bip.odd.parity")
    conflict = _pairs(fixtures, "connectivity.bip.odd.conflict")[0]
    cycle = ints(fixtures, "connectivity.bip.odd.cycle")
    cy = 300
    even = _ring(140, cy, 62, 6)
    odd = _ring(350, cy, 62, 5)
    odd[5] = (448, cy - 96)

    y = c.step(156, 1, "Two-colour by breadth-first parity")
    c.text(140, 196, "6-cycle", anchor="middle", size=18, weight="700", color="muted")
    c.text(350, 196, "5-cycle + tail", anchor="middle", size=18, weight="700", color="muted")
    for a, b in even_edges:
        _line(c, even, a, b)
    for vertex in range(6):
        _node(c, even, vertex, state="focus" if vertex in left else "alt")
    for a, b in odd_edges:
        broken = {a, b} == set(conflict)
        _line(c, odd, a, b, color="red" if broken else "line", dashed=broken)
    for vertex in range(6):
        _node(c, odd, vertex, state="focus" if parity[vertex] == 0 else "alt")
    base = cy + 62 + R + 28
    c.text(140, base, f"left {_vertex_set(left)}", anchor="middle", size=18, weight="700", color="blue")
    c.text(140, base + 26, f"right {_vertex_set(right)}", anchor="middle", size=18, weight="700", color="violet")
    c.text(350, base, f"{conflict[0]} and {conflict[1]} share a side", anchor="middle", size=18,
           weight="700", color="red")
    c.text(350, base + 26, "no valid colouring", anchor="middle", size=18, weight="700", color="muted")

    y = c.step(base + 78, 2, f"Witness: an odd cycle of length {len(cycle)}")
    states = ["bad"] * len(cycle)
    c.cell_row(39, y + 4, cycle, cell=60, gap=12, states=states)
    c.text(39 + 5 * 72 - 12 + 12, y + 33, f"→ {cycle[0]}", size=20, weight="700", color="red")
    c.text(66, y + 86, f"{len(cycle)} vertices, {len(cycle)} edges: the tree paths to the", size=18, color="muted")
    c.text(66, y + 112, f"conflict edge {conflict[0]}-{conflict[1]} plus that edge.", size=18, color="muted")

    y = c.legend(y + 156, [("focus", "left side"), ("alt", "right side"), ("bad", "witness cycle")])
    bipartite_odd_cycle.end = y + 12 + c.note_height(BIP_IDEA)
    c.note(y + 12, BIP_IDEA)


# ---- euler-path -----------------------------------------------------------------------

EULER_IDEA = "Connected edges with zero or two odd-degree vertices give an Euler path."


def euler_path(c, fixtures):
    count = int(fixtures["connectivity.euler.vertices"])
    edges = _pairs(fixtures, "connectivity.euler.edges")
    degrees = ints(fixtures, "connectivity.euler.degrees")
    odd = ints(fixtures, "connectivity.euler.odd")
    start = int(fixtures["connectivity.euler.start"])
    path = ints(fixtures, "connectivity.euler.path")
    end = path[-1]
    drop = 92

    def place(top):
        return {0: (120, top), 1: (270, top), 2: (270, top + drop), 3: (120, top + drop),
                4: (420, top + drop)}

    y = c.step(156, 1, "Count the degree of every vertex")
    pos = place(y + 14 + 18 + R + 8)
    for a, b in edges:
        _line(c, pos, a, b)
    for vertex in range(count):
        _node(c, pos, vertex, state="warn" if vertex in odd else None)
        _side_label(c, pos, vertex, f"deg {degrees[vertex]}", side="above" if vertex < 2 else "below",
                    color="gold" if vertex in odd else "muted")
    y = pos[3][1] + R + 24 + 34
    c.text(28, y, f"Odd degree: {_vertex_set(odd)}. Start at the smaller, {start}.", size=18, weight="700")

    y = c.step(y + 40, 2, f"Walk every edge once, {start} to {end}")
    pos = place(y + 14 + R)
    for a, b in edges:
        _line(c, pos, a, b)
    for number, (a, b) in enumerate(zip(path, path[1:]), start=1):
        _line(c, pos, a, b, color="blue", directed=True, label=str(number))
    for vertex in range(count):
        _node(c, pos, vertex, state="focus" if vertex == start else "good" if vertex == end else None)
    _side_label(c, pos, start, "start", side="left", color="blue")
    _side_label(c, pos, end, "end", side="right", color="green")
    y = pos[3][1] + R + 34
    c.text(28, y, "Path  " + "  ".join(str(v) for v in path) + f"   ({len(edges)} edges)", weight="700")

    y = c.legend(y + 38, [("warn", "odd degree"), ("focus", "start"), ("good", "end")])
    euler_path.end = y + 12 + c.note_height(EULER_IDEA)
    c.note(y + 12, EULER_IDEA)


# ---- mst-kruskal-prim -----------------------------------------------------------------

MST_IDEA = "Both algorithms add cheapest safe edges, in different orders, to reach one total."


def mst_kruskal_prim(c, fixtures):
    edges = _weighted(fixtures, "connectivity.mst.edges")
    count = int(fixtures["connectivity.mst.vertices"])
    ordered = _weighted(fixtures, "connectivity.mst.sorted")
    kruskal = _weighted(fixtures, "connectivity.mst.kruskal")
    prim = _weighted(fixtures, "connectivity.mst.prim")
    total = int(fixtures["connectivity.mst.total"])
    tree = {frozenset(e[:2]) for e in kruskal}

    y = c.step(156, 1, f"One graph; its minimum tree totals {total}")
    top = y + 34
    pos = {0: (70, top + 56), 1: (230, top), 2: (230, top + 112), 3: (390, top), 4: (430, top + 112)}
    for a, b, w in edges:
        chosen = frozenset((a, b)) in tree
        _line(c, pos, a, b, color="blue" if chosen else "line", dashed=not chosen, label=str(w))
    for vertex in range(count):
        _node(c, pos, vertex)
    y = top + 112 + R + 30
    c.text(28, y, "solid: in the tree   dashed: left out", size=18, color="muted")

    def plus(es):
        return " + ".join(str(w) for _, _, w in es)

    y = c.step(y + 40, 2, f"Kruskal: {plus(kruskal)} = {total}")
    states = ["good" if frozenset(e[:2]) in tree else "bad" for e in ordered]
    y = c.cell_row(39, y + 4, [w for _, _, w in ordered], cell=60, gap=10, states=states,
                   labels=[f"{a}-{b}" for a, b, _ in ordered])

    y = c.step(y + 36, 3, f"Prim from {fixtures['connectivity.mst.primStart']}: {plus(prim)} = {total}")
    y = c.cell_row(39, y + 4, [w for _, _, w in prim], cell=60, gap=10, states=["good"] * len(prim),
                   labels=[f"{a}\u2192{b}" for a, b, _ in prim])

    y = c.legend(y + 38, [("good", "edge kept"), ("bad", "closes a cycle")])
    mst_kruskal_prim.end = y + 12 + c.note_height(MST_IDEA)
    c.note(y + 12, MST_IDEA)


# ---- max-flow-min-cut -----------------------------------------------------------------

FLOW_IDEA = "Edges leaving the final source side are saturated and sum to the flow."


def max_flow_min_cut(c, fixtures):
    count = int(fixtures["connectivity.flow.vertices"])
    source, sink = int(fixtures["connectivity.flow.source"]), int(fixtures["connectivity.flow.sink"])
    edges = _weighted(fixtures, "connectivity.flow.edges")
    flows = ints(fixtures, "connectivity.flow.flows")
    value = int(fixtures["connectivity.flow.value"])
    side = ints(fixtures, "connectivity.flow.sourceSide")
    cut_edges = [(a, b, w) for (a, b, w) in edges if a in side and b not in side]
    drop = 60

    def place(cy):
        return {0: (64, cy), 1: (260, cy - drop), 2: (260, cy + drop), 3: (456, cy)}

    def roles(pos, vertex):
        if vertex == source:
            _side_label(c, pos, vertex, "source", side="below")
        if vertex == sink:
            _side_label(c, pos, vertex, "sink", side="below")

    y = c.step(156, 1, "Each edge has a capacity")
    pos = place(y + 14 + R + drop + 10)
    for a, b, w in edges:
        _line(c, pos, a, b, directed=True, label=str(w))
    for vertex in range(count):
        _node(c, pos, vertex)
        roles(pos, vertex)

    y = c.step(pos[2][1] + R + 36, 2, f"Maximum flow {value}: flow/capacity")
    pos = place(y + 14 + R + drop + 10)
    for (a, b, w), flow in zip(edges, flows):
        crossing = (a, b, w) in cut_edges
        _line(c, pos, a, b, directed=True, color="red" if crossing else "blue" if flow == w else "line",
              label=f"{flow}/{w}")
    for vertex in range(count):
        _node(c, pos, vertex, state="focus" if vertex in side else "alt")
        roles(pos, vertex)

    y = pos[2][1] + R + 36
    text = "Cut: " + " + ".join(str(w) for _, _, w in cut_edges) + f" = {sum(w for _, _, w in cut_edges)} = flow {value}"
    c.text(28, y, text, weight="700", color="red")
    c.text(28, y + 28, f"{edges[0][0]}\u2192{edges[0][1]} has room left, so vertex {edges[0][1]} stays reachable.",
           size=18, color="muted")

    y = c.legend(y + 68, [("focus", "source side"), ("alt", "sink side"), ("bad", "edge crossing the cut")])
    max_flow_min_cut.end = y + 12 + c.note_height(FLOW_IDEA)
    c.note(y + 12, FLOW_IDEA)


# ---- matching-augmenting-path ---------------------------------------------------------

MATCH_IDEA = "Flipping one augmenting path grows a matching by exactly one pair."


def matching_augmenting_path(c, fixtures):
    edges = _pairs(fixtures, "connectivity.match.edges")
    partial = _pairs(fixtures, "connectivity.match.partial")
    final = _pairs(fixtures, "connectivity.match.final")
    path_left = ints(fixtures, "connectivity.match.pathLeft")
    path_right = ints(fixtures, "connectivity.match.pathRight")
    free_left = int(fixtures["connectivity.match.freeLeft"])
    free_right = int(fixtures["connectivity.match.freeRight"])
    row = 58
    # Left rows are ordered so the augmenting path is a clean zig-zag with no crossings.
    left_order, right_order = [1, 0, 2], [0, 1, 2]

    def draw(top, matched, joining, free_l, free_r):
        left = {v: (130, top + row * i) for i, v in enumerate(left_order)}
        right = {v: (390, top + row * i) for i, v in enumerate(right_order)}
        for a, b in edges:
            if (a, b) in matched:
                c.edge(left[a], right[b], radius=R, color="green", directed=False)
            elif (a, b) in joining:
                c.edge(left[a], right[b], radius=R, color="blue", dashed=True, directed=False)
            else:
                c.edge(left[a], right[b], radius=R, color="line", directed=False)
        for v, (x, y_) in left.items():
            c.node(x, y_, f"L{v}", radius=R, state="warn" if v in free_l else "good")
        for v, (x, y_) in right.items():
            c.node(x, y_, f"R{v}", radius=R, state="warn" if v in free_r else "good")
        for v in free_l:
            c.text(left[v][0] - R - 8, left[v][1] + 6, "free", anchor="end", size=18, weight="700", color="gold")
        for v in free_r:
            c.text(right[v][0] + R + 8, right[v][1] + 6, "free", anchor="start", size=18, weight="700",
                   color="gold")
        return top + 2 * row + R

    path_edges = {(a, b) for a, b in zip(path_left, path_right)}
    path_edges |= {(a, b) for a, b in zip(path_left[1:], path_right)}
    joining = path_edges - set(partial)

    y = c.step(156, 1, f"A matching of size {len(partial)}; two vertices are free")
    bottom = draw(y + 14 + R, set(partial), joining, {free_left}, {free_right})
    walk = " \u2013 ".join(f"{'L' if i % 2 == 0 else 'R'}{v}" for i, v in
                           enumerate(v for pair in zip(path_left, path_right) for v in pair))
    c.text(28, bottom + 34, f"Alternating path: {walk}", size=18, weight="700", color="blue")

    y = c.step(bottom + 78, 2, f"Flip the path: a matching of size {len(final)}")
    bottom = draw(y + 14 + R, set(final), set(), set(), set())

    y = c.legend(bottom + 48, [("good", "matched vertex"), ("warn", "free vertex")])
    c.text(28, y + 22, "solid green: matched edge", size=18, color="muted")
    c.text(28, y + 48, "dashed blue: becomes matched after the flip", size=18, color="muted")
    matching_augmenting_path.end = y + 72 + c.note_height(MATCH_IDEA)
    c.note(y + 72, MATCH_IDEA)


# ---- lca-binary-lifting ---------------------------------------------------------------

LCA_IDEA = "Equalise depths, then jump both by powers of two while ancestors differ."


def lca_binary_lifting(c, fixtures):
    count = int(fixtures["connectivity.lca.vertices"])
    edges = _pairs(fixtures, "connectivity.lca.edges")
    a, b = int(fixtures["connectivity.lca.a"]), int(fixtures["connectivity.lca.b"])
    depths = ints(fixtures, "connectivity.lca.depths")
    table = [ints(fixtures, f"connectivity.lca.up{j}") for j in range(3)]
    lifted = int(fixtures["connectivity.lca.lifted"])
    meet = ints(fixtures, "connectivity.lca.meet")
    answer = int(fixtures["connectivity.lca.answer"])
    gap = depths[a] - depths[b]
    row = 50
    top = 204
    pos = {0: (260, top), 1: (150, top + row), 2: (350, top + row), 3: (150, top + 2 * row),
           4: (350, top + 2 * row), 5: (150, top + 3 * row), 6: (150, top + 4 * row)}

    y = c.step(156, 1, f"lca({a}, {b}): {a} is {gap} deeper")
    for u, v in edges:
        _line(c, pos, u, v)
    # Jumps drawn on the left of each chain, bulging away from the tree edges.
    c.curve(126, pos[a][1] - 6, 126, pos[lifted][1] + 8, bend=36, color="blue", label="2^1")
    c.curve(126, pos[lifted][1] - 8, 126, pos[meet[0]][1] + 8, bend=26, color="violet", label="2^0")
    c.curve(326, pos[b][1] - 8, 326, pos[meet[1]][1] + 8, bend=26, color="violet", label="2^0")
    for vertex in range(count):
        state = {a: "focus", b: "alt", answer: "good", lifted: "focus"}.get(vertex)
        _node(c, pos, vertex, state=state)
    _side_label(c, pos, a, f"a, depth {depths[a]}", side="right", color="blue")
    _side_label(c, pos, b, f"b, depth {depths[b]}", side="right", color="violet")
    _side_label(c, pos, answer, "lca", side="right", color="green")
    c.text(300, top + 3 * row + 10, f"lift {a} by {gap}: {a} → {lifted}", size=18, weight="700", color="blue")
    c.text(300, top + 3 * row + 36, "j=2, 1: equal, skip", size=18, color="muted")
    c.text(300, top + 3 * row + 62, f"j=0: {meet[0]} vs {meet[1]},", size=18, color="violet", weight="700")
    c.text(300, top + 3 * row + 88, "jump both, then up", size=18, color="violet", weight="700")

    y = c.step(top + 4 * row + R + 44, 2, "The table up[j][v]: 2^j-th ancestor")
    grid_x = 84
    states = {(1, a): "focus", (2, lifted): "muted", (2, b): "muted", (1, lifted): "muted", (1, b): "muted",
              (0, lifted): "warn", (0, b): "warn", (0, meet[0]): "good"}
    bottom = c.grid(grid_x, y + 34, table, cell_w=54, cell_h=38,
                    row_labels=["2^0", "2^1", "2^2"], col_labels=[str(v) for v in range(count)],
                    states=states)
    c.text(28, bottom + 30, f"lca({a}, {b}) = up[0][{meet[0]}] = {answer}", weight="700", color="green")

    y = c.legend(bottom + 66, [("focus", "lift to equal depth"), ("muted", "equal: skip"),
                               ("warn", "differ: jump"), ("good", "answer")])
    lca_binary_lifting.end = y + 12 + c.note_height(LCA_IDEA)
    c.note(y + 12, LCA_IDEA)


# ---- tree-diameter --------------------------------------------------------------------

DIAMETER_IDEA = "A sweep from any vertex ends at a diameter endpoint; sweep again from there."


def tree_diameter(c, fixtures):
    count = int(fixtures["connectivity.diameter.vertices"])
    edges = _pairs(fixtures, "connectivity.diameter.edges")
    start = int(fixtures["connectivity.diameter.start"])
    dist0 = ints(fixtures, "connectivity.diameter.dist0")
    u = int(fixtures["connectivity.diameter.u"])
    dist_u = ints(fixtures, "connectivity.diameter.distU")
    v = int(fixtures["connectivity.diameter.v"])
    path = ints(fixtures, "connectivity.diameter.path")
    length = int(fixtures["connectivity.diameter.length"])
    row = 50

    def place(top):
        return {0: (260, top), 1: (170, top + row), 2: (350, top + row), 3: (110, top + 2 * row),
                4: (230, top + 2 * row), 5: (350, top + 2 * row), 6: (110, top + 3 * row)}

    y = c.step(156, 1, f"Sweep 1 from {start}: farthest is {u}")
    pos = place(y + 14 + R)
    for p, q in edges:
        _line(c, pos, p, q)
    for vertex in range(count):
        _node(c, pos, vertex, state="focus" if vertex == start else "warn" if vertex == u else None)
        tag = " start" if vertex == start else " = u" if vertex == u else ""
        _side_label(c, pos, vertex, f"d={dist0[vertex]}{tag}", side="right",
                    color="blue" if vertex == start else "gold" if vertex == u else "muted")

    y = c.step(pos[6][1] + R + 36, 2, f"Sweep 2 from {u}: farthest is {v}")
    pos = place(y + 14 + R)
    on_path = {frozenset(pair) for pair in zip(path, path[1:])}
    for p, q in edges:
        if frozenset((p, q)) not in on_path:
            _line(c, pos, p, q)
    for p, q in edges:
        if frozenset((p, q)) in on_path:
            _line(c, pos, p, q, color="green")
    for vertex in range(count):
        _node(c, pos, vertex, state="focus" if vertex == u else "good" if vertex == v else None)
        tag = " start" if vertex == u else " = v" if vertex == v else ""
        _side_label(c, pos, vertex, f"d={dist_u[vertex]}{tag}", side="right",
                    color="blue" if vertex == u else "green" if vertex == v else "muted")
    y = pos[6][1] + R + 34
    c.text(28, y, "Path " + " \u2192 ".join(str(x) for x in path) + f" ({length} edges)", weight="700",
           color="green")

    tree_diameter.end = y + 22 + c.note_height(DIAMETER_IDEA)
    c.note(y + 22, DIAMETER_IDEA)


FIGURES = [
    {
        "id": "articulation-points",
        "title": "Articulation points",
        "category": CATEGORY,
        "description": ("A bowtie graph of two triangles sharing vertex 2 is searched depth-first from 0 with "
                        "discovery and low values shown, and removing vertex 2 leaves the separate components "
                        "{0, 1} and {3, 4}."),
        "keyIdea": CUT_IDEA,
        "lesson": "algorithms/graph/graph-structure.md",
        "height": 896,
        "draw": articulation_points,
    },
    {
        "id": "bipartite-odd-cycle",
        "title": "Bipartite or odd cycle",
        "category": CATEGORY,
        "description": ("A 6-cycle splits into left {0, 2, 4} and right {1, 3, 5}, while a 5-cycle with a tail "
                        "has an edge 2-3 joining two vertices of the same colour, and the witness is the odd "
                        "cycle 2, 1, 0, 4, 3."),
        "keyIdea": BIP_IDEA,
        "lesson": "algorithms/graph/graph-structure.md",
        "height": 850,
        "draw": bipartite_odd_cycle,
    },
    {
        "id": "euler-path",
        "title": "Euler path",
        "category": CATEGORY,
        "description": ("A square with a diagonal and a pendant edge has odd degree only at vertices 0 and 4, so "
                        "the Euler path starts at 0 and walks every edge once: 0, 1, 2, 3, 0, 2, 4."),
        "keyIdea": EULER_IDEA,
        "lesson": "algorithms/graph/graph-structure.md",
        "height": 850,
        "draw": euler_path,
    },
    {
        "id": "mst-kruskal-prim",
        "title": "Kruskal and Prim",
        "category": CATEGORY,
        "description": ("One weighted graph on five vertices has a minimum spanning tree of total weight 11. "
                        "Kruskal keeps edges in the order 1, 2, 3, 5 and skips two that close a cycle, while Prim "
                        "from vertex 0 picks 1, 2, 5, 3."),
        "keyIdea": MST_IDEA,
        "lesson": "algorithms/graph/minimum-spanning-trees.md",
        "height": 882,
        "draw": mst_kruskal_prim,
    },
    {
        "id": "max-flow-min-cut",
        "title": "Maximum flow and minimum cut",
        "category": CATEGORY,
        "description": ("A four-vertex flow network from source 0 to sink 3 carries a maximum flow of 5. The source "
                        "side {0, 1} is the set reachable in the residual graph, and the three edges leaving it "
                        "have capacities 2, 1 and 2, summing to 5."),
        "keyIdea": FLOW_IDEA,
        "lesson": "algorithms/graph/flows-and-matching.md",
        "height": 900,
        "draw": max_flow_min_cut,
    },
    {
        "id": "matching-augmenting-path",
        "title": "Augmenting a matching",
        "category": CATEGORY,
        "description": ("A bipartite matching of size 2 leaves left vertex 1 and right vertex 1 free. Flipping the "
                        "alternating path L1, R0, L0, R1 gives the maximum matching of size 3."),
        "keyIdea": MATCH_IDEA,
        "lesson": "algorithms/graph/flows-and-matching.md",
        "height": 888,
        "draw": matching_augmenting_path,
    },
    {
        "id": "lca-binary-lifting",
        "title": "Binary lifting for LCA",
        "category": CATEGORY,
        "description": ("In a seven-vertex tree rooted at 0, lca(6, 4) lifts vertex 6 by 2 to vertex 3, finds the "
                        "2^2 and 2^1 ancestors equal, jumps both vertices by 2^0 to 1 and 2, and returns their "
                        "parent 0, read from the table of 2^j-th ancestors."),
        "keyIdea": LCA_IDEA,
        "lesson": "algorithms/graph/tree-queries.md",
        "height": 892,
        "draw": lca_binary_lifting,
    },
    {
        "id": "tree-diameter",
        "title": "Tree diameter by two sweeps",
        "category": CATEGORY,
        "description": ("In a seven-vertex tree, a breadth-first sweep from vertex 0 ends farthest at 6, and a "
                        "second sweep from 6 ends farthest at 5, so the path 6, 3, 1, 0, 2, 5 of 5 edges is a "
                        "longest path."),
        "keyIdea": DIAMETER_IDEA,
        "lesson": "algorithms/graph/tree-queries.md",
        "height": 868,
        "draw": tree_diameter,
    },
]
