# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Fast Furnace V2 is a NeoForge mod for Minecraft. Its current content is Fast Cobblestone (`fastfurnacev2:fast_cobblestone`) and Fast Furnace (`fastfurnacev2:fast_furnace`), both based on vanilla blocks with generated animated lava veins. Fast Furnace currently operates at vanilla furnace speed and uses vanilla smelting recipes, fuels, menus, inventory handling, experience, and comparator behavior. The mod has a custom creative tab named Fast Furnace V2 with Fast Cobblestone as its icon. Mining stone with a correct, non-Silk-Touch tool also has a 10% chance to add one bonus Fast Cobblestone alongside vanilla cobblestone.

The MDK example content, example configuration and client classes, startup messages, and unused empty Mixin registration have been removed.

## Toolchain

- Java 25
- Gradle 9.2.1 (use the included wrapper)
- NeoForge ModDev Gradle plugin 2.0.148
- Minecraft 26.1.2
- NeoForge 26.1.2.126
- Optional texture tooling: Python 3 and Pillow 13.0.0, installed in an ignored `.venv` as documented in `tools/README.md`

Version and mod metadata live in `gradle.properties`. Keep `mod_id` synchronized with `FastFurnaceV2.MODID`, and ensure Minecraft and NeoForge versions remain compatible.

## Common Commands

Use `.\gradlew.bat` on Windows or `./gradlew` on macOS/Linux.

```bash
# Compile, run checks, and assemble the mod
.\gradlew.bat build

# Clean and rebuild
.\gradlew.bat clean build

# Launch a development client
.\gradlew.bat runClient

# Launch a dedicated development server
.\gradlew.bat runServer

# Generate data resources into src/generated/resources
.\gradlew.bat runData
```

The build is the primary Java validation command and matches GitHub Actions CI. There are currently no Java unit tests or GameTests; the Python texture generator has its own unittest suite under `tools/`. Built JARs are written to `build/libs/`.

## Project Structure

- `src/main/java/com/skylark193/fastfurnacev2/FastFurnaceV2.java`: common mod entry point; block, item, block entity, and creative-tab registration; vanilla tab population; and game-event listener registration.
- `src/main/java/com/skylark193/fastfurnacev2/FastCobblestoneDrops.java`: appends the 10% bonus drop to stone's existing drops for eligible players and tools.
- `src/main/java/com/skylark193/fastfurnacev2/FastFurnaceBlock.java`: vanilla-style furnace facing, lighting ticker hookup, menu opening, statistics, fire sounds, and particles.
- `src/main/java/com/skylark193/fastfurnacev2/FastFurnaceBlockEntity.java`: vanilla smelting and fuel logic through `AbstractFurnaceBlockEntity`, using the custom block entity type and `FurnaceMenu`.
- `src/main/resources/assets/fastfurnacev2/blockstates/fast_cobblestone.json`: blockstate-to-model mapping.
- `src/main/resources/assets/fastfurnacev2/blockstates/fast_furnace.json`: four horizontal facings with normal and lit models.
- `src/main/resources/assets/fastfurnacev2/models/block/fast_cobblestone.json`: cube-all block model using the mod's texture.
- `src/main/resources/assets/fastfurnacev2/models/block/fast_furnace.json` and `fast_furnace_on.json`: vanilla orientable furnace models using animated mod textures.
- `src/main/resources/assets/fastfurnacev2/items/`: item model definitions; this Minecraft version uses `assets/<namespace>/items/`.
- `src/main/resources/assets/fastfurnacev2/textures/block/fast_cobblestone.png`: eight-frame vertical animation strip based on vanilla cobblestone with lava veins.
- `src/main/resources/assets/fastfurnacev2/textures/block/fast_cobblestone.png.mcmeta`: sequential, non-interpolated animation with a two-tick frame time.
- `src/main/resources/assets/fastfurnacev2/lang/en_us.json`: block and creative-tab translations.
- `src/main/resources/data/fastfurnacev2/loot_table/blocks/`: self-drop loot tables; Fast Furnace also preserves its custom name.
- `src/main/resources/data/fastfurnacev2/recipe/fast_furnace.json`: vanilla furnace crafting pattern using eight Fast Cobblestone.
- `src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json`: adds both blocks to vanilla's pickaxe-minable blocks.
- `src/main/templates/META-INF/neoforge.mods.toml`: mod metadata template, expanded using `gradle.properties` during resource processing.
- `build.gradle`: NeoForge dependencies, run configurations, generated metadata, and publishing setup.
- `.github/workflows/build.yml`: Java 25 CI build.
- `tools/generate_lava_texture.py`: seeded Pillow-based generator for tileable lava veins, branching paths, crust, and traveling heat pulses.
- `tools/texture_sources/`: preserved square vanilla cobblestone and four furnace source textures used for regeneration.
- `tools/test_generate_lava_texture.py`: deterministic, animation, stone-preservation, and transparency tests for the generator.
- `tools/README.md`: isolated environment setup, regeneration command, and options for future textures.

