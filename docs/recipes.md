# Recipes and fuels

[Documentation home](README.md) · [Blocks](blocks.md) · [Items](items.md)

This page collects the **36 checked-in crafting recipes**, built-in machine conversions, alloying, smelting behavior, and fuel values. Generators and batteries do not have crafting recipes yet. Other mods can add crusher recipes, Accelerator conversions, Alloyer recipes, and fuel definitions through the [API](api.md).

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
| Fast Furnace | 8 × Block of Fast Iron | Iron Fast Furnace |
| Iron Fast Furnace | 8 × Block of Fast Gold | Gold Fast Furnace |
| Gold Fast Furnace | 8 × Block of Fast Diamond | Diamond Fast Furnace |

Upgrades must follow this order; a lower tier cannot skip directly to Gold or Diamond.

## Fast Crusher

Use three Fast Diamonds, five Fast Iron Ingots, and one Fast Furnace:

```text
D  D  D
I  F  I
I  I  I
```

`D` = Fast Diamond; `I` = Fast Iron Ingot; `F` = Fast Furnace

Output: **1 × Fast Crusher**

## Fast Crusher upgrades

Upgrades use the same shaped pattern as furnace upgrades:

```text
M  M  M
M  P  M
M  M  M
```

| Previous crusher `P` | Metal blocks `M` | Output |
|---|---|---|
| Fast Crusher | 8 × Block of Fast Iron | Iron Fast Crusher |
| Iron Fast Crusher | 8 × Block of Fast Gold | Gold Fast Crusher |
| Gold Fast Crusher | 8 × Block of Fast Diamond | Diamond Fast Crusher |

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

## Block of Extreme Coal

Use the same reversible storage pattern with Extreme Coal:

```text
E  E  E
E  E  E
E  E  E
```

`E` = Extreme Coal. Output: **1 × Block of Extreme Coal**. One block also has a shapeless unpacking recipe back into **9 × Extreme Coal**.

## Fast material storage blocks

Fast Iron Ingots, Fast Gold Ingots, and Fast Diamonds each have a reversible storage block:

```text
M  M  M        1 × Block of Fast material
M  M  M  →     9 × matching Fast material (shapeless unpacking)
M  M  M
```

`M` is nine matching Fast Iron Ingots, Fast Gold Ingots, or Fast Diamonds for the shaped packing recipe. The three Fast storage blocks are also required for furnace and crusher upgrades.

## Fast tools and swords

All twelve tools use vanilla patterns with sticks and the matching Fast material. `M` is Fast Iron Ingot, Fast Gold Ingot, or Fast Diamond; `#` is Stick.

```text
Pickaxe       Shovel        Axe           Sword
M  M  M       M             M  M          M
   #             #          M  #          M
   #             #             #          #
```

