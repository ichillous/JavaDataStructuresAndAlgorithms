"""Layout guard: estimated text boxes stay on the canvas and never collide, in both themes.

The estimate in drawing.estimate_text_width is calibrated against Chromium measurements so
that it is slightly wider than real text; browser_review.js checks the real boxes.
"""
import importlib.util
from pathlib import Path
import re
import unittest

from drawing import Canvas, STATES, estimate_text_width

ROOT = Path(__file__).resolve().parents[2]

# Figures allowed to break a layout rule, by id, each with the reason. Keep this empty;
# an entry needs an owner's decision recorded beside it.
EXEMPT = {}


def load_generator():
    spec = importlib.util.spec_from_file_location("generate_visuals", ROOT / "scripts/generate_visuals.py")
    generator = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(generator)
    return generator


def canvas(height=400):
    return Canvas("test", "01  Test figure", "Linear structures · Worked example", height)


class LayoutGuardTest(unittest.TestCase):
    def test_every_figure_passes_in_both_themes(self):
        generator = load_generator()
        fixtures = generator.load_all_fixtures(generator.FIXTURES)
        figures = generator.collect_figures(generator.figure_modules(generator.VISUALS_SOURCE))
        for number, figure in enumerate(figures, 1):
            for theme in ("light", "dark"):
                if figure["id"] in EXEMPT:
                    continue
                with self.subTest(figure=figure["id"], theme=theme):
                    drawn = generator.draw_figure(figure, number, theme, fixtures)
                    self.assertEqual([], drawn.layout_issues())

    def test_text_leaving_the_canvas_is_reported_with_figure_theme_and_text(self):
        drawn = Canvas("wide", "01  Wide", "Subtitle", 400, theme="dark")
        drawn.text(300, 200, "This label is much too long for the canvas")
        drawn.text(-4, 240, "left")
        issues = drawn.layout_issues()
        self.assertEqual(2, len(issues), issues)
        self.assertIn("wide (dark)", issues[0])
        self.assertIn("This label is much too long", issues[0])
        self.assertIn("'left'", issues[1])

    def test_overlapping_text_boxes_name_both_strings(self):
        drawn = canvas()
        drawn.text(40, 200, "first label")
        drawn.text(60, 210, "second label")
        issues = drawn.layout_issues()
        self.assertEqual(1, len(issues), issues)
        self.assertIn("'first label'", issues[0])
        self.assertIn("'second label'", issues[0])
        self.assertIn("test (light)", issues[0])

    def test_touching_boxes_within_the_tolerance_and_text_inside_shapes_are_allowed(self):
        drawn = canvas()
        drawn.cell_row(40, 200, [1, 2, 3], labels=["0", "1", "2"])
        drawn.node(300, 230, "7", state="good")
        drawn.text(40, 300, "upper line")
        drawn.text(40, 300 + 27, "lower line")
        self.assertEqual([], drawn.layout_issues())

    def test_drawing_into_the_bottom_margin_is_reported(self):
        drawn = canvas(400)
        drawn.rect(28, 300, 100, 90)
        self.assertEqual(1, len(drawn.layout_issues()))
        drawn = canvas(400)
        drawn.path("M 28 300 L 60 385", arrow=True)
        self.assertEqual(1, len(drawn.layout_issues()))
        drawn = canvas(400)
        drawn.text(28, 395, "low")
        self.assertTrue(any("bottom margin" in issue for issue in drawn.layout_issues()))

    def test_the_width_estimate_grows_with_length_size_and_weight(self):
        self.assertLess(estimate_text_width("iii", 20), estimate_text_width("MMM", 20))
        self.assertLess(estimate_text_width("label", 18), estimate_text_width("label", 20))
        self.assertLess(estimate_text_width("label", 20), estimate_text_width("label", 20, "700"))
        self.assertEqual(0, estimate_text_width("", 20))


