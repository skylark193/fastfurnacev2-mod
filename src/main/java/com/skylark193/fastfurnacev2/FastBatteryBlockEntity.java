package com.skylark193.fastfurnacev2;

import com.skylark193.fastfurnacev2.api.FastEnergyStorage;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jspecify.annotations.Nullable;

public class FastBatteryBlockEntity extends BaseContainerBlockEntity {
    private final FastFurnaceTier tier;
    private final FastEnergyStorage energy;
    private final EnergySideConfiguration sideConfiguration = new EnergySideConfiguration(EnergySideMode.BOTH);
    private final Map<Direction, EnergyHandler> sideHandlers = new EnumMap<>(Direction.class);
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.getAmountAsInt() & 0xFFFF;
                case 1 -> energy.getAmountAsInt() >>> 16;
                case 2 -> energy.getCapacityAsInt() & 0xFFFF;
                case 3 -> energy.getCapacityAsInt() >>> 16;
                case 4, 5, 6, 7, 8, 9 ->
                        sideConfiguration.get(Direction.values()[index - 4]).ordinal();
                case 10 -> tier.ordinal();
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
        this.energy = new FastEnergyStorage(capacity, this.tier.getBatteryTransferLimit(), this::onEnergyChanged);
        for (Direction direction : Direction.values()) {
            this.sideHandlers.put(direction, new EnergySideHandler(this.energy, this.sideConfiguration, direction));
        }
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

    public FastFurnaceTier getTier() {
        return this.tier;
    }

    public FastEnergyStorage energy() {
        return this.energy;
    }

    public EnergyHandler energyForSide(@Nullable Direction direction) {
        return direction == null ? this.energy : this.sideHandlers.get(direction);
    }

    public EnergySideMode getEnergySideMode(Direction direction) {
        return this.sideConfiguration.get(direction);
    }

    public void cycleEnergySide(Direction direction) {
        this.sideConfiguration.cycle(direction);
        this.setChanged();
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
        return new FastBatteryMenu(containerId, inventory, this, this.dataAccess, this.worldPosition);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.worldPosition);
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        int stored = components.getOrDefault(FastFurnaceV2.STORED_ENERGY.get(), 0);
        this.energy.set(Math.min(stored, this.energy.getCapacityAsInt()));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(FastFurnaceV2.STORED_ENERGY.get(), this.energy.getAmountAsInt());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.energy.deserialize(input.childOrEmpty("energy"));
        this.sideConfiguration.deserialize(input.getIntOr("energy_side_modes", EnergySideConfiguration.ALL_BOTH));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.energy.serialize(output.child("energy"));
        output.putInt("energy_side_modes", this.sideConfiguration.serialize());
    }
}
