package dev.tocraft.crafted.ctgen.fabric;

import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.data.MapImageRegistry;
import dev.tocraft.crafted.ctgen.impl.CTGCommand;
import dev.tocraft.crafted.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.crafted.ctgen.worldgen.MapBasedChunkGenerator;
import dev.tocraft.crafted.ctgen.worldgen.MapBasedBiomeSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SuppressWarnings("unused")
@ApiStatus.Internal
public class CTGFabric {
    public static void initialize() {
        Registry.register(BuiltInRegistries.BIOME_SOURCE, CTerrainGeneration.id("map_based_biome_source"), MapBasedBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, CTerrainGeneration.id("map_based_chunk_generator"), MapBasedChunkGenerator.CODEC);

        {
            MapImageRegistry reloadListener = new MapImageRegistry();
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
                @Override
                public ResourceLocation getFabricId() {
                    return CTerrainGeneration.id("map_image_listener");
                }

                @Override
                public @NotNull String getName() {
                    return reloadListener.getName();
                }

                @Override
                public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                    return reloadListener.reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
                }
            });
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> CTGCommand.register(dispatcher, context));

        ClientPlayNetworking.registerGlobalReceiver(
                SyncMapPacket.PACKET_ID,
                (client, handler, buf, sender) -> {
                    SyncMapPacket packet = SyncMapPacket.decode(buf);
                    packet.handle();
                }
        );
    }
}
