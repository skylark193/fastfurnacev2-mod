package com.skylark193.fastfurnacev2.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

final class CrushedOreTintSource implements ItemTintSource {
    static final CrushedOreTintSource INSTANCE = new CrushedOreTintSource();
    static final MapCodec<CrushedOreTintSource> CODEC = MapCodec.unit(INSTANCE);

    private CrushedOreTintSource() {
    }

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        return CrushedOreColors.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    @Override
    public MapCodec<CrushedOreTintSource> type() {
        return CODEC;
    }
}
