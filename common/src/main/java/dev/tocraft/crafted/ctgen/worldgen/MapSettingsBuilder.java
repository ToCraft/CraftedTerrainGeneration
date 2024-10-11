package dev.tocraft.crafted.ctgen.worldgen;

import dev.tocraft.crafted.ctgen.zone.CarverSetting;
import dev.tocraft.crafted.ctgen.zone.Zone;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
public class MapSettingsBuilder {
    private ResourceLocation biomeMapId;
    private boolean pixelsAreChunks = MapSettings.DEFAULT.pixelsAreChunks;
    private int thresholdModifier = MapSettings.DEFAULT.thresholdModifier;
    private List<Holder<Zone>> zones = MapSettings.DEFAULT.zones;
    private Holder<Zone> defaultBiome;
    private int deepslateLevel = MapSettings.DEFAULT.deepslateLevel;
    private int surfaceLevel = MapSettings.DEFAULT.surfaceLevel;
    private int minY = MapSettings.DEFAULT.minY;
    private int genHeight = MapSettings.DEFAULT.genHeight;
    private int seaLevel = MapSettings.DEFAULT.seaLevel;
    private int transition = MapSettings.DEFAULT.transition;
    private int noiseStretch = MapSettings.DEFAULT.noiseStretch;
    private int noiseDetail = MapSettings.DEFAULT.noiseDetail;
    private Optional<Integer> spawnX = MapSettings.DEFAULT.spawnX;
    private Optional<Integer> spawnY = MapSettings.DEFAULT.spawnY;
    private List<CarverSetting> carverSettings = MapSettings.DEFAULT.carverSettings;

    public MapSettingsBuilder setBiomeMapId(ResourceLocation biomeMapId) {
        this.biomeMapId = biomeMapId;
        return this;
    }

    public MapSettingsBuilder setPixelsAreChunks(boolean pixelsAreChunks) {
        this.pixelsAreChunks = pixelsAreChunks;
        return this;
    }

    public MapSettingsBuilder setThresholdModifier(int thresholdModifier) {
        this.thresholdModifier = thresholdModifier;
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

    public MapSettingsBuilder setDeepslateLevel(int deepslateLevel) {
        this.deepslateLevel = deepslateLevel;
        return this;
    }

    public MapSettingsBuilder setSurfaceLevel(int surfaceLevel) {
        this.surfaceLevel = surfaceLevel;
        return this;
    }

    public MapSettingsBuilder setMinY(int minY) {
        this.minY = minY;
        return this;
    }

    public MapSettingsBuilder setGenHeight(int genHeight) {
        this.genHeight = genHeight;
        return this;
    }

    public MapSettingsBuilder setSeaLevel(int seaLevel) {
        this.seaLevel = seaLevel;
        return this;
    }

    public MapSettingsBuilder setTransition(int transition) {
        this.transition = transition;
        return this;
    }

    public MapSettingsBuilder setNoiseStretch(int noiseStretch) {
        this.noiseStretch = noiseStretch;
        return this;
    }

    public MapSettingsBuilder setNoiseDetail(int noiseDetail) {
        this.noiseDetail = noiseDetail;
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

    public MapSettingsBuilder setCarverSettings(List<CarverSetting> carverSetting) {
        this.carverSettings = carverSetting;
        return this;
    }

    public MapSettings build() {
        return new MapSettings(biomeMapId, pixelsAreChunks, thresholdModifier, zones, defaultBiome, deepslateLevel, surfaceLevel, minY, genHeight, seaLevel, transition, noiseStretch, noiseDetail, spawnX, spawnY, carverSettings);
    }
}