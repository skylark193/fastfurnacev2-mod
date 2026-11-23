package com.skylark193.fastfurnacev2.api.event;

import com.skylark193.fastfurnacev2.api.CrusherRecipe;
import java.util.Objects;
import java.util.function.Consumer;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/** Fired once on the mod event bus after default item components are bound. */
public final class RegisterFastCrusherRecipesEvent extends Event implements IModBusEvent {
    private final Consumer<CrusherRecipe> registrar;

    @ApiStatus.Internal
    public RegisterFastCrusherRecipesEvent(Consumer<CrusherRecipe> registrar) {
        this.registrar = registrar;
    }

    /** Registers a recipe. IDs must be unique; ingredient priority controls overrides. */
    public void register(CrusherRecipe recipe) {
        this.registrar.accept(Objects.requireNonNull(recipe, "recipe"));
    }
}
