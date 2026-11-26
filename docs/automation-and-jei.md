# Automation, GUI, and JEI

[Documentation home](README.md) · [Fast Furnaces](furnaces.md) · [Fast Crushers](crushers.md) · [Accelerator](accelerator.md) · [Extremely Fast Alloyer](alloyer.md) · [Energy blocks](energy.md)

## Machine slots

Fast Furnaces and Fast Crushers use the familiar three-slot furnace layout:

| Slot | Purpose |
|---|---|
| Input | Smelting input for furnaces; registered crushing input for crushers |
| Fuel | Ordinary, built-in special, or API-registered fuel |
| Result | Completed output; players and automation cannot insert into it |

Fast Furnaces provide the vanilla smelting recipe book. Crushers share the same menu foundation, but an ordinary smelting recipe is not a valid crusher recipe unless a matching crusher definition is registered.

The Accelerator also has input, fuel/boost, and result slots, but no ordinary smelting recipe book. Its input accepts only registered Accelerator ingredients, and its fuel slot accepts only registered Accelerator boosts.

Shift-clicking an Accelerator-compatible item from the player inventory tries the conversion input first, then the boost slot. Unsupported items move between the main inventory and hotbar instead.

The Extremely Fast Alloyer has **three ingredient slots, one Extreme Coal fuel slot, and one result slot**, with no ordinary smelting recipe book. Its built-in ingredients are shapeless across the three input slots. Shift-clicking accepted ingredients and Extreme Coal routes them into their corresponding slots; the result returns to the player inventory. No other fuel works. Unsupported furnace, crusher, Accelerator, and crafting inputs are not thereby valid Alloyer recipes.

Fast Generators have one fuel slot and no result slot. Fast Batteries and Fast Power Conduits have no item slots. Generator GUIs show active FE/t output below the fuel slot, burn progress above it, and stored FE in the energy bar; hovering the bar shows capacity. Battery GUIs show exact stored FE and capacity directly. Conduits have no GUI.

## Hopper and automation directions

| Machine | Top | Horizontal sides | Bottom |
|---|---|---|---|
| Fast Furnace | Insert smelting input | Insert fuel | Extract smelting result |
| Fast Crusher | Insert registered crusher input | Insert fuel | Extract crushed result |
| Accelerator | Insert registered conversion input | Insert accepted boost fuel | Extract conversion result only |
| Extremely Fast Alloyer | Insert accepted Alloyer ingredients | Insert Extreme Coal only | Extract alloying result only |
| Fast Generator | Insert or extract fuel | Insert or extract fuel | Insert or extract fuel |
| Fast Battery | No item slots | No item slots | No item slots |
| Fast Power Conduit | No item slots | No item slots | No item slots |

Accelerator insertion also checks ingredient acceptance. A partial input stack can accumulate until it reaches the required count; processing lookup then enforces the full input count. Alloyer top insertion accepts only items used by its dedicated recipes, and horizontal-side insertion accepts only the Extreme Coal item. A complete operation still requires three distinct matched input slots.

Generators, batteries, and power conduits expose NeoForge's standard block energy capability on every side. Generators automatically push FE to adjacent consumers and reject external FE insertion. Batteries accept and supply FE with tier-scaled transfer limits, and their items expose the standard item energy capability. Conduits form connected networks with one shared per-tick budget determined by their slowest tier. See [Generators, batteries, and conduits](energy.md) for exact values and network rules.

Fast Furnace V2 supports NeoForge item transactions around machine input changes. Aborted transfers preserve machine progress and pending crusher results; committed empty/fill transitions reset the relevant operation so a new input cannot inherit stale progress. The Alloyer uses the same progress-preserving transaction rule.

## Information panels

Fast Furnace, Fast Crusher, Accelerator, Extremely Fast Alloyer, and Fast Generator GUIs have a small **`i`** button near the upper-right corner. Click it to toggle an information fly-out. The panel prefers the right side of the GUI, then the left, then an on-screen fallback position at narrow scaled widths.

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

### Fast Generator panel

- Current status: idle, generating, or paused with a full buffer.
- Active FE/t output, including bonuses from the consumed fuel.
- Exact remaining active-fuel time and total supplied burn time in seconds.

Stored FE is intentionally omitted because the energy bar already displays it; hover the bar for maximum capacity.

### Accelerator panel

- Horizontal lava-side count, highlighted according to whether all four are present.
- Current processing speed.
- Remaining boosted work.
- Built-in item/block conversion times and built-in boost capacities.

### Extremely Fast Alloyer panel

- The Extreme Coal-only fuel rule.
- The 40-second base time and current lava-adjusted processing time.
- Horizontal lava-side count.
- A reminder that only dedicated Alloyer recipes are accepted.

The Alloyer GUI has an Extreme Coal fuel slot, burn flame, and progress arrow, but no boost display or smelting recipe book.

## Comparators, naming, and drops

