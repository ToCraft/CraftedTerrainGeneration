package dev.tocraft.ctgen.xtend.height;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.util.Noise;
import dev.tocraft.ctgen.worldgen.MapSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class NoiseHeight extends TerrainHeight {
    public static final NoiseHeight DEFAULT = new NoiseHeight(Noise.DEFAULT);

    public static final Codec<NoiseHeight> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Noise.CODEC.optionalFieldOf("noise", Noise.DEFAULT).forGetter(o -> o.noise)
    ).apply(instance, NoiseHeight::new));
    public static final ResourceLocation ID = CTerrainGeneration.id("noise_height");

    private final Noise noise;

    public NoiseHeight(Noise noise) {
        this.noise = noise;
    }


    @Override
    public double getHeight(MapSettings settings, SimplexNoise noise, int x, int z, double terrainModifier) {
        return this.noise.getPerlin(noise, x, z) * terrainModifier;
    }

    @Override
    protected Codec<NoiseHeight> codec() {
        return CODEC;
    }
}
