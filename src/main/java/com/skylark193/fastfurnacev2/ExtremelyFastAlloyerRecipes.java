package com.skylark193.fastfurnacev2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Built-in recipe storage and shapeless matching for the Extremely Fast Alloyer. */
public final class ExtremelyFastAlloyerRecipes {
    public static final Identifier EXTREMELY_FAST_INGOT_RECIPE_ID = Identifier.fromNamespaceAndPath(
            FastFurnaceV2.MODID, "alloyer/extremely_fast_ingot");
    private static final List<ExtremelyFastAlloyerRecipe> RECIPES = new ArrayList<>();
    private static final List<ExtremelyFastAlloyerRecipe> RECIPE_VIEW = Collections.unmodifiableList(RECIPES);
    private static boolean initialized;

    private ExtremelyFastAlloyerRecipes() {
    }

    /** Creates recipe stacks only after default item components have been bound. */
    public static void initialize() {
        if (initialized) {
            return;
        }
        RECIPES.add(new ExtremelyFastAlloyerRecipe(
                EXTREMELY_FAST_INGOT_RECIPE_ID,
                List.of(
                        Ingredient.of(FastFurnaceV2.FAST_IRON_INGOT.get()),
                        Ingredient.of(FastFurnaceV2.FAST_GOLD_INGOT.get()),
                        Ingredient.of(FastFurnaceV2.FAST_DIAMOND.get())),
                new ItemStack(FastFurnaceV2.EXTREMELY_FAST_INGOT.get()),
                ExtremelyFastAlloyerBlockEntity.RECIPE_TICKS));
        initialized = true;
    }

    public static List<ExtremelyFastAlloyerRecipe> recipes() {
        return RECIPE_VIEW;
    }

    public static boolean isInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return RECIPES.stream()
                .flatMap(recipe -> recipe.ingredients().stream())
                .anyMatch(ingredient -> ingredient.test(stack));
    }

    static Optional<Match> findMatch(List<ItemStack> items) {
        if (items.size() < ExtremelyFastAlloyerMenu.INPUT_SLOT_COUNT) {
            return Optional.empty();
        }
        for (ExtremelyFastAlloyerRecipe recipe : RECIPES) {
            int[] slotsByIngredient = new int[ExtremelyFastAlloyerMenu.INPUT_SLOT_COUNT];
            if (matches(recipe, items, 0, new boolean[ExtremelyFastAlloyerMenu.INPUT_SLOT_COUNT], slotsByIngredient)) {
                return Optional.of(new Match(recipe, slotsByIngredient));
            }
        }
        return Optional.empty();
    }

    private static boolean matches(
            ExtremelyFastAlloyerRecipe recipe,
            List<ItemStack> items,
            int ingredientIndex,
            boolean[] usedSlots,
            int[] slotsByIngredient) {
        if (ingredientIndex == ExtremelyFastAlloyerMenu.INPUT_SLOT_COUNT) {
            return true;
        }
        for (int slot = 0; slot < ExtremelyFastAlloyerMenu.INPUT_SLOT_COUNT; slot++) {
            ItemStack stack = items.get(slot);
            if (!usedSlots[slot] && !stack.isEmpty()
                    && recipe.ingredients().get(ingredientIndex).test(stack)) {
                usedSlots[slot] = true;
                slotsByIngredient[ingredientIndex] = slot;
                if (matches(recipe, items, ingredientIndex + 1, usedSlots, slotsByIngredient)) {
                    return true;
                }
                usedSlots[slot] = false;
            }
        }
        return false;
    }

    record Match(ExtremelyFastAlloyerRecipe recipe, int[] slotsByIngredient) {
        void consumeIngredients(List<ItemStack> items) {
            for (int slot : this.slotsByIngredient) {
                items.get(slot).shrink(1);
            }
        }
    }
}
