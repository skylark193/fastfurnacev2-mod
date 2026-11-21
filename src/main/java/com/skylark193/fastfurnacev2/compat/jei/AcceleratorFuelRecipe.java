package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.AcceleratorBlockEntity;
import net.minecraft.world.item.ItemStack;

record AcceleratorFuelRecipe(ItemStack input, int boostWork) {
    AcceleratorFuelRecipe {
        input = input.copy();
        if (!AcceleratorBlockEntity.isAcceleratorFuel(input)
                || AcceleratorBlockEntity.getBoostWork(input) != boostWork) {
            throw new IllegalArgumentException("Accelerator fuel and boost work must match");
        }
    }

    @Override
    public ItemStack input() {
        return this.input.copy();
    }
}
