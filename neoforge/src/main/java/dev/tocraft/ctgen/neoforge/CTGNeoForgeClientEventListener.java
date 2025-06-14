package dev.tocraft.ctgen.neoforge;

import dev.tocraft.ctgen.data.MapOverlayTextLoader;
import dev.tocraft.ctgen.impl.CTGClient;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@ApiStatus.Internal
@OnlyIn(Dist.CLIENT)
public final class CTGNeoForgeClientEventListener {
    public static void initialize(IEventBus modEventBus) {
        modEventBus.addListener(CTGNeoForgeClientEventListener::registerKeys);
        modEventBus.addListener(CTGNeoForgeClientEventListener::registerReloadListeners);
        NeoForge.EVENT_BUS.addListener(CTGNeoForgeClientEventListener::tick);
    }

    private static void registerKeys(@NotNull RegisterKeyMappingsEvent event) {
        event.register(CTGClient.OPEN_MAP_KEY);
    }

    private static void tick(ClientTickEvent.Pre event) {
        CTGClient.tick(Minecraft.getInstance());
    }

    private static void registerReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(MapOverlayTextLoader.ID, new MapOverlayTextLoader());
    }
}
