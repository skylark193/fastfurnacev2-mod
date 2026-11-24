import random
from pathlib import Path
import unittest

from PIL import Image

from generate_lava_texture import build_veins, generate_frames


class LavaTextureGeneratorTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        source = Path(__file__).parent / "texture_sources" / "cobblestone.png"
        with Image.open(source) as image:
            cls.base = image.convert("RGBA")

    def test_generation_is_deterministic_and_animated(self) -> None:
        first, _ = generate_frames(self.base, 8, 2, 22, 5, 193)
        second, _ = generate_frames(self.base, 8, 2, 22, 5, 193)

        self.assertEqual(len(first), 8)
        self.assertEqual([frame.tobytes() for frame in first], [frame.tobytes() for frame in second])
        self.assertEqual(len({frame.tobytes() for frame in first}), 8)

    def test_veins_stay_within_tile_and_preserve_stone(self) -> None:
        veins = build_veins(self.base.width, 2, 22, 5, random.Random(193))
        self.assertTrue(veins)
        self.assertTrue(all(0 <= x < self.base.width and 0 <= y < self.base.height for x, y in veins))

        frames, _ = generate_frames(self.base, 8, 2, 22, 5, 193)
        original_pixels = list(self.base.get_flattened_data())
        for frame in frames:
            self.assertEqual(frame.size, self.base.size)
            unchanged = sum(actual == original for actual, original in zip(frame.get_flattened_data(), original_pixels))
            self.assertGreater(unchanged, self.base.width * self.base.height // 4)

    def test_transparent_source_pixels_remain_transparent(self) -> None:
        transparent = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        frames, vein_pixels = generate_frames(transparent, 8, 2, 22, 5, 193)

        self.assertEqual(vein_pixels, 0)
        for frame in frames:
            self.assertEqual(frame.tobytes(), transparent.tobytes())

    def test_deepslate_ores_and_tools_have_deterministic_visible_veins(self) -> None:
        cases = [
            ("deepslate_coal_ore", 433, 58, None),
            ("deepslate_iron_ore", 434, 67, None),
            ("deepslate_gold_ore", 435, 55, None),
            ("deepslate_diamond_ore", 436, 68, None),
            *[
                (f"{material}_{tool}", 439, lava_pixels, unchanged)
                for material in ("iron", "golden", "diamond")
                for tool, lava_pixels, unchanged in (
                    ("pickaxe", 12, 33),
                    ("shovel", 10, 25),
                    ("axe", 9, 33),
                    ("sword", 19, 41),
                )
            ],
        ]
        source_directory = Path(__file__).parent / "texture_sources"
        for filename, seed, expected_lava_pixels, expected_unchanged in cases:
            with self.subTest(source=filename):
                with Image.open(source_directory / f"{filename}.png") as image:
                    source = image.convert("RGBA")
                original_bytes = source.tobytes()
                frames, lava_pixels = generate_frames(source, 8, 2, 22, 5, seed)
                repeated, _ = generate_frames(source, 8, 2, 22, 5, seed)

                self.assertEqual(source.tobytes(), original_bytes)
                self.assertEqual(lava_pixels, expected_lava_pixels)
                self.assertEqual(len({frame.tobytes() for frame in frames}), 8)
                self.assertEqual(
                    [frame.tobytes() for frame in frames],
                    [frame.tobytes() for frame in repeated],
                )
                source_pixels = list(source.get_flattened_data())
                for frame in frames:
                    self.assertEqual(frame.size, (16, 16))
                    result_pixels = list(frame.get_flattened_data())
                    for source_pixel, result_pixel in zip(source_pixels, result_pixels):
                        self.assertEqual(source_pixel[3], result_pixel[3])
                        if source_pixel[3] == 0:
                            self.assertEqual(source_pixel, result_pixel)
                    if expected_unchanged is not None:
                        unchanged = sum(
                            source_pixel[3] > 0 and source_pixel == result_pixel
                            for source_pixel, result_pixel in zip(source_pixels, result_pixels)
                        )
                        self.assertEqual(unchanged, expected_unchanged)


if __name__ == "__main__":
    unittest.main()
