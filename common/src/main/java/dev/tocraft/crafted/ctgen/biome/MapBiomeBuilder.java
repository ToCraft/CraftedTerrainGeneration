package dev.tocraft.crafted.ctgen.biome;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import java.awt.*;
import java.util.Optional;

@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
public class MapBiomeBuilder {
    private Holder<Biome> biome;
    private int color;
    private int height = MapBiome.DEFAULT_HEIGHT;
    private double perlinMultiplier = MapBiome.DEFAULT_PERLIN_MULTIPLIER;
    private double pixelWeight = MapBiome.DEFAULT_PIXEL_WEIGHT;
    private Block deepslateBlock = MapBiome.DEFAULT_DEEPSLATE_BLOCK;
    private Block stoneBlock = MapBiome.DEFAULT_STONE_BLOCK;
    private Block dirtBlock = MapBiome.DEFAULT_DIRT_BLOCK;
    private Block surfaceBlock = MapBiome.DEFAULT_SURFACE_BLOCK;
    private Optional<Double> caveThreshold = Optional.empty();

    public MapBiomeBuilder setBiome(Holder<Biome> biome) {
        this.biome = biome;
        return this;
    }

    public MapBiomeBuilder setColor(int color) {
        this.color = color;
        return this;
    }

    public MapBiomeBuilder setColor(Color color) {
        this.color = color.getRGB();
        return this;
    }

    public MapBiomeBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    public MapBiomeBuilder setPerlinMultiplier(double perlinMultiplier) {
        this.perlinMultiplier = perlinMultiplier;
        return this;
    }

    public MapBiomeBuilder setPixelWeight(double pixelWeight) {
        this.pixelWeight = pixelWeight;
        return this;
    }

    public MapBiomeBuilder setDeepslateBlock(Block deepslateBlock) {
        this.deepslateBlock = deepslateBlock;
        return this;
    }

    public MapBiomeBuilder setStoneBlock(Block stoneBlock) {
        this.stoneBlock = stoneBlock;
        return this;
    }

    public MapBiomeBuilder setDirtBlock(Block dirtBlock) {
        this.dirtBlock = dirtBlock;
        return this;
    }

    public MapBiomeBuilder setSurfaceBlock(Block surfaceBlock) {
        this.surfaceBlock = surfaceBlock;
        return this;
    }

    public MapBiomeBuilder setCaveThreshold(double caveThreshold) {
        this.caveThreshold = Optional.of(caveThreshold);
        return this;
    }

    public MapBiome build() {
        return new MapBiome(biome, color, deepslateBlock, stoneBlock, dirtBlock, surfaceBlock, height, perlinMultiplier, pixelWeight, caveThreshold);
    }
}