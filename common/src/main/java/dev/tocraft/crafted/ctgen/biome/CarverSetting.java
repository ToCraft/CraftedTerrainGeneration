package dev.tocraft.crafted.ctgen.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CarverSetting(int detail, int caveStretchXZ, int caveStretchY, double midThreshold, double bedrockThreshold, double entryThreshold) {
    public static final CarverSetting DEFAULT = new CarverSetting(3, 15, 7, 0.75, 1, 0.95);

    public static final Codec<CarverSetting> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.INT.optionalFieldOf("detail", DEFAULT.detail).forGetter(CarverSetting::detail),
            Codec.INT.optionalFieldOf("cave_stretch_xz", DEFAULT.caveStretchXZ).forGetter(CarverSetting::caveStretchXZ),
            Codec.INT.optionalFieldOf("cave_stretch_y", DEFAULT.caveStretchY).forGetter(CarverSetting::caveStretchY),
            Codec.DOUBLE.optionalFieldOf("mid_threshold", DEFAULT.midThreshold).forGetter(CarverSetting::midThreshold),
            Codec.DOUBLE.optionalFieldOf("bedrock_threshold", DEFAULT.bedrockThreshold).forGetter(CarverSetting::bedrockThreshold),
            Codec.DOUBLE.optionalFieldOf("entry_threshold", DEFAULT.entryThreshold).forGetter(CarverSetting::entryThreshold)
    ).apply(instance, instance.stable(CarverSetting::new)));
}
