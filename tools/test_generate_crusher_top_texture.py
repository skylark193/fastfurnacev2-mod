import math
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

from PIL import Image

from generate_crusher_top_texture import CENTER, generate_top_textures


class CrusherTopTextureGeneratorTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        source = Path(__file__).parent / "texture_sources" / "netherrack.png"
        with Image.open(source) as image:
            cls.source = image.convert("RGBA")
        cls.trims = {}
        for tier in ("iron", "gold", "diamond"):
            trim_path = Path(__file__).parent / "texture_sources" / f"{tier}_block.png"
            with Image.open(trim_path) as image:
                cls.trims[tier] = image.convert("RGBA")

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

    def test_tier_trim_is_visible_distinct_and_stationary(self) -> None:
        base_idle, _ = generate_top_textures(self.source, 8)
        tier_outputs = set()
        for tier, trim in self.trims.items():
            original_trim = trim.tobytes()
            idle, frames = generate_top_textures(self.source, 8, trim)
            tier_outputs.add(idle.tobytes())

            self.assertNotEqual(idle.tobytes(), base_idle.tobytes(), tier)
            self.assertEqual(idle.getpixel((1, 6)), trim.getpixel((1, 6)), tier)
            self.assertEqual(trim.tobytes(), original_trim, tier)
            for frame in frames:
                for point in ((1, 6), (14, 6), (6, 1), (6, 14)):
                    self.assertEqual(frame.getpixel(point), idle.getpixel(point), tier)

        self.assertEqual(len(tier_outputs), len(self.trims))
        with self.assertRaises(ValueError):
            generate_top_textures(self.source, 8, Image.new("RGBA", (16, 32)))

    def test_source_is_preserved_and_size_is_checked(self) -> None:
        original = self.source.tobytes()
        idle, frames = generate_top_textures(self.source, 8)

        self.assertEqual(self.source.tobytes(), original)
        self.assertEqual(idle.size, self.source.size)
        self.assertTrue(all(frame.size == self.source.size for frame in frames))
        with self.assertRaises(ValueError):
            generate_top_textures(Image.new("RGBA", (16, 32)), 8)

    def test_cli_removes_stale_idle_animation_metadata(self) -> None:
        script = Path(__file__).with_name("generate_crusher_top_texture.py")
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            idle = root / "idle.png"
            active = root / "active.png"
            stale_metadata = Path(f"{idle}.mcmeta")
            stale_metadata.write_text('{"animation": {"frames": [0, 1]}}', encoding="utf-8")
            subprocess.run(
                [
                    sys.executable,
                    "-I",
                    str(script),
                    "--input",
                    str(Path(__file__).parent / "texture_sources" / "netherrack.png"),
                    "--idle-output",
                    str(idle),
                    "--active-output",
                    str(active),
                ],
                cwd=root,
                capture_output=True,
                text=True,
                check=True,
            )
            self.assertFalse(stale_metadata.exists())
            self.assertTrue(Path(f"{active}.mcmeta").is_file())


if __name__ == "__main__":
    unittest.main()
