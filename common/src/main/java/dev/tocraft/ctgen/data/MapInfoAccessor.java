package dev.tocraft.ctgen.data;

import dev.tocraft.ctgen.worldgen.MapSettings;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;

public interface MapInfoAccessor {
    void ctgen$setSettings(Supplier<MapSettings> settings);

    MapSettings ctgen$getSettings();

    @Unique
    void ctgen$setNoise(Supplier<SimplexNoise> noise);

    @Unique
    SimplexNoise ctgen$getNoise();
}
