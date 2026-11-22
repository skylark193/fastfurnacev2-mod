from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

from PIL import Image

from generate_dust_texture import normalize_dust


class DustTextureGeneratorTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        source = Path(__file__).parent / "texture_sources" / "gunpowder.png"
        with Image.open(source) as image:
            cls.source = image.convert("RGBA")

    def test_normalization_is_deterministic_and_preserves_transparency(self) -> None:
        first = normalize_dust(self.source)
        second = normalize_dust(self.source)

        self.assertEqual(first.tobytes(), second.tobytes())
        self.assertEqual(first.size, self.source.size)
        for source_pixel, result_pixel in zip(
                self.source.get_flattened_data(), first.get_flattened_data()):
            self.assertEqual(source_pixel[3], result_pixel[3])
            self.assertEqual(result_pixel[0], result_pixel[1])
            self.assertEqual(result_pixel[1], result_pixel[2])

    def test_normalization_uses_full_tintable_range(self) -> None:
        result = normalize_dust(self.source)
        grays = [pixel[0] for pixel in result.get_flattened_data() if pixel[3] > 0]

        self.assertEqual(min(grays), 80)
        self.assertEqual(max(grays), 255)

    def test_cli_removes_stale_animation_metadata(self) -> None:
        script = Path(__file__).with_name("generate_dust_texture.py")
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            output = root / "dust.png"
            stale_metadata = Path(f"{output}.mcmeta")
            stale_metadata.write_text('{"animation": {"frames": [0, 1]}}', encoding="utf-8")
            subprocess.run(
                [
                    sys.executable,
                    "-I",
                    str(script),
                    "--input",
                    str(Path(__file__).parent / "texture_sources" / "gunpowder.png"),
                    "--output",
                    str(output),
                ],
                cwd=root,
                capture_output=True,
                text=True,
                check=True,
            )
            self.assertFalse(stale_metadata.exists())
            with Image.open(output) as result:
                self.assertEqual(result.size, self.source.size)


if __name__ == "__main__":
    unittest.main()
