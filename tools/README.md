# Texture Generation

`generate_lava_texture.py` adds deterministic, animated lava veins to a square Minecraft texture while retaining the source as the background.

The generator:

- Creates connected primary veins and branches that wrap across texture edges for seamless tiling.
- Preserves transparent pixels and the original source pixels outside the vein/crust masks.
- Adds a dark crust around the lava and a traveling heat pulse through each vein.
- Produces a vertical PNG animation strip and its companion `.png.mcmeta` file.
- Uses a fixed seed so repeated runs with the same arguments produce identical output.

The vanilla cobblestone, coal, coal block, Netherrack, and gunpowder sources are preserved at `texture_sources/cobblestone.png`, `texture_sources/coal.png`, `texture_sources/coal_block.png`, `texture_sources/netherrack.png`, and `texture_sources/gunpowder.png`. Vanilla furnace front, lit front, side, and top sources are preserved under `texture_sources/furnace/`. The iron, gold, and diamond block sources are preserved as `texture_sources/iron_block.png`, `texture_sources/gold_block.png`, and `texture_sources/diamond_block.png`. Each `<tier>_furnace/` directory contains that tier's four square furnace sources, combining the metal block pattern with vanilla furnace shading while preserving burning-fire pixels. Keep square source textures here; do not use a generated animation strip as the input for another run.

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

This writes the animated block texture and its `.png.mcmeta` metadata. The existing block model references the animated texture automatically.

## Regenerate Fast Coal

Windows:

```bash
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\coal.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_coal.png --seed 415 --frames 8 --frame-time 2 --veins 2
```

macOS/Linux:

```bash
.venv/bin/python -I tools/generate_lava_texture.py --input tools/texture_sources/coal.png --output src/main/resources/assets/fastfurnacev2/textures/item/fast_coal.png --seed 415 --frames 8 --frame-time 2 --veins 2
```

This writes the animated item texture and its `.png.mcmeta` metadata. The existing Fast Coal item model references the texture automatically.

## Regenerate Block of Fast Coal

Windows:

```bash
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\coal_block.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_coal_block.png --seed 416 --frames 8 --frame-time 2 --veins 2
```

macOS/Linux:

```bash
.venv/bin/python -I tools/generate_lava_texture.py --input tools/texture_sources/coal_block.png --output src/main/resources/assets/fastfurnacev2/textures/block/fast_coal_block.png --seed 416 --frames 8 --frame-time 2 --veins 2
```

This writes the animated block texture and its `.png.mcmeta` metadata. The block and item definitions reference the same `cube_all` block model automatically.

## Regenerate Fast Crusher

Windows:

```bash
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\netherrack.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_crusher.png --seed 417 --frames 8 --frame-time 2 --veins 2
```

macOS/Linux:

```bash
.venv/bin/python -I tools/generate_lava_texture.py --input tools/texture_sources/netherrack.png --output src/main/resources/assets/fastfurnacev2/textures/block/fast_crusher.png --seed 417 --frames 8 --frame-time 2 --veins 2
```

This writes the animated all-sided block texture and its `.png.mcmeta` metadata.

## Regenerate Furnace Textures

Each furnace texture is generated independently from its square source. All use seed **311** intentionally: identical seeds and generator settings give every furnace tier and lit state the same vein/crust mask and animation, so their lava veins tile together. Changing the seed for only one texture breaks that alignment. From the project root on Windows, run:

```bash
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\furnace\furnace_front.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_furnace_front.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\furnace\furnace_front_on.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_furnace_front_on.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\furnace\furnace_side.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_furnace_side.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\furnace\furnace_top.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_furnace_top.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\iron_furnace\furnace_front.png --output src\main\resources\assets\fastfurnacev2\textures\block\iron_fast_furnace_front.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\iron_furnace\furnace_front_on.png --output src\main\resources\assets\fastfurnacev2\textures\block\iron_fast_furnace_front_on.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\iron_furnace\furnace_side.png --output src\main\resources\assets\fastfurnacev2\textures\block\iron_fast_furnace_side.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\iron_furnace\furnace_top.png --output src\main\resources\assets\fastfurnacev2\textures\block\iron_fast_furnace_top.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\gold_furnace\furnace_front.png --output src\main\resources\assets\fastfurnacev2\textures\block\gold_fast_furnace_front.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\gold_furnace\furnace_front_on.png --output src\main\resources\assets\fastfurnacev2\textures\block\gold_fast_furnace_front_on.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\gold_furnace\furnace_side.png --output src\main\resources\assets\fastfurnacev2\textures\block\gold_fast_furnace_side.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\gold_furnace\furnace_top.png --output src\main\resources\assets\fastfurnacev2\textures\block\gold_fast_furnace_top.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\diamond_furnace\furnace_front.png --output src\main\resources\assets\fastfurnacev2\textures\block\diamond_fast_furnace_front.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\diamond_furnace\furnace_front_on.png --output src\main\resources\assets\fastfurnacev2\textures\block\diamond_fast_furnace_front_on.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\diamond_furnace\furnace_side.png --output src\main\resources\assets\fastfurnacev2\textures\block\diamond_fast_furnace_side.png --seed 311
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\diamond_furnace\furnace_top.png --output src\main\resources\assets\fastfurnacev2\textures\block\diamond_fast_furnace_top.png --seed 311
```

On macOS/Linux, use `.venv/bin/python` and forward slashes. The defaults produce eight frames with a two-tick frame time and two primary veins for each texture. Every tier's normal and lit models share its animated side and top textures, while its blockstate switches between the unlit and burning front models. Each command writes both its vertical PNG strip and sibling `.png.mcmeta` file.

## Regenerate the Crushed Ore Dust Base

The shared crushed-ore texture is a neutral grayscale version of vanilla gunpowder. The game applies a different derived tint to each discovered raw-ore variant at render time; per-mod texture files are not needed.

Windows:

```bash
.venv\Scripts\python -I tools\generate_dust_texture.py --input tools\texture_sources\gunpowder.png --output src\main\resources\assets\fastfurnacev2\textures\item\crushed_ore.png
```

macOS/Linux:

```bash
.venv/bin/python -I tools/generate_dust_texture.py --input tools/texture_sources/gunpowder.png --output src/main/resources/assets/fastfurnacev2/textures/item/crushed_ore.png
```

The generator preserves the source silhouette and transparency while remapping visible pixels to an 80–255 grayscale range.

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

The tests check deterministic output, distinct animation frames, in-bounds wrapping paths, preservation of source pixels, and source transparency. They do not prove byte-for-byte reproduction of every checked-in texture; compare regenerated assets separately when needed.
