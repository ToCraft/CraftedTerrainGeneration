package dev.tocraft.ctgen.data;

import com.mojang.logging.LogUtils;
import dev.tocraft.ctgen.CTerrainGeneration;
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

public class BiomeImageRegistry extends SimplePreparableReloadListener<Map<ResourceLocation, BufferedImage>> {
    public static final ResourceLocation ID = CTerrainGeneration.id("biome_map_image_listener");
    private static final Map<ResourceLocation, BufferedImage> MAPS = new ConcurrentHashMap<>();
    private static final String DIRECTORY = "worldgen/map_based/biomes";

    @Override
    protected @NotNull Map<ResourceLocation, BufferedImage> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, BufferedImage> preparedMaps = new HashMap<>();

        FileToIdConverter converter = new FileToIdConverter(DIRECTORY, ".png");
        for (Map.Entry<ResourceLocation, Resource> entry : converter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation id = converter.fileToId(entry.getKey());
            try (InputStream is = entry.getValue().open()) {
                BufferedImage image = ImageIO.read(is);
                preparedMaps.put(id, image);
                LogUtils.getLogger().info("Registered biome map image: {}", id);
            } catch (IOException e) {
                LogUtils.getLogger().error("Caught an error while reading map image {}", id, e);
            }
        }
        return preparedMaps;
    }

    @Override
    protected void apply(Map<ResourceLocation, BufferedImage> preparedMaps, ResourceManager resourceManager, ProfilerFiller profiler) {
        MAPS.clear();
        MAPS.putAll(preparedMaps);
    }

    @Nullable
    public static BufferedImage getById(ResourceLocation id) {
        return MAPS.get(id);
    }
}
