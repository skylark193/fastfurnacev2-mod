package com.skylark193.fastfurnacev2;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jspecify.annotations.Nullable;

final class CrushedOreResources {
    private static final Gson GSON = new Gson();
    private static final String PACK_ID = FastFurnaceV2.MODID + ":crushed_ores";

    private CrushedOreResources() {
    }

    static void addPackFinder(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES || CrushedOres.all().isEmpty()) {
            return;
        }
        PackLocationInfo location = new PackLocationInfo(
                PACK_ID,
                Component.literal("Fast Furnace V2 Crushed Ores"),
                PackSource.BUILT_IN,
                Optional.empty());
        Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo info) {
                return new DynamicPack(info);
            }

            @Override
            public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                return new DynamicPack(info);
            }
        };
        Pack pack = Pack.readMetaAndCreate(location, resources, PackType.CLIENT_RESOURCES,
                new PackSelectionConfig(true, Pack.Position.BOTTOM, false));
        if (pack != null) {
            event.addRepositorySource(output -> output.accept(pack));
        }
    }

    private static final class DynamicPack extends AbstractPackResources {
        private DynamicPack(PackLocationInfo location) {
            super(location);
        }

        @Override
        public @Nullable IoSupplier<InputStream> getRootResource(String... path) {
            if (path.length == 1 && PackResources.PACK_META.equals(path[0])) {
                JsonObject pack = new JsonObject();
                JsonObject section = new JsonObject();
                int packFormat = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES).major();
                section.addProperty("pack_format", packFormat);
                section.addProperty("min_format", packFormat);
                section.addProperty("max_format", packFormat);
                section.addProperty("description", "Generated crushed-ore item definitions");
                pack.add("pack", section);
                return bytes(GSON.toJson(pack));
            }
            return null;
        }

        @Override
        public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier location) {
            if (type != PackType.CLIENT_RESOURCES || !FastFurnaceV2.MODID.equals(location.getNamespace())
                    || !location.getPath().startsWith("items/") || !location.getPath().endsWith(".json")) {
                return null;
            }
            String itemPath = location.getPath().substring(6, location.getPath().length() - 5);
            CrushedOreItem item = CrushedOres.all().stream()
                    .filter(candidate -> BuiltInRegistries.ITEM.getKey(candidate).getPath().equals(itemPath))
                    .findFirst().orElse(null);
            if (item == null) {
                return null;
            }
            JsonObject tint = new JsonObject();
            tint.addProperty("type", FastFurnaceV2.MODID + ":crushed_ore");
            JsonObject model = new JsonObject();
            model.addProperty("type", "minecraft:model");
            model.addProperty("model", FastFurnaceV2.MODID + ":item/crushed_ore");
            JsonArray tints = new JsonArray();
            tints.add(tint);
            model.add("tints", tints);
            JsonObject definition = new JsonObject();
            definition.add("model", model);
            return bytes(GSON.toJson(definition));
        }

        @Override
        public void listResources(PackType type, String namespace, String directory, PackResources.ResourceOutput output) {
            if (type != PackType.CLIENT_RESOURCES || !FastFurnaceV2.MODID.equals(namespace)) {
                return;
            }
            for (CrushedOreItem item : CrushedOres.all()) {
                Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
                Identifier resource = Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "items/" + itemId.getPath() + ".json");
                if (resource.getPath().startsWith(directory)) {
                    IoSupplier<InputStream> supplier = getResource(type, resource);
                    if (supplier != null) {
                        output.accept(resource, supplier);
                    }
                }
            }
        }

        @Override
        public Set<String> getNamespaces(PackType type) {
            return type == PackType.CLIENT_RESOURCES ? Set.of(FastFurnaceV2.MODID) : Set.of();
        }

        @Override
        public void close() {
        }

        private static IoSupplier<InputStream> bytes(String contents) {
            byte[] bytes = contents.getBytes(StandardCharsets.UTF_8);
            return () -> new ByteArrayInputStream(bytes);
        }
    }
}
