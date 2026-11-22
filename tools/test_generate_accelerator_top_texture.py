from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

from PIL import Image

from generate_accelerator_top_texture import (
    bolt_geometry,
    filled_bolt_points,
    generate_top_textures,
)


class AcceleratorTopTextureGeneratorTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        source = Path(__file__).parent / "texture_sources" / "smooth_stone.png"
        with Image.open(source) as image:
            cls.source = image.convert("RGBA")

    def generate(self, frames: int = 8) -> list[list[Image.Image]]:
        return generate_top_textures(self.source, frames, 2, 22, 5, 418)

    def test_generation_is_deterministic_and_animated(self) -> None:
        first = self.generate()
        second = self.generate()

        self.assertEqual(len(first), 11)
        self.assertEqual(len({frame.tobytes() for frame in first[0]}), 8)
        for first_stage, second_stage in zip(first, second):
            self.assertEqual(
                [frame.tobytes() for frame in first_stage],
                [frame.tobytes() for frame in second_stage],
            )

    def test_bolt_fills_bottom_up_without_gaps(self) -> None:
        interior, _ = bolt_geometry()
        previous: set[tuple[int, int]] = set()
        for level in range(11):
            filled = filled_bolt_points(interior, level)
            self.assertTrue(previous.issubset(filled))
            if filled:
                minimum_filled_y = min(y for _, y in filled)
                self.assertTrue(all(y >= minimum_filled_y for _, y in filled))
            previous = filled
        self.assertEqual(previous, interior)
        with self.assertRaises(ValueError):
            filled_bolt_points(interior, 11)

    def test_bolt_and_outline_stay_stationary_during_animation(self) -> None:
        stages = self.generate()
        interior, outline = bolt_geometry()
        for frames in stages:
            first = frames[0]
            for frame in frames[1:]:
                for point in interior | outline:
                    self.assertEqual(frame.getpixel(point), first.getpixel(point))

    def test_source_is_preserved_and_size_is_checked(self) -> None:
        original = self.source.tobytes()
        stages = self.generate(2)
        self.assertEqual(self.source.tobytes(), original)
        self.assertTrue(all(frame.size == self.source.size for stage in stages for frame in stage))
        with self.assertRaises(ValueError):
            generate_top_textures(Image.new("RGBA", (16, 32)), 2, 2, 22, 5, 418)

    def test_cli_rejects_an_output_that_would_overwrite_its_input(self) -> None:
        script = Path(__file__).with_name("generate_accelerator_top_texture.py")
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "stage0.png"
            self.source.save(source)
            original = source.read_bytes()
            result = subprocess.run(
                [
                    sys.executable,
                    "-I",
                    str(script),
                    "--input",
                    str(source),
                    "--output-prefix",
                    str(root / "stage"),
                ],
                cwd=root,
                capture_output=True,
                text=True,
                check=False,
            )
            self.assertNotEqual(result.returncode, 0, result.stderr)
            self.assertEqual(source.read_bytes(), original)


if __name__ == "__main__":
    unittest.main()
