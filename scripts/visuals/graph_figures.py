"""Fixture-backed graph lessons, drawn as readable portrait SVG figures."""

from collections import deque
from math import hypot

from drawing import ints


def _tuples(fixtures, key):
    value = fixtures[key]
    return [] if not value else [tuple(int(part) for part in item.split(":")) for item in value.split(",")]


def _edge(c, start, end, *, color="line", width=2, dashed=False, directed=True):
    """Keep both the line and its arrowhead clear of the vertex circles."""
    dx, dy = end[0] - start[0], end[1] - start[1]
    length = hypot(dx, dy)
    ux, uy = dx / length, dy / length
    c.line(start[0] + ux * 32, start[1] + uy * 32,
           end[0] - ux * 35, end[1] - uy * 35,
           color=color, width=width, dashed=dashed, arrow=directed)


def _weight(c, x, y, label, color="ink"):
    width = max(58, len(label) * 11 + 14)
    c.rect(x - width / 2, y - 22, width, 30, fill="paper", stroke="paper", radius=4)
    c.text(x, y, label, anchor="middle", color=color, weight="700")


def _path_text(vertices):
    return " → ".join(str(vertex) for vertex in vertices)


def _queue_after(fixtures, processed_count):
    adjacency = [[] for _ in range(int(fixtures["bfs.vertices"]))]
    for source, target in _tuples(fixtures, "bfs.edges"):
        adjacency[source].append(target)
        adjacency[target].append(source)
    source = ints(fixtures, "bfs.order")[0]
    queue, discovered, processed = deque([source]), {source}, []
    while len(processed) < processed_count:
        current = queue.popleft()
        processed.append(current)
        for neighbor in adjacency[current]:
            if neighbor not in discovered:
                discovered.add(neighbor)
                queue.append(neighbor)
    return processed, list(queue)


def draw_bfs(c, fixtures):
    c.text(28, 153, "UNDIRECTED GRAPH · source 0", weight="700")
    positions = {0: (280, 215), 1: (180, 335), 2: (400, 335),
                 3: (180, 455), 4: (400, 455), 5: (70, 215)}
    shortest_path = ints(fixtures, "bfs.path")
    path_edges = set(zip(shortest_path, shortest_path[1:]))
    for source, target in _tuples(fixtures, "bfs.edges"):
        selected = (source, target) in path_edges
        _edge(c, positions[source], positions[target], color="blue" if selected else "line",
              width=4 if selected else 2, dashed=not selected, directed=False)
    for vertex in range(int(fixtures["bfs.vertices"])):
        c.node(*positions[vertex], vertex, active=vertex in shortest_path)
    c.text(70, 265, "isolated", anchor="middle", color="muted")
    c.text(28, 510, "Thick lines: path " + _path_text(shortest_path), color="blue")

    processed, queue = _queue_after(fixtures, 2)
    c.rect(28, 540, 464, 174, fill="panel")
    c.text(44, 573, "After processing vertex " + str(processed[-1]), weight="700")
    c.text(44, 625, "Queue front → back")
    c.cells(285, 596, queue, cell=72, gap=12, active=(0,))
    c.text(44, 674, "2 is next; 3 is discovered, still queued.")

    c.text(28, 755, "Discovery / enqueue order", weight="700")
    c.cells(28, 774, ints(fixtures, "bfs.order"), cell=76, gap=16)
    c.text(28, 866, "Processing order: " + _path_text(ints(fixtures, "bfs.order")))
    c.note(904, "Mark a vertex when it enters the queue. Discovering it does not mean it has been processed; vertex 5 is never reached.")


