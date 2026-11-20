# Fast Furnace V2

Fast Furnace V2 is a NeoForge mod for Minecraft 26.1.2. It adds Fast Cobblestone, Fast Coal, a Block of Fast Coal, four tiers of accelerated furnaces, a Fast Crusher, dynamically generated crushed ores, animated lava-vein textures, and optional JEI integration.

## Features

### Fast Cobblestone

- Matches vanilla cobblestone's mining requirements and physical properties, but drops Fast Cobblestone.
- Mining stone in survival mode with a correct, non-Silk-Touch tool has a base 10% chance to add one Fast Cobblestone alongside the normal drops. Each Fortune level adds 5 percentage points to this chance. The bonus respects the block-drops game rule.
- Used to craft Fast Furnace with the vanilla furnace pattern: eight Fast Cobblestone around an empty center.
- Appears in Building Blocks and the mod's Fast Furnace V2 creative tab.

### Fast Coal

- Mining coal ore or deepslate coal ore in survival mode with a correct, non-Silk-Touch tool has a base 10% chance to add one Fast Coal alongside the normal drops. Each Fortune level adds 5 percentage points to this chance. The bonus respects the block-drops game rule.
- Burns for 1,600 ticks in ordinary furnaces, exactly like vanilla coal.
- Adds 50 percentage points to the active furnace's smelting speed and receives a tier-dependent burn-time bonus while actively burning.
- Appears in Ingredients and the mod's Fast Furnace V2 creative tab.

### Block of Fast Coal

- Crafted from a full 3×3 grid of Fast Coal and can be unpacked back into nine Fast Coal.
- Matches a vanilla coal block's mining requirements and physical properties, drops itself, and has an animated coal-block texture with lava veins.
- Burns for 16,000 ticks in ordinary furnaces, exactly like a vanilla coal block.
- Counts as active Fast Coal fuel in every Fast Furnace tier, adding 50 percentage points to smelting speed and receiving the tier's duration multiplier and independent fuel-doubling roll.
- Burns for 24,000 / 28,000 / 32,000 / 36,000 ticks in Fast / Iron / Gold / Diamond Fast Furnaces before doubling, or 48,000 / 56,000 / 64,000 / 72,000 ticks after doubling.
- Appears in Building Blocks and the mod's Fast Furnace V2 creative tab.

### Crushed Ores

- Discovers vanilla and modded raw ore items during registration, including conventional `raw_...` and `..._raw` item IDs and items listed in packaged `c:raw_materials` tags or their subtags.
- Adds one crushed item per discovered raw ore. Each appears as a dust pile tinted from the visible pixels of its source item's texture, and appears in Ingredients and the mod's creative tab.
- Copies each raw ore's ordinary smelting recipe for its crushed variant, preserving the output, experience, cooking time, category, and group. Crushed ores therefore work in ordinary furnaces and every Fast Furnace tier.
- Vanilla raw iron, raw copper, and raw gold become Crushed Iron, Crushed Copper, and Crushed Gold. Modded variants use namespace-qualified registry IDs to avoid collisions and derive material names from their source IDs.
- Discovery occurs during mod registration; datapacks cannot introduce new raw item types afterward. A source texture that does not use the conventional `textures/item/<raw_item_id>.png` path falls back to a neutral dust color.

### Fast Crusher

- Processes raw ores through the dynamic crushed-ore system instead of performing ordinary smelting. Each raw ore produces two matching crushed ore items; ordinary furnace inputs such as cobblestone are not accepted.
- Retains Fast-tier processing speed: 150% normally and 200% with active Fast Coal, so a standard 200-tick operation takes 133 / 100 ticks respectively. A raw ore's ordinary smelting time supplies the operation duration when present, with 200 ticks as the fallback. Fuel duration is not doubled.
- Each completed operation has a 10% chance to double its output from two crushed items to four. If a successful doubled result cannot fit, processing and fuel countdown pause without consuming the input until all four fit; the crusher never falls back to the two-item result for that operation.
- Uses animated Netherrack textures on its sides and bottom. Its top-down blender has a stationary rotor while idle and animated rotating blades with swirling contents while lit.
- Requires a pickaxe, drops itself, preserves custom names, and emits light level 13 while lit.
- Appears in Functional Blocks and the mod's Fast Furnace V2 creative tab. It does not have a crafting recipe yet.

### Fast Furnace Tiers

| Furnace | Normal speed | With Fast Coal | Standard recipe, normal / Fast Coal | Fuel doubling chance | Fast Coal burn time |
|---|---:|---:|---:|---:|---:|
| Fast Furnace | 150% | 200% | 133 / 100 ticks | 10% | 2,400 ticks |
| Iron Fast Furnace | 200% | 250% | 100 / 80 ticks | 15% | 2,800 ticks |
| Gold Fast Furnace | 250% | 300% | 80 / 66 ticks | 20% | 3,200 ticks |
| Diamond Fast Furnace | 300% | 350% | 66 / 57 ticks | 25% | 3,600 ticks |

Standard recipes take 200 ticks in a vanilla furnace. Each consumed fuel item independently rolls the listed chance to double its adjusted burn time. Fast Coal's listed burn times are before that roll; doubled values are 4,800, 5,600, 6,400, and 7,200 ticks respectively.

