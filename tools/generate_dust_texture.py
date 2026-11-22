#!/usr/bin/env python3
"""Normalize a square Minecraft dust texture to a neutral tintable grayscale."""

from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image


def normalize_dust(source: Image.Image, shadow: int = 80, highlight: int = 255) -> Image.Image:
    image = source.convert("RGBA")
    pixels = list(image.get_flattened_data())
    luminances = [
        round(red * 0.2126 + green * 0.7152 + blue * 0.0722)
        for red, green, blue, alpha in pixels
        if alpha > 0
    ]
    if not luminances:
        return image
    if not 0 <= shadow <= highlight <= 255:
        raise ValueError("Require 0 <= shadow <= highlight <= 255")

    darkest = min(luminances)
    brightest = max(luminances)
    scale = (highlight - shadow) / max(1, brightest - darkest)
    output = []
    for red, green, blue, alpha in pixels:
        luminance = round(red * 0.2126 + green * 0.7152 + blue * 0.0722)
        gray = shadow if alpha == 0 else round(shadow + (luminance - darkest) * scale)
        gray = max(0, min(255, gray))
        output.append((gray, gray, gray, alpha))
    result = Image.new("RGBA", image.size)
    result.putdata(output)
    return result


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", required=True, type=Path, help="Square source PNG")
    parser.add_argument("--output", required=True, type=Path, help="Destination grayscale PNG")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    if args.input.resolve() == args.output.resolve():
        raise ValueError("Input and output paths must differ")
    with Image.open(args.input) as source:
        result = normalize_dust(source)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    result.save(args.output)
    Path(f"{args.output}.mcmeta").unlink(missing_ok=True)
    print(f"Generated neutral {result.width}x{result.height} dust texture: {args.output}")


if __name__ == "__main__":
    main()
