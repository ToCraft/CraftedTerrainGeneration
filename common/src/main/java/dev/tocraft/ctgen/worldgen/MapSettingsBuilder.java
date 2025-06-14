package dev.tocraft.ctgen.worldgen;

import dev.tocraft.ctgen.zone.Zone;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
public class MapSettingsBuilder {
    private ResourceLocation biomeMapId;
    private List<Holder<Zone>> zones = MapSettings.DEFAULT.zones;
    private Holder<Zone> defaultBiome;
    private int surfaceLevel = MapSettings.DEFAULT.surfaceLevel;
    private Optional<Integer> spawnX = MapSettings.DEFAULT.spawnX;
    private Optional<Integer> spawnY = MapSettings.DEFAULT.spawnY;

    public MapSettingsBuilder setBiomeMapId(ResourceLocation biomeMapId) {
        this.biomeMapId = biomeMapId;
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

    public MapSettingsBuilder setSpawnX(int spawnX) {
        this.spawnX = Optional.of(spawnX);
        return this;
    }

    public MapSettingsBuilder setSpawnY(int spawnY) {
        this.spawnY = Optional.of(spawnY);
        return this;
    }

    public MapSettings build() {
        return new MapSettings(biomeMapId, zones, defaultBiome, surfaceLevel, spawnX, spawnY, MapSettings.DEFAULT.noiseGenSettings);
    }
}