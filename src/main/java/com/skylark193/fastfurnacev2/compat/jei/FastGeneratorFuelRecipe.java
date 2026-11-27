package com.skylark193.fastfurnacev2.compat.jei;

import java.util.List;
import net.minecraft.world.item.ItemStack;

record FastGeneratorFuelRecipe(
        List<ItemStack> inputs, int burnTime, int outputPerTick, int lavaSides, int burnTimePercent) {
    FastGeneratorFuelRecipe {
        inputs = List.copyOf(inputs);
        if (burnTime <= 0) {
            throw new IllegalArgumentException("Burn time must be greater than zero");
        }
        if (outputPerTick <= 0) {
            throw new IllegalArgumentException("Output must be greater than zero");
        }
        if (lavaSides < 0 || lavaSides > 4) {
            throw new IllegalArgumentException("Lava sides must be between zero and four");
        }
        if (burnTimePercent <= 0) {
            throw new IllegalArgumentException("Burn time percentage must be greater than zero");
        }
    }

    long totalEnergy() {
        return this.burnTime * (long) this.outputPerTick;
    }
}
