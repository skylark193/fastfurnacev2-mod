# Extremely Fast Alloyer

[Documentation home](README.md) · [Blocks](blocks.md) · [Recipes and fuels](recipes.md) · [Automation, GUI, and JEI](automation-and-jei.md)

The **Extremely Fast Alloyer** combines three dedicated alloying ingredients into one result using Extreme Coal fuel. It is a separate machine family: ordinary furnace, crusher, Accelerator, and crafting recipes are not Alloyer recipes.

## Crafting and appearance

Surround one **Accelerator** with eight **Blocks of Extreme Coal**:

```text
E  E  E
E  A  E
E  E  E
```

`E` = Block of Extreme Coal; `A` = Accelerator

Output: **1 × Extremely Fast Alloyer**

The Alloyer has the Accelerator's directional shape and eleven-stage top lightning bolt, but every visible body pixel is animated lava rather than smooth stone with lava veins. The bolt fills from bottom to top as work advances and returns to stage zero after output. Its inherited `lit` state tracks whether work advanced on the current tick.

It requires a pickaxe, drops itself, preserves its custom name, supports directional placement and comparators, and appears in Functional Blocks and the Fast Furnace V2 creative tab. Breaking it does not preserve inventory, fuel, or processing progress.

## Built-in alloying recipe

Place one of each ingredient into the three input slots, in any order:

| Ingredient 1 | Ingredient 2 | Ingredient 3 | Output | Base processing time |
|---|---|---|---|---:|
| 1 × Fast Iron Ingot | 1 × Fast Gold Ingot | 1 × Fast Diamond | 1 × Extremely Fast Ingot | 800 ticks (40 seconds) |

All three input slots must contain their matched ingredient. Duplicate copies of one ingredient cannot substitute for another ingredient, and a partial recipe does not process. Other mods can add recipes through the [public API](api.md#extremely-fast-alloyer-recipes); custom recipes use the same fuel, lava, automation, persistence, and JEI behavior.

Each horizontally adjacent source or flowing lava block reduces the operation by **100 ticks**, to a minimum of **400 ticks**:

| Horizontal lava sides | Processing time |
|---:|---:|
| 0 | 800 ticks (40 seconds) |
| 1 | 700 ticks (35 seconds) |
| 2 | 600 ticks (30 seconds) |
| 3 | 500 ticks (25 seconds) |
| 4 | 400 ticks (20 seconds) |

Changing the lava count preserves elapsed work and recalculates the current operation's total duration. Lava above or below does not count.

The **Extremely Fast Ingot** preserves an ingot silhouette while replacing every visible pixel with animated lava. It appears in Ingredients and the Fast Furnace V2 creative tab and crafts the four Extremely Fast tools.

## Extreme Coal fuel and processing rules

- The fuel slot accepts **only the Extreme Coal item**. Fast Coal, ordinary coal, and both special-coal storage blocks do not work.
- One Extreme Coal supplies **1,600 ticks of valid processing**. Fuel is consumed only when a complete valid recipe has output space and no burn time remains.
- Valid work advances exactly one tick per server tick and consumes one tick of banked fuel.
- Missing fuel pauses without losing progress. Refueling resumes the same operation.
- Empty, incomplete, duplicated, or unsupported inputs reset progress but retain banked burn time.
- Changing an input resets progress so a different recipe cannot inherit partial work.
- A result slot that cannot accept the complete output pauses processing and the fuel countdown without losing progress or consuming ingredients.
- Ingredients are consumed only when the result completes and fits. One item is removed from each matched input slot.
- With stacked inputs, fuel, and available output space, the next valid operation begins automatically.
- Input, fuel, and progress are saved while the block remains placed. Item transactions preserve progress when aborted; committed empty/fill input transitions reset it.

The Alloyer does not inherit the Accelerator's conversion recipes, boost fuels, or work-unit system. Its animated lava appearance is separate from the optional horizontal-lava speed reduction.

## Slots and automation

| Slot | Purpose |
|---|---|
| 0–2 | Dedicated Alloyer ingredients, in any arrangement |
| 3 | Extreme Coal fuel only |
| 4 | Completed result; insertion is rejected |

Hoppers and other automation can insert accepted ingredients through the **top**, insert only Extreme Coal through **horizontal sides**, and extract the result through the **bottom**. Unsupported furnace, crusher, and Accelerator inputs cannot be inserted as Alloyer ingredients. Shift-clicking accepted ingredients and Extreme Coal routes them to their corresponding slots; shift-clicking the result returns it to the player inventory.

## GUI and JEI

The furnace-style GUI contains three input slots, one Extreme Coal fuel slot and flame, one output slot, and a progress arrow. It has no ordinary smelting recipe book. Its information fly-out reports the 40-second base time, current lava-adjusted time, horizontal lava count, Extreme Coal-only fuel rule, and dedicated-recipe restriction.

With a compatible JEI release, the **Extremely Fast Alloying** category displays all three inputs, the output, the base processing time, and the horizontal-lava reduction rule. The Alloyer is the category catalyst, its progress arrow opens alloying recipes, and recipe transfer targets the three input slots. JEI also reserves space around the visible information fly-out and provides descriptions for the Alloyer and Extremely Fast tools.

The current JEI compatibility boundary is described in [Automation, GUI, and JEI](automation-and-jei.md#current-compatibility-boundary); compilation does not constitute JEI runtime validation.
