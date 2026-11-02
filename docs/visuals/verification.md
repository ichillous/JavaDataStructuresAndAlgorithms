# Visual coverage and verification

The visual layer contains **26 numbered figures**, each in explicit light/dark
SVG variants (**52 SVGs**), plus **five interactive traces with 54 states**. The
searchable companion is `index.html`; the [Markdown index](README.md) works on
GitHub and links every figure to its lesson. Generated regions place diagrams in
45 original topic lessons, the repository README, complexity guide and solutions.

| Family | Figures |
| --- | --- |
| Linear structures | Array insertion, doubly linked unlink, physical/logical queue wrap, hash chaining, LRU promotion/eviction, stack LIFO |
| Trees and ranges | AVL rotation, heap array/tree anatomy, trie pruning, union-find components, Fenwick blocks, segment minima |
| Algorithm states | Lower-bound search, merge recursion, three-way partition, coin-change DP, 0/1 knapsack, KMP overlap, queen backtracking, activity selection, adjacent bubble swaps |
| Graphs | BFS discovery/processing, Dijkstra path/distances, Bellman negative edge/cycle, directed SCCs versus undirected bridges |
| Complexity | Common-scale illustrative growth functions with a logarithmic work axis |

The step-throughs cover lower bound, merge sort, BFS, Dijkstra and coin change.
Controls include previous/next/reset, a position slider, keyboard navigation,
search, category filters, enlarged diagrams and system/light/dark appearance.

## Rendered review

Actual Chromium review covered all 52 SVGs and all 54 states at **1440px and
390px** widths in **both themes** (216 viewer state/layout checks). It also
checked keyboard controls, end-state buttons, search/empty results, expanded
figures, theme-specific assets and absence of external resource requests.
Final SVG text bounds/collision checks and viewer overflow checks found no issues.
The [captured browser report](data/browser-review.json) records the reviewed counts and viewports.
Small labels retain a minimum 18px size in SVG coordinates; opening an SVG directly
allows unrestricted zoom. Contrast tests use the normal-text 4.5:1 threshold even
for headings, because images scale on phones.

Captured interactive previews:

- [Desktop, light](previews/desktop-light.png) · [Desktop, dark](previews/desktop-dark.png)
- [Phone, light](previews/mobile-light.png) · [Phone, dark](previews/mobile-dark.png)

The managed review browser blocked direct `file://` navigation, so rendered checks
used a loopback HTTP server. The companion contains local classic scripts and
assets, with no fetch, modules, remote fonts or account requirement. Open the HTML
locally in a modern browser, or serve it from the repository root:

```sh
python3 -m http.server --bind 127.0.0.1 8000
```

Then visit `http://127.0.0.1:8000/docs/visuals/`. Serving the repository root also
keeps relative lesson links reachable. All 79 unique viewer, diagram and lesson
HTTP targets were checked successfully with this layout. A Maven build is not
required to browse it.

## Complete build results

The complete Java 17 build passed **357 tests across 33 classes**, including 23
public-API visual fixture tests and all 99 executable Markdown examples. There
were zero failures, errors or skips. Production coverage remains 97.25% lines and
97.26% branches; enforced coverage floors and Javadoc validation passed. Ten
portable visual checks and exact regeneration passed as well.

## Reproducibility and semantic checks

The standard-library generator reproduces SVGs, manifest, data script, visual index
and marked lesson regions. Ten portable visual tests cover exact regeneration,
HTML/asset references, SVG accessibility, palette contrast and independent trace
invariants: retained binary boundaries, merge multiplicities, BFS layers, settled
distances and solved DP predecessors. Java `VisualExamplesTest` reads the same
canonical properties and checks examples against maintained public APIs.

Run the generator's `--check`, visual unittest suite, documentation-link checker
and complete Maven verification before submitting a change. CI repeats these
checks; the exact commit's Actions run is the publication evidence. See
[repository validation](../validation.md) for complete Java totals and the
[design/provenance note](design.md) for Emergent Mind attribution.

## Limits

Traces illustrate fixed small examples rather than arbitrary user input or measured
performance. They reveal a valid conceptual sequence, not private memory addresses
or a debugger transcript. The complexity plot compares explicitly chosen formulas
in arbitrary work units, with no benchmark or speedup claim. Screenshots capture
one Chromium/system-font environment; other modern browsers may differ slightly.
The static SVGs, captions and lesson text remain available without the interactive
viewer. No hosted site was created; the companion stays in the repository.
