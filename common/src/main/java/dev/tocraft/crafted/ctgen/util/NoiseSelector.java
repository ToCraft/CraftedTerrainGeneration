package dev.tocraft.crafted.ctgen.util;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class NoiseSelector<T> {
    @Nullable
    private final Noise noise;
    @NotNull
    private final Map<Double, T> thresholdMap;
    @NotNull
    private final T defaultValue;

    private NoiseSelector(T value) {
        this(null, new HashMap<>(), value);
    }

    private NoiseSelector(@Nullable Noise noise, @NotNull Map<Double,T> thresholdMap, @NotNull T defaultValue) {
        this.noise = noise;
        this.thresholdMap = thresholdMap;
        this.defaultValue = defaultValue;
    }

    @Contract("_ -> new")
    public static <T> @NotNull NoiseSelector<T> of(@NotNull T value) {
        return new NoiseSelector<>(value);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static <T> @NotNull NoiseSelector<T> of(Noise noise, Map<Double, T> thresholdMap, T value) {
        return new NoiseSelector<>(noise, thresholdMap, value);
    }

    public boolean hasNoise() {
        return noise != null && !thresholdMap.isEmpty();
    }

    public T get(SimplexNoise noise, double x, double y, double z) {
        double perlin;

        if (this.noise != null && hasNoise()) {
            perlin = this.noise.getPerlin(noise, x, y, z);
        } else  {
            perlin = 0;
        }

        double lastThreshold = -2;
        T value = defaultValue;
        for (Map.Entry<Double, T> entry : thresholdMap.entrySet()) {
            if (lastThreshold < entry.getKey() && entry.getKey() <= perlin) {
                value = entry.getValue();
                lastThreshold = entry.getKey();
            }
        }

        return value;
    }

    public T get(SimplexNoise noise, double x, double z) {
        double perlin;

        if (this.noise != null && hasNoise()) {
            perlin = this.noise.getPerlin(noise, x, z);
        } else  {
            perlin = 0;
        }

        double lastThreshold = -2;
        T value = defaultValue;
        for (Map.Entry<Double, T> entry : thresholdMap.entrySet()) {
            if (lastThreshold < entry.getKey() && entry.getKey() <= perlin) {
                value = entry.getValue();
                lastThreshold = entry.getKey();
            }
        }

        return value;
    }

    public static <T> Codec<NoiseSelector<T>> codec(Codec<T> typeCodec) {
        Codec<Map<Double, T>> mapCodec = Codec.unboundedMap(Codec.STRING, typeCodec).xmap(
                map -> {
                    Map<Double, T> doubleMap = new HashMap<>();
                    for (Map.Entry<String, T> entry : map.entrySet()) {
                        doubleMap.put(Double.parseDouble(entry.getKey()), entry.getValue());
                    }
                    return doubleMap;
                },
                map -> {
                    Map<String, T> stringMap = new HashMap<>();
                    for (Map.Entry<Double, T> entry : map.entrySet()) {
                        stringMap.put(String.valueOf((double) entry.getKey()), entry.getValue());
                    }
                    return stringMap;
                }
        );

        Codec<NoiseSelector<T>> directCodec = RecordCodecBuilder.create(instance -> instance.group(
                Noise.CODEC.optionalFieldOf("noise", Noise.DEFAULT).forGetter(o -> o.noise),
                mapCodec.optionalFieldOf("values", new HashMap<>()).forGetter(o -> o.thresholdMap),
                typeCodec.fieldOf("default").forGetter(o -> o.defaultValue)
        ).apply(instance, instance.stable(NoiseSelector::new)));

        return Codec.either(typeCodec, directCodec)
                .xmap(either -> either.right().orElseGet(() -> of(either.left().orElseThrow())), selector -> selector.hasNoise() ? Either.right(selector) : Either.left(selector.defaultValue));
    }
}