def draw_dijkstra(c, fixtures):
    c.text(28, 153, "DIRECTED · weights are labeled w=number", weight="700")
    positions = {0: (90, 215), 1: (420, 215), 2: (90, 410),
                 3: (420, 410), 4: (260, 490)}
    labels = {(0, 1): (255, 202), (0, 2): (90, 320),
              (2, 1): (255, 315), (1, 3): (420, 320)}
    path = ints(fixtures, "dijkstra.path")
    path_edges = set(zip(path, path[1:]))
    for source, target, weight in _tuples(fixtures, "dijkstra.edges"):
        selected = (source, target) in path_edges
        _edge(c, positions[source], positions[target], color="blue" if selected else "line",
              width=4 if selected else 2, dashed=not selected)
        _weight(c, *labels[source, target], "w=" + str(weight), color="blue" if selected else "ink")
    for vertex in range(int(fixtures["dijkstra.vertices"])):
        c.node(*positions[vertex], vertex, active=vertex in path)
    c.text(260, 540, "unreachable", anchor="middle", color="muted")
    c.text(28, 585, "Thick solid = path; dashed = alternative.")
    c.text(28, 630, "Shortest distance from 0", weight="700")
    distances = ["none" if value == "unreachable" else value
                 for value in fixtures["dijkstra.distances"].split(",")]
    c.cells(28, 650, distances, cell=76, gap=16,
            labels=["v" + str(vertex) for vertex in range(int(fixtures["dijkstra.vertices"]))])
    c.text(28, 778, "Path: " + _path_text(path), color="blue", weight="700")
    weights = {(source, target): weight for source, target, weight in _tuples(fixtures, "dijkstra.edges")}
    path_weights = [weights[edge] for edge in zip(path, path[1:])]
    c.text(28, 817, "Cost: " + " + ".join(str(weight) for weight in path_weights)
           + " = " + str(sum(path_weights)))
    c.note(855, "Nonnegative weights let Dijkstra settle the smallest tentative distance. Vertex 4 has no path; absence is not a numeric distance.")


def draw_bellman(c, fixtures):
    c.text(28, 153, "Original edges solid; added edge dashed.", weight="700")
    positions = {0: (80, 235), 1: (410, 235), 2: (245, 435)}
    labels = {(0, 1): (245, 222), (0, 2): (145, 350), (1, 2): (340, 350)}
    path = ints(fixtures, "bellman.path")
    path_edges = set(zip(path, path[1:]))
    edges = _tuples(fixtures, "bellman.edges")
    for source, target, weight in edges:
        selected = (source, target) in path_edges
        _edge(c, positions[source], positions[target], color="blue" if selected else "line",
              width=4 if selected else 2)
        _weight(c, *labels[source, target], "w=" + str(weight), color="blue" if selected else "ink")

    cycle_source, cycle_target, cycle_weight = _tuples(fixtures, "bellman.cycleEdge")[0]
    # This separate, curved return edge cannot obscure the original 1 -> 2 arrow.
    c.path("M 278 447 C 467 505 510 310 444 245", color="red", width=3, dashed=True, arrow=True)
    _weight(c, 418, 430, "added w=" + str(cycle_weight), color="red")
    for vertex in range(int(fixtures["bellman.vertices"])):
        c.node(*positions[vertex], vertex, active=vertex in path)

    c.text(28, 520, "Distances before adding the extra edge:", weight="700")
    c.cells(28, 541, fixtures["bellman.distances"].split(","), cell=120, gap=28,
            labels=["v" + str(vertex) for vertex in range(int(fixtures["bellman.vertices"]))])
    weights = {(source, target): weight for source, target, weight in edges}
    path_weights = [weights[edge] for edge in zip(path, path[1:])]
    c.text(28, 672, "Best path: " + _path_text(path), color="blue", weight="700")
    c.text(28, 711, "Cost: " + " + ".join("(" + str(weight) + ")" if weight < 0 else str(weight)
                                         for weight in path_weights) + " = " + str(sum(path_weights)))
    cycle_forward_weight = weights[cycle_target, cycle_source]
    c.text(28, 767, "Add dashed " + str(cycle_source) + " → " + str(cycle_target)
           + " with w=" + str(cycle_weight) + ".", color="red")
    c.text(28, 806, "Cycle 1 → 2 → 1 costs " + str(cycle_forward_weight)
           + " + " + str(cycle_weight) + " = " + str(cycle_forward_weight + cycle_weight) + ".")
    c.text(28, 845, "Targets 1 and 2: no finite minimum.", color="red")
    c.note(885, "Source 0 keeps distance 0. The reachable negative cycle makes targets 1 and 2 unbounded, so this API rejects the entire result.")


