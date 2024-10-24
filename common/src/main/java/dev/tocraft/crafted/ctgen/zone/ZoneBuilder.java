package dev.tocraft.crafted.ctgen.zone;

import dev.tocraft.crafted.ctgen.blockplacer.BasicPlacer;
import dev.tocraft.crafted.ctgen.blockplacer.BlockPlacer;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
public class ZoneBuilder {
    private Holder<Biome> biome;
    private int color;
    private int height = Zone.DEFAULT_HEIGHT;
    private double perlinMultiplier = Zone.DEFAULT_PERLIN_MULTIPLIER;
    private double pixelWeight = Zone.DEFAULT_PIXEL_WEIGHT;
    private Map<String, BlockPlacer> layers = new HashMap<>();
    private Optional<Integer> thresholdModifier = Optional.empty();

    public ZoneBuilder setBiome(Holder<Biome> biome) {
        this.biome = biome;
        return this;
    }

    public ZoneBuilder setColor(int color) {
        this.color = color;
        return this;
    }

    public ZoneBuilder setColor(Color color) {
        this.color = color.getRGB();
        return this;
    }

    public ZoneBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    public ZoneBuilder setPerlinMultiplier(double perlinMultiplier) {
        this.perlinMultiplier = perlinMultiplier;
        return this;
    }

    public ZoneBuilder setPixelWeight(double pixelWeight) {
        this.pixelWeight = pixelWeight;
        return this;
    }

    public ZoneBuilder setLayers(Map<String, BlockPlacer> layers) {
        this.layers = layers;
        return this;
    }

    public ZoneBuilder putLayer(String layer, BlockPlacer placer) {
        this.layers.put(layer, placer);
        return this;
    }

    public ZoneBuilder setDeepslateBlock(Block block) {
        this.layers.put("deepslate", new BasicPlacer(block));
        return this;
    }

    public ZoneBuilder setStoneBlock(Block block) {
        this.layers.put("stone", new BasicPlacer(block));
        return this;
    }

    public ZoneBuilder setDirtBlock(Block block) {
        this.layers.put("dirt", new BasicPlacer(block));
        return this;
    }

    public ZoneBuilder setSurfaceBlock(Block block) {
        this.layers.put("surface", new BasicPlacer(block));
        return this;
    }

    public ZoneBuilder setThresholdModifier(int thresholdModifier) {
        this.thresholdModifier = Optional.of(thresholdModifier);
        return this;
    }

    public Zone build() {
        return new Zone(biome, color, layers, height, perlinMultiplier, pixelWeight, thresholdModifier);
    }
}