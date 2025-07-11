package dev.tocraft.ctgen.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Function;

// prevent air caves in water
@Mixin(WorldCarver.class)
public class WorldCarverMixin<C extends CarverConfiguration> {
    @Inject(
            method = "carveBlock",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
                    ordinal = 0,
                    shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true
    )
    private void atlas_stopWaterCarving(CarvingContext context, C config, ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeGetter, CarvingMask carvingMask, BlockPos.MutableBlockPos pos, BlockPos.MutableBlockPos checkPos, Aquifer aquifer, MutableBoolean reachedSurface, CallbackInfoReturnable<Boolean> cir, @NotNull BlockState blockState) {
        if (blockState.getFluidState().isSource()) {
            cir.setReturnValue(false);
        }
    }
}
