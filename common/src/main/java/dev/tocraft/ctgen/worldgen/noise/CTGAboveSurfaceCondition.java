package dev.tocraft.ctgen.worldgen.noise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.data.MapInfoAccessor;
import dev.tocraft.ctgen.worldgen.MapSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public record CTGAboveSurfaceCondition(int depth) implements SurfaceRules.ConditionSource {
    public static final KeyDispatchDataCodec<CTGAboveSurfaceCondition> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Codec.INT.optionalFieldOf("depth", 5).forGetter(CTGAboveSurfaceCondition::depth))
                    .apply(instance, CTGAboveSurfaceCondition::new)));

    @Override
    public @NotNull KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.Condition apply(final SurfaceRules.Context surfaceRuleContext) {
        MapSettings settings = ((MapInfoAccessor) (Object) surfaceRuleContext).ctgen$getSettings();
        SimplexNoise noise = ((MapInfoAccessor) (Object) surfaceRuleContext).ctgen$getNoise();
        double elevation;
        if (settings != null && noise != null) {
            elevation = settings.getHeight(noise, surfaceRuleContext.blockX >> 2, surfaceRuleContext.blockZ);
        } else {
            elevation = -1;
        }
        return new SurfaceRules.LazyYCondition(surfaceRuleContext) {
            @Override
            protected boolean compute() {
                return elevation == -1 || this.context.blockY > elevation - CTGAboveSurfaceCondition.this.depth;
            }
        };
    }

    public static void register(@NotNull BiConsumer<ResourceLocation, MapCodec<? extends SurfaceRules.ConditionSource>> registerFunc) {
        registerFunc.accept(CTerrainGeneration.id("above_preliminary_surface"), CTGAboveSurfaceCondition.CODEC.codec());
    }
}