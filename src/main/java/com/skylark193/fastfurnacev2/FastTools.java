package com.skylark193.fastfurnacev2;

import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;
import org.jspecify.annotations.Nullable;

/** Shared Fast tool materials and the durability-saving Fast Diamond item behavior. */
final class FastTools {
    static final float DURABILITY_SAVE_CHANCE = 0.05F;

    static final ToolMaterial FAST_IRON = withMiningSpeed(
            ToolMaterial.IRON, BlockTags.INCORRECT_FOR_IRON_TOOL, "fast_iron_tool_materials", 1.20F);
    static final ToolMaterial FAST_GOLD = withMiningSpeed(
            ToolMaterial.GOLD, BlockTags.INCORRECT_FOR_GOLD_TOOL, "fast_gold_tool_materials", 1.30F);
    static final ToolMaterial FAST_DIAMOND = withMiningSpeed(
            ToolMaterial.DIAMOND, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, "fast_diamond_tool_materials", 1.20F);

    private FastTools() {
    }

    private static ToolMaterial withMiningSpeed(
            ToolMaterial vanilla,
            net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> incorrectBlocksForDrops,
            String repairTag,
            float speedMultiplier) {
        return new ToolMaterial(
                incorrectBlocksForDrops,
                vanilla.durability(),
                vanilla.speed() * speedMultiplier,
                vanilla.attackDamageBonus(),
                vanilla.enchantmentValue(),
                ItemTags.create(Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, repairTag)));
    }

    /** Scale actual attacks per second, whose player-inclusive base is 4 + the item modifier. */
    static float scaleSwordAttackSpeed(float vanillaBaseline, float speedMultiplier) {
        return (4.0F + vanillaBaseline) * speedMultiplier - 4.0F;
    }

    /** Scale total vanilla sword damage while retaining the custom material's damage bonus. */
    static float scaleSwordDamage(
            ToolMaterial vanillaMaterial,
            ToolMaterial fastMaterial,
            float vanillaDamageBaseline,
            float damageMultiplier) {
        float vanillaDamage = 1.0F + vanillaDamageBaseline + vanillaMaterial.attackDamageBonus();
        return vanillaDamage * damageMultiplier - 1.0F - fastMaterial.attackDamageBonus();
    }

    private static int rollDurabilityDamage(int amount, @Nullable LivingEntity entity) {
        return amount > 0
                && entity != null
                && !entity.level().isClientSide()
                && entity.level().getRandom().nextFloat() < DURABILITY_SAVE_CHANCE
                ? 0
                : amount;
    }

    static final class Standard extends Item {
        Standard(Item.Properties properties) {
            super(properties);
        }

        @Override
        public <T extends LivingEntity> int damageItem(
                ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
            return rollDurabilityDamage(amount, entity);
        }
    }

    static final class Axe extends AxeItem {
        Axe(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline, Item.Properties properties) {
            super(material, attackDamageBaseline, attackSpeedBaseline, properties);
        }

        @Override
        public <T extends LivingEntity> int damageItem(
                ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
            return rollDurabilityDamage(amount, entity);
        }
    }

    static final class Shovel extends ShovelItem {
        Shovel(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline, Item.Properties properties) {
            super(material, attackDamageBaseline, attackSpeedBaseline, properties);
        }

        @Override
        public <T extends LivingEntity> int damageItem(
                ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
            return rollDurabilityDamage(amount, entity);
        }
    }
}
