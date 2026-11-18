# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Fast Furnace V2 is a NeoForge mod for Minecraft. Its current content is Fast Cobblestone (`fastfurnacev2:fast_cobblestone`), Fast Coal (`fastfurnacev2:fast_coal`), and Fast Furnace (`fastfurnacev2:fast_furnace`), based on vanilla content with generated animated lava veins. Fast Furnace normally cooks at 150% of vanilla speed, increases to 200% while burning Fast Coal, and has a 10% chance per consumed fuel item to double that item's burn time. Fast Coal burns for vanilla coal's 1600 ticks in normal furnaces, but for 2400 ticks before doubling in Fast Furnace. Fast Furnace retains vanilla smelting recipes, other fuels, inventory handling, experience, and comparator behavior. The mod has a custom creative tab named Fast Furnace V2 with Fast Cobblestone as its icon. Mining stone with a correct, non-Silk-Touch tool has a 10% chance to add bonus Fast Cobblestone alongside vanilla cobblestone; coal ore and deepslate coal ore similarly have a 10% chance to add Fast Coal alongside their normal drops. Optional JEI integration provides recipe/fuel catalysts and transfers, click areas, acquisition information, a Fast-Furnace-specific fuel category, and layout space for the furnace's information fly-out.

The MDK example content, example configuration and client classes, startup messages, and unused empty Mixin registration have been removed.

## Toolchain

- Java 25
- Gradle 9.2.1 (use the included wrapper)
- NeoForge ModDev Gradle plugin 2.0.148
- Minecraft 26.1.2
- NeoForge 26.1.2.126
- JEI 29.2.0.21 common and NeoForge APIs (`compileOnly`), with optional runtime integration
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

