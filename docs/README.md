# Fast Furnace V2 documentation

This documentation applies to **Fast Furnace V2 1.2.0** for **Minecraft 26.1.2** and **NeoForge 26.1.2.126 or newer**. It covers the built-in content and the extension points available to other mods.

## Documentation map

| Page | Contents |
|---|---|
| [Blocks](blocks.md) | Every registered block, its ID, acquisition, mining behavior, drops, and creative-tab placement |
| [Items](items.md) | Fast Coal, Extreme Coal, block items, and generated or reused crushed-ore items |
| [Fast Furnaces](furnaces.md) | Tier speeds, fuel duration bonuses, fuel doubling, smelting behavior, and saved progress |
| [Fast Crushers](crushers.md) | Raw-ore crushing, custom recipes, tier timing, output doubling, and pending results |
| [Accelerator](accelerator.md) | Coal conversion, lava placement, exact-work boosts, automation, and visual progress |
| [Recipes and fuels](recipes.md) | Crafting patterns, machine conversions, smelting behavior, and fuel reference tables |
| [World generation and resources](world-generation.md) | Mining bonuses, Extreme Coal Ore generation, and dynamic crushed-ore discovery |
| [Automation, GUI, and JEI](automation-and-jei.md) | Slots, hopper directions, information panels, comparators, persistence, and recipe-viewer integration |
| [Modding API](api.md) | Dependency setup, registration events, custom content, queries, priorities, and examples |

For installation and development commands, see the [project README](../README.md). Texture-generation instructions are in [tools/README.md](../tools/README.md).

## Quick start

1. Mine stone with a suitable non-Silk-Touch tool for a chance at bonus **Fast Cobblestone**, or mine coal ore for a chance at bonus **Fast Coal**.
2. Craft a **Fast Furnace** from eight Fast Cobblestone using the vanilla furnace pattern.
3. Use ordinary smelting recipes and fuels. Fast Coal and Extreme Coal provide additional speed and burn-duration bonuses.
4. Upgrade through **Iron**, **Gold**, and **Diamond** tiers by surrounding the previous furnace with eight matching metal blocks.
5. Find **Extreme Coal Ore** among regular coal ore for a stronger special fuel.
6. Use an **Accelerator** surrounded by lava on four horizontal sides to convert coal into Fast Coal. The Accelerator and base Fast Crusher have no built-in crafting recipe.

See [Recipes and fuels](recipes.md) for complete patterns and [World generation and resources](world-generation.md) for acquisition chances.

## Conventions

- **Tick:** one server game tick. At the standard 20 ticks per second, 20 ticks equal one second; lag can increase real elapsed time.
- **Ordinary furnace:** a furnace using vanilla fuel values and ordinary smelting behavior, rather than Fast Furnace V2's tier bonuses.
- **Fast Furnace family:** Fast Furnace, Iron Fast Furnace, Gold Fast Furnace, and Diamond Fast Furnace. “Fast Furnace” without “family” refers to the base tier.
- **Fast Crusher family:** Fast Crusher, Iron Fast Crusher, Gold Fast Crusher, and Diamond Fast Crusher.
- **Speed percentage:** processing speed relative to a vanilla furnace. For example, 200% completes a 200-tick operation in 100 ticks.
- **Before doubling:** a fuel duration after its tier adjustment but before that consumed item's independent random duration-doubling roll.
- **Built-in content:** the behavior described in these pages without another mod registering custom fuels, crusher recipes, or Accelerator conversions. The [API](api.md) can extend these lists.

All built-in random rolls are independent. A fuel-duration roll in a Fast Furnace does not affect later fuel items, and each completed crusher operation makes its own output roll.

## Common questions

### Why can I not craft an Accelerator or base Fast Crusher?

They intentionally have no built-in crafting recipe. They can be obtained in creative mode or supplied by commands, another mod, or a modpack. Crusher upgrades still require the preceding crusher tier.

### Why is my machine lit but not using fuel time?

Fast Furnaces and Fast Crushers pause their active fuel countdown while no required result can be processed. Banked fuel remains active, and its special bonuses are retained. See [Fast Furnaces](furnaces.md#fuel-countdown-and-saved-progress) or [Fast Crushers](crushers.md#blocked-and-pending-results).

### Why did my Accelerator stop without losing progress?

The Accelerator pauses when any horizontal lava side is missing or its output cannot accept the complete result. It does not consume boost work while paused. See [Accelerator pause and reset rules](accelerator.md#pause-and-reset-rules).

### Where are crushed ores?

The mod discovers raw ores during registration. It reuses a compatible external crushed item or dust when available and otherwise generates its own tinted crushed item. See [Dynamic crushed ores](world-generation.md#dynamic-crushed-ores).
