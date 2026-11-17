#!/usr/bin/env python3
"""Generate/check the committed visual learning assets, using only Python's standard library.

Figures come from scripts/visuals/*_figures.py modules; fixtures from
docs/visuals/data/examples*.properties. Markdown pages place figures with named regions:

    <!-- visual:generated FIGURE_ID -->            or
    <!-- visual:generated FIGURE_ID | Caption. -->
    <!-- /visual:generated -->

The generator rewrites each region body. See docs/visuals/design.md, "Adding a figure".
"""
import argparse
from collections import namedtuple
import importlib
from html import escape
import json
import os
from pathlib import Path
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
VISUALS_SOURCE = ROOT / "scripts" / "visuals"
FIXTURES = ROOT / "docs" / "visuals" / "data"
sys.path.insert(0, str(VISUALS_SOURCE))
from drawing import Canvas, load_all_fixtures  # noqa: E402

# Figures 01-26 keep their numbers: these load first, in this order.
CORE_MODULES = ("linear_figures", "tree_figures", "algorithm_figures", "graph_figures", "complexity_figures")
FIGURE_KEYS = {"id", "title", "category", "description", "keyIdea", "lesson", "height", "draw"}

END = "<!-- /visual:generated -->"
FIGURE_ID = re.compile(r"[a-z0-9]+(?:-[a-z0-9]+)*")
INLINE_CODE = re.compile(r"(`+)(?:(?!\1).)+?\1")
OPEN = re.compile(r"<!-- visual:generated (?P<id>[a-z0-9]+(?:-[a-z0-9]+)*)(?: \| (?P<caption>\S(?:.*\S)?))? -->")
FENCE = re.compile(r"^ {0,3}(`{3,}|~{3,})")
# Pages never scanned for regions: plans and fragments, and the generated visual index.
SKIPPED_PREFIXES = ("docs/superpowers/", "target/")
SKIPPED_PAGES = {"docs/visuals/README.md"}

Region = namedtuple("Region", "figure_id caption start end")


def figure_modules(directory=VISUALS_SOURCE):
    """Core modules in their fixed order, then every other *_figures.py sorted by file name."""
    found = sorted(path.stem for path in Path(directory).glob("*_figures.py"))
    return [name for name in CORE_MODULES if name in found] + [name for name in found if name not in CORE_MODULES]


def collect_figures(module_names):
    """Concatenate each module's FIGURES list after validating it.

    A module without a FIGURES list, a figure that is not a dict, lacks a required key or has
    an id that is not lowercase words joined by hyphens, or a repeated id fails the build.
    """
    figures, owners = [], {}
    for name in module_names:
        module_figures = getattr(importlib.import_module(name), "FIGURES", None)
        if not isinstance(module_figures, list):
            raise ValueError(f"{name}: define FIGURES as a list of figure dicts")
        for position, figure in enumerate(module_figures):
            if not isinstance(figure, dict):
                raise ValueError(f"{name}: FIGURES[{position}] is not a dict")
            label = f"{name}: figure {figure.get('id', f'FIGURES[{position}]')!r}"
            missing = FIGURE_KEYS - set(figure)
            if missing:
                raise ValueError(f"{label} lacks {', '.join(sorted(missing))}")
            if not isinstance(figure["id"], str) or not FIGURE_ID.fullmatch(figure["id"]):
                raise ValueError(f"{label}: an id must be lowercase words joined by hyphens")
            if not callable(figure["draw"]):
                raise ValueError(f"{label}: draw must be a function")
            if figure["id"] in owners:
                raise ValueError(f"Duplicate figure id {figure['id']!r} in {owners[figure['id']]} and {name}")
            owners[figure["id"]] = name
            figures.append(figure)
    return figures


