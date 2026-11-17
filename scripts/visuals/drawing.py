"""Small deterministic SVG primitives shared by the educational figures (stdlib only).

Every figure draws on a :class:`Canvas` 520 units wide. All text is at least 18px. The
canvas records an estimated box for every text and the lowest point of every shape, so
``test_layout.py`` can reject labels that collide, leave the canvas, or enter the bottom
margin. See docs/visuals/design.md ("Adding a figure") for the authoring guide.
"""
from html import escape
import math
from pathlib import Path
import re
import textwrap

PALETTES = {
    "light": {"paper": "#ffffff", "ink": "#1f2430", "muted": "#505b6b", "line": "#8994a5",
              "blue": "#2451b7", "soft": "#edf2fc", "note": "#fff3c4", "gold": "#8a5e00",
              "red": "#bd3026", "panel": "#f6f8fb",
              "green": "#1c6b3c", "mint": "#e3f4e8", "violet": "#6b3fb8", "lilac": "#f0eafb",
              "rose": "#fde9e7"},
    "dark": {"paper": "#111827", "ink": "#eef2f8", "muted": "#bec9da", "line": "#8291aa",
             "blue": "#9cbcff", "soft": "#203455", "note": "#302a18", "gold": "#f4cf70",
             "red": "#ffaaa1", "panel": "#192333",
             "green": "#86dca2", "mint": "#173527", "violet": "#cdb6ff", "lilac": "#2a2346",
             "rose": "#3d1f22"},
}

# State name -> (stroke, fill, text color, dashed outline). Every state must also be named
# in a label or a legend entry: color never carries meaning alone.
STATES = {
    "focus": ("blue", "soft", "blue", False),     # the element the step is about
    "good": ("green", "mint", "green", False),    # done, kept, copied, correct
    "alt": ("violet", "lilac", "violet", False),  # a second group or alternative
    "bad": ("red", "rose", "red", False),         # removed, rejected, invalid
    "warn": ("gold", "note", "gold", False),      # needs attention, pending
    "muted": ("line", "paper", "muted", True),    # empty, free or out of scope
}

# The fill that pairs with each accent color in chips.
CHIP_FILLS = {"blue": "soft", "green": "mint", "violet": "lilac", "red": "rose", "gold": "note",
              "muted": "panel", "ink": "panel"}

# Advance widths in em, rounded up to the next 0.05, of the wider of the regular and bold
# system-ui faces as Chromium renders them (measured with getComputedTextLength).
_WIDTH_CLASSES = (
    (0.30, " 'Iijl·"), (0.35, "!,.:;"), (0.40, "()/[\\]`t|"), (0.45, "1-fr"),
    (0.50, "^_{}²³–"), (0.60, "7\"*?FJLacekosvxz"),
    (0.65, "23569#+=EPTbdghnpquy~≤≥≠−"), (0.70, "048$&<>BRSZ×√"), (0.75, "ACDHKUVXY"),
    (0.80, "GNOQ"), (0.90, "w↕"), (0.95, "Mm←↑→↓⇒⇐"), (1.00, "—∞"), (1.05, "%@W…"),
    (1.35, "↔"))
_WIDTHS = {char: width for width, chars in _WIDTH_CLASSES for char in chars}
_DEFAULT_WIDTH = 0.70
# Per-weight factors calibrated against Chromium getBBox() widths of every text in the
# committed figures so the estimate is a little wider than the rendered text; see
# "Calibrating the layout guard" in docs/visuals/design.md before changing them.
TEXT_SCALE = {"400": 0.96, "700": 1.0}
# Vertical extent of a text box relative to its baseline, as getBBox() reports it.
ASCENT, DESCENT = 0.95, 0.25


def estimate_text_width(text, size, weight="400"):
    """A slightly conservative width, in SVG units, for one line of system-ui text."""
    text = str(text)
    if not text:
        return 0
    units = sum(_WIDTHS.get(char, _DEFAULT_WIDTH) for char in text)
    scale = TEXT_SCALE["700" if str(weight) in ("600", "700", "bold") else "400"]
    return units * float(size) * scale


