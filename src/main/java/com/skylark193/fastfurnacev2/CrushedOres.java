package com.skylark193.fastfurnacev2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;
import net.neoforged.neoforge.registries.IRegistryExtension;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.callback.AddCallback;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.slf4j.Logger;

public final class CrushedOres {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Identifier, CrushedOreItem> BY_RAW_ORE = new LinkedHashMap<>();
    private static final Map<Identifier, CrushedOreItem> BY_ID = new LinkedHashMap<>();
    private static final Set<Identifier> TAGGED_RAW_ORES = new HashSet<>();
    private static final Map<Identifier, List<JsonElement>> RAW_MATERIAL_TAGS = new HashMap<>();
    private static List<Map.Entry<ResourceKey<Item>, Item>> vanillaItems = List.of();
    private static boolean discoveringVanilla;

    private CrushedOres() {
    }

    public static void registerDiscovery(ModifyRegistriesEvent event) {
        Registry<Item> registry = event.getRegistry(Registries.ITEM);
        scanPackagedRawMaterialTags();
        vanillaItems = new ArrayList<>(registry.entrySet());
        AddCallback<Item> callback = (addedRegistry, id, key, item) -> {
            discoverVanilla(addedRegistry);
            discover(addedRegistry, key.identifier(), item);
        };
        ((IRegistryExtension<Item>) registry).addCallback(callback);
    }

    public static void registerItems(RegisterEvent event) {
        Registry<Item> registry = event.getRegistry(Registries.ITEM);
        if (registry == null) {
            return;
        }
        discoverVanilla(registry);
        for (Map.Entry<ResourceKey<Item>, Item> entry : new ArrayList<>(registry.entrySet())) {
            discover(registry, entry.getKey().identifier(), entry.getValue());
        }
        LOGGER.info("Discovered {} raw ore items", BY_RAW_ORE.size());
    }

    private static void discoverVanilla(Registry<Item> registry) {
        if (discoveringVanilla || vanillaItems.isEmpty()) {
            return;
        }
        discoveringVanilla = true;
        try {
            for (Map.Entry<ResourceKey<Item>, Item> entry : vanillaItems) {
                discover(registry, entry.getKey().identifier(), entry.getValue());
            }
        } finally {
            vanillaItems = List.of();
            discoveringVanilla = false;
        }
    }

    public static Collection<CrushedOreItem> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    public static CrushedOreItem forRawOre(Identifier rawOreId) {
        return BY_RAW_ORE.get(rawOreId);
    }

    public static void addCreative(CreativeModeTab.Output output) {
        BY_ID.values().forEach(output::accept);
    }

    public static void generateRecipes(ModifyRecipeJsonsEvent event) {
        if (BY_RAW_ORE.isEmpty()) {
            return;
        }
        Map<Identifier, JsonElement> recipes = event.getRecipeJsons();
        int generated = 0;
        for (Map.Entry<Identifier, JsonElement> entry : new ArrayList<>(recipes.entrySet())) {
            if (!(entry.getValue() instanceof JsonObject source)
                    || !source.has("type") || !source.get("type").isJsonPrimitive()
                    || !"minecraft:smelting".equals(source.get("type").getAsString())
                    || !source.has("ingredient")) {
                continue;
            }
            Ingredient ingredient;
            try {
                ingredient = Ingredient.CODEC.parse(event.getOps(), source.get("ingredient")).getOrThrow();
            } catch (RuntimeException exception) {
                LOGGER.warn("Could not inspect smelting ingredient in {}", entry.getKey(), exception);
                continue;
            }
            for (Map.Entry<Identifier, CrushedOreItem> crushed : BY_RAW_ORE.entrySet()) {
                if (!ingredient.acceptsItem(BuiltInRegistries.ITEM.get(crushed.getKey()).orElseThrow())) {
                    continue;
                }
                Identifier id = generatedRecipeId(crushed.getValue(), entry.getKey());
                if (recipes.containsKey(id)) {
                    LOGGER.warn("Skipping duplicate generated crushed-ore recipe {}", id);
                    continue;
                }
                JsonObject recipe = source.deepCopy();
                recipe.addProperty("ingredient", BuiltInRegistries.ITEM.getKey(crushed.getValue()).toString());
                recipes.put(id, recipe);
                generated++;
            }
        }
        LOGGER.info("Generated {} crushed-ore smelting recipes", generated);
    }

