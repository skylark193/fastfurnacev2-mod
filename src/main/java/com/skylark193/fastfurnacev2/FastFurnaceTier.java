package com.skylark193.fastfurnacev2;

public enum FastFurnaceTier {
    FAST("fast_furnace", 150, 10, 150),
    IRON("iron_fast_furnace", 200, 15, 175),
    GOLD("gold_fast_furnace", 250, 20, 200),
    DIAMOND("diamond_fast_furnace", 300, 25, 225);

    public static final int FAST_COAL_SPEED_BONUS_PERCENT = 50;
    public static final int EXTREME_COAL_SPEED_BONUS_PERCENT = FAST_COAL_SPEED_BONUS_PERCENT * 2;
    public static final int VANILLA_COOK_TIME = 200;

    private final String id;
    private final int speedPercent;
    private final int doubleBurnChancePercent;
    private final int fastCoalBurnTimePercent;

    FastFurnaceTier(String id, int speedPercent, int doubleBurnChancePercent, int fastCoalBurnTimePercent) {
        this.id = id;
        this.speedPercent = speedPercent;
        this.doubleBurnChancePercent = doubleBurnChancePercent;
        this.fastCoalBurnTimePercent = fastCoalBurnTimePercent;
    }

    public String getId() {
        return this.id;
    }

    public String getCrusherId() {
        return this.id.replace("_furnace", "_crusher");
    }

    public String getGeneratorId() {
        return this.id.replace("_furnace", "_generator");
    }

    public String getBatteryId() {
        return this.id.replace("_furnace", "_battery");
    }

    public String getConduitId() {
        return this.id.replace("_furnace", "_power_conduit");
    }

    public int getGeneratorOutput() {
        return 128 << this.ordinal();
    }

    public int getGeneratorBufferCapacity() {
        return this.getGeneratorOutput() * 1_000;
    }

    public int getGeneratorExtractionLimit() {
        return this.getGeneratorOutput() * 2;
    }

    public int getBatteryCapacity() {
        return switch (this) {
            case FAST -> 256_000;
            case IRON -> 512_000;
            case GOLD -> 1_024_000;
            case DIAMOND -> 2_560_000;
        };
    }

    public int getBatteryTransferLimit() {
        return switch (this) {
            case FAST -> 256;
            case IRON -> 512;
            case GOLD -> 1_024;
            case DIAMOND -> 2_560;
        };
    }

    /** Returns the shared per-tick transfer budget for a network containing this tier. */
    public int getConduitTransferRate() {
        return switch (this) {
            case FAST -> 256;
            case IRON -> 512;
            case GOLD -> 1_024;
            case DIAMOND -> 2_560;
        };
    }

    public int getSpeedPercent() {
        return this.speedPercent;
    }

    public int getSpeedPercent(int fuelSpeedBonusPercent) {
        return (int) Math.min(Integer.MAX_VALUE, this.speedPercent + (long) fuelSpeedBonusPercent);
    }

    public int getSpeedPercent(boolean fastCoalActive) {
        return this.getSpeedPercent(fastCoalActive ? FAST_COAL_SPEED_BONUS_PERCENT : 0);
    }

    public int getSpeedPercent(boolean fastCoalActive, boolean extremeCoalActive) {
        if (extremeCoalActive) {
            return this.getSpeedPercent(EXTREME_COAL_SPEED_BONUS_PERCENT);
        }
        return this.getSpeedPercent(fastCoalActive);
    }

    public int getFastCoalBurnTimePercent() {
        return this.fastCoalBurnTimePercent;
    }

    public int getDoubleBurnChancePercent() {
        return this.doubleBurnChancePercent;
    }

    public float getDoubleBurnChance() {
        return this.doubleBurnChancePercent / 100.0F;
    }

    public int scaleCookTime(int vanillaCookTime, boolean fastCoalActive) {
        return this.scaleCookTime(vanillaCookTime, fastCoalActive, false);
    }

    public int scaleCookTime(int vanillaCookTime, boolean fastCoalActive, boolean extremeCoalActive) {
        return this.scaleCookTime(vanillaCookTime, extremeCoalActive
                ? EXTREME_COAL_SPEED_BONUS_PERCENT
                : fastCoalActive ? FAST_COAL_SPEED_BONUS_PERCENT : 0);
    }

    public int scaleCookTime(int vanillaCookTime, int fuelSpeedBonusPercent) {
        return Math.max(1, (int) (vanillaCookTime * 100L / this.getSpeedPercent(fuelSpeedBonusPercent)));
    }

    public int scaleFastCoalBurnTime(int burnTime) {
        return (int) Math.min(Integer.MAX_VALUE, burnTime * (long) this.fastCoalBurnTimePercent / 100L);
    }

    public int scaleExtremeCoalBurnTime(int burnTime) {
        // Double Fast Coal's added duration, rather than doubling its total adjusted duration.
        long extremeBurnTime = burnTime + 2L * (this.scaleFastCoalBurnTime(burnTime) - (long) burnTime);
        return (int) Math.min(Integer.MAX_VALUE, extremeBurnTime);
    }

    public float getSmeltCount(int burnTime, boolean fastCoalActive) {
        return this.getSmeltCount(burnTime, fastCoalActive, false);
    }

    public float getSmeltCount(int burnTime, boolean fastCoalActive, boolean extremeCoalActive) {
        return burnTime / (float) this.scaleCookTime(VANILLA_COOK_TIME, fastCoalActive, extremeCoalActive);
    }

    public float getSmeltCount(int burnTime, int fuelSpeedBonusPercent) {
        return burnTime / (float) this.scaleCookTime(VANILLA_COOK_TIME, fuelSpeedBonusPercent);
    }
}
