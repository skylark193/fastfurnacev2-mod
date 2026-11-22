package com.skylark193.fastfurnacev2;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
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
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class FastFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    public static final int VANILLA_COAL_BURN_TIME = 1600;
    public static final int VANILLA_COAL_BLOCK_BURN_TIME = 16000;
    public static final int FAST_CRUSHER_OUTPUT_COUNT = 2;

    private final FastFurnaceTier tier;
    private final boolean fastCrusher;
    private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck =
            RecipeManager.createCheck(RecipeType.SMELTING);
    private int currentBurnTime;
    private int totalFuelTime;
    private boolean fastCoalBurning;
    private boolean extremeCoalBurning;
    private boolean crusherDoubleOutputPending;
    private boolean needsCrusherDoubleReset;
    private final SnapshotJournal<Boolean> crusherDoubleResetJournal = new SnapshotJournal<>() {
        @Override
        protected Boolean createSnapshot() {
            return needsCrusherDoubleReset;
        }

        @Override
        protected void revertToSnapshot(Boolean snapshot) {
            needsCrusherDoubleReset = snapshot;
        }

        @Override
        protected void onRootCommit(Boolean originalState) {
            if (needsCrusherDoubleReset) {
                crusherDoubleOutputPending = false;
                needsCrusherDoubleReset = false;
                setChanged();
            }
        }
    };

    // Split integers preserve exact fuel times; the last two values identify active fuel and crusher behavior.
    private final ContainerData menuDataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 4 -> currentBurnTime & 0xFFFF;
                case 5 -> currentBurnTime >>> 16;
                case 6 -> totalFuelTime & 0xFFFF;
                case 7 -> totalFuelTime >>> 16;
                case 8 -> extremeCoalBurning ? 2 : fastCoalBurning ? 1 : 0;
                case 9 -> fastCrusher ? 1 : 0;
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
                case 8 -> {
                    fastCoalBurning = value == 1;
                    extremeCoalBurning = value == 2;
                }
                case 9 -> {
                    // Crusher identity comes from the immutable block type, not client-supplied data.
                }
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
        this.tier = ((FastFurnaceBlock) state.getBlock()).getTier();
        this.fastCrusher = ((FastFurnaceBlock) state.getBlock()).isFastCrusher();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FastFurnaceMenu(containerId, inventory, this, this.menuDataAccess, this.tier);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.currentBurnTime = input.getIntOr("lit_time_remaining", 0);
        this.totalFuelTime = input.getIntOr("lit_total_time", 0);
        this.extremeCoalBurning = this.currentBurnTime > 0
                && input.getBooleanOr("extreme_coal_burning", false);
        this.fastCoalBurning = this.currentBurnTime > 0 && !this.extremeCoalBurning
                && input.getBooleanOr("fast_coal_burning", false);
        this.crusherDoubleOutputPending = this.fastCrusher
                && input.getBooleanOr("fast_crusher_double_output_pending", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("lit_time_remaining", this.currentBurnTime);
        output.putInt("lit_total_time", this.totalFuelTime);
        output.putBoolean("fast_coal_burning", this.fastCoalBurning);
        output.putBoolean("extreme_coal_burning", this.extremeCoalBurning);
        output.putBoolean("fast_crusher_double_output_pending", this.crusherDoubleOutputPending);
    }

    @Override
    protected int getBurnDuration(FuelValues fuelValues, ItemStack fuel) {
        int burnTime = super.getBurnDuration(fuelValues, fuel);
        if (burnTime > 0 && FastFurnaceV2.isExtremeCoalFuel(fuel)) {
            burnTime = this.tier.scaleExtremeCoalBurnTime(burnTime);
        } else if (burnTime > 0 && FastFurnaceV2.isFastCoalFuel(fuel)) {
            burnTime = this.tier.scaleFastCoalBurnTime(burnTime);
        }
        if (burnTime > 0
                && !this.fastCrusher
                && this.level != null
                && this.level.getRandom().nextFloat() < this.tier.getDoubleBurnChance()) {
            return (int) Math.min(Integer.MAX_VALUE, burnTime * 2L);
        }
        return burnTime;
    }

    @Override
    public void setItem(int slot, ItemStack itemStack, boolean insideTransaction) {
        ItemStack oldStack = this.getItem(slot);
        boolean same = !itemStack.isEmpty() && ItemStack.isSameItemSameComponents(oldStack, itemStack);
        super.setItem(slot, itemStack, insideTransaction);
        if (slot == SLOT_INPUT && !same && !insideTransaction) {
            this.crusherDoubleOutputPending = false;
            if (this.level instanceof ServerLevel serverLevel) {
                this.dataAccess.set(DATA_COOKING_TOTAL_TIME, this.getTotalCookTime(serverLevel));
                this.dataAccess.set(DATA_COOKING_PROGRESS, 0);
            }
        }
    }

    @Override
    public void onTransfer(int slot, int amountChange, TransactionContext transaction) {
        super.onTransfer(slot, amountChange, transaction);
        if (this.fastCrusher && slot == SLOT_INPUT) {
            int currentAmount = this.getItem(slot).getCount();
            if (currentAmount == 0 || currentAmount == amountChange) {
                // Item snapshots cannot restore custom pending state; defer its reset until the root commit.
                this.crusherDoubleResetJournal.updateSnapshots(transaction);
                this.needsCrusherDoubleReset = true;
            }
        }
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, FastFurnaceBlockEntity entity) {
        boolean changed = false;
        boolean wasLit = entity.currentBurnTime > 0;
        boolean isLit = wasLit;

        ItemStack fuel = entity.items.get(SLOT_FUEL);
        ItemStack ingredient = entity.items.get(SLOT_INPUT);
        boolean hasIngredient = !ingredient.isEmpty();
        boolean hasFuel = !fuel.isEmpty();
        if (isLit || hasFuel && hasIngredient) {
            if (hasIngredient) {
                SingleRecipeInput input = new SingleRecipeInput(ingredient);
                ProcessingRecipe processing = entity.getProcessingRecipe(ingredient, input, level);
                if (processing != null) {
                    int maxStackSize = entity.getMaxStackSize();
                    ItemStack burnResult = processing.result();
                    ItemStack capacityResult = entity.crusherDoubleOutputPending
                            ? doubleOutput(burnResult)
                            : burnResult;
                    boolean canProcess = !burnResult.isEmpty() && canBurn(entity.items, maxStackSize, capacityResult);
                    if (canProcess && wasLit) {
                        entity.currentBurnTime--;
                        isLit = entity.currentBurnTime > 0;
                        if (!isLit) {
                            entity.fastCoalBurning = false;
                            entity.extremeCoalBurning = false;
                        }
                        entity.syncBurnData();
                    }
                    int expectedCookTime = entity.scaleCookTime(processing.cookingTime(), entity.fastCoalBurning, entity.extremeCoalBurning);
                    entity.updateCookingTotal(expectedCookTime);
                    if (canProcess) {
                        if (!isLit) {
                            int newBurnTime = entity.getBurnDuration(level.fuelValues(), fuel);
                            entity.currentBurnTime = newBurnTime;
                            entity.totalFuelTime = newBurnTime;
                            entity.syncBurnData();
                            if (newBurnTime > 0) {
                                entity.extremeCoalBurning = FastFurnaceV2.isExtremeCoalFuel(fuel);
                                entity.fastCoalBurning = !entity.extremeCoalBurning && FastFurnaceV2.isFastCoalFuel(fuel);
                                expectedCookTime = entity.scaleCookTime(processing.cookingTime(), entity.fastCoalBurning, entity.extremeCoalBurning);
                                entity.updateCookingTotal(expectedCookTime);
                                consumeFuel(entity.items, fuel);
                                isLit = true;
                                changed = true;
                            }
                        }

                        if (isLit) {
                            int cookingTimer = entity.dataAccess.get(DATA_COOKING_PROGRESS) + 1;
                            if (cookingTimer >= entity.dataAccess.get(DATA_COOKING_TOTAL_TIME)) {
                                ItemStack recipeOutput = burnResult;
                                if (entity.fastCrusher) {
                                    recipeOutput = entity.crusherDoubleOutputPending
                                            ? doubleOutput(burnResult)
                                            : entity.rollCrusherOutput(level, burnResult);
                                }
                                if (canBurn(entity.items, maxStackSize, recipeOutput)) {
                                    entity.dataAccess.set(DATA_COOKING_PROGRESS, 0);
                                    entity.dataAccess.set(DATA_COOKING_TOTAL_TIME, expectedCookTime);
                                    burn(entity.items, ingredient, recipeOutput);
                                    if (processing.recipe() != null) {
                                        entity.setRecipeUsed(processing.recipe());
                                    }
                                    entity.crusherDoubleOutputPending = false;
                                    changed = true;
                                } else {
                                    entity.crusherDoubleOutputPending = true;
                                    entity.dataAccess.set(DATA_COOKING_PROGRESS, Math.max(0, expectedCookTime - 1));
                                    changed = true;
                                }
                            } else {
                                entity.dataAccess.set(DATA_COOKING_PROGRESS, cookingTimer);
                            }
                        }
                    } else if (!entity.crusherDoubleOutputPending) {
                        entity.dataAccess.set(DATA_COOKING_PROGRESS, 0);
                    }
                }
            } else {
                entity.dataAccess.set(DATA_COOKING_PROGRESS, 0);
            }
        } else if (!hasIngredient) {
            entity.dataAccess.set(DATA_COOKING_PROGRESS, 0);
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
        return scaleCookTime(this.getVanillaCookTime(input, level), this.fastCoalBurning, this.extremeCoalBurning);
    }

    private int getVanillaCookTime(SingleRecipeInput input, ServerLevel level) {
        return this.quickCheck.getRecipeFor(input, level)
                .map(recipe -> recipe.value().cookingTime())
                .orElse(BURN_TIME_STANDARD);
    }

    private ProcessingRecipe getProcessingRecipe(
            ItemStack ingredient, SingleRecipeInput input, ServerLevel level) {
        if (this.fastCrusher) {
            Item crushed = CrushedOres.forRawOre(BuiltInRegistries.ITEM.getKey(ingredient.getItem()));
            if (crushed == null) {
                return null;
            }
            // The raw smelting recipe supplies timing only; crushing always produces crushed ore instead.
            return new ProcessingRecipe(null, this.getVanillaCookTime(input, level),
                    new ItemStack(crushed, FAST_CRUSHER_OUTPUT_COUNT));
        }

        RecipeHolder<? extends AbstractCookingRecipe> recipe = this.quickCheck.getRecipeFor(input, level).orElse(null);
        return recipe == null ? null : new ProcessingRecipe(
                recipe, recipe.value().cookingTime(), recipe.value().assemble(input));
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

    private int scaleCookTime(int vanillaCookTime, boolean fastCoalActive, boolean extremeCoalActive) {
        return this.tier.scaleCookTime(vanillaCookTime, fastCoalActive, extremeCoalActive);
    }

    private ItemStack rollCrusherOutput(ServerLevel level, ItemStack burnResult) {
        return level.getRandom().nextFloat() < this.tier.getDoubleBurnChance()
                ? doubleOutput(burnResult)
                : burnResult;
    }

    private static ItemStack doubleOutput(ItemStack result) {
        ItemStack doubled = result.copy();
        doubled.setCount(result.getCount() * 2);
        return doubled;
    }

    private record ProcessingRecipe(
            RecipeHolder<? extends AbstractCookingRecipe> recipe, int cookingTime, ItemStack result) {
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