def _path_points(d):
    """Absolute coordinates touched by an SVG path using M, L, H, V, Q, C, T, S and Z."""
    points, command, numbers, x, y = [], None, [], 0.0, 0.0
    tokens = re.findall(r"[A-Za-z]|-?\d*\.?\d+(?:e-?\d+)?", d)

    def flush():
        nonlocal x, y
        if command in "MLTQCS":
            for index in range(0, len(numbers) - 1, 2):
                x, y = numbers[index], numbers[index + 1]
                points.append((x, y))
        elif command == "H":
            for value in numbers:
                x = value
                points.append((x, y))
        elif command == "V":
            for value in numbers:
                y = value
                points.append((x, y))

    for token in tokens:
        if token.isalpha():
            if command is not None:
                flush()
            if token.islower():
                raise ValueError(f"Use absolute path commands only, not {token!r}")
            command, numbers = token.upper(), []
        else:
            numbers.append(float(token))
    if command is not None:
        flush()
    return points


class Canvas:
    """A 520-unit portrait canvas, deliberately readable in a narrow lesson column.

    Coordinates are SVG units with y growing downward. Regular labels are 20px, secondary
    labels 18px and headings 26px. Methods accept semantic color names (see PALETTES) or
    state names (see STATES) where documented. Content lives between x = 28 and x = 492,
    below the header rule at y = 118 and above ``height - 20``.
    """
    width = 520
    left, right = 28, 492

    def __init__(self, figure_id, title, subtitle, height, theme="light"):
        self.id, self.title, self.height, self.theme = figure_id, title, height, theme
        self.colors = PALETTES[theme]
        self.parts = []
        self.text_boxes = []   # (left, top, right, bottom, text) for every text element
        self.shape_bottoms = []  # (lowest y, description) for every shape after the frame
        self.sizes = []
        self.rect(0, 0, self.width, height, fill="paper", stroke="line", radius=16)
        self.shape_bottoms.clear()  # the frame itself fills the canvas by design
        self.text(28, 42, title, size=26, weight="700")
        self.paragraph(28, 74, subtitle, width=43, size=20, color="muted")
        self.line(28, 118, 492, 118, color="line", width=1)

    def color(self, color):
        return self.colors.get(color, color)

    # Original primitives: their output must not change.

    def text(self, x, y, text, size=20, color="ink", anchor="start", weight="400"):
        text = str(text)
        width = estimate_text_width(text, size, weight)
        left = x - {"start": 0, "middle": width / 2, "end": width}[anchor]
        self.text_boxes.append((left, y - ASCENT * size, left + width, y + DESCENT * size, text))
        self.sizes.append(size)
        self.parts.append(f'<text x="{x}" y="{y}" font-size="{size}" fill="{self.color(color)}" '
                          f'text-anchor="{anchor}" font-weight="{weight}">{escape(text)}</text>')

    def paragraph(self, x, y, text, width=42, size=20, color="ink", line_height=27):
        lines = textwrap.wrap(str(text), width=width, break_long_words=False, break_on_hyphens=False)
        for index, line in enumerate(lines):
            self.text(x, y + index * line_height, line, size=size, color=color)
        return y + len(lines) * line_height

    def rect(self, x, y, width, height, fill="paper", stroke="line", radius=8, dashed=False):
        self.shape_bottoms.append((y + height, f"rect at y={y} height={height}"))
        dash = ' stroke-dasharray="6 5"' if dashed else ''
        self.parts.append(f'<rect x="{x}" y="{y}" width="{width}" height="{height}" rx="{radius}" '
                          f'fill="{self.color(fill)}" stroke="{self.color(stroke)}" stroke-width="2"{dash}/>')

    def line(self, x1, y1, x2, y2, color="line", width=2, dashed=False, arrow=False):
        self.shape_bottoms.append((max(y1, y2), f"line from ({x1}, {y1}) to ({x2}, {y2})"))
        dash = ' stroke-dasharray="7 5"' if dashed else ''
        marker = f' marker-end="url(#{self.id}-{color}-arrow)"' if arrow else ''
        self.parts.append(f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" '
                          f'stroke="{self.color(color)}" stroke-width="{width}"{dash}{marker}/>')

    def arrow(self, x1, y1, x2, y2, color="blue", dashed=False):
        self.line(x1, y1, x2, y2, color=color, width=2.5, dashed=dashed, arrow=True)

    def path(self, d, color="line", width=2, dashed=False, arrow=False, fill="none"):
        points = _path_points(d)
        if points:
            self.shape_bottoms.append((max(py for _, py in points), f"path {d[:40]}"))
        dash = ' stroke-dasharray="7 5"' if dashed else ''
        marker = f' marker-end="url(#{self.id}-{color}-arrow)"' if arrow else ''
        self.parts.append(f'<path d="{d}" fill="{self.color(fill)}" stroke="{self.color(color)}" '
                          f'stroke-width="{width}"{dash}{marker}/>')

    def node(self, x, y, label, active=False, radius=27, color=None, state=None):
        """A circle with a centred bold label; ``state`` (see STATES) overrides active/color."""
        self.shape_bottoms.append((y + radius, f"node {label!r} at ({x}, {y})"))
        if state is not None:
            stroke, fill, ink, dashed = _state(state)
            dash = ' stroke-dasharray="6 5"' if dashed else ''
            self.parts.append(f'<circle cx="{x}" cy="{y}" r="{radius}" fill="{self.color(fill)}" '
                              f'stroke="{self.color(stroke)}" stroke-width="3"{dash}/>')
            self.text(x, y + 7, label, anchor="middle", weight="700", color=ink)
            return
        stroke = color or ("blue" if active else "line")
        self.parts.append(f'<circle cx="{x}" cy="{y}" r="{radius}" fill="{self.color("soft" if active else "paper")}" '
                          f'stroke="{self.color(stroke)}" stroke-width="{3 if active else 2}"/>')
        self.text(x, y + 7, label, anchor="middle", weight="700")

    def cells(self, x, y, values, cell=60, gap=10, active=(), labels=None):
        for index, value in enumerate(values):
            left = x + index * (cell + gap)
            self.rect(left, y, cell, 48, fill="soft" if index in active else "paper",
                      stroke="blue" if index in active else "line")
            self.text(left + cell / 2, y + 31, value, anchor="middle", weight="700" if index in active else "400")
            if labels is not None:
                self.text(left + cell / 2, y + 74, labels[index], anchor="middle", size=18, color="muted")

    def note(self, y, text):
        lines = textwrap.wrap(text, width=39, break_long_words=False, break_on_hyphens=False)
        self.rect(28, y, 464, 55 + 27 * len(lines), fill="note", stroke="gold")
        self.text(44, y + 31, "KEY IDEA", size=18, color="gold", weight="700")
        self.paragraph(44, y + 60, text, width=39, size=20)

    # Shared primitives for new figures. Signatures are a contract for every figure module.

    @staticmethod
    def note_height(text):
        """The height ``note`` uses for this text, so a figure can end exactly below it."""
        return 55 + 27 * len(textwrap.wrap(text, width=39, break_long_words=False, break_on_hyphens=False))

    @staticmethod
    def cell_center(x, index, cell=60, gap=10):
        """The x centre of cell ``index`` in a ``cell_row`` that starts at ``x``."""
        return x + index * (cell + gap) + cell / 2

    def step(self, y, number, text):
        """A numbered blue badge and a bold 20px heading on baseline ``y``.

        Returns the y where the step's content should start (22 units below the baseline).
        """
        self.parts.append(f'<circle cx="42" cy="{y - 7}" r="14" fill="{self.color("blue")}" '
                          f'stroke="{self.color("blue")}" stroke-width="2"/>')
        self.shape_bottoms.append((y + 7, f"step badge {number}"))
        self.text(42, y - 1, number, size=18, color="paper", anchor="middle", weight="700")
        self.text(66, y, text, weight="700")
        return y + 22

    def cell_row(self, x, y, values, *, cell=60, gap=10, height=48, states=None, labels=None):
        """A row of boxed values with optional per-cell states and 18px labels below.

        ``states`` is a list as long as ``values`` of ``None`` or a STATES name. Returns the
        y just below the row (below its labels when given).
        """
        if states is not None and len(states) != len(values):
            raise ValueError(f"cell_row has {len(values)} values but {len(states)} states")
        for index, value in enumerate(values):
            left = x + index * (cell + gap)
            state = states[index] if states else None
            if state is None:
                stroke, fill, ink, dashed, weight = "line", "paper", "ink", False, "400"
            else:
                stroke, fill, ink, dashed = _state(state)
                weight = "400" if state == "muted" else "700"
            self.rect(left, y, cell, height, fill=fill, stroke=stroke, dashed=dashed)
            if str(value) != "":
                self.text(left + cell / 2, y + height / 2 + 7, value, anchor="middle", weight=weight, color=ink)
            if labels is not None and str(labels[index]) != "":
                self.text(left + cell / 2, y + height + 26, labels[index], anchor="middle", size=18, color="muted")
        return y + height + (32 if labels is not None else 0)

    def grid(self, x, y, rows, *, cell_w=56, cell_h=44, row_labels=None, col_labels=None, states=None):
        """A table of values for DP tables and matrices, with its top-left cell at (x, y).

        Column labels sit above the grid and row labels to its left, both 18px muted.
        ``states`` maps ``(row, column)`` to a STATES name. Returns the y below the grid.
        """
        states = states or {}
        for (row, column), state in states.items():
            _state(state)
            if not (0 <= row < len(rows) and 0 <= column < len(rows[row])):
                raise ValueError(f"grid state at {(row, column)} is outside the grid")
        for column, label in enumerate(col_labels or []):
            self.text(x + column * cell_w + cell_w / 2, y - 12, label, anchor="middle", size=18, color="muted")
        for row, values in enumerate(rows):
            top = y + row * cell_h
            if row_labels is not None:
                self.text(x - 12, top + cell_h / 2 + 6, row_labels[row], anchor="end", size=18, color="muted")
            for column, value in enumerate(values):
                state = states.get((row, column))
                if state is None:
                    stroke, fill, ink, dashed, weight = "line", "paper", "ink", False, "400"
                else:
                    stroke, fill, ink, dashed = _state(state)
                    weight = "400" if state == "muted" else "700"
                self.rect(x + column * cell_w, top, cell_w, cell_h, fill=fill, stroke=stroke, radius=3,
                          dashed=dashed)
                if str(value) != "":
                    self.text(x + column * cell_w + cell_w / 2, top + cell_h / 2 + 7, value,
                              anchor="middle", weight=weight, color=ink)
        return y + len(rows) * cell_h

    def pointer(self, x, y, label, *, color="blue", below=True):
        """A short labelled arrow pointing at the edge at (x, y) from below or above.

        Returns the far edge of the label: about y + 64 below, or y - 62 above.
        """
        if below:
            self.arrow(x, y + 34, x, y + 6, color=color)
            self.text(x, y + 56, label, anchor="middle", size=18, color=color, weight="700")
            return y + 64
        self.arrow(x, y - 34, x, y - 6, color=color)
        self.text(x, y - 42, label, anchor="middle", size=18, color=color, weight="700")
        return y - 62

    def chip(self, x, y, text, *, color="blue"):
        """A rounded 30-unit-tall label with its top-left at (x, y); returns its width."""
        width = round(estimate_text_width(text, 18, "700") + 24)
        self.rect(x, y, width, 30, fill=CHIP_FILLS.get(color, "panel"), stroke=color, radius=15)
        self.text(x + width / 2, y + 21, text, anchor="middle", size=18, color=color, weight="700")
        return width

    def brace(self, x1, x2, y, label, *, color="muted", above=False):
        """A bracket spanning x1..x2 at height y with a centred 18px label.

        With ``above=False`` the bracket sits below a range and its label below it; with
        ``above=True`` it sits above a range and its label above it. Returns the label's far
        edge (y + 34 or y - 32).
        """
        x1, x2 = min(x1, x2), max(x1, x2)
        middle = (x1 + x2) / 2
        tip = -8 if above else 8      # toward the label
        arm = -tip                    # toward the range
        curl = min(8, (x2 - x1) / 4)
        self.path(f"M {x1} {y + arm} Q {x1} {y} {x1 + curl} {y} H {middle - curl} "
                  f"Q {middle} {y} {middle} {y + tip} Q {middle} {y} {middle + curl} {y} "
                  f"H {x2 - curl} Q {x2} {y} {x2} {y + arm}", color=color)
        if above:
            self.text(middle, y - 16, label, anchor="middle", size=18, color=color, weight="700")
            return y - 32
        self.text(middle, y + 28, label, anchor="middle", size=18, color=color, weight="700")
        return y + 34

    def curve(self, x1, y1, x2, y2, *, bend=40, color="blue", dashed=False, arrow=True, label=None):
        """A quadratic curve; positive ``bend`` bulges to the left of the direction of travel
        (upward for a left-to-right curve). An optional 18px label sits outside the bulge."""
        length = math.hypot(x2 - x1, y2 - y1) or 1
        nx, ny = (y2 - y1) / length, -(x2 - x1) / length
        cx, cy = (x1 + x2) / 2 + nx * bend * 2, (y1 + y2) / 2 + ny * bend * 2
        self.path(f"M {x1:g} {y1:g} Q {cx:g} {cy:g} {x2:g} {y2:g}", color=color, width=2.5,
                  dashed=dashed, arrow=arrow)
        if label is not None:
            apex_x, apex_y = (x1 + 2 * cx + x2) / 4, (y1 + 2 * cy + y2) / 4
            sign = 1 if bend >= 0 else -1
            lx, ly = apex_x + nx * sign * 14, apex_y + ny * sign * 14
            baseline = ly + (16 if ny * sign > 0.3 else -4 if ny * sign < -0.3 else 6)
            anchor = "middle" if abs(nx) < 0.5 else ("start" if nx * sign > 0 else "end")
            self.text(round(lx, 2), round(baseline, 2), label, anchor=anchor, size=18, color=color, weight="700")

    def edge(self, start, end, *, color="line", radius=27, dashed=False, label=None, directed=True):
        """A connector between two node centres, trimmed to both circles.

        ``directed`` adds an arrowhead at ``end``. A label (an edge weight, say) sits beside
        the midpoint, above a horizontal edge and to the left of a vertical one.
        """
        (sx, sy), (ex, ey) = start, end
        length = math.hypot(ex - sx, ey - sy)
        ux, uy = (ex - sx) / length, (ey - sy) / length
        end_trim = radius + (8 if directed else 4)
        points = (round(sx + ux * (radius + 4), 2), round(sy + uy * (radius + 4), 2),
                  round(ex - ux * end_trim, 2), round(ey - uy * end_trim, 2))
        if directed:
            self.line(*points, color=color, width=2.5, dashed=dashed, arrow=True)
        else:
            self.line(*points, color=color, width=2.5, dashed=dashed)
        if label is not None:
            nx, ny = uy, -ux
            if ny > 0 or (ny == 0 and nx > 0):
                nx, ny = -nx, -ny
            mx, my = (sx + ex) / 2 + nx * 16, (sy + ey) / 2 + ny * 16
            anchor = "middle" if abs(nx) < 0.5 else "end"
            self.text(round(mx, 2), round(my + (0 if abs(nx) < 0.5 else 6), 2), label, anchor=anchor,
                      size=18, color="ink" if color == "line" else color, weight="700")

    def legend(self, y, items):
        """Swatches with 18px labels for ``(state, text)`` pairs, wrapping onto new rows.

        ``y`` is the text baseline of the first row. Returns the y just below the last row.
        """
        x = self.left
        for state, label in items:
            stroke, fill, _, dashed = _state(state)
            width = 34 + estimate_text_width(label, 18) + 22
            if x > self.left and x + width - 22 > self.right:
                x, y = self.left, y + 32
            self.rect(x, y - 16, 26, 20, fill=fill, stroke=stroke, radius=4, dashed=dashed)
            self.text(x + 34, y, label, size=18)
            x += width
        return y + 10

    def panel(self, y, height):
        """The framed panel background used by existing figures, spanning the content width."""
        self.rect(28, y, 464, height, fill="panel")

    # Layout guard.

    def font_sizes(self):
        return list(self.sizes)

    def layout_issues(self, tolerance=2):
        """Messages for text outside the canvas, colliding text boxes, and drawing in the
        bottom 20 units. Boxes may touch or overlap by up to ``tolerance`` units."""
        where = f"{self.id} ({self.theme})"
        issues = []
        limit = self.height - 20
        for left, top, right, bottom, text in self.text_boxes:
            box = f"[{left:.0f}, {top:.0f}, {right:.0f}, {bottom:.0f}]"
            if left < 0 or top < 0 or right > self.width or bottom > self.height:
                issues.append(f"{where}: text {text!r} leaves the {self.width}x{self.height} canvas at {box}")
            elif bottom > limit:
                issues.append(f"{where}: text {text!r} enters the bottom margin below y={limit} at {box}")
        for bottom, description in self.shape_bottoms:
            if bottom > limit:
                issues.append(f"{where}: {description} reaches y={bottom:g}, inside the bottom margin below y={limit}")
        boxes = self.text_boxes
        for index, first in enumerate(boxes):
            for second in boxes[index + 1:]:
                overlap_x = min(first[2], second[2]) - max(first[0], second[0])
                overlap_y = min(first[3], second[3]) - max(first[1], second[1])
                if overlap_x > tolerance and overlap_y > tolerance:
                    issues.append(f"{where}: text {first[4]!r} overlaps text {second[4]!r} "
                                  f"by {overlap_x:.0f}x{overlap_y:.0f} units")
        return issues

    def svg(self, description):
        defs = []
        for color in self.colors:
            defs.append(f'<marker id="{self.id}-{color}-arrow" markerWidth="9" markerHeight="9" '
                        f'refX="8" refY="4" orient="auto" markerUnits="userSpaceOnUse">'
                        f'<path d="M 0 0 L 8 4 L 0 8 Z" fill="{self.color(color)}"/></marker>')
        return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 520 {self.height}" '
                f'width="520" height="{self.height}" role="img" aria-labelledby="title desc">\n'
                f'<title id="title">{escape(self.title)}</title>\n<desc id="desc">{escape(description)}</desc>\n'
                '<style>text{font-family:system-ui,-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif} '
                'line,path{stroke-linecap:round;stroke-linejoin:round}</style>\n'
                '<defs>' + ''.join(defs) + '</defs>\n' + '\n'.join(self.parts) + '\n</svg>\n')


