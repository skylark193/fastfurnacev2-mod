# Lava-Infused Crushers

[Documentation home](README.md) · [Dynamic crushed ores](world-generation.md#dynamic-crushed-ores) · [Recipes](recipes.md)

Lava-Infused Crushers are fuel-powered machines that turn raw ore items into matching crushed items. They use the same tier speeds and special-fuel duration bonuses as Lava-Infused Furnaces, but their random tier roll doubles **output**, never fuel duration.

| Tier | Block ID | Normal speed | Output-doubling chance |
|---|---|---:|---:|
| Lava-Infused | `fastfurnacev2:fast_crusher` | 150% | 10% |
| Iron | `fastfurnacev2:iron_fast_crusher` | 200% | 15% |
| Gold | `fastfurnacev2:gold_fast_crusher` | 250% | 20% |
| Diamond | `fastfurnacev2:diamond_fast_crusher` | 300% | 25% |

Craft the base Lava-Infused Crusher with three Lava-Infused Diamonds across the top row, a Lava-Infused Furnace in the center, and Lava-Infused Iron Ingots in the remaining five slots. Iron, Gold, and Diamond upgrades each surround the previous crusher with eight matching Blocks of Lava-Infused Iron, Lava-Infused Gold, or Lava-Infused Diamond. See [Recipes](recipes.md#lava-infused-crusher).

All crushers require a pickaxe, drop themselves, preserve custom names, support comparators and directional placement, and emit light level 13 while lit. Their sides and bottom use animated Netherrack textures. The top blender is static while idle and animated while lit; upgrade tiers add their metal block's texture to the stationary housing.

## Built-in crushing

Each discovered raw ore consumes **one input item** and produces **two matching crushed items**. The result can be a generated Fast Furnace V2 item or an external crushed item or dust selected during discovery.

Without external replacements, the vanilla built-ins are:

| Input | Output |
|---|---|
| `minecraft:raw_iron` | 2 × `fastfurnacev2:crushed_iron` |
| `minecraft:raw_copper` | 2 × `fastfurnacev2:crushed_copper` |
| `minecraft:raw_gold` | 2 × `fastfurnacev2:crushed_gold` |
| `fastfurnacev2:raw_fast_iron` | 2 × `fastfurnacev2:crushed_fast_iron` |
| `fastfurnacev2:raw_fast_gold` | 2 × `fastfurnacev2:crushed_fast_gold` |
| `fastfurnacev2:raw_fast_diamond` | 2 × `fastfurnacev2:crushed_fast_diamond` |

Crushing does not use the raw ore's ordinary smelting result. Ordinary furnace inputs such as cobblestone are not accepted unless another mod registers a crusher recipe for them. Built-in crushing does not award ordinary smelting experience.

Fast Furnace V2 also registers these ore-block recipes:

| Input | Normal output | Successful output-doubling roll |
|---|---|---|
| Regular or Deepslate Lava-Infused Coal Ore | 3 × Lava-Infused Coal (`extreme_coal`) | 6 × Lava-Infused Coal (`extreme_coal`) |
| Regular or Deepslate Lava-Infused Iron Ore | 3 × Crushed Lava-Infused Iron | 6 × Crushed Lava-Infused Iron |
| Regular or Deepslate Lava-Infused Gold Ore | 3 × Crushed Lava-Infused Gold | 6 × Crushed Lava-Infused Gold |
| Regular or Deepslate Lava-Infused Diamond Ore | 3 × Crushed Lava-Infused Diamond | 6 × Crushed Lava-Infused Diamond |

The selected external crushed item or dust is used when discovery supplies one. These recipes inherit ordinary-smelting-time fallback and tier/fuel timing like other built-in crusher definitions.

Generated crushed items receive ordinary smelting recipes when needed, so crushing remains useful as a two-step ore-processing chain. See [Dynamic crushed ores](world-generation.md#dynamic-crushed-ores).

## Processing time

A built-in raw-ore operation inherits the raw item's ordinary smelting time. If no ordinary smelting recipe exists, it uses **200 ticks**. Tier and active-fuel speed scaling then use the same formula as furnaces:

```text
processing ticks = max(1, floor(base processing ticks × 100 / effective speed percent))
```

For a standard 200-tick operation:

| Tier | Effective speed: ordinary / `fast_coal` / `extreme_coal` | Processing time: ordinary / `fast_coal` / `extreme_coal` |
|---|---:|---:|
| Lava-Infused | 150% / 200% / 250% | 133 / 100 / 80 ticks |
| Iron | 200% / 250% / 300% | 100 / 80 / 66 ticks |
| Gold | 250% / 300% / 350% | 80 / 66 / 57 ticks |
| Diamond | 300% / 350% / 400% | 66 / 57 / 50 ticks |

Custom API recipes can specify their own base processing time. A custom time of zero uses the same ordinary-smelting-time inheritance and 200-tick fallback.

## Fuel behavior

The `fast_coal` item and block add 50 percentage points to active processing speed; the `extreme_coal` item and block add 100. Their adjusted durations are identical to the **before doubling** values used by furnaces:

| Tier | `fast_coal` | `fast_coal_block` | `extreme_coal` | `extreme_coal_block` |
|---|---:|---:|---:|---:|
| Lava-Infused | 2,400 | 24,000 | 3,200 | 32,000 |
| Iron | 2,800 | 28,000 | 4,000 | 40,000 |
| Gold | 3,200 | 32,000 | 4,800 | 48,000 |
| Diamond | 3,600 | 36,000 | 5,600 | 56,000 |

All durations are in ticks. **No crusher tier rolls for doubled fuel duration.** Ordinary and custom fuels are otherwise supported through the same fuel system as furnaces.

## Output doubling

Once per completed crushing operation, the crusher rolls its tier's output-doubling chance:

- A normal built-in raw-item result is two crushed items and doubles to four.
- A normal built-in ore-block result is three items and doubles to six.
- A custom recipe's complete output stack is doubled, whatever its output count is.

Input is consumed only when the selected result can be inserted. A failed roll still produces the normal result.

## Blocked and pending results

If a successful doubled result cannot fit, the crusher enters a pending state:

1. Progress remains at the final tick.
2. The input is not consumed.
3. The complete doubled result remains required; the crusher does not fall back to normal output.
4. Fuel countdown and processing pause until that exact result fits.
5. Making output space resumes the same result without another random roll.

The pending state is saved in the world. Clearing or changing the input clears it; a committed input extraction and refill permits a fresh operation and output roll. NeoForge item transactions preserve pending output if a transfer is aborted.

For ordinary blocked results, processing progress resets unless a doubled result is pending. Empty or unprocessable input and all blocked output states pause fuel countdown without consuming additional fuel. If fuel expires during processable work, progress remains saved and resumes after refueling.

## Custom crusher recipes

The [modding API](api.md#lava-infused-crusher-recipes) supports:

- Item, tag, or other ingredient matching.
- Input stacks requiring more than one item.
- Arbitrary output counts that leave room for doubling.
- Explicit processing times or ordinary-smelting-time inheritance.
- Priorities for overlapping definitions.

Custom definitions participate in actual consumption, insertion, output doubling, automation, persistence, GUI synchronization, and JEI. They do not automatically create ordinary smelting recipes or new crushed-item types.

Breaking a crusher preserves only its custom name on the dropped block item, not its inventory, active fuel, processing progress, or pending result.