Each recipe produces one `fastfurnacev2:fast_<material>_<tool>` item. Axe recipes use the left-handed vanilla orientation shown above. The finished Fast material also repairs its corresponding tool set. See [Fast tools and swords](items.md#fast-tools-and-swords) for exact bonuses.

The Extremely Fast Pickaxe, Shovel, Axe, and Sword use the same four patterns with `M` = Extremely Fast Ingot and `#` = Stick. Their output IDs are `fastfurnacev2:extremely_fast_pickaxe`, `extremely_fast_shovel`, `extremely_fast_axe`, and `extremely_fast_sword`. The ingot also repairs all four. See [Extremely Fast tools and swords](items.md#extremely-fast-tools-and-swords).

## Raw Fast material smelting

Each raw Fast material smelts directly into one finished material:

| Input | Output | Cooking time | Experience |
|---|---|---:|---:|
| Raw Fast Iron | Fast Iron Ingot | 200 ticks | 0.7 |
| Raw Fast Gold | Fast Gold Ingot | 200 ticks | 1.0 |
| Raw Fast Diamond | Fast Diamond | 200 ticks | 1.0 |

The corresponding crushed Fast items receive cloned smelting recipes with the same output, time, and experience.

## Accelerator crafting

Surround one Iron Fast Furnace with eight Redstone Blocks:

```text
R  R  R
R  F  R
R  R  R
```

`R` = Redstone Block; `F` = Iron Fast Furnace

Output: **1 × Accelerator**

## Extremely Fast Alloyer crafting

Surround one Accelerator with eight Blocks of Extreme Coal:

```text
E  E  E
E  A  E
E  E  E
```

`E` = Block of Extreme Coal; `A` = Accelerator

Output: **1 × Extremely Fast Alloyer**

## Extremely Fast Alloying

The Alloyer has three input slots, one Extreme Coal fuel slot, and one output slot. Its built-in recipe is shapeless across those inputs:

```text
1 × Fast Iron Ingot + 1 × Fast Gold Ingot + 1 × Fast Diamond
    → 1 × Extremely Fast Ingot
```

The three ingredients can occupy the input slots in any order, but every ingredient must be present in a distinct slot. Only the **Extreme Coal item** is accepted as fuel; each supplies 1,600 valid processing ticks. The base operation takes **800 ticks (40 seconds)**, reduced by 100 ticks per horizontal lava side to a minimum of 400 ticks. The Alloyer does not accept ordinary smelting, crushing, Accelerator, or crafting recipes. A blocked result or missing fuel pauses without consuming input or losing progress. See [Extremely Fast Alloyer](alloyer.md) for complete behavior.

## Accelerator conversions

The Accelerator does not use ordinary smelting JSON for these operations:

| Input | Output | Unboosted duration |
|---|---|---:|
| 1 × Coal | 1 × Fast Coal | 400 ticks |
| 1 × Iron Ingot | 1 × Fast Iron Ingot | 400 ticks |
| 1 × Gold Ingot | 1 × Fast Gold Ingot | 400 ticks |
| 1 × Diamond | 1 × Fast Diamond | 400 ticks |
| 1 × Coal Block | 1 × Block of Fast Coal | 3,600 ticks |
| 1 × Iron Block | 1 × Block of Fast Iron | 3,600 ticks |
| 1 × Gold Block | 1 × Block of Fast Gold | 3,600 ticks |
| 1 × Diamond Block | 1 × Block of Fast Diamond | 3,600 ticks |

All four horizontal neighboring positions must contain lava. Boosts change work per tick, not the recipe's total work requirement. See [Accelerator](accelerator.md) for exact boosted times and capacities.

## Built-in crushing

Each built-in raw-ore operation consumes one raw item and produces two selected crushed items:

| Input | Normal output | Successful output-doubling roll |
|---|---|---|
| Raw Iron | 2 × Crushed Iron | 4 × Crushed Iron |
| Raw Copper | 2 × Crushed Copper | 4 × Crushed Copper |
| Raw Gold | 2 × Crushed Gold | 4 × Crushed Gold |
| Raw Fast Iron | 2 × Crushed Fast Iron | 4 × Crushed Fast Iron |
| Raw Fast Gold | 2 × Crushed Fast Gold | 4 × Crushed Fast Gold |
| Raw Fast Diamond | 2 × Crushed Fast Diamond | 4 × Crushed Fast Diamond |

An external crushed item or dust replaces the shown result when discovery selects one. Modded raw ores discovered through conventional IDs or packaged raw-material tags receive equivalent recipes.

Built-in ore blocks instead produce three items per operation:

| Input | Normal output | Successful output-doubling roll |
|---|---|---|
| Regular or Deepslate Extreme Coal Ore | 3 × Extreme Coal | 6 × Extreme Coal |
| Regular or Deepslate Fast Iron Ore | 3 × Crushed Fast Iron | 6 × Crushed Fast Iron |
| Regular or Deepslate Fast Gold Ore | 3 × Crushed Fast Gold | 6 × Crushed Fast Gold |
| Regular or Deepslate Fast Diamond Ore | 3 × Crushed Fast Diamond | 6 × Crushed Fast Diamond |

The tier's 10% / 15% / 20% / 25% output roll is independent for each completed operation. A blocked doubled result remains pending rather than becoming the smaller result. See [Fast Crushers](crushers.md).

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
| Block of Extreme Coal | 16,000 ticks |

Fast Coal and Extreme Coal match vanilla coal. Both storage blocks match a vanilla coal block, not the combined 14,400-tick value of nine individual coal items.

### Fast Furnace durations

The following values include tier scaling and the independent per-item duration roll where shown:

| Tier | Fast Coal before / after doubling | Extreme Coal before / after doubling | Block of Fast Coal before / after doubling | Block of Extreme Coal before / after doubling |
|---|---:|---:|---:|---:|
| Fast | 2,400 / 4,800 | 3,200 / 6,400 | 24,000 / 48,000 | 32,000 / 64,000 |
| Iron | 2,800 / 5,600 | 4,000 / 8,000 | 28,000 / 56,000 | 40,000 / 80,000 |
| Gold | 3,200 / 6,400 | 4,800 / 9,600 | 32,000 / 64,000 | 48,000 / 96,000 |
| Diamond | 3,600 / 7,200 | 5,600 / 11,200 | 36,000 / 72,000 | 56,000 / 112,000 |

All values are ticks. Every valid fuel consumed by a Fast Furnace makes the tier's independent duration roll, including ordinary fuels. Fast Crushers use the **before doubling** values and never roll for fuel duration.

### Accelerator boost values

Accelerator boosts use work units rather than ordinary furnace burn time:

| Fuel | Work supplied | Work per tick |
|---|---:|---:|
| Fast Coal | 3,600 | 3 |
| Block of Fast Coal | 4,000 | 3 |
| Extreme Coal | 7,200 | 5 |
| Block of Extreme Coal | 8,000 | 5 |

The Accelerator also operates without boost at one work unit per tick. See [Accelerator conversion capacities](accelerator.md#conversion-times-and-capacities).

## Built-in content without crafting recipes

- Fast Cobblestone.
- Fast Coal.
- Extreme Coal.
- Regular and Deepslate Extreme Coal Ore.
- Regular and deepslate Fast Iron Ore, Fast Gold Ore, and Fast Diamond Ore.
- Raw Fast Iron, Raw Fast Gold, and Raw Fast Diamond.
- Extremely Fast Ingot, which instead has the dedicated machine recipe described in [Extremely Fast Alloying](#extremely-fast-alloying).

Their acquisition is described in [Blocks](blocks.md), [Items](items.md), and [World generation](world-generation.md). The presence of creative-tab content, upgrades, or machine conversions does not imply a survival crafting recipe. Other mods and modpacks may supply their own acquisition methods.
