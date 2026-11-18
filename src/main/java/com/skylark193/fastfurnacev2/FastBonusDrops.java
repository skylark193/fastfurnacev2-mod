package com.skylark193.fastfurnacev2;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

final class FastBonusDrops {
    private static final float BASE_DROP_CHANCE = 0.10F;
    private static final float FORTUNE_CHANCE_BONUS = 0.05F;

    private FastBonusDrops() {
    }

    static void onBlockDrops(BlockDropsEvent event) {
        ItemLike bonusItem;
        if (event.getState().is(Blocks.STONE)) {
            bonusItem = FastFurnaceV2.FAST_COBBLESTONE_ITEM.get();
        } else if (event.getState().is(BlockTags.COAL_ORES)) {
            bonusItem = FastFurnaceV2.FAST_COAL.get();
        } else {
            return;
        }

        if (!(event.getBreaker() instanceof Player player) || player.isCreative()) {
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
        int fortuneLevel = event.getTool().getEnchantmentLevel(
                level.registryAccess().getOrThrow(Enchantments.FORTUNE));
        if (silkTouchLevel > 0 || level.getRandom().nextFloat() >= getDropChance(fortuneLevel)) {
            return;
        }

        // Vanilla drops have already been calculated; append the bonus without replacing them.
        ItemStack bonusDrop = new ItemStack(bonusItem);
        event.getDrops().add(new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                bonusDrop));
    }

    static float getDropChance(int fortuneLevel) {
        return Math.min(1.0F, BASE_DROP_CHANCE + Math.max(0, fortuneLevel) * FORTUNE_CHANCE_BONUS);
    }
}
