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


if __name__ == "__main__":
    unittest.main()
