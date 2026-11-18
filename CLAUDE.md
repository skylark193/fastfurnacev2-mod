# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Fast Furnace V2 is a NeoForge mod for Minecraft. Its current content is Fast Cobblestone (`fastfurnacev2:fast_cobblestone`), a block based on vanilla cobblestone. The mod has a custom creative tab named Fast Furnace V2 with Fast Cobblestone as its icon.

The MDK example content, example configuration and client classes, startup messages, and unused empty Mixin registration have been removed. Furnace-specific behavior has not been implemented yet.

## Toolchain

- Java 25
- Gradle 9.2.1 (use the included wrapper)
- NeoForge ModDev Gradle plugin 2.0.148
- Minecraft 26.1.2
- NeoForge 26.1.2.126

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

The build is the primary validation command and matches GitHub Actions CI. There are currently no standalone unit tests or GameTests. Built JARs are written to `build/libs/`.

## Project Structure

- `src/main/java/com/skylark193/fastfurnacev2/FastFurnaceV2.java`: common mod entry point, Fast Cobblestone block/item registration, custom creative tab, and Building Blocks tab population.
- `src/main/resources/assets/fastfurnacev2/blockstates/fast_cobblestone.json`: blockstate-to-model mapping.
- `src/main/resources/assets/fastfurnacev2/models/block/fast_cobblestone.json`: cube-all block model using the mod's texture.
- `src/main/resources/assets/fastfurnacev2/items/fast_cobblestone.json`: item model definition; this Minecraft version uses `assets/<namespace>/items/`.
- `src/main/resources/assets/fastfurnacev2/textures/block/fast_cobblestone.png`: local copy of the vanilla cobblestone texture.
- `src/main/resources/assets/fastfurnacev2/lang/en_us.json`: block and creative-tab translations.
- `src/main/resources/data/fastfurnacev2/loot_table/blocks/fast_cobblestone.json`: self-drop loot table.
- `src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json`: adds Fast Cobblestone to vanilla's pickaxe-minable blocks.
- `src/main/templates/META-INF/neoforge.mods.toml`: mod metadata template, expanded using `gradle.properties` during resource processing.
- `build.gradle`: NeoForge dependencies, run configurations, generated metadata, and publishing setup.
- `.github/workflows/build.yml`: Java 25 CI build.

## Development Notes

- Use NeoForge `DeferredRegister` objects for blocks, items, and creative tabs; register all registers with the mod event bus.
- Fast Cobblestone mirrors vanilla cobblestone's explicit properties: stone map color, base-drum instrument, `requiresCorrectToolForDrops()`, and strength 2.0/6.0. The mineable tag supplies pickaxe behavior, and the loot table explicitly drops Fast Cobblestone.
- Do not replace the explicit properties with `BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)`: full copy also copies vanilla's loot-table key and description ID, which can produce the wrong drops and translation key.
- Add translations for new registry objects and configuration options.
- Keep common logic separate from any future client-only logic. Client classes must use `Dist.CLIENT` and must not be referenced from dedicated-server paths.
- Update generated mod metadata through `gradle.properties` and `src/main/templates/META-INF/neoforge.mods.toml`, not files under `build/`.
- Generated resources belong in `src/generated/resources`; do not edit Gradle caches or build outputs.
- The project uses Mojang official mappings. Their license terms are separate from this project's MIT license.

## Working Rules

1. As changes are made to the project, update `README.md` and `CLAUDE.md` so they remain accurate.
2. Commit all changes that pass validation. Run `.\gradlew.bat build` (or `./gradlew build`) before committing, and do not commit changes while validation is failing.
