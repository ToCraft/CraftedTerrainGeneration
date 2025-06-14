package dev.tocraft.ctgen.neoforge;

import dev.tocraft.ctgen.data.BiomeImageRegistry;
import dev.tocraft.ctgen.data.HeightImageRegistry;
import dev.tocraft.ctgen.impl.CTGCommand;
import dev.tocraft.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.ctgen.worldgen.MapBasedBiomeSource;
import dev.tocraft.ctgen.worldgen.MapBasedChunkGenerator;
import dev.tocraft.ctgen.worldgen.noise.CTGAboveSurfaceCondition;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class CTGNeoForgeEventListener {
    private static final String PROTOCOL_VERSION = "1";

    public static void initialize(@NotNull IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(CTGNeoForgeEventListener::addReloadListenerEvent);
        NeoForge.EVENT_BUS.addListener(CTGNeoForgeEventListener::registerCommands);
        modEventBus.addListener(CTGNeoForgeEventListener::register);
        modEventBus.addListener(CTGNeoForgeEventListener::registerPayload);
    }

    private static void addReloadListenerEvent(@NotNull AddServerReloadListenersEvent event) {
        event.addListener(BiomeImageRegistry.ID, new BiomeImageRegistry());
        event.addListener(HeightImageRegistry.ID, new HeightImageRegistry());
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
