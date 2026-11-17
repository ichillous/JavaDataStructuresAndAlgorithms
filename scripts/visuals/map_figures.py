"""Overview maps for the folder landing pages; fixtures in examples-map.properties.

Each map is a column of cards, one per question (data structures) or task (algorithms). A
card names the question, the family that answers it, one public ``Class.method`` to start
from and the lesson folders to open, relative to the landing page. VisualExamplesMapTest
checks every ``Class.method`` against the public API; test_assets.py checks every folder.
"""
from pathlib import Path

from drawing import Canvas, estimate_text_width, load_fixtures

FIXTURE_FILE = Path(__file__).resolve().parents[2] / "docs/visuals/data/examples-map.properties"

CAPTION_Y = 146     # baseline of the column captions
FIRST_ROW = 160     # top of the first card
ROW_GAP = 6
LINE_1, LINE_STEP, ROW_PAD = 26, 23, 9    # first baseline in a card, line step, padding below
LEFT_X = 50          # text starts right of the accent bar
RIGHT_X = 476        # right-aligned text ends here
COLUMN_GAP = 24      # minimum space between the left and right texts of one line


def rows(fixtures, prefix):
    """The cards of one map as dicts with label, family, api and folders (with a slash)."""
    names = [name.strip() for name in fixtures[f"{prefix}.rows"].split(",")]
    return [{"label": fixtures[f"{prefix}.{name}.label"], "family": fixtures[f"{prefix}.{name}.family"],
             "api": fixtures[f"{prefix}.{name}.api"],
             "folders": [folder.strip() + "/" for folder in fixtures[f"{prefix}.{name}.folders"].split(",")]}
            for name in names]


def folder_lines(row):
    """Folders joined by ' · '; the first line must clear the family, later lines wrap."""
    room = RIGHT_X - LEFT_X - estimate_text_width(row["family"], 18) - COLUMN_GAP
    lines, current = [], ""
    for folder in row["folders"]:
        candidate = folder if not current else current + " · " + folder
        if current and estimate_text_width(candidate, 18) > (room if not lines else RIGHT_X - LEFT_X):
            lines.append(current)
            current = folder
        else:
            current = candidate
    lines.append(current)
    if estimate_text_width(lines[0], 18) > room:
        raise ValueError(f"map card {row['label']!r}: folder {lines[0]!r} does not fit beside {row['family']!r}")
    return lines


def card_height(row):
    return LINE_1 + LINE_STEP * len(folder_lines(row)) + ROW_PAD


def note_top(row_list):
    return FIRST_ROW + sum(card_height(row) + ROW_GAP for row in row_list) + 8


def figure_height(row_list, idea):
    return note_top(row_list) + Canvas.note_height(idea) + 24


def draw_map(c, row_list, left_caption, idea):
    c.text(LEFT_X, CAPTION_Y, left_caption, size=18, color="muted")
    c.text(RIGHT_X, CAPTION_Y, "Start with · folder", size=18, color="muted", anchor="end")
    y = FIRST_ROW
    for row in row_list:
        height = card_height(row)
        c.rect(28, y, 464, height, fill="panel", stroke="line", radius=10)
        c.rect(36, y + 10, 4, height - 20, fill="blue", stroke="blue", radius=2)
        c.text(LEFT_X, y + LINE_1, row["label"], size=20, color="blue", weight="700")
        c.text(RIGHT_X, y + LINE_1, row["api"], size=18, weight="700", anchor="end")
        c.text(LEFT_X, y + LINE_1 + LINE_STEP, row["family"], size=18)
        for index, line in enumerate(folder_lines(row)):
            c.text(RIGHT_X, y + LINE_1 + LINE_STEP * (index + 1), line, size=18, color="muted", anchor="end")
        y += height + ROW_GAP
    c.note(note_top(row_list), idea)


def describe(row_list, opening):
    cards = "; ".join(f'{row["label"].lower()}, {row["api"]}, {" and ".join(row["folders"])}' for row in row_list)
    return f"{opening} Each card names the family, a method to start with and the folder to open: {cards}."


_ROWS = load_fixtures(FIXTURE_FILE)
STRUCTURES = rows(_ROWS, "map.structures")
ALGORITHMS = rows(_ROWS, "map.algorithms")
STRUCTURES_IDEA = "Pick by the question you must answer."
ALGORITHMS_IDEA = "Pick by the task you must perform."


def structures_map(c, fixtures):
    draw_map(c, rows(fixtures, "map.structures"), "Question · family", STRUCTURES_IDEA)


def algorithms_map(c, fixtures):
    draw_map(c, rows(fixtures, "map.algorithms"), "Task · family", ALGORITHMS_IDEA)


FIGURES = [
    {"id": "data-structures-map", "title": "Data structures by question", "category": "Maps",
     "subtitle": "Maps · Ask a question, open a folder",
     "description": describe(STRUCTURES, f"{len(STRUCTURES)} cards sort the structures by the question they answer."),
     "keyIdea": STRUCTURES_IDEA, "lesson": "data-structures/README.md",
     "height": figure_height(STRUCTURES, STRUCTURES_IDEA), "draw": structures_map},
    {"id": "algorithms-map", "title": "Algorithms by task", "category": "Maps",
     "subtitle": "Maps · Name the task, open a folder",
     "description": describe(ALGORITHMS, f"{len(ALGORITHMS)} cards sort the algorithms by the task they perform."),
     "keyIdea": ALGORITHMS_IDEA, "lesson": "algorithms/README.md",
     "height": figure_height(ALGORITHMS, ALGORITHMS_IDEA), "draw": algorithms_map},
]
