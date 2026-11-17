"""Portable checks for generated assets and independently reasoned trace invariants."""
from collections import Counter, deque
from html.parser import HTMLParser
import importlib.util
import json
from pathlib import Path
import re
import tempfile
import unittest
from urllib.parse import unquote, urlsplit
import xml.etree.ElementTree as ET

from drawing import load_all_fixtures, ints
from trace_data import build_traces

ROOT = Path(__file__).resolve().parents[2]
VISUALS = ROOT / "docs/visuals"
NS = {"s": "http://www.w3.org/2000/svg"}


class References(HTMLParser):
    def __init__(self):
        super().__init__()
        self.references = []

    def handle_starttag(self, tag, attributes):
        values = dict(attributes)
        for key in ("src", "href"):
            if key in values:
                self.references.append(values[key])
        if "srcset" in values:
            self.references.extend(item.strip().split()[0] for item in values["srcset"].split(","))


def load_generator():
    spec = importlib.util.spec_from_file_location("generate_visuals", ROOT / "scripts/generate_visuals.py")
    generator = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(generator)
    return generator


class VisualAssetTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.generator = load_generator()
        cls.fixtures = load_all_fixtures(VISUALS / "data")
        cls.manifest = json.loads((VISUALS / "data/manifest.json").read_text(encoding="utf-8"))

    def test_registered_assets_have_accessible_metadata_and_portable_svg(self):
        ids = [figure["id"] for figure in self.manifest["figures"]]
        self.assertEqual(len(ids), len(set(ids)))
        self.assertGreaterEqual(len(ids), 26)
        for figure in self.manifest["figures"]:
            with self.subTest(figure=figure["id"]):
                self.assertTrue((VISUALS / figure["lesson"]).resolve().is_file())
                self.assertTrue(figure["description"] and figure["keyIdea"])
                for theme in ("light", "dark"):
                    path = VISUALS / figure[theme]
                    tree = ET.fromstring(path.read_text(encoding="utf-8"))
                    self.assertEqual(tree.get("role"), "img")
                    self.assertTrue(tree.find("s:title", NS).text)
                    self.assertEqual(tree.find("s:desc", NS).text, figure["description"])
                    self.assertEqual(tree.get("viewBox").split()[2], "520")
                    for element in tree.iter():
                        self.assertNotIn(element.tag.split("}")[-1], ("script", "foreignObject", "image"))
                        if element.tag.endswith("}text"):
                            self.assertGreaterEqual(float(element.get("font-size")), 18)
                        for key, value in element.attrib.items():
                            if key.endswith("href"):
                                self.assertTrue(value.startswith("#"), "SVG must not require an external resource")

    def test_html_and_lesson_image_references_resolve(self):
        lessons = [name for name in self.generator.region_pages()
                   if self.generator.find_regions(name, (ROOT / name).read_text(encoding="utf-8"))]
        self.assertGreaterEqual(len(lessons), 48)
        pages = [VISUALS / "index.html", VISUALS / "README.md"] + [ROOT / name for name in lessons]
        for path in pages:
            parser = References()
            parser.feed(path.read_text(encoding="utf-8"))
            for reference in parser.references:
                url = urlsplit(reference)
                if url.scheme or url.netloc or not url.path:
                    continue
                with self.subTest(page=str(path.relative_to(ROOT)), target=reference):
                    self.assertTrue((path.parent / unquote(url.path)).resolve().is_file())

    def test_committed_outputs_are_exactly_reproducible(self):
        for name, expected in self.generator.build_outputs().items():
            with self.subTest(output=name):
                self.assertEqual((ROOT / name).read_text(encoding="utf-8"), expected)

    def test_binary_predicate_bounds_keep_the_answer_even_at_high(self):
        trace = next(trace for trace in build_traces(self.fixtures) if trace["id"] == "binary")
        answer = int(self.fixtures["binary.result"])
        target = int(self.fixtures["binary.target"])
        for frame in trace["frames"]:
            view = frame["view"]
            low, high = view["range"]
            self.assertLessEqual(low, answer)
            self.assertLessEqual(answer, high)
            self.assertTrue(all(value < target for value in view["values"][:low]))
            self.assertTrue(all(value >= target for value in view["values"][high:]))
            if high == answer and high < len(view["states"]):
                self.assertNotEqual(view["states"][high], "excluded")
        self.assertEqual(trace["frames"][-1]["view"]["range"], [answer, answer])

    def test_merge_frames_preserve_the_multiset_and_sorted_output_prefix(self):
        trace = next(trace for trace in build_traces(self.fixtures) if trace["id"] == "merge")
        original = Counter(ints(self.fixtures, "merge.values"))
        for frame in trace["frames"]:
            view = frame["view"]
            self.assertEqual(Counter(view["values"]), original)
            lanes = view.get("lanes", [])
            if lanes:
                self.assertEqual(len(lanes), 3)
                output = lanes[-1]["values"]
                self.assertEqual(output, sorted(output))
                consumed = []
                for lane in lanes[:2]:
                    consumed.extend(value for value, state in zip(lane["values"], lane["states"]) if state == "excluded")
                self.assertEqual(Counter(consumed), Counter(output))

    def test_bfs_queues_are_unique_and_final_order_has_shortest_layers(self):
        trace = next(trace for trace in build_traces(self.fixtures) if trace["id"] == "bfs")
        adjacency = [[] for _ in range(int(self.fixtures["bfs.vertices"]))]
        for edge in self.fixtures["bfs.edges"].split(","):
            a, b = map(int, edge.split(":"))
            adjacency[a].append(b)
            adjacency[b].append(a)
        distance = {0: 0}
        queue = deque([0])
        while queue:
            current = queue.popleft()
            for neighbor in adjacency[current]:
                if neighbor not in distance:
                    distance[neighbor] = distance[current] + 1
                    queue.append(neighbor)
        for frame in trace["frames"]:
            view = frame["view"]
            self.assertFalse(view["directed"])
            self.assertEqual(len(view["queue"]), len(set(view["queue"])))
            self.assertEqual(len(view["order"]), len(set(view["order"])))
            layers = [distance[vertex] for vertex in view["order"]]
            self.assertEqual(layers, sorted(layers))
            self.assertTrue(all(view["nodes"][vertex]["state"] == "frontier" for vertex in view["queue"]))
        self.assertEqual(trace["frames"][-1]["view"]["order"], ints(self.fixtures, "bfs.order"))

    def test_dijkstra_settled_distances_are_final_and_path_cost_is_exact(self):
        trace = next(trace for trace in build_traces(self.fixtures) if trace["id"] == "dijkstra")
        expected = [None if part == "unreachable" else int(part) for part in self.fixtures["dijkstra.distances"].split(",")]
        for frame in trace["frames"]:
            view = frame["view"]
            for node in view["nodes"]:
                if node["state"] in ("done", "current"):
                    self.assertEqual(view["distances"][node["id"]], expected[node["id"]])
        final = trace["frames"][-1]["view"]
        weights = {(int(a), int(b)): int(w) for a, b, w in (edge.split(":") for edge in self.fixtures["dijkstra.edges"].split(","))}
        self.assertEqual(sum(weights[edge] for edge in zip(final["path"], final["path"][1:])), expected[final["path"][-1]])

    def test_coin_candidates_use_previously_solved_amounts(self):
        trace = next(trace for trace in build_traces(self.fixtures) if trace["id"] == "coins")
        for frame in trace["frames"]:
            view = frame["view"]
            for candidate in view.get("candidates", []):
                self.assertLess(candidate["from"], view["current"])
                self.assertEqual(candidate["cost"], candidate["previous"] + 1)
                self.assertEqual(candidate["previous"], view["values"][candidate["from"]])
            if view.get("candidates"):
                self.assertEqual(view["values"][view["current"]], min(item["cost"] for item in view["candidates"]))
        self.assertEqual(trace["frames"][-1]["view"]["values"], ints(self.fixtures, "coins.table"))


