package dev.tocraft.ctgen.impl.services;

import dev.tocraft.ctgen.impl.network.SyncMapPacket;
import net.minecraft.server.level.ServerPlayer;

public interface ServerPlatform {
    ServerPlatform INSTANCE = PlatformService.load(ServerPlatform.class);

    void send(SyncMapPacket packet, ServerPlayer to);
}
