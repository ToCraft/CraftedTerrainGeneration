package dev.tocraft.ctgen.data;

import dev.tocraft.ctgen.worldgen.MapSettings;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

import java.util.function.Supplier;

public interface SurfaceBuilderAccess {
    void ctgen$buildSurface(RandomState noiseConfig, BiomeManager biomeAccess, Registry<Biome> biomeRegistry, boolean useLegacyRandom, WorldGenerationContext heightContext, final ChunkAccess chunk, NoiseChunk chunkNoiseSampler, SurfaceRules.RuleSource materialRule, Supplier<MapSettings> mapSettings);

}