def trace_named(fixtures, trace_id):
    return next(trace for trace in build_traces(fixtures) if trace["id"] == trace_id)


def naive_z(text):
    """Reference Z-array: longest common prefix of the text and each suffix, z[0] = length."""
    result = []
    for start in range(len(text)):
        length = 0
        while start + length < len(text) and text[length] == text[start + length]:
            length += 1
        result.append(length)
    return result


class MoreTraceTest(unittest.TestCase):
    """Invariants recomputed from the fixtures, never from the trace builders' own state."""

    @classmethod
    def setUpClass(cls):
        cls.fixtures = load_all_fixtures(VISUALS / "data")

    def test_every_trace_is_bounded_and_links_an_existing_lesson(self):
        traces = build_traces(self.fixtures)
        self.assertEqual([trace["id"] for trace in traces][5:],
                         ["insertion", "quickselect", "radix", "two-pointers", "window",
                          "z-function", "dfs", "topological"])
        for trace in traces:
            with self.subTest(trace=trace["id"]):
                self.assertLessEqual(len(trace["frames"]), 16)
                self.assertTrue((VISUALS / trace["lesson"]).resolve().is_file())
                for frame in trace["frames"]:
                    self.assertTrue(frame["title"] and frame["explanation"])

    def test_insertion_prefix_is_sorted_and_values_are_a_permutation(self):
        original = ints(self.fixtures, "traces.insertion.values")
        trace = trace_named(self.fixtures, "insertion")
        for frame in trace["frames"]:
            view = frame["view"]
            self.assertEqual(Counter(view["values"]), Counter(original))
            prefix = view["values"][:view["range"][1]]
            self.assertEqual(prefix, sorted(prefix))
        self.assertEqual(trace["frames"][0]["view"]["values"], original)
        self.assertEqual(trace["frames"][-1]["view"]["values"], sorted(original))
        self.assertEqual(trace["frames"][-1]["view"]["range"], [0, len(original)])

    def test_quickselect_keeps_the_answer_index_inside_the_active_range(self):
        original = ints(self.fixtures, "traces.quickselect.values")
        k = int(self.fixtures["traces.quickselect.k"])
        answer = sorted(original)[k]
        trace = trace_named(self.fixtures, "quickselect")
        for frame in trace["frames"]:
            view = frame["view"]
            low, high = view["range"]
            self.assertLessEqual(low, k)
            self.assertLess(k, high)
            self.assertEqual(Counter(view["values"]), Counter(original))
            self.assertTrue(all(value <= answer for value in view["values"][:low]))
            self.assertTrue(all(value >= answer for value in view["values"][high:]))
            self.assertEqual(view["markers"]["k"], k)
        final = trace["frames"][-1]["view"]
        self.assertEqual(final["values"][k], answer)
        self.assertEqual(final["states"][k], "chosen")

    def test_radix_passes_sort_stably_by_the_last_digits(self):
        original = ints(self.fixtures, "traces.radix.values")
        trace = trace_named(self.fixtures, "radix")
        frames = trace["frames"]
        self.assertEqual(frames[0]["view"]["values"], original)
        order = original
        for digits in (1, 2, 3):
            key = lambda value, d=digits: value % 10 ** d
            expected = sorted(order, key=key)  # Python's sort is stable
            self.assertEqual(frames[2 * digits - 1]["view"]["values"], order)
            after = frames[2 * digits]["view"]["values"]
            self.assertEqual(after, expected)
            self.assertEqual([key(value) for value in after], sorted(key(value) for value in after))
            order = after
        self.assertEqual(frames[-1]["view"]["values"], sorted(original))
        self.assertEqual(len(frames), 8)

    def test_two_pointers_never_exclude_a_pair_that_sums_to_the_target(self):
        values = ints(self.fixtures, "traces.two.values")
        target = int(self.fixtures["traces.two.target"])
        pairs = [(i, j) for i in range(len(values)) for j in range(i + 1, len(values))
                 if values[i] + values[j] == target]
        self.assertEqual(len(pairs), 1)
        trace = trace_named(self.fixtures, "two-pointers")
        for frame in trace["frames"]:
            view = frame["view"]
            left, right = view["markers"]["left"], view["markers"]["right"]
            for i, j in pairs:
                self.assertTrue(left <= i and j <= right, frame["title"])
            for index, state in enumerate(view["states"]):
                self.assertEqual(state == "excluded", index < left or index > right)
        final = trace["frames"][-1]["view"]
        self.assertEqual([i for i, state in enumerate(final["states"]) if state == "chosen"], list(pairs[0]))

    def test_window_never_repeats_a_character_and_best_is_the_longest(self):
        text = self.fixtures["traces.window.text"]
        trace = trace_named(self.fixtures, "window")
        best_length = 0
        for frame in trace["frames"]:
            view = frame["view"]
            inside = [i for i, state in enumerate(view["states"]) if state in ("active", "current")]
            if inside:
                self.assertEqual(inside, list(range(inside[0], inside[-1] + 1)))
                chars = [text[i] for i in inside]
                self.assertEqual(len(chars), len(set(chars)), frame["title"])
                # Maximal: the window ends at the character just read and cannot grow left.
                self.assertEqual(inside[-1], view["markers"]["end"])
                self.assertTrue(inside[0] == 0 or text[inside[0] - 1] in chars, frame["title"])
            best = view["lanes"][0]["values"]
            self.assertEqual(len(best), len(set(best)))
            self.assertGreaterEqual(len(best), best_length)
            best_length = len(best)
        start, end = map(int, self.fixtures["traces.window.result"].split(":"))
        longest = max(b - a for a in range(len(text)) for b in range(a, len(text) + 1)
                      if len(set(text[a:b])) == b - a)
        self.assertEqual(end - start, longest)
        self.assertEqual(trace["frames"][-1]["view"]["lanes"][0]["values"], list(text[start:end]))

    def test_z_entries_that_are_shown_equal_the_naive_values(self):
        text = self.fixtures["traces.z.text"]
        expected = naive_z(text)
        trace = trace_named(self.fixtures, "z-function")
        for frame in trace["frames"]:
            lane = frame["view"]["lanes"][0]
            self.assertEqual(len(lane["values"]), len(text))
            for index, value in enumerate(lane["values"]):
                if value is not None:
                    self.assertEqual(value, expected[index])
        self.assertEqual(trace["frames"][-1]["view"]["lanes"][0]["values"], ints(self.fixtures, "traces.z.result"))
        self.assertEqual(expected, ints(self.fixtures, "traces.z.result"))

    def test_dfs_stack_is_always_a_path_from_the_source(self):
        size = int(self.fixtures["traces.dfs.vertices"])
        adjacency = [set() for _ in range(size)]
        for edge in self.fixtures["traces.dfs.edges"].split(","):
            a, b = map(int, edge.split(":"))
            adjacency[a].add(b)
            adjacency[b].add(a)
        trace = trace_named(self.fixtures, "dfs")
        for frame in trace["frames"]:
            view = frame["view"]
            stack = view["queue"]
            self.assertEqual(len(stack), len(set(stack)))
            if stack:
                self.assertEqual(stack[0], 0)
                for below, above in zip(stack, stack[1:]):
                    self.assertIn(above, adjacency[below])
                self.assertEqual(view["nodes"][stack[-1]]["state"], "current")
            self.assertTrue(set(stack) <= set(view["order"]))
            self.assertEqual(view["order"], ints(self.fixtures, "traces.dfs.order")[:len(view["order"])])
        frames = [frame["view"] for frame in trace["frames"]]
        for before, after in zip(frames, frames[1:]):
            if len(after["queue"]) == len(before["queue"]) - 1:
                popped = before["queue"][-1]  # a vertex is popped only when nothing unvisited is left
                self.assertTrue(adjacency[popped] <= set(before["order"]))
                self.assertEqual(after["queue"], before["queue"][:-1])
            elif after["queue"]:
                self.assertEqual(after["queue"][:-1], before["queue"])
                pushed = after["queue"][-1]
                self.assertIn(pushed, adjacency[before["queue"][-1]])
                self.assertNotIn(pushed, before["order"])
        final = trace["frames"][-1]["view"]
        self.assertEqual(final["queue"], [])
        self.assertEqual(final["order"], ints(self.fixtures, "traces.dfs.order"))

    def test_topological_order_emits_a_vertex_only_after_all_its_predecessors(self):
        size = int(self.fixtures["traces.topo.vertices"])
        predecessors = [set() for _ in range(size)]
        for edge in self.fixtures["traces.topo.edges"].split(","):
            a, b = map(int, edge.split(":"))
            predecessors[b].add(a)
        trace = trace_named(self.fixtures, "topological")
        for frame in trace["frames"]:
            view = frame["view"]
            self.assertTrue(view["directed"])
            emitted = view["order"]
            for position, vertex in enumerate(emitted):
                self.assertTrue(predecessors[vertex] <= set(emitted[:position]), frame["title"])
            for vertex in view["queue"]:
                self.assertTrue(predecessors[vertex] <= set(emitted))
                self.assertNotIn(vertex, emitted)
            done = {node["id"] for node in view["nodes"] if node["state"] in ("done", "current")}
            self.assertEqual(done, set(emitted))
            for vertex, remaining in enumerate(view["distances"]):
                self.assertEqual(remaining, len(predecessors[vertex] - set(emitted)))
        self.assertEqual(trace["frames"][-1]["view"]["order"], ints(self.fixtures, "traces.topo.order"))
        self.assertEqual(sorted(ints(self.fixtures, "traces.topo.order")), list(range(size)))


