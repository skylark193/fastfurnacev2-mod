# Fast Furnace V2

A NeoForge mod for Minecraft 26.1.2, adding Fast Cobblestone, Fast Coal, and an accelerated Fast Furnace with animated lava-vein textures and optional JEI integration.

## Features

### Fast Cobblestone

- Registry ID: `fastfurnacev2:fast_cobblestone`
- Uses the vanilla cobblestone texture as a base with generated, animated lava veins and matching block/item models.
- Matches vanilla cobblestone's stone map color, base-drum note-block instrument, 2.0 hardness, and 6.0 blast resistance.
- Is mineable with a pickaxe and requires an appropriate tool for drops, matching vanilla cobblestone's wooden-pickaxe-or-better behavior.
- Drops Fast Cobblestone—not vanilla cobblestone—when broken with an appropriate tool.
- Mining stone with a correct, non-Silk-Touch tool has an independent 10% chance to drop one bonus Fast Cobblestone alongside the normal vanilla cobblestone drop. The bonus does not apply in creative mode or when block drops are disabled.
- Appears in both the vanilla **Building Blocks** tab and the mod's **Fast Furnace V2** tab, whose icon is Fast Cobblestone.

### Fast Coal

- Registry ID: `fastfurnacev2:fast_coal`.
- Uses the vanilla coal item as its base with a generated, animated lava-vein texture.
- Mining coal ore or deepslate coal ore with a correct, non-Silk-Touch tool has an independent 10% chance to drop one bonus Fast Coal alongside the ore's normal drops. The bonus does not apply in creative mode or when block drops are disabled.
- Burns for 1,600 ticks in normal furnaces, exactly like regular coal.
- Burns for 2,400 ticks in Fast Furnace before its independent 10% double-burn roll, which can increase that duration to 4,800 ticks.
- Raises Fast Furnace to 200% of vanilla smelting speed while actively burning, reducing a normal 200-tick recipe from 133 ticks to 100 ticks.
- Appears in both the vanilla **Ingredients** tab and the mod's **Fast Furnace V2** tab.

### Fast Furnace

- Registry ID: `fastfurnacev2:fast_furnace`.
- Uses vanilla smelting recipes and fuels, recipe-book integration, inventory/experience handling, directional placement, comparator output, and familiar fire sounds and particles.
- Smelts at 150% of vanilla speed, or 200% while Fast Coal is actively burning; a normal 200-tick item completes in 133 or 100 ticks respectively.
- Each consumed fuel item has an independent 10% chance to provide twice its adjusted burn time.
- Has an information button at the top right of its GUI. Its fly-out shows the current speed, double-burn chance, remaining burn time, and current fuel's total burn time in explicitly formatted seconds with exactly one decimal place, synchronized from exact tick values every tick. Numeric values are right-aligned close to their labels and the seconds units stay at fixed positions as the countdown changes width.
- Uses the vanilla furnace's 3.5 hardness and blast resistance, requires a pickaxe for drops, and emits light level 13 while lit.
- Drops itself and preserves a custom name assigned before breaking.
- Can be crafted with the vanilla furnace pattern using eight Fast Cobblestone.
- Appears in both the vanilla **Functional Blocks** tab and the mod's **Fast Furnace V2** tab.
- Uses animated lava-vein variants of the vanilla furnace front, lit front, side, and top textures.

The former MDK example blocks, items, configuration, startup messages, client setup, and empty Mixin registration have been removed.

### Optional JEI Integration

When a JEI release compatible with Minecraft 26.1.2 is installed, Fast Furnace is registered for smelting and a dedicated Fast Furnace Fuels category. JEI recipe transfer supports both types; clicking the furnace progress arrow opens smelting recipes, and clicking the fire icon opens valid fuels with their Fast Furnace burn durations. Fast Coal appears as 8 smelts in vanilla furnaces and 12 smelts in Fast Furnace before the random double-burn roll. Fast Cobblestone and Fast Coal have information pages explaining their bonus drops, and JEI reserves space for the furnace information fly-out while it is open.

