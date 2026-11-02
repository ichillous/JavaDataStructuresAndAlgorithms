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

Do not edit generated SVGs, the manifest, visual data script, or visual index by
hand. Edit their source and regenerate. See [verification](verification.md) for
the tested coverage, preview images and practical limits.
