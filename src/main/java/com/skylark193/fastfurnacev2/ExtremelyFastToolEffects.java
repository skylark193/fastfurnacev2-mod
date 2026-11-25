package com.skylark193.fastfurnacev2;

import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Independent registered tool proc rolls, applied after normal loot and base damage. */
final class ExtremelyFastToolEffects {
    private ExtremelyFastToolEffects() {
    }

    static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player)
                || player.isCreative()
                || !event.getLevel().getGameRules().get(GameRules.BLOCK_DROPS)
                || !player.hasCorrectToolForDrops(event.getState(), event.getLevel(), event.getPos())) {
            return;
        }
        float chance = FastFurnaceApi.findToolModifiers(event.getTool())
                .map(modifiers -> modifiers.blockDropDoubleChance())
                .orElse(0.0F);
        if (roll(event.getLevel(), chance)) {
            event.getDrops().addAll(duplicateDrops(event.getLevel(), event.getDrops()));
        }
    }

    static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)
                || !isDirectMeleeAttack(event.getSource())
                || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        float chance = FastFurnaceApi.findToolModifiers(player.getMainHandItem())
                .map(modifiers -> modifiers.killDropDoubleChance())
                .orElse(0.0F);
        if (roll(level, chance)) {
            event.getDrops().addAll(duplicateDrops(level, event.getDrops()));
        }
    }

    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)
                || !isDirectMeleeAttack(event.getSource())
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        float chance = FastFurnaceApi.findToolModifiers(player.getMainHandItem())
                .map(modifiers -> modifiers.damageDoubleChance())
                .orElse(0.0F);
        if (roll(level, chance)) {
            event.setAmount(event.getAmount() * 2.0F);
        }
    }

    private static boolean isDirectMeleeAttack(DamageSource source) {
        return source.isDirect() && source.is(DamageTypes.PLAYER_ATTACK);
    }

    private static boolean roll(ServerLevel level, float chance) {
        return chance >= 1.0F || chance > 0.0F && level.getRandom().nextFloat() < chance;
    }

    private static List<ItemEntity> duplicateDrops(ServerLevel level, Collection<ItemEntity> drops) {
        List<ItemEntity> duplicates = new ArrayList<>(drops.size());
        for (ItemEntity drop : drops) {
            ItemEntity duplicate = new ItemEntity(
                    level, drop.getX(), drop.getY(), drop.getZ(), drop.getItem().copy());
            duplicate.setDeltaMovement(drop.getDeltaMovement());
            duplicates.add(duplicate);
        }
        return duplicates;
    }
}
