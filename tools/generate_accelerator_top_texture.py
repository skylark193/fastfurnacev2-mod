#!/usr/bin/env python3
"""Generate animated Accelerator tops with a bottom-up lightning-bolt fill."""

from __future__ import annotations

import argparse
import importlib.util
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter


def load_lava_generator():
    path = Path(__file__).with_name("generate_lava_texture.py")
    spec = importlib.util.spec_from_file_location("fastfurnacev2_lava_texture", path)
    if spec is None or spec.loader is None:
        raise ImportError(f"Cannot load lava generator from {path}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


LAVA_GENERATOR = load_lava_generator()
generate_frames = LAVA_GENERATOR.generate_frames
positive_int = LAVA_GENERATOR.positive_int

RGBA = tuple[int, int, int, int]
Point = tuple[int, int]

OUTLINE_COLOR: RGBA = (48, 42, 35, 255)
EMPTY_BOLT_COLOR: RGBA = (83, 88, 91, 255)
FILL_COLORS: tuple[RGBA, ...] = (
    (255, 244, 128, 255),
    (255, 220, 50, 255),
    (255, 183, 25, 255),
)
BOLT_POLYGON = ((9, 1), (4, 9), (8, 9), (6, 15), (13, 6), (9, 6), (11, 1))


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", required=True, type=Path, help="Square smooth-stone source PNG")
    parser.add_argument("--output-prefix", required=True, type=Path, help="Output path ending in the top filename prefix")
    parser.add_argument("--frames", type=positive_int, default=8, help="Animation frames (default: 8)")
    parser.add_argument("--frame-time", type=positive_int, default=2, help="Game ticks per frame (default: 2)")
    parser.add_argument("--seed", type=int, default=418, help="Lava layout seed (default: 418)")
    parser.add_argument("--veins", type=positive_int, default=2, help="Primary lava paths (default: 2)")
    parser.add_argument("--length", type=positive_int, help="Steps per primary lava path")
    parser.add_argument("--branch-length", type=positive_int, help="Steps per lava branch")
    return parser.parse_args()


def bolt_geometry(size: int = 16) -> tuple[set[Point], set[Point]]:
    if size != 16:
        raise ValueError("The Accelerator lightning bolt requires a 16×16 texture")
    bolt = Image.new("1", (size, size))
    ImageDraw.Draw(bolt).polygon(BOLT_POLYGON, fill=1)
    expanded = bolt.filter(ImageFilter.MaxFilter(3))
    interior = {(x, y) for y in range(size) for x in range(size) if bolt.getpixel((x, y))}
    outline = {
        (x, y)
        for y in range(size)
        for x in range(size)
        if expanded.getpixel((x, y)) and (x, y) not in interior
    }
    return interior, outline


def filled_bolt_points(interior: set[Point], level: int, max_level: int = 10) -> set[Point]:
    if not 0 <= level <= max_level:
        raise ValueError(f"Bolt level must be between 0 and {max_level}")
    if level == 0:
        return set()
    minimum_y = min(y for _, y in interior)
    maximum_y = max(y for _, y in interior)
    height = maximum_y - minimum_y + 1
    fill_height = round(level * height / max_level)
    threshold = maximum_y - fill_height + 1
    return {point for point in interior if point[1] >= threshold}


def apply_bolt(base: Image.Image, level: int, max_level: int = 10) -> Image.Image:
    interior, outline = bolt_geometry(base.width)
    filled = filled_bolt_points(interior, level, max_level)
    frame = base.copy()
    for point in outline:
        frame.putpixel(point, OUTLINE_COLOR)
    for x, y in interior:
        color = FILL_COLORS[(x + y) % len(FILL_COLORS)] if (x, y) in filled else EMPTY_BOLT_COLOR
        frame.putpixel((x, y), color)
    return frame


def generate_top_textures(
    base: Image.Image,
    frame_count: int,
    vein_count: int,
    length: int,
    branch_length: int,
    seed: int,
    max_level: int = 10,
) -> list[list[Image.Image]]:
    if base.width != base.height:
        raise ValueError("The Accelerator source must be square")
    base_frames, _ = generate_frames(
        base,
        frame_count=frame_count,
        vein_count=vein_count,
        length=length,
        branch_length=branch_length,
        seed=seed,
    )
    return [
        [apply_bolt(frame, level, max_level) for frame in base_frames]
        for level in range(max_level + 1)
    ]


def main() -> None:
    args = parse_args()
    outputs = [Path(f"{args.output_prefix}{level}.png") for level in range(11)]
    output_paths = {path.resolve() for output in outputs for path in (output, Path(f"{output}.mcmeta"))}
    if args.input.resolve() in output_paths:
        raise SystemExit("Input path must differ from every generated texture and metadata path.")

    with Image.open(args.input) as source:
        base = source.convert("RGBA")
    size = base.width
    length = args.length or max(1, round(size * 1.4))
    branch_length = args.branch_length or max(1, size // 3)
    stages = generate_top_textures(
        base,
        frame_count=args.frames,
        vein_count=args.veins,
        length=length,
        branch_length=branch_length,
        seed=args.seed,
    )

    args.output_prefix.parent.mkdir(parents=True, exist_ok=True)
    metadata = {
        "animation": {
            "frametime": args.frame_time,
            "interpolate": False,
            "frames": list(range(args.frames)),
        }
    }
    for level, frames in enumerate(stages):
        output = outputs[level]
        strip = Image.new("RGBA", (size, size * args.frames))
        for frame_index, frame in enumerate(frames):
            strip.paste(frame, (0, frame_index * size))
        strip.save(output, format="PNG", optimize=True)
        Path(f"{output}.mcmeta").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
        print(f"Generated lightning-bolt level {level}: {output}")


if __name__ == "__main__":
    main()
