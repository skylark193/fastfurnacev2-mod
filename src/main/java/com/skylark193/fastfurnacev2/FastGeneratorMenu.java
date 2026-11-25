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
import net.minecraft.world.item.crafting.RecipeType;

public class FastGeneratorMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = 1;
    public static final int DATA_COUNT = 7;
    private static final int INVENTORY_START = 1;
    private static final int INVENTORY_END = 28;
    private static final int HOTBAR_END = 37;
    private final Container container;
    private final ContainerData data;

    public FastGeneratorMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT));
    }

    public FastGeneratorMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(FastFurnaceV2.FAST_GENERATOR_MENU.get(), containerId);
        checkContainerSize(container, SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.addSlot(new Slot(container, 0, 80, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getBurnTime(RecipeType.SMELTING, inventory.player.level().fuelValues()) > 0;
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
            if (slotIndex == 0) {
                if (!this.moveItemStackTo(stack, INVENTORY_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 1, false)) {
                if (slotIndex < INVENTORY_END) {
                    if (!this.moveItemStackTo(stack, INVENTORY_END, HOTBAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false)) {
                    return ItemStack.EMPTY;
                }
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

    public int getEnergy() {
        return (this.data.get(1) << 16) | (this.data.get(0) & 0xFFFF);
    }

    public int getCapacity() {
        return (this.data.get(6) << 16) | (this.data.get(5) & 0xFFFF);
    }

    public int getBurnTime() {
        return this.data.get(2);
    }

    public int getTotalBurnTime() {
        return this.data.get(3);
    }

    public int getOutput() {
        return this.data.get(4);
    }

    public float getEnergyProgress() {
        int capacity = this.getCapacity();
        return capacity == 0 ? 0.0F : Mth.clamp(this.getEnergy() / (float) capacity, 0.0F, 1.0F);
    }

    public float getBurnProgress() {
        int total = this.getTotalBurnTime();
        return total == 0 ? 0.0F : Mth.clamp(this.getBurnTime() / (float) total, 0.0F, 1.0F);
    }
}
