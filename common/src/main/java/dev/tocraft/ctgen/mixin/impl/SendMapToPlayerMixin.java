package dev.tocraft.ctgen.mixin.impl;

import dev.tocraft.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.ctgen.worldgen.MapBasedChunkGenerator;
import dev.tocraft.ctgen.worldgen.MapSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;

@Mixin(ServerPlayer.class)
public abstract class SendMapToPlayerMixin {
    @Unique
    private final ArrayDeque<SyncMapPacket> ctgen$packetStack = new ArrayDeque<>();

    @Inject(method = "setServerLevel", at = @At("HEAD"))
    private void onDimChange(@NotNull ServerLevel level, CallbackInfo ci) {
        SyncMapPacket packet;
        if (level.getChunkSource().getGenerator() instanceof MapBasedChunkGenerator generator) {
            MapSettings settings = generator.getSettings();
            ResourceLocation mapId = settings.getMapId();
            boolean pixelsAreChunks = settings.isPixelsAreChunks();
            int xOffset = settings.xOffset(0);
            int yOffset = settings.yOffset(0);
            int mapWidth = settings.getMapWidth();
            int mapHeight = settings.getMapHeight();
            packet = new SyncMapPacket(mapId, pixelsAreChunks, xOffset, yOffset, mapWidth, mapHeight);
        } else {
            packet = SyncMapPacket.empty();
        }

        ctgen$packetStack.addLast(packet);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        while (!ctgen$packetStack.isEmpty()) {
            SyncMapPacket packet = ctgen$packetStack.pollFirst();
            packet.send((ServerPlayer) (Object) this);
        }
    }
}
