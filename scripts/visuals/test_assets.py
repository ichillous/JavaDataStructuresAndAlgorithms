"""Portable checks for generated assets and independently reasoned trace invariants."""
from collections import Counter, deque
from html.parser import HTMLParser
import importlib.util
import json
from pathlib import Path
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


if __name__ == "__main__":
    unittest.main()
