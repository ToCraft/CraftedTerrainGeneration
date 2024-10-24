package dev.tocraft.crafted.ctgen.layer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.blockplacer.BlockPlacer;
import dev.tocraft.crafted.ctgen.zone.Zone;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class SurfaceLayer extends BlockLayer {
    public SurfaceLayer(String name, BlockPlacer fallback) {
        this(name, true, fallback);
    }

    public SurfaceLayer(String name, boolean hasCaves, BlockPlacer fallback) {
        super(name, hasCaves, fallback);
    }

    @Override
    public boolean is(SimplexNoise noise, int x, int y, int z, Zone zone, int minY, int seaLevel, double surfaceHeight, int genHeight, int shift) {
        return y == (int) surfaceHeight;
    }

    public static final Codec<SurfaceLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", "surface").forGetter(BlockLayer::getName),
            Codec.BOOL.optionalFieldOf("has_caves", true).forGetter(BlockLayer::hasCaves),
            BlockPlacer.CODEC.fieldOf("fallback").forGetter(BlockLayer::getFallback)
    ).apply(instance, instance.stable(SurfaceLayer::new)));

    public static final ResourceLocation ID = CTerrainGeneration.id("sea");

    @Override
    protected Codec<SurfaceLayer> codec() {
        return CODEC;
    }
}
