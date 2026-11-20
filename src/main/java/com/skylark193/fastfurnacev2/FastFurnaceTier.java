package com.skylark193.fastfurnacev2;

public enum FastFurnaceTier {
    FAST("fast_furnace", 150, 10, 150),
    IRON("iron_fast_furnace", 200, 15, 175),
    GOLD("gold_fast_furnace", 250, 20, 200),
    DIAMOND("diamond_fast_furnace", 300, 25, 225);

    public static final int FAST_COAL_SPEED_BONUS_PERCENT = 50;
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

    public int getSpeedPercent() {
        return this.speedPercent;
    }

    public int getSpeedPercent(boolean fastCoalActive) {
        return this.speedPercent + (fastCoalActive ? FAST_COAL_SPEED_BONUS_PERCENT : 0);
    }

    public int getDoubleBurnChancePercent() {
        return this.doubleBurnChancePercent;
    }

    public float getDoubleBurnChance() {
        return this.doubleBurnChancePercent / 100.0F;
    }

    public int scaleCookTime(int vanillaCookTime, boolean fastCoalActive) {
        return Math.max(1, (int) (vanillaCookTime * 100L / getSpeedPercent(fastCoalActive)));
    }

    public int scaleFastCoalBurnTime(int burnTime) {
        return (int) Math.min(Integer.MAX_VALUE, burnTime * (long) this.fastCoalBurnTimePercent / 100L);
    }

    public float getSmeltCount(int burnTime, boolean fastCoalActive) {
        return burnTime / (float) scaleCookTime(VANILLA_COOK_TIME, fastCoalActive);
    }
}
