package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.FastFurnaceTier;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.Map;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import net.minecraft.network.chat.Component;

final class FastFurnaceFuelCategory implements IRecipeCategory<IJeiFuelingRecipe> {
    private static final Map<FastFurnaceTier, IRecipeType<IJeiFuelingRecipe>> RECIPE_TYPES =
            new EnumMap<>(FastFurnaceTier.class);
    private static final int WIDTH = 160;
    private static final int HEIGHT = 34;

    static {
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            RECIPE_TYPES.put(tier, IRecipeType.create(
                    FastFurnaceV2.MODID, tier.getId() + "_fuel", IJeiFuelingRecipe.class));
        }
    }

    private final FastFurnaceTier tier;
    private final Component title;
    private final IDrawable icon;

    FastFurnaceFuelCategory(IGuiHelper guiHelper, FastFurnaceTier tier) {
        this.tier = tier;
        this.title = Component.translatable("jei.fastfurnacev2." + tier.getId() + "_fuel");
        this.icon = guiHelper.createDrawableItemLike(FastFurnaceV2.getFurnaceBlock(tier).get());
    }

    static IRecipeType<IJeiFuelingRecipe> getRecipeType(FastFurnaceTier tier) {
        return RECIPE_TYPES.get(tier);
    }

    @Override
    public IRecipeType<IJeiFuelingRecipe> getRecipeType() {
        return getRecipeType(this.tier);
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IJeiFuelingRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 17)
                .setStandardSlotBackground()
                .addItemStacks(recipe.getInputs());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, IJeiFuelingRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeFlame(recipe.getBurnTime()).setPosition(1, 0);
        builder.addText(createSmeltCountText(this.tier, recipe), WIDTH - 20, HEIGHT)
                .setPosition(20, 0)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setTextAlignment(VerticalAlignment.CENTER)
                .setColor(0xFF808080);
    }

    private static Component createSmeltCountText(FastFurnaceTier tier, IJeiFuelingRecipe recipe) {
        int speedBonusPercent;
        if (recipe instanceof FastFurnaceFuelRecipe fastRecipe) {
            speedBonusPercent = fastRecipe.speedBonusPercent();
        } else {
            boolean extremeCoalActive = recipe.getInputs().stream()
                    .anyMatch(FastFurnaceV2::isExtremeCoalFuel);
            boolean fastCoalActive = !extremeCoalActive && recipe.getInputs().stream()
                    .anyMatch(FastFurnaceV2::isFastCoalFuel);
            speedBonusPercent = extremeCoalActive ? FastFurnaceTier.EXTREME_COAL_SPEED_BONUS_PERCENT
                    : fastCoalActive ? FastFurnaceTier.FAST_COAL_SPEED_BONUS_PERCENT : 0;
        }
        float smeltCount = tier.getSmeltCount(recipe.getBurnTime(), speedBonusPercent);
        if (smeltCount == 1.0F) {
            return Component.translatable("gui.jei.category.fuel.smeltCount.single");
        }
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        return Component.translatable(
                "gui.jei.category.fuel.smeltCount",
                numberFormat.format(smeltCount));
    }
}
