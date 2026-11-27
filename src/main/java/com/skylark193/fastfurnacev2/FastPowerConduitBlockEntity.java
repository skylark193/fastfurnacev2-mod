package com.skylark193.fastfurnacev2;

import com.skylark193.fastfurnacev2.api.FastEnergyStorage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public class FastPowerConduitBlockEntity extends BlockEntity implements MenuProvider {
    private final FastFurnaceTier tier;
    private final FastEnergyStorage energy;
    private final EnergySideConfiguration connectionConfiguration =
            new EnergySideConfiguration(EnergySideMode.BOTH);
    private final Map<Direction, EnergyHandler> sideHandlers = new EnumMap<>(Direction.class);
    private ConduitTransferMode transferMode = ConduitTransferMode.FILL_FIRST_FOUND;
    private long lastNetworkTick = Long.MIN_VALUE;

    public FastPowerConduitBlockEntity(BlockPos pos, BlockState state) {
        super(FastFurnaceV2.FAST_POWER_CONDUIT_BLOCK_ENTITY.get(), pos, state);
        this.tier = ((FastPowerConduitBlock) state.getBlock()).getTier();
        int rate = this.tier.getConduitTransferRate();
        this.energy = new FastEnergyStorage(rate, rate, this::setChanged);
        for (Direction direction : Direction.values()) {
            this.sideHandlers.put(
                    direction, new EnergySideHandler(this.energy, this.connectionConfiguration, direction));
        }
    }

    public FastFurnaceTier tier() {
        return this.tier;
    }

    public FastEnergyStorage energy() {
        return this.energy;
    }

    public EnergyHandler energyForSide(@Nullable Direction direction) {
        return direction == null ? this.energy : this.sideHandlers.get(direction);
    }

    public EnergySideMode getConnectionMode(Direction direction) {
        return this.connectionConfiguration.get(direction);
    }

    public void cycleConnectionMode(Direction direction) {
        this.connectionConfiguration.cycle(direction);
        this.setChanged();
    }

    public ConduitTransferMode getTransferMode() {
        return this.transferMode;
    }

    public ConduitTransferMode getNetworkTransferMode() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return this.transferMode;
        }
        return collectNetwork(serverLevel, this).stream()
                .min(Comparator.comparing(FastPowerConduitBlockEntity::getBlockPos))
                .map(conduit -> conduit.transferMode)
                .orElse(this.transferMode);
    }

    public int getNetworkTransferRate() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return this.tier.getConduitTransferRate();
        }
        return networkRate(collectNetwork(serverLevel, this));
    }

    public void cycleNetworkTransferMode() {
        this.setNetworkTransferMode(this.getNetworkTransferMode().next());
    }

    public void setNetworkTransferMode(ConduitTransferMode mode) {
        if (this.level instanceof ServerLevel serverLevel) {
            for (FastPowerConduitBlockEntity conduit : collectNetwork(serverLevel, this)) {
                conduit.setLocalTransferMode(mode);
            }
        } else {
            this.setLocalTransferMode(mode);
        }
    }

    private void setLocalTransferMode(ConduitTransferMode mode) {
        if (this.transferMode != mode) {
            this.transferMode = mode;
            this.setChanged();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FastPowerConduitMenu(containerId, inventory, this);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.worldPosition);
    }

    public static void serverTick(
            ServerLevel level, BlockPos pos, BlockState state, FastPowerConduitBlockEntity conduit) {
        runNetwork(level, conduit, level.getGameTime());
    }

    static void runNetwork(ServerLevel level, FastPowerConduitBlockEntity start, long tick) {
        if (start.lastNetworkTick == tick) {
            return;
        }
        List<FastPowerConduitBlockEntity> conduits = collectNetwork(level, start, tick);
        for (FastPowerConduitBlockEntity conduit : conduits) {
            conduit.lastNetworkTick = tick;
        }
        int remaining = networkRate(conduits);
        if (remaining == 0) {
            return;
        }
        ConduitTransferMode mode = conduits.stream()
                .min(Comparator.comparing(FastPowerConduitBlockEntity::getBlockPos))
                .map(conduit -> conduit.transferMode)
                .orElse(ConduitTransferMode.FILL_FIRST_FOUND);
        for (FastPowerConduitBlockEntity conduit : conduits) {
            conduit.setLocalTransferMode(mode);
        }

        List<Endpoint> endpoints = findEndpoints(level, conduits);
        endpoints.sort(Comparator.comparing(endpoint -> endpoint.pos));
        List<Endpoint> sinks = endpoints.stream().filter(Endpoint::canInsert).toList();
        Set<BlockPos> supplied = new HashSet<>();
        Set<BlockPos> received = new HashSet<>();
        for (FastPowerConduitBlockEntity conduit : mode.order(conduits, tick, level.getRandom())) {
            for (Endpoint sink : mode.order(sinks, tick, level.getRandom())) {
                int moved = move(conduit.energy, sink.insertHandler, remaining);
                remaining -= moved;
                if (moved > 0) {
                    received.add(sink.pos);
                }
                if (remaining == 0) {
                    return;
                }
            }
        }

        List<Endpoint> sources = endpoints.stream().filter(Endpoint::canExtract).toList();
        for (Endpoint source : mode.order(sources, tick, level.getRandom())) {
            if (received.contains(source.pos)) {
                continue;
            }
            for (Endpoint sink : mode.order(sinks, tick, level.getRandom())) {
                if (source.pos.equals(sink.pos) || received.contains(source.pos)
                        || supplied.contains(sink.pos)
                        || source.battery && sink.battery
                        || source.canInsert() && sink.canExtract()
                        && source.insertHandler.getAmountAsLong() <= sink.extractHandler.getAmountAsLong()) {
                    continue;
                }
                int moved = move(source.extractHandler, sink.insertHandler, remaining);
                remaining -= moved;
                if (moved > 0) {
                    supplied.add(source.pos);
                    received.add(sink.pos);
                }
                if (remaining == 0) {
                    return;
                }
            }
        }
    }

    private static int networkRate(List<FastPowerConduitBlockEntity> conduits) {
        return conduits.stream()
                .mapToInt(conduit -> conduit.tier.getConduitTransferRate())
                .min()
                .orElse(0);
    }

    private static List<FastPowerConduitBlockEntity> collectNetwork(
            ServerLevel level, FastPowerConduitBlockEntity start) {
        return collectNetwork(level, start, null);
    }

    private static List<FastPowerConduitBlockEntity> collectNetwork(
            ServerLevel level, FastPowerConduitBlockEntity start, @Nullable Long alreadyRunTick) {
        List<FastPowerConduitBlockEntity> conduits = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<FastPowerConduitBlockEntity> pending = new ArrayDeque<>();
        pending.add(start);
        while (!pending.isEmpty()) {
            FastPowerConduitBlockEntity conduit = pending.removeFirst();
            if (!visited.add(conduit.getBlockPos())
                    || alreadyRunTick != null && conduit.lastNetworkTick == alreadyRunTick) {
                continue;
            }
            conduits.add(conduit);
            for (Direction direction : Direction.values()) {
                if (level.getBlockEntity(conduit.getBlockPos().relative(direction))
                        instanceof FastPowerConduitBlockEntity neighbor
                        && !visited.contains(neighbor.getBlockPos())
                        && (alreadyRunTick == null || neighbor.lastNetworkTick != alreadyRunTick)) {
                    pending.addLast(neighbor);
                }
            }
        }
        return conduits;
    }

    private static List<Endpoint> findEndpoints(
            ServerLevel level, List<FastPowerConduitBlockEntity> conduits) {
        Map<BlockPos, Endpoint> endpoints = new LinkedHashMap<>();
        for (FastPowerConduitBlockEntity conduit : conduits) {
            for (Direction direction : Direction.values()) {
                BlockPos pos = conduit.getBlockPos().relative(direction);
                if (level.getBlockEntity(pos) instanceof FastPowerConduitBlockEntity) {
                    continue;
                }
                EnergyHandler handler = level.getCapability(
                        Capabilities.Energy.BLOCK, pos, direction.getOpposite());
                if (handler == null) {
                    continue;
                }
                Endpoint endpoint = endpoints.computeIfAbsent(pos,
                        ignored -> new Endpoint(pos, level.getBlockEntity(pos) instanceof FastBatteryBlockEntity));
                EnergySideMode mode = conduit.getConnectionMode(direction);
                if (mode.allowsInput() && endpoint.extractHandler == null && canExtract(handler)) {
                    endpoint.extractHandler = handler;
                }
                if (mode.allowsOutput() && endpoint.insertHandler == null && canInsert(handler)) {
                    endpoint.insertHandler = handler;
                }
            }
        }
        endpoints.values().removeIf(endpoint -> !endpoint.canExtract() && !endpoint.canInsert());
        return new ArrayList<>(endpoints.values());
    }

    private static boolean canInsert(EnergyHandler handler) {
        try (Transaction transaction = Transaction.openRoot()) {
            return handler.insert(1, transaction) > 0;
        }
    }

    private static boolean canExtract(EnergyHandler handler) {
        try (Transaction transaction = Transaction.openRoot()) {
            return handler.extract(1, transaction) > 0;
        }
    }

    private static int move(EnergyHandler source, EnergyHandler target, int amount) {
        if (amount <= 0) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int moved = EnergyHandlerUtil.move(source, target, amount, transaction);
            transaction.commit();
            return moved;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.energy.deserialize(input.childOrEmpty("energy"));
        this.connectionConfiguration.deserialize(
                input.getIntOr("energy_connection_modes", EnergySideConfiguration.ALL_BOTH));
        this.transferMode = ConduitTransferMode.fromSerialized(input.getIntOr("network_transfer_mode", 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.energy.serialize(output.child("energy"));
        output.putInt("energy_connection_modes", this.connectionConfiguration.serialize());
        output.putInt("network_transfer_mode", this.transferMode.ordinal());
    }

    private static final class Endpoint {
        private final BlockPos pos;
        private final boolean battery;
        private @Nullable EnergyHandler insertHandler;
        private @Nullable EnergyHandler extractHandler;

        private Endpoint(BlockPos pos, boolean battery) {
            this.pos = pos;
            this.battery = battery;
        }

        private boolean canInsert() {
            return this.insertHandler != null;
        }

        private boolean canExtract() {
            return this.extractHandler != null;
        }
    }
}
