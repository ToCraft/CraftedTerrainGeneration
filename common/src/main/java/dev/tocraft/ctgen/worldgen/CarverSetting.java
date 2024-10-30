package dev.tocraft.ctgen.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.util.Noise;

import java.util.List;

public record CarverSetting(Noise noise, double threshold) {
    public static final CarverSetting DEFAULT = new CarverSetting(new Noise(List.of(1F, 0.5F), 2, 63, 47), 0.55F);

    public static final Codec<CarverSetting> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Noise.CODEC.optionalFieldOf("noise", DEFAULT.noise).forGetter(CarverSetting::noise),
            Codec.DOUBLE.optionalFieldOf("threshold", DEFAULT.threshold).forGetter(CarverSetting::threshold)
    ).apply(instance, instance.stable(CarverSetting::new)));
}
