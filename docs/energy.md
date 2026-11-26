# Generators, batteries, and power conduits

[Documentation home](README.md) · [Blocks](blocks.md) · [Automation and GUI](automation-and-jei.md)

Fast Furnace V2 adds four generator, battery, and power-conduit tiers using NeoForge's standard Forge Energy (FE) capabilities. **None of these blocks has a crafting recipe yet.** They are available in the Fast Furnace V2 and Functional Blocks creative tabs.

## Fast Generators

Generators accept ordinary furnace fuel in their single slot. They burn each item at its ordinary furnace duration, except for the built-in special fuels below. Furnace tier speed, random fuel doubling, and crusher output rules do not apply.

| Tier | Ordinary fuel | Fast Coal | Extreme Coal | Internal buffer |
|---|---:|---:|---:|---:|
| Fast Generator | 128 FE/t | 192 FE/t | 256 FE/t | 128,000 FE |
| Iron Fast Generator | 256 FE/t | 384 FE/t | 512 FE/t | 256,000 FE |
| Gold Fast Generator | 512 FE/t | 768 FE/t | 1,024 FE/t | 512,000 FE |
| Diamond Fast Generator | 1,024 FE/t | 1,536 FE/t | 2,048 FE/t | 1,024,000 FE |

Fast Coal and its block provide **50% more FE/t**, while Extreme Coal and its block provide **100% more FE/t**. Both special fuel types burn **50% faster**, lasting half their ordinary duration. These modifiers apply independently at every tier.

Generators push stored FE to all adjacent energy consumers, up to twice their ordinary tier output per neighbor per tick. They pause burn time and production when the internal buffer is full. Replacing the fuel slot does not change the active burn's bonuses. Energy, remaining burn time, total burn time, and active-fuel output are saved with the placed block; breaking it drops only the block item.

The GUI has one centered fuel slot, with burn progress above it and active FE/t output below. The energy bar shows current FE storage; hovering it displays maximum capacity. The **`i`** button opens an information fly-out with generation status, active output, and remaining and total fuel times. Hoppers and item automation can insert fuel from any side and remove fuel-slot items.

## Fast Batteries

Batteries accept and supply FE through the standard block energy capability on every side.

| Tier | Capacity | Maximum insertion or extraction per operation |
|---|---:|---:|
| Fast Battery | 256,000 FE | 256 FE |
| Iron Fast Battery | 512,000 FE | 512 FE |
| Gold Fast Battery | 1,024,000 FE | 1,024 FE |
| Diamond Fast Battery | 2,560,000 FE | 2,560 FE |

Breaking a battery preserves its exact stored FE in the dropped item. Placing it restores that charge, clamped to the new battery's capacity if its components were modified to contain more. Battery items also expose NeoForge's standard transactional item energy capability, allowing other mods to charge and discharge them safely.

The paired bars on every vertical face fill from bottom to top in eleven charge steps, including empty and full. The animated lightning bolt remains on top. Charge persists in the world and across breaking and placement. The GUI reports exact stored FE and capacity. Batteries have no item slots.

## Fast Power Conduits

Conduits connect to adjacent conduits and blocks exposing an energy capability. Their connected models have animated internal lava channels; the channel widens from 2 pixels in the Fast tier to 4, 6, and 8 pixels in the Iron, Gold, and Diamond tiers. Eight-pixel-wide selection and collision shapes cover the core and connected arms so conduits can be targeted and broken reliably.

| Tier | Shared network transfer budget |
|---|---:|
| Fast Power Conduit | 256 FE/t |
| Iron Fast Power Conduit | 512 FE/t |
| Gold Fast Power Conduit | 1,024 FE/t |
| Diamond Fast Power Conduit | 2,560 FE/t |

Each connected network runs at most once per game tick and moves no more than its shared budget, including energy already stored in conduit buffers. Chains, branches, and loops do not multiply this limit. A mixed-tier network uses the rate of its slowest connected conduit. Actual transfer can be lower when sources, destinations, or their per-operation limits cannot supply or accept the full budget.

Conduits first drain their buffers into adjacent energy consumers, then transfer between adjacent external energy handlers. They avoid directly returning energy to its source or immediately re-supplying energy received during the same network pass. Between bidirectional endpoints, energy preferentially flows from the higher stored amount to the lower; output-only sources can still charge fuller consumers.

Each placed conduit has an internal buffer equal to its tier rate and persists that buffer in the world. Breaking a conduit drops the item without preserving buffered FE. Conduits have no GUI or item slots and do not have crafting recipes yet.
