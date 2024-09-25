package dev.tocraft.crafted.ctgen.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.biome.CarverSetting;
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
    public void applyCarvers(@NotNull WorldGenRegion level, long pSeed, @NotNull RandomState random, @NotNull BiomeManager pBiomeManager, @NotNull StructureManager pStructureManager, @NotNull ChunkAccess chunk, GenerationStep.@NotNull Carving step) {
    }

    @Override
    public void buildSurface(@NotNull WorldGenRegion pLevel, @NotNull StructureManager pStructureManager, @NotNull RandomState pRandom, @NotNull ChunkAccess chunk) {
        setNoise(pRandom);

        int minHeight = chunk.getMinBuildHeight() + BEDROCK_SIZE;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int xOff = chunk.getPos().getBlockX(x);
                int zOff = chunk.getPos().getBlockZ(z);

                MapBiome biomeData = getSettings().getMapBiome(xOff >> 2, zOff >> 2).value();
                double surfaceHeight = getSettings().getHeight(noise, xOff, zOff) + getSettings().surfaceLevel;

                for (int y = chunk.getMinBuildHeight(); y <= surfaceHeight || y <= getSeaLevel(); y++) {
                    BlockPos pos = chunk.getPos().getBlockAt(x, y, z);
                    if (y < minHeight) {
                        // place bedrock
                        chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false);
                        continue;
                    } else if (y > surfaceHeight && surfaceHeight < getSeaLevel()) {
                        // place oceans if the surface isn't higher than the sea level
                        chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
                    // check for caves
                    } else if (canSetBlock(pos, minHeight, surfaceHeight)) {
                        if (y < getSettings().deepslateLevel && getSettings().deepslateLevel < surfaceHeight) {
                            // place deepslate
                            chunk.setBlockState(pos, biomeData.deepslateBlock().defaultBlockState(), false);
                        } else if (y < surfaceHeight - DIRT_SIZE) {
                            // place stone between deepslate and surface - DIRT_SIZE
                            chunk.setBlockState(pos, biomeData.stoneBlock().defaultBlockState(), false);
                        } else if (y < surfaceHeight - 1) {
                            // place dirt below surface
                            chunk.setBlockState(pos, biomeData.dirtBlock().defaultBlockState(), false);
                        }
                        // only surface is missing
                        else {
                            Block surfaceBlock = biomeData.surfaceBlock();
                            // no grass underwater
                            if (surfaceHeight < getSeaLevel() && surfaceBlock == Blocks.GRASS_BLOCK) {
                                surfaceBlock = Blocks.DIRT;
                            }
                            chunk.setBlockState(pos, surfaceBlock.defaultBlockState(), false);
                        }
                    }
                }
            }
        }
    }

    private boolean canSetBlock(BlockPos pos, int minHeight, double surfaceHeight) {
        // Modify cave threshold based on y, (nearly) no caves for 100% height nor 0%
        // Get scaled noise values
        int y2 = pos.getY() - minHeight;
        int deepLevel = getSettings().deepslateLevel - minHeight;
        for (CarverSetting carver : getSettings().carverSettings) {
            double threshold;
            if (y2 <= deepLevel) {
                threshold = lerp(carver.bedrockThreshold(), carver.midThreshold(), (double) y2 / deepLevel);
            } else {
                double halfSurfaceLevel = surfaceHeight / 2;
                if (pos.getY() > halfSurfaceLevel) {
                    double entryThreshold = getSettings().getValueWithTransition(pos.getX(), pos.getZ(), mapBiome -> mapBiome.caveThreshold().orElse(carver.entryThreshold()));
                    threshold = lerp(carver.midThreshold(), entryThreshold, (pos.getY() - halfSurfaceLevel) / halfSurfaceLevel);
                } else {
                    threshold = carver.midThreshold();
                }
            }

            // get noise value
            double perlin = getPerlin3D(carver.detail(), (double) pos.getX() / carver.caveStretchXZ(), (double) pos.getY() / carver.caveStretchY(), (double) pos.getZ() / carver.caveStretchXZ());

            // check if there should be a cave
            if (perlin > threshold) {
                return false;
            }
        }

        return true;
    }

    private static double lerp(double start, double end, double percentage) {
        return (start * (1 - percentage)) + (end * percentage);
    }

    public double getPerlin3D(int detail, double x, double y, double z) {
        double perlin = 0;
        for (int i = 0; i < detail; i++) {
            perlin += Math.pow(0.5, i) * noise.getValue(x * Math.pow(2, i) / detail, y * Math.pow(2, i) / detail, z * Math.pow(2, i) / detail);
        }
        perlin = perlin / (1 - Math.pow(0.5, detail));
        return perlin;
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