package dev.tocraft.crafted.ctgen.impl;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tocraft.crafted.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.crafted.ctgen.impl.screen.MapScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class CTGClient {
    public static final AtomicReference<SyncMapPacket> LAST_SYNC_MAP_PACKET = new AtomicReference<>(null);
    public static final KeyMapping OPEN_MAP_KEY = new KeyMapping("key.ctgen_map", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.ctgen");

    public static void tick(Minecraft minecraft) {
        assert minecraft.player != null;

        if (OPEN_MAP_KEY.consumeClick()) {
            SyncMapPacket packet = LAST_SYNC_MAP_PACKET.get();
            if (packet != null) {
                ResourceLocation mapId = packet.getMapId();
                int xOffset = packet.getXOffset();
                int yOffset = packet.getYOffset();
                int mapWidth = packet.getMapWidth();
                int mapHeight = packet.getMapHeight();
                minecraft.setScreen(new MapScreen(minecraft, mapId, xOffset, yOffset, mapWidth, mapHeight));
            }
        }
    }
}
