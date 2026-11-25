package com.skylark193.fastfurnacev2;

import com.skylark193.fastfurnacev2.api.FastEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FastBatteryBlockEntity extends BaseContainerBlockEntity {
    private final FastFurnaceTier tier;
    private final FastEnergyStorage energy;
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.getAmountAsInt() & 0xFFFF;
                case 1 -> energy.getAmountAsInt() >>> 16;
                case 2 -> energy.getCapacityAsInt() & 0xFFFF;
                case 3 -> energy.getCapacityAsInt() >>> 16;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return FastBatteryMenu.DATA_COUNT;
        }
    };

    public FastBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(FastFurnaceV2.FAST_BATTERY_BLOCK_ENTITY.get(), pos, state);
        this.tier = ((FastBatteryBlock) state.getBlock()).getTier();
        int capacity = this.tier.getBatteryCapacity();
        this.energy = new FastEnergyStorage(capacity, Math.max(1, capacity / 20), this::onEnergyChanged);
    }

    private void onEnergyChanged() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            int charge = FastBatteryBlock.chargeLevel(this.energy.getAmountAsInt(), this.energy.getCapacityAsInt());
            if (state.getValue(FastBatteryBlock.CHARGE_LEVEL) != charge) {
                this.level.setBlock(this.worldPosition, state.setValue(FastBatteryBlock.CHARGE_LEVEL, charge), 3);
            }
        }
    }

    public FastEnergyStorage energy() {
        return this.energy;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.fastfurnacev2." + this.tier.getBatteryId());
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return NonNullList.create();
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FastBatteryMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.energy.deserialize(input.childOrEmpty("energy"));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.energy.serialize(output.child("energy"));
    }
}
