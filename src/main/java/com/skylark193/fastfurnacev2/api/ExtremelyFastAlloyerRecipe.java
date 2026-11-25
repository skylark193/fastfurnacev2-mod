package com.skylark193.fastfurnacev2.api;

import java.util.List;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** An immutable shapeless three-ingredient recipe used only by the Extremely Fast Alloyer. */
public record ExtremelyFastAlloyerRecipe(
        Identifier id,
        List<Ingredient> ingredients,
        ItemStack output,
        int processingTicks,
        int priority) {
    public static final int INGREDIENT_COUNT = 3;

    public ExtremelyFastAlloyerRecipe {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ingredients, "ingredients");
        Objects.requireNonNull(output, "output");
        ingredients = List.copyOf(ingredients);
        if (ingredients.size() != INGREDIENT_COUNT || ingredients.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Extremely Fast Alloyer recipes require exactly three nonnull ingredients");
        }
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Extremely Fast Alloyer output cannot be empty");
        }
        if (processingTicks <= 0) {
            throw new IllegalArgumentException("Extremely Fast Alloyer processing time must be positive");
        }
        output = output.copy();
    }

    public ExtremelyFastAlloyerRecipe(
            Identifier id, List<Ingredient> ingredients, ItemStack output, int processingTicks) {
        this(id, ingredients, output, processingTicks, 0);
    }

    @Override
    public ItemStack output() {
        return this.output.copy();
    }

    /** Creates fresh display stacks for one ingredient in this shapeless recipe. */
    public List<ItemStack> inputItems(int ingredientIndex) {
        return this.ingredients.get(ingredientIndex).items().map(ItemStack::new).toList();
    }
}