JEI is an optional mod dependency and its API is compile-only. The BlameJared Maven repository and JEI versions are configured in [build.gradle](build.gradle) and [gradle.properties](gradle.properties).

**Runtime compatibility:** JEI 29.2.0.21 currently declares support for Minecraft 26.1 exactly, not 26.1.2, so it is excluded from normal development launches. After selecting a compatible JEI release, opt into its development runtime with `-PwithJei`, for example `./gradlew runClient -PwithJei`. Without a compatible release, JEI's own mod requirement prevents Minecraft from starting; do not bypass that requirement as a substitute for compatibility testing.

## Requirements

For development:

- JDK 25
- No separate Gradle installation; use the included Gradle 9.2.1 wrapper

The mod metadata targets Minecraft 26.1.2 and requires NeoForge 26.1.2.126 or newer. These versions are defined in [gradle.properties](gradle.properties).

## Build

From the project root:

```bash
# Windows
.\gradlew.bat build

# macOS / Linux
./gradlew build
```

The build compiles the Java sources, processes resources, runs available checks, and assembles the mod JAR in `build/libs/`.

For a clean rebuild:

```bash
.\gradlew.bat clean build
```

## Run in Development

```bash
# Windows: use .\gradlew.bat instead of ./gradlew
./gradlew runClient   # Development client
./gradlew runServer   # Dedicated server with --nogui
./gradlew runData     # Data generation into src/generated/resources
./gradlew runGameTestServer  # Fast Furnace GameTests
```

## Project Structure

```text
src/main/java/com/skylark193/fastfurnacev2/
├── FastFurnaceV2.java             # Registration and event listener setup
├── FastBonusDrops.java            # Bonus drops from player-mined stone and coal ores
├── FastFurnaceBlock.java          # Vanilla-style furnace block behavior
├── FastFurnaceBlockEntity.java    # Accelerated smelting, fuel rolls, and timer synchronization
├── FastFurnaceMenu.java           # Custom furnace menu and exact fuel/active-fuel data
├── FastFurnaceGameTests.java      # Cooking-speed and fuel-behavior GameTests
├── client/
│   ├── FastFurnaceV2Client.java   # Client-only screen registration
│   └── FastFurnaceScreen.java     # Furnace GUI and live information fly-out
└── compat/jei/
    ├── FastFurnaceJeiPlugin.java  # Optional JEI recipes, information, and GUI integration
    ├── FastFurnaceFuelCategory.java
    └── FastFurnaceFuelRecipe.java
src/main/resources/
├── assets/fastfurnacev2/
│   ├── blockstates/               # Fast Cobblestone and directional/lit furnace states
│   ├── items/                     # Block and Fast Coal item model definitions
│   ├── lang/en_us.json
│   ├── models/                    # Block models and Fast Coal's generated item model
│   └── textures/                  # Animated block/item PNG strips and .png.mcmeta metadata
└── data/
    ├── fastfurnacev2/
    │   ├── loot_table/blocks/     # Self-drops; furnace preserves custom names
    │   └── recipe/fast_furnace.json
    ├── minecraft/tags/block/mineable/pickaxe.json
    └── neoforge/data_maps/item/furnace_fuels.json
src/main/templates/META-INF/
└── neoforge.mods.toml             # Metadata populated from gradle.properties
tools/
├── generate_lava_texture.py       # Reusable animated lava-vein generator
├── test_generate_lava_texture.py  # Generator determinism and image checks
├── requirements.txt               # Pinned Pillow dependency
├── texture_sources/               # Preserved square vanilla source textures
└── README.md                      # Texture generation instructions
```

## Texture Generation

Fast Cobblestone, Fast Coal, and all four Fast Furnace textures use eight-frame lava-vein animations with a two-tick frame time. The reusable generator, preserved vanilla sources, pinned Python dependency, and full regeneration commands are in [tools/README.md](tools/README.md). Fast Coal uses seed 415.

