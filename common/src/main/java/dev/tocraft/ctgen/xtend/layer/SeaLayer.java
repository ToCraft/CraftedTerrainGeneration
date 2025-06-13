package dev.tocraft.ctgen.xtend.layer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.xtend.placer.BlockPlacer;
import dev.tocraft.ctgen.zone.Zone;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class SeaLayer extends BlockLayer {
    public SeaLayer(String name, BlockPlacer fallback) {
        super(name, fallback);
    }

    @Override
    public boolean is(SimplexNoise noise, int x, int y, int z, Zone zone, int minY, int seaLevel, double surfaceHeight, int genHeight, int shift) {
        return y > surfaceHeight && surfaceHeight < seaLevel;
    }

    public static final MapCodec<SeaLayer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(BlockLayer::getName),
            BlockPlacer.CODEC.fieldOf("fallback").forGetter(BlockLayer::getFallback)
    ).apply(instance, instance.stable(SeaLayer::new)));

    public static final ResourceLocation ID = CTerrainGeneration.id("sea_layer");

    @Override
    protected MapCodec<SeaLayer> codec() {
        return CODEC;
    }
}
