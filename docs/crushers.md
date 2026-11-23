# Fast Crushers

[Documentation home](README.md) · [Dynamic crushed ores](world-generation.md#dynamic-crushed-ores) · [Recipes](recipes.md)

Fast Crushers are fuel-powered machines that turn raw ore items into matching crushed items. They use the same tier speeds and special-fuel duration bonuses as Fast Furnaces, but their random tier roll doubles **output**, never fuel duration.

| Tier | Block ID | Normal speed | Output-doubling chance |
|---|---|---:|---:|
| Fast | `fastfurnacev2:fast_crusher` | 150% | 10% |
| Iron | `fastfurnacev2:iron_fast_crusher` | 200% | 15% |
| Gold | `fastfurnacev2:gold_fast_crusher` | 250% | 20% |
| Diamond | `fastfurnacev2:diamond_fast_crusher` | 300% | 25% |

Craft the base Fast Crusher with three Fast Diamonds across the top row, a Fast Furnace in the center, and Fast Iron Ingots in the remaining five slots. Iron, Gold, and Diamond upgrades each surround the previous crusher with eight matching Blocks of Fast Iron, Fast Gold, or Fast Diamond. See [Recipes](recipes.md#fast-crusher).

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

Generated crushed items receive ordinary smelting recipes when needed, so crushing remains useful as a two-step ore-processing chain. See [Dynamic crushed ores](world-generation.md#dynamic-crushed-ores).

## Processing time

A built-in raw-ore operation inherits the raw item's ordinary smelting time. If no ordinary smelting recipe exists, it uses **200 ticks**. Tier and active-fuel speed scaling then use the same formula as furnaces:

```text
processing ticks = max(1, floor(base processing ticks × 100 / effective speed percent))
```

For a standard 200-tick operation:

| Tier | Effective speed: ordinary / Fast Coal / Extreme Coal | Processing time: ordinary / Fast Coal / Extreme Coal |
|---|---:|---:|
| Fast | 150% / 200% / 250% | 133 / 100 / 80 ticks |
| Iron | 200% / 250% / 300% | 100 / 80 / 66 ticks |
| Gold | 250% / 300% / 350% | 80 / 66 / 57 ticks |
| Diamond | 300% / 350% / 400% | 66 / 57 / 50 ticks |

Custom API recipes can specify their own base processing time. A custom time of zero uses the same ordinary-smelting-time inheritance and 200-tick fallback.

## Fuel behavior

Fast Coal and Block of Fast Coal add 50 percentage points to active processing speed; Extreme Coal adds 100. Their adjusted durations are identical to the **before doubling** values used by furnaces:

| Tier | Fast Coal | Block of Fast Coal | Extreme Coal |
|---|---:|---:|---:|
| Fast | 2,400 | 24,000 | 3,200 |
| Iron | 2,800 | 28,000 | 4,000 |
| Gold | 3,200 | 32,000 | 4,800 |
| Diamond | 3,600 | 36,000 | 5,600 |

All durations are in ticks. **No crusher tier rolls for doubled fuel duration.** Ordinary and custom fuels are otherwise supported through the same fuel system as furnaces.

## Output doubling

Once per completed crushing operation, the crusher rolls its tier's output-doubling chance:

- A normal built-in result is two crushed items.
- A successful built-in roll produces four crushed items.
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

The [modding API](api.md#fast-crusher-recipes) supports:

- Item, tag, or other ingredient matching.
- Input stacks requiring more than one item.
- Arbitrary output counts that leave room for doubling.
- Explicit processing times or ordinary-smelting-time inheritance.
- Priorities for overlapping definitions.

Custom definitions participate in actual consumption, insertion, output doubling, automation, persistence, GUI synchronization, and JEI. They do not automatically create ordinary smelting recipes or new crushed-item types.

Breaking a crusher preserves only its custom name on the dropped block item, not its inventory, active fuel, processing progress, or pending result.
