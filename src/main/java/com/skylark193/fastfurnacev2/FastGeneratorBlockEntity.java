package com.skylark193.fastfurnacev2;

import com.skylark193.fastfurnacev2.api.FastEnergyStorage;
import com.skylark193.fastfurnacev2.api.FastFuel;
import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
    private final EnergyHandler unsidedHandler;
    private final EnergySideConfiguration sideConfiguration = new EnergySideConfiguration(EnergySideMode.OUTPUT);
    private final Map<Direction, EnergyHandler> sideHandlers = new EnumMap<>(Direction.class);
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private int burnTime;
    private int totalBurnTime;
    private int outputPercent = 100;
    private int lavaSides;
    private int activeLavaBonusPercent;
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
                case 8, 9, 10, 11, 12, 13 ->
                        sideConfiguration.get(Direction.values()[index - 8]).ordinal();
                case 14 -> lavaSides;
                case 15 -> activeLavaBonusPercent;
                case 16 -> burnTime >>> 16;
                case 17 -> totalBurnTime >>> 16;
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
                this.tier.getGeneratorBufferCapacity(),
                this.tier.getGeneratorExtractionLimit(),
                this.tier.getGeneratorExtractionLimit(), this::setChanged);
        this.unsidedHandler = new EnergySideHandler(this.energy, EnergySideMode.OUTPUT);
        for (Direction direction : Direction.values()) {
            this.sideHandlers.put(direction, new EnergySideHandler(this.energy, this.sideConfiguration, direction));
        }
    }

    public static void serverTick(
            ServerLevel level, BlockPos pos, BlockState state, FastGeneratorBlockEntity generator) {
        int previousBurn = generator.burnTime;
        int previousTotal = generator.totalBurnTime;
        generator.lavaSides = MachineLogic.countHorizontalLava(level, pos);
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
                    EnergyHandlerUtil.move(generator.energyForSide(direction), target,
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
        this.activeLavaBonusPercent = FastGeneratorFuelLogic.getLavaBurnBonusPercent(definition, this.lavaSides);
        this.burnTime = FastGeneratorFuelLogic.getBurnTime(ordinaryBurnTime, definition, this.lavaSides);
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
        this.activeLavaBonusPercent = 0;
        this.activeFuelId = null;
    }

    public FastFurnaceTier getTier() {
        return this.tier;
    }

    public FastEnergyStorage energy() {
        return this.energy;
    }

    public EnergyHandler energyForSide(@Nullable Direction direction) {
        return direction == null ? this.unsidedHandler : this.sideHandlers.get(direction);
    }

    public EnergySideMode getEnergySideMode(Direction direction) {
        return this.sideConfiguration.get(direction);
    }

    public void cycleEnergySide(Direction direction) {
        this.sideConfiguration.cycle(direction);
        this.setChanged();
    }

    public int getLavaSides() {
        return this.lavaSides;
    }

    public int getActiveLavaBonusPercent() {
        return this.activeLavaBonusPercent;
    }

    public int getActiveBurnTimePercent() {
        FastFuel activeFuel = this.activeFuelId == null
                ? null : FastFurnaceApi.findFuel(this.activeFuelId).orElse(null);
        return FastGeneratorFuelLogic.isBuiltInSpecialFuel(activeFuel)
                ? FastGeneratorFuelLogic.BASE_SPECIAL_BURN_TIME_PERCENT + this.activeLavaBonusPercent : 100;
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
        return new FastGeneratorMenu(containerId, inventory, this, this.dataAccess, this.worldPosition);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.worldPosition);
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
        this.activeLavaBonusPercent = this.burnTime > 0 ? input.getIntOr("generator_lava_bonus", 0) : 0;
        this.sideConfiguration.deserialize(input.getIntOr("energy_side_modes", EnergySideConfiguration.ALL_OUTPUT));
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
        output.putInt("generator_lava_bonus", this.activeLavaBonusPercent);
        output.putInt("energy_side_modes", this.sideConfiguration.serialize());
        if (this.activeFuelId != null) {
            output.putString("generator_active_fuel", this.activeFuelId.toString());
        }
    }
}
