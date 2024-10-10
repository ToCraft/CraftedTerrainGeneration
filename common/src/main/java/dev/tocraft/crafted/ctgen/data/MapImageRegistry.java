package dev.tocraft.crafted.ctgen.data;

import com.mojang.logging.LogUtils;
import dev.tocraft.crafted.ctgen.zone.Zone;
import dev.tocraft.crafted.ctgen.util.MapUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class MapImageRegistry extends SimplePreparableReloadListener<Map<ResourceLocation, BufferedImage>> {
    private static final Map<ResourceLocation, BufferedImage> MAPS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, BufferedImage> UPSCALED_MAPS = new ConcurrentHashMap<>();
    private static final String DIRECTORY = "worldgen/map_based/maps";

    @Override
    protected @NotNull Map<ResourceLocation, BufferedImage> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, BufferedImage> preparedMaps = new HashMap<>();

        FileToIdConverter converter = new FileToIdConverter(DIRECTORY, ".png");
        for (Map.Entry<ResourceLocation, Resource> entry : converter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation id = converter.fileToId(entry.getKey());
            try (InputStream is = entry.getValue().open()) {
                BufferedImage image = ImageIO.read(is);
                preparedMaps.put(id, image);
                LogUtils.getLogger().info("Registered map image: {}", id);
            } catch (IOException e) {
                LogUtils.getLogger().error("Caught an error while reading map image {}", id, e);
            }
        }
        return preparedMaps;
    }

    @Override
    protected void apply(Map<ResourceLocation, BufferedImage> preparedMaps, ResourceManager resourceManager, ProfilerFiller profiler) {
        MAPS.putAll(preparedMaps);
    }

    @Nullable
    public static BufferedImage getByIdOrUpscale(ResourceLocation id, boolean pixelsAreChunks, Supplier<Iterable<Zone>> zones) {
        if (pixelsAreChunks) {
            if (UPSCALED_MAPS.containsKey(id)) {
                return UPSCALED_MAPS.get(id);
            } else {
                BufferedImage original = MAPS.get(id);
                if (original != null) {
                    return UPSCALED_MAPS.put(id, MapUtils.generateDetailedMap(original, zones.get()));
                } else {
                    return null;
                }
            }
        } else {
            return MAPS.get(id);
        }
    }
}
