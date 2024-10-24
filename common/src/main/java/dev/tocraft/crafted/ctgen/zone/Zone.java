package dev.tocraft.crafted.ctgen.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.blockplacer.BlockPlacer;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record Zone(Holder<Biome> biome, int color, Map<String, BlockPlacer> layers, int height, double perlinMultiplier,
                   double pixelWeight,
                   Optional<Integer> thresholdModifier) {

    public static final int DEFAULT_HEIGHT = 0;
    public static final double DEFAULT_PERLIN_MULTIPLIER = 8;
    public static final double DEFAULT_PIXEL_WEIGHT = 1;

    public static final Codec<Zone> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Biome.CODEC.fieldOf("biome").forGetter(Zone::biome),
            Codecs.COLOR.fieldOf("color").forGetter(Zone::color),
            Codec.unboundedMap(Codec.STRING, BlockPlacer.CODEC).optionalFieldOf("layers", new HashMap<>()).forGetter(Zone::layers),
            Codec.INT.optionalFieldOf("height", DEFAULT_HEIGHT).forGetter(Zone::height),
            Codec.DOUBLE.optionalFieldOf("perlin_multiplier", DEFAULT_PERLIN_MULTIPLIER).forGetter(Zone::perlinMultiplier),
            Codec.DOUBLE.optionalFieldOf("pixel_weight", DEFAULT_PIXEL_WEIGHT).forGetter(Zone::pixelWeight),
            Codec.INT.optionalFieldOf("threshold_modifier").forGetter(Zone::thresholdModifier)
    ).apply(instance, instance.stable(Zone::new)));

    public static RegistryFileCodec<Zone> CODEC = RegistryFileCodec.create(CTerrainGeneration.MAP_ZONES_REGISTRY, DIRECT_CODEC);
}
