# Fast Furnace V2

Fast Furnace V2 is a NeoForge mod for Minecraft 26.1.2. It expands furnace progression with faster machines, ore crushing, animated Fast materials, powerful tools, and optional JEI integration.

## Features

- **Four furnace tiers** with faster smelting, special fuel bonuses, and familiar vanilla behavior.
- **Four crusher tiers** that turn raw ores into smeltable crushed materials, with a chance to double output.
- **Fast and Extreme materials** obtained through mining bonuses, ore replacement, and material conversion.
- **Fast and Extremely Fast tools** with faster actions, built-in enchantments, and other bonuses.
- **The Accelerator and Extremely Fast Alloyer** for advanced material conversion and alloying.
- **Mod compatibility** through crushed-ore discovery, a public extension API, and optional JEI displays.

See the [documentation home](docs/README.md) for a quick start and the complete player reference.

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 26.1.2 |
| NeoForge | 26.1.2.126 or newer |
| Java | 25 |

JEI is optional, but requires a release compatible with Minecraft 26.1.2 and this integration. The currently configured JEI API targets Minecraft 26.1 and is not runtime-compatible with 26.1.2; see [JEI compatibility](docs/automation-and-jei.md#current-compatibility-boundary).

## Installation

1. Install Java 25 and a NeoForge profile for Minecraft 26.1.2.
2. Build the mod or obtain its JAR from a release.
3. Place `fastfurnacev2-1.5.1.jar` in the Minecraft `mods` directory.

To build the JAR yourself, run the included Gradle wrapper from the project root:

```bash
# Windows
.\gradlew.bat build

# macOS / Linux
./gradlew build
```

The JAR is written to `build/libs/`.

## Documentation

| Topic | Guide |
|---|---|
| Start here | [Documentation home and quick start](docs/README.md) |
| Content | [Blocks](docs/blocks.md), [items](docs/items.md), and [recipes and fuels](docs/recipes.md) |
| Machines | [Fast Furnaces](docs/furnaces.md), [Fast Crushers](docs/crushers.md), [Accelerator](docs/accelerator.md), and [Extremely Fast Alloyer](docs/alloyer.md) |
| World and resources | [World generation and dynamic resources](docs/world-generation.md) |
| Automation and viewers | [Automation, GUI, and JEI](docs/automation-and-jei.md) |
| Mod developers | [Public API](docs/api.md) |
| Contributors | [Development guide](docs/development.md) and [architecture notes](docs/architecture.md) |
| Texture tooling | [Texture generation](tools/README.md) |

## Contributing

Keep changes focused, update relevant documentation, and run the checks for the affected area. The [development guide](docs/development.md) covers setup, commands, testing, and version management.

This project was developed with assistance from [Claude Code](https://claude.com/claude-code), under the maintainer's direction. This does not imply endorsement by Anthropic.

## License

Licensed under the [MIT License](LICENSE). The original [NeoForge MDK template license](TEMPLATE_LICENSE.txt) is retained. Minecraft, NeoForge, Mojang mappings, and copied vanilla assets remain governed by their respective licenses.
