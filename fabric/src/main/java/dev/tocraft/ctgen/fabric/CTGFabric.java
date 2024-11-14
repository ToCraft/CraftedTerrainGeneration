package dev.tocraft.ctgen.fabric;

import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.data.MapImageRegistry;
import dev.tocraft.ctgen.impl.CTGCommand;
import dev.tocraft.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.ctgen.worldgen.MapBasedBiomeSource;
import dev.tocraft.ctgen.worldgen.MapBasedChunkGenerator;
import dev.tocraft.ctgen.xtend.CTRegistries;
import dev.tocraft.ctgen.xtend.carver.Carver;
import dev.tocraft.ctgen.xtend.height.TerrainHeight;
import dev.tocraft.ctgen.xtend.layer.BlockLayer;
import dev.tocraft.ctgen.xtend.placer.BlockPlacer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SuppressWarnings("unused")
@ApiStatus.Internal
public final class CTGFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // register chunk generator
        Registry.register(BuiltInRegistries.BIOME_SOURCE, MapBasedBiomeSource.ID, MapBasedBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, MapBasedChunkGenerator.ID, MapBasedChunkGenerator.CODEC);

        // register reload listener
        MapImageRegistry reloadListener = new MapImageRegistry();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return CTerrainGeneration.id("map_image_listener");
            }

            @Override
            public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor) {
                return reloadListener.reload(preparationBarrier, resourceManager, backgroundExecutor, gameExecutor);
            }

            @Override
            public @NotNull String getName() {
                return reloadListener.getName();
            }
        });

        // register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> CTGCommand.register(dispatcher, context));

        // register built-in registry entries
        BlockPlacer.register((id, codec) -> Registry.register(CTRegistries.BLOCK_PLACER, id, codec));
        BlockLayer.register((id, codec) -> Registry.register(CTRegistries.BLOCK_LAYER, id, codec));
        TerrainHeight.register((id, codec) -> Registry.register(CTRegistries.TERRAIN, id, codec));
        Carver.register((id, codec) -> Registry.register(CTRegistries.CARVER, id, codec));

        // register network packet type
        PayloadTypeRegistry.playS2C().register(SyncMapPacket.TYPE, SyncMapPacket.streamCodec());
    }
}
