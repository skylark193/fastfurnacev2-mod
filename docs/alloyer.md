# Extremely Fast Alloyer

[Documentation home](README.md) · [Blocks](blocks.md) · [Recipes and fuels](recipes.md) · [Automation, GUI, and JEI](automation-and-jei.md)

The **Extremely Fast Alloyer** combines three dedicated alloying ingredients into one result. It is a separate machine family: ordinary furnace, crusher, Accelerator, and crafting recipes are not Alloyer recipes.

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

It requires a pickaxe, drops itself, preserves its custom name, supports directional placement and comparators, and appears in Functional Blocks and the Fast Furnace V2 creative tab. Breaking it does not preserve inventory or processing progress.

## Built-in alloying recipe

Place one of each ingredient into the three input slots, in any order:

| Ingredient 1 | Ingredient 2 | Ingredient 3 | Output | Processing time |
|---|---|---|---|---:|
| 1 × Fast Iron Ingot | 1 × Fast Gold Ingot | 1 × Fast Diamond | 1 × Extremely Fast Ingot | 200 ticks (10 seconds) |

All three input slots must contain their matched ingredient. Duplicate copies of one ingredient cannot substitute for another ingredient, and a partial recipe does not process. The built-in operation runs at **100% ordinary furnace speed**, exactly **200 ticks**, without fuel, a fuel slot, or surrounding lava.

The **Extremely Fast Ingot** preserves an ingot silhouette while replacing every visible pixel with animated lava. It appears in Ingredients and the Fast Furnace V2 creative tab.

## Processing rules

- The three inputs are shapeless across the input slots; each recipe ingredient consumes a distinct nonempty slot.
- Valid work advances exactly one tick per server tick.
- Empty, incomplete, duplicated, or unsupported inputs reset progress.
- Changing an input resets progress so a different recipe cannot inherit partial work.
- A result slot that cannot accept the complete output pauses processing without losing progress or consuming ingredients.
- Ingredients are consumed only when the result completes and fits. One item is removed from each matched input slot.
- With stacked inputs and available output space, the next valid operation begins automatically.
- Input and progress are saved while the block remains placed. Item transactions preserve progress when aborted; committed empty/fill input transitions reset it.

The Alloyer does not inherit the Accelerator's four-side lava requirement, conversion recipes, boost fuels, or work-unit system. Its animated lava appearance is visual only.

## Slots and automation

| Slot | Purpose |
|---|---|
| 0–2 | Dedicated Alloyer ingredients, in any arrangement |
| 3 | Completed result; insertion is rejected |

Hoppers and other automation can insert accepted ingredients through the **top or any horizontal side**. The **bottom** exposes only the result slot for extraction. Unsupported furnace, crusher, and Accelerator inputs cannot be inserted as Alloyer ingredients. Shift-clicking an accepted ingredient from the player inventory routes it into the input slots; shift-clicking the result returns it to the player inventory.

## GUI and JEI

The furnace-style GUI contains three input slots, one output slot, and a progress arrow. It has no flame, fuel slot, boost display, or ordinary smelting recipe book. Its information fly-out reports 100% speed, the ten-second built-in recipe time, and the dedicated-recipe restriction.

With a compatible JEI release, the **Extremely Fast Alloying** category displays all three inputs, the output, and the recipe's processing time. The Alloyer is the category catalyst, its progress arrow opens alloying recipes, and recipe transfer targets the three input slots. JEI also reserves space around the visible information fly-out and provides short descriptions for the Alloyer and Extremely Fast Ingot.

The current JEI compatibility boundary is described in [Automation, GUI, and JEI](automation-and-jei.md#current-compatibility-boundary); compilation does not constitute JEI runtime validation.