    private static void discover(Registry<Item> registry, Identifier rawOreId, Item rawOre) {
        if (rawOre instanceof BlockItem) {
            return;
        }
        if (!rawOreId.getPath().startsWith("raw_") && !rawOreId.getPath().contains("/raw_")
                && !rawOreId.getPath().endsWith("_raw") && !TAGGED_RAW_ORES.contains(rawOreId)) {
            return;
        }
        if (BY_RAW_ORE.containsKey(rawOreId)) {
            return;
        }
        String material = materialName(rawOreId.getPath());
        String path = rawOreId.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)
                ? "crushed_" + material
                : "crushed_" + rawOreId.getNamespace() + "_" + material;
        Identifier id = Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, path.replace('/', '_'));
        if (registry.containsKey(id)) {
            LOGGER.warn("Cannot create crushed ore for {} because {} is already registered", rawOreId, id);
            return;
        }
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        CrushedOreItem crushedOre = new CrushedOreItem(key, rawOreId, material);
        BY_RAW_ORE.put(rawOreId, crushedOre);
        BY_ID.put(id, crushedOre);
        Registry.register(registry, key, crushedOre);
        LOGGER.debug("Created crushed ore {} from {}", id, rawOreId);
    }

    private static void scanPackagedRawMaterialTags() {
        PackLocationInfo location = new PackLocationInfo(
                FastFurnaceV2.MODID + ":raw_tag_scan", Component.empty(), PackSource.DEFAULT, Optional.empty());
        for (var modFile : ModList.get().getModFiles()) {
            try (PackResources pack = ResourcePackLoader.createPackForMod(modFile).openPrimary(location)) {
                for (String namespace : pack.getNamespaces(PackType.SERVER_DATA)) {
                    Map<Identifier, IoSupplier<InputStream>> resources = new HashMap<>();
                    pack.listResources(PackType.SERVER_DATA, namespace, "tags/item", (id, resource) -> {
                        if (id.getPath().matches("tags/item/raw_materials(?:/.+)?\\.json")) {
                            resources.put(id, resource);
                        }
                    });
                    for (var resource : resources.entrySet()) {
                        Identifier tagId = Identifier.fromNamespaceAndPath(resource.getKey().getNamespace(),
                                resource.getKey().getPath().substring(10, resource.getKey().getPath().length() - 5));
                        try (InputStream input = resource.getValue().get();
                                InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                            JsonObject tag = JsonParser.parseReader(reader).getAsJsonObject();
                            if (tag.get("values") instanceof com.google.gson.JsonArray values) {
                                RAW_MATERIAL_TAGS.computeIfAbsent(tagId, ignored -> new ArrayList<>()).addAll(values.asList());
                            }
                        }
                    }
                }
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("Could not inspect packaged raw-material tags from {}", modFile.getFile().getFileName(), exception);
            }
        }
        for (Identifier tagId : RAW_MATERIAL_TAGS.keySet()) {
            collectTaggedRawOres(tagId, new HashSet<>());
        }
    }

    private static void collectTaggedRawOres(Identifier tagId, Set<Identifier> visiting) {
        if (!visiting.add(tagId)) {
            return;
        }
        for (JsonElement element : RAW_MATERIAL_TAGS.getOrDefault(tagId, List.of())) {
            String value = element.isJsonPrimitive() ? element.getAsString()
                    : element instanceof JsonObject object && object.has("id") ? object.get("id").getAsString() : "";
            if (value.startsWith("#")) {
                Identifier nested = Identifier.tryParse(value.substring(1));
                if (nested != null) {
                    collectTaggedRawOres(nested, visiting);
                }
            } else {
                Identifier item = Identifier.tryParse(value);
                if (item != null) {
                    TAGGED_RAW_ORES.add(item);
                }
            }
        }
        visiting.remove(tagId);
    }

    private static String materialName(String rawPath) {
        int slash = rawPath.lastIndexOf('/');
        String fileName = rawPath.substring(slash + 1);
        String material = fileName.startsWith("raw_")
                ? fileName.substring(4)
                : fileName.endsWith("_raw") ? fileName.substring(0, fileName.length() - 4) : fileName;
        String directory = rawPath.substring(0, slash + 1).replace('/', '_');
        return directory + material;
    }

    private static Identifier generatedRecipeId(CrushedOreItem item, Identifier sourceRecipe) {
        String itemPath = BuiltInRegistries.ITEM.getKey(item).getPath();
        return Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID,
                itemPath + "_from_" + sourceRecipe.getNamespace() + "_" + sourceRecipe.getPath().replace('/', '_'));
    }
}
