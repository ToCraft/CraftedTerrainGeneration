package dev.tocraft.crafted.ctgen.impl;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tocraft.crafted.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.crafted.ctgen.impl.screen.MapScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class CTGClient {
    @ApiStatus.Internal
    public static final AtomicReference<SyncMapPacket> LAST_SYNC_MAP_PACKET = new AtomicReference<>(SyncMapPacket.empty());
    @ApiStatus.Internal
    public static final KeyMapping OPEN_MAP_KEY = new KeyMapping("key.ctgen_map", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.ui");
    private static final Map<ResourceLocation, BiFunction<Minecraft, SyncMapPacket, Screen>> MENU_REGISTRY = new ConcurrentHashMap<>();

    @ApiStatus.Internal
    public static void tick(@NotNull Minecraft minecraft) {
        assert minecraft.player != null;

        if (OPEN_MAP_KEY.consumeClick()) {
            SyncMapPacket packet = CTGClient.LAST_SYNC_MAP_PACKET.get();
            BiFunction<Minecraft, SyncMapPacket, Screen> screenFunc;
            ResourceLocation mapId = packet.getMapId();
            if (mapId != null && MENU_REGISTRY.containsKey(mapId)) {
                screenFunc = MENU_REGISTRY.get(mapId);
            } else {
                screenFunc = MapScreen::new;
            }
            minecraft.setScreen(screenFunc.apply(minecraft, packet));
        }
    }

    @SuppressWarnings("unused")
    @ApiStatus.Internal
    public static void registerMenu(ResourceLocation mapId, BiFunction<Minecraft, SyncMapPacket, Screen> entry) {
        MENU_REGISTRY.put(mapId, entry);
    }
}
