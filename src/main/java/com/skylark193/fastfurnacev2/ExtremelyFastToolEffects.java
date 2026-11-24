package com.skylark193.fastfurnacev2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Independent Extremely Fast tool proc rolls, applied after normal loot and base damage. */
final class ExtremelyFastToolEffects {
    static final float DROP_DOUBLE_CHANCE = 0.05F;
    static final float DAMAGE_DOUBLE_CHANCE = 0.10F;

    private ExtremelyFastToolEffects() {
    }

    static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player)
                || player.isCreative()
                || !isHarvestingTool(event.getTool())
                || !event.getLevel().getGameRules().get(GameRules.BLOCK_DROPS)
                || !player.hasCorrectToolForDrops(event.getState(), event.getLevel(), event.getPos())
                || event.getLevel().getRandom().nextFloat() >= DROP_DOUBLE_CHANCE) {
            return;
        }
        event.getDrops().addAll(duplicateDrops(event.getLevel(), event.getDrops()));
    }

    static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)
                || !(event.getEntity().level() instanceof ServerLevel level)
                || !isSword(player.getMainHandItem())
                || level.getRandom().nextFloat() >= DROP_DOUBLE_CHANCE) {
            return;
        }
        event.getDrops().addAll(duplicateDrops(level, event.getDrops()));
    }

    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)
                || !(player.level() instanceof ServerLevel level)
                || !isSword(player.getMainHandItem())
                || level.getRandom().nextFloat() >= DAMAGE_DOUBLE_CHANCE) {
            return;
        }
        event.setAmount(event.getAmount() * 2.0F);
    }

    private static boolean isHarvestingTool(ItemStack stack) {
        return stack.is(FastFurnaceV2.EXTREMELY_FAST_PICKAXE.get())
                || stack.is(FastFurnaceV2.EXTREMELY_FAST_SHOVEL.get())
                || stack.is(FastFurnaceV2.EXTREMELY_FAST_AXE.get());
    }

    private static boolean isSword(ItemStack stack) {
        return stack.is(FastFurnaceV2.EXTREMELY_FAST_SWORD.get());
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