def draw_figure(figure, number, theme, fixtures):
    """Draw one figure in one theme and return the Canvas (used by the layout guard too)."""
    missing = FIGURE_KEYS - set(figure)
    if missing:
        raise ValueError(f"Figure {figure.get('id')!r} lacks {', '.join(sorted(missing))}")
    canvas = Canvas(figure["id"], f'{number:02d}  {figure["title"]}',
                    figure.get("subtitle", figure["category"] + " · Worked example"), figure["height"], theme)
    figure["draw"](canvas, fixtures)
    return canvas


def region_pages():
    """Tracked and untracked-but-not-ignored Markdown pages that may hold regions."""
    names = subprocess.check_output(
        ["git", "ls-files", "-z", "--cached", "--others", "--exclude-standard"], cwd=ROOT
    ).decode().split("\0")
    pages = []
    for name in sorted(set(names)):
        if (not name.endswith(".md") or name in SKIPPED_PAGES or name.startswith(SKIPPED_PREFIXES)
                or any(part.startswith(".") for part in name.split("/"))):
            continue
        if (ROOT / name).is_file():
            pages.append(name)
    return pages


def find_regions(name, text):
    """Parse the named regions of one page; a malformed marker fails with file and line.

    Markers count only on a line of their own outside fenced code blocks, so documentation
    may show the syntax in a fence or in inline code.
    """
    regions, current, fence = [], None, None
    for number, line in enumerate(text.split("\n"), 1):
        fence_match = FENCE.match(line)
        if fence_match:
            marker = fence_match.group(1)
            if fence is None:
                fence = marker
            elif marker[0] == fence[0] and len(marker) >= len(fence) and not line.strip()[len(marker):]:
                fence = None
            continue
        stripped = line.strip()
        if fence is not None or "visual:generated" not in INLINE_CODE.sub("", stripped):
            continue
        where = f"{name}:{number}"
        if not stripped.startswith("<!--") or not stripped.endswith("-->") or stripped.count("<!--") > 1:
            raise ValueError(f"{where}: a region marker must be alone on its line; "
                             "put documentation of the syntax in inline code or a fence")
        if stripped == END:
            if current is None:
                raise ValueError(f"{where}: closing marker without an opening region")
            regions.append(current._replace(end=number))
            current = None
            continue
        match = OPEN.fullmatch(stripped)
        if not match:
            raise ValueError(f"{where}: malformed region marker {stripped!r}; use "
                             "'<!-- visual:generated FIGURE_ID -->' or '<!-- visual:generated FIGURE_ID | Caption. -->'")
        caption = match.group("caption")
        if caption is not None and "--" in caption:
            raise ValueError(f"{where}: a caption must not contain '--' because it lives in an HTML comment")
        if current is not None:
            raise ValueError(f"{where}: nested region; the region opened on line {current.start} is still open")
        current = Region(match.group("id"), caption, number, None)
    if current is not None:
        raise ValueError(f"{name}:{current.start}: unclosed region for {current.figure_id!r}; add '{END}'")
    return regions


def region_body(name, item, caption):
    parent = (ROOT / name).parent
    light = os.path.relpath(ROOT / "docs/visuals" / item["light"], parent).replace("\\", "/")
    dark = os.path.relpath(ROOT / "docs/visuals" / item["dark"], parent).replace("\\", "/")
    index = os.path.relpath(ROOT / "docs/visuals/README.md", parent).replace("\\", "/")
    return (f'<picture><source media="(prefers-color-scheme: dark)" srcset="{dark}"><img src="{light}" '
            f'alt="{escape(item["description"], quote=True)}" width="520"></picture>\n\n'
            f'**Figure {item["number"]:02d}.** {caption or item["description"]} '
            f'[Visual index and step-throughs]({index}).')


def rewrite_regions(name, text, figures_by_id):
    """Replace every region body in ``text``; ids must exist in ``figures_by_id``."""
    regions = find_regions(name, text)
    lines = text.split("\n")
    for region in reversed(regions):
        if region.figure_id not in figures_by_id:
            raise ValueError(f"{name}:{region.start}: unknown figure id {region.figure_id!r}")
        body = region_body(name, figures_by_id[region.figure_id], region.caption).split("\n")
        lines[region.start:region.end - 1] = body
    return "\n".join(lines)


