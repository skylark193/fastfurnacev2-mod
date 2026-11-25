# Generators and batteries

[Documentation home](README.md) · [Blocks](blocks.md) · [Automation and GUI](automation-and-jei.md)

Fast Furnace V2 adds four generator and four battery tiers using NeoForge's standard Forge Energy (FE) capability. **Neither family has crafting recipes yet.** They are available in the Fast Furnace V2 and Functional Blocks creative tabs.

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

The paired bars on every vertical face fill from bottom to top in eleven charge steps, including empty and full. The animated lightning bolt remains on top. Charge and block visuals persist in the world; breaking a battery drops an empty, uncharged block item rather than a portable energy-storage item.

The GUI reports exact stored FE and capacity. Batteries have no item slots.
