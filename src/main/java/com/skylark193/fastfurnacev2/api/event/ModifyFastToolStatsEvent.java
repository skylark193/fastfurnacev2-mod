package com.skylark193.fastfurnacev2.api.event;

import com.skylark193.fastfurnacev2.api.FastToolStats;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.jetbrains.annotations.ApiStatus;

/** Composes tool-stat patches while default item components are still mutable. */
public final class ModifyFastToolStatsEvent extends Event implements IModBusEvent {
    private final ModifyDefaultComponentsEvent delegate;

    @ApiStatus.Internal
    public ModifyFastToolStatsEvent(ModifyDefaultComponentsEvent delegate) {
        this.delegate = delegate;
    }

    public void modify(Identifier itemId, UnaryOperator<FastToolStats> modifier) {
        Item item = Objects.requireNonNull(
                BuiltInRegistries.ITEM.getValue(itemId), "Unknown item: " + itemId);
        this.modify(item, modifier);
    }

    public void modify(Item item, UnaryOperator<FastToolStats> modifier) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(modifier, "modifier");
        this.delegate.modify(item, (components, context, currentItem) -> {
            FastToolStats original = FastToolStats.from(components);
            FastToolStats modified = Objects.requireNonNull(
                    modifier.apply(original), "The tool-stats modifier returned null");
            modified.applyTo(components, original);
        });
    }
}
