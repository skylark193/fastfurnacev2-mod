# Texture Generation

`generate_lava_texture.py` adds deterministic, animated lava veins to a square Minecraft texture while retaining the source as the background.

The lava-vein generator:

- Creates connected primary veins and branches that wrap across texture edges for seamless tiling.
- Preserves transparent pixels and the original source pixels outside the vein/crust masks.
- Adds a dark crust around the lava and a traveling heat pulse through each vein.
- Produces a vertical PNG animation strip and its companion `.png.mcmeta` file.
- Uses a fixed seed so repeated runs with the same arguments produce identical output.

The vanilla cobblestone, coal, coal ore, coal block, smooth stone, Netherrack, and gunpowder sources are preserved at `texture_sources/cobblestone.png`, `texture_sources/coal.png`, `texture_sources/coal_ore.png`, `texture_sources/coal_block.png`, `texture_sources/smooth_stone.png`, `texture_sources/netherrack.png`, and `texture_sources/gunpowder.png`. Regular and deepslate Fast material ore sources are preserved as `iron_ore.png`, `deepslate_iron_ore.png`, `gold_ore.png`, `deepslate_gold_ore.png`, `diamond_ore.png`, and `deepslate_diamond_ore.png`; `deepslate_coal_ore.png` supplies Deepslate Extreme Coal Ore. Raw and finished Fast material sources are `raw_iron.png`, `raw_gold.png`, `raw_diamond.png`, `iron_ingot.png`, `gold_ingot.png`, and `diamond.png`. Vanilla tool sources are the four `iron_*`, four `golden_*`, and four `diamond_*` PNGs for pickaxes, shovels, axes, and swords in the same directory. Vanilla furnace front, lit front, side, and top sources are preserved under `texture_sources/furnace/`. The iron, gold, and diamond block sources are preserved as `texture_sources/iron_block.png`, `texture_sources/gold_block.png`, and `texture_sources/diamond_block.png`. Each `<tier>_furnace/` directory contains that tier's four square furnace sources, combining the metal block pattern with vanilla furnace shading while preserving burning-fire pixels. Keep square source textures here; do not use a generated animation strip as the input for another run.

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

## Regenerate Extreme Coal Ore and Extreme Coal

Extreme Coal Ore uses the standard lava-vein generator with seed **419**. Extreme Coal uses seed **420** and a separate generator that preserves the coal silhouette and alpha channel while replacing every visible pixel with an animated lava-palette color. It loads the shared palette directly from the sibling lava generator so it works under Python's isolated `-I` mode.

Windows:

```bash
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\coal_ore.png --output src\main\resources\assets\fastfurnacev2\textures\block\extreme_coal_ore.png --seed 419 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_extreme_coal_texture.py --input tools\texture_sources\coal.png --output src\main\resources\assets\fastfurnacev2\textures\item\extreme_coal.png --seed 420 --frames 8 --frame-time 2
```

macOS/Linux:

```bash
.venv/bin/python -I tools/generate_lava_texture.py --input tools/texture_sources/coal_ore.png --output src/main/resources/assets/fastfurnacev2/textures/block/extreme_coal_ore.png --seed 419 --frames 8 --frame-time 2 --veins 2
.venv/bin/python -I tools/generate_extreme_coal_texture.py --input tools/texture_sources/coal.png --output src/main/resources/assets/fastfurnacev2/textures/item/extreme_coal.png --seed 420 --frames 8 --frame-time 2
```

Both commands write eight-frame vertical strips and sibling `.png.mcmeta` files with two ticks per frame and no interpolation. The Extreme Coal generator's `--seed`, `--frames`, and `--frame-time` options control deterministic heat variation and animation timing.

## Regenerate Deepslate Ores and Block of Extreme Coal

Deepslate Extreme Coal Ore uses seed **433**, Deepslate Fast Iron Ore **434**, Deepslate Fast Gold Ore **435**, and Deepslate Fast Diamond Ore **436**. Block of Extreme Coal uses seed **437** with the all-lava generator, preserving the coal-block source's alpha and luminance detail while changing every visible pixel's color over time. All five outputs have eight frames at two ticks per frame with no interpolation.

Windows:

```bash
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\deepslate_coal_ore.png --output src\main\resources\assets\fastfurnacev2\textures\block\deepslate_extreme_coal_ore.png --seed 433 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\deepslate_iron_ore.png --output src\main\resources\assets\fastfurnacev2\textures\block\deepslate_fast_iron_ore.png --seed 434 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\deepslate_gold_ore.png --output src\main\resources\assets\fastfurnacev2\textures\block\deepslate_fast_gold_ore.png --seed 435 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\deepslate_diamond_ore.png --output src\main\resources\assets\fastfurnacev2\textures\block\deepslate_fast_diamond_ore.png --seed 436 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_extreme_coal_texture.py --input tools\texture_sources\coal_block.png --output src\main\resources\assets\fastfurnacev2\textures\block\extreme_coal_block.png --seed 437 --frames 8 --frame-time 2
```

