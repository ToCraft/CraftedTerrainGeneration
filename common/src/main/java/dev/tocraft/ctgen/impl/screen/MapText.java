package dev.tocraft.ctgen.impl.screen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record MapText(int x,
                      int y,
                      float size,
                      float minZoom,
                      float maxZoom,
                      float rotation,
                      Component text
) {
    public static final Codec<MapText> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(MapText::x),
            Codec.INT.fieldOf("y").forGetter(MapText::y),
            Codec.FLOAT.optionalFieldOf("size", 50f).forGetter(MapText::size),
            Codec.FLOAT.fieldOf("min_zoom").orElse(-1f).forGetter(MapText::minZoom),
            Codec.FLOAT.fieldOf("max_zoom").orElse(-1f).forGetter(MapText::maxZoom),
            Codec.FLOAT.fieldOf("rotation").orElse(0f).forGetter(MapText::rotation),
            ComponentSerialization.CODEC.fieldOf("text").forGetter(MapText::text)
    ).apply(instance, MapText::new));
}