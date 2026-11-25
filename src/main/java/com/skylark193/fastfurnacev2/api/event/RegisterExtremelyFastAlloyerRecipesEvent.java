package com.skylark193.fastfurnacev2.api.event;

import com.skylark193.fastfurnacev2.api.ExtremelyFastAlloyerRecipe;
import java.util.Objects;
import java.util.function.Consumer;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/** Fired once on the mod event bus after default item components are bound. */
public final class RegisterExtremelyFastAlloyerRecipesEvent extends Event implements IModBusEvent {
    private final Consumer<ExtremelyFastAlloyerRecipe> registrar;

    @ApiStatus.Internal
    public RegisterExtremelyFastAlloyerRecipesEvent(Consumer<ExtremelyFastAlloyerRecipe> registrar) {
        this.registrar = registrar;
    }

    /** Registers a recipe. IDs must be unique; recipe priority controls overrides. */
    public void register(ExtremelyFastAlloyerRecipe recipe) {
        this.registrar.accept(Objects.requireNonNull(recipe, "recipe"));
    }
}
