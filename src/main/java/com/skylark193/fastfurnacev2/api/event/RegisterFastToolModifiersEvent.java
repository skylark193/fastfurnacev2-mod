package com.skylark193.fastfurnacev2.api.event;

import com.skylark193.fastfurnacev2.api.FastToolModifiers;
import java.util.Objects;
import java.util.function.Consumer;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/** Mod-bus event posted once after default item components bind. */
public final class RegisterFastToolModifiersEvent extends Event implements IModBusEvent {
    private final Consumer<FastToolModifiers> registrar;

    @ApiStatus.Internal
    public RegisterFastToolModifiersEvent(Consumer<FastToolModifiers> registrar) {
        this.registrar = registrar;
    }

    public void register(FastToolModifiers modifiers) {
        this.registrar.accept(Objects.requireNonNull(modifiers, "modifiers"));
    }
}
