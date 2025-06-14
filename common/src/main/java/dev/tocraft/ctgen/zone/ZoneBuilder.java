package dev.tocraft.ctgen.zone;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@SuppressWarnings({"unused"})
public class ZoneBuilder {
    private Holder<Biome> biome;
    private int color;
    private int height = Zone.DEFAULT_HEIGHT;
    private double terrainModifier = Zone.DEFAULT_TERRAIN_MODIFIER;
    private double pixelWeight = Zone.DEFAULT_PIXEL_WEIGHT;

    public ZoneBuilder setBiome(Holder<Biome> biome) {
        this.biome = biome;
        return this;
    }

    public ZoneBuilder setColor(int color) {
        this.color = color;
        return this;
    }

    public ZoneBuilder setColor(@NotNull Color color) {
        this.color = color.getRGB();
        return this;
    }

    public ZoneBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    public ZoneBuilder setTerrainModifier(double terrainModifier) {
        this.terrainModifier = terrainModifier;
        return this;
    }

    public ZoneBuilder setPixelWeight(double pixelWeight) {
        this.pixelWeight = pixelWeight;
        return this;
    }

    public Zone build() {
        return new Zone(biome, color, height, terrainModifier, pixelWeight);
    }
}