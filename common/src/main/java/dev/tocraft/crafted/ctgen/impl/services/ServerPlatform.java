package dev.tocraft.crafted.ctgen.impl.services;

import dev.tocraft.crafted.ctgen.impl.network.SyncMapPacket;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public interface ServerPlatform {
    ServerPlatform INSTANCE = PlatformService.load(ServerPlatform.class);

    void send(SyncMapPacket packet, ServerPlayer to);

    <T> Registry<T> simpleRegistry(ResourceKey<Registry<T>> registryKey);
}
