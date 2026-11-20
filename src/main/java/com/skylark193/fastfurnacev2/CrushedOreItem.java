package com.skylark193.fastfurnacev2;

import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CrushedOreItem extends Item {
    private final Identifier rawOreId;
    private final Component materialName;

    CrushedOreItem(ResourceKey<Item> key, Identifier rawOreId, String material) {
        super(new Item.Properties().setId(key));
        this.rawOreId = rawOreId;
        this.materialName = createMaterialName(rawOreId, material);
    }

    public Identifier getRawOreId() {
        return this.rawOreId;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.fastfurnacev2.crushed_ore", this.materialName);
    }

    static Component createMaterialName(Identifier rawOreId, String material) {
        String displayMaterial = displayMaterialName(material);
        String materialKey = "material." + rawOreId.getNamespace() + "." + displayMaterial.replace('/', '.');
        return Component.translatableWithFallback(materialKey, humanize(displayMaterial));
    }

    static String displayMaterialName(String material) {
        if (material.endsWith("_raw_ore")) {
            return material.substring(0, material.length() - "_raw_ore".length());
        }
        if (material.endsWith("_ore")) {
            return material.substring(0, material.length() - "_ore".length());
        }
        return material;
    }

    private static String humanize(String material) {
        String[] words = material.replace('/', '_').split("_");
        StringBuilder name = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        return name.toString();
    }
}
