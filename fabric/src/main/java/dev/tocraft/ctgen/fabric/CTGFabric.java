package dev.tocraft.ctgen.fabric;

import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.data.BiomeImageRegistry;
import dev.tocraft.ctgen.data.HeightImageRegistry;
import dev.tocraft.ctgen.impl.CTGCommand;
import dev.tocraft.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.ctgen.worldgen.MapBasedBiomeSource;
import dev.tocraft.ctgen.worldgen.MapBasedChunkGenerator;
import dev.tocraft.ctgen.worldgen.noise.CTGAboveSurfaceCondition;
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
        BiomeImageRegistry biomeImageRegistry = new BiomeImageRegistry();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return BiomeImageRegistry.ID;
            }

            @Override
            public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor) {
                return biomeImageRegistry.reload(preparationBarrier, resourceManager, backgroundExecutor, gameExecutor);
            }

            @Override
            public @NotNull String getName() {
                return biomeImageRegistry.getName();
            }
        });
        HeightImageRegistry heightImageRegistry = new HeightImageRegistry();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return HeightImageRegistry.ID;
            }

            @Override
            public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor) {
                return heightImageRegistry.reload(preparationBarrier, resourceManager, backgroundExecutor, gameExecutor);
            }

            @Override
            public @NotNull String getName() {
                return heightImageRegistry.getName();
            }
        });

        // register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> CTGCommand.register(dispatcher, context));

        // register built-in registry entries
        CTGAboveSurfaceCondition.register((id, codec) -> Registry.register(BuiltInRegistries.MATERIAL_CONDITION, id, codec));

        // register network packet type
        PayloadTypeRegistry.playS2C().register(SyncMapPacket.TYPE, SyncMapPacket.streamCodec());
    }
}
