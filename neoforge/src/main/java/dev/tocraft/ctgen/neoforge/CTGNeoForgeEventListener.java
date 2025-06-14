package dev.tocraft.ctgen.neoforge;

import dev.tocraft.ctgen.data.MapImageRegistry;
import dev.tocraft.ctgen.impl.CTGCommand;
import dev.tocraft.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.ctgen.worldgen.MapBasedBiomeSource;
import dev.tocraft.ctgen.worldgen.MapBasedChunkGenerator;
import dev.tocraft.ctgen.worldgen.noise.CTGAboveSurfaceCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
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

    private static void register(@NotNull RegisterEvent event) {
        // generic stuff
        event.register(Registries.BIOME_SOURCE, helper -> helper.register(MapBasedBiomeSource.ID, MapBasedBiomeSource.CODEC));
        event.register(Registries.CHUNK_GENERATOR, helper -> helper.register(MapBasedChunkGenerator.ID, MapBasedChunkGenerator.CODEC));

        // surface rules
        event.register(Registries.MATERIAL_CONDITION, helper -> CTGAboveSurfaceCondition.register(helper::register));
    }

    private static void registerPayload(@NotNull RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL_VERSION).playToClient(SyncMapPacket.TYPE, SyncMapPacket.streamCodec(), (packet, context) -> packet.handle());
    }
}
