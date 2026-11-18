package com.skylark193.fastfurnacev2;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

final class FastCobblestoneDrops {
    private static final float DROP_CHANCE = 0.10F;

    private FastCobblestoneDrops() {
    }

    static void onBlockDrops(BlockDropsEvent event) {
        if (!event.getState().is(Blocks.STONE)
                || !(event.getBreaker() instanceof Player player)
                || player.isCreative()) {
            return;
        }

        ServerLevel level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!level.getGameRules().get(GameRules.BLOCK_DROPS)
                || !player.hasCorrectToolForDrops(event.getState(), level, pos)) {
            return;
        }

        int silkTouchLevel = event.getTool().getEnchantmentLevel(
                level.registryAccess().getOrThrow(Enchantments.SILK_TOUCH));
        if (silkTouchLevel > 0 || level.getRandom().nextFloat() >= DROP_CHANCE) {
            return;
        }

        // Vanilla drops have already been calculated; append the bonus without replacing them.
        ItemStack bonusDrop = new ItemStack(FastFurnaceV2.FAST_COBBLESTONE_ITEM.get());
        event.getDrops().add(new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                bonusDrop));
    }
}
