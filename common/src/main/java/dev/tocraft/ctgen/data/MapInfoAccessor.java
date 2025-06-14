package dev.tocraft.ctgen.data;

import dev.tocraft.ctgen.worldgen.MapSettings;

import java.util.function.Supplier;

public interface MapInfoAccessor {
    void ctgen$setSettings(Supplier<MapSettings> settings);
    MapSettings ctgen$getSettings();
}