On macOS/Linux, use `.venv/bin/python` and forward slashes with the same arguments.

## Regenerate Fast Tools and Swords

All twelve tool textures use shared seed **439**, eight frames, two ticks per frame, no interpolation, and two primary veins. The shared seed intentionally gives identical tool silhouettes the same vein placement across material tiers.

Windows:

```bash
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\iron_pickaxe.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_iron_pickaxe.png --seed 439 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\iron_shovel.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_iron_shovel.png --seed 439 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\iron_axe.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_iron_axe.png --seed 439 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\iron_sword.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_iron_sword.png --seed 439 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\golden_pickaxe.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_gold_pickaxe.png --seed 439 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\golden_shovel.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_gold_shovel.png --seed 439 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\golden_axe.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_gold_axe.png --seed 439 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\golden_sword.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_gold_sword.png --seed 439 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\diamond_pickaxe.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_diamond_pickaxe.png --seed 439 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\diamond_shovel.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_diamond_shovel.png --seed 439 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\diamond_axe.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_diamond_axe.png --seed 439 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\diamond_sword.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_diamond_sword.png --seed 439 --frames 8 --frame-time 2 --veins 2
```

On macOS/Linux, use `.venv/bin/python` and forward slashes with the same arguments. The checked-in tests cover all twelve concrete outputs.

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

## Regenerate Fast Material Textures

The Fast ores, raw materials, finished materials, and storage blocks use seeds **421–432** in material order. Every command writes an eight-frame vertical strip with two ticks per frame, no interpolation, and two primary veins.

| Output | Source | Seed |
|---|---|---:|
| `block/fast_iron_ore.png` | `iron_ore.png` | 421 |
| `item/raw_fast_iron.png` | `raw_iron.png` | 422 |
| `item/fast_iron_ingot.png` | `iron_ingot.png` | 423 |
| `block/fast_iron_block.png` | `iron_block.png` | 424 |
| `block/fast_gold_ore.png` | `gold_ore.png` | 425 |
| `item/raw_fast_gold.png` | `raw_gold.png` | 426 |
| `item/fast_gold_ingot.png` | `gold_ingot.png` | 427 |
| `block/fast_gold_block.png` | `gold_block.png` | 428 |
| `block/fast_diamond_ore.png` | `diamond_ore.png` | 429 |
| `item/raw_fast_diamond.png` | `raw_diamond.png` | 430 |
| `item/fast_diamond.png` | `diamond.png` | 431 |
| `block/fast_diamond_block.png` | `diamond_block.png` | 432 |

Windows, from the project root:

```bash
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\iron_ore.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_iron_ore.png --seed 421 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\raw_iron.png --output src\main\resources\assets\fastfurnacev2\textures\item\raw_fast_iron.png --seed 422 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\iron_ingot.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_iron_ingot.png --seed 423 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\iron_block.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_iron_block.png --seed 424 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\gold_ore.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_gold_ore.png --seed 425 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\raw_gold.png --output src\main\resources\assets\fastfurnacev2\textures\item\raw_fast_gold.png --seed 426 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\gold_ingot.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_gold_ingot.png --seed 427 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\gold_block.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_gold_block.png --seed 428 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\diamond_ore.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_diamond_ore.png --seed 429 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\raw_diamond.png --output src\main\resources\assets\fastfurnacev2\textures\item\raw_fast_diamond.png --seed 430 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\diamond.png --output src\main\resources\assets\fastfurnacev2\textures\item\fast_diamond.png --seed 431 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\diamond_block.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_diamond_block.png --seed 432 --frames 8 --frame-time 2 --veins 2
```

On macOS/Linux, use `.venv/bin/python` and forward slashes with the same arguments.

## Regenerate Accelerator Textures

The Accelerator uses seed **418** for its smooth-stone body and all eleven top stages. Reusing the seed gives the tops the same animated vein layout as the body. The top generator loads the lava generator directly from its sibling file so it also works under Python's isolated `-I` mode.

Windows:

```bash
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\smooth_stone.png --output src\main\resources\assets\fastfurnacev2\textures\block\accelerator.png --seed 418 --frames 8 --frame-time 2 --veins 2
.venv\Scripts\python -I tools\generate_accelerator_top_texture.py --input tools\texture_sources\smooth_stone.png --output-prefix src\main\resources\assets\fastfurnacev2\textures\block\accelerator_top_ --seed 418 --frames 8 --frame-time 2 --veins 2
```

macOS/Linux:

