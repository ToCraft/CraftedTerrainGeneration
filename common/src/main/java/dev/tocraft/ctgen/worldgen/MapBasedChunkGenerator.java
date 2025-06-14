package dev.tocraft.ctgen.worldgen;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.data.SurfaceBuilderAccess;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class MapBasedChunkGenerator extends ChunkGenerator {
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public static final ResourceLocation ID = CTerrainGeneration.id("map_based_chunk_generator");
    public static final MapCodec<MapBasedChunkGenerator> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            MapSettings.CODEC.fieldOf("settings").forGetter(MapBasedChunkGenerator::getSettings)
    ).apply(instance, instance.stable(MapBasedChunkGenerator::of)));

    private final NoiseBasedChunkGenerator delegate;;

    protected final MapBasedBiomeSource biomeSource;
    private SimplexNoise noise = null;

    private MapBasedChunkGenerator(MapBasedBiomeSource biomeSource) {
        super(biomeSource);
        this.biomeSource = biomeSource;
        this.delegate = new NoiseBasedChunkGenerator(biomeSource, getSettings().noiseGenSettings);
        LogUtils.getLogger().warn("LOL: " + getSettings().surfaceLevel);
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
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState noiseConfig, ChunkAccess chunk) {
        if (SharedConstants.debugVoidTerrain(chunk.getPos())) {
            return;
        }
        WorldGenerationContext heightContext = new WorldGenerationContext(this, region);
        this.buildSurface(chunk, heightContext, noiseConfig, structures, region.getBiomeManager(), region.registryAccess().lookupOrThrow(Registries.BIOME), Blender.of(region));
    }

    @VisibleForTesting
    public void buildSurface(ChunkAccess chunk, WorldGenerationContext heightContext, RandomState noiseConfig, StructureManager structureAccessor, BiomeManager biomeAccess, Registry<Biome> biomeRegistry, Blender blender) {
        NoiseGeneratorSettings chunkGeneratorSettings = this.getNoiseGenSettings();
        NoiseChunk chunkNoiseSampler = chunk.getOrCreateNoiseChunk(chunk3 -> this.createChunkNoiseSampler(chunkGeneratorSettings, chunk3, structureAccessor, blender, noiseConfig));
        ((SurfaceBuilderAccess) noiseConfig.surfaceSystem()).ctgen$buildSurface(noiseConfig, biomeAccess, biomeRegistry, chunkGeneratorSettings.useLegacyRandomSource(), heightContext, chunk, chunkNoiseSampler, chunkGeneratorSettings.surfaceRule(), this::getSettings);
    }

    private NoiseChunk createChunkNoiseSampler(NoiseGeneratorSettings settings, ChunkAccess chunk, StructureManager world, Blender blender, RandomState noiseConfig) {
        return NoiseChunk.forChunk(chunk, noiseConfig, Beardifier.forStructuresInChunk(world, chunk.getPos()), settings, this.createFluidPicker(settings), blender);
    }

    private Aquifer.@NotNull FluidPicker createFluidPicker(@NotNull NoiseGeneratorSettings settings) {
        Aquifer.FluidStatus fluidLevel = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int i = settings.seaLevel();
        return (x, y, z) -> {
            if (y < Math.min(-54, i)) {
                //return fluidLevel; // TODO: uncomment for lava
            }
            return new Aquifer.FluidStatus(settings.seaLevel(), settings.defaultFluid());
        };
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState noiseConfig, StructureManager structureAccessor, @NotNull ChunkAccess chunk) {
        NoiseSettings generationShapeConfig = getNoiseGenSettings().noiseSettings().clampToHeightAccessor(chunk.getHeightAccessorForGeneration());
        int k = Mth.floorDiv(generationShapeConfig.height(), generationShapeConfig.noiseSizeVertical());
        if (k <= 0) {
            return CompletableFuture.completedFuture(chunk);
        }
        int minimumCellY = Mth.floorDiv(generationShapeConfig.minY(), generationShapeConfig.getCellHeight());
        int cellHeight = Mth.floorDiv(generationShapeConfig.height(), generationShapeConfig.getCellHeight());
        return CompletableFuture.supplyAsync(() -> this.populateNoise(chunk, structureAccessor, blender, noiseConfig, minimumCellY, cellHeight), Util.backgroundExecutor());
    }

    @Contract("_, _, _, _, _, _ -> param1")
    private @NotNull ChunkAccess populateNoise(@NotNull ChunkAccess chunk, StructureManager accessor, Blender blender, RandomState noiseConfig, int minimumCellY, int cellHeight) {
        NoiseChunk chunkNoiseSampler = chunk.getOrCreateNoiseChunk(chunk1 -> this.createChunkNoiseSampler(getNoiseGenSettings(), chunk, accessor, blender, noiseConfig));
        Heightmap oceanHeightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surfaceHeightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.getMinBlockX();
        int j = chunkPos.getMinBlockZ();
        Aquifer aquiferSampler = chunkNoiseSampler.aquifer();
        chunkNoiseSampler.initializeForFirstCellX();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int minY = getNoiseGenSettings().noiseSettings().minY();
        int k = chunkNoiseSampler.cellWidth();
        int l = chunkNoiseSampler.cellHeight();
        int m = 16 / k;
        int n = 16 / k;
        BlockState defaultBlock = getNoiseGenSettings().defaultBlock();
        BlockState defaultFluid = getNoiseGenSettings().defaultFluid();
        for (int o = 0; o < m; ++o) {
            chunkNoiseSampler.advanceCellX(o);
            for (int p = 0; p < n; ++p) {
                int q1 = chunk.getSectionsCount() - 1;
                LevelChunkSection chunkSection = chunk.getSection(q1);
                for (int q = cellHeight - 1; q >= 0; --q) {
                    chunkNoiseSampler.selectCellYZ(q, p);
                    for (int r = l - 1; r >= 0; --r) {
                        int s = (minimumCellY + q) * l + r;
                        int t = s & 0xF;
                        int u = chunk.getSectionIndex(s);
                        if (q1 != u) {
                            q1 = u;
                            chunkSection = chunk.getSection(u);
                        }
                        double d = (double) r / (double) l;
                        chunkNoiseSampler.updateForY(s, d);
                        for (int v = 0; v < k; ++v) {
                            int w = i + o * k + v;
                            int x = w & 0xF;
                            double e = (double) v / (double) k;
                            chunkNoiseSampler.updateForX(w, e);
                            for (int y = 0; y < k; ++y) {
                                int z = j + p * k + y;
                                int aa = z & 0xF;
                                double f = (double) y / (double) k;
                                chunkNoiseSampler.updateForZ(z, f);
                                int blockX = chunkNoiseSampler.blockX();
                                int blockY = chunkNoiseSampler.blockY();
                                int blockZ = chunkNoiseSampler.blockZ();
                                mutable.set(blockX, blockY, blockZ);
                                int seaLevel = this.getSeaLevel();
                                int elevation = Math.min(this.getSettings().getElevation(blockX, blockZ), this.getMinY() + this.getNoiseGenSettings().noiseSettings().height());
                                if (blockY >= seaLevel && blockY >= elevation || elevation < this.getMinY())
                                    continue;
                                int height = blockY - minY;
                                int maxHeight = elevation - minY;
                                double cave;
                                BlockState state;
                                if (maxHeight - height <= 10) {
                                    cave = noiseConfig.router().initialDensityWithoutJaggedness().compute(chunkNoiseSampler);
                                    BlockState caveAir;
                                    if (elevation < seaLevel) {
                                        caveAir = defaultFluid;
                                    } else {
                                        caveAir = AIR;
                                    }
                                    if (blockY < elevation) {
                                        if (cave > 0) {
                                            state = defaultBlock;
                                        } else {
                                            state = caveAir;
                                        }
                                    } else if (blockY < seaLevel) {
                                        state = defaultFluid;
                                    } else {
                                        state = AIR;
                                    }
                                    chunk.setBlockState(mutable, state, false);
                                    surfaceHeightmap.update(blockX & 0xF, blockY, blockZ & 0xF, state);
                                    oceanHeightmap.update(blockX & 0xF, blockY, blockZ & 0xF, state);
                                } else {
                                    state = chunkNoiseSampler.getInterpolatedState();
                                    if (state == null) {
                                        state = getNoiseGenSettings().defaultBlock();
                                    }
                                    if ((state == AIR || SharedConstants.debugVoidTerrain(chunk.getPos())))
                                        continue;
                                    chunkSection.setBlockState(x, t, aa, state, false);
                                    oceanHeightmap.update(x, s, aa, state);
                                    surfaceHeightmap.update(x, s, aa, state);
                                }
                                if (!aquiferSampler.shouldScheduleFluidUpdate() || state.getFluidState().isEmpty()) continue;
                                mutable.set(w, s, z);
                                chunk.markPosForPostprocessing(mutable);
                            }
                        }
                    }
                }
            }
            chunkNoiseSampler.swapSlices();
        }
        chunkNoiseSampler.stopInterpolation();
        return chunk;
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
    public int getSeaLevel() {
        return getNoiseGenSettings().seaLevel();
    }

    @Override
    public int getMinY() {
        return getSettings().minY;
    }

    @Override
    public int getBaseHeight(int pX, int pZ, @NotNull Heightmap.Types pType, @NotNull LevelHeightAccessor pLevel, @NotNull RandomState pRandom) {
        setNoise(pRandom);
        return Math.max(getSettings().getElevation(pX, pZ), getSeaLevel());
    }

    @Override
    public @NotNull NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world, RandomState noiseConfig) {
        int elevation = this.getSettings().getElevation(x, z);
        int seaLevel = this.getSeaLevel();
        if (elevation < this.getMinY())
            return new NoiseColumn(world.getMinY(), new BlockState[]{Blocks.AIR.defaultBlockState()});
        if (elevation < seaLevel) {
            return new NoiseColumn(
                    this.getSettings().minY,
                    Stream.concat(
                            Stream.generate(() -> this.getNoiseGenSettings().defaultBlock()).limit(elevation - this.getMinY()),
                            Stream.generate(() -> this.getNoiseGenSettings().defaultFluid()).limit(seaLevel - elevation - this.getMinY())
                    ).toArray(BlockState[]::new));
        }
        return new NoiseColumn(
                this.getSettings().minY,
                Stream.generate(() -> this.getNoiseGenSettings().defaultBlock()).limit(elevation - this.getMinY() + 1).toArray(BlockState[]::new)

        );
    }

    @Override
    public void addDebugScreenInfo(@NotNull List<String> pInfo, @NotNull RandomState pRandom, @NotNull BlockPos pPos) {
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

    @ApiStatus.Internal
    public NoiseGeneratorSettings getNoiseGenSettings() {
        return biomeSource.settings.noiseGenSettings.value();
    }

    public void setNoise(RandomState randomState) {
        if (noise == null) {
            RandomSource randomSource = randomState.getOrCreateRandomFactory(CTerrainGeneration.id("generator")).at(0, 0, 0);
            noise = new SimplexNoise(randomSource);
        }
    }
}