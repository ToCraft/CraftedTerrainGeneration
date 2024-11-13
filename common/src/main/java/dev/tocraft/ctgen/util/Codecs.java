package dev.tocraft.ctgen.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public final class Codecs {
    public static final Codec<Integer> COLOR_RGB = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("red").forGetter(i -> (i >> 16) & 0xFF),
            Codec.INT.fieldOf("green").forGetter(i -> (i >> 8) & 0xFF),
            Codec.INT.fieldOf("blue").forGetter(i -> i & 0xFF)
    ).apply(instance, instance.stable((r, g, b) -> (0xFF << 24) |
            ((r & 0xFF) << 16) |
            ((g & 0xFF) << 8)  |
            ((b & 0xFF)))));

    public static final Codec<Integer> COLOR_HEX = Codec.STRING.xmap(s -> ((0xFF) << 24) |
            Integer.decode(s), i -> String.format("#%02x%02x%02x", (i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF));

    public static final Codec<Integer> COLOR = Codec.either(COLOR_HEX, COLOR_RGB)
            .xmap(either -> either.left().orElseGet(() -> either.right().orElseThrow()), Either::left);

    public static final Codec<Block> BLOCK = BuiltInRegistries.BLOCK.byNameCodec().stable();
}
