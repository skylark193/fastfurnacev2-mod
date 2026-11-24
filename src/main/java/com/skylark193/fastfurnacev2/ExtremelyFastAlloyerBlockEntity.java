package com.skylark193.fastfurnacev2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

public class ExtremelyFastAlloyerBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int RECIPE_TICKS = 200;
    private static final int[] INPUT_SLOTS = {0, 1, 2};
    private static final int[] RESULT_SLOT = {ExtremelyFastAlloyerMenu.RESULT_SLOT};

    private NonNullList<ItemStack> items = NonNullList.withSize(ExtremelyFastAlloyerMenu.SLOT_COUNT, ItemStack.EMPTY);
    private int workProgress;
    private int recipeTicks;
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
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ExtremelyFastAlloyerBlockEntity.this.workProgress;
                case 1 -> ExtremelyFastAlloyerBlockEntity.this.recipeTicks;
                case 2 -> ExtremelyFastAlloyerBlockEntity.this.processing ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> ExtremelyFastAlloyerBlockEntity.this.workProgress = value;
                case 1 -> ExtremelyFastAlloyerBlockEntity.this.recipeTicks = value;
                case 2 -> ExtremelyFastAlloyerBlockEntity.this.processing = value != 0;
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return ExtremelyFastAlloyerMenu.DATA_COUNT;
        }
    };

    public ExtremelyFastAlloyerBlockEntity(BlockPos pos, BlockState state) {
        super(FastFurnaceV2.EXTREMELY_FAST_ALLOYER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(
            ServerLevel level, BlockPos pos, BlockState state, ExtremelyFastAlloyerBlockEntity entity) {
        entity.processing = false;
        ExtremelyFastAlloyerRecipes.findMatch(entity.items).ifPresentOrElse(match -> {
            if (entity.recipeTicks != match.recipe().processingTicks()) {
                entity.workProgress = 0;
                entity.recipeTicks = match.recipe().processingTicks();
            }
            if (!entity.canFit(match.recipe().output())) {
                updateBlockState(level, pos, state, entity);
                return;
            }
            entity.processing = true;
            entity.workProgress++;
            if (entity.workProgress >= match.recipe().processingTicks()) {
                entity.addResult(match.recipe().output());
                match.consumeIngredients(entity.items);
                entity.workProgress = 0;
                if (ExtremelyFastAlloyerRecipes.findMatch(entity.items).isEmpty()) {
                    entity.recipeTicks = 0;
                }
            }
            updateBlockState(level, pos, state, entity);
        }, () -> {
            if (entity.workProgress != 0 || entity.recipeTicks != 0) {
                entity.workProgress = 0;
                entity.recipeTicks = 0;
                updateBlockState(level, pos, state, entity);
            } else if (state.getValue(ExtremelyFastAlloyerBlock.LIT)
                    || state.getValue(ExtremelyFastAlloyerBlock.BOLT_LEVEL) != 0) {
                updateBlockState(level, pos, state, entity);
            }
        });
    }

    private static void updateBlockState(
            ServerLevel level, BlockPos pos, BlockState state, ExtremelyFastAlloyerBlockEntity entity) {
        int boltLevel = entity.recipeTicks == 0
                ? 0
                : Math.min(ExtremelyFastAlloyerBlock.MAX_BOLT_LEVEL,
                        (entity.workProgress * ExtremelyFastAlloyerBlock.MAX_BOLT_LEVEL
                                + entity.recipeTicks - 1) / entity.recipeTicks);
        BlockState newState = state
                .setValue(ExtremelyFastAlloyerBlock.BOLT_LEVEL, boltLevel)
                .setValue(ExtremelyFastAlloyerBlock.LIT, entity.processing);
        if (!newState.equals(state)) {
            level.setBlock(pos, newState, 3);
        }
        entity.setChanged();
    }

    private boolean canFit(ItemStack result) {
        ItemStack output = this.items.get(ExtremelyFastAlloyerMenu.RESULT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount()
                        <= Math.min(output.getMaxStackSize(), result.getMaxStackSize());
    }

    private void addResult(ItemStack result) {
        ItemStack output = this.items.get(ExtremelyFastAlloyerMenu.RESULT_SLOT);
        if (output.isEmpty()) {
            this.items.set(ExtremelyFastAlloyerMenu.RESULT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.fastfurnacev2.extremely_fast_alloyer");
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
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ExtremelyFastAlloyerMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public void setItem(int slot, ItemStack stack, boolean insideTransaction) {
        ItemStack previous = this.items.get(slot);
        boolean sameInput = !stack.isEmpty() && ItemStack.isSameItemSameComponents(previous, stack);
        super.setItem(slot, stack, insideTransaction);
        if (slot < ExtremelyFastAlloyerMenu.INPUT_SLOT_COUNT && !sameInput && !insideTransaction) {
            this.resetProgressForInput();
        }
    }

    private void resetProgressForInput() {
        this.workProgress = 0;
        this.recipeTicks = ExtremelyFastAlloyerRecipes.findMatch(this.items)
                .map(match -> match.recipe().processingTicks())
                .orElse(0);
        this.setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
        this.workProgress = input.getIntOr("alloyer_progress", 0);
        this.recipeTicks = input.getIntOr("alloyer_recipe_ticks", 0);
        this.processing = false;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("alloyer_progress", this.workProgress);
        output.putInt("alloyer_recipe_ticks", this.recipeTicks);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return direction == Direction.DOWN ? RESULT_SLOT.clone() : INPUT_SLOTS.clone();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == ExtremelyFastAlloyerMenu.RESULT_SLOT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot >= 0 && slot < ExtremelyFastAlloyerMenu.INPUT_SLOT_COUNT
                && ExtremelyFastAlloyerRecipes.isInput(stack);
    }

    @Override
    public void onTransfer(int slot, int amountChange, TransactionContext transaction) {
        if (slot < ExtremelyFastAlloyerMenu.INPUT_SLOT_COUNT) {
            int currentAmount = this.getItem(slot).getCount();
            if (currentAmount == 0 || currentAmount == amountChange) {
                // Stack snapshots restore items after aborts; committed empty/fill transitions also reset progress.
                this.progressResetJournal.updateSnapshots(transaction);
                this.needsProgressReset = true;
            }
        }
    }
}