Every Fast Furnace tier and Fast Crusher pauses its active fuel countdown when there is no processable work, including empty or unprocessable input and an output slot that cannot accept the required result. For Fast Crusher, this includes waiting for a pending doubled result to fit. The countdown resumes when valid work returns, preserving any active Fast Coal bonus. If fuel runs out during an operation, recipe progress is preserved instead of cooling down or resetting; adding fuel resumes the same operation from its saved progress.

Craft the base Fast Furnace from eight Fast Cobblestone using the vanilla furnace pattern, then upgrade it in order:

- Surround Fast Furnace with eight iron blocks to craft Iron Fast Furnace.
- Surround Iron Fast Furnace with eight gold blocks to craft Gold Fast Furnace.
- Surround Gold Fast Furnace with eight diamond blocks to craft Diamond Fast Furnace.

Every tier supports vanilla smelting recipes and fuels, the furnace recipe book, inventory and experience handling, comparator output, directional placement, and familiar furnace sounds and particles. All tiers require a pickaxe, drop themselves, preserve custom names, and emit light level 13 while lit.

Every tier uses the same furnace GUI. Its information fly-out shows the tier's current speed, fuel doubling chance, remaining burn time, and total current-fuel burn time. The Fast Crusher's fly-out instead shows its 10% output doubling chance. Timers use exact tick counts and display seconds with one decimal place. All furnaces appear in Functional Blocks and the mod's Fast Furnace V2 creative tab.

Fast Cobblestone is the icon of the mod's creative tab. Fast Cobblestone, Fast Coal, Block of Fast Coal, and all furnace textures use animated lava veins. Each upgrade has a furnace-shaped front, side, and top made from its tier material, with distinct unlit and burning front models. All furnace textures share one vein layout so adjacent furnace tiers tile together.

## Optional JEI Integration

With a compatible JEI release installed:

- Each Fast Furnace tier's progress arrow opens smelting recipes, and its fire icon opens that tier's fuel category. Fast Crusher's progress arrow opens its dedicated crushing category, and it is a catalyst for crushing and Fast-tier fuels, not smelting.
- The crushing category contains one recipe per discovered vanilla or modded raw ore, showing its two-item crushed output and 10% chance for four. Looking up a crushed ore shows how it is made, while looking up a raw ore shows its crushing use.
- Fuel categories calculate items smelted from each tier's actual cooking speed, including Fast Coal and Fast Coal Block speed and duration bonuses.
- Recipe transfer supports smelting for the four Fast Furnace tiers, crushing for Fast Crusher, and fuel insertion for every tier and Fast Crusher. Smelting transfers and click areas remain disabled for Fast Crusher.
- Every furnace includes a short speed and fuel-doubling description; Fast Crusher instead describes its raw-ore crushing and output doubling. Fast Cobblestone and Fast Coal include acquisition information, and Block of Fast Coal includes crafting and fuel information.
- JEI reserves space around the furnace information fly-out.

**Compatibility:** JEI 29.2.0.21 supports Minecraft 26.1 exactly and cannot launch with this mod's Minecraft 26.1.2 environment. The JEI APIs are therefore compile-only by default, and the integration has not been runtime-validated on 26.1.2. A JEI release compatible with both Minecraft 26.1.2 and this integration is required.

## Requirements and Installation

- Minecraft 26.1.2
- NeoForge 26.1.2.126 or newer
- Java 25

Build the mod as described below, then copy the JAR from `build/libs/` into the Minecraft `mods` directory. JEI is optional when a compatible release is available.

## Build and Run

Development requires JDK 25. Use the included Gradle wrapper; no separate Gradle installation is needed.

```bash
# Windows
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
.\gradlew.bat runGameTestServer

# macOS / Linux
./gradlew build
./gradlew runClient
./gradlew runServer
./gradlew runGameTestServer
```

The build compiles the mod, runs the checks configured in Gradle, and writes the JAR to `build/libs/`. GameTests run separately with `runGameTestServer`. Use `runData` to generate data resources into `src/generated/resources`.

For a clean rebuild, replace `build` with `clean build`.

## Textures

The reusable Pillow-based generator, preserved vanilla source images, environment setup, regeneration commands, and test instructions are documented in [tools/README.md](tools/README.md). Always regenerate from the square source textures, not from generated animation strips.

## Contributing

Keep changes focused, remove obsolete code and resources, and update relevant documentation. Run the build and the tests applicable to the changed area before submitting changes. See [CLAUDE.md](CLAUDE.md) for architecture, implementation constraints, and detailed validation guidance.

GitHub Actions runs `./gradlew build` on pushes and pull requests using JDK 25.

## AI Assistance Disclosure

This project was developed with assistance from [Claude Code](https://claude.com/claude-code), Anthropic's AI coding assistant. AI-assisted contributions include Java code, tests, documentation, resource files, texture-generation tooling, and generated textures. Development is directed by the project maintainer. Validation commands are documented in this README. This disclosure does not imply endorsement by Anthropic.

## License

This project is licensed under the [MIT License](LICENSE).

The original NeoForge MDK template license is retained in [TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt). Minecraft, NeoForge, Mojang mappings, and copied vanilla assets are governed by their respective licenses.
