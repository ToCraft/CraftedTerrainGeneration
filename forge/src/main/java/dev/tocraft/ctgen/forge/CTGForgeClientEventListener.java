package dev.tocraft.ctgen.forge;

import dev.tocraft.ctgen.impl.CTGClient;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@ApiStatus.Internal
@OnlyIn(Dist.CLIENT)
public final class CTGForgeClientEventListener {
    public static void initialize(IEventBus modEventBus) {
        modEventBus.addListener(CTGForgeClientEventListener::registerKeys);
        MinecraftForge.EVENT_BUS.addListener(CTGForgeClientEventListener::tick);
    }

    private static void registerKeys(@NotNull RegisterKeyMappingsEvent event) {
        event.register(CTGClient.OPEN_MAP_KEY);
    }

    private static void tick(TickEvent.@NotNull ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            CTGClient.tick(Minecraft.getInstance());
        }
    }
}
