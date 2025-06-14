package dev.tocraft.ctgen.mixin;

import com.mojang.datafixers.DataFixer;
import dev.tocraft.ctgen.worldgen.MapBasedChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

// allow noise for chunk generator
@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @Mutable
    @Shadow
    @Final
    private RandomState randomState;

    @Inject(method = "<init>",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;createState(Lnet/minecraft/core/HolderLookup;Lnet/minecraft/world/level/levelgen/RandomState;J)Lnet/minecraft/world/level/chunk/ChunkGeneratorStructureState;",
                    shift = At.Shift.BEFORE)
    )
    private void populateNoise(
            ServerLevel world,
            LevelStorageSource.LevelStorageAccess session,
            DataFixer dataFixer,
            StructureTemplateManager structureTemplateManager,
            Executor executor,
            BlockableEventLoop<Runnable> mainThreadExecutor,
            LightChunkGetter chunkProvider,
            ChunkGenerator chunkGenerator,
            ChunkProgressListener worldGenerationProgressListener,
            ChunkStatusUpdateListener chunkStatusChangeListener,
            Supplier<DimensionDataStorage> persistentStateManagerFactory,
            int viewDistance,
            boolean dsync,
            CallbackInfo ci
    ) {
        if (chunkGenerator instanceof MapBasedChunkGenerator generator) {
            this.randomState = RandomState.create(
                    generator.getNoiseGenSettings(),
                    world.registryAccess().lookupOrThrow(Registries.NOISE),
                    world.getSeed()
            );
        }
    }
}
