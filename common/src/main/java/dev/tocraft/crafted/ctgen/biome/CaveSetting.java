package dev.tocraft.crafted.ctgen.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CaveSetting(double midThreshold, double bedrockThreshold, double entryThreshold) {
    public static final CaveSetting DEFAULT = new CaveSetting(0.75, 1, 1);

    public static final Codec<CaveSetting> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.DOUBLE.optionalFieldOf("mid_threshold", DEFAULT.midThreshold).forGetter(CaveSetting::midThreshold),
            Codec.DOUBLE.optionalFieldOf("bedrock_threshold", DEFAULT.bedrockThreshold).forGetter(CaveSetting::bedrockThreshold),
            Codec.DOUBLE.optionalFieldOf("entry_threshold", DEFAULT.entryThreshold).forGetter(CaveSetting::entryThreshold)
    ).apply(instance, instance.stable(CaveSetting::new)));
}
