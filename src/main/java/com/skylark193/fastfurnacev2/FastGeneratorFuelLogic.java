package com.skylark193.fastfurnacev2;

import com.skylark193.fastfurnacev2.api.FastFuel;
import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
import org.jspecify.annotations.Nullable;

/** Shared generator fuel calculations for machine operation and optional recipe displays. */
public final class FastGeneratorFuelLogic {
    public static final int MAX_HORIZONTAL_LAVA = 4;
    public static final int LAVA_BURN_BONUS_PER_SIDE = 25;
    public static final int MAX_LAVA_BURN_BONUS_PERCENT = 100;
    public static final int BASE_SPECIAL_BURN_TIME_PERCENT = 50;

    private FastGeneratorFuelLogic() {
    }

    public static int getBurnTime(int ordinaryBurnTime, @Nullable FastFuel definition) {
        return getBurnTime(ordinaryBurnTime, definition, 0);
    }

    public static int getBurnTime(
            int ordinaryBurnTime, @Nullable FastFuel definition, int horizontalLavaSides) {
        if (!isBuiltInSpecialFuel(definition)) {
            return ordinaryBurnTime;
        }
        int durationPercent = BASE_SPECIAL_BURN_TIME_PERCENT
                + getLavaBurnBonusPercent(definition, horizontalLavaSides);
        return (int) Math.min(Integer.MAX_VALUE, ordinaryBurnTime * (long) durationPercent / 100L);
    }

    public static int getLavaBurnBonusPercent(@Nullable FastFuel definition, int horizontalLavaSides) {
        if (!isBuiltInSpecialFuel(definition)) {
            return 0;
        }
        int sides = Math.max(0, Math.min(MAX_HORIZONTAL_LAVA, horizontalLavaSides));
        return Math.min(MAX_LAVA_BURN_BONUS_PERCENT, sides * LAVA_BURN_BONUS_PER_SIDE);
    }

    public static int getOutputPercent(@Nullable FastFuel definition) {
        return definition == null ? 100 : 100 + definition.speedBonusPercent();
    }

    public static int getOutput(FastFurnaceTier tier, @Nullable FastFuel definition) {
        return tier.getGeneratorOutput() * getOutputPercent(definition) / 100;
    }

    public static boolean isBuiltInSpecialFuel(@Nullable FastFuel definition) {
        if (definition == null) {
            return false;
        }
        return definition.id().equals(FastFurnaceApi.FAST_COAL_FUEL_ID)
                || definition.id().equals(FastFurnaceApi.FAST_COAL_BLOCK_FUEL_ID)
                || definition.id().equals(FastFurnaceApi.EXTREME_COAL_FUEL_ID)
                || definition.id().equals(FastFurnaceApi.EXTREME_COAL_BLOCK_FUEL_ID);
    }
}
