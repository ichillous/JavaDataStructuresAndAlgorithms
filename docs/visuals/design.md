# Figure design and provenance

The user requested Emergent Mind guidance. No named Emergent Mind skill package
was installed in the connected cloud skill catalog, executor catalog, or local
workspace skill locations. These figures instead adapt the actual public
[Textbook Figure](https://www.emergentmind.com/chart-prompts/textbook-figure)
and [Whiteboard](https://www.emergentmind.com/chart-prompts/whiteboard) chart prompts,
found through the [Emergent Mind chart gallery](https://www.emergentmind.com/chart-prompts).
This is an attributed design adaptation, not a claim that an installed skill ran.

The textbook approach informed numbered figures, restrained blue emphasis, an
explicit key idea and a caption that preserves the example's meaning. The
whiteboard approach informed direct labels and deliberate state-to-state arrows.
We omitted decorative board textures and external fonts to keep figures crisp,
portable and usable offline. System fonts replace web-font dependencies.

## Meaning comes before styling

- Every operation uses a canonical fixture checked against the Java APIs. A
  conceptual picture of a valid state does not promise private implementation
  details unless the lesson explicitly identifies that representation.
- Arrows distinguish directed graph edges, data movement, parent relationships
  and ordered steps through nearby labels. A color alone never identifies state.
- Light and dark SVGs use explicit backgrounds. Text colors are checked against
  their actual fills, with at least 4.5:1 contrast, including note labels.
- Figures use a narrow portrait composition and large labels. At small widths,
  a reader can also open an SVG directly for unrestricted zoom.
- Complexity curves are chosen mathematical functions on a stated common scale.
  They contain no measured runtime, machine comparison or speedup claim.

## Reproduce and change a figure

Edit a figure module under `scripts/visuals/` or the shared fixtures in
`data/examples.properties`, then run from the repository root:

```sh
python3 scripts/generate_visuals.py
python3 scripts/generate_visuals.py --check
python3 -m unittest discover -s scripts/visuals -p 'test_*.py'
./mvnw clean verify
```

The generator uses only Python's standard library. Java tests read the same
properties to check arrays, queue order, cache eviction, trees, string matches,
DP states and graph results. Browser trace builders derive bounded steps from
those inputs. Static checks verify SVGs, links, generated data and frame invariants;
actual browser review checks text bounds, keyboard controls and narrow layouts.

The generator prints the current figure and SVG counts, and the
[visual index](README.md) lists every figure with its lesson. Do not edit generated
SVGs, the manifest, visual data script, visual index or gallery bodies by
hand. Edit their source and regenerate. See [verification](verification.md) for
the tested coverage, preview images and practical limits.

## Adding a figure

A figure belongs to a family. A family has a **stem** (lowercase letters, digits and
underscores, such as `linear_more`) and owns three files:

| File | Contents |
| --- | --- |
| `scripts/visuals/<stem>_figures.py` | The drawing functions and a `FIGURES` list |
| `docs/visuals/data/examples-<stem>.properties` | The canonical example; every key starts with `<stem>.` |
| `src/test/java/io/github/ichillous/dsa/visuals/VisualExamples<Stem>Test.java` | A JUnit test that checks every displayed value against the library, using `VisualFixtures` |

`<Stem>` is the stem in UpperCamelCase without underscores: `linear_more` becomes
`VisualExamplesLinearMoreTest`. The exemplar for all three is `dynamic-array-growth` in
[`linear_more_figures.py`](../../scripts/visuals/linear_more_figures.py), with
[`examples-linear_more.properties`](data/examples-linear_more.properties) and
[`VisualExamplesLinearMoreTest`](../../src/test/java/io/github/ichillous/dsa/visuals/VisualExamplesLinearMoreTest.java).
Read it before you start, and copy its structure.

### How the generator finds figures and fixtures

- **Modules.** `linear_figures`, `tree_figures`, `algorithm_figures`, `graph_figures` and
  `complexity_figures` load first, in that order, so figures 01 to 26 keep their
  numbers. Every other `scripts/visuals/*_figures.py` follows, sorted by file name.
  Figure numbers therefore change when another family is added: never write a figure
  number in lesson prose. A repeated figure id fails the build.
- **`FIGURES` entries** are dicts with the keys `id` (lowercase words joined by
  hyphens), `title`, `category`, `description` (the alt text: what is shown,
  concretely, in one or two sentences), `keyIdea` (one sentence), `lesson` (the
  repository path of the main lesson; that page must place the figure, which
  `test_assets.py` checks), `height` and `draw`, plus an optional
  `subtitle` (default: `<category> · Worked example`). The gallery builds its
  category filters from the data, so a new category needs no viewer change.
- **Fixtures.** `drawing.load_all_fixtures` reads `examples.properties`, then every
  `examples-*.properties` sorted by name, and merges them. A key defined twice, or a
  family key that does not start with `<stem>.`, fails the build. `draw(canvas, fixtures)`
  receives the merged mapping of strings; `drawing.ints(fixtures, key)` parses a list.

A minimal complete module:

```text
"""Figures for the example family; fixtures in examples-example.properties."""
from drawing import ints

IDEA = "Appending at size writes one slot; no element moves."


def append_at_size(c, fixtures):
    values = ints(fixtures, "example.values")
    y = c.step(156, 1, f"Append {values[-1]} at index {len(values) - 1}")
    states = [None] * (len(values) - 1) + ["focus"]
    y = c.cell_row(60, y, values, states=states, labels=list(range(len(values))))
    y = c.legend(y + 30, [("focus", "written by this step")])
    c.note(y + 14, IDEA)


FIGURES = [{
    "id": "example-append", "title": "Append at size", "category": "Linear structures",
    "description": "The array 10, 20, 30 receives 30 at index 2; no earlier element moves.",
    "keyIdea": IDEA, "lesson": "data-structures/arrays/operations.md",
    "height": 560, "draw": append_at_size,
}]
```

With `docs/visuals/data/examples-example.properties` containing `example.values=10,20,30`.
The Java test loads the same file and checks it against the public API:

```text
class VisualExamplesExampleTest {
    private static final VisualFixtures DATA = VisualFixtures.load("examples-example.properties");

    @Test
    void appendWritesTheLastSlot() {
        DynamicArray<Integer> values = new DynamicArray<>();
        for (int value : DATA.ints("example.values")) values.add(value);
        assertEquals(DATA.list("example.values"), contents(values));
    }
}
```

`VisualFixtures` offers `text`, `number`, `ints`, `longs`, `list` (boxed integers)
and `strings`; each fails the test with the key and file name when the key is
missing or malformed. Assert every value the figure displays, not just the input.

### Placing a figure in a lesson

Write an empty named region where the figure belongs, next to the text it
illustrates, then run the generator. It fills the body with the light and dark
picture, alt text, a bold `Figure NN.` label, the caption and a link to the visual
index:

```text
<!-- visual:generated FIGURE_ID -->
<!-- /visual:generated -->
```

The caption defaults to the figure's `description`. To use another caption, add it
after a pipe: `<!-- visual:generated FIGURE_ID | One caption sentence. -->`. A caption
must not contain two consecutive hyphens, because it lives in an HTML comment. The
caption is emitted as Markdown, not escaped, so `*`, `` ` `` and links work and a literal
`<` must be written as `&lt;`; the description is escaped for the alt text. A marker
that shares its line with other text fails the build, unless it is inside inline code. A page
may hold any number of regions. Markers count only on a line of their own, outside
fenced code. The generator scans tracked and untracked, non-ignored Markdown, except
`docs/superpowers/`, hidden directories and this folder's generated `README.md`. An
unknown id, a malformed marker, an unclosed region or a nested region fails the build
and names the file and line. Never edit a region body by hand.

### Lesson contract

Every Markdown page under `algorithms/` and `data-structures/` places at least one figure
and opens with an At a glance table directly under its first paragraph. Write the labels
with non-breaking spaces so GitHub keeps each on one line, and end the first two cells
with a full stop (`test_assets.py` enforces both):

```text
| At&nbsp;a&nbsp;glance | |
| --- | --- |
| **Use&nbsp;it&nbsp;when** | One sentence ending with a full stop. |
| **Avoid&nbsp;it&nbsp;when** | One sentence ending with a full stop. |
| **Cost** | Time and extra space |
| **In&nbsp;this&nbsp;repo** | A linked class or method |
| **In&nbsp;the&nbsp;JDK** | A class or method as code, or "no direct equivalent" |
```

A lesson then keeps its invariant, a worked `text` trace, an executable example and a
costs table, and ends with **In real Java** (what to use in production, with one
executable example), **Common mistakes** (each with the input that exposes it),
**Practice** and **Check yourself**: three `<details>` blocks whose `<summary>` is the
question, written with `<code>` rather than backticks, which can show literally inside a
summary, with a blank line after `</summary>` and before `</details>`.

### Landing-page galleries

`data-structures/README.md` and `algorithms/README.md` list every lesson of their folder
in a generated gallery region, a marker alone on its line, `<!-- visual:gallery FOLDER -->`,
closed by `<!-- /visual:gallery -->`, where `FOLDER` is the folder path from the
repository root. The generator writes one table per immediate subfolder, with a
`README.md` first and then the other lessons by file name; each row shows a thumbnail of
the lesson's first figure not already used higher on the page, the lesson's `# ` title
and its "Use it when" cell. A lesson placed directly in the gallery folder, or one
without a figure, a title or an At a glance table, fails the build with its path. A new
lesson therefore needs no landing-page edit: regenerate and it appears.

### Drawing primitives

The canvas is 520 units wide; content runs from x = 28 to x = 492, below the header
rule at y = 118. All text is at least 18px: 20px for values and headings, 18px for
secondary labels. Colors and states are semantic names, never hex values.

| Primitive | Purpose |
| --- | --- |
| `step(y, number, text)` | Numbered blue badge and bold heading on baseline `y`; returns where the step's content starts (y + 22). |
| `cell_row(x, y, values, *, cell=60, gap=10, height=48, states=None, labels=None)` | A row of boxed values; `states` holds `None` or a state per cell, `labels` go below; returns the y below the row and labels. An empty string draws an empty cell. |
| `cell_center(x, index, cell=60, gap=10)` | The x centre of one cell of such a row, for pointers and braces. |
| `grid(x, y, rows, *, cell_w=56, cell_h=44, row_labels=None, col_labels=None, states=None)` | A table for DP grids and matrices; `states` maps `(row, column)` to a state; column labels above, row labels to the left; returns the y below the grid. |
| `pointer(x, y, label, *, color="blue", below=True)` | A short labelled arrow pointing at the edge at (x, y) from below (or above); returns the label's far edge. |
| `chip(x, y, text, *, color="blue")` | A rounded 30-unit-tall label with its top-left at (x, y); returns its width. |
| `brace(x1, x2, y, label, *, color="muted", above=False)` | A bracket over x1..x2 with a centred label, below a range (or above it); returns the label's far edge. |
| `curve(x1, y1, x2, y2, *, bend=40, color="blue", dashed=False, arrow=True, label=None)` | A curved arrow; positive `bend` bulges to the left of travel (upward when going right). |
| `edge(start, end, *, color="line", radius=27, dashed=False, label=None, directed=True)` | A node-to-node connector trimmed to both circles, with an optional weight label. |
| `node(x, y, label, active=False, radius=27, color=None, state=None)` | A circle with a centred bold label; `state` uses the state styles. |
| `legend(y, items)` | Swatches for `(state, text)` pairs from baseline `y`, wrapping to new rows; returns the y below. |
| `panel(y, height)` | The framed background used by existing figures. |
| `note(y, text)` and `note_height(text)` | The KEY IDEA box and the height it will take. |
| `text`, `paragraph`, `rect`, `line`, `arrow`, `path`, `cells` | The original low-level primitives; `path` accepts absolute commands only. |

States, each a stroke, fill and text color that pass the 4.5:1 contrast tests in both
themes:

| State | Look | Meaning |
| --- | --- | --- |
| `focus` | blue on soft | The element this step is about |
| `good` | green on mint | Done, kept, copied or correct |
| `alt` | violet on lilac | A second group or an alternative |
| `bad` | red on rose | Removed, rejected or invalid |
| `warn` | gold on note | Pending or needing attention |
| `muted` | dashed outline, muted text | Empty, free or out of scope |

Color never carries meaning alone: name every state you use in a label or a legend
entry. The plain palette names (`ink`, `muted`, `line`, `blue`, `soft`, `note`, `gold`,
`red`, `panel`, `green`, `mint`, `violet`, `lilac`, `rose`) are also accepted wherever
a `color` is taken, and so are the state names, each meaning its stroke color
(`color="good"` is green; `muted` keeps its palette meaning). `"none"` means no paint
and is allowed only for fills and strokes, not for arrows. Any other name raises
`ValueError`. Fixture files fail with file and line on a repeated key or a line
without `=`. `cell_row` and `grid` labels must match the number of cells, rows or
columns. `edge` needs two different endpoints. `path` rejects relative and arc
commands.

### Composition

- **One idea per figure.** If you need two key ideas, make two figures.
- **Read top to bottom.** Number the steps with `step`, starting at y = 156, and keep
  each step's drawing directly under its heading. Use one row geometry for arrays
  that should line up from step to step.
- **Concrete values.** Show the fixture's actual values, sizes and indices; the
  Java test must assert every one of them.
- **Height.** Aim for 560 to 860 units. Split a figure rather than exceed 900.
- **End with the KEY IDEA note.** Put the legend just above it, then
  `note(y, keyIdea)`, and choose `height` so the note ends at least 20 units above
  the bottom (`note_height` gives its size).
- Keep arrows clear of text, and leave at least a few units between labels.

### Layout guard and rendered review

The canvas records an estimated box for every text. `scripts/visuals/test_layout.py`
draws every figure in both themes and fails, naming the figure, theme and strings,
when a text box leaves the canvas, two text boxes overlap by more than 2 units, or
anything is drawn below `height - 20`. Text drawn inside a cell or node is fine.

Then look at the real rendering. The review tool uses the pre-installed Playwright
Chromium, reads the committed SVGs (run the generator first) and measures real text
boxes:

```sh
node scripts/visuals/browser_review.js --figures dynamic-array-growth,array-insertion --sheets /tmp/sheets
node scripts/visuals/browser_review.js --all
node scripts/visuals/browser_review.js --all --write
```

The first command writes contact sheets, `sheet-01.png` and so on, with up to four
figures per sheet, light and dark side by side, about 1100px wide. The figure id
is printed above each figure. Open every sheet and fix overlaps, clipped or cramped
labels, arrows through text and anything hard to read in the dark theme. `--all` also
serves the repository on a loopback port and steps the viewer through every trace
frame at 1440px and 390px in both themes. `--write` rewrites `data/browser-review.json`,
and `--previews` also recaptures `previews/*.png`. The tool exits non-zero when it
finds an issue. It is optional tooling and not part of the Maven build.

#### Calibrating the layout guard

`drawing.estimate_text_width` sums per-character widths (the wider of the regular
and bold system-ui faces, rounded up to 0.05 em), then multiplies by `TEXT_SCALE`
for each weight. The factors were fitted to Chromium `getBBox()` widths of every
text in the committed figures. For every string of three or more characters the
estimate is at least as wide as the rendered text, and regular-weight text is
estimated about 5% wider on average. If you change the table, re-measure the
committed SVGs and keep both properties. All existing figures must still pass.
