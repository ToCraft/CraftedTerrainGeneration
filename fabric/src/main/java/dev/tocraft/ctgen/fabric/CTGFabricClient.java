package dev.tocraft.ctgen.fabric;

import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.data.MapOverlayTextLoader;
import dev.tocraft.ctgen.impl.CTGClient;
import dev.tocraft.ctgen.impl.network.SyncMapPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class CTGFabricClient {
    public static void initialize() {
        KeyBindingHelper.registerKeyBinding(CTGClient.OPEN_MAP_KEY);
        ClientTickEvents.START_CLIENT_TICK.register(CTGClient::tick);

        ClientPlayNetworking.registerGlobalReceiver(
                SyncMapPacket.TYPE,
                (payload, context) -> payload.handle()
        );


        // register text overlay listener
        MapOverlayTextLoader overlayTextLoader = new MapOverlayTextLoader();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return CTerrainGeneration.id("text_overlay_loader");
            }

            @Override
            public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor) {
                return overlayTextLoader.reload(preparationBarrier, resourceManager, backgroundExecutor, gameExecutor);
            }

            @Override
            public @NotNull String getName() {
                return overlayTextLoader.getName();
            }
        });
    }
}
