# Lava-Infused Furnaces

[Documentation home](README.md) · [Recipes](recipes.md) · [Automation and GUI](automation-and-jei.md)

The Lava-Infused Furnace family contains four vanilla-compatible smelting machines:

| Tier | Block ID | Normal speed | Upgrade material |
|---|---|---:|---|
| Lava-Infused | `fastfurnacev2:fast_furnace` | 150% | Eight Lava-Infused Cobblestone around an empty center |
| Iron | `fastfurnacev2:iron_fast_furnace` | 200% | Eight Blocks of Lava-Infused Iron around Lava-Infused Furnace |
| Gold | `fastfurnacev2:gold_fast_furnace` | 250% | Eight Blocks of Lava-Infused Gold around Iron Lava-Infused Furnace |
| Diamond | `fastfurnacev2:diamond_fast_furnace` | 300% | Eight Blocks of Lava-Infused Diamond around Gold Lava-Infused Furnace |

All four support ordinary `minecraft:smelting` recipes, ordinary and modded fuels, the vanilla furnace recipe book, fuel remainders, smelting experience, comparators, directional placement, custom names, and familiar furnace sounds and particles. Blasting and smoking recipes are not substituted for ordinary smelting recipes. They require a pickaxe, drop themselves, and emit light level 13 while lit.

## Processing speed

Processing time is calculated from the recipe's ordinary smelting time:

```text
processing ticks = max(1, floor(ordinary ticks × 100 / effective speed percent))
```

Lava-Infused Coal and Block of Lava-Infused Coal from the `fast_coal` family add **50 percentage points** to the tier's speed while actively burning. The corresponding `extreme_coal` items add **100 percentage points**. These are additions to the tier speed, not multipliers.

The following table uses a standard 200-tick smelting recipe:

| Tier | Effective speed: ordinary / `fast_coal` / `extreme_coal` | Processing time: ordinary / `fast_coal` / `extreme_coal` |
|---|---:|---:|
| Lava-Infused | 150% / 200% / 250% | 133 / 100 / 80 ticks |
| Iron | 200% / 250% / 300% | 100 / 80 / 66 ticks |
| Gold | 250% / 300% / 350% | 80 / 66 / 57 ticks |
| Diamond | 300% / 350% / 400% | 66 / 57 / 50 ticks |

Recipes with other ordinary cooking times use the same formula. Another mod can register fuels with different speed bonuses through the [API](api.md#fuels-and-custom-bonuses).

## Fuel-duration bonuses

Outside these machines, both Lava-Infused Coal items have the same ordinary fuel value as vanilla coal: **1,600 ticks**. Both Blocks of Lava-Infused Coal have the same ordinary value as a vanilla coal block: **16,000 ticks**.

Lava-Infused Coal (`fast_coal`) and its block use these tier duration multipliers:

| Tier | `fast_coal` duration multiplier |
|---|---:|
| Lava-Infused | 150% |
| Iron | 175% |
| Gold | 200% |
| Diamond | 225% |

The `extreme_coal` family doubles `fast_coal`'s **added** duration rather than doubling the whole adjusted value:

```text
fast_coal duration    = floor(ordinary duration × fast_coal percentage / 100)
extreme_coal duration = ordinary duration + 2 × (fast_coal duration - ordinary duration)
```

### Built-in fuel durations in Lava-Infused Furnaces

| Tier | `fast_coal` | `fast_coal` after doubling | `extreme_coal` | `extreme_coal` after doubling |
|---|---:|---:|---:|---:|
| Lava-Infused | 2,400 | 4,800 | 3,200 | 6,400 |
| Iron | 2,800 | 5,600 | 4,000 | 8,000 |
| Gold | 3,200 | 6,400 | 4,800 | 9,600 |
| Diamond | 3,600 | 7,200 | 5,600 | 11,200 |

| Tier | `fast_coal_block` | `fast_coal_block` after doubling | `extreme_coal_block` | `extreme_coal_block` after doubling |
|---|---:|---:|---:|---:|
| Lava-Infused | 24,000 | 48,000 | 32,000 | 64,000 |
| Iron | 28,000 | 56,000 | 40,000 | 80,000 |
| Gold | 32,000 | 64,000 | 48,000 | 96,000 |
| Diamond | 36,000 | 72,000 | 56,000 | 112,000 |

All durations are in ticks.

## Independent fuel doubling

Every valid fuel item consumed by a Lava-Infused Furnace independently rolls the tier's chance to double its adjusted burn time:

| Tier | Chance per consumed fuel item |
|---|---:|
| Lava-Infused | 10% |
| Iron | 15% |
| Gold | 20% |
| Diamond | 25% |

This roll applies to ordinary fuels, all four built-in special coal items and blocks, and registered custom fuels. It is a duration roll, not an output-doubling roll. Lava-Infused Crushers never make this roll; their equivalent percentage applies to completed output instead.

The machine tracks the definition from the fuel stack actually consumed. Replacing the item in the fuel slot does not remove the active fuel's speed or duration effect.

## Fuel countdown and saved progress

The active fuel countdown decreases only while the required nonempty result can be processed. The countdown pauses for:

- Empty input.
- Input with no ordinary smelting recipe.
- An output slot containing a different item.
- An output slot without room for the complete result.

While paused, banked fuel keeps the block lit and retains its active special-fuel state. No additional fuel item is consumed merely to idle. Countdown and processing resume when valid work returns.

If fuel expires during an operation and no replacement fuel is available, recipe progress remains saved instead of cooling down or resetting. Supplying new fuel resumes that operation. If changing the active fuel changes processing speed, existing progress is rescaled to preserve its completed fraction.

Clearing the input or replacing it with a different item resets processing progress. Changing only the count of the same input does not by itself select a new recipe.

## Results, experience, and persistence

- Ordinary smelting output, experience, cooking time, category, and group remain those of the selected recipe.
- Fuel remainders such as empty buckets are retained using vanilla behavior.
- Exact remaining and total burn times are synchronized to the GUI.
- Active fuel identity, burn time, inventory, experience, and recipe progress are saved in the world.
- Breaking a furnace drops the block and preserves its custom name, but does **not** preserve its inventory, stored experience, active fuel, or burn state in the dropped item.

See [Automation, GUI, and JEI](automation-and-jei.md) for slots and hopper directions.
