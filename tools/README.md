# Texture Generation

`generate_lava_texture.py` adds deterministic, animated lava veins to a square Minecraft texture while retaining the source as the stone background.

The generator:

- Creates connected primary veins and branches that wrap across texture edges for seamless tiling.
- Preserves transparent pixels and the original cobblestone outside the vein/crust masks.
- Adds a dark crust around the lava and a traveling heat pulse through each vein.
- Produces a vertical PNG animation strip and its companion `.png.mcmeta` file.
- Uses a fixed seed so repeated runs with the same arguments produce identical output.

The vanilla cobblestone source is preserved at `texture_sources/cobblestone.png`. Keep square source textures here; do not use a generated animation strip as the input for another run.

## Setup

From the project root on Windows:

```bash
py -m venv .venv
.venv\Scripts\python -m pip install -r tools\requirements.txt
```

On macOS/Linux, use `python3 -m venv .venv` and `.venv/bin/python` instead. Pillow is the only dependency and is pinned in `requirements.txt`. The virtual environment is excluded from Git.

## Regenerate Fast Cobblestone

Windows:

```bash
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\cobblestone.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_cobblestone.png --seed 193 --frames 8 --frame-time 2 --veins 2
```

macOS/Linux:

```bash
.venv/bin/python -I tools/generate_lava_texture.py --input tools/texture_sources/cobblestone.png --output src/main/resources/assets/fastfurnacev2/textures/block/fast_cobblestone.png --seed 193 --frames 8 --frame-time 2 --veins 2
```

This writes:

- `src/main/resources/assets/fastfurnacev2/textures/block/fast_cobblestone.png`
- `src/main/resources/assets/fastfurnacev2/textures/block/fast_cobblestone.png.mcmeta`

The existing block model references the animated texture automatically; no model change is needed.

## Generate Another Lava-Vein Texture

Place a new square source PNG in `tools/texture_sources/`, then change `--input`, `--output`, and `--seed`. Different seeds produce different vein layouts. Optional controls:

- `--veins`: number of primary paths; default `2`.
- `--length`: primary path length; default is 1.4 times the texture width.
- `--branch-length`: branch length; default is one-third of the texture width.
- `--frames`: animation frame count; default `8`.
- `--frame-time`: game ticks per frame; default `2`.

Run the generator with `--help` using the virtual environment's Python for the complete command-line reference. Input and output paths must differ to protect source artwork.

## Validate the Generator

Windows:

```bash
.venv\Scripts\python -I -m unittest discover -s tools -p "test_*.py"
```

macOS/Linux:

```bash
.venv/bin/python -I -m unittest discover -s tools -p "test_*.py"
```

The tests check deterministic output, distinct animation frames, in-bounds wrapping paths, preservation of stone pixels, and source transparency.
