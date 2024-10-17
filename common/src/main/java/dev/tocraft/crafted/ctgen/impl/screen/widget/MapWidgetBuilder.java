package dev.tocraft.crafted.ctgen.impl.screen.widget;

import dev.tocraft.crafted.ctgen.impl.CTGClient;
import dev.tocraft.crafted.ctgen.impl.network.SyncMapPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class MapWidgetBuilder {
    private Minecraft minecraft = Minecraft.getInstance();
    private int x;
    private int y;
    private int width;
    private int height;
    private ResourceLocation mapId;
    private int xOffset;
    private int yOffset;
    private int mapWidth;
    private int mapHeight;
    private float zoomFactor = 1.1f;
    private int headScale = 8;

    public static MapWidgetBuilder createDefault() {
        SyncMapPacket packet = CTGClient.LAST_SYNC_MAP_PACKET.get();
        if (packet != null) {
            ResourceLocation mapId = packet.getMapId();
            int xOffset = packet.getXOffset();
            int yOffset = packet.getYOffset();
            int mapWidth = packet.getMapWidth();
            int mapHeight = packet.getMapHeight();
            if (mapId != null) {
                return new MapWidgetBuilder()
                        .setMapId(new ResourceLocation(mapId.getNamespace(), "textures/gui/" + mapId.getPath() + ".png"))
                        .setXOffset(xOffset).setYOffset(yOffset)
                        .setMapWidth(mapWidth)
                        .setMapHeight(mapHeight);
            }
        }
        return null;
    }

    public MapWidgetBuilder setMinecraft(Minecraft minecraft) {
        this.minecraft = minecraft;
        return this;
    }

    public MapWidgetBuilder setX(int x) {
        this.x = x;
        return this;
    }

    public MapWidgetBuilder setY(int y) {
        this.y = y;
        return this;
    }

    public MapWidgetBuilder setWidth(int width) {
        this.width = width;
        return this;
    }

    public MapWidgetBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    public MapWidgetBuilder setMapId(ResourceLocation mapId) {
        this.mapId = mapId;
        return this;
    }

    public MapWidgetBuilder setXOffset(int xOffset) {
        this.xOffset = xOffset;
        return this;
    }

    public MapWidgetBuilder setYOffset(int yOffset) {
        this.yOffset = yOffset;
        return this;
    }

    public MapWidgetBuilder setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
        return this;
    }

    public MapWidgetBuilder setMapHeight(int mapHeight) {
        this.mapHeight = mapHeight;
        return this;
    }

    public MapWidgetBuilder setZoomFactor(float zoomFactor) {
        this.zoomFactor = zoomFactor;
        return this;
    }

    public MapWidgetBuilder setHeadScale(int headScale) {
        this.headScale = headScale;
        return this;
    }

    public MapWidget build() {
        return new MapWidget(minecraft, x, y, width, height, mapId, xOffset, yOffset, mapWidth, mapHeight, zoomFactor, headScale);
    }
}