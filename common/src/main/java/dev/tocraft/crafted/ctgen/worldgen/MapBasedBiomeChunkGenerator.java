package dev.tocraft.crafted.ctgen.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.map.MapBiome;
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

public class MapBasedBiomeChunkGenerator extends ChunkGenerator {
    public static final Codec<MapBasedBiomeChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            MapSettings.CODEC.fieldOf("settings").forGetter(o -> o.biomeSource.getSettings())
    ).apply(instance, instance.stable(MapBasedBiomeChunkGenerator::of)));

    protected final MapBasedBiomeSource biomeSource;
    private SimplexNoise noise = null;

    private MapBasedBiomeChunkGenerator(MapBasedBiomeSource biomeSource) {
        super(biomeSource);
        this.biomeSource = biomeSource;
    }

    public static MapBasedBiomeChunkGenerator of(MapSettings settings) {
        MapBasedBiomeSource biomeSource = new MapBasedBiomeSource(settings);
        return new MapBasedBiomeChunkGenerator(biomeSource);
    }

    @Override
    protected @NotNull Codec<MapBasedBiomeChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(@NotNull WorldGenRegion pLevel, long pSeed, @NotNull RandomState pRandom, @NotNull BiomeManager pBiomeManager, @NotNull StructureManager pStructureManager, @NotNull ChunkAccess pChunk, GenerationStep.@NotNull Carving pStep) {
    }

    /**
     * places stone and dirt. Will be replaced by biomes automatically
     */
    @Override
    public void buildSurface(@NotNull WorldGenRegion pLevel, @NotNull StructureManager pStructureManager, @NotNull RandomState pRandom, @NotNull ChunkAccess chunk) {
        setNoise(pRandom);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos pos = chunk.getPos().getBlockAt(x, chunk.getHeight(), z);

                Holder<Biome> biomeHolder = pLevel.getBiome(pos);
                for (MapBiome biomeData : biomeSource.getSettings().biomeData().stream().map(Holder::value).toList()) {

                    if (biomeHolder.equals(biomeData.biome())) {
                        double height = biomeSource.getSettings().getHeight(noise, pos.getX(), pos.getZ());

                        for (int y = chunk.getMinBuildHeight(); y < chunk.getMinBuildHeight() + 4; y++) {
                            chunk.setBlockState(chunk.getPos().getBlockAt(x, y, z), Blocks.BEDROCK.defaultBlockState(), false);
                        }

                        for (int y = chunk.getMinBuildHeight() + 4; y < biomeSource.getSettings().deepslateLevel(); y++) {
                            chunk.setBlockState(chunk.getPos().getBlockAt(x, y, z), biomeData.deepslateBlock().defaultBlockState(), false);
                        }

                        double dirtHeight = this.biomeSource.getSettings().dirtLevel() + height - 1;

                        // stone
                        for (int y = this.biomeSource.getSettings().deepslateLevel(); y <= dirtHeight; y++) {
                            chunk.setBlockState(chunk.getPos().getBlockAt(x, y, z), biomeData.stoneBlock().defaultBlockState(), false);
                        }

                        for (int y = (int) (this.biomeSource.getSettings().dirtLevel() - 4 + height); y < this.biomeSource.getSettings().dirtLevel() + height; y++) {
                            chunk.setBlockState(chunk.getPos().getBlockAt(x, y, z), biomeData.dirtBlock().defaultBlockState(), false);
                        }
                        Block grassBlock = biomeData.surfaceBlock();

                        // no grass underwater
                        if (this.biomeSource.getSettings().dirtLevel() + height < getSeaLevel() && grassBlock == Blocks.GRASS_BLOCK) {
                            grassBlock = Blocks.DIRT;
                        }

                        chunk.setBlockState(chunk.getPos().getBlockAt(x, (int) (this.biomeSource.getSettings().dirtLevel() + height), z), grassBlock.defaultBlockState(), false);

                        // place oceans
                        for (int y = (int) (this.biomeSource.getSettings().dirtLevel() + height + 1); y <= this.getSeaLevel(); y++) {
                            chunk.setBlockState(chunk.getPos().getBlockAt(x, y, z), Blocks.WATER.defaultBlockState(), false);
                        }

                        break;
                    }
                }
            }
        }
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
        return this.biomeSource.getSettings().height();
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(@NotNull Executor pExecutor, @NotNull Blender pBlender, @NotNull RandomState pRandom, @NotNull StructureManager pStructureManager, @NotNull ChunkAccess pChunk) {
        return CompletableFuture.completedFuture(pChunk);
    }

    @Override
    public int getSeaLevel() {
        return biomeSource.getSettings().seaLevel();
    }

    @Override
    public int getMinY() {
        return this.biomeSource.getSettings().minY();
    }

    @Override
    public int getBaseHeight(int pX, int pZ, @NotNull Heightmap.Types pType, @NotNull LevelHeightAccessor pLevel, @NotNull RandomState pRandom) {
        setNoise(pRandom);
        return Math.max((int) (1 + biomeSource.getSettings().dirtLevel() + biomeSource.getSettings().getHeight(noise, pX, pZ)), getSeaLevel());
    }

    @Override
    public @NotNull NoiseColumn getBaseColumn(int pX, int pZ, @NotNull LevelHeightAccessor pHeight, @NotNull RandomState pRandom) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(@NotNull List<String> pInfo, @NotNull RandomState pRandom, @NotNull BlockPos pPos) {
        biomeSource.getSettings().getMapBiome(pPos.getX() >> 2, pPos.getZ() >> 2).unwrapKey().ifPresent(key -> pInfo.add("Map Biome: " + key.location()));
        pInfo.add("Pixel Pos: X: " + biomeSource.getSettings().xOffset(pPos.getX() >> 2) + " Y: " + biomeSource.getSettings().yOffset(pPos.getZ() >> 2));
    }

    @Override
    @NotNull
    public MapBasedBiomeSource getBiomeSource() {
        return biomeSource;
    }

    public void setNoise(RandomState randomState) {
        if (noise == null) {
            RandomSource randomSource = randomState.getOrCreateRandomFactory(CTerrainGeneration.id("generator")).at(0, 0, 0);
            noise = new SimplexNoise(randomSource);
        }
    }
}