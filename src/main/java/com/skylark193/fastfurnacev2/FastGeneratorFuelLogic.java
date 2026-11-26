package com.skylark193.fastfurnacev2;

import com.skylark193.fastfurnacev2.api.FastFuel;
import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
import org.jspecify.annotations.Nullable;

/** Shared generator fuel calculations for machine operation and optional recipe displays. */
public final class FastGeneratorFuelLogic {
    private FastGeneratorFuelLogic() {
    }

    public static int getBurnTime(int ordinaryBurnTime, @Nullable FastFuel definition) {
        int durationPercent = isBuiltInSpecialFuel(definition) ? 50 : 100;
        return (int) Math.min(Integer.MAX_VALUE, ordinaryBurnTime * (long) durationPercent / 100L);
    }

    public static int getOutputPercent(@Nullable FastFuel definition) {
        return definition == null ? 100 : 100 + definition.speedBonusPercent();
    }

    public static int getOutput(FastFurnaceTier tier, @Nullable FastFuel definition) {
        return tier.getGeneratorOutput() * getOutputPercent(definition) / 100;
    }

    private static boolean isBuiltInSpecialFuel(@Nullable FastFuel definition) {
        if (definition == null) {
            return false;
        }
        return definition.id().equals(FastFurnaceApi.FAST_COAL_FUEL_ID)
                || definition.id().equals(FastFurnaceApi.FAST_COAL_BLOCK_FUEL_ID)
                || definition.id().equals(FastFurnaceApi.EXTREME_COAL_FUEL_ID)
                || definition.id().equals(FastFurnaceApi.EXTREME_COAL_BLOCK_FUEL_ID);
    }
}
