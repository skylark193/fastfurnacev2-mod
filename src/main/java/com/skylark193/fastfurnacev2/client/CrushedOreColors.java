package com.skylark193.fastfurnacev2.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.skylark193.fastfurnacev2.CrushedOreItem;
import com.skylark193.fastfurnacev2.CrushedOres;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

final class CrushedOreColors extends SimplePreparableReloadListener<Map<Identifier, Integer>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FALLBACK_TINT = ARGB.color(230, 230, 230);
    private static volatile Map<Identifier, Integer> colors = Map.of();

    static int get(Identifier itemId) {
        return colors.getOrDefault(itemId, FALLBACK_TINT);
    }

    @Override
    protected Map<Identifier, Integer> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, Integer> loaded = new HashMap<>();
        for (CrushedOreItem item : CrushedOres.all()) {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            Identifier rawOreId = item.getRawOreId();
            Identifier texture = Identifier.fromNamespaceAndPath(
                    rawOreId.getNamespace(), "textures/item/" + rawOreId.getPath() + ".png");
            try (var resource = manager.getResource(texture).orElseThrow().open();
                    NativeImage image = NativeImage.read(resource)) {
                loaded.put(itemId, averageTint(image));
            } catch (IOException | RuntimeException exception) {
                loaded.put(itemId, FALLBACK_TINT);
                LOGGER.warn("Using neutral crushed-ore tint because {} could not be read", texture, exception);
            }
        }
        return loaded;
    }

    @Override
    protected void apply(Map<Identifier, Integer> loaded, ResourceManager manager, ProfilerFiller profiler) {
        colors = Map.copyOf(loaded);
        LOGGER.info("Loaded {} crushed-ore texture colors", loaded.size());
    }

    static int averageTint(NativeImage image) {
        long red = 0;
        long green = 0;
        long blue = 0;
        int count = 0;
        int frameSize = Math.min(image.getWidth(), image.getHeight());
        for (int y = 0; y < frameSize; y++) {
            for (int x = 0; x < frameSize; x++) {
                int pixel = image.getPixel(x, y);
                if (ARGB.alpha(pixel) < 128) {
                    continue;
                }
                red += ARGB.red(pixel);
                green += ARGB.green(pixel);
                blue += ARGB.blue(pixel);
                count++;
            }
        }
        if (count == 0) {
            return FALLBACK_TINT;
        }
        int averageRed = (int) ((red + count / 2) / count);
        int averageGreen = (int) ((green + count / 2) / count);
        int averageBlue = (int) ((blue + count / 2) / count);
        int maximum = Math.max(averageRed, Math.max(averageGreen, averageBlue));
        if (maximum == 0) {
            return FALLBACK_TINT;
        }
        return ARGB.color(
                Math.round(averageRed * 230.0F / maximum),
                Math.round(averageGreen * 230.0F / maximum),
                Math.round(averageBlue * 230.0F / maximum));
    }
}