# Run the Fast Furnace GameTests
.\gradlew.bat runGameTestServer
```

The build is the primary Java validation command and matches GitHub Actions CI. GameTests cover furnace cooking speed and fuel behavior and run separately with `runGameTestServer`. The Python texture generator has its own unittest suite under `tools/`. Built JARs are written to `build/libs/`.

JEI API dependencies use the BlameJared Maven repository and are `compileOnly`. JEI 29.2.0.21 requires Minecraft 26.1 exactly and cannot launch on 26.1.2, so its full runtime is excluded by default. `-PwithJei` opts into `localRuntime` after a compatible JEI release is selected; do not treat dependency-resolution or compilation success as JEI runtime validation.

## Project Structure

- `src/main/java/com/skylark193/fastfurnacev2/FastFurnaceV2.java`: common mod entry point; block, item, block entity, and creative-tab registration; vanilla tab population; and game-event listener registration.
- `src/main/java/com/skylark193/fastfurnacev2/FastBonusDrops.java`: appends Fast Cobblestone or Fast Coal bonus drops to the existing drops of eligible stone and coal ores.
- `src/main/java/com/skylark193/fastfurnacev2/FastFurnaceBlock.java`: vanilla-style furnace facing, lighting ticker hookup, menu opening, statistics, fire sounds, and particles.
- `src/main/java/com/skylark193/fastfurnacev2/FastFurnaceBlockEntity.java`: custom server ticking at 150% or Fast-Coal-boosted 200% cooking speed, adjusted per-fuel burn times, 10% doubled burn time, and exact timer/active-fuel synchronization through `AbstractFurnaceBlockEntity`.
- `src/main/java/com/skylark193/fastfurnacev2/FastFurnaceMenu.java`: custom `AbstractFurnaceMenu` using vanilla smelting recipe-book properties and nine data slots, including split full-integer fuel timers and the active Fast Coal flag.
- `src/main/java/com/skylark193/fastfurnacev2/FastFurnaceGameTests.java`: registry-based GameTests for normal and Fast-Coal-boosted furnace behavior.
- `src/main/java/com/skylark193/fastfurnacev2/client/FastFurnaceV2Client.java`: client-only menu screen registration.
- `src/main/java/com/skylark193/fastfurnacev2/client/FastFurnaceScreen.java`: vanilla-style furnace screen with an information button, live speed/fuel-time fly-out, and JEI exclusion bounds.
- `src/main/java/com/skylark193/fastfurnacev2/compat/jei/FastFurnaceJeiPlugin.java`: optional JEI catalysts, recipe transfers, smelting/custom-fuel click areas, Fast Cobblestone/Fast Coal information, fuel-list generation, and fly-out GUI handler.
- `src/main/java/com/skylark193/fastfurnacev2/compat/jei/FastFurnaceFuelCategory.java`: Fast-Furnace-specific JEI fuel category with adjusted smelt counts.
- `src/main/java/com/skylark193/fastfurnacev2/compat/jei/FastFurnaceFuelRecipe.java`: immutable JEI fuel recipe inputs and burn-time values.
- `src/main/resources/assets/fastfurnacev2/blockstates/fast_cobblestone.json`: blockstate-to-model mapping.
- `src/main/resources/assets/fastfurnacev2/blockstates/fast_furnace.json`: four horizontal facings with normal and lit models.
- `src/main/resources/assets/fastfurnacev2/models/block/fast_cobblestone.json`: cube-all block model using the mod's texture.
- `src/main/resources/assets/fastfurnacev2/models/block/fast_furnace.json` and `fast_furnace_on.json`: vanilla orientable furnace models using animated mod textures.
- `src/main/resources/assets/fastfurnacev2/items/`: item model definitions; this Minecraft version uses `assets/<namespace>/items/`.
- `src/main/resources/assets/fastfurnacev2/models/item/fast_coal.json`: generated item model using the animated Fast Coal texture.
- `src/main/resources/assets/fastfurnacev2/textures/item/fast_coal.png` and `.png.mcmeta`: eight-frame Fast Coal lava-vein animation with a two-tick frame time.
- `src/main/resources/data/neoforge/data_maps/item/furnace_fuels.json`: gives Fast Coal the standard vanilla-coal burn time of 1600 ticks for ordinary fuel consumers.
- `src/main/resources/assets/fastfurnacev2/textures/block/fast_cobblestone.png`: eight-frame vertical animation strip based on vanilla cobblestone with lava veins.
- `src/main/resources/assets/fastfurnacev2/textures/block/fast_cobblestone.png.mcmeta`: sequential, non-interpolated animation with a two-tick frame time.
- `src/main/resources/assets/fastfurnacev2/lang/en_us.json`: block, item, creative-tab, GUI, and JEI translations.
- `src/main/resources/data/fastfurnacev2/loot_table/blocks/`: self-drop loot tables; Fast Furnace also preserves its custom name.
- `src/main/resources/data/fastfurnacev2/recipe/fast_furnace.json`: vanilla furnace crafting pattern using eight Fast Cobblestone.
- `src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json`: adds both blocks to vanilla's pickaxe-minable blocks.
- `src/main/templates/META-INF/neoforge.mods.toml`: mod metadata template, expanded using `gradle.properties` during resource processing.
- `build.gradle`: NeoForge dependencies, run configurations, generated metadata, and publishing setup.
- `.github/workflows/build.yml`: Java 25 CI build.
- `tools/generate_lava_texture.py`: seeded Pillow-based generator for tileable lava veins, branching paths, crust, and traveling heat pulses.
- `tools/texture_sources/`: preserved square vanilla cobblestone, coal, and four furnace source textures used for regeneration.
- `tools/test_generate_lava_texture.py`: deterministic, animation, stone-preservation, and transparency tests for the generator.
- `tools/README.md`: isolated environment setup, regeneration command, and options for future textures.

## Development Notes

- Use NeoForge `DeferredRegister` objects for blocks, items, block entities, menus, GameTest instance types, and creative tabs; register all registers with the mod event bus.
- Fast Cobblestone mirrors vanilla cobblestone's explicit properties: stone map color, base-drum instrument, `requiresCorrectToolForDrops()`, and strength 2.0/6.0. The mineable tag supplies pickaxe behavior, and the loot table explicitly drops Fast Cobblestone.
- `FastBonusDrops` handles NeoForge's server-side `BlockDropsEvent`, after vanilla drops have been calculated. It requires stone or a block in `BlockTags.COAL_ORES`, a non-creative player breaker, enabled block drops, and a correct tool without Silk Touch. Its 10% roll appends one matching Fast Cobblestone or Fast Coal to `event.getDrops()`; do not replace or clear normal drops. The coal-ores tag covers both coal ore and deepslate coal ore.
- Fast Coal uses vanilla coal's default item properties, belongs in Ingredients and the mod tab, and has a NeoForge `furnace_fuels` data-map value of 1600 so ordinary furnaces treat it exactly like coal. Only `FastFurnaceBlockEntity` applies its 50% longer duration and 200% smelting speed.
- Do not replace the explicit properties with `BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)`: full copy also copies vanilla's loot-table key and description ID, which can produce the wrong drops and translation key.
- Fast Furnace has its own `BlockEntityType`, constructed with this version's public `BlockEntityType` constructor; `BlockEntityType.Builder` is not available. Its ticker must use `FAST_FURNACE_BLOCK_ENTITY`, not `BlockEntityType.FURNACE`.
- `FastFurnaceBlock` extends `AbstractFurnaceBlock` and mirrors vanilla furnace behavior. Its ticker must call `FastFurnaceBlockEntity.serverTick` through `createTickerHelper`; vanilla `createFurnaceTicker` would bypass the custom timing logic. Retain the custom `openContainer` check because vanilla `FurnaceBlock` specifically checks `FurnaceBlockEntity`.
- `FastFurnaceBlockEntity` extends `AbstractFurnaceBlockEntity` with `RecipeType.SMELTING` and uses a custom server tick. It scales 200-tick recipes to 133 ticks normally or 100 ticks while Fast Coal is active. Fast Coal's standard 1600-tick value becomes 2400 ticks through `scaleFastCoalBurnTime`, then the independent 10% roll can double it to 4800. Track `fastCoalBurning` from actual fuel consumption rather than the fuel slot, clear it when burn time expires, and persist it under `fast_coal_burning`. Preserve standard smelting recipes, other fuels, inventory, XP, comparator behavior, light level 13 while lit, custom-name drops, and the eight-Fast-Cobblestone crafting pattern.
- `FastFurnaceMenu` extends `AbstractFurnaceMenu` with nine data slots. Indices 0–3 retain vanilla behavior; indices 4–7 contain low/high halves of the exact remaining and total fuel times; index 8 is the active Fast Coal flag. Use the split values rather than adding a custom per-tick packet or relying on vanilla's short-value normalization.
- `FastFurnaceScreen` is client-only and draws the information button and compact fly-out in `extractLabels`. It selects 150% or 200% from the synchronized active-fuel flag, explicitly formats exact tick timers in Java with `Locale.ROOT` and `%.1f`, right-aligns each numeric value close to its label, and draws the translated seconds unit separately at a fixed X coordinate; do not move numeric formatting back into translation strings. Its `getJeiExtraAreas` bounds are relative to the current GUI position and appear only while the fly-out is visible.
- `FastFurnaceJeiPlugin` registers Fast Furnace for `RecipeTypes.SMELTING` and the custom `FastFurnaceFuelCategory.RECIPE_TYPE`, adds transfer handlers and click areas for each, adds Fast Cobblestone/Fast Coal ingredient information, and registers the fly-out as extra GUI space. The custom fuel category enumerates registered item fuels just like JEI's vanilla category, applies `scaleFastCoalBurnTime` only to Fast Coal, and therefore shows 12 Fast Coal smelts in Fast Furnace while vanilla's category continues to show 8. Do not replace this with modified `RecipeTypes.SMELTING_FUEL` entries, which would incorrectly change normal furnace displays. Keep JEI and Minecraft client references in optional compatibility classes and out of dedicated-server paths.
- GameTests use `RegisterGameTestsEvent` plus the registered `fastfurnacev2:function` instance codec. Vanilla's `TestFunctionLoader` bootstrap runs before mod construction, so registering a loader in mod/class initialization is too late; the custom `DirectGameTestInstance` invokes the mod's mapped functions directly.
- Add translations for new registry objects and configuration options.
- Do not use generated animation strips as generator input. Use the preserved square sources in `tools/texture_sources/` and follow `tools/README.md`; the generator writes both the strip and `.mcmeta` file. Fast Furnace uses seeds 311 (front), 312 (lit front), 313 (side), and 314 (top); Fast Coal uses seed 415. Run generator tests with `.venv/Scripts/python -I -m unittest discover -s tools -p 'test_*.py'` on Windows or `.venv/bin/python -I -m unittest discover -s tools -p 'test_*.py'` on macOS/Linux.
- Keep common logic separate from client-only logic. Client entry points must use `@Mod(value = MODID, dist = Dist.CLIENT)` and client classes must not be referenced from dedicated-server paths. Do not add `@OnlyIn` for runtime stripping: NeoForge 26.1.2 reports that this stripping behavior no longer exists.
- Update generated mod metadata through `gradle.properties` and `src/main/templates/META-INF/neoforge.mods.toml`, not files under `build/`.
- Generated resources belong in `src/generated/resources`; do not edit Gradle caches or build outputs.
- The project uses Mojang official mappings. Their license terms are separate from this project's MIT license.

## Working Rules

1. As changes are made to the project, update `README.md` and `CLAUDE.md` so they remain accurate.
2. Commit all changes that pass validation. Run `.\gradlew.bat build` (or `./gradlew build`) before committing, and do not commit changes while validation is failing.
