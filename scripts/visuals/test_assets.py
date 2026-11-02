"""Portable checks for generated assets and independently reasoned trace invariants."""
from collections import Counter, deque
from html.parser import HTMLParser
import importlib.util
import json
from pathlib import Path
import unittest
from urllib.parse import unquote, urlsplit
import xml.etree.ElementTree as ET

from drawing import load_fixtures, ints
from lesson_links import LESSONS
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


class VisualAssetTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.fixtures = load_fixtures(VISUALS / "data/examples.properties")
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
        pages = [VISUALS / "index.html", VISUALS / "README.md"] + [ROOT / name for name in LESSONS]
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
        spec = importlib.util.spec_from_file_location("generate_visuals", ROOT / "scripts/generate_visuals.py")
        generator = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(generator)
        for name, expected in generator.build_outputs().items():
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


if __name__ == "__main__":
    unittest.main()
