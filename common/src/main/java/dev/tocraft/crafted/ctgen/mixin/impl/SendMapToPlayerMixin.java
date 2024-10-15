package dev.tocraft.crafted.ctgen.mixin.impl;

import dev.tocraft.crafted.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.crafted.ctgen.worldgen.MapBasedChunkGenerator;
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
public class SendMapToPlayerMixin {
    @Unique
    private final ArrayDeque<SyncMapPacket> ctgen$packetStack = new ArrayDeque<>();

    @Inject(method = "setServerLevel", at = @At("HEAD"))
    private void onDimChange(@NotNull ServerLevel level, CallbackInfo ci) {
        ResourceLocation mapId;
        if (level.getChunkSource().getGenerator() instanceof MapBasedChunkGenerator generator) {
            mapId = generator.getSettings().getMapId();
        } else {
            mapId = null;
        }

        SyncMapPacket packet = new SyncMapPacket(mapId);
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
