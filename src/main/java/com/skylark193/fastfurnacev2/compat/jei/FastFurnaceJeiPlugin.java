package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.AcceleratorBlockEntity;
import com.skylark193.fastfurnacev2.AcceleratorMenu;
import com.skylark193.fastfurnacev2.CrushedOres;
import com.skylark193.fastfurnacev2.FastFurnaceBlockEntity;
import com.skylark193.fastfurnacev2.FastFurnaceMenu;
import com.skylark193.fastfurnacev2.FastFurnaceTier;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
import com.skylark193.fastfurnacev2.client.AcceleratorScreen;
import com.skylark193.fastfurnacev2.client.DiamondFastFurnaceScreen;
import com.skylark193.fastfurnacev2.client.FastFurnaceScreen;
import com.skylark193.fastfurnacev2.client.GoldFastFurnaceScreen;
import com.skylark193.fastfurnacev2.client.IronFastFurnaceScreen;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        registration.addRecipeCategories(
                new FastCrusherCategory(registration.getJeiHelpers().getGuiHelper()),
                new AcceleratorCategory(registration.getJeiHelpers().getGuiHelper()),
                new AcceleratorFuelCategory(registration.getJeiHelpers().getGuiHelper()));
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
        registration.addIngredientInfo(
                FastFurnaceV2.EXTREME_COAL_ORE.get(),
                Component.translatable("jei.fastfurnacev2.extreme_coal_ore"));
        registration.addIngredientInfo(
                FastFurnaceV2.EXTREME_COAL.get(),
                Component.translatable("jei.fastfurnacev2.extreme_coal"));
        registration.addIngredientInfo(
                FastFurnaceV2.ACCELERATOR.get(),
                Component.translatable("jei.fastfurnacev2.accelerator_info"));
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            registration.addIngredientInfo(
                    FastFurnaceV2.getFurnaceBlock(tier).get(),
                    Component.translatable("jei.fastfurnacev2." + tier.getId()));
            registration.addIngredientInfo(
                    FastFurnaceV2.getCrusherBlock(tier).get(),
                    Component.translatable("jei.fastfurnacev2." + tier.getCrusherId()));
        }

        registration.addRecipes(FastCrusherCategory.getCrushingRecipeType(), createCrushingRecipes());
        registration.addRecipes(AcceleratorCategory.getAcceleratorRecipeType(), createAcceleratorRecipes());
        registration.addRecipes(AcceleratorFuelCategory.getAcceleratorFuelRecipeType(), createAcceleratorFuels());

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
            boolean extremeCoal = FastFurnaceV2.isExtremeCoalFuel(stack);
            boolean fastCoal = FastFurnaceV2.isFastCoalFuel(stack);
            for (FastFurnaceTier tier : FastFurnaceTier.values()) {
                int adjustedBurnTime = extremeCoal ? tier.scaleExtremeCoalBurnTime(burnTime)
                        : fastCoal ? tier.scaleFastCoalBurnTime(burnTime) : burnTime;
                fuelsByTier.get(tier).add(new FastFurnaceFuelRecipe(List.of(stack), adjustedBurnTime));
            }
        }
        for (Map.Entry<FastFurnaceTier, List<IJeiFuelingRecipe>> entry : fuelsByTier.entrySet()) {
            List<IJeiFuelingRecipe> fuels = entry.getValue();
            fuels.sort(Comparator.comparingInt(IJeiFuelingRecipe::getBurnTime));
            registration.addRecipes(FastFurnaceFuelCategory.getRecipeType(entry.getKey()), fuels);
        }
    }

    private static List<FastCrusherRecipe> createCrushingRecipes() {
        List<FastCrusherRecipe> recipes = new ArrayList<>();
        for (Identifier rawOreId : CrushedOres.rawOres()) {
            Item crushed = CrushedOres.forRawOre(rawOreId);
            if (crushed == null) {
                continue;
            }
            ItemStack input = new ItemStack(BuiltInRegistries.ITEM.getValue(rawOreId));
            recipes.add(new FastCrusherRecipe(
                    input,
                    new ItemStack(crushed, FastFurnaceBlockEntity.FAST_CRUSHER_OUTPUT_COUNT)));
        }
        return recipes;
    }

    private static List<AcceleratorRecipe> createAcceleratorRecipes() {
        return List.of(
                new AcceleratorRecipe(
                        new ItemStack(Items.COAL),
                        new ItemStack(FastFurnaceV2.FAST_COAL.get()),
                        AcceleratorBlockEntity.COAL_RECIPE_TICKS),
                new AcceleratorRecipe(
                        new ItemStack(Items.COAL_BLOCK),
                        new ItemStack(FastFurnaceV2.FAST_COAL_BLOCK.get()),
                        AcceleratorBlockEntity.COAL_BLOCK_RECIPE_TICKS));
    }

    private static List<AcceleratorFuelRecipe> createAcceleratorFuels() {
        ItemStack fastCoal = new ItemStack(FastFurnaceV2.FAST_COAL.get());
        ItemStack fastCoalBlock = new ItemStack(FastFurnaceV2.FAST_COAL_BLOCK.get());
        ItemStack extremeCoal = new ItemStack(FastFurnaceV2.EXTREME_COAL.get());
        return List.of(
                new AcceleratorFuelRecipe(fastCoal, AcceleratorBlockEntity.getBoostWork(fastCoal)),
                new AcceleratorFuelRecipe(fastCoalBlock, AcceleratorBlockEntity.getBoostWork(fastCoalBlock)),
                new AcceleratorFuelRecipe(extremeCoal, AcceleratorBlockEntity.getBoostWork(extremeCoal)));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(
                AcceleratorCategory.getAcceleratorRecipeType(), FastFurnaceV2.ACCELERATOR.get());
        registration.addCraftingStation(
                AcceleratorFuelCategory.getAcceleratorFuelRecipeType(), FastFurnaceV2.ACCELERATOR.get());
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            registration.addCraftingStation(RecipeTypes.SMELTING, FastFurnaceV2.getFurnaceBlock(tier).get());
            registration.addCraftingStation(
                    FastFurnaceFuelCategory.getRecipeType(tier), FastFurnaceV2.getFurnaceBlock(tier).get());
            registration.addCraftingStation(
                    FastCrusherCategory.getCrushingRecipeType(), FastFurnaceV2.getCrusherBlock(tier).get());
            registration.addCraftingStation(
                    FastFurnaceFuelCategory.getRecipeType(tier), FastFurnaceV2.getCrusherBlock(tier).get());
        }
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                AcceleratorMenu.class,
                FastFurnaceV2.ACCELERATOR_MENU.get(),
                AcceleratorCategory.getAcceleratorRecipeType(),
                AcceleratorMenu.INPUT_SLOT,
                1,
                AcceleratorMenu.SLOT_COUNT,
                36);
        registration.addRecipeTransferHandler(
                AcceleratorMenu.class,
                FastFurnaceV2.ACCELERATOR_MENU.get(),
                AcceleratorFuelCategory.getAcceleratorFuelRecipeType(),
                AcceleratorMenu.FUEL_SLOT,
                1,
                AcceleratorMenu.SLOT_COUNT,
                36);
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            registerSmeltingTransfer(registration, tier);
            registerFilteredTransfer(
                    registration,
                    tier,
                    FastCrusherCategory.getCrushingRecipeType(),
                    FastFurnaceMenu::isFastCrusher);
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
        registration.addGuiContainerHandler(AcceleratorScreen.class, new IGuiContainerHandler<AcceleratorScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(AcceleratorScreen screen) {
                return screen.getJeiExtraAreas();
            }

            @Override
            public List<IGuiClickableArea> getGuiClickableAreas(
                    AcceleratorScreen screen, double guiMouseX, double guiMouseY) {
                return List.of(
                        IGuiClickableArea.createBasic(
                                78, 32, 28, 23, AcceleratorCategory.getAcceleratorRecipeType()),
                        IGuiClickableArea.createBasic(
                                56, 36, 14, 14, AcceleratorFuelCategory.getAcceleratorFuelRecipeType()));
            }
        });
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            registerGuiHandlers(registration, getScreenClass(tier), FastFurnaceFuelCategory.getRecipeType(tier));
        }
    }

    private static void registerSmeltingTransfer(
            IRecipeTransferRegistration registration, FastFurnaceTier tier) {
        registerFilteredTransfer(
                registration, tier, RecipeTypes.SMELTING, menu -> !menu.isFastCrusher());
    }

    private static <R> void registerFilteredTransfer(
            IRecipeTransferRegistration registration,
            FastFurnaceTier tier,
            IRecipeType<R> recipeType,
            Predicate<FastFurnaceMenu> menuFilter) {
        IRecipeTransferInfo<FastFurnaceMenu, R> delegate =
                registration.getTransferHelper().createBasicRecipeTransferInfo(
                        FastFurnaceMenu.class,
                        FastFurnaceV2.getFurnaceMenu(tier).get(),
                        recipeType,
                        0,
                        1,
                        3,
                        36);
        registration.addRecipeTransferHandler(new IRecipeTransferInfo<FastFurnaceMenu, R>() {
            @Override
            public Class<? extends FastFurnaceMenu> getContainerClass() {
                return delegate.getContainerClass();
            }

            @Override
            public Optional<MenuType<FastFurnaceMenu>> getMenuType() {
                return delegate.getMenuType();
            }

            @Override
            public IRecipeType<R> getRecipeType() {
                return delegate.getRecipeType();
            }

            @Override
            public boolean canHandle(FastFurnaceMenu container, R recipe) {
                return menuFilter.test(container) && delegate.canHandle(container, recipe);
            }

            @Override
            public List<Slot> getRecipeSlots(FastFurnaceMenu container, R recipe) {
                return delegate.getRecipeSlots(container, recipe);
            }

            @Override
            public List<Slot> getInventorySlots(FastFurnaceMenu container, R recipe) {
                return delegate.getInventorySlots(container, recipe);
            }
        });
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
        registration.addGuiContainerHandler(screenClass, new IGuiContainerHandler<S>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(S screen) {
                return screen.getJeiExtraAreas();
            }

            @Override
            public List<IGuiClickableArea> getGuiClickableAreas(S screen, double guiMouseX, double guiMouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                if (screen.getMenu().isFastCrusher()) {
                    areas.add(IGuiClickableArea.createBasic(
                            78, 32, 28, 23, FastCrusherCategory.getCrushingRecipeType()));
                } else {
                    areas.add(IGuiClickableArea.createBasic(78, 32, 28, 23, RecipeTypes.SMELTING));
                }
                areas.add(IGuiClickableArea.createBasic(56, 36, 14, 14, fuelType));
                return areas;
            }
        });
    }
}
