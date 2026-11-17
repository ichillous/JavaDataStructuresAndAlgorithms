"""Generator contracts: figure-module discovery, fixture merging and named lesson regions."""
import importlib.util
from pathlib import Path
import sys
import tempfile
import types
import unittest

from drawing import load_all_fixtures

ROOT = Path(__file__).resolve().parents[2]


def load_generator():
    spec = importlib.util.spec_from_file_location("generate_visuals", ROOT / "scripts/generate_visuals.py")
    generator = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(generator)
    return generator


GENERATOR = load_generator()


def figure(figure_id, number):
    return {"id": figure_id, "number": number, "description": f"Description of {figure_id}.",
            "light": f"assets/{figure_id}.svg", "dark": f"assets/{figure_id}-dark.svg"}


FIGURES = {"alpha": figure("alpha", 1), "beta": figure("beta", 7)}


class ModuleDiscoveryTest(unittest.TestCase):
    def test_core_modules_keep_their_order_and_others_follow_sorted_by_file_name(self):
        with tempfile.TemporaryDirectory() as directory:
            names = ["zeta_figures.py", "alpha_figures.py", "complexity_figures.py", "graph_figures.py",
                     "linear_figures.py", "tree_figures.py", "algorithm_figures.py", "drawing.py",
                     "linear_more_figures.py", "test_figures.txt"]
            for name in names:
                (Path(directory) / name).write_text("", encoding="utf-8")
            self.assertEqual(["linear_figures", "tree_figures", "algorithm_figures", "graph_figures",
                              "complexity_figures", "alpha_figures", "linear_more_figures", "zeta_figures"],
                             GENERATOR.figure_modules(Path(directory)))

    def test_repository_discovery_keeps_the_original_twenty_six_numbers(self):
        figures = GENERATOR.collect_figures(GENERATOR.figure_modules(ROOT / "scripts/visuals"))
        self.assertEqual("array-insertion", figures[0]["id"])
        self.assertEqual("complexity-growth", figures[25]["id"])
        self.assertIn("dynamic-array-growth", [item["id"] for item in figures[26:]])

    def test_duplicate_figure_ids_fail_the_build(self):
        first = types.ModuleType("dup_one_figures")
        first.FIGURES = [{"id": "same"}]
        second = types.ModuleType("dup_two_figures")
        second.FIGURES = [{"id": "same"}]
        sys.modules.update(dup_one_figures=first, dup_two_figures=second)
        try:
            with self.assertRaisesRegex(ValueError, "same"):
                GENERATOR.collect_figures(["dup_one_figures", "dup_two_figures"])
        finally:
            del sys.modules["dup_one_figures"], sys.modules["dup_two_figures"]


class FixtureMergeTest(unittest.TestCase):
    def write(self, directory, name, text):
        (Path(directory) / name).write_text(text, encoding="utf-8")

    def test_base_file_then_family_files_sorted_by_name_are_merged(self):
        with tempfile.TemporaryDirectory() as directory:
            self.write(directory, "examples.properties", "# shared\narray.before=1,2\n")
            self.write(directory, "examples-zeta.properties", "zeta.value=9\n")
            self.write(directory, "examples-alpha.properties", "\nalpha.value = 3\n")
            self.write(directory, "unrelated.properties", "array.before=7\n")
            fixtures = load_all_fixtures(Path(directory))
        self.assertEqual({"array.before": "1,2", "alpha.value": "3", "zeta.value": "9"}, fixtures)
        self.assertEqual(["array.before", "alpha.value", "zeta.value"], list(fixtures))

    def test_a_duplicate_key_fails_and_names_the_key(self):
        with tempfile.TemporaryDirectory() as directory:
            self.write(directory, "examples.properties", "shared.key=1\n")
            self.write(directory, "examples-shared.properties", "shared.key=2\n")
            with self.assertRaisesRegex(ValueError, "shared.key"):
                load_all_fixtures(Path(directory))

    def test_a_family_key_must_start_with_the_file_stem(self):
        with tempfile.TemporaryDirectory() as directory:
            self.write(directory, "examples.properties", "")
            self.write(directory, "examples-graphs_more.properties", "graph.order=1\n")
            with self.assertRaisesRegex(ValueError, "graphs_more"):
                load_all_fixtures(Path(directory))


