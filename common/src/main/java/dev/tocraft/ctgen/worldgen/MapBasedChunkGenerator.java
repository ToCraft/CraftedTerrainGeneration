package dev.tocraft.ctgen.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.xtend.layer.BlockLayer;
import dev.tocraft.ctgen.xtend.placer.BasicPlacer;
import dev.tocraft.ctgen.xtend.placer.BlockPlacer;
import dev.tocraft.ctgen.zone.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
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

public class MapBasedChunkGenerator extends ChunkGenerator {
    public static final ResourceLocation ID = CTerrainGeneration.id("map_based_chunk_generator");
    public static final MapCodec<MapBasedChunkGenerator> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            MapSettings.CODEC.fieldOf("settings").forGetter(MapBasedChunkGenerator::getSettings)
    ).apply(instance, instance.stable(MapBasedChunkGenerator::of)));

    private final NoiseBasedChunkGenerator delegate;

    private static final int BEDROCK_SIZE = 3;

    protected final MapBasedBiomeSource biomeSource;
    private SimplexNoise noise = null;

    private MapBasedChunkGenerator(MapBasedBiomeSource biomeSource) {
        super(biomeSource);
        this.biomeSource = biomeSource;
        this.delegate = new NoiseBasedChunkGenerator(biomeSource, biomeSource.settings.noiseGenSettings);
    }

    public static @NotNull MapBasedChunkGenerator of(MapSettings settings) {
        MapBasedBiomeSource biomeSource = new MapBasedBiomeSource(settings);
        return new MapBasedChunkGenerator(biomeSource);
    }

    @Override
    protected @NotNull MapCodec<MapBasedChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion chunkRegion, long seed, RandomState noiseConfig, BiomeManager biomeAccess, StructureManager structureAccessor, ChunkAccess chunk2) {
        delegate.applyCarvers(chunkRegion, seed, noiseConfig, biomeAccess, structureAccessor, chunk2);
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
                        Block block = placer.get(this.noise, pos.getX(), pos.getY(), pos.getZ(), surfaceHeight, layer != null ? layer.getName() : "fill");

                        if (layer != null) {
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

    @Override
    public void spawnOriginalMobs(@NotNull WorldGenRegion pLevel) {
        ChunkPos chunkpos = pLevel.getCenter();
        Holder<Biome> holder = pLevel.getBiome(chunkpos.getWorldPosition().atY(pLevel.getMaxY() - 1));
        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        worldgenrandom.setDecorationSeed(pLevel.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(pLevel, holder, chunkpos, worldgenrandom);
    }

    @Override
    public int getGenDepth() {
        return getSettings().genHeight;
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.completedFuture(chunkAccess);
    }

    @Override
    public int getSeaLevel() {
        return getSettings().noiseGenSettings.value().seaLevel();
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