package dev.tocraft.crafted.ctgen.forge.services;

import dev.tocraft.crafted.ctgen.forge.CTGForge;
import dev.tocraft.crafted.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.crafted.ctgen.impl.services.ServerPlatform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;

public final class ForgeServerPlatform implements ServerPlatform {
    @Override
    public void send(SyncMapPacket packet, ServerPlayer to) {
        CTGForge.SYNC_MAP_CHANNEL.sendTo(packet, to.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
