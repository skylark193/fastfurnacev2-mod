package com.skylark193.fastfurnacev2;

import com.skylark193.fastfurnacev2.api.ExtremelyFastAlloyerRecipe;
import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Internal recipe storage and distinct-slot shapeless matching for the Extremely Fast Alloyer. */
public final class ExtremelyFastAlloyerRecipes {
    private static final List<ExtremelyFastAlloyerRecipe> RECIPES = new ArrayList<>();
    private static final List<ExtremelyFastAlloyerRecipe> RECIPE_VIEW = Collections.unmodifiableList(RECIPES);
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final Map<Identifier, ExtremelyFastAlloyerRecipe> RECIPES_BY_ID = new HashMap<>();
    private static List<ExtremelyFastAlloyerRecipe> matchOrder = List.of();
    private static int nextSequence;

    private ExtremelyFastAlloyerRecipes() {
    }

    /** Creates recipe stacks only after default item components have been bound. */
    public static void registerBuiltIn() {
        register(new ExtremelyFastAlloyerRecipe(
                FastFurnaceApi.EXTREMELY_FAST_INGOT_RECIPE_ID,
                List.of(
                        Ingredient.of(FastFurnaceV2.FAST_IRON_INGOT.get()),
                        Ingredient.of(FastFurnaceV2.FAST_GOLD_INGOT.get()),
                        Ingredient.of(FastFurnaceV2.FAST_DIAMOND.get())),
                new ItemStack(FastFurnaceV2.EXTREMELY_FAST_INGOT.get()),
                ExtremelyFastAlloyerBlockEntity.RECIPE_TICKS));
    }

    public static void register(ExtremelyFastAlloyerRecipe recipe) {
        if (RECIPES_BY_ID.putIfAbsent(recipe.id(), recipe) != null) {
            throw new IllegalArgumentException("Duplicate Extremely Fast Alloyer recipe ID: " + recipe.id());
        }
        ENTRIES.add(new Entry(recipe, nextSequence++));
        RECIPES.add(recipe);
        matchOrder = ENTRIES.stream()
                .sorted(Comparator.comparingInt((Entry entry) -> entry.recipe().priority())
                        .thenComparingInt(entry -> entry.sequence())
                        .reversed())
                .map(entry -> entry.recipe())
                .toList();
    }

    public static List<ExtremelyFastAlloyerRecipe> recipes() {
        return RECIPE_VIEW;
    }

    public static Optional<ExtremelyFastAlloyerRecipe> findRecipe(Identifier id) {
        return Optional.ofNullable(RECIPES_BY_ID.get(id));
    }

    public static boolean isInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return RECIPES.stream()
                .flatMap(recipe -> recipe.ingredients().stream())
                .anyMatch(ingredient -> ingredient.test(stack));
    }

    /** Finds a match using only the first three items, which correspond to the Alloyer input slots. */
    public static Optional<ExtremelyFastAlloyerRecipe> findRecipe(List<ItemStack> items) {
        return findMatch(items).map(match -> match.recipe());
    }

    static Optional<Match> findMatch(List<ItemStack> items) {
        if (items.size() < ExtremelyFastAlloyerRecipe.INGREDIENT_COUNT) {
            return Optional.empty();
        }
        for (ExtremelyFastAlloyerRecipe recipe : matchOrder) {
            int[] slotsByIngredient = new int[ExtremelyFastAlloyerRecipe.INGREDIENT_COUNT];
            if (matches(recipe, items, 0, new boolean[ExtremelyFastAlloyerRecipe.INGREDIENT_COUNT], slotsByIngredient)) {
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
        if (ingredientIndex == ExtremelyFastAlloyerRecipe.INGREDIENT_COUNT) {
            return true;
        }
        for (int slot = 0; slot < ExtremelyFastAlloyerRecipe.INGREDIENT_COUNT; slot++) {
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

    private record Entry(ExtremelyFastAlloyerRecipe recipe, int sequence) {
    }

    record Match(ExtremelyFastAlloyerRecipe recipe, int[] slotsByIngredient) {
        void consumeIngredients(List<ItemStack> items) {
            for (int slot : this.slotsByIngredient) {
                items.get(slot).shrink(1);
            }
        }
    }
}
