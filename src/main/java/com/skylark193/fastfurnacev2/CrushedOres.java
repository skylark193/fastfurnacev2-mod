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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;
import net.neoforged.neoforge.registries.IRegistryExtension;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.callback.AddCallback;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.slf4j.Logger;

public final class CrushedOres {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Identifier, Identifier> BY_RAW_ORE = new LinkedHashMap<>();
    private static final Map<Identifier, CrushedOreItem> BY_ID = new LinkedHashMap<>();
    private static final Set<Identifier> TAGGED_RAW_ORES = new HashSet<>();
    private static final Set<Identifier> BUILT_IN_RAW_ORES = Set.of(
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "raw_fast_iron"),
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "raw_fast_gold"),
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "raw_fast_diamond"));
    private static final Set<Identifier> KNOWN_CRUSHED_ORES = new HashSet<>();
    private static final Set<Identifier> TAGGED_CRUSHED_ORES = new HashSet<>();
    private static final Map<Identifier, List<JsonElement>> PACKAGED_ITEM_TAGS = new HashMap<>();
    private static List<Map.Entry<ResourceKey<Item>, Item>> vanillaItems = List.of();
    private static boolean discoveringVanilla;

    private CrushedOres() {
    }

    public static void registerDiscovery(ModifyRegistriesEvent event) {
        Registry<Item> registry = event.getRegistry(Registries.ITEM);
        scanPackagedTags();
        vanillaItems = new ArrayList<>(registry.entrySet());
        AddCallback<Item> callback = (addedRegistry, id, key, item) -> {
            rememberExistingCrushedOre(key.identifier(), item);
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
        for (Map.Entry<ResourceKey<Item>, Item> entry : new ArrayList<>(registry.entrySet())) {
            rememberExistingCrushedOre(entry.getKey().identifier(), entry.getValue());
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

    public static Set<Identifier> rawOres() {
        return Collections.unmodifiableSet(BY_RAW_ORE.keySet());
    }

    public static Item forRawOre(Identifier rawOreId) {
        Identifier crushedOreId = BY_RAW_ORE.get(rawOreId);
        return crushedOreId == null ? null
                : BuiltInRegistries.ITEM.get(crushedOreId).map(holder -> holder.value()).orElse(null);
    }

    public static void addCreative(CreativeModeTab.Output output) {
        BY_ID.values().forEach(output::accept);
    }

    public static void generateRecipes(ModifyRecipeJsonsEvent event) {
        if (BY_RAW_ORE.isEmpty()) {
            return;
        }
        Map<Identifier, JsonElement> recipes = event.getRecipeJsons();
        List<ParsedSmeltingRecipe> sources = new ArrayList<>();
        for (Map.Entry<Identifier, JsonElement> entry : new ArrayList<>(recipes.entrySet())) {
            if (!(entry.getValue() instanceof JsonObject source)
                    || !ICondition.conditionsMatched(event.getOps(), source)
                    || !source.has("type") || !source.get("type").isJsonPrimitive()
                    || !"minecraft:smelting".equals(source.get("type").getAsString())
                    || !source.has("ingredient")) {
                continue;
            }
            try {
                Ingredient ingredient = Ingredient.CODEC.parse(event.getOps(), source.get("ingredient")).getOrThrow();
                sources.add(new ParsedSmeltingRecipe(entry.getKey(), source, ingredient));
            } catch (RuntimeException exception) {
                LOGGER.warn("Could not inspect smelting ingredient in {}", entry.getKey(), exception);
            }
        }

        Set<Identifier> externalItemsWithRecipes = new HashSet<>();
        for (ParsedSmeltingRecipe source : sources) {
            for (Identifier crushedOreId : BY_RAW_ORE.values()) {
                if (BY_ID.containsKey(crushedOreId)) {
                    continue;
                }
                Holder<Item> crushedOre = BuiltInRegistries.ITEM.get(crushedOreId).orElse(null);
                if (crushedOre != null && source.ingredient().acceptsItem(crushedOre)) {
                    externalItemsWithRecipes.add(crushedOreId);
                }
            }
        }

        int generated = 0;
        for (ParsedSmeltingRecipe source : sources) {
            for (Map.Entry<Identifier, Identifier> crushed : BY_RAW_ORE.entrySet()) {
                Holder<Item> rawOre = BuiltInRegistries.ITEM.get(crushed.getKey()).orElse(null);
                Holder<Item> crushedOre = BuiltInRegistries.ITEM.get(crushed.getValue()).orElse(null);
                if (rawOre == null || crushedOre == null || !source.ingredient().acceptsItem(rawOre)
                        || externalItemsWithRecipes.contains(crushed.getValue())) {
                    continue;
                }
                Identifier id = generatedRecipeId(crushed.getValue(), source.id());
                if (recipes.containsKey(id)) {
                    LOGGER.warn("Skipping duplicate generated crushed-ore recipe {}", id);
                    continue;
                }
                JsonObject recipe = source.json().deepCopy();
                recipe.addProperty("ingredient", crushed.getValue().toString());
                recipes.put(id, recipe);
                generated++;
            }
        }
        LOGGER.info("Generated {} crushed-ore smelting recipes", generated);
    }

    static boolean isDiscoverableRawOre(Identifier rawOreId, Item rawOre) {
        // Generated IDs can retain a source's _raw_ore suffix; never feed them back into discovery.
        if (rawOre instanceof BlockItem
                || FastFurnaceV2.MODID.equals(rawOreId.getNamespace()) && !BUILT_IN_RAW_ORES.contains(rawOreId)) {
            return false;
        }
        return rawOreId.getPath().startsWith("raw_") || rawOreId.getPath().contains("/raw_")
                || rawOreId.getPath().endsWith("_raw") || rawOreId.getPath().endsWith("_raw_ore")
                || TAGGED_RAW_ORES.contains(rawOreId);
    }

    private static void discover(Registry<Item> registry, Identifier rawOreId, Item rawOre) {
        if (!isDiscoverableRawOre(rawOreId, rawOre)) {
            return;
        }
        if (BY_RAW_ORE.containsKey(rawOreId)) {
            return;
        }
        String rawPath = rawOreId.getPath();
        String material = materialName(rawPath);
        String externalMaterial = materialName(rawPath.substring(rawPath.lastIndexOf('/') + 1));
        Identifier existing = findExistingCrushedOre(rawOreId, externalMaterial);
        if (existing != null) {
            BY_RAW_ORE.put(rawOreId, existing);
            LOGGER.debug("Using existing crushed ore {} for {}", existing, rawOreId);
            return;
        }

        String path = rawOreId.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) || BUILT_IN_RAW_ORES.contains(rawOreId)
                ? "crushed_" + material
                : "crushed_" + rawOreId.getNamespace() + "_" + material;
        Identifier id = Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, path.replace('/', '_'));
        if (registry.containsKey(id)) {
            id = Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, id.getPath() + "_" + stableSuffix(rawOreId));
        }
        if (registry.containsKey(id)) {
            LOGGER.warn("Cannot create crushed ore for {} because {} is already registered", rawOreId, id);
            return;
        }
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        CrushedOreItem crushedOre = new CrushedOreItem(key, rawOreId, material);
        BY_RAW_ORE.put(rawOreId, id);
        BY_ID.put(id, crushedOre);
        Registry.register(registry, key, crushedOre);
        LOGGER.debug("Created crushed ore {} from {}", id, rawOreId);
    }

    private static void rememberExistingCrushedOre(Identifier itemId, Item item) {
        if (!(item instanceof BlockItem) && !FastFurnaceV2.MODID.equals(itemId.getNamespace())
                && isPotentialCrushedOrePath(itemId.getPath())
                && KNOWN_CRUSHED_ORES.add(itemId)) {
            preferRegisteredCrushedOres();
        }
    }

    private static void preferRegisteredCrushedOres() {
        for (Map.Entry<Identifier, Identifier> entry : BY_RAW_ORE.entrySet()) {
            Identifier rawOreId = entry.getKey();
            String rawPath = rawOreId.getPath();
            String material = materialName(rawPath.substring(rawPath.lastIndexOf('/') + 1));
            Identifier registered = findExistingCrushedOre(rawOreId, material);
            if (registered != null && !registered.equals(entry.getValue())) {
                Identifier previous = entry.setValue(registered);
                BY_ID.remove(previous);
                LOGGER.debug("Replaced provisional or lower-priority {} with registered crushed ore {} for {}",
                        previous, registered, rawOreId);
            }
        }
    }

    private static Identifier findExistingCrushedOre(Identifier rawOreId, String material) {
        return KNOWN_CRUSHED_ORES.stream()
                .filter(id -> isCrushedOreForMaterial(id, material))
                .min(Comparator.comparing((Identifier id) -> !id.getNamespace().equals(rawOreId.getNamespace()))
                        .thenComparing(id -> !TAGGED_CRUSHED_ORES.contains(id))
                        .thenComparing(id -> id.toString()))
                .orElse(null);
    }

    static boolean isCrushedOreForMaterial(Identifier candidate, String rawMaterial) {
        if (FastFurnaceV2.MODID.equals(candidate.getNamespace())) {
            return false;
        }
        String material = CrushedOreItem.displayMaterialName(rawMaterial);
        if (material.isEmpty()) {
            return false;
        }
        String path = candidate.getPath().replace('/', '_');
        return path.equals("crushed_raw_" + material)
                || path.equals("crushed_raw_" + material + "_ore")
                || path.equals("crushed_" + material)
                || path.equals("crushed_" + material + "_ore")
                || path.equals(material + "_crushed")
                || path.equals(material + "_crushed_raw")
                || path.equals(material + "_crushed_raw_ore")
                || path.equals(material + "_crushed_ore")
                || path.equals("dust_" + material)
                || path.equals("dust_" + material + "_ore")
                || path.equals(material + "_dust")
                || path.equals(material + "_dust_ore");
    }

    private static boolean isPotentialCrushedOrePath(String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        return fileName.contains("crushed") || fileName.contains("dust");
    }

    private static void scanPackagedTags() {
        PackLocationInfo location = new PackLocationInfo(
                FastFurnaceV2.MODID + ":raw_tag_scan", Component.empty(), PackSource.DEFAULT, Optional.empty());
        for (var modFile : ModList.get().getModFiles()) {
            try (PackResources pack = ResourcePackLoader.createPackForMod(modFile).openPrimary(location)) {
                for (String namespace : pack.getNamespaces(PackType.SERVER_DATA)) {
                    Map<Identifier, IoSupplier<InputStream>> resources = new HashMap<>();
                    pack.listResources(PackType.SERVER_DATA, namespace, "tags/item", (id, resource) -> {
                        if (id.getPath().startsWith("tags/item/") && id.getPath().endsWith(".json")) {
                            resources.put(id, resource);
                        }
                    });
                    for (var resource : resources.entrySet()) {
                        Identifier tagId = Identifier.fromNamespaceAndPath(resource.getKey().getNamespace(),
                                resource.getKey().getPath().substring(10, resource.getKey().getPath().length() - 5));
                        try (InputStream input = resource.getValue().get();
                                InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                            JsonObject tag = JsonParser.parseReader(reader).getAsJsonObject();
                            List<JsonElement> values = PACKAGED_ITEM_TAGS.computeIfAbsent(
                                    tagId, ignored -> new ArrayList<>());
                            if (tag.get("replace") instanceof com.google.gson.JsonPrimitive replace
                                    && replace.isBoolean() && replace.getAsBoolean()) {
                                values.clear();
                            }
                            if (tag.get("values") instanceof com.google.gson.JsonArray entries) {
                                values.addAll(entries.asList());
                            }
                        }
                    }
                }
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("Could not inspect packaged ore tags from {}",
                        modFile.getFile().getFileName(), exception);
            }
        }
        for (Identifier tagId : PACKAGED_ITEM_TAGS.keySet()) {
            if (!"c".equals(tagId.getNamespace())) {
                continue;
            }
            String path = tagId.getPath();
            if (path.equals("raw_materials") || path.startsWith("raw_materials/")) {
                collectTaggedItems(tagId, new HashSet<>(), TAGGED_RAW_ORES, false);
            } else if (path.equals("dusts") || path.startsWith("dusts/")
                    || path.equals("crushed_ores") || path.startsWith("crushed_ores/")) {
                collectTaggedItems(tagId, new HashSet<>(), TAGGED_CRUSHED_ORES, true);
            }
        }
    }

    private static void collectTaggedItems(
            Identifier tagId, Set<Identifier> visiting, Set<Identifier> destination, boolean requiredOnly) {
        if (!visiting.add(tagId)) {
            return;
        }
        for (JsonElement element : PACKAGED_ITEM_TAGS.getOrDefault(tagId, List.of())) {
            if (requiredOnly && element instanceof JsonObject object
                    && object.has("required") && !object.get("required").getAsBoolean()) {
                continue;
            }
            String value = element.isJsonPrimitive() ? element.getAsString()
                    : element instanceof JsonObject object && object.has("id") ? object.get("id").getAsString() : "";
            if (value.startsWith("#")) {
                Identifier nested = Identifier.tryParse(value.substring(1));
                if (nested != null) {
                    collectTaggedItems(nested, visiting, destination, requiredOnly);
                }
            } else {
                Identifier item = Identifier.tryParse(value);
                if (item != null) {
                    destination.add(item);
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

    static Identifier generatedRecipeId(Identifier crushedOreId, Identifier sourceRecipe) {
        String itemPath = BY_ID.containsKey(crushedOreId)
                ? crushedOreId.getPath()
                : (crushedOreId.getNamespace() + "_" + crushedOreId.getPath()).replace('/', '_');
        return Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID,
                itemPath + "_from_" + sourceRecipe.getNamespace() + "_"
                        + sourceRecipe.getPath().replace('/', '_') + "_"
                        + stableSuffix(crushedOreId, sourceRecipe));
    }

    private static String stableSuffix(Identifier... ids) {
        StringBuilder value = new StringBuilder();
        for (Identifier id : ids) {
            if (!value.isEmpty()) {
                value.append('|');
            }
            value.append(id);
        }
        return UUID.nameUUIDFromBytes(value.toString().getBytes(StandardCharsets.UTF_8)).toString();
    }

    private record ParsedSmeltingRecipe(Identifier id, JsonObject json, Ingredient ingredient) {
    }
}
