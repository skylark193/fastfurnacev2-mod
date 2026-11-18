package com.skylark193.fastfurnacev2.compat.jei;

import java.util.List;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import net.minecraft.world.item.ItemStack;

record FastFurnaceFuelRecipe(List<ItemStack> inputs, int burnTime) implements IJeiFuelingRecipe {
    FastFurnaceFuelRecipe {
        inputs = List.copyOf(inputs);
        if (burnTime <= 0) {
            throw new IllegalArgumentException("Burn time must be greater than zero");
        }
    }

    @Override
    public List<ItemStack> getInputs() {
        return this.inputs;
    }

    @Override
    public int getBurnTime() {
        return this.burnTime;
    }
}
