package com.skylark193.fastfurnacev2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FastFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    public static final int SPEED_PERCENT = 150;
    public static final int DOUBLE_BURN_CHANCE_PERCENT = 10;
    private static final float DOUBLE_BURN_CHANCE = DOUBLE_BURN_CHANCE_PERCENT / 100.0F;
    private static final Component DEFAULT_NAME = Component.translatable("block.fastfurnacev2.fast_furnace");

    private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck =
            RecipeManager.createCheck(RecipeType.SMELTING);
    private int currentBurnTime;
    private int totalFuelTime;

    // The four vanilla values retain their normal behavior. Four extra split integers preserve exact fuel times.
    private final ContainerData menuDataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 4 -> currentBurnTime & 0xFFFF;
                case 5 -> currentBurnTime >>> 16;
                case 6 -> totalFuelTime & 0xFFFF;
                case 7 -> totalFuelTime >>> 16;
                default -> dataAccess.get(index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 4 -> currentBurnTime = (currentBurnTime & 0xFFFF0000) | (value & 0xFFFF);
                case 5 -> currentBurnTime = (currentBurnTime & 0xFFFF) | (value << 16);
                case 6 -> totalFuelTime = (totalFuelTime & 0xFFFF0000) | (value & 0xFFFF);
                case 7 -> totalFuelTime = (totalFuelTime & 0xFFFF) | (value << 16);
                default -> dataAccess.set(index, value);
            }
        }

        @Override
        public int getCount() {
            return FastFurnaceMenu.MENU_DATA_COUNT;
        }
    };

    public FastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(FastFurnaceV2.FAST_FURNACE_BLOCK_ENTITY.get(), pos, state, RecipeType.SMELTING);
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FastFurnaceMenu(containerId, inventory, this, this.menuDataAccess);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.currentBurnTime = input.getIntOr("lit_time_remaining", 0);
        this.totalFuelTime = input.getIntOr("lit_total_time", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("lit_time_remaining", this.currentBurnTime);
        output.putInt("lit_total_time", this.totalFuelTime);
    }

    @Override
    protected int getBurnDuration(FuelValues fuelValues, ItemStack fuel) {
        int burnTime = super.getBurnDuration(fuelValues, fuel);
        if (burnTime > 0 && this.level != null && this.level.getRandom().nextFloat() < DOUBLE_BURN_CHANCE) {
            return (int) Math.min(Integer.MAX_VALUE, burnTime * 2L);
        }
        return burnTime;
    }

    @Override
    public void setItem(int slot, ItemStack itemStack, boolean insideTransaction) {
        ItemStack oldStack = this.getItem(slot);
        boolean same = !itemStack.isEmpty() && ItemStack.isSameItemSameComponents(oldStack, itemStack);
        super.setItem(slot, itemStack, insideTransaction);
        if (slot == SLOT_INPUT && !same && !insideTransaction && this.level instanceof ServerLevel serverLevel) {
            this.dataAccess.set(DATA_COOKING_TOTAL_TIME, this.getTotalCookTime(serverLevel));
            this.dataAccess.set(DATA_COOKING_PROGRESS, 0);
        }
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, FastFurnaceBlockEntity entity) {
        boolean changed = false;
        boolean wasLit = entity.currentBurnTime > 0;
        boolean isLit = wasLit;
        if (wasLit) {
            entity.currentBurnTime--;
            isLit = entity.currentBurnTime > 0;
            entity.syncBurnData();
        }

        ItemStack fuel = entity.items.get(SLOT_FUEL);
        ItemStack ingredient = entity.items.get(SLOT_INPUT);
        boolean hasIngredient = !ingredient.isEmpty();
        boolean hasFuel = !fuel.isEmpty();
        if (isLit || hasFuel && hasIngredient) {
            if (hasIngredient) {
                SingleRecipeInput input = new SingleRecipeInput(ingredient);
                RecipeHolder<? extends AbstractCookingRecipe> recipe = entity.quickCheck.getRecipeFor(input, level).orElse(null);
                if (recipe != null) {
                    int expectedCookTime = scaleCookTime(recipe.value().cookingTime());
                    entity.updateCookingTotal(expectedCookTime);
                    int maxStackSize = entity.getMaxStackSize();
                    ItemStack burnResult = recipe.value().assemble(input);
                    if (!burnResult.isEmpty() && canBurn(entity.items, maxStackSize, burnResult)) {
                        if (!isLit) {
                            int newBurnTime = entity.getBurnDuration(level.fuelValues(), fuel);
                            entity.currentBurnTime = newBurnTime;
                            entity.totalFuelTime = newBurnTime;
                            entity.syncBurnData();
                            if (newBurnTime > 0) {
                                consumeFuel(entity.items, fuel);
                                isLit = true;
                                changed = true;
                            }
                        }

                        if (isLit) {
                            int cookingTimer = entity.dataAccess.get(DATA_COOKING_PROGRESS) + 1;
                            if (cookingTimer >= entity.dataAccess.get(DATA_COOKING_TOTAL_TIME)) {
                                entity.dataAccess.set(DATA_COOKING_PROGRESS, 0);
                                entity.dataAccess.set(DATA_COOKING_TOTAL_TIME, expectedCookTime);
                                burn(entity.items, ingredient, burnResult);
                                entity.setRecipeUsed(recipe);
                                changed = true;
                            } else {
                                entity.dataAccess.set(DATA_COOKING_PROGRESS, cookingTimer);
                            }
                        } else {
                            entity.dataAccess.set(DATA_COOKING_PROGRESS, 0);
                        }
                    } else {
                        entity.dataAccess.set(DATA_COOKING_PROGRESS, 0);
                    }
                }
            } else {
                entity.dataAccess.set(DATA_COOKING_PROGRESS, 0);
            }
        } else if (entity.dataAccess.get(DATA_COOKING_PROGRESS) > 0) {
            int cookingTimer = Mth.clamp(
                    entity.dataAccess.get(DATA_COOKING_PROGRESS) - BURN_COOL_SPEED,
                    0,
                    entity.dataAccess.get(DATA_COOKING_TOTAL_TIME));
            entity.dataAccess.set(DATA_COOKING_PROGRESS, cookingTimer);
        }

        if (wasLit != isLit) {
            changed = true;
            state = state.setValue(AbstractFurnaceBlock.LIT, isLit);
            level.setBlock(pos, state, 3);
        }

        if (changed) {
            setChanged(level, pos, state);
        }
    }

    private void syncBurnData() {
        this.dataAccess.set(DATA_LIT_TIME, this.currentBurnTime);
        this.dataAccess.set(DATA_LIT_DURATION, this.totalFuelTime);
    }

    private int getTotalCookTime(ServerLevel level) {
        SingleRecipeInput input = new SingleRecipeInput(this.getItem(SLOT_INPUT));
        int vanillaCookTime = this.quickCheck.getRecipeFor(input, level)
                .map(recipe -> recipe.value().cookingTime())
                .orElse(BURN_TIME_STANDARD);
        return scaleCookTime(vanillaCookTime);
    }

    private void updateCookingTotal(int expectedCookTime) {
        int currentTotal = this.dataAccess.get(DATA_COOKING_TOTAL_TIME);
        if (currentTotal != expectedCookTime) {
            int currentProgress = this.dataAccess.get(DATA_COOKING_PROGRESS);
            int scaledProgress = currentTotal > 0
                    ? (int) Math.min(expectedCookTime, (long) currentProgress * expectedCookTime / currentTotal)
                    : 0;
            this.dataAccess.set(DATA_COOKING_TOTAL_TIME, expectedCookTime);
            this.dataAccess.set(DATA_COOKING_PROGRESS, scaledProgress);
        }
    }

    private static int scaleCookTime(int vanillaCookTime) {
        return Math.max(1, (int) (vanillaCookTime * 2L / 3L));
    }

    private static void consumeFuel(NonNullList<ItemStack> items, ItemStack fuel) {
        ItemStackTemplate remainder = fuel.getCraftingRemainder();
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            items.set(SLOT_FUEL, remainder != null ? remainder.create() : ItemStack.EMPTY);
        }
    }

    private static boolean canBurn(NonNullList<ItemStack> items, int maxStackSize, ItemStack burnResult) {
        ItemStack resultStack = items.get(SLOT_RESULT);
        if (resultStack.isEmpty()) {
            return true;
        } else if (!ItemStack.isSameItemSameComponents(resultStack, burnResult)) {
            return false;
        } else {
            int resultCount = resultStack.getCount() + burnResult.count();
            int maxResultCount = Math.min(maxStackSize, burnResult.getMaxStackSize());
            return resultCount <= maxResultCount;
        }
    }

    private static void burn(NonNullList<ItemStack> items, ItemStack input, ItemStack result) {
        ItemStack resultStack = items.get(SLOT_RESULT);
        if (resultStack.isEmpty()) {
            items.set(SLOT_RESULT, result.copy());
        } else {
            resultStack.grow(result.getCount());
        }

        if (input.is(Items.WET_SPONGE) && !items.get(SLOT_FUEL).isEmpty() && items.get(SLOT_FUEL).is(Items.BUCKET)) {
            items.set(SLOT_FUEL, new ItemStack(Items.WATER_BUCKET));
        }
        input.shrink(1);
    }
}
