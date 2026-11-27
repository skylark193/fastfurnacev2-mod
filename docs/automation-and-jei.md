# Automation, GUI, JEI, and Jade

[Documentation home](README.md) · [Lava-Infused Furnaces](furnaces.md) · [Lava-Infused Crushers](crushers.md) · [Accelerator](accelerator.md) · [Lava-Infused Alloyer](alloyer.md) · [Energy blocks](energy.md)

## Machine slots

Lava-Infused Furnaces and Lava-Infused Crushers use the familiar three-slot furnace layout:

| Slot | Purpose |
|---|---|
| Input | Smelting input for furnaces; registered crushing input for crushers |
| Fuel | Ordinary, built-in special, or API-registered fuel |
| Result | Completed output; players and automation cannot insert into it |

Lava-Infused Furnaces provide the vanilla smelting recipe book. Crushers share the same menu foundation, but an ordinary smelting recipe is not a valid crusher recipe unless a matching crusher definition is registered.

The Accelerator also has input, fuel/boost, and result slots, but no ordinary smelting recipe book. Its input accepts only registered Accelerator ingredients, and its fuel slot accepts only registered Accelerator boosts.

Shift-clicking an Accelerator-compatible item from the player inventory tries the conversion input first, then the boost slot. Unsupported items move between the main inventory and hotbar instead.

The Lava-Infused Alloyer has **three ingredient slots, one Lava-Infused Coal (`extreme_coal`) fuel slot, and one result slot**, with no ordinary smelting recipe book. Its built-in ingredients are shapeless across the three input slots. Shift-clicking accepted ingredients and `extreme_coal` routes them into their corresponding slots; the result returns to the player inventory. No other fuel works. Unsupported furnace, crusher, Accelerator, and crafting inputs are not thereby valid Alloyer recipes.

Lava-Infused Generators have one fuel slot and no result slot. Lava-Infused Batteries and Lava-Infused Power Conduits have no item slots. Generator GUIs show active FE/t output below the fuel slot, burn progress above it, and stored FE in the energy bar; hovering the bar shows capacity. Battery GUIs show exact stored FE and capacity directly. Right-clicking a conduit opens its network-mode and per-connection configuration GUI.

## Hopper and automation directions

| Machine | Top | Horizontal sides | Bottom |
|---|---|---|---|
| Lava-Infused Furnace | Insert smelting input | Insert fuel | Extract smelting result |
| Lava-Infused Crusher | Insert registered crusher input | Insert fuel | Extract crushed result |
| Accelerator | Insert registered conversion input | Insert accepted boost fuel | Extract conversion result only |
| Lava-Infused Alloyer | Insert accepted Alloyer ingredients | Insert `extreme_coal` only | Extract alloying result only |
| Lava-Infused Generator | Insert or extract fuel | Insert or extract fuel | Insert or extract fuel |
| Lava-Infused Battery | No item slots | No item slots | No item slots |
| Lava-Infused Power Conduit | No item slots | No item slots | No item slots |

Accelerator insertion also checks ingredient acceptance. A partial input stack can accumulate until it reaches the required count; processing lookup then enforces the full input count. Alloyer top insertion accepts only items used by its dedicated recipes, and horizontal-side insertion accepts only the `extreme_coal` item. A complete operation still requires three distinct matched input slots.

Generators, batteries, and power conduits expose NeoForge's standard block energy capability on every side. Generator and battery side modes control insertion and extraction; generators automatically push only through output-enabled sides. Batteries have tier-scaled transfer limits, and their items expose the standard item energy capability. Conduits form connected networks with one shared per-tick budget determined by their slowest tier, configurable transfer ordering, and per-connection direction modes. See [Generators, batteries, and conduits](energy.md) for exact values and network rules.

Fast Furnace V2 supports NeoForge item transactions around machine input changes. Aborted transfers preserve machine progress and pending crusher results; committed empty/fill transitions reset the relevant operation so a new input cannot inherit stale progress. The Alloyer uses the same progress-preserving transaction rule.

## Information panels

Lava-Infused Furnace, Lava-Infused Crusher, Accelerator, Lava-Infused Alloyer, and Lava-Infused Generator GUIs have a small **`i`** button near the upper-right corner. Click it to toggle an information fly-out. The panel prefers the right side of the GUI, then the left, then an on-screen fallback position at narrow scaled widths.

### Furnace panel

