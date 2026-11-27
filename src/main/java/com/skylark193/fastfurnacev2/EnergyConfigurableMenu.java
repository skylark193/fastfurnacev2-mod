package com.skylark193.fastfurnacev2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface EnergyConfigurableMenu {
    BlockPos getEnergyBlockPos();

    EnergySideMode getEnergySideMode(Direction direction);
}
