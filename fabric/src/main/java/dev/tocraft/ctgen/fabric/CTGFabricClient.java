package dev.tocraft.ctgen.fabric;

import dev.tocraft.ctgen.impl.CTGClient;
import dev.tocraft.ctgen.impl.network.SyncMapPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.jetbrains.annotations.ApiStatus;

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
    }
}
