#!/usr/bin/env python3
"""Create square energy sources and generate animated generators and batteries."""

from __future__ import annotations

import argparse
import importlib.util
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

RGBA = tuple[int, int, int, int]
RGB = tuple[int, int, int]
Point = tuple[int, int]

TEXTURE_SIZE = 16
FRAME_COUNT = 8
FRAME_TIME = 2
VEIN_COUNT = 2
VEIN_LENGTH = 22
BRANCH_LENGTH = 5
TIERS = ("fast", "iron_fast", "gold_fast", "diamond_fast")
TIER_SEEDS = {
    "fast": 443,
    "iron_fast": 444,
    "gold_fast": 445,
    "diamond_fast": 446,
}
BASE_FACES = (
    "generator_front",
    "generator_front_on",
    "generator_side",
    "generator_top",
    "battery_side",
    "battery_top",
    "battery_bottom",
)
METAL_SOURCES = {
    "fast": "smooth_stone.png",
    "iron_fast": "iron_block.png",
    "gold_fast": "gold_block.png",
    "diamond_fast": "diamond_block.png",
}
TOOLS_DIRECTORY = Path(__file__).resolve().parent
PROJECT_ROOT = TOOLS_DIRECTORY.parent
DEFAULT_SOURCE_DIRECTORY = TOOLS_DIRECTORY / "texture_sources" / "energy"
DEFAULT_OUTPUT_DIRECTORY = (
    PROJECT_ROOT
    / "src"
    / "main"
    / "resources"
    / "assets"
    / "fastfurnacev2"
    / "textures"
    / "block"
)
BAR_INTERIORS = frozenset(
    {(x, y) for x in (2, 3, 12, 13) for y in range(4, 14)}
)


def load_lava_generator():
    """Load the sibling module by path so Python -I does not hide it."""
    path = Path(__file__).with_name("generate_lava_texture.py")
    spec = importlib.util.spec_from_file_location("fastfurnacev2_energy_lava", path)
    if spec is None or spec.loader is None:
        raise ImportError(f"Cannot load lava generator from {path}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


LAVA_GENERATOR = load_lava_generator()


def scale_color(color: RGB, factor: float) -> RGBA:
    return tuple(max(0, min(255, round(channel * factor))) for channel in color) + (255,)


def mix_with_white(color: RGB, amount: float) -> RGBA:
    return tuple(round(channel * (1.0 - amount) + 255 * amount) for channel in color) + (255,)


def average_color(image: Image.Image) -> RGB:
    pixels = [pixel for pixel in image.convert("RGBA").get_flattened_data() if pixel[3] > 0]
    if not pixels:
        raise ValueError("Tier metal source must contain visible pixels")
    return tuple(round(sum(pixel[channel] for pixel in pixels) / len(pixels)) for channel in range(3))


def rectangle_points(bounds: tuple[int, int, int, int]) -> set[Point]:
    left, top, right, bottom = bounds
    return {(x, y) for y in range(top, bottom + 1) for x in range(left, right + 1)}


def make_panel(metal: Image.Image, tier: str, brightness: float) -> tuple[Image.Image, RGBA, RGBA, RGBA]:
    image = metal.convert("RGBA").copy()
    if image.size != (TEXTURE_SIZE, TEXTURE_SIZE):
        raise ValueError(f"Tier metal source must be 16×16, got {image.width}×{image.height}")
    tint = (0.78, 0.88, 0.96) if tier == "fast" else (1.0, 1.0, 1.0)
    for y in range(TEXTURE_SIZE):
        for x in range(TEXTURE_SIZE):
            red, green, blue, _ = image.getpixel((x, y))
            image.putpixel(
                (x, y),
                (
                    min(255, round(red * brightness * tint[0])),
                    min(255, round(green * brightness * tint[1])),
                    min(255, round(blue * brightness * tint[2])),
                    255,
                ),
            )

    average = average_color(image)
    dark = scale_color(average, 0.38)
    light = mix_with_white(average, 0.38)
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, 15, 15), outline=dark)
    draw.line((1, 1, 14, 1), fill=light)
    draw.line((1, 1, 1, 14), fill=light)
    draw.line((1, 14, 14, 14), fill=dark)
    draw.line((14, 1, 14, 14), fill=dark)
    return image, dark, average + (255,), light