class NamedRegionTest(unittest.TestCase):
    def rewrite(self, text, name="data-structures/arrays/operations.md"):
        return GENERATOR.rewrite_regions(name, text, FIGURES)

    def test_a_known_id_gets_the_picture_label_default_caption_and_index_link(self):
        text = "# Arrays\n\nIntro.\n\n<!-- visual:generated beta -->\n<!-- /visual:generated -->\n\n## Next\n"
        expected = ("# Arrays\n\nIntro.\n\n<!-- visual:generated beta -->\n"
                    '<picture><source media="(prefers-color-scheme: dark)" '
                    'srcset="../../docs/visuals/assets/beta-dark.svg"><img src="../../docs/visuals/assets/beta.svg" '
                    'alt="Description of beta." width="520"></picture>\n\n'
                    "**Figure 07.** Description of beta. [Visual index and step-throughs](../../docs/visuals/README.md).\n"
                    "<!-- /visual:generated -->\n\n## Next\n")
        self.assertEqual(expected, self.rewrite(text))

    def test_a_caption_after_a_pipe_overrides_the_description_and_old_bodies_are_replaced(self):
        text = ("<!-- visual:generated alpha | A custom caption. -->\nstale body\n<!-- /visual:generated -->\n"
                "between\n<!-- visual:generated beta -->\n<!-- /visual:generated -->\n")
        result = self.rewrite(text, "README.md")
        self.assertIn("**Figure 01.** A custom caption. [Visual index and step-throughs](docs/visuals/README.md).", result)
        self.assertIn('srcset="docs/visuals/assets/alpha-dark.svg"', result)
        self.assertNotIn("stale body", result)
        self.assertIn("**Figure 07.** Description of beta.", result)
        self.assertEqual(result, self.rewrite(result, "README.md"), "rewriting must be idempotent")

    def test_an_unknown_id_fails_with_the_file_and_line(self):
        text = "one\ntwo\n<!-- visual:generated missing-figure -->\n<!-- /visual:generated -->\n"
        with self.assertRaisesRegex(ValueError, r"lesson\.md:3: unknown figure id 'missing-figure'"):
            self.rewrite(text, "lesson.md")

    def test_an_unclosed_region_fails_with_the_opening_line(self):
        with self.assertRaisesRegex(ValueError, r"lesson\.md:2: .*unclosed"):
            self.rewrite("x\n<!-- visual:generated alpha -->\nbody\n", "lesson.md")

    def test_a_nested_region_fails(self):
        text = "<!-- visual:generated alpha -->\n<!-- visual:generated beta -->\n<!-- /visual:generated -->\n"
        with self.assertRaisesRegex(ValueError, r"lesson\.md:2: .*nested"):
            self.rewrite(text, "lesson.md")

    def test_a_stray_close_and_malformed_markers_fail(self):
        with self.assertRaisesRegex(ValueError, r"lesson\.md:1: .*without an opening"):
            self.rewrite("<!-- /visual:generated -->\n", "lesson.md")
        for marker in ("<!-- visual:generated -->", "<!-- visual:generated Alpha -->",
                       "<!-- visual:generated alpha |  -->", "<!-- visual:generated alpha-->",
                       "<!-- visual:generated alpha | Uses -- a dash. -->"):
            with self.subTest(marker=marker):
                with self.assertRaisesRegex(ValueError, r"lesson\.md:1: "):
                    self.rewrite(marker + "\n<!-- /visual:generated -->\n", "lesson.md")

    def test_markers_inside_fenced_code_and_inline_code_are_documentation(self):
        text = ("Write `<!-- visual:generated FIGURE_ID -->` where it belongs.\n\n```text\n"
                "<!-- visual:generated FIGURE_ID -->\n<!-- /visual:generated -->\n```\n")
        self.assertEqual(text, self.rewrite(text, "lesson.md"))

    def test_repository_pages_are_scanned_from_git_and_skip_generated_and_plan_files(self):
        pages = GENERATOR.region_pages()
        self.assertIn("README.md", pages)
        self.assertIn("data-structures/arrays/operations.md", pages)
        self.assertFalse([page for page in pages if page.startswith("docs/superpowers/")])
        self.assertNotIn("docs/visuals/README.md", pages)
        self.assertTrue(all(page.endswith(".md") for page in pages))

    def test_every_original_page_keeps_a_named_region(self):
        regions = {}
        for page in GENERATOR.region_pages():
            for region in GENERATOR.find_regions(page, (ROOT / page).read_text(encoding="utf-8")):
                regions.setdefault(page, []).append(region.figure_id)
        self.assertGreaterEqual(sum(len(ids) for ids in regions.values()), 48)
        self.assertEqual(["array-insertion"], regions["README.md"])
        self.assertIn("dynamic-array-growth", regions["data-structures/arrays/operations.md"])


if __name__ == "__main__":
    unittest.main()