- Current effective processing speed, including custom fuel bonuses.
- Tier fuel-doubling chance.
- Exact remaining active-fuel time.
- Total burn time supplied by the current consumed fuel item.

### Crusher panel

- Current effective processing speed.
- Tier output-doubling chance instead of fuel doubling.
- Exact remaining and total active-fuel times.

Furnace and crusher timers are calculated from exact tick counts and displayed in seconds with one decimal place.

### Lava-Infused Generator panel

- Current status: idle, generating, or paused with a full buffer.
- Active FE/t output, including bonuses from the consumed fuel.
- Exact remaining active-fuel time and total supplied burn time in seconds.
- The active special-fuel lava bonus, locked in when that fuel item was consumed.

Stored FE is intentionally omitted because the energy bar already displays it; hover the bar for maximum capacity.

### Energy side configuration

Generator and battery GUIs have an **`E`** button directly below **`i`**. It opens a fly-out with six buttons for Down, Up, North, South, West, and East. Click a direction to cycle through **Input**, **Output**, and **Both**. These directions are from the configured block's perspective: Input accepts FE from its neighbor, while Output supplies FE to it. Generators default to Output and batteries default to Both. Changes apply immediately and persist with the block.

Right-click any power conduit to open a dedicated configuration GUI. Its upper button cycles the whole network through Fill first found, Round-robin, and Random. The same six direction buttons configure that conduit's adjacent energy-handler connections. See [energy blocks](energy.md#lava-infused-power-conduits) for transfer semantics.

### Accelerator panel

- Horizontal lava-side count, highlighted according to whether all four are present.
- Current processing speed.
- Remaining boosted work.
- Built-in item/block conversion times and built-in boost capacities.

### Lava-Infused Alloyer panel

- The `extreme_coal`-only fuel rule.
- The 40-second base time and current lava-adjusted processing time.
- Horizontal lava-side count.
- A reminder that only dedicated Alloyer recipes are accepted.

The Alloyer GUI has a Lava-Infused Coal (`extreme_coal`) fuel slot, burn flame, and progress arrow, but no boost display or smelting recipe book.

## Comparators, naming, and drops

All machines support comparator output based on their container contents. They can receive custom names through normal Minecraft naming mechanics, and their loot preserves that custom name.

Breaking most machines does **not** create a portable inventory or energy-storage item. Batteries are the explicit energy-storage exception:

| Machine | Preserved in dropped item | Not preserved |
|---|---|---|
| Lava-Infused Furnace | Custom name | Inventory, smelting experience, progress, active fuel, burn state |
| Lava-Infused Crusher | Custom name | Inventory, progress, active fuel, pending doubled output |
| Accelerator | Custom name | Inventory, conversion progress, banked boost |
| Lava-Infused Alloyer | Custom name | Three input stacks, fuel and result stacks, burn time, alloying progress |
| Lava-Infused Generator | Custom name | Fuel stack, stored FE, active fuel, burn state |
| Lava-Infused Battery | Custom name and exact stored FE | — |

Inventory, processing state, stored energy, and active fuel or boost are saved while the block remains placed in the world. Conduits also save their buffers in the world, but dropped conduit items preserve no FE. Lava-Infused storage blocks, Lava-Infused Cobblestone, and Lava-Infused ores use their own ordinary block loot rather than machine state rules.

## Optional JEI integration

With a JEI release compatible with both Minecraft 26.1.2 and this integration, Fast Furnace V2 supplies the following categories:

| Category | Contents | Catalysts |
|---|---|---|
| Vanilla smelting | Ordinary and generated crushed-ore smelting recipes | All four Lava-Infused Furnace tiers |
| Tier-specific Lava-Infused Furnace fuels | Actual adjusted burn times and speed-adjusted smelt counts | The matching furnace and crusher tier |
| Tier-specific Lava-Infused Generator fuels | Actual FE/t output and total FE per item; special fuels have separate entries for all five lava-boost levels | The matching generator tier |
| Ore Crushing | Built-in and API-registered crusher recipes, counts, timing, and output-doubling note | All four Lava-Infused Crusher tiers |
| Accelerator Conversions | Built-in and custom conversions, normal and registered boost-speed durations, and lava requirement | Accelerator |
| Accelerator Boosts | Registered boost ingredients, speed, exact work, and fully boosted conversion capacities | Accelerator |
| Lava-Infused Alloying | Three dedicated shapeless inputs, one output, base processing time, and lava-reduction rule | Lava-Infused Alloyer |