All machines support comparator output based on their container contents. They can receive custom names through normal Minecraft naming mechanics, and their loot preserves that custom name.

Breaking most machines does **not** create a portable inventory or energy-storage item. Batteries are the explicit energy-storage exception:

| Machine | Preserved in dropped item | Not preserved |
|---|---|---|
| Fast Furnace | Custom name | Inventory, smelting experience, progress, active fuel, burn state |
| Fast Crusher | Custom name | Inventory, progress, active fuel, pending doubled output |
| Accelerator | Custom name | Inventory, conversion progress, banked boost |
| Extremely Fast Alloyer | Custom name | Three input stacks, fuel and result stacks, burn time, alloying progress |
| Fast Generator | Custom name | Fuel stack, stored FE, active fuel, burn state |
| Fast Battery | Custom name and exact stored FE | — |

Inventory, processing state, stored energy, and active fuel or boost are saved while the block remains placed in the world. Conduits also save their buffers in the world, but dropped conduit items preserve no FE. Fast storage blocks, Fast Cobblestone, and Fast ores use their own ordinary block loot rather than machine state rules.

## Optional JEI integration

With a JEI release compatible with both Minecraft 26.1.2 and this integration, Fast Furnace V2 supplies the following categories:

| Category | Contents | Catalysts |
|---|---|---|
| Vanilla smelting | Ordinary and generated crushed-ore smelting recipes | All four Fast Furnace tiers |
| Tier-specific Fast Furnace fuels | Actual adjusted burn times and speed-adjusted smelt counts | The matching furnace and crusher tier |
| Ore Crushing | Built-in and API-registered crusher recipes, counts, timing, and output-doubling note | All four Fast Crusher tiers |
| Accelerator Conversions | Built-in and custom conversions, normal and registered boost-speed durations, and lava requirement | Accelerator |
| Accelerator Boosts | Registered boost ingredients, speed, exact work, and fully boosted conversion capacities | Accelerator |
| Extremely Fast Alloying | Three dedicated shapeless inputs, one output, base processing time, and lava-reduction rule | Extremely Fast Alloyer |

### GUI click areas

- A Fast Furnace's **progress arrow** opens smelting recipes.
- A Fast Crusher's **progress arrow** opens crushing recipes, not smelting.
- A furnace or crusher **flame** opens its current tier's fuel category.
- The Accelerator's **progress arrow** opens conversions.
- The Accelerator's **flame** opens boost fuels.
- The Extremely Fast Alloyer's **progress arrow** opens dedicated alloying recipes, not smelting or Accelerator conversions.

JEI reserves space around visible information fly-outs so they are not covered by recipe-viewer panels.

### Recipe transfer

Recipe transfer supports:

- Smelting input for Fast Furnaces.
- Crushing input for Fast Crushers.
- Fuel insertion for both machine families.
- Accelerator conversion input and boost insertion.
- All three Extremely Fast Alloyer ingredient slots.

Smelting transfers and smelting click areas are deliberately disabled for crushers.

### Fuel smelt counts

JEI calculates smelt count from adjusted burn time divided by that tier's actual 200-tick processing time. Values below are approximate and **before any random furnace fuel-doubling roll**:

| Fuel | Fast | Iron | Gold | Diamond |
|---|---:|---:|---:|---:|
| Vanilla coal | 12.03 | 16 | 20 | 24.24 |
| Fast Coal | 24 | 35 | 48.48 | 63.16 |
| Block of Fast Coal | 240 | 350 | 484.85 | 631.58 |
| Extreme Coal | 40 | 60.61 | 84.21 | 112 |
| Block of Extreme Coal | 400 | 606.06 | 842.11 | 1,120 |

Custom fuels use their registered duration and speed bonuses in the same calculation.

### Ingredient information

JEI includes concise information for Fast Cobblestone, Fast and Extreme Coal and their storage blocks, regular and deepslate Fast and Extreme Coal ores, the Accelerator, the Extremely Fast Alloyer, every furnace and crusher tier, and all Fast and Extremely Fast tools. Descriptions focus on special drop chances, machine and tool bonuses, fuel rules, and lava requirements; ordinary drops and crafting/processing recipes remain in their normal JEI views. Ore Crushing displays include the built-in ore-block recipes and their three-item normal results. Looking up a Fast material or storage block also exposes its Accelerator conversion as a crafting source.

### Current compatibility boundary

The compile-time integration targets **JEI 29.2.0.21**, which declares support for Minecraft **26.1**, not **26.1.2**. That JEI release cannot launch in this mod's normal Minecraft 26.1.2 environment. A newer release compatible with both Minecraft and the integration API is required for runtime use; JEI remains optional.

## Extending these behaviors

Other mods do not need separate JEI integrations for content registered through Fast Furnace V2's API. Custom fuels, crusher recipes, Accelerator conversions, boosts, and Alloyer recipes automatically use the real machine logic and corresponding displays. See the [modding API guide](api.md).
