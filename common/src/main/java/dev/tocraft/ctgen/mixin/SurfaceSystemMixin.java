package dev.tocraft.ctgen.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.tocraft.ctgen.data.MapInfoAccessor;
import dev.tocraft.ctgen.data.SurfaceBuilderAccess;
import dev.tocraft.ctgen.worldgen.MapSettings;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(SurfaceSystem.class)
public abstract class SurfaceSystemMixin implements SurfaceBuilderAccess {
    @Shadow
    public abstract void buildSurface(RandomState randomState, BiomeManager biomeManager, Registry<Biome> biomes, boolean useLegacyRandomSource, WorldGenerationContext context, ChunkAccess chunk, NoiseChunk noiseChunk, SurfaceRules.RuleSource ruleSource);

    @Unique
    private Supplier<MapSettings> ctgen$settings = () -> null;
    private Supplier<SimplexNoise> ctgen$noise = () -> null;

    @Unique
    @Override
    public void ctgen$buildSurface(RandomState randomState, BiomeManager biomeManager, Registry<Biome> biomes, boolean useLegacyRandomSource, WorldGenerationContext context, final ChunkAccess chunk, NoiseChunk noiseChunk, SurfaceRules.RuleSource ruleSource, Supplier<MapSettings> mapSettings, Supplier<SimplexNoise> noise) {
        this.ctgen$settings = mapSettings;
        this.ctgen$noise = noise;
        this.buildSurface(randomState, biomeManager, biomes, useLegacyRandomSource, context, chunk, noiseChunk, ruleSource);
    }

    @Inject(method = "buildSurface", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/SurfaceRules$RuleSource;apply(Ljava/lang/Object;)Ljava/lang/Object;"))
    private void setMapContext(RandomState randomState, BiomeManager biomeManager, Registry<Biome> biomes, boolean useLegacyRandomSource, WorldGenerationContext context, ChunkAccess chunk, NoiseChunk noiseChunk, SurfaceRules.RuleSource ruleSource, CallbackInfo ci, @Local SurfaceRules.Context context2) {
        ((MapInfoAccessor) (Object) context2).ctgen$setSettings(this.ctgen$settings);
        ((MapInfoAccessor) (Object) context2).ctgen$setNoise(this.ctgen$noise);
    }
}