### GUI click areas

- A Lava-Infused Furnace's **progress arrow** opens smelting recipes.
- A Lava-Infused Crusher's **progress arrow** opens crushing recipes, not smelting.
- A furnace or crusher **flame** opens its current tier's fuel category.
- The Accelerator's **progress arrow** opens conversions.
- The Accelerator's **flame** opens boost fuels.
- The Lava-Infused Alloyer's **progress arrow** opens dedicated alloying recipes, not smelting or Accelerator conversions.
- A Lava-Infused Generator's **flame** opens its current tier's generator-fuel category, including FE/t and total FE per fuel item at every applicable lava-boost level.

JEI reserves space around visible information and energy-configuration fly-outs so they are not covered by recipe-viewer panels.

### Recipe transfer

Recipe transfer supports:

- Smelting input for Lava-Infused Furnaces.
- Crushing input for Lava-Infused Crushers.
- Fuel insertion for both machine families.
- Accelerator conversion input and boost insertion.
- All three Lava-Infused Alloyer ingredient slots.

Smelting transfers and smelting click areas are deliberately disabled for crushers.

### Fuel smelt counts

JEI calculates smelt count from adjusted burn time divided by that tier's actual 200-tick processing time. Values below are approximate and **before any random furnace fuel-doubling roll**:

| Fuel | Lava-Infused | Iron | Gold | Diamond |
|---|---:|---:|---:|---:|
| Vanilla coal | 12.03 | 16 | 20 | 24.24 |
| Lava-Infused Coal (`fast_coal`) | 24 | 35 | 48.48 | 63.16 |
| Block of Lava-Infused Coal (`fast_coal_block`) | 240 | 350 | 484.85 | 631.58 |
| Lava-Infused Coal (`extreme_coal`) | 40 | 60.61 | 84.21 | 112 |
| Block of Lava-Infused Coal (`extreme_coal_block`) | 400 | 606.06 | 842.11 | 1,120 |

Custom fuels use their registered duration and speed bonuses in the same calculation.

### Ingredient information

JEI includes concise information for Lava-Infused Cobblestone, both special coal items (`fast_coal` and `extreme_coal`) and their storage blocks, their regular and deepslate ore variants, the Accelerator, the Lava-Infused Alloyer, every furnace, crusher, generator, battery, and power-conduit tier, and all enhanced tool sets. Descriptions focus on special drop chances, machine and tool bonuses, fuel rules, lava requirements, energy output and storage, and conduit throughput; ordinary drops and crafting/processing recipes remain in their normal JEI views. Ore Crushing displays include the built-in ore-block recipes and their three-item normal results. Looking up a Lava-Infused material or storage block also exposes its Accelerator conversion as a crafting source.

### Current compatibility boundary

The compile-time integration targets **JEI 29.2.0.21**, which declares support for Minecraft **26.1**, not **26.1.2**. That JEI release cannot launch in this mod's normal Minecraft 26.1.2 environment. A newer release compatible with both Minecraft and the integration API is required for runtime use; JEI remains optional.

## Optional Jade integration

With Jade installed, Fast Furnace V2 adds a **Fast Furnace V2 machine status** provider for its machines and conduits. The information is obtained from the server, so energy, fuel, and progress values reflect the placed block rather than client estimates.

| Block | Jade information |
|---|---|
| Lava-Infused Furnace or Crusher | Remaining and total fuel ticks, operation progress, effective speed, and pending doubled crusher output |
| Accelerator | Conversion progress, remaining and total boost work, processing speed, and horizontal lava count |
| Lava-Infused Alloyer | Remaining and total fuel ticks, alloying progress, and horizontal lava count |
| Lava-Infused Generator | Stored FE and capacity, active FE/t generation, fuel time, active burn-duration percentage, per-output-side transfer limit, and horizontal lava count |
| Lava-Infused Battery | Stored FE and capacity and its per-enabled-side transfer limit |
| Lava-Infused Power Conduit | Local buffered FE, shared network transfer budget, and network ordering mode |

The provider can be toggled through Jade's normal plugin configuration. Jade is optional, and the mod does not require it at runtime.

## Extending these behaviors

Other mods do not need separate JEI integrations for content registered through Fast Furnace V2's API. Custom fuels, crusher recipes, Accelerator conversions, boosts, and Alloyer recipes automatically use the real machine logic and corresponding displays. See the [modding API guide](api.md).
