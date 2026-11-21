# Fast Furnace V2

Fast Furnace V2 is a NeoForge mod for Minecraft 26.1.2. It adds Fast Cobblestone, Fast Coal, a Block of Fast Coal, a lava-powered Accelerator, four tiers of accelerated furnaces, four tiers of raw-ore crushers, dynamically generated crushed ores, animated lava-vein textures, and optional JEI integration.

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

### Accelerator

- Converts coal into Fast Coal in 100 ticks (5 seconds) and coal blocks into Blocks of Fast Coal in 900 ticks (45 seconds). It has no other processing recipes and currently has no crafting recipe.
- Requires lava on all four horizontal sides. Source or flowing lava works, and no lava is required above or below. Missing lava pauses processing without losing progress or stored boost.
- Runs at normal speed without fuel. Its fuel slot accepts only Fast Coal and Blocks of Fast Coal; ordinary furnace fuels are not accepted. Fuel is consumed only when valid work can proceed.
- Fast Coal supplies 900 boosted work units, exactly nine coal conversions or one coal-block conversion. A Block of Fast Coal supplies 1,000 units, exactly ten coal conversions or one coal-block conversion with enough left over for one coal conversion.
- Boosted work advances three units per tick instead of one. Coal therefore takes 34 ticks and a coal block takes 300 ticks (15 seconds). Work is clamped to the remaining recipe and boost, so partial final ticks do not waste fuel.
- Missing lava and a blocked output pause processing without losing stored boost or recipe progress. Empty or unsupported input resets progress, as does changing between coal and coal blocks; stored boost survives these resets and is saved with the block.
- Uses animated smooth stone with lava veins. Its top face has a lightning bolt that fills from bottom to top through eleven block-state stages as recipe progress advances; the front and other faces retain the smooth-stone body texture.
- Requires a pickaxe, drops itself, preserves custom names, supports directional placement and comparator output, and appears in Functional Blocks and the mod's creative tab. Its furnace-style GUI has no ordinary smelting recipe book and includes an information fly-out for lava, boost, and recipe timing.

### Crushed Ores

- Discovers vanilla and modded raw ore items during registration, including conventional `raw_...`, `..._raw`, and `..._raw_ore` item IDs and items listed in packaged `c:raw_materials` tags or their subtags.
- Reuses a matching crushed item or dust from another mod instead of generating a duplicate. Detection recognizes conventional IDs such as FTB Materials' `tin_dust` and Create's `crushed_raw_iron`, along with packaged `c:dusts` and `c:crushed_ores` tags. Same-namespace matches and tagged items are preferred.
- When no external match exists, adds a crushed item for the raw ore. Each generated item appears as a dust pile tinted from the visible pixels of its source item's texture and appears in Ingredients and the mod's creative tab. External items keep their own names, models, and creative-tab placement.
- Copies each raw ore's ordinary smelting recipe when the selected crushed item needs one, preserving the output, experience, cooking time, category, and group. Existing external smelting recipes are left unchanged. Crushed ores therefore work in ordinary furnaces and every Fast Furnace tier.
- Without an external match, vanilla raw iron, raw copper, and raw gold become Crushed Iron, Crushed Copper, and Crushed Gold. Generated modded variants use namespace-qualified registry IDs to avoid collisions and derive English material names from their source IDs, removing raw-ore affixes such as `_raw_ore`. For example, a generated crushed item for a modded `tin_raw_ore` item is displayed as Crushed Tin when no material translation is supplied.
- Discovery occurs during mod registration; datapacks cannot introduce new raw item types afterward. Packaged item definitions and required tag entries allow external items to be recognized even when their mod registers later. If a generated item's source texture does not use the conventional `textures/item/<raw_item_id>.png` path, its dust falls back to a neutral color.

### Fast Crushers

- Fast Crusher, Iron Fast Crusher, Gold Fast Crusher, and Diamond Fast Crusher process raw ores through the dynamic crushed-ore system instead of performing ordinary smelting. Each raw ore produces two matching crushed ore items; ordinary furnace inputs such as cobblestone are not accepted.
- A raw ore's ordinary smelting time supplies the operation duration when present, with 200 ticks as the fallback. Every crusher tier uses the corresponding furnace tier's normal and Fast Coal speed, but never doubles fuel duration.

| Crusher | Normal speed | With Fast Coal | Standard operation, normal / Fast Coal | Four-item output chance |
|---|---:|---:|---:|---:|
| Fast Crusher | 150% | 200% | 133 / 100 ticks | 10% |
| Iron Fast Crusher | 200% | 250% | 100 / 80 ticks | 15% |
| Gold Fast Crusher | 250% | 300% | 80 / 66 ticks | 20% |
| Diamond Fast Crusher | 300% | 350% | 66 / 57 ticks | 25% |

