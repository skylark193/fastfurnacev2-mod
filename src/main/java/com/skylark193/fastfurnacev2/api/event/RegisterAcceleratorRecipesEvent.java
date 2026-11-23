package com.skylark193.fastfurnacev2.api.event;

import com.skylark193.fastfurnacev2.api.AcceleratorConversion;
import java.util.Objects;
import java.util.function.Consumer;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/** Fired once on the mod event bus after default item components are bound. */
public final class RegisterAcceleratorRecipesEvent extends Event implements IModBusEvent {
    private final Consumer<AcceleratorConversion> registrar;

    @ApiStatus.Internal
    public RegisterAcceleratorRecipesEvent(Consumer<AcceleratorConversion> registrar) {
        this.registrar = registrar;
    }

    /** Registers a conversion. IDs must be unique; ingredient priority controls overrides. */
    public void register(AcceleratorConversion conversion) {
        this.registrar.accept(Objects.requireNonNull(conversion, "conversion"));
    }
}
