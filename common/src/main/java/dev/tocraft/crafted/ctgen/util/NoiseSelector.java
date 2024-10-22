package dev.tocraft.crafted.ctgen.util;

import com.mojang.datafixers.util.Either;
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

    public NoiseSelector(T value) {
        this(null, new HashMap<>(), value);
    }

    public NoiseSelector(@Nullable Noise noise, @NotNull Map<Double,T> thresholdMap, @NotNull T defaultValue) {
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
        return noise != null;
    }

    public T get(SimplexNoise noise, double x, double y, double z) {
        double perlin;

        if (this.noise != null) {
            perlin = this.noise.getPerlin(noise, x, y, z);
        } else  {
            perlin = 0;
        }

        T value = defaultValue;
        for (Map.Entry<Double, T> entry : thresholdMap.entrySet()) {
            if (entry.getKey() <= perlin) {
                value = entry.getValue();
            }
        }

        return value;
    }

    public static <T> Codec<NoiseSelector<T>> codec(Codec<T> typeCodec) {
        Codec<NoiseSelector<T>> directCodec = RecordCodecBuilder.create(instance -> instance.group(
                Noise.CODEC.optionalFieldOf("noise", null).forGetter(o -> o.noise),
                Codec.unboundedMap(Codec.DOUBLE, typeCodec).optionalFieldOf("values", new HashMap<>()).forGetter(o -> o.thresholdMap),
                typeCodec.fieldOf("default").forGetter(o -> o.defaultValue)
        ).apply(instance, instance.stable(NoiseSelector::new)));

        return Codec.either(typeCodec, directCodec)
                .xmap(either -> either.right().orElseGet(() -> of(either.left().orElseThrow())), selector -> selector.hasNoise() ? Either.right(selector) : Either.left(selector.defaultValue));
    }
}
