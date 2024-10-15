package dev.tocraft.crafted.ctgen.impl.network;

import com.mojang.logging.LogUtils;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.impl.CTGClient;
import dev.tocraft.crafted.ctgen.impl.services.ServerPlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class SyncMapPacket {
    public static final ResourceLocation PACKET_ID = CTerrainGeneration.id("sync_map_id");

    @Nullable
    private final ResourceLocation mapId;

    public SyncMapPacket(@Nullable ResourceLocation mapId) {
        this.mapId = mapId;
    }

    public void encode(@NotNull FriendlyByteBuf buf) {
        boolean bl = this.mapId != null;
        buf.writeBoolean(bl);
        if (bl) {
            buf.writeResourceLocation(this.mapId);
        }
    }

    public static @NotNull SyncMapPacket decode(@NotNull FriendlyByteBuf buf) {
        boolean bl = buf.readBoolean();
        ResourceLocation mapId = bl ? buf.readResourceLocation() : null;
        return new SyncMapPacket(mapId);
    }

    public void handle() {
        CTGClient.CURRENT_MAP.set(mapId);
        LogUtils.getLogger().info("Using map: {}", mapId);
    }

    public void send(ServerPlayer to) {
        ServerPlatform.INSTANCE.send(this, to);
    }
}
