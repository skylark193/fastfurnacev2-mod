#!/usr/bin/env python3
"""Generate a looping Minecraft texture with animated lava veins.

The source image is used as the unchanged stone background. The output is a
vertical animation strip with a companion .png.mcmeta file.
"""

from __future__ import annotations

import argparse
import json
import math
import random
from pathlib import Path

from PIL import Image

RGBA = tuple[int, int, int, int]
Point = tuple[int, int]

LAVA_PALETTE: tuple[RGBA, ...] = (
    (174, 32, 3, 255),
    (210, 43, 3, 255),
    (237, 59, 4, 255),
    (250, 83, 5, 255),
    (255, 112, 8, 255),
    (255, 147, 16, 255),
    (255, 188, 35, 255),
    (255, 224, 83, 255),
)
CRUST_COLOR = (102, 20, 8)
DIRECTIONS = ((1, 0), (0, 1), (-1, 0), (0, -1))
TAU = math.tau


def positive_int(value: str) -> int:
    number = int(value)
    if number < 1:
        raise argparse.ArgumentTypeError("must be at least 1")
    return number


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", required=True, type=Path, help="Square source PNG")
    parser.add_argument("--output", required=True, type=Path, help="Destination animation-strip PNG")
    parser.add_argument("--frames", type=positive_int, default=8, help="Frames in the loop (default: 8)")
    parser.add_argument("--frame-time", type=positive_int, default=2, help="Game ticks per frame (default: 2)")
    parser.add_argument("--seed", type=int, default=193, help="Deterministic layout seed (default: 193)")
    parser.add_argument("--veins", type=positive_int, default=2, help="Primary vein paths (default: 2)")
    parser.add_argument("--length", type=positive_int, help="Steps per primary path (default: 1.4 × texture width)")
    parser.add_argument("--branch-length", type=positive_int, help="Steps per branch (default: texture width ÷ 3)")
    return parser.parse_args()


def build_veins(
    size: int,
    count: int,
    length: int,
    branch_length: int,
    rng: random.Random,
) -> dict[Point, int]:
    """Create connected paths on a torus, storing a flow position per pixel."""
    flow_positions: dict[Point, int] = {}

    def add_path(points: list[Point], phase: int) -> None:
        for step, (x, y) in enumerate(points):
            point = (x % size, y % size)
            flow_positions.setdefault(point, phase + step)

    for _ in range(count):
        x = rng.randrange(size)
        y = rng.randrange(size)
        direction = rng.randrange(len(DIRECTIONS))
        phase = rng.randrange(length)
        path: list[Point] = []

        for step in range(length):
            path.append((x, y))

            if step > 1 and step % max(4, size // 3) == 0:
                branch_direction = (direction + rng.choice((-1, 1))) % len(DIRECTIONS)
                branch_dx, branch_dy = DIRECTIONS[branch_direction]
                branch_x, branch_y = x, y
                branch: list[Point] = []
                for _ in range(branch_length):
                    branch.append((branch_x, branch_y))
                    branch_x += branch_dx
                    branch_y += branch_dy
                add_path(branch, phase + step)

            if rng.random() < 0.35:
                direction = (direction + rng.choice((-1, 1))) % len(DIRECTIONS)
            dx, dy = DIRECTIONS[direction]
            x += dx
            y += dy

        add_path(path, phase)

    return flow_positions


def blend_with_crust(base: RGBA) -> RGBA:
    red, green, blue, alpha = base
    opacity = 0.68
    return (
        round(red * (1.0 - opacity) + CRUST_COLOR[0] * opacity),
        round(green * (1.0 - opacity) + CRUST_COLOR[1] * opacity),
        round(blue * (1.0 - opacity) + CRUST_COLOR[2] * opacity),
        alpha,
    )


def generate_frames(
    base: Image.Image,
    frame_count: int,
    vein_count: int,
    length: int,
    branch_length: int,
    seed: int,
) -> tuple[list[Image.Image], int]:
    size = base.width
    rng = random.Random(seed)
    flow_positions = build_veins(size, vein_count, length, branch_length, rng)
    flow_positions = {
        point: flow
        for point, flow in flow_positions.items()
        if base.getpixel(point)[3] > 0
    }

    crust: set[Point] = set()
    for x, y in flow_positions:
        for offset_x in (-1, 0, 1):
            for offset_y in (-1, 0, 1):
                point = ((x + offset_x) % size, (y + offset_y) % size)
                if point not in flow_positions and base.getpixel(point)[3] > 0:
                    crust.add(point)

    variation_rng = random.Random(seed ^ 0x5A17)
    variations = {
        point: variation_rng.choice((-1, 0, 0, 0, 1))
        for point in flow_positions
    }
    flow_period = max(4, size // 2)
    frames: list[Image.Image] = []

    for frame_index in range(frame_count):
        frame = base.copy()
        for point in crust:
            frame.putpixel(point, blend_with_crust(base.getpixel(point)))

        for point, flow in flow_positions.items():
            wave = math.sin(TAU * (flow / flow_period - frame_index / frame_count))
            heat = (wave + 1.0) / 2.0
            palette_index = round(heat * (len(LAVA_PALETTE) - 1)) + variations[point]
            palette_index = max(0, min(len(LAVA_PALETTE) - 1, palette_index))
            red, green, blue, _ = LAVA_PALETTE[palette_index]
            alpha = base.getpixel(point)[3]
            frame.putpixel(point, (red, green, blue, alpha))

        frames.append(frame)

    return frames, len(flow_positions)


def main() -> None:
    args = parse_args()
    if args.input.resolve() == args.output.resolve():
        raise SystemExit("Input and output must differ; preserve the square source texture.")

    with Image.open(args.input) as source:
        base = source.convert("RGBA")

    if base.width != base.height:
        raise SystemExit(f"Source must be square, got {base.width}×{base.height} pixels.")

    size = base.width
    length = args.length or max(1, round(size * 1.4))
    branch_length = args.branch_length or max(1, size // 3)
    frames, vein_pixel_count = generate_frames(
        base,
        frame_count=args.frames,
        vein_count=args.veins,
        length=length,
        branch_length=branch_length,
        seed=args.seed,
    )

    strip = Image.new("RGBA", (size, size * args.frames))
    for frame_index, frame in enumerate(frames):
        strip.paste(frame, (0, frame_index * size))

    args.output.parent.mkdir(parents=True, exist_ok=True)
    strip.save(args.output, format="PNG", optimize=True)

    metadata = {
        "animation": {
            "frametime": args.frame_time,
            "interpolate": False,
            "frames": list(range(args.frames)),
        }
    }
    metadata_path = Path(f"{args.output}.mcmeta")
    metadata_path.write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")

    print(
        f"Generated {args.frames} frames at {size}×{size} with "
        f"{vein_pixel_count} lava pixels per frame: {args.output}"
    )
    print(f"Wrote animation metadata: {metadata_path}")


if __name__ == "__main__":
    main()
