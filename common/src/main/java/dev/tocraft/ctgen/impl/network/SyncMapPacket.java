package dev.tocraft.ctgen.impl.network;

import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.impl.CTGClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class SyncMapPacket implements CustomPacketPayload {
    public static final ResourceLocation PACKET_ID = CTerrainGeneration.id("sync_map_id");
    public static final Type<SyncMapPacket> TYPE = new Type<>(PACKET_ID);

    private static final List<Consumer<SyncMapPacket>> handlers = new CopyOnWriteArrayList<>();

    @Nullable
    private final ResourceLocation mapId;
    private final boolean pixelsAreChunks;
    private final int xOffset;
    private final int yOffset;
    private final int mapWidth;
    private final int mapHeight;

    @ApiStatus.Internal
    public SyncMapPacket(@Nullable ResourceLocation mapId, boolean pixelsAreChunks, int xOffset, int yOffset, int mapWidth, int mapHeight) {
        this.mapId = mapId;
        this.pixelsAreChunks = pixelsAreChunks;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    @Contract(value = " -> new", pure = true)
    @ApiStatus.Internal
    public static @NotNull SyncMapPacket empty() {
        return new SyncMapPacket(null, false, -1, -1, -1, -1);
    }

    @ApiStatus.Internal
    public void handle() {
        CTGClient.LAST_SYNC_MAP_PACKET.set(this);
        for (Consumer<SyncMapPacket> handler : handlers) {
            handler.accept(this);
        }
    }

    public static void registerHandler(Consumer<SyncMapPacket> handler) {
        handlers.add(handler);
    }

    @ApiStatus.Internal
    public void send(@NotNull ServerPlayer to) {
        ClientboundCustomPayloadPacket payload = new ClientboundCustomPayloadPacket(this);
        to.connection.send(payload);
    }

    public @Nullable ResourceLocation getMapId() {
        return mapId;
    }

    public boolean isPixelsAreChunks() {
        return pixelsAreChunks;
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

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, SyncMapPacket> streamCodec() {
        return new StreamCodec<>() {
            @Override
            public @NotNull SyncMapPacket decode(@NotNull RegistryFriendlyByteBuf buf) {
                boolean bl = buf.readBoolean();
                if (bl) {
                    ResourceLocation mapId = buf.readResourceLocation();
                    boolean pixelsAreChunks = buf.readBoolean();
                    int xOffset = buf.readInt();
                    int yOffset = buf.readInt();
                    int mapWidth = buf.readInt();
                    int mapHeight = buf.readInt();
                    return new SyncMapPacket(mapId, pixelsAreChunks, xOffset, yOffset, mapWidth, mapHeight);
                } else {
                    return empty();
                }
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull SyncMapPacket payload) {
                boolean bl = payload.mapId != null;
                buf.writeBoolean(bl);
                if (bl) {
                    buf.writeResourceLocation(payload.mapId);
                    buf.writeBoolean(payload.pixelsAreChunks);
                    buf.writeInt(payload.xOffset);
                    buf.writeInt(payload.yOffset);
                    buf.writeInt(payload.mapWidth);
                    buf.writeInt(payload.mapHeight);
                }
            }
        };
    }
}
