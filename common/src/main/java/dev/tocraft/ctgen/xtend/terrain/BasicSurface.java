package dev.tocraft.ctgen.xtend.terrain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.util.Noise;
import dev.tocraft.ctgen.worldgen.MapSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class BasicSurface extends TerrainHeight {
    public static final BasicSurface DEFAULT = new BasicSurface(Noise.DEFAULT);

    public static final Codec<BasicSurface> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Noise.CODEC.optionalFieldOf("noise", Noise.DEFAULT).forGetter(o -> o.noise)
    ).apply(instance, BasicSurface::new));
    public static final ResourceLocation ID = CTerrainGeneration.id("basic_surface");

    private final Noise noise;

    public BasicSurface(Noise noise) {
        this.noise = noise;
    }


    @Override
    public double getHeight(MapSettings settings, SimplexNoise noise, int x, int z, double terrainModifier) {
        return this.noise.getPerlin(noise, x, z) * terrainModifier;
    }

    @Override
    protected Codec<BasicSurface> codec() {
        return CODEC;
    }
}
