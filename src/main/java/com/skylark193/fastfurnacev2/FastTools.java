package com.skylark193.fastfurnacev2;

import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
import com.skylark193.fastfurnacev2.api.FastToolType;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/** Shared Fast tool materials and modifier-driven item durability behavior. */
@ApiStatus.Internal
public final class FastTools {
    public static final float DURABILITY_SAVE_CHANCE = 0.05F;
    public static final float DROP_DOUBLE_CHANCE = 0.05F;
    public static final float DAMAGE_DOUBLE_CHANCE = 0.10F;

    static final ToolMaterial FAST_IRON = withMiningSpeed(
            ToolMaterial.IRON, BlockTags.INCORRECT_FOR_IRON_TOOL, "fast_iron_tool_materials", 1.20F);
    static final ToolMaterial FAST_GOLD = withMiningSpeed(
            ToolMaterial.GOLD, BlockTags.INCORRECT_FOR_GOLD_TOOL, "fast_gold_tool_materials", 1.30F);
    static final ToolMaterial FAST_DIAMOND = withMiningSpeed(
            ToolMaterial.DIAMOND, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, "fast_diamond_tool_materials", 1.20F);
    static final ToolMaterial EXTREMELY_FAST = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            ToolMaterial.DIAMOND.durability() * 2,
            ToolMaterial.DIAMOND.speed() * 1.50F,
            ToolMaterial.DIAMOND.attackDamageBonus(),
            ToolMaterial.DIAMOND.enchantmentValue(),
            ItemTags.create(Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "extremely_fast_tool_materials")));

    private FastTools() {
    }

    public static Item create(
            FastToolType type,
            ToolMaterial material,
            float attackDamageBaseline,
            float attackSpeedBaseline,
            Item.Properties properties) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(properties, "properties");
        return switch (type) {
            case PICKAXE -> new Standard(properties.pickaxe(material, attackDamageBaseline, attackSpeedBaseline));
            case SHOVEL -> new Shovel(material, attackDamageBaseline, attackSpeedBaseline, properties);
            case AXE -> new Axe(material, attackDamageBaseline, attackSpeedBaseline, properties);
            case SWORD -> new Standard(properties.sword(material, attackDamageBaseline, attackSpeedBaseline));
        };
    }

    private static ToolMaterial withMiningSpeed(
            ToolMaterial vanilla,
            TagKey<Block> incorrectBlocksForDrops,
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

    private static int rollDurabilityDamage(ItemStack stack, int amount, @Nullable LivingEntity entity) {
        if (amount <= 0 || entity == null || entity.level().isClientSide()) {
            return amount;
        }
        float chance = FastFurnaceApi.findToolModifiers(stack)
                .map(modifiers -> modifiers.durabilitySaveChance())
                .orElse(0.0F);
        return chance >= 1.0F || chance > 0.0F && entity.level().getRandom().nextFloat() < chance ? 0 : amount;
    }

    static final class Standard extends Item {
        Standard(Item.Properties properties) {
            super(properties);
        }

        @Override
        public <T extends LivingEntity> int damageItem(
                ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
            return rollDurabilityDamage(stack, amount, entity);
        }
    }

    static final class Axe extends AxeItem {
        Axe(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline, Item.Properties properties) {
            super(material, attackDamageBaseline, attackSpeedBaseline, properties);
        }

        @Override
        public <T extends LivingEntity> int damageItem(
                ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
            return rollDurabilityDamage(stack, amount, entity);
        }
    }

    static final class Shovel extends ShovelItem {
        Shovel(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline, Item.Properties properties) {
            super(material, attackDamageBaseline, attackSpeedBaseline, properties);
        }

        @Override
        public <T extends LivingEntity> int damageItem(
                ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
            return rollDurabilityDamage(stack, amount, entity);
        }
    }
}