```bash
.venv/bin/python -I tools/generate_lava_texture.py --input tools/texture_sources/smooth_stone.png --output src/main/resources/assets/fastfurnacev2/textures/block/accelerator.png --seed 418 --frames 8 --frame-time 2 --veins 2
.venv/bin/python -I tools/generate_accelerator_top_texture.py --input tools/texture_sources/smooth_stone.png --output-prefix src/main/resources/assets/fastfurnacev2/textures/block/accelerator_top_ --seed 418 --frames 8 --frame-time 2 --veins 2
```

The first command writes the eight-frame body texture. The second writes `accelerator_top_0.png` through `accelerator_top_10.png`, each with eight frames and sibling animation metadata. It rejects an input path that collides with any generated PNG or metadata path before writing, protecting the square source artwork. The lightning-bolt outline and current fill remain stationary within each animation; only the lava background moves. Higher levels fill more of the bolt from bottom to top. The blockstate selects these models from `bolt_level`, independently of the inherited `lit` state. Each model puts its stage texture on the top face only and retains the body texture on the front, sides, and bottom.

## Regenerate Extremely Fast Alloyer and Ingot Textures

The Alloyer body and all eleven top stages use seed **440**; the Extremely Fast Ingot uses seed **441**. Both use the all-lava generator, which preserves the square source's silhouette and alpha while replacing every visible pixel with an animated shared-palette lava color. The Alloyer top generator's `--all-lava` option combines that background with the same stationary lightning-bolt geometry and eleven-stage bottom-up fill used by the Accelerator. Do not substitute the ordinary lava-vein generator.

Windows:

```bash
.venv\Scripts\python -I tools\generate_extreme_coal_texture.py --input tools\texture_sources\smooth_stone.png --output src\main\resources\assets\fastfurnacev2\textures\block\extremely_fast_alloyer.png --seed 440 --frames 8 --frame-time 2
.venv\Scripts\python -I tools\generate_accelerator_top_texture.py --input tools\texture_sources\smooth_stone.png --output-prefix src\main\resources\assets\fastfurnacev2\textures\block\extremely_fast_alloyer_top_ --seed 440 --frames 8 --frame-time 2 --all-lava
.venv\Scripts\python -I tools\generate_extreme_coal_texture.py --input tools\texture_sources\iron_ingot.png --output src\main\resources\assets\fastfurnacev2\textures\item\extremely_fast_ingot.png --seed 441 --frames 8 --frame-time 2
.venv\Scripts\python -I tools\generate_extreme_coal_texture.py --input tools\texture_sources\diamond_pickaxe.png --output src\main\resources\assets\fastfurnacev2\textures\item\extremely_fast_pickaxe.png --seed 442 --frames 8 --frame-time 2
.venv\Scripts\python -I tools\generate_extreme_coal_texture.py --input tools\texture_sources\diamond_shovel.png --output src\main\resources\assets\fastfurnacev2\textures\item\extremely_fast_shovel.png --seed 442 --frames 8 --frame-time 2
.venv\Scripts\python -I tools\generate_extreme_coal_texture.py --input tools\texture_sources\diamond_axe.png --output src\main\resources\assets\fastfurnacev2\textures\item\extremely_fast_axe.png --seed 442 --frames 8 --frame-time 2
.venv\Scripts\python -I tools\generate_extreme_coal_texture.py --input tools\texture_sources\diamond_sword.png --output src\main\resources\assets\fastfurnacev2\textures\item\extremely_fast_sword.png --seed 442 --frames 8 --frame-time 2
```

On macOS/Linux, use `.venv/bin/python` and forward slashes with the same arguments.

The body and ingot commands write eight-frame strips with two ticks per frame and no interpolation. The top command writes `extremely_fast_alloyer_top_0.png` through `extremely_fast_alloyer_top_10.png`, each with eight frames and sibling metadata. The bolt outline and current fill remain stationary within each animation while the all-lava background moves. The same input/output and metadata collision checks as the Accelerator top generator apply.

## Regenerate Fast Crusher Sides and Bottom

Windows:

```bash
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\netherrack.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_crusher.png --seed 417 --frames 8 --frame-time 2 --veins 2
```

macOS/Linux:

```bash
.venv/bin/python -I tools/generate_lava_texture.py --input tools/texture_sources/netherrack.png --output src/main/resources/assets/fastfurnacev2/textures/block/fast_crusher.png --seed 417 --frames 8 --frame-time 2 --veins 2
```

This writes the animated side/bottom block texture and its `.png.mcmeta` metadata.

## Regenerate the Fast Crusher Blender Top

The top generator draws a metal housing, glass rim, four-blade rotor, and lava-colored contents over the preserved 16×16 Netherrack source. It writes a static idle PNG without animation metadata and a separate eight-frame processing strip with one tick per frame. The rotor completes one revolution per loop; the housing remains unchanged in every frame. The optional `--trim` argument accepts a 16×16 iron, gold, or diamond block source and applies its pixels only to the stationary outer housing around the blender. The Netherrack corners, glass, and animated center remain unchanged.

