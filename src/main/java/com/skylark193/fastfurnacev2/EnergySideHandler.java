package com.skylark193.fastfurnacev2;

import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

final class EnergySideHandler implements EnergyHandler {
    private final EnergyHandler delegate;
    private final EnergySideConfiguration configuration;
    private final net.minecraft.core.Direction direction;

    EnergySideHandler(
            EnergyHandler delegate, EnergySideConfiguration configuration, net.minecraft.core.Direction direction) {
        this.delegate = delegate;
        this.configuration = configuration;
        this.direction = direction;
    }

    EnergySideHandler(EnergyHandler delegate, EnergySideMode fixedMode) {
        this(delegate, new EnergySideConfiguration(fixedMode), net.minecraft.core.Direction.DOWN);
    }

    @Override
    public long getAmountAsLong() {
        return this.delegate.getAmountAsLong();
    }

    @Override
    public long getCapacityAsLong() {
        return this.delegate.getCapacityAsLong();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return this.configuration.get(this.direction).allowsInput()
                ? this.delegate.insert(amount, transaction)
                : 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return this.configuration.get(this.direction).allowsOutput()
                ? this.delegate.extract(amount, transaction)
                : 0;
    }
}
