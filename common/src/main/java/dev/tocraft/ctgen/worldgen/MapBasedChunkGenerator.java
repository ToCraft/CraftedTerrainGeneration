package dev.tocraft.ctgen.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.xtend.layer.BlockLayer;
import dev.tocraft.ctgen.xtend.placer.BasicPlacer;
import dev.tocraft.ctgen.xtend.placer.BlockPlacer;
import dev.tocraft.ctgen.zone.Zone;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MapBasedChunkGenerator extends ChunkGenerator {
    public static final Codec<MapBasedChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            MapSettings.CODEC.fieldOf("settings").forGetter(MapBasedChunkGenerator::getSettings)
    ).apply(instance, instance.stable(MapBasedChunkGenerator::of)));

    private static final int BEDROCK_SIZE = 3;

    protected final MapBasedBiomeSource biomeSource;
    private SimplexNoise noise = null;

    private MapBasedChunkGenerator(MapBasedBiomeSource biomeSource) {
        super(biomeSource);
        this.biomeSource = biomeSource;
    }

    public static @NotNull MapBasedChunkGenerator of(MapSettings settings) {
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

                int shift = (int) (noise.getValue(xOff, zOff) * 3);
                int bedrockLevel = minHeight + shift;

                for (int y = getSettings().minY; y < surfaceHeight || y <= getSeaLevel(); y++) {
                    BlockPos pos = chunk.getPos().getBlockAt(x, y, z);
                    if (y < bedrockLevel) {
                        // place bedrock
                        chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false);
                    } else {
                        @Nullable BlockLayer layer = null;
                        for (BlockLayer blockLayer : getSettings().getLayers()) {
                            if (blockLayer.is(this.noise, x, y, z, zone, getMinY(), getSeaLevel(), surfaceHeight, getGenDepth(), shift)) {
                                layer = blockLayer;
                                break;
                            }
                        }

                        BlockPlacer placer = layer != null ? zone.layers().getOrDefault(layer.getName(), layer.getFallback()) : BasicPlacer.AIR;
                        Block block = placer.get(this.noise, x, y, z, layer != null ? layer.getName() : "fill");

                        if (layer != null && !layer.hasCaves() || canSetBlock(pos, surfaceHeight, minHeight, thresholdModifier)) {
                            // no grass underwater
                            if (surfaceHeight < getSeaLevel() && block == Blocks.GRASS_BLOCK) {
                                block = Blocks.DIRT;
                            }

                            BlockState blockState = block.defaultBlockState();

                            // is air by default, no need to place it again
                            if (!blockState.isAir()) {
                                chunk.setBlockState(pos, blockState, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean canSetBlock(@NotNull BlockPos pos, double surfaceHeight, int minHeight, int thresholdModifier) {
        double height = (double) (pos.getY() - minHeight) / (surfaceHeight - minHeight) - 0.5;
        double addThreshold = height * height * height * height * thresholdModifier;

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