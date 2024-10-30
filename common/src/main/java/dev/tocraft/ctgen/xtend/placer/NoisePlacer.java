package dev.tocraft.ctgen.xtend.placer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.util.Noise;
import dev.tocraft.ctgen.zone.Codecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class NoisePlacer extends BlockPlacer {
    @Nullable
    private final Noise noise;
    @NotNull
    private final Map<Double, Block> thresholdMap;
    @NotNull
    private final Block defaultValue;

    private NoisePlacer(@Nullable Noise noise, @NotNull Map<Double, Block> thresholdMap, @NotNull Block defaultValue) {
        this.noise = noise;
        this.thresholdMap = thresholdMap;
        this.defaultValue = defaultValue;
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull NoisePlacer of(Noise noise, Map<Double, Block> thresholdMap, Block value) {
        return new NoisePlacer(noise, thresholdMap, value);
    }

    public boolean hasNoise() {
        return noise != null && !thresholdMap.isEmpty();
    }

    public @NotNull Block get(SimplexNoise noise, double x, double y, double z, String layer) {
        double perlin;

        if (this.noise != null && hasNoise()) {
            perlin = this.noise.getPerlin(noise, x, y, z);
        } else {
            perlin = 0;
        }

        double lastThreshold = -2;
        Block value = defaultValue;
        for (Map.Entry<Double, Block> entry : thresholdMap.entrySet()) {
            if (lastThreshold < entry.getKey() && entry.getKey() <= perlin) {
                value = entry.getValue();
                lastThreshold = entry.getKey();
            }
        }

        return value;
    }

    @Override
    public @NotNull Block get(SimplexNoise noise, double x, double z, String layer) {
        double perlin;

        if (this.noise != null && hasNoise()) {
            perlin = this.noise.getPerlin(noise, x, z);
        } else {
            perlin = 0;
        }

        double lastThreshold = -2;
        Block value = defaultValue;
        for (Map.Entry<Double, Block> entry : thresholdMap.entrySet()) {
            if (lastThreshold < entry.getKey() && entry.getKey() <= perlin) {
                value = entry.getValue();
                lastThreshold = entry.getKey();
            }
        }

        return value;
    }

    private static final Codec<Map<Double, Block>> VALUE_MAP_CODEC = Codec.unboundedMap(Codec.STRING, Codecs.BLOCK).xmap(
            map -> {
                Map<Double, Block> doubleMap = new HashMap<>();
                for (Map.Entry<String, Block> entry : map.entrySet()) {
                    doubleMap.put(Double.parseDouble(entry.getKey()), entry.getValue());
                }
                return doubleMap;
            },
            map -> {
                Map<String, Block> stringMap = new HashMap<>();
                for (Map.Entry<Double, Block> entry : map.entrySet()) {
                    stringMap.put(String.valueOf((double) entry.getKey()), entry.getValue());
                }
                return stringMap;
            }
    );

    public static final Codec<NoisePlacer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Noise.CODEC.optionalFieldOf("noise", Noise.DEFAULT).forGetter(o -> o.noise),
            VALUE_MAP_CODEC.fieldOf("values").forGetter(o -> o.thresholdMap),
            Codecs.BLOCK.fieldOf("default").forGetter(o -> o.defaultValue)
    ).apply(instance, instance.stable(NoisePlacer::new)));

    public static final ResourceLocation ID = CTerrainGeneration.id("noise");

    @Override
    public Codec<NoisePlacer> codec() {
        return CODEC;
    }
}
