package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.AcceleratorBlockEntity;
import net.minecraft.world.item.ItemStack;

record AcceleratorRecipe(ItemStack input, ItemStack output, int workTicks) {
    AcceleratorRecipe {
        if (input.isEmpty() || output.isEmpty()) {
            throw new IllegalArgumentException("Accelerator ingredients and results must not be empty");
        }
        if (workTicks <= 0) {
            throw new IllegalArgumentException("Accelerator work time must be greater than zero");
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

    int boostedTicks() {
        return boostedTicks(AcceleratorBlockEntity.BOOSTED_WORK_PER_TICK);
    }

    int extremeBoostedTicks() {
        return boostedTicks(AcceleratorBlockEntity.EXTREME_COAL_BOOSTED_WORK_PER_TICK);
    }

    private int boostedTicks(int workPerTick) {
        return (this.workTicks + workPerTick - 1) / workPerTick;
    }
}
