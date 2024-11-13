package dev.tocraft.ctgen.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.List;

public record Noise(List<Octave> octaves, int stretchXZ, int stretchY) {
    public record Octave(float frequency, float amplitude) {
        public static final Codec<Octave> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.FLOAT.fieldOf("frequency").forGetter(Octave::frequency),
                Codec.FLOAT.fieldOf("amplitude").forGetter(Octave::amplitude)
        ).apply(instance, instance.stable(Octave::new)));
    }

    public static final Noise DEFAULT = new Noise(List.of(new Octave(1, 1), new Octave(2, 0.5f), new Octave(4, 0.25f)), 250);

    public Noise(List<Octave> octaves, int stretch) {
        this(octaves, stretch, -1);
    }

    public static final Codec<Noise> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.list(Octave.CODEC).optionalFieldOf("octaves", DEFAULT.octaves).forGetter(Noise::octaves),
            Codec.INT.optionalFieldOf("stretch", DEFAULT.stretchXZ).forGetter(Noise::stretchXZ),
            Codec.INT.optionalFieldOf("stretch_y", DEFAULT.stretchY).forGetter(Noise::stretchY)
    ).apply(instance, instance.stable(Noise::new)));

    public double getPerlin(SimplexNoise noise, double x, double y, double z) {
        double x2 = x / stretchXZ;
        double y2 = y / stretchY;
        double z2 = z / stretchXZ;

        double total = 0;
        double totalAmplitude = 0;
        for (Octave octave : octaves) {
            total += noise.getValue(x2 * octave.frequency, y2 * octave.frequency, z2 * octave.frequency) * octave.amplitude;
            totalAmplitude += octave.amplitude;
        }

        return total / totalAmplitude;
    }

    public double getPerlin(SimplexNoise noise, double x, double z) {
        double x2 = x / stretchXZ;
        double z2 = z / stretchXZ;

        double total = 0;
        double totalAmplitude = 0;
        for (Octave octave : octaves) {
            total += noise.getValue(x2 * octave.frequency, z2 * octave.frequency) * octave.amplitude;
            totalAmplitude += octave.amplitude;
        }

        total /= totalAmplitude;
        return total;
    }
}