def draw_rivets(image: Image.Image, light: RGBA, dark: RGBA) -> set[Point]:
    draw = ImageDraw.Draw(image)
    points = {(1, 1), (14, 1), (1, 14), (14, 14)}
    for point in points:
        draw.point(point, fill=light)
    for point in ((2, 1), (14, 2), (1, 13), (13, 14)):
        draw.point(point, fill=dark)
        points.add(point)
    return points


def draw_bolt(
    image: Image.Image,
    polygon: tuple[Point, ...],
    colors: tuple[RGBA, ...],
) -> set[Point]:
    bolt = Image.new("1", image.size)
    ImageDraw.Draw(bolt).polygon(polygon, fill=1)
    expanded = bolt.filter(ImageFilter.MaxFilter(3))
    interior = {
        (x, y)
        for y in range(TEXTURE_SIZE)
        for x in range(TEXTURE_SIZE)
        if bolt.getpixel((x, y))
    }
    outline = {
        (x, y)
        for y in range(TEXTURE_SIZE)
        for x in range(TEXTURE_SIZE)
        if expanded.getpixel((x, y)) and (x, y) not in interior
    }
    for point in outline:
        image.putpixel(point, (38, 39, 38, 255))
    for x, y in interior:
        image.putpixel((x, y), colors[(x + y) % len(colors)])
    return interior | outline


def generator_front_source(metal: Image.Image, tier: str, active: bool) -> tuple[Image.Image, set[Point]]:
    image, dark, middle, light = make_panel(metal, tier, 0.74 if active else 0.64)
    protected = draw_rivets(image, light, dark)
    draw = ImageDraw.Draw(image)
    chamber = (3, 3, 12, 13)
    protected |= rectangle_points(chamber)
    draw.rectangle(chamber, fill=dark, outline=light)
    draw.rectangle((4, 4, 11, 12), fill=(25, 29, 31, 255))
    draw.rectangle((4, 4, 11, 4), fill=middle)

    coil_colors = (
        ((255, 224, 83, 255), (255, 112, 8, 255))
        if active
        else ((132, 55, 28, 255), (82, 38, 27, 255))
    )
    for row, y in enumerate((6, 8, 10)):
        draw.line((5, y, 10, y), fill=coil_colors[row % 2])
        if active:
            draw.point((5 + row * 2, y), fill=(255, 244, 160, 255))
    for x in (5, 8, 11):
        draw.line((x, 5, x, 12), fill=(42, 47, 49, 255))
    draw.line((4, 12, 11, 12), fill=(15, 18, 20, 255))
    draw.point((4, 4), fill=(255, 188, 35, 255) if active else (104, 43, 25, 255))
    draw.point((11, 4), fill=(255, 112, 8, 255) if active else (76, 36, 26, 255))
    return image, protected


def generator_side_source(metal: Image.Image, tier: str) -> tuple[Image.Image, set[Point]]:
    image, dark, middle, light = make_panel(metal, tier, 0.72)
    protected = draw_rivets(image, light, dark)
    draw = ImageDraw.Draw(image)
    draw.rectangle((3, 3, 12, 12), fill=dark, outline=light)
    draw.rectangle((4, 4, 11, 11), fill=middle)
    for y in (5, 7, 9):
        draw.line((5, y, 10, y), fill=dark)
        draw.line((5, y + 1, 10, y + 1), fill=light)
        protected |= rectangle_points((5, y, 10, y + 1))
    return image, protected


def generator_top_source(metal: Image.Image, tier: str) -> tuple[Image.Image, set[Point]]:
    image, dark, middle, light = make_panel(metal, tier, 0.82)
    protected = draw_rivets(image, light, dark)
    draw = ImageDraw.Draw(image)
    draw.rectangle((3, 3, 12, 12), fill=dark, outline=light)
    draw.rectangle((4, 4, 11, 11), fill=middle)
    for x in (5, 7, 9, 11):
        draw.line((x, 4, x, 11), fill=dark)
        protected |= rectangle_points((x, 4, x, 11))
    draw.line((4, 4, 11, 4), fill=light)
    return image, protected


