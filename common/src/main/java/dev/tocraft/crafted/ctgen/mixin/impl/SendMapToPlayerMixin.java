package dev.tocraft.crafted.ctgen.mixin.impl;

import dev.tocraft.crafted.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.crafted.ctgen.worldgen.MapBasedChunkGenerator;
import dev.tocraft.crafted.ctgen.worldgen.MapSettings;
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
        ResourceLocation mapId;
        int xOffset;
        int yOffset;
        int mapWidth;
        int mapHeight;
        if (level.getChunkSource().getGenerator() instanceof MapBasedChunkGenerator generator) {
            MapSettings settings = generator.getSettings();
            mapId = settings.getMapId();
            xOffset = settings.xOffset(0);
            yOffset = settings.yOffset(0);
            mapWidth = settings.getMapWidth();
            mapHeight = settings.getMapHeight();
        } else {
            mapId = null;
            xOffset = -1;
            yOffset = -1;
            mapWidth = -1;
            mapHeight = -1;
        }

        SyncMapPacket packet = new SyncMapPacket(mapId, xOffset, yOffset, mapWidth, mapHeight);
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
