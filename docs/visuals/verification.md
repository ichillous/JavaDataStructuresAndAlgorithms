# Visual coverage and verification

This page records what was checked on 2026-11-17, after the lessons and figures
expansion. Every number below was measured on that tree; the command that produced it
is named beside it, so you can measure it again rather than trust this page.

## What exists

The generator's summary line (`python3 scripts/generate_visuals.py --check`) reports
**98 figures** and **196 SVGs**: an explicit light and dark variant of each figure. The
searchable companion `index.html` adds **13 step-throughs with 118 states** (frames in
`data/visual-data.js`). The [Markdown index](README.md) is generated from the same data,
lists every figure with its category, alt text and lesson, and is the source of truth
for the current list.

Generated regions place figures in every one of the 71 Markdown pages under
`algorithms/` and `data-structures/` (69 lessons and the two landing pages), and in the
repository README, the complexity guide and the worked solutions. Each figure's `lesson`
is a page that places it, and every figure is placed at least once; `test_assets.py`
checks both.

The viewer builds one category filter per figure category, plus "All". With these
counts it showed (button label, then the figures the filter left visible):

| Category | Figures |
| --- | --- |
| Linear structures | 11 |
| Trees and ranges | 16 |
| Algorithm traces | 9 |
| Graphs | 19 |
| Complexity | 1 |
| Hashing and sketches | 6 |
| Maps | 2 |
| Math and bits | 6 |
| Optimization | 7 |
| Array patterns | 6 |
| Sorting and searching | 9 |
| Strings | 6 |

The step-throughs cover binary search (lower bound), merge sort, BFS, Dijkstra, coin
change, insertion sort, quickselect, radix sort, two pointers, a sliding window, the
Z-function, DFS and Kahn's topological sort. Each links the lesson that teaches it.
Controls include previous/next/reset, a position slider, keyboard navigation, search,
category filters, enlarged diagrams and system/light/dark appearance.

## Rendered review

`node scripts/visuals/browser_review.js --all --write --previews` drove Chromium over a
loopback HTTP server and recorded the [browser report](data/browser-review.json):

- all **196 SVG variants** were measured for real text boxes that leave the canvas,
  enter the 20-unit bottom margin or overlap another label: no issue;
- the viewer was stepped through all 118 states at **1440px and 390px** in **both
  themes**, **472 state/layout checks**, for sideways scrolling, overlapping state
  labels, the position read-out and the Next button: no issue;
- the four previews below were recaptured.

A separate scripted pass over the viewer clicked every category filter (each showed
exactly its stated count, and the twelve categories add up to 98), searched for
"shortest path" (2 of 98 figures), searched for a nonsense word (the empty state
appeared), cleared the filters (98 of 98) and found a non-empty state legend on all
118 frames, with no page error. Contact sheets of 20 figures from eight categories
(linear structures, trees and ranges, algorithm traces, graphs, hashing, optimization,
strings and both maps), light and dark side by side, and the four previews were
also inspected by eye; nothing needed a change.

Captured interactive previews:

- [Desktop, light](previews/desktop-light.png) · [Desktop, dark](previews/desktop-dark.png)
- [Phone, light](previews/mobile-light.png) · [Phone, dark](previews/mobile-dark.png)

Small labels keep a minimum 18px size in SVG coordinates; opening an SVG directly
allows unrestricted zoom. Contrast tests use the normal-text 4.5:1 threshold even for
headings, because images scale on phones.

## Opening the companion

The managed review browser blocked direct `file://` navigation, so rendered checks
used a loopback HTTP server. The companion contains local classic scripts and assets,
with no fetch, modules, remote fonts or account requirement. Open the HTML locally in a
modern browser, or serve it from the repository root:

```sh
python3 -m http.server --bind 127.0.0.1 8000
```

Then visit `http://127.0.0.1:8000/docs/visuals/`. Serving the repository root also keeps
relative lesson links reachable: all **263 unique targets** (the viewer page and its
scripts and styles, the visual index, both SVGs of every figure, and every figure and
trace lesson) returned HTTP 200 from such a server. A Maven build is not required to
browse it.

## Reproducibility and semantic checks

The standard-library generator reproduces SVGs, manifest, data script, visual index,
lesson regions and landing-page galleries; `--check` reported 0 stale files. The Python
suite (`python3 -m unittest discover -s scripts/visuals -p 'test_*.py'`, 66 tests)
covers exact regeneration, HTML and asset references, SVG accessibility, palette
contrast, the layout guard, the galleries, the At a glance form of every lesson,
figure placement and the rendered-review report, and recomputes trace invariants
independently of the trace builders. Each figure family's `VisualExamples*Test` (16
Java test classes) reads the same canonical properties and checks every displayed
value against the public API.

See [repository validation](../validation.md) for the complete Java totals and the
[design and provenance note](design.md) for the figure rules and Emergent Mind
attribution.

## Limits

Traces illustrate fixed small examples rather than arbitrary user input or measured
performance. They reveal a valid conceptual sequence, not private memory addresses or a
debugger transcript. The complexity plot compares explicitly chosen formulas in
arbitrary work units, with no benchmark or speedup claim. Screenshots capture one
Chromium/system-font environment; other modern browsers may differ slightly. The static
SVGs, captions and lesson text remain available without the interactive viewer. No
hosted site was created; the companion stays in the repository.
