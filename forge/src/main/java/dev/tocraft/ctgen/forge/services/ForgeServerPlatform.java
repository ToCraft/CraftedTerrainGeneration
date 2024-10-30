package dev.tocraft.ctgen.forge.services;

import dev.tocraft.ctgen.forge.CTGForge;
import dev.tocraft.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.ctgen.impl.services.ServerPlatform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import org.jetbrains.annotations.NotNull;

public final class ForgeServerPlatform implements ServerPlatform {
    @Override
    public void send(SyncMapPacket packet, @NotNull ServerPlayer to) {
        CTGForge.SYNC_MAP_CHANNEL.sendTo(packet, to.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
