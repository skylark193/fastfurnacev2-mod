package com.skylark193.fastfurnacev2.api;

import java.util.Optional;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantable;
import org.jetbrains.annotations.ApiStatus;

/** A patchable snapshot of vanilla-style tool defaults. */
public record FastToolStats(
        int durability,
        float miningSpeedMultiplier,
        double attackDamage,
        double attackSpeed,
        int enchantmentValue) {
    public FastToolStats {
        if (durability <= 0) {
            throw new IllegalArgumentException("Tool durability must be positive");
        }
        if (!Float.isFinite(miningSpeedMultiplier) || miningSpeedMultiplier <= 0.0F) {
            throw new IllegalArgumentException("Mining-speed multiplier must be positive and finite");
        }
        if (!Double.isFinite(attackDamage) || attackDamage < 0.0) {
            throw new IllegalArgumentException("Attack damage must be nonnegative and finite");
        }
        if (!Double.isFinite(attackSpeed) || attackSpeed <= 0.0) {
            throw new IllegalArgumentException("Attack speed must be positive and finite");
        }
        if (enchantmentValue <= 0) {
            throw new IllegalArgumentException("Enchantment value must be positive");
        }
    }

    public FastToolStats withDurability(int durability) {
        return new FastToolStats(
                durability, this.miningSpeedMultiplier, this.attackDamage, this.attackSpeed, this.enchantmentValue);
    }

    /** Multiplies every finite mining-speed rule, preserving their relative speeds. */
    public FastToolStats withMiningSpeedMultiplier(float miningSpeedMultiplier) {
        return new FastToolStats(
                this.durability, miningSpeedMultiplier, this.attackDamage, this.attackSpeed, this.enchantmentValue);
    }

    /** Sets total main-hand attack damage, including the player's base damage of one. */
    public FastToolStats withAttackDamage(double attackDamage) {
        return new FastToolStats(
                this.durability, this.miningSpeedMultiplier, attackDamage, this.attackSpeed, this.enchantmentValue);
    }

    /** Sets total main-hand attacks per second, including the player's base speed of four. */
    public FastToolStats withAttackSpeed(double attackSpeed) {
        return new FastToolStats(
                this.durability, this.miningSpeedMultiplier, this.attackDamage, attackSpeed, this.enchantmentValue);
    }

    public FastToolStats withEnchantmentValue(int enchantmentValue) {
        return new FastToolStats(
                this.durability, this.miningSpeedMultiplier, this.attackDamage, this.attackSpeed, enchantmentValue);
    }

    @ApiStatus.Internal
    public static FastToolStats from(DataComponentMap.Builder components) {
        Integer durability = components.get(DataComponents.MAX_DAMAGE);
        Enchantable enchantable = components.get(DataComponents.ENCHANTABLE);
        ItemAttributeModifiers attributes = components.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (attributes == null) {
            attributes = ItemAttributeModifiers.EMPTY;
        }
        return new FastToolStats(
                durability == null ? 1 : Math.max(1, durability),
                1.0F,
                attributes.compute(Attributes.ATTACK_DAMAGE, 1.0, EquipmentSlot.MAINHAND),
                attributes.compute(Attributes.ATTACK_SPEED, 4.0, EquipmentSlot.MAINHAND),
                enchantable == null ? 1 : enchantable.value());
    }

    @ApiStatus.Internal
    public void applyTo(DataComponentMap.Builder components, FastToolStats original) {
        if (this.durability != original.durability) {
            components.set(DataComponents.MAX_DAMAGE, this.durability);
            components.set(DataComponents.MAX_STACK_SIZE, 1);
        }
        if (this.miningSpeedMultiplier != 1.0F) {
            Tool tool = components.get(DataComponents.TOOL);
            if (tool != null) {
                components.set(DataComponents.TOOL, new Tool(
                        tool.rules().stream().map(rule -> scaleRule(rule, this.miningSpeedMultiplier)).toList(),
                        tool.defaultMiningSpeed(), tool.damagePerBlock(), tool.canDestroyBlocksInCreative()));
            }
        }
        if (this.enchantmentValue != original.enchantmentValue) {
            components.set(DataComponents.ENCHANTABLE, new Enchantable(this.enchantmentValue));
        }
        if (this.attackDamage != original.attackDamage) {
            setBaseAttribute(components, Item.BASE_ATTACK_DAMAGE_ID, this.attackDamage - 1.0, true);
        }
        if (this.attackSpeed != original.attackSpeed) {
            setBaseAttribute(components, Item.BASE_ATTACK_SPEED_ID, this.attackSpeed - 4.0, false);
        }
    }

    private static Tool.Rule scaleRule(Tool.Rule rule, float multiplier) {
        if (rule.speed().isEmpty() || !Float.isFinite(rule.speed().get())) {
            return rule;
        }
        float scaledSpeed = Math.min(Float.MAX_VALUE, rule.speed().get() * multiplier);
        return new Tool.Rule(rule.blocks(), Optional.of(scaledSpeed), rule.correctForDrops());
    }

    private static void setBaseAttribute(
            DataComponentMap.Builder components, Identifier id, double amount, boolean attackDamage) {
        ItemAttributeModifiers attributes = components.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (attributes == null) {
            attributes = ItemAttributeModifiers.EMPTY;
        }
        components.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes.withModifierAdded(
                attackDamage ? Attributes.ATTACK_DAMAGE : Attributes.ATTACK_SPEED,
                new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND));
    }
}
