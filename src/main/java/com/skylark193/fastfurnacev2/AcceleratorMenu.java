package com.skylark193.fastfurnacev2;

import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AcceleratorMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int DATA_COUNT = 7;
    private static final int INVENTORY_SLOT_START = 3;
    private static final int INVENTORY_SLOT_END = 30;
    private static final int HOTBAR_SLOT_START = 30;
    private static final int HOTBAR_SLOT_END = 39;

    private final Container container;
    private final ContainerData data;

    public AcceleratorMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT));
    }

    public AcceleratorMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(FastFurnaceV2.ACCELERATOR_MENU.get(), containerId);
        checkContainerSize(container, SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.addSlot(new Slot(container, INPUT_SLOT, 56, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AcceleratorBlockEntity.isAcceleratorInput(stack);
            }
        });
        this.addSlot(new Slot(container, FUEL_SLOT, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AcceleratorBlockEntity.isAcceleratorFuel(stack);
            }
        });
        this.addSlot(new Slot(container, RESULT_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addStandardInventorySlots(inventory, 8, 84);
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack clicked = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            clicked = stack.copy();
            if (slotIndex == RESULT_SLOT) {
                if (!this.moveItemStackTo(stack, INVENTORY_SLOT_START, HOTBAR_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, clicked);
            } else if (slotIndex >= INVENTORY_SLOT_START) {
                if (AcceleratorBlockEntity.isAcceleratorInput(stack)) {
                    if (!this.moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (AcceleratorBlockEntity.isAcceleratorFuel(stack)) {
                    if (!this.moveItemStackTo(stack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex < INVENTORY_SLOT_END) {
                    if (!this.moveItemStackTo(stack, HOTBAR_SLOT_START, HOTBAR_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex < HOTBAR_SLOT_END
                        && !this.moveItemStackTo(stack, INVENTORY_SLOT_START, INVENTORY_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, INVENTORY_SLOT_START, HOTBAR_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == clicked.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return clicked;
    }

    public float getProgress() {
        int total = this.getRecipeTicks();
        return total == 0 ? 0.0F : Mth.clamp(this.getWorkProgress() / (float) total, 0.0F, 1.0F);
    }

    public float getBoostProgress() {
        int total = this.getTotalBoostWork();
        return total == 0 ? 0.0F : Mth.clamp(this.getBoostWork() / (float) total, 0.0F, 1.0F);
    }

    public int getWorkProgress() {
        return this.data.get(0);
    }

    public int getRecipeTicks() {
        return this.data.get(1);
    }

    public int getBoostWork() {
        return this.data.get(2);
    }

    public int getTotalBoostWork() {
        return this.data.get(3);
    }

    public int getLavaSideCount() {
        return this.data.get(4);
    }

    public boolean isProcessing() {
        return this.data.get(5) != 0;
    }

    public boolean isBoosted() {
        return this.getBoostWork() > 0;
    }

    public boolean isExtremeBoosted() {
        return this.isBoosted() && this.data.get(6) != 0;
    }
}