GALLERY_FOLDERS = ("algorithms", "data-structures")
ROW_LINK = re.compile(r"\| \[(?P<title>(?:[^\]\\]|\\.)+)\]\((?P<href>[^)]+)\) \|")


def gallery_lessons(folder):
    """Every lesson the gallery for ``folder`` must show, recomputed from the file system."""
    lessons = []
    for directory in sorted(path for path in (ROOT / folder).iterdir() if path.is_dir()):
        names = sorted(path.name for path in directory.glob("*.md"))
        names.sort(key=lambda name: name != "README.md")
        lessons.extend(f"{directory.name}/{name}" for name in names)
    return lessons


def gallery_region(text):
    lines = text.split("\n")
    opens = [index for index, line in enumerate(lines) if line.startswith("<!-- visual:gallery ")]
    closes = [index for index, line in enumerate(lines) if line == "<!-- /visual:gallery -->"]
    if len(opens) != 1 or len(closes) != 1:
        raise AssertionError("expected exactly one gallery region")
    return "\n".join(lines[opens[0] + 1:closes[0]])


def lesson_text(title, figure="alpha", glance=True):
    table = ("| At a glance | |\n| --- | --- |\n| **Use it when** |  You need *this* `x`.  |\n"
             "| **Cost** | O(1) |\n" if glance else "")
    region = f"<!-- visual:generated {figure} -->\n<!-- /visual:generated -->\n" if figure else ""
    return f"# {title}\n\nIntro.\n\n{table}\n{region}\n## More\n"


