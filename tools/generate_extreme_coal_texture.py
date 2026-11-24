#!/usr/bin/env python3
"""Turn every visible source pixel into animated lava while preserving its silhouette."""

from __future__ import annotations

import argparse
import importlib.util
import json
import math
import random
from pathlib import Path

from PIL import Image


def load_lava_palette() -> tuple[tuple[int, int, int, int], ...]:
    module_path = Path(__file__).with_name("generate_lava_texture.py")
    spec = importlib.util.spec_from_file_location("generate_lava_texture", module_path)
    if spec is None or spec.loader is None:
        raise ImportError(f"Cannot load lava palette from {module_path}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module.LAVA_PALETTE


LAVA_PALETTE = load_lava_palette()


def positive_int(value: str) -> int:
    number = int(value)
    if number < 1:
        raise argparse.ArgumentTypeError("must be at least 1")
    return number


def generate_frames(source: Image.Image, frame_count: int, seed: int) -> list[Image.Image]:
    if frame_count < 1:
        raise ValueError("Frame count must be at least 1")
    image = source.convert("RGBA")
    pixels = list(image.get_flattened_data())
    luminances = [
        round(red * 0.2126 + green * 0.7152 + blue * 0.0722)
        for red, green, blue, alpha in pixels
        if alpha > 0
    ]
    darkest = min(luminances, default=0)
    brightest = max(luminances, default=0)
    variation_rng = random.Random(seed)
    variations = [variation_rng.choice((-1, 0, 0, 0, 1)) for _ in pixels]
    frames = [image.copy() for _ in range(frame_count)]

    for index, (red, green, blue, alpha) in enumerate(pixels):
        if alpha == 0:
            continue
        x = index % image.width
        y = index // image.width
        luminance = round(red * 0.2126 + green * 0.7152 + blue * 0.0722)
        source_heat = (luminance - darkest) / max(1, brightest - darkest)
        flow = (x * 3 + y * 5 + x * y % 7) / 12.0
        for frame_index, frame in enumerate(frames):
            wave = (math.sin(math.tau * (flow - frame_index / frame_count)) + 1.0) / 2.0
            heat = source_heat * 0.72 + wave * 0.28
            palette_index = round(heat * (len(LAVA_PALETTE) - 1)) + variations[index]
            palette_index = max(0, min(len(LAVA_PALETTE) - 1, palette_index))
            lava_red, lava_green, lava_blue, _ = LAVA_PALETTE[palette_index]
            frame.putpixel((x, y), (lava_red, lava_green, lava_blue, alpha))

    return frames


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", required=True, type=Path, help="Square source PNG")
    parser.add_argument("--output", required=True, type=Path, help="Destination animation-strip PNG")
    parser.add_argument("--frames", type=positive_int, default=8, help="Frames in the loop (default: 8)")
    parser.add_argument("--frame-time", type=positive_int, default=2, help="Game ticks per frame (default: 2)")
    parser.add_argument("--seed", type=int, default=420, help="Deterministic variation seed (default: 420)")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    if args.input.resolve() == args.output.resolve():
        raise SystemExit("Input and output must differ; preserve the square source texture.")
    with Image.open(args.input) as source:
        base = source.convert("RGBA")
    if base.width != base.height:
        raise SystemExit(f"Source must be square, got {base.width}×{base.height} pixels.")

    frames = generate_frames(base, args.frames, args.seed)
    strip = Image.new("RGBA", (base.width, base.height * args.frames))
    for frame_index, frame in enumerate(frames):
        strip.paste(frame, (0, frame_index * base.height))
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
    print(f"Generated {args.frames} all-lava frames at {base.width}×{base.height}: {args.output}")
    print(f"Wrote animation metadata: {metadata_path}")


if __name__ == "__main__":
    main()
