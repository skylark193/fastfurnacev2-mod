package com.skylark193.fastfurnacev2;

import com.skylark193.fastfurnacev2.api.FastEnergyStorage;
import com.skylark193.fastfurnacev2.api.FastFuel;
import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public class FastGeneratorBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private static final int[] FUEL_SLOT = {0};
    private final FastFurnaceTier tier;
    private final FastEnergyStorage energy;
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private int burnTime;
    private int totalBurnTime;
    private int outputPercent = 100;
    private @Nullable Identifier activeFuelId;
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.getAmountAsInt() & 0xFFFF;
                case 1 -> energy.getAmountAsInt() >>> 16;
                case 2 -> burnTime;
                case 3 -> totalBurnTime;
                case 4 -> getOutput();
                case 5 -> energy.getCapacityAsInt() & 0xFFFF;
                case 6 -> energy.getCapacityAsInt() >>> 16;
                case 7 -> tier.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return FastGeneratorMenu.DATA_COUNT;
        }
    };

    public FastGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(FastFurnaceV2.FAST_GENERATOR_BLOCK_ENTITY.get(), pos, state);
        this.tier = ((FastGeneratorBlock) state.getBlock()).getTier();
        this.energy = new FastEnergyStorage(
                this.tier.getGeneratorBufferCapacity(), 0,
                this.tier.getGeneratorExtractionLimit(), this::setChanged);
    }

    public static void serverTick(
            ServerLevel level, BlockPos pos, BlockState state, FastGeneratorBlockEntity generator) {
        int previousBurn = generator.burnTime;
        int previousTotal = generator.totalBurnTime;
        boolean generating = false;
        if (generator.energy.getAmountAsInt() < generator.energy.getCapacityAsInt()) {
            if (generator.burnTime <= 0) {
                generator.consumeFuel(level);
            }
            int output = generator.getOutput();
            int space = generator.energy.getCapacityAsInt() - generator.energy.getAmountAsInt();
            if (generator.burnTime > 0 && space >= output) {
                generator.energy.set(generator.energy.getAmountAsInt() + output);
                generator.burnTime--;
                generating = true;
                if (generator.burnTime == 0) {
                    generator.clearActiveFuel();
                }
            }
        }
        for (Direction direction : Direction.values()) {
            EnergyHandler target = level.getCapability(
                    Capabilities.Energy.BLOCK, pos.relative(direction), direction.getOpposite());
            if (target != null) {
                try (Transaction transaction = Transaction.openRoot()) {
                    EnergyHandlerUtil.move(generator.energy, target,
                            generator.tier.getGeneratorExtractionLimit(), transaction);
                    transaction.commit();
                }
            }
        }
        if (state.getValue(FastGeneratorBlock.LIT) != generating) {
            level.setBlock(pos, state.setValue(FastGeneratorBlock.LIT, generating), 3);
        }
        if (generator.burnTime != previousBurn || generator.totalBurnTime != previousTotal) {
            generator.setChanged();
        }
    }

    private void consumeFuel(ServerLevel level) {
        ItemStack fuel = this.items.get(0);
        int ordinaryBurnTime = fuel.getBurnTime(RecipeType.SMELTING, level.fuelValues());
        if (ordinaryBurnTime <= 0) {
            return;
        }
        FastFuel definition = FastFurnaceApi.findFuel(fuel).orElse(null);
        this.outputPercent = FastGeneratorFuelLogic.getOutputPercent(definition);
        this.activeFuelId = definition == null ? null : definition.id();
        this.burnTime = FastGeneratorFuelLogic.getBurnTime(ordinaryBurnTime, definition);
        this.totalBurnTime = this.burnTime;
        ItemStackTemplate remainder = fuel.getCraftingRemainder();
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            this.items.set(0, remainder != null ? remainder.create() : ItemStack.EMPTY);
        }
        this.setChanged();
    }

    private void clearActiveFuel() {
        this.outputPercent = 100;
        this.activeFuelId = null;
    }

    public FastEnergyStorage energy() {
        return this.energy;
    }

    public int getOutput() {
        return this.tier.getGeneratorOutput() * this.outputPercent / 100;
    }

    public int getBurnTime() {
        return this.burnTime;
    }

    public int getTotalBurnTime() {
        return this.totalBurnTime;
    }

    public @Nullable Identifier getActiveFuelId() {
        return this.activeFuelId;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.fastfurnacev2." + this.tier.getGeneratorId());
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
        return new FastGeneratorMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return FUEL_SLOT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && this.level != null
                && stack.getBurnTime(RecipeType.SMELTING, this.level.fuelValues()) > 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
        this.energy.deserialize(input.childOrEmpty("energy"));
        this.burnTime = input.getIntOr("generator_burn_time", 0);
        this.totalBurnTime = input.getIntOr("generator_total_burn_time", 0);
        this.outputPercent = this.burnTime > 0 ? input.getIntOr("generator_output_percent", 100) : 100;
        Identifier fuelId = Identifier.tryParse(input.getStringOr("generator_active_fuel", ""));
        this.activeFuelId = this.burnTime > 0 ? fuelId : null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        this.energy.serialize(output.child("energy"));
        output.putInt("generator_burn_time", this.burnTime);
        output.putInt("generator_total_burn_time", this.totalBurnTime);
        output.putInt("generator_output_percent", this.outputPercent);
        if (this.activeFuelId != null) {
            output.putString("generator_active_fuel", this.activeFuelId.toString());
        }
    }
}