def _state(name):
    try:
        return STATES[name]
    except KeyError:
        raise ValueError(f"Unknown state {name!r}; use one of {', '.join(STATES)}") from None


def load_fixtures(path):
    """Read one Java Properties fixture file of ASCII ``key=value`` lines without escapes."""
    values = {}
    for line in Path(path).read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#"):
            key, value = line.split("=", 1)
            values[key.strip()] = value.strip()
    return values


def load_all_fixtures(directory):
    """Merge ``examples.properties`` with every ``examples-<stem>.properties``, sorted by name.

    Keys in a family file must start with ``<stem>.``; a key defined twice fails with both
    file names, so parallel families can never silently override each other.
    """
    directory = Path(directory)
    merged, origin = {}, {}
    paths = [directory / "examples.properties"] + sorted(directory.glob("examples-*.properties"))
    for path in paths:
        stem = path.stem[len("examples-"):] if path.name != "examples.properties" else None
        for key, value in load_fixtures(path).items():
            if stem is not None and not key.startswith(stem + "."):
                raise ValueError(f"{path.name}: key {key!r} must start with {stem + '.'!r}")
            if key in merged:
                raise ValueError(f"Duplicate visual fixture key {key!r} in {origin[key]} and {path.name}")
            merged[key], origin[key] = value, path.name
    return merged


def ints(fixtures, key):
    value = fixtures[key]
    return [] if not value else [int(part) for part in value.split(",")]
