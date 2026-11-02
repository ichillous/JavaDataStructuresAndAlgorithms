"""Small deterministic SVG primitives shared by the educational figures (stdlib only)."""
from html import escape
import textwrap

PALETTES = {
    "light": {"paper": "#ffffff", "ink": "#1f2430", "muted": "#505b6b", "line": "#8994a5",
              "blue": "#2451b7", "soft": "#edf2fc", "note": "#fff3c4", "gold": "#8a5e00",
              "red": "#bd3026", "panel": "#f6f8fb"},
    "dark": {"paper": "#111827", "ink": "#eef2f8", "muted": "#bec9da", "line": "#8291aa",
             "blue": "#9cbcff", "soft": "#203455", "note": "#302a18", "gold": "#f4cf70",
             "red": "#ffaaa1", "panel": "#192333"},
}


class Canvas:
    """A 520-unit portrait canvas, deliberately readable in a narrow lesson column.

    Coordinates are SVG units. All regular labels are 20px; headings 26px.
    Methods accept semantic color names (ink, muted, line, blue, soft, note, gold, red, panel).
    """
    width = 520

    def __init__(self, figure_id, title, subtitle, height, theme="light"):
        self.id, self.title, self.height = figure_id, title, height
        self.colors = PALETTES[theme]
        self.parts = []
        self.rect(0, 0, self.width, height, fill="paper", stroke="line", radius=16)
        self.text(28, 42, title, size=26, weight="700")
        self.paragraph(28, 74, subtitle, width=43, size=20, color="muted")
        self.line(28, 118, 492, 118, color="line", width=1)

    def color(self, color):
        return self.colors.get(color, color)

    def text(self, x, y, text, size=20, color="ink", anchor="start", weight="400"):
        self.parts.append(f'<text x="{x}" y="{y}" font-size="{size}" fill="{self.color(color)}" '
                          f'text-anchor="{anchor}" font-weight="{weight}">{escape(str(text))}</text>')

    def paragraph(self, x, y, text, width=42, size=20, color="ink", line_height=27):
        lines = textwrap.wrap(str(text), width=width, break_long_words=False, break_on_hyphens=False)
        for index, line in enumerate(lines):
            self.text(x, y + index * line_height, line, size=size, color=color)
        return y + len(lines) * line_height

    def rect(self, x, y, width, height, fill="paper", stroke="line", radius=8, dashed=False):
        dash = ' stroke-dasharray="6 5"' if dashed else ''
        self.parts.append(f'<rect x="{x}" y="{y}" width="{width}" height="{height}" rx="{radius}" '
                          f'fill="{self.color(fill)}" stroke="{self.color(stroke)}" stroke-width="2"{dash}/>')

    def line(self, x1, y1, x2, y2, color="line", width=2, dashed=False, arrow=False):
        dash = ' stroke-dasharray="7 5"' if dashed else ''
        marker = f' marker-end="url(#{self.id}-{color}-arrow)"' if arrow else ''
        self.parts.append(f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" '
                          f'stroke="{self.color(color)}" stroke-width="{width}"{dash}{marker}/>')

    def arrow(self, x1, y1, x2, y2, color="blue", dashed=False):
        self.line(x1, y1, x2, y2, color=color, width=2.5, dashed=dashed, arrow=True)

    def path(self, d, color="line", width=2, dashed=False, arrow=False, fill="none"):
        dash = ' stroke-dasharray="7 5"' if dashed else ''
        marker = f' marker-end="url(#{self.id}-{color}-arrow)"' if arrow else ''
        self.parts.append(f'<path d="{d}" fill="{self.color(fill)}" stroke="{self.color(color)}" '
                          f'stroke-width="{width}"{dash}{marker}/>')

    def node(self, x, y, label, active=False, radius=27, color=None):
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


def load_fixtures(path):
    """The shared Java Properties fixture uses ASCII keys/values without escapes."""
    values = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#"):
            key, value = line.split("=", 1)
            values[key.strip()] = value.strip()
    return values


def ints(fixtures, key):
    value = fixtures[key]
    return [] if not value else [int(part) for part in value.split(",")]
