"""Overview maps for the folder landing pages; fixtures in examples-map.properties.

Each map is a column of cards, one per question (data structures) or task (algorithms). A
card shows a small pictogram of the thing it names, then the question and the family that
answers it, one public ``Class.method`` to start from and the lesson folders to open,
relative to the landing page. The card's text carries the meaning; the pictogram (no text,
palette colors only) is a visual cue. VisualExamplesMapTest checks every ``Class.method``
against the public API; test_assets.py checks every folder.
"""
from pathlib import Path

from drawing import Canvas, estimate_text_width, load_fixtures

FIXTURE_FILE = Path(__file__).resolve().parents[2] / "docs/visuals/data/examples-map.properties"

FIRST_ROW = 134     # top of the first card
ROW_GAP = 5
LINE_1, LINE_STEP, ROW_PAD = 26, 22, 10   # first baseline in a card, line step, padding below
ICON_X, ICON_W, ICON_H = 40, 56, 40        # pictogram box, vertically centred in its card
TEXT_X = 110         # card text starts right of the pictogram tile
RIGHT_X = 478        # right-aligned text ends here
TITLE_GAP = 12       # space between the question and its family on the first line
COLUMN_GAP = 14      # minimum space between left and right texts on one line


def rows(fixtures, prefix):
    """The cards of one map as dicts with name, label, family, api and folders (with a slash)."""
    names = [name.strip() for name in fixtures[f"{prefix}.rows"].split(",")]
    return [{"name": name, "label": fixtures[f"{prefix}.{name}.label"],
             "family": fixtures[f"{prefix}.{name}.family"], "api": fixtures[f"{prefix}.{name}.api"],
             "folders": [folder.strip() + "/" for folder in fixtures[f"{prefix}.{name}.folders"].split(",")]}
            for name in names]


def folder_lines(row):
    """The right-aligned folder column, one entry per card line ("" leaves a line empty).

    Folders fill the card's lines from the top: beside the question and family, then beside
    the method, then on lines of their own; neighbours share a line, joined by ' · ', when
    they fit. Raises ValueError when the question and family, or the method, cannot fit on
    one line, so a fixture edit can never make card text overlap.
    """
    width = RIGHT_X - TEXT_X
    first = estimate_text_width(row["label"], 20, "700") + TITLE_GAP + estimate_text_width(row["family"], 18)
    second = estimate_text_width(row["api"], 18, "700")
    if first > width or second > width:
        raise ValueError(f"map card {row['label']!r}: the question and family, or the method, do not fit")

    def room(index):
        return width - COLUMN_GAP - (first, second)[index] if index < 2 else width

    lines, current = [], ""
    for folder in row["folders"]:
        candidate = folder if not current else current + " · " + folder
        while not current and estimate_text_width(folder, 18) > room(len(lines)):
            lines.append("")  # nothing fits beside this line's text; start lower
        if current and estimate_text_width(candidate, 18) > room(len(lines)):
            lines.append(current)
            current = folder
            while estimate_text_width(folder, 18) > room(len(lines)):
                lines.append("")
        else:
            current = candidate
    lines.append(current)
    return lines + [""] * (2 - len(lines))


def card_height(row):
    return max(LINE_1 + LINE_STEP * (len(folder_lines(row)) - 1) + ROW_PAD, ICON_H + 16)


def note_top(row_list):
    return FIRST_ROW + sum(card_height(row) + ROW_GAP for row in row_list) + 8


def figure_height(row_list, idea):
    return note_top(row_list) + Canvas.note_height(idea) + 24


# Pictograms. Each draws inside a 60 x 40 box with its top-left at (x, y), with 2-unit
# strokes on whole-unit coordinates, using palette names only so both themes work.

def _dot(c, x, y, r, fill="paper", stroke="line", width=2):
    c.shape_bottoms.append((y + r, f"pictogram dot at ({x}, {y})"))
    c.parts.append(f'<circle cx="{x}" cy="{y}" r="{r}" fill="{c.color(fill)}" stroke="{c.color(stroke)}" '
                   f'stroke-width="{width}"/>')


