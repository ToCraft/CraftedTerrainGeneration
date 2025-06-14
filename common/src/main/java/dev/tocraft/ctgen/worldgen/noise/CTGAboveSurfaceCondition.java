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
    public SurfaceRules.Condition apply(final SurfaceRules.Context materialRuleContext) {
        class AboveSurfacePredicate
                extends SurfaceRules.LazyYCondition {
            AboveSurfacePredicate() {
                super(materialRuleContext);
            }

            @Override
            protected boolean compute() {
                MapSettings settings = ((MapInfoAccessor)(Object) materialRuleContext).ctgen$getSettings();
                // should only apply when carvers call this function, which is okay to always have grass
                if (settings == null) return true;
                double elevation = settings.getValueWithTransition(this.context.blockX, this.context.blockY, zone -> (double) zone.height());
                return this.context.blockY > elevation - CTGAboveSurfaceCondition.this.depth;
            }
        }
        return new AboveSurfacePredicate();
    }

    public static void register(@NotNull BiConsumer<ResourceLocation, MapCodec<? extends SurfaceRules.ConditionSource>> registerFunc) {
        registerFunc.accept(CTerrainGeneration.id("above_preliminary_surface"), CTGAboveSurfaceCondition.CODEC.codec());
    }
}