def battery_side_source(metal: Image.Image, tier: str) -> tuple[Image.Image, set[Point]]:
    image, dark, _, light = make_panel(metal, tier, 0.64)
    protected = draw_rivets(image, light, dark)
    draw = ImageDraw.Draw(image)
    for left in (1, 11):
        right = left + 3
        draw.rectangle((left, 3, right, 14), fill=dark, outline=light)
        draw.rectangle((left + 1, 4, right - 1, 13), fill=(17, 24, 27, 255))
        draw.point((left + 1, 4), fill=(47, 59, 61, 255))
        protected |= rectangle_points((left, 3, right, 14))
    protected |= draw_bolt(
        image,
        ((8, 4), (5, 9), (7, 9), (6, 13), (10, 7), (8, 7), (9, 4)),
        ((255, 224, 83, 255), (255, 147, 16, 255)),
    )
    return image, protected


def battery_top_source(metal: Image.Image, tier: str) -> tuple[Image.Image, set[Point]]:
    image, dark, middle, light = make_panel(metal, tier, 0.78)
    protected = draw_rivets(image, light, dark)
    draw = ImageDraw.Draw(image)
    for bounds in ((2, 2, 5, 5), (10, 2, 13, 5)):
        draw.rectangle(bounds, fill=dark, outline=light)
        left, top, right, bottom = bounds
        draw.rectangle((left + 1, top + 1, right - 1, bottom - 1), fill=middle)
        protected |= rectangle_points(bounds)
    draw.line((3, 3, 4, 3), fill=light)
    draw.point((3, 2), fill=light)
    draw.line((11, 3, 12, 3), fill=dark)
    protected |= draw_bolt(
        image,
        ((9, 4), (5, 10), (8, 10), (7, 14), (12, 7), (9, 7), (10, 4)),
        ((255, 244, 128, 255), (255, 220, 50, 255), (255, 183, 25, 255)),
    )
    return image, protected


def battery_bottom_source(metal: Image.Image, tier: str) -> tuple[Image.Image, set[Point]]:
    image, dark, middle, light = make_panel(metal, tier, 0.60)
    protected = draw_rivets(image, light, dark)
    draw = ImageDraw.Draw(image)
    draw.rectangle((3, 3, 12, 12), fill=dark, outline=light)
    draw.rectangle((4, 4, 11, 11), fill=middle)
    return image, protected


def create_source(tier: str, face: str, metal: Image.Image) -> tuple[Image.Image, set[Point]]:
    if face == "generator_front":
        return generator_front_source(metal, tier, active=False)
    if face == "generator_front_on":
        return generator_front_source(metal, tier, active=True)
    if face == "generator_side":
        return generator_side_source(metal, tier)
    if face == "generator_top":
        return generator_top_source(metal, tier)
    if face == "battery_side":
        return battery_side_source(metal, tier)
    if face == "battery_top":
        return battery_top_source(metal, tier)
    if face == "battery_bottom":
        return battery_bottom_source(metal, tier)
    raise ValueError(f"Unknown energy texture face: {face}")


def load_metal_sources() -> dict[str, Image.Image]:
    source_root = TOOLS_DIRECTORY / "texture_sources"
    metals: dict[str, Image.Image] = {}
    for tier, filename in METAL_SOURCES.items():
        with Image.open(source_root / filename) as image:
            metals[tier] = image.convert("RGBA")
    return metals


def animation_metadata() -> dict[str, dict[str, object]]:
    return {
        "animation": {
            "frametime": FRAME_TIME,
            "interpolate": False,
            "frames": list(range(FRAME_COUNT)),
        }
    }


def save_animation(output: Path, frames: list[Image.Image]) -> None:
    strip = Image.new("RGBA", (TEXTURE_SIZE, TEXTURE_SIZE * FRAME_COUNT))
    for index, frame in enumerate(frames):
        strip.paste(frame, (0, index * TEXTURE_SIZE))
    strip.save(output, format="PNG", optimize=True)
    Path(f"{output}.mcmeta").write_text(
        json.dumps(animation_metadata(), indent=2) + "\n",
        encoding="utf-8",
    )


