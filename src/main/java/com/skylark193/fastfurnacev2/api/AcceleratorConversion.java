package com.skylark193.fastfurnacev2.api;

import java.util.List;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** An immutable Accelerator conversion. One work unit equals one unboosted tick of work. */
public record AcceleratorConversion(
        Identifier id,
        Ingredient ingredient,
        int inputCount,
        ItemStack output,
        int workUnits,
        int priority) {

    public AcceleratorConversion {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ingredient, "ingredient");
        Objects.requireNonNull(output, "output");
        if (inputCount <= 0) {
            throw new IllegalArgumentException("Accelerator input count must be positive");
        }
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Accelerator output cannot be empty");
        }
        if (output.getCount() > output.getMaxStackSize()) {
            throw new IllegalArgumentException("Accelerator output exceeds its maximum stack size");
        }
        if (workUnits <= 0) {
            throw new IllegalArgumentException("Accelerator work units must be positive");
        }
        output = output.copy();
    }

    public AcceleratorConversion(
            Identifier id, Ingredient ingredient, ItemStack output, int workUnits) {
        this(id, ingredient, 1, output, workUnits, 0);
    }

    @Override
    public ItemStack output() {
        return this.output.copy();
    }

    /** Creates fresh display stacks with the required input count. */
    public List<ItemStack> inputItems() {
        return this.ingredient.items().map(holder -> new ItemStack(holder, this.inputCount)).toList();
    }
}
