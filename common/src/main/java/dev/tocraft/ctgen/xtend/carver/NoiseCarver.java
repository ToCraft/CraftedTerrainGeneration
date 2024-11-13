package dev.tocraft.ctgen.xtend.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.util.Noise;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NoiseCarver extends Carver {
    public static final NoiseCarver DEFAULT = new NoiseCarver(new Noise(List.of(new Noise.Octave(1, 1), new Noise.Octave(0.5f, 2)), 63, 47), 0.55F);

    public static final MapCodec<NoiseCarver> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Noise.CODEC.optionalFieldOf("noise", DEFAULT.noise).forGetter(o -> o.noise),
            Codec.DOUBLE.optionalFieldOf("threshold", DEFAULT.threshold).forGetter(o -> o.threshold)
    ).apply(instance, NoiseCarver::new));
    public static final ResourceLocation ID = CTerrainGeneration.id("noise_carver");

    private final Noise noise;
    private final double threshold;

    public NoiseCarver(Noise noise, double threshold) {
        this.noise = noise;
        this.threshold = threshold;
    }


    @Override
    public boolean canSetBlock(SimplexNoise noise, @NotNull BlockPos pos, double surfaceHeight, int minHeight, double carverModifier) {
        double height = (double) (pos.getY() - minHeight) / (surfaceHeight - minHeight) - 0.5;
        double addThreshold = height * height * height * height * carverModifier;

        double perlin = this.noise.getPerlin(noise, pos.getX(), pos.getY(), pos.getZ());

        double threshold = this.threshold + addThreshold;
        return perlin <= threshold;
    }

    @Override
    protected MapCodec<NoiseCarver> codec() {
        return CODEC;
    }
}