def draw_scc_bridges(c, fixtures):
    c.text(28, 153, "A. DIRECTED: strong components", weight="700")
    c.rect(40, 180, 265, 124, fill="soft", stroke="blue")
    c.rect(190, 350, 290, 124, fill="panel", stroke="gold")
    positions = {0: (90, 235), 1: (245, 235), 2: (245, 405), 3: (420, 405)}
    routes = {(0, 1): "M 120 223 Q 167 180 215 223",
              (1, 0): "M 215 247 Q 167 290 120 247",
              (2, 3): "M 275 393 Q 332 350 390 393",
              (3, 2): "M 390 417 Q 332 460 275 417"}
    for source, target in _tuples(fixtures, "scc.edges"):
        if (source, target) in routes:
            c.path(routes[source, target], color="blue", arrow=True)
        else:
            _edge(c, positions[source], positions[target])
    for vertex in range(int(fixtures["scc.vertices"])):
        c.node(*positions[vertex], vertex, active=True)
    groups = [list(group) for group in _tuples(fixtures, "scc.groups")]
    c.text(54, 291, "SCC " + str(groups[0]), size=18, color="blue", weight="700")
    c.text(204, 461, "SCC " + str(groups[1]), size=18, color="gold", weight="700")
    c.text(299, 327, "one-way", size=18, color="muted")
    c.text(28, 520, "Canonical groups: " + ", ".join(str(group) for group in groups))
    c.text(28, 558, "Across groups, 1 → 2 has no return path.")

    c.line(28, 592, 492, 592, color="line", width=1)
    c.text(28, 632, "B. UNDIRECTED: an essential edge", weight="700")
    positions = {0: (90, 705), 1: (255, 705), 2: (180, 865), 3: (420, 865)}
    bridges = {tuple(sorted(edge)) for edge in _tuples(fixtures, "bridges.result")}
    for source, target in _tuples(fixtures, "bridges.edges"):
        bridge = tuple(sorted((source, target))) in bridges
        _edge(c, positions[source], positions[target], directed=False,
              color="blue" if bridge else "line", width=4 if bridge else 2, dashed=bridge)
    for vertex in range(int(fixtures["bridges.vertices"])):
        c.node(*positions[vertex], vertex)
    bridge_labels = [str(first) + "—" + str(second) for first, second in sorted(bridges)]
    c.text(28, 932, "Dashed " + ", ".join(bridge_labels) + " is the only bridge.", color="blue")
    c.text(28, 970, "Removing it isolates vertex 3.")
    c.note(1010, "SCCs need directed return paths. A bridge is an undirected edge whose removal splits a connected component.")


FIGURES = [
    {"id": "bfs-frontier", "title": "BFS: discovered vs processed", "category": "Graphs",
     "description": "Follow the queue separately from discovery; isolate unreachable vertex 5.",
     "lesson": "algorithms/graph/bfs.md",
     "keyIdea": "Discovery happens on enqueue, before processing; isolated vertices stay unreachable.",
     "height": 1110, "draw": draw_bfs},
    {"id": "dijkstra-path", "title": "Dijkstra: distance and path", "category": "Graphs",
     "description": "Compare weighted routes from source 0 and distinguish unreachable vertex 4.",
     "lesson": "algorithms/graph/bfs.md",
     "keyIdea": "Choose the cheapest weighted path; absence is separate from every numeric distance.",
     "height": 1070, "draw": draw_dijkstra},
    {"id": "bellman-negative", "title": "Negative edge or cycle?", "category": "Graphs",
     "description": "A negative edge yields -3; an added cycle makes targets 1 and 2 unbounded.",
     "lesson": "algorithms/graph/bfs.md",
     "keyIdea": "A negative edge can be safe; this API rejects any reachable negative cycle.",
     "height": 1100, "draw": draw_bellman},
    {"id": "scc-bridges", "title": "SCCs and bridges differ", "category": "Graphs",
     "description": "Directed return paths form SCCs; undirected essential edges are bridges.",
     "lesson": "algorithms/graph/dfs.md",
     "keyIdea": "SCCs test directed mutual reachability; bridges test an undirected edge's necessity.",
     "height": 1210, "draw": draw_scc_bridges},
]
