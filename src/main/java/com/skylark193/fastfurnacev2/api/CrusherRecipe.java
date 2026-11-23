package com.skylark193.fastfurnacev2.api;

import java.util.List;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * An immutable Fast Crusher recipe. Processing time is measured at vanilla furnace speed and is
 * scaled by the crusher tier and active fuel. A processing time of zero inherits the input's
 * ordinary smelting time, falling back to 200 ticks when no smelting recipe exists. Output doubling
 * doubles the complete output stack and follows the same blocked-output rules as built-in recipes.
 */
public record CrusherRecipe(
        Identifier id,
        Ingredient ingredient,
        int inputCount,
        ItemStack output,
        int processingTime,
        int priority) {

    public CrusherRecipe {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ingredient, "ingredient");
        Objects.requireNonNull(output, "output");
        if (inputCount <= 0) {
            throw new IllegalArgumentException("Crusher input count must be positive");
        }
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Crusher output cannot be empty");
        }
        if (output.getCount() * 2 > output.getMaxStackSize()) {
            throw new IllegalArgumentException("Crusher output must leave room for its doubled result");
        }
        if (processingTime < 0) {
            throw new IllegalArgumentException("Crusher processing time cannot be negative");
        }
        output = output.copy();
    }

    public CrusherRecipe(Identifier id, Ingredient ingredient, ItemStack output, int processingTime) {
        this(id, ingredient, 1, output, processingTime, 0);
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
