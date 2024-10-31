package dev.tocraft.ctgen.xtend.layer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.xtend.placer.BlockPlacer;
import dev.tocraft.ctgen.zone.Zone;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

@SuppressWarnings("unused")
public class HeightLayer extends BlockLayer {
    private final int min;
    private final int max;
    private final boolean limitedToSurface;
    private final boolean hasShift;

    public HeightLayer(int minY, int maxY, String name, BlockPlacer fallback) {
        this(minY, maxY, true, name, fallback);
    }

    public HeightLayer(int minY, int maxY, boolean hasShift, String name, BlockPlacer fallback) {
        this(minY, maxY, hasShift, name, true, fallback);
    }

    public HeightLayer(int minY, int maxY, boolean hasShift, String name, boolean hasCaves, BlockPlacer fallback) {
        this(minY, maxY, true, hasShift, name, hasCaves, fallback);
    }

    public HeightLayer(int minY, int maxY, boolean limitedToSurface, boolean hasShift, String name, boolean hasCaves, BlockPlacer fallback) {
        super(name, hasCaves, fallback);
        this.min = minY;
        this.max = maxY;
        this.hasShift = hasShift;
        this.limitedToSurface = limitedToSurface;
    }

    public int getMaxY() {
        return max;
    }

    public int getMinY() {
        return min;
    }

    public boolean hasShift() {
        return hasShift;
    }

    public boolean isLimitedToSurface() {
        return limitedToSurface;
    }

    @Override
    public boolean is(SimplexNoise noise, int x, int y, int z, Zone zone, int minY, int seaLevel, double surfaceHeight, int genHeight, int shift) {
        int y2 = hasShift ? y + shift : y;
        return y2 >= min - 1 && y2 <= max && (!limitedToSurface || y < surfaceHeight);
    }

    public static final Codec<HeightLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("min").forGetter(HeightLayer::getMinY),
            Codec.INT.fieldOf("max").forGetter(HeightLayer::getMaxY),
            Codec.BOOL.optionalFieldOf("limit_to_surface", true).forGetter(HeightLayer::isLimitedToSurface),
            Codec.BOOL.optionalFieldOf("shift", true).forGetter(HeightLayer::hasShift),
            Codec.STRING.fieldOf("name").forGetter(BlockLayer::getName),
            Codec.BOOL.optionalFieldOf("has_caves", true).forGetter(BlockLayer::hasCaves),
            BlockPlacer.CODEC.fieldOf("fallback").forGetter(BlockLayer::getFallback)
    ).apply(instance, instance.stable(HeightLayer::new)));

    public static final ResourceLocation ID = CTerrainGeneration.id("height_layer");

    @Override
    protected Codec<HeightLayer> codec() {
        return CODEC;
    }
}
