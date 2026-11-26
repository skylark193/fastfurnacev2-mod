package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.AcceleratorMenu;
import com.skylark193.fastfurnacev2.ExtremelyFastAlloyerMenu;
import com.skylark193.fastfurnacev2.FastFurnaceMenu;
import com.skylark193.fastfurnacev2.FastFurnaceTier;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
import com.skylark193.fastfurnacev2.FastGeneratorFuelLogic;
import com.skylark193.fastfurnacev2.FastGeneratorMenu;
import com.skylark193.fastfurnacev2.api.AcceleratorConversion;
import com.skylark193.fastfurnacev2.api.CrusherRecipe;
import com.skylark193.fastfurnacev2.api.FastFuel;
import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
import com.skylark193.fastfurnacev2.client.AcceleratorScreen;
import com.skylark193.fastfurnacev2.client.ExtremelyFastAlloyerScreen;
import com.skylark193.fastfurnacev2.client.FastFurnaceScreen;
import com.skylark193.fastfurnacev2.client.FastGeneratorScreen;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
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
        registration.addRecipeCategories(
                new FastCrusherCategory(registration.getJeiHelpers().getGuiHelper()),
                new AcceleratorCategory(registration.getJeiHelpers().getGuiHelper()),
                new AcceleratorFuelCategory(registration.getJeiHelpers().getGuiHelper()),
                new ExtremelyFastAlloyerCategory(registration.getJeiHelpers().getGuiHelper()));
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            registration.addRecipeCategories(
                    new FastFurnaceFuelCategory(registration.getJeiHelpers().getGuiHelper(), tier),
                    new FastGeneratorFuelCategory(registration.getJeiHelpers().getGuiHelper(), tier));
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
                FastFurnaceV2.EXTREME_COAL_BLOCK.get(),
                Component.translatable("jei.fastfurnacev2.extreme_coal_block"));
        registration.addIngredientInfo(
                FastFurnaceV2.DEEPSLATE_EXTREME_COAL_ORE.get(),
                Component.translatable("jei.fastfurnacev2.deepslate_extreme_coal_ore"));
        for (var ore : List.of(
                FastFurnaceV2.FAST_IRON_ORE.get(),
                FastFurnaceV2.FAST_GOLD_ORE.get(),
                FastFurnaceV2.FAST_DIAMOND_ORE.get(),
                FastFurnaceV2.DEEPSLATE_FAST_IRON_ORE.get(),
                FastFurnaceV2.DEEPSLATE_FAST_GOLD_ORE.get(),
                FastFurnaceV2.DEEPSLATE_FAST_DIAMOND_ORE.get())) {
            registration.addIngredientInfo(ore, Component.translatable("jei.fastfurnacev2.fast_ore"));
        }
        for (var tool : List.of(
                FastFurnaceV2.FAST_IRON_PICKAXE.get(),
                FastFurnaceV2.FAST_IRON_SHOVEL.get(),
                FastFurnaceV2.FAST_IRON_AXE.get())) {
            registration.addIngredientInfo(tool, Component.translatable("jei.fastfurnacev2.fast_iron_tool"));
        }
        registration.addIngredientInfo(
                FastFurnaceV2.FAST_IRON_SWORD.get(),
                Component.translatable("jei.fastfurnacev2.fast_iron_sword"));
        for (var tool : List.of(
                FastFurnaceV2.FAST_GOLD_PICKAXE.get(),
                FastFurnaceV2.FAST_GOLD_SHOVEL.get(),
                FastFurnaceV2.FAST_GOLD_AXE.get())) {
            registration.addIngredientInfo(tool, Component.translatable("jei.fastfurnacev2.fast_gold_tool"));
        }
        registration.addIngredientInfo(
                FastFurnaceV2.FAST_GOLD_SWORD.get(),
                Component.translatable("jei.fastfurnacev2.fast_gold_sword"));
        for (var tool : List.of(
                FastFurnaceV2.FAST_DIAMOND_PICKAXE.get(),
                FastFurnaceV2.FAST_DIAMOND_SHOVEL.get(),
                FastFurnaceV2.FAST_DIAMOND_AXE.get())) {
            registration.addIngredientInfo(tool, Component.translatable("jei.fastfurnacev2.fast_diamond_tool"));
        }
        registration.addIngredientInfo(
                FastFurnaceV2.FAST_DIAMOND_SWORD.get(),
                Component.translatable("jei.fastfurnacev2.fast_diamond_sword"));
        for (var tool : List.of(
                FastFurnaceV2.EXTREMELY_FAST_PICKAXE.get(),
                FastFurnaceV2.EXTREMELY_FAST_SHOVEL.get(),
                FastFurnaceV2.EXTREMELY_FAST_AXE.get())) {
            registration.addIngredientInfo(tool, Component.translatable("jei.fastfurnacev2.extremely_fast_tool"));
        }
        registration.addIngredientInfo(
                FastFurnaceV2.EXTREMELY_FAST_SWORD.get(),
                Component.translatable("jei.fastfurnacev2.extremely_fast_sword"));
        registration.addIngredientInfo(
                FastFurnaceV2.ACCELERATOR.get(),
                Component.translatable("jei.fastfurnacev2.accelerator_info"));
        registration.addIngredientInfo(
                FastFurnaceV2.EXTREMELY_FAST_ALLOYER.get(),
                Component.translatable("jei.fastfurnacev2.alloyer_info"));
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            registration.addIngredientInfo(
                    FastFurnaceV2.getFurnaceBlock(tier).get(),
                    Component.translatable("jei.fastfurnacev2." + tier.getId()));
            registration.addIngredientInfo(
                    FastFurnaceV2.getCrusherBlock(tier).get(),
                    Component.translatable("jei.fastfurnacev2." + tier.getCrusherId()));
            int generatorOutput = tier.getGeneratorOutput();
            registration.addIngredientInfo(
                    FastFurnaceV2.getGeneratorBlock(tier).get(),
                    Component.translatable(
                            "jei.fastfurnacev2.generator_info",
                            formatEnergy(generatorOutput),
                            formatEnergy(generatorOutput
                                    * (100 + FastFurnaceTier.FAST_COAL_SPEED_BONUS_PERCENT) / 100),
                            formatEnergy(generatorOutput
                                    * (100 + FastFurnaceTier.EXTREME_COAL_SPEED_BONUS_PERCENT) / 100),
                            formatEnergy(tier.getGeneratorBufferCapacity())));
            registration.addIngredientInfo(
                    FastFurnaceV2.getBatteryBlock(tier).get(),
                    Component.translatable(
                            "jei.fastfurnacev2.battery_info",
                            formatEnergy(tier.getBatteryCapacity()),
                            formatEnergy(tier.getBatteryTransferLimit())));
            registration.addIngredientInfo(
                    FastFurnaceV2.getConduitBlock(tier).get(),
                    Component.translatable(
                            "jei.fastfurnacev2.power_conduit_info",
                            formatEnergy(tier.getConduitTransferRate())));
        }

        registration.addRecipes(FastCrusherCategory.getCrushingRecipeType(), createCrushingRecipes());
        registration.addRecipes(AcceleratorCategory.getAcceleratorRecipeType(), createAcceleratorRecipes());
        registration.addRecipes(AcceleratorFuelCategory.getAcceleratorFuelRecipeType(), createAcceleratorFuels());
        registration.addRecipes(ExtremelyFastAlloyerCategory.getAlloyerRecipeType(), FastFurnaceApi.alloyerRecipes());

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        FuelValues fuelValues = minecraft.level.fuelValues();
        Map<FastFurnaceTier, List<IJeiFuelingRecipe>> fuelsByTier = new EnumMap<>(FastFurnaceTier.class);
        Map<FastFurnaceTier, List<FastGeneratorFuelRecipe>> generatorFuelsByTier =
                new EnumMap<>(FastFurnaceTier.class);
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            fuelsByTier.put(tier, new ArrayList<>());
            generatorFuelsByTier.put(tier, new ArrayList<>());
        }
        for (ItemStack stack : registration.getIngredientManager().getAllItemStacks()) {
            int burnTime = stack.getBurnTime(RecipeType.SMELTING, fuelValues);
            if (burnTime <= 0) {
                continue;
            }
            FastFuel definition = FastFurnaceApi.findFuel(stack).orElse(null);
            int generatorBurnTime = FastGeneratorFuelLogic.getBurnTime(burnTime, definition);
            for (FastFurnaceTier tier : FastFurnaceTier.values()) {
                int adjustedBurnTime = definition == null
                        ? burnTime
                        : definition.scaleBurnTime(tier, burnTime);
                if (adjustedBurnTime > 0) {
                    int speedBonus = definition == null ? 0 : definition.speedBonusPercent();
                    fuelsByTier.get(tier).add(new FastFurnaceFuelRecipe(
                            List.of(stack), adjustedBurnTime, speedBonus));
                }
                if (generatorBurnTime > 0) {
                    generatorFuelsByTier.get(tier).add(new FastGeneratorFuelRecipe(
                            List.of(stack), generatorBurnTime,
                            FastGeneratorFuelLogic.getOutput(tier, definition)));
                }
            }
        }
        for (Map.Entry<FastFurnaceTier, List<IJeiFuelingRecipe>> entry : fuelsByTier.entrySet()) {
            List<IJeiFuelingRecipe> fuels = entry.getValue();
            fuels.sort(Comparator.comparingInt(IJeiFuelingRecipe::getBurnTime));
            registration.addRecipes(FastFurnaceFuelCategory.getRecipeType(entry.getKey()), fuels);
        }
        for (Map.Entry<FastFurnaceTier, List<FastGeneratorFuelRecipe>> entry : generatorFuelsByTier.entrySet()) {
            List<FastGeneratorFuelRecipe> fuels = entry.getValue();
            fuels.sort(Comparator.comparingLong(FastGeneratorFuelRecipe::totalEnergy));
            registration.addRecipes(FastGeneratorFuelCategory.getRecipeType(entry.getKey()), fuels);
        }
    }

    private static List<CrusherRecipe> createCrushingRecipes() {
        return FastFurnaceApi.crusherRecipes();
    }

    private static List<AcceleratorConversion> createAcceleratorRecipes() {
        return FastFurnaceApi.acceleratorRecipes();
    }

    private static List<FastFuel> createAcceleratorFuels() {
        return FastFurnaceApi.fuels().stream().filter(FastFuel::isAcceleratorFuel).toList();
    }

    private static String formatEnergy(int amount) {
        return String.format(Locale.ROOT, "%,d", amount);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(
                AcceleratorCategory.getAcceleratorRecipeType(), FastFurnaceV2.ACCELERATOR.get());
        registration.addCraftingStation(
                AcceleratorFuelCategory.getAcceleratorFuelRecipeType(), FastFurnaceV2.ACCELERATOR.get());
        registration.addCraftingStation(
                ExtremelyFastAlloyerCategory.getAlloyerRecipeType(), FastFurnaceV2.EXTREMELY_FAST_ALLOYER.get());
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            registration.addCraftingStation(RecipeTypes.SMELTING, FastFurnaceV2.getFurnaceBlock(tier).get());
            registration.addCraftingStation(
                    FastFurnaceFuelCategory.getRecipeType(tier), FastFurnaceV2.getFurnaceBlock(tier).get());
            registration.addCraftingStation(
                    FastCrusherCategory.getCrushingRecipeType(), FastFurnaceV2.getCrusherBlock(tier).get());
            registration.addCraftingStation(
                    FastFurnaceFuelCategory.getRecipeType(tier), FastFurnaceV2.getCrusherBlock(tier).get());
            registration.addCraftingStation(
                    FastGeneratorFuelCategory.getRecipeType(tier), FastFurnaceV2.getGeneratorBlock(tier).get());
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
        registration.addRecipeTransferHandler(
                ExtremelyFastAlloyerMenu.class,
                FastFurnaceV2.EXTREMELY_FAST_ALLOYER_MENU.get(),
                ExtremelyFastAlloyerCategory.getAlloyerRecipeType(),
                ExtremelyFastAlloyerMenu.INPUT_SLOT,
                ExtremelyFastAlloyerMenu.INPUT_SLOT_COUNT,
                ExtremelyFastAlloyerMenu.SLOT_COUNT,
                36);
        registerFilteredTransfer(registration, RecipeTypes.SMELTING, menu -> !menu.isFastCrusher());
        registerFilteredTransfer(
                registration, FastCrusherCategory.getCrushingRecipeType(), FastFurnaceMenu::isFastCrusher);
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
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
        registration.addGuiContainerHandler(
                ExtremelyFastAlloyerScreen.class, new IGuiContainerHandler<ExtremelyFastAlloyerScreen>() {
                    @Override
                    public List<Rect2i> getGuiExtraAreas(ExtremelyFastAlloyerScreen screen) {
                        return screen.getJeiExtraAreas();
                    }

                    @Override
                    public List<IGuiClickableArea> getGuiClickableAreas(
                            ExtremelyFastAlloyerScreen screen, double guiMouseX, double guiMouseY) {
                        return List.of(IGuiClickableArea.createBasic(
                                78, 32, 28, 23, ExtremelyFastAlloyerCategory.getAlloyerRecipeType()));
                    }
                });
        registerFurnaceGuiHandler(registration);
        registration.addGuiContainerHandler(
                FastGeneratorScreen.class, new IGuiContainerHandler<FastGeneratorScreen>() {
                    @Override
                    public List<Rect2i> getGuiExtraAreas(FastGeneratorScreen screen) {
                        return screen.getJeiExtraAreas();
                    }

                    @Override
                    public List<IGuiClickableArea> getGuiClickableAreas(
                            FastGeneratorScreen screen, double guiMouseX, double guiMouseY) {
                        return List.of(IGuiClickableArea.createBasic(
                                FastGeneratorMenu.FUEL_SLOT_X, 36, 14, 14,
                                FastGeneratorFuelCategory.getRecipeType(screen.getMenu().getTier())));
                    }
                });
    }

    private static <R> void registerFilteredTransfer(
            IRecipeTransferRegistration registration,
            IRecipeType<R> recipeType,
            Predicate<FastFurnaceMenu> menuFilter) {
        IRecipeTransferInfo<FastFurnaceMenu, R> delegate =
                registration.getTransferHelper().createBasicRecipeTransferInfo(
                        FastFurnaceMenu.class,
                        null,
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

    private static void registerFurnaceGuiHandler(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(FastFurnaceScreen.class, new IGuiContainerHandler<FastFurnaceScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(FastFurnaceScreen screen) {
                return screen.getJeiExtraAreas();
            }

            @Override
            public List<IGuiClickableArea> getGuiClickableAreas(
                    FastFurnaceScreen screen, double guiMouseX, double guiMouseY) {
                List<IGuiClickableArea> areas = new ArrayList<>();
                if (screen.getMenu().isFastCrusher()) {
                    areas.add(IGuiClickableArea.createBasic(
                            78, 32, 28, 23, FastCrusherCategory.getCrushingRecipeType()));
                } else {
                    areas.add(IGuiClickableArea.createBasic(78, 32, 28, 23, RecipeTypes.SMELTING));
                }
                areas.add(IGuiClickableArea.createBasic(56, 36, 14, 14,
                        FastFurnaceFuelCategory.getRecipeType(screen.getMenu().getTier())));
                return areas;
            }
        });
    }
}
