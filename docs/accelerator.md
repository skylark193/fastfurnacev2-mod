# Accelerator

[Documentation home](README.md) · [Fuel items](items.md#special-fuels) · [Automation and GUI](automation-and-jei.md)

The **Accelerator** (`fastfurnacev2:accelerator`) converts coal into Fast Coal and coal blocks into Blocks of Fast Coal. It is separate from ordinary smelting and the vanilla recipe book.

It has no built-in crafting recipe. Obtain it through creative mode, commands, or content supplied by another mod or modpack. It requires a pickaxe, drops itself, preserves a custom name, supports directional placement and comparators, and appears in Functional Blocks and the Fast Furnace V2 creative tab.

## Lava setup

The Accelerator requires lava on **all four horizontal sides**. Viewed from above:

```text
    Lava
Lava  A  Lava
    Lava
```

`A` is the Accelerator. Source and flowing lava both count. Lava above or below is irrelevant, and lava is not consumed. The block can be placed before its lava enclosure is built; lava is required for operation, not placement.

Missing lava pauses processing, boost drain, and fuel consumption without erasing recipe progress or banked boost.

## Built-in conversions

| Input | Output | Unboosted work and time |
|---|---|---:|
| 1 × `minecraft:coal` | 1 × `fastfurnacev2:fast_coal` | 400 work units / 400 ticks |
| 1 × `minecraft:coal_block` | 1 × `fastfurnacev2:fast_coal_block` | 3,600 work units / 3,600 ticks |

One work unit advances per tick without a boost. At 20 ticks per second, these operations take 20 seconds and 180 seconds respectively under ideal tick rates.

The Accelerator does not require fuel to perform these conversions. Other mods can register additional conversions through the [API](api.md#accelerator-conversions-and-boosts).

## Boost fuels

By default, only Fast Coal, Block of Fast Coal, and Extreme Coal are accepted as Accelerator boosts. Ordinary coal and other unregistered furnace fuels are rejected by the boost slot and automation.

| Boost item | Work supplied | Maximum work per tick | Effective speed |
|---|---:|---:|---:|
| Fast Coal | 3,600 | 3 | 300% |
| Block of Fast Coal | 4,000 | 3 | 300% |
| Extreme Coal | 7,200 | 5 | 500% |

When valid work can proceed and no boost remains, one accepted boost item is consumed and its full work capacity is banked. Each active tick advances by the smallest of:

- The boost's maximum work per tick, or one without boost.
- Remaining work in the current conversion.
- Remaining banked boost work.

This clamping prevents wasted partial ticks or boost work.

### Conversion times and capacities

| Boost | Coal conversion | Coal-block conversion | Built-in capacity from one boost item |
|---|---:|---:|---|
| None | 400 ticks | 3,600 ticks | Not applicable; conversions run without boost |
| Fast Coal | 134 ticks | 1,200 ticks | 9 coal conversions, or 1 coal-block conversion |
| Block of Fast Coal | 134 ticks | 1,200 ticks | 10 coal conversions, or 1 coal-block conversion plus 1 coal conversion |
| Extreme Coal | 80 ticks | 720 ticks | 18 coal conversions, or 2 coal-block conversions |

Boost work can be spent across different conversion types. For example, a Block of Fast Coal can fund one 3,600-work coal-block conversion and then one 400-work coal conversion.

## Pause and reset rules

| Condition | Effect |
|---|---|
| Fewer than four horizontal lava sides | Pause progress, boost drain, and new boost consumption |
| Output contains a different item | Pause |
| Output lacks room for the complete result | Pause |
| Empty or unsupported input | Reset conversion progress |
| Input changes to a different item or components | Reset conversion progress for the new input |
| Input count changes without changing item identity, while the required count remains available | Preserve progress |
| Banked boost while paused or after input reset | Preserve boost |
| World save and reload | Preserve inventory, progress, remaining boost, total boost, and active boost identity |

The Accelerator accepts a matching custom ingredient into its input slot even before the required recipe count is present. Processing starts only when enough items are available.

## GUI and visual state

The furnace-style GUI contains input, boost, and output slots but no ordinary smelting recipe book. Its information button shows:

- Current horizontal lava-side count.
- Current processing speed.
- Remaining boosted work.
- Built-in conversion times and boost capacities.

The top face displays an eleven-stage lightning bolt that fills from bottom to top using ceiling progress. The final partial decile displays the full bolt before the completed output resets progress to zero. The body uses animated smooth stone with lava veins. The lit block state means work advanced on that tick, not merely that boost remains banked.

## Automation and breaking

- **Top:** insert conversion input.
- **Horizontal sides:** insert accepted boost fuel.
- **Bottom:** extract results only.
- Shift-clicking from the player inventory routes conversion input first, then boost fuel, with unsupported items remaining in the inventory.

Progress and boost are saved in the placed block entity. Breaking the Accelerator does not store its inventory, conversion progress, or boost in the dropped item; only its custom name is preserved.

Custom conversions and boosts use these same lava, timing, automation, persistence, GUI, and JEI rules. See the [API guide](api.md).
