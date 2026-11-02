"""Theoretical normalized growth, deliberately not a measured performance chart."""
import math


def draw_growth(c, fixtures):
    del fixtures
    left, top, right, bottom = 74, 174, 466, 430
    c.text(left, 151, "Normalized work (log scale)", size=20)
    functions = [
        ("log2(n+1)", lambda n: math.log2(n + 1), "blue", False),
        ("n", lambda n: n, "ink", False),
        ("n log2(n+1)", lambda n: n * math.log2(n + 1), "blue", True),
        ("n²", lambda n: n * n, "red", False),
        ("2^(n-1)", lambda n: 2 ** (n - 1), "red", True),
    ]
    x = lambda n: left + (n - 1) * (right - left) / 15
    y = lambda value: bottom - math.log10(value) * (bottom - top) / 5
    for power in range(6):
        value = 10 ** power
        level = y(value)
        c.line(left, level, right, level, width=1)
        c.text(left - 10, level + 6, str(value) if power < 4 else f"10^{power}",
               anchor="end", size=18, color="muted")
    c.line(left, top, left, bottom, color="ink")
    c.line(left, bottom, right, bottom, color="ink")
    for n in (1, 4, 8, 12, 16):
        c.text(x(n), bottom + 28, n, anchor="middle", size=18)
    c.text((left + right) / 2, bottom + 60, "Input size n (elements)", anchor="middle")
    for label, function, color, dashed in functions:
        points = [(x(n), y(function(n))) for n in range(1, 17)]
        c.path("M " + " L ".join(f"{px:.2f} {py:.2f}" for px, py in points), color=color,
               dashed=dashed, width=3)
    for index, (label, function, color, dashed) in enumerate(functions):
        row = 524 + index * 39
        c.line(42, row - 6, 90, row - 6, color=color, width=3, dashed=dashed)
        c.text(106, row, label)
        c.text(472, row, f"{function(16):.1f}", anchor="end", size=18)
    c.text(472, 496, "at n=16", anchor="end", size=18, color="muted")
    c.paragraph(30, 744, "All five functions equal 1 at n=1. Logarithmic y spacing compresses large gaps; it does not make them small.", width=43)
    c.note(840, "These are chosen mathematical functions in arbitrary work units. Big-O is a bound, not a stopwatch or a speedup claim.")


FIGURES = [{
    "id": "complexity-growth", "title": "Compare growth, not timings", "category": "Complexity",
    "description": "Five illustrative functions share a value of one at input size one. A logarithmic work axis shows logarithmic, linear, n log n, quadratic and exponential growth without implying benchmark timings.",
    "lesson": "complexity-analysis/time-complexity.md",
    "keyIdea": "Theoretical normalized work; constants, machines and measured runtime are outside this comparison.",
    "height": 1040, "draw": draw_growth,
}]
