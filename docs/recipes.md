# Recipes and fuels

[Documentation home](README.md) · [Blocks](blocks.md) · [Items](items.md)

This page collects the **36 checked-in crafting recipes**, built-in machine conversions, alloying, smelting behavior, and fuel values. Generators, batteries, and power conduits do not have crafting recipes yet. Other mods can add crusher recipes, Accelerator conversions, Alloyer recipes, and fuel definitions through the [API](api.md).

## Crafting conventions

Patterns below show a 3×3 crafting grid. Blank cells must be empty unless noted. Metal upgrades use **blocks**, not ingots.

## Lava-Infused Furnace

Use eight Lava-Infused Cobblestone in the vanilla furnace pattern:

```text
F  F  F
F     F
F  F  F
```

`F` = Lava-Infused Cobblestone

Output: **1 × Lava-Infused Furnace**

## Lava-Infused Furnace upgrades

Each upgrade surrounds the previous furnace with eight metal blocks:

```text
M  M  M
M  P  M
M  M  M
```

`M` = upgrade material; `P` = previous furnace

| Previous furnace `P` | Metal blocks `M` | Output |
|---|---|---|
| Lava-Infused Furnace | 8 × Block of Lava-Infused Iron | Iron Lava-Infused Furnace |
| Iron Lava-Infused Furnace | 8 × Block of Lava-Infused Gold | Gold Lava-Infused Furnace |
| Gold Lava-Infused Furnace | 8 × Block of Lava-Infused Diamond | Diamond Lava-Infused Furnace |

Upgrades must follow this order; a lower tier cannot skip directly to Gold or Diamond.

## Lava-Infused Crusher

Use three Lava-Infused Diamonds, five Lava-Infused Iron Ingots, and one Lava-Infused Furnace:

```text
D  D  D
I  F  I
I  I  I
```

`D` = Lava-Infused Diamond; `I` = Lava-Infused Iron Ingot; `F` = Lava-Infused Furnace

Output: **1 × Lava-Infused Crusher**

## Lava-Infused Crusher upgrades

Upgrades use the same shaped pattern as furnace upgrades:

```text
M  M  M
M  P  M
M  M  M
```

| Previous crusher `P` | Metal blocks `M` | Output |
|---|---|---|
| Lava-Infused Crusher | 8 × Block of Lava-Infused Iron | Iron Lava-Infused Crusher |
| Iron Lava-Infused Crusher | 8 × Block of Lava-Infused Gold | Gold Lava-Infused Crusher |
| Gold Lava-Infused Crusher | 8 × Block of Lava-Infused Diamond | Diamond Lava-Infused Crusher |

## Block of Lava-Infused Coal (`fast_coal_block`)

### Packing

Fill the entire crafting grid with Lava-Infused Coal (`fast_coal`):

```text
C  C  C
C  C  C
C  C  C
```

`C` = Lava-Infused Coal (`fast_coal`)

Output: **1 × Block of Lava-Infused Coal (`fast_coal_block`)**

### Unpacking

Place one Block of Lava-Infused Coal (`fast_coal_block`) in a shapeless crafting recipe:

```text
1 × fast_coal_block → 9 × fast_coal
```

Packing and unpacking are reversible and lossless in item count.

## Block of Lava-Infused Coal (`extreme_coal_block`)

Use the same reversible storage pattern with Lava-Infused Coal (`extreme_coal`):

```text
E  E  E
E  E  E
E  E  E
```

`E` = Lava-Infused Coal (`extreme_coal`). Output: **1 × Block of Lava-Infused Coal (`extreme_coal_block`)**. One block also has a shapeless unpacking recipe back into **9 × `extreme_coal`**.

## Lava-Infused material storage blocks

Lava-Infused Iron Ingots, Lava-Infused Gold Ingots, and Lava-Infused Diamonds each have a reversible storage block:

```text
M  M  M        1 × Block of Lava-Infused material
M  M  M  →     9 × matching Lava-Infused material (shapeless unpacking)
M  M  M
```

`M` is nine matching Lava-Infused Iron Ingots, Lava-Infused Gold Ingots, or Lava-Infused Diamonds for the shaped packing recipe. The three Lava-Infused storage blocks are also required for furnace and crusher upgrades.

## Lava-Infused material tools and swords

All twelve tools use vanilla patterns with sticks and the matching Lava-Infused material. `M` is Lava-Infused Iron Ingot, Lava-Infused Gold Ingot, or Lava-Infused Diamond; `#` is Stick.

```text
Pickaxe       Shovel        Axe           Sword
M  M  M       M             M  M          M
   #             #          M  #          M
   #             #             #          #
```

