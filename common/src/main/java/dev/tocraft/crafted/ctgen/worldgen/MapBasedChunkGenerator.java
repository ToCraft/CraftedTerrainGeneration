package dev.tocraft.crafted.ctgen.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.zone.CarverSetting;
import dev.tocraft.crafted.ctgen.zone.Zone;
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MapBasedChunkGenerator extends ChunkGenerator {
    public static final Codec<MapBasedChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            MapSettings.CODEC.fieldOf("settings").forGetter(MapBasedChunkGenerator::getSettings)
    ).apply(instance, instance.stable(MapBasedChunkGenerator::of)));

    private static final int BEDROCK_SIZE = 3;
    private static final int DIRT_SIZE = 6;

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

        int minHeight = getSettings().minY + BEDROCK_SIZE;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int xOff = chunk.getPos().getBlockX(x);
                int zOff = chunk.getPos().getBlockZ(z);

                Zone zone = getSettings().getZone(xOff >> 2, zOff >> 2).value();
                double surfaceHeight = getSettings().getHeight(noise, xOff, zOff) + getSettings().surfaceLevel;
                int thresholdModifier = (int) getSettings().getValueWithTransition(xOff, zOff, zo -> (double) zo.thresholdModifier().orElse(getSettings().thresholdModifier));

                Block surfaceBlock = zone.surfaceBlock().get(noise, xOff, zOff);
                // no grass underwater
                if (surfaceHeight < getSeaLevel() && surfaceBlock == Blocks.GRASS_BLOCK) {
                    surfaceBlock = Blocks.DIRT;
                }

                int shift = (int) (noise.getValue(xOff, zOff) * 3);
                int bedrockLevel = minHeight + shift;
                int deepslateLevel = getSettings().deepslateLevel + shift;
                int dirtLevel = (int) (surfaceHeight - DIRT_SIZE + shift);

                for (int y = getSettings().minY; y <= surfaceHeight || y <= getSeaLevel(); y++) {
                    BlockPos pos = chunk.getPos().getBlockAt(x, y, z);
                    if (y < bedrockLevel) {
                        // place bedrock
                        chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false);
                    } else if (y > surfaceHeight && surfaceHeight < getSeaLevel()) {
                        // place oceans if the surface isn't higher than the sea level
                        chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
                        // check for caves
                    } else if (canSetBlock(pos, surfaceHeight, getSettings().deepslateLevel, minHeight + 3, thresholdModifier)) {
                        if (y < deepslateLevel && deepslateLevel < surfaceHeight) {
                            // place deepslate
                            chunk.setBlockState(pos, zone.deepslateBlock().get(noise, pos.getX(), pos.getY(), pos.getZ()).defaultBlockState(), false);
                        } else if (y < dirtLevel) {
                            // place stone between deepslate and dirt
                            chunk.setBlockState(pos, zone.stoneBlock().get(noise, pos.getX(), pos.getY(), pos.getZ()).defaultBlockState(), false);
                        } else if (y < surfaceHeight - 1) {
                            // place dirt below surface
                            chunk.setBlockState(pos, zone.dirtBlock().get(noise, pos.getX(), pos.getY(), pos.getZ()).defaultBlockState(), false);
                        }
                        // only surface is missing
                        else {
                            chunk.setBlockState(pos, surfaceBlock.defaultBlockState(), false);
                        }
                    }
                }
            }
        }
    }

    private boolean canSetBlock(BlockPos pos, double surfaceHeight, int deepslateLevel, int bedrockLevel, int thresholdModifier) {
        double height = (double) (pos.getY() - bedrockLevel) / (surfaceHeight - bedrockLevel) - 0.5;
        int mod = height > (deepslateLevel / (surfaceHeight - bedrockLevel) - 0.5) ? thresholdModifier : 16;
        double addThreshold = height * height * height * height * mod;

        for (CarverSetting carver : getSettings().carverSettings) {
            double perlin = carver.noise().getPerlin(this.noise, pos.getX(), pos.getY(), pos.getZ());

            double threshold = carver.threshold() + addThreshold;
            if (perlin > threshold) {
                return false;
            }
        }

        return true;
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
        getSettings().getZone(pPos.getX() >> 2, pPos.getZ() >> 2).unwrapKey().ifPresent(key -> pInfo.add("Zone: " + key.location()));
        pInfo.add("Pixel Pos: X: " + getSettings().xOffset(pPos.getX() >> 2) + " Y: " + getSettings().yOffset(pPos.getZ() >> 2));
    }

    @Override
    @NotNull
    public MapBasedBiomeSource getBiomeSource() {
        return biomeSource;
    }

    @ApiStatus.Internal
    public MapSettings getSettings() {
        return biomeSource.settings;
    }

    public void setNoise(RandomState randomState) {
        if (noise == null) {
            RandomSource randomSource = randomState.getOrCreateRandomFactory(CTerrainGeneration.id("generator")).at(0, 0, 0);
            noise = new SimplexNoise(randomSource);
        }
    }
}