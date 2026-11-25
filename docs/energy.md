# Generators, batteries, and power conduits

[Documentation home](README.md) · [Blocks](blocks.md) · [Automation and GUI](automation-and-jei.md)

Fast Furnace V2 adds four generator, battery, and power-conduit tiers using NeoForge's standard Forge Energy (FE) capabilities. **None of these blocks has a crafting recipe yet.** They are available in the Fast Furnace V2 and Functional Blocks creative tabs.

## Fast Generators

Generators accept ordinary furnace fuel in their single slot. They burn each item at its ordinary furnace duration, except for the built-in special fuels below. Furnace tier speed, random fuel doubling, and crusher output rules do not apply.

| Tier | Ordinary fuel | Fast Coal | Extreme Coal | Internal buffer |
|---|---:|---:|---:|---:|
| Fast Generator | 80 FE/t | 120 FE/t | 160 FE/t | 3,200 FE |
| Iron Fast Generator | 160 FE/t | 240 FE/t | 320 FE/t | 6,400 FE |
| Gold Fast Generator | 320 FE/t | 480 FE/t | 640 FE/t | 12,800 FE |
| Diamond Fast Generator | 640 FE/t | 960 FE/t | 1,280 FE/t | 25,600 FE |

Fast Coal and its block provide **50% more FE/t** and **25% longer burn time**. Extreme Coal and its block provide **100% more FE/t** and **50% longer burn time**. These bonuses apply independently at every tier.

Generators push stored FE to all adjacent energy consumers, up to twice their ordinary tier output per neighbor per tick. They pause burn time and production when the internal buffer is full. Replacing the fuel slot does not change the active burn's bonuses. Energy, remaining burn time, total burn time, and active-fuel output are saved with the placed block; breaking it drops only the block item.

The GUI shows current FE storage, capacity, active output, fuel, and burn progress. Hoppers and item automation can insert fuel from any side and remove fuel-slot items.

## Fast Batteries

Batteries accept and supply FE through the standard block energy capability on every side.

| Tier | Capacity | Maximum insertion or extraction per operation |
|---|---:|---:|
| Fast Battery | 80,000 FE | 4,000 FE |
| Iron Fast Battery | 160,000 FE | 8,000 FE |
| Gold Fast Battery | 320,000 FE | 16,000 FE |
| Diamond Fast Battery | 640,000 FE | 32,000 FE |

Breaking a battery preserves its exact stored FE in the dropped item. Placing it restores that charge, clamped to the new battery's capacity if its components were modified to contain more. Battery items also expose NeoForge's standard transactional item energy capability, allowing other mods to charge and discharge them safely.

The paired bars on every vertical face fill from bottom to top in eleven charge steps, including empty and full. The animated lightning bolt remains on top. Charge persists in the world and across breaking and placement. The GUI reports exact stored FE and capacity. Batteries have no item slots.

## Fast Power Conduits

Conduits connect to adjacent conduits and blocks exposing an energy capability. Their connected models have animated internal lava channels; the channel widens from 2 pixels in the Fast tier to 4, 6, and 8 pixels in the Iron, Gold, and Diamond tiers.

| Tier | Shared network transfer budget |
|---|---:|
| Fast Power Conduit | 1,000 FE/t |
| Iron Fast Power Conduit | 10,000 FE/t |
| Gold Fast Power Conduit | 100,000 FE/t |
| Diamond Fast Power Conduit | 1,000,000 FE/t |

Each connected network runs at most once per game tick and moves no more than its shared budget, including energy already stored in conduit buffers. Chains, branches, and loops do not multiply this limit. A mixed-tier network uses the rate of its slowest connected conduit. Actual transfer can be lower when sources, destinations, or their per-operation limits cannot supply or accept the full budget.

Conduits first drain their buffers into adjacent energy consumers, then transfer between adjacent external energy handlers. They avoid directly returning energy to its source or immediately re-supplying energy received during the same network pass. Between bidirectional endpoints, energy preferentially flows from the higher stored amount to the lower; output-only sources can still charge fuller consumers.

Each placed conduit has an internal buffer equal to its tier rate and persists that buffer in the world. Breaking a conduit drops the item without preserving buffered FE. Conduits have no GUI or item slots and do not have crafting recipes yet.
