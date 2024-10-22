package dev.tocraft.crafted.ctgen.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.List;

public record Noise(List<Float> octaves, double persistence, int stretchXZ, int stretchY) {
    public static final Noise DEFAULT = new Noise(List.of(1f, 2f, 4f), 0.5f, 250);

    public Noise(List<Float> octaves, double persistence, int stretch) {
        this(octaves, persistence, stretch, -1);
    }

    public static final Codec<Noise> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.list(Codec.FLOAT).optionalFieldOf("octaves", DEFAULT.octaves).forGetter(Noise::octaves),
            Codec.DOUBLE.optionalFieldOf("persistence", DEFAULT.persistence).forGetter(Noise::persistence),
            Codec.INT.optionalFieldOf("stretch", DEFAULT.stretchXZ).forGetter(Noise::stretchXZ),
            Codec.INT.optionalFieldOf("stretch_y", DEFAULT.stretchY).forGetter(Noise::stretchY)
    ).apply(instance, instance.stable(Noise::new)));

    public double getPerlin(SimplexNoise noise, double x, double y, double z) {
        double x2 = x / stretchXZ;
        double y2 = y / stretchY;
        double z2 = z / stretchXZ;

        double total = 0;
        double amplitude = 1;
        double totalAmplitude = 0;
        for(float frequency : octaves) {
            total += noise.getValue(x2 * frequency, y2 * frequency, z2 * frequency) * amplitude;
            totalAmplitude += amplitude;
            amplitude *= persistence;
        }

        return total / totalAmplitude;
    }

    public double getPerlin(SimplexNoise noise, double x, double z) {
        double x2 = x / stretchXZ;
        double z2 = z / stretchXZ;

        double total = 0;
        double amplitude = 1;
        double totalAmplitude = 0;

        for(float frequency : octaves) {
            total += noise.getValue(x2 * frequency, z2 * frequency) * amplitude;
            totalAmplitude += amplitude;
            amplitude *= persistence;
        }

        total /= totalAmplitude;
        return total;
    }
}
