package dev.tocraft.crafted.ctgen.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.data.MapImageRegistry;
import dev.tocraft.crafted.ctgen.util.Noise;
import dev.tocraft.crafted.ctgen.zone.CarverSetting;
import dev.tocraft.crafted.ctgen.zone.Zone;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class MapSettings {
    static final MapSettings DEFAULT = new MapSettings(null, true, 6, new ArrayList<>(), null, 0, 66, -64, 279, 64, 31, Noise.DEFAULT, Optional.empty(), Optional.empty(), List.of(CarverSetting.DEFAULT));

    public static final Codec<MapSettings> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("biome_map").forGetter(o -> o.mapId),
            Codec.BOOL.optionalFieldOf("pixels_are_chunks", DEFAULT.pixelsAreChunks).forGetter(o -> o.pixelsAreChunks),
            Codec.INT.optionalFieldOf("default_threshold_modifier", DEFAULT.thresholdModifier).forGetter(o -> o.thresholdModifier),
            Codec.list(Zone.CODEC).optionalFieldOf("zones", DEFAULT.zones).forGetter(o -> o.zones),
            Zone.CODEC.fieldOf("default_map_biome").forGetter(o -> o.defaultBiome),
            Codec.INT.optionalFieldOf("deepslate_level", DEFAULT.deepslateLevel).forGetter(o -> o.deepslateLevel),
            Codec.INT.optionalFieldOf("surface_level", DEFAULT.surfaceLevel).forGetter(o -> o.surfaceLevel),
            Codec.INT.optionalFieldOf("min_y", DEFAULT.minY).forGetter(o -> o.minY),
            Codec.INT.optionalFieldOf("gen_height", DEFAULT.genHeight).forGetter(o -> o.genHeight),
            Codec.INT.optionalFieldOf("sea_level", DEFAULT.seaLevel).forGetter(o -> o.seaLevel),
            Codec.INT.optionalFieldOf("transition", DEFAULT.transition).forGetter(o -> o.transition),
            Noise.CODEC.optionalFieldOf("noise", DEFAULT.noise).forGetter(o -> o.noise),
            Codec.INT.optionalFieldOf("spawn_pixel_x").forGetter(o -> o.spawnX),
            Codec.INT.optionalFieldOf("spawn_pixel_y").forGetter(o -> o.spawnY),
            Codec.list(CarverSetting.CODEC).optionalFieldOf("cave_carver", DEFAULT.carverSettings).forGetter(o -> o.carverSettings)
    ).apply(instance, instance.stable(MapSettings::new)));

    private final ResourceLocation mapId;
    final boolean pixelsAreChunks;
    final int thresholdModifier;
    final List<Holder<Zone>> zones;
    private final Holder<Zone> defaultBiome;
    final int deepslateLevel;
    final int surfaceLevel;
    final int minY;
    final int genHeight;
    final int seaLevel;
    final int transition;
    final Noise noise;
    private final Supplier<BufferedImage> mapImage;
    final Optional<Integer> spawnX;
    final Optional<Integer> spawnY;
    final List<CarverSetting> carverSettings;

    @ApiStatus.Internal
    public MapSettings(ResourceLocation mapId, boolean pixelsAreChunks, int thresholdModifier, List<Holder<Zone>> zones, Holder<Zone> defaultBiome, int deepslateLevel, int surfaceLevel, int minY, int genHeight, int seaLevel, int transition, Noise noise, Optional<Integer> spawnX, Optional<Integer> spawnY, List<CarverSetting> carverSettings) {
        this.mapId = mapId;
        this.pixelsAreChunks = pixelsAreChunks;
        this.thresholdModifier = thresholdModifier;
        this.zones = zones;
        this.defaultBiome = defaultBiome;
        this.deepslateLevel = deepslateLevel;
        this.surfaceLevel = surfaceLevel;
        this.minY = minY;
        this.genHeight = genHeight;
        this.seaLevel = seaLevel;
        this.transition = transition;
        this.noise = noise;
        this.mapImage = () -> MapImageRegistry.getByIdOrUpscale(mapId, pixelsAreChunks, () -> zones.stream().map(Holder::value).toList());
        this.spawnX = spawnX.map(sX -> {
            if (pixelsAreChunks) {
                return sX >> 2;
            } else {
                return sX;
            }
        });
        this.spawnY = spawnY.map(sX -> {
            if (pixelsAreChunks) {
                return sX >> 2;
            } else {
                return sX;
            }
        });
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
    public Holder<Zone> getZone(int pX, int pY) {
        int x = xOffset(pX);
        int y = yOffset(pY);

        // check if coordinate is inbound
        if (isPixelInBiomeMap(x, y)) {
            Holder<Zone> biome = getByColor(mapImage.get().getRGB(x, y));
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
        double perlin = getPerlin(noise, pX, pY) * getValueWithTransition(pX, pY, Zone::perlinMultiplier);
        double genHeight = getValueWithTransition(pX, pY, zone -> (double) zone.height());
        return genHeight + perlin;
    }

    public double getPerlin(SimplexNoise noise, int x, int z) {
        return this.noise.getPerlin(noise, x, z);
    }

    double getValueWithTransition(int x, int y, Function<Zone, Double> function) {
        // Determine the base coordinates for the current grid
        int baseX = (x / transition) * transition;
        int baseY = (y / transition) * transition;

        // Adjust base coordinates for negative values
        if (x < 0) baseX -= transition;
        if (y < 0) baseY -= transition;

        Zone biome00 = getZone(baseX >> 2, baseY >> 2).value(); // Top-left
        Zone biome10 = getZone((baseX + transition) >> 2, baseY >> 2).value(); // Top-right
        Zone biome01 = getZone(baseX >> 2, (baseY + transition) >> 2).value(); // Bottom-left
        Zone biome11 = getZone((baseX + transition) >> 2, (baseY + transition) >> 2).value(); // Bottom-right

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
        return x >= 0 && y >= 0 && x < mapImage.get().getWidth() && y < mapImage.get().getHeight();
    }

    // used to move the map in order to spawn in the center
    public int xOffset(int x) {
        return x + spawnX.orElseGet(() -> mapImage.get().getWidth() / 2);
    }

    public int yOffset(int y) {
        return y + spawnY.orElseGet(() -> mapImage.get().getHeight() / 2);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MapSettings) obj;
        return Objects.equals(this.mapId, that.mapId) &&
                Objects.equals(this.zones, that.zones) &&
                Objects.equals(this.defaultBiome, that.defaultBiome) &&
                this.deepslateLevel == that.deepslateLevel &&
                this.surfaceLevel == that.surfaceLevel &&
                this.minY == that.minY &&
                this.genHeight == that.genHeight &&
                this.seaLevel == that.seaLevel &&
                this.transition == that.transition;
    }

    @Nullable
    private Holder<Zone> getByColor(int color) {
        return zones.stream().filter(biome -> biome.value().color() == color).findAny().orElse(null);
    }

    @ApiStatus.Internal
    public BufferedImage getMapImage() {
        return mapImage.get();
    }

    public ResourceLocation getMapId() {
        return mapId;
    }

    @ApiStatus.Internal
    public int getMapWidth() {
        return mapImage.get().getWidth();
    }

    @ApiStatus.Internal
    public int getMapHeight() {
        return mapImage.get().getHeight();
    }
}
