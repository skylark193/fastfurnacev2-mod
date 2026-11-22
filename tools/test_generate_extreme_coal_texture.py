from pathlib import Path
import unittest

from PIL import Image

from generate_extreme_coal_texture import LAVA_PALETTE, generate_frames


class ExtremeCoalTextureGeneratorTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        source = Path(__file__).parent / "texture_sources" / "coal.png"
        with Image.open(source) as image:
            cls.source = image.convert("RGBA")

    def test_generation_is_deterministic_and_animated(self) -> None:
        original = self.source.tobytes()
        first = generate_frames(self.source, 8, 420)
        second = generate_frames(self.source, 8, 420)

        self.assertEqual(self.source.tobytes(), original)
        self.assertEqual(len(first), 8)
        self.assertEqual([frame.tobytes() for frame in first], [frame.tobytes() for frame in second])
        self.assertEqual(len({frame.tobytes() for frame in first}), 8)

    def test_every_visible_pixel_is_lava_and_transparency_is_preserved(self) -> None:
        lava_colors = {pixel[:3] for pixel in LAVA_PALETTE}
        for frame in generate_frames(self.source, 8, 420):
            self.assertEqual(frame.size, self.source.size)
            for source_pixel, result_pixel in zip(
                    self.source.get_flattened_data(), frame.get_flattened_data()):
                self.assertEqual(source_pixel[3], result_pixel[3])
                if source_pixel[3] > 0:
                    self.assertIn(result_pixel[:3], lava_colors)

    def test_fully_transparent_source_is_unchanged(self) -> None:
        transparent = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        for frame in generate_frames(transparent, 8, 420):
            self.assertEqual(frame.tobytes(), transparent.tobytes())


if __name__ == "__main__":
    unittest.main()
