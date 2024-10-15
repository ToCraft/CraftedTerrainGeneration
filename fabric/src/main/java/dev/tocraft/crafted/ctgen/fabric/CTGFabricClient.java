package dev.tocraft.crafted.ctgen.fabric;

import dev.tocraft.crafted.ctgen.impl.CTGClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class CTGFabricClient {
    public static void initialize() {
        KeyBindingHelper.registerKeyBinding(CTGClient.OPEN_MAP_KEY);
        ClientTickEvents.START_CLIENT_TICK.register(CTGClient::tick);
    }
}
