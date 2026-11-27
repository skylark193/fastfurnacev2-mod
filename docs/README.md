# Fast Furnace V2 documentation

This guide covers **Fast Furnace V2 1.13.0** for **Minecraft 26.1.2**. Public API documentation refers to **API 1.4.0**.

The former Fast and Extreme materials now share Lava-Infused display names. Their registry IDs are unchanged; guides include IDs such as `fast_coal` and `extreme_coal` where their behavior differs.

## Start playing

1. **Gather Lava-Infused Cobblestone.** Mine stone in survival mode with a suitable tool and without Silk Touch. There is a base 10% chance to receive Lava-Infused Cobblestone in addition to normal drops.
2. **Craft a Lava-Infused Furnace.** Arrange eight Lava-Infused Cobblestone in the vanilla furnace pattern.
3. **Collect special fuel.** Coal ore can grant bonus Lava-Infused Coal under similar mining conditions. Lava-Infused ore variants can also replace vanilla ore during chunk processing.
4. **Upgrade your machines.** Use Blocks of Lava-Infused Iron, Lava-Infused Gold, and Lava-Infused Diamond to progress through furnace and crusher tiers.
5. **Expand production.** Add a Lava-Infused Crusher for additional ore yield, an Accelerator for Lava-Infused material conversion, and a Lava-Infused Alloyer for endgame tools.

Furnace, crusher, Accelerator, Alloyer, and generator GUIs include an **`i`** button with current timing, speed, fuel, and other useful information. Generators and batteries also have an **`E`** button for per-side FE configuration; right-click a conduit to configure its network and connections. Energy-block item tooltips summarize FE rates and storage. With Jade installed, looking at a machine or conduit displays live status such as burn time, progress, FE generation, storage, and transfer rates.

## Player and modpack guides

| Guide | What it covers |
|---|---|
| [Blocks](blocks.md) | Complete block index, mining properties, acquisition, and machine appearances |
| [Items](items.md) | Fuels, materials, crushed ores, and tool bonuses |
| [Recipes and fuels](recipes.md) | Crafting patterns, machine conversions, smelting, and fuel tables |
| [Lava-Infused Furnaces](furnaces.md) | Tier upgrades, processing speed, fuel doubling, and pause/resume behavior |
| [Lava-Infused Crushers](crushers.md) | Ore processing, output doubling, fuel use, and pending output |
| [Accelerator](accelerator.md) | Lava setup, conversions, boosts, and exact work accounting |
| [Lava-Infused Alloyer](alloyer.md) | Three-input alloying, `extreme_coal` fuel, and lava-adjusted timing |
| [Generators, batteries, and conduits](energy.md) | FE generation, portable storage, side configuration, and rate-limited conduit networks |
| [World generation and dynamic resources](world-generation.md) | Mining bonuses, ore replacement, and modded crushed-ore discovery |
| [Automation, GUI, JEI, and Jade](automation-and-jei.md) | Slots, energy configuration, information panels, recipe viewing, and Jade status |

## Developer resources

- [Public modding API](api.md): custom fuels, crusher and Alloyer recipes, Accelerator conversions, boosts, and tool integration.
- [Development guide](development.md): environment setup, build commands, validation, and versioning.
- [Architecture notes](architecture.md): code map and version-specific implementation constraints.
- [Texture generation](../tools/README.md): source artwork, deterministic generators, regeneration, and tests.

## Common questions

### Does Lava-Infused Coal change ordinary furnaces?

No. Lava-Infused Coal burns like vanilla coal in ordinary furnaces. Its speed and duration bonuses apply only in Fast Furnace V2 machines, plus its separate Accelerator boost behavior.

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
