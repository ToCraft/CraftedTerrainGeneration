package dev.tocraft.crafted.ctgen.forge;

import dev.tocraft.crafted.ctgen.impl.CTGClient;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@ApiStatus.Internal
@OnlyIn(Dist.CLIENT)
public class CTGForgeClient {
    public CTGForgeClient() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void event(@NotNull RegisterKeyMappingsEvent event) {
        event.register(CTGClient.OPEN_MAP_KEY);
    }

    @SubscribeEvent
    public void event(TickEvent.@NotNull ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            CTGClient.tick(Minecraft.getInstance());
        }
    }
}
