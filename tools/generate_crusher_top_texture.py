#!/usr/bin/env python3
"""Generate static idle and animated lit top-down blender textures."""

from __future__ import annotations

import argparse
import json
import math
from pathlib import Path

from PIL import Image, ImageDraw

RGBA = tuple[int, int, int, int]
TAU = math.tau
TEXTURE_SIZE = 16
CENTER = (TEXTURE_SIZE - 1) / 2
LIQUID_PALETTE: tuple[RGBA, ...] = (
    (91, 20, 12, 255),
    (135, 29, 12, 255),
    (183, 43, 10, 255),
    (222, 65, 9, 255),
    (244, 101, 13, 255),
    (255, 153, 29, 255),
)


def positive_int(value: str) -> int:
    number = int(value)
    if number < 1:
        raise argparse.ArgumentTypeError("must be at least 1")
    return number


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", required=True, type=Path, help="16×16 square Netherrack source PNG")
    parser.add_argument("--idle-output", required=True, type=Path, help="Destination static PNG")
    parser.add_argument("--active-output", required=True, type=Path, help="Destination animation-strip PNG")
    parser.add_argument("--frames", type=positive_int, default=8, help="Frames per revolution (default: 8)")
    parser.add_argument("--frame-time", type=positive_int, default=1, help="Game ticks per frame (default: 1)")
    return parser.parse_args()


def point_on_circle(radius: float, angle: float) -> tuple[float, float]:
    return CENTER + radius * math.cos(angle), CENTER + radius * math.sin(angle)


def create_housing(source: Image.Image) -> Image.Image:
    image = source.copy()
    for y in range(TEXTURE_SIZE):
        for x in range(TEXTURE_SIZE):
            red, green, blue, alpha = image.getpixel((x, y))
            image.putpixel((x, y), (round(red * 0.55), round(green * 0.55), round(blue * 0.55), alpha))

    draw = ImageDraw.Draw(image)
    draw.polygon(
        [(2, 0), (13, 0), (15, 2), (15, 13), (13, 15), (2, 15), (0, 13), (0, 2)],
        fill=(35, 42, 48, 255),
        outline=(18, 22, 26, 255),
    )
    draw.polygon(
        [(3, 1), (12, 1), (14, 3), (14, 12), (12, 14), (3, 14), (1, 12), (1, 3)],
        fill=(78, 88, 96, 255),
    )
    draw.ellipse((2, 2, 13, 13), fill=(31, 42, 47, 255), outline=(171, 184, 188, 255))
    draw.ellipse((3, 3, 12, 12), fill=(20, 30, 34, 255), outline=(83, 122, 130, 255))

    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        draw.point((x, y), fill=(211, 219, 218, 255))
    for x, y in ((4, 3), (3, 4), (3, 5)):
        draw.point((x, y), fill=(158, 205, 207, 255))
    return image


def render_frame(housing: Image.Image, phase: float) -> Image.Image:
    image = housing.copy()
    for y in range(TEXTURE_SIZE):
        for x in range(TEXTURE_SIZE):
            dx = x - CENTER
            dy = y - CENTER
            radius = math.hypot(dx, dy)
            if radius > 4.45:
                continue
            angle = math.atan2(dy, dx)
            swirl = math.sin(3.0 * (angle - TAU * phase) + radius * 1.4)
            fleck = math.sin(5.0 * (angle + TAU * phase) - radius * 2.1)
            heat = max(0.0, min(1.0, 0.46 + swirl * 0.31 + fleck * 0.17))
            palette_index = round(heat * (len(LIQUID_PALETTE) - 1))
            image.putpixel((x, y), LIQUID_PALETTE[palette_index])

    draw = ImageDraw.Draw(image)
    blade_angle = TAU * phase - math.pi / 2
    for blade_index in range(4):
        angle = blade_angle + blade_index * TAU / 4
        blade = [
            point_on_circle(1.0, angle - 0.42),
            point_on_circle(3.9, angle - 0.18),
            point_on_circle(3.9, angle + 0.18),
            point_on_circle(1.35, angle + 0.48),
        ]
        draw.polygon(blade, fill=(139, 154, 161, 255), outline=(40, 49, 55, 255))
        draw.line(
            [point_on_circle(1.6, angle - 0.16), point_on_circle(3.45, angle - 0.16)],
            fill=(222, 229, 226, 255),
        )

    draw.ellipse((6, 6, 9, 9), fill=(48, 57, 62, 255), outline=(20, 25, 28, 255))
    draw.rectangle((7, 7, 8, 8), fill=(206, 215, 213, 255))
    return image


def generate_top_textures(source: Image.Image, frame_count: int) -> tuple[Image.Image, list[Image.Image]]:
    base = source.convert("RGBA")
    if base.size != (TEXTURE_SIZE, TEXTURE_SIZE):
        raise ValueError(f"Source must be {TEXTURE_SIZE}×{TEXTURE_SIZE}, got {base.width}×{base.height}")
    if frame_count < 1:
        raise ValueError("Frame count must be at least 1")

    housing = create_housing(base)
    idle = render_frame(housing, 0.0)
    frames = [render_frame(housing, frame_index / frame_count) for frame_index in range(frame_count)]
    return idle, frames


def main() -> None:
    args = parse_args()
    paths = (args.input.resolve(), args.idle_output.resolve(), args.active_output.resolve())
    if len(set(paths)) != len(paths):
        raise SystemExit("Input, idle output, and active output paths must differ.")

    with Image.open(args.input) as source:
        idle, frames = generate_top_textures(source, args.frames)

    strip = Image.new("RGBA", (TEXTURE_SIZE, TEXTURE_SIZE * args.frames))
    for frame_index, frame in enumerate(frames):
        strip.paste(frame, (0, frame_index * TEXTURE_SIZE))

    args.idle_output.parent.mkdir(parents=True, exist_ok=True)
    args.active_output.parent.mkdir(parents=True, exist_ok=True)
    idle.save(args.idle_output, format="PNG", optimize=True)
    strip.save(args.active_output, format="PNG", optimize=True)

    metadata = {
        "animation": {
            "frametime": args.frame_time,
            "interpolate": False,
            "frames": list(range(args.frames)),
        }
    }
    metadata_path = Path(f"{args.active_output}.mcmeta")
    metadata_path.write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
    print(f"Generated static idle top: {args.idle_output}")
    print(f"Generated {args.frames} animated processing frames: {args.active_output}")
    print(f"Wrote animation metadata: {metadata_path}")


if __name__ == "__main__":
    main()
