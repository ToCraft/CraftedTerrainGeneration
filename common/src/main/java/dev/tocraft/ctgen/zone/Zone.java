package dev.tocraft.ctgen.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.util.Codecs;
import dev.tocraft.ctgen.xtend.CTRegistries;
import dev.tocraft.ctgen.xtend.placer.BlockPlacer;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;

public record Zone(Holder<Biome> biome, int color, Map<String, BlockPlacer> layers, int height, double terrainModifier,
                   double pixelWeight) {

    public static final int DEFAULT_HEIGHT = 0;
    public static final double DEFAULT_TERRAIN_MODIFIER = 8;
    public static final double DEFAULT_PIXEL_WEIGHT = 1;

    public static final Codec<Zone> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Biome.CODEC.fieldOf("biome").forGetter(Zone::biome),
            Codecs.COLOR.fieldOf("color").forGetter(Zone::color),
            Codec.unboundedMap(Codec.STRING, BlockPlacer.CODEC).optionalFieldOf("layers", new HashMap<>()).forGetter(Zone::layers),
            Codec.INT.optionalFieldOf("height", DEFAULT_HEIGHT).forGetter(Zone::height),
            Codec.DOUBLE.optionalFieldOf("terrain_modifier", DEFAULT_TERRAIN_MODIFIER).forGetter(Zone::terrainModifier),
            Codec.DOUBLE.optionalFieldOf("pixel_weight", DEFAULT_PIXEL_WEIGHT).forGetter(Zone::pixelWeight)
    ).apply(instance, instance.stable(Zone::new)));

    public static RegistryFileCodec<Zone> CODEC = RegistryFileCodec.create(CTRegistries.ZONES_KEY, DIRECT_CODEC);
}