Windows:

```bash
.venv\Scripts\python -I tools\generate_crusher_top_texture.py --input tools\texture_sources\netherrack.png --idle-output src\main\resources\assets\fastfurnacev2\textures\block\fast_crusher_top.png --active-output src\main\resources\assets\fastfurnacev2\textures\block\fast_crusher_top_on.png --frames 8 --frame-time 1
.venv\Scripts\python -I tools\generate_crusher_top_texture.py --input tools\texture_sources\netherrack.png --trim tools\texture_sources\iron_block.png --idle-output src\main\resources\assets\fastfurnacev2\textures\block\iron_fast_crusher_top.png --active-output src\main\resources\assets\fastfurnacev2\textures\block\iron_fast_crusher_top_on.png --frames 8 --frame-time 1
.venv\Scripts\python -I tools\generate_crusher_top_texture.py --input tools\texture_sources\netherrack.png --trim tools\texture_sources\gold_block.png --idle-output src\main\resources\assets\fastfurnacev2\textures\block\gold_fast_crusher_top.png --active-output src\main\resources\assets\fastfurnacev2\textures\block\gold_fast_crusher_top_on.png --frames 8 --frame-time 1
.venv\Scripts\python -I tools\generate_crusher_top_texture.py --input tools\texture_sources\netherrack.png --trim tools\texture_sources\diamond_block.png --idle-output src\main\resources\assets\fastfurnacev2\textures\block\diamond_fast_crusher_top.png --active-output src\main\resources\assets\fastfurnacev2\textures\block\diamond_fast_crusher_top_on.png --frames 8 --frame-time 1
```

macOS/Linux:

```bash
.venv/bin/python -I tools/generate_crusher_top_texture.py --input tools/texture_sources/netherrack.png --idle-output src/main/resources/assets/fastfurnacev2/textures/block/fast_crusher_top.png --active-output src/main/resources/assets/fastfurnacev2/textures/block/fast_crusher_top_on.png --frames 8 --frame-time 1
.venv/bin/python -I tools/generate_crusher_top_texture.py --input tools/texture_sources/netherrack.png --trim tools/texture_sources/iron_block.png --idle-output src/main/resources/assets/fastfurnacev2/textures/block/iron_fast_crusher_top.png --active-output src/main/resources/assets/fastfurnacev2/textures/block/iron_fast_crusher_top_on.png --frames 8 --frame-time 1
.venv/bin/python -I tools/generate_crusher_top_texture.py --input tools/texture_sources/netherrack.png --trim tools/texture_sources/gold_block.png --idle-output src/main/resources/assets/fastfurnacev2/textures/block/gold_fast_crusher_top.png --active-output src/main/resources/assets/fastfurnacev2/textures/block/gold_fast_crusher_top_on.png --frames 8 --frame-time 1
.venv/bin/python -I tools/generate_crusher_top_texture.py --input tools/texture_sources/netherrack.png --trim tools/texture_sources/diamond_block.png --idle-output src/main/resources/assets/fastfurnacev2/textures/block/diamond_fast_crusher_top.png --active-output src/main/resources/assets/fastfurnacev2/textures/block/diamond_fast_crusher_top_on.png --frames 8 --frame-time 1
```

Each crusher blockstate selects its static top for `lit=false` and animated top for `lit=true`. Keep every `<tier>_fast_crusher_top.png.mcmeta` idle metadata file absent (including `fast_crusher_top.png.mcmeta`); only the corresponding `_top_on.png.mcmeta` files should exist. The generator removes stale idle metadata when it writes the static PNG. All four tiers reference the same animated Netherrack `fast_crusher` side and bottom texture.

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

The generator preserves the source silhouette and transparency while remapping visible pixels to an 80–255 grayscale range. It removes any stale `.png.mcmeta` beside the static output so an earlier animated result cannot leave the dust animated.

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

The tests check deterministic output, distinct animation frames, in-bounds wrapping paths, preservation of source pixels, source transparency, exact lava coverage and unchanged visible pixels for the four deepslate ores and all twelve tools, all-lava visible pixels and preserved silhouettes for Extreme Coal, its block, the Alloyer body, and Extremely Fast Ingot, per-pixel all-lava animation, the Accelerator and Alloyer bolts' monotonic bottom-up fill and stationary outlines, the crusher top's stationary housing, and visible, distinct, stationary iron/gold/diamond trim. CLI subprocess tests also reject Accelerator/Alloyer top input/output collisions without changing the source and verify removal of stale animation metadata from static crusher and dust outputs. They do not prove byte-for-byte reproduction of every checked-in texture; compare regenerated assets separately when needed.