def _head(c, x, y, direction, color="blue"):
    """A small solid arrowhead whose tip is at (x, y), pointing left, right, up or down."""
    shapes = {"right": f"M {x} {y} L {x - 6} {y - 4} L {x - 6} {y + 4} Z",
              "left": f"M {x} {y} L {x + 6} {y - 4} L {x + 6} {y + 4} Z",
              "down": f"M {x} {y} L {x - 4} {y - 6} L {x + 4} {y - 6} Z",
              "up": f"M {x} {y} L {x - 4} {y + 6} L {x + 4} {y + 6} Z"}
    c.path(shapes[direction], color=color, width=1, fill=color)


def _cell(c, x, y, w, h, state=None):
    if state == "dim":  # discarded: a faded outline
        c.shape_bottoms.append((y + h, f"pictogram cell at ({x}, {y})"))
        c.parts.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="2" fill="{c.color("panel")}" '
                       f'stroke="{c.color("line")}" stroke-width="2" opacity="0.45"/>')
        return
    fills = {None: ("paper", "line"), "on": ("blue", "blue"), "soft": ("soft", "blue")}
    fill, stroke = fills[state]
    c.rect(x, y, w, h, fill=fill, stroke=stroke, radius=2)


def _position(c, x, y):
    for index in range(4):
        _cell(c, x + 2 + index * 14, y + 18, 12, 18, "on" if index == 2 else None)
    c.line(x + 37, y + 2, x + 37, y + 9, color="blue", width=2)
    _head(c, x + 37, y + 15, "down")


def _arrival(c, x, y):
    for index in range(3):
        _cell(c, x + 16 + index * 10, y + 11, 8, 18, "soft" if index == 0 else None)
    c.line(x + 58, y + 20, x + 53, y + 20, color="blue", width=2)
    _head(c, x + 48, y + 20, "left")
    c.line(x + 13, y + 20, x + 6, y + 20, color="blue", width=2)
    _head(c, x + 1, y + 20, "left")


def _key(c, x, y):
    _dot(c, x + 6, y + 20, 5, fill="blue", stroke="blue")
    c.line(x + 12, y + 20, x + 24, y + 20, color="blue", width=2)
    _head(c, x + 30, y + 20, "right")
    for index in range(3):
        _cell(c, x + 33, y + 3 + index * 12, 25, 10, "soft" if index == 1 else None)


def _order(c, x, y):
    for (ax, ay), (bx, by) in (((28, 7), (14, 21)), ((28, 7), (42, 21)), ((14, 21), (24, 34))):
        c.line(x + ax, y + ay, x + bx, y + by, color="line", width=2)
    _dot(c, x + 28, y + 7, 5, fill="soft", stroke="blue")
    _dot(c, x + 14, y + 21, 5)
    _dot(c, x + 42, y + 21, 5)
    _dot(c, x + 24, y + 34, 4, fill="blue", stroke="blue")


def _priority(c, x, y):
    c.path(f"M {x + 30} {y + 4} L {x + 8} {y + 36} L {x + 52} {y + 36} Z", color="line", width=2, fill="panel")
    c.line(x + 28, y + 12, x + 19, y + 26, color="line", width=2)
    c.line(x + 32, y + 12, x + 41, y + 26, color="line", width=2)
    _dot(c, x + 30, y + 9, 6, fill="blue", stroke="blue")
    _dot(c, x + 17, y + 29, 5)
    _dot(c, x + 43, y + 29, 5)


def _prefix(c, x, y):
    c.line(x + 8, y + 20, x + 24, y + 9, color="blue", width=2)
    c.line(x + 8, y + 20, x + 24, y + 31, color="line", width=2)
    for end in (3, 20, 37):
        c.line(x + 24, y + 9, x + 50, y + end, color="blue" if end == 20 else "line", width=2)
    _dot(c, x + 6, y + 20, 4, fill="blue", stroke="blue")
    _dot(c, x + 24, y + 9, 4, fill="blue", stroke="blue")
    _dot(c, x + 24, y + 31, 4)
    for end in (3, 20, 37):
        _dot(c, x + 52, min(max(end, 4), 36) + y, 3, fill="blue" if end == 20 else "paper",
             stroke="blue" if end == 20 else "line")


