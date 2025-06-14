package dev.tocraft.ctgen.mixin;

import dev.tocraft.ctgen.data.MapInfoAccessor;
import dev.tocraft.ctgen.worldgen.MapSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;

@Mixin(SurfaceRules.Context.class)
public class SurfaceRulesContextMixin implements MapInfoAccessor {
    @Unique
    private Supplier<MapSettings> ctgen$settings = () -> null;
    @Unique
    private Supplier<SimplexNoise> ctgen$noise = () -> null;

    @Unique
    @Override
    public void ctgen$setSettings(Supplier<MapSettings> settings) {
        this.ctgen$settings = settings;
    }

    @Unique
    @Override
    public MapSettings ctgen$getSettings() {
        return this.ctgen$settings.get();
    }

    @Unique
    @Override
    public void ctgen$setNoise(Supplier<SimplexNoise> noise) {
        this.ctgen$noise = noise;
    }

    @Unique
    @Override
    public SimplexNoise ctgen$getNoise() {
        return this.ctgen$noise.get();
    }
}
