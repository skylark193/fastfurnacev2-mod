package com.skylark193.fastfurnacev2.api;

import java.util.List;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Immutable runtime proc chances for matching tools. Chances are probabilities from zero through one. */
public record FastToolModifiers(
        Identifier id,
        Ingredient ingredient,
        float durabilitySaveChance,
        float blockDropDoubleChance,
        float killDropDoubleChance,
        float damageDoubleChance,
        int priority) {
    public FastToolModifiers {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ingredient, "ingredient");
        validateChance(durabilitySaveChance, "durabilitySaveChance");
        validateChance(blockDropDoubleChance, "blockDropDoubleChance");
        validateChance(killDropDoubleChance, "killDropDoubleChance");
        validateChance(damageDoubleChance, "damageDoubleChance");
    }

    public static Builder builder(Identifier id, Ingredient ingredient) {
        return new Builder(id, ingredient);
    }

    public List<ItemStack> inputItems() {
        return this.ingredient.items().map(ItemStack::new).toList();
    }

    private static void validateChance(float chance, String name) {
        if (!Float.isFinite(chance) || chance < 0.0F || chance > 1.0F) {
            throw new IllegalArgumentException(name + " must be between zero and one");
        }
    }

    public static final class Builder {
        private final Identifier id;
        private final Ingredient ingredient;
        private float durabilitySaveChance;
        private float blockDropDoubleChance;
        private float killDropDoubleChance;
        private float damageDoubleChance;
        private int priority;

        private Builder(Identifier id, Ingredient ingredient) {
            this.id = Objects.requireNonNull(id, "id");
            this.ingredient = Objects.requireNonNull(ingredient, "ingredient");
        }

        public Builder durabilitySaveChance(float chance) {
            this.durabilitySaveChance = chance;
            return this;
        }

        public Builder blockDropDoubleChance(float chance) {
            this.blockDropDoubleChance = chance;
            return this;
        }

        public Builder killDropDoubleChance(float chance) {
            this.killDropDoubleChance = chance;
            return this;
        }

        public Builder damageDoubleChance(float chance) {
            this.damageDoubleChance = chance;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public FastToolModifiers build() {
            return new FastToolModifiers(
                    this.id, this.ingredient, this.durabilitySaveChance, this.blockDropDoubleChance,
                    this.killDropDoubleChance, this.damageDoubleChance, this.priority);
        }
    }
}
