package com.skylark193.fastfurnacev2;

import com.skylark193.fastfurnacev2.api.AcceleratorConversion;
import com.skylark193.fastfurnacev2.api.FastFuel;
import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class AcceleratorBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int ITEM_RECIPE_TICKS = 400;
    public static final int BLOCK_RECIPE_TICKS = 3600;
    public static final int FAST_COAL_BOOST_WORK = 3600;
    public static final int FAST_COAL_BLOCK_BOOST_WORK = 4000;
    public static final int EXTREME_COAL_BOOST_WORK = FAST_COAL_BOOST_WORK * 2;
    public static final int BOOSTED_WORK_PER_TICK = 3;
    public static final int EXTREME_COAL_BOOSTED_WORK_PER_TICK = BOOSTED_WORK_PER_TICK * 2 - 1;
    public static final int REQUIRED_LAVA_SIDES = 4;
    private static final int[] INPUT_SLOTS = {AcceleratorMenu.INPUT_SLOT};
    private static final int[] FUEL_SLOTS = {AcceleratorMenu.FUEL_SLOT};
    private static final int[] RESULT_SLOTS = {AcceleratorMenu.RESULT_SLOT};
    private NonNullList<ItemStack> items = NonNullList.withSize(AcceleratorMenu.SLOT_COUNT, ItemStack.EMPTY);
    private int workProgress;
    private int recipeTicks;
    private int boostWork;
    private int totalBoostWork;
    private FastFuel activeBoostFuel;
    private boolean extremeBoost;
    private int boostWorkPerTick = 1;
    private int lavaSideCount;
    private boolean processing;
    private boolean needsProgressReset;
    private final SnapshotJournal<Boolean> progressResetJournal = new SnapshotJournal<>() {
        @Override
        protected Boolean createSnapshot() {
            return needsProgressReset;
        }

        @Override
        protected void revertToSnapshot(Boolean snapshot) {
            needsProgressReset = snapshot;
        }

        @Override
        protected void onRootCommit(Boolean originalState) {
            if (needsProgressReset) {
                resetProgressForInput();
                needsProgressReset = false;
                setChanged();
            }
        }
    };

    private final ContainerData menuDataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> workProgress;
                case 1 -> recipeTicks;
                case 2 -> boostWork;
                case 3 -> totalBoostWork;
                case 4 -> lavaSideCount;
                case 5 -> processing ? 1 : 0;
                case 6 -> extremeBoost ? 1 : 0;
                case 7 -> boostWorkPerTick;
                default -> throw new IllegalArgumentException("Invalid Accelerator data index: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> workProgress = value;
                case 1 -> recipeTicks = value;
                case 2 -> boostWork = value;
                case 3 -> totalBoostWork = value;
                case 4 -> lavaSideCount = value;
                case 5 -> processing = value != 0;
                case 6 -> extremeBoost = value != 0;
                case 7 -> boostWorkPerTick = value;
                default -> throw new IllegalArgumentException("Invalid Accelerator data index: " + index);
            }
        }

        @Override
        public int getCount() {
            return AcceleratorMenu.DATA_COUNT;
        }
    };

    public AcceleratorBlockEntity(BlockPos pos, BlockState state) {
        super(FastFurnaceV2.ACCELERATOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static boolean isAcceleratorInput(ItemStack stack) {
        return FastFurnaceApi.isAcceleratorInput(stack);
    }

    public static boolean isAcceleratorFuel(ItemStack stack) {
        return FastFurnaceApi.findFuel(stack).filter(FastFuel::isAcceleratorFuel).isPresent();
    }

    public static int getBoostWork(ItemStack fuel) {
        return FastFurnaceApi.findFuel(fuel).map(FastFuel::acceleratorWork).orElse(0);
    }

    public static int getBoostWorkPerTick(ItemStack fuel) {
        return FastFurnaceApi.findFuel(fuel).map(FastFuel::acceleratorWorkPerTick).orElse(0);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(FastFurnaceV2.ACCELERATOR.get().getDescriptionId());
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new AcceleratorMenu(containerId, inventory, this, this.menuDataAccess);
    }

    @Override
    public int getContainerSize() {
        return AcceleratorMenu.SLOT_COUNT;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
        this.workProgress = input.getIntOr("accelerator_progress", 0);
        this.recipeTicks = input.getIntOr("accelerator_recipe_ticks", 0);
        this.boostWork = input.getIntOr("accelerator_boost_work", 0);
        this.totalBoostWork = input.getIntOr("accelerator_total_boost_work", 0);
        FastFuel loadedBoost = null;
        Identifier boostId = Identifier.tryParse(input.getStringOr("accelerator_boost_fuel", ""));
        if (boostId != null) {
            loadedBoost = FastFurnaceApi.findFuel(boostId).filter(FastFuel::isAcceleratorFuel).orElse(null);
        } else {
            // Old saves recorded only whether the active built-in boost was Extreme Coal.
            Identifier legacyId = input.getBooleanOr("accelerator_extreme_boost", false)
                    ? FastFurnaceApi.EXTREME_COAL_FUEL_ID
                    : FastFurnaceApi.FAST_COAL_FUEL_ID;
            loadedBoost = FastFurnaceApi.findFuel(legacyId).orElse(null);
        }
        this.setActiveBoost(this.boostWork > 0 ? loadedBoost : null);
        this.lavaSideCount = 0;
        this.processing = false;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("accelerator_progress", this.workProgress);
        output.putInt("accelerator_recipe_ticks", this.recipeTicks);
        output.putInt("accelerator_boost_work", this.boostWork);
        output.putInt("accelerator_total_boost_work", this.totalBoostWork);
        if (this.activeBoostFuel != null) {
            output.putString("accelerator_boost_fuel", this.activeBoostFuel.id().toString());
        }
        output.putBoolean("accelerator_extreme_boost", this.extremeBoost);
    }

    @Override
    public void setItem(int slot, ItemStack stack, boolean insideTransaction) {
        ItemStack oldStack = this.getItem(slot);
        boolean sameInput = !stack.isEmpty() && ItemStack.isSameItemSameComponents(oldStack, stack);
        super.setItem(slot, stack, insideTransaction);
        if (slot == AcceleratorMenu.INPUT_SLOT && !sameInput && !insideTransaction) {
            this.resetProgressForInput();
        }
    }

    @Override
    public void onTransfer(int slot, int amountChange, TransactionContext transaction) {
        if (slot == AcceleratorMenu.INPUT_SLOT) {
            int currentAmount = this.getItem(slot).getCount();
            if (currentAmount == 0 || currentAmount == amountChange) {
                // Preserve progress when an item transaction aborts, but reset it after a committed empty/fill transition.
                this.progressResetJournal.updateSnapshots(transaction);
                this.needsProgressReset = true;
            }
        }
    }

    private void resetProgressForInput() {
        this.workProgress = 0;
        AcceleratorConversion recipe = getRecipe(this.getItem(AcceleratorMenu.INPUT_SLOT));
        this.recipeTicks = recipe == null ? 0 : recipe.workUnits();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case AcceleratorMenu.INPUT_SLOT -> isAcceleratorInput(stack);
            case AcceleratorMenu.FUEL_SLOT -> isAcceleratorFuel(stack);
            default -> false;
        };
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.UP) {
            return INPUT_SLOTS;
        }
        return direction == Direction.DOWN ? RESULT_SLOTS : FUEL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == AcceleratorMenu.RESULT_SLOT;
    }

    public static void serverTick(
            ServerLevel level, BlockPos pos, BlockState state, AcceleratorBlockEntity entity) {
        boolean changed = false;
        entity.lavaSideCount = countLavaSides(level, pos);
        entity.processing = false;

        ItemStack input = entity.getItem(AcceleratorMenu.INPUT_SLOT);
        AcceleratorConversion recipe = getRecipe(input);
        if (recipe == null) {
            changed = entity.resetProgress() || changed;
            entity.recipeTicks = 0;
        } else {
            if (entity.recipeTicks != recipe.workUnits()) {
                entity.recipeTicks = recipe.workUnits();
                entity.workProgress = 0;
                changed = true;
            }
            if (entity.lavaSideCount == REQUIRED_LAVA_SIDES && canAcceptResult(entity.items, recipe.output())) {
                if (entity.boostWork == 0) {
                    entity.setActiveBoost(null);
                    ItemStack fuel = entity.getItem(AcceleratorMenu.FUEL_SLOT);
                    int fuelWork = getBoostWork(fuel);
                    if (fuelWork > 0) {
                        entity.boostWork = fuelWork;
                        entity.totalBoostWork = fuelWork;
                        entity.setActiveBoost(FastFurnaceApi.findFuel(fuel).orElse(null));
                        fuel.shrink(1);
                        if (fuel.isEmpty()) {
                            entity.items.set(AcceleratorMenu.FUEL_SLOT, ItemStack.EMPTY);
                        }
                        changed = true;
                    }
                }

                int remainingRecipeWork = recipe.workUnits() - entity.workProgress;
                int step = Math.min(
                        entity.boostWork > 0 ? entity.boostWorkPerTick : 1,
                        remainingRecipeWork);
                if (entity.boostWork > 0) {
                    step = Math.min(step, entity.boostWork);
                    entity.boostWork -= step;
                    if (entity.boostWork == 0) {
                        entity.setActiveBoost(null);
                    }
                }
                entity.workProgress += step;
                entity.processing = step > 0;
                changed = true;

                if (entity.workProgress >= recipe.workUnits()) {
                    addResult(entity.items, recipe.output());
                    input.shrink(recipe.inputCount());
                    entity.workProgress = 0;
                    if (input.isEmpty()) {
                        entity.recipeTicks = 0;
                    }
                }
            }
        }

        int boltLevel = entity.recipeTicks == 0
                ? 0
                : Math.min(AcceleratorBlock.MAX_BOLT_LEVEL,
                        (entity.workProgress * AcceleratorBlock.MAX_BOLT_LEVEL
                                + entity.recipeTicks - 1) / entity.recipeTicks);
        BlockState newState = state
                .setValue(AcceleratorBlock.BOLT_LEVEL, boltLevel)
                .setValue(AcceleratorBlock.LIT, entity.processing);
        if (!newState.equals(state)) {
            level.setBlock(pos, newState, 3);
            state = newState;
        }
        if (changed) {
            setChanged(level, pos, state);
        }
    }

    private static int countLavaSides(ServerLevel level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.LAVA)) {
                count++;
            }
        }
        return count;
    }

    private boolean resetProgress() {
        boolean changed = this.workProgress != 0 || this.recipeTicks != 0;
        this.workProgress = 0;
        this.recipeTicks = 0;
        return changed;
    }

    private static @Nullable AcceleratorConversion getRecipe(ItemStack input) {
        return FastFurnaceApi.findAcceleratorRecipe(input).orElse(null);
    }

    private static boolean canAcceptResult(NonNullList<ItemStack> items, ItemStack result) {
        ItemStack output = items.get(AcceleratorMenu.RESULT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= Math.min(output.getMaxStackSize(), result.getMaxStackSize());
    }

    private static void addResult(NonNullList<ItemStack> items, ItemStack result) {
        ItemStack output = items.get(AcceleratorMenu.RESULT_SLOT);
        if (output.isEmpty()) {
            items.set(AcceleratorMenu.RESULT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }
    }

    private void setActiveBoost(FastFuel fuel) {
        this.activeBoostFuel = fuel;
        this.extremeBoost = fuel != null && fuel.id().equals(FastFurnaceApi.EXTREME_COAL_FUEL_ID);
        this.boostWorkPerTick = fuel == null ? 1 : fuel.acceleratorWorkPerTick();
    }
}
