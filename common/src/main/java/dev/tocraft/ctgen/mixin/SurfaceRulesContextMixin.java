package dev.tocraft.ctgen.mixin;

import dev.tocraft.ctgen.data.MapInfoAccessor;
import dev.tocraft.ctgen.worldgen.MapSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;

@Mixin(SurfaceRules.Context.class)
public class SurfaceRulesContextMixin implements MapInfoAccessor {
    @Unique
    private Supplier<MapSettings> ctgen$settings;


    @Override
    public void ctgen$setSettings(Supplier<MapSettings> settings) {
        this.ctgen$settings = settings;
    }

    @Override
    public MapSettings ctgen$getSettings() {
        return this.ctgen$settings.get();
    }
}
