package dev.tocraft.crafted.ctgen.fabric.services;

import dev.tocraft.crafted.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.crafted.ctgen.impl.services.ServerPlatform;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public final class FabricServerPlatform implements ServerPlatform {
    @Override
    public void send(SyncMapPacket packet, ServerPlayer to) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);
        ServerPlayNetworking.send(to, SyncMapPacket.PACKET_ID, buf);
    }

    @Override
    public <T> Registry<T> simpleRegistry(ResourceKey<Registry<T>> registryKey) {
        return FabricRegistryBuilder.createSimple(registryKey).buildAndRegister();
    }
}