def build_outputs():
    fixtures = load_all_fixtures(FIXTURES)
    figures = collect_figures(figure_modules())
    outputs = {}
    metadata = []
    for index, figure in enumerate(figures, 1):
        item = {key: value for key, value in figure.items() if key not in ("draw", "height")}
        item.update(number=index, light=f'assets/{figure["id"]}.svg', dark=f'assets/{figure["id"]}-dark.svg')
        item["lesson"] = "../../" + figure["lesson"]
        metadata.append(item)
        for theme, suffix in (("light", ""), ("dark", "-dark")):
            canvas = draw_figure(figure, index, theme, fixtures)
            outputs[f'docs/visuals/assets/{figure["id"]}{suffix}.svg'] = canvas.svg(figure["description"])
    traces = importlib.import_module("trace_data").build_traces(fixtures)
    data = {"figures": metadata, "traces": traces}
    encoded = json.dumps(data, ensure_ascii=False, indent=2) + "\n"
    outputs["docs/visuals/data/manifest.json"] = encoded
    outputs["docs/visuals/data/visual-data.js"] = "// Generated by scripts/generate_visuals.py; do not edit.\n" + "globalThis.DSA_VISUALS_DATA = " + encoded.rstrip() + ";\n"
    lines = ["# Visual learning index", "",
             "Open [index.html](index.html) locally for the searchable gallery and five step-throughs.",
             "The HTML uses local assets and works offline without a server. On GitHub, browse the figures below.",
             "", "Each numbered figure has an explicit light/dark version, an accessible description and a linked lesson.",
             "Blue identifies the current focus; plain outlines give context. Dashed strokes mark alternatives or invalid choices where labeled.",
             "Figures use the same canonical examples as the Java visual-contract tests. See [design and provenance](design.md) and [verification](verification.md).", ""]
    for item in metadata:
        lines.extend([f'## {item["number"]:02d}. {item["title"]}', "",
                      f'[{item["category"]}: read the lesson]({item["lesson"]})', "",
                      f'<picture><source media="(prefers-color-scheme: dark)" srcset="{item["dark"]}"><img src="{item["light"]}" alt="{escape(item["description"], quote=True)}" width="520"></picture>',
                      "", f'**Key idea:** {item["keyIdea"]}', ""])
    outputs["docs/visuals/README.md"] = "\n".join(lines)
    by_id = {item["id"]: item for item in metadata}
    for name in region_pages():
        text = (ROOT / name).read_text(encoding="utf-8")
        if "visual:generated" in text:
            rewritten = rewrite_regions(name, text, by_id)
            if find_regions(name, text):
                outputs[name] = rewritten
    return outputs


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--check", action="store_true", help="fail instead of rewriting missing/stale outputs")
    args = parser.parse_args()
    try:
        outputs = build_outputs()
    except ValueError as error:
        print("Visual generation failed:", error, file=sys.stderr)
        return 2
    stale = []
    for name, expected in outputs.items():
        path = ROOT / name
        if args.check:
            if not path.is_file() or path.read_text(encoding="utf-8") != expected:
                stale.append(name)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(expected, encoding="utf-8", newline="\n")
    expected_assets = {name for name in outputs if name.endswith(".svg")}
    unexpected = {str(path.relative_to(ROOT)).replace("\\", "/") for path in (ROOT / "docs/visuals/assets").glob("*.svg")} - expected_assets
    stale.extend(sorted(unexpected))
    for name in stale:
        print("Stale or unexpected visual asset:", name)
    print(f'{len(expected_assets) // 2} figures, {len(expected_assets)} SVGs, {len(outputs)} generated files; {len(stale)} stale.')
    return bool(stale)


if __name__ == "__main__":
    raise SystemExit(main())
