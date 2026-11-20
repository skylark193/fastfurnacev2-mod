package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.FastFurnaceBlockEntity;
import com.skylark193.fastfurnacev2.FastFurnaceTier;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
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

final class FastCrusherCategory implements IRecipeCategory<FastCrusherRecipe> {
    private static final IRecipeType<FastCrusherRecipe> RECIPE_TYPE =
            IRecipeType.create(FastFurnaceV2.MODID, "crushing", FastCrusherRecipe.class);
    private static final int WIDTH = 150;
    private static final int HEIGHT = 56;
    private static final int STANDARD_COOK_TIME = FastFurnaceTier.FAST.scaleCookTime(200, false);

    private final Component title;
    private final IDrawable icon;

    FastCrusherCategory(IGuiHelper guiHelper) {
        this.title = Component.translatable("jei.fastfurnacev2.crushing");
        this.icon = guiHelper.createDrawableItemLike(FastFurnaceV2.FAST_CRUSHER.get());
    }

    static IRecipeType<FastCrusherRecipe> getCrushingRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public IRecipeType<FastCrusherRecipe> getRecipeType() {
        return getCrushingRecipeType();
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
    public void setRecipe(IRecipeLayoutBuilder builder, FastCrusherRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(20, 17)
                .setStandardSlotBackground()
                .addItemStacks(List.of(recipe.input()));
        builder.addOutputSlot(100, 17)
                .setStandardSlotBackground()
                .addItemStacks(List.of(recipe.output()));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, FastCrusherRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(STANDARD_COOK_TIME).setPosition(58, 18);
        builder.addText(Component.translatable(
                        "jei.fastfurnacev2.crushing_output_chance",
                        FastFurnaceBlockEntity.FAST_CRUSHER_DOUBLE_OUTPUT_CHANCE_PERCENT,
                        FastFurnaceBlockEntity.FAST_CRUSHER_OUTPUT_COUNT * 2),
                WIDTH, 10)
                .setPosition(0, 44)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080);
    }
}