- Each completed operation independently rolls its tier's chance to double output from two crushed items to four. If a successful doubled result cannot fit, processing and fuel countdown pause without consuming the input until all four fit; the crusher never falls back to the two-item result for that operation.
- Every tier uses animated Netherrack textures on its sides and bottom. Its top-down blender has a stationary rotor while idle and animated rotating blades with swirling contents while lit. Iron, gold, and diamond tops show the corresponding block texture in the stationary housing around the blender.
- Every crusher requires a pickaxe, drops itself, preserves custom names, and emits light level 13 while lit. All four appear in Functional Blocks and the mod's Fast Furnace V2 creative tab.
- The base Fast Crusher has no crafting recipe. Surround it with eight iron blocks to craft Iron Fast Crusher, surround that with eight gold blocks to craft Gold Fast Crusher, then surround that with eight diamond blocks to craft Diamond Fast Crusher.

### Fast Furnace Tiers

| Furnace | Normal speed | With Fast Coal | Standard recipe, normal / Fast Coal | Fuel doubling chance | Fast Coal burn time |
|---|---:|---:|---:|---:|---:|
| Fast Furnace | 150% | 200% | 133 / 100 ticks | 10% | 2,400 ticks |
| Iron Fast Furnace | 200% | 250% | 100 / 80 ticks | 15% | 2,800 ticks |
| Gold Fast Furnace | 250% | 300% | 80 / 66 ticks | 20% | 3,200 ticks |
| Diamond Fast Furnace | 300% | 350% | 66 / 57 ticks | 25% | 3,600 ticks |

Standard recipes take 200 ticks in a vanilla furnace. Each consumed fuel item independently rolls the listed chance to double its adjusted burn time. Fast Coal's listed burn times are before that roll; doubled values are 4,800, 5,600, 6,400, and 7,200 ticks respectively.

Every Fast Furnace and Fast Crusher tier pauses its active fuel countdown when there is no processable work, including empty or unprocessable input and an output slot that cannot accept the required result. For crushers, this includes waiting for a pending doubled result to fit. The countdown resumes when valid work returns, preserving any active Fast Coal bonus. If fuel runs out during an operation, recipe progress is preserved instead of cooling down or resetting; adding fuel resumes the same operation from its saved progress.

Craft the base Fast Furnace from eight Fast Cobblestone using the vanilla furnace pattern, then upgrade it in order:

- Surround Fast Furnace with eight iron blocks to craft Iron Fast Furnace.
- Surround Iron Fast Furnace with eight gold blocks to craft Gold Fast Furnace.
- Surround Gold Fast Furnace with eight diamond blocks to craft Diamond Fast Furnace.

Every tier supports vanilla smelting recipes and fuels, the furnace recipe book, inventory and experience handling, comparator output, directional placement, and familiar furnace sounds and particles. All tiers require a pickaxe, drop themselves, preserve custom names, and emit light level 13 while lit.

Every tier uses the same furnace-style GUI. Its information fly-out shows the tier's current speed, fuel doubling chance, remaining burn time, and total current-fuel burn time. Crusher fly-outs instead show their tier's 10% / 15% / 20% / 25% output-doubling chance. Timers use exact tick counts and display seconds with one decimal place. All furnaces and crushers appear in Functional Blocks and the mod's Fast Furnace V2 creative tab.

Fast Cobblestone is the icon of the mod's creative tab. Fast Cobblestone, Fast Coal, Block of Fast Coal, and all furnace textures use animated lava veins. Each upgrade has a furnace-shaped front, side, and top made from its tier material, with distinct unlit and burning front models. All furnace textures share one vein layout so adjacent furnace tiers tile together.

## Optional JEI Integration

With a compatible JEI release installed:

- Each Fast Furnace tier's progress arrow opens smelting recipes, and its fire icon opens that tier's fuel category. Every crusher's progress arrow opens the dedicated crushing category, and each is a catalyst for crushing and its own tier's fuel category, not smelting.
- The crushing category contains one recipe per discovered vanilla or modded raw ore, showing two of its selected generated or external crushed item and the tier-dependent 10–25% chance for four. Looking up that crushed item shows how it is made, while looking up a raw ore shows its crushing use.
- Fuel categories calculate items smelted from each tier's actual cooking speed, including Fast Coal and Fast Coal Block speed and duration bonuses.
- Recipe transfer supports smelting for the four Fast Furnace tiers, crushing for all four crusher tiers, and fuel insertion for every machine. Smelting transfers and click areas remain disabled for crushers.
- Every furnace includes a short speed and fuel-doubling description; each crusher instead describes its tier's raw-ore crushing speed and output-doubling chance. Fast Cobblestone and Fast Coal include acquisition information, and Block of Fast Coal includes crafting and fuel information.
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
