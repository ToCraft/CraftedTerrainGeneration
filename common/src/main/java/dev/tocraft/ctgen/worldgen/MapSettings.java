package dev.tocraft.ctgen.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.data.BiomeImageRegistry;
import dev.tocraft.ctgen.data.HeightImageRegistry;
import dev.tocraft.ctgen.util.Noise;
import dev.tocraft.ctgen.zone.Zone;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class MapSettings {
    public static final Codec<MapSettings> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("map_id").forGetter(o -> o.mapId),
            Codec.list(Zone.CODEC).fieldOf("zones").forGetter(o -> o.zones),
            Zone.CODEC.fieldOf("default_map_biome").forGetter(o -> o.defaultBiome),
            Codec.INT.optionalFieldOf("transition", 16).forGetter(o -> o.transition),
            Codec.INT.optionalFieldOf("spawn_pixel_x").forGetter(o -> o.spawnX),
            Codec.INT.optionalFieldOf("spawn_pixel_y").forGetter(o -> o.spawnY),
            NoiseGeneratorSettings.CODEC.fieldOf("noise_gen_settings").forGetter(o -> o.noiseGenSettings)
    ).apply(instance, instance.stable(MapSettings::new)));

    private final ResourceLocation mapId;
    final List<Holder<Zone>> zones;
    private final Holder<Zone> defaultBiome;
    final int transition;
    private final Supplier<BufferedImage> mapImage;
    private final Supplier<BufferedImage> heightmap;
    final Optional<Integer> spawnX;
    final Optional<Integer> spawnY;
    public final Holder<NoiseGeneratorSettings> noiseGenSettings;


    @ApiStatus.Internal
    public MapSettings(ResourceLocation mapId, List<Holder<Zone>> zones, Holder<Zone> defaultBiome, int transition, @NotNull Optional<Integer> spawnX, @NotNull Optional<Integer> spawnY, Holder<NoiseGeneratorSettings> noiseGenSettings) {
        this.mapId = mapId;
        this.zones = zones;
        this.defaultBiome = defaultBiome;
        this.transition = transition;
        this.mapImage = () -> BiomeImageRegistry.getById(mapId);
        this.heightmap = () -> HeightImageRegistry.getById(mapId);
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.noiseGenSettings = noiseGenSettings;
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
        double genHeight = getTransitionedHeight(pX, pY);
        double addHeight = Noise.DEFAULT.getPerlin(noise, pX, pY) * getTransitionedModifier(pX, pY);
        return genHeight + addHeight;
    }

    public double getTransitionedModifier(int x, int y) {
        // Determine the base coordinates for the current grid
        int baseX = (x / transition) * transition;
        int baseY = (y / transition) * transition;

        // Adjust base coordinates for negative values
        if (x < 0) baseX -= transition;
        if (y < 0) baseY -= transition;

        double th00 = getZone(baseX >> 2, baseY >> 2).value().terrainModifier(); // Top-left
        double th10 = getZone((baseX + transition) >> 2, baseY >> 2).value().terrainModifier(); // Top-right
        double th01 = getZone(baseX >> 2, (baseY + transition) >> 2).value().terrainModifier(); // Bottom-left
        double th11 = getZone((baseX + transition) >> 2, (baseY + transition) >> 2).value().terrainModifier(); // Bottom-right

        // Calculate the fractional positions within the grid, relative to base coordinates
        double xPercent = Math.abs((double) (x - baseX) / transition);
        double yPercent = Math.abs((double) (y - baseY) / transition);

        // Calculate bi-linear interpolation
        return (th00 * (1 - xPercent) * (1 - yPercent)) +
                (th10 * xPercent * (1 - yPercent)) +
                (th01 * (1 - xPercent) * yPercent) +
                (th11 * xPercent * yPercent);
    }

    public double getTransitionedHeight(int x, int y) {
        // Determine the base coordinates for the current grid
        int baseX = (x / transition) * transition;
        int baseY = (y / transition) * transition;

        // Adjust base coordinates for negative values
        if (x < 0) baseX -= transition;
        if (y < 0) baseY -= transition;

        int h00 = getRedHeight(baseX >> 2, baseY >> 2); // Top-left
        int h10 = getRedHeight(baseX + transition >> 2, baseY >> 2); // Top-right
        int h01 = getRedHeight(baseX >> 2, baseY + transition >> 2); // Bottom-left
        int h11 = getRedHeight(baseX + transition >> 2, baseY + transition >> 2); // Bottom-right

        // Calculate the fractional positions within the grid, relative to base coordinates
        double xPercent = Math.abs((double) (x - baseX) / transition);
        double yPercent = Math.abs((double) (y - baseY) / transition);

        // Introduce cubic-like transitions based on weight differences
        xPercent = smoothStep(xPercent);
        yPercent = smoothStep(yPercent);

        // Calculate bi-linear interpolation
        return (h00 * (1 - xPercent) * (1 - yPercent)) +
                (h10 * xPercent * (1 - yPercent)) +
                (h01 * (1 - xPercent) * yPercent) +
                (h11 * xPercent * yPercent);
    }

    public int getRedHeight(int pX, int pY) {
        int x = xOffset(pX);
        int y = yOffset(pY);

        if (isPixelInHeightmap(x, y)) {
            return heightmap.get().getRGB(x, y) >> 16 & 0xFF; // heightmap is grey - r g b should be the same
        }
        return heightmap.get().getRGB(0, 0) >> 16 & 0xFF; // the top-left pixel is most likely ocean -> use it's high
    }


    private double smoothStep(double t) {
        return t * t * (3 - 2 * t);
    }

    private boolean isPixelInHeightmap(int x, int y) {
        return x >= 0 && y >= 0 && x < heightmap.get().getWidth() && y < heightmap.get().getHeight();
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
