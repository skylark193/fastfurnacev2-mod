# Generators, batteries, and power conduits

[Documentation home](README.md) · [Blocks](blocks.md) · [Automation and GUI](automation-and-jei.md)

Fast Furnace V2 adds four generator, battery, and power-conduit tiers using NeoForge's standard Forge Energy (FE) capabilities. **None of these blocks has a crafting recipe yet.** They are available in the Fast Furnace V2 and Functional Blocks creative tabs. Their inventory tooltips summarize base generation, storage capacity, and transfer rates as applicable.

## Lava-Infused Generators

Generators accept ordinary furnace fuel in their single slot. They burn each item at its ordinary furnace duration, except for the built-in special fuels below. Furnace tier speed, random fuel doubling, and crusher output rules do not apply.

| Tier | Ordinary fuel | Lava-Infused Coal (`fast_coal`) | Lava-Infused Coal (`extreme_coal`) | Internal buffer |
|---|---:|---:|---:|---:|
| Lava-Infused Generator | 128 FE/t | 192 FE/t | 256 FE/t | 128,000 FE |
| Iron Lava-Infused Generator | 256 FE/t | 384 FE/t | 512 FE/t | 256,000 FE |
| Gold Lava-Infused Generator | 512 FE/t | 768 FE/t | 1,024 FE/t | 512,000 FE |
| Diamond Lava-Infused Generator | 1,024 FE/t | 1,536 FE/t | 2,048 FE/t | 1,024,000 FE |

The `fast_coal` item and its storage block provide **50% more FE/t**, while the `extreme_coal` item and its storage block provide **100% more FE/t**. Both special fuel families normally last **50% of their ordinary burn time**. When a special fuel item is consumed, each horizontal lava side adds **25 percentage points** to that duration:

| Horizontal lava sides | Special-fuel burn time |
|---:|---:|
| 0 | 50% of ordinary |
| 1 | 75% of ordinary |
| 2 | 100% of ordinary |
| 3 | 125% of ordinary |
| 4 | 150% of ordinary |

Ordinary and custom non-special fuels receive no lava duration bonus. The lava count and resulting duration are locked in when each item is consumed; adding or removing lava does not retroactively change fuel already burning. Output FE/t and the active lava bonus likewise come from the consumed stack rather than the current fuel slot.

Generators push stored FE to adjacent consumers through output-enabled sides, up to twice their ordinary tier output per neighbor per tick. They pause burn time and production when the internal buffer cannot accept a complete tick of output. Energy, burn state, active-fuel bonuses, and side configuration are saved with the placed block; breaking it drops only the block item.

The GUI has one centered fuel slot, burn progress above it, active FE/t output below, and stored FE in the energy bar. The **`i`** button opens generation status and timing information. The **`E`** button below it opens per-side FE configuration. Each side cycles through **Input**, **Output**, and **Both**; generators default to Output on every side. Input permits external charging, Output permits extraction and automatic pushing, and Both permits both directions.

With compatible JEI installed, clicking the burn flame opens the current tier's generator-fuel category. Ordinary fuels have one entry. Each built-in special fuel has five entries, one for every lava count from zero through four, showing its FE/t output, burn-time percentage, and **total FE produced per item** at that boosted duration.

## Lava-Infused Batteries

Batteries accept and supply FE through the standard block energy capability on every side.

| Tier | Capacity | Maximum insertion or extraction per operation |
|---|---:|---:|
| Lava-Infused Battery | 256,000 FE | 256 FE |
| Iron Lava-Infused Battery | 512,000 FE | 512 FE |
| Gold Lava-Infused Battery | 1,024,000 FE | 1,024 FE |
| Diamond Lava-Infused Battery | 2,560,000 FE | 2,560 FE |

Breaking a battery preserves its exact stored FE in the dropped item. Placing it restores that charge, clamped to the new battery's capacity if its components were modified to contain more. Battery items also expose NeoForge's standard transactional item energy capability, allowing other mods to charge and discharge them safely.

The paired bars on every vertical face fill from bottom to top in eleven charge steps, including empty and full. The animated lightning bolt remains on top. Charge persists in the world and across breaking and placement. The GUI reports exact stored FE and capacity and has no item slots. Its **`i`** button opens storage information, while the **`E`** button below it configures each side as Input, Output, or Both. Batteries default to Both on every side.

## Lava-Infused Power Conduits

Conduits connect to adjacent conduits and blocks exposing an energy capability. Their connected models have animated internal lava channels; the channel widens from 2 pixels in the Lava-Infused tier to 4, 6, and 8 pixels in the Iron, Gold, and Diamond tiers. Eight-pixel-wide selection and collision shapes cover the core and connected arms so conduits can be targeted and broken reliably.

| Tier | Shared network transfer budget |
|---|---:|
| Lava-Infused Power Conduit | 256 FE/t |
| Iron Lava-Infused Power Conduit | 512 FE/t |
| Gold Lava-Infused Power Conduit | 1,024 FE/t |
| Diamond Lava-Infused Power Conduit | 2,560 FE/t |

Each connected network runs at most once per game tick and moves no more than its shared budget, including energy already stored in conduit buffers. Chains, branches, and loops do not multiply this limit. A mixed-tier network uses the rate of its slowest connected conduit. Actual transfer can be lower when sources, destinations, or their per-operation limits cannot supply or accept the full budget.

**Right-click any conduit** to open its configuration GUI. The network-mode button applies one ordering mode to the entire connected network:

- **Fill first found:** use deterministic position order and fill each available destination before moving to the next.
- **Round-robin:** rotate source, destination, and buffered-conduit order each game tick.
- **Random:** shuffle that ordering for each network pass.

The six side buttons configure that conduit's connections to adjacent energy handlers as **Input**, **Output**, or **Both**, all defaulting to Both. Input allows the network to extract from the neighbor, Output allows it to insert into the neighbor, and Both allows both directions. The same restrictions apply to the side's standard energy capability.

Conduits first drain their buffers into permitted energy consumers, then transfer between permitted external endpoints. They avoid directly returning energy to its source or immediately re-supplying energy received during the same network pass. Between bidirectional non-battery endpoints, energy preferentially flows from the higher stored amount to the lower; output-only sources can still charge fuller consumers. **Batteries on the same conduit network do not transfer energy directly between one another**, preventing endless charge equalization.

Each placed conduit has an internal buffer equal to its tier rate. Its buffer, network mode, and connection configuration persist in the world. Breaking a conduit drops the item without preserving buffered FE. Conduits have no item slots and do not have crafting recipes yet.

## Jade status

With Jade installed, looking at these blocks adds live information to its tooltip. Generators report stored FE, active FE/t generation and burn time, active duration bonus, lava sides, and output rate. Batteries report stored FE and per-side transfer rate. Conduits report their local buffer, shared network budget, and network ordering mode. Jade is optional; none of these blocks requires it to operate.
