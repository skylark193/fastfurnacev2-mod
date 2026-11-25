import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

from PIL import Image

from generate_energy_textures import (
    BAR_INTERIORS,
    BASE_FACES,
    FRAME_COUNT,
    TIERS,
    charge_overlay,
    create_source,
    generate_all,
    load_metal_sources,
)


class EnergyTextureGeneratorTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.temporary_directory = tempfile.TemporaryDirectory()
        root = Path(cls.temporary_directory.name)
        cls.source_directory = root / "sources"
        cls.output_directory = root / "outputs"
        generate_all(cls.source_directory, cls.output_directory)

    @classmethod
    def tearDownClass(cls) -> None:
        cls.temporary_directory.cleanup()

    def test_expected_source_and_output_layout_and_dimensions(self) -> None:
        source_paths = sorted(self.source_directory.glob("*.png"))
        output_paths = sorted(self.output_directory.glob("*.png"))
        expected_sources = {f"{tier}_{face}.png" for tier in TIERS for face in BASE_FACES}
        expected_outputs = expected_sources | {f"battery_charge_{level}.png" for level in range(11)}

        self.assertEqual({path.name for path in source_paths}, expected_sources)
        self.assertEqual({path.name for path in output_paths}, expected_outputs)
        self.assertEqual(len([name for name in expected_sources if "generator" in name]), 16)
        self.assertEqual(len([name for name in expected_sources if "battery" in name]), 12)
        for path in source_paths:
            with self.subTest(source=path.name), Image.open(path) as image:
                self.assertEqual(image.size, (16, 16))
        for path in output_paths:
            with self.subTest(output=path.name), Image.open(path) as image:
                expected_size = (16, 16) if "charge" in path.name else (16, 128)
                self.assertEqual(image.size, expected_size)

    def test_animation_metadata_is_complete_and_overlays_are_static(self) -> None:
        expected = {
            "animation": {
                "frametime": 2,
                "interpolate": False,
                "frames": list(range(FRAME_COUNT)),
            }
        }
        for tier in TIERS:
            for face in BASE_FACES:
                name = f"{tier}_{face}.png"
                with self.subTest(texture=name):
                    metadata = json.loads(Path(f"{self.output_directory / name}.mcmeta").read_text(encoding="utf-8"))
                    self.assertEqual(metadata, expected)
        for level in range(11):
            self.assertFalse(Path(f"{self.output_directory / f'battery_charge_{level}.png'}.mcmeta").exists())

    def test_every_base_texture_has_eight_distinct_deterministic_frames(self) -> None:
        for tier in TIERS:
            for face in BASE_FACES:
                path = self.output_directory / f"{tier}_{face}.png"
                with self.subTest(texture=path.name), Image.open(path) as strip:
                    frames = [
                        strip.crop((0, index * 16, 16, (index + 1) * 16)).tobytes()
                        for index in range(FRAME_COUNT)
                    ]
                self.assertEqual(len(set(frames)), FRAME_COUNT)

        before = {
            path.relative_to(path.parent.parent): path.read_bytes()
            for directory in (self.source_directory, self.output_directory)
            for path in directory.iterdir()
            if path.is_file()
        }
        generate_all(self.source_directory, self.output_directory)
        after = {
            path.relative_to(path.parent.parent): path.read_bytes()
            for directory in (self.source_directory, self.output_directory)
            for path in directory.iterdir()
            if path.is_file()
        }
        self.assertEqual(after, before)

    def test_functional_details_stay_stationary_and_active_front_is_brighter(self) -> None:
        metals = load_metal_sources()
        for tier in TIERS:
            for face in BASE_FACES:
                _, protected = create_source(tier, face, metals[tier])
                with Image.open(self.source_directory / f"{tier}_{face}.png") as source_image:
                    source = source_image.convert("RGBA")
                with Image.open(self.output_directory / f"{tier}_{face}.png") as strip:
                    for index in range(FRAME_COUNT):
                        frame = strip.crop((0, index * 16, 16, (index + 1) * 16))
                        for point in protected:
                            self.assertEqual(frame.getpixel(point), source.getpixel(point))

            with Image.open(self.source_directory / f"{tier}_generator_front.png") as idle_image:
                idle = idle_image.convert("RGB")
            with Image.open(self.source_directory / f"{tier}_generator_front_on.png") as active_image:
                active = active_image.convert("RGB")
            idle_luminance = sum(sum(idle.getpixel((x, 6))) for x in (6, 7, 9, 10))
            active_luminance = sum(sum(active.getpixel((x, 6))) for x in (6, 7, 9, 10))
            self.assertGreater(active_luminance, idle_luminance)

    def test_charge_overlays_fill_both_interiors_bottom_up(self) -> None:
        previous = set()
        for level in range(11):
            image = charge_overlay(level)
            expected = {(x, y) for x, y in BAR_INTERIORS if y >= 14 - level}
            if level == 0:
                expected = set()
            self.assertEqual(image.size, (16, 16))
            self.assertTrue(previous.issubset(expected))
            self.assertEqual(sum(pixel[3] > 0 for pixel in image.get_flattened_data()), level * 4)
            for y in range(16):
                for x in range(16):
                    pixel = image.getpixel((x, y))
                    if (x, y) in expected:
                        self.assertEqual(pixel[3], 255)
                    else:
                        self.assertEqual(pixel, (0, 0, 0, 0))
            previous = expected
        self.assertEqual(previous, BAR_INTERIORS)
        self.assertEqual(charge_overlay(0).getextrema()[3], (0, 0))
        with self.assertRaises(ValueError):
            charge_overlay(11)

    def test_cli_rejects_source_output_collisions_before_writing(self) -> None:
        script = Path(__file__).with_name("generate_energy_textures.py")
        with tempfile.TemporaryDirectory() as directory:
            shared = Path(directory) / "shared"
            result = subprocess.run(
                [
                    sys.executable,
                    "-I",
                    str(script),
                    "--source-dir",
                    str(shared),
                    "--output-dir",
                    str(shared),
                ],
                cwd=directory,
                capture_output=True,
                text=True,
                check=False,
            )
            self.assertNotEqual(result.returncode, 0, result.stderr)
            self.assertFalse(shared.exists())


if __name__ == "__main__":
    unittest.main()
