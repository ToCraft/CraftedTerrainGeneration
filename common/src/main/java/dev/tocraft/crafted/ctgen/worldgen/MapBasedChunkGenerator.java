package dev.tocraft.crafted.ctgen.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.biome.MapBiome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MapBasedChunkGenerator extends ChunkGenerator {
    public static final Codec<MapBasedChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            MapSettings.CODEC.fieldOf("settings").forGetter(MapBasedChunkGenerator::getSettings)
    ).apply(instance, instance.stable(MapBasedChunkGenerator::of)));

    private static final int BEDROCK_SIZE = 4;
    private static final int DIRT_SIZE = 4;

    protected final MapBasedBiomeSource biomeSource;
    private SimplexNoise noise = null;

    private MapBasedChunkGenerator(MapBasedBiomeSource biomeSource) {
        super(biomeSource);
        this.biomeSource = biomeSource;
    }

    public static MapBasedChunkGenerator of(MapSettings settings) {
        MapBasedBiomeSource biomeSource = new MapBasedBiomeSource(settings);
        return new MapBasedChunkGenerator(biomeSource);
    }

    @Override
    protected @NotNull Codec<MapBasedChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(@NotNull WorldGenRegion pLevel, long pSeed, @NotNull RandomState pRandom, @NotNull BiomeManager pBiomeManager, @NotNull StructureManager pStructureManager, @NotNull ChunkAccess pChunk, GenerationStep.@NotNull Carving pStep) {
    }

    @Override
    public void buildSurface(@NotNull WorldGenRegion pLevel, @NotNull StructureManager pStructureManager, @NotNull RandomState pRandom, @NotNull ChunkAccess chunk) {
        setNoise(pRandom);

        int minHeight = chunk.getMinBuildHeight() + BEDROCK_SIZE;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos pos = chunk.getPos().getBlockAt(x, chunk.getHeight(), z);

                MapBiome biomeData = getSettings().getMapBiome(pos.getX() >> 2, pos.getZ() >> 2).value();
                double surfaceHeight = getSettings().getHeight(noise, pos.getX(), pos.getZ()) + getSettings().surfaceLevel;

                for (int y = chunk.getMinBuildHeight(); y <= surfaceHeight || y <= getSeaLevel(); y++) {
                    BlockPos blockPos = chunk.getPos().getBlockAt(x, y, z);
                    if (y < minHeight) {
                        // place bedrock
                        chunk.setBlockState(blockPos, Blocks.BEDROCK.defaultBlockState(), false);
                        continue;
                    } else if (y < getSettings().deepslateLevel && getSettings().deepslateLevel < surfaceHeight) {
                        // place deepslate
                        chunk.setBlockState(blockPos, biomeData.deepslateBlock().defaultBlockState(), false);
                    } else if (y < surfaceHeight - DIRT_SIZE) {
                        // place stone between deepslate and surface - DIRT_SIZE
                        chunk.setBlockState(blockPos, biomeData.stoneBlock().defaultBlockState(), false);
                    } else if (y < surfaceHeight - 1) {
                        // place dirt below surface
                        chunk.setBlockState(blockPos, biomeData.dirtBlock().defaultBlockState(), false);
                    } else if (y <= getSeaLevel()) {
                        // place oceans if the surface isn't higher than the sea level
                        chunk.setBlockState(chunk.getPos().getBlockAt(x, y, z), Blocks.WATER.defaultBlockState(), false);
                        // place no caves in ocean
                        continue;
                    }
                    // only surface is missing
                    else {
                        Block surfaceBlock = biomeData.surfaceBlock();
                        // no grass underwater
                        if (surfaceHeight < getSeaLevel() && surfaceBlock == Blocks.GRASS_BLOCK) {
                            surfaceBlock = Blocks.DIRT;
                        }
                        chunk.setBlockState(blockPos, surfaceBlock.defaultBlockState(), false);
                    }

                    // Caves
                    // Modify cave threshold based on y, (nearly) no caves for 100% height nor 0%
                    double threshold;
                    int y2 = y - minHeight;
                    int deepLevel = getSettings().deepslateLevel - minHeight;
                    if (y2 <= deepLevel) {
                        threshold = lerp(getSettings().caves.bedrockThreshold(), getSettings().caves.midThreshold(), (double) y2 / deepLevel);
                    } else {
                        double halfSurfaceLevel = surfaceHeight / 2;
                        if (y > halfSurfaceLevel) {
                            double entryThreshold = getSettings().getValueWithTransition(pos.getX(), pos.getZ(), mapBiome -> mapBiome.caveThreshold().orElse(getSettings().caves.entryThreshold()));
                            threshold = lerp(getSettings().caves.midThreshold(), entryThreshold, (y - halfSurfaceLevel) / halfSurfaceLevel);
                        }
                        else {
                            threshold = getSettings().caves.midThreshold();
                        }
                    }

                    double noiseValue = get3DPerlin(noise, blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    if (noiseValue > threshold) {
                        chunk.setBlockState(blockPos, Blocks.CAVE_AIR.defaultBlockState(), false);
                    }
                }
            }
        }
    }

    public double get3DPerlin(SimplexNoise noise, int x, int y, int z) {
        double perlin = 0;
        for (int i = 0; i < 1; i++) {
            perlin += Math.pow(0.5, i) * noise.getValue(x * Math.pow(2, i) / getSettings().caveStretchXZ, y * Math.pow(2, i) / getSettings().caveStretchY, z * Math.pow(2, i) / getSettings().caveStretchXZ);
        }
        perlin = perlin / (1 - Math.pow(0.5, getSettings().noiseDetail));
        return perlin;
    }

    private static double lerp(double start, double end, double percentage) {
        return (start * (1 - percentage)) + (end * percentage);
    }

    @Override
    public void spawnOriginalMobs(@NotNull WorldGenRegion pLevel) {
        ChunkPos chunkpos = pLevel.getCenter();
        Holder<Biome> holder = pLevel.getBiome(chunkpos.getWorldPosition().atY(pLevel.getMaxBuildHeight() - 1));
        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        worldgenrandom.setDecorationSeed(pLevel.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(pLevel, holder, chunkpos, worldgenrandom);
    }

    @Override
    public int getGenDepth() {
        return getSettings().genHeight;
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(@NotNull Executor pExecutor, @NotNull Blender pBlender, @NotNull RandomState pRandom, @NotNull StructureManager pStructureManager, @NotNull ChunkAccess pChunk) {
        return CompletableFuture.completedFuture(pChunk);
    }

    @Override
    public int getSeaLevel() {
        return getSettings().seaLevel;
    }

    @Override
    public int getMinY() {
        return getSettings().minY;
    }

    @Override
    public int getBaseHeight(int pX, int pZ, @NotNull Heightmap.Types pType, @NotNull LevelHeightAccessor pLevel, @NotNull RandomState pRandom) {
        setNoise(pRandom);
        return Math.max((int) (1 + getSettings().surfaceLevel + getSettings().getHeight(noise, pX, pZ)), getSeaLevel());
    }

    @Override
    public @NotNull NoiseColumn getBaseColumn(int pX, int pZ, @NotNull LevelHeightAccessor pHeight, @NotNull RandomState pRandom) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(@NotNull List<String> pInfo, @NotNull RandomState pRandom, @NotNull BlockPos pPos) {
        getSettings().getMapBiome(pPos.getX() >> 2, pPos.getZ() >> 2).unwrapKey().ifPresent(key -> pInfo.add("Map Biome: " + key.location()));
        pInfo.add("Pixel Pos: X: " + getSettings().xOffset(pPos.getX() >> 2) + " Y: " + getSettings().yOffset(pPos.getZ() >> 2));
    }

    @Override
    @NotNull
    public MapBasedBiomeSource getBiomeSource() {
        return biomeSource;
    }

    private MapSettings getSettings() {
        return biomeSource.settings;
    }

    public void setNoise(RandomState randomState) {
        if (noise == null) {
            RandomSource randomSource = randomState.getOrCreateRandomFactory(CTerrainGeneration.id("generator")).at(0, 0, 0);
            noise = new SimplexNoise(randomSource);
        }
    }
}