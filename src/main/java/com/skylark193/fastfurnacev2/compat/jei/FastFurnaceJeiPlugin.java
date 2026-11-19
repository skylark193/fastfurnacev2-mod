package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.FastFurnaceMenu;
import com.skylark193.fastfurnacev2.FastFurnaceTier;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
import com.skylark193.fastfurnacev2.client.DiamondFastFurnaceScreen;
import com.skylark193.fastfurnacev2.client.FastFurnaceScreen;
import com.skylark193.fastfurnacev2.client.GoldFastFurnaceScreen;
import com.skylark193.fastfurnacev2.client.IronFastFurnaceScreen;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;

@JeiPlugin
public class FastFurnaceJeiPlugin implements IModPlugin {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            registration.addRecipeCategories(
                    new FastFurnaceFuelCategory(registration.getJeiHelpers().getGuiHelper(), tier));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
                FastFurnaceV2.FAST_COBBLESTONE.get(),
                Component.translatable("jei.fastfurnacev2.fast_cobblestone"));
        registration.addIngredientInfo(
                FastFurnaceV2.FAST_COAL.get(),
                Component.translatable("jei.fastfurnacev2.fast_coal"));
        registration.addIngredientInfo(
                FastFurnaceV2.FAST_COAL_BLOCK.get(),
                Component.translatable("jei.fastfurnacev2.fast_coal_block"));
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            registration.addIngredientInfo(
                    FastFurnaceV2.getFurnaceBlock(tier).get(),
                    Component.translatable("jei.fastfurnacev2." + tier.getId()));
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        FuelValues fuelValues = minecraft.level.fuelValues();
        Map<FastFurnaceTier, List<IJeiFuelingRecipe>> fuelsByTier = new EnumMap<>(FastFurnaceTier.class);
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            fuelsByTier.put(tier, new ArrayList<>());
        }
        for (ItemStack stack : registration.getIngredientManager().getAllItemStacks()) {
            int burnTime = stack.getBurnTime(RecipeType.SMELTING, fuelValues);
            if (burnTime <= 0) {
                continue;
            }
            boolean fastCoal = FastFurnaceV2.isFastCoalFuel(stack);
            for (FastFurnaceTier tier : FastFurnaceTier.values()) {
                int adjustedBurnTime = fastCoal ? tier.scaleFastCoalBurnTime(burnTime) : burnTime;
                fuelsByTier.get(tier).add(new FastFurnaceFuelRecipe(List.of(stack), adjustedBurnTime));
            }
        }
        for (Map.Entry<FastFurnaceTier, List<IJeiFuelingRecipe>> entry : fuelsByTier.entrySet()) {
            List<IJeiFuelingRecipe> fuels = entry.getValue();
            fuels.sort(Comparator.comparingInt(IJeiFuelingRecipe::getBurnTime));
            registration.addRecipes(FastFurnaceFuelCategory.getRecipeType(entry.getKey()), fuels);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            registration.addCraftingStation(RecipeTypes.SMELTING, FastFurnaceV2.getFurnaceBlock(tier).get());
            registration.addCraftingStation(
                    FastFurnaceFuelCategory.getRecipeType(tier), FastFurnaceV2.getFurnaceBlock(tier).get());
        }
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            registration.addRecipeTransferHandler(
                    FastFurnaceMenu.class,
                    FastFurnaceV2.getFurnaceMenu(tier).get(),
                    RecipeTypes.SMELTING,
                    0,
                    1,
                    3,
                    36);
            registration.addRecipeTransferHandler(
                    FastFurnaceMenu.class,
                    FastFurnaceV2.getFurnaceMenu(tier).get(),
                    FastFurnaceFuelCategory.getRecipeType(tier),
                    1,
                    1,
                    3,
                    36);
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            registerGuiHandlers(registration, getScreenClass(tier), FastFurnaceFuelCategory.getRecipeType(tier));
        }
    }

    private static Class<? extends FastFurnaceScreen> getScreenClass(FastFurnaceTier tier) {
        return switch (tier) {
            case FAST -> FastFurnaceScreen.class;
            case IRON -> IronFastFurnaceScreen.class;
            case GOLD -> GoldFastFurnaceScreen.class;
            case DIAMOND -> DiamondFastFurnaceScreen.class;
        };
    }

    private static <S extends FastFurnaceScreen> void registerGuiHandlers(
            IGuiHandlerRegistration registration,
            Class<S> screenClass,
            IRecipeType<IJeiFuelingRecipe> fuelType) {
        registration.addRecipeClickArea(screenClass, 78, 32, 28, 23, RecipeTypes.SMELTING);
        registration.addRecipeClickArea(screenClass, 56, 36, 14, 14, fuelType);
        registration.addGuiContainerHandler(screenClass, new IGuiContainerHandler<S>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(S screen) {
                return screen.getJeiExtraAreas();
            }
        });
    }
}
