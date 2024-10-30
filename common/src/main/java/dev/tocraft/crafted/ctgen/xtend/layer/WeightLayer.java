package dev.tocraft.crafted.ctgen.xtend.layer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.xtend.placer.BlockPlacer;
import dev.tocraft.crafted.ctgen.zone.Zone;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class WeightLayer extends BlockLayer {
    private final double min;
    private final double max;
    private final boolean hasShift;

    public WeightLayer(double minPercentage, double maxPercentage, String name, BlockPlacer fallback) {
        this(minPercentage, maxPercentage, true, name, fallback);
    }

    public WeightLayer(double minPercentage, double maxPercentage, boolean hasShift, String name, BlockPlacer fallback) {
        this(minPercentage, maxPercentage, hasShift, name, true, fallback);
    }

    public WeightLayer(double minPercentage, double maxPercentage, boolean hasShift, String name, boolean hasCaves, BlockPlacer fallback) {
        super(name, hasCaves, fallback);
        this.min = minPercentage;
        this.max = maxPercentage;
        this.hasShift = hasShift;
    }

    public double getMaxPercentage() {
        return max;
    }

    public double getMinPercentage() {
        return min;
    }

    public boolean hasShift() {
        return hasShift;
    }

    @Override
    public boolean is(SimplexNoise noise, int x, int y, int z, Zone zone, int minY, int seaLevel, double surfaceHeight, int genHeight, int shift) {
        int y2 = hasShift ? y + shift : y;
        double percentage = (y2 - minY) / (surfaceHeight - minY);
        return this.min <= percentage && percentage <= this.max;
    }

    public static final Codec<WeightLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("min_percentage").forGetter(WeightLayer::getMinPercentage),
            Codec.DOUBLE.fieldOf("max_percentage").forGetter(WeightLayer::getMaxPercentage),
            Codec.BOOL.optionalFieldOf("shift", true).forGetter(WeightLayer::hasShift),
            Codec.STRING.fieldOf("name").forGetter(BlockLayer::getName),
            Codec.BOOL.optionalFieldOf("has_caves", true).forGetter(BlockLayer::hasCaves),
            BlockPlacer.CODEC.fieldOf("fallback").forGetter(BlockLayer::getFallback)
    ).apply(instance, instance.stable(WeightLayer::new)));

    public static final ResourceLocation ID = CTerrainGeneration.id("weight");

    @Override
    protected Codec<WeightLayer> codec() {
        return CODEC;
    }
}
