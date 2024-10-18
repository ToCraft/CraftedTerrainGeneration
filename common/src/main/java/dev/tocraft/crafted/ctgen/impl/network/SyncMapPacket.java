package dev.tocraft.crafted.ctgen.impl.network;

import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.impl.CTGClient;
import dev.tocraft.crafted.ctgen.impl.services.ServerPlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SyncMapPacket {
    public static final ResourceLocation PACKET_ID = CTerrainGeneration.id("sync_map_id");

    @Nullable
    private final ResourceLocation mapId;
    private final int xOffset;
    private final int yOffset;
    private final int mapWidth;
    private final int mapHeight;

    @ApiStatus.Internal
    public SyncMapPacket(@Nullable ResourceLocation mapId, int xOffset, int yOffset, int mapWidth, int mapHeight) {
        this.mapId = mapId;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    @ApiStatus.Internal
    public void encode(@NotNull FriendlyByteBuf buf) {
        boolean bl = this.mapId != null;
        buf.writeBoolean(bl);
        if (bl) {
            buf.writeResourceLocation(this.mapId);
            buf.writeInt(this.xOffset);
            buf.writeInt(this.yOffset);
            buf.writeInt(this.mapWidth);
            buf.writeInt(this.mapHeight);
        }
    }

    @ApiStatus.Internal
    public static @NotNull SyncMapPacket decode(@NotNull FriendlyByteBuf buf) {
        boolean bl = buf.readBoolean();
        if (bl) {
            ResourceLocation mapId = buf.readResourceLocation();
            int xOffset = buf.readInt();
            int yOffset = buf.readInt();
            int mapWidth = buf.readInt();
            int mapHeight = buf.readInt();
            return new SyncMapPacket(mapId, xOffset, yOffset, mapWidth, mapHeight);
        }
        else {
            return empty();
        }
    }

    @ApiStatus.Internal
    public static SyncMapPacket empty() {
        return new SyncMapPacket(null, -1, -1, -1, -1);
    }

    @ApiStatus.Internal
    public void handle() {
        CTGClient.LAST_SYNC_MAP_PACKET.set(this);
    }

    @ApiStatus.Internal
    public void send(ServerPlayer to) {
        ServerPlatform.INSTANCE.send(this, to);
    }

    public @Nullable ResourceLocation getMapId() {
        return mapId;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getMapWidth() {
        return mapWidth;
    }
}
