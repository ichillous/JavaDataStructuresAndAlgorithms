"""Independent contrast checks for text used by both generated SVG themes."""
import unittest

from drawing import PALETTES


def luminance(hex_color):
    """WCAG relative luminance for the six-digit sRGB colors in the drawing palette."""
    channels = [int(hex_color[index:index + 2], 16) / 255 for index in (1, 3, 5)]
    linear = [channel / 12.92 if channel <= 0.04045
              else ((channel + 0.055) / 1.055) ** 2.4 for channel in channels]
    return sum(channel * weight for channel, weight in zip(linear, (0.2126, 0.7152, 0.0722)))


def contrast(foreground, background):
    darker, lighter = sorted((luminance(foreground), luminance(background)))
    return (lighter + 0.05) / (darker + 0.05)


class PaletteContrastReviewTest(unittest.TestCase):
    def test_contrast_oracle_has_standard_endpoints_and_is_symmetric(self):
        self.assertAlmostEqual(21.0, contrast("#000000", "#ffffff"))
        self.assertAlmostEqual(1.0, contrast("#2451b7", "#2451b7"))
        self.assertAlmostEqual(contrast("#2451b7", "#edf2fc"),
                               contrast("#edf2fc", "#2451b7"))

    def test_small_text_meets_contrast_floor_on_its_actual_fills(self):
        # Body/header text, secondary labels, active nodes, and KEY IDEA note labels.
        # Use the normal-text threshold even for headings: phone layouts scale SVGs down.
        pairings = (
            ("ink", "paper"), ("ink", "panel"), ("ink", "soft"), ("ink", "note"),
            ("muted", "paper"), ("muted", "panel"), ("muted", "soft"), ("muted", "note"),
            ("blue", "paper"), ("blue", "soft"),
            ("red", "paper"), ("red", "soft"),
            ("gold", "paper"), ("gold", "panel"), ("gold", "note"),
        )
        for theme, palette in PALETTES.items():
            for foreground, background in pairings:
                with self.subTest(theme=theme, foreground=foreground, background=background):
                    ratio = contrast(palette[foreground], palette[background])
                    self.assertGreaterEqual(ratio, 4.5,
                                            f"{theme}: {foreground} on {background} is {ratio:.2f}:1")


if __name__ == "__main__":
    unittest.main()
