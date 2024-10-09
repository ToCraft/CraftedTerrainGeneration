package dev.tocraft.crafted.ctgen.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CarverSetting(float occurrences, int radius, int minLength, int maxLength) {
    public static final CarverSetting DEFAULT = new CarverSetting(0.25F, 3, 50, 100);

    public static final Codec<CarverSetting> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.FLOAT.optionalFieldOf("occurrences", DEFAULT.occurrences).forGetter(CarverSetting::occurrences),
            Codec.INT.optionalFieldOf("radius", DEFAULT.radius).forGetter(CarverSetting::radius),
            Codec.INT.optionalFieldOf("min_length", DEFAULT.minLength).forGetter(CarverSetting::minLength),
            Codec.INT.optionalFieldOf("max_length", DEFAULT.minLength).forGetter(CarverSetting::maxLength)
    ).apply(instance, instance.stable(CarverSetting::new)));
}
