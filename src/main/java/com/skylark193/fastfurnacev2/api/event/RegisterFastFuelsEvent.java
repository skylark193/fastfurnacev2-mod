package com.skylark193.fastfurnacev2.api.event;

import com.skylark193.fastfurnacev2.api.FastFuel;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/** Fired once on the mod event bus after default item components are bound. */
public final class RegisterFastFuelsEvent extends Event implements IModBusEvent {
    private final Consumer<FastFuel> registrar;

    @ApiStatus.Internal
    public RegisterFastFuelsEvent(Consumer<FastFuel> registrar) {
        this.registrar = registrar;
    }

    /** Registers a definition. IDs must be unique. */
    public void register(FastFuel fuel) {
        this.registrar.accept(Objects.requireNonNull(fuel, "fuel"));
    }

    /** Registers an ordinary fuel with no custom bonuses. */
    public void register(Identifier id, ItemLike fuel, int burnTime) {
        this.register(FastFuel.builder(id, Ingredient.of(fuel), burnTime).build());
    }
}
