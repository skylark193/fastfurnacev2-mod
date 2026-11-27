package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.FastFurnaceTier;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
import java.util.EnumMap;
import java.util.Locale;
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
import net.minecraft.network.chat.Component;

final class FastGeneratorFuelCategory implements IRecipeCategory<FastGeneratorFuelRecipe> {
    private static final Map<FastFurnaceTier, IRecipeType<FastGeneratorFuelRecipe>> RECIPE_TYPES =
            new EnumMap<>(FastFurnaceTier.class);
    private static final int WIDTH = 180;
    private static final int HEIGHT = 64;

    static {
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            RECIPE_TYPES.put(tier, IRecipeType.create(
                    FastFurnaceV2.MODID, tier.getGeneratorId() + "_fuel", FastGeneratorFuelRecipe.class));
        }
    }

    private final FastFurnaceTier tier;
    private final Component title;
    private final IDrawable icon;

    FastGeneratorFuelCategory(IGuiHelper guiHelper, FastFurnaceTier tier) {
        this.tier = tier;
        this.title = Component.translatable("jei.fastfurnacev2." + tier.getGeneratorId() + "_fuel");
        this.icon = guiHelper.createDrawableItemLike(FastFurnaceV2.getGeneratorBlock(tier).get());
    }

    static IRecipeType<FastGeneratorFuelRecipe> getRecipeType(FastFurnaceTier tier) {
        return RECIPE_TYPES.get(tier);
    }

    @Override
    public IRecipeType<FastGeneratorFuelRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, FastGeneratorFuelRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 18)
                .setStandardSlotBackground()
                .addItemStacks(recipe.inputs());
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder, FastGeneratorFuelRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeFlame(recipe.burnTime()).setPosition(1, 1);
        addCenteredText(builder, Component.translatable(
                "jei.fastfurnacev2.generator_fuel_output", formatEnergy(recipe.outputPerTick())), 0);
        addCenteredText(builder, Component.translatable(
                "jei.fastfurnacev2.generator_fuel_total", formatEnergy(recipe.totalEnergy())), 16);
        addCenteredText(builder, Component.translatable(
                "jei.fastfurnacev2.generator_fuel_burn_percent", recipe.burnTimePercent()), 32);
        addCenteredText(builder, Component.translatable(
                "jei.fastfurnacev2.generator_fuel_lava", recipe.lavaSides()), 48);
    }

    private static void addCenteredText(IRecipeExtrasBuilder builder, Component text, int y) {
        builder.addText(text, WIDTH - 20, 14)
                .setPosition(20, y)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setTextAlignment(VerticalAlignment.CENTER)
                .setColor(0xFF808080);
    }

    private static String formatEnergy(long amount) {
        return String.format(Locale.ROOT, "%,d", amount);
    }
}