## Development Notes

- Use NeoForge `DeferredRegister` objects for blocks, items, block entities, and creative tabs; register all registers with the mod event bus.
- Fast Cobblestone mirrors vanilla cobblestone's explicit properties: stone map color, base-drum instrument, `requiresCorrectToolForDrops()`, and strength 2.0/6.0. The mineable tag supplies pickaxe behavior, and the loot table explicitly drops Fast Cobblestone.
- `FastCobblestoneDrops` handles NeoForge's server-side `BlockDropsEvent`, after vanilla drops have been calculated. It requires stone, a non-creative player breaker, enabled block drops, and a correct tool without Silk Touch. Its 10% roll appends one Fast Cobblestone to `event.getDrops()`; do not replace or clear the normal cobblestone drop.
- Do not replace the explicit properties with `BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)`: full copy also copies vanilla's loot-table key and description ID, which can produce the wrong drops and translation key.
- Fast Furnace has its own `BlockEntityType`, constructed with this version's public `BlockEntityType` constructor; `BlockEntityType.Builder` is not available. Its ticker must use `FAST_FURNACE_BLOCK_ENTITY`, not `BlockEntityType.FURNACE`.
- `FastFurnaceBlock` extends `AbstractFurnaceBlock` and mirrors vanilla furnace behavior. `FastFurnaceBlockEntity` extends `AbstractFurnaceBlockEntity` with `RecipeType.SMELTING` and returns a vanilla `FurnaceMenu`. Keep the custom type in both construction and ticker registration, and retain the custom `openContainer` check because vanilla `FurnaceBlock` specifically checks `FurnaceBlockEntity`.
- Fast Furnace currently has vanilla speed. Preserve standard smelting recipes, fuels, inventory, XP, comparator behavior, light level 13 while lit, custom-name drops, and the eight-Fast-Cobblestone crafting pattern when adding upgrades later.
- Add translations for new registry objects and configuration options.
- Do not use generated animation strips as generator input. Use the preserved square sources in `tools/texture_sources/` and follow `tools/README.md`; the generator writes both the strip and `.mcmeta` file. Fast Furnace uses seeds 311 (front), 312 (lit front), 313 (side), and 314 (top). Run generator tests with `.venv/Scripts/python -I -m unittest discover -s tools -p 'test_*.py'` on Windows or `.venv/bin/python -I -m unittest discover -s tools -p 'test_*.py'` on macOS/Linux.
- Keep common logic separate from any future client-only logic. Client classes must use `Dist.CLIENT` and must not be referenced from dedicated-server paths.
- Update generated mod metadata through `gradle.properties` and `src/main/templates/META-INF/neoforge.mods.toml`, not files under `build/`.
- Generated resources belong in `src/generated/resources`; do not edit Gradle caches or build outputs.
- The project uses Mojang official mappings. Their license terms are separate from this project's MIT license.

## Working Rules

1. As changes are made to the project, update `README.md` and `CLAUDE.md` so they remain accurate.
2. Commit all changes that pass validation. Run `.\gradlew.bat build` (or `./gradlew build`) before committing, and do not commit changes while validation is failing.
