# Fast Furnace V2 documentation

This guide covers **Fast Furnace V2 1.6.1** for **Minecraft 26.1.2**. Public API documentation refers to **API 1.2.0**.

## Start playing

1. **Gather Fast Cobblestone.** Mine stone in survival mode with a suitable tool and without Silk Touch. There is a base 10% chance to receive Fast Cobblestone in addition to normal drops.
2. **Craft a Fast Furnace.** Arrange eight Fast Cobblestone in the vanilla furnace pattern.
3. **Collect special fuel.** Coal ore can grant bonus Fast Coal under similar mining conditions. Fast and Extreme ores can also replace vanilla ore during chunk processing.
4. **Upgrade your machines.** Use Blocks of Fast Iron, Fast Gold, and Fast Diamond to progress through furnace and crusher tiers.
5. **Expand production.** Add a Fast Crusher for additional ore yield, an Accelerator for Fast material conversion, and an Extremely Fast Alloyer for endgame tools.

Each machine GUI includes an **`i`** button with current timing, speed, fuel, and other useful information.

## Player and modpack guides

| Guide | What it covers |
|---|---|
| [Blocks](blocks.md) | Complete block index, mining properties, acquisition, and machine appearances |
| [Items](items.md) | Fuels, materials, crushed ores, and tool bonuses |
| [Recipes and fuels](recipes.md) | Crafting patterns, machine conversions, smelting, and fuel tables |
| [Fast Furnaces](furnaces.md) | Tier upgrades, processing speed, fuel doubling, and pause/resume behavior |
| [Fast Crushers](crushers.md) | Ore processing, output doubling, fuel use, and pending output |
| [Accelerator](accelerator.md) | Lava setup, conversions, boosts, and exact work accounting |
| [Extremely Fast Alloyer](alloyer.md) | Three-input alloying, Extreme Coal fuel, and lava-adjusted timing |
| [World generation and dynamic resources](world-generation.md) | Mining bonuses, ore replacement, and modded crushed-ore discovery |
| [Automation, GUI, and JEI](automation-and-jei.md) | Slots, hopper directions, information panels, and recipe viewing |

## Developer resources

- [Public modding API](api.md): custom fuels, crusher and Alloyer recipes, Accelerator conversions, boosts, and tool integration.
- [Development guide](development.md): environment setup, build commands, validation, and versioning.
- [Architecture notes](architecture.md): code map and version-specific implementation constraints.
- [Texture generation](../tools/README.md): source artwork, deterministic generators, regeneration, and tests.

## Common questions

### Does Fast Coal change ordinary furnaces?

No. Fast Coal burns like vanilla coal in ordinary furnaces. Its speed and duration bonuses apply only in Fast Furnace V2 machines, plus its separate Accelerator boost behavior.

### Will installing the mod affect an existing world?

Yes. Previously generated chunks receive a once-only ore-replacement pass when loaded. Regular and deepslate ores are tracked separately. See [world generation](world-generation.md#new-and-existing-chunks) before using the mod in a valuable world.

### Why does my crusher show a smelting interface?

Crushers intentionally share the furnace menu foundation, but only registered crusher recipes process. The progress arrow opens crushing recipes when compatible JEI is installed.

### Why is JEI not loading?

JEI 29.2.0.21 declares compatibility with Minecraft 26.1, not 26.1.2. Use a JEI release compatible with both Minecraft 26.1.2 and this integration. The mod itself does not require JEI.

### Are other mods' crushed ores supported?

Yes. Fast Furnace V2 prefers suitable registered external crushed items or dusts and generates a fallback only when needed. See [dynamic crushed ores](world-generation.md#dynamic-crushed-ores).

### Where are the source and build instructions?

The project root contains the source, Gradle wrapper, and license. Start with the [development guide](development.md) for local setup.