def _range(c, x, y):
    for index in range(6):
        _cell(c, x + 2 + index * 9, y + 20, 7, 16, "soft" if 1 <= index <= 3 else None)
    c.path(f"M {x + 11} {y + 14} L {x + 11} {y + 8} L {x + 36} {y + 8} L {x + 36} {y + 14}", color="blue", width=2)
    c.line(x + 23, y + 8, x + 23, y + 3, color="blue", width=2)


def _connectivity(c, x, y):
    for a, b in (((8, 12), (22, 30)), ((8, 12), (26, 8)), ((26, 8), (22, 30))):
        c.line(x + a[0], y + a[1], x + b[0], y + b[1], color="blue", width=2)
    for px, py in ((8, 12), (26, 8), (22, 30)):
        _dot(c, x + px, y + py, 4, fill="blue", stroke="blue")
    c.line(x + 42, y + 14, x + 52, y + 30, color="line", width=2)
    _dot(c, x + 42, y + 14, 4)
    _dot(c, x + 52, y + 30, 4)


def _approximate(c, x, y):
    for index in range(8):
        _cell(c, x + 1 + index * 7, y + 13, 6, 14, "on" if index in (1, 4, 6) else None)


def _search(c, x, y):
    for index in range(7):
        state = "dim" if index < 3 else "on" if index == 3 else None
        _cell(c, x + 1 + index * 8, y + 18, 6, 18, state)
    c.line(x + 28, y + 2, x + 28, y + 8, color="blue", width=2)
    _head(c, x + 28, y + 14, "down")


def _bars(c, x, y, heights, highlight=None, rising=False):
    for index, height in enumerate(heights):
        state = "on" if index == highlight else "soft" if rising else None
        _cell(c, x + 4 + index * 11, y + 37 - height, 8, height, state)


def _sort(c, x, y):
    _bars(c, x, y, [8, 14, 20, 26, 32], rising=True)


def _select(c, x, y):
    _bars(c, x, y, [20, 8, 30, 14, 24], highlight=3)


GRAPH = {0: (6, 20), 1: (22, 6), 2: (22, 34), 3: (40, 20), 4: (54, 6)}


def _graph(c, x, y, edges, strong=(), rings=(), filled=()):
    for a, b in edges:
        (ax, ay), (bx, by) = GRAPH[a], GRAPH[b]
        key = (a, b) in strong or (b, a) in strong
        c.line(x + ax, y + ay, x + bx, y + by, color="blue" if key else "line", width=3 if key else 2)
    for vertex, (px, py) in GRAPH.items():
        if vertex in rings:
            _dot(c, x + px, y + py, 6, fill="none", stroke="blue", width=2)
        on = vertex in filled
        _dot(c, x + px, y + py, 3, fill="blue" if on else "paper", stroke="blue" if on else "line")


EDGES = ((0, 1), (0, 2), (1, 3), (2, 3), (3, 4), (1, 4))


def _traverse(c, x, y):
    _graph(c, x, y, EDGES, rings=(0, 1, 2), filled=(0, 1, 2))


def _shortest(c, x, y):
    _graph(c, x, y, EDGES, strong=((0, 2), (2, 3), (3, 4)), filled=(0, 2, 3, 4))


def _connect(c, x, y):
    corners = [(10, 6), (50, 6), (10, 34), (50, 34)]
    c.line(x + 10, y + 34, x + 50, y + 6, color="line", width=2, dashed=True)
    for a, b in ((0, 1), (0, 2), (2, 3)):
        (ax, ay), (bx, by) = corners[a], corners[b]
        c.line(x + ax, y + ay, x + bx, y + by, color="blue", width=3)
    for px, py in corners:
        _dot(c, x + px, y + py, 4, fill="blue", stroke="blue")


def _optimize(c, x, y):
    path = {(0, 0): "soft", (1, 0): "soft", (1, 1): "soft", (2, 1): "soft", (2, 2): "on"}
    for row in range(3):
        for column in range(3):
            _cell(c, x + 15 + column * 11, y + 4 + row * 11, 9, 9, path.get((row, column)))


def _text(c, x, y):
    for index in range(8):
        _cell(c, x + 1 + index * 7, y + 13, 6, 14, "soft" if 3 <= index <= 5 else None)
    c.rect(x + 20, y + 9, 23, 22, fill="none", stroke="blue", radius=3)


