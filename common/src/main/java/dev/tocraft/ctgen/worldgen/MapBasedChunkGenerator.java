package dev.tocraft.ctgen.worldgen;

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
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class MapBasedChunkGenerator extends ChunkGenerator {
    public static final ResourceLocation ID = CTerrainGeneration.id("map_based_chunk_generator");
    public static final MapCodec<MapBasedChunkGenerator> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            MapSettings.CODEC.fieldOf("settings").forGetter(MapBasedChunkGenerator::getSettings)
    ).apply(instance, instance.stable(MapBasedChunkGenerator::of)));

    private final NoiseBasedChunkGenerator delegate;

    protected final MapBasedBiomeSource biomeSource;
    private SimplexNoise noise = null;

    private MapBasedChunkGenerator(MapBasedBiomeSource biomeSource) {
        super(biomeSource);
        this.biomeSource = biomeSource;
        this.delegate = new NoiseBasedChunkGenerator(biomeSource, getSettings().noiseGenSettings);
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
        setNoise(noiseConfig);
        delegate.applyCarvers(chunkRegion, seed, noiseConfig, biomeAccess, structureAccessor, chunk2);
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState noiseConfig, ChunkAccess chunk) {
        setNoise(noiseConfig);
        if (SharedConstants.debugVoidTerrain(chunk.getPos())) {
            return;
        }
        WorldGenerationContext heightContext = new WorldGenerationContext(this, region);
        this.buildSurface(chunk, heightContext, noiseConfig, structures, region.getBiomeManager(), region.registryAccess().lookupOrThrow(Registries.BIOME), Blender.of(region));
    }

    private void buildSurface(ChunkAccess chunk, WorldGenerationContext heightContext, RandomState noiseConfig, StructureManager structureAccessor, BiomeManager biomeAccess, Registry<Biome> biomeRegistry, Blender blender) {
        NoiseGeneratorSettings chunkGeneratorSettings = this.getNoiseGenSettings();
        NoiseChunk chunkNoiseSampler = chunk.getOrCreateNoiseChunk(chunk3 -> this.createChunkNoiseSampler(chunkGeneratorSettings, chunk3, structureAccessor, blender, noiseConfig));
        ((SurfaceBuilderAccess) noiseConfig.surfaceSystem()).ctgen$buildSurface(noiseConfig, biomeAccess, biomeRegistry, chunkGeneratorSettings.useLegacyRandomSource(), heightContext, chunk, chunkNoiseSampler, chunkGeneratorSettings.surfaceRule(), this::getSettings, () -> this.noise);
    }

    private NoiseChunk createChunkNoiseSampler(NoiseGeneratorSettings settings, ChunkAccess chunk, StructureManager world, Blender blender, RandomState noiseConfig) {
        return NoiseChunk.forChunk(chunk, noiseConfig, Beardifier.forStructuresInChunk(world, chunk.getPos()), settings, this.createFluidPicker(settings), blender);
    }

    private Aquifer.@NotNull FluidPicker createFluidPicker(@NotNull NoiseGeneratorSettings settings) {
        Aquifer.FluidStatus fluidLevel = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int i = settings.seaLevel();
        return (x, y, z) -> {
            if (y < Math.min(-54, i)) {
                return fluidLevel;
            }
            return new Aquifer.FluidStatus(settings.seaLevel(), settings.defaultFluid());
        };
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState noiseConfig, StructureManager structureAccessor, @NotNull ChunkAccess chunk) {
        setNoise(noiseConfig);

        NoiseSettings generationShapeConfig = getNoiseGenSettings().noiseSettings().clampToHeightAccessor(chunk.getHeightAccessorForGeneration());
        int k = Mth.floorDiv(generationShapeConfig.height(), generationShapeConfig.noiseSizeVertical());
        if (k <= 0) {
            return CompletableFuture.completedFuture(chunk);
        }
        return CompletableFuture.supplyAsync(() -> this.fill(chunk), Util.backgroundExecutor());
    }

    private @NotNull ChunkAccess fill(@NotNull ChunkAccess chunk) {
        BlockState defaultBlock = biomeSource.settings.noiseGenSettings.value().defaultBlock();
        BlockState defaultFluid = biomeSource.settings.noiseGenSettings.value().defaultFluid();

        ChunkPos chunkPos = chunk.getPos();
        int minY = getNoiseGenSettings().noiseSettings().minY();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int xOff = chunk.getPos().getBlockX(x);
                int zOff = chunk.getPos().getBlockZ(z);

                double surfaceHeight = getSettings().getHeight(noise, xOff, zOff);

                for (int y = minY; y < surfaceHeight; y++) {
                    BlockPos pos = chunkPos.getBlockAt(x, y, z);
                    chunk.setBlockState(pos, defaultBlock , false);
                }

                for (int y = (int) surfaceHeight; y < getSeaLevel(); y++) {
                    BlockPos pos = chunkPos.getBlockAt(x, y, z);
                    chunk.setBlockState(pos, defaultFluid , false);
                }
            }
        }

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
        return getNoiseGenSettings().noiseSettings().height();
    }

    @Override
    public int getSeaLevel() {
        return getNoiseGenSettings().seaLevel();
    }

    @Override
    public int getMinY() {
        return getNoiseGenSettings().noiseSettings().minY();
    }

    @Override
    public int getBaseHeight(int pX, int pZ, @NotNull Heightmap.Types pType, @NotNull LevelHeightAccessor pLevel, @NotNull RandomState pRandom) {
        setNoise(pRandom);
        return Math.max((int) (1 + getSettings().getHeight(noise, pX, pZ)), getSeaLevel());
    }

    @Override
    public @NotNull NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world, RandomState noiseConfig) {
        setNoise(noiseConfig);
        int elevation = Math.max((int) (1 + getSettings().getHeight(noise, x, z)), getSeaLevel());
        int seaLevel = this.getSeaLevel();
        if (elevation < this.getMinY())
            return new NoiseColumn(world.getMinY(), new BlockState[]{Blocks.AIR.defaultBlockState()});
        if (elevation < seaLevel) {
            return new NoiseColumn(
                    this.getMinY(),
                    Stream.concat(
                            Stream.generate(() -> this.getNoiseGenSettings().defaultBlock()).limit(elevation - this.getMinY()),
                            Stream.generate(() -> this.getNoiseGenSettings().defaultFluid()).limit(seaLevel - elevation - this.getMinY())
                    ).toArray(BlockState[]::new));
        }
        return new NoiseColumn(
                this.getMinY(),
                Stream.generate(() -> this.getNoiseGenSettings().defaultBlock()).limit(elevation - this.getMinY() + 1).toArray(BlockState[]::new)

        );
    }

    @Override
    public void addDebugScreenInfo(@NotNull List<String> pInfo, @NotNull RandomState pRandom, @NotNull BlockPos pPos) {
        setNoise(pRandom);
        pInfo.add("Pixel Pos: X: " + getSettings().xOffset(pPos.getX() >> 2) + " Y: " + getSettings().yOffset(pPos.getZ() >> 2));
        // only show 'Zone' when it is actually registered
        getSettings().getZone(pPos.getX() >> 2, pPos.getZ() >> 2).unwrapKey().ifPresent(zoneResourceKey -> pInfo.add("Zone: " + zoneResourceKey.location()));
        pInfo.add("Pixel Height: " + getSettings().getRedHeight(pPos.getX() >> 2, pPos.getZ() >> 2));
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