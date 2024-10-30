package dev.tocraft.ctgen.fabric.services;

import dev.tocraft.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.ctgen.impl.services.ServerPlatform;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class FabricServerPlatform implements ServerPlatform {
    @Override
    public void send(SyncMapPacket packet, ServerPlayer to) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);
        ServerPlayNetworking.send(to, SyncMapPacket.PACKET_ID, buf);
    }
}