Each recipe produces one `fastfurnacev2:fast_<material>_<tool>` item. Axe recipes use the left-handed vanilla orientation shown above. The finished Lava-Infused material also repairs its corresponding tool set. See [Lava-Infused tools and swords](items.md#lava-infused-material-tools-and-swords) for exact bonuses.

The Lava-Infused Pickaxe, Shovel, Axe, and Sword use the same four patterns with `M` = Lava-Infused Ingot and `#` = Stick. Their output IDs are `fastfurnacev2:extremely_fast_pickaxe`, `extremely_fast_shovel`, `extremely_fast_axe`, and `extremely_fast_sword`. The ingot also repairs all four. See [Lava-Infused tools and swords](items.md#lava-infused-alloy-tools-and-swords).

## Raw Lava-Infused material smelting

Each raw Lava-Infused material smelts directly into one finished material:

| Input | Output | Cooking time | Experience |
|---|---|---:|---:|
| Raw Lava-Infused Iron | Lava-Infused Iron Ingot | 200 ticks | 0.7 |
| Raw Lava-Infused Gold | Lava-Infused Gold Ingot | 200 ticks | 1.0 |
| Raw Lava-Infused Diamond | Lava-Infused Diamond | 200 ticks | 1.0 |

The corresponding crushed Lava-Infused items receive cloned smelting recipes with the same output, time, and experience.

## Accelerator crafting

Surround one Iron Lava-Infused Furnace with eight Redstone Blocks:

```text
R  R  R
R  F  R
R  R  R
```

`R` = Redstone Block; `F` = Iron Lava-Infused Furnace

Output: **1 × Accelerator**

## Lava-Infused Alloyer crafting

Surround one Accelerator with eight Blocks of Lava-Infused Coal (`extreme_coal_block`):

```text
E  E  E
E  A  E
E  E  E
```

`E` = Block of Lava-Infused Coal (`extreme_coal_block`); `A` = Accelerator

Output: **1 × Lava-Infused Alloyer**

## Lava-Infused Alloying

The Alloyer has three input slots, one Lava-Infused Coal (`extreme_coal`) fuel slot, and one output slot. Its built-in recipe is shapeless across those inputs:

```text
1 × Lava-Infused Iron Ingot + 1 × Lava-Infused Gold Ingot + 1 × Lava-Infused Diamond
    → 1 × Lava-Infused Ingot
```

The three ingredients can occupy the input slots in any order, but every ingredient must be present in a distinct slot. Only the **Lava-Infused Coal item with ID `fastfurnacev2:extreme_coal`** is accepted as fuel; each supplies 1,600 valid processing ticks. The base operation takes **800 ticks (40 seconds)**, reduced by 100 ticks per horizontal lava side to a minimum of 400 ticks. The Alloyer does not accept ordinary smelting, crushing, Accelerator, or crafting recipes. A blocked result or missing fuel pauses without consuming input or losing progress. See [Lava-Infused Alloyer](alloyer.md) for complete behavior.

## Accelerator conversions

The Accelerator does not use ordinary smelting JSON for these operations:

| Input | Output | Unboosted duration |
|---|---|---:|
| 1 × Coal | 1 × Lava-Infused Coal | 400 ticks |
| 1 × Iron Ingot | 1 × Lava-Infused Iron Ingot | 400 ticks |
| 1 × Gold Ingot | 1 × Lava-Infused Gold Ingot | 400 ticks |
| 1 × Diamond | 1 × Lava-Infused Diamond | 400 ticks |
| 1 × Coal Block | 1 × Block of Lava-Infused Coal | 3,600 ticks |
| 1 × Iron Block | 1 × Block of Lava-Infused Iron | 3,600 ticks |
| 1 × Gold Block | 1 × Block of Lava-Infused Gold | 3,600 ticks |
| 1 × Diamond Block | 1 × Block of Lava-Infused Diamond | 3,600 ticks |

All four horizontal neighboring positions must contain lava. Boosts change work per tick, not the recipe's total work requirement. See [Accelerator](accelerator.md) for exact boosted times and capacities.

## Built-in crushing

Each built-in raw-ore operation consumes one raw item and produces two selected crushed items:

| Input | Normal output | Successful output-doubling roll |
|---|---|---|
| Raw Iron | 2 × Crushed Iron | 4 × Crushed Iron |
| Raw Copper | 2 × Crushed Copper | 4 × Crushed Copper |
| Raw Gold | 2 × Crushed Gold | 4 × Crushed Gold |
| Raw Lava-Infused Iron | 2 × Crushed Lava-Infused Iron | 4 × Crushed Lava-Infused Iron |
| Raw Lava-Infused Gold | 2 × Crushed Lava-Infused Gold | 4 × Crushed Lava-Infused Gold |
| Raw Lava-Infused Diamond | 2 × Crushed Lava-Infused Diamond | 4 × Crushed Lava-Infused Diamond |

An external crushed item or dust replaces the shown result when discovery selects one. Modded raw ores discovered through conventional IDs or packaged raw-material tags receive equivalent recipes.

Built-in ore blocks instead produce three items per operation:

| Input | Normal output | Successful output-doubling roll |
|---|---|---|
| Regular or Deepslate Lava-Infused Coal Ore | 3 × Lava-Infused Coal | 6 × Lava-Infused Coal |
| Regular or Deepslate Lava-Infused Iron Ore | 3 × Crushed Lava-Infused Iron | 6 × Crushed Lava-Infused Iron |
| Regular or Deepslate Lava-Infused Gold Ore | 3 × Crushed Lava-Infused Gold | 6 × Crushed Lava-Infused Gold |
| Regular or Deepslate Lava-Infused Diamond Ore | 3 × Crushed Lava-Infused Diamond | 6 × Crushed Lava-Infused Diamond |

The tier's 10% / 15% / 20% / 25% output roll is independent for each completed operation. A blocked doubled result remains pending rather than becoming the smaller result. See [Lava-Infused Crushers](crushers.md).

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

The raw item's original smelting recipe remains available. Lava-Infused Furnaces can smelt either raw or crushed inputs using their normal tier rules.

## Fuel reference

### Ordinary fuel values

These values apply in ordinary furnaces and other machines using the normal fuel-values system:

| Fuel | Ordinary burn time |
|---|---:|
| Lava-Infused Coal (`fast_coal`) | 1,600 ticks |
| Lava-Infused Coal (`extreme_coal`) | 1,600 ticks |
| Block of Lava-Infused Coal (`fast_coal_block`) | 16,000 ticks |
| Block of Lava-Infused Coal (`extreme_coal_block`) | 16,000 ticks |

Both Lava-Infused Coal items match vanilla coal. Both storage blocks match a vanilla coal block, not the combined 14,400-tick value of nine individual coal items.

### Lava-Infused Furnace durations

The following values include tier scaling and the independent per-item duration roll where shown:

| Tier | `fast_coal` before / after doubling | `extreme_coal` before / after doubling | `fast_coal_block` before / after doubling | `extreme_coal_block` before / after doubling |
|---|---:|---:|---:|---:|
| Lava-Infused | 2,400 / 4,800 | 3,200 / 6,400 | 24,000 / 48,000 | 32,000 / 64,000 |
| Iron | 2,800 / 5,600 | 4,000 / 8,000 | 28,000 / 56,000 | 40,000 / 80,000 |
| Gold | 3,200 / 6,400 | 4,800 / 9,600 | 32,000 / 64,000 | 48,000 / 96,000 |
| Diamond | 3,600 / 7,200 | 5,600 / 11,200 | 36,000 / 72,000 | 56,000 / 112,000 |

All values are ticks. Every valid fuel consumed by a Lava-Infused Furnace makes the tier's independent duration roll, including ordinary fuels. Lava-Infused Crushers use the **before doubling** values and never roll for fuel duration.

### Accelerator boost values

Accelerator boosts use work units rather than ordinary furnace burn time:

| Fuel | Work supplied | Work per tick |
|---|---:|---:|
| Lava-Infused Coal (`fast_coal`) | 3,600 | 3 |
| Block of Lava-Infused Coal (`fast_coal_block`) | 4,000 | 3 |
| Lava-Infused Coal (`extreme_coal`) | 7,200 | 5 |
| Block of Lava-Infused Coal (`extreme_coal_block`) | 8,000 | 5 |

The Accelerator also operates without boost at one work unit per tick. See [Accelerator conversion capacities](accelerator.md#conversion-times-and-capacities).

## Built-in content without crafting recipes

- Lava-Infused Cobblestone.
- Lava-Infused Coal (`fast_coal`).
- Lava-Infused Coal (`extreme_coal`).
- Regular and Deepslate Lava-Infused Coal Ore.
- Regular and deepslate Lava-Infused Iron Ore, Lava-Infused Gold Ore, and Lava-Infused Diamond Ore.
- Raw Lava-Infused Iron, Raw Lava-Infused Gold, and Raw Lava-Infused Diamond.
- Lava-Infused Ingot, which instead has the dedicated machine recipe described in [Lava-Infused Alloying](#lava-infused-alloying).

Their acquisition is described in [Blocks](blocks.md), [Items](items.md), and [World generation](world-generation.md). The presence of creative-tab content, upgrades, or machine conversions does not imply a survival crafting recipe. Other mods and modpacks may supply their own acquisition methods.
