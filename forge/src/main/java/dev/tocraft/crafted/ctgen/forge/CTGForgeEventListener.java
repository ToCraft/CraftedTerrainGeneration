package dev.tocraft.crafted.ctgen.forge;

import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.data.MapImageRegistry;
import dev.tocraft.crafted.ctgen.impl.CTGCommand;
import dev.tocraft.crafted.ctgen.worldgen.MapBasedBiomeSource;
import dev.tocraft.crafted.ctgen.worldgen.MapBasedChunkGenerator;
import dev.tocraft.crafted.ctgen.xtend.CTRegistries;
import dev.tocraft.crafted.ctgen.xtend.layer.BlockLayer;
import dev.tocraft.crafted.ctgen.xtend.placer.BlockPlacer;
import dev.tocraft.crafted.ctgen.xtend.terrain.TerrainHeight;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("unused")
public class CTGForgeEventListener {
    private static final List<PreparableReloadListener> RELOAD_LISTENERS = new CopyOnWriteArrayList<>() {
        {
            add(new MapImageRegistry());
        }
    };

    @SubscribeEvent
    public void addReloadListenerEvent(AddReloadListenerEvent event) {
        for (PreparableReloadListener reloadListener : RELOAD_LISTENERS) {
            event.addListener(reloadListener);
        }
    }

    @SubscribeEvent
    public void registerCommands(@NotNull RegisterCommandsEvent event) {
        CTGCommand.register(event.getDispatcher(), event.getBuildContext());
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void register(RegisterEvent event) {
        // generic stuff
        Registry.register(BuiltInRegistries.BIOME_SOURCE, CTerrainGeneration.id("map_based_biome_source"), MapBasedBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, CTerrainGeneration.id("map_based_chunk_generator"), MapBasedChunkGenerator.CODEC);

        // custom built-in registries
        Registry.register((Registry<Registry<?>>) BuiltInRegistries.REGISTRY, (ResourceKey<Registry<?>>) (ResourceKey<?>) CTRegistries.BLOCK_PLAYER_KEY, CTRegistries.BLOCK_PLACER);
        Registry.register((Registry<Registry<?>>) BuiltInRegistries.REGISTRY, (ResourceKey<Registry<?>>) (ResourceKey<?>) CTRegistries.BLOCK_LAYER_KEY, CTRegistries.BLOCK_LAYER);
        Registry.register((Registry<Registry<?>>) BuiltInRegistries.REGISTRY, (ResourceKey<Registry<?>>) (ResourceKey<?>) CTRegistries.TERRAIN_KEY, CTRegistries.TERRAIN);

        // values for the built-in registries
        BlockPlacer.register();
        BlockLayer.register();
        TerrainHeight.register();
    }
}