def _compute(c, x, y):
    _dot(c, x + 30, y + 20, 16, fill="paper", stroke="line")
    for dx, dy in ((0, -1), (0.866, -0.5), (0.866, 0.5), (0, 1), (-0.866, 0.5), (-0.866, -0.5)):
        c.line(round(x + 30 + dx * 11, 1), round(y + 20 + dy * 11, 1), round(x + 30 + dx * 15, 1),
               round(y + 20 + dy * 15, 1), color="muted", width=2)
    c.line(x + 30, y + 20, x + 38, y + 12, color="blue", width=2)
    _dot(c, x + 30, y + 20, 3, fill="blue", stroke="blue")


PICTOGRAMS = {"position": _position, "arrival": _arrival, "key": _key, "order": _order, "priority": _priority,
              "prefix": _prefix, "range": _range, "connectivity": _connectivity, "approximate": _approximate,
              "search": _search, "sort": _sort, "select": _select, "traverse": _traverse,
              "shortest": _shortest, "connect": _connect, "optimize": _optimize, "text": _text,
              "compute": _compute}


def draw_map(c, row_list, idea):
    y = FIRST_ROW
    for row in row_list:
        height = card_height(row)
        c.rect(28, y, 464, height, fill="panel", stroke="line", radius=10)
        icon_y = y + (height - ICON_H) // 2
        c.rect(ICON_X - 4, icon_y - 2, ICON_W + 8, ICON_H + 4, fill="paper", stroke="line", radius=8)
        PICTOGRAMS[row["name"]](c, ICON_X, icon_y)
        c.text(TEXT_X, y + LINE_1, row["label"], size=20, color="blue", weight="700")
        family_x = TEXT_X + round(estimate_text_width(row["label"], 20, "700")) + TITLE_GAP
        c.text(family_x, y + LINE_1, row["family"], size=18)
        c.text(TEXT_X, y + LINE_1 + LINE_STEP, row["api"], size=18, weight="700")
        for index, line in enumerate(folder_lines(row)):
            if line:
                c.text(RIGHT_X, y + LINE_1 + LINE_STEP * index, line, size=18, color="muted", anchor="end")
        y += height + ROW_GAP
    c.note(note_top(row_list), idea)


def describe(row_list, opening):
    cards = "; ".join(f'{row["label"].lower()}, {row["api"]}, {" and ".join(row["folders"])}' for row in row_list)
    return (f"{opening} Each card shows a small pictogram, names the family, a method to start with and "
            f"the folder to open: {cards}.")


_ROWS = load_fixtures(FIXTURE_FILE)
STRUCTURES = rows(_ROWS, "map.structures")
ALGORITHMS = rows(_ROWS, "map.algorithms")
STRUCTURES_IDEA = "Pick by the question you must answer."
ALGORITHMS_IDEA = "Pick by the task you must perform."


def structures_map(c, fixtures):
    draw_map(c, rows(fixtures, "map.structures"), STRUCTURES_IDEA)


def algorithms_map(c, fixtures):
    draw_map(c, rows(fixtures, "map.algorithms"), ALGORITHMS_IDEA)


FIGURES = [
    {"id": "data-structures-map", "title": "Data structures by question", "category": "Maps",
     "subtitle": "Maps · Question, family, method, folder",
     "description": describe(STRUCTURES, f"{len(STRUCTURES)} cards sort the structures by the question they answer."),
     "keyIdea": STRUCTURES_IDEA, "lesson": "data-structures/README.md",
     "height": figure_height(STRUCTURES, STRUCTURES_IDEA), "draw": structures_map},
    {"id": "algorithms-map", "title": "Algorithms by task", "category": "Maps",
     "subtitle": "Maps · Task, family, method, folder",
     "description": describe(ALGORITHMS, f"{len(ALGORITHMS)} cards sort the algorithms by the task they perform."),
     "keyIdea": ALGORITHMS_IDEA, "lesson": "algorithms/README.md",
     "height": figure_height(ALGORITHMS, ALGORITHMS_IDEA), "draw": algorithms_map},
]
