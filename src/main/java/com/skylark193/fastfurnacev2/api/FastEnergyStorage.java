package com.skylark193.fastfurnacev2.api;

import java.util.Objects;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

/**
 * Transaction-aware energy storage with configurable transfer limits and value-IO persistence.
 *
 * <p>The change callback runs immediately after {@link #set(int)} changes the amount and after a
 * transaction with transfer changes reaches its root commit. Aborted transactions and save loading
 * do not invoke it. Block entities can use the callback to mark themselves dirty and synchronize
 * energy with clients.</p>
 */
public final class FastEnergyStorage extends SimpleEnergyHandler {
    private final Runnable onChanged;

    /** Creates storage with the same insertion and extraction limit per operation. */
    public FastEnergyStorage(int capacity, int maxTransfer, Runnable onChanged) {
        this(capacity, maxTransfer, maxTransfer, onChanged);
    }

    /** Creates storage with separate insertion and extraction limits; zero disables that direction. */
    public FastEnergyStorage(int capacity, int maxInsert, int maxExtract, Runnable onChanged) {
        super(capacity, maxInsert, maxExtract);
        this.onChanged = Objects.requireNonNull(onChanged, "onChanged");
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        this.onChanged.run();
    }
}
