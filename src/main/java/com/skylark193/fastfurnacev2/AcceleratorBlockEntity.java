package com.skylark193.fastfurnacev2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class AcceleratorBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int COAL_RECIPE_TICKS = 400;
    public static final int COAL_BLOCK_RECIPE_TICKS = 3600;
    public static final int FAST_COAL_BOOST_WORK = 3600;
    public static final int FAST_COAL_BLOCK_BOOST_WORK = 4000;
    public static final int BOOSTED_WORK_PER_TICK = 3;
    public static final int REQUIRED_LAVA_SIDES = 4;
    private static final int[] INPUT_SLOTS = {AcceleratorMenu.INPUT_SLOT};
    private static final int[] FUEL_SLOTS = {AcceleratorMenu.FUEL_SLOT};
    private static final int[] RESULT_SLOTS = {AcceleratorMenu.RESULT_SLOT};
    private static final ConversionRecipe COAL_RECIPE =
            new ConversionRecipe(FastFurnaceV2.FAST_COAL.get(), COAL_RECIPE_TICKS);
    private static final ConversionRecipe COAL_BLOCK_RECIPE =
            new ConversionRecipe(FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get(), COAL_BLOCK_RECIPE_TICKS);

    private NonNullList<ItemStack> items = NonNullList.withSize(AcceleratorMenu.SLOT_COUNT, ItemStack.EMPTY);
    private int workProgress;
    private int recipeTicks;
    private int boostWork;
    private int totalBoostWork;
    private int lavaSideCount;
    private boolean processing;

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
        return stack.is(Items.COAL) || stack.is(Items.COAL_BLOCK);
    }

    public static boolean isAcceleratorFuel(ItemStack stack) {
        return FastFurnaceV2.isFastCoalFuel(stack);
    }

    public static int getBoostWork(ItemStack fuel) {
        if (fuel.is(FastFurnaceV2.FAST_COAL.get())) {
            return FAST_COAL_BOOST_WORK;
        }
        if (fuel.is(FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get())) {
            return FAST_COAL_BLOCK_BOOST_WORK;
        }
        return 0;
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
    }

    @Override
    public void setItem(int slot, ItemStack stack, boolean insideTransaction) {
        ItemStack oldStack = this.getItem(slot);
        boolean sameInput = !stack.isEmpty() && ItemStack.isSameItemSameComponents(oldStack, stack);
        super.setItem(slot, stack, insideTransaction);
        if (slot == AcceleratorMenu.INPUT_SLOT && !sameInput) {
            this.workProgress = 0;
            ConversionRecipe recipe = getRecipe(stack);
            this.recipeTicks = recipe == null ? 0 : recipe.ticks();
        }
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
        ConversionRecipe recipe = getRecipe(input);
        if (recipe == null) {
            changed = entity.resetProgress() || changed;
            entity.recipeTicks = 0;
        } else {
            if (entity.recipeTicks != recipe.ticks()) {
                entity.recipeTicks = recipe.ticks();
                entity.workProgress = 0;
                changed = true;
            }
            if (entity.lavaSideCount == REQUIRED_LAVA_SIDES && canAcceptResult(entity.items, recipe.result())) {
                if (entity.boostWork == 0) {
                    ItemStack fuel = entity.getItem(AcceleratorMenu.FUEL_SLOT);
                    int fuelWork = getBoostWork(fuel);
                    if (fuelWork > 0) {
                        entity.boostWork = fuelWork;
                        entity.totalBoostWork = fuelWork;
                        fuel.shrink(1);
                        if (fuel.isEmpty()) {
                            entity.items.set(AcceleratorMenu.FUEL_SLOT, ItemStack.EMPTY);
                        }
                        changed = true;
                    }
                }

                int remainingRecipeWork = recipe.ticks() - entity.workProgress;
                int step = Math.min(
                        entity.boostWork > 0 ? BOOSTED_WORK_PER_TICK : 1,
                        remainingRecipeWork);
                if (entity.boostWork > 0) {
                    step = Math.min(step, entity.boostWork);
                    entity.boostWork -= step;
                }
                entity.workProgress += step;
                entity.processing = step > 0;
                changed = true;

                if (entity.workProgress >= recipe.ticks()) {
                    addResult(entity.items, recipe.result());
                    input.shrink(1);
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

    private static @Nullable ConversionRecipe getRecipe(ItemStack input) {
        if (input.is(Items.COAL)) {
            return COAL_RECIPE;
        }
        return input.is(Items.COAL_BLOCK) ? COAL_BLOCK_RECIPE : null;
    }

    private static boolean canAcceptResult(NonNullList<ItemStack> items, Item result) {
        ItemStack output = items.get(AcceleratorMenu.RESULT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, result.getDefaultInstance())
                && output.getCount() < output.getMaxStackSize();
    }

    private static void addResult(NonNullList<ItemStack> items, Item result) {
        ItemStack output = items.get(AcceleratorMenu.RESULT_SLOT);
        if (output.isEmpty()) {
            items.set(AcceleratorMenu.RESULT_SLOT, new ItemStack(result));
        } else {
            output.grow(1);
        }
    }

    private record ConversionRecipe(Item result, int ticks) {
    }
}