To regenerate after setting up the tools environment:

```bash
.venv\Scripts\python -I tools\generate_lava_texture.py --input tools\texture_sources\cobblestone.png --output src\main\resources\assets\fastfurnacev2\textures\block\fast_cobblestone.png --seed 193 --frames 8 --frame-time 2 --veins 2
```

The generator writes both the vertical PNG strip and its `.png.mcmeta` animation metadata. Use different input/output paths and seeds for future lava-vein textures.

## Block Behavior and Metadata

Fast Cobblestone's Java properties mirror the vanilla cobblestone definition. Its pickaxe-minable tag controls mining behavior, while its loot table explicitly specifies the Fast Cobblestone item as its drop. Keep these definitions synchronized if its behavior changes.

Fast Furnace uses its own block, block entity, menu, and client screen types. `FastFurnaceBlockEntity.serverTick` retains vanilla smelting, fuel, inventory, and experience behavior while scaling recipe cooking times to two-thirds normally or one-half while Fast Coal is active. Fast Coal receives a 50% burn-time increase only in Fast Furnace, after which each ignition independently rolls the 10% doubled-burn chance. The active-fuel flag is persisted with the block entity. The blockstate selects separate normal and lit models, while the loot table copies `minecraft:custom_name` from the block entity just like vanilla.

`FastFurnaceMenu` extends the vanilla abstract furnace menu with five extra data values. The client receives the low and high halves of the exact remaining and total burn times on every menu synchronization, avoiding vanilla's short-value normalization for long fuel durations, plus the active Fast Coal flag for the displayed speed. `FastFurnaceScreen` uses these values in its compact fly-out and exposes its bounds to the optional JEI plugin.

`FastBonusDrops` listens to NeoForge's server-side `BlockDropsEvent`. For stone or blocks in vanilla's coal-ores tag broken by a non-creative player, it checks the block-drops game rule, the player's harvest tool, and the tool's Silk Touch level before applying the 10% roll. A successful roll appends one matching bonus item to the existing drop list without replacing vanilla drops.

Use [gradle.properties](gradle.properties) to change the mod ID, display name, version, Minecraft/NeoForge versions, group ID, or license. If the mod ID changes, also update `FastFurnaceV2.MODID`, resource paths, package references, and metadata as needed.

The `neoforge.mods.toml` file is generated from [src/main/templates/META-INF/neoforge.mods.toml](src/main/templates/META-INF/neoforge.mods.toml) during resource processing. Do not edit generated files under `build/`.

## Contributing

1. Make focused changes and remove obsolete code and imports.
2. Update [README.md](README.md) and [CLAUDE.md](CLAUDE.md) whenever project behavior, structure, commands, or development guidance changes.
3. Run `.\gradlew.bat clean build` and `.\gradlew.bat runGameTestServer` on Windows (or `./gradlew clean build` and `./gradlew runGameTestServer` on macOS/Linux). When changing texture generation, also run the generator tests documented in [tools/README.md](tools/README.md).
4. Commit all changes that pass validation; do not commit while validation is failing.

GitHub Actions runs the same Gradle build on pushes and pull requests using JDK 25.

## Resources

- [NeoForge documentation](https://docs.neoforged.net/)
- [NeoForge Discord](https://discord.neoforged.net/)
- [Mojang mappings license information](https://github.com/NeoForged/NeoForm/blob/main/Mojang.md)

## AI Assistance Disclosure

This project was developed with assistance from [Claude Code](https://claude.com/claude-code), Anthropic's AI coding assistant. AI-assisted contributions include Java code, tests, documentation, resource files, texture-generation tooling, and generated textures. Development is directed by the project maintainer, and changes are validated using the project's build, GameTests, and texture-generator tests where applicable. This disclosure does not imply endorsement by Anthropic.

## License

This project is licensed under the [MIT License](LICENSE).

The original NeoForge MDK template license is retained in [TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt). Minecraft, NeoForge, Mojang mappings, and copied vanilla assets are governed by their respective licenses.
