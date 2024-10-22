package dev.tocraft.crafted.ctgen.blockplacer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.util.Noise;
import dev.tocraft.crafted.ctgen.zone.Codecs;
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

    private NoisePlacer(Block value) {
        this(null, new HashMap<>(), value);
    }

    private NoisePlacer(@Nullable Noise noise, @NotNull Map<Double,Block> thresholdMap, @NotNull Block defaultValue) {
        this.noise = noise;
        this.thresholdMap = thresholdMap;
        this.defaultValue = defaultValue;
    }

    @Contract("_ -> new")
    public static @NotNull NoisePlacer of(@NotNull Block value) {
        return new NoisePlacer(value);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull NoisePlacer of(Noise noise, Map<Double, Block> thresholdMap, Block value) {
        return new NoisePlacer(noise, thresholdMap, value);
    }

    public boolean hasNoise() {
        return noise != null && !thresholdMap.isEmpty();
    }

    public Block get(SimplexNoise noise, double x, double y, double z) {
        double perlin;

        if (this.noise != null && hasNoise()) {
            perlin = this.noise.getPerlin(noise, x, y, z);
        } else  {
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
    public Block get(SimplexNoise noise, double x, double z) {
        double perlin;

        if (this.noise != null && hasNoise()) {
            perlin = this.noise.getPerlin(noise, x, z);
        } else  {
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

    public static final Codec<NoisePlacer> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Noise.CODEC.optionalFieldOf("noise", Noise.DEFAULT).forGetter(o -> o.noise),
            VALUE_MAP_CODEC.optionalFieldOf("values", new HashMap<>()).forGetter(o -> o.thresholdMap),
            Codecs.BLOCK.fieldOf("default").forGetter(o -> o.defaultValue)
    ).apply(instance, instance.stable(NoisePlacer::new)));

    public static final Codec<NoisePlacer> CODEC = Codec.either(Codecs.BLOCK, DIRECT_CODEC)
            .xmap(either -> either.right().orElseGet(() -> of(either.left().orElseThrow())), selector -> selector.hasNoise() ? Either.right(selector) : Either.left(selector.defaultValue));

    @Override
    public Codec<NoisePlacer> codec() {
        return CODEC;
    }
}