FAKE_FIGURES = {
    figure_id: {"id": figure_id, "number": number, "description": f"Shows {figure_id} & more.",
                "light": f"assets/{figure_id}.svg", "dark": f"assets/{figure_id}-dark.svg"}
    for number, figure_id in ((1, "alpha"), (2, "beta"))}


class GalleryTest(unittest.TestCase):
    """The generated gallery on each folder landing page lists every lesson in that folder."""

    PAGE = "# Topic\n\n<!-- visual:gallery topic -->\nstale\n<!-- /visual:gallery -->\n\nEnd.\n"
    SMALL = {"topic/README.md": "", "topic/divide-and-conquer/b.md": lesson_text("Bee", "beta"),
             "topic/divide-and-conquer/README.md": lesson_text("Choose | well"),
             "topic/hash-tables/a.md": lesson_text("Ay")}

    @classmethod
    def setUpClass(cls):
        cls.generator = load_generator()

    def test_each_landing_page_has_one_row_per_lesson_in_folder_and_file_order(self):
        for folder in GALLERY_FOLDERS:
            with self.subTest(folder=folder):
                text = (ROOT / folder / "README.md").read_text(encoding="utf-8")
                self.assertIn(f"<!-- visual:gallery {folder} -->", text)
                region = gallery_region(text)
                links = [match.group("href") for match in ROW_LINK.finditer(region)]
                self.assertEqual(gallery_lessons(folder), links)
                headings = [line[4:] for line in region.split("\n") if line.startswith("### ")]
                subfolders = sorted(path.name for path in (ROOT / folder).iterdir() if path.is_dir())
                self.assertEqual(len(subfolders), len(headings))

    def test_gallery_rows_use_the_first_figure_title_and_use_it_when_cell_and_resolve(self):
        manifest = json.loads((VISUALS / "data/manifest.json").read_text(encoding="utf-8"))
        by_id = {item["id"]: item for item in manifest["figures"]}
        for folder in GALLERY_FOLDERS:
            page = ROOT / folder / "README.md"
            region = gallery_region(page.read_text(encoding="utf-8"))
            parser = References()
            parser.feed(region)
            self.assertTrue(parser.references)
            for reference in parser.references:
                with self.subTest(page=folder, target=reference):
                    self.assertTrue((page.parent / unquote(reference)).resolve().is_file())
            rows = [line for line in region.split("\n") if line.startswith("| <a href=")]
            self.assertEqual(len(gallery_lessons(folder)), len(rows))
            for row, lesson in zip(rows, gallery_lessons(folder)):
                with self.subTest(lesson=lesson):
                    text = (page.parent / lesson).read_text(encoding="utf-8")
                    first = self.generator.find_regions(lesson, text)[0].figure_id
                    self.assertIn(f'srcset="../docs/visuals/assets/{first}-dark.svg"', row)
                    self.assertIn(f'<img src="../docs/visuals/assets/{first}.svg"', row)
                    self.assertIn('width="160"', row)
                    title = next(line[2:] for line in text.split("\n") if line.startswith("# "))
                    self.assertIn(f"| [{title}]({lesson}) |", row)
                    use = next(line for line in text.split("\n") if line.startswith("| **Use it when** |"))
                    cell = use[len("| **Use it when** |"):].rstrip().rstrip("|").strip()
                    self.assertTrue(row.endswith(f" | {cell} |"), row)
                    alt = by_id[first]["description"].replace("&", "&amp;").replace('"', "&quot;")
                    self.assertIn(f'alt="{alt}"', row)

    def build(self, files, page_text):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        root = Path(directory.name)
        for name, text in files.items():
            (root / name).parent.mkdir(parents=True, exist_ok=True)
            (root / name).write_text(text, encoding="utf-8")
        return self.generator.rewrite_galleries("topic/README.md", page_text, FAKE_FIGURES, root=root)

    def test_a_small_folder_renders_headings_rows_and_relative_paths(self):
        result = self.build(self.SMALL, self.PAGE)
        self.assertNotIn("stale", result)
        self.assertIn("### Divide and conquer\n", result)
        self.assertIn("### Hash tables\n", result)
        self.assertLess(result.index("### Divide and conquer"), result.index("### Hash tables"))
        self.assertLess(result.index("(divide-and-conquer/README.md)"), result.index("(divide-and-conquer/b.md)"))
        self.assertIn('| <a href="divide-and-conquer/b.md"><picture><source media="(prefers-color-scheme: dark)" '
                      'srcset="../docs/visuals/assets/beta-dark.svg"><img src="../docs/visuals/assets/beta.svg" '
                      'alt="Shows beta &amp; more." width="160"></picture></a> | [Bee](divide-and-conquer/b.md) | '
                      'You need *this* `x`. |', result)
        self.assertIn(r"[Choose \| well](divide-and-conquer/README.md)", result)
        self.assertNotIn("(README.md)", result, "the landing page itself is not a lesson")
        self.assertTrue(result.startswith("# Topic\n\n<!-- visual:gallery topic -->\n### "))
        self.assertTrue(result.endswith("\n<!-- /visual:gallery -->\n\nEnd.\n"))
        self.assertEqual(result, self.build(self.SMALL, result), "regenerating must be idempotent")

    def test_a_lesson_without_a_figure_or_at_a_glance_table_fails_with_page_line_and_lesson(self):
        with self.assertRaisesRegex(ValueError, r"topic/README\.md:3: .*topic/sub/plain\.md has no figure region"):
            self.build({"topic/sub/plain.md": lesson_text("Plain", figure=None)}, self.PAGE)
        with self.assertRaisesRegex(ValueError, r"topic/README\.md:3: .*topic/sub/plain\.md has no At a glance"):
            self.build({"topic/sub/plain.md": lesson_text("Plain", glance=False)}, self.PAGE)
        with self.assertRaisesRegex(ValueError, r"topic/README\.md:3: .*topic/sub/plain\.md has no '# ' title"):
            self.build({"topic/sub/plain.md": lesson_text("Plain").replace("# Plain", "Plain")}, self.PAGE)
        with self.assertRaisesRegex(ValueError, r"unknown figure id 'gamma'"):
            self.build({"topic/sub/plain.md": lesson_text("Plain", figure="gamma")}, self.PAGE)

    def test_malformed_gallery_markers_fail_with_file_and_line(self):
        files = {"topic/sub/a.md": lesson_text("Ay")}
        with self.assertRaisesRegex(ValueError, r"topic/README\.md:3: unknown gallery folder 'missing'"):
            self.build(files, self.PAGE.replace("gallery topic", "gallery missing"))
        with self.assertRaisesRegex(ValueError, r"topic/README\.md:3: .*unclosed gallery"):
            self.build(files, "x\n\n<!-- visual:gallery topic -->\n")
        with self.assertRaisesRegex(ValueError, r"topic/README\.md:1: .*without an opening"):
            self.build(files, "<!-- /visual:gallery -->\n")
        with self.assertRaisesRegex(ValueError, r"topic/README\.md:2: .*alone on its line"):
            self.build(files, "x\nSee <!-- visual:gallery topic --> here\n<!-- /visual:gallery -->\n")
        with self.assertRaisesRegex(ValueError, r"topic/README\.md:2: .*nested"):
            self.build(files, "<!-- visual:gallery topic -->\n<!-- visual:gallery topic -->\n"
                              "<!-- /visual:gallery -->\n")
        with self.assertRaisesRegex(ValueError, r"topic/README\.md:2: .*inside the gallery"):
            self.build(files, "<!-- visual:gallery topic -->\n<!-- visual:generated alpha -->\n"
                              "<!-- /visual:generated -->\n<!-- /visual:gallery -->\n")
        documented = "Use `<!-- visual:gallery FOLDER -->`.\n```text\n<!-- visual:gallery topic -->\n```\n"
        self.assertEqual(documented, self.build(files, documented))

    def test_a_lesson_placed_directly_in_the_gallery_folder_fails(self):
        with self.assertRaisesRegex(ValueError, r"topic/loose\.md .*subfolder"):
            self.build({"topic/sub/a.md": lesson_text("Ay"), "topic/loose.md": lesson_text("Loose")}, self.PAGE)


if __name__ == "__main__":
    unittest.main()
