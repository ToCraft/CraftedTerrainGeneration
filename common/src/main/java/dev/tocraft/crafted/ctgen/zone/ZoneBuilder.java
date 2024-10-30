package dev.tocraft.crafted.ctgen.zone;

import dev.tocraft.crafted.ctgen.xtend.placer.BasicPlacer;
import dev.tocraft.crafted.ctgen.xtend.placer.BlockPlacer;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
public class ZoneBuilder {
    private Holder<Biome> biome;
    private int color;
    private int height = Zone.DEFAULT_HEIGHT;
    private double terrainModifier = Zone.DEFAULT_TERRAIN_MODIFIER;
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
        return new Zone(biome, color, layers, height, terrainModifier, pixelWeight, thresholdModifier);
    }
}