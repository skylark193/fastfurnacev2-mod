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

public class ExtremelyFastAlloyerMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT = 0;
    public static final int INPUT_SLOT_COUNT = 3;
    public static final int FUEL_SLOT = 3;
    public static final int RESULT_SLOT = 4;
    public static final int SLOT_COUNT = 5;
    public static final int DATA_COUNT = 6;
    private static final int INVENTORY_SLOT_START = SLOT_COUNT;
    private static final int INVENTORY_SLOT_END = INVENTORY_SLOT_START + 27;
    private static final int HOTBAR_SLOT_START = INVENTORY_SLOT_END;
    private static final int HOTBAR_SLOT_END = HOTBAR_SLOT_START + 9;
    private static final int[] INPUT_X = {38, 56, 47};
    private static final int[] INPUT_Y = {17, 17, 35};

    private final Container container;
    private final ContainerData data;

    public ExtremelyFastAlloyerMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT));
    }

    public ExtremelyFastAlloyerMenu(
            int containerId, Inventory inventory, Container container, ContainerData data) {
        super(FastFurnaceV2.EXTREMELY_FAST_ALLOYER_MENU.get(), containerId);
        checkContainerSize(container, SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        for (int slot = 0; slot < INPUT_SLOT_COUNT; slot++) {
            this.addSlot(new Slot(container, slot, INPUT_X[slot], INPUT_Y[slot]) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return ExtremelyFastAlloyerRecipes.isInput(stack);
                }
            });
        }
        this.addSlot(new Slot(container, FUEL_SLOT, 47, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ExtremelyFastAlloyerBlockEntity.isExtremeCoal(stack);
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

    public static int getInputX(int slot) {
        return INPUT_X[slot];
    }

    public static int getInputY(int slot) {
        return INPUT_Y[slot];
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
                if (ExtremelyFastAlloyerRecipes.isInput(stack)) {
                    if (!this.moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (ExtremelyFastAlloyerBlockEntity.isExtremeCoal(stack)) {
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

    public float getBurnProgress() {
        int total = this.getTotalBurnTime();
        return total == 0 ? 0.0F : Mth.clamp(this.getBurnTimeRemaining() / (float) total, 0.0F, 1.0F);
    }

    public int getWorkProgress() {
        return this.data.get(0);
    }

    public int getRecipeTicks() {
        return this.data.get(1);
    }

    public boolean isProcessing() {
        return this.data.get(2) != 0;
    }

    public int getBurnTimeRemaining() {
        return this.data.get(3);
    }

    public int getTotalBurnTime() {
        return this.data.get(4);
    }

    public int getLavaSides() {
        return this.data.get(5);
    }
}
