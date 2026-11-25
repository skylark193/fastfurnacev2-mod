package com.skylark193.fastfurnacev2.api;

import com.skylark193.fastfurnacev2.FastFurnaceTier;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * An immutable fuel definition shared by Fast Furnaces, Fast Crushers, Fast Generators, and the
 * Accelerator.
 *
 * <p>A zero base burn time leaves the item's ordinary fuel value unchanged. This allows a
 * definition to add bonuses to an existing vanilla or modded fuel, or to be Accelerator-only.
 * Burn-time percentages apply to furnace and crusher duration. Speed bonuses add percentage points
 * to furnace/crusher speed and generator output. Generators otherwise use ordinary burn duration;
 * only the built-in Fast and Extreme Coal definitions receive their separate generator duration
 * bonuses. Accelerator work is optional and independent of the other fuel behavior.</p>
 */
public record FastFuel(
        Identifier id,
        Ingredient ingredient,
        int baseBurnTime,
        ToIntFunction<FastFurnaceTier> burnTimePercent,
        int speedBonusPercent,
        int acceleratorWork,
        int acceleratorWorkPerTick,
        int priority) {

    public FastFuel {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ingredient, "ingredient");
        Objects.requireNonNull(burnTimePercent, "burnTimePercent");
        if (baseBurnTime < 0) {
            throw new IllegalArgumentException("Base burn time cannot be negative");
        }
        if (speedBonusPercent < 0) {
            throw new IllegalArgumentException("Speed bonus cannot be negative");
        }
        if (acceleratorWork < 0 || acceleratorWorkPerTick < 0) {
            throw new IllegalArgumentException("Accelerator boost values cannot be negative");
        }
        if (acceleratorWork == 0 && acceleratorWorkPerTick != 0
                || acceleratorWork > 0 && acceleratorWorkPerTick == 0) {
            throw new IllegalArgumentException(
                    "Accelerator work and work per tick must either both be zero or both be positive");
        }
    }

    public static Builder builder(Identifier id, Ingredient ingredient, int baseBurnTime) {
        return new Builder(id, ingredient, baseBurnTime);
    }

    /** Returns the adjusted burn time before the furnace tier's random doubling roll. */
    public int scaleBurnTime(FastFurnaceTier tier, int ordinaryBurnTime) {
        int percent = this.burnTimePercent.applyAsInt(tier);
        if (percent < 0) {
            throw new IllegalStateException("Fuel " + this.id + " returned a negative burn-time percentage");
        }
        return (int) Math.min(Integer.MAX_VALUE, ordinaryBurnTime * (long) percent / 100L);
    }

    /** Returns the total processing speed supplied by this fuel in the given tier. */
    public int getSpeedPercent(FastFurnaceTier tier) {
        return tier.getSpeedPercent(this.speedBonusPercent);
    }

    public boolean isAcceleratorFuel() {
        return this.acceleratorWork > 0;
    }

    /** Creates fresh display stacks for JEI and other recipe viewers. */
    public List<ItemStack> inputItems() {
        return this.ingredient.items().map(holder -> new ItemStack(holder)).toList();
    }

    public static final class Builder {
        private final Identifier id;
        private final Ingredient ingredient;
        private final int baseBurnTime;
        private ToIntFunction<FastFurnaceTier> burnTimePercent = tier -> 100;
        private int speedBonusPercent;
        private int acceleratorWork;
        private int acceleratorWorkPerTick;
        private int priority;

        private Builder(Identifier id, Ingredient ingredient, int baseBurnTime) {
            this.id = Objects.requireNonNull(id, "id");
            this.ingredient = Objects.requireNonNull(ingredient, "ingredient");
            this.baseBurnTime = baseBurnTime;
        }

        /** Uses one burn-time percentage for every tier; 100 leaves duration unchanged. */
        public Builder burnTimePercent(int percent) {
            if (percent < 0) {
                throw new IllegalArgumentException("Burn-time percentage cannot be negative");
            }
            this.burnTimePercent = tier -> percent;
            return this;
        }

        /** Supplies tier-dependent burn-time percentages; 100 leaves duration unchanged. */
        public Builder burnTimePercent(ToIntFunction<FastFurnaceTier> percent) {
            this.burnTimePercent = Objects.requireNonNull(percent, "percent");
            return this;
        }

        public Builder speedBonusPercent(int bonus) {
            this.speedBonusPercent = bonus;
            return this;
        }

        /** Adds an Accelerator boost. One work unit equals one unboosted tick of work. */
        public Builder acceleratorBoost(int work, int workPerTick) {
            this.acceleratorWork = work;
            this.acceleratorWorkPerTick = workPerTick;
            return this;
        }

        /** Higher priorities win when multiple ingredients match; later registrations win ties. */
        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public FastFuel build() {
            FastFuel fuel = new FastFuel(
                    this.id, this.ingredient, this.baseBurnTime, this.burnTimePercent,
                    this.speedBonusPercent, this.acceleratorWork, this.acceleratorWorkPerTick,
                    this.priority);
            for (FastFurnaceTier tier : FastFurnaceTier.values()) {
                if (fuel.burnTimePercent.applyAsInt(tier) < 0) {
                    throw new IllegalArgumentException("Burn-time percentage cannot be negative for " + tier);
                }
            }
            return fuel;
        }
    }
}
