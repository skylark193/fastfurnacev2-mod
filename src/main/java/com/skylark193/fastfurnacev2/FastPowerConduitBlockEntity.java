package com.skylark193.fastfurnacev2;

import com.skylark193.fastfurnacev2.api.FastEnergyStorage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class FastPowerConduitBlockEntity extends BlockEntity {
    private final FastFurnaceTier tier;
    private final FastEnergyStorage energy;
    private long lastNetworkTick = Long.MIN_VALUE;

    public FastPowerConduitBlockEntity(BlockPos pos, BlockState state) {
        super(FastFurnaceV2.FAST_POWER_CONDUIT_BLOCK_ENTITY.get(), pos, state);
        this.tier = ((FastPowerConduitBlock) state.getBlock()).getTier();
        int rate = this.tier.getConduitTransferRate();
        this.energy = new FastEnergyStorage(rate, rate, this::setChanged);
    }

    public FastFurnaceTier tier() {
        return this.tier;
    }

    public FastEnergyStorage energy() {
        return this.energy;
    }

    public static void serverTick(
            ServerLevel level, BlockPos pos, BlockState state, FastPowerConduitBlockEntity conduit) {
        runNetwork(level, conduit, level.getGameTime());
    }

    static void runNetwork(ServerLevel level, FastPowerConduitBlockEntity start, long tick) {
        if (start.lastNetworkTick == tick) {
            return;
        }
        List<FastPowerConduitBlockEntity> conduits = discoverNetwork(level, start, tick);
        int remaining = conduits.stream()
                .mapToInt(conduit -> conduit.tier.getConduitTransferRate())
                .min()
                .orElse(0);
        if (remaining == 0) {
            return;
        }

        List<Endpoint> endpoints = findEndpoints(level, conduits);
        Set<BlockPos> supplied = new HashSet<>();
        Set<BlockPos> received = new HashSet<>();
        for (FastPowerConduitBlockEntity conduit : conduits) {
            for (Endpoint sink : endpoints) {
                int moved = move(conduit.energy, sink.handler, remaining);
                remaining -= moved;
                if (moved > 0) {
                    received.add(sink.pos);
                }
                if (remaining == 0) {
                    return;
                }
            }
        }

        for (Endpoint source : endpoints) {
            if (received.contains(source.pos)) {
                continue;
            }
            for (Endpoint sink : endpoints) {
                if (source.pos.equals(sink.pos) || received.contains(source.pos)
                        || supplied.contains(sink.pos)
                        || source.canInsert && sink.canExtract
                        && source.handler.getAmountAsLong() <= sink.handler.getAmountAsLong()) {
                    continue;
                }
                int moved = move(source.handler, sink.handler, remaining);
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

    private static List<FastPowerConduitBlockEntity> discoverNetwork(
            ServerLevel level, FastPowerConduitBlockEntity start, long tick) {
        List<FastPowerConduitBlockEntity> conduits = new ArrayList<>();
        ArrayDeque<FastPowerConduitBlockEntity> pending = new ArrayDeque<>();
        pending.add(start);
        while (!pending.isEmpty()) {
            FastPowerConduitBlockEntity conduit = pending.removeFirst();
            if (conduit.lastNetworkTick == tick) {
                continue;
            }
            conduit.lastNetworkTick = tick;
            conduits.add(conduit);
            for (Direction direction : Direction.values()) {
                if (level.getBlockEntity(conduit.getBlockPos().relative(direction))
                        instanceof FastPowerConduitBlockEntity neighbor
                        && neighbor.lastNetworkTick != tick) {
                    pending.addLast(neighbor);
                }
            }
        }
        return conduits;
    }

    private static List<Endpoint> findEndpoints(
            ServerLevel level, List<FastPowerConduitBlockEntity> conduits) {
        List<Endpoint> endpoints = new ArrayList<>();
        Set<BlockPos> positions = new HashSet<>();
        for (FastPowerConduitBlockEntity conduit : conduits) {
            for (Direction direction : Direction.values()) {
                BlockPos pos = conduit.getBlockPos().relative(direction);
                if (level.getBlockState(pos).getBlock() instanceof FastPowerConduitBlock) {
                    continue;
                }
                EnergyHandler handler = level.getCapability(
                        Capabilities.Energy.BLOCK, pos, direction.getOpposite());
                if (handler != null && positions.add(pos)) {
                    endpoints.add(new Endpoint(pos, handler, canInsert(handler), canExtract(handler)));
                }
            }
        }
        return endpoints;
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
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.energy.serialize(output.child("energy"));
    }

    private record Endpoint(BlockPos pos, EnergyHandler handler, boolean canInsert, boolean canExtract) {
    }
}
