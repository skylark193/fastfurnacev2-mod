package com.skylark193.fastfurnacev2.compat.jei;

import net.minecraft.world.item.ItemStack;

record FastCrusherRecipe(ItemStack input, ItemStack output) {
    FastCrusherRecipe {
        if (input.isEmpty() || output.isEmpty()) {
            throw new IllegalArgumentException("Crushing ingredients and results must not be empty");
        }
        input = input.copy();
        output = output.copy();
    }

    @Override
    public ItemStack input() {
        return this.input.copy();
    }

    @Override
    public ItemStack output() {
        return this.output.copy();
    }
}
