package dev.tocraft.ctgen.neoforge;

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
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ApiStatus.Internal
public final class CTGNeoForgeEventListener {
    private static final String PROTOCOL_VERSION = "1";

    public static void initialize(@NotNull IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(CTGNeoForgeEventListener::addReloadListenerEvent);
        NeoForge.EVENT_BUS.addListener(CTGNeoForgeEventListener::registerCommands);
        modEventBus.addListener(CTGNeoForgeEventListener::registerRegistries);
        modEventBus.addListener(CTGNeoForgeEventListener::register);
        modEventBus.addListener(CTGNeoForgeEventListener::registerPayload);
    }

    private static final List<PreparableReloadListener> RELOAD_LISTENERS = new CopyOnWriteArrayList<>() {
        {
            add(new MapImageRegistry());
        }
    };

    private static void addReloadListenerEvent(AddReloadListenerEvent event) {
        for (PreparableReloadListener reloadListener : RELOAD_LISTENERS) {
            event.addListener(reloadListener);
        }
    }

    private static void registerCommands(@NotNull RegisterCommandsEvent event) {
        CTGCommand.register(event.getDispatcher(), event.getBuildContext());
    }

    private static void registerRegistries(@NotNull NewRegistryEvent event) {
        event.register(CTRegistries.BLOCK_PLACER);
        event.register(CTRegistries.BLOCK_LAYER);
        event.register(CTRegistries.TERRAIN);
        event.register(CTRegistries.CARVER);
    }

    private static void register(@NotNull RegisterEvent event) {
        // generic stuff
        event.register(Registries.BIOME_SOURCE, helper -> helper.register(MapBasedBiomeSource.ID, MapBasedBiomeSource.CODEC));
        event.register(Registries.CHUNK_GENERATOR, helper -> helper.register(MapBasedChunkGenerator.ID, MapBasedChunkGenerator.CODEC));

        // custom stuff
        event.register(CTRegistries.BLOCK_PLACER_KEY, helper -> BlockPlacer.register(helper::register));
        event.register(CTRegistries.BLOCK_LAYER_KEY, helper -> BlockLayer.register(helper::register));
        event.register(CTRegistries.TERRAIN_KEY, helper -> TerrainHeight.register(helper::register));
        event.register(CTRegistries.CARVER_KEY, helper -> Carver.register(helper::register));
    }

    private static void registerPayload(@NotNull RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL_VERSION).playToClient(SyncMapPacket.TYPE, SyncMapPacket.streamCodec(), (packet, context) -> packet.handle());
    }
}
