package dev.tocraft.crafted.ctgen.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.biome.CarverSetting;
import dev.tocraft.crafted.ctgen.biome.MapBiome;
import dev.tocraft.crafted.ctgen.data.MapImageRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class MapSettings {
    static final MapSettings DEFAULT = new MapSettings(null, new ArrayList<>(), null, 0, 66, -32, 279, 64, 31, 250, 3, Optional.empty(), Optional.empty(), List.of(CarverSetting.DEFAULT));

    public static final Codec<MapSettings> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("biome_map").forGetter(o -> o.biomeMapId),
            Codec.list(MapBiome.CODEC).optionalFieldOf("map_biomes", DEFAULT.biomeData).forGetter(o -> o.biomeData),
            MapBiome.CODEC.fieldOf("default_map_biome").forGetter(o -> o.defaultBiome),
            Codec.INT.optionalFieldOf("deepslate_level", DEFAULT.deepslateLevel).forGetter(o -> o.deepslateLevel),
            Codec.INT.optionalFieldOf("surface_level", DEFAULT.surfaceLevel).forGetter(o -> o.surfaceLevel),
            Codec.INT.optionalFieldOf("min_y", DEFAULT.minY).forGetter(o -> o.minY),
            Codec.INT.optionalFieldOf("gen_height", DEFAULT.genHeight).forGetter(o -> o.genHeight),
            Codec.INT.optionalFieldOf("sea_level", DEFAULT.seaLevel).forGetter(o -> o.seaLevel),
            Codec.INT.optionalFieldOf("transition", DEFAULT.transition).forGetter(o -> o.transition),
            Codec.INT.optionalFieldOf("noise_stretch", DEFAULT.noiseStretch).forGetter(o -> o.noiseStretch),
            Codec.INT.optionalFieldOf("noise_detail", DEFAULT.noiseDetail).forGetter(o -> o.noiseDetail),
            Codec.INT.optionalFieldOf("spawn_pixel_x").forGetter(o -> o.spawnX),
            Codec.INT.optionalFieldOf("spawn_pixel_y").forGetter(o -> o.spawnY),
            Codec.list(CarverSetting.CODEC).optionalFieldOf("cave_carver", DEFAULT.carverSettings).forGetter(o -> o.carverSettings)
    ).apply(instance, instance.stable(MapSettings::new)));

    private final ResourceLocation biomeMapId;
    final List<Holder<MapBiome>> biomeData;
    private final Holder<MapBiome> defaultBiome;
    final int deepslateLevel;
    final int surfaceLevel;
    final int minY;
    final int genHeight;
    final int seaLevel;
    final int transition;
    final int noiseStretch;
    final int noiseDetail;
    private final Supplier<BufferedImage> biomeMap;
    final Optional<Integer> spawnX;
    final Optional<Integer> spawnY;
    final List<CarverSetting> carverSettings;

    public MapSettings(ResourceLocation biomeMapId, List<Holder<MapBiome>> biomeData, Holder<MapBiome> defaultBiome, int deepslateLevel, int surfaceLevel, int minY, int genHeight, int seaLevel, int transition, int noiseStretch, int noiseDetail, Optional<Integer> spawnX, Optional<Integer> spawnY, List<CarverSetting> carverSettings) {
        this.biomeMapId = biomeMapId;
        this.biomeData = biomeData;
        this.defaultBiome = defaultBiome;
        this.deepslateLevel = deepslateLevel;
        this.surfaceLevel = surfaceLevel;
        this.minY = minY;
        this.genHeight = genHeight;
        this.seaLevel = seaLevel;
        this.transition = transition;
        this.noiseStretch = noiseStretch;
        this.noiseDetail = noiseDetail;
        this.biomeMap = () -> MapImageRegistry.getById(biomeMapId);
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.carverSettings = carverSettings;
    }

    /**
     * Gets the Color from a position on the map
     *
     * @param pX this should be a section position x
     * @param pY this should be a section position z
     * @return Returns the biome color or the default biome if none was found
     */
    @NotNull
    public Holder<MapBiome> getMapBiome(int pX, int pY) {
        int x = xOffset(pX);
        int y = yOffset(pY);

        // check if coordinate is inbound
        if (isPixelInBiomeMap(x, y)) {
            Holder<MapBiome> biome = getByColor(biomeMap.get().getRGB(x, y));
            return biome != null ? biome : defaultBiome;
        } else {
            //fallback
            return defaultBiome;
        }
    }

    /**
     * @param noise the SimplexNoise to be used as perlin
     * @param pX    this should be a block position x
     * @param pY    this should be a block position z
     * @return the relative height
     */
    public double getHeight(SimplexNoise noise, int pX, int pY) {
        double perlin = getPerlin(noise, pX, pY) * getValueWithTransition(pX, pY, MapBiome::perlinMultiplier);
        double genHeight = getValueWithTransition(pX, pY, mapBiome -> (double) mapBiome.height());
        return genHeight + perlin;
    }

    public double getPerlin(SimplexNoise noise, int x, int z) {
        double perlin = 0;
        for (int i = 0; i < noiseDetail; i++) {
            perlin += Math.pow(0.5, i) * noise.getValue(x * Math.pow(2, i) / noiseStretch, z * Math.pow(2, i) / noiseStretch);
        }
        perlin = perlin / (1 - Math.pow(0.5, noiseDetail));
        return perlin;
    }

    private double getValueWithTransition(int x, int y, Function<MapBiome, Double> function) {
        // Determine the base coordinates for the current grid
        int baseX = (x / transition) * transition;
        int baseY = (y / transition) * transition;

        // Adjust base coordinates for negative values
        if (x < 0) baseX -= transition;
        if (y < 0) baseY -= transition;

        MapBiome biome00 = getMapBiome(baseX >> 2, baseY >> 2).value(); // Top-left
        MapBiome biome10 = getMapBiome((baseX + transition) >> 2, baseY >> 2).value(); // Top-right
        MapBiome biome01 = getMapBiome(baseX >> 2, (baseY + transition) >> 2).value(); // Bottom-left
        MapBiome biome11 = getMapBiome((baseX + transition) >> 2, (baseY + transition) >> 2).value(); // Bottom-right

        double h00 = function.apply(biome00);
        double h10 = function.apply(biome10);
        double h01 = function.apply(biome01);
        double h11 = function.apply(biome11);

        // Calculate the fractional positions within the grid, relative to base coordinates
        double xPercent = (double) (x - baseX) / transition;
        double yPercent = (double) (y - baseY) / transition;

        // Ensure fractional values are within [0, 1] (handling negative values)
        xPercent = Math.abs(xPercent);
        yPercent = Math.abs(yPercent);

        // Introduce cubic-like transitions based on weight differences
        xPercent = smoothStep(xPercent);
        yPercent = smoothStep(yPercent);

        // Calculate bi-linear interpolation
        return (h00 * (1 - xPercent) * (1 - yPercent)) +
                (h10 * xPercent * (1 - yPercent)) +
                (h01 * (1 - xPercent) * yPercent) +
                (h11 * xPercent * yPercent);
    }

    private double smoothStep(double t) {
        return t * t * (3 - 2 * t);
    }

    private boolean isPixelInBiomeMap(int x, int y) {
        return x >= 0 && y >= 0 && x < biomeMap.get().getWidth() && y < biomeMap.get().getHeight();
    }

    // used to move the map in order to spawn in the center
    public int xOffset(int x) {
        return x + spawnX.orElseGet(() -> biomeMap.get().getWidth() / 2);
    }

    public int yOffset(int y) {
        return y + spawnY.orElseGet(() -> biomeMap.get().getHeight() / 2);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MapSettings) obj;
        return Objects.equals(this.biomeMapId, that.biomeMapId) &&
                Objects.equals(this.biomeData, that.biomeData) &&
                Objects.equals(this.defaultBiome, that.defaultBiome) &&
                this.deepslateLevel == that.deepslateLevel &&
                this.surfaceLevel == that.surfaceLevel &&
                this.minY == that.minY &&
                this.genHeight == that.genHeight &&
                this.seaLevel == that.seaLevel &&
                this.transition == that.transition &&
                this.noiseStretch == that.noiseStretch &&
                this.noiseDetail == that.noiseDetail;
    }

    @Nullable
    private Holder<MapBiome> getByColor(int color) {
        return biomeData.stream().filter(biome -> biome.value().color() == color).findAny().orElse(null);
    }
}
