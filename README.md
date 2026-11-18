# Fast Furnace V2

A NeoForge mod for Minecraft 26.1.2, currently adding Fast Cobblestone as its first block.

## Features

### Fast Cobblestone

- Registry ID: `fastfurnacev2:fast_cobblestone`
- Uses a local copy of the vanilla cobblestone texture and matching block/item models.
- Matches vanilla cobblestone's stone map color, base-drum note-block instrument, 2.0 hardness, and 6.0 blast resistance.
- Is mineable with a pickaxe and requires an appropriate tool for drops, matching vanilla cobblestone's wooden-pickaxe-or-better behavior.
- Drops Fast Cobblestone—not vanilla cobblestone—when broken with an appropriate tool.
- Appears in both the vanilla **Building Blocks** tab and the mod's **Fast Furnace V2** tab, whose icon is Fast Cobblestone.

The former MDK example blocks, items, configuration, startup messages, client setup, and empty Mixin registration have been removed.

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
```

## Project Structure

```text
src/main/java/com/skylark193/fastfurnacev2/
└── FastFurnaceV2.java             # Block/item/tab registration and creative tabs
src/main/resources/
├── assets/fastfurnacev2/
│   ├── blockstates/fast_cobblestone.json
│   ├── items/fast_cobblestone.json
│   ├── lang/en_us.json
│   ├── models/block/fast_cobblestone.json
│   └── textures/block/fast_cobblestone.png
└── data/
    ├── fastfurnacev2/loot_table/blocks/fast_cobblestone.json
    └── minecraft/tags/block/mineable/pickaxe.json
src/main/templates/META-INF/
└── neoforge.mods.toml             # Metadata populated from gradle.properties
```

## Block Behavior and Metadata

Fast Cobblestone's Java properties mirror the vanilla cobblestone definition. Its pickaxe-minable tag controls mining behavior, while its loot table explicitly specifies the Fast Cobblestone item as its drop. Keep these definitions synchronized if its behavior changes.

Use [gradle.properties](gradle.properties) to change the mod ID, display name, version, Minecraft/NeoForge versions, group ID, or license. If the mod ID changes, also update `FastFurnaceV2.MODID`, resource paths, package references, and metadata as needed.

The `neoforge.mods.toml` file is generated from [src/main/templates/META-INF/neoforge.mods.toml](src/main/templates/META-INF/neoforge.mods.toml) during resource processing. Do not edit generated files under `build/`.

## Contributing

1. Make focused changes and remove obsolete code and imports.
2. Update [README.md](README.md) and [CLAUDE.md](CLAUDE.md) whenever project behavior, structure, commands, or development guidance changes.
3. Run `.\gradlew.bat build` on Windows or `./gradlew build` on macOS/Linux.
4. Commit all changes that pass validation; do not commit while validation is failing.

GitHub Actions runs the same Gradle build on pushes and pull requests using JDK 25.

## Resources

- [NeoForge documentation](https://docs.neoforged.net/)
- [NeoForge Discord](https://discord.neoforged.net/)
- [Mojang mappings license information](https://github.com/NeoForged/NeoForm/blob/main/Mojang.md)

## License

This project is licensed under the [MIT License](LICENSE).

The original NeoForge MDK template license is retained in [TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt). Minecraft, NeoForge, Mojang mappings, and copied vanilla assets are governed by their respective licenses.
