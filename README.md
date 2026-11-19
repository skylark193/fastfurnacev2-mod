# Fast Furnace V2

Fast Furnace V2 is a NeoForge mod for Minecraft 26.1.2. It adds Fast Cobblestone, Fast Coal, and four tiers of accelerated furnaces with animated lava-vein textures and optional JEI integration.

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

### Fast Furnace Tiers

| Furnace | Normal speed | With Fast Coal | Standard recipe, normal / Fast Coal | Fuel doubling chance | Fast Coal burn time |
|---|---:|---:|---:|---:|---:|
| Fast Furnace | 150% | 200% | 133 / 100 ticks | 10% | 2,400 ticks |
| Iron Fast Furnace | 200% | 250% | 100 / 80 ticks | 15% | 2,800 ticks |
| Gold Fast Furnace | 250% | 300% | 80 / 66 ticks | 20% | 3,200 ticks |
| Diamond Fast Furnace | 300% | 350% | 66 / 57 ticks | 25% | 3,600 ticks |

Standard recipes take 200 ticks in a vanilla furnace. Each consumed fuel item independently rolls the listed chance to double its adjusted burn time. Fast Coal's listed burn times are before that roll; doubled values are 4,800, 5,600, 6,400, and 7,200 ticks respectively.

Craft the base Fast Furnace from eight Fast Cobblestone using the vanilla furnace pattern, then upgrade it in order:

- Surround Fast Furnace with eight iron blocks to craft Iron Fast Furnace.
- Surround Iron Fast Furnace with eight gold blocks to craft Gold Fast Furnace.
- Surround Gold Fast Furnace with eight diamond blocks to craft Diamond Fast Furnace.

Every tier supports vanilla smelting recipes and fuels, the furnace recipe book, inventory and experience handling, comparator output, directional placement, and familiar furnace sounds and particles. All tiers require a pickaxe, drop themselves, preserve custom names, and emit light level 13 while lit.

Every tier uses the same furnace GUI. Its information fly-out shows the tier's current speed, fuel doubling chance, remaining burn time, and total current-fuel burn time. Timers use exact tick counts and display seconds with one decimal place. All furnaces appear in Functional Blocks and the mod's Fast Furnace V2 creative tab.

Fast Cobblestone is the icon of the mod's creative tab. Fast Cobblestone, Fast Coal, and all furnace textures use animated lava veins. Iron and Gold Fast Furnace have furnace-shaped fronts, sides, and tops made from their tier materials, with distinct unlit and burning front models. All furnace textures share one vein layout so adjacent furnace tiers tile together.

## Optional JEI Integration

With a compatible JEI release installed:

- Each furnace's progress arrow opens smelting recipes, and its fire icon opens that tier's fuel category.
- Fuel categories calculate items smelted from each tier's actual cooking speed, including Fast Coal's speed and duration bonuses.
- Recipe transfer supports smelting and fuel insertion for every tier.
- Every furnace includes a short speed and fuel-doubling description; Fast Cobblestone and Fast Coal include acquisition information.
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
