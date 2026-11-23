package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.FastFurnaceV2;
import com.skylark193.fastfurnacev2.api.AcceleratorConversion;
import com.skylark193.fastfurnacev2.api.FastFuel;
import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;

final class AcceleratorCategory implements IRecipeCategory<AcceleratorConversion> {
    private static final IRecipeType<AcceleratorConversion> RECIPE_TYPE =
            IRecipeType.create(FastFurnaceV2.MODID, "accelerator", AcceleratorConversion.class);
    private static final int WIDTH = 170;

    private final Component title;
    private final IDrawable icon;
    private final List<Integer> boostSpeeds;

    AcceleratorCategory(IGuiHelper guiHelper) {
        this.title = Component.translatable("jei.fastfurnacev2.accelerator");
        this.icon = guiHelper.createDrawableItemLike(FastFurnaceV2.ACCELERATOR.get());
        this.boostSpeeds = FastFurnaceApi.fuels().stream()
                .filter(FastFuel::isAcceleratorFuel)
                .mapToInt(FastFuel::acceleratorWorkPerTick)
                .distinct()
                .sorted()
                .boxed()
                .toList();
    }

    static IRecipeType<AcceleratorConversion> getAcceleratorRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public IRecipeType<AcceleratorConversion> getRecipeType() {
        return getAcceleratorRecipeType();
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
        return 72 + 11 * this.boostSpeeds.size();
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AcceleratorConversion recipe, IFocusGroup focuses) {
        builder.addInputSlot(20, 16)
                .setStandardSlotBackground()
                .addItemStacks(recipe.inputItems());
        builder.addOutputSlot(120, 16)
                .setStandardSlotBackground()
                .addItemStacks(List.of(recipe.output()));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, AcceleratorConversion recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(recipe.workUnits()).setPosition(60, 17);
        builder.addText(Component.translatable(
                        "jei.fastfurnacev2.accelerator_duration", recipe.workUnits()), WIDTH, 12)
                .setPosition(0, 42)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080);
        for (int index = 0; index < this.boostSpeeds.size(); index++) {
            int workPerTick = this.boostSpeeds.get(index);
            int boostedTicks = (recipe.workUnits() + workPerTick - 1) / workPerTick;
            builder.addText(Component.translatable(
                            "jei.fastfurnacev2.accelerator_boosted_duration", workPerTick, boostedTicks), WIDTH, 12)
                    .setPosition(0, 53 + 11 * index)
                    .setTextAlignment(HorizontalAlignment.CENTER)
                    .setColor(0xFF808080);
        }
        builder.addText(Component.translatable("jei.fastfurnacev2.accelerator_lava"), WIDTH, 12)
                .setPosition(0, 58 + 11 * this.boostSpeeds.size())
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080);
    }
}
