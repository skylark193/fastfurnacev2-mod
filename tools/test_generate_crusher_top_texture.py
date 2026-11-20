import math
from pathlib import Path
import unittest

from PIL import Image

from generate_crusher_top_texture import CENTER, generate_top_textures


class CrusherTopTextureGeneratorTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        source = Path(__file__).parent / "texture_sources" / "netherrack.png"
        with Image.open(source) as image:
            cls.source = image.convert("RGBA")

    def test_generation_is_deterministic_and_animated(self) -> None:
        first_idle, first_frames = generate_top_textures(self.source, 8)
        second_idle, second_frames = generate_top_textures(self.source, 8)

        self.assertEqual(first_idle.tobytes(), second_idle.tobytes())
        self.assertEqual(first_idle.tobytes(), first_frames[0].tobytes())
        self.assertEqual([frame.tobytes() for frame in first_frames], [frame.tobytes() for frame in second_frames])
        self.assertEqual(len({frame.tobytes() for frame in first_frames}), 8)

    def test_housing_stays_stationary(self) -> None:
        idle, frames = generate_top_textures(self.source, 8)
        stationary_pixels = [
            (x, y)
            for y in range(idle.height)
            for x in range(idle.width)
            if math.hypot(x - CENTER, y - CENTER) > 4.8
        ]

        self.assertTrue(stationary_pixels)
        for frame in frames:
            for point in stationary_pixels:
                self.assertEqual(frame.getpixel(point), idle.getpixel(point))

    def test_source_is_preserved_and_size_is_checked(self) -> None:
        original = self.source.tobytes()
        idle, frames = generate_top_textures(self.source, 8)

        self.assertEqual(self.source.tobytes(), original)
        self.assertEqual(idle.size, self.source.size)
        self.assertTrue(all(frame.size == self.source.size for frame in frames))
        with self.assertRaises(ValueError):
            generate_top_textures(Image.new("RGBA", (16, 32)), 8)


if __name__ == "__main__":
    unittest.main()
