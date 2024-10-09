package dev.tocraft.crafted.ctgen.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record CarverSetting(List<Float> octaves, int caveStretchXZ, int caveStretchY, double threshold) {
    public static final CarverSetting DEFAULT = new CarverSetting(List.of(1F, 0.5F), 63, 47, 0.55F);

    public static final Codec<CarverSetting> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.list(Codec.FLOAT).optionalFieldOf("octaves", DEFAULT.octaves).forGetter(CarverSetting::octaves),
            Codec.INT.optionalFieldOf("cave_stretch_xz", DEFAULT.caveStretchXZ).forGetter(CarverSetting::caveStretchXZ),
            Codec.INT.optionalFieldOf("cave_stretch_y", DEFAULT.caveStretchY).forGetter(CarverSetting::caveStretchY),
            Codec.DOUBLE.optionalFieldOf("threshold", DEFAULT.threshold).forGetter(CarverSetting::threshold)
    ).apply(instance, instance.stable(CarverSetting::new)));
}
