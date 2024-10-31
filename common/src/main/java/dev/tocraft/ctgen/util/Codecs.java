package dev.tocraft.ctgen.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.awt.*;

public final class Codecs {
    public static final Codec<Color> COLOR_DIRECT = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("r").forGetter(Color::getRed),
            Codec.INT.fieldOf("g").forGetter(Color::getGreen),
            Codec.INT.fieldOf("b").forGetter(Color::getBlue)
    ).apply(instance, instance.stable(Color::new)));

    public static final Codec<Integer> COLOR = Codec.either(COLOR_DIRECT, Codec.INT)
            .xmap(either -> either.right().orElseGet(() -> either.left().orElseThrow().getRGB()), rgb -> {
                Color color = new Color(rgb);
                if (color.getAlpha() < 255) {
                    return Either.right(rgb);
                } else {
                    return Either.left(color);
                }
            });

    public static final Codec<Block> BLOCK = BuiltInRegistries.BLOCK.byNameCodec().stable();
}
