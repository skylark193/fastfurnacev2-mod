# Recipes and fuels

[Documentation home](README.md) · [Blocks](blocks.md) · [Items](items.md)

This page collects the **nine checked-in crafting recipes**, built-in machine conversions, smelting behavior, and fuel values. Other mods can add crusher recipes, Accelerator conversions, and fuel definitions through the [API](api.md).

## Crafting conventions

Patterns below show a 3×3 crafting grid. Blank cells must be empty unless noted. Metal upgrades use **blocks**, not ingots.

## Fast Furnace

Use eight Fast Cobblestone in the vanilla furnace pattern:

```text
F  F  F
F     F
F  F  F
```

`F` = Fast Cobblestone

Output: **1 × Fast Furnace**

## Fast Furnace upgrades

Each upgrade surrounds the previous furnace with eight metal blocks:

```text
M  M  M
M  P  M
M  M  M
```

`M` = upgrade material; `P` = previous furnace

| Previous furnace `P` | Metal blocks `M` | Output |
|---|---|---|
| Fast Furnace | 8 × Iron Block | Iron Fast Furnace |
| Iron Fast Furnace | 8 × Gold Block | Gold Fast Furnace |
| Gold Fast Furnace | 8 × Diamond Block | Diamond Fast Furnace |

Upgrades must follow this order; a lower tier cannot skip directly to Gold or Diamond.

## Fast Crusher upgrades

The **base Fast Crusher has no built-in crafting recipe**. Its upgrades use the same shaped pattern as furnace upgrades:

```text
M  M  M
M  P  M
M  M  M
```

| Previous crusher `P` | Metal blocks `M` | Output |
|---|---|---|
| Fast Crusher | 8 × Iron Block | Iron Fast Crusher |
| Iron Fast Crusher | 8 × Gold Block | Gold Fast Crusher |
| Gold Fast Crusher | 8 × Diamond Block | Diamond Fast Crusher |

The base crusher must first be supplied through creative mode, commands, another mod, or a modpack.

## Block of Fast Coal

### Packing

Fill the entire crafting grid with Fast Coal:

```text
C  C  C
C  C  C
C  C  C
```

`C` = Fast Coal

Output: **1 × Block of Fast Coal**

### Unpacking

Place one Block of Fast Coal in a shapeless crafting recipe:

```text
1 × Block of Fast Coal → 9 × Fast Coal
```

Packing and unpacking are reversible and lossless in item count.

## Accelerator conversions

The Accelerator has **no built-in crafting recipe** and does not use ordinary smelting JSON for these operations:

| Input | Output | Unboosted duration |
|---|---|---:|
| 1 × Coal | 1 × Fast Coal | 400 ticks |
| 1 × Coal Block | 1 × Block of Fast Coal | 3,600 ticks |

All four horizontal neighboring positions must contain lava. Boosts change work per tick, not the recipe's total work requirement. See [Accelerator](accelerator.md) for exact boosted times and capacities.

## Built-in crushing

Each built-in raw-ore operation consumes one raw item and produces two selected crushed items:

| Input | Normal output | Successful output-doubling roll |
|---|---|---|
| Raw Iron | 2 × Crushed Iron | 4 × Crushed Iron |
| Raw Copper | 2 × Crushed Copper | 4 × Crushed Copper |
| Raw Gold | 2 × Crushed Gold | 4 × Crushed Gold |

An external crushed item or dust replaces the shown result when discovery selects one. Modded raw ores discovered through conventional IDs or packaged raw-material tags receive equivalent recipes.

The tier's 10% / 15% / 20% / 25% output roll is independent for each completed operation. A blocked doubled result remains pending rather than becoming two items. See [Fast Crushers](crushers.md).

## Crushed-ore smelting

Fast Furnace V2 generates only missing ordinary `minecraft:smelting` recipes for selected crushed items. It does not clone blasting, smoking, or other cooking recipe types:

```text
1 × selected crushed item → the source raw item's ordinary smelting result
```

The generated recipe preserves the original recipe's:

- Output item and count.
- Experience.
- Cooking time.
- Category.
- Group.

If an external result already has a smelting recipe, that recipe is reused and no duplicate is generated. When such a recipe is available, crushing a raw ore into two crushed items and then smelting both preserves the original per-item smelting behavior while yielding two sets of results.

The raw item's original smelting recipe remains available. Fast Furnaces can smelt either raw or crushed inputs using their normal tier rules.

## Fuel reference

### Ordinary fuel values

These values apply in ordinary furnaces and other machines using the normal fuel-values system:

| Fuel | Ordinary burn time |
|---|---:|
| Fast Coal | 1,600 ticks |
| Extreme Coal | 1,600 ticks |
| Block of Fast Coal | 16,000 ticks |

Fast Coal and Extreme Coal match vanilla coal. Block of Fast Coal matches a vanilla coal block, not the combined 14,400-tick value of nine Fast Coal items.

### Fast Furnace durations

The following values include tier scaling and the independent per-item duration roll where shown:

| Tier | Fast Coal before / after doubling | Extreme Coal before / after doubling | Block of Fast Coal before / after doubling |
|---|---:|---:|---:|
| Fast | 2,400 / 4,800 | 3,200 / 6,400 | 24,000 / 48,000 |
| Iron | 2,800 / 5,600 | 4,000 / 8,000 | 28,000 / 56,000 |
| Gold | 3,200 / 6,400 | 4,800 / 9,600 | 32,000 / 64,000 |
| Diamond | 3,600 / 7,200 | 5,600 / 11,200 | 36,000 / 72,000 |

All values are ticks. Every valid fuel consumed by a Fast Furnace makes the tier's independent duration roll, including ordinary fuels. Fast Crushers use the **before doubling** values and never roll for fuel duration.

### Accelerator boost values

Accelerator boosts use work units rather than ordinary furnace burn time:

| Fuel | Work supplied | Work per tick |
|---|---:|---:|
| Fast Coal | 3,600 | 3 |
| Block of Fast Coal | 4,000 | 3 |
| Extreme Coal | 7,200 | 5 |

The Accelerator also operates without boost at one work unit per tick. See [Accelerator conversion capacities](accelerator.md#conversion-times-and-capacities).

## Built-in content without crafting recipes

- Fast Cobblestone.
- Fast Coal.
- Extreme Coal.
- Extreme Coal Ore.
- Base Fast Crusher.
- Accelerator.

Their acquisition is described in [Blocks](blocks.md), [Items](items.md), and [World generation](world-generation.md). The presence of creative-tab content, upgrades, or machine conversions does not imply a survival crafting recipe. Other mods and modpacks may supply their own acquisition methods.