def charge_overlay(level: int) -> Image.Image:
    if not 0 <= level <= 10:
        raise ValueError("Battery charge level must be between 0 and 10")
    image = Image.new("RGBA", (TEXTURE_SIZE, TEXTURE_SIZE), (0, 0, 0, 0))
    if level == 0:
        return image
    first_row = 14 - level
    for x, y in BAR_INTERIORS:
        if y < first_row:
            continue
        color = (255, 220, 50, 255) if x in (2, 12) else (255, 147, 16, 255)
        if y <= 6:
            color = (255, 244, 128, 255) if x in (2, 12) else (255, 188, 35, 255)
        image.putpixel((x, y), color)
    return image


def planned_paths(source_directory: Path, output_directory: Path) -> tuple[list[Path], list[Path]]:
    sources = [source_directory / f"{tier}_{face}.png" for tier in TIERS for face in BASE_FACES]
    outputs = [output_directory / source.name for source in sources]
    outputs.extend(output_directory / f"battery_charge_{level}.png" for level in range(11))
    return sources, outputs


def generate_all(source_directory: Path = DEFAULT_SOURCE_DIRECTORY, output_directory: Path = DEFAULT_OUTPUT_DIRECTORY) -> list[Path]:
    """Recreate all square sources, animated base textures, and static overlays."""
    source_directory = source_directory.resolve()
    output_directory = output_directory.resolve()
    sources, outputs = planned_paths(source_directory, output_directory)
    source_paths = {path for source in sources for path in (source, Path(f"{source}.mcmeta"))}
    output_paths = {path for output in outputs for path in (output, Path(f"{output}.mcmeta"))}
    collisions = source_paths & output_paths
    if collisions:
        raise ValueError(f"Source and output paths must differ: {sorted(collisions)[0]}")

    source_directory.mkdir(parents=True, exist_ok=True)
    output_directory.mkdir(parents=True, exist_ok=True)
    metals = load_metal_sources()
    protected_details: dict[str, set[Point]] = {}
    for tier in TIERS:
        for face in BASE_FACES:
            source, protected = create_source(tier, face, metals[tier])
            source_path = source_directory / f"{tier}_{face}.png"
            source.save(source_path, format="PNG", optimize=True)
            Path(f"{source_path}.mcmeta").unlink(missing_ok=True)
            protected_details[f"{tier}_{face}"] = protected

    for tier in TIERS:
        for face in BASE_FACES:
            name = f"{tier}_{face}"
            source_path = source_directory / f"{name}.png"
            with Image.open(source_path) as image:
                base = image.convert("RGBA")
            if base.size != (TEXTURE_SIZE, TEXTURE_SIZE):
                raise ValueError(f"Energy source must be 16×16: {source_path}")
            frames, _ = LAVA_GENERATOR.generate_frames(
                base,
                frame_count=FRAME_COUNT,
                vein_count=VEIN_COUNT,
                length=VEIN_LENGTH,
                branch_length=BRANCH_LENGTH,
                seed=TIER_SEEDS[tier],
            )
            protected = protected_details[name]
            for frame in frames:
                for point in protected:
                    frame.putpixel(point, base.getpixel(point))
            if len({frame.tobytes() for frame in frames}) != FRAME_COUNT:
                raise RuntimeError(f"Energy texture has no distinct eight-frame animation: {name}")
            save_animation(output_directory / f"{name}.png", frames)

    for level in range(11):
        output = output_directory / f"battery_charge_{level}.png"
        charge_overlay(level).save(output, format="PNG", optimize=True)
        Path(f"{output}.mcmeta").unlink(missing_ok=True)

    print(f"Created {len(sources)} square sources in {source_directory}")
    print(f"Generated {len(sources)} animated base textures and 11 static charge overlays in {output_directory}")
    return sources + outputs


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-dir", type=Path, default=DEFAULT_SOURCE_DIRECTORY, help="Square source directory")
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_OUTPUT_DIRECTORY, help="Block texture output directory")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    try:
        generate_all(args.source_dir, args.output_dir)
    except ValueError as error:
        raise SystemExit(str(error)) from error


if __name__ == "__main__":
    main()
