package dev.tocraft.ctgen.worldgen;

import dev.tocraft.ctgen.zone.Zone;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class MapSettingsBuilder {
    private ResourceLocation mapId;
    private List<Holder<Zone>> zones;
    private Holder<Zone> defaultBiome;
    private int surfaceLevel;
    private int transition = MapSettings.DEFAULT.transition;
    private @NotNull Optional<Integer> spawnX = Optional.empty();
    private @NotNull Optional<Integer> spawnY = Optional.empty();
    private Holder<NoiseGeneratorSettings> noiseGenSettings;

    public MapSettingsBuilder setMapId(ResourceLocation mapId) {
        this.mapId = mapId;
        return this;
    }

    public MapSettingsBuilder setZones(List<Holder<Zone>> zones) {
        this.zones = zones;
        return this;
    }

    public MapSettingsBuilder setDefaultBiome(Holder<Zone> defaultBiome) {
        this.defaultBiome = defaultBiome;
        return this;
    }

    public MapSettingsBuilder setSurfaceLevel(int surfaceLevel) {
        this.surfaceLevel = surfaceLevel;
        return this;
    }

    public MapSettingsBuilder setTransition(int transition) {
        this.transition = transition;
        return this;
    }

    public MapSettingsBuilder setSpawnX(int spawnX) {
        this.spawnX = Optional.of(spawnX);
        return this;
    }

    public MapSettingsBuilder setSpawnY(int spawnY) {
        this.spawnY = Optional.of(spawnY);
        return this;
    }

    public MapSettingsBuilder setSpawnX(@NotNull Optional<Integer> spawnX) {
        this.spawnX = spawnX;
        return this;
    }

    public MapSettingsBuilder setSpawnY(@NotNull Optional<Integer> spawnY) {
        this.spawnY = spawnY;
        return this;
    }

    public MapSettingsBuilder setNoiseGenSettings(Holder<NoiseGeneratorSettings> noiseGenSettings) {
        this.noiseGenSettings = noiseGenSettings;
        return this;
    }

    public MapSettings createMapSettings() {
        return new MapSettings(mapId, zones, defaultBiome, surfaceLevel, transition, spawnX, spawnY, noiseGenSettings);
    }
}