class PrimitiveTest(unittest.TestCase):
    def test_states_cover_the_documented_names(self):
        self.assertEqual({"focus", "good", "alt", "bad", "warn", "muted"}, set(STATES))

    def test_step_and_legend_return_the_next_free_y(self):
        drawn = canvas(600)
        after_step = drawn.step(150, 1, "Append to a full array")
        self.assertGreater(after_step, 150)
        after_legend = drawn.legend(after_step, [("good", "copied"), ("focus", "new value"), ("muted", "free slot")])
        self.assertGreater(after_legend, after_step)
        self.assertEqual([], drawn.layout_issues())

    def test_cell_row_states_use_semantic_colors_and_reject_unknown_names(self):
        drawn = canvas()
        drawn.cell_row(40, 160, ["a", "b", ""], states=["good", None, "muted"])
        svg = drawn.svg("cells")
        self.assertIn(drawn.color("mint"), svg)
        self.assertIn('stroke-dasharray="6 5"', svg)
        with self.assertRaisesRegex(ValueError, "glow"):
            drawn.cell_row(40, 260, ["a"], states=["glow"])

    def test_state_names_are_colors_and_unknown_colors_fail(self):
        drawn = canvas(900)
        self.assertEqual(drawn.color("green"), drawn.color("good"))
        # "muted" is both a palette name and a state; as a color it keeps its palette meaning.
        self.assertNotEqual(drawn.color("line"), drawn.color("muted"))
        self.assertEqual("none", drawn.color("none"))
        drawn.chip(40, 140, "kept", color="good")
        drawn.pointer(300, 200, "size", color="bad")
        drawn.brace(40, 200, 300, "copies", color="alt")
        drawn.curve(60, 400, 220, 400, color="warn", label="move")
        drawn.node(320, 480, "1")
        drawn.node(440, 480, "2")
        drawn.edge((320, 480), (440, 480), color="focus", label="3")
        drawn.arrow(40, 560, 120, 560, color="good")
        drawn.line(40, 600, 120, 600, color="bad", arrow=True)
        drawn.path("M 40 640 H 120", color="muted", arrow=True)
        drawn.rect(200, 620, 40, 40, fill="none", stroke="alt")
        svg = drawn.svg("colors")
        defined = set(re.findall(r'<marker id="([^"]+)"', svg))
        used = set(re.findall(r'marker-end="url\(#([^)]+)\)"', svg))
        self.assertTrue(used, "the canvas should use arrow markers")
        self.assertLessEqual(used, defined)
        for paint in re.findall(r'(?:fill|stroke)="([^"]*)"', svg):
            self.assertRegex(paint, r"^(#[0-9a-f]{6}|none)$")
        for bad in ("glow", "#ff0000", "Good"):
            with self.subTest(color=bad):
                with self.assertRaisesRegex(ValueError, "Unknown color.*good"):
                    drawn.chip(40, 700, "x", color=bad)
                with self.assertRaisesRegex(ValueError, "Unknown color"):
                    drawn.arrow(40, 700, 80, 700, color=bad)
        with self.assertRaisesRegex(ValueError, "not 'none'"):
            drawn.line(40, 700, 80, 700, color="none", arrow=True)

    def test_mismatched_labels_identical_edge_ends_and_arcs_fail(self):
        drawn = canvas(600)
        with self.assertRaisesRegex(ValueError, "3 values but 2 labels"):
            drawn.cell_row(40, 160, [1, 2, 3], labels=["0", "1"])
        with self.assertRaisesRegex(ValueError, "2 rows but 1 row labels"):
            drawn.grid(100, 200, [[1, 2], [3, 4]], row_labels=["a"])
        with self.assertRaisesRegex(ValueError, "2 columns but 3 column labels"):
            drawn.grid(100, 200, [[1, 2], [3, 4]], col_labels=["a", "b", "c"])
        with self.assertRaisesRegex(ValueError, "two different endpoints"):
            drawn.edge((100, 100), (100, 100))
        with self.assertRaisesRegex(ValueError, "'A'"):
            drawn.path("M 10 10 A 5 5 0 0 1 20 20")

    def test_every_primitive_draws_inside_a_clean_layout(self):
        drawn = canvas(800)
        width = drawn.chip(40, 140, "capacity 8", color="green")
        self.assertGreater(width, 60)
        drawn.grid(100, 220, [[0, 1], [1, 2]], row_labels=["a", "b"], col_labels=["0", "1"],
                   states={(1, 1): "alt"})
        drawn.pointer(300, 260, "size", below=True)
        drawn.pointer(420, 260, "head", color="red", below=False)
        drawn.brace(40, 200, 360, "4 copies", above=False)
        drawn.brace(260, 460, 440, "free", color="green", above=True)
        drawn.curve(60, 520, 220, 520, label="copy")
        drawn.node(320, 560, "1", state="focus")
        drawn.node(440, 560, "2")
        drawn.edge((320, 560), (440, 560), label="w=3")
        drawn.panel(620, 80)
        self.assertEqual([], drawn.layout_issues())
        self.assertTrue(all(float(size) >= 18 for size in drawn.font_sizes()))


if __name__ == "__main__":
    unittest.main()
