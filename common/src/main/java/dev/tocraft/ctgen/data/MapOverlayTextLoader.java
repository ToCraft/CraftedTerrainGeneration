package dev.tocraft.ctgen.data;

import com.mojang.serialization.Codec;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.impl.screen.MapText;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class MapOverlayTextLoader extends SimpleJsonResourceReloadListener<List<MapText>> {
    public static final ResourceLocation ID = CTerrainGeneration.id("text_overlay_loader");
    public static final Map<ResourceLocation, List<MapText>> ENTRIES = new HashMap<>();

    public MapOverlayTextLoader() {
        super(Codec.list(MapText.CODEC), new FileToIdConverter("map_texts", ".json"));
    }

    @Override
    protected void apply(Map<ResourceLocation, List<MapText>> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        ENTRIES.clear();
        ENTRIES.putAll(map);
    }
}
