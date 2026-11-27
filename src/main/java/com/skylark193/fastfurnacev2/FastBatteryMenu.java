package com.skylark193.fastfurnacev2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class FastBatteryMenu extends AbstractContainerMenu implements EnergyConfigurableMenu {
    public static final int DATA_COUNT = 11;
    private final Container container;
    private final ContainerData data;
    private final BlockPos pos;

    public FastBatteryMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, new SimpleContainer(0), new SimpleContainerData(DATA_COUNT), pos);
    }

    public FastBatteryMenu(
            int containerId, Inventory inventory, Container container, ContainerData data, BlockPos pos) {
        super(FastFurnaceV2.FAST_BATTERY_MENU.get(), containerId);
        checkContainerSize(container, 0);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.pos = pos;
        this.addStandardInventorySlots(inventory, 8, 84);
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack clicked = stack.copy();
        int start = slotIndex < 27 ? 27 : 0;
        int end = slotIndex < 27 ? 36 : 27;
        if (!this.moveItemStackTo(stack, start, end, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return clicked;
    }

    public FastFurnaceTier getTier() {
        FastFurnaceTier[] tiers = FastFurnaceTier.values();
        return tiers[Mth.clamp(this.data.get(10), 0, tiers.length - 1)];
    }

    public int getEnergy() {
        return (this.data.get(1) << 16) | (this.data.get(0) & 0xFFFF);
    }

    public int getCapacity() {
        return (this.data.get(3) << 16) | (this.data.get(2) & 0xFFFF);
    }

    public float getEnergyProgress() {
        int capacity = this.getCapacity();
        return capacity == 0 ? 0.0F : Mth.clamp(this.getEnergy() / (float) capacity, 0.0F, 1.0F);
    }

    @Override
    public BlockPos getEnergyBlockPos() {
        return this.pos;
    }

    @Override
    public EnergySideMode getEnergySideMode(Direction direction) {
        return EnergySideMode.fromSerialized(this.data.get(4 + direction.ordinal()));
    }
